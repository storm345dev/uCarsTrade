package net.stormdev.ucars.trade.AIVehicles.spawning;

import java.util.ArrayList;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import com.useful.ucars.ClosestFace;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.AIRouter;
import net.stormdev.ucars.trade.AIVehicles.SpawnData;
import net.stormdev.ucars.utils.ReturnTask;
import net.stormdev.ucars.utils.Scheduler;
import net.stormdev.ucars.utils.SyncReturnTask;

public class AIWorldProbingSpawnManager extends AbstractAISpawnManager {
	private BukkitTask task = null;
	private static long spawnRate = 20l;
	
	public AIWorldProbingSpawnManager(main plugin, boolean enabled) {
		super(plugin, enabled);
	}

	@Override
	public void shutdown() {
		if(task != null){
			task.cancel();
		}
	}

	@Override
	public void initSpawnTask() {
		task = main.plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){

			public void run() {
				if(!enabled){
					return;
				}
				if(spawnedCount >= liveCap || spawnedCount >= cap){
					return;
				}
				/*boolean longSpawns = main.random.nextBoolean();
				boolean doubleSpawns = longSpawns && main.random.nextBoolean();*/
				boolean longSpawns = main.random.nextInt(10) < 8; //8/10 chance
				boolean doubleSpawns = longSpawns && main.random.nextInt(10) < 8; //8/10 * 8/10 chance
				
				for(Player player:new ArrayList<Player>(Bukkit.getOnlinePlayers())){
					try {
						doSpawns(player);
					} catch (Exception e) {
						e.printStackTrace();
						//Error spawning
					}
					if(longSpawns){
						doLongSpawns(player);
					}
					if(doubleSpawns){
						try {
							doSpawns(player);
						} catch (Exception e) {
							//Error spawning
							e.printStackTrace();
						}
					}
				}
				return;
			}}, spawnRate, spawnRate);
	}
	
	
	public void doLongSpawns(final Player player){
		if(!enabled){
			return;
		}
		if(main.random.nextInt(10) < 2){ //2/10 chance
			return; //Next iteration
		}
		if(player == null || !player.isOnline()){
			return; //Next iteration
		}
		try {
			Block tracked = null;
			boolean stopSearch = false;
			
			SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

				@Override
				public SpawnData[] execute() {
					Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
					Block br = b.getRelative(randomFace(), randomDir3Amount());
					World w = b.getWorld();
					int y = br.getY();
					int x = br.getX();
					int z = br.getZ();
					
					return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
				}}).executeOnce();
			SpawnData data = spawnData.getResults()[0];
			
			//Location playerLoc = data.getPlayerLoc();
			Block b = data.getB();
			Block br = data.getBr();
			final World w = data.getWorld();
			int y = data.getY();
			final int x = data.getX();
			final int z = data.getZ();
			
			int minY = y-10;
			
			tracked = AIRouter.isTrackBlock(b.getType()) ? b : null;
			tracked = AIRouter.isTrackBlock(br.getType()) ? br : null;
			
			while(tracked == null
					&& !stopSearch
					&& y>minY){
				final int yy = y;
				Scheduler.runBlockingSyncTask(new Runnable(){

					@Override
					public void run() {
						Location check = new Location(w, x, yy, z);
						if(AIRouter.isTrackBlock(check.getBlock().getType())){
							followRoadAndSpawnCarFromTrackerBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
						}
						return;
					}});
				
				y--;
			}
		} catch (Exception e) {
			//They just joined, or error spawning
		}
		return;
	}
	public void doLongSpawns(){
		if(!enabled){
			return;
		}
		Player[] online = new ArrayList<Player>(plugin.getServer().getOnlinePlayers()).toArray(new Player[]{});
		for(final Player player:online){
			if(main.random.nextBoolean()){
				continue; //Next iteration
			}
			if(player == null || !player.isOnline()){
				continue; //Next iteration
			}
			try {
				Block tracked = null;
				boolean stopSearch = false;
				
				SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

					@Override
					public SpawnData[] execute() {
						Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
						Block br = b.getRelative(randomFace(), randomDir3Amount());
						World w = b.getWorld();
						int y = br.getY();
						int x = br.getX();
						int z = br.getZ();
						
						return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
					}}).executeOnce();
				SpawnData data = spawnData.getResults()[0];
				
				//Location playerLoc = data.getPlayerLoc();
				Block b = data.getB();
				Block br = data.getBr();
				final World w = data.getWorld();
				int y = data.getY();
				final int x = data.getX();
				final int z = data.getZ();
				
				int minY = y-10;
				
				tracked = AIRouter.isTrackBlock(b.getType()) ? b : null;
				tracked = AIRouter.isTrackBlock(br.getType()) ? br : null;
				
				while(tracked == null
						&& !stopSearch
						&& y>minY){
					final int yy = y;
					Scheduler.runBlockingSyncTask(new Runnable(){

						@Override
						public void run() {
							Location check = new Location(w, x, yy, z);
							if(AIRouter.isTrackBlock(check.getBlock().getType())){
								followRoadAndSpawnCarFromTrackerBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
							}
							return;
						}});
					
					y--;
				}
			} catch (Exception e) {
				//They just joined
			}
		}
		return;
	}
	public void doSpawns() throws Exception{
		if(!enabled){
			return;
		}
		Player[] online = new ArrayList<Player>(plugin.getServer().getOnlinePlayers()).toArray(new Player[]{});
		for(final Player player:online){
			if(main.random.nextInt(10) < 2){ //2/10 chance
				continue; //Next iteration
			}
			if(player == null || !player.isOnline()){
				continue; //Next iteration
			}
			Block tracked = null;
			boolean stopSearch = false;
			
			SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

				@Override
				public SpawnData[] execute() {
					Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
					Block br = null;
					if(main.random.nextBoolean()){
						br = b.getRelative(randomFace(), randomDir3Amount());
					}
					else {
						br = b.getRelative(randomFace(), randomDir2Amount());
					}
					World w = b.getWorld();
					int y = br.getY();
					int x = br.getX();
					int z = br.getZ();
					
					return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
				}}).executeOnce();
			SpawnData data = spawnData.getResults()[0];
			
			//Location playerLoc = data.getPlayerLoc();
			Block b = data.getB();
			Block br = data.getBr();
			final World w = data.getWorld();
			int y = data.getY();
			final int x = data.getX();
			final int z = data.getZ();
			
			int minY = y-10;
			
			tracked = AIRouter.isTrackBlock(b.getType()) ? b : null;
			tracked = AIRouter.isTrackBlock(br.getType()) ? br : null;
			
			while(tracked == null
					&& !stopSearch
					&& y>minY){
				
				Location check = new Location(w, x, y, z);
				if(AIRouter.isTrackBlock(check.getBlock().getType())){
					followRoadAndSpawnCarFromTrackerBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
				}
				
				y--;
			}
		}
		return;
	}
	
	public void doSpawns(final Player player) throws Exception{
		if(!enabled){
			return;
		}
		if(main.random.nextInt(10) < 2){ //2/10 chance
			return; //Next iteration
		}
		if(player == null || !player.isOnline()){
			return;//Next iteration
		}
		Block tracked = null;
		boolean stopSearch = false;
		
		SyncReturnTask<SpawnData> spawnData = new SyncReturnTask<SpawnData>(new ReturnTask<SpawnData>(){

			@Override
			public SpawnData[] execute() {
				Block b = player.getLocation().getBlock().getRelative(BlockFace.UP);
				Block br = null;
				if(main.random.nextBoolean()){
					br = b.getRelative(randomFace(), randomDirAmount());
				}
				else {
					br = b.getRelative(randomFace(), randomDir2Amount());
				}
				World w = b.getWorld();
				int y = br.getY();
				int x = br.getX();
				int z = br.getZ();
				
				return new SpawnData[]{new SpawnData(b, br, w, x, y, z)};
			}}).executeOnce();
		SpawnData data;
		try {
			data = spawnData.getResults()[0];
		} catch (Exception e) {
			return;
		}
		
		//Location playerLoc = data.getPlayerLoc();
		Block b = data.getB();
		Block br = data.getBr();
		final World w = data.getWorld();
		int y = data.getY();
		final int x = data.getX();
		final int z = data.getZ();
		
		int minY = y-10;
		
		tracked = AIRouter.isTrackBlock(b.getType()) ? b : null;
		tracked = AIRouter.isTrackBlock(br.getType()) ? br : null;
		
		while(tracked == null
				&& !stopSearch
				&& y>minY){
			
			final Location check = new Location(w, x, y, z);
			Scheduler.runBlockingSyncTask(new Runnable(){

				@Override
				public void run() {
					if(AIRouter.isTrackBlock(check.getBlock().getType())){
						followRoadAndSpawnCarFromTrackerBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
					}
					return;
				}}, 60);
			
			y--;
		}
		return;
	}
}
