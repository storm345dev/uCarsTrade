package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.spawning.AbstractAISpawnManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.useful.ucars.util.UEntityMeta;

public class AINodesSpawnManager extends AbstractAISpawnManager {

	private static NodesStore nodes = null;
	private BukkitTask task = null;
	private BukkitTask task2 = null;
	private long spawnRate = 35l;
	public static int minDistance = 30;
	private int maxDistance = 70;
	
	public static Map<String, List<Entity>> entitiesInWorld = new HashMap<String, List<Entity>>();
	
	public AINodesSpawnManager(main plugin, boolean enabled, File nodesSaveFile) {
		super(plugin, enabled);
		nodes = new NodesStore(nodesSaveFile);
		if(!main.config.contains("general.ai.maxSpawnDistanceFromPlayers")){
			main.config.set("general.ai.maxSpawnDistanceFromPlayers", 70);
		}
		maxDistance = main.config.getInt("general.ai.maxSpawnDistanceFromPlayers");
		maxDistance = Math.min(maxDistance, (Bukkit.getServer().getViewDistance()*16)-10);
		if(!main.config.contains("general.ai.minSpawnDistanceFromPlayers")){
			main.config.set("general.ai.minSpawnDistanceFromPlayers", 30);
		}
		minDistance = main.config.getInt("general.ai.minSpawnDistanceFromPlayers");
		if(maxDistance - minDistance < 10){
			minDistance = maxDistance - 10;
		}
		AIRouter.PLAYER_RADIUS = maxDistance;
		plugin.saveConfig();
	}
	
	public static NodesStore getNodesStore(){
		return nodes;
	}

	@Override
	public void shutdown() {
		if(task != null){
			task.cancel();
		}
		if(task2 != null){
			task2.cancel();
			this.entitiesInWorld.clear();
		}
	}
	
	private boolean randomDoSpawn(){
		if(getSpawnedAICount() < 1){
			return true;
		}
		return main.random.nextInt(5) < 1;
	}
	
	private int randomMinCarSpacingSquared(){
		return main.random.nextInt(100-4) + 4;
	}

	@Override
	public void initSpawnTask() {
		final double minDistanceSquared = Math.pow(minDistance, 2);
		final double maxDistanceSquared = Math.pow(maxDistance, 2);
		task2 = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable(){

			@Override
			public void run() {
				for(World w:Bukkit.getWorlds()){
					entitiesInWorld.put(w.getName(), new ArrayList<Entity>(w.getEntities()));
				}
				return;
			}}, 20l, 20l);
		task = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){

			@Override
			public void run() {
				if(!enabled){
					return;
				}
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
					
					int chance = 20;
					chance -= (activeNodes.size()*0.5d); //Make it more likely to spawn a car; the more nodes there are active
					if(chance < 0){
						chance = 0;
					}
					
					if(!(chance == 0 || main.random.nextInt(chance) < 1)){
						continue;
					}
					
					final Node randomNode = activeNodes.get(main.random.nextInt(activeNodes.size()));
					
					final Location randomNodeLoc = randomNode.getLocation();
					
					int nearCount = 0;
					boolean cancel = false;
					for(Player pl:new ArrayList<Player>(Bukkit.getOnlinePlayers())){
						if(pl.equals(player)){
							continue;
						}
						if(pl.getLocation().distanceSquared(randomNodeLoc) < minDistanceSquared){
							cancel = true;
							break; //Don't spawn at node if other player right near it
						}
						else if(pl.getLocation().distanceSquared(randomNodeLoc) < maxDistanceSquared){
							//It's also near to them
							nearCount++;
						}
					}
					
					if(cancel){
						continue;
					}
					
					chance = (int) ((nearCount));
					if(!(chance == 0 || main.random.nextInt(50) > chance)){ //Avoids LOTS of cars spawning where there's LOTS of players
						continue;
					}
					
					boolean closeCar = false;
					int minSpacing = randomMinCarSpacingSquared();
					final List<Entity> ents = new ArrayList<Entity>();
					try {
						List<Entity> inWorld = entitiesInWorld.get(randomNodeLoc.getWorld().getName());
						if(inWorld != null){
							ents.addAll(inWorld);
						}
					} catch (Exception e1) {
						e1.printStackTrace();
						continue;
					}
					for(Entity e:ents){ //PLEASE don't get caught by AsyncCatcher...
						if(!UEntityMeta.hasMetadata(e, "trade.npc")){
							continue;
						}
						Location l = e.getLocation();
						if(l.toVector().clone().subtract(randomNodeLoc.toVector()).lengthSquared() < minSpacing){
							closeCar = true;
							break;
						}
					}
					if(closeCar){
						continue;
					}
					
					randomNode.spawnAICarIfLogicalToDoSo();
				}
				return;
			}}, spawnRate, spawnRate);
	}

}
