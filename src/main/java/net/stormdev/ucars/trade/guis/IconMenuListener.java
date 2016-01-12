package net.stormdev.ucars.trade.guis;

import net.stormdev.ucars.trade.main;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public class IconMenuListener implements Listener {
	public IconMenuListener(){
		Bukkit.getPluginManager().registerEvents(this, main.plugin);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	void invClose(InventoryCloseEvent event){
		Inventory inv = event.getInventory();
		InventoryHolder ih = inv.getHolder();
		if(ih != null && ih instanceof IconMenu){
			((IconMenu)ih).invClose(event);
		}
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	void invClick(InventoryClickEvent event){
		Inventory inv = event.getInventory();
		InventoryHolder ih = inv.getHolder();
		if(ih != null && ih instanceof IconMenu){
			((IconMenu)ih).onInventoryClick(event);
		}
	}
}
