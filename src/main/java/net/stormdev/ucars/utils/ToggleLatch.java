package net.stormdev.ucars.utils;

public class ToggleLatch {
	private boolean locked = false;
	public ToggleLatch(){
		this.locked = false; //Make sure
	}
	
	public ToggleLatch(boolean lock){
		this.locked = lock;
	}
	
	public synchronized ToggleLatch lock(){
		this.locked = true;
		return this;
	}
	
	public synchronized ToggleLatch unlock(){
		this.locked = false;
		return this;
	}
	
	public synchronized boolean isLocked(){
		return this.locked;
	}
	
	public synchronized ToggleLatch toggle(){
		this.locked = !locked;
		return this;
	}
	
	public synchronized ToggleLatch getCloneAndThenLock(){
		ToggleLatch clone = new ToggleLatch(locked);
		this.locked = true;
		return clone;
	}
	
	public synchronized ToggleLatch getCloneAndThenUnlock(){
		ToggleLatch clone = new ToggleLatch(locked);
		this.locked = false;
		return clone;
	}
	
	public synchronized ToggleLatch getCloneAndThenToggle(){
		ToggleLatch clone = new ToggleLatch(locked);
		this.locked = !locked;
		return clone;
	}
}
