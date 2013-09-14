package net.stormdev.ucars.trade;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class UTradeCommandExecutor implements CommandExecutor {
	main plugin = null;
	public UTradeCommandExecutor(main plugin){
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		
		
		return false;
	}

}
