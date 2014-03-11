package net.stormdev.ucars.trade.AIVehicles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.stormdev.ucars.stats.Stat;
import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.utils.Car;
import net.stormdev.ucars.utils.CarGenerator;
import net.stormdev.ucars.utils.ReturnTask;
import net.stormdev.ucars.utils.Scheduler;
import net.stormdev.ucars.utils.SyncReturnTask;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import com.useful.ucars.ClosestFace;
import com.useful.ucarsCommon.StatValue;

public class AISpawnManager {
	private main plugin;
	private boolean enabled;
	private boolean fullEnable;
	private Material trackBlock;
	private Material roadEdge;
	private Material junction;
	private BukkitTask task = null;
	private static long spawnRate = 6l;
	private List<String> aiNames;
	private static int cap = 69;
	private static int liveCap = 5;
	private static int spawned = 0;
	
	public static void decrementSpawned(){
		spawned--;
		if(spawned < 0){
			spawned = 0;
		}
	}
	
	public static void incrementSpawned(){
		spawned++;
	}
	
	public static int getLiveCap(){
		return liveCap;
	}
	
	public static void setLiveCap(int cap){
		liveCap = cap;
	}
	
	public boolean NPCsEnabled(){
		return fullEnable;
	}
	
	public boolean NPCsCurrentlyEnabled(){
		return enabled;
	}
	
	public synchronized void setNPCsCurrentlyEnabled(boolean on){
		this.enabled = on;
	}
	
	public AISpawnManager(main plugin, boolean enabled){
		this.plugin = plugin;
		this.enabled = enabled;
		this.fullEnable = enabled;
		String trackRaw = main.config.getString("general.ai.trackerBlock");
		String edgeRaw = main.config.getString("general.ai.roadEdgeBlock");
		String junRaw = main.config.getString("general.ai.junctionBlock");
		aiNames = main.config.getStringList("general.ai.names");
		cap = main.config.getInt("general.ai.limit");
		liveCap = 5;
		// A task to dynamically change liveCap to match the server's current AI holdings
		Bukkit.getScheduler().runTaskTimerAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				// Use resource score to get live cap
				int score = DynamicLagReducer.getResourceScore();
				int newCap = liveCap;
				if(score > 84 && DynamicLagReducer.getTPS() > 19.5){
					newCap++;
				}
				else if(score < 70 || DynamicLagReducer.getTPS() < 17.5){
					newCap *= 0.5; //Half
				}
				else if(score < 70 || DynamicLagReducer.getTPS() < 18){
					newCap -= 10;
				}
				else if(score < 70 || DynamicLagReducer.getTPS() < 19){
					newCap -= 5;
				}
				else if(score < 75 || DynamicLagReducer.getTPS() < 19){
					newCap--;
				}
				
				if(newCap != liveCap){
					if(newCap > cap){
						newCap = cap;
					}
					if(newCap < 2){
						newCap = 2; //Min of 2
					}
					liveCap = newCap;
				}
				
				return;
			}}, 100l, 100l); //Every 5s
		
		new DynamicLagReducer().start();
		
		trackBlock = Material.getMaterial(trackRaw);
		roadEdge = Material.getMaterial(edgeRaw);
		junction = Material.getMaterial(junRaw);
		if(trackBlock == null || roadEdge == null || junction == null){
			main.logger.info("Didn't enable AIs as configuration is invalid!");
			enabled = false;
		}
		if(enabled){
			task = main.plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new BukkitRunnable(){

				public void run() {
					if(spawned >= liveCap || spawned >= cap){
						return;
					}
					boolean longSpawns = main.random.nextBoolean();
					boolean doubleSpawns = longSpawns && main.random.nextBoolean();
					
					for(Player player:new ArrayList<Player>(Arrays.asList(Bukkit.getOnlinePlayers()))){
						try {
							doSpawns(player);
						} catch (Exception e) {
							e.printStackTrace();
							//Error spawning
						}
						if(longSpawns){
							doLongSpawns(player);
						}
						if(doubleSpawns){
							try {
								doSpawns(player);
							} catch (Exception e) {
								//Error spawning
								e.printStackTrace();
							}
						}
					}
					return;
				}}, spawnRate, spawnRate);
		}
	}
	public void end(){
		if(task != null){
			task.cancel();
		}
	}
	public void doLongSpawns(final Player player){
		if(!enabled){
			return;
		}
		if(main.random.nextBoolean()){
			return; //Next iteration
		}
		if(player == null || !player.isOnline()){
			return; //Next iteration
		}
		try {
			Block tracked = null;
			boolean stopSearch = false;
			
			SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

				@Override
				public SpawnData[] execute() {
					Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
					Block br = b.getRelative(randomFace(), randomDir3Amount());
					World w = b.getWorld();
					int y = br.getY();
					int x = br.getX();
					int z = br.getZ();
					
					return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
				}}).executeOnce();
			SpawnData data = spawnData.getResults()[0];
			
			//Location playerLoc = data.getPlayerLoc();
			Block b = data.getB();
			Block br = data.getBr();
			final World w = data.getWorld();
			int y = data.getY();
			final int x = data.getX();
			final int z = data.getZ();
			
			int minY = y-10;
			
			tracked = b.getType() == trackBlock ? b : null;
			tracked = br.getType() == trackBlock ? br : null;
			
			while(tracked == null
					&& !stopSearch
					&& y>minY){
				final int yy = y;
				Scheduler.runBlockingSyncTask(new Runnable(){

					@Override
					public void run() {
						Location check = new Location(w, x, yy, z);
						if(check.getBlock().getType() == trackBlock){
							spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
						}
						return;
					}});
				
				y--;
			}
		} catch (Exception e) {
			//They just joined, or error spawning
		}
		return;
	}
	public void doLongSpawns(){
		if(!enabled){
			return;
		}
		Player[] online = plugin.getServer().getOnlinePlayers().clone();
		for(final Player player:online){
			if(main.random.nextBoolean()){
				continue; //Next iteration
			}
			if(player == null || !player.isOnline()){
				continue; //Next iteration
			}
			try {
				Block tracked = null;
				boolean stopSearch = false;
				
				SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

					@Override
					public SpawnData[] execute() {
						Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
						Block br = b.getRelative(randomFace(), randomDir3Amount());
						World w = b.getWorld();
						int y = br.getY();
						int x = br.getX();
						int z = br.getZ();
						
						return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
					}}).executeOnce();
				SpawnData data = spawnData.getResults()[0];
				
				//Location playerLoc = data.getPlayerLoc();
				Block b = data.getB();
				Block br = data.getBr();
				final World w = data.getWorld();
				int y = data.getY();
				final int x = data.getX();
				final int z = data.getZ();
				
				int minY = y-10;
				
				tracked = b.getType() == trackBlock ? b : null;
				tracked = br.getType() == trackBlock ? br : null;
				
				while(tracked == null
						&& !stopSearch
						&& y>minY){
					final int yy = y;
					Scheduler.runBlockingSyncTask(new Runnable(){

						@Override
						public void run() {
							Location check = new Location(w, x, yy, z);
							if(check.getBlock().getType() == trackBlock){
								spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
							}
							return;
						}});
					
					y--;
				}
			} catch (Exception e) {
				//They just joined
			}
		}
		return;
	}
	public void doSpawns() throws Exception{
		if(!enabled){
			return;
		}
		Player[] online = plugin.getServer().getOnlinePlayers().clone();
		for(final Player player:online){
			if(main.random.nextBoolean()){
				continue; //Next iteration
			}
			if(player == null || !player.isOnline()){
				continue; //Next iteration
			}
			Block tracked = null;
			boolean stopSearch = false;
			
			SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

				@Override
				public SpawnData[] execute() {
					Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
					Block br = null;
					if(main.random.nextBoolean()){
						br = b.getRelative(randomFace(), randomDir3Amount());
					}
					else {
						br = b.getRelative(randomFace(), randomDir2Amount());
					}
					World w = b.getWorld();
					int y = br.getY();
					int x = br.getX();
					int z = br.getZ();
					
					return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
				}}).executeOnce();
			SpawnData data = spawnData.getResults()[0];
			
			//Location playerLoc = data.getPlayerLoc();
			Block b = data.getB();
			Block br = data.getBr();
			final World w = data.getWorld();
			int y = data.getY();
			final int x = data.getX();
			final int z = data.getZ();
			
			int minY = y-10;
			
			tracked = b.getType() == trackBlock ? b : null;
			tracked = br.getType() == trackBlock ? br : null;
			
			while(tracked == null
					&& !stopSearch
					&& y>minY){
				
				Location check = new Location(w, x, y, z);
				if(check.getBlock().getType() == trackBlock){
					spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
				}
				
				y--;
			}
		}
		return;
	}
	
	public void doSpawns(final Player player) throws Exception{
		if(!enabled){
			return;
		}
		if(main.random.nextBoolean()){
			return; //Next iteration
		}
		if(player == null || !player.isOnline()){
			return;//Next iteration
		}
		Block tracked = null;
		boolean stopSearch = false;
		
		SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

			@Override
			public SpawnData[] execute() {
				Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
				Block br = null;
				if(main.random.nextBoolean()){
					br = b.getRelative(randomFace(), randomDirAmount());
				}
				else {
					br = b.getRelative(randomFace(), randomDir2Amount());
				}
				World w = b.getWorld();
				int y = br.getY();
				int x = br.getX();
				int z = br.getZ();
				
				return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
			}}).executeOnce();
		SpawnData data = spawnData.getResults()[0];
		
		//Location playerLoc = data.getPlayerLoc();
		Block b = data.getB();
		Block br = data.getBr();
		final World w = data.getWorld();
		int y = data.getY();
		final int x = data.getX();
		final int z = data.getZ();
		
		int minY = y-10;
		
		tracked = b.getType() == trackBlock ? b : null;
		tracked = br.getType() == trackBlock ? br : null;
		
		while(tracked == null
				&& !stopSearch
				&& y>minY){
			
			Location check = new Location(w, x, y, z);
			if(check.getBlock().getType() == trackBlock){
				spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
			}
			
			y--;
		}
		return;
	}
	
	public void spawnFromTrackBlock(Location location, BlockFace currentDir){
		//Track road and spawn in an AI
		Block current = location.getBlock();
		int distance = randomDistanceAmount();
		while(distance > 0){
			//Need to follow the road
			TrackingData data = AITrackFollow.nextBlock(current, currentDir, trackBlock, junction, null);
			
			current = data.nextBlock;
			currentDir = data.dir;
			distance--;
		}
		//Current is the track block
		Block toSpawn = current.getRelative(BlockFace.UP);
		while(toSpawn.getType() == trackBlock && toSpawn.getY() <= 256){ //Height limit
			toSpawn = toSpawn.getRelative(BlockFace.UP);
		}
		//toSpawn is the road surface
		Location spawnLoc = toSpawn.getLocation().add(0, 1.5, 0); //Position to spawn car
		if(!spawnLoc.getBlock().isEmpty()){
			spawnLoc = spawnLoc.add(0, 0.6, 0);
			if(!spawnLoc.getBlock().isEmpty()){
				//Must spawn car in an air block
				return;
			}
		}
		BlockFace carDirection = currentDir;
		carDirection = carriagewayDirection(current);
		if(carDirection == null){
			//Not a valid road structure
			return;
		}
		spawnNPC(spawnLoc, carDirection);
	}
	
	public BlockFace carriagewayDirection(Block roadSpawnBlock){
		BlockFace dir = BlockFace.NORTH; //By default, north bound
		
		//Find road edges
		RoadEdge north = findRoadEdge(roadSpawnBlock, BlockFace.NORTH);
		RoadEdge east = findRoadEdge(roadSpawnBlock, BlockFace.EAST);
		RoadEdge south = findRoadEdge(roadSpawnBlock, BlockFace.SOUTH);
		RoadEdge west = findRoadEdge(roadSpawnBlock, BlockFace.WEST);
		
		//Choose if N/S bound or E/W bound
		north = south == null ? null:north;
		south = north == null ? null:south;
		west = east == null ? null:west;
		east = west == null ? null:east;
		
		if(east != null && west != null
				&& north != null && south != null){
			//At a corner or junction or whatever where road is not square
			RoadEdge NE = findRoadEdge(roadSpawnBlock, BlockFace.NORTH_EAST);
			RoadEdge SE = findRoadEdge(roadSpawnBlock, BlockFace.SOUTH_EAST);
			RoadEdge SW = findRoadEdge(roadSpawnBlock, BlockFace.SOUTH_WEST);
			RoadEdge NW = findRoadEdge(roadSpawnBlock, BlockFace.NORTH_WEST);
			
			NE = SW == null ? null:NE;
			SW = NE == null ? null:SW;
			SE = NW == null ? null:SE;
			NW = SE == null ? null:NW;
			
			if(NE != null && SW != null
					&& SE != null && NW != null){
				//At some complex junction some give up trying to spawn here
				return null;
			}
			//  '/' = NW + SE edges
			//  '\' = SW + NE edges
			else if(NW != null){ //Road is SW/NE bound ('/')
				dir = BlockFace.NORTH_EAST;
				if(NW.distance < SE.distance){ //On left of road
					dir = BlockFace.SOUTH_WEST;
				}
			}
			else if(SW != null){ //Road is NW/SE bound ('\')
				dir = BlockFace.NORTH_WEST;
				if(SW.distance < NE.distance){
					dir = BlockFace.SOUTH_EAST;
				}
			}
			else{
				//Unable to find any road elements
				return null;
			}
		}
		else if(east != null){ //Road is N/S bound
			if(west.distance < east.distance){ //On left of road
				dir = BlockFace.SOUTH; //Go southbound
			}
		}
		else if(north != null){ //Road is E/W bound
			dir = BlockFace.EAST;
			if(north.distance < south.distance){ //On left of road
				dir = BlockFace.WEST; //Go westbound
			}
		}
		else{
			return null;
		}
		
		return dir;
	}
	
	public RoadEdge findRoadEdge(Block roadSpawnBlock, BlockFace dir){
		Block edge = null;
		int distance = 1;
		int z = 1;
		while(z<20 && edge == null){
			Block b = roadSpawnBlock.getRelative(dir, z);
			if(b.getType() == roadEdge){
				edge = b;
				distance = z;
			}
			z++;
		}
		if(edge != null){
			return new RoadEdge(edge, distance);
		}
		return null;
	}
	
	public void spawnNPC(Location spawn, final BlockFace currentDirection){
		spawn = spawn.add(0.5, 0, 0.5);
		final Location spawnLoc = spawn;
		plugin.getServer().getScheduler().runTask(plugin, new BukkitRunnable(){

			public void run() {
				final Minecart m = (Minecart) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.MINECART);
				List<Entity> es = m.getNearbyEntities(2, 2, 2);
				for(Entity e:es){
					if(e.getType() == EntityType.MINECART){
						m.remove();
						return; //Already a car in close proximity
					}
				}
				List<Entity> nearby = m.getNearbyEntities(10, 3, 10);
				if(nearby.size() > 2){
					//Too many in area
					m.remove();
					return;
				}
				//Is valid
				Villager v = (Villager) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.VILLAGER);
				v.setAdult();
				v.setBreed(false);
				v.setAgeLock(true);
				v.setCanPickupItems(false);
				v.setCustomName(randomName());
				v.setCustomNameVisible(true);
				m.setPassenger(v);
				
				Car c = CarGenerator.gen();
				if(c.stats.containsKey("trade.handling")){
					c.stats.remove("trade.handling");
				}
				//Make it a car
				c.id = m.getUniqueId();
				c.isPlaced = true;
				//Set it as an NPC car and set direction
				c.stats.put("trade.npc", new Stat(true, plugin));
				plugin.carSaver.setCar(m.getUniqueId(), c);
				m.setMetadata("trade.npc", new StatValue(new VelocityData(currentDirection, null), plugin));
				spawned++;
				return;
			}});
		return;
	}
	
	public int randomDistanceAmount(){
		return main.random.nextInt(25-10)+10; //Between 10 and 30
	}
	
	public int randomDirAmount(){
		return main.random.nextInt(5)+1; //1 to 5
	}
	
	public int randomDir2Amount(){
		return main.random.nextInt(10-5)+5; //5 to 10
	}
	
	public int randomDir3Amount(){
		return main.random.nextInt(30-20)+20; //30 to 20
	}
	
	public BlockFace randomFace(){
		//N, NE, E, SE, S, SW, W, NW (8)
		int rand = main.random.nextInt(8);
		switch(rand){
		case 0:return BlockFace.NORTH;
		case 1:return BlockFace.NORTH_EAST;
		case 2:return BlockFace.EAST;
		case 3:return BlockFace.SOUTH_EAST;
		case 4:return BlockFace.SOUTH;
		case 5:return BlockFace.SOUTH_WEST;
		case 6:return BlockFace.WEST;
		case 7:return BlockFace.NORTH_WEST;
		default: return BlockFace.NORTH;
		}
	}
	public String randomName(){
		if(aiNames == null){
			return "Citizen";
		}
		return aiNames.get(main.random.nextInt(aiNames.size())); //Select a random name
	}
}
