package net.stormdev.ucarstrade.cars;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.utils.ItemRename;
import net.stormdev.ucarstrade.cars.CarPresets.CarPreset;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.MaterialData;

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
	private double accelMod = 1;
	private double turnAmountPerTick = 5;

	public DrivenCar(CarPreset cp){
		this(cp.getName(),cp.getSpeed(),cp.getHealth(),cp.getAcceleration(),cp.getTurnAmountPerTick(),false,cp.getModifications());
	}

	public DrivenCar(String name, double speed, double health, double acceleration, double turnAmountPerTick, boolean isHandlingDamaged, List<String> modifiers){
		this.setName(name);
		this.setSpeed(speed);
		this.setHealth(health);
		this.setHandlingDamaged(isHandlingDamaged);
		this.setModifiers(modifiers);
		this.setAccelMod(acceleration);
		this.setTurnAmountPerTick(turnAmountPerTick);
	}
	
	public MaterialData getBaseDisplayBlock(){
		if(!name.toLowerCase().contains("cop")){
			return null;
		}
		return new ItemStack(Material.EMERALD_ORE).getData();
	}
	
	public DrivenCar(DrivenCar other){
		this(other.getName(), other.getSpeed(), other.getHealth(), other.getAccelMod(), other.getTurnAmountPerTick(), other.isHandlingDamaged(), other.getModifiers());
		this.id = other.getId();
	}
	
	private static String getHandleString(boolean isDamaged){
		if(isDamaged){
			return "damaged";
		}
		return "undamaged";
	}
	
	public ItemStack toItemStack(){
		ItemStack stack = new ItemStack(Material.MINECART);
		List<String> lore = new ArrayList<String>();
		ItemMeta meta = stack.getItemMeta();
		lore.add(ChatColor.GRAY+"car");
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+speed+"x");
		double max = ucars.config.getDouble("general.cars.health.max");
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health+"/"+max);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+(10.0d*getTurnAmountPerTick())); //Eg 5degrees/tick = 50
		lore.add(main.colors.getTitle()+"[Acceleration:] "+main.colors.getInfo()+(getAccelMod()*10.0d));
		lore.add(main.colors.getTitle()+"[Damage:] "+main.colors.getInfo()+getHandleString(isHandlingDamaged));
		for(String x:modifiers){
			lore.add(main.colors.getTitle()+"-Modifier: "+main.colors.getInfo()+x);
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
		stack = ItemRename.rename(stack, name);
		return stack;
	}
	
	public boolean hasPreset(){
		return getPreset() != null;
	}
	
	private transient CarPreset preset;
	
	public CarPreset getPreset(){
		if(!CarPresets.isCarPresetsUsed()){
			return null;
		}
		if(preset != null){
			return preset;
		}
		List<CarPreset> cp = CarPresets.getPresets();
		for(CarPreset c:new ArrayList<CarPreset>(cp)){
			if(ChatColor.translateAlternateColorCodes('&', c.getName()).equals(this.name)){
				preset = c;
				return c;
			}
		}
		return null;
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
		if(this.isNPC && !isNPC){ //Being made NOT an NPC
			main.plugin.aiSpawns.decrementSpawnedAICount();
		}
		this.isNPC = isNPC;
		return this;
	}

	public double getTurnAmountPerTick() {
		if(this.turnAmountPerTick <= 0){
			this.turnAmountPerTick = 5;
		}
		return turnAmountPerTick;
	}

	public void setTurnAmountPerTick(double turnAmountPerTick) {
		this.turnAmountPerTick = turnAmountPerTick;
	}

	public double getAccelMod() {
		if(this.accelMod <= 0){
			this.accelMod = 1;
		}
		return accelMod;
	}

	public void setAccelMod(double accelMod) {
		this.accelMod = accelMod;
	}
	
	
}
