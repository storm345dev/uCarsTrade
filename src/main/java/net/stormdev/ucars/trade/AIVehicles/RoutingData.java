package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.Location;
import org.bukkit.block.Block;

public class RoutingData {
	private Location loc;
	private Block under;
	
	public RoutingData(Location loc, Block under){
		this.loc = loc;
		this.under = under;
	}
	
	public Location getLoc(){
		return loc;
	}
	
	public Block getUnder(){
		return under;
	}
}
