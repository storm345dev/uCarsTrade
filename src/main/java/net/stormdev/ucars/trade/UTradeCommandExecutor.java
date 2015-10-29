package net.stormdev.ucars.trade;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.AITrackFollow;
import net.stormdev.ucars.trade.AIVehicles.DynamicLagReducer;
import net.stormdev.ucars.trade.AIVehicles.routing.NetworkConversionScan;
import net.stormdev.ucars.trade.AIVehicles.routing.RouteMethod;
import net.stormdev.ucars.trade.AIVehicles.spawning.nodes.AINodesSpawnManager;
import net.stormdev.ucars.trade.AIVehicles.spawning.nodes.NetworkScan;
import net.stormdev.ucars.trade.AIVehicles.spawning.nodes.Node;
import net.stormdev.ucars.utils.CarGenerator;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class UTradeCommandExecutor implements CommandExecutor {
	main plugin = null;
	public UTradeCommandExecutor(main plugin){
		this.plugin = plugin;
	}

	public boolean onCommand(final CommandSender sender, Command cmd, String alias,
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
				
				BlockFace dir = AITrackFollow.carriagewayDirection(target).getDirection();
				if(dir == null){
					sender.sendMessage(ChatColor.GREEN+"Road direction: Continue");
					return true;
				}
				sender.sendMessage(ChatColor.GREEN+"Road direction: "+dir+" ("+dir.getModX()+","+dir.getModZ()+")");
				return true;
			}
			else if(command.equalsIgnoreCase("debugrotation") && player != null && player.isOp()){
				Location loc = player.getLocation().add(player.getEyeLocation().getDirection().clone().setY(0).normalize().multiply(2));
				loc.setYaw(90+45);
				loc.getWorld().spawnEntity(loc, EntityType.MINECART);
				sender.sendMessage("Spawned cart with at location with diagonal rotation!");
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
			else if(command.equalsIgnoreCase("aispawn")){
				if(!AIRouter.isAIEnabled()){
					sender.sendMessage(ChatColor.RED+"AI Cars aren't enabled!");
					return true;
				}
				if(!(sender instanceof Player)){
					sender.sendMessage(ChatColor.RED+"Sorry this feature is for players!");
					return true;
				}
				
				Block under = player.getLocation().getBlock().getRelative(BlockFace.DOWN, 2);
				if(!AIRouter.isTrackBlock(under.getType())){
					sender.sendMessage(ChatColor.RED+"You need to stand above the road tracker block!!");
					return true;
				}
				BlockFace dir = AITrackFollow.carriagewayDirection(under).getDirection();
				if(dir == null){
					sender.sendMessage(ChatColor.RED+"Cannot spawn car where there is no determinable direction!");
					return true;
				}
				
				main.plugin.aiSpawns.spawnNPCCar(under.getRelative(BlockFace.UP, 2).getLocation(), dir);
				sender.sendMessage(ChatColor.GREEN+"Car spawned!");
				return true;
			}
			else if(command.equalsIgnoreCase("convertnetwork")){
				if(!AIRouter.isAIEnabled()){
					sender.sendMessage(ChatColor.RED+"AI Cars aren't enabled!");
					return true;
				}
				if(sender instanceof Player && !(sender.isOp())){
					sender.sendMessage(ChatColor.RED+"Sorry this feature is for ops only - And with v. good reason!");
					return true;
				}
				if(main.plugin.aiRouteMethod.equals(RouteMethod.ENCODED)){
					main.plugin.aiRouteMethod = RouteMethod.ORE;
					sender.sendMessage(ChatColor.ITALIC+"Switched back to ORE route following for the duration of the conversion...");
				}
				sender.sendMessage(ChatColor.YELLOW+"Commencing network scan and replacing of blocks... If you didn't backup the server then cross your fingers!");
				final Player pl = player;
				Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

					@Override
					public void run() {
						new NetworkConversionScan(pl);
						return;
					}});
				return true;
			}
			else if(command.equalsIgnoreCase("ainodes")){
				if(!AIRouter.isAIEnabled()){
					sender.sendMessage(ChatColor.RED+"AI Cars aren't enabled!");
					return true;
				}
				if(sender instanceof Player && !(sender.isOp())){
					sender.sendMessage(ChatColor.RED+"Sorry this feature is for ops only - And with v. good reason!");
					return true;
				}
				if(args.length < 2){
					sender.sendMessage(ChatColor.RED+"Options:");
					sender.sendMessage("/car ainodes count - Count and show how many nodes there are");
					sender.sendMessage("/car ainodes revalidate - Forces revalidation of ALL nodes (will likely cause lag)");
					sender.sendMessage("/car ainodes clear - Clears ALL nodes (PERMANENT; don't do this unless you want to have to re-calculate them all again)");
					sender.sendMessage("/car ainodes scan - Starts where the player is and then follows the whole connected road network, placing nodes for villager cars to spawn at (Will definitely cause lag)");
					sender.sendMessage("/car ainodes show - Sends block changes to the player to show the nodes near then - Will require relogging to make the world look correct again!");
					sender.sendMessage("/car ainodes delnode - Deletes a node being stood on!");
					return true;
				}
				String action = args[1];
				if(action.equalsIgnoreCase("revalidate")){
					sender.sendMessage(ChatColor.GREEN+"Starting revalidation of all nodes! This could take some time, look at the console for notification of when completed!");
					if(main.plugin.aiSpawns instanceof AINodesSpawnManager){
						Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

							@Override
							public void run() {
								((AINodesSpawnManager)main.plugin.aiSpawns).getNodesStore().revalidateNodes();
								return;
							}});
						
					}
					return true;
				}
				else if(action.equalsIgnoreCase("count")){
					sender.sendMessage(ChatColor.GRAY+"Counting unique nodes...");
					if(main.plugin.aiSpawns instanceof AINodesSpawnManager){
						Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

							@Override
							public void run() {
								int nodes = ((AINodesSpawnManager)main.plugin.aiSpawns).getNodesStore().getNodeCount();
								sender.sendMessage(ChatColor.GREEN+"There are "+nodes+" unique nodes!");
								return;
							}});
						
					}
					return true;
				}
				else if(action.equalsIgnoreCase("clear")){
					sender.sendMessage(ChatColor.RED+"CLEARING ALL saved nodes! (GOD I hope you know what this command does... or I feel so sorry for you whenever somebody who does realises what you have done - Just in case the existing nodes will be saved to 'oldNodes.nodelist')");
					if(main.plugin.aiSpawns instanceof AINodesSpawnManager){
						Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

							@Override
							public void run() {
								((AINodesSpawnManager)main.plugin.aiSpawns).getNodesStore().resetNodes();
								return;
							}});
					}
					return true;
				}
				else if(action.equalsIgnoreCase("scan")){
					if(!(sender instanceof Player)){
						sender.sendMessage(ChatColor.RED+"Only players can use this command");
						return true;
					}
					sender.sendMessage(ChatColor.YELLOW+"Commencing network scan...");
					final Player pl = player;
					Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

						@Override
						public void run() {
							new NetworkScan(pl);
							return;
						}});
					return true;
				}
				else if(action.equalsIgnoreCase("show")){
					if(!(sender instanceof Player)){
						sender.sendMessage(ChatColor.RED+"Only players can use this command");
						return true;
					}
					sender.sendMessage(ChatColor.GREEN+"Highlighting local nodes for your client with emerald blocks! To remove the (fake) highlighted blocks, relog!");
					if(main.plugin.aiSpawns instanceof AINodesSpawnManager){
						List<Node> nodeList = ((AINodesSpawnManager)main.plugin.aiSpawns).getNodesStore().getActiveNodes(player);
						sender.sendMessage(ChatColor.GRAY+"Painting "+nodeList.size()+" nodes...");
						for(Node node:new ArrayList<Node>(nodeList)){
							Location l = node.getLocation();
							l = l.getBlock().getRelative(BlockFace.UP).getLocation();
							player.sendBlockChange(l, Material.EMERALD_BLOCK, (byte) 0);
						}
					}
				}
				else if(action.equalsIgnoreCase("delnode")){
					if(!(sender instanceof Player)){
						sender.sendMessage(ChatColor.RED+"Only players can use this command");
						return true;
					}
					if(main.plugin.aiSpawns instanceof AINodesSpawnManager){
						List<Node> nodeList = ((AINodesSpawnManager)main.plugin.aiSpawns).getNodesStore().getActiveNodes(player);
						Location loc = player.getLocation().getBlock().getLocation();
						for(Node node:new ArrayList<Node>(nodeList)){
							Location l = node.getLocation().getBlock().getLocation();
							if(l.getX() == loc.getX() && l.getY() == loc.getY() && l.getZ() == loc.getZ()){
								((AINodesSpawnManager)main.plugin.aiSpawns).getNodesStore().removeNode(node);
								player.sendMessage(ChatColor.GREEN+"Node deleted!");
								return true;
							}
						}
						sender.sendMessage(ChatColor.RED+"Could not find a node to delete!");
						return true;
					}
				}
				else {
					sender.sendMessage("Do '/car ainodes' for a list of options!");
				}
				
				return true;
			}
			return true;
		}
		return false;
	}

}
