package net.stormdev.ucars.trade;

import org.bukkit.ChatColor;

public class Colors {
	private String success = "";
	private String error = "";
	private String info = "";
	private String title = "";
	private String tp = "";

	public Colors(String success, String error, String info, String title,
			String tp) {
		this.success = main.colorise(success);
		this.error = main.colorise(error);
		this.info = main.colorise(info);
		this.title = main.colorise(title);
		this.tp = main.colorise(tp);
	}
	
	public static String strip(String in){
		in = ChatColor.stripColor(in);
		in = ChatColor.translateAlternateColorCodes('&', in);
		in = ChatColor.translateAlternateColorCodes('§', in);
		in = ChatColor.translateAlternateColorCodes('?', in);
		return in;
	}

	public String getSuccess() {
		return this.success;
	}

	public String getError() {
		return this.error;
	}

	public String getInfo() {
		return this.info;
	}

	public String getTitle() {
		return this.title;
	}

	public String getTp() {
		return this.tp;
	}
}
