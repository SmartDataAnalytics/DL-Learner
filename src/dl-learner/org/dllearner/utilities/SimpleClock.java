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
		
		System.out.println("needed "+getTime()+" ms");
		setTime();
	}
	
	
	/**
	 * prints time needed
	 *  and resets the clock 
	 * @param s String for printing
	 */
	public void printAndSet(String s) {
		
		System.out.println(s+" needed "+getTime()+" ms");
		setTime();
	}
	
	public String getAndSet(String s) {
		
		String ret = s+" needed "+getTime()+" ms";
		setTime();
		return ret;
		
	}
	
	
	/**
	 * prints time needed
	 *  
	 * @param s String for printing
	 */
	public void print(String s) {
		
		System.out.println(s+" needed "+getTime()+" ms");
		
	}
	
	public void setTime() {
		time=System.currentTimeMillis();
	}
	public long getTime() {
		long now=System.currentTimeMillis();
		return now-time;
	}
	
}
