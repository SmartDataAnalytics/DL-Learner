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
package org.dllearner.core.config;

import java.io.File;

import org.dllearner.core.ComponentManager;

/**
 * Collects information about all used configuration options and
 * writes them into a file. This way the documentation is always
 * in sync with the source code.
 * 
 * @author Jens Lehmann
 *
 */
public class ConfigDocumentationGenerator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		File file = new File("doc/configOptions.txt");
		ComponentManager cm = ComponentManager.getInstance();
		cm.writeConfigDocumentation(file);
	}

}
