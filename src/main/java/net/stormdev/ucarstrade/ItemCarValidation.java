package net.stormdev.ucarstrade;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import net.stormdev.ucars.trade.Colors;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class ItemCarValidation {
	public static DrivenCar getCar(ItemStack item){
		if(item == null || !item.getType().equals(Material.MINECART)){
			return null;
		}
		
		ItemMeta im = item.getItemMeta();
		if(im == null || im.getLore() == null){
			return null;
		}
		
		List<String> lore = im.getLore();
		
		String name = im.getDisplayName();
		if(name == null || name.equalsIgnoreCase("null")){
			name = "Car";
		}
		
		DrivenCar c = getCarFromLore(im.getDisplayName(), lore);
		return c;
	}
	
	public static DrivenCar getCarFromLore(String name, List<String> lore){
		if(lore.size() < 3){
			return null;
		}
		int i = 0;
		if(!Colors.strip((lore.get(i))).toLowerCase().contains("[speed:]")){
			i = 1;
			if(!Colors.strip((lore.get(i))).toLowerCase().contains("[speed:]")){ //Using deprecated format
				return null;
			}
		}
		
		double speed = 1;
		double health = 50;
		boolean isHandlingDamaged = false;
		List<String> modifiers = new ArrayList<String>();
		
		String line = Colors.strip(lore.get(i)).toLowerCase(); //[Speed:] 0.8x
		String speedRaw = line.replaceFirst(Pattern.quote("[speed:] "), "").replaceAll(Pattern.quote("x"), "");
		try {
			speed = Double.parseDouble(speedRaw);
		} catch (NumberFormatException e) {
			//Oh well
		}
		
		i++;
		line = Colors.strip(lore.get(i)).toLowerCase(); //[Health:] 10.0/100.0
		String[] healthRaw = line.replaceFirst(Pattern.quote("[health:] "), "").split(Pattern.quote("/"));
		try {
			health = Double.parseDouble(healthRaw[0]);
		} catch (NumberFormatException e) {
			//Oh well, unable to read value
		}
		
		i++;
		if(i >= lore.size()){
			return null;
		}
		else {
			line = Colors.strip(lore.get(i)).toLowerCase(); //[Handling:] undamaged
			String handlingRaw = line.replaceFirst(Pattern.quote("[handling:] "), "").trim();
			if(handlingRaw.equalsIgnoreCase("damaged")){
				isHandlingDamaged = true;
			}
		}
		
		try {
			if(lore.size() > i){
				i++;
				for(@SuppressWarnings("unused")
				int z=i;i<lore.size();i++){
					line = Colors.strip(lore.get(i)).toLowerCase(); //-Modifier: <Name>
					modifiers.add(line.replaceFirst(Pattern.quote("-modifier: "), "").trim());
				}
			}
		} catch (Exception e) {
			return null;
		}
		return new DrivenCar(name, speed, health, isHandlingDamaged, modifiers);
	}
}
