package net.stormdev.ucars.stats;

import org.bukkit.plugin.Plugin;

import com.useful.ucarsCommon.StatValue;

public class HandlingDamagedStat extends StatValue {
	Boolean overrideDefaults = true;
	public HandlingDamagedStat(Object value, Plugin plugin) {
		super(value, plugin);
	}
	public Boolean getDamagedHandling(){
		try {
			return (Boolean) this.getValue();
		} catch (Exception e) {
			return false;
		}
	}
	public void setDamagedHandling(Boolean damaged){
		this.setValue(damaged);
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
