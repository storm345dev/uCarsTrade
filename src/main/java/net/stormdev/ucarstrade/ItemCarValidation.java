package net.stormdev.ucarstrade;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

import net.stormdev.ucars.trade.Colors;
import net.stormdev.ucarstrade.cars.DrivenCar;

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
			String firstLine = Colors.strip((lore.get(i))).toLowerCase();
			if(firstLine.equalsIgnoreCase("plane") || firstLine.equalsIgnoreCase("helicopter") || !firstLine.equalsIgnoreCase("car")){
				return null;
			}
			else if(!firstLine.equalsIgnoreCase("car")){ //It doesn't say car...
				try {
					UUID.fromString(firstLine); //Test if it's a UUID string
				} catch (Exception e) {
					// Not an old car either
					return null;
				}
				//It's a UUID...
				
			}
			i = 1;
			if(!Colors.strip((lore.get(i))).toLowerCase().contains("[speed:]")){ //Using deprecated format
				return null;
			}
		}
		
		double speed = 1;
		double health = 50;
		boolean isHandlingDamaged = false;
		double turnAmountPerTick = 5;
		double acceleration = 1;
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
			if(!line.contains("handling")){
				return null;
			}
			String handlingRaw = line.replaceFirst(Pattern.quote("[handling:] "), "").trim();
			if(handlingRaw.equalsIgnoreCase("damaged")){ //Old style item
				isHandlingDamaged = true;
			}
			try {
				double d = Double.parseDouble(handlingRaw);
				turnAmountPerTick = d/10.0d;
			} catch (NumberFormatException e) {
				//Oh well; it's the old style item
			}
		}
		
		if(i+1 >= lore.size()){ //Old style item
			return new DrivenCar(name, speed, health, 1, 5, isHandlingDamaged, modifiers);
		}
		
		line = Colors.strip(lore.get(i+1)).toLowerCase(); //[Acceleration:] accelMod*10
		if(line.contains("acceleration")){
			i++;
			String accelRaw = line.replaceFirst(Pattern.quote("[acceleration:] "), "").trim();
			try {
				double d = Double.parseDouble(accelRaw);
				acceleration = d/10.0d;
			} catch (NumberFormatException e) {
				return null;
			}
			
			line = Colors.strip(lore.get(i+1)).toLowerCase(); //[Damage:] accelMod*10
			if(line.contains("damage")){
				i++;
				String damageRaw = line.replaceFirst(Pattern.quote("[damage:] "), "").trim();
				if(damageRaw.equalsIgnoreCase("damaged")){ //Old style item
					isHandlingDamaged = true;
				}
			}
		}
		
		try {
			if(lore.size() > i){
				i++;
				for(@SuppressWarnings("unused")
				int z=i;i<lore.size();i++){
					line = Colors.strip(lore.get(i)); //-Modifier: <Name>
					modifiers.add(line.replaceFirst(Pattern.quote("-Modifier: "), "").trim());
				}
			}
		} catch (Exception e) {
			return null;
		}
		return new DrivenCar(name, speed, health, acceleration, turnAmountPerTick, isHandlingDamaged, modifiers);
	}
}
