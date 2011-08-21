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

package org.dllearner.utilities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.log4j.Logger;

public class URLencodeUTF8 {

	public static Logger logger = Logger.getLogger(URLencodeUTF8.class);

	public static String encode(String toEncode) {
		String retVal = "";
		try{
			retVal = URLEncoder.encode(toEncode, "UTF-8");
		}catch (UnsupportedEncodingException e) {
			logger.error("This error should never occur, check your java for UTF-8 support");
			e.printStackTrace();
		}
		return retVal;
	}
}
