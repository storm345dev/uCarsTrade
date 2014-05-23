package net.stormdev.ucarstrade.cars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.utils.Car;
import net.stormdev.ucars.utils.ItemRename;
import net.stormdev.ucarstrade.ItemCarValidation;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.ucars.ucars;

public class DrivenCar implements Serializable {
	private static final long serialVersionUID = 1L;
	
	private UUID id;
	private String name = "Car";
	private double speed;
	private double health;
	private boolean isHandlingDamaged;
	private List<String> modifiers = new ArrayList<String>();
	private boolean isNPC = false;

	public static DrivenCar convert(Car c){
		try {
			ItemStack i = c.getItem();
			List<String> lore = i.getItemMeta().getLore();
			lore.remove(0);
			return ItemCarValidation.getCarFromLore(i.getItemMeta().getDisplayName(), lore);
		} catch (Exception e) {
			// Invalid
			return null;
		}
	}
	
	public DrivenCar(String name, double speed, double health, boolean isHandlingDamaged, List<String> modifiers){
		this.setName(name);
		this.setSpeed(speed);
		this.setHealth(health);
		this.setHandlingDamaged(isHandlingDamaged);
		this.setModifiers(modifiers);
	}
	
	public ItemStack toItemStack(){
		ItemStack stack = new ItemStack(Material.MINECART);
		List<String> lore = new ArrayList<String>();
		ItemMeta meta = stack.getItemMeta();
		
		lore.add("[Speed:] "+main.colors.getInfo()+speed+"x");
		double max = ucars.config.getDouble("general.cars.health.max");
		lore.add("[Health:] "+main.colors.getInfo()+health+"/"+max);
		lore.add("[Handling:] "+main.colors.getInfo()+isHandlingDamaged);
		for(String x:modifiers){
			lore.add("-Modifier: "+x);
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
		stack = ItemRename.rename(stack, name);
		return stack;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getHealth() {
		return health;
	}

	public void setHealth(double health) {
		this.health = health;
	}

	public boolean isHandlingDamaged() {
		return isHandlingDamaged;
	}

	public void setHandlingDamaged(boolean isHandlingDamaged) {
		this.isHandlingDamaged = isHandlingDamaged;
	}

	public List<String> getModifiers() {
		return modifiers;
	}

	public void setModifiers(List<String> modifiers) {
		this.modifiers = modifiers;
	}

	public UUID getId() {
		return id;
	}

	public void setId(UUID id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isNPC() {
		return isNPC;
	}

	public DrivenCar setNPC(boolean isNPC) {
		this.isNPC = isNPC;
		return this;
	}
	
	
}
