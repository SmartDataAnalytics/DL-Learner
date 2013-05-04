package org.dllearner.scripts;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import org.coode.owlapi.functionalparser.OWLFunctionalSyntaxOWLParser;
import org.dllearner.algorithms.pattern.OWLAxiomPatternFinder;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;
import org.dllearner.kb.repository.tones.TONESRepository;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

public class OWLAxiomPatternDetectionEvaluation {
	
	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private Connection conn;

	public OWLAxiomPatternDetectionEvaluation() {
		initDBConnection();
	}
	
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
	}
	
	public void run(Collection<OntologyRepository> repositories){
		//analyze repositories
		analyze(repositories);
		
		//create statistics for the repositories
		makeRepositoryStatistics(repositories);
		
		//get top n TBox, RBox and ABox patterns
		makePatternStatistics(repositories);
	}
	
	private void analyze(Collection<OntologyRepository> repositories){
		for (OntologyRepository repository : repositories) {
			repository.initialize();
			OWLAxiomPatternFinder patternFinder = new OWLAxiomPatternFinder(repository, conn);
			patternFinder.start();
		}
	}
	
	private void makePatternStatistics(Collection<OntologyRepository> repositories){
		int n = 10;
		String latex = "";
		Map<OWLAxiom, Integer> topNTBoxAxiomPatterns = getTopNTBoxAxiomPatterns(n);
		latex += asLatex("Total TBox", topNTBoxAxiomPatterns) + "\n\n";
		Map<OWLAxiom, Integer> topNRBoxAxiomPatterns = getTopNRBoxAxiomPatterns(n);
		latex += asLatex("Total RBox", topNRBoxAxiomPatterns) + "\n\n";
		Map<OWLAxiom, Integer> topNABoxAxiomPatterns = getTopNABoxAxiomPatterns(n);
		latex += asLatex("Total ABox", topNABoxAxiomPatterns) + "\n\n";
		
		//get top n TBox, RBox and ABox patterns by repository
		
		Map<OntologyRepository, Map<OWLAxiom, Integer>> topNTBoxAxiomPatternsByRepository = getTopNTBoxAxiomPatterns(repositories, n);
		Map<OntologyRepository, Map<OWLAxiom, Integer>> topNRBoxAxiomPatternsByRepository = getTopNRBoxAxiomPatterns(repositories, n);
		Map<OntologyRepository, Map<OWLAxiom, Integer>> topNABoxAxiomPatternsByRepository = getTopNABoxAxiomPatterns(repositories, n);
		for (OntologyRepository repository : repositories) {
			latex += asLatex(repository.getName() + " TBox", topNTBoxAxiomPatternsByRepository.get(repository)) + "\n\n";
			latex += asLatex(repository.getName() + " RBox", topNRBoxAxiomPatternsByRepository.get(repository)) + "\n\n";
			latex += asLatex(repository.getName() + " ABox", topNABoxAxiomPatternsByRepository.get(repository)) + "\n\n";
		}
		try {
			new FileOutputStream("pattern-statistics.tex").write(latex.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makeRepositoryStatistics(Collection<OntologyRepository> repositories){
		String latexTable = "\\begin{tabular}{lrr|rrr|rrr|rrr|rrr}";
		latexTable += "\\toprule\n";
		latexTable += "Repository & \\multicolumn{2}{c}{\\#Ontologies} & \\multicolumn{12}{c}{\\#Axioms} \\\\\n";
		latexTable += "& Total & Error & \\multicolumn{3}{c}{Total} & \\multicolumn{3}{c}{Tbox} & \\multicolumn{3}{c}{RBox} & \\multicolumn{3}{c}{Abox} \\\\\\midrule\n";
		latexTable += "&   &                              & Min & Avg & Max & Min & Avg & Max & Min & Avg & Max & Min & Avg & Max \\\\\\midrule\n";
        
 
		PreparedStatement ps;
		ResultSet rs;
		
		int numberOfOntologies;
		int numberOfErrorOntologies;
		int minNumberOfLogicalAxioms;
		int maxNumberOfLogicalAxioms;
		int avgNumberOfLogicalAxioms;
		int minNumberOfTboxAxioms;
		int maxNumberOfTboxAxioms;
		int avgNumberOfTboxAxioms;
		int minNumberOfRboxAxioms;
		int maxNumberOfRboxAxioms;
		int avgNumberOfRboxAxioms;
		int minNumberOfAboxAxioms;
		int maxNumberOfAboxAxioms;
		int avgNumberOfAboxAxioms;
		
		//for each repository
		for (OntologyRepository repository : repositories) {
			try {
				//get number of ontologies
				ps = conn.prepareStatement("SELECT COUNT(*) FROM Ontology WHERE repository=?");
				ps.setString(1, repository.getName());
				numberOfOntologies = count(ps);
				//get number of error causing ontologies
				ps = conn.prepareStatement("SELECT COUNT(*) FROM Ontology WHERE repository=? AND iri LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				numberOfErrorOntologies = count(ps);
				//get min number of logical axioms
				ps = conn.prepareStatement("SELECT MIN(logical_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				minNumberOfLogicalAxioms  = count(ps);
				//get max number of logical axioms
				ps = conn.prepareStatement("SELECT MAX(logical_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				maxNumberOfLogicalAxioms  = count(ps);
				//get avg number of logical axioms
				ps = conn.prepareStatement("SELECT AVG(logical_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				avgNumberOfLogicalAxioms = count(ps);
				//get min number of tbox axioms
				ps = conn.prepareStatement("SELECT MIN(tbox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				minNumberOfTboxAxioms = count(ps);
				//get max number of tbox axioms
				ps = conn.prepareStatement("SELECT MAX(tbox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				maxNumberOfTboxAxioms = count(ps);
				//get avg number of tbox axioms
				ps = conn.prepareStatement("SELECT AVG(tbox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				avgNumberOfTboxAxioms = count(ps);
				//get min number of rbox axioms
				ps = conn.prepareStatement("SELECT MIN(rbox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				minNumberOfRboxAxioms = count(ps);
				//get max number of rbox axioms
				ps = conn.prepareStatement("SELECT MAX(rbox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				maxNumberOfRboxAxioms = count(ps);
				//get avg number of rbox axioms
				ps = conn.prepareStatement("SELECT AVG(rbox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				avgNumberOfRboxAxioms = count(ps);
				//get min number of abox axioms
				ps = conn.prepareStatement("SELECT MIN(abox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				minNumberOfAboxAxioms = count(ps);
				//get max number of abox axioms
				ps = conn.prepareStatement("SELECT MAX(abox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				maxNumberOfAboxAxioms = count(ps);
				//get avg number of abox axioms
				ps = conn.prepareStatement("SELECT AVG(abox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
				ps.setString(1, repository.getName());
				avgNumberOfAboxAxioms = count(ps);
				
				latexTable += 
						repository.getName() + "&" + 
						numberOfOntologies + "&" +
						numberOfErrorOntologies + "&" +
						minNumberOfLogicalAxioms + "&" +
						avgNumberOfLogicalAxioms + "&" +
						maxNumberOfLogicalAxioms + "&" +
						minNumberOfTboxAxioms + "&" +
						avgNumberOfTboxAxioms + "&" +
						maxNumberOfTboxAxioms + "&" +
						minNumberOfRboxAxioms + "&" +
						avgNumberOfRboxAxioms + "&" +
						maxNumberOfRboxAxioms + "&" +
						minNumberOfAboxAxioms + "&" +
						avgNumberOfAboxAxioms + "&" +
						maxNumberOfAboxAxioms + "\\\\\n";
				
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		latexTable += "\\bottomrule\\end{tabular}";
		try {
			new FileOutputStream("repository-statistics.tex").write(latexTable.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String asLatex(String title, Map<OWLAxiom, Integer> topN){
		String latexTable = "\\begin{table}\n";
		latexTable += "\\begin{tabular}{lr}\n";
		latexTable += "\\toprule\n";
		latexTable += "Pattern & Frequency\\\\\\midrule\n";
		
		for (Entry<OWLAxiom, Integer> entry : topN.entrySet()) {
			OWLAxiom axiom = entry.getKey();
			Integer frequency = entry.getValue();
			
			latexTable += axiomRenderer.render(axiom) + " & " + frequency + "\\\\\n";
			
		}
		latexTable += "\\bottomrule\\end{tabular}\n";
		latexTable += "\\caption{" + title + "}\n";
		latexTable += "\\end{table}\n";
		return latexTable;
	}
	
	private Map<OWLAxiom, Integer> getTopNTBoxAxiomPatterns(int n){
		Map<OWLAxiom, Integer> topN = new LinkedHashMap<OWLAxiom, Integer>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = conn.prepareStatement("SELECT pattern,SUM(occurrences) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND P.axiom_type=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, "TBox");
			ps.setInt(2, n);
			rs = ps.executeQuery();
			while(rs.next()){
				topN.put(asOWLAxiom(rs.getString(1)), rs.getInt(2));
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	private Map<OntologyRepository, Map<OWLAxiom, Integer>> getTopNTBoxAxiomPatterns(Collection<OntologyRepository> repositories, int n){
		Map<OntologyRepository, Map<OWLAxiom, Integer>> topNByRepository = new LinkedHashMap<OntologyRepository, Map<OWLAxiom,Integer>>();
		PreparedStatement ps;
		ResultSet rs;
		//for each repository
		for (OntologyRepository repository : repositories) {
			Map<OWLAxiom, Integer> topN = new LinkedHashMap<OWLAxiom, Integer>();
			try {
				//get number of ontologies
				ps = conn.prepareStatement("SELECT pattern,SUM(occurrences) FROM " +
						"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
						"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND O.repository=? AND P.axiom_type=?) " +
						"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
				ps.setString(1, repository.getName());
				ps.setString(2, "TBox");
				ps.setInt(3, n);
				rs = ps.executeQuery();
				while(rs.next()){
					topN.put(asOWLAxiom(rs.getString(1)), rs.getInt(2));
				}
			} catch(SQLException e){
				e.printStackTrace();
			}
			topNByRepository.put(repository, topN);
		}
		return topNByRepository;
	}
	
	private Map<OWLAxiom, Integer> getTopNRBoxAxiomPatterns(int n){
		Map<OWLAxiom, Integer> topN = new LinkedHashMap<OWLAxiom, Integer>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = conn.prepareStatement("SELECT pattern,SUM(occurrences) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND P.axiom_type=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, "RBox");
			ps.setInt(2, n);
			rs = ps.executeQuery();
			while(rs.next()){
				topN.put(asOWLAxiom(rs.getString(1)), rs.getInt(2));
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	private Map<OntologyRepository, Map<OWLAxiom, Integer>> getTopNRBoxAxiomPatterns(Collection<OntologyRepository> repositories, int n){
		Map<OntologyRepository, Map<OWLAxiom, Integer>> topNByRepository = new LinkedHashMap<OntologyRepository, Map<OWLAxiom,Integer>>();
		PreparedStatement ps;
		ResultSet rs;
		//for each repository
		for (OntologyRepository repository : repositories) {
			Map<OWLAxiom, Integer> topN = new LinkedHashMap<OWLAxiom, Integer>();
			try {
				//get number of ontologies
				ps = conn.prepareStatement("SELECT pattern,SUM(occurrences) FROM " +
						"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
						"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND O.repository=? AND P.axiom_type=?) " +
						"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
				ps.setString(1, repository.getName());
				ps.setString(2, "RBox");
				ps.setInt(3, n);
				rs = ps.executeQuery();
				while(rs.next()){
					topN.put(asOWLAxiom(rs.getString(1)), rs.getInt(2));
				}
			} catch(SQLException e){
				e.printStackTrace();
			}
			topNByRepository.put(repository, topN);
		}
		return topNByRepository;
	}

	private Map<OWLAxiom, Integer> getTopNABoxAxiomPatterns(int n){
		Map<OWLAxiom, Integer> topN = new LinkedHashMap<OWLAxiom, Integer>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = conn.prepareStatement("SELECT pattern,SUM(occurrences) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND P.axiom_type=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, "ABox");
			ps.setInt(2, n);
			rs = ps.executeQuery();
			while(rs.next()){
				topN.put(asOWLAxiom(rs.getString(1)), rs.getInt(2));
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	private Map<OntologyRepository, Map<OWLAxiom, Integer>> getTopNABoxAxiomPatterns(Collection<OntologyRepository> repositories, int n){
		Map<OntologyRepository, Map<OWLAxiom, Integer>> topNByRepository = new LinkedHashMap<OntologyRepository, Map<OWLAxiom,Integer>>();
		PreparedStatement ps;
		ResultSet rs;
		//for each repository
		for (OntologyRepository repository : repositories) {
			Map<OWLAxiom, Integer> topN = new LinkedHashMap<OWLAxiom, Integer>();
			try {
				//get number of ontologies
				ps = conn.prepareStatement("SELECT pattern,SUM(occurrences) FROM " +
						"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
						"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND O.repository=? AND P.axiom_type=?) " +
						"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
				ps.setString(1, repository.getName());
				ps.setString(2, "ABox");
				ps.setInt(3, n);
				rs = ps.executeQuery();
				while(rs.next()){
					topN.put(asOWLAxiom(rs.getString(1)), rs.getInt(2));
				}
			} catch(SQLException e){
				e.printStackTrace();
			}
			topNByRepository.put(repository, topN);
		}
		return topNByRepository;
	}
	
	private OWLAxiom asOWLAxiom(String functionalSyntaxAxiomString){
		try {
			StringDocumentSource s = new StringDocumentSource("Ontology(<http://www.pattern.org>" + functionalSyntaxAxiomString + ")");
			OWLFunctionalSyntaxOWLParser p = new OWLFunctionalSyntaxOWLParser();
			OWLOntology newOntology = OWLManager.createOWLOntologyManager().createOntology();
			p.parse(s, newOntology);
			if(!newOntology.getLogicalAxioms().isEmpty()){
				return newOntology.getLogicalAxioms().iterator().next();
			}
		} catch (UnloadableImportException e) {
			e.printStackTrace();
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		} catch (OWLParserException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private int count(PreparedStatement ps) throws SQLException{
		ResultSet rs = ps.executeQuery();
		rs.next();
		return rs.getInt(1);
	}
	
	public static void main(String[] args) throws Exception {
//		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		new OWLAxiomPatternDetectionEvaluation().run(Arrays.asList(
				new TONESRepository(), new BioPortalRepository()));
	}
	

}
