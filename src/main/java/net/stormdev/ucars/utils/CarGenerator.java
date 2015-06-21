package net.stormdev.ucars.utils;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucarstrade.cars.CarPresets;
import net.stormdev.ucarstrade.cars.CarPresets.CarPreset;
import net.stormdev.ucarstrade.cars.DrivenCar;

import com.useful.ucars.ucars;

public class CarGenerator {

	public static DrivenCar gen(){
		if(CarPresets.isCarPresetsUsed()){
			CarPreset cp = CarPresets.getPresets().get(main.random.nextInt(CarPresets.getPresets().size()));
			DrivenCar dc = new DrivenCar(cp.getName(), cp.getSpeed(), cp.getHealth(), cp.getAcceleration(), cp.getTurnAmountPerTick(), false, cp.getModifications());
			return dc;
		}
		
		double health = ucars.config.getDouble("general.cars.health.default");
		double speed = 1;
		String name = "Car";
		if(main.random.nextBoolean()){
			int i = main.random.nextInt(5); // 0,1,2,3,4
			i = i-2; //-2,-1,0,1,2
			health = health + i;
		}
		if(health < 1){
			health = 1;
		}
		int rand = main.random.nextInt(19-2)+2; //5 -- 15
		speed = rand;
		if(speed < 2){
			speed = 2;
		}
		double speeD = speed/10;
		List<String> names = main.config.getStringList("general.cars.names");
		if(names.size() > 0){
			int max = names.size();
			int random = main.random.nextInt(max);
			name = names.get(random);
		}
		return gen(speeD, health, name, (main.random.nextBoolean()&&main.random.nextBoolean()&&main.random.nextBoolean()));
	}
	public static DrivenCar gen(double speed, double health, String name){
		return new DrivenCar(name, speed, health, 1, 5, false, new ArrayList<String>());
	}
	public static DrivenCar gen(double speed, double health, String name, boolean handlingDamaged){
		return new DrivenCar(name, speed, health, 1, 5, handlingDamaged, new ArrayList<String>());
	}
}
