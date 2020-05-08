package net.stormdev.ucarstrade.displays;

import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class DisplayManager {
	
	public static void fillCar(Entity vehicle, DrivenCar car, Player owner){
		//TODO Put correct display items in car
		if(car.getModifiers().size() > 0){
			for(int i=0;i<car.getModifiers().size();i++){
				DisplayType t = Displays.getFromName(car.getModifiers().get(i));
				if(t != null){
					t.fill(vehicle, owner);
				}
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
