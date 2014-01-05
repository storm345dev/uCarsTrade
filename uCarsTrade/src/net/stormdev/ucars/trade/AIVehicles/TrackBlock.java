package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.block.Block;

public class TrackBlock {
	public Block block;
	public boolean junction;
	public TrackBlock(Block block, boolean junction){
		this.block = block;
		this.junction = junction;
	}
}
