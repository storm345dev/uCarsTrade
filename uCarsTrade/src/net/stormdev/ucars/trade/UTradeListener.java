package net.stormdev.ucars.trade;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.ucars.stats.Stat;
import net.stormdev.ucars.utils.Car;
import net.stormdev.ucars.utils.CarGenerator;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.useful.ucars.CarHealthData;
import com.useful.ucars.Lang;
import com.useful.ucars.PlaceManager;
import com.useful.ucars.ucarDeathEvent;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class UTradeListener implements Listener {
	main plugin = null;
	public UTradeListener(main plugin){
		this.plugin = plugin;
	}
	@EventHandler(priority = EventPriority.MONITOR)
	void itemCraft(CraftItemEvent event){
		if(event.isCancelled()){
			return;
		}
		ItemStack recipe = event.getCurrentItem();
		if(!(recipe.getType() == Material.MINECART)){
			return;
		}
		if(recipe.getDurability() < 20){
			return;
		}
		Car car = CarGenerator.gen();
        event.setCurrentItem(car.getItem());
        main.plugin.carSaver.cars.put(car.getId(), car);
        main.plugin.carSaver.save();
		return;
	}
	@EventHandler (priority = EventPriority.HIGH)
	void carUpgradeAnvil(InventoryClickEvent event){
		Player player = (Player) event.getWhoClicked();
		Inventory inv = event.getInventory();
		if(!(inv instanceof AnvilInventory)){
			return;
		}
		AnvilInventory i = (AnvilInventory) inv;
		main.logger.info("Action: "+event.getAction().name());
		return;
	}
	/*
	@SuppressWarnings("deprecation")
	@EventHandler(priority = EventPriority.HIGHEST)
	void carUpgradeRecipes(InventoryClickEvent event){
		Inventory inv = event.getInventory();
		if(!(inv instanceof CraftingInventory)){
			return;
		}
		ItemStack[] items = inv.getContents().clone();
		ArrayList<ItemStack> upgradeItems = new ArrayList<ItemStack>();
		Boolean isUpgrade = false;
		ItemStack car = null;
		for(ItemStack item:items){
			if(item.getType().equals(Material.MINECART)){
				if(item.getDurability() > 19){
					isUpgrade  = true;
					car = item;
				}
			}
			upgradeItems.add(item);
		}
		if(!isUpgrade){
			return;
		}
		ItemMeta meta = car.getItemMeta();
		List<String> lore = meta.getLore();
		UUID id;
		try {
			if(lore.size() < 1){
				return;
			}
			id = UUID.fromString(ChatColor.stripColor(lore.get(0)));
		} catch (Exception e) {
			return;
		}
		Car c = null;
		if(!plugin.carSaver.cars.containsKey(id)){
			return;
		}
		c = plugin.carSaver.cars.get(id);
		HashMap<String, Stat> stats = c.getStats();
		upgradeItems.add(event.getCurrentItem());
		for(ItemStack upgrade:upgradeItems){
			if(upgrade.getType() == Material.MINECART || upgrade.getType() == Material.AIR){
				//Allowed
			}
			else if(upgrade.getType() == Material.IRON_BLOCK){
				//Health upgrade
				double health = ucars.config.getDouble("general.cars.health.default");
				double maxHealth = ucars.config.getDouble("general.cars.health.max");
				HealthStat stat = new HealthStat(health, plugin);
				if(stats.containsKey("trade.health")){
					stat = (HealthStat) stats.get("trade.health");
				}
				health = health + (9*upgrade.getAmount()); //Add 9 to health stat
				if(health > maxHealth){
					health = maxHealth;
				}
				upgrade.setAmount(0);
				stat.setHealth(health);
				stats.put("trade.health", stat);
			}
			else if(upgrade.getType() == Material.IRON_INGOT){
				//Health upgrade
				double health = ucars.config.getDouble("general.cars.health.default");
				double maxHealth = ucars.config.getDouble("general.cars.health.max");
				HealthStat stat = new HealthStat(health, plugin);
				if(stats.containsKey("trade.health")){
					stat = (HealthStat) stats.get("trade.health");
				}
				health = health + (1*upgrade.getAmount()); //Add 1 to health stat
				if(health > maxHealth){
					health = maxHealth;
				}
				upgrade.setAmount(0);
				stat.setHealth(health);
				stats.put("trade.health", stat);
			}
			else{
				//Invalid item
				return;
			}
		}
		c.setStats(stats);
	    CraftingInventory ci = (CraftingInventory) inv;
	    ci.clear();
	    ((Player)event.getView().getPlayer()).getInventory().addItem(c.getItem());
	    ((Player)event.getView().getPlayer()).updateInventory();
	    plugin.carSaver.cars.put(id, c);
	    plugin.carSaver.save();
		return;
	}
	*/
	@EventHandler(priority = EventPriority.LOW)
	void carPlace(PlayerInteractEvent event){
		if(event.isCancelled()){
			return;
		}
		if (!(event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		Block block = event.getClickedBlock();
		ItemStack inHand = event.getPlayer().getItemInHand().clone();
		if(inHand.getDurability() < 20){ //Not a car
			return;
		}
		if (inHand.getType() == Material.MINECART) {
			// Its a minecart!
			int iar = block.getTypeId();
			if (iar == 66 || iar == 28 || iar == 27) {
				return;
			}
			if(!PlaceManager.placeableOn(iar, block.getData())){
				return;
			}
			if (!ucars.config.getBoolean("general.cars.enable")) {
				return;
			}
			if (ucars.config.getBoolean("general.cars.placePerm.enable")) {
				String perm = ucars.config
						.getString("general.cars.placePerm.perm");
				if (!event.getPlayer().hasPermission(perm)) {
					String noPerm = Lang.get("lang.messages.noPlacePerm");
					noPerm = noPerm.replaceAll("%perm%", perm);
					event.getPlayer().sendMessage(
							ucars.colors.getError() + noPerm);
					return;
				}
			}
			if (event.isCancelled()) {
				event.getPlayer().sendMessage(
						ucars.colors.getError()
								+ Lang.get("lang.messages.noPlaceHere"));
				return;
			}
			List<String> lore = inHand.getItemMeta().getLore();
			Car c = null;
			if(lore.size() > 0){
			UUID carId = UUID.fromString(ChatColor.stripColor(lore.get(0)));
			if(!plugin.carSaver.cars.containsKey(carId)){
				return;
			}
			c = plugin.carSaver.cars.get(carId);
			}
			else{
				return;
			}
			HashMap<String, Stat> stats = c.getStats();
			Location loc = block.getLocation().add(0, 1.5, 0);
			loc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			final Entity car = event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
			double health = ucars.config.getDouble("general.cars.health.default");
			if(stats.containsKey("trade.health")){
				try {
					health = (Double) stats.get("trade.health").getValue();
				} catch (Exception e) {
					//Leave health to default
				}
			}
			Runnable onDeath = new Runnable(){
				//@Override
				public void run(){
					plugin.getServer().getPluginManager().callEvent(new ucarDeathEvent((Minecart) car));
				}
			};
			car.setMetadata("carhealth", new CarHealthData(health, onDeath, plugin));
			/*
			 * Location carloc = car.getLocation();
			 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
			 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
			 * car.setVelocity(new Vector(0,0,0));
			 */
			UUID id = car.getUniqueId();
			car.setMetadata("trade.car", new StatValue(true, plugin));
				ItemStack placed = event.getPlayer().getItemInHand();
				placed.setAmount(0);
				event.getPlayer().getInventory().setItemInHand(placed);
			event.setCancelled(true);
			while(plugin.carSaver.cars.containsKey(id)){
				Car cr = plugin.carSaver.cars.get(id);
				plugin.carSaver.cars.remove(id);
			    UUID newId = UUID.randomUUID();
			    while(plugin.carSaver.cars.containsKey(newId)){
			    	newId = UUID.randomUUID();
			    }
			    plugin.carSaver.cars.put(newId, cr);
			}
			plugin.carSaver.cars.remove(c.getId());
			c.setId(id); //Bind car id to minecart id
			c.isPlaced = true;
			plugin.carSaver.cars.put(id, c);
			plugin.carSaver.save();
			String name = "Unnamed";
			if(stats.containsKey("trade.name")){
				name = stats.get("trade.name").getValue().toString();
			}
			String placeMsg = net.stormdev.ucars.trade.Lang.get("general.place.msg");
			placeMsg = main.colors.getInfo() + placeMsg.replaceAll(Pattern.quote("%name%"), "'"+name+"'");
			event.getPlayer().sendMessage(placeMsg);
			//Registered car
		}
		return;
	}
	@EventHandler (priority=EventPriority.LOW)
	void carRemoval(ucarDeathEvent event){
		Minecart cart = event.getCar();
		UUID id = cart.getUniqueId();
		if(!plugin.carSaver.cars.containsKey(id)){
			return;
		}
		Car car = plugin.carSaver.cars.get(id);
		if(!car.isPlaced){
			return;
		}
		car.isPlaced = false;
		plugin.carSaver.cars.put(id, car);
		plugin.carSaver.save();
		cart.eject();
		Location loc = cart.getLocation();
		cart.remove();
		loc.getWorld().dropItemNaturally(loc, new ItemStack(car.getItem()));
		//Remove car and get back item
		event.setCancelled(true);
		return;
	}
	//TODO Manage actual car events eg. Make Car uuid = minecart uuid
	

}
