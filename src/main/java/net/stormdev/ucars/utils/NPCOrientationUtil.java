package net.stormdev.ucars.utils;

import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.EntityLiving;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftLivingEntity;
import org.bukkit.entity.LivingEntity;

import java.lang.reflect.Method;

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
	
	public static void setYaw(LivingEntity v, float yaw){
		EntityLiving ve = ((CraftLivingEntity)v).getHandle();
		
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
