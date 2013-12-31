package net.stormdev.ucars.utils;

import java.io.Serializable;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Player;

public class Displays implements Serializable {
	public static DisplayType Entity_Pig = 
			new DisplayType("Pig", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.PIG);
					return;
					}});
	public static DisplayType Entity_Sheep = 
			new DisplayType("Sheep", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.SHEEP);
					return;
					}});
	public static DisplayType Entity_Chicken = 
			new DisplayType("Chicken", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.CHICKEN);
					return;
					}});
	public static DisplayType Entity_Blaze = 
			new DisplayType("Blaze", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.BLAZE);
					return;
					}});
	public static DisplayType Entity_Bat = 
			new DisplayType("Bat", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.BAT);
					return;
					}});
	public static DisplayType Entity_Boat = 
			new DisplayType("Boat", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.BOAT);
					return;
					}});
	public static DisplayType Entity_Cow = 
			new DisplayType("Chicken", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.COW);
					return;
					}});
	public static DisplayType Entity_Spider = 
			new DisplayType("Spider", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.SPIDER);
					return;
					}});
	public static DisplayType Entity_Zombie = 
			new DisplayType("Zombie", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.ZOMBIE);
					return;
					}});
	public static DisplayType Entity_Horse = 
			new DisplayType("Horse", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					Horse h = (Horse) DisplayType.spawnEntityAtCar(car, EntityType.HORSE);
					h.setAdult();
					h.setOwner(player);
					DisplayType.putEntityInCar(car, h);
					return;
					}});
	public static DisplayType Entity_IronGolem = 
			new DisplayType("Iron Golem", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.IRON_GOLEM);
					return;
					}});
	public static DisplayType Entity_MagmaCube = 
			new DisplayType("Magma Cube", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.MAGMA_CUBE);
					return;
					}});
	public static DisplayType Entity_Slime = 
			new DisplayType("Slime", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.SLIME);
					return;
					}});
	public static DisplayType Entity_Skeleton = 
			new DisplayType("Skeleton", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.SKELETON);
					return;
					}});
	public static DisplayType Entity_Squid = 
			new DisplayType("Squid", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.SQUID);
					return;
					}});
	public static DisplayType Entity_SnowMan = 
			new DisplayType("SnowMan", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.SNOWMAN);
					return;
					}});
	public static DisplayType Entity_Villager = 
			new DisplayType("Villager", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.VILLAGER);
					return;
					}});
	public static DisplayType Entity_Witch = 
			new DisplayType("Witch", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.WITCH);
					return;
					}});
	public static DisplayType Entity_Wolf = 
			new DisplayType("Wolf", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.WOLF);
					return;
					}});
	public static DisplayType Entity_Ocelot = 
			new DisplayType("Ocelot", new CarFiller(){
				private static final long serialVersionUID = 1L;
				public void putInCar(Minecart car, Player player) {
					DisplayType.putEntityInCar(car, EntityType.OCELOT);
					return;
					}});
}
