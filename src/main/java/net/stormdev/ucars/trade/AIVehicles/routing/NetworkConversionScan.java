package net.stormdev.ucars.trade.AIVehicles.routing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.md_5.bungee.api.ChatColor;
import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.AITrackFollow;
import net.stormdev.ucars.trade.AIVehicles.DynamicLagReducer;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class NetworkConversionScan { 
	private static int SCAN_BRANCH_LIMIT = 1000;
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
		/*REVALIDATE_EXISTING_NODES(0),*/
		SCAN_ROAD_NETWORK_BLOCKS(1),
		REPLACE_ROAD_NETWORK_BLOCKS(2);
		/*CHECK_NODES(3),*/
		
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
	private Stage stage = Stage.SCAN_ROAD_NETWORK_BLOCKS;
	private volatile Map<Block, BlockRouteData> roadNetwork = new HashMap<Block, BlockRouteData>();
	/*private volatile List<Node> nodes = new ArrayList<Node>();*/
	private volatile long REST_TIME = 50; //Rest time between calculations
	private volatile BukkitTask restTimeChecker = null;
	
	public NetworkConversionScan(Player player){ //Constructed and called async
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
		
		restTimeChecker = Bukkit.getScheduler().runTaskTimerAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				double tps = DynamicLagReducer.getTPS();
				if(tps > 18){
					if(REST_TIME > 10){
						REST_TIME -= 10;
					}
					if(REST_TIME < 0){
						REST_TIME = 0;
					}
				}
				else if(tps > 14){
					if(REST_TIME > 10){
						REST_TIME -= 5;
					}
					if(REST_TIME < 2){
						REST_TIME = 2;
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
	
	private void replaceRoadNetwork(){
		if(roadNetwork == null){
			return;
		}
		
		int prevPercent = -1;
		
		int i=0;
		for(Entry<Block, BlockRouteData> blockLoc:roadNetwork.entrySet()){
			i++;
			sleep();
			BlockRouteData brd = blockLoc.getValue();
			final Block bl = blockLoc.getKey();
			final int data = RouteDecoder.getDataFromDir(brd.getType(), brd.getDirection());
			try {
				Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<Void>(){

					@Override
					public Void call() throws Exception {
						bl.setType(Material.STAINED_GLASS);
						bl.setData((byte) data);
						return null;
					}}).get();
			} catch (Exception e) {
				e.printStackTrace();
			}
			int percent = (int) Math.floor((i/((double)roughSize)) * 100.0d);
			if(percent != prevPercent){
				prevPercent = percent;
				logger.log("Replacing control blocks "+percent+"%!");
			}
		}
		logger.log("Block replacing complete!");
	}
	
	public void finish(){
		origin = null;
		main.plugin.aiRouteMethod = RouteMethod.ENCODED;
		main.config.set("general.ai.routing", RouteMethod.ENCODED.name());
		main.plugin.saveConfig();
		origin.getWorld().save();
		roadNetwork.clear();
		roadNetwork = null;
		restTimeChecker.cancel();
		/*nodes.clear();
		nodes = null;*/
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
		case REPLACE_ROAD_NETWORK_BLOCKS: {
			replaceRoadNetwork();
			nextStage();
		}
			break;
		/*case REVALIDATE_EXISTING_NODES: {
			rescanNodes();
			nextStage();
		}*/
			/*break;*/
		case SCAN_ROAD_NETWORK_BLOCKS: {
			scanRoadNetwork();
			nextStage();
		}
			break;
		default:
			break;
		}
	}
	
	/*public void saveNodes(){
		logger.log("Saving "+nodes.size()+" nodes into the correct chunks for them to be 'active' inside of (5x5 chunk grid with node at center FYI)...");
		
		for(Node node:nodes){
			spawnManager.getNodesStore().setNodeIntoCorrectActiveChunks(node);
		}
		
		spawnManager.getNodesStore().asyncSave();
		
		main.config.set("general.ai.spawnMethod", SpawnMethod.NODES.name());
		main.plugin.saveConfig();
		
		logger.log("Successfully saved the nodes! Villager cars should now start spawning!");
	}*/
	
	/*public void checkNodes(){ //Not used anymore... (Waste of processing)
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
						if(distanceSquared <= 49){ //Nodes too close together!
							BlockFace n1Dir = node.getCarriagewayDirection();
							BlockFace n2Dir = otherNode.getCarriagewayDirection();
							if(!n1Dir.getOppositeFace().equals(n2Dir)){ //Allow close nodes if on different sides of the road
								double absModX = Math.abs(diff.getX()); //If re-implemented, diff is the vector to get from the first node to the second
								double absModZ = Math.abs(diff.getZ());
								if(absModX < 2 || absModZ < 2){ //Check if on same carriageway (Breaks at corners, but is accurate enough)
									return false;
								}
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
	}*/
	
	private void sleep(){
		Thread.yield();
		if(REST_TIME > 0){
			try {
				Thread.sleep(REST_TIME); //Give the main thread a break from being spammed to calculate stuff
			} catch (InterruptedException e) {
				//Uh oh
				e.printStackTrace();
			}
		}
	}
	
	public void scanRoadNetwork(){
		if(roadNetwork == null){
			roadNetwork = new HashMap<Block, BlockRouteData>();
		}
		roadNetwork.clear(); //In case it isn't already
		logger.log("Starting indexing of the road network... (This could take a long time)");
		
		blockScan(origin.getBlock());
		
		roadScanOutput();
		
		logger.log("Road network indexed!");
	}
	
	private void roadScanOutput(){
		while((countScanBranches() > 0) && (System.currentTimeMillis() - lastStartTime) < 120000){ //120 second timeout if no more blocks are scanned within it (May cause task to stop premature if server lags extremely)
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				//oh well
			}
			logger.log("Currently active scan branches: "+scansRunning+" Queued extra branches: "+queuedBranches.size()+" Current network size: "+roughSize);
		}
		int count = 0;
		while(((countScanBranches() <= 0) || (System.currentTimeMillis() - lastStartTime) > 120000) && count < 5){ //Give it 5 extra seconds after scanning the network for safety
			count++;
			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		if(count < 5){ //It wasn't actually finished
			roadScanOutput();
			return;
		}
		roughSize = roadNetwork.size(); //Make sure it's correct or it looks dodgy
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
			decrementScansRunning();
			return;
		}
		if(roadNetwork.containsKey(block)){
			decrementScansRunning();
			return;
		}
		if(scansRunning > NetworkConversionScan.SCAN_BRANCH_LIMIT){
			queuedBranches.add(block);
			decrementScansRunning();
			return;
		}
		try {
			sleep(); //Give the main thread a rest occasionally so the server hopefully doesn't crash
			BlockRouteData brd;
			try {
				brd = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<BlockRouteData>(){

					@Override
					public BlockRouteData call() throws Exception {
						return AITrackFollow.carriagewayDirection(block);
					}}).get();
			} catch (Exception e1) {
				e1.printStackTrace();
				brd = new BlockRouteData(RouteBlockType.DIRECTIONAL, null);
			}
			if(brd.getType() == null){
				brd.setType(RouteBlockType.DIRECTIONAL);
				brd.setDirection(null);
			}
			roughSize++;
			roadNetwork.put(block, brd);
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
									if(AIRouter.isTrackBlock(newBlock.getType())){
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
