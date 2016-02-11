package org.dllearner.test;

import com.google.common.collect.Sets;
import javassist.ClassPool;
import org.apache.log4j.Logger;
import org.dllearner.core.AnnComponentManager;
import org.dllearner.core.Component;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.annotations.OutVariable;
import org.dllearner.core.annotations.Unused;
import org.dllearner.core.config.ConfigOption;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;

/* (no java-doc)
 * Internal tool to help find undocumented config options
 */
public class UndocumentedOptionScanner {
	public static void main(String[] args) {
		ClassPool cp = ClassPool.getDefault();
		Logger logger = Logger.getLogger(UndocumentedOptionScanner.class);
		AnnComponentManager cm = AnnComponentManager.getInstance();
		for (Class<? extends Component> c : cm.getComponents()) {
			logger.info("\n@"+c.getCanonicalName());
			Map<String, List<Field>> fields = new TreeMap<>();
			Map<String, List<Method>> methods = new TreeMap<>();
			for (Method m : c.getMethods()) {
				String name = m.getName();
				List<Method> set = methods.get(name);
				if (set == null) {
					set = new LinkedList<Method>();
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
			for (Entry<String, List<Field>> f : fields.entrySet()) {
				boolean isDocumented = false;
				for (Field fs : f.getValue()) {
					isDocumented = isDocumented || fs.isAnnotationPresent(ConfigOption.class)
							|| fs.isAnnotationPresent(Unused.class)
							|| fs.isAnnotationPresent(OutVariable.class)
							|| fs.isAnnotationPresent(NoConfigOption.class);
				}
				if (isDocumented) {
					hasDoc.add(AnnComponentManager.getName(f.getValue().get(0)));
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
						logger.warn("setter+var. but no @configOption: " + optionName + "(" + fields.get(optionName).get(0).getDeclaringClass().getSimpleName().concat(".java") + ":12)");
						noCO.add(optionName);

					} else {
						boolean deprecated = false;
						for (Method ms : m.getValue()) {
							deprecated = deprecated || ms.isAnnotationPresent(Deprecated.class);
						}
						if (!deprecated) {
							logger.info("setter without var: "+optionName);
						}
					}
				}
			}
			for (String noSetter : Sets.difference(hasDoc, hasSetter)) {
				logger.warn("option without setter! " +noSetter);
			}
			//if (c.equals(CELOE.class)) { System.exit(0); }
		}
	}
}
