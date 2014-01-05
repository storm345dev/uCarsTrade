package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class AITrackFollow {
	public static TrackingData nextBlock(Block current, BlockFace dir, Material trackBlock){
		Block cr = current.getRelative(dir);
		Block check = checkIfTracker(current, cr, trackBlock);
		if(check != null){
			return new TrackingData(check, dir);
		}
		
		//Need to get right/left of it
		BlockFace leftCheck = nextLeftFace(dir);
		BlockFace rightCheck = nextRightFace(dir);
		BlockFace behind = dir.getOppositeFace();
		
		while(leftCheck != behind && rightCheck != behind){
			Block lb = current.getRelative(leftCheck);
			Block rb = current.getRelative(rightCheck);
			
			Block clb = checkIfTracker(current, lb, trackBlock);
			Block crb = checkIfTracker(current, rb, trackBlock);
			if(clb != null){
				return new TrackingData(clb, leftCheck);
			}
			else if(crb != null) {
				return new TrackingData(crb, rightCheck);
			}
			leftCheck = nextLeftFace(leftCheck);
			rightCheck = nextRightFace(rightCheck);
		}
		
		Block cameFrom = current.getRelative(behind);
		if(cameFrom.getType() == trackBlock){ //Cannot follow road so return where we came from
			return new TrackingData(cameFrom, behind);
		}
		
		return new TrackingData(current, dir); //Where we came from isnt road, stay where we are
	}
	public static Block checkIfTracker(Block current, Block check, Material trackBlock){
		if(check.getType() == trackBlock){
			current = check;
			return current;
		}
		else if(check.getRelative(BlockFace.UP).getType() == trackBlock){
			current = check.getRelative(BlockFace.UP);
			return current;
		}
		else if(check.getRelative(BlockFace.DOWN).getType() == trackBlock){
			current = check.getRelative(BlockFace.DOWN);
			return current;
		}
		return null;
	}
	public static BlockFace nextRightFace(BlockFace face){
		switch(face){
		case NORTH: return BlockFace.NORTH_EAST;
		case NORTH_EAST: return BlockFace.EAST;
		case EAST: return BlockFace.SOUTH_EAST;
		case SOUTH_EAST: return BlockFace.SOUTH;
		case SOUTH: return BlockFace.SOUTH_WEST;
		case SOUTH_WEST: return BlockFace.WEST;
		case WEST: return BlockFace.NORTH_WEST;
		case NORTH_WEST: return BlockFace.NORTH;
		default: return face.getOppositeFace();
		}
	}
	public static BlockFace nextLeftFace(BlockFace face){
		switch(face){
		case NORTH: return BlockFace.NORTH_WEST;
		case NORTH_WEST: return BlockFace.WEST;
		case WEST: return BlockFace.SOUTH_WEST;
		case SOUTH_WEST: return BlockFace.SOUTH;
		case SOUTH: return BlockFace.SOUTH_EAST;
		case SOUTH_EAST: return BlockFace.EAST;
		case EAST: return BlockFace.NORTH_EAST;
		case NORTH_EAST: return BlockFace.NORTH;
		default: return face.getOppositeFace();
		}
	}
}
