package net.stormdev.ucars.trade.AIVehicles.spawning;

import java.util.ArrayList;
import java.util.List;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.AITrackFollow;
import net.stormdev.ucars.trade.AIVehicles.DynamicLagReducer;
import net.stormdev.ucars.trade.AIVehicles.TrackingData;
import net.stormdev.ucars.trade.AIVehicles.VelocityData;
import net.stormdev.ucars.utils.CarGenerator;
import net.stormdev.ucarstrade.cars.CarPresets.CarPreset;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Villager;

import com.useful.ucars.CarHealthData;
import com.useful.ucars.ucars;
import com.useful.ucarsCommon.StatValue;

public abstract class AbstractAISpawnManager implements AISpawnManager {

	protected main plugin;
	protected boolean enabled;
	protected boolean fullEnable;
	protected static Material roadEdge;
	protected Material junction;
	protected List<String> aiNames;
	protected static int cap = 30;
	protected static int liveCap = 5;
	protected static int spawnedCount = 0;
	
	public AbstractAISpawnManager(main plugin, boolean enabled){
		this.plugin = plugin;
		this.enabled = enabled;
		this.fullEnable = enabled;
		String edgeRaw = main.config.getString("general.ai.roadEdgeBlock");
		String junRaw = main.config.getString("general.ai.junctionBlock");
		aiNames = main.config.getStringList("general.ai.names");
		cap = main.config.getInt("general.ai.limit");
		
		liveCap = 5;
		// A task to dynamically change liveCap to match the server's current AI holdings
		Bukkit.getScheduler().runTaskTimerAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				// Use resource score to get live cap
				int score = DynamicLagReducer.getResourceScore();
				int newCap = liveCap;
				double tps = DynamicLagReducer.getTPS();
				if(score > 75 && tps > 19.4){
					newCap++;
				}
				else if(score < 70 || tps < 17.5){
					newCap *= 0.5; //Half
				}
				else if(score < 70 || tps < 18){
					newCap -= 10;
				}
				else if(score < 70 || tps < 19){
					newCap -= 5;
				}
				else if(score < 75 || tps < 19){
					newCap--;
				}
				
				if(newCap != liveCap){
					if(newCap > cap){
						newCap = cap;
					}
					if(newCap < 2){
						newCap = 2; //Min of 2
					}
					liveCap = newCap;
				}
				
				return;
			}}, 100l, 100l); //Every 5s
		Bukkit.getScheduler().runTaskTimerAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				int aiCars = 0;
				for(World world:new ArrayList<World>(Bukkit.getWorlds())){
					for(Entity e:new ArrayList<Entity>(world.getEntities())){
						if(!(e instanceof Minecart)){
							continue;
						}
						Minecart cart = (Minecart) e;
						if(!cart.hasMetadata("trade.npc")){
							continue;
						}
						final DrivenCar c = main.plugin.carSaver.getCarInUse(cart.getUniqueId());
						if(c == null){
							continue;
						}
						
						if(!c.isNPC()){
							continue;
						}
						aiCars++;
					}
				}
				spawnedCount = aiCars;
				return;
			}}, 10*60*20l, 10*60*20l); //Every 10 mins
		
		new DynamicLagReducer().start();
		
		roadEdge = Material.getMaterial(edgeRaw);
		junction = Material.getMaterial(junRaw);
		if(roadEdge == null || junction == null){
			main.logger.info("Didn't enable AIs as configuration is invalid!");
			enabled = false;
		}
		if(enabled){
			main.plugin.getLogger().info("AI Cars enabled successfully!");
			initSpawnTask();
		}
	}
	
	public abstract void initSpawnTask();
	
	@Override
	public boolean isNPCCarsEnabled() {
		return this.fullEnable;
	}

	@Override
	public boolean isNPCCarsSpawningNow() {
		return this.enabled;
	}

	@Override
	public int getCurrentAICap() {
		return liveCap;
	}

	@Override
	public int getMaxAICap() {
		return cap;
	}

	@Override
	public int getSpawnedAICount() {
		return spawnedCount;
	}

	@Override
	public void decrementSpawnedAICount() {
		spawnedCount--;
		if(spawnedCount < 0){
			spawnedCount = 0;
		}
	}

	@Override
	public void incrementSpawnedAICount() {
		spawnedCount++;
	}

	@Override
	public void setCurrentAICap(int cap) {
		liveCap = cap;		
	}

	@Override
	public synchronized void setNPCsCurrentlySpawning(boolean flag) {
		enabled = flag;
	}

	@Override
	public void spawnNPCCar(Location spawn, final BlockFace carriagewayDir) {
		spawn = spawn.add(0.5, 0, 0.5);
		final Location spawnLoc = spawn;
		final DrivenCar c = CarGenerator.gen().setNPC(true);
		plugin.getServer().getScheduler().runTask(plugin, new Runnable(){ //TODO THIS is what is creating the lag

			public void run() {
				final Minecart m = (Minecart) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.MINECART);
				if(main.plugin.aiSpawnMethod.equals(SpawnMethod.WORLD_PROBE)){
					List<Entity> es = m.getNearbyEntities(2, 2, 2);
					for(Entity e:es){
						if(e.getType() == EntityType.MINECART){
							m.remove();
							return; //Already a car in close proximity
						}
					}
					List<Entity> nearby = new ArrayList<Entity>(m.getNearbyEntities(AIRouter.PLAYER_RADIUS, 3, AIRouter.PLAYER_RADIUS));
					for(Entity e:new ArrayList<Entity>(nearby)){
						if(e.getType() != EntityType.MINECART){
							nearby.remove(e);
						}
					}
					if(nearby.size() > 2){
						//Too many in area
						m.remove();
						return;
					}
				}
				//It's valid
				final Villager v = (Villager) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.VILLAGER);
				v.setAdult();
				v.setBreed(false);
				v.setAgeLock(true);
				v.setCanPickupItems(false);
				v.setCustomName(randomName());
				v.setCustomNameVisible(true);
				m.setPassenger(v);
			
				//Make it a car
				c.setId(m.getUniqueId());
				m.setMetadata("trade.npc", new StatValue(new VelocityData(carriagewayDir, null), plugin));
				
				CarPreset cp = c.getPreset();
				if(cp != null && cp.hasDisplayBlock()){
					m.setDisplayBlock(cp.getDisplayBlock());
					m.setDisplayBlockOffset(cp.getDisplayBlockOffset());
				}
				
				ucars.listener.updateCarHealthHandler(m, new CarHealthData(c.getHealth(), plugin));
				Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

					@Override
					public void run() {
						if(!m.isDead() && m.isValid() && v.isValid() && !v.isDead()){ //Cart hasn't despawned
							plugin.carSaver.carNowInUse(c);
							incrementSpawnedAICount();
						}
						return;
					}});
				return;
			}});
		return;
	}

	@Override
	public void followRoadAndSpawnCarFromTrackerBlock(Location trackerBlockLoc,
			BlockFace followDir) {
				if(!plugin.aiSpawns.isNPCCarsEnabled()){
					return;
				}
		//Track road and spawn in an AI
				Block current = trackerBlockLoc.getBlock();
				int distance = randomDistanceAmount();
				while(distance > 0){
					//Need to follow the road
					TrackingData data = AITrackFollow.nextBlock(current, followDir, junction, null, false);
					
					current = data.nextBlock;
					followDir = data.dir;
					distance--;
				}
				//Current is the track block
				Block toSpawn = current.getRelative(BlockFace.UP);
				while(AIRouter.isTrackBlock(toSpawn.getType()) && toSpawn.getY() <= 256){ //Height limit
					toSpawn = toSpawn.getRelative(BlockFace.UP);
				}
				//toSpawn is the road surface
				Location spawnLoc = toSpawn.getLocation().add(0, 1.5, 0); //Position to spawn car
				if(!spawnLoc.getBlock().isEmpty()){
					spawnLoc = spawnLoc.add(0, 0.6, 0);
					if(!spawnLoc.getBlock().isEmpty()){
						//Must spawn car in an air block
						return;
					}
				}
				BlockFace carDirection = followDir;
				carDirection = AITrackFollow.carriagewayDirection(current);
				if(carDirection == null){
					//Not a valid road structure
					return;
				}
				spawnNPCCar(spawnLoc, carDirection);
	}
	
	public int randomDistanceAmount(){
		return main.random.nextInt(25-10)+10; //Between 10 and 30
	}
	
	public int randomDirAmount(){
		return main.random.nextInt(5)+1; //1 to 5
	}
	
	public int randomDir2Amount(){
		return main.random.nextInt(10-5)+5; //5 to 10
	}
	
	public int randomDir3Amount(){
		return main.random.nextInt(30-20)+20; //30 to 20
	}
	
	public BlockFace randomFace(){
		//N, NE, E, SE, S, SW, W, NW (8)
		int rand = main.random.nextInt(8);
		switch(rand){
		case 0:return BlockFace.NORTH;
		case 1:return BlockFace.NORTH_EAST;
		case 2:return BlockFace.EAST;
		case 3:return BlockFace.SOUTH_EAST;
		case 4:return BlockFace.SOUTH;
		case 5:return BlockFace.SOUTH_WEST;
		case 6:return BlockFace.WEST;
		case 7:return BlockFace.NORTH_WEST;
		default: return BlockFace.NORTH;
		}
	}
	public String randomName(){
		if(aiNames == null){
			return "Citizen";
		}
		return aiNames.get(main.random.nextInt(aiNames.size())); //Select a random name
	}

}
