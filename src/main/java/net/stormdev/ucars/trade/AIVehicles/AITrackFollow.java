package net.stormdev.ucars.trade.AIVehicles;

import net.stormdev.ucars.trade.main;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;

import com.useful.ucarsCommon.StatValue;

public class AITrackFollow {
	public static TrackingData nextBlock(Block current, BlockFace dir, Material junctionBlock, Entity vehicle){
		Block cr = current.getRelative(dir);
		TrackBlock ch = checkIfTracker(current, cr, junctionBlock);
		boolean turn = false;
		if(ch != null){
			Block check = ch.block;
			if(check != null){
				if(ch.junction && main.random.nextBoolean() && main.random.nextBoolean()
						&& (vehicle != null && !vehicle.hasMetadata("npc.turning"))){
					turn = true;
				}
				else{
					return new TrackingData(check, dir, false);
				}
			}
		}
		
		//Need to get right/left of it
		BlockFace leftCheck = nextLeftFace(dir);
		BlockFace rightCheck = nextRightFace(dir);
		BlockFace behind = dir.getOppositeFace();
		
		while(leftCheck != behind && rightCheck != behind){
			Block lb = current.getRelative(leftCheck);
			Block rb = current.getRelative(rightCheck);
			TrackBlock clb = checkIfTracker(current, lb, junctionBlock);
			TrackBlock crb = checkIfTracker(current, rb, junctionBlock);
			//Check right first
			if(crb != null) {
				if(vehicle != null){
					if(!crb.junction){
						vehicle.removeMetadata("npc.turning", main.plugin);
					}
					else if(turn){
						if(!vehicle.hasMetadata("npc.turning")){
							vehicle.setMetadata("npc.turning", new StatValue(null, main.plugin));
						}
					}
				}
				return new TrackingData(crb.block, rightCheck, 
						crb.junction);
			}
			else if(clb != null){
				if(vehicle != null){
					if(!clb.junction){
						vehicle.removeMetadata("npc.turning", main.plugin);
					}
					else if(turn){
						if(!vehicle.hasMetadata("npc.turning")){
							vehicle.setMetadata("npc.turning", new StatValue(null, main.plugin));
						}
					}
				}
				return new TrackingData(clb.block, leftCheck, 
						clb.junction);
			}
			//Didn't find a block to follow on
			leftCheck = nextLeftFace(leftCheck);
			rightCheck = nextRightFace(rightCheck);
		}
		
		return new TrackingData(current, dir, false); //Where we came from isnt road, stay where we are
	}
	public static TrackBlock checkIfTracker(Block current, Block check, Material junction){
		if(AIRouter.isTrackBlock(check.getType())){
			current = check;
			return new TrackBlock(current, false);
		}
		else if(check.getType() == junction){
			current = check;
			return new TrackBlock(current, true);
		}
		else if(AIRouter.isTrackBlock(check.getRelative(BlockFace.UP).getType())){
			current = check.getRelative(BlockFace.UP);
			return new TrackBlock(current, false);
		}
		else if(check.getRelative(BlockFace.UP).getType() == junction){
			current = check.getRelative(BlockFace.UP);
			return new TrackBlock(current, true);
		}
		else if(AIRouter.isTrackBlock(check.getRelative(BlockFace.DOWN).getType())){
			current = check.getRelative(BlockFace.DOWN);
			return new TrackBlock(current, false);
		}
		else if(check.getRelative(BlockFace.DOWN).getType() == junction){
			current = check.getRelative(BlockFace.DOWN);
			return new TrackBlock(current, true);
		}
		return null;
	}
	
	public static BlockFace[] compassDirs(){ //used for iterating over dirs for pattern matching
		return new BlockFace[]{
				BlockFace.NORTH,
				/*BlockFace.NORTH_EAST,*/
				BlockFace.EAST,
				/*BlockFace.SOUTH_EAST,*/
				BlockFace.SOUTH,
				/*BlockFace.SOUTH_WEST,*/
				BlockFace.WEST,
				/*BlockFace.NORTH_WEST*/
		};
	}
	
	public static BlockFace[] diagonalDirs(){ //used for iterating over dirs for pattern matching
		return new BlockFace[]{
				/*BlockFace.NORTH,*/
				BlockFace.NORTH_EAST,
				/*BlockFace.EAST,*/
				BlockFace.SOUTH_EAST,
				/*BlockFace.SOUTH,*/
				BlockFace.SOUTH_WEST,
				/*BlockFace.WEST,*/
				BlockFace.NORTH_WEST
		};
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
		default: return face;
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
		default: return face;
		}
	}
	public static BlockFace nextCompassPointRight(BlockFace face){
		switch(face){
		case NORTH: return BlockFace.EAST;
		case EAST: return BlockFace.SOUTH;
		case SOUTH: return BlockFace.WEST;
		case WEST: return BlockFace.NORTH;
		default: return face.getOppositeFace();
		}
	}
	public static BlockFace nextCompassPointLeft(BlockFace face){
		switch(face){
		case NORTH: return BlockFace.WEST;
		case WEST: return BlockFace.SOUTH;
		case SOUTH: return BlockFace.EAST;
		case EAST: return BlockFace.NORTH;
		default: return face.getOppositeFace();
		}
	}
	public static BlockFace randomCompassDir(){
		int rand = main.random.nextInt(4); //0-3
		switch(rand){
		case 0:return BlockFace.NORTH;
		case 1:return BlockFace.EAST;
		case 2:return BlockFace.WEST;
		case 3:return BlockFace.SOUTH;
		default: return BlockFace.SOUTH;
		}
	}
}
