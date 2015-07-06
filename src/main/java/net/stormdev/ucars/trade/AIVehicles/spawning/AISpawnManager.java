package net.stormdev.ucars.trade.AIVehicles.spawning;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

public interface AISpawnManager {
	public boolean isNPCCarsEnabled();
	public boolean isNPCCarsSpawningNow();
	public int getCurrentAICap();
	public int getMaxAICap();
	public int getSpawnedAICount();
	public boolean isEnabled();
	public void setEnabled(boolean b);
	public void decrementSpawnedAICount();
	public void incrementSpawnedAICount();
	public void setCurrentAICap(int cap);
	public void setNPCsCurrentlySpawning(boolean flag);
	public void shutdown();
	public void spawnNPCCar(Location carSpawnLoc, BlockFace carriagewayDir);
	public void followRoadAndSpawnCarFromTrackerBlock(Location trackerBlockLoc, BlockFace followDir);
}
