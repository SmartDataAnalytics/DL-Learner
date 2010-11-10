package org.dllearner.utilities.examples;

import java.util.Collection;
import java.util.Random;

public class ExMakerHelper {

	
	/**
	 * bad performance don't use for large sets
	 * use collections.shuffle and remove last
	 * @param from
	 * @return
	 */
	public static String pickOneRandomly(Collection<String> from){
//		Monitor m =  JamonMonitorLogger.getTimeMonitor(ExMakerHelper.class, "bad_performance").start();
		
		if(from.isEmpty()){
			return null;
		}
		Random r = new Random();
		String[] array = from.toArray(new String[] {});
		
		int index = Math.round((float)(array.length*r.nextFloat()));
//		m.stop();
		try{
			return array[index];
		}catch (Exception e) {
			return pickOneRandomly(from);
		}
	}
}
