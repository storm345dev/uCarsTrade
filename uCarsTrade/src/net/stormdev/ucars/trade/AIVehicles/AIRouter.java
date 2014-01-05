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
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.util.Vector;

import com.useful.ucarsCommon.StatValue;

public class AIRouter {
	
	private boolean enabled;
	private Material trackBlock;
	private Material roadEdge;
	
	public AIRouter(boolean enabled){
		this.enabled = enabled;
		String trackRaw = main.config.getString("general.ai.trackerBlock");
		String edgeRaw = main.config.getString("general.ai.roadEdgeBlock");
		trackBlock = Material.getMaterial(trackRaw);
		roadEdge = Material.getMaterial(edgeRaw);
		if(trackBlock == null || roadEdge == null){
			main.logger.info("Didn't enable AIs routing as configuration is invalid!");
			enabled = false;
		}
	}
	
	public void route(Minecart car, Car c){
		double speed = 1;
		BlockFace direction = BlockFace.NORTH;
		Location loc = car.getLocation();
		Block under = loc.getBlock().getRelative(BlockFace.DOWN);
		Vector vel = car.getVelocity();
		double cx = loc.getX();
		double cy = loc.getY();
		double cz = loc.getZ();
		
		if(!c.stats.containsKey("trade.npc")){
			//Not an npc
			return;
		}
		
		if(under.getType() != trackBlock){
			car.setVelocity(vel.multiply(-1)); //Reverse
			return;
		}
		
		List<Entity> nearby = car.getNearbyEntities(1.5, 1.5, 1.5); //Nearby cars
		for(Entity e:nearby){
			if(e instanceof Minecart){ //Avoid driving into another car
				car.setVelocity(new Vector(0,0,0)); //Stop
				return;
			}
		}
		
		if(main.random.nextInt(5) < 1){ // 1 in 5 chance
			//Check if players nearby
			boolean nearbyPlayers = false;
			nearby = car.getNearbyEntities(20, 10, 20); //20x20 radius
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
		
		if(c.stats.containsKey("trade.speed")){
			speed = ((SpeedStat) c.stats.get("trade.speed")).getSpeedMultiplier();
		}
		
		if(car.hasMetadata("trade.npc")){
			List<MetadataValue> ms = car.getMetadata("trade.npc");
			direction = (BlockFace) ms.get(0).value();
		}
		else{
			//Calculate direction from road
			direction = main.plugin.aiSpawns.carriagewayDirection(under);
		}
		
		if(direction == null){ //Not on a road
 			//Pretend we're a parked car as we're pushed of course...
 			return;
		}
		
		//Now we need to route it...
		TrackingData nextTrack = AITrackFollow.nextBlock(under, direction, trackBlock);
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
		double z = tz = cz;
		
		Boolean ux = true;
		double px = Math.abs(x);
		double pz = Math.abs(z);
		if (px > pz) {
			ux = false;
		}

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
		vel = new Vector(x,y,z); //Go to block
		
		car.setVelocity(vel);
		return;
	}
}
