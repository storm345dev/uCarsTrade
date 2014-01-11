package net.stormdev.ucars.stats;

import java.io.Serializable;

import org.bukkit.plugin.Plugin;

public class SpeedStat extends Stat implements Serializable {
	private static final long serialVersionUID = -1817681841757332605L;
	Boolean overrideDefaults = true;
	public SpeedStat(double value, Plugin plugin) {
		super(value, plugin);
		val = value;
	}
	public double getSpeedMultiplier(){
		try {
			return (Double) this.val;
		} catch (Exception e) {
			return 30;
		}
	}
	public void setSpeedMultiplier(double speedMod){
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
