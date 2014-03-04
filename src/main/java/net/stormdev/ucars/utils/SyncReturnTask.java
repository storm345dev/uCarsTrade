package net.stormdev.ucars.utils;


public class SyncReturnTask<T> {
	private T[] result = null;
	private ReturnTask<T> task;
	private boolean executed = false;
	
	public SyncReturnTask(ReturnTask<T> task){
		this.task = task;
		this.executed = false;
	}
	
	public synchronized boolean hasExecuted(){
		return this.executed;
	}
	
	public synchronized boolean hasResults(){
		return result != null;
	}
	
	public synchronized T[] getResults(){
		return result;
	}
	
	public synchronized SyncReturnTask<T> executeOnce() throws Exception{
		if(hasExecuted()){
			return this;
		}
		
		execute();
		return this;
	}
	
	public synchronized SyncReturnTask<T> executeOnce(int timeout) throws Exception{
		if(hasExecuted()){
			return this;
		}
		
		execute(timeout);
		return this;
	}
	
	public synchronized SyncReturnTask<T> executeOnceNoTimeout() throws Exception{
		if(hasExecuted()){
			return this;
		}
		
		executeNoTimeout();
		return this;
	}
	
	public synchronized SyncReturnTask<T> execute() throws Exception{
		Scheduler.runBlockingSyncTask(new Runnable(){

			@Override
			public void run() {
				result = task.execute();
				return;
			}});
		
		this.executed = true;
		return this;
	}
	
	public synchronized SyncReturnTask<T> execute(int timeout) throws Exception{
		Scheduler.runBlockingSyncTask(new Runnable(){

			@Override
			public void run() {
				result = task.execute();
				return;
			}}, timeout);
		
		this.executed = true;
		return this;
	}
	
	public synchronized SyncReturnTask<T> executeNoTimeout() throws Exception{
		Scheduler.runBlockingSyncTaskNoTimeout(new Runnable(){

			@Override
			public void run() {
				result = task.execute();
				return;
			}});
		
		this.executed = true;
		return this;
	}
}
