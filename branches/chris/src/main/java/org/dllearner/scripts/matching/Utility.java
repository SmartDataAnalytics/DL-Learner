/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
package org.dllearner.scripts.matching;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DataFormatException;

import org.apache.log4j.Logger;

/**
 * Utility methods for creating/optimising matches between knowledge bases.
 * 
 * @author Jens Lehmann
 *
 */
public final class Utility {
	
	private static Logger logger = Logger.getLogger(Utility.class);
	
	/**
	 * Reads in a file containing known matches between knowledge bases.
	 * 
	 * Note: 
	 * - Currently, it is assumed that the format is URI + tabulator + URI + newline.
	 * - Currently, #id is appended to the second URI on the line.
	 * - Currently, the order is reversed. 
	 * 
	 * @param file A file, which contains matches.
	 * @return A map containing the matches.
	 * @throws IOException If an error occurs during reading the file.
	 */
	public static Map<URI, URI> getMatches(File file) throws IOException, DataFormatException {
		Map<URI,URI> matches = new HashMap<URI,URI>();
		// read file line by line to collect matches
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line;
		int lineCount = 0;
		while ((line = br.readLine()) != null) {
			String[] tmp = line.split("\t");
			if(tmp.length != 2) {
				throw new DataFormatException("Line " + lineCount + " incorrectly formatted.");
			}
//			if(matches.containsKey(URI.create(tmp[1]))) {
//				logger.warn("multiple key " + tmp[1] + " ... ignoring all but one value");
//			}
			matches.put(URI.create(tmp[1]), URI.create(tmp[0] + "#id"));
			lineCount++;
		}	
		logger.info("read " + lineCount + " lines");
		return matches;
	}
	
}
