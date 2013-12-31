package net.stormdev.ucars.utils;

import java.io.Serializable;

import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public interface CarFiller extends Serializable {
	public void putInCar(Minecart car, Player owner);
}
