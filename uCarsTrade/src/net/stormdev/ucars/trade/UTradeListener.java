package net.stormdev.ucars.trade;

import net.stormdev.ucars.utils.Car;
import net.stormdev.ucars.utils.CarGenerator;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.CraftItemEvent;

public class UTradeListener implements Listener {
	main plugin = null;
	public UTradeListener(main plugin){
		this.plugin = plugin;
	}
	@EventHandler(priority = EventPriority.MONITOR)
	void itemCraft(CraftItemEvent event){
		if(event.isCancelled()){
			return;
		}
		Car car = CarGenerator.gen();
        event.setCurrentItem(car.getItem());
        main.plugin.carSaver.cars.put(car.getId(), car);
        main.plugin.carSaver.save();
		return;
	}
	//TODO Manage actual car events
	

}
