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
import java.util.prefs.Preferences;

import org.dllearner.algorithms.pattern.OWLAxiomPatternFinder;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;
import org.dllearner.kb.repository.tones.TONESRepository;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;

public class OWLAxiomPatternDetectionEvaluation {
	
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
	}
	
	private void analyze(Collection<OntologyRepository> repositories){
		for (OntologyRepository repository : repositories) {
			repository.initialize();
			OWLAxiomPatternFinder patternFinder = new OWLAxiomPatternFinder(repository, conn);
			patternFinder.start();
		}
	}
	
	private void makeRepositoryStatistics(Collection<OntologyRepository> repositories){
		String latexTable = "\\begin{tabular}{lrr|rrr|rrr|rrr|rrr}";
		latexTable += "\\toprule";
		latexTable += "Repository & \\multicolumn{2}{c}{\\#Ontologies} & \\multicolumn{12}{c}{\\#Axioms} \\\\";
		latexTable += "& Total & Error & \\multicolumn{3}{c}{Total} & \\multicolumn{3}{c}{Tbox} & \\multicolumn{3}{c}{RBox} & \\multicolumn{3}{c}{Abox} \\\\\\midrule";
		latexTable += "&   &                              & Min & Avg & Max & Min & Avg & Max & Min & Avg & Max & Min & Avg & Max \\\\\\midrule";
        
 
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
				ps = conn.prepareStatement("SELECT MAX(tbox_axioms) FROM Ontology WHERE repository=? AND iri NOT LIKE 'ERROR%'");
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
						maxNumberOfAboxAxioms + "\\\\";
				
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
	
	private int count(PreparedStatement ps) throws SQLException{
		ResultSet rs = ps.executeQuery();
		rs.next();
		return rs.getInt(1);
	}
	
	public static void main(String[] args) throws Exception {
		new OWLAxiomPatternDetectionEvaluation().run(Arrays.asList(
				new TONESRepository(), new BioPortalRepository()));
	}
	

}
