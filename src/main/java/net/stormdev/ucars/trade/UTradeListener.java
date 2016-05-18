package net.stormdev.ucars.trade;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.milkbowl.vault.economy.EconomyResponse;
import net.stormdev.ucars.stats.StatType;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.CarStealEvent;
import net.stormdev.ucars.trade.guis.IconMenu;
import net.stormdev.ucars.trade.guis.InputMenu;
import net.stormdev.ucars.trade.guis.InputMenu.OptionClickEvent;
import net.stormdev.ucars.utils.CarForSale;
import net.stormdev.ucars.utils.CarGenerator;
import net.stormdev.ucars.utils.CarValueCalculator;
import net.stormdev.ucars.utils.InputMenuClickEvent;
import net.stormdev.ucars.utils.TradeBoothClickEvent;
import net.stormdev.ucars.utils.TradeBoothMenuType;
import net.stormdev.ucars.utils.UpgradeForSale;
import net.stormdev.ucarstrade.ItemCarValidation;
import net.stormdev.ucarstrade.cars.CarPresets;
import net.stormdev.ucarstrade.cars.CarPresets.CarPreset;
import net.stormdev.ucarstrade.cars.DrivenCar;
import net.stormdev.ucarstrade.displays.DisplayManager;
import net.stormdev.ucarstrade.displays.DisplayType;
import net.stormdev.ucarstrade.displays.Displays;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Chest;
import org.bukkit.block.DoubleChest;
import org.bukkit.block.Sign;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.event.Event.Result;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByBlockEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;
import org.bukkit.event.vehicle.VehicleUpdateEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarCrashEvent;
import com.useful.uCarsAPI.uCarRespawnEvent;
import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.CarHealthData;
import com.useful.ucars.CartOrientationUtil;
import com.useful.ucars.ClosestFace;
import com.useful.ucars.Lang;
import com.useful.ucars.PlaceManager;
import com.useful.ucars.ucarDeathEvent;
import com.useful.ucars.ucars;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;

public class UTradeListener implements Listener {
	main plugin = null;
	private double hovercarHeightLimit = 256;
	private boolean safeExit = false;
	private boolean stealNPC;
	public UTradeListener(main plugin){
		this.plugin = plugin;
		hovercarHeightLimit = main.config.getDouble("general.hoverCar.heightLimit");
		stealNPC = main.config.getBoolean("general.ai.canSteal");
		safeExit = main.config.getBoolean("general.car.safeExit");
	}
	
	void npcCarSteal(final Minecart vehicle, final Player player, final DrivenCar c, final boolean setPassenger){
		if(!UEntityMeta.hasMetadata(vehicle, "trade.npc") || !c.isNPC()){
			return; //Not an npc
		}
		if(!stealNPC){
			if(player.getVehicle() != null){
				final Location loc = player.getVehicle().getLocation();
				player.getVehicle().eject();
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

					public void run() {
						player.teleport(loc.add(0, 0.5, 0));
						return;
					}}, 2l);
			}
			return;
		}
		final Entity e = vehicle.getPassenger();
		if(e != null){
			//Has an NPC riding it
			vehicle.eject();
			e.remove();
			AIRouter.clearNPCMeta(vehicle);
		}
		if(setPassenger){
			if(player.getVehicle() != null){
				player.getVehicle().eject();
			}
		}
		plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

			public void run() {
				c.setNPC(false);
				UEntityMeta.removeMetadata(vehicle, "trade.npc");
				final CarStealEvent evt = new CarStealEvent(vehicle, player, c);
				Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

					@Override
					public void run() {
						plugin.getServer().getPluginManager().callEvent(evt);
						return;
					}});
				plugin.carSaver.carNowInUse(vehicle, c); //Update changes to car, aka it's not an npc
				if(setPassenger){
					vehicle.setPassenger(player);
				}
				player.sendMessage(main.colors.getInfo()+net.stormdev.ucars.trade.Lang.get("general.steal.taken"));
				return;
			}}, 3l);
		return;
	}
	
	@EventHandler(priority=EventPriority.HIGHEST)
	void aiCarDie(VehicleDestroyEvent event){
		if(!event.isCancelled() && event.getVehicle() instanceof Minecart){
			main.plugin.carSaver.carNoLongerInUse(event.getVehicle().getUniqueId());
			UEntityMeta.removeMetadata(event.getVehicle(), CarSaver.META);
		}
		if(!UEntityMeta.hasMetadata(event.getVehicle(), "trade.npc") || event.isCancelled() || event.getVehicle().hasMetadata("car.destroyed") || UEntityMeta.hasMetadata(event.getVehicle(), "car.destroyed")){
			return;
		}
		
		//The ai car has died
		Vehicle veh = event.getVehicle();
		Location loc = veh.getLocation();
		Entity passenger = veh.getPassenger();
		while(passenger != null){
			final Entity pass = passenger;
			passenger = passenger.getPassenger();
			Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

				@Override
				public void run() {
					pass.remove();
					return;
				}}, 2l);
		}
		List<Entity> near = veh.getNearbyEntities(5, 5, 5);
		
		AIRouter.clearNPCMeta(veh);
		veh.remove();
		event.setCancelled(true);
		
		for(Entity e:near){
			if(e.getType().equals(EntityType.VILLAGER) 
					|| e.getType().equals(EntityType.DROPPED_ITEM)){
				e.remove();
			}
			else if(e instanceof Minecart){
				//Nothing
			}
			else if(e instanceof Damageable){
				((Damageable)e).damage(5);
			}
		}
		
		loc.getWorld().playEffect(loc, Effect.EXPLOSION_HUGE, 20);
		loc.getWorld().playSound(loc, Sound.ENTITY_GENERIC_EXPLODE, 5f, 1f);
	}
	
	@EventHandler
	void respawn(uCarRespawnEvent event){
		UUID old = event.getOldEntityId();
		UUID newId = event.getNewEntityId();
		DrivenCar d = plugin.carSaver.getCarInUseWithEntityID(old);
		if(d == null){
			return;
		}
		d.setId(newId);
		plugin.carSaver.carNoLongerInUse(old);
		plugin.carSaver.carNowInUse(event.getNewCar(), d);
	}
	
	@EventHandler(priority = EventPriority.LOWEST) //Get called first
	void vehicleEntry(PlayerInteractEntityEvent event){
		if(!stealNPC){
			return;
		}
		final Player player = event.getPlayer();
		Entity i = event.getRightClicked();
		Entity cart = i;
		if(!(cart instanceof Minecart)){
			cart = this.isEntityInCar(cart);
		}
		if(cart == null || !(cart instanceof Minecart)){
			return;
		}
		
		if(UEntityMeta.hasMetadata(cart, "trade.npc")){
			
			final DrivenCar c = plugin.carSaver.getCarInUse(cart);
			if(c == null){
				return;
			}
			event.setCancelled(true);
			final Minecart m = (Minecart) cart;
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

				public void run() {
					npcCarSteal(m, player, c, true);
					return;
				}}, 4l);
		}
		return;
	}
	
	@EventHandler
	void NpcController(VehicleUpdateEvent event){
		try {
			Vehicle v = event.getVehicle();
			if(!(v instanceof Minecart)){
				return;
			}
			final Minecart m = (Minecart) v;
			final DrivenCar c = plugin.carSaver.getCarInUse(m);
			if(c == null
					|| !c.isNPC()){
				return; //Not a car or not an npc car
			}
			Entity driver = ucars.listener.getDrivingPassengerOfCar(m);
			if(driver == null || !(driver instanceof Villager)){
				if(c.isNPC() && m.isValid() && !m.isDead()){
					//No longer an NPC car
					UEntityMeta.removeMetadata(m, "trade.npc");
					c.setNPC(false);
					plugin.carSaver.carNowInUse(m, c);
				}				
				return;
			}
			//Use AIRouter to route it
			plugin.aiController.route(m, c);
		} catch (Exception e) {
			//Removed by worldedit etc....
		}
		return;
	}
	
    @EventHandler (priority = EventPriority.LOW) //Call early
    void vehicleExit(VehicleExitEvent event){
    	//Safe exit
    	if(!safeExit){
    		return; //Don't bother
    	}
    	Vehicle veh = event.getVehicle();
    	if(veh.hasMetadata("safeExit.ignore") || UEntityMeta.hasMetadata(veh, "safeExit.ignore")){
    		return;
    	}
    	final Location loc = veh.getLocation();
        Block b = loc.getBlock();
        final Entity exited = event.getExited();
        if(!(exited instanceof Player) || !(veh instanceof Minecart)){
        	return;
        }
        if(!uCarsAPI.getAPI().checkIfCar((Minecart)veh)){
        	return;
        }
        Player player = (Player) exited;
        if(exited.isDead() || player.getHealth() < 1){
        	return; //Allow them to exit
        }
        
      //Handle the exit ourselves
        loc.setYaw(player.getLocation().getYaw());
        loc.setPitch(player.getLocation().getPitch());
    	main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){

			public void run() {
				exited.teleport(loc.add(0, 0.5, 0));
				return;
			}}, 2l); //Teleport back to car loc after exit
        /*if((!b.isEmpty() && !b.isLiquid()) 
        		|| (!b.getRelative(BlockFace.UP).isEmpty() && !b.getRelative(BlockFace.UP).isLiquid())
        		|| (!b.getRelative(BlockFace.UP, 2).isEmpty() && !b.getRelative(BlockFace.UP, 2).isLiquid())){
        	//Not allowed to exit
        	player.sendMessage(main.colors.getError()+net.stormdev.ucars.trade.Lang.get("general.noExit.msg"));
        	event.setCancelled(true);
        }
        else{
        	//Handle the exit ourselves
        	main.plugin.getServer().getScheduler().runTaskLater(main.plugin, new Runnable(){

				public void run() {
					exited.teleport(loc.add(0, 0.5, 0));
					return;
				}}, 2l); //Teleport back to car loc after exit
        }*/
    	return;
    }
	/*@EventHandler
	void lostCars(final ItemDespawnEvent event){
		Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				Item i = event.getEntity();
				ItemStack is = i.getItemStack();
				if(is.getType() != Material.MINECART){
					return;
				}
				ItemMeta im = is.getItemMeta();
				if(im.getLore() == null || im.getLore().size() < 1){
					return;
				}
				String id = ChatColor.stripColor(im.getLore().get(0));
				UUID carId;
				try {
					carId = UUID.fromString(id);
				} catch (Exception e) {
					//Not a car
					return;
				}
				plugin.carSaver.carNoLongerInUse(carId);
				return;
			}});
		return;
	}*/
	@EventHandler
	void displayUpgrades(VehicleUpdateEvent event){
		Vehicle veh = event.getVehicle();
		if(!(veh instanceof Minecart)){
			return;
		}
		Minecart car = (Minecart) veh;
		Location loc = car.getLocation();
		Entity passenger = car.getPassenger();
		if(passenger == null){
			return;
		}
		if(passenger instanceof Boat){
			//Float
			Block b = loc.getBlock();
			Vector vel = car.getVelocity();
			Boolean inWater = false;
			if(b.isLiquid()){
				inWater = true;
				if(vel.getY() < 0.5){
					vel.setY(0.5);
				}
			}
			else if(b.getRelative(BlockFace.DOWN).isLiquid()){
				inWater = true;
				vel.setY(0);
			}
			if(inWater){
			BlockFace f = ClosestFace.getClosestFace(loc.getYaw());
			if(f != BlockFace.UP && f != BlockFace.DOWN){
				Block toGo = b.getRelative(f);
				if(!toGo.isLiquid() && !toGo.isEmpty()){
					//Let the car re-enter land
					vel.setY(0.1);
				}
			}
			}
			car.setVelocity(vel);
			return;
		}
		else if(passenger instanceof Bat && UEntityMeta.hasMetadata(car, "trade.hover")){
			if(passenger.getPassenger() == null){
				//No empty hovercars allowed to fly
				return;
			}
			//Hover
			Block b = loc.getBlock();
			Vector vel = car.getVelocity();
			Block under = b.getRelative(BlockFace.DOWN);
			Block under2 = b.getRelative(BlockFace.DOWN,2);
			Boolean descending = UEntityMeta.hasMetadata(car, "car.braking");
			Boolean ascending = UEntityMeta.hasMetadata(car, "car.action");
			int count = 0;
			if(!b.isEmpty()){
				count++;
			}
			if(!under.isEmpty()){
				count++;
			}
			if(!under2.isEmpty()){
				count++;
			}
			switch(count){
			case 0:vel.setY(-0.3);under.getWorld().playEffect(under.getLocation(), Effect.SMOKE, 1); break;
			case 1:vel.setY(2); break;
			case 2:vel.setY(1); break;
			case 3:vel.setY(0.1);under.getWorld().playEffect(under.getLocation(), Effect.SMOKE, 1); break;
			}
			if(descending && ascending){
				vel.setY(0);
			}
			else if(descending){
				vel.setY(-0.6); //uCar gravity for road convenience
			}
			else if(ascending){
			    vel.setY(0.6);	
			}
			if((loc.getY() < hovercarHeightLimit) || descending){
				car.setVelocity(vel);
			}
			else{
				Entity p = car.getPassenger();
				while(p!=null && !(p instanceof Player) 
						&& p.getPassenger() != null){
					p = p.getPassenger();
				}
				if(p!=null && p instanceof Player){
					String msg = net.stormdev.ucars.trade.Lang.get("general.hovercar.heightLimit");
					((Player)p).sendMessage(main.colors.getInfo()+msg);
				}
			}
			return;
		}
		return;
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
		if(!ChatColor.stripColor(recipe.getItemMeta().getDisplayName()).equalsIgnoreCase("car")){
			return;
		}
		DrivenCar car = CarGenerator.gen();
        event.setCurrentItem(car.toItemStack());
		return;
	}
	@EventHandler (priority = EventPriority.MONITOR)
	void carUpgradeAnvil(final InventoryClickEvent event){
		if(event.getAction()==InventoryAction.CLONE_STACK){
			ItemStack cloned = event.getCursor();
			if(cloned.getType() == Material.MINECART){
				event.setCancelled(true);
				return;
			}
			return;
		}
		final Player player = (Player) event.getWhoClicked();
		InventoryView view = event.getView();
		
		if(event.isShiftClick() && (view.getBottomInventory() instanceof AnvilInventory || view.getTopInventory() instanceof AnvilInventory)){
			event.setCancelled(true); //Disables shift clicking stuff into anvils since it doesn't update properly with the upgrading
			return;
		}
		
		final Inventory i = event.getInventory();
		if(!(i instanceof AnvilInventory)){
			return;
		}
		int slotNumber = event.getRawSlot();
		if(!(slotNumber == view.convertSlot(slotNumber))){
			//Not clicking in the anvil
			return;
		}
		
		//AnvilInventory i = (AnvilInventory) inv;
		Boolean update = true;
		Boolean save = false;
		Boolean pickup = false;
		if(event.getAction() == InventoryAction.PICKUP_ALL || event.getAction() == InventoryAction.PICKUP_HALF || event.getAction() == InventoryAction.PICKUP_ONE || event.getAction() == InventoryAction.PICKUP_SOME){
			update = false;
			pickup = true;
			if(slotNumber == 2){ //Result slot
				save = true;
			}
		}
		ItemStack carItem = null;
		try {
			carItem = i.getItem(0);
		} catch (Exception e) {
			return;
		}
		if(carItem == null){
			if(!pickup && i.getItem(1) != null){ //Put down item and already an upgrade in slot 2...
				ItemStack held = event.getCursor();
				DrivenCar car = ItemCarValidation.getCar(held);
				if(car == null){
					return;
				}
				//They just placed the car; revalidate upgrades next tick
				Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

					@Override
					public void run() {
						if(i.getItem(0) != null){
							carUpgradeAnvil(event);
						}
						return;
					}}, 1l);
			}
			return;
		}
		if(!(carItem.getType() == Material.MINECART) || 
				carItem.getItemMeta().getLore().size() < 2){
			return; //Not a car
		}
		//Anvil contains a car in first slot.
		DrivenCar car = ItemCarValidation.getCar(carItem);
		if(car == null){
			return;
		}
        if(save && slotNumber ==2){
			//They are renaming it
        	ItemStack result = event.getCurrentItem();
        	if(!CarPresets.isCarPresetsUsed || CarPresets.isCarAllowedRename){
        		String name = ChatColor.stripColor(result.getItemMeta().getDisplayName());
            	car.setName(name);
            	player.sendMessage(main.colors.getSuccess()+"+"+main.colors.getInfo()+" Renamed car to: '"+name+"'");
            	return;
        	}
        	event.setCancelled(true);
        	return;
		}
		InventoryAction a = event.getAction();
		ItemStack upgrade = null;
		Boolean set = false;
		final ItemStack up = upgrade;
		final Boolean updat = update;
		final Boolean sav = save;
		final DrivenCar ca = car;
		if(slotNumber == 1 && (a==InventoryAction.PLACE_ALL || a==InventoryAction.PLACE_ONE || a==InventoryAction.PLACE_SOME) && event.getCursor().getType()!=Material.AIR){
			//upgrade = event.getCursor().clone();
			plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

				public void run() {
					ItemStack upgrade = up;
					try {
						upgrade = i.getItem(1); //Upgrade slot
					} catch (Exception e) {
						return;
					}
					if(upgrade == null){
						return;
					}
					//A dirty trick to get the inventory to look correct on the client
					applyUpgrades(upgrade, ca, updat, sav, player, i);
					return;
				}}, 1l);
			set = true;
			return;
		}
		if(!set){
		    try {
				upgrade = i.getItem(1); //Upgrade slot
			} catch (Exception e) {
				return;
			}
		}
		if(upgrade == null){
			return;
		}
		if(pickup && slotNumber == 1){
			return; //Don't bother tracking and updating, etc...
		} 
		if(pickup && slotNumber == 0 && upgrade != null && !upgrade.getType().equals(Material.AIR)){
			//Don't apply upgrades as item is being removed...
			return;
		}
		applyUpgrades(upgrade, car, update, save, player, i);
		return;
	}
	
	@SuppressWarnings("deprecation")
	public void applyUpgrades(ItemStack upgrade, DrivenCar car, Boolean update, Boolean save, Player player, Inventory i){
		   String upgradeMsg = net.stormdev.ucars.trade.Lang.get("general.upgrade.msg");
		   if(upgrade.getType() == Material.IRON_BLOCK){
				//Health upgrade
				double health = car.getHealth();
				double maxHealth = ucars.config.getDouble("general.cars.health.max");
				double bonus = (9*upgrade.getAmount());
				health = health + bonus; //Add 9 to health stat
				if(health > maxHealth){
					health = maxHealth;
				}
				upgradeMsg = ucars.colorise(upgradeMsg);
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
				player.sendMessage(upgradeMsg);
				upgrade.setAmount(0);
				car.setHealth(health);
			}
			else if(upgrade.getType() == Material.IRON_INGOT){
				//Health upgrade
				double health = car.getHealth();
				double maxHealth = ucars.config.getDouble("general.cars.health.max");
				double bonus = (1*upgrade.getAmount());
				health = health + bonus; //Add 9 to health stat
				if(health > maxHealth){
					health = maxHealth;
				}
				upgradeMsg = ucars.colorise(upgradeMsg);
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), bonus+"");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "health");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), health+" (Max: "+maxHealth+")");
				player.sendMessage(upgradeMsg);
				upgrade.setAmount(0);
				car.setHealth(health);
			}
			else if(upgrade.getType() == Material.LEVER){
				if(car.isHandlingDamaged()){
					car.setHandlingDamaged(false);
				}
				upgradeMsg = ucars.colorise(upgradeMsg);
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), "Fixed");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "all damage to the car");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), "Undamaged");
				player.sendMessage(upgradeMsg);
				upgrade.setAmount(0);
			}
			else if(upgrade.getType() == Material.REDSTONE){
				//Increment speed
				double speed = car.getSpeed();
				speed = speed + (0.05d*upgrade.getAmount());
				speed = speed * 1000;
				speed = Math.round(speed);
				speed = speed / 1000;
				if(speed > 4){
					speed = 4;
				}
				upgradeMsg = ucars.colorise(upgradeMsg);
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), (0.05*upgrade.getAmount())+"x");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+"x (Max: "+4+"x)");
				player.sendMessage(upgradeMsg);
				upgrade.setAmount(0);
				car.setSpeed(speed);
			}
			else if(upgrade.getType() == Material.REDSTONE_BLOCK){
				//Increment speed
				double speed = car.getSpeed();
				speed = speed + (0.05d*9d*upgrade.getAmount());
				speed = speed * 1000;
				speed = Math.round(speed);
				speed = speed / 1000;
				if(speed > 4){
					speed = 4;
				}
				upgradeMsg = ucars.colorise(upgradeMsg);
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), (0.05*upgrade.getAmount())+"x");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "speed");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), speed+"x (Max: "+4+"x)");
				player.sendMessage(upgradeMsg);
				upgrade.setAmount(0);
				car.setSpeed(speed);
			}
			else{
				//Apply display upgrades
				DisplayType type = Displays.canAdd(upgrade);
				if(type == null){
					//Invalid item
					return;
				}
				if(car.getModifiers().size() > 0){ //Already got a modifier
					return;
				}
				//Some kind of new way for displays to work
				List<String> modifys = car.getModifiers();
				modifys.add(type.getName());
				car.setModifiers(modifys);
				upgradeMsg = ucars.colorise(upgradeMsg);
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%amount%"), "1");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%stat%"), "Modifier");
				upgradeMsg = upgradeMsg.replaceAll(Pattern.quote("%value%"), type.getName());
				player.sendMessage(upgradeMsg);
				upgrade.setAmount(0);
			}
			i.clear(1);
			if(update){
				i.setItem(0, car.toItemStack());
				player.updateInventory();
			}
			return;
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	void carStackRemoval(VehicleDestroyEvent event){
		if(event.isCancelled()){
			return;
		}
		Vehicle v = event.getVehicle();
		if(v instanceof Minecart){
			if(!uCarsAPI.getAPI().checkIfCar((Minecart)v)){
				v.remove();
				return; //Stop poking our nose in
			}
			//Read up the stack and remove all
			Minecart car = (Minecart)v;
			Entity top = car;
			Location loc = car.getLocation();
			while(top.getPassenger() != null){
				top = top.getPassenger();
			}
			while(top.getVehicle() != null){
				Entity veh = top.getVehicle();
				top.remove();
				top = veh;
			}
			top.remove();
			loc.getWorld().dropItemNaturally(loc, new ItemStack(Material.MINECART));
			main.plugin.carSaver.carNoLongerInUse(v.getUniqueId());
		}
		return;
	}
	
	@EventHandler (priority = EventPriority.HIGH)
	void enterCar(PlayerInteractEntityEvent event){
		//Enter things such as pigucarts
		if(event.isCancelled()){
			return;
		}
		Entity clicked = event.getRightClicked();
		if(!(clicked instanceof Minecart)){
			Minecart m = isEntityInCar(clicked);
			if(m != null){
				clicked = m;
			}
			else{
				return;
			}
		}
		Minecart car = (Minecart) clicked;
		if(car.getPassenger() == null
				|| car.getPassenger() instanceof Player){
			return;
		}
		Entity top = car;
		while(top.getPassenger() != null
				&& !(top.getPassenger() instanceof Player)){
			top = top.getPassenger();
		}
		top.setPassenger(event.getPlayer());
		event.setCancelled(true);
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
	
	@EventHandler(priority = EventPriority.HIGH) //Call first
 	void carDisplayDamage(EntityDamageEvent event){
		if(event.getEntity() instanceof Player){
			return;
		}
		Entity e = event.getEntity();
		Entity v = isEntityInCar(e);
		if(v == null){
			return;
		}
		if(UEntityMeta.hasMetadata(v, "trade.npc") || v.hasMetadata("mta.copentity") || UEntityMeta.hasMetadata(v, "mta.copentity")){
			//Allow it to be hurt
		}
		/*else {
			//Part of a car stack
			event.setDamage(0);
			event.setCancelled(true);
		}*/
		return;
	}
	
	@EventHandler(priority = EventPriority.LOW)
	void carDisplayDeaths(EntityDeathEvent event){
		try {
			if(event.getEntity() instanceof Player){
				return;
			}
			Entity e = event.getEntity();
			Entity v = isEntityInCar(e);
			if(v == null){
				return;
			}
			//Part of a car stack
			event.setDroppedExp(0);
			event.getDrops().clear();
			if(e instanceof Villager && UEntityMeta.hasMetadata(v, "trade.npc")){ //Handle as if car is stolen
				final DrivenCar c = plugin.carSaver.getCarInUse(v);
				if(c == null || !(v instanceof Minecart)){
					return;
				}
				final Minecart m = (Minecart) v;
				
				if(!(e.getLastDamageCause() instanceof EntityDamageByEntityEvent)){
					c.setNPC(false);
					UEntityMeta.removeMetadata(m, "trade.npc");
					plugin.carSaver.carNowInUse(m, c);
					return;
				}
				EntityDamageByEntityEvent ev = (EntityDamageByEntityEvent) e.getLastDamageCause();
				Entity damager = ev.getDamager();
				if(damager instanceof Projectile){
					Projectile p = (Projectile) damager;
					if(p.getShooter() instanceof Player){
						damager = (Entity) p.getShooter();
					}
				}
				if(!(damager instanceof Player)){
					c.setNPC(false);
					UEntityMeta.removeMetadata(m, "trade.npc");
					plugin.carSaver.carNowInUse(m, c);
					return;
				}
				final Player player = (Player) damager;
				
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

					public void run() {
						npcCarSteal(m, player, c, false);
						return;
					}}, 4l);
			}
		} catch (Exception e) {
			//Entities already removed
		}
		return;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	void carDestroy(VehicleDestroyEvent event){
		if(event.getVehicle() instanceof Minecart){
			return; //uCars can handle it
		}
		final Minecart car = isEntityInCar(event.getVehicle());
		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
			return;
		}
		event.setCancelled(true);
		return;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	void boatDestroy(EntityDamageByBlockEvent event){
		final Minecart car = isEntityInCar(event.getEntity());
		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
		return;
	}
	
	@EventHandler(priority = EventPriority.HIGH)
	void carDestroy(VehicleDamageEvent event){
		if(event.isCancelled()){
			return;
		}
		if(event.getVehicle() instanceof Minecart){
			return; //uCars can handle it
		}
		final Minecart car = isEntityInCar(event.getVehicle());
		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
			return;
		}
		event.setDamage(0);
		event.setCancelled(true);
		return;
	}
	
	@EventHandler(priority = EventPriority.LOW) //Call second
	void carDestroy(EntityDamageByEntityEvent event){
		if(event.getEntity() instanceof Minecart){
			return; //uCars can handle it, or they're not a player
		}
		if(event.isCancelled() || event.getDamage() <= 0){
			return;
		}
		final Player player = event.getDamager() instanceof Player ? (Player) event.getDamager() : null;
		final Minecart car = isEntityInCar(event.getEntity());
		if(car == null || !uCarsAPI.getAPI().checkIfCar(car)){
			return;
		}
		if((car.getPassenger() != null && car.getPassenger().getType().equals(EntityType.PLAYER)) || (UEntityMeta.hasMetadata(car, "trade.npc") && event.getEntity().getType().equals(EntityType.VILLAGER)) || (car.getPassenger() != null && (UEntityMeta.hasMetadata(car.getPassenger(), "mta.copentity") || car.getPassenger().hasMetadata("mta.copentity")))){
			return; //They punched the villager; don't take car health
		}
		CarHealthData health = ucars.listener.getCarHealthHandler(car);
		if(health == null){
			return;
		}
		double damage = event.getDamage();
		if(player != null){
			if(player.getItemInHand() == null | player.getItemInHand().getType().equals(Material.AIR)
					|| damage < ucars.config.getDouble("general.cars.health.punchDamage")){
				damage = ucars.config.getDouble("general.cars.health.punchDamage");
			}
		}
		event.setDamage(-70);
		event.setCancelled(true);
		if (damage > 0) {
			double max = ucars.config.getDouble("general.cars.health.default");
			double left = health.getHealth() - damage;
			ChatColor color = ChatColor.YELLOW;
			if (left > (max * 0.66)) {
				color = ChatColor.GREEN;
			}
			if (left < (max * 0.33)) {
				color = ChatColor.RED;
			}
			if (left < 0) {
				left = 0;
			}
			if(player != null){
				player.sendMessage(ChatColor.RED + "-" + damage + ChatColor.YELLOW
						+ "[" + player.getName() + "]" + color + " (" + left + ")");
			}
			if(player != null){
				health.damage(damage, car, player);
			}
			else {
				health.damage(damage, car);
			}
			ucars.listener.updateCarHealthHandler(car, health);
		}
		return;
	}
	
	@SuppressWarnings("deprecation")
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
		if(inHand == null ||
				inHand.getItemMeta() == null ||
				inHand.getItemMeta().getLore() == null ||
				inHand.getItemMeta().getLore().size() < 2 || inHand.getType() != Material.MINECART){ //Not a car
			return;
		}
		// Its a minecart!
		if(!PlaceManager.placeableOn(block.getType().name(), block.getData())){
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
		
		DrivenCar c = ItemCarValidation.getCar(inHand);
		if(c == null){
			return;
		}
		
		Location loc = block.getLocation().add(0, 1.5, 0);
		
		if(loc.getY() >= loc.getWorld().getMaxHeight()){
			event.setCancelled(true);
			event.getPlayer().sendMessage(
					ucars.colors.getError()
							+ Lang.get("lang.messages.noPlaceHere"));
			return;
		}
		
		Vector v = loc.toVector().clone().setY(event.getPlayer().getLocation().getY()).subtract(event.getPlayer().getLocation().toVector());
		v = v.normalize();
		boolean loop = safeExit;
		Location current = event.getPlayer().getLocation().clone();
		double prevDist = Double.MAX_VALUE;
		while (loop){
			double dist = current.distanceSquared(loc);
			if(dist > prevDist || dist < 1.5){
				loop = false;
			}
			prevDist = dist;
			
			Block b = current.getBlock();
			if(!(b.isEmpty() || b.isLiquid())){
				event.getPlayer().sendMessage(ChatColor.RED+"You need a clear path between you and the vehicle to be placed!");
				event.setCancelled(true);
				return;
			}
			
			current = current.add(v);
		}
		
		loc.setYaw(event.getPlayer().getLocation().getYaw() + 90);
		Block in = loc.getBlock();
		if(!in.isEmpty() && !in.isLiquid()){
			return;
		}
		final Minecart car = (Minecart) event.getPlayer().getWorld().spawnEntity(loc, EntityType.MINECART);
		float yaw = event.getPlayer().getLocation().getYaw()+90;
		if(yaw < 0){
			yaw = 360 + yaw;
		}
		else if(yaw >= 360){
			yaw = yaw - 360;
		}
		CartOrientationUtil.setYaw(car, yaw);
		
		//Display blocks
		CarPreset cp = c.getPreset();
		if(cp != null && cp.hasDisplayBlock()){
			car.setDisplayBlock(cp.getDisplayBlock());
			car.setDisplayBlockOffset(cp.getDisplayBlockOffset());
		}
		else if(c.getBaseDisplayBlock() != null){
			car.setDisplayBlock(c.getBaseDisplayBlock());
			car.setDisplayBlockOffset(0);
		}
		
		in = car.getLocation().getBlock();
		Block n = in.getRelative(BlockFace.NORTH);   // The directions minecraft aligns the cart to
		Block w = in.getRelative(BlockFace.WEST);
		Block nw = in.getRelative(BlockFace.NORTH_WEST);
		Block ne = in.getRelative(BlockFace.NORTH_EAST);
		Block sw = in.getRelative(BlockFace.SOUTH_WEST);
		if((!in.isEmpty() && !in.isLiquid())
				|| (!n.isEmpty() && !n.isLiquid())
				|| (!w.isEmpty() && !w.isLiquid())
				|| (!ne.isEmpty() && !ne.isLiquid())
				|| (!nw.isEmpty() && !nw.isLiquid())
				|| (!sw.isEmpty() && !sw.isLiquid())){
			car.remove();
			event.setUseItemInHand(Result.DENY);
			return;
		}
		double health = c.getHealth();
		CarHealthData chd = ucars.listener.getCarHealthHandler(car);
		chd.setHealth(health);
		ucars.listener.updateCarHealthHandler(car, chd);
		/*
		 * Location carloc = car.getLocation();
		 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
		 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
		 * car.setVelocity(new Vector(0,0,0));
		 */
		UEntityMeta.setMetadata(car, "trade.car", new StatValue(true, plugin));
			ItemStack placed = event.getPlayer().getItemInHand();
			placed.setAmount(0);
			event.getPlayer().getInventory().setItemInHand(placed);
		event.setCancelled(true);
		c.setId(car.getUniqueId());
		plugin.carSaver.carNowInUse(car, c);
		String name = c.getName();
		String placeMsg = net.stormdev.ucars.trade.Lang.get("general.place.msg");
		placeMsg = main.colors.getInfo() + placeMsg.replaceAll(Pattern.quote("%name%"), "'"+name+"'");
		event.getPlayer().sendMessage(placeMsg);
		DisplayManager.fillCar(car, c, event.getPlayer());
		return;
	}
	 
	@EventHandler (priority=EventPriority.HIGHEST)
	void carCrash(uCarCrashEvent event){
		if(event.isCancelled()){
			return;
		}
		
		Minecart cart = event.getCar();
		if(UEntityMeta.hasMetadata(cart, "car.destroyed") || cart.hasMetadata("car.destroyed")){
			return; //Don't damage the car
		}
		UUID id = cart.getUniqueId();
		DrivenCar car = plugin.carSaver.getCarInUse(cart);
		if(car == null){
			cart.remove(); //Stop invalid cars from keeping on driving
			return;
		}
		
		if(main.random.nextInt(10) < 1){ // 1/10 chance
			car.setHandlingDamaged(true);
			plugin.carSaver.asyncSave();
			
			Entity passenger = cart.getPassenger();
			while(!passenger.getType().equals(EntityType.PLAYER) && passenger.getPassenger() != null){
				passenger = passenger.getPassenger();
			}
			if(passenger instanceof Player){
				((Player) passenger).sendMessage(ChatColor.RED+"*The car's handling broke* If you want to repair it, you will need to use an anvil and a lever!");
			}
		}
	}
	
	@EventHandler (priority=EventPriority.HIGHEST)
	void carRemoval(ucarDeathEvent event){
		event.setCancelled(true);
		Minecart cart = event.getCar();
		if(UEntityMeta.hasMetadata(cart, "car.destroyed") || cart.hasMetadata("car.destroyed")){
			return; //Don't destroy the car
		}
		UUID id = cart.getUniqueId();
		DrivenCar car = plugin.carSaver.getCarInUse(cart);
		if(car == null){
			cart.remove(); //IDK
			return;
		}
		/*
		if(!car.isPlaced){
			return;
		}
		*/
		UEntityMeta.setMetadata(cart, "car.destroyed", new StatValue(true, ucars.plugin));
		event.setCancelled(true);
		
		if(UEntityMeta.hasMetadata(cart, "trade.npc")){
			//Say it's no longer in use
			main.plugin.aiSpawns.decrementSpawnedAICount();
			if(event.didPlayerKill()){
				Player player = event.getPlayerWhoKilled();
				npcCarSteal(cart, player, car, false);
			}
		}
		
		/*if(main.random.nextBoolean() && main.config.getBoolean("general.car.damage")){
			if(main.random.nextBoolean()){
				if(main.random.nextBoolean()){
				    car.setHandlingDamaged(true);
				}
			}
		}*/
		plugin.carSaver.carNoLongerInUse(car);
		Location loc = cart.getLocation();
		Entity top = cart;
		while(top.getPassenger() != null
				&& !(top.getPassenger() instanceof Player)){
			top = top.getPassenger();
		}
		if(top.getPassenger() instanceof Player){
			final Player pl = (Player) top.getPassenger();
			top.eject();
			if(safeExit){
				final Location exitLoc = loc.clone().add(0, 0.5, 0);
				Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

					@Override
					public void run() {
						pl.teleport(exitLoc);
						return;
					}}, 1l);
			}
		}
		while(top.getVehicle() != null){
			Entity veh = top.getVehicle();
			top.remove();
			top = veh;
		}
		cart.eject();
		cart.remove();
		loc.getWorld().dropItemNaturally(loc, new ItemStack(car.toItemStack()));
		//Remove car and get back item
		return;
	}
	//Add extra functions; eg. Car trade station, etc...
	@EventHandler
	public void signWriter(SignChangeEvent event){
		String[] lines = event.getLines();
		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Trade]")){
			lines[0] = ChatColor.GREEN+"[Trade]";
			lines[1] = ChatColor.RED + ChatColor.stripColor(lines[1]);
			lines[2] = "Place chest";
			lines[3] = "above";
		}
		if(ChatColor.stripColor(lines[0]).equalsIgnoreCase("[Shop]")){
			lines[0] = ChatColor.GREEN+"[Shop]";
			lines[1] = ChatColor.RED + ChatColor.stripColor(lines[1]);
			lines[2] = "Place chest";
			lines[3] = "above";
		}
		return;
	}
	@EventHandler
	public void tradeBooth(InventoryOpenEvent event){
		if(!main.config.getBoolean("general.carTrading.enable")){
			//Don't do car trading
			return;
		}
		if(main.economy == null){
	        Boolean installed = plugin.setupEconomy();
	        if(!installed){
	        	main.config.set("general.carTrading.enable", false);
	        	main.logger.info(main.colors.getError()+"[Important] Unable to find an economy plugin:"
	        			+ " trade booths have been closed.");
	        	return;
	        }
		}
		Inventory inv = event.getInventory();
		if (!(inv.getHolder() instanceof Chest || inv.getHolder() instanceof DoubleChest)){
            return;
        }
		//They opened a chest
		Block block = null;
		if(inv.getHolder() instanceof Chest){
			block = ((Chest)inv.getHolder()).getBlock();
		}
		else{
			block = ((DoubleChest)inv.getHolder()).getLocation().getBlock();
		}
		Block underBlock = block.getRelative(BlockFace.DOWN);
		if(!(underBlock.getState() instanceof Sign)){
			return;
		}
		Sign sign = (Sign) underBlock.getState();
		if(!(ChatColor.stripColor(sign.getLines()[0])).equalsIgnoreCase("[Trade]") || !(ChatColor.stripColor(sign.getLines()[1])).equalsIgnoreCase("cars")){
			if((ChatColor.stripColor(sign.getLines()[0])).equalsIgnoreCase("[Shop]") && (ChatColor.stripColor(sign.getLines()[1])).equalsIgnoreCase("cars")){
				event.getView().close();
				event.setCancelled(true); //Cancel the event
				plugin.carShop.open((Player)event.getPlayer());
			}
			return;
		}
		//A trade sign for cars
		//Create a trade inventory
		Player player = (Player) event.getPlayer(); //Get the player from the event
		event.getView().close();
		event.setCancelled(true); //Cancel the event
		plugin.tradeMenu.open(player);
		//Made the trade booth
		return;
	}
	@SuppressWarnings("unchecked")
	@EventHandler
	public void tradeMenuSelect(final TradeBoothClickEvent event){
		IconMenu.OptionClickEvent clickEvent = event.getClickEvent();
		final Player player = clickEvent.getPlayer();
		int position = clickEvent.getPosition();
		Runnable doAfter = null;
		if(event.getMenuType() == TradeBoothMenuType.MENU){ //They are selecting which menu to open
			if(position == 0){ //Read tutorial
				player.sendMessage(main.colors.getTitle()+"Tutorial coming soon!");
				return;
			}
			else if(position == 1){ //Buy cars
				doAfter = new Runnable(){
					public void run() {
						getCarsForSaleMenu(1).open(player);
						return;
				    }};
			    //Don't return
			}
			else if(position == 2){ //Sell cars
				doAfter = new Runnable(){
					public void run() {
						getSellCarsInputMenu().open(player);
						return;
				    }};
				//Don't return
			}
			else if(position == 3){ //Buy upgrades
				doAfter = new Runnable(){
					public void run() {
						getUpgradesForSaleMenu(1).open(player);
						return;
				    }};
			    //Don't return
			}
			else if(position == 4){ //Sell upgrades
				doAfter = new Runnable(){
					public void run() {
						getSellUpgradesInputMenu().open(player);
						return;
				    }};
				//Don't return
			}
		}
		else if(event.getMenuType() == TradeBoothMenuType.BUY_CARS){
			if(position == 0){
				//Return to menu
				doAfter = new Runnable(){
					public void run() {
						plugin.tradeMenu.open(player);
						return;
				    }};
			}
			else if(position == 53){
				//Next page
				doAfter = new Runnable(){
					public void run() {
						getCarsForSaleMenu(event.getPage()+1).open(player);
						return;
				    }};
			}
			else if(position == 52){
				int page = event.getPage();
				if(page > 1){
					page--;
				}
				final int p = page;
				doAfter = new Runnable(){
					public void run() {
						getCarsForSaleMenu(p).open(player);
						return;
				    }};
			}
			else{
				//Positions 1-> 51
				int page = event.getPage();
				int slot = clickEvent.getPosition() - 1; //Click on '1' = pos=0
				int mapPos = (page-1)*51+slot; //Page one, slot 1 = mapPos: 0
				HashMap<UUID, CarForSale> cars = null;
				try {
				    cars = (HashMap<UUID, CarForSale>) event.getArgs()[0];
				} catch (Exception e) {
					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
					return;
				}
				CarForSale car = null;
				try {
					car = cars.get(cars.keySet().toArray()[mapPos]);
				} catch (Exception e) {
					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
					return;
				}
				//We have selected the correct car
				if(!plugin.salesManager.carsForSale.containsKey(car.getUUID())){
					//It was just bought
					return;
				}
				if(main.economy == null){
					if(!plugin.setupEconomy()){
						player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
						return;
					}
				}
				//Economy plugin successfully hooked
				double price = car.getPrice();
				double balance = main.economy.getBalance(player.getName());
				if(balance < price){
					String msg = net.stormdev.ucars.trade.Lang.get("general.buy.notEnoughMoney");
					msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
					player.sendMessage(main.colors.getError()+msg);
					return;
				}
				plugin.salesManager.carsForSale.remove(car.getUUID());
				EconomyResponse er = main.economy.withdrawPlayer(player.getName(), price);
				balance = er.balance;
				if(!er.transactionSuccess()){
					player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
					return;
				}
				double profit = car.getProfit();
				EconomyResponse er2 = main.economy.depositPlayer(car.getSeller(), profit);
				if(plugin.getServer().getPlayer(car.getSeller())!=null && plugin.getServer().getPlayer(car.getSeller()).isOnline()){
					Player pl = plugin.getServer().getPlayer(car.getSeller());
					pl.sendMessage(main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For car sale!");
				}
				else{
					DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy@HH:mm]");
					String time = dateFormat.format(new Date());
					String msg = main.colors.getInfo()+time+" "+ChatColor.RESET+main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For car sale!";
					plugin.alerts.put(car.getSeller(), msg);
				}
				if(!er2.transactionSuccess()){
					main.logger.info(main.colors.getError()+"Failed to give seller money for seller: "+car.getSeller()+"!");
				}
				String msg = net.stormdev.ucars.trade.Lang.get("general.buy.success");
				msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
				msg = msg.replaceAll(Pattern.quote("%item%"), "1 car");
				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+price));
				//Give them the car and remove it from the list
				DrivenCar c = car.getCar();
				plugin.salesManager.saveCars();
				player.getInventory().addItem(c.toItemStack());
				player.sendMessage(main.colors.getSuccess()+msg);
			}
		}
		else if(event.getMenuType() == TradeBoothMenuType.BUY_UPGRADES){
			if(position == 0){
				//Return to menu
				doAfter = new Runnable(){
					public void run() {
						plugin.tradeMenu.open(player);
						return;
				    }};
			}
			else if(position == 53){
				//Next page
				doAfter = new Runnable(){
					public void run() {
						getUpgradesForSaleMenu(event.getPage()+1).open(player);
						return;
				    }};
			}
			else if(position == 52){
				int page = event.getPage();
				if(page > 1){
					page--;
				}
				final int p = page;
				doAfter = new Runnable(){
					public void run() {
						getUpgradesForSaleMenu(p).open(player);
						return;
				    }};
			}
			else{
				//Positions 1-> 51
				//Get upgrade and buy it using vault
				int page = event.getPage();
				int slot = clickEvent.getPosition() - 1; //Click on '1' = pos=0
				int mapPos = (page-1)*51+slot; //Page one, slot 1 = mapPos: 0
				HashMap<UUID, UpgradeForSale> cars = null;
				try {
				    cars = (HashMap<UUID, UpgradeForSale>) event.getArgs()[0];
				} catch (Exception e) {
					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
					return;
				}
				UpgradeForSale car = null;
				try {
					car = cars.get(cars.keySet().toArray()[mapPos]);
				} catch (Exception e) {
					player.sendMessage(main.colors.getError()+"An error occured. Please try again.");
					return;
				}
				//We have selected the correct car
				if(!plugin.salesManager.upgradeForSale.containsKey(car.getSaleId())){
					//It was just bought
					return;
				}
				if(main.economy == null){
					if(!plugin.setupEconomy()){
						player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
						return;
					}
				}
				//Economy plugin successfully hooked
				double price = car.getPrice();
				double balance = main.economy.getBalance(player.getName());
				if(balance < price){
					String msg = net.stormdev.ucars.trade.Lang.get("general.buy.notEnoughMoney");
					msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
					player.sendMessage(main.colors.getError()+msg);
					return;
				}
				plugin.salesManager.upgradeForSale.remove(car.getSaleId());
				EconomyResponse er = main.economy.withdrawPlayer(player.getName(), price);
				balance = er.balance;
				if(!er.transactionSuccess()){
					player.sendMessage(main.colors.getError()+"An error occured. Please try again later.");
					return;
				}
				double profit = car.getProfit();
				EconomyResponse er2 = main.economy.depositPlayer(car.getSeller(), profit);
				if(plugin.getServer().getPlayer(car.getSeller())!=null && plugin.getServer().getPlayer(car.getSeller()).isOnline()){
					Player pl = plugin.getServer().getPlayer(car.getSeller());
					pl.sendMessage(main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For upgrade sale!");
				}
				else{
					DateFormat dateFormat = new SimpleDateFormat("[dd/MM/yyyy@HH:mm]");
					String time = dateFormat.format(new Date());
					String msg = main.colors.getInfo()+time+" "+ChatColor.RESET+main.colors.getSuccess()+"+"+main.config.getString("general.carTrading.currencySign")+profit+main.colors.getInfo()+" For upgrade sale!";
					plugin.alerts.put(car.getSeller(), msg);
				}
				if(!er2.transactionSuccess()){
					main.logger.info(main.colors.getError()+"Failed to give seller money for seller: "+car.getSeller()+"!");
				}
				String msg = net.stormdev.ucars.trade.Lang.get("general.buy.success");
				msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+balance));
				msg = msg.replaceAll(Pattern.quote("%item%"), "upgrades");
				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(main.config.getString("general.carTrading.currencySign")+price));
				//Give them the car and remove it from the list
				plugin.salesManager.saveUpgrades();
				ItemStack item = new ItemStack(Material.IRON_INGOT);
				ItemMeta im = item.getItemMeta();
				List<String> lore = new ArrayList<String>();
		        StatType type = car.getUpgradeType();
	        	if(type == StatType.HANDLING_DAMAGED){
	        		item = new ItemStack(Material.LEVER);
	        		im = item.getItemMeta();
	        		im.setDisplayName("Repair Upgrade");
	        		lore.add(main.colors.getInfo()+"Repairs all car damage");
	        		im.setLore(lore);
	        	    item.setItemMeta(im);
	        	}
	        	else if(type == StatType.HEALTH){
	        		item = new ItemStack(Material.IRON_INGOT);
	        		im = item.getItemMeta();
	        		im.setDisplayName("Health Upgrade");
	        		lore.add(main.colors.getInfo()+"Adds 1 health to your car");
	        		im.setLore(lore);
	        	    item.setItemMeta(im);
	        	}
	        	else if(type == StatType.SPEED){
	        		item = new ItemStack(Material.REDSTONE);
	        		im = item.getItemMeta();
	        		im.setDisplayName("Speed Upgrade");
	        		lore.add(main.colors.getInfo()+"Adds 0.05x speed to your car");
	        		im.setLore(lore);
	        	    item.setItemMeta(im);
	        	}
	        	item.setAmount(car.getQuantity());
				player.getInventory().addItem(item);
				player.sendMessage(main.colors.getSuccess()+msg);
			}
		}
		if(doAfter != null){
			plugin.getServer().getScheduler().runTaskLater(plugin, doAfter, 2l);
		}
		return;
	}
	@EventHandler
	void sellStuff(InputMenuClickEvent event){
		OptionClickEvent clickEvent = event.getClickEvent();
		final Player player = clickEvent.getPlayer();
		int position = clickEvent.getPosition();
		Runnable doAfter = null;
		if(event.getMenuType() == TradeBoothMenuType.SELL_CARS){
			if(position == 0){
				//Return to menu
				doAfter = new Runnable(){
					public void run() {
						plugin.tradeMenu.open(player);
						return;
				    }};
			}
			else if(position == 8){
				//Check if valid and try to sell it
				Inventory i = clickEvent.getInventory();
				if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
					return;
				}
				ItemStack carItem = i.getItem(4);
				if(carItem.getType() != Material.MINECART
						|| carItem.getItemMeta() == null
						|| carItem.getItemMeta().getLore() == null
						|| carItem.getItemMeta().getLore().size() < 2){
					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
					return;
				}
				//Is a valid car to sell
				DrivenCar c = ItemCarValidation.getCar(carItem);
				if(c == null){
					player.sendMessage(main.colors.getInfo()+"Invalid item to sell!");
					return;
				}
				double price = CarValueCalculator.getCarValueForSale(c);
				if(main.economy == null){
					return;
				}
				String msg = net.stormdev.ucars.trade.Lang.get("general.sell.msg");
				msg = msg.replaceAll(Pattern.quote("%item%"), "a car");
				String units = main.config.getString("general.carTrading.currencySign")+price;
				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(units));
				// Add to market for sale
				double purchase = CarValueCalculator.getCarValueForPurchase(c);
				CarForSale saleItem = new CarForSale(c, player.getName(), 
						purchase, price, null);
				plugin.salesManager.carsForSale.put(saleItem.getUUID(), saleItem);
				plugin.salesManager.saveCars();
				player.sendMessage(main.colors.getInfo()+msg); //Tell player they are selling it on the market
				event.getClickEvent().setWillClose(true);
				event.getClickEvent().setWillDestroy(true);
			}
			else if(position == 4){
				//Check if car and if it is update the sale button
				final Inventory inv = clickEvent.getInventory();
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

					public void run() {
						Inventory i = inv;
						if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
							return;
						}
						ItemStack carItem = i.getItem(4);
						if(carItem.getType() != Material.MINECART 
								|| carItem.getItemMeta() == null
								|| carItem.getItemMeta().getLore() == null
								|| carItem.getItemMeta().getLore().size() < 2){
							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
							return;
						}
						DrivenCar c = ItemCarValidation.getCar(carItem);
						if(c == null){
							player.sendMessage(main.colors.getInfo()+"Invalid item to sell!");
							return;
						}
						double price = CarValueCalculator.getCarValueForSale(c);
						if(main.economy == null){
							return;
						}
						ItemStack sellItem = new ItemStack(Material.EMERALD);
						ItemMeta im = sellItem.getItemMeta();
						im.setDisplayName(main.colors.getTitle()+"Sell");
						ArrayList<String> lre = new ArrayList<String>();
					    lre.add(main.colors.getInfo()+"For: "+main.config.getString("general.carTrading.currencySign")+price);
						im.setLore(lre);
						sellItem.setItemMeta(im);
						inv.setItem(8, sellItem);
						return;
					}}, 1l);
			}
		}
		else if(event.getMenuType() == TradeBoothMenuType.SELL_UPGRADES){
			if(position == 0){
				//Return to menu
				doAfter = new Runnable(){
					public void run() {
						plugin.tradeMenu.open(player);
						return;
				    }};
			}
			else if(position == 8){
				//Check if valid and try to sell it
				Inventory i = clickEvent.getInventory();
				if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
					return;
				}
				ItemStack upgradeItem = i.getItem(4).clone();
				i.clear(4);
				Material type = upgradeItem.getType();
				StatType upgradeType = StatType.SPEED;
				if(type == Material.IRON_INGOT){ //Health Upgrade
					upgradeType = StatType.HEALTH;
				}
				else if(type == Material.REDSTONE){ //Speed upgrade
					upgradeType = StatType.SPEED;
				}
				else if(type == Material.LEVER){ //Repair upgrade
					upgradeType = StatType.HANDLING_DAMAGED;
				}
				else{
					player.sendMessage(main.colors.getError()+"Invalid item to sell!");
					return;
				}
				double price = main.config.getDouble("general.carTrading.upgradeValue")*upgradeItem.getAmount();
				double sellFor = price + (main.config.getDouble("general.carTrading.VATPercent")*price)/100;
				sellFor = Math.round((sellFor*100));
				sellFor = sellFor / 100;
				UUID saleId = UUID.randomUUID();
				UpgradeForSale saleItem = new UpgradeForSale(saleId, player.getName(), sellFor, upgradeType, upgradeItem.getAmount(), price);
				String msg = net.stormdev.ucars.trade.Lang.get("general.sell.msg");
				msg = msg.replaceAll(Pattern.quote("%item%"), "upgrades");
				String units = main.config.getString("general.carTrading.currencySign")+price;
				msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(units));
				// Add to market for sale
				if(!plugin.salesManager.upgradeForSale.containsKey(saleId)){
					plugin.salesManager.upgradeForSale.put(saleId, saleItem);
					plugin.salesManager.saveUpgrades();
					player.sendMessage(main.colors.getInfo()+msg); //Tell player they are selling it on the market
				}
				event.getClickEvent().setWillClose(true);
				event.getClickEvent().setWillDestroy(true);
				clickEvent.getMenu().destroy(); //Close the menu
			}
			else if(position == 4){
				//Check if valid and if it is update the sale button
				final Inventory inv = clickEvent.getInventory();
				plugin.getServer().getScheduler().runTaskLater(plugin, new Runnable(){

					public void run() {
						Inventory i = inv;
						if(i.getItem(4)==null || i.getItem(4).getType() == Material.AIR){
							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
							return;
						}
						Material type = i.getItem(4).getType();
						if(type == Material.IRON_INGOT || type == Material.REDSTONE
								|| type == Material.LEVER){ //Valid Upgrade
						    //Update button	
							ItemStack sellItem = new ItemStack(Material.EMERALD);
							ItemMeta im = sellItem.getItemMeta();
							im.setDisplayName(main.colors.getTitle()+"Sell");
							ArrayList<String> lre = new ArrayList<String>();
							double price = main.config.getDouble("general.carTrading.upgradeValue")*i.getItem(4).getAmount();
						    lre.add(main.colors.getInfo()+"For: "+main.config.getString("general.carTrading.currencySign")+price);
							im.setLore(lre);
							sellItem.setItemMeta(im);
							inv.setItem(8, sellItem);
						}
						else{
							player.sendMessage(main.colors.getError()+"Invalid item to sell!");
							return;
						}
						return;
					}}, 1l);
			}
		}
		if(doAfter != null){
			plugin.getServer().getScheduler().runTaskLater(plugin, doAfter, 2l);
		}
		return;
	}
	IconMenu getCarsForSaleMenu(final int page){
		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.buyCars")+" Page: "+page;
		if(title.length() > 32){
			title = main.colors.getError()+"Buy Cars (ERROR:Too Long)";
		}
		@SuppressWarnings("unchecked")
		final HashMap<UUID, CarForSale> cars = (HashMap<UUID, CarForSale>) plugin.salesManager.carsForSale.clone();
		IconMenu menu = new IconMenu(title, 54, new IconMenu.OptionClickEventHandler() {
            public void onOptionClick(IconMenu.OptionClickEvent event) {
            	TradeBoothClickEvent evt = new TradeBoothClickEvent(event, TradeBoothMenuType.BUY_CARS, page, new Object[]{cars});
            	plugin.getServer().getPluginManager().callEvent(evt);
            	event.setWillClose(true);
            	event.setWillDestroy(true);
            }
        }, plugin, true);
		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
		menu.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
		menu.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
		//1-51 slots available on the page
		int pos = 1;
		int start = (page-1)*51;
		Object[] keys = cars.keySet().toArray();
		for(int i=start;i<keys.length;i++){
			CarForSale car= cars.get(keys[i]);
			double price = car.getPrice();
	        String seller = car.getSeller();
	        ItemStack item = new ItemStack(Material.AIR);
	        String name = "Car";
	        List<String> lore = new ArrayList<String>();
	        DrivenCar c = car.getCar();
        	name = c.getName();
        	item = c.toItemStack();
        	ItemMeta im = item.getItemMeta();
        	lore.add(main.colors.getInfo()+main.config.getString("general.carTrading.currencySign")+price);
        	lore.add(main.colors.getInfo()+"Seller: "+seller);
        	List<String> iml = im.getLore();
        	iml.remove(0);
        	lore.addAll(2, iml);
        	im.setLore(lore);
        	item.setItemMeta(im); 
        if(pos < 52){
    		menu.setOption(pos, item, main.colors.getTitle()+name, lore);
    		pos++;
    	}
		}
		return menu;
	}
	IconMenu getUpgradesForSaleMenu(final int page){
		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.buyUpgrades")+" Page: "+page;
		if(title.length() > 32){
			title = main.colors.getError()+"Buy Upgrades (ERROR:Too Long)";
		}
		@SuppressWarnings("unchecked")
		final HashMap<UUID, UpgradeForSale> ups = (HashMap<UUID, UpgradeForSale>) plugin.salesManager.upgradeForSale.clone();
		IconMenu menu = new IconMenu(title, 54, new IconMenu.OptionClickEventHandler() {
            public void onOptionClick(IconMenu.OptionClickEvent event) {
            	TradeBoothClickEvent evt = new TradeBoothClickEvent(event, TradeBoothMenuType.BUY_UPGRADES, page, new Object[]{ups});
            	plugin.getServer().getPluginManager().callEvent(evt);
            	event.setWillClose(true);
            	event.setWillDestroy(true);
            }
        }, plugin, true);
		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
		menu.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
		menu.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
		//Set option slots for all upgrades for sale
		//1-51 slots available on the page
				int pos = 1;
				int start = (page-1)*51;
				Object[] keys = ups.keySet().toArray();
				for(int i=start;i<keys.length;i++){
					UpgradeForSale car= ups.get(keys[i]);
					double price = car.getPrice();
			        String seller = car.getSeller();
			        ItemStack item = new ItemStack(Material.AIR);
			        ItemMeta im = item.getItemMeta();
			        String name = "Upgrade";
			        List<String> lore = new ArrayList<String>();
			        item = new ItemStack(Material.IRON_INGOT);
			        StatType type = car.getUpgradeType();
		        	if(type == StatType.HANDLING_DAMAGED){
		        		item = new ItemStack(Material.LEVER);
		        		im = item.getItemMeta();
		        		name = "Repair Upgrade";
		        		lore.add(main.colors.getInfo()+"Repairs all car damage");
		        		im.setLore(lore);
		        	    item.setItemMeta(im);
		        	}
		        	else if(type == StatType.HEALTH){
		        		item = new ItemStack(Material.IRON_INGOT);
		        		im = item.getItemMeta();
		        		name = "Health Upgrade";
		        		lore.add(main.colors.getInfo()+"Adds 1 health to your car");
		        		im.setLore(lore);
		        	    item.setItemMeta(im);
		        	}
		        	else if(type == StatType.SPEED){
		        		item = new ItemStack(Material.REDSTONE);
		        		im = item.getItemMeta();
		        		name = "Speed Upgrade";
		        		lore.add(main.colors.getInfo()+"Adds 0.05x speed to your car");
		        		im.setLore(lore);
		        	    item.setItemMeta(im);
		        	}
		        	lore = new ArrayList<String>();
		        	lore.add(main.colors.getInfo()+main.config.getString("general.carTrading.currencySign")+price);
		        	lore.add(main.colors.getInfo()+"Seller: "+seller);
		        	lore.addAll(2, im.getLore());
		        	im.setLore(lore);
		        	item.setItemMeta(im);	
		        	item.setAmount(car.getQuantity());
			        if(pos < 52){
		        		menu.setOption(pos, item, main.colors.getTitle()+name, lore);
		        		pos++;
		        	}
				}
		return menu;
	}
	
	InputMenu getSellCarsInputMenu(){
		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.sellCars");
		if(title.length() > 32){
			title = main.colors.getError()+"Sell a car";
		}
		InputMenu menu = new InputMenu(title, 9, new InputMenu.OptionClickEventHandler() {
            public void onOptionClick(InputMenu.OptionClickEvent event) {
            	if(event.getPosition() == 0 || event.getPosition() == 8){
            		event.setWillClose(true);
            	}
            	InputMenuClickEvent evt = new InputMenuClickEvent(event, TradeBoothMenuType.SELL_CARS);
            	plugin.getServer().getPluginManager().callEvent(evt);
            }

			@Override
			public void onClose(Player player, Inventory inv) {
			}
        }, plugin, true);
		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
		menu.setOption(8, new ItemStack(Material.EMERALD), main.colors.getTitle()+"Sell", main.colors.getError()+"Unavailable");
		menu.setOption(1, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
		menu.setOption(2, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
		menu.setOption(3, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
		menu.setOption(5, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
		menu.setOption(6, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
		menu.setOption(7, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
		//4 is the input box
		return menu;
	}
	InputMenu getSellUpgradesInputMenu(){
		String title = main.colors.getTitle()+net.stormdev.ucars.trade.Lang.get("title.trade.sellUpgrades");
		if(title.length() > 32){
			title = main.colors.getError()+"Sell upgrades";
		}
		InputMenu menu = new InputMenu(title, 9, new InputMenu.OptionClickEventHandler() {
            public void onOptionClick(InputMenu.OptionClickEvent event) {
            	if(event.getPosition() == 0 || event.getPosition() == 8){
            		event.setWillClose(true);
            	}
            	InputMenuClickEvent evt = new InputMenuClickEvent(event, TradeBoothMenuType.SELL_UPGRADES);
            	plugin.getServer().getPluginManager().callEvent(evt);
            }

			@Override
			public void onClose(Player player, Inventory inv) {
			}
        }, plugin, true);
		menu.setOption(0, new ItemStack(Material.BOOK), main.colors.getTitle()+"Back to menu", main.colors.getInfo()+"Return back to the selection menu");
		menu.setOption(8, new ItemStack(Material.EMERALD), main.colors.getTitle()+"Sell", main.colors.getError()+"Unavailable");
		menu.setOption(1, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
		menu.setOption(2, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
		menu.setOption(3, new ItemStack(Material.PAPER), main.colors.getTitle()+">", main.colors.getInfo()+">");
		menu.setOption(5, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
		menu.setOption(6, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
		menu.setOption(7, new ItemStack(Material.PAPER), main.colors.getTitle()+"<", main.colors.getInfo()+"<");
		//4 is the input box
		return menu;
	}
	@EventHandler (priority = EventPriority.MONITOR)
	void alerts(PlayerJoinEvent event){
		String name = event.getPlayer().getName();
		if(!plugin.alerts.containsKey(name)){
			return;
		}
		event.getPlayer().sendMessage(plugin.alerts.get(name));
		plugin.alerts.remove(name);
		return;
	}
	
	public Minecart isEntityInCar(Entity e){
		if(e.getVehicle() == null){
			return null;
		}
		Entity v = e.getVehicle();
		while(v!=null && v.getVehicle() != null && !(v instanceof Minecart)){
			v = v.getVehicle();
		}
		if(v == null || !(v instanceof Minecart)){
			return null;
		}
		return (Minecart) v;
	}

}
