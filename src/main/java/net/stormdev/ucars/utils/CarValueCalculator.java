package net.stormdev.ucars.utils;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucarstrade.cars.DrivenCar;

import com.useful.ucars.ucars;

public class CarValueCalculator {
	public static double getCarValueForSale(DrivenCar car){
		double average = main.config.getDouble("general.carTrading.averageCarValue");
		double rating = 20;
		boolean handlingDamaged = car.isHandlingDamaged();
		double speed = car.getSpeed();
		String name = car.getName();
		double defaultHealth = ucars.config.getDouble("general.cars.health.default");
		double maxHealth = ucars.config.getDouble("general.cars.health.max");
		double health = car.getHealth();
		double bonus = 0;
		rating += bonus;
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
			rating = rating - (3-(5*speed));
		}
		else {
			rating = rating + (15*speed);
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
		double handling = car.getTurnAmountPerTick();
		double accel = car.getAccelMod();
		rating *= (accel);
		rating *= (handling / 5.0);
		int percentExtra = 0;
		rating -= 50;
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
			if(rating < 5){
				percentExtra = 0;
			}
			else{
			percentExtra = ((int)rating-5)/2; //Round to closest int
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
	public static double getCarValueForPurchase(DrivenCar car){
		double n = getCarValueForSale(car);
		double cost = n + (main.config.getDouble("general.carTrading.VATPercent")*n)/100; //Add VAT% to the value it's worth
		return Math.round(cost*100)/100; //Round to 2 d.p
	}
}
