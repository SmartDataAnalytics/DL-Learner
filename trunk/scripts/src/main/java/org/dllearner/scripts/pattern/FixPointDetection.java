/**
 * 
 */
package org.dllearner.scripts.pattern;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.prefs.Preferences;

import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.io.Files;

/**
 * @author Lorenz Buehmann
 *
 */
public class FixPointDetection {
	
	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	
	public FixPointDetection() {
		initDBConnection();
		
		File dir = new File("pattern-fixpoint");
		dir.mkdir();
	}
	
	private Connection conn;
	private PreparedStatement ps;
	
	private void initDBConnection() {
		try {
			InputStream is = this.getClass().getClassLoader().getResourceAsStream("db_settings.ini");
			Preferences prefs = new IniPreferences(is);
			String dbServer = prefs.node("database").get("server", null);
			String dbName = prefs.node("database").get("name", null);
			String dbUser = prefs.node("database").get("user", null);
			String dbPass = prefs.node("database").get("pass", null);

			Class.forName("com.mysql.jdbc.Driver");
			String url = "jdbc:mysql://" + dbServer + "/" + dbName;
			conn = DriverManager.getConnection(url, dbUser, dbPass);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (InvalidFileFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			ps = conn.prepareStatement("SELECT occurrences FROM Ontology_Pattern WHERE pattern_id=? AND ontology_id=?");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	
	private List<Integer> getProcessedOntologies() throws SQLException{
		List<Integer> ids = new ArrayList<Integer>();
		ResultSet rs = conn.createStatement().executeQuery("SELECT DISTINCT id FROM Ontology");
		while(rs.next()){
			int id = rs.getInt(1);
			ids.add(id);
		}
		return ids;
	}
	
	private int getPatternFrequency(int ontologyID, int patternID) throws SQLException{
		int frequency = 0;
		ps.clearParameters();
		ps.setInt(1, patternID);
		ps.setInt(2, ontologyID);
		ResultSet rs = ps.executeQuery();
		while(rs.next()){
			frequency = rs.getInt(1);
		}
		return frequency;
	}
	
	private Map<Integer, OWLAxiom> getPatternIDs(Set<OWLAxiom> patterns) throws SQLException{
		Map<Integer, OWLAxiom> patternIDs = new TreeMap<Integer, OWLAxiom>();
		
		PreparedStatement ps = conn.prepareStatement("SELECT id FROM Pattern WHERE pattern=?");
		ResultSet rs;
		for (OWLAxiom pattern : patterns) {
			String patternString = render(pattern);
			ps.setString(1, patternString);
			rs = ps.executeQuery();
			while(rs.next()){
				int id = rs.getInt(1);
				patternIDs.put(id, pattern);
			}
		}
		return patternIDs;
	}
	
	private String render(OWLAxiom axiom){
		try {
			OWLOntologyManager man = OWLManager.createOWLOntologyManager();
			OWLOntology ontology = man.createOntology();
			man.addAxiom(ontology, axiom);
			StringWriter sw = new StringWriter();
			org.coode.owlapi.functionalrenderer.OWLObjectRenderer r = new org.coode.owlapi.functionalrenderer.OWLObjectRenderer(man, ontology, sw);
			axiom.accept(r);
			return sw.toString();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
		return null;
	}
	
//	public void start(Set<OWLAxiom> patterns) throws SQLException{
//		//get all ontology ids
//		List<Integer> ontologyIDs = getProcessedOntologies();
//		//shuffle the ids randomly
//		Collections.shuffle(ontologyIDs, new Random(123));
//		//get all pattern IDs of the patterns we want to analyze
//		Map<Integer, OWLAxiom> patternIDs = getPatternIDs(patterns);
//		Multimap<Integer, Point> pattern2Series = LinkedListMultimap.create();
//		
//		for (Integer patternID : patternIDs.keySet()) {System.out.print(patternID + ": ");
//			int x = 0;
//			int y = 0;
//			OWLAxiom pattern = patternIDs.get(patternID);
//			
//			//add (0,0) point
//			pattern2Series.put(patternID, new Point(x, y));
//			for (Integer ontologyID : ontologyIDs) {
//				int frequency = getPatternFrequency(ontologyID, patternID);
//				x++;
//				y += frequency;System.out.print(y + ", ");
//				pattern2Series.put(patternID, new Point(x, y));
//			}
//			System.out.println();
//			try {
//				Files.write(Joiner.on("\n").join(pattern2Series.get(patternID)), new File("pattern-fixpoint/" + axiomRenderer.render(pattern).replace(" ", "_") + ".csv"), Charsets.UTF_8);
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
//	}
	
	public void start(Set<OWLAxiom> patterns) throws SQLException, IOException{
		//get all ontology ids
		List<Integer> ontologyIDs = getProcessedOntologies();
		//shuffle the ids randomly
		Collections.shuffle(ontologyIDs, new Random(123));
		//get all pattern IDs of the patterns we want to analyze
		Map<Integer, OWLAxiom> patternIDs = getPatternIDs(patterns);
		Multimap<Integer, Point> pattern2Series = LinkedListMultimap.create();
		
		for (Integer patternID : patternIDs.keySet()) {System.out.print(patternID + ": ");
			int x = 0;
			int y = 0;
			OWLAxiom pattern = patternIDs.get(patternID);
			
			//add (0,0) point
			pattern2Series.put(patternID, new Point(x, y));
			for (Integer ontologyID : ontologyIDs) {
				int frequency = getPatternFrequency(ontologyID, patternID);
				x++;
				y += frequency;System.out.print(y + ", ");
				pattern2Series.put(patternID, new Point(x, y));
			}
			System.out.println();
			try {
				Files.write(Joiner.on("\n").join(pattern2Series.get(patternID)), new File("pattern-fixpoint/" + axiomRenderer.render(pattern).replace(" ", "_") + ".csv"), Charsets.UTF_8);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//print values
		StringBuilder sb = new StringBuilder();
		List<List<Point>> lists = new ArrayList<List<Point>>();
		for (Entry<Integer, Collection<Point>> entry : pattern2Series.asMap().entrySet()) {
			lists.add(new ArrayList<Point>(entry.getValue()));
			sb.append(axiomRenderer.render(patternIDs.get(entry.getKey())).replace("\n(", "(") + ",");
		}
		sb.append("\n");
		for(int x = 0; x < ontologyIDs.size()+1; x++){
			for (List<Point> list : lists) {
				int y = list.get(x).getY();
				sb.append(y).append(",");
			}
			sb.append("\n");
		}
		Files.write(sb.toString(), new File("pattern-fixpoint/all.csv"), Charsets.UTF_8);
		
		//print ranks
		sb = new StringBuilder();
		for (Entry<Integer, Collection<Point>> entry : pattern2Series.asMap().entrySet()) {
			sb.append(axiomRenderer.render(patternIDs.get(entry.getKey())).replace("\n(", "(") + ",");
		}
		sb.append("\n");
		for(int x = 0; x < ontologyIDs.size()+1; x++){
			List<Integer> yValues = new ArrayList<Integer>();
			for (List<Point> list : lists) {
				yValues.add(list.get(x).getY());
			}
			List<Integer> ranks = getRanks(yValues);
			sb.append(Joiner.on(",").join(ranks));
			sb.append("\n");
		}
		Files.write(sb.toString(), new File("pattern-fixpoint/all-ranks.csv"), Charsets.UTF_8);
		
	}
	
	private List<Integer> getRanks(List<Integer> values){
		List<Integer> ranks = new ArrayList<Integer>();
		ArrayList<Integer> sortedValues = new ArrayList<Integer>(new HashSet<Integer>(values));
		Collections.sort(sortedValues, Collections.reverseOrder());
		for (Integer v : values) {
			ranks.add(sortedValues.indexOf(v) + 1);
		}
		return ranks;
	}
	
	class Point {
		int x, y;
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		public int getX() {
			return x;
		}
		public int getY() {
			return y;
		}
		@Override
		public String toString() {
			return x + "," + y;
		}
	}
	
	public static void main(String[] args) throws Exception {
		File patternsFile = new File(args[0]);
		OWLOntology patternsOntology = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(patternsFile);
		
		new FixPointDetection().start(patternsOntology.getAxioms());
	}

}
