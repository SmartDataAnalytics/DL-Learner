/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
