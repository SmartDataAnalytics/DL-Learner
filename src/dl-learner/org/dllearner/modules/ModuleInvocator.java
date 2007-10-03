package org.dllearner.modules;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

import org.dllearner.cli.ConfFileOption;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.Individual;
import org.dllearner.core.dl.KB;

public class ModuleInvocator {

	private ClassLoader loader;
	private String module;
	
	public ModuleInvocator(ClassLoader loader, String module) {
		this.loader = loader;
		this.module = module;
	}
	
	@SuppressWarnings({"unchecked"})
	public void invokePreprocessingModule(KB kb, Map<AtomicConcept, SortedSet<Individual>> positiveExamples,
			Map<AtomicConcept, SortedSet<Individual>> negativeExamples,
			List<ConfFileOption> confOptions, List<List<String>> functionCalls,
			String baseDir, boolean useQueryMode) {
		try {
			// Config.preprocessingModule = "org.dllearner.algorithms.gp.GP";
			
			
			Class moduleMainClass = loader.loadClass(module);
			
			/*
			Class[] interfaces = moduleMainClass.getInterfaces();
			
			boolean implementsPreprocessingModule = false;
			String preprocessingInterface = "org.dllearner.modules.PreprocessingModule";
			for(Class tmp : interfaces) {
				// System.out.println(tmp.getCanonicalName());
				if(tmp.getCanonicalName().equals(preprocessingInterface))
					implementsPreprocessingModule = true;
			}
			*/
			
			// PreprocessingModule moduleObject = (PreprocessingModule) moduleMainClass.newInstance();
			
			Object moduleObject = moduleMainClass.newInstance();
			
			
			// abbrechen, falls Modul das Interface nicht implementiert
			// aus irgendeinem Grund funktioniert der Test nicht
			// if(moduleObject instanceof AbstractPreprocessingModule) {
			if(false) {
				System.out.println("The given module does not implement the preprocessing module interface. Exiting.");
				System.exit(0);
			// Methode ausführen, falls Interface implementiert wurde
			} else {
				

				
				// Method registerIntOptions = moduleMainClass.getMethod("getIntegerOptions", new Class[]{});
				// Map<String, Integer[]> test = (Map<String, Integer[]>) registerIntOptions.invoke(moduleObject, new Object[]{});
				
				
				// Preprocessing-Modul ausführen
				

				Method modulePreprocessingMethod = moduleMainClass.getMethod("preprocess", new Class[]{KB.class, Map.class,
						Map.class,List.class, List.class, String.class, boolean.class});
				// null als erster Parameter bedeutet statischer Aufruf
				modulePreprocessingMethod.invoke(moduleObject, kb, positiveExamples, negativeExamples,
						confOptions, functionCalls, baseDir, useQueryMode);
			}	
			
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}		
	}
	
}
