package net.stormdev.ucars.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.stormdev.ucars.stats.HandlingDamagedStat;
import net.stormdev.ucars.stats.HealthStat;
import net.stormdev.ucars.stats.NameStat;
import net.stormdev.ucars.stats.SpeedStat;
import net.stormdev.ucars.stats.Stat;
import net.stormdev.ucars.trade.main;

import org.bukkit.Material;
import org.bukkit.entity.Minecart;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public class Car implements Serializable {
	private static final long serialVersionUID = -4379501444781549934L;
	public Boolean isPlaced = false;
	public UUID id = null;
	public HashMap<String, Stat> stats = new HashMap<String, Stat>();
	public Car(Boolean isPlaced, HashMap<String, Stat> stats){
		this.isPlaced = isPlaced;
		this.stats = stats;
		this.id = UUID.randomUUID();
	}
	public UUID getId(){
		return this.id;
	}
	public void setId(UUID newId){
		this.id = newId;
		return;
	}
	public ItemStack getItem(){
		ItemStack stack = new ItemStack(Material.MINECART);
		stack.setDurability((short) 20);
		ItemMeta meta = stack.getItemMeta();
		List<String> lore = new ArrayList<String>();
		lore.clear();
		String health = "Undamaged";
		String handling = "Undamaged";
		String speed = "+15.0";
		String name = "Car";
		List<String> extra = new ArrayList<String>();
		for(Stat stat:stats.values()){
			if(stat instanceof SpeedStat){
				speed = ""+stat.getValue();
			}
			else if(stat instanceof HandlingDamagedStat){
				HandlingDamagedStat s = (HandlingDamagedStat) stat;
				if(s.getDamagedHandling()){
					handling = "Damaged";
				}
			}
			else if(stat instanceof HealthStat){
				health = ""+stat.getValue();
			}
			else if(stat instanceof NameStat){
				name = ""+stat.getValue();
			}
			else{
				extra.add("-"+stat.getValue());
			}	
		}
		lore.add(main.colors.getTitle()+this.id);
		lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+speed+"x");
		double max = ucars.config.getDouble("general.cars.health.max");
		lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health+"/"+max);
		lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+handling);
		for(String x:extra){
			lore.add(main.colors.getInfo()+x);
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
		stack = ItemRename.rename(stack, name);
		return stack;
	}
	public HashMap<String, Stat> getStats(){
		return this.stats;
	}
	public void setStats(HashMap<String, Stat> stats){
		this.stats = stats;
		return;
	}
    
}
