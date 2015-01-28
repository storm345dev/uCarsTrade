package net.stormdev.ucarstrade.cars;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.trade.main;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

public class CarPresets {
	private static boolean isCarPresetsUsed = false;
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
			addCarPreset(new CarPreset(name, speed, health, modifiers));
		}
		
		main.logger.info("Loaded "+getPresets().size() + " car presets!");
	}
	
	public static class CarPreset {
		private String name;
		private double speed;
		private double health;
		private List<String> modifiers = new ArrayList<String>();
		public CarPreset(String name, double speed, double health, List<String> modifiers){
			this.name = name;
			this.speed = speed;
			this.health = health;
			this.modifiers = modifiers;
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
		
		
	}
}
