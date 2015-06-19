package net.stormdev.ucars.trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;

import net.milkbowl.vault.economy.Economy;
import net.stormdev.ucars.shops.CarShop;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.spawning.AISpawnManager;
import net.stormdev.ucars.trade.AIVehicles.spawning.AIWorldProbingSpawnManager;
import net.stormdev.ucars.trade.AIVehicles.spawning.SpawnMethod;
import net.stormdev.ucars.trade.AIVehicles.spawning.nodes.AINodesSpawnManager;
import net.stormdev.ucars.utils.IconMenu;
import net.stormdev.ucars.utils.ItemRename;
import net.stormdev.ucars.utils.SalesManager;
import net.stormdev.ucars.utils.TradeBoothClickEvent;
import net.stormdev.ucars.utils.TradeBoothMenuType;
import net.stormdev.ucarstrade.cars.CarPresets;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import com.useful.uCarsAPI.ItemCarCheck;
import com.useful.uCarsAPI.uCarsAPI;
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
	public HashMap<String, String> alerts = new HashMap<String, String>();
	File alertsFile = null;
	public AISpawnManager aiSpawns = null;
	public AIRouter aiController = null;
	public CarShop carShop = null;
	public BukkitTask lagReducer = null;
	public int carCache = 20;
	public SpawnMethod aiSpawnMethod = SpawnMethod.WORLD_PROBE;
	
	public boolean setupEconomy() {
		RegisteredServiceProvider<Economy> economyProvider = getServer()
				.getServicesManager().getRegistration(
						net.milkbowl.vault.economy.Economy.class);
		if (economyProvider != null) {
			economy = economyProvider.getProvider();
		}
		return (economy != null);
	}
	
	public void onEnable(){
		try {
			if(Double.parseDouble(com.useful.ucars.ucars.plugin.getDescription().getVersion())
					< 17){
				getLogger().log(Level.SEVERE, "uCarsTrade needs uCars v17 or newer to function with the new API!"
						+ "(Installed version: "+com.useful.ucars.ucars.plugin.getDescription().getVersion()+")");
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					//Oh well
				}
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
		} catch (NumberFormatException e2) {
			//Error determining uCarsVersion
		}
		plugin = this;
		getDataFolder().mkdirs();
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
			if(!lang.contains("general.noExit.msg")){
				lang.set("general.noExit.msg", "You may only exit in a clear area!");
			}
			if(!lang.contains("general.sell.msg")){
				lang.set("general.sell.msg", "Selling %item% for %price% on the market!");
			}
			if(!lang.contains("general.buy.notEnoughMoney")){
				lang.set("general.buy.notEnoughMoney", "You cannot afford that item! You only have %balance%!");
			}
			if(!lang.contains("general.steal.taken")){
				lang.set("general.steal.taken", "Car stolen, watch out for the cops!");
			}
			if(!lang.contains("general.buy.taken")){
				lang.set("general.buy.taken", "Sorry, somebody else just bought that item.");
			}
			if(!lang.contains("general.buy.success")){
				lang.set("general.buy.success", "Successfully bought %item% for %price%, you now have %balance%!");
			}
			if(!lang.contains("general.upgrade.msg")){
				lang.set("general.upgrade.msg", "&a+%amount% &e%stat%. Value: %value%");
			}
			if(!lang.contains("general.cmd.playersOnly")){
				lang.set("general.cmd.playersOnly", "Players Only!");
			}
			if(!lang.contains("general.cmd.give")){
				lang.set("general.cmd.give", "Given you a car!");
			}
			if(!lang.contains("general.hovercar.heightLimit")){
				lang.set("general.hovercar.heightLimit", "You may not hover beyond this height!");
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
			if(!lang.contains("title.trade.sellCars")){
				lang.set("title.trade.sellCars", "Sell a car");
			}
			if(!lang.contains("title.trade.sellUpgrades")){
				lang.set("title.trade.sellUpgrades", "Sell Upgrades");
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
        	if (!config.contains("general.car.damage")) {
				config.set("general.car.damage", true);
			}
        	if (!config.contains("general.car.safeExit")) {
				config.set("general.car.safeExit", true);
			}
        	if (!config.contains("general.car.cache")) {
				config.set("general.car.cache", 100);
			}
        	if (!config.contains("general.carTrading.enable")) {
				config.set("general.carTrading.enable", true);
			}
        	if (!config.contains("general.carTrading.currencySign")) {
				config.set("general.carTrading.currencySign", "$");
			}
        	if (!config.contains("general.carTrading.averageCarValue")) {
				config.set("general.carTrading.averageCarValue", 29.99);
			}
        	if (!config.contains("general.carTrading.upgradeValue")) {
				config.set("general.carTrading.upgradeValue", 5.00);
			}
        	if (!config.contains("general.carTrading.VATPercent")) {
				config.set("general.carTrading.VATPercent", 12.50);
			}
        	if (!config.contains("general.hoverCar.heightLimit")) {
				config.set("general.hoverCar.heightLimit", 256.0);
			}
        	if (!config.contains("general.ai.enable")) {
				config.set("general.ai.enable", false);
			}
        	if (!config.contains("general.ai.trackerBlock.a")) {
				config.set("general.ai.trackerBlock.a", "DIAMOND_ORE");
			}
        	if (!config.contains("general.ai.trackerBlock.b")) {
				config.set("general.ai.trackerBlock.b", "EMERALD_ORE");
			}
        	if (!config.contains("general.ai.trackerBlock.c")) {
				config.set("general.ai.trackerBlock.c", "REDSTONE_ORE");
			}
        	if (!config.contains("general.ai.trackerBlock.d")) {
				config.set("general.ai.trackerBlock.d", "GOLD_ORE");
			}
        	if (!config.contains("general.ai.trackerBlock.pattern")) {
				config.set("general.ai.trackerBlock.pattern", "a,b,c,d");
			}
        	if (!config.contains("general.ai.roadEdgeBlock")) {
				config.set("general.ai.roadEdgeBlock", "IRON_ORE");
			}
        	if (!config.contains("general.ai.junctionBlock")) {
				config.set("general.ai.junctionBlock", "COAL_ORE");
			}
        	if (!config.contains("general.ai.names")) {
				config.set("general.ai.names", new String[]{"Jeff", "Bob", "Todd", "Jimmy", "Peter"
						, "Fred"});
			}
        	if (!config.contains("general.ai.limit")) {
				config.set("general.ai.limit", 69);
			}
        	if (!config.contains("general.ai.spawnMethod")) {
				config.set("general.ai.spawnMethod", SpawnMethod.WORLD_PROBE.name());
			}
        	if (!config.contains("general.ai.canSteal")) {
				config.set("general.ai.canSteal", true);
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
        	e.printStackTrace();
        }
		
		carCache = config.getInt("general.car.cache");
		
		//Load the colour scheme
		colors = new Colors(config.getString("colorScheme.success"),
				config.getString("colorScheme.error"),
				config.getString("colorScheme.info"),
				config.getString("colorScheme.title"),
				config.getString("colorScheme.title"));
		
		CarPresets.init(config);
		
		logger.info("Config loaded!");
		
		saveConfig();
		try {
			lang.save(langFile);
		} catch (IOException e1) {
			getLogger().info("Error parsing lang file!");
		}
		
		alertsFile = new File(getDataFolder().getAbsolutePath()
				+ File.separator + "alerts.stringMap");
		alertsFile.getParentFile().mkdirs();
		if(alertsFile.length()<1||!alertsFile.exists()){
			try {
				alertsFile.createNewFile();
			} catch (IOException e) {
				main.logger.info(main.colors.getError()+"Failed to create Alerts File");
			}
		}
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
		File carSaveFile = new File(getDataFolder()+File.separator+"cardata.cache");
		if(carSaveFile.length() < 1 || !carSaveFile.exists()){
			try {
				carSaveFile.getParentFile().mkdirs();
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
		
        this.carShop = new CarShop(this);
        
        uCarsAPI.getAPI().registerItemCarCheck(this, new ItemCarCheck(){

			@Override
			public Boolean isACar(ItemStack arg0) {
				return false; //Let us handle it
			}});
        
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
		this.alerts = loadHashMapAlerts(alertsFile.getAbsolutePath());
		if(this.alerts == null){
			this.alerts = new HashMap<String, String>();
		}
		
		if(config.getBoolean("general.ai.enable")){
			String spawnMethodRaw = config.getString("general.ai.spawnMethod");
			try {
				SpawnMethod method = SpawnMethod.valueOf(spawnMethodRaw);
				if(method == null){throw new Exception();}
				this.aiSpawnMethod = method;
			} catch (Exception e) {
				getLogger().info("INVALID AI spawn method set, it must be either 'WORLD_PROBE' or 'NODES'");
			}
		}
		if(this.aiSpawnMethod.equals(SpawnMethod.WORLD_PROBE)){
			this.aiSpawns = new AIWorldProbingSpawnManager(this, config.getBoolean("general.ai.enable"));
		}
		else if(this.aiSpawnMethod.equals(SpawnMethod.NODES)){
			initNodeAISpawnManager();
		}
		this.aiController = new AIRouter(config.getBoolean("general.ai.enable"));
		
        logger.info("uCarsTrade v"+plugin.getDescription().getVersion()+" has been enabled!");
	}
	
	public void onDisable(){
		try {
			if(alertsFile.length()<1||!alertsFile.exists()){
				try {
					alertsFile.createNewFile();
				} catch (IOException e) {
					main.logger.info(main.colors.getError()+"Failed to create Alerts File");
				}
			}
			saveHashMapAlerts(alerts, alertsFile.getAbsolutePath());
			if(ucars != null){
			ucars.unHookPlugin(this);
			}
			this.aiSpawns.shutdown();
			for(World w:Bukkit.getWorlds()){
				for(Entity e:new ArrayList<Entity>(w.getEntities())){
					try {
						if(e.getType().equals(EntityType.MINECART) && e.hasMetadata("trade.npc")){
							final DrivenCar c = plugin.carSaver.getCarInUse(e.getUniqueId());
							if(c == null
									|| !c.isNPC()){
								continue; //Not a car or not an npc car
							}
							AIRouter.despawnNPCCarNow(((Minecart)e), c);
							e.remove();
						}
					} catch (Exception e1) {
						//Oki....
						e1.printStackTrace();
					}
				}
			}
			this.carShop.destroy();
			Bukkit.getScheduler().cancelTasks(this);
			logger.info("uCarsTrade has been disabled!");
		} catch (Exception e) {
			//Disabled without being enabled
		} finally {
			getLogger().info("Disabled uCarsTrade!");
		}
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
	@SuppressWarnings("unchecked")
	public static HashMap<String, String> loadHashMapAlerts(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			return (HashMap<String, String>) result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static void saveHashMapAlerts(HashMap<String, String> map, String path)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			//Handle I/O exceptions
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public void putBlockInCar(Minecart car, int id, int data){
		car.setDisplayBlock(new ItemStack(id, 1, (byte) data).getData());
		/*Boolean useFallingBlock = false;
		// net.minecraft.server.v1_7_R1.EntityMinecartAbstract;
		// org.bukkit.craftbukkit.v1_7_R1.entity.CraftEntity;
		String NMSversion = "net.minecraft.server." + Bukkit.getServer().getClass().getPackage()
				.getName().replace(".", ",").split(",")[3];
		String CBversion = "org.bukkit.craftbukkit." + Bukkit.getServer().getClass().getPackage()
				.getName().replace(".", ",").split(",")[3];
		Class nms = null;
		Class cb = null;
		Class nmsEntity = null;
		try {
			nms = Class.forName(NMSversion + ".EntityMinecartAbstract");
			nmsEntity = Class.forName(NMSversion + ".Entity");
			cb = Class.forName(CBversion + ".entity.CraftEntity");
			Method carId = nms.getMethod("k", int.class);
			Method carData = nms.getMethod("l", int.class); //Method 'm' is for height/offset
			Method getNMSEntity = cb.getMethod("getHandle");
			carId.setAccessible(true);
			carData.setAccessible(true);
			getNMSEntity.setAccessible(true);
			Object ce = cb.cast(car);
			Object nmsE = nmsEntity.cast(getNMSEntity.invoke(ce));
			carId.invoke(nmsE, id);
			carData.invoke(nmsE, data);
		} catch (Exception e) {
			useFallingBlock = true;
		}
		if(useFallingBlock){
			//Don't use falling blocks as they're derpy
			main.logger.info("[ALERT] uCarsTrade was unable to place a wool block in a car,"
					+ " please check for an update.");
		}
		return;*/
	}
	
	public void initNodeAISpawnManager(){
		this.aiSpawns = new AINodesSpawnManager(this, 
				config.getBoolean("general.ai.enable"),
				new File(getDataFolder() + File.separator + "aiSpawnNodes.nodelist"));
	}
}
