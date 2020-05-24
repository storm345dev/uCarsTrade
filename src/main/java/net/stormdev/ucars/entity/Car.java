package net.stormdev.ucars.entity;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Vehicle;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public interface Car extends ArmorStand,Vehicle {
	public void setDisplay(ItemStack stack, double d);
	public boolean addPassenger(Entity passenger);
	public List<Entity> getPassengers();
	public double getDisplayOffset();
	public float getHitBoxX();
	public float getHitBoxZ();
	public void setHitBoxX(float x);
	public void setHitBoxZ(float z);
	public void setYaw(float yaw);
	public void setPitch(float pitch);
	public void setRoll(float roll);
	public void setYawPitch(float yaw, float pitch);
	public double getMaxPassengers();
	public double getBoatRotationOffsetDegrees();
}
