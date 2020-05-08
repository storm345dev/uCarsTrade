package net.stormdev.ucars.trade.AIVehicles;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.CartOrientationUtil;
import com.useful.ucars.ClosestFace;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;
import net.stormdev.ucars.trade.AIVehicles.routing.BlockRouteData;
import net.stormdev.ucars.trade.AIVehicles.routing.RouteMethod;
import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.utils.NPCOrientationUtil;
import net.stormdev.ucars.utils.NoMobAI;
import net.stormdev.ucarstrade.cars.DrivenCar;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Vehicle;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class AIRouter {
	public static int PLAYER_RADIUS = 70;
	public static double PLAYER_RADIUS_SQ = Math.pow(PLAYER_RADIUS, 2);
	
	private static boolean enabled;
	private static Map<String, Material> trackBlocks = new HashMap<String, Material>();
	private String trackPattern = "a,b,c";
	public static String[] pattern = new String[]{};
	private Material roadEdge;
	public static Material junction;
	private uCarsAPI api;
	
	/*private volatile boolean runRoutingTask = true;*/
	
	/*private void startRoutingTask(){
		new Thread(){
			@Override
			public void run(){				
				long lastTickTime = System.currentTimeMillis();
				long nextTickTime;
				while(enabled && runRoutingTask){
					lastTickTime = System.currentTimeMillis();
					nextTickTime = lastTickTime+50;
					//Update time info so we can be roughly every tick
					
					try {
						for(World world:Bukkit.getServer().getWorlds()){
							List<Entity> entities;
							try {
								entities = new ArrayList<Entity>(world.getEntities());
							} catch (ConcurrentModificationException e) {
								//Oh well; better performance to do this async and just let it fail sometimes
								continue;
							}
							
							for(Entity e:entities){
								if(!(e instanceof Minecart)){
									continue;
								}
								final Minecart m = (Minecart) e;
								final DrivenCar c = main.plugin.carSaver.getCarInUse(m);
								if(c == null
										|| !c.isNPC()){
									continue; //Not a car or not an npc car
								}
								Entity driver = m.getPassenger();
								if(driver == null || !(driver instanceof Villager)){
									Bukkit.getScheduler().runTaskLater(main.plugin, new Runnable(){

										@Override
										public void run() {
											if(c.isNPC() && m.isValid() && !m.isDead()){
												//No longer an NPC car
												m.removeMetadata("trade.npc", main.plugin);
												c.setNPC(false);
												main.plugin.carSaver.carNowInUse(m, c);
											}
											return;
										}}, 2l);				
									continue;
								}
								//Use AIRouter to route it
								Bukkit.getScheduler().runTask(main.plugin, new Runnable(){

									@Override
									public void run() {
										try {
											route(m, c);
										} catch (Exception e) {
											e.printStackTrace();
										}
										return;
									}});
							}
						}
					} catch(Exception e){
						e.printStackTrace();
					}
					
					//Wait about a tick in time
					long sleepTime = nextTickTime-System.currentTimeMillis();
					if(sleepTime > 0){
						try {
							Thread.sleep(sleepTime);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Thread.yield();
					}
				}
				return;
			}
		}.start();
	}*/
	
	public static boolean isEncodedRouting(){
		return main.plugin.aiRouteMethod.equals(RouteMethod.ENCODED);
	}
	
	public static boolean isTrackBlock(Material mat){
		if(isEncodedRouting()){
			return mat.equals(Material.STAINED_GLASS);
		}
		for(Material mate:trackBlocks.values()){
			if(mat.name().contains(mate.name())){
				return true;
			}
		}
		return mat.equals(junction);
	}
	
	public static String getTrackBlockType(Material mat){
		for(String name:trackBlocks.keySet()){
			if(mat.name().contains(trackBlocks.get(name).name())){
				return name;
			}
		}
		return null;
	}
	
	public static int getTrackBlockIndexByType(String type){
		for(int i=0;i<pattern.length;i++){
			if(pattern[i].equals(type)){
				return i;
			}
		}
		return -1;
	}
	
	public AIRouter(boolean enabled){
		AIRouter.enabled = enabled;
		String edgeRaw = main.config.getString("general.ai.roadEdgeBlock");
		String junRaw = main.config.getString("general.ai.junctionBlock");
		roadEdge = Material.getMaterial(edgeRaw);
		junction = Material.getMaterial(junRaw);
		
		ConfigurationSection sect = main.config.getConfigurationSection("general.ai.trackerBlock");
		for(String patternName:sect.getKeys(false)){
			if(patternName != null && patternName.length() > 0 && !patternName.equals("pattern")){
				trackBlocks.put(patternName, Material.getMaterial(sect.getString(patternName)));
				main.plugin.getLogger().info("Found AI tracker block: "+sect.getString(patternName));
			}
		}
		trackPattern = main.config.getString("general.ai.trackerBlock.pattern");
		pattern = trackPattern.split(Pattern.quote(","));
		
		if(roadEdge == null || junction == null){
			main.logger.info("Didn't enable AIs routing as configuration is invalid!");
			enabled = false;
		}
		api = uCarsAPI.getAPI();
		/*if(enabled){
			this.startRoutingTask();
		}*/
	}
	
	/*public void stopRoutingTask(){
		this.runRoutingTask = false;
	}*/
	
	public static boolean isAIEnabled(){
		return enabled && main.plugin.aiSpawns.isNPCCarsSpawningNow();
	}
	
	public static class PositionTracking {
		private Location current;
		private int stationaryCount = 0;
		
		public PositionTracking(Location current){
			this.current = current;
		}
		
		public void updateLocation(Location loc){
			if(loc.distanceSquared(current) < 2){
				stationaryCount++;
			}
			else {
				stationaryCount = 0;
				current = loc;
			}
		}
		
		public int getStationaryCount(){
			return this.stationaryCount;
		}
	}
	
	public VelocityData getVelocityData(Vehicle car){
		VelocityData data = new VelocityData(null, null, car.getLocation());
		if(UEntityMeta.hasMetadata(car, "trade.npc")){
			List<MetadataValue> ms = UEntityMeta.getMetadata(car, "trade.npc");
			try {
				data = (VelocityData) ms.get(0).value();
			} catch (Exception e) {
				UEntityMeta.removeMetadata(car, "trade.npc");
			}
		}
		return data;
	}
	
	public void route(final Entity car, final DrivenCar c) throws Exception{
		if(!enabled){
			return;
		}
		double speed = 2;
		BlockFace direction = BlockFace.NORTH;
		Vector vel = car.getVelocity();
		
		final Location loc = car.getLocation();
		Block under = loc.clone().add(0, -2, 0).getBlock();
		
		double cx = loc.getX();
		double cy = loc.getY();
		double cz = loc.getZ();
		
		if(!c.isNPC()){
			//Not an npc
			return;
		}		
		
		
		/*List<Entity> nearby = car.getNearbyEntities(PLAYER_RADIUS, 50, PLAYER_RADIUS); //20x20 radius
		List<Player> nearbyPlayersList = new ArrayList<Player>();
		if(main.random.nextInt(5) < 1){ // 1 in 5 chance
			//Check if players nearby
			boolean nearbyPlayers = false;
			for(Entity e:nearby){
				if(e instanceof Player){
					nearbyPlayers = true;
					nearbyPlayersList.add((Player) e);
				}
			}
			if(!nearbyPlayers){
				despawnNPCCar(car, c);
			}
		}*/
		
		/*List<Entity> nearby = car.getNearbyEntities(1.5, 1.5, 1.5); //Nearby cars
		Boolean stop = false;
		for(Entity e:nearby){
			if(e.getType() == EntityType.MINECART && e.hasMetadata("trade.npc")){ //Avoid driving into another car
				Location l = e.getLocation();
				//Compare 'l' and 'loc' to see who is in front
				//West = -x, East = +x, South = +z, North = -z
				double lx = l.getX();
				double lz = l.getZ();
				
				if(direction == BlockFace.EAST){
					//Heading east
					if(cx < lx){
						stop = true;
					}
				}
				else if(direction == BlockFace.WEST){
					//Heading west
					if(cx > lx){
						stop = true;
					}
				}
				else if(direction == BlockFace.NORTH){
					//Heading north
					if(cz > lz){
						stop = true;
					}
				}
				else if(direction == BlockFace.SOUTH){
					//Heading south
					if(cz < lz){
						stop = true;
					}
				}
			}
		}*/
		
		VelocityData data = new VelocityData(null, null, car.getLocation());
		try {
			data = (VelocityData) UEntityMeta.getMetadata(car, "trade.npc").get(0).value();
			if(data.getDir() != null){
				direction = data.getDir();
			}
		} catch (Exception e1) {
			UEntityMeta.removeMetadata(car, "trade.npc");
			UEntityMeta.setMetadata(car, "trade.npc", new StatValue(data, main.plugin));
		}
		
		data.updateLocation(car.getLocation());
		boolean supposedToBeStopped = data.isStoppedForOtherCar() || car.hasMetadata("car.frozen") || UEntityMeta.hasMetadata(car, "car.frozen") || api.atTrafficLight(car);
		long stationaryRemoveTime = supposedToBeStopped ? 2000:200;
		
		if(data.getStationaryCount() > stationaryRemoveTime){ //Being stationary a while
			despawnNPCCar(car, c);
			return;
		}
		
		final VelocityData vd = data;
		final List<Entity> es = car.getWorld().getEntities();
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				
				if(main.random.nextInt(5) < 1){ // 1 in 5 chance
					List<Player> pls = new ArrayList<Player>(Bukkit.getOnlinePlayers());
					double radiusSq = Math.pow(PLAYER_RADIUS, 2);
					if(car.getTicksLived() > 2400){
						radiusSq = 35*35; //30 blocks
					}
					for(Player pl:pls){
						double d = pl.getLocation().clone().toVector().subtract(loc.clone().toVector()).lengthSquared();
						if(d < radiusSq){
							return; //Player in radius
						}
					}
					
					//No players in radius
					despawnNPCCar(car, c);
					return;
				}
				
				Vector carLoc = car.getLocation().toVector();
				boolean nearbyCars = false;
				for(Entity e:es){
					if(e.equals(car)){
						continue;
					}
					if(/*!e.getType().equals(EntityType.MINECART) || */!UEntityMeta.hasMetadata(e, "trade.npc")){
						continue;
					}
					Vector diff = e.getLocation().toVector().clone().subtract(carLoc.clone());
					if(Math.abs(diff.lengthSquared()) < 12){
						VelocityData otherCar = getVelocityData((Vehicle) e);
						if(otherCar.getDir() != null && vd.getDir() != null && otherCar.getDir().equals(vd.getDir())){
							BlockFace dir = otherCar.getDir();
							double cx = dir.getModX();
							double cz = dir.getModZ();
							
							if(cx == 0 && Math.abs(diff.getX()) >= 1){
								continue;
							}
							if(cz == 0 && Math.abs(diff.getZ()) >= 1){
								continue;
							}
							if((cx > 0 && diff.getX() < 0) || (cx < 0 && diff.getX() > 0)){
								continue;
							}
							if((cz > 0 && diff.getZ() < 0) || (cz < 0 && diff.getZ() > 0)){
								continue;
							}
							if((cx > 0 && cz > 0 && (diff.getX() < 0 || diff.getZ() < 0))
									|| (cx < 0 && cz < 0 && (diff.getX() > 0 || diff.getZ() > 0))){
								continue;
							}
							
							nearbyCars = true;
							break;
						}
					}
				}
				
				vd.setStoppedForOtherCar(nearbyCars);
				return;
			}});
				
		if(/*stop ||*/supposedToBeStopped){
			vd.notStationary();
			UEntityMeta.setMetadata(car, "currentlyStopped", new StatValue(true, main.plugin));
			car.setVelocity(new Vector(0,0,0)); //Stop (or trafficlights)
			return;
		}
		UEntityMeta.removeMetadata(car, "currentlyStopped");

		BlockRouteData brm = AITrackFollow.carriagewayDirection(under);
		
		if(brm.getType() == null){ //Lost road
			under = under.getRelative(BlockFace.DOWN);
			brm = AITrackFollow.carriagewayDirection(under);
			if(brm.getType() == null){
				under = under.getRelative(BlockFace.UP, 2);
				brm = AITrackFollow.carriagewayDirection(under);
				if(brm.getType() == null){
					/*relocateRoad(car, under.getRelative(BlockFace.DOWN), loc, false, data);*/
					/*despawnNPCCar(car, c);
*/
					findRoad(car, c, speed, under.getRelative(BlockFace.DOWN), loc, false, data);
					return;
				}
			}
		}
		
		boolean keepVel = !brm.isJunction() && !vd.isInProgressOfTurningAtJunction() && !UEntityMeta.hasMetadata(car, "relocatingRoad");
		
		if(!UEntityMeta.hasMetadata(car, "trade.npc")){
			//Calculate direction from road
			if(!brm.isJunction()){
				BlockFace face = AITrackFollow.carriagewayDirection(under).getDirection();
				if(!direction.equals(face)){
					vd.resetUpdatesSinceTurn();
					direction = face;
					keepVel = false;
					data.setMotion(null);
				}
			}
			else{
				/*despawnNPCCar(car, c);
*/				/*relocateRoad(car, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, brm.isJunction(), data);*/
				findRoad(car, c, speed, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, brm.isJunction(), data);
				return;
			}
		}
		else {
			if(keepVel && !data.hasMotion()){
				keepVel = false;
			}
		}
		
		if((brm.getDirection() == null && direction == null) || brm.getType() == null){ //Not on a road
			//Try to recover
			/*relocateRoad(car, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, brm.isJunction(), data);*/
			findRoad(car, c, speed, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, brm.isJunction(), data);
			/*despawnNPCCar(car, c);
*/			return;
		}
		
		if(brm.getDirection() != null){
			if(direction == null || !direction.equals(brm.getDirection())){
				vd.resetUpdatesSinceTurn();
				direction = brm.getDirection();
				keepVel = false;
				data.setDir(direction);
			}
		}
		
		//Now we need to route it...
		TrackingData nextTrack = AITrackFollow.nextBlock(under, vd, brm, car);
		
		if(direction != nextTrack.dir && !vd.isInProgressOfTurningAtJunction() && !brm.isJunction()){
			direction = nextTrack.dir;
			keepVel = false;
			vd.resetUpdatesSinceTurn();
			//Update direction stored on car...
			data.setDir(direction);
			data.setMotion(null);
		}
		if(nextTrack.forJunction){
			keepVel = false; //make it recalculate so we can go slower
		}
		if(vd.isInProgressOfTurningAtJunction()){
			keepVel = false;
			direction = nextTrack.dir;
			vd.resetUpdatesSinceTurn();
		}
		/*if(brm.isJunction()&&!vd.isInProgressOfTurningAtJunction()){
			keepVel = false;
			direction = nextTrack.dir;
			data.setMotion(null);
			car.setMetadata("car.atJunction", new StatValue(nextTrack.dir, main.plugin));
		}
		else if(brm.isJunction() && car.hasMetadata("car.atJunction")){
			try {
				direction = (BlockFace) car.getMetadata("car.atJunction").get(0).value();
			} catch (Exception e) {
				//invalid meta
				car.removeMetadata("car.atJunction", main.plugin);
			}
			nextTrack.nextBlock = under.getRelative(direction);
			keepVel = true;
		}
		else if(!brm.isJunction() && car.hasMetadata("car.atJunction")){
			car.removeMetadata("car.atJunction", main.plugin);
			keepVel = false; //Recalcualte faster speed vector
		}*/
		
		Block next = nextTrack.nextBlock;
		Block road = next/*.getRelative(BlockFace.UP)*/;
		while(isTrackBlock(road.getType())){
			road = road.getRelative(BlockFace.UP);
		}
		vd.setTargetBlockLoc(road.getLocation().clone().add(0, -1, 0));
		Block toDrive = road.getRelative(BlockFace.UP);
		if(!toDrive.isEmpty()){
			//Car has hit a wall
			return;
		}
		String rName = road.getType().name().toLowerCase();
		String tName = toDrive.getType().name().toLowerCase();
		if(toDrive.getLocation().distanceSquared(loc) >= 3.25 ||
				toDrive.getY() > loc.getBlockY()
				|| ((tName.contains("step")||tName.contains("carpet")))
				){ 
			keepVel = false;
		}
		
		if(vd.getUpdatesSinceTurn() < 3){
			keepVel = false;
		}
		
		if(keepVel){
			vel = data.getMotion();
			car.setVelocity(vel);
			vd.incrementUpdatesSinceTurn();
		}
		else{
			//Calculate vector to get there...
			double tx = toDrive.getX()+0.5;
			double ty = toDrive.getY()+0.1;
			if(rName.contains("step") && !rName.contains("double") 
					&& ((int)road.getData())<8 //Makes sure it's a bottom slab
					){
				ty -= 0.5;
			}
			if(rName.contains("carpet")){
				ty -= 0.9;
			}
			double tz = toDrive.getZ()+0.5;
			
			double x = tx - cx /*+ 0.5*/;
			double y = ty - cy;
			double z = tz - cz /*+ 0.5*/;
			
			/*if(y > 0.2){ //Going up
				y+=0.2; //Help climb smoother
			}*/
			
			double px = Math.abs(x);
			double pz = Math.abs(z);
			boolean ux = px > pz ? false:true;

			double mult = speed * 0.15;
			
			if(y<2){
				if (ux) {
					// x is smaller
					// long mult = (long) (pz/speed);
					x = (x / pz) * mult;
					z = (z / pz) * mult;
				} else {
					// z is smaller
					// long mult = (long) (px/speed);
					x = (x / px) * mult;
					z = (z / px) * mult;
				}
			}
			if(nextTrack.forJunction || brm.isJunction()){ //Slow down for junction
				x *= 0.5;
				z *= 0.5;
			}
/*			if(px > 0.1 && pz > 0.1 && AITrackFollow.isCompassDir(direction)) { //They aren't going in a totally straight line, slow it down so they don't wiggle everywhere
				//System.out.println("DECREMENTING VECTOR");
				x *= 0.1;
				z *= 0.1;
			}*/
			/*if(AITrackFollow.isDiagonalDir(direction)){
				x *= 0.3;
				z *= 0.3;
			}*/
			/*if(y>0.2){ //Going upwards
				y += 3;
			}*/
			
			vel = new Vector(x,y,z); //Go to block
			UEntityMeta.removeMetadata(car, "relocatingRoad");
			data.setMotion(vel);
			car.setVelocity(vel);
		}
		Vector dirVec = vel.clone().setY(0).normalize();
		if(dirVec.lengthSquared() > 0.01){
			Location dirLoc = new Location(car.getWorld(), 0, 0, 0); //Make sure car always faces the RIGHT "forwards"
			dirLoc.setDirection(dirVec);
			float yaw = dirLoc.getYaw()+90;
			/*if(event.getDir().equals(CarDirection.BACKWARDS)){
				yaw += 180;
			}*/
			while(yaw < 0){
				yaw = 360 + yaw;
			}
			while(yaw >= 360){
				yaw = yaw - 360;
			}
			CartOrientationUtil.setYaw(car, yaw);
			Entity pass = car.getPassenger();
			if(pass != null && pass instanceof Villager){
				NoMobAI.clearAI(pass);
				NPCOrientationUtil.setYaw((Villager) pass, yaw-90);
				NoMobAI.clearAI(pass);
			/*	Bukkit.broadcastMessage((yaw-180)+"");
				NPCOrientationUtil.setYaw((Villager) pass, yaw-180);*/
			}
			/*Entity pass = car.getPassenger();
			WrapperPlayServerEntityLook p = new WrapperPlayServerEntityLook();
			p.setEntityID(car.getEntityId());
			p.setYaw(yaw);
			p.setPitch(car.getLocation().getPitch());
			WrapperPlayServerEntityLook p2 = null;
			if(pass != null){
				p2 = new WrapperPlayServerEntityLook();
				p.setEntityID(pass.getEntityId());
				p.setYaw(yaw);
				p.setPitch(0);
			}
			for(Player player:nearbyPlayersList){
				p.sendPacket(player);
				if(p2 != null){
					p2.sendPacket(player);
				}
			}*/
		}
		
		data.setDir(direction);		
		return;
	}
	
	public void findRoad(final Entity car, DrivenCar c, double speed, Block under, Location currentLoc, boolean atJunction, VelocityData vd){
		vd.resetUpdatesSinceTurn();
		
		if(vd.getTargetBlockLoc() == null){
			despawnNPCCar(car, c);
			return;
		}
		
		Location road = vd.getTargetBlockLoc().clone();
		Block rb = road.getBlock();
		Material rbt = rb.getType();
		String rName = rbt.name().toLowerCase();
		if(!isTrackBlock(rbt)){
			despawnNPCCar(car, c);
			return;
		}
		
		Location toDriveLoc = road.add(0, 2, 0);
		Block toDrive = toDriveLoc.getBlock();
		if(!toDrive.isEmpty()){
			despawnNPCCar(car, c);
			return;
		}
		
		Location carLoc = car.getLocation();

		double cx = carLoc.getX();
		double cy = carLoc.getY();
		double cz = carLoc.getZ();
		
		//Calculate vector to get there...
		double tx = toDrive.getX() + 0.5;
		double ty = toDrive.getY() + 0.1;
		if(rName.contains("step") && !rName.contains("double") 
				&& ((int)rb.getData())<8 //Makes sure it's a bottom slab
				){
			ty -= 0.5;
		}
		if(rName.contains("carpet")){
			ty -= 0.9;
		}
		double tz = toDrive.getZ() + 0.5;
		
		double x = tx - cx /*+ 0.5*/;
		double y = ty - cy;
		double z = tz - cz /*+ 0.5*/;
		
		/*if(y > 0.2){ //Going up
			y+=0.2; //Help climb smoother
		}*/
		
		double px = Math.abs(x);
		double pz = Math.abs(z);
		boolean ux = px > pz ? false:true;

		double mult = speed * 0.15;
		
		if(y<2){
			if (ux) {
				// x is smaller
				// long mult = (long) (pz/speed);
				x = (x / pz) * mult;
				z = (z / pz) * mult;
			} else {
				// z is smaller
				// long mult = (long) (px/speed);
				x = (x / px) * mult;
				z = (z / px) * mult;
			}
		}
/*			if(px > 0.1 && pz > 0.1 && AITrackFollow.isCompassDir(direction)) { //They aren't going in a totally straight line, slow it down so they don't wiggle everywhere
			//System.out.println("DECREMENTING VECTOR");
			x *= 0.1;
			z *= 0.1;
		}*/
		/*if(AITrackFollow.isDiagonalDir(direction)){
			x *= 0.3;
			z *= 0.3;
		}*/
		/*if(y>0.2){ //Going upwards
			y += 3;
		}*/
		
		final Vector vel = new Vector(x,y,z); //Go to block
		vd.setMotion(vel);
		
		car.setVelocity(vel);
	}
	
	public void relocateRoad(Entity car, Block under, Location currentLoc, boolean atJunction, VelocityData vd){
		vd.resetUpdatesSinceTurn();
		/*BlockRouteData found = null;
		Block foundBlock = null;
		Bukkit.broadcastMessage(under.getY()+"");
		for(BlockFace dir:AITrackFollow.allDirs()){
			Block b = under.getRelative(dir);
			BlockRouteData brd = AITrackFollow.carriagewayDirection(b);
			if(brd.getType() != null){
				if(found == null){
					found = brd;
					foundBlock = b;
				}
				else {
					if(brd.getDirection() != null && brd.getDirection().equals(vd.getDir())){
						found = brd;
						foundBlock = b;
					}
				}
			}
		}
		
		if(found == null){
			Bukkit.broadcastMessage("FAILED TO FIND ROAD");
			//We lost the road
			return;
		}
		
		Bukkit.broadcastMessage("FOUND ROAD!");
		
		Vector toGetThere = car.getLocation().toVector().clone().subtract(foundBlock.getLocation().add(0.5, 0.1, 0.5).toVector());
		car.setVelocity(toGetThere);
		vd.setDir(null);
		vd.setMotion(null);*/
		Location prev = null;
		if(UEntityMeta.hasMetadata(car, "relocatingRoad")){
			Object o = UEntityMeta.getMetadata(car, "relocatingRoad").get(0).value();
			if(o != null && o instanceof Location){
				prev = (Location) o;
				UEntityMeta.removeMetadata(car, "relocatingRoad");
			}
			else {
				UEntityMeta.removeMetadata(car, "relocatingRoad");
				return;
			}
		}
		
		Vector vel = vd.getMotion();
		double cx = currentLoc.getX();
		double cy = currentLoc.getY();
		double cz = currentLoc.getZ();
		Block toGo = null;
		BlockFace dir = null;
		BlockFace goDir = null;
		
		if(prev == null){
			BlockFace currentDir = vd.getDir();
			for(BlockFace d:AITrackFollow.compassDirs()){
				Block b = under.getRelative(d);
				if(isTrackBlock(b.getType()) || b.getType() == junction){
					if(toGo != null){
						BlockFace bd = AITrackFollow.carriagewayDirection(b).getDirection();
						if(bd != null && bd.equals(currentDir) && !(goDir != null && goDir.equals(currentDir))){
							toGo = b;
							dir = d;
							goDir = d;
							continue;
						}
						if(currentDir != null && d.equals(currentDir) && !(dir != null && dir.equals(currentDir))){ //Favour finding under-the-road blocks in the same direction as we were going
							toGo = b;
							dir = d;
							continue;
						}
						continue;
					}
					toGo = b;
					dir = d;
				}
			}
			for(BlockFace d:AITrackFollow.diagonalDirs()){
				Block b = under.getRelative(d);
				if(isTrackBlock(b.getType()) || b.getType() == junction){
					if(toGo != null){
						BlockFace bd = AITrackFollow.carriagewayDirection(b).getDirection();
						if(bd != null && bd.equals(currentDir) && !(goDir != null && goDir.equals(currentDir))){
							toGo = b;
							dir = d;
							goDir = d;
							continue;
						}
						if(currentDir != null && d.equals(currentDir) && !(dir != null && dir.equals(currentDir))){ //Favour finding under-the-road blocks in the same direction as we were going
							toGo = b;
							dir = d;
							continue;
						}
						continue;
					}
					toGo = b;
					dir = d;
				}
			}
		}
		else {
			toGo = prev.getBlock();
		}
		
		if(toGo == null){
			vd.setMotion(vel.multiply(-1));
			car.setVelocity(vel.multiply(-1));
			return;
		}
		
		UEntityMeta.setMetadata(car, "relocatingRoad", new StatValue(toGo.getLocation(), main.plugin));
		
		if(dir != null && toGo != null){
			vd.setDir(dir);
		}
		
		toGo = toGo.getRelative(BlockFace.UP,2);
		if(!toGo.isEmpty()){
			//Invalid
			toGo = toGo.getRelative(BlockFace.UP);
			if(!toGo.isEmpty()){
				//Invalid still
				return;
			}
		}
		
		//Calculate vector to get there...
		double tx = toGo.getX()+0.5;
		double ty = toGo.getY();
		double tz = toGo.getZ()+0.5;
		
		double x = tx - (cx);
		double y = ty - cy;
		double z = tz - (cz);

		vel = new Vector(x,y,z); //Go to block
		
		car.setVelocity(vel);
		BlockFace direction = ClosestFace.getClosestFace(car.getLocation().getYaw());
		if(!atJunction){
			if(goDir == null){
				direction = AITrackFollow.carriagewayDirection(under).getDirection();
			}
			if(direction == null){
				direction = dir;
			}
		}	
		
		vd.setDir(direction);
		vd.setMotion(null);
		return;
	}
	
	public static void clearNPCMeta(Entity car){
		car.removeMetadata("trade.npc", main.plugin);
		car.removeMetadata("relocatingRoad", main.plugin);
		car.removeMetadata("npc.turning", main.plugin);
		UEntityMeta.removeMetadata(car, "trade.npc");
		UEntityMeta.removeMetadata(car, "relocatingRoad");
		UEntityMeta.removeMetadata(car, "npc.turning");
	}
	
	public static void despawnNPCCar(final Entity car, final DrivenCar c){
		//Remove me
		Runnable run = new Runnable(){

			@Override
			public void run() {
				Entity pass = car.getPassenger();
				if(pass != null){
					pass.remove();
				}
				clearNPCMeta(car);
				car.remove();
				Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

					@Override
					public void run() {
						main.plugin.aiSpawns.decrementSpawnedAICount();
						main.plugin.carSaver.carNoLongerInUse(c.getId());
						return;
					}});				
				return;
			}};
		if(Bukkit.isPrimaryThread()){
			run.run();
		}
		else {
			Bukkit.getScheduler().runTask(main.plugin, run);
		}
	}
	
	public static void despawnNPCCarNow(Entity car, final DrivenCar c){
		//Remove me
		Entity pass = car.getPassenger();
		clearNPCMeta(car);
		car.remove();
		if(pass != null){
			pass.remove();
		}
		main.plugin.carSaver.carNoLongerInUseNow(c.getId());
		main.plugin.aiSpawns.decrementSpawnedAICount();
	}
	
	public boolean isCompassDir(BlockFace face){
		switch(face){
		case NORTH: return true;
		case EAST: return true;
		case SOUTH: return true;
		case WEST: return true;
		default: return false;
		}
	}
}
