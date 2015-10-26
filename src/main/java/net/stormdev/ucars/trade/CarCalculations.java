package net.stormdev.ucars.trade;

import java.util.UUID;

import net.stormdev.ucarstrade.cars.DrivenCar;

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
		DrivenCar car = main.plugin.carSaver.getCarInUse(cart);
		if(car == null){
			return current;
		}
		current = current.multiply(car.getSpeed());
		if(car.isHandlingDamaged()){
			double x = current.getX();
			double z = current.getZ();
			double degrees = main.random.nextInt(180)-90;
			double[] output = rotateVector2d(x, z, Math.toRadians(degrees));
			current.setX(output[0]);
			current.setZ(output[1]);
			current.multiply((main.random.nextInt(100)/100.0d));
		}
		return current;
	}
	public Boolean isAuCar(Minecart car){
		if(main.plugin.carSaver.isAUCar(car)){
			return true;
		}
		if(car.hasMetadata("kart.racing")){
			return true;
		}
		return false;
	}
	
	public static double[] rotateVector2d(double x, double y, double radians)
	{
	    double[] result = new double[2];
	    result[0] = x * Math.cos(radians) - y * Math.sin(radians);
	    result[1] = x * Math.sin(radians) + y * Math.cos(radians);
	    return result;
	}
}
