package net.stormdev.ucars.utils;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.DynamicLagReducer;

import org.bukkit.Bukkit;

public class Scheduler {
	public static void runBlockingSyncTask(final Runnable run) throws Exception{
		final ToggleLatch latch = new ToggleLatch().lock(); //Create a new latch, and lock it
		
		Bukkit.getScheduler().runTask(main.plugin, new Runnable(){

			@Override
			public void run() {
				try {
					run.run();
				}
				finally {
					//It's finished
					latch.unlock();
				}
				return;
			}});
		
		int timeout = 40;
		while(latch.isLocked() && timeout > 0){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
			timeout--;
		}
		
		if(timeout < 1){
			//It timed out
			DynamicLagReducer.failedSyncTask();
			throw new Exception("Sync blocking task in uCarsTrade failed to finish in time and was timed out! This isn't a bug, it's just an operation failure.");
		}
	}
	
	public static void runBlockingSyncTask(final Runnable run, int timeOut) throws Exception{
		final ToggleLatch latch = new ToggleLatch().lock(); //Create a new latch, and lock it
		
		Bukkit.getScheduler().runTask(main.plugin, new Runnable(){

			@Override
			public void run() {
				try {
					run.run();
				}
				finally {
					//It's finished
					latch.unlock();
				}
				return;
			}});
		
		int timeout = timeOut * 4;
		while(latch.isLocked() && timeout > 0){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
			timeout--;
		}
		
		if(timeout < 1){
			//It timed out
			DynamicLagReducer.failedSyncTask();
			throw new Exception("Sync blocking task in uCarsTrade failed to finish in time and was timed out! This isn't a bug, it's just an operation failure.");
		}
	}
	
	public static void runBlockingSyncTaskNoTimeout(final Runnable run) throws Exception{
		final ToggleLatch latch = new ToggleLatch().lock(); //Create a new latch, and lock it
		
		Bukkit.getScheduler().runTask(main.plugin, new Runnable(){

			@Override
			public void run() {
				try {
					run.run();
				}
				finally {
					//It's finished
					latch.unlock();
				}
				return;
			}});
		
		while(latch.isLocked()){
			try {
				Thread.sleep(250);
			} catch (InterruptedException e) {
			}
		}
	}
}
