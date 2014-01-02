package net.stormdev.ucarsTrade.utils;

import java.io.Serializable;
import java.util.List;

import net.stormdev.mario.mariokart.main;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class DisplayType implements Serializable {
	private static final long serialVersionUID = 4323952036275549097L;

	public static void putEntityInCar(Minecart car, Entity entity){
		car.setPassenger(entity);
		return;
	}
	
	public static void putEntityInCar(Minecart car, EntityType type){
		Chunk c = car.getLocation().getChunk();
		if(!c.isLoaded()){
			c.load(true);
		}
		Entity e = car.getWorld().spawnEntity(car.getLocation(), type);
		putEntityInCar(car, e);
		return;
	}
	public static Entity spawnEntityAtCar(Minecart car, EntityType type){
		Chunk c = car.getLocation().getChunk();
		if(!c.isLoaded()){
			c.load(true);
		}
		Entity e = car.getWorld().spawnEntity(car.getLocation(), type);
		return e;
	}
	
	private String name;
	private CarFiller filler;
	private Boolean restrictedJumping;
	private double carValueRatingBonus;
	private Material displayItem;
	private Material upgradeNeeded;
	private int amount;
	private List<String> lore;
	protected DisplayType(String name, CarFiller filler, Boolean restrictedJumping,
			double carValueRatingBonus, Material displayItem, Material upgradeNeeded, int amount,
			List<String> lore){
		this.name = name;
		this.filler = filler;
		this.restrictedJumping = restrictedJumping;
		this.carValueRatingBonus = carValueRatingBonus;
		this.displayItem = displayItem;
		this.amount = amount;
		this.upgradeNeeded = upgradeNeeded;
		this.lore = lore;
	}
	public ItemStack getDisplayItemForShop(){
		ItemStack i = new ItemStack(displayItem);
	    ItemMeta im = i.getItemMeta();
	    im.setDisplayName(main.colors.getTitle()+getName());
	    im.setLore(lore);
	    i.setItemMeta(im);
	    return i;
	}
	public ItemStack getUpgradeItemStack(){
		ItemStack i = new ItemStack(upgradeNeeded);
		i.setAmount(amount);
		return i;
	}
	public Boolean isJumpingRestriced(){
		return restrictedJumping;
	}
	public double getCarValueRatingBonus(){
		return carValueRatingBonus;
	}
	public String getName(){
		return name;
	}
	public CarFiller getFiller(){
		return filler;
	}
	public void fill(Minecart car, Player owner){
		getFiller().putInCar(car, owner);
		return;
	}
}
