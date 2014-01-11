package net.stormdev.ucars.stats;

import java.io.Serializable;

import org.bukkit.plugin.Plugin;

public class NameStat extends Stat implements Serializable {
	private static final long serialVersionUID = -3699118953150138669L;
	Boolean overrideDefaults = true;
	public NameStat(String value, Plugin plugin) {
		super(value, plugin);
		this.val = value;
	}
	public String getName(){
		try {
			return (String) this.val;
		} catch (Exception e) {
			return "Car";
		}
	}
	public void setName(String name){
		this.val = name;
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
    	return StatType.NAME;
    }
}
