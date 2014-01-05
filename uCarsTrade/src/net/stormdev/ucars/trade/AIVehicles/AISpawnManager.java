package net.stormdev.ucars.trade.AIVehicles;

import net.stormdev.ucars.trade.main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class AISpawnManager {
	private main plugin;
	private boolean enabled;
	private Material trackBlock;
	private BukkitTask task = null;
	private long spawnRate = 5l;
	public AISpawnManager(main plugin, boolean enabled){
		this.plugin = plugin;
		this.enabled = enabled;
		String trackRaw = main.config.getString("general.ai.trackerBlock");
		trackBlock = Material.getMaterial(trackRaw);
		if(trackBlock == null){
			enabled = false;
		}
		if(enabled){
			task = main.plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new BukkitRunnable(){

				public void run() {
					doSpawns();
					return;
				}}, spawnRate, spawnRate);
		}
	}
	public void end(){
		if(task != null){
			task.cancel();
		}
	}
	public void doSpawns(){
		if(!enabled){
			return;
		}
		Player[] online = plugin.getServer().getOnlinePlayers().clone();
		for(Player player:online){
			if(main.random.nextBoolean()){
				continue; //Next iteration
			}
			if(player == null || !player.isOnline()){
				continue; //Next iteration
			}
			Block tracked = null;
			boolean stopSearch = false;
			
			Location playerLoc = player.getLocation();
			Block b = playerLoc.getBlock().getRelative(BlockFace.UP);
			Block br = b.getRelative(randomFace(), randomDirAmount());
			Block br2 = b.getRelative(randomFace(), randomDir2Amount());
			World w = b.getWorld();
			int y = br.getY();
			int x = br.getX();
			int z = br.getZ();
			
			int minY = y-10;
			
			tracked = b.getType() == trackBlock ? b : null;
			tracked = br.getType() == trackBlock ? br : null;
			tracked = br2.getType() == trackBlock ? br : null;
			
			while(tracked == null
					&& !stopSearch
					&& y>minY){
				
				Location check = new Location(w, x, y, z);
				if(check.getBlock().getType() == trackBlock){
					spawnFromTrackBlock(check);
				}
				
				y--;
			}
		}
		return;
	}
	
	public void spawnFromTrackBlock(Location location){
		main.logger.info("Running spawn thing");
		//TODO Track road and spawn in an AI
		Block current = location.getBlock();
		int distance = randomDistanceAmount();
		BlockFace currentDir = BlockFace.NORTH;
		while(distance > 0){
			//Need to follow the road
			TrackingData data = AITrackFollow.nextBlock(current, currentDir, trackBlock);
			
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
		//TODO Debug, spawn a villager
		spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.VILLAGER);
	}
	
	public int randomDistanceAmount(){
		return main.random.nextInt(33-6)+6; //Between 6 and 32
	}
	
	public int randomDirAmount(){
		return main.random.nextInt(5)+1; //1 to 5
	}
	
	public int randomDir2Amount(){
		return main.random.nextInt(10)+5; //5 to 10
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
}
