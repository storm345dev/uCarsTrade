package net.stormdev.ucars.stats;

import java.io.Serializable;

import org.bukkit.plugin.Plugin;

public class HealthStat extends Stat implements Serializable {
	private static final long serialVersionUID = -5068812300427014970L;
	Boolean overrideDefaults = true;
	public HealthStat(double value, Plugin plugin) {
		super(value, plugin);
		this.val = value;
	}
	public double getHealth(){
		try {
			return (Double) this.val;
		} catch (Exception e) {
			return 100;
		}
	}
	public void setHealth(double speedMod){
		this.val = speedMod;
		return;
	}
    public Boolean getOverrideDefaults(){
    	return this.overrideDefaults;
    }
    public void setOverrideDefaults(Boolean bool){
    	this.overrideDefaults = bool;
    	return;
    }
    public StatType getType(){
    	return StatType.SPEED;
    }
}
