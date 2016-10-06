/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.utilities.examples;

import org.dllearner.algorithms.ocel.OCEL;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.URLencodeUTF8;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * Creates a temporary file with all the data collected for examples.
 * e.g.:
 * basedir = tiger/  
 * one Example file with nt data = tiger/uri
 * uris will be urlencoded
 * 
 *  
 * @author Sebastian Hellmann <hellmann@informatik.uni-leipzig.de>
 *
 */
public class ExampleDataCollector {

	SortedSet<String> exampleURIs;
	private String baseDir;
	
	public ExampleDataCollector(String baseDir, SortedSet<String> exampleURIs, List<File> additionalSources){
		this.exampleURIs=exampleURIs;
		this.baseDir = baseDir(baseDir);
	}
	
	public static void main(String[] args) {
		String b = "http://nlp2rdf.org/ontology/s";
		String baseDir = "examples/nlp2rdf/tiger/";
		
		SortedSet<String> pos = new TreeSet<>(Arrays.asList(new String[]{b + "197", b + "2013", b + "2704"}));
		SortedSet<String> neg = new TreeSet<>(Arrays.asList(new String[]{b + "1", b + "2", b + "3"}));
		List<URL> urls = new ArrayList<>();
		urls.addAll (convert(baseDir, pos));
		urls.addAll (convert(baseDir, neg));
		
		Set<KnowledgeSource> tmp = new HashSet<>();
		try {
			URL add = new File(baseDir+"tiger.rdf").toURI().toURL();
//			 add = new File(baseDir+"new.rdf").toURI().toURL();
			urls.add(add);
			
			for(URL u: urls){
				OWLFile ks = new OWLFile();
				ks.setUrl(u);
				tmp.add(ks);
			}
			
			ClosedWorldReasoner rc = new ClosedWorldReasoner(tmp);
			PosNegLPStandard lp = new PosNegLPStandard(rc);
			lp.setPositiveExamples(Helper.getIndividualSet(pos));
			lp.setNegativeExamples(Helper.getIndividualSet(neg));
			OCEL la = new OCEL(lp, rc);
			for(KnowledgeSource ks: tmp){
				ks.init();
			}
			rc.init();
			lp.init();
			la.init();

			// start learning algorithm
			la.start();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static List<URL> convert(String baseDir, SortedSet<String> exampleURIs){
		List<URL> u = new ArrayList<>();
		for (String exampleURI : exampleURIs) {
			try {
				u.add(new File(toFileName(baseDir, exampleURI)).toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		
		}
		return u;
		
	}
	
	@SuppressWarnings("unused")
	private File collect(){
		String from = null;
		File tmpFile = null;
		FileWriter fw = null;
		try{
			tmpFile = File.createTempFile(ExampleDataCollector.class.getSimpleName(), null);
			fw = new FileWriter(tmpFile, false);
			for(String one: exampleURIs){
				from = toFileName(baseDir, one) ;
				addToFile(fw, from);
			}
		}catch (Exception e) {
			e.printStackTrace();
		}finally{
				try {
					fw.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		
		return tmpFile;
	}
	
	private void addToFile(FileWriter fw, String fileName)throws Exception{
		String tmp = Files.readFile(new File(fileName));
		fw.write(tmp);
		fw.flush();
	}
	
	private static String toFileName(String baseDir, String exampleURI){
			return baseDir(baseDir)+URLencodeUTF8.encode(exampleURI);
	}
	
	private static String baseDir(String baseDir){
		return (baseDir.endsWith(File.separator))?baseDir:baseDir+File.separator;
	}
	
}
