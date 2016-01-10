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
