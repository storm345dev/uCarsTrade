package net.stormdev.ucars.trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;

import org.bukkit.scheduler.BukkitRunnable;

import net.stormdev.ucars.utils.Car;

public class CarSaver {
	public HashMap<UUID, Car> cars = new HashMap<UUID, Car>();
	public HashMap<UUID, Car> cache = new HashMap<UUID, Car>();
	File saveFile = null;
	public CarSaver(File saveFile){
		this.saveFile = saveFile;
	}
	public synchronized Boolean isACar(UUID carId){
		if(cache.containsKey(carId)){
			return true;
		}
		Boolean b = cars.containsKey(carId);
		if(b){
			cache.put(carId, cars.get(carId));
			cacheSize();
		}
		return b;
	}
	public synchronized Car getCar(UUID carId){
		Car c = cache.get(carId);
		if(c != null){
			return c;
		}
		return cars.get(carId);
	}
	public synchronized void setCar(UUID carId, Car car){
		if(!car.isPlaced){
			cache.remove(carId);
		}
		else{
			cache.put(carId, car);
		}
		cars.put(carId, car);
		cacheSize();
		asyncSave();
	}
	public synchronized void removeCar(UUID carId){
		cache.remove(carId);
		cars.remove(cars);
		asyncSave();
	}
	public void updateCar(UUID old, Car current){
		removeCar(old);
		setCar(current.id, current);
		asyncSave();
	}
	public synchronized void cacheSize(){
		while(cache.size() > 20){ //Maximum car cache
			cache.remove(cache.keySet().toArray()[0]); //Clear it back to size
		}
		return;
	}
	public synchronized void removeFromCache(UUID carId){
		cache.remove(carId);
	}
	public void noLongerPlaced(UUID carId){
		removeFromCache(carId);
	}
	public void asyncSave(){
		main.plugin.getServer().getScheduler().runTaskAsynchronously(main.plugin, new BukkitRunnable(){

			public void run() {
				save();
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
			this.cars = loadHashMap(this.saveFile.getAbsolutePath());
		}
		if(this.cars == null){
			this.cars = new HashMap<UUID, Car>();
		}
	}
	public void save(){
		if(!this.saveFile.exists() || this.saveFile.length() < 1){
			try {
				this.saveFile.createNewFile();
			} catch (IOException e) {
			}
		}
		saveHashMap(cars, this.saveFile.getAbsolutePath());
	}
	public static void saveHashMap(HashMap<UUID, Car> map, String path)
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
	public static HashMap<UUID, Car> loadHashMap(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			return (HashMap<UUID, Car>) result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
}
