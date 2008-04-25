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
	
	public String getAndSet(String s) {
		long now=System.currentTimeMillis();
		String ret = s+" needed "+(now-time)+" ms";
		time=now;
		return ret;
		
	}
	
	
	/**
	 * prints time needed
	 *  
	 * @param s String for printing
	 */
	public void print(String s) {
		long now=System.currentTimeMillis();
		System.out.println(s+" needed "+(now-time)+" ms");
		
	}
	
	public void setTime() {
		time=System.currentTimeMillis();
	}
	
}
