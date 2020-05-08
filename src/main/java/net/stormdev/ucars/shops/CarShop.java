package net.stormdev.ucars.shops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.stormdev.ucars.trade.Lang;
import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.guis.IconMenu;
import net.stormdev.ucars.trade.guis.IconMenu.OptionClickEvent;
import net.stormdev.ucars.trade.guis.IconMenu.OptionClickEventHandler;
import net.stormdev.ucars.utils.CarGenerator;
import net.stormdev.ucars.utils.CarValueCalculator;
import net.stormdev.ucarstrade.cars.CarPresets;
import net.stormdev.ucarstrade.cars.CarPresets.CarPreset;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.MaterialData;

public class CarShop {
	private double value;
	private IconMenu menu = null;
	private main plugin;
	private Map<Integer, IconMenu> menus = new HashMap<Integer, IconMenu>();
	
	public CarShop(main plugin){
		this.plugin = plugin;
		value = main.config.getDouble("general.carTrading.averageCarValue")*1.25;
		int v = (int)value*100;
		value = (double) v/100;
		setupMenu(plugin);
	}
	
	public void destroy(){
		menu.destroy();
	}

	public IconMenu getShopWindow(){
		if(menu == null){
			setupMenu(plugin);
		}
		return menu;
	}
	
	public void onClick(OptionClickEvent event){
		event.setWillClose(false);
		event.setWillDestroy(false);
		
		int slot = event.getPosition();
		
		if(!CarPresets.isCarPresetsUsed()){
			if(slot == 4){
				//Buy a car
				event.setWillClose(true);
				buyCar(event.getPlayer(), CarGenerator.gen());
			}
		}
		else {
			String title = ChatColor.stripColor(event.getInventory().getTitle());
			title = title.replaceFirst("Car Shop Page ", "");
			int page;
			try {
				page = Integer.parseInt(title);
			} catch (NumberFormatException e) {
				return;
			}
			
			int pos = event.getPosition();
			if(pos == 52){ //Prev page
				int prevPage = page-1;
				if(!pageExists(prevPage)){
					event.getPlayer().sendMessage(ChatColor.RED+"That page doesn't exist!");
					return;
				}
				final IconMenu nextPage = getPage(prevPage);
				final Player pl = event.getPlayer();
				event.setWillClose(true);
				Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

					@Override
					public void run() {
						nextPage.open(pl);
						return;
					}}, 2l);
				return;
			}
			else if(pos == 53){ //Next page
				int nextPage = page+1;
				if(!pageExists(nextPage)){
					event.getPlayer().sendMessage(ChatColor.RED+"That page doesn't exist!");
					return;
				}
				final IconMenu ppage = getPage(nextPage);
				final Player pl = event.getPlayer();
				event.setWillClose(true);
				Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

					@Override
					public void run() {
						ppage.open(pl);
						return;
					}}, 2l);
				return;
			}
			else {
				//Clicked a car
				List<CarPreset> cars = CarPresets.getPresets();
				int startPos = (page-1)*52; //52 items per page
				int arrayPos = startPos + pos;
				if(arrayPos >= cars.size()){
					return; //Clicked on a blank slot
				}
				CarPreset cp = cars.get(arrayPos);
				event.setWillClose(true);
				DrivenCar dc = new DrivenCar(cp.getName(), cp.getSpeed(), cp.getHealth(), cp.getAcceleration(), cp.getTurnAmountPerTick(), false, cp.getModifications());
				buyCar(event.getPlayer(), dc);
				return;
			}
		}
		return;
	}
	
	public void open(Player player){
		if(this.menu == null){
			player.sendMessage(ChatColor.RED+"No cars for sale!");
			return;
		}
		getShopWindow().open(player);
		return;
	}
	
	public void buyCar(Player player, DrivenCar dc){
		if(main.economy == null){
			main.plugin.setupEconomy();
			if(main.economy == null){
				player.sendMessage(main.colors.getError()+"No economy plugin found! Error!");
				return;
			}
		}
		double bal = main.economy.getBalance(player.getName());
		double cost = CarPresets.isCarPresetsUsed() ? CarValueCalculator.getCarValueForPurchase(dc):value;
		if(cost < 1){
			return;
		}
		double rem = bal-cost;
		if(rem<0){
			String msg = Lang.get("general.buy.notEnoughMoney");
			msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(bal+""));
			player.sendMessage(main.colors.getError()+msg);
			return;
		}
		main.economy.withdrawPlayer(player.getName(), cost);
		
		String currency = main.config.getString("general.carTrading.currencySign");
		String msg = Lang.get("general.buy.success");
		msg = msg.replaceAll(Pattern.quote("%item%"), "a car");
		msg = msg.replaceAll(Pattern.quote("%price%"), Matcher.quoteReplacement(currency+cost));
		msg = msg.replaceAll(Pattern.quote("%balance%"), Matcher.quoteReplacement(currency+rem));
		player.sendMessage(main.colors.getSuccess()+msg);
		
		//Give them the car
		ItemStack i = dc.toItemStack();
		player.getInventory().addItem(i);
		
		return;
	}
	
	private boolean pageExists(int page){
		return menus.containsKey(page);
	}
	
	public IconMenu getPage(int page){
		return menus.get(page);
	}
	
	public void setupMenu(main plugin){
		String currency = main.config.getString("general.carTrading.currencySign");
		
		if(!CarPresets.isCarPresetsUsed()){
			this.menu = new IconMenu("Car Shop", 9, new OptionClickEventHandler(){

				public void onOptionClick(OptionClickEvent event) {
					onClick(event);
					return;
				}}, plugin);
			List<String> info = new ArrayList<String>();
			info.add(main.colors.getTitle()+"[Price:] "+main.colors.getInfo()+currency+value);
			this.menu.setOption(4, new ItemStack(Material.MINECART), main.colors.getTitle()+"Buy Random Car", info);
		}
		else {
			//Paged icon menu, eurgh
			List<CarPreset> cars = CarPresets.getPresets();
			int pagesNeeded = (int) Math.ceil(((double)cars.size()) / 52.0d); //52 per page
			main.logger.info("Pages of cars: "+pagesNeeded+", cars: "+cars.size());
			for(int i=0;i<pagesNeeded;i++){
				int startPos = i*52;
				IconMenu page = new IconMenu("Car Shop Page "+(i+1), 54, new OptionClickEventHandler(){

					public void onOptionClick(OptionClickEvent event) {
						onClick(event);
						return;
					}}, plugin);
				
				for(int pos=0;pos<52;pos++){
					if((startPos+pos) >= cars.size()){
						continue; //Skip
					}
					int arrayPos = (startPos+pos);
					CarPreset cp = cars.get(arrayPos);
					List<String> lore = new ArrayList<String>();
					lore.add(ChatColor.YELLOW+"Speed: "+cp.getSpeed()+"x");
					lore.add(ChatColor.YELLOW+"Health: "+cp.getHealth());
					lore.add(ChatColor.YELLOW+"Acceleration: "+cp.getAcceleration()*10.0d);
					lore.add(ChatColor.YELLOW+"Handling: "+cp.getTurnAmountPerTick()*10.0d);
					if(cp.getModifications().size() > 0){
						lore.add(ChatColor.YELLOW+"Modifications:");
						for(String n:cp.getModifications()){
							lore.add(ChatColor.YELLOW+" -"+n);
						}
					}
					lore.add(ChatColor.WHITE+"Price: "+currency+CarValueCalculator.getCarValueForPurchase(new DrivenCar(cp.getName(), cp.getSpeed(), cp.getHealth(), cp.getAcceleration(), cp.getTurnAmountPerTick(), false, cp.getModifications())));

					MaterialData md = cp.toItemStack().getData();
					ItemStack is = new ItemStack(md.getItemType());
					is.setData(md);
					page.setOption(pos, is, ChatColor.BLUE+cp.getName(),
							lore);
				}
				page.setOption(52, new ItemStack(Material.PAPER), main.colors.getTitle()+"Previous Page", main.colors.getInfo()+"Go to previous page");
				page.setOption(53, new ItemStack(Material.PAPER), main.colors.getTitle()+"Next Page", main.colors.getInfo()+"Go to next page");
				if(i == 0){
					this.menu = page;
				}
				menus.put(i+1, page);
			}
		}
	}
	
}
