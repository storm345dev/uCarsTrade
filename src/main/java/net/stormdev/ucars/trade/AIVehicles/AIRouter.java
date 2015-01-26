package net.stormdev.ucars.trade.AIVehicles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.uCarsAPI.uCarsAPI;
import com.useful.ucars.ClosestFace;
import com.useful.ucarsCommon.StatValue;

public class AIRouter {
	
	private static boolean enabled;
	private static Map<String, Material> trackBlocks = new HashMap<String, Material>();
	private String trackPattern = "a,b,c";
	public static String[] pattern = new String[]{};
	private Material roadEdge;
	private Material junction;
	private uCarsAPI api;
	
	public static boolean isTrackBlock(Material mat){
		for(Material mate:trackBlocks.values()){
			if(mat.name().contains(mate.name())){
				return true;
			}
		}
		return false;
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
		this.enabled = enabled;
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
	}
	
	public static boolean isAIEnabled(){
		return enabled && AISpawnManager.enabled;
	}
	
	public void route(final Minecart car, final DrivenCar c) throws Exception{
		if(!enabled){
			return;
		}
		double speed = 2;
		BlockFace direction = BlockFace.NORTH;
		Vector vel = car.getVelocity();
		
		Location loc = car.getLocation();
		Block under = loc.getBlock().getRelative(BlockFace.DOWN, 2);
		
		double cx = loc.getX();
		double cy = loc.getY();
		double cz = loc.getZ();
		
		if(!c.isNPC()){
			//Not an npc
			return;
		}
		List<Entity> nearby = car.getNearbyEntities(20, 10, 20); //20x20 radius
		if(main.random.nextInt(5) < 1){ // 1 in 5 chance
			//Check if players nearby
			boolean nearbyPlayers = false;
			for(Entity e:nearby){
				if(e instanceof Player){
					nearbyPlayers = true;
				}
			}
			if(!nearbyPlayers){
				//Remove me
				if(car.getPassenger() != null){
					car.getPassenger().remove();
				}
				main.plugin.carSaver.carNoLongerInUse(c.getId());
				car.remove();
				AISpawnManager.decrementSpawnedCount();
			}
		}
		
		/*nearby = car.getNearbyEntities(1.5, 1.5, 1.5); //Nearby cars
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
		
		if(/*stop || */car.hasMetadata("car.frozen") || api.atTrafficLight(car)){
			car.setVelocity(new Vector(0,0,0)); //Stop (or trafficlights)
			return;
		}
		
		VelocityData data = new VelocityData(null, null);
		if(car.hasMetadata("trade.npc")){
			List<MetadataValue> ms = car.getMetadata("trade.npc");
			data = (VelocityData) ms.get(0).value();
			if(data.getDir() != null){
				direction = data.getDir();
			}
			//direction = (BlockFace) ms.get(0).value();
		}
		
		Material ut = under.getType();
		
		if(!isTrackBlock(ut) && ut != junction){
			Block u= under.getRelative(BlockFace.DOWN);
			ut = u.getType();
			if(!isTrackBlock(ut) && ut != junction){
				u = under.getRelative(BlockFace.UP);
				ut = u.getType();
				if(!isTrackBlock(ut) && ut != junction){
					relocateRoad(car, under, loc, ut==junction, data);
					return;
				}
			}
			under = u;
			ut = under.getType();
		}
		
		boolean atJ = false;
		
		if(ut == junction){
			atJ = true;
		}
		
		boolean keepVel = !atJ && !car.hasMetadata("npc.turning") && !car.hasMetadata("relocatingRoad");

		if(!car.hasMetadata("trade.npc")){
			//Calculate direction from road
			if(!atJ){
				BlockFace face = AISpawnManager.carriagewayDirection(under);
				if(!direction.equals(face)){
					direction = face;
					keepVel = false;
					data.setMotion(null);
				}
			}
			else{
				relocateRoad(car, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, atJ, data);
				return;
			}
		}
		else {
			if(keepVel && !data.hasMotion()){
				keepVel = false;
			}
		}
		
		if(direction == null){ //Not on a road
			//Try to recover
			relocateRoad(car, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, atJ, data);
			return;
		}
		
		//Recalculate dir pretty goddam often
		if(AIRouter.isTrackBlock(under.getType())){
			BlockFace bf = AISpawnManager.carriagewayDirection(under);
			if(bf != null){
				direction = bf;
				keepVel = false;
				data.setDir(direction);
			}
		}
		
		//Now we need to route it...
		TrackingData nextTrack = AITrackFollow.nextBlock(under, direction, junction, car);
		if(nextTrack.junction){
			keepVel = false;
		}
		if(direction != nextTrack.dir){
			direction = nextTrack.dir;
			keepVel = false;
			//Update direction stored on car...
			car.removeMetadata("trade.npc", main.plugin);
			data.setMotion(null);
			car.setMetadata("trade.npc", new StatValue(new VelocityData(direction, null), main.plugin));
		}
		Block next = nextTrack.nextBlock;
		Block road = next.getRelative(BlockFace.UP);
		while(isTrackBlock(road.getType())){
			road = road.getRelative(BlockFace.UP);
		}
		Block toDrive = road.getRelative(BlockFace.UP);
		if(!toDrive.isEmpty()){
			//Car has hit a wall
			return;
		}
		if(toDrive.getLocation().distanceSquared(loc) >= 3.25 ||
				toDrive.getY() > loc.getBlockY()){
			keepVel = false;
		}
		if(keepVel){
			vel = data.getMotion();
			car.removeMetadata("relocatingRoad", main.plugin);
			car.setVelocity(vel);
		}
		else{
			//Calculate vector to get there...
			double tx = toDrive.getX()+0.5;
			double ty = toDrive.getY()+0.1;
			double tz = toDrive.getZ()+0.5;
			
			double x = tx - cx /*+ 0.5*/;
			double y = ty - cy;
			double z = tz - cz /*+ 0.5*/;
			
			if((y-0.1) > 0.1){ //Going up
				y+=0.2; //Help climb smoother
			}
			
			double px = Math.abs(x);
			double pz = Math.abs(z);
			boolean ux = px > pz ? false:true;

			double mult = speed * 0.15;
			
			if(atJ){
				x *= 2;
				z *= 2;
			}
			else if(y<2 && !atJ){
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
			if(y>0.2){ //Going upwards
				y += 3;
			}
			
			vel = new Vector(x,y,z); //Go to block
			car.removeMetadata("relocatingRoad", main.plugin);
			data.setMotion(vel);
			car.setVelocity(vel);
		}
		data.setDir(direction);
		car.removeMetadata("trade.npc", main.plugin);
		car.setMetadata("trade.npc", new StatValue(data, main.plugin));
		return;
	}
	
	public void relocateRoad(Minecart car, Block under, Location currentLoc, boolean atJunction, VelocityData vd){
		Location prev = null;
		if(car.hasMetadata("relocatingRoad")){
			Object o = car.getMetadata("relocatingRoad").get(0).value();
			if(o != null && o instanceof Location){
				prev = (Location) o;
				car.removeMetadata("relocatingRoad", main.plugin);
			}
			else {
				car.removeMetadata("relocatingRoad", main.plugin);
				return;
			}
		}
		
		Vector vel = car.getVelocity();
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
						BlockFace bd = AISpawnManager.carriagewayDirection(b);
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
						BlockFace bd = AISpawnManager.carriagewayDirection(b);
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
			car.setVelocity(vel.multiply(-1)); //Reverse
			return;
		}
		
		car.setMetadata("relocatingRoad", new StatValue(toGo.getLocation(), main.plugin));
		
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
				direction = AISpawnManager.carriagewayDirection(under);
			}
			if(direction == null){
				direction = dir;
			}
		}
		else{
			if(!car.hasMetadata("car.needRouteCheck")){
				car.setMetadata("car.needRouteCheck", new StatValue(null, main.plugin));
			}
		}		
		
		//Update direction stored on car...
		car.removeMetadata("trade.npc", main.plugin);
		car.setMetadata("trade.npc", new StatValue(new VelocityData(direction, null), main.plugin));
		return;
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
