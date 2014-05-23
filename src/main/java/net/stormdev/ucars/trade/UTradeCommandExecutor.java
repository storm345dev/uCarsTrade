package net.stormdev.ucars.trade;

import net.stormdev.ucars.utils.CarGenerator;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UTradeCommandExecutor implements CommandExecutor {
	main plugin = null;
	public UTradeCommandExecutor(main plugin){
		this.plugin = plugin;
	}

	public boolean onCommand(CommandSender sender, Command cmd, String alias,
			String[] args) {
		Player player = null;
		if(sender instanceof Player){
			player = (Player) sender;
		}
		if(cmd.getName().equalsIgnoreCase("uCarsTrade")){
			if(args.length < 1){
				return false;
			}
			String command = args[0];
			if(command.equalsIgnoreCase("give")){
				if(player == null){
					sender.sendMessage(main.colors.getError()+Lang.get("general.playersOnly"));
					return true;
				}
				if(args.length < 2){
					return false;
				}
				String type = args[1];
				if(type.equalsIgnoreCase("random")){
					//Generate and give a random car
					DrivenCar car = CarGenerator.gen();
					ItemStack item = car.toItemStack();
			        player.getInventory().addItem(item);
			        sender.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.give"));
					return true;
				}
				else{
					// /uCarsTrade give <Speed> <Health> <Name> (4 args)
					if(args.length < 4){
						return false;
					}
					String speedRaw = args[1];
					String healthRaw = args[2];
					String name = args[3];
					for(int i=4;i<args.length; i++){
						name += " "+args[i];
					}
					double speed = 1;
					try {
						speed = Double.parseDouble(speedRaw);
					} catch (NumberFormatException e) {
						return false;
					}
					double health = 10;
					try {
						health = Double.parseDouble(healthRaw);
					} catch (NumberFormatException e) {
						return false;
					}
					//Generate and give a random car
					DrivenCar car = CarGenerator.gen(speed, health, name);
					ItemStack item = car.toItemStack();
			        player.getInventory().addItem(item);
			        sender.sendMessage(main.colors.getSuccess()+Lang.get("general.cmd.give"));
					return true;
				}
			}
			return true;
		}
		return false;
	}

}
