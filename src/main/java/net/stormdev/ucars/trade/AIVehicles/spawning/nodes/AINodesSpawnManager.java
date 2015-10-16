package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.spawning.AbstractAISpawnManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class AINodesSpawnManager extends AbstractAISpawnManager {

	private NodesStore nodes = null;
	private BukkitTask task = null;
	private long spawnRate = 50l;
	public static int minDistance = 30;
	private int maxDistance = 70;
	
	public AINodesSpawnManager(main plugin, boolean enabled, File nodesSaveFile) {
		super(plugin, enabled);
		this.nodes = new NodesStore(nodesSaveFile);
		if(!main.config.contains("general.ai.minSpawnDistanceFromPlayers")){
			main.config.set("general.ai.minSpawnDistanceFromPlayers", 30);
		}
		minDistance = main.config.getInt("general.ai.minSpawnDistanceFromPlayers");
		if(!main.config.contains("general.ai.maxSpawnDistanceFromPlayers")){
			main.config.set("general.ai.maxSpawnDistanceFromPlayers", 70);
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
		return main.random.nextInt(7) < 1; //1/7 chance
	}
	
	private int randomMinCarSpacing(){
		return main.random.nextInt(15-5) + 5;
	}

	@Override
	public void initSpawnTask() {
		final double minDistanceSquared = Math.pow(minDistance, 2);
		final double maxDistanceSquared = Math.pow(maxDistance, 2);
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
					
					int chance = 50;
					chance -= (activeNodes.size()*0.4d); //Make it more likely to spawn a car; the more nodes there are active
					if(chance < 2){
						chance = 2;
					}
					
					if(!(main.random.nextInt(chance) < 1)){
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
					
					chance = (int) ((nearCount*2.3) + 1);
					if(!(main.random.nextInt(chance) <= 1)){ //Avoids LOTS of cars spawning where there's LOTS of players
						continue;
					}
					
					boolean closeCar = false;
					int minSpacing = randomMinCarSpacing();
					final List<Entity> ents = new ArrayList<Entity>();
					try {
						Bukkit.getScheduler().callSyncMethod(plugin, new Callable<Void>(){

							@Override
							public Void call() throws Exception {
								ents.addAll(randomNodeLoc.getWorld().getEntities());
								return null;
							}}).get();
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					for(Entity e:ents){ //PLEASE don't get caught by AsyncCatcher...
						if(!e.getType().equals(EntityType.MINECART) && e.hasMetadata("trade.npc")){
							continue;
						}
						Location l = e.getLocation();
						if(l.distanceSquared(randomNodeLoc) < minSpacing){
							closeCar = true;
							break;
						}
					}
					if(closeCar){
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
