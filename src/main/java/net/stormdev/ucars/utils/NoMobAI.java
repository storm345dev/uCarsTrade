package net.stormdev.ucars.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

import net.minecraft.server.v1_9_R1.EntityInsentient;

import org.bukkit.craftbukkit.v1_9_R1.entity.CraftEntity;
import org.bukkit.entity.Entity;

public class NoMobAI {
	public static void clearAI(Entity e){
		net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity)e).getHandle();
		if(!(nmsEntity instanceof EntityInsentient)){
			return;
		}
		EntityInsentient ve = (EntityInsentient) nmsEntity;
		try {
			Class<?> pathClass = ve.goalSelector.getClass();
			Field b = pathClass.getDeclaredField("b");
			b.setAccessible(true);
			Object goal = ve.goalSelector;
			Object target = ve.targetSelector;
			Class<?> setClass = Set.class;
			Method m = setClass.getDeclaredMethod("clear");
			m.setAccessible(true);
			m.invoke(setClass.cast(b.get(goal)));
			m.invoke(setClass.cast(b.get(target)));
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}
	
	public static void setAI(Entity bukkitEntity, boolean ai) {
		net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
		if(nmsEntity instanceof EntityInsentient){
			((EntityInsentient)nmsEntity).m(!ai);
		}
		/*net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
	    NBTTagCompound tag = new NBTTagCompound();
	    nmsEntity.e(tag);
	    if (tag == null) {
	        tag = new NBTTagCompound();
	    }
	    nmsEntity.c(tag);
	    tag.setInt("NoAI", ai?1:0);
	    nmsEntity.f(tag);*/
	}
	
	public static void noAI(Entity bukkitEntity) {
		setAI(bukkitEntity, false);
		/*net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
		NBTTagCompound tag = new NBTTagCompound();
	    nmsEntity.e(tag);
	    if (tag == null) {
	        tag = new NBTTagCompound();
	    }
	    nmsEntity.c(tag);
	    tag.setInt("NoAI", 1);
	    nmsEntity.f(tag);*/
	}
	
	public static void AI(Entity bukkitEntity) {
		setAI(bukkitEntity, true);
		/*net.minecraft.server.v1_9_R1.Entity nmsEntity = ((CraftEntity) bukkitEntity).getHandle();
		NBTTagCompound tag = new NBTTagCompound();
	    nmsEntity.e(tag);
	    if (tag == null) {
	        tag = new NBTTagCompound();
	    }
	    nmsEntity.c(tag);
	    tag.setInt("NoAI", 0);
	    nmsEntity.f(tag);*/
	}
}
