/**
 * Copyright (C) 2007, Sebastian Hellmann
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
package org.dllearner.kb.sparql.test;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;

import org.dllearner.kb.sparql.Manager;

/**
 * Test class, uses the whole thing
 * 
 * @author Sebastian Hellmann
 * 
 */
public class Test {

	public static void main(String[] args) {
		System.out.println("Start");
		// String test2 = "http://www.extraction.org/config#dbpediatest";
		// String test = "http://www.extraction.org/config#localjoseki";
		try {
			// URI u = new URI(test);
			Manager m = new Manager();
			// m.usePredefinedConfiguration(u);

			URI u2 = new URI("http://dbpedia.org/resource/Angela_Merkel");

			String filename = System.currentTimeMillis() + ".nt";
			FileWriter fw = new FileWriter(new File(filename), true);
			fw.write(m.extract(u2));
			fw.flush();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
