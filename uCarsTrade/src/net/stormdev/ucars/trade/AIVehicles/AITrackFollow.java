package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class AITrackFollow {
	public static Block nextBlock(Block current, BlockFace dir){
		
		
		return current;
	}
	public static BlockFace nextRightFace(BlockFace face){
		switch(face){
		case NORTH: return BlockFace.NORTH_EAST;
		case NORTH_EAST: return BlockFace.EAST;
		}
	}
}
