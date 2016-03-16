package net.stormdev.ucars.utils;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class SquareDistanceCheck {
	public static double getShortestXZDistance(Location one, Location two){
		Vector v = one.toVector().clone().subtract(two.toVector());
		return Math.min(Math.abs(v.getX()), Math.abs(v.getZ()));
	}
}
