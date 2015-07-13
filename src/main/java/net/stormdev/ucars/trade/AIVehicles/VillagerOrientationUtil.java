package net.stormdev.ucars.trade.AIVehicles;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import org.bukkit.entity.Villager;

import com.useful.ucars.Reflect;

public class VillagerOrientationUtil {
	public static void setPitch(Villager cart, float pitch){
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityVillager");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = ema.getField("pitch");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), -pitch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void setYaw(Villager cart, float yaw){
		try {
			Class<?> cmr = cart.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityVillager");
			Object nmsCart = getHandle.invoke(cmr.cast(cart));
			Field p = ema.getField("yaw");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), yaw);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
