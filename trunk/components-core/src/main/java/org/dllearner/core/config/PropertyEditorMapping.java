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
package org.dllearner.core.config;

import java.beans.PropertyEditor;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.propertyeditors.URLEditor;

/**
 * This class declares the mapping between property editors and the types they should 
 * edit in DL-Learner.
 * 
 * @author Jens Lehmann
 *
 */
public final class PropertyEditorMapping {

	public static final Map<Class<?>,Class<? extends PropertyEditor>> map = new HashMap<Class<?>,Class<? extends PropertyEditor>>();
	static {
		map.put(Boolean.class, BooleanEditor.class);
		map.put(Integer.class, IntegerEditor.class);
		map.put(URL.class, URLEditor.class);
	}
	
}
