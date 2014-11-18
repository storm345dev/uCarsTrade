package net.stormdev.ucars.trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.stormdev.ucarstrade.cars.DrivenCar;

import org.bukkit.scheduler.BukkitRunnable;

public class CarSaver {
	private ConcurrentHashMap<UUID, DrivenCar> inUse = new ConcurrentHashMap<UUID, DrivenCar>();
	private ConcurrentHashMap<UUID, DrivenCar> cache = new ConcurrentHashMap<UUID, DrivenCar>();
	
	File newSaveFile = null;
	public CarSaver(File newSaveFile){
		this.newSaveFile = newSaveFile;
	}
	
	public boolean isAUCar(UUID carId){
		if(cacheSize()){
			if(cache.containsKey(carId)){
				return true;
			}
		}
		return inUse.containsKey(carId);
	}
	
	public DrivenCar getCarInUse(UUID carId){
		if(cacheSize()){
			DrivenCar dc = cache.get(carId);
			if(dc != null){
				return dc;
			}
		}
		return inUse.get(carId);
	}
	
	private boolean cacheSize(){
		while(cache.size() > main.plugin.carCache){
			cache.remove(cache.keySet().toArray(new UUID[]{})[0]);
		}
		return inUse.size() > cache.size();
	}
	
	public void carNoLongerInUse(DrivenCar car){
		if(car == null || car.getId() == null){
			throw new RuntimeException("DrivenCar is null!");
		}
		inUse.remove(car.getId());
		cache.remove(car.getId());
		asyncSave();
		cacheSize();
	}
	
	public void carNoLongerInUse(UUID id){
		inUse.remove(id);
		cache.remove(id);
		asyncSave();
		cacheSize();
	}
	
	public void carNowInUse(DrivenCar car){
		if(car == null || car.getId() == null){
			throw new RuntimeException("DrivenCar is null!");
		}
		inUse.put(car.getId(), car);
		cache.put(car.getId(), car);
		asyncSave();
		cacheSize();
	}
	
	public void asyncSave(){
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			public void run() {
				saveIt();
				return;
			}});
	}
	public void load(){
		
		this.newSaveFile.getParentFile().mkdirs();
		if(!this.newSaveFile.exists() || this.newSaveFile.length() < 1){
			try {
				this.newSaveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		else{
			try {
				this.inUse = loadHashMap(this.newSaveFile.getAbsolutePath());
			} catch (Exception e) {
				//Bad format
				this.inUse = null;
			}
		}
		if(this.inUse == null){
			this.inUse = new ConcurrentHashMap<UUID, DrivenCar>();
		}
		cacheSize();
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
	        Object result;
			try {
				result = ois.readObject();
			} catch (NoClassDefFoundError e1) {
				try {
					File f = new File(path);
					f.delete();
					f.createNewFile();
				} catch (Exception e) {
					//CLear invalid file
				}
				ois.close();
				result = new ConcurrentHashMap<UUID, DrivenCar>();
			}
	        ois.close();
			try {
				return (ConcurrentHashMap<UUID, DrivenCar>) result;
			} catch (Exception e) {
				return new ConcurrentHashMap<UUID, DrivenCar>();
			}
		}
		catch(Exception e)
		{
			return null;
		}
	}
}
