package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.spawning.AbstractAISpawnManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class AINodesSpawnManager extends AbstractAISpawnManager {

	private NodesStore nodes = null;
	private BukkitTask task = null;
	private long spawnRate = 40l;
	private int minDistance = 10;
	private int maxDistance = 40;
	
	public AINodesSpawnManager(main plugin, boolean enabled, File nodesSaveFile) {
		super(plugin, enabled);
		this.nodes = new NodesStore(nodesSaveFile);
		if(!main.config.contains("general.ai.minSpawnDistanceFromPlayers")){
			main.config.set("general.ai.minSpawnDistanceFromPlayers", 10);
		}
		minDistance = main.config.getInt("general.ai.minSpawnDistanceFromPlayers");
		if(!main.config.contains("general.ai.maxSpawnDistanceFromPlayers")){
			main.config.set("general.ai.maxSpawnDistanceFromPlayers", 40);
		}
		maxDistance = main.config.getInt("general.ai.maxSpawnDistanceFromPlayers");
		AIRouter.PLAYER_RADIUS = maxDistance;
		plugin.saveConfig();
	}
	
	public NodesStore getNodesStore(){
		return this.nodes;
	}

	@Override
	public void shutdown() {
		if(task != null){
			task.cancel();
		}
	}
	
	private boolean randomDoSpawn(){
		return main.random.nextInt(1) < 1; //50/50 chance
	}

	@Override
	public void initSpawnTask() {
		final double minDistanceSquared = Math.pow(minDistance, 2);
		final double maxDistanceSquared = Math.pow(maxDistance, 2);
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){

			@Override
			public void run() { //TODO Scan for nodes async, but if bukkit doesn't like it then will do sync
				if(!AINodesSpawnManager.this.isNPCCarsSpawningNow()){
					return;
				}
				if(AINodesSpawnManager.this.getSpawnedAICount() >= AINodesSpawnManager.this.getCurrentAICap()){
					return;
				}
				for(Player player:new ArrayList<Player>(Bukkit.getOnlinePlayers())){
					if(!randomDoSpawn()){ //Random if we spawn cars near this player this cycle or not
						continue;
					}
					List<Node> activeNodes = getNodesStore().getActiveNodes(player, minDistance, maxDistance);
					if(activeNodes.size() < 1){ //No nodes near the player
						continue;
					}
					
					int chance = 60;
					chance -= activeNodes.size(); //Make it more likely to spawn a car; the more nodes there are active
					if(chance < 2){
						chance = 2;
					}
					
					if(!(main.random.nextInt(chance) < 1)){
						continue;
					}
					
					final Node randomNode = activeNodes.get(main.random.nextInt(activeNodes.size()));
					
					Location randomNodeLoc = randomNode.getLocation();
					
					int nearCount = 0;
					for(Player pl:new ArrayList<Player>(Bukkit.getOnlinePlayers())){
						if(pl.equals(player)){
							continue;
						}
						if(pl.getLocation().distanceSquared(randomNodeLoc) < minDistanceSquared){
							return; //Don't spawn at node if other player right near it
						}
						else if(pl.getLocation().distanceSquared(randomNodeLoc) < maxDistanceSquared){
							//It's also near to them
							nearCount++;
						}
					}
					chance = (int) ((nearCount*1.5) + 1);
					if(!(main.random.nextInt(chance) <= 1)){ //Avoids LOTS of cars spawning where there's LOTS of players
						continue;
					}
					
					Bukkit.getScheduler().runTask(plugin, new Runnable(){

						@Override
						public void run() {
							randomNode.spawnAICarIfLogicalToDoSo();
							return;
						}});
				}
				return;
			}}, spawnRate, spawnRate);
	}

}
