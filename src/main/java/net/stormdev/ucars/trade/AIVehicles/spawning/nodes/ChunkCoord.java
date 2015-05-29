package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.Serializable;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkCoord implements Serializable {
	private static final long serialVersionUID = 1L;

	private UUID worldUUID;
	private int x;
	private int z;
	
	public ChunkCoord(Chunk chunk){
		this(chunk.getWorld(), chunk.getX(), chunk.getZ());
	}
	
	public ChunkCoord(World world, int x, int z){
		this(world.getUID(), x, z);
	}
	
	public ChunkCoord(UUID worldUUID, int x, int z){
		this.worldUUID = worldUUID;
		this.x = x;
		this.z = z;
	}
	
	public boolean isValid(){
		return getWorld() != null && getChunk() != null;
	}
	
	public Chunk getChunk(){
		World w = getWorld();
		if(w == null) {return null;}
		return w.getChunkAt(getX(), getZ());
	}
	
	public World getWorld(){
		return Bukkit.getWorld(worldUUID);
	}
	
	public void setWorld(World world){
		this.worldUUID = world.getUID();
	}

	public UUID getWorldUUID() {
		return worldUUID;
	}

	public void setWorldUUID(UUID worldUUID) {
		this.worldUUID = worldUUID;
	}

	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getZ() {
		return z;
	}

	public void setZ(int z) {
		this.z = z;
	}
	
	public boolean isEqualTo(ChunkCoord coord){
		return this.worldUUID.equals(coord.worldUUID)
				&& this.x == coord.x
				&& this.z == coord.z;
	}
	
	@Override
	public boolean equals(Object obj){
		if(!(obj instanceof ChunkCoord)){
			return false;
		}
		return isEqualTo((ChunkCoord) obj);
	}
}
