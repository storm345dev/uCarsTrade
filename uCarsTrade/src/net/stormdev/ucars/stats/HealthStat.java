package net.stormdev.ucars.stats;

import org.bukkit.plugin.Plugin;

import com.useful.ucarsCommon.StatValue;

public class HealthStat extends StatValue {
	Boolean overrideDefaults = true;
	public HealthStat(double value, Plugin plugin) {
		super(value, plugin);
	}
	public double getHealth(){
		try {
			return (Double) this.getValue();
		} catch (Exception e) {
			return 100;
		}
	}
	public void setHealth(double speedMod){
		this.setValue(speedMod);
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
