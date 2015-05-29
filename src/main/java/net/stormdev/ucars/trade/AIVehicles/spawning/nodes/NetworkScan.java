package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.md_5.bungee.api.ChatColor;
import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.AITrackFollow;
import net.stormdev.ucars.trade.AIVehicles.DynamicLagReducer;
import net.stormdev.ucars.trade.AIVehicles.spawning.SpawnMethod;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

public class NetworkScan { //TODO Should probably also check against other active nodes in chunks overlapping so that re-running the scan won't double the nodes (Will allow for partial scans if/when implemented)
	private static int SCAN_BRANCH_LIMIT = 2500;
	public static class Logger {
		private UUID startPlayerUUID;
		
		public Logger(Player starter){
			startPlayerUUID = starter.getUniqueId();
		}
		
		public void log(String message){
			main.plugin.getLogger().info(message);
			Player player = Bukkit.getPlayer(startPlayerUUID);
			if(player != null){ //They're still online
				player.sendMessage(ChatColor.RED+"[RoadNetworkScan]:"+ChatColor.RESET+message);
			}
		}
	}
	
	public static enum Stage {
		REVALIDATE_EXISTING_NODES(0),
		SCAN_ROAD_NETWORK_BLOCKS(1),
		PLACE_NODES(2),
		/*CHECK_NODES(3),*/
		SAVE_AND_ACTIVATE(3);
		
		private int pos;
		private Stage(int pos){
			this.pos = pos;
		}
		
		public Stage getNext(){
			int nextPos = pos+1;
			return getStage(nextPos); //Null if no next stage
		}
		
		private static Stage getStage(int pos){
			for(Stage s:values()){
				if(s.pos == pos){
					return s;
				}
			}
			return null;
		}
	}
	
	private Logger logger = null;
	private Location origin = null;
	private Stage stage = Stage.REVALIDATE_EXISTING_NODES;
	private AINodesSpawnManager spawnManager = null;
	private volatile List<Block> roadNetwork = new ArrayList<Block>();
	private volatile List<Node> nodes = new ArrayList<Node>();
	private volatile long REST_TIME = 50; //Rest time between calculations
	private volatile BukkitTask restTimeChecker = null;
	
	public NetworkScan(Player player){ //Constructed and called async
		if(Bukkit.isPrimaryThread()){
			throw new RuntimeException("Don't use in main thread");
		}
		
		this.logger = new Logger(player);
		logger.log("Detecting road network...");
		
		final Location playerLoc = player.getLocation();
		Location startTrackerBlock = null;
		
		Future<Location> trackerBlockFind = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<Location>(){

			@Override
			public Location call() throws Exception {
				for(int i=0;i<5;i++){
					Block under = playerLoc.getBlock().getRelative(BlockFace.DOWN, i);
					if(AIRouter.isTrackBlock(under.getType())){
						return under.getLocation();
					}
				}
				return null;
			}});
		try {
			startTrackerBlock = trackerBlockFind.get();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(startTrackerBlock == null){
			logger.log("Unable to detect road network! Scan cancelled! Please make sure you're standing on it!");
			return;
		}
		
		origin = startTrackerBlock;
		logger.log("Successfully found road network's entry point!");
		
		if(!(main.plugin.aiSpawns instanceof AINodesSpawnManager)){
			logger.log("Switching to node based spawn system... (No cars will spawn until nodes are setup though)");
			main.plugin.aiSpawns.shutdown();
			main.plugin.initNodeAISpawnManager();
		}
		spawnManager = (AINodesSpawnManager) main.plugin.aiSpawns;
		
		restTimeChecker = Bukkit.getScheduler().runTaskTimerAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				double tps = DynamicLagReducer.getTPS();
				if(tps > 18){
					if(REST_TIME > 10){
						REST_TIME -= 10;
					}
					if(REST_TIME < 10){
						REST_TIME = 10;
					}
				}
				else if(tps > 14){
					if(REST_TIME > 10){
						REST_TIME -= 5;
					}
					if(REST_TIME < 10){
						REST_TIME = 10;
					}
				}
				else {
					REST_TIME += 20;
					if(REST_TIME > 200){
						REST_TIME = 200;
					}
				}
				return;
			}}, 20l, 20l);
		startStage();
	}
	
	private void nextStage(){
		stage = stage.getNext();
		if(stage == null){
			finish();
		}
		else {
			startStage();
		}
	}
	
	public void finish(){
		spawnManager.getNodesStore().asyncSave();
		origin = null;
		spawnManager = null;
		roadNetwork.clear();
		roadNetwork = null;
		nodes.clear();
		nodes = null;
		logger.log("Network scanning terminated!");
		logger = null;
		System.gc(); //Try and get java to garbage collect all the junk now it's done with
	}
	
	private void startStage(){
		switch(stage){
		/*case CHECK_NODES: {
			checkNodes();
			nextStage();
		}*/
			/*break;*/
		case PLACE_NODES: {
			nodePlacing();
			nextStage();
		}
			break;
		case REVALIDATE_EXISTING_NODES: {
			rescanNodes();
			nextStage();
		}
			break;
		case SAVE_AND_ACTIVATE: {
			saveNodes();
			nextStage();
		}
			break;
		case SCAN_ROAD_NETWORK_BLOCKS: {
			scanRoadNetwork();
			nextStage();
		}
			break;
		default:
			break;
		}
	}
	
	public void saveNodes(){
		logger.log("Saving "+nodes.size()+" nodes into the correct chunks for them to be 'active' inside of (5x5 chunk grid with node at center FYI)...");
		
		for(Node node:nodes){
			spawnManager.getNodesStore().setNodeIntoCorrectActiveChunks(node);
		}
		
		main.config.set("general.ai.spawnMethod", SpawnMethod.NODES.name());
		main.plugin.saveConfig();
		
		logger.log("Successfully saved the nodes! Villager cars should now start spawning!");
	}
	
	public void checkNodes(){
		logger.log("Starting validation of placed nodes, this could also take a long time...");
		
		int removed = 0;
		for(final Node node:new ArrayList<Node>(nodes)){
			Future<Boolean> validation = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<Boolean>(){

				@Override
				public Boolean call() throws Exception {
					if(!node.isValid()){
						return false;
					}
					
					for(Node otherNode:new ArrayList<Node>(nodes)){
						if(otherNode.equals(node)){
							continue; //SAME node
						}
						double distanceSquared = otherNode.getLocation().distanceSquared(node.getLocation());
						if(distanceSquared <= 48){ //Nodes too close together!
							BlockFace n1Dir = node.getCarriagewayDirection();
							BlockFace n2Dir = otherNode.getCarriagewayDirection();
							if(!n1Dir.getOppositeFace().equals(n2Dir)){ //Allow close nodes if on different sides of the road
								return false;
							}
						}
					}
					return true;
				}});
			try {
				if(!validation.get()){
					nodes.remove(node);
					removed++;
				}
			} catch (Exception e) {
				//uh oh
				e.printStackTrace();
			}
			Thread.yield();
			try {
				Thread.sleep(REST_TIME);
			} catch (InterruptedException e) {
				//kk
			} //Give it time to recover
		}
		
		logger.log("Validated placed nodes successfully! "+removed+" nodes were removed! There are "+nodes.size()+" valid nodes on this network!");
	}
	
	private volatile int roughNodes = 0;
	public void nodePlacing(){
		logger.log("Starting placing of nodes throughout the network, this could also take a long time...");
		
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				while(stage.equals(Stage.PLACE_NODES)){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						//oh well
					}
					logger.log("Gone through "+roughNodes+" out of "+roughSize+" road blocks...");
				}
				return;
			}});
		
		for(final Block block:new ArrayList<Block>(roadNetwork)){
			roughNodes++;
			if(block == null){
				continue;
			}
			Vector blockLoc = new Vector(block.getX(), block.getY(), block.getZ());
			boolean overlappingNode = false;
			Future<BlockFace> getDir = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<BlockFace>(){

				@Override
				public BlockFace call() throws Exception {
					return AITrackFollow.carriagewayDirection(block);
				}});
			BlockFace dir = null;
			try {
				dir = getDir.get();
			} catch (Exception e1) {
				//Uh oh
				e1.printStackTrace();
			}
			
			if(dir == null){ //No direction establishable, skip
				continue;
			}
			
			for(final Node node:nodes){
				if(node.getLocation() == null){
					continue;
				}
				Vector nodeLoc = new Vector(node.getLocation().getX(), node.getLocation().getY(), node.getLocation().getZ());
				Vector diff = nodeLoc.subtract(blockLoc);
				if(diff.lengthSquared() < 49){ //There is another node within 7 blocks of this locations, check if we should skip it
					Future<BlockFace> existingNodeDir = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<BlockFace>(){

						@Override
						public BlockFace call() throws Exception {
							return node.getCarriagewayDirection();
						}});
					try {
						BlockFace existingNodeCarriageway = existingNodeDir.get();
						if(!existingNodeCarriageway.getOppositeFace().equals(dir)){ //if it is opposite, then it's the opposite carriageway so we're ok to place a node
							overlappingNode = true;
							break;
						}
					} catch (Exception e) {
						//Uh oh
						e.printStackTrace();
					}
				}
			}
			if(overlappingNode){
				continue;
			}
			//Place a node here!
			nodes.add(new Node(block.getLocation()));
			Thread.yield();
			try {
				Thread.sleep(REST_TIME); //Give the main thread a break from being spammed to calculate stuff
			} catch (InterruptedException e) {
				//Uh oh
				e.printStackTrace();
			}
		}
		
		logger.log("Nodes distributed throughout the network successfully! There are now "+nodes.size()+" nodes placed!");
	}
	
	public void rescanNodes(){
		logger.log("Starting revalidating of existing nodes, this may take a short while...");
		spawnManager.getNodesStore().revalidateNodesNow();
		logger.log("Existing nodes successfully revalidated!");
	}
	
	public void scanRoadNetwork(){
		if(roadNetwork == null){
			roadNetwork = new ArrayList<Block>();
		}
		roadNetwork.clear(); //In case it isn't already
		logger.log("Starting indexing of the road network... (This could take a long time)");
		
		blockScan(origin.getBlock());
		
		while((countScanBranches() > 0) && (System.currentTimeMillis() - lastStartTime) < 120000){ //120 second timeout if no more blocks are scanned within it (May cause task to stop premature if server lags extremely)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//oh well
			}
			logger.log("Currently active scan branches: "+scansRunning+" Queued extra branches: "+queuedBranches.size()+" Current network size: "+roughSize);
		}
		
		logger.log("Road network indexed!");
	}
	
	private int countScanBranches(){
		if(queuedBranches.size() > 0 && scansRunning < SCAN_BRANCH_LIMIT){
			int toStart = SCAN_BRANCH_LIMIT - scansRunning;
			if(toStart > queuedBranches.size()){
				toStart = queuedBranches.size();
			}
			for(int i=0;i<toStart && scansRunning<SCAN_BRANCH_LIMIT;i++){
				final Block b = queuedBranches.get(0);
				queuedBranches.remove(0);
				incrementScansRunning();
				Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

					@Override
					public void run() {
						blockScan(b);
						return;
					}});
				Thread.yield();
				try {
					Thread.sleep(REST_TIME); //Prevent starting LOTS of branches at one time
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		if(queuedBranches.size() > 0 && scansRunning < 1){
			return 1; //Don't stop the scan...
		}
		return scansRunning;
	}
	
	private volatile List<Block> queuedBranches = new ArrayList<Block>();
	
	private volatile long lastStartTime = 0;
	private volatile int scansRunning = 1;
	private volatile int roughSize = 0; //rough because of thread synchronizing
	
	private synchronized void incrementScansRunning(){
		scansRunning++;
	}
	
	private synchronized void decrementScansRunning(){
		scansRunning--;
	}
	
	private void blockScan(final Block block){
		lastStartTime = System.currentTimeMillis();
		if(block == null){
			return;
		}
		if(scansRunning > NetworkScan.SCAN_BRANCH_LIMIT){
			queuedBranches.add(block);
			decrementScansRunning();
			return;
		}
		try {
			Thread.yield();
			Thread.sleep(REST_TIME); //Give the main thread a rest occasionally so the server hopefully doesn't crash
			roughSize++;
			roadNetwork.add(block);
			//Now check for nearby tracker blocks
			Future<Boolean> moreStarted = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<Boolean>(){

				@Override
				public Boolean call() {
					boolean startedMore = false;
					for(int modY=-1;modY<=1;modY++){
						for(int modX=-1;modX<=1;modX++){
							for(int modZ=-1;modZ<=1;modZ++){
								try {
									final Block newBlock = new Location(block.getWorld(), block.getX()+modX, block.getY()+modY, block.getZ()+modZ).getBlock();
									if(AIRouter.isTrackBlock(newBlock.getType()) && !roadNetwork.contains(newBlock)){
										final boolean originalScan = !startedMore;
										startedMore = true;
										Bukkit.getScheduler().runTaskLaterAsynchronously(main.plugin, new Runnable(){

											@Override
											public void run() {
												if(!originalScan){
													incrementScansRunning();
												}
												blockScan(newBlock);
												return;
											}}, 1l);
									}
								} catch (Exception e) {
									//Invalid block location maybe?
									e.printStackTrace();
								}
							}
						}
					}
					return startedMore;
				}});
			if(!moreStarted.get()){ //Block on the near blocks being scanned (So it's easier to tell if we're done or not)
				decrementScansRunning();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
