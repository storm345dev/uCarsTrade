package net.stormdev.ucars.stats;

import org.bukkit.plugin.Plugin;

import com.useful.ucarsCommon.StatValue;

public class SpeedStat extends StatValue {
	Boolean overrideDefaults = true;
	public SpeedStat(double value, Plugin plugin) {
		super(value, plugin);
	}
	public double getSpeedMultiplier(){
		try {
			return (Double) this.getValue();
		} catch (Exception e) {
			return 30;
		}
	}
	public void setSpeedMultiplier(double speedMod){
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
