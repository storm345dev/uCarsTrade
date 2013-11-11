package net.stormdev.ucars.utils;

import java.util.HashMap;

import com.useful.ucars.ucars;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.stats.Stat;

public class CarValueCalculator {
	public static double getCarValueForSale(Car car){
		double average = main.config.getDouble("general.carTrading.averageCarValue");
		double rating = 20;
		HashMap<String, Stat> stats = car.getStats();
		boolean handlingDamaged = false;
		double speed = 1;
		String name = "Car";
		double defaultHealth = ucars.config.getDouble("general.cars.health.default");
		double maxHealth = ucars.config.getDouble("general.cars.health.max");
		double health = defaultHealth;
		if(stats.containsKey("trade.handling")){
			try {
				handlingDamaged = (Boolean) stats.get("trade.handling").getValue();
			} catch (Exception e) {
				handlingDamaged = true; //Well it did have the stat set!
			}
		}
		if(stats.containsKey("trade.speed")){
			try {
				speed = (Double) stats.get("trade.speed").getValue();
			} catch (Exception e) {
				speed = 1;
			}
		}
		if(stats.containsKey("trade.name")){
			name = stats.get("trade.name").toString();
		}
		if(stats.containsKey("trade.health")){
			try {
				health = (Double) stats.get("trade.health").getValue();
			} catch (Exception e) {
				//Use default health
			}
		}
		if(handlingDamaged){
			rating = rating - 50;
		}
		if(speed < 1){
			rating = rating - (10*speed); //0.8->8, etc...
		}
		else if(speed == 1){
			rating = rating + 1;
		}
		else if(speed > 3){
			rating = rating - (3-5*speed);
		}
		else {
			rating = rating + (10*speed);
		}
		if(name.length() > 16){
			rating = rating - 2;
		}
		if(health < defaultHealth){
			rating = rating - (defaultHealth - health);
		}
		else if(health == defaultHealth){
			rating = rating + 5;
		}
		else{
			double h = (health/maxHealth * 100) - defaultHealth + 5;
			rating = rating + h;
		}
		int percentExtra = 0;
	    Boolean worse = rating < 0;
		// ratings - -30 is normally worst, -24 = crap car, 26 = average car, 100+ is great
		if(rating < 0){
			percentExtra = percentExtra + 10;
			if(rating < -5){
				percentExtra = percentExtra + 5;
			}
			if(rating < -20){
				percentExtra = percentExtra + 10; //25 so far	
			}
			if(rating < -25){
				percentExtra = 50; //Horrible car!
			}
		}
		else{ //Rating is more than 0
			if(rating < 10){
				percentExtra = 0;
			}
			else{
			percentExtra = (int)rating; //Round to closest int
			}
		}
		double cost = average;
		if(worse){
			cost = cost - ((percentExtra*average)/100);
		}
		else{
			cost = cost + ((percentExtra*average)/100);
		}
		if(cost < 0.1){
			cost = 0.1;
		}
		return Math.round(cost*100)/100; //Round to 2 d.p
	}
	public static double getCarValueForPurchase(Car car){
		double n = getCarValueForSale(car);
		double cost = n + (12.5*n)/100; //Add 12.5 to the value it's worth
		return Math.round(cost*100)/100; //Round to 2 d.p
	}
}
