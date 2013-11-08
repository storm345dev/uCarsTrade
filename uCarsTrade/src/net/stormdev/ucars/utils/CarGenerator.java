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
		if(rand == 10){ //1 in 10 chance
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
		meta.put("trade.speed", new SpeedStat(speeD, main.plugin));
		Car c = new Car(false, meta);
		return c;
	}
}
