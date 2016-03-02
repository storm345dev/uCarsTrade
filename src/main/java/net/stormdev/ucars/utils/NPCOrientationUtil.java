package net.stormdev.ucars.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityVillager;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_9_R1.util.UnsafeList;
import org.bukkit.entity.Villager;

public class NPCOrientationUtil {
	/*public static void setPitch(Villager v, float pitch){
		try {
			Class<?> cmr = v.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityVillager");
			Object nmsCart = getHandle.invoke(cmr.cast(v));
			Field p = ema.getField("pitch");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), -pitch);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}*/
	
	public static void setYaw(Villager v, float yaw){
		EntityVillager ve = ((CraftVillager)v).getHandle();
		
		Class<?> c = Entity.class;
		try {
			Method m = c.getDeclaredMethod("setYawPitch", float.class, float.class);
			m.setAccessible(true);
			m.invoke(ve, yaw, ve.pitch);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		/*try {
			Class<?> cmr = v.getClass();
			Method getHandle = cmr.getMethod("getHandle");
			Class<?> ema = Reflect.getNMSClass("EntityVillager");
			Object nmsCart = getHandle.invoke(cmr.cast(v));
			Field p = ema.getField("yaw");
			p.setAccessible(true);
			p.set(ema.cast(nmsCart), yaw);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
	}
}
