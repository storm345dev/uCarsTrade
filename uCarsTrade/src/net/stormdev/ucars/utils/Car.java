package net.stormdev.ucars.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import net.stormdev.ucars.stats.HandlingDamagedStat;
import net.stormdev.ucars.stats.HealthStat;
import net.stormdev.ucars.stats.SpeedStat;
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
	public HashMap<String, StatValue> stats = new HashMap<String, StatValue>();
	public Car(Boolean isPlaced, HashMap<String, StatValue> stats){
		this.isPlaced = isPlaced;
		this.stats = stats;
	}
	public Minecart getCar(Minecart base){
		//TODO Create item off of stats
		for(String statName:stats.keySet()){
			base.setMetadata(statName, stats.get(statName));
		}
		return base;
	}
	public ItemStack getItem(){
		ItemStack stack = new ItemStack(Material.MINECART);
		ItemMeta meta = stack.getItemMeta();
		List<String> lore = new ArrayList<String>();
		String health = "Undamaged";
		String handling = "Undamaged";
		String speed = "30.0";
		List<String> extra = new ArrayList<String>();
		for(StatValue stat:stats.values()){
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
			else{
				extra.add("-"+stat.getValue());
			}
			lore.add(main.colors.getTitle()+"[Speed:] "+main.colors.getInfo()+speed);
			double max = ucars.config.getDouble("general.cars.health.max");
			lore.add(main.colors.getTitle()+"[Health:] "+main.colors.getInfo()+health+"/"+max);
			lore.add(main.colors.getTitle()+"[Handling:] "+main.colors.getInfo()+handling);
			for(String x:extra){
				lore.add(main.colors.getInfo()+x);
			}
		}
		meta.setLore(lore);
		stack.setItemMeta(meta);
		return stack;
	}
	public HashMap<String, StatValue> getStats(){
		return this.stats;
	}
	public void setStats(HashMap<String, StatValue> stats){
		this.stats = stats;
		return;
	}
    
}
