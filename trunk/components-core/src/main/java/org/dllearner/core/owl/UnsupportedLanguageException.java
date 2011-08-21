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

package org.dllearner.core.owl;

/**
 * This exception is thrown if an operation does not support
 * the required language. For instance, if a description containing
 * a disjunction is passed to a method, which is only designed to
 * handle EL concepts, this exception can be thrown.
 * 
 * @author Jens Lehmann
 *
 */
public class UnsupportedLanguageException extends RuntimeException {

	private static final long serialVersionUID = -1271204878357422920L;

	public UnsupportedLanguageException(String unsupportedConstruct, String targetLanguage) {
		super("Unsupported construct \"" + unsupportedConstruct + "\". The target language is \"" + targetLanguage + "\".");
	}
	
}
