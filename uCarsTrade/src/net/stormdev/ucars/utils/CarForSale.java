package net.stormdev.ucars.utils;

import java.io.Serializable;
import java.util.UUID;

public class CarForSale implements Serializable {
	private static final long serialVersionUID = -5344858010281860425L;
	private UUID carId = null;
	private String seller = null;
	private double price = 1.0;
	private double profit = 1.0;
	private DisplayType modifier = null;
	public CarForSale(UUID carId, String sellerName, double price, double profitForSeller,
			DisplayType modifier){
		this.carId = carId;
		this.seller = sellerName;
		this.price = price;
		this.profit = profitForSeller;
		this.modifier = modifier;
	}
	public String getSeller(){
		return seller;
	}
	public UUID getCarId(){
		return carId;
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
