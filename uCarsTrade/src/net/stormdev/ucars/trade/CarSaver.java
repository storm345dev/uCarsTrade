package net.stormdev.ucars.trade;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;

import net.stormdev.ucars.utils.Car;

public class CarSaver {
	public HashMap<UUID, Car> cars = new HashMap<UUID, Car>();
	File saveFile = null;
	public CarSaver(File saveFile){
		this.saveFile = saveFile;
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
