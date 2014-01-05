package net.stormdev.ucars.trade.AIVehicles;

import java.util.List;

import net.stormdev.ucars.stats.SpeedStat;
import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.utils.Car;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.ucars.ClosestFace;
import com.useful.ucarsCommon.StatValue;

public class AIRouter {
	
	private boolean enabled;
	private Material trackBlock;
	private Material roadEdge;
	private Material junction;
	
	public AIRouter(boolean enabled){
		this.enabled = enabled;
		String trackRaw = main.config.getString("general.ai.trackerBlock");
		String edgeRaw = main.config.getString("general.ai.roadEdgeBlock");
		String junRaw = main.config.getString("general.ai.junctionBlock");
		trackBlock = Material.getMaterial(trackRaw);
		roadEdge = Material.getMaterial(edgeRaw);
		junction = Material.getMaterial(junRaw);
		if(trackBlock == null || roadEdge == null || junction == null){
			main.logger.info("Didn't enable AIs routing as configuration is invalid!");
			enabled = false;
		}
	}
	
	public void route(Minecart car, Car c){
		double speed = 1.6;
		BlockFace direction = BlockFace.NORTH;
		Location loc = car.getLocation();
		Block under = loc.getBlock().getRelative(BlockFace.DOWN, 2);
		Vector vel = car.getVelocity();
		double cx = loc.getX();
		double cy = loc.getY();
		double cz = loc.getZ();
		
		if(!c.stats.containsKey("trade.npc")){
			//Not an npc
			return;
		}
		List<Entity> nearby = car.getNearbyEntities(30, 10, 30); //20x20 radius
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
				car.getPassenger().remove();
				main.plugin.carSaver.removeCar(car.getUniqueId());
				car.remove();
				return;
			}
		}
		
		nearby = car.getNearbyEntities(1.5, 1.5, 1.5); //Nearby cars
		Boolean stop = false;
		for(Entity e:nearby){
			if(e.getType() == EntityType.MINECART){ //Avoid driving into another car
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
		}
		
		if(stop || car.hasMetadata("car.frozen")){
			car.setVelocity(new Vector(0,0,0)); //Stop (or trafficlights)
			return;
		}
		
		Material ut = under.getType();
		
		if(ut != trackBlock && ut != junction){
			Block u= under.getRelative(BlockFace.DOWN);
			ut = u.getType();
			if(ut != trackBlock && ut != junction){
				u = under.getRelative(BlockFace.UP);
				ut = u.getType();
				if(ut != trackBlock && ut != junction){
					relocateRoad(car, under, loc, ut==junction);
					return;
				}
			}
			under = u;
			ut = under.getType();
		}
		
		Boolean atJ = false;
		if(ut == junction){
			atJ = true;
			if(!car.hasMetadata("car.needRouteCheck")){
				car.setMetadata("car.needRouteCheck", new StatValue(null, main.plugin));
			}
		}
		else{
			if(car.hasMetadata("car.needRouteCheck")){
				car.removeMetadata("car.needRouteCheck", main.plugin);
				direction = main.plugin.aiSpawns.carriagewayDirection(under);
				//Update direction stored on car...
				car.removeMetadata("trade.npc", main.plugin);
				car.setMetadata("trade.npc", new StatValue(direction, main.plugin));
			}
		}
		
		if(c.stats.containsKey("trade.speed")){
			speed = ((SpeedStat) c.stats.get("trade.speed")).getSpeedMultiplier()*1.6;
		}
		
		if(car.hasMetadata("trade.npc")){
			List<MetadataValue> ms = car.getMetadata("trade.npc");
			direction = (BlockFace) ms.get(0).value();
		}
		else{
			//Calculate direction from road
			if(!atJ){
				direction = main.plugin.aiSpawns.carriagewayDirection(under);
			}
		}
		
		if(direction == null){ //Not on a road
			//Try to recover
			relocateRoad(car, car.getLocation().getBlock().getRelative(BlockFace.DOWN, 2), loc, atJ);
			return;
		}
		
		//Now we need to route it...
		TrackingData nextTrack = AITrackFollow.nextBlock(under, direction, trackBlock, junction);
		if(direction != nextTrack.dir){
			direction = nextTrack.dir;
			//Update direction stored on car...
			car.removeMetadata("trade.npc", main.plugin);
			car.setMetadata("trade.npc", new StatValue(direction, main.plugin));
		}
		Block next = nextTrack.nextBlock;
		Block road = next.getRelative(BlockFace.UP);
		while(road.getType() == trackBlock){
			road = road.getRelative(BlockFace.UP);
		}
		Block toDrive = road.getRelative(BlockFace.UP);
		if(!toDrive.isEmpty()){
			//Car has hit a wall
			return;
		}
		//Calculate vector to get there...
		double tx = toDrive.getX();
		double ty = toDrive.getY();
		double tz = toDrive.getZ();
		
		double x = tx - cx;
		double y = ty - cy;
		double z = tz - cz;
		
		Boolean ux = true;
		double px = Math.abs(x);
		double pz = Math.abs(z);
		if (px > pz) {
			ux = false;
		}

		if(y<0.15){
			if (ux) {
				// x is smaller
				// long mult = (long) (pz/speed);
				x = (x / pz) * speed;
				z = (z / pz) * speed;
			} else {
				// z is smaller
				// long mult = (long) (px/speed);
				x = (x / px) * speed;
				z = (z / px) * speed;
			}
		}
		if(y>0){
			y = 3;
			x*= 10;
			x*= 10;
		}
		vel = new Vector(x,y,z); //Go to block
		
		car.setVelocity(vel);
		return;
	}
	
	public void relocateRoad(Minecart car, Block under, Location currentLoc, boolean atJunction){
		
		Vector vel = car.getVelocity();
		double cx = currentLoc.getX();
		double cy = currentLoc.getY();
		double cz = currentLoc.getZ();
		
		//Find track block
		Block N = under.getRelative(BlockFace.NORTH);
		Block E = under.getRelative(BlockFace.EAST);
		Block S = under.getRelative(BlockFace.SOUTH);
		Block W = under.getRelative(BlockFace.WEST);
		Block toGo = null;
		
		if(N.getType() == trackBlock || N.getType() == junction){
			toGo = N;
		}
		else if(E.getType() == trackBlock || E.getType() == junction){
			toGo = E;
		}
		else if(S.getType() == trackBlock || S.getType() == junction){
			toGo = S;
		}
		else if(W.getType() == trackBlock || W.getType() == junction){
			toGo = W;
		}
		
		if(toGo == null){
			car.setVelocity(vel.multiply(-1)); //Reverse
			return;
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
		double tx = toGo.getX();
		double ty = toGo.getY();
		double tz = toGo.getZ();
		
		double x = tx - cx;
		double y = ty - cy;
		double z = tz - cz;

		vel = new Vector(x,y,z); //Go to block
		
		car.setVelocity(vel);
		BlockFace direction = ClosestFace.getClosestFace(car.getLocation().getYaw());
		if(!atJunction){
			direction = main.plugin.aiSpawns.carriagewayDirection(under);
		}
		else{
			if(!car.hasMetadata("car.needRouteCheck")){
				car.setMetadata("car.needRouteCheck", new StatValue(null, main.plugin));
			}
		}
		//Update direction stored on car...
		car.removeMetadata("trade.npc", main.plugin);
		car.setMetadata("trade.npc", new StatValue(direction, main.plugin));
		return;
	}
}
