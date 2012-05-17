package org.dllearner.algorithms.ParCEL;

import java.util.Map;
import java.util.Map.Entry;

public class ParCELStringUtilities {
	/**
	 * =====================================================================================<br>
	 * This method is used to add padding "0" before a string so that the string has the expected
	 * length.
	 * 
	 * @param s
	 *            String that need to be padded with zero ahead "0"
	 *            
	 * @return A zero "0" padding string
	 */
	public static String zeroPad(String s, int len) {
		String result = s;
		for (int i = s.length(); i < len; i++)
			result = "0".concat(result);

		return result;
	}

	/**
	 * =====================================================================================<br>
	 * Shorten an URI by removing all bases or using its prefixes
	 * 
	 * @param uri
	 *            String need to be shortened
	 * @param baseURI
	 *            Base URI. Null if we don't want to used base uri
	 * @param prefixes
	 *            List of prefixes <prefix, string>
	 * @return
	 */
	public static String replaceString(String uri, String baseURI, Map<String, String> prefixes) {
		if (baseURI != null && uri.startsWith(baseURI)) {
			return uri.replace(baseURI, "");
		} else {
			if (prefixes != null) {
				for (Entry<String, String> prefix : prefixes.entrySet())
					uri = uri.replace(prefix.getValue(), prefix.getKey());
			}
			return uri;
		}
	}
}
