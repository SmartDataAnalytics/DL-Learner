package org.dllearner.utilities;

/**
 * class for counting time and output it
 *
 */
public class SimpleClock {
	private long time;
	
	public SimpleClock() {
		time=System.currentTimeMillis();
	}
	
	/**
	 *  prints time needed
	 *  and resets the clock 
	 */
	public void printAndSet() {
		long now=System.currentTimeMillis();
		System.out.println("needed "+(now-time)+" ms");
		time=now;
	}
	
	
	/**
	 * prints time needed
	 *  and resets the clock 
	 * @param s String for printing
	 */
	public void printAndSet(String s) {
		long now=System.currentTimeMillis();
		System.out.println(s+" needed "+(now-time)+" ms");
		time=now;
	}
	
	public void setTime() {
		time=System.currentTimeMillis();
	}
	
}
