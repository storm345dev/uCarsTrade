package net.stormdev.ucars.utils;

import java.util.HashMap;
import java.util.List;

import net.stormdev.ucars.stats.HandlingDamagedStat;
import net.stormdev.ucars.stats.HealthStat;
import net.stormdev.ucars.stats.NameStat;
import net.stormdev.ucars.stats.SpeedStat;
import net.stormdev.ucars.stats.Stat;
import net.stormdev.ucars.trade.main;

import com.useful.ucars.ucars;

public class CarGenerator {

	public static Car gen(){
		HashMap<String, Stat> meta = new HashMap<String, Stat>();
		double health = ucars.config.getDouble("general.cars.health.default");
		double speed = ucars.config.getDouble("general.cars.defSpeed");
		String name = "Car";
		if(main.random.nextBoolean()){
			int i = main.random.nextInt(5); // 0,1,2,3,4
			i = i-2; //-2,-1,0,1,2
			health = health + i;
		}
		if(health < 1){
			health = 1;
		}
		int rand = main.random.nextInt(25); //0 -- 24
		int mod = rand - 12; //-12 -- 12
		speed = speed + mod;
		if(speed < 5){
			speed = 5;
		}
		if(rand == 24){ //1 in 25 chance
			meta.put("trade.handling", new HandlingDamagedStat(true, main.plugin));
		}
		List<String> names = main.config.getStringList("general.cars.names");
		if(names.size() > 0){
			int max = names.size();
			int random = main.random.nextInt(max);
			name = names.get(random);
		}
		meta.put("trade.name", new NameStat(name, main.plugin));
		meta.put("trade.health", new HealthStat(health, main.plugin));
		meta.put("trade.speed", new SpeedStat(speed, main.plugin));
		Car c = new Car(false, meta);
		return c;
	}
}
