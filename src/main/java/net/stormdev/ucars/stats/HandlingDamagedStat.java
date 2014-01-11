package net.stormdev.ucars.stats;

import java.io.Serializable;

import org.bukkit.plugin.Plugin;

public class HandlingDamagedStat extends Stat implements Serializable{
	private static final long serialVersionUID = -3043496785688659505L;
	Boolean overrideDefaults = true;
	public HandlingDamagedStat(Boolean value, Plugin plugin) {
		super(value, plugin);
		this.val = value;
	}
	public Boolean getDamagedHandling(){
		try {
			return (Boolean) this.val;
		} catch (Exception e) {
			return false;
		}
	}
	public void setDamagedHandling(Boolean damaged){
		this.val = damaged;
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
    	return StatType.HANDLING_DAMAGED;
    }
}
