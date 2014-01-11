package net.stormdev.ucars.trade;

import net.stormdev.ucars.stats.Stat;
import net.stormdev.ucars.utils.Car;
import net.stormdev.ucars.utils.DisplayType;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class DisplayManager {
	
	public static void fillCar(Minecart vehicle, Car car, Player owner){
		//TODO Put correct display items in car
		if(car.getStats().containsKey("trade.display")){
			Stat display = car.getStats().get("trade.display");
			DisplayType displayFiller = (DisplayType) display.getValue();
			//Put in car
			displayFiller.fill(vehicle, owner);
		}
		return;
	}
}
