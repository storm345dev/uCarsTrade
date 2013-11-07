package net.stormdev.ucars.trade;

import java.util.UUID;

import net.stormdev.ucars.utils.Car;

import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

public class CarCalculations {

	public CarCalculations(){
		
	}
	public Vector getVelocity(Minecart cart, Vector current){
		UUID id = cart.getUniqueId();
		if(!main.plugin.carSaver.cars.containsKey(id)){
			return current;
		}
		Car car = main.plugin.carSaver.cars.get(id);
		//TODO
		return current;
	}
}
