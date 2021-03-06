package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

import net.stormdev.ucars.trade.main;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;

import com.google.common.io.Files;

public class NodesStore {
	private Map<ChunkCoord, List<Node>> nodesByActiveChunks = new HashMap<ChunkCoord, List<Node>>(); //Each chunk has a list of nodes which are within activation radius(within 5x5 chunks with this at center) of it
	private File saveFile = null;
	
	public static boolean beingModified = false;
	
	public NodesStore(File saveFile){
		this.saveFile = saveFile;
		load();
	}

	public List<Node> getAllNodes(){
		List<Node> results = new ArrayList<>();
		for(List<Node> nodes:nodesByActiveChunks.values()){
			results.addAll(nodes);
		}
		return Collections.unmodifiableList(results);
	}
	
	public int getNodeCount(){
		if(beingModified){
			synchronized(this){
				return nodeCount();
			}
		}
		return nodeCount();
	}
	
	private int nodeCount(){
		//Have to actually count nodes...
				List<Node> counted = new ArrayList<Node>();
				for(ChunkCoord chunkCoord:new ArrayList<ChunkCoord>(nodesByActiveChunks.keySet())){
					List<Node> nodes = nodesByActiveChunks.get(chunkCoord);
					for(Node n:nodes){
						boolean alreadyCounted = false;
						for(Node countedNode:counted){
							if(n.equals(countedNode)){
								alreadyCounted = true;
								break;
							}
						}
						if(!alreadyCounted){
							counted.add(n);
						}
					}
				}
				return counted.size();
	}
	
	public void removeNode(Node node){
		if(beingModified){
			synchronized(this){
				removeANode(node);
				return;
			}
		}
		removeANode(node);
	}
	
	private void removeANode(Node node){
		for(ChunkCoord key: new ArrayList<ChunkCoord>(nodesByActiveChunks.keySet())){
			List<Node> nodes = nodesByActiveChunks.get(key);
			if(node != null && nodes.contains(node)){
				nodes.remove(node);
			}
		}
		asyncSave();
	}
	
	public void revalidateNodes(){
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				revalidateNodesNow();
				return;
			}});
	}
	
	public synchronized void resetNodes(){
		//Copy the current nodes in case this was an error
		File backup = new File(saveFile.getParentFile()+File.separator+"oldNodes.nodelist");
		if(!backup.exists()){
			backup.getParentFile().mkdirs();
			try {
				backup.createNewFile();
			} catch (IOException e) {
				//oh well... they don't get a backup...
				e.printStackTrace();
			}
		}
		try {
			if(saveFile.exists()){
				Files.copy(saveFile, backup);
				saveFile.delete();
			}
		} catch (IOException e) {
			//oh well... they don't get a backup...
			e.printStackTrace();
		}
		nodesByActiveChunks.clear();
		asyncSave();
	}
	
	public void revalidateNodesNow(){ //Called 99% of the time async
		main.plugin.getLogger().info("Revalidating ALL AI spawn nodes; this could take some time!");
		int nodesRemoved = 0;
		for(ChunkCoord coord:new ArrayList<ChunkCoord>(nodesByActiveChunks.keySet())){
			List<Node> chunkNodes = nodesByActiveChunks.get(coord);
			for(final Node n:new ArrayList<Node>(chunkNodes)){
				boolean valid = true;
				if(!Bukkit.isPrimaryThread()){
					Future<Boolean> isValid = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<Boolean>(){

						@Override
						public Boolean call() throws Exception {
							return n.isValid();
						}});
					try {
						valid = isValid.get();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				else { //In main thread
					valid = n.isValid();
				}
				
				if(!valid){
					nodesRemoved++;
					chunkNodes.remove(n);
				}
			}
			nodesByActiveChunks.put(coord, chunkNodes);
			
			Thread.yield();
			try {
				Thread.sleep(50); //Take a break so we're not just constantly spamming the main server thread asking it to validate nodes
			} catch (InterruptedException e) {
			} 
		}
		if(nodesRemoved > 0){
			saveNow();
		}
		
		main.plugin.getLogger().info("Successfully revalidated all AI spawn nodes - "+nodesRemoved+" invalid nodes were purged!");
	}
	
	public Node getRandomActiveNode(Entity entity, float minDistance, float maxDistance){
		return getRandomActiveNode(entity.getLocation(), minDistance, maxDistance);
	}
	
	public Node getRandomActiveNode(Location location, float minDistance, float maxDistance){
		List<Node> avail = getActiveNodes(location, minDistance, maxDistance);
		if(avail.size() < 1){
			return null;
		}
		return avail.get(main.random.nextInt(avail.size()));
	}
	
	public List<Node> getActiveNodes(Entity entity, float minDistance, float maxDistance){
		return getActiveNodes(entity.getLocation(), minDistance, maxDistance);
	}
	
	public List<Node> getActiveNodes(Location location, float minDistance, float maxDistance){
		List<Node> active = getActiveNodes(location);
		List<Node> res = new ArrayList<Node>();
		double minSquared = Math.pow(minDistance, 2);
		double maxSquared = Math.pow(maxDistance, 2);
		
		for(Node n:active){
			Location nl = n.getLocation();
			if(!nl.getWorld().equals(location.getWorld())){
				continue;
			}
			double distanceSquared = nl.distanceSquared(location);
			if(distanceSquared < maxSquared && distanceSquared > minSquared){
				res.add(n);
			}
		}
		return res;
	}
	
	public List<Node> getActiveNodes(Entity entity){
		return getActiveNodes(entity.getLocation());
	}
	
	public List<Node> getActiveNodes(final Location location){
		int cx = location.getBlockX() >> 4;
		int cz = location.getBlockZ() >> 4;
		ChunkCoord cord = new ChunkCoord(location.getWorld(), cx, cz);
		List<Node> res = getActiveNodes(cord);
		if(res == null){
			return new ArrayList<Node>();
		}
		return res;
		/*Chunk chunk = null;
		if(Bukkit.isPrimaryThread()){
			chunk = location.getChunk();
		}
		else {
			Future<Chunk> getChunk = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<Chunk>(){

				@Override
				public Chunk call() throws Exception {
					return location.getChunk();
				}});
			try {
				chunk = getChunk.get();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if(chunk == null){
			return new ArrayList<Node>();
		}
		return getActiveNodes(chunk);*/
	}
	
	public List<Node> getActiveNodes(Chunk chunk){
		return getActiveNodes(new ChunkCoord(chunk));
	}
	
	public List<Node> getActiveNodes(ChunkCoord coord){
		if(beingModified){
			synchronized(this){
				return getChunkActiveNodes(coord);
			}
		}
		return getChunkActiveNodes(coord);
	}
	
	private List<Node> getChunkActiveNodes(ChunkCoord coord){
		List<Node> chunkNodes = nodesByActiveChunks.get(coord);/*new ArrayList<Node>();
		for(ChunkCoord key:new ArrayList<ChunkCoord>(new HashMap<ChunkCoord, List<Node>>(nodesByActiveChunks).keySet())){
			if(key.isEqualTo(coord)){
				chunkNodes = nodesByActiveChunks.get(key);
			}
		}*/
		return chunkNodes;
	}
	
	public synchronized void setNodeIntoCorrectActiveChunks(final Node node){ //Also works to update the node
		beingModified = true;
		try {
			Chunk nodeChunk = null;
			World nodeWorld = null;
			if(Bukkit.isPrimaryThread()){
				nodeChunk = node.getChunk();
				nodeWorld = nodeChunk.getWorld();
			}
			else {
				try {
					Future<Chunk> getChunk = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<Chunk>(){

						@Override
						public Chunk call() {
							return node.getChunk();
						}});
					nodeChunk = getChunk.get();
					final Chunk nc = nodeChunk;
					Future<World> getWorld = Bukkit.getScheduler().callSyncMethod(main.plugin, new Callable<World>(){

						@Override
						public World call() {
							return nc.getWorld();
						}});
					nodeWorld = getWorld.get();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			int chunkX = nodeChunk.getX();
			int chunkZ = nodeChunk.getZ();
			
			//Generate the 6x6 grid with this chunk at the center and then add the node to the list; if it isn't already (NO duplicate nodes)
			for(int x = chunkX -6;x<=chunkX+7;x++){
				for(int z = chunkZ -6;z<=chunkZ+7;z++){
					ChunkCoord coord = new ChunkCoord(nodeWorld, x, z);
					
					List<Node> chunkNodes = new ArrayList<Node>();
					for(ChunkCoord key:new ArrayList<ChunkCoord>(nodesByActiveChunks.keySet())){
						if(key.isEqualTo(coord)){
							chunkNodes = nodesByActiveChunks.get(key);
						}
					}
					
					boolean alreadySet = false;
					for(Node n:new ArrayList<Node>(chunkNodes)){
						if(!n.isEqualTo(node)){
							continue;
						}
						if(n.equals(node)){ //Node already set and java objects are equal
							alreadySet = true;
							break;
						}
						chunkNodes.remove(n); //Node on list is the same place, but different java object; needs updating
					}
					
					if(alreadySet){
						continue; //Move onto next chunk
					}
					
					chunkNodes.add(node);
					nodesByActiveChunks.put(coord, chunkNodes);
				}
			}
		} finally {
			beingModified = false;
		}
	}
	
	public synchronized void saveNow(){ //synchronized so nodesByActiveChunks not modified whilst saving
		try {
			if(!this.saveFile.exists()){
				saveFile.getParentFile().mkdirs();
				saveFile.createNewFile();
			}
			
			ObjectOutputStream oos = new ObjectOutputStream(new DataOutputStream(new FileOutputStream(saveFile)));
			try {
				oos.writeObject(nodesByActiveChunks);
			}
			finally {
				oos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void asyncSave(){
		Bukkit.getScheduler().runTaskAsynchronously(main.plugin, new Runnable(){

			@Override
			public void run() {
				saveNow();
				return;
			}});
	}
	
	@SuppressWarnings("unchecked")
	public synchronized void load(){
		main.plugin.getLogger().info("Loading existing AI spawn nodes...");
		
		if(!saveFile.exists() || saveFile.length() < 1){
			return;
		}
		
		try {
			ObjectInputStream ois = new ObjectInputStream(new DataInputStream(new FileInputStream(saveFile)));
			try {
				Object in = ois.readObject();
				if(in != null && in instanceof Map<?,?>){
					nodesByActiveChunks = (Map<ChunkCoord, List<Node>>) in;
				}
			} finally {
				ois.close();
			}
		} catch (FileNotFoundException e) {}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		main.plugin.getLogger().info("Nodes loaded!");
	}
}
