package net.stormdev.ucars.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import net.minecraft.server.v1_9_R1.Entity;
import net.minecraft.server.v1_9_R1.EntityVillager;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_9_R1.entity.CraftVillager;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;

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
		
		ve.h(yaw); //Set head rotation
		
		/*ProtocolManager manager = ProtocolLibrary.getProtocolManager();
		PacketContainer headPos = manager.createPacket(PacketType.Play.Server.ENTITY_HEAD_ROTATION);
		
		float ya = yaw;
		while(ya < 0){
			ya+=360;
		}
		while(ya > 360){
			ya-=360;
		}
		float angle = (yaw*256.0f/360.0f);
		int a = (int) angle;
		headPos.getIntegers().write(0, ve.getId());
		headPos.getBytes().write(0, (byte) a);
		
		List<org.bukkit.entity.Entity> near = v.getNearbyEntities(50, 5, 50);
		for(org.bukkit.entity.Entity e:near){
			if(e instanceof Player){
				try {
					manager.sendServerPacket((Player) e, headPos);
				} catch (InvocationTargetException e1) {
					e1.printStackTrace();
				}
			}
		}
		*/
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
