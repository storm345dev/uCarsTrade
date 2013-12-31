package net.stormdev.ucars.utils;

import java.io.Serializable;

import org.bukkit.Chunk;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class DisplayType implements Serializable {
	private static final long serialVersionUID = 4323952036275549097L;

	public static void putEntityInCar(Minecart car, Entity entity){
		car.setPassenger(entity);
		return;
	}
	
	public static void putEntityInCar(Minecart car, EntityType type){
		Chunk c = car.getLocation().getChunk();
		if(!c.isLoaded()){
			c.load(true);
		}
		Entity e = car.getWorld().spawnEntity(car.getLocation(), type);
		putEntityInCar(car, e);
		return;
	}
	public static Entity spawnEntityAtCar(Minecart car, EntityType type){
		Chunk c = car.getLocation().getChunk();
		if(!c.isLoaded()){
			c.load(true);
		}
		Entity e = car.getWorld().spawnEntity(car.getLocation(), type);
		return e;
	}
	
	private String name;
	private CarFiller filler;
	protected DisplayType(String name, CarFiller filler){
		this.name = name;
		this.filler = filler;
	}
	public String getName(){
		return name;
	}
	public CarFiller getFiller(){
		return filler;
	}
	public void fill(Minecart car, Player owner){
		getFiller().putInCar(car, owner);
		return;
	}
}
