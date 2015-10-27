package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TrackingData {
	public Block nextBlock;
	public BlockFace dir;
	public boolean forJunction;
	public TrackingData(Block nextBlock, BlockFace dir, boolean forJunction){
		this.nextBlock = nextBlock;
		this.dir = dir;
		this.forJunction = forJunction;
	}
}
