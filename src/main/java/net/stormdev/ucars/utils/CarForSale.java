package net.stormdev.ucars.utils;

import java.io.Serializable;
import java.util.UUID;

import net.stormdev.ucarstrade.cars.DrivenCar;
import net.stormdev.ucarstrade.displays.DisplayType;

public class CarForSale implements Serializable {
	private static final long serialVersionUID = 2L;
	private DrivenCar car = null;
	private String seller = null;
	private double price = 1.0;
	private double profit = 1.0;
	private DisplayType modifier = null;
	private UUID uuid = UUID.randomUUID();
	public CarForSale(DrivenCar car, String sellerName, double price, double profitForSeller,
			DisplayType modifier){
		this.uuid = UUID.randomUUID();
		this.car = car;
		this.seller = sellerName;
		this.price = price;
		this.profit = profitForSeller;
		this.modifier = modifier;
	}
	public String getSeller(){
		return seller;
	}
	public UUID getUUID(){
		return uuid;
	}
	public DrivenCar getCar(){
		return car;
	}
    public double getPrice(){
    	return price;
    }
    public double getProfit(){
    	try {
			return this.profit;
		} catch (Exception e) {
			return getPrice();
		}
    }
    public Boolean hasModifier(){
    	return modifier != null;
    }
    public DisplayType getModifier(){
    	return modifier;
    }
}
