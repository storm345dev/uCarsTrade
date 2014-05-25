package net.stormdev.ucars.trade;

import java.util.UUID;

import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.entity.Minecart;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.CarCheck;
import com.useful.uCarsAPI.CarSpeedModifier;
import com.useful.uCarsAPI.uCarsAPI;

public class CarCalculations {

	private uCarsAPI api = null;
	public CarCalculations(){
		setup();
	}
	public void setup(){
		api = uCarsAPI.getAPI();
		if(!api.isPluginHooked(main.plugin)){
			api.hookPlugin(main.plugin);
		}
		CarCheck isACar = new CarCheck(){
			public Boolean isACar(Minecart car) {
				return isAuCar(car);
			}};
		api.registerCarCheck(main.plugin, isACar);
		CarSpeedModifier mod = new CarSpeedModifier(){
			public Vector getModifiedSpeed(Minecart car, Vector travelVector, double currentMult) {
				Vector v = getVelocity(car, travelVector, currentMult);
				return v;
			}};
		api.registerSpeedMod(main.plugin, mod);
		return;
	}
	public Vector getVelocity(Minecart cart, Vector current, double currentMult){
		UUID id = cart.getUniqueId();
		DrivenCar car = main.plugin.carSaver.getCarInUse(id);
		if(car == null){
			return current;
		}
		current = current.multiply(car.getSpeed());
		if(car.isHandlingDamaged()){
			double x = current.getX();
			double z = current.getZ();
			if(!main.random.nextBoolean()){
				if(main.random.nextBoolean()){
					//Change x
					x = x*currentMult/3;
				}
				else if(main.random.nextBoolean()){
					x = -x;
					z = -z;
				}
				else{
					//Change z
					z = z*currentMult/3;	
				}
				current.setX(x);
				current.setZ(z);
			}
		}
		return current;
	}
	public Boolean isAuCar(Minecart car){
		if(main.plugin.carSaver.isAUCar(car.getUniqueId())){
			return true;
		}
		if(car.hasMetadata("kart.racing")){
			return true;
		}
		return false;
	}
}
