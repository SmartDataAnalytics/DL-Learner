/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.gui;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * Convenience class for choosing files from the example directory with a specific file ending.
 * 
 * @author Jens Lehmann
 *
 */
public class ExampleFileChooser extends JFileChooser {

	private static final long serialVersionUID = 1566010391199697892L;

	private final String description;
	
	// public ExampleFileChooser(final String fileEnding) {
	public ExampleFileChooser(final String ... fileEndings) {
		super(new File("examples/"));
		
		String display = "";
		for(String fileEnding : fileEndings) {
			display += "*."+ fileEnding + " ";
		}
		display += "files";
		description = display;
		
		FileFilter filter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				for(String fileEnding : fileEndings) {
					if(f.getName().toLowerCase().endsWith("." + fileEnding)) {
						return true;
					}
				}
				return false;
			}

			@Override
			public String getDescription() {
				return description;
			}
		};
		
		addChoosableFileFilter(filter);
		setFileFilter(filter);
	}
	
}
