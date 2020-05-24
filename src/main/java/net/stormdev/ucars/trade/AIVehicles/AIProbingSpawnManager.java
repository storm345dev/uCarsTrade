package net.stormdev.ucars.trade.AIVehicles;


public class AIProbingSpawnManager {
/*	private main plugin;
	public static boolean enabled;
	private boolean fullEnable;
	private static Material roadEdge;
	private Material junction;
	private BukkitTask task = null;
	private static long spawnRate = 20l;
	private List<String> aiNames;
	private static int cap = 30;
	private static int liveCap = 5;
	private static int spawnedCount = 0;
	
	public static void decrementSpawnedCount(){
		spawnedCount--;
		if(spawnedCount < 0){
			spawnedCount = 0;
		}
	}
	
	public static void incrementSpawnedCount(){
		spawnedCount++;
	}
	
	public static int getCurrentSpawnedCount(){
		return spawnedCount;
	}
	
	public static int getLiveCap(){
		return liveCap;
	}
	
	public static void setLiveCap(int cap){
		liveCap = cap;
	}
	
	public boolean NPCsEnabled(){
		return fullEnable;
	}
	
	public boolean NPCsCurrentlyEnabled(){
		return enabled;
	}
	
	public synchronized void setNPCsCurrentlyEnabled(boolean on){
		AIProbingSpawnManager.enabled = on;
	}
	
	private String randomCarName(){
		DrivenCar car = CarGenerator.gen();
		return car.getName();
	}
	
	public AIProbingSpawnManager(main plugin, boolean enabled){
		this.plugin = plugin;
		AIProbingSpawnManager.enabled = enabled;
		this.fullEnable = enabled;
		String edgeRaw = main.config.getString("general.ai.roadEdgeBlock");
		String junRaw = main.config.getString("general.ai.junctionBlock");
		aiNames = main.config.getStringList("general.ai.names");
		cap = main.config.getInt("general.ai.limit");
		
		liveCap = 5;
		// A task to dynamically change liveCap to match the server's current AI holdings
		Bukkit.getScheduler().runTaskTimerAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				// Use resource score to get live cap
				int score = DynamicLagReducer.getResourceScore();
				int newCap = liveCap;
				double tps = DynamicLagReducer.getTPS();
				if(score > 75 && tps > 19.4){
					newCap++;
				}
				else if(score < 70 || tps < 17.5){
					newCap *= 0.5; //Half
				}
				else if(score < 70 || tps < 18){
					newCap -= 10;
				}
				else if(score < 70 || tps < 19){
					newCap -= 5;
				}
				else if(score < 75 || tps < 19){
					newCap--;
				}
				
				if(newCap != liveCap){
					if(newCap > cap){
						newCap = cap;
					}
					if(newCap < 2){
						newCap = 2; //Min of 2
					}
					liveCap = newCap;
				}
				
				return;
			}}, 100l, 100l); //Every 5s
		Bukkit.getScheduler().runTaskTimerAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				int aiCars = 0;
				for(World world:new ArrayList<World>(Bukkit.getWorlds())){
					for(Entity e:new ArrayList<Entity>(world.getEntities())){
						if(!(e instanceof Minecart)){
							continue;
						}
						Minecart cart = (Minecart) e;
						if(!cart.hasMetadata("trade.npc")){
							continue;
						}
						final DrivenCar c = main.plugin.carSaver.getCarInUse(cart.getUniqueId());
						if(c == null){
							continue;
						}
						
						if(!c.isNPC()){
							continue;
						}
						aiCars++;
					}
				}
				spawnedCount = aiCars;
				return;
			}}, 10*60*20l, 10*60*20l); //Every 10 mins
		
		new DynamicLagReducer().start();
		
		roadEdge = Material.getMaterial(edgeRaw);
		junction = Material.getMaterial(junRaw);
		if(roadEdge == null || junction == null){
			main.logger.info("Didn't enable AIs as configuration is invalid!");
			enabled = false;
		}
		if(enabled){
			main.plugin.getLogger().info("AI Cars enabled successfully!");
			task = main.plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new Runnable(){

				public void run() {
					if(spawnedCount >= liveCap || spawnedCount >= cap){
						return;
					}
					boolean longSpawns = main.random.nextBoolean();
					boolean doubleSpawns = longSpawns && main.random.nextBoolean();
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
	}
	public void end(){
		if(task != null){
			task.cancel();
		}
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
							spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
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
								spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
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
					spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
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
				
				return new SpawnData[]{new (b, br, w, x, y, z)};
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
						spawnFromTrackBlock(check, ClosestFace.getClosestFace(player.getLocation().getYaw()));
					}
					return;
				}}, 60);
			
			y--;
		}
		return;
	}
	
	public void spawnFromTrackBlock(Location location, BlockFace currentDir){
		//Track road and spawn in an AI
		Block current = location.getBlock();
		int distance = randomDistanceAmount();
		while(distance > 0){
			//Need to follow the road
			TrackingData data = AITrackFollow.nextBlock(current, currentDir, junction, null);
			
			current = data.nextBlock;
			currentDir = data.dir;
			distance--;
		}
		//Current is the track block
		Block toSpawn = current.getRelative(BlockFace.UP);
		while(AIRouter.isTrackBlock(toSpawn.getType()) && toSpawn.getY() <= 256){ //Height limit
			toSpawn = toSpawn.getRelative(BlockFace.UP);
		}
		//toSpawn is the road surface
		Location spawnLoc = toSpawn.getLocation().add(0, 1.5, 0); //Position to spawn car
		if(!spawnLoc.getBlock().isEmpty()){
			spawnLoc = spawnLoc.add(0, 0.6, 0);
			if(!spawnLoc.getBlock().isEmpty()){
				//Must spawn car in an air block
				return;
			}
		}
		BlockFace carDirection = currentDir;
		carDirection = carriagewayDirection(current);
		if(carDirection == null){
			//Not a valid road structure
			return;
		}
		spawnNPCCar(spawnLoc, carDirection);
	}
	
	public void spawnNPCCar(Location spawn, final BlockFace currentDirection){
		spawn = spawn.add(0.5, 0, 0.5);
		final Location spawnLoc = spawn;
		plugin.getServer().getScheduler().runTask(plugin, new Runnable(){

			public void run() {
				final Minecart m = (Minecart) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.MINECART);
				List<Entity> es = m.getNearbyEntities(2, 2, 2);
				for(Entity e:es){
					if(e.getType() == EntityType.MINECART){
						m.remove();
						return; //Already a car in close proximity
					}
				}
				List<Entity> nearby = new ArrayList<Entity>(m.getNearbyEntities(20, 3, 20));
				for(Entity e:new ArrayList<Entity>(nearby)){
					if(e.getType() != EntityType.MINECART){
						nearby.remove(e);
					}
				}
				if(nearby.size() > 2){
					//Too many in area
					m.remove();
					return;
				}
				//It's valid
				Villager v = (Villager) spawnLoc.getWorld().spawnEntity(spawnLoc, EntityType.VILLAGER);
				v.setAdult();
				v.setBreed(false);
				v.setAgeLock(true);
				v.setCanPickupItems(false);
				v.setCustomName(randomName());
				v.setCustomNameVisible(true);
				m.setPassenger(v);
				
				DrivenCar c = CarGenerator.gen().setNPC(true);
				//Make it a car
				c.setId(m.getUniqueId());
				plugin.carSaver.carNowInUse(c);
				m.setMetadata("trade.npc", new StatValue(new VelocityData(currentDirection, null), plugin));
				incrementSpawnedCount();
				return;
			}});
		return;
	}
	
	public int randomDistanceAmount(){
		return main.random.nextInt(25-10)+10; //Between 10 and 30
	}
	
	public int randomDirAmount(){
		return main.random.nextInt(5)+1; //1 to 5
	}
	
	public int randomDir2Amount(){
		return main.random.nextInt(10-5)+5; //5 to 10
	}
	
	public int randomDir3Amount(){
		return main.random.nextInt(30-20)+20; //30 to 20
	}
	
	public BlockFace randomFace(){
		//N, NE, E, SE, S, SW, W, NW (8)
		int rand = main.random.nextInt(8);
		switch(rand){
		case 0:return BlockFace.NORTH;
		case 1:return BlockFace.NORTH_EAST;
		case 2:return BlockFace.EAST;
		case 3:return BlockFace.SOUTH_EAST;
		case 4:return BlockFace.SOUTH;
		case 5:return BlockFace.SOUTH_WEST;
		case 6:return BlockFace.WEST;
		case 7:return BlockFace.NORTH_WEST;
		default: return BlockFace.NORTH;
		}
	}
	public String randomName(){
		if(aiNames == null){
			return "Citizen";
		}
		return aiNames.get(main.random.nextInt(aiNames.size())); //Select a random name
	}*/
}
