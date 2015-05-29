package net.stormdev.ucars.trade;

import java.util.Set;

import net.stormdev.ucars.trade.AIVehicles.AIProbingSpawnManager;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.AITrackFollow;
import net.stormdev.ucars.trade.AIVehicles.DynamicLagReducer;
import net.stormdev.ucars.utils.CarGenerator;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
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
			else if(command.equalsIgnoreCase("debug")){
				if(player == null){
					return true;
				}
				Block target = player.getTargetBlock(((Set<Material>)null), 10);
				if(target == null){
					sender.sendMessage(ChatColor.RED+"You aren't looking a block");
					return true;
				}
				Material mat = target.getType();
				if(!AIRouter.isTrackBlock(mat)){
					sender.sendMessage(ChatColor.RED+"You aren't looking at an AI tracker block, you are looking at "+mat.name());
					return true;
				}
				
				BlockFace dir = AITrackFollow.carriagewayDirection(target);
				sender.sendMessage(ChatColor.GREEN+"Road direction: "+dir);
				return true;
			}
			else if(command.equalsIgnoreCase("ai")){
				if(!AIRouter.isAIEnabled()){
					sender.sendMessage(ChatColor.RED+"AI Cars aren't enabled!");
					return true;
				}
				int cap = main.plugin.aiSpawns.getCurrentAICap();
				int spawn = main.plugin.aiSpawns.getSpawnedAICount();
				double availMem = Math.round(DynamicLagReducer.getAvailableMemory()*100.0d)/100.0d;
				double maxMem = Math.round(DynamicLagReducer.getMaxMemory()*100.0d)/100.0d;
				double percent = Math.round(((availMem/maxMem)*100)*100.0d)/100.0d;
				sender.sendMessage(ChatColor.YELLOW+"Available memory: "+availMem+"MB / "+maxMem+"MB ("+percent+"%)");
				sender.sendMessage(ChatColor.YELLOW+"Server Resource Score: "+DynamicLagReducer.getResourceScore()+"%");
				sender.sendMessage(ChatColor.YELLOW+"Server TPS: "+Math.round(DynamicLagReducer.getTPS()*100.0d)/100.0d+"(/20.0)");
				sender.sendMessage(ChatColor.GREEN+"Currently spawning NPC cars: "+main.plugin.aiSpawns.isNPCCarsSpawningNow());
				sender.sendMessage(ChatColor.GREEN+"Currently spawned: "+spawn);
				sender.sendMessage(ChatColor.GREEN+"Current spawn cap: "+cap);
				return true;
			}
			return true;
		}
		return false;
	}

}
