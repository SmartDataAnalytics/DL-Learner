package org.dllearner.test;

import com.google.common.collect.Sets;
import javassist.ClassPool;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.annotations.OutVariable;
import org.dllearner.core.annotations.Unused;
import org.dllearner.core.config.ConfigOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/* (no java-doc)
 * Internal tool to help find undocumented config options
 */
public class UndocumentedOptionScanner {

	private static Logger logger = LoggerFactory.getLogger(UndocumentedOptionScanner.class);
	private static AnnComponentManager cm = AnnComponentManager.getInstance();
	private static Class<?> currentClaz;
	private static boolean loggedCurrentClaz;

	private static void logClass() {
		if (!loggedCurrentClaz) {
			logger.info("\n@" + currentClaz.getCanonicalName());
			loggedCurrentClaz = true;
		}
	}
	private static void startClass(Class<?> clazz) {
		currentClaz = clazz;
		loggedCurrentClaz = false;
	}
	public static void main(String[] args) {
		ClassPool cp = ClassPool.getDefault();
		for (Class<? extends Component> c : cm.getComponents()) {
			startClass(c);
			Map<String, List<Field>> fields = new TreeMap<>();
			Map<String, List<Method>> methods = new TreeMap<>();
			for (Method m : c.getMethods()) {
				String name = m.getName();
				List<Method> set = methods.get(name);
				if (set == null) {
					set = new LinkedList<>();
					methods.put(name, set);
				}
				set.add(m);
			}
			for (
					Class<?> cc = c;
					cc != null;
					cc = cc.getSuperclass()
							) {
				for (Field f : cc.getDeclaredFields()) {
					String name = f.getName();
					List<Field> set = fields.get(name);
					if (set == null) {
						set = new LinkedList<>();
						fields.put(name, set);
					}
					set.add(f);
				}
			}
			
			Set<String> hasDoc = new TreeSet<>();
			Set<String> noDoc = new TreeSet<>();
			Set<String> notConfigSet = new TreeSet<>();
			for (Entry<String, List<Field>> f : fields.entrySet()) {
				boolean isDocumented = false;
				boolean notConfig = false;
				for (Field fs : f.getValue()) {
					isDocumented = isDocumented || fs.isAnnotationPresent(ConfigOption.class);
					notConfig = notConfig || fs.isAnnotationPresent(Unused.class)
							|| fs.isAnnotationPresent(OutVariable.class)
							|| fs.isAnnotationPresent(NoConfigOption.class);
				}
				if (isDocumented) {
					hasDoc.add(AnnComponentManager.getName(f.getValue().get(0)));
				} else if (notConfig) {
					notConfigSet.add(f.getKey());
				} else {
					noDoc.add(f.getKey());
				}
			}
			Set<String> hasSetter = new TreeSet<>();
			Set<String> noCO = new TreeSet<>();
			for (Entry<String, List<Method>> m : methods.entrySet()) {
				String cn = m.getKey();
				if (cn.startsWith("set")) {
					String optionName = cn.substring(3, 4).toLowerCase() + cn.substring(4);
					if (cn.substring(4).equals(cn.substring(4).toUpperCase())) { optionName = optionName.toLowerCase(); }
					if (hasDoc.contains(optionName)) {
						hasSetter.add(optionName);
						
					} else if (noDoc.contains(optionName)) {
						Class<?> claz = fields.get(optionName).get(0).getDeclaringClass();
						String fileName = getFilename(claz);
						File file = findSource(claz);
						int foundLine = findFieldLine(file, optionName);
						logClass();
						logger.warn("setter+var but no @configOption . " + optionName + "(" + claz.getSimpleName() + ".java:"+foundLine+")");
						noCO.add(optionName);

					} else {
						boolean notConfig = false;
						for (Method ms : m.getValue()) {
							notConfig = notConfig
									|| ms.isAnnotationPresent(Deprecated.class)
									|| ms.isAnnotationPresent(NoConfigOption.class);
						}
						if (!notConfig && !notConfigSet.contains(optionName)) {
							Method m0 = m.getValue().get(0);
							Class<?> claz = m0.getDeclaringClass();
							String fileName = getFilename(claz);
							File file = findSource(claz);
							int foundLine = findFieldLine(file, m0.getName());
							logClass();
							logger.info("setter without var . "+optionName + "(" + claz.getSimpleName() + ".java:"+foundLine+")");
						}
					}
				}
			}
			for (String noSetter : Sets.difference(hasDoc, hasSetter)) {
				Class<?> claz = fields.get(noSetter).get(0).getDeclaringClass();
				String fileName = getFilename(claz);
				File file = findSource(claz);
				int foundLine = findFieldLine(file, noSetter);
				logClass();
				logger.warn("option without setter! . " +noSetter + "(" + claz.getSimpleName() + ".java:"+foundLine+")");
			}
		}
	}

	private static String getFilename(Class<?> clazz) {
		return clazz.getCanonicalName().replaceAll("\\.", "/") + ".java";
	}
	private static File findSource(Class<?> clazz) {
		File file = null;
		String fileName = getFilename(clazz);
		URL[] urls = Stream.of(System.getProperty("java.class.path").split(File.pathSeparator))
				.map(entry -> {
					try {
						return new File(entry).toURI().toURL();
					} catch (MalformedURLException e) {
						throw new IllegalArgumentException("URL could not be created from '" + entry + "'", e);
					}
				})
				.toArray(URL[]::new);
		//URLClassLoader currentClassLoader = ((URLClassLoader) (Thread.currentThread().getContextClassLoader()));
		List<URL> sourceCP = Arrays.stream(urls).filter(url -> !url.getPath().endsWith(".jar")).collect(Collectors.toList());
		for (URL u : sourceCP) {
			File tFile = new File(u.getFile() + "/../../src/main/java/" + fileName);
			if (tFile.isFile()) {
				file = tFile;
				break;
			}
		}
		return file;
	}

	private static int findFieldLine(File file, String optionName) {
		int foundLine = 0;
		if (file != null) try {
			int lineno = 0;
			boolean inComment = false;
			for (String line : Files.readAllLines(file.toPath())) {
				lineno++;
				if (line.matches(".*/\\*.*") && !line.matches(".*\\*/.*")) {
					inComment = true;
				}
				if (!inComment && !line.matches("\\s*//.*") && line.matches("(\\w|\\s|[><,])*(\\w|[><,])+\\s" + Pattern.quote(optionName) + "\\s*(\\W.*|)")) {
					foundLine = lineno;
					break;
				}
				if (inComment && !line.matches(".*/\\*.*") && line.matches(".*\\*/.*")) {
					inComment = false;
				}
			}
		} catch (IOException e) {
			logger.warn("Problem reading " + file + " (" + e.getMessage() + ")");
		}
		return foundLine;
	}
}
