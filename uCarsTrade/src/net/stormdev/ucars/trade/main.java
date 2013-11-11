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

import net.milkbowl.vault.economy.Economy;
import net.stormdev.ucars.utils.IconMenu;
import net.stormdev.ucars.utils.ItemRename;
import net.stormdev.ucars.utils.SalesManager;
import net.stormdev.ucars.utils.TradeBoothClickEvent;
import net.stormdev.ucars.utils.TradeBoothMenuType;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
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
	public ShapedRecipe carRecipe = null;
	public Boolean mariokartInstalled = false;
	public Boolean uCarsRaceInstalled = false;
	public IconMenu tradeMenu = null;
	public SalesManager salesManager = null;
	public static Economy economy = null;
	
	protected boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
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
			if(!lang.contains("general.place.msg")){
				lang.set("general.place.msg", "Placed car %name%, cars can be driven with similar controls to a horse!");
			}
			if(!lang.contains("general.upgrade.msg")){
				lang.set("general.upgrade.msg", "&a+%amount% &e%stat%. Value: %value%");
			}
			if(!lang.contains("title.carTrading")){
				lang.set("title.carTrading", "Car Trading");
			}
			if(!lang.contains("title.trade.buyCars")){
				lang.set("title.trade.buyCars", "Buy Cars");
			}
			if(!lang.contains("title.trade.buyUpgrades")){
				lang.set("title.trade.buyUpgrades", "Buy Upgrades");
			}
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
        	if (!config.contains("general.carTrading.enable")) {
				config.set("general.carTrading.enable", true);
			}
        	if (!config.contains("general.carTrading.averageCarValue")) {
				config.set("general.carTrading.averageCarValue", 459.99d);
			}
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
		com.useful.ucars.ucars.plugin.ucarsTrade = true;
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
		car.setDurability((short) 20);
		car = ItemRename.rename(car, "Car");
		this.carRecipe = new ShapedRecipe(car);
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
		if(getServer().getPluginManager().getPlugin("uCarsRace") != null){
			uCarsRaceInstalled = true;
		}
		if(getServer().getPluginManager().getPlugin("MarioKart") != null){
			mariokartInstalled = true;
		}
        tradeMenu = new IconMenu(main.colors.getTitle()+Lang.get("title.carTrading"), 9, new IconMenu.OptionClickEventHandler() {
            public void onOptionClick(IconMenu.OptionClickEvent event) {
            	event.setWillClose(true);
            	TradeBoothClickEvent evt = new TradeBoothClickEvent(event, TradeBoothMenuType.MENU, 1);
            	plugin.getServer().getPluginManager().callEvent(evt);
            }
        }, plugin);
        tradeMenu.setOption(0, new ItemStack(Material.BOOK, 1), colors.getTitle()+"Read Tutorial", colors.getInfo()+"Read the tutorial!");
        tradeMenu.setOption(1, new ItemStack(Material.MINECART, 1), colors.getTitle()+"Buy Cars", colors.getInfo()+"Buy Cars!");
        tradeMenu.setOption(2, new ItemStack(Material.MINECART, 1), colors.getTitle()+"Sell Cars", colors.getInfo()+"Sell cars!");
        tradeMenu.setOption(3, new ItemStack(Material.IRON_INGOT, 1), colors.getTitle()+"Buy Upgrades", colors.getInfo()+"Buy upgrades for your cars!");
        tradeMenu.setOption(4, new ItemStack(Material.IRON_INGOT, 1), colors.getTitle()+"Sell Upgrades", colors.getInfo()+"Sell upgrades for cars!");
		File carsMarketSaveFile = new File(getDataFolder().getAbsolutePath() + File.separator + "carsMarket.marketData");
		File upgradesMarketSaveFile = new File(getDataFolder().getAbsolutePath() + File.separator + "upgradesMarket.marketData");
		this.salesManager = new SalesManager(carsMarketSaveFile, upgradesMarketSaveFile);
		if(config.getBoolean("general.carTrading.enable")){
			if(!setupEconomy()){
				logger.info("Economy plugin not found on startup. An economy plugin and vault are needed"
						+ "for trade features. If one is successfully installed then don't worry. We'll "
						+ "find it later.");
				//Don't bother to save
			}
		}
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
