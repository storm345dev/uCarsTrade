package net.stormdev.ucars.trade;

import java.util.HashMap;
import java.util.UUID;

import net.stormdev.ucars.stats.Stat;
import net.stormdev.ucars.utils.Car;

import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

public class CarCalculations {

	public CarCalculations(){
		
	}
	public Vector getVelocity(Minecart cart, Vector current, double currentMult){
		UUID id = cart.getUniqueId();
		if(!main.plugin.carSaver.cars.containsKey(id)){
			return current;
		}
		Car car = main.plugin.carSaver.cars.get(id);
		HashMap<String, Stat> stats = car.getStats();
		if(stats.containsKey("trade.speed")){
			Stat stat = stats.get("trade.speed");
			double mod = 1;
			try {
				mod = (Double) stat.getValue();
			} catch (Exception e) {
			}
			current = current.multiply(mod);			
		}
		
		//TODO
		return current;
	}
	public Boolean isACar(Minecart car){
		if(main.plugin.carSaver.cars.containsKey(car.getUniqueId())){
			return true;
		}
		return false;
	}
}
