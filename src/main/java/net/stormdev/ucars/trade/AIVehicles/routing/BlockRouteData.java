package net.stormdev.ucars.trade.AIVehicles.routing;

import java.io.Serializable;

import org.bukkit.block.BlockFace;

public class BlockRouteData implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private BlockFace direction;
	private RouteBlockType type;
	
	public BlockRouteData(RouteBlockType type, BlockFace direction){
		this.direction = direction;
		this.type = type;
	}
	
	public BlockFace getDirection(){
		return this.direction;
	}
	
	public RouteBlockType getType(){
		return this.type;
	}
	
	public boolean isJunction(){
		return type == null ? false : type.equals(RouteBlockType.JUNCTION);
	}
}
