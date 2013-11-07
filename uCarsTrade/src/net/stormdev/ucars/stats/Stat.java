package net.stormdev.ucars.stats;

import java.io.Serializable;

import org.bukkit.plugin.Plugin;

public class Stat implements Serializable {
	private static final long serialVersionUID = -7275161546527988371L;
	public Object val = null;
	public Stat(Object val, Plugin plugin){
		this.val = val;
	}
	public Object getValue(){
		return val;
	}
}
