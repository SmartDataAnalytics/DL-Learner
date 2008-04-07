package org.dllearner.utilities;

public class SimpleClock {
	private long time;
	
	public SimpleClock() {
		time=System.currentTimeMillis();
	}
	
	public void printAndSet() {
		long now=System.currentTimeMillis();
		System.out.println("needed "+(now-time)+" ms");
		time=now;
	}
	
	public void setTime() {
		time=System.currentTimeMillis();
	}
	
}
