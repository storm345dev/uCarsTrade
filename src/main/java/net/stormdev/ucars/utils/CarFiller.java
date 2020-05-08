package net.stormdev.ucars.utils;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.Serializable;

public interface CarFiller extends Serializable {
	public void putInCar(Entity car, Player owner);
}
