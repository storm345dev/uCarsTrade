package net.stormdev.ucars.trade.AIVehicles;

import net.stormdev.ucarstrade.cars.DrivenCar;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class CarStealEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private Entity vehicle;
    private Player stealer;
    private DrivenCar car;
	public CarStealEvent(Entity vehicle, Player stealer, DrivenCar car) {
		this.vehicle = vehicle;
		this.stealer = stealer;
		this.car = car;
	}
	public Entity getVehicle(){
		return vehicle;
	}
	public Player getStealer(){
		return stealer;
	}
	public DrivenCar getCar(){
		return car;
	}
	public HandlerList getHandlers() {
		return handlers;
	}
	public static HandlerList getHandlerList(){
		return handlers;
	}
}
