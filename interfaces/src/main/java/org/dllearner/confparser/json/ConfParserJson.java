package org.dllearner.confparser.json;

import org.dllearner.cli.ConfFileOption;
import org.dllearner.confparser.AbstractConfParser;
import org.dllearner.core.ComponentInitException;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Convert Json Config to DL-Learner config
 */
public class ConfParserJson extends AbstractConfParser {
	final static Logger logger = LoggerFactory.getLogger(ConfParserJson.class);
	private InputStream inputStream;
	private StringBuffer out;
	private Map jsonMap;

	public ConfParserJson(InputStream inputStream) {
		this.inputStream = inputStream;
		this.jsonMap = null;
	}

	public ConfParserJson(Map jsonMap) {
		this.inputStream = null;
		this.jsonMap = jsonMap;
	}

	private void resolveBeanRef(ConfFileOption o, Object value) throws ComponentInitException {
		o.setPropertyType(value.getClass());
		String jsonString = JSONValue.toJSONString(value);
		if (value != null && !"null".equals(jsonString))
			o.setPropertyValue(value instanceof String ? (String) value : jsonString);

		if (value instanceof String && ((String) value).startsWith("#")) {
			o.setBeanRef(true);
			o.setBeanReferenceCollection(false);
			o.setValueObject(((String) value).substring(1));
		} else {
			Object vo = value;
			boolean refCollection = false;
			if (value instanceof Collection) {
				if (!((Collection) value).isEmpty()) {
					Object first = ((Collection) value).iterator().next();
					if (first instanceof String && ((String) first).startsWith("#")) {
						o.setBeanReferenceCollection(true);
						Collection v1new;
						try {
							v1new = (Collection) value.getClass().newInstance();
						} catch (InstantiationException e) {
							throw new ComponentInitException(e);
						} catch (IllegalAccessException e) {
							throw new ComponentInitException(e);
						}
						for (Object o1 : (Collection) value) {
							if (!"#".equals(o1)) {
								if (o1 instanceof String && ((String) o1).startsWith("#"))
									v1new.add(((String) o1).substring(1));
								else
									v1new.add(o1);
							}
						}
						vo = v1new;
						refCollection = true;
					}
				}
			}
			o.setBeanRef(false);
			o.setBeanReferenceCollection(refCollection);
			o.setValueObject(vo);
		}
	}

	public void init() throws ComponentInitException {
		if (isInitialized()) return;

		if (jsonMap == null) {
			jsonMap = parseJson();
		}
		convertMap();
		postProcess();

		this.initialized = true;
	}

	@NotNull
	protected Map parseJson() throws ComponentInitException {
		JSONObject ret = new JSONObject();
		JSONParser parser = new JSONParser();

		Object parse = null;
		try {
			parse = parser.parse(new InputStreamReader(inputStream), new InsertionOrderedContainerFactory());
		} catch (org.json.simple.parser.ParseException e) {
			throw new ComponentInitException(e.toString(), e);
		} catch (IOException e) {
			throw new ComponentInitException(e);
		}
		logger.trace("parse was: " + parse.toString());
		if (!(parse instanceof Map)) {
			throw new ComponentInitException("Not a JSON object: " + parse.getClass());
		}
		return (Map) parse;
	}

	public void convertMap() throws ComponentInitException {
		for (Map.Entry e1 : (Set<Map.Entry>) jsonMap.entrySet()) {
			Object v1 = e1.getValue();
			final String k1 = (String) e1.getKey();
			if (v1 instanceof Map && ((Map) v1).containsKey("type")) {
				for (Map.Entry e2 : (Set<Map.Entry>) ((Map) v1).entrySet()) {
					final String k2 = (String) e2.getKey();
					if ("comment".equalsIgnoreCase(k2)) continue;
					ConfFileOption o = new ConfFileOption();
					o.setBeanName(k1);
					o.setPropertyName(k2);
					resolveBeanRef(o, e2.getValue());
					addConfOption(o);
				}
			} else {
				ConfFileOption o = new ConfFileOption();
				o.setBeanName(k1);
				resolveBeanRef(o, v1);
				addConfOption(o);
			}
		}
	}
}
