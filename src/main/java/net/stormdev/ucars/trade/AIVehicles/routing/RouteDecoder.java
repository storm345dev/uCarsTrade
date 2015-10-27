package net.stormdev.ucars.trade.AIVehicles.routing;

import org.bukkit.Material;
import org.bukkit.block.BlockFace;

public class RouteDecoder {
	private static final BlockRouteData[] key = new BlockRouteData[]{
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.NORTH),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.NORTH_EAST),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.EAST),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.SOUTH_EAST),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.SOUTH),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.SOUTH_WEST),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.WEST),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.NORTH_WEST),
		new BlockRouteData(RouteBlockType.JUNCTION, null),
		new BlockRouteData(RouteBlockType.CONTINUE, null),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.UP),
		new BlockRouteData(RouteBlockType.DIRECTIONAL, BlockFace.DOWN)
	};
	
	public static int getDataFromDir(RouteBlockType type, BlockFace direction){
		for(int i=0;i<key.length;i++){
			BlockRouteData brd = key[i];
			if(brd.getType().equals(type)){
				if(brd.getDirection() == direction){
					return i;
				}
			}
		}
		return 15;
	}
	
	public static BlockRouteData getDirection(Material type, int data){
		if(!type.equals(Material.STAINED_GLASS)){
			return new BlockRouteData(null, null);
		}
		
		try {
			return key[data];
		} catch (Exception e) {
			return new BlockRouteData(null, null);
		}
	}
}
