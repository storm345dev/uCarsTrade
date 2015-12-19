package net.stormdev.ucars.trade.AIVehicles;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.routing.BlockRouteData;
import net.stormdev.ucars.trade.AIVehicles.routing.RouteBlockType;
import net.stormdev.ucars.trade.AIVehicles.routing.RouteDecoder;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Sign;
import org.bukkit.entity.Entity;

import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;

public class AITrackFollow {
	private static Material roadEdge = null;
	private static Material getRoadEdgeType(){
		if(roadEdge != null) {return roadEdge;}
		roadEdge = Material.getMaterial(main.config.getString("general.ai.roadEdgeBlock"));
		return roadEdge;
	}
	
	public static RoadEdge findRoadEdge(Block roadSpawnBlock, BlockFace dir){
		Block edge = null;
		int distance = 1;
		int z = 1;
		while(z<20 && edge == null){
			Block b = roadSpawnBlock.getRelative(dir, z);
			if(b.getType() == getRoadEdgeType()){
				edge = b;
				distance = z;
			}
			z++;
		}
		if(edge != null){
			return new RoadEdge(edge, distance);
		}
		return null;
	}
	
	public static BlockRouteData carriagewayDirection(Block roadSpawnBlock){
		if(AIRouter.isEncodedRouting()){
			BlockRouteData brd = RouteDecoder.getDirection(roadSpawnBlock.getType(), roadSpawnBlock.getData());
			if(brd == null || (brd.getType() == null && brd.getDirection() == null)){
				Block under = roadSpawnBlock.getRelative(BlockFace.DOWN);
				brd = RouteDecoder.getDirection(under.getType(), under.getData());
				if(brd == null || (brd.getType() == null && brd.getDirection() == null)){
					under = under.getRelative(BlockFace.DOWN);
					brd = RouteDecoder.getDirection(under.getType(), under.getData());
				}
			}
			return brd;
		}
		
		if(roadSpawnBlock.getType().equals(AIRouter.junction)){
			return new BlockRouteData(RouteBlockType.JUNCTION, null);
		}
		
		String currentType = AIRouter.getTrackBlockType(roadSpawnBlock.getType());
		if(currentType != null){
			int currentPos = AIRouter.getTrackBlockIndexByType(currentType);
			int nextPos = currentPos+1;
			if(nextPos >= AIRouter.pattern.length){
				nextPos = 0;
			}
			if(currentPos >= 0){
				Block underunder = roadSpawnBlock.getRelative(BlockFace.DOWN);
				if(underunder.getState() instanceof Sign){
					Sign s = (Sign) underunder.getState();
					String top = s.getLine(0);
					if(top != null && top.length() > 0){
						try {
							return new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.valueOf(top));
						} catch (Exception e) {
							if(top.equalsIgnoreCase("NONE") || top.equalsIgnoreCase("NULL")){
								return new BlockRouteData(RouteBlockType.CONTINUE, null);
							}
							//Not a road related sign
						}
					}
				}
				for(BlockFace pDir: AITrackFollow.compassDirs()){
					Block next = roadSpawnBlock.getRelative(pDir);
					String type = AIRouter.getTrackBlockType(next.getType());
					if(type == null || type.equals(currentType)){
						continue;
					}
					int pos = AIRouter.getTrackBlockIndexByType(type);
					if(pos == nextPos && pos != currentPos){
						return new BlockRouteData(RouteBlockType.DIRECTIONAL, pDir);
					}
				}
				for(BlockFace pDir: AITrackFollow.diagonalDirs()){
					Block next = roadSpawnBlock.getRelative(pDir);
					String type = AIRouter.getTrackBlockType(next.getType());
					if(type == null || type.equals(currentType)){
						continue;
					}
					int pos = AIRouter.getTrackBlockIndexByType(type);
					if(pos == nextPos && pos != currentPos){
						return new BlockRouteData(RouteBlockType.DIRECTIONAL, pDir);
					}
				}
			}
		}
		
		//If no pattern, do this
		BlockFace dir = BlockFace.NORTH; //By default, north bound
		//Find road edges
		RoadEdge north = findRoadEdge(roadSpawnBlock, BlockFace.NORTH);
		RoadEdge east = findRoadEdge(roadSpawnBlock, BlockFace.EAST);
		RoadEdge south = findRoadEdge(roadSpawnBlock, BlockFace.SOUTH);
		RoadEdge west = findRoadEdge(roadSpawnBlock, BlockFace.WEST);
		
		//Choose if N/S bound or E/W bound
		north = south == null ? null:north;
		south = north == null ? null:south;
		west = east == null ? null:west;
		east = west == null ? null:east;
		
		if(east != null && west != null
				&& north != null && south != null){
			//At a corner or junction or whatever where road is not square
			RoadEdge NE = findRoadEdge(roadSpawnBlock, BlockFace.NORTH_EAST);
			RoadEdge SE = findRoadEdge(roadSpawnBlock, BlockFace.SOUTH_EAST);
			RoadEdge SW = findRoadEdge(roadSpawnBlock, BlockFace.SOUTH_WEST);
			RoadEdge NW = findRoadEdge(roadSpawnBlock, BlockFace.NORTH_WEST);
			
			NE = SW == null ? null:NE;
			SW = NE == null ? null:SW;
			SE = NW == null ? null:SE;
			NW = SE == null ? null:NW;
			
			if(NE != null && SW != null
					&& SE != null && NW != null){
				//At some complex junction some give up trying to spawn here
				return new BlockRouteData(null, null);
			}
			//  '/' = NW + SE edges
			//  '\' = SW + NE edges
			else if(NW != null){ //Road is SW/NE bound ('/')
				dir = BlockFace.NORTH_EAST;
				if(NW.distance < SE.distance){ //On left of road
					dir = BlockFace.SOUTH_WEST;
				}
			}
			else if(SW != null){ //Road is NW/SE bound ('\')
				dir = BlockFace.NORTH_WEST;
				if(SW.distance < NE.distance){
					dir = BlockFace.SOUTH_EAST;
				}
			}
			else{
				//Unable to find any road elements
				return new BlockRouteData(null, null);
			}
		}
		else if(east != null){ //Road is N/S bound
			if(west.distance < east.distance){ //On left of road
				dir = BlockFace.SOUTH; //Go southbound
			}
		}
		else if(north != null){ //Road is E/W bound
			dir = BlockFace.EAST;
			if(north.distance < south.distance){ //On left of road
				dir = BlockFace.WEST; //Go westbound
			}
		}
		else{
			return new BlockRouteData(null, null);
		}
		
		return new BlockRouteData(RouteBlockType.DIRECTIONAL, dir);
	}
	
	public static TrackingData nextBlock(Block current, VelocityData vd, BlockRouteData routeData, Entity vehicle){ //TODO WTf why doesn't this listen to dir signs, etc...
		BlockFace dir = routeData.getDirection() != null ? routeData.getDirection() : vd.getDir();
		
		if(AIRouter.isEncodedRouting()){
			//arg0 = block to drive to next
			//arg1 = direction of new block
			//arg2 = is next block a junction
			if(!routeData.isJunction() || vd.isInProgressOfTurningAtJunction()){
				Block cr = current.getRelative(dir);
				BlockRouteData brd = carriagewayDirection(cr);
				if(brd.getType() == null){
					cr = cr.getRelative(BlockFace.UP);
					brd = carriagewayDirection(cr);
					if(brd.getType() == null){
						cr = cr.getRelative(BlockFace.DOWN, 2);
						brd = carriagewayDirection(cr);
						if(brd.getType() == null){ //No road beyond here
							return new TrackingData(current, dir, routeData.isJunction());
						}
					}
				}
				if(!routeData.isJunction() && !brd.isJunction()){
					vd.setInProgressOfTurningAtJunction(false);
				}
				return new TrackingData(cr, dir, brd.isJunction());
			}
			//At a junction
			
			if(main.random.nextBoolean()){ //Don't turn off
				vd.setInProgressOfTurningAtJunction(true);
				return new TrackingData(current.getRelative(dir), dir, false); 
			}
			
			if(dir == null){
				dir = BlockFace.NORTH;
			}
			
			BlockFace leftCheck = nextLeftFace(dir);
			BlockFace rightCheck = nextRightFace(dir);
			BlockFace behind = dir.getOppositeFace();
			while(!leftCheck.equals(behind) && !rightCheck.equals(behind)){
				try {
					Block left = current.getRelative(leftCheck);
					Block right = current.getRelative(rightCheck);
					
					//Check right first
					BlockRouteData brd = carriagewayDirection(right);
					if(brd.getType()!=null){
						/*vd.setDir(rightCheck);*/
						vd.setInProgressOfTurningAtJunction(true);
						return new TrackingData(right, rightCheck, 
								true);
					}
					
					//Then check left
					brd = carriagewayDirection(left);
					if(brd.getType()!=null){
						/*vd.setDir(leftCheck);*/
						vd.setInProgressOfTurningAtJunction(true);
						return new TrackingData(left, leftCheck, 
								true);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				leftCheck = nextLeftFace(leftCheck);
				rightCheck = nextRightFace(rightCheck);
			}
			
			vd.setInProgressOfTurningAtJunction(true);
			return new TrackingData(current.getRelative(dir), dir, false); 
		}		
		
		Block cr = current.getRelative(dir);
		
		TrackBlock ch = checkIfOreTracker(current, cr);
		boolean turn = false;
		if(ch != null){
			Block check = ch.block;
			if(check != null){
				if(/*ch.junction*/routeData.isJunction() && main.random.nextBoolean()
						&& (vehicle != null && !UEntityMeta.hasMetadata(vehicle, "npc.turning"))){
					turn = true;
					/*return new TrackingData(check, dir, true); //TODO Maybe don't handle junction turns before we even get there...
*/				}
				else{
					if(vehicle != null){
						UEntityMeta.removeMetadata(vehicle, "npc.turning");
					}
					return new TrackingData(check, dir, ch.junction);
				}
			}
		}
		boolean chJ = ch!=null?ch.junction:false;
		
		if(dir == null){
			dir = BlockFace.NORTH;
		}
		
		//Need to get right/left of it
		BlockFace leftCheck = nextLeftFace(dir);
		BlockFace rightCheck = nextRightFace(dir);
		BlockFace behind = dir.getOppositeFace();
		
		while(!leftCheck.equals(behind) && !rightCheck.equals(behind)){
			Block lb = current.getRelative(leftCheck);
			Block rb = current.getRelative(rightCheck);
			TrackBlock clb = checkIfOreTracker(current, lb);
			TrackBlock crb = checkIfOreTracker(current, rb);
			//Check right first
			if(crb != null) {
				if(vehicle != null){
					if(!crb.junction){
						UEntityMeta.removeMetadata(vehicle, "npc.turning");
					}
					else if(turn){
						if(!UEntityMeta.hasMetadata(vehicle, "npc.turning")){
							UEntityMeta.setMetadata(vehicle, "npc.turning", new StatValue(null, main.plugin));
						}
					}
				}
				return new TrackingData(crb.block, rightCheck, 
						chJ);
			}
			else if(clb != null){
				if(vehicle != null){
					if(!clb.junction){
						UEntityMeta.removeMetadata(vehicle, "npc.turning");
					}
					else if(turn){
						if(!UEntityMeta.hasMetadata(vehicle, "npc.turning")){
							UEntityMeta.setMetadata(vehicle, "npc.turning", new StatValue(null, main.plugin));
						}
					}
				}
				TrackingData res = new TrackingData(clb.block, leftCheck, 
						chJ);
				return res;
			}
			//Didn't find a block to follow on
			leftCheck = nextLeftFace(leftCheck);
			rightCheck = nextRightFace(rightCheck);
		}
		
		return new TrackingData(current, dir, chJ); //Where we came from isnt road, stay where we are
	}
	public static TrackBlock checkIfOreTracker(Block current, Block check){
		Material junction = AIRouter.junction;
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
	
	public static boolean isCompassDir(BlockFace dir){
		if(dir == null){
			return false;
		}
		return dir.equals(BlockFace.NORTH) 
				|| dir.equals(BlockFace.EAST)
				|| dir.equals(BlockFace.SOUTH)
				|| dir.equals(BlockFace.WEST);
	}
	
	public static boolean isDiagonalDir(BlockFace dir){
		if(dir == null){
			return false;
		}
		return dir.equals(BlockFace.NORTH_EAST) 
				|| dir.equals(BlockFace.SOUTH_EAST)
				|| dir.equals(BlockFace.SOUTH_WEST)
				|| dir.equals(BlockFace.NORTH_WEST);
	}
	
	public static BlockFace[] allDirs(){ //used for iterating over dirs for pattern matching
		return new BlockFace[]{
				BlockFace.NORTH,
				BlockFace.NORTH_EAST,
				BlockFace.EAST,
				BlockFace.SOUTH_EAST,
				BlockFace.SOUTH,
				BlockFace.SOUTH_WEST,
				BlockFace.WEST,
				BlockFace.NORTH_WEST
		};
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
