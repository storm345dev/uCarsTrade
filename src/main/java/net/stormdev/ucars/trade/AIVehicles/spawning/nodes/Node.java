package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.Serializable;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.AITrackFollow;
import net.stormdev.ucars.utils.SerializableLocation;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class Node implements Serializable {
	private static final long serialVersionUID = 1L;
	private SerializableLocation trackerBlockSloc = null;
	private transient volatile long lastSpawnTime = -1l; //Stops lots spawning at node at once
	
	public Node(Location location){
		this.trackerBlockSloc = new SerializableLocation(location);
	}
	
	public void setLocation(Location loc){
		this.trackerBlockSloc = new SerializableLocation(loc);
	}
	
	public Location getLocation(){
		return trackerBlockSloc.getLocation(Bukkit.getServer());
	}
	
	public Chunk getChunk(){
		return getLocation().getChunk();
	}
	
	public boolean isValid(){ //Checks location is of a tracker block, with a distinguishable direction and room to spawn a car above the road
		//Is it a tracker block?
		Block block = getLocation().getBlock();
		if(!AIRouter.isTrackBlock(block.getType())) {return false;} //Not a tracker block
		
		//Has it got a distinguishable direction?
		if(AITrackFollow.carriagewayDirection(block)==null) {return false;} //No distinguishable direction, maybe it's a junction block or something
		
		//Can a car spawn on top?
		return isRoomForCarToSpawn();
	}
	
	public BlockFace getCarriagewayDirection(){
		return AITrackFollow.carriagewayDirection(getLocation().getBlock());
	}
	
	public boolean isRoomForCarToSpawn(){ //Checks if there is room for a car to spawn
		Block block = getLocation().getBlock();
		
		for(int i=1;i<=3;i++){
			Block relative = block.getRelative(BlockFace.UP, i);
			if(relative.isEmpty() || relative.isLiquid()){
				return true;
			}
		}
		return false;
	}
	
	public void spawnAICarIfLogicalToDoSo(){
		if((System.currentTimeMillis() - lastSpawnTime) < 2500){ //2.5 second cooldown
			return;
		}
		Block tracker = getLocation().getBlock();
		
		final Location spawnLoc = tracker.getRelative(BlockFace.UP, 2).getLocation();
		if(!isRoomForCarToSpawn()){
			return;
		}
		
		final BlockFace carriagewayDir = AITrackFollow.carriagewayDirection(tracker);
		
		if(carriagewayDir != null){
			lastSpawnTime = System.currentTimeMillis();
			Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

				@Override
				public void run() {
					main.plugin.aiSpawns.spawnNPCCar(spawnLoc, carriagewayDir);
					return;
				}});
		}
	}
	
	public boolean isEqualTo(Node other){
		Location self = getLocation();
		Location otherLoc = other.getLocation();
		return self.getX() == otherLoc.getX() 
				&& self.getY() == otherLoc.getY()
				&& self.getZ() == otherLoc.getZ();
	}
}
