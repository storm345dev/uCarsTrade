package net.stormdev.ucars.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemRename {
	public static ItemStack rename(ItemStack item, String name){
		ItemMeta meta = item.getItemMeta();
		meta.setDisplayName(name);
		item.setItemMeta(meta);
		return item;
	}
	public static boolean hasCustomName(ItemStack item){
		if(item.getItemMeta().hasDisplayName()){
			return true;
		}
		return false;
	}
}
