/**
 * Copyright (C) 2007, Jens Lehmann
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
 *
 */
package org.dllearner.core;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

/**
 * Central manager class for DL-Learner.
 * 
 * @author Jens Lehmann
 *
 */
public class ComponentManager {

	private static String componentsFile = "lib/components.ini";
	
	private static ComponentManager cm = new ComponentManager();
	
	private ComponentManager() {
		
	}
	
	public static ComponentManager getInstance() {
		return cm;
	}
	
	private static List<String> readComponentsFile() {
		List<String> componentStrings = new LinkedList<String>();
		
		try {
			FileInputStream fstream = new FileInputStream(componentsFile);

			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;

			while ((line = br.readLine()) != null) {
				if(!(line.startsWith("#") || line.startsWith("//") || line.startsWith("%")))
					componentStrings.add(line);
			}
			
			in.close();			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return componentStrings;
	}
	
	public LearningProblemNew learningProblem(Class<LearningProblemNew> lp, ReasonerComponent reasoner) {
		try {
			Constructor<LearningProblemNew> constructor = lp.getConstructor(ReasonerComponent.class);
			return constructor.newInstance(reasoner);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
}
