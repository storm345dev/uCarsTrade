package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class TrackingData {
	public Block nextBlock;
	public BlockFace dir;
	public boolean junction;
	public boolean forJunction;
	public TrackingData(Block nextBlock, BlockFace dir, boolean junction, boolean forJunction){
		this.nextBlock = nextBlock;
		this.dir = dir;
		this.junction = junction;
		this.forJunction = forJunction;
	}
}
