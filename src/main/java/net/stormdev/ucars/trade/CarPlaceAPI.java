package net.stormdev.ucars.trade;

import java.util.regex.Pattern;

import net.stormdev.ucars.entity.Car;
import net.stormdev.ucars.entity.CarMinecraftEntity;
import net.stormdev.ucarstrade.cars.CarPresets.CarPreset;
import net.stormdev.ucarstrade.cars.DrivenCar;
import net.stormdev.ucarstrade.displays.DisplayManager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

import com.useful.ucars.CarHealthData;
import com.useful.ucars.CartOrientationUtil;
import com.useful.ucars.ucars;
import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;
import org.bukkit.inventory.ItemStack;

public class CarPlaceAPI {
	public static Entity placeCar(DrivenCar carData, Location placeLoc, Player placer, float direction){
		Location loc = placeLoc.clone().add(0, 1.6, 0);
		loc.setYaw(direction+90);
		Block in = loc.getBlock();
		CarMinecraftEntity hce = new CarMinecraftEntity(loc.clone().add(0, 0.0, 0));
		hce.setHitBoxX(carData.getHitboxX());
		hce.setHitBoxZ(carData.getHitboxZ());
		hce.setMaxPassengers(carData.getMaxPassengers());
		hce.setBoatOffsetDeg(carData.getBoatOrientationOffsetDeg());
		final Car car = hce.spawn();
		//final Minecart car = (Minecart) placeLoc.getWorld().spawnEntity(loc, EntityType.MINECART);
		float yaw = direction;
		if(yaw < 0){
			yaw = 360 + yaw;
		}
		else if(yaw >= 360){
			yaw = yaw - 360;
		}
		CartOrientationUtil.setYaw(car, yaw+90);
		
		//Display blocks
		CarPreset cp = carData.getPreset();
		if(cp != null && cp.hasDisplayBlock()){
			car.setDisplay(new ItemStack(cp.getDisplayBlock().getItemType(), 1, cp.getDisplayBlock().getData()), cp.getDisplayBlockOffset());
		}
		else if(carData.getBaseDisplayBlock() != null){

			car.setDisplay(new ItemStack(carData.getBaseDisplayBlock().getItemType(), 1, carData.getBaseDisplayBlock().getData()), 0);
		}
		
		in = car.getLocation().getBlock();
		Block n = in.getRelative(BlockFace.NORTH);   // The directions minecraft aligns the cart to
		Block w = in.getRelative(BlockFace.WEST);
		Block nw = in.getRelative(BlockFace.NORTH_WEST);
		Block ne = in.getRelative(BlockFace.NORTH_EAST);
		Block sw = in.getRelative(BlockFace.SOUTH_WEST);
		if((!in.isEmpty() && !in.isLiquid())
				|| (!n.isEmpty() && !n.isLiquid())
				|| (!w.isEmpty() && !w.isLiquid())
				|| (!ne.isEmpty() && !ne.isLiquid())
				|| (!nw.isEmpty() && !nw.isLiquid())
				|| (!sw.isEmpty() && !sw.isLiquid())){
			if(placer != null){
				car.remove();
				return null;
			}
		}
		double health = carData.getHealth();
		CarHealthData chd = ucars.listener.getCarHealthHandler(car);
		chd.setHealth(health);
		ucars.listener.updateCarHealthHandler(car, chd);
		/*
		 * Location carloc = car.getLocation();
		 * carloc.setYaw(event.getPlayer().getLocation().getYaw() + 270);
		 * car.setVelocity(new Vector(0,0,0)); car.teleport(carloc);
		 * car.setVelocity(new Vector(0,0,0));
		 */
		/*car.setMetadata("trade.car", new StatValue(true, main.plugin));*/
		UEntityMeta.setMetadata(car, "trade.car", new StatValue(true, main.plugin));
		carData.setId(car.getUniqueId());
		main.plugin.carSaver.carNowInUse(car, carData);
		String name = carData.getName();
		String placeMsg = net.stormdev.ucars.trade.Lang.get("general.place.msg");
		placeMsg = main.colors.getInfo() + placeMsg.replaceAll(Pattern.quote("%name%"), "'"+name+"'");
		if(placer != null){
			placer.sendMessage(placeMsg);
			DisplayManager.fillCar(car, carData, placer);
		}
		return car;
	}
}
