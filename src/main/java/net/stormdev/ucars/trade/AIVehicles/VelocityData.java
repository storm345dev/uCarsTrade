package net.stormdev.ucars.trade.AIVehicles;

import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class VelocityData {
	private volatile BlockFace dir;
	private volatile Vector motion;
	private volatile int updatesSinceTurn = 0;
	private volatile boolean stoppedForOtherCar = false;
	private volatile boolean inProgressOfTurningAtJunction = false;
	
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
	public boolean isInProgressOfTurningAtJunction() {
		return inProgressOfTurningAtJunction;
	}
	public void setInProgressOfTurningAtJunction(
			boolean inProgressOfTurningAtJunction) {
		this.inProgressOfTurningAtJunction = inProgressOfTurningAtJunction;
	}
	public void resetUpdatesSinceTurn(){
		this.updatesSinceTurn = 0;
	}
	public void incrementUpdatesSinceTurn(){
		this.updatesSinceTurn++;
	}
	public int getUpdatesSinceTurn() {
		return updatesSinceTurn;
	}
	public void setUpdatesSinceTurn(int updatesSinceTurn) {
		this.updatesSinceTurn = updatesSinceTurn;
	}
}
