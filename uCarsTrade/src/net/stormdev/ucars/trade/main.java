package net.stormdev.ucars.trade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import net.stormdev.ucars.utils.ItemRename;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import com.useful.ucars.Colors;
import com.useful.ucars.ucars;

public class main extends JavaPlugin {
	public static YamlConfiguration lang = new YamlConfiguration();
	public static main plugin;
	public static FileConfiguration config = new YamlConfiguration();
	public static Colors colors; 
	public static CustomLogger logger = null;
	public static ucars ucars = null;
	public static UTradeCommandExecutor cmdExecutor = null;
	public static UTradeListener listener = null;
	public static Random random = new Random();
	public CarSaver carSaver = null;
	public CarCalculations carCals = null;
	
	public void onEnable(){
		plugin = this;
		File langFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "lang.yml");
		if (langFile.exists() == false
				|| langFile.length() < 1) {
			try {
				langFile.createNewFile();
				// newC.save(configFile);
			} catch (IOException e) {
			}
			
		}
		try {
			lang.load(langFile);
		} catch (Exception e1) {
			getLogger().log(Level.WARNING, "Error creating/loading lang file! Regenerating..");
		}
		if (new File(getDataFolder().getAbsolutePath() + File.separator
				+ "config.yml").exists() == false
				|| new File(getDataFolder().getAbsolutePath() + File.separator
						+ "config.yml").length() < 1) {
			getDataFolder().mkdirs();
			File configFile = new File(getDataFolder().getAbsolutePath()
					+ File.separator + "config.yml");
			try {
				configFile.createNewFile();
			} catch (IOException e) {
			}
			copy(getResource("ucarsTradeConfigHeader.yml"), configFile);
		}
		config = getConfig();
		logger = new CustomLogger(getServer().getConsoleSender(), getLogger());
        try {
        	if (!config.contains("general.logger.colour")) {
				config.set("general.logger.colour", true);
			}
        	if (!config.contains("general.cars.names")) {
        		ArrayList<String> names = new ArrayList<String>();
            	names.add("Car");
            	names.add("Peugot");
            	names.add("Ferrari");
            	names.add("Lotus");
            	names.add("Pagani");
            	names.add("SmartCar");
            	names.add("Maclaren");
            	names.add("PimpMobile");
            	names.add("SwagMobile");
            	names.add("Ford");
            	names.add("Koinsegg");
            	names.add("ArielAtom");
            	names.add("TravelWagon");
            	names.add("GeneralMotor");
            	names.add("ValueCar");
				config.set("general.cars.names", names);
			}
        	//Setup the colour scheme
        	if (!config.contains("colorScheme.success")) {
				config.set("colorScheme.success", "&a");
			}
			if (!config.contains("colorScheme.error")) {
				config.set("colorScheme.error", "&c");
			}
			if (!config.contains("colorScheme.info")) {
				config.set("colorScheme.info", "&e");
			}
			if (!config.contains("colorScheme.title")) {
				config.set("colorScheme.title", "&9");
			}
			if (!config.contains("colorScheme.tp")) {
				config.set("colorScheme.tp", "&5");
			}
        } catch(Exception e){
        }
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		//Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		logger.info("Config loaded!");
		logger.info("Searching for uCars...");
		Plugin[] plugins = getServer().getPluginManager().getPlugins();
		Boolean installed = false;
		for(Plugin p:plugins){
			if(p.getName().equals("uCars")){
			installed = true;
			ucars = (com.useful.ucars.ucars) p;
			}
		}
		if(!installed){
			logger.info("Unable to find uCars!");
			getServer().getPluginManager().disablePlugin(this);
		}
		ucars.hookPlugin(this);
		logger.info("uCars found and hooked!");
		PluginDescriptionFile pldesc = plugin.getDescription();
		Map<String, Map<String, Object>> commands = pldesc.getCommands();
		Set<String> keys = commands.keySet();
		main.cmdExecutor = new UTradeCommandExecutor(this);
		for (String k : keys) {
			try {
				getCommand(k).setExecutor(cmdExecutor);
			} catch (Exception e) {
				getLogger().log(Level.SEVERE,
						"Error registering command " + k.toString());
				e.printStackTrace();
			}
		}
		main.listener = new UTradeListener(this);
		getServer().getPluginManager().registerEvents(main.listener,
				this);
		ItemStack car = new ItemStack(Material.MINECART);
		car = ItemRename.rename(car, "Car");
		ShapedRecipe carRecipe = new ShapedRecipe(car);
		carRecipe.shape("012","345","678");
		carRecipe.setIngredient('0', Material.REDSTONE);
		carRecipe.setIngredient('1', Material.LEVER);
		carRecipe.setIngredient('2', Material.REDSTONE);
		carRecipe.setIngredient('3', Material.IRON_INGOT);
		carRecipe.setIngredient('4', Material.REDSTONE);
		carRecipe.setIngredient('5', Material.IRON_INGOT);
		carRecipe.setIngredient('6', Material.IRON_INGOT);
		carRecipe.setIngredient('7', Material.IRON_INGOT);
		carRecipe.setIngredient('8', Material.IRON_INGOT);
		getServer().addRecipe(carRecipe);
		File carSaveFile = new File(getDataFolder()+File.separator+".carData");
		if(carSaveFile.length() < 1 || !carSaveFile.exists()){
			try {
				carSaveFile.createNewFile();
			} catch (IOException e) {
				main.logger.info(colors.getError()+"Failed to create new car data file!");
			}
		}
		this.carSaver = new CarSaver(carSaveFile);
		this.carSaver.load();
		this.carCals = new CarCalculations();
		logger.info("uCarsTrade v"+plugin.getDescription().getVersion()+" has been enabled!");
	}
	
	public void onDisable(){
		if(ucars != null){
		ucars.unHookPlugin(this);
		}
		logger.info("uCarsTrade has been disabled!");
	}
	
	private void copy(InputStream in, File file) {
		try {
			OutputStream out = new FileOutputStream(file);
			byte[] buf = new byte[1024];
			int len;
			while ((len = in.read(buf)) > 0) {
				out.write(buf, 0, len);
				// System.out.write(buf, 0, len);
			}
			out.close();
			in.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static String colorise(String prefix) {
		 return ChatColor.translateAlternateColorCodes('&', prefix);
	}
}
