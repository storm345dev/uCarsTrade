package net.stormdev.ucarstrade.cars;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.trade.main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

import com.useful.ucars.ItemStackFromId;

public class CarPresets {
	public static boolean isCarPresetsUsed = false;
	public static boolean isCarAllowedRename = true;
	private static List<CarPreset> carPresets = new ArrayList<CarPreset>();
	
	public static List<CarPreset> getPresets(){
		return new ArrayList<CarPreset>(carPresets);
	}
	
	public static void addCarPreset(CarPreset preset){
		carPresets.add(preset);
	}
	
	public static void setCarPresetsUsed(boolean b){
		isCarPresetsUsed = b;
	}
	
	public static boolean isCarPresetsUsed(){
		return isCarPresetsUsed;
	}
	
	public static void init(FileConfiguration config){
		if(!config.contains("general.cars.presets.enabled")){
			config.set("general.cars.presets.enabled", false);
			config.set("general.cars.presets.ferarriF50.name", "Ferrari F50");
			config.set("general.cars.presets.ferarriF50.speed", 1.5d);
			config.set("general.cars.presets.ferarriF50.health", 100d);
			config.set("general.cars.presets.lotusesprit.name", "Lotus Esprit");
			config.set("general.cars.presets.lotusesprit.speed", 1.2d);
			config.set("general.cars.presets.lotusesprit.health", 110d);
			config.set("general.cars.presets.bugattiveyron.name", "Bugatti Veyron");
			config.set("general.cars.presets.bugattiveyron.speed", 2.0d);
			config.set("general.cars.presets.bugattiveyron.health", 75d);
			config.set("general.cars.presets.geewhizz.name", "G-Whizz");
			config.set("general.cars.presets.geewhizz.speed", 0.25d);
			config.set("general.cars.presets.geewhizz.health", 50d);
			config.set("general.cars.presets.mini.name", "Mini Cooper");
			config.set("general.cars.presets.mini.speed", 1.00d);
			config.set("general.cars.presets.mini.health", 50d);
			config.set("general.cars.presets.terrafugiatransition.name", "Terrafugia Transition");
			config.set("general.cars.presets.terrafugiatransition.speed", 1.0d);
			config.set("general.cars.presets.terrafugiatransition.health", 100d);
			config.set("general.cars.presets.terrafugiatransition.modifiers", new String[]{"Hover Upgrade"});
		}
		if(!config.contains("general.cars.presets.allowRename")){
			config.set("general.cars.presets.allowRename", false);
		}
		CarPresets.isCarAllowedRename = config.getBoolean("general.cars.presets.allowRename");
		setCarPresetsUsed(config.getBoolean("general.cars.presets.enabled"));
		if(!isCarPresetsUsed()){
			main.logger.info("Car presets are NOT enabled! Cars will be randomly generated!");
			return;
		}
		
		ConfigurationSection cars = config.getConfigurationSection("general.cars.presets");
		for(String key:cars.getKeys(false)){
			ConfigurationSection carSect = cars.getConfigurationSection(key);
			if(carSect == null){
				if(key != null && !key.equals("enabled")){
					main.logger.info("ERROR: No information for car preset: "+key);
				}
				continue;
			}
			if(!carSect.contains("name") || !carSect.contains("speed") || !carSect.contains("health")){
				main.logger.info("ERROR: Car preset "+key+" doesn't have all necessary fields! (name, speed, health and optionally hover)");
				continue;
			}
			String name = carSect.getString("name");
			double speed = carSect.getDouble("speed");
			double health = carSect.getDouble("health");
			List<String> modifiers = new ArrayList<String>();
			if(carSect.contains("modifiers")){
				modifiers = carSect.getStringList("modifiers");
			}
			double accelMod = carSect.contains("acceleration") ? carSect.getDouble("acceleration") / 10.0d : 1;
			double turnAmountPerTick = carSect.contains("handling") ? carSect.getDouble("handling") / 10.0d : 5;
			MaterialData displayBlock = null;
			int offset = 0;
			if(carSect.contains("display")){
				ItemStack is = ItemStackFromId.get(carSect.getString("display"));
				displayBlock = is.getData();
			}
			if(carSect.contains("displayOffset")){
				offset = carSect.getInt("displayOffset");
			}
			addCarPreset(new CarPreset(name, speed, health, accelMod, turnAmountPerTick, modifiers, displayBlock, offset));
		}
		
		main.logger.info("Loaded "+getPresets().size() + " car presets!");
	}
	
	public static class CarPreset {
		private String name;
		private double speed;
		private double health;
		private double acceleration;
		private double turnAmountPerTick;
		private MaterialData displayBlock;
		private int displayBlockOffset = 0;
		private List<String> modifiers = new ArrayList<String>();
		public CarPreset(String name, double speed, double health, double accelMod, double turnAmountPerTick, List<String> modifiers, MaterialData displayBlock, int displayBlockOffset){
			this.name = name;
			this.speed = speed;
			this.health = health;
			this.modifiers = modifiers;
			this.setAcceleration(accelMod);
			this.setTurnAmountPerTick(turnAmountPerTick);
			this.setDisplayBlock(displayBlock);
			this.setDisplayBlockOffset(displayBlockOffset);
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
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
		public List<String> getModifications() {
			return this.modifiers;
		}
		public void setModifications(List<String> modifiers) {
			this.modifiers = modifiers;
		}
		public double getAcceleration() {
			return acceleration;
		}
		public void setAcceleration(double acceleration) {
			this.acceleration = acceleration;
		}
		public double getTurnAmountPerTick() {
			return turnAmountPerTick;
		}
		public void setTurnAmountPerTick(double turnAmountPerTick) {
			this.turnAmountPerTick = turnAmountPerTick;
		}
		public MaterialData getDisplayBlock() {
			return displayBlock;
		}
		public void setDisplayBlock(MaterialData displayBlock) {
			this.displayBlock = displayBlock;
		}
		public boolean hasDisplayBlock(){
			return this.displayBlock != null;
		}
		public int getDisplayBlockOffset() {
			return displayBlockOffset;
		}
		public void setDisplayBlockOffset(int displayBlockOffset) {
			this.displayBlockOffset = displayBlockOffset;
		}
		public ItemStack toItemStack(){
			return new DrivenCar(this).toItemStack();
		}
	}
}
