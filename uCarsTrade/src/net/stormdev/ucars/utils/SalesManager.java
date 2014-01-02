package net.stormdev.ucars.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.UUID;

import net.stormdev.ucars.race.main;

public class SalesManager {
	
	public File carSaveFile = null;
	public File upgradeSaveFile = null;
	public HashMap<UUID, CarForSale> carsForSale = new HashMap<UUID,CarForSale>();
	public HashMap<UUID, UpgradeForSale> upgradeForSale = new HashMap<UUID, UpgradeForSale>();
	public SalesManager(File carSaveFile, File upgradeSaveFile){
		this.carSaveFile = carSaveFile;
		this.upgradeSaveFile = upgradeSaveFile;
		load();
	}
	public synchronized void load(){
		if(carSaveFile.length() < 1 || !carSaveFile.exists()){
			try {
				carSaveFile.createNewFile();
			} catch (IOException e) {
				main.logger.info(main.colors.getError()+"Error creating save file for cars market!");
			}
		}
		else{
			this.carsForSale = loadHashMapCars(carSaveFile.getAbsolutePath());
		}
		if(upgradeSaveFile.length() < 1 || !upgradeSaveFile.exists()){
			try {
				upgradeSaveFile.createNewFile();
			} catch (IOException e) {
				main.logger.info(main.colors.getError()+"Error creating save file for upgrade market!");
			}
		}
		else{
			this.upgradeForSale = loadHashMapUpgrades(upgradeSaveFile.getAbsolutePath());
		}
		if(this.carsForSale == null){
			this.carsForSale = new HashMap<UUID,CarForSale>();
		}
		return;
	}
	public void saveAll(){
	    saveCars();
	    saveUpgrades();
	}
	public synchronized void saveCars(){
		if(!this.carSaveFile.exists() || this.carSaveFile.length() < 1){
			try {
				carSaveFile.createNewFile();
			} catch (IOException e) {
				main.logger.info(main.colors.getError()+"Error creating save file for cars market!");
				return;
			}
		}
		saveHashMapCars(carsForSale, carSaveFile.getAbsolutePath());
	}
	public synchronized void saveUpgrades(){
		if(!this.upgradeSaveFile.exists() || this.upgradeSaveFile.length() < 1){
			try {
				upgradeSaveFile.createNewFile();
			} catch (IOException e) {
				main.logger.info(main.colors.getError()+"Error creating save file for upgrades market!");
				return;
			}
		}
		saveHashMapUpgrades(upgradeForSale, upgradeSaveFile.getAbsolutePath());
	}
	public static void saveHashMapCars(HashMap<UUID, CarForSale> map, String path)
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
	public static HashMap<UUID, CarForSale> loadHashMapCars(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			return (HashMap<UUID, CarForSale>) result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static void saveHashMapUpgrades(HashMap<UUID, UpgradeForSale> map, String path)
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
	public static HashMap<UUID, UpgradeForSale> loadHashMapUpgrades(String path)
	{
		try
		{
	        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(path));
	        Object result = ois.readObject();
	        ois.close();
			return (HashMap<UUID, UpgradeForSale>) result;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}

}
