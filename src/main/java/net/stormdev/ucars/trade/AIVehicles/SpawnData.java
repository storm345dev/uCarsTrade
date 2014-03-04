package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.World;
import org.bukkit.block.Block;

public class SpawnData {
	private Block b;
	private Block br;
	private World w;
	private int x;
	private int y;
	private int z;
	
	public SpawnData(Block b, Block br, World w, int x, int y, int z){
		this.b = b;
		this.br = br;
		this.w = w;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Block getB(){
		return b;
	}
	
	public Block getBr(){
		return br;
	}
	
	public World getWorld(){
		return w;
	}
	
	public int getX(){
		return x;
	}
	
	public int getY(){
		return y;
	}
	
	public int getZ(){
		return z;
	}
}
