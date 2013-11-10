package net.stormdev.ucars.utils;

import java.io.Serializable;
import java.util.UUID;

import net.stormdev.ucars.stats.StatType;

public class UpgradeForSale implements Serializable {
	private static final long serialVersionUID = -5344858010281860425L;
    
	UUID saleId = null;
	String seller = null;
	double price = 1.0;
	StatType upgradeType = StatType.SPEED;
	int quantity = 1;
	public UpgradeForSale(UUID saleId, String sellerName, double price, StatType upgradeType, int quantity){
		this.saleId = saleId;
		this.seller = sellerName;
		this.price = 1.0;
		this.upgradeType = upgradeType;
		this.quantity = quantity;
	}
	public String getSeller(){
		return seller;
	}
	public UUID getSaleId(){
		return saleId;
	}
    public double getPrice(){
    	return price;
    }
    public int getQuantity(){
    	return quantity;
    }
    public StatType getUpgradeType(){
    	return upgradeType;
    }
}
