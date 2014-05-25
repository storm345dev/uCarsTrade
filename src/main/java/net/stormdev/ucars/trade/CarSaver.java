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
	
	File newSaveFile = null;
	public CarSaver(File saveFile, File newSaveFile){
		this.newSaveFile = newSaveFile;
	}
	
	public boolean isAUCar(UUID carId){
		boolean b = inUse.contains(carId);
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
}
