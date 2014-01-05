package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TrackingData {
	public Block nextBlock;
	public BlockFace dir;
	public boolean junction;
	public TrackingData(Block nextBlock, BlockFace dir, boolean junction){
		this.nextBlock = nextBlock;
		this.dir = dir;
		this.junction = junction;
	}
}
