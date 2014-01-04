package net.stormdev.ucars.trade.AIVehicles;

import net.stormdev.ucars.trade.main;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class AISpawnManager {
	private main plugin;
	private boolean enabled;
	private Material trackBlock;
	public AISpawnManager(main plugin, boolean enabled){
		this.plugin = plugin;
		this.enabled = enabled;
		String trackRaw = main.config.getString("DIAMOND_ORE");
		trackBlock = Material.getMaterial(trackRaw);
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
			World w = b.getWorld();
			int y = br.getY();
			int x = br.getX();
			int z = br.getZ();
			
			tracked = b.getType() == trackBlock ? b : null;
			tracked = br.getType() == trackBlock ? br : null;
			
			while(tracked == null
					&& !stopSearch
					&& y>4){
				
				Location check = new Location(w, x, y, z);
				
				if(check.getBlock().getType() == trackBlock)
					spawnFromTrackBlock(check);
				
				y--;
			}
		}
		return;
	}
	
	public void spawnFromTrackBlock(Location location){
		//TODO Track road and them spawn
		Block current = location.getBlock();
		int distance = randomDistanceAmount();
		BlockFace currentDir = BlockFace.NORTH;
		while(distance > 0){
			if(current.getRelative(currentDir).getType() == trackBlock){
				current = current.getRelative(currentDir);
				continue;
			}
			//TODO Need to follow the road as it's not straight
			distance--;
		}
	}
	
	public int randomDistanceAmount(){
		return main.random.nextInt(33-6)+6; //Between 6 and 32
	}
	
	public int randomDirAmount(){
		return main.random.nextInt(16)+1;
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
