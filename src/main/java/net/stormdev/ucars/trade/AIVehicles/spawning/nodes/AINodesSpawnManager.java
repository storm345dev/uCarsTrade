package net.stormdev.ucars.trade.AIVehicles.spawning.nodes;

import java.io.File;

import net.stormdev.ucars.trade.main;
import net.stormdev.ucars.trade.AIVehicles.spawning.AbstractAISpawnManager;

public class AINodesSpawnManager extends AbstractAISpawnManager {

	private NodesStore nodes = null;
	
	public AINodesSpawnManager(main plugin, boolean enabled, File nodesSaveFile) {
		super(plugin, enabled);
		this.nodes = new NodesStore(nodesSaveFile);
	}
	
	public NodesStore getNodesStore(){
		return this.nodes;
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void initSpawnTask() {
		// TODO Auto-generated method stub
		
	}

}
