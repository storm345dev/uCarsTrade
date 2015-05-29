package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.File;
import java.util.ArrayList;

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
	private long spawnRate = 20l;
	private int minDistance = 10;
	private int maxDistance = 35;
	
	public AINodesSpawnManager(main plugin, boolean enabled, File nodesSaveFile) {
		super(plugin, enabled);
		this.nodes = new NodesStore(nodesSaveFile);
		AIRouter.PLAYER_RADIUS = 40;
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
		return main.random.nextInt(10) < 1;
	}

	@Override
	public void initSpawnTask() {
		final double minDistanceSquared = Math.pow(minDistance, 2);
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){

			@Override
			public void run() { //TODO Scan for nodes async, but if bukkit doesn't like it then will do sync
				if(!AINodesSpawnManager.this.isNPCCarsSpawningNow()){
					return;
				}
				for(Player player:new ArrayList<Player>(Bukkit.getOnlinePlayers())){
					if(!randomDoSpawn()){ //Random if we spawn cars near this player this cycle or not
						continue;
					}
					final Node randomNode = getNodesStore().getRandomActiveNode(player, minDistance, maxDistance);
					if(randomNode == null){ //No nodes near the player
						continue;
					}
					
					Location randomNodeLoc = randomNode.getLocation();
					
					for(Player pl:new ArrayList<Player>(Bukkit.getOnlinePlayers())){
						if(pl.equals(player)){
							continue;
						}
						if(pl.getLocation().distanceSquared(randomNodeLoc) < minDistanceSquared){
							return; //Don't spawn at node if other player right near it
						}
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
