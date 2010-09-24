package org.dllearner.scripts.tiger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.ocel.ROLearner2;
import org.dllearner.core.ComponentPool;
import org.dllearner.core.owl.Description;
import org.dllearner.kb.sparql.Cache;
import org.dllearner.kb.sparql.SPARQLTasks;
import org.dllearner.kb.sparql.SparqlEndpoint;
import org.dllearner.kb.sparql.SparqlQuery;
import org.dllearner.kb.sparql.SparqlQueryDescriptionConvertVisitor;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.JamonMonitorLogger;
import org.dllearner.utilities.experiments.Table;
import org.dllearner.utilities.experiments.TableRowColumn;
import org.dllearner.utilities.experiments.TableRowColumn.Display;

import com.jamonapi.MonKeyImp;
import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class TestQueries {
	private static final Logger logger = Logger.getLogger(TestQueries.class);

	static DecimalFormat df = new DecimalFormat("00.###%");
	public static DecimalFormat dftime = new DecimalFormat("#####.#");

	// static String backgroundXML = "files/tiger.noSchema.noImports.rdf";
	static String backgroundXML = "files/tiger_trimmed_toPOS.rdf";
	static String propertiesXML = "files/propertiesOnly.rdf";
	static String sentenceXMLFolder = "files/tiger/";
	static String resultFolder = "tigerResults/";

	static String sentenceprefix = "http://nlp2rdf.org/ontology/s";
	static String prefix = "http://nlp2rdf.org/ontology/";

	static String active = "files/active_all_sentenceNumbers.txt";
	static String passiveNoZU = "files/passive_noZuInf_sentenceNumbers.txt";
	static String passiveWithZu = "files/passive_zuInf_sentenceNumbers.txt";
	static String test_has_pos = "files/test_has_pos.txt";
	static String test_has_neg = "files/test_has_neg.txt";

	static SparqlEndpoint sparqlEndpoint;
	static SPARQLTasks sparqlTasks;

	static String sparqlEndpointURL = "http://db0.aksw.org:8893/sparql";
	static String graph = "http://nlp2rdf.org/tiger";
	static String rulegraph = "http://nlp2rdf.org/schema/rules1";

	static MonKeyImp queryTime = new MonKeyImp("Query Time", JamonMonitorLogger.MS);
	static MonKeyImp length = new MonKeyImp("length", JamonMonitorLogger.COUNT);
	static MonKeyImp hits = new MonKeyImp("hits", JamonMonitorLogger.COUNT);

	static List<MonKeyImp> mks = new ArrayList<MonKeyImp>(Arrays.asList(new MonKeyImp[] { queryTime}));

	
	static SortedSet<String> concepts = new TreeSet<String>();


	static String conceptFile = "files/WITH_ZU.log";
	
	public static void main(String[] args) {
		LogHelper.initLoggers();
		
		
		
		Logger.getLogger(Cache.class).setLevel(Level.INFO);
		Logger.getLogger(ComponentPool.class).setLevel(Level.INFO);
		Logger.getLogger(ROLearner2.class).setLevel(Level.INFO);
		Logger.getLogger(RhoDRDown.class).setLevel(Level.INFO);
		Logger.getLogger(SparqlQuery.class).setLevel(Level.INFO);

		try {
			sparqlEndpoint = new SparqlEndpoint(new URL(sparqlEndpointURL), new ArrayList<String>(Arrays
					.asList(new String[] { graph })), new ArrayList<String>());
			sparqlTasks = new SPARQLTasks( sparqlEndpoint);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
		
		String[] concepts;
		try {
			concepts = Files.readFileAsArray(new File(conceptFile));
			Map<String,Monitor> mm1 = new HashMap<String, Monitor>();
			Map<String,Monitor> mm2 = new HashMap<String, Monitor>();
			for (int i = 0; i < concepts.length; i++) {
				Description d = KBParser.parseConcept(concepts[i]);
				SparqlQueryDescriptionConvertVisitor visit = new SparqlQueryDescriptionConvertVisitor();
				visit.setDistinct(true);
				visit.setLabels(false);
				visit.setLimit(-1);
				String q  = visit.getSparqlQuery(d);
				q = " \n define input:inference \"" + rulegraph + "\" \n" + "" + q;
				logger.warn(concepts[i]);
				logger.warn(q);
				
				String label1 = "Time "+d.getLength();
				String label2 = "Length "+d.getLength();
				Monitor m1 = MonitorFactory.getTimeMonitor(label1).start();
				Monitor m2 = MonitorFactory.getMonitor(label2, JamonMonitorLogger.COUNT);

				m2.add(d.getLength());
				sparqlTasks.queryAsResultSet(q);
				m1.stop();
				mm1.put(label1, m1);
				mm2.put(label2, m2);
				
			}
			
			Monitor[] mons1 = new Monitor[mm1.size()];
			Monitor[] mons2 = new Monitor[mm1.size()];
			SortedSet<String> keys1 = new TreeSet<String>(mm1.keySet());
			SortedSet<String> keys2 = new TreeSet<String>(mm2.keySet());
			int i = 0;
			for(String key:keys1){
				mons1[i] = mm1.get(key);
				i++;
			}
			i = 0;
			for(String key:keys2){
				mons2[i] = mm2.get(key);
				i++;
			}
			Table t = new Table();

			TableRowColumn tc =  new TableRowColumn(mons2, "testqueries", "length");
			t.addTableRowColumn(tc);
			
			tc =  new TableRowColumn(mons2, "testqueries", "length");
			tc.setDisplay(Display.HITS);
			t.addTableRowColumn(tc);
			
			t.addTableRowColumn(new TableRowColumn(mons1, "testqueries", "time"));
			t.write(resultFolder, "testqueries");
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}catch (ParseException e) {
			e.printStackTrace();
		}
	}
}
