package net.stormdev.ucars.trade;

import com.useful.ucars.util.UEntityMeta;
import com.useful.ucarsCommon.StatValue;
import net.stormdev.ucarstrade.cars.DrivenCar;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CarSaver {
	public static final String META = "car.drivenCarMeta";
	private volatile Map<UUID, DrivenCar> inUse = new ConcurrentHashMap<UUID, DrivenCar>(200, 0.75f, 2);
	/*private volatile Map<UUID, DrivenCar> cache = new ConcurrentHashMap<UUID, DrivenCar>();*/
	
	File newSaveFile = null;
	public CarSaver(File newSaveFile){
		this.newSaveFile = newSaveFile;
		Bukkit.getScheduler().runTaskTimer(main.plugin, new Runnable(){

			@Override
			public void run() {
				//Remove removed entities from inUse
				final List<Entity> entities = new ArrayList<Entity>();
				for(World w:Bukkit.getWorlds()){
					entities.addAll(w.getEntities());
				}
				Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

					@Override
					public void run() {
						mainLoop: for(UUID id:new ArrayList<UUID>(inUse.keySet())){
							for(Entity e:entities){
								if(e.getUniqueId().equals(id)){
									continue mainLoop;
								}
							}
							//No entity matched it!
							inUse.remove(id);
						}
						return;
					}});
				return;
			}}, 180*20l, 180*20l);
	}
	
	public boolean isAUCar(Entity v){
		if(UEntityMeta.hasMetadata(v, META)){
			return true;
		}
		return inUse.containsKey(v.getUniqueId());
	}
	
	public boolean isAUCarWithEntityID(UUID carId){
		return inUse.containsKey(carId);
	}
	
	public DrivenCar getCarInUse(Entity cart){
		try {
			return (DrivenCar) UEntityMeta.getMetadata(cart, META).get(0).value();
		} catch (Exception e) {
			UEntityMeta.removeMetadata(cart, META);
			return inUse.get(cart.getUniqueId());
		}
	}
	
	public DrivenCar getCarInUseWithEntityID(UUID carId){
		return inUse.get(carId);
	}
	
	public void carNoLongerInUse(final DrivenCar car){
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				synchronized(CarSaver.this){
					if(car == null || car.getId() == null){
						throw new RuntimeException("DrivenCar is null!");
					}
					inUse.remove(car.getId());
					asyncSave();
				}
				return;
			}});
	}
	
	public void carNoLongerInUse(final UUID id){
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				synchronized(CarSaver.this){
					inUse.remove(id);
					asyncSave();
				}
				return;
			}});	
	}
	
	public void carNoLongerInUseNow(final UUID id){
		synchronized(CarSaver.this){
			inUse.remove(id);
			save();
		}
	}
	
	public void carNowInUse(final Entity v, final DrivenCar car){
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				UEntityMeta.removeMetadata(v, META);
				UEntityMeta.setMetadata(v, META, new StatValue(car, main.plugin));
				if(car == null || car.getId() == null){
					throw new RuntimeException("DrivenCar is null!");
				}
				synchronized(CarSaver.this){
					inUse.put(car.getId(), car);
					asyncSave();
				}
				return;
			}});
		
	}
	
	public void carNowInUseNoEntity(final DrivenCar car){
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				if(car == null || car.getId() == null){
					throw new RuntimeException("DrivenCar is null!");
				}
				synchronized(CarSaver.this){
					inUse.put(car.getId(), car);
					asyncSave();
				}
				return;
			}});
		
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
	public static void saveHashMap(Map<UUID, DrivenCar> map, String path)
	{
		synchronized(main.plugin.carSaver){
			HashMap<?, ?> hm = new HashMap<UUID, DrivenCar>(map);
			try
			{
				ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(path));
				oos.writeObject(hm);
				oos.flush();
				oos.close();
				//Handle I/O exceptions
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	@SuppressWarnings("unchecked")
	public static Map<UUID, DrivenCar> loadHashMap(String path)
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
				result = new HashMap<UUID, DrivenCar>();
			}
	        ois.close();
			try {
				Map<UUID, DrivenCar> res = (Map<UUID, DrivenCar>) result;
				if(res instanceof ConcurrentHashMap){
					Map<UUID, DrivenCar> r = res;
					res = new HashMap<UUID, DrivenCar>();
					res.putAll(r);
				}
				return res;
			} catch (Exception e) {
				return new HashMap<UUID, DrivenCar>();
			}
		}
		catch(Exception e)
		{
			return null;
		}
	}
}
