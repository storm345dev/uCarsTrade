package net.stormdev.ucarsTrade.utils;

import java.io.Serializable;
import java.util.UUID;

import net.stormdev.ucars.stats.StatType;

public class UpgradeForSale implements Serializable {
	private static final long serialVersionUID = -5344858010281860425L;
    
	UUID saleId = null;
	String seller = null;
	double price = 1.0;
	double profit = 1.0;
	StatType upgradeType = StatType.SPEED;
	int quantity = 1;
	public UpgradeForSale(UUID saleId, String sellerName, double price, StatType upgradeType, int quantity, double profit){
		this.saleId = saleId;
		this.seller = sellerName;
		this.price = price;
		this.upgradeType = upgradeType;
		this.quantity = quantity;
		this.profit = profit;
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
    public double getProfit(){
    	return profit;
    }
    public int getQuantity(){
    	return quantity;
    }
    public StatType getUpgradeType(){
    	return upgradeType;
    }
}
