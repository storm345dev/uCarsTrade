package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class VelocityData {
	private BlockFace dir;
	private Vector motion;
	private volatile boolean stoppedForOtherCar = false;
	
	public VelocityData(BlockFace dir, Vector motion){
		this.setDir(dir);
		this.setMotion(motion);
	}
	public BlockFace getDir() {
		return dir;
	}
	public void setDir(BlockFace dir) {
		this.dir = dir;
	}
	public Vector getMotion() {
		return motion.clone();
	}
	public boolean hasMotion(){
		return motion != null;
	}
	public void setMotion(Vector motion) {
		this.motion = motion;
	}
	public boolean isStoppedForOtherCar() {
		return stoppedForOtherCar;
	}
	public void setStoppedForOtherCar(boolean stoppedForOtherCar) {
		this.stoppedForOtherCar = stoppedForOtherCar;
	}
}
