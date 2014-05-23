package net.stormdev.ucars.trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.stormdev.ucars.utils.Car;
import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class CarSaver {
	private ConcurrentHashMap<UUID, Car> oldCars = new ConcurrentHashMap<UUID, Car>();
	private ConcurrentHashMap<UUID, DrivenCar> inUse = new ConcurrentHashMap<UUID, DrivenCar>();
	
	File saveFile = null;
	File newSaveFile = null;
	public CarSaver(File saveFile, File newSaveFile){
		this.saveFile = saveFile;
		this.newSaveFile = newSaveFile;
	}
	
	public boolean isAUCar(UUID carId){
		boolean b = inUse.contains(carId);
		if(!b){
			return isACar(carId);
		}
		return b;
	}
	
	public DrivenCar getCarInUse(UUID carId){
		return inUse.get(carId);
	}
	
	public void carNoLongerInUse(DrivenCar car){
		inUse.remove(car.getId());
		asyncSave();
	}
	
	public void carNoLongerInUse(UUID id){
		inUse.remove(id);
		asyncSave();
	}
	
	public void carNowInUse(DrivenCar car){
		inUse.put(car.getId(), car);
		asyncSave();
	}
	
	
	@Deprecated
	public Boolean isACar(UUID carId){
		Boolean b = oldCars.containsKey(carId);
		return b;
	}
	@Deprecated
	public Car getOldCar(UUID carId){
		return oldCars.get(carId);
	}
	@Deprecated
	public void removeOldCar(UUID carId){
		oldCars.remove(carId);
		asyncSave();
	}
	public void asyncSave(){
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			public void run() {
				saveIt();
				return;
			}});
	}
	public void load(){
		this.saveFile.getParentFile().mkdirs();
		if(!this.saveFile.exists() || this.saveFile.length() < 1){
			try {
				this.saveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		else{
			try {
				this.oldCars = loadOldHashMap(this.saveFile.getAbsolutePath());
			} catch (Exception e) {
				//Old format
				this.oldCars = null;
			}
		}
		if(this.oldCars == null){
			this.oldCars = new ConcurrentHashMap<UUID, Car>();
		}
		
		this.newSaveFile.getParentFile().mkdirs();
		if(!this.newSaveFile.exists() || this.saveFile.length() < 1){
			try {
				this.newSaveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		else{
			try {
				this.inUse = loadHashMap(this.saveFile.getAbsolutePath());
			} catch (Exception e) {
				//Bad format
				this.inUse = null;
			}
		}
		if(this.inUse == null){
			this.inUse = new ConcurrentHashMap<UUID, DrivenCar>();
		}
	}
	public void save(){
		asyncSave();
		return;
	}
	private void saveIt(){
		if(!this.newSaveFile.exists() || this.newSaveFile.length() < 1){
			try {
				this.newSaveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		saveHashMap(inUse, this.newSaveFile.getAbsolutePath());
	}
	public static void saveHashMap(ConcurrentHashMap<UUID, DrivenCar> map, String path)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			//Handle I/O exceptions
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public static ConcurrentHashMap<UUID, DrivenCar> loadHashMap(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			try {
				return (ConcurrentHashMap<UUID, DrivenCar>) result;
			} catch (Exception e) {
				return new ConcurrentHashMap<UUID, DrivenCar>();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static void saveOldHashMap(ConcurrentHashMap<UUID, Car> map, String path)
	{
		try
		{
			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
			oos.writeObject(map);
			oos.flush();
			oos.close();
			//Handle I/O exceptions
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	@SuppressWarnings("unchecked")
	public static ConcurrentHashMap<UUID, Car> loadOldHashMap(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			try {
				return (ConcurrentHashMap<UUID, Car>) result;
			} catch (Exception e) {
				try {
					HashMap<UUID, Car> oldFormat = (HashMap<UUID, Car>) result;
					ConcurrentHashMap<UUID, Car> rm = new ConcurrentHashMap<UUID, Car>();
					rm.putAll(oldFormat);
					return rm;
				} catch (Exception e1) {
					return new ConcurrentHashMap<UUID, Car>();
				}
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
