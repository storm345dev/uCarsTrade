package net.stormdev.ucars.utils;

import java.io.Serializable;
import java.util.UUID;

public class CarForSale implements Serializable {
	private static final long serialVersionUID = -5344858010281860425L;
    
	UUID carId = null;
	String seller = null;
	double price = 1.0;
	double profit = 1.0;
	public CarForSale(UUID carId, String sellerName, double price, double profitForSeller){
		this.carId = carId;
		this.seller = sellerName;
		this.price = price;
		this.profit = profitForSeller;
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
}
