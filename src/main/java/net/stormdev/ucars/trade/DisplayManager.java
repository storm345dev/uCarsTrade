package net.stormdev.ucars.trade;

import net.stormdev.ucars.stats.Stat;
import net.stormdev.ucars.utils.DisplayType;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class DisplayManager {
	
	public static void fillCar(Minecart vehicle, DrivenCar car, Player owner){
		//TODO Put correct display items in car
		if(car.getModifiers().size() > 0){
			for(int i=0;i<car.getModifiers().size();i++){
				//TODO Make it work
			}
			
			/*
			Stat display = car.getStats().get("trade.display");
			DisplayType displayFiller = (DisplayType) display.getValue();
			//Put in car
			displayFiller.fill(vehicle, owner);
			*/
		}
		return;
	}
}
