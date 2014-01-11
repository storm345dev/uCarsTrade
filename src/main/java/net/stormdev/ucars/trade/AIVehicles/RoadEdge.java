package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.block.Block;

public class RoadEdge {
	public int distance;
	public Block edge;
	public RoadEdge(Block edge, int distance){
		this.edge = edge;
		this.distance = distance;
	}
}
