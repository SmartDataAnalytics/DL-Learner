package org.dllearner.scripts.pattern;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.prefs.Preferences;

import org.aksw.commons.util.Pair;
import org.coode.owlapi.functionalparser.OWLFunctionalSyntaxOWLParser;
import org.coode.owlapi.latex.LatexWriter;
import org.dllearner.algorithms.pattern.OWLAxiomPatternFinder;
import org.dllearner.kb.repository.OntologyRepository;
import org.dllearner.kb.repository.bioportal.BioPortalRepository;
import org.dllearner.kb.repository.oxford.OxfordRepository;
import org.dllearner.kb.repository.tones.TONESRepository;
import org.ini4j.IniPreferences;
import org.ini4j.InvalidFileFormatException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLObjectRenderer;
import org.semanticweb.owlapi.io.OWLParserException;
import org.semanticweb.owlapi.io.StringDocumentSource;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.UnloadableImportException;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

public class OWLAxiomPatternDetectionEvaluation {
	
	enum AxiomTypeCategory{
		TBox, RBox, ABox
	}
	
	private OWLObjectRenderer axiomRenderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
	private OWLDataFactory df = new OWLDataFactoryImpl();
	private Connection conn;
	
	private boolean fancyLatex = false;
	private boolean dlSyntax = true;
	private boolean formatNumbers = true;
	
	private int numberOfRowsPerTable = 25;

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
	
	public void run(boolean analyzeRepositories, Collection<OntologyRepository> repositories){
		//analyze repositories
		if(analyzeRepositories){
			analyze(repositories);
		}
		
		//create statistics for the repositories
		makeRepositoryStatistics(repositories);
		
		//get top n TBox, RBox and ABox patterns
		makePatternStatisticsSingleTable(repositories);
	}
	
	public void run(Collection<OntologyRepository> repositories){
		run(true, repositories);
	}
	
	public List<OWLAxiom> getPatternsToEvaluate(){
		List<OWLAxiom> axiomPatterns = new ArrayList<OWLAxiom>();
		
		Map<OWLAxiom, Pair<Integer, Integer>> topNAxiomPatterns = getTopNAxiomPatterns(AxiomTypeCategory.TBox, 10);
		axiomPatterns.addAll(topNAxiomPatterns.keySet());
		
		return axiomPatterns;
	}
	
	private void analyze(Collection<OntologyRepository> repositories){
		for (OntologyRepository repository : repositories) {
			repository.initialize();
			OWLAxiomPatternFinder patternFinder = new OWLAxiomPatternFinder(repository, conn);
			patternFinder.start();
		}
	}
	
	private void makePatternStatistics(Collection<OntologyRepository> repositories){
		int n = numberOfRowsPerTable;
		
		String latex = "";
		
		//total pattern statistics
		for (AxiomTypeCategory axiomTypeCategory : AxiomTypeCategory.values()) {
			Map<OWLAxiom, Pair<Integer, Integer>> topNAxiomPatterns = getTopNAxiomPatterns(axiomTypeCategory, n);
			latex += asLatex("Top " + n + " " + axiomTypeCategory.name() + " axiom patterns.", topNAxiomPatterns) + "\n\n";
		}
		
		//get top n TBox, RBox and ABox patterns by repository
		for (OntologyRepository repository : repositories) {
			for (AxiomTypeCategory axiomTypeCategory : AxiomTypeCategory.values()) {
				Map<OWLAxiom, Pair<Integer, Integer>> topNAxiomPatterns = getTopNAxiomPatterns(repository, axiomTypeCategory, n);
				latex += asLatex("Top " + n + " " + axiomTypeCategory.name() + " axiom patterns for " + repository.getName() + " repository.", topNAxiomPatterns) + "\n\n";
			}
		}
		try {
			new FileOutputStream("pattern-statistics.tex").write(latex.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void makePatternStatisticsSingleTable(Collection<OntologyRepository> repositories){
		int n = numberOfRowsPerTable;
		
		String latex = "";
		
		//total pattern statistics
		for (AxiomTypeCategory axiomTypeCategory : AxiomTypeCategory.values()) {
			Map<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> topNAxiomPatterns = getTopNAxiomPatternsWithId(axiomTypeCategory, n);
			latex += asLatexWithId(axiomTypeCategory, topNAxiomPatterns, repositories, n) + "\n\n";
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
				
				if(formatNumbers){
					latexTable += 
							repository.getName() + " & " + 
							"\\num{" + numberOfOntologies + "} & " +
							"\\num{" + numberOfErrorOntologies + "} & " +
							"\\num{" + minNumberOfLogicalAxioms + "} & " +
							"\\num{" + avgNumberOfLogicalAxioms + "} & " +
							"\\num{" + maxNumberOfLogicalAxioms + "} & " +
							"\\num{" + minNumberOfTboxAxioms + "} & " +
							"\\num{" + avgNumberOfTboxAxioms + "} & " +
							"\\num{" + maxNumberOfTboxAxioms + "} & " +
							"\\num{" + minNumberOfRboxAxioms + "} & " +
							"\\num{" + avgNumberOfRboxAxioms + "} & " +
							"\\num{" + maxNumberOfRboxAxioms + "} & " +
							"\\num{" + minNumberOfAboxAxioms + "} & " +
							"\\num{" + avgNumberOfAboxAxioms + "} & " +
							"\\num{" + maxNumberOfAboxAxioms + "}\\\\\n";
				} else {
					latexTable += 
							repository.getName() + " & " + 
							numberOfOntologies + " & " +
							numberOfErrorOntologies + " & " +
							minNumberOfLogicalAxioms + " & " +
							avgNumberOfLogicalAxioms + " & " +
							maxNumberOfLogicalAxioms + " & " +
							minNumberOfTboxAxioms + " & " +
							avgNumberOfTboxAxioms + " & " +
							maxNumberOfTboxAxioms + " & " +
							minNumberOfRboxAxioms + " & " +
							avgNumberOfRboxAxioms + " & " +
							maxNumberOfRboxAxioms + " & " +
							minNumberOfAboxAxioms + " & " +
							avgNumberOfAboxAxioms + " & " +
							maxNumberOfAboxAxioms + "\\\\\n";
				}
				
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
	
	private String asLatex(String title, Map<OWLAxiom, Pair<Integer, Integer>> topN){
		StringWriter sw = new StringWriter();
		LatexWriter w = new LatexWriter(sw);
		LatexObjectVisitor renderer = new LatexObjectVisitor(w, df);
		String latexTable = "\\begin{table}\n";
		latexTable += "\\begin{tabular}{lrrr}\n";
		latexTable += "\\toprule\n";
		latexTable += "Pattern & Frequency & \\#Ontologies\\\\\\midrule\n";
		
		for (Entry<OWLAxiom, Pair<Integer, Integer>> entry : topN.entrySet()) {
			OWLAxiom axiom = entry.getKey();
			Integer frequency = entry.getValue().getKey();
			Integer df = entry.getValue().getValue();
			
			if(axiom != null){
				String axiomColumn = axiomRenderer.render(axiom);
				if(fancyLatex){
					axiomColumn = "\\begin{lstlisting}[language=manchester]" + axiomColumn + "\\end{lstlisting}";
				}
				if(dlSyntax){
					axiom.accept(renderer);
					axiomColumn = sw.toString();
					sw.getBuffer().setLength(0);
					
				}
				if(formatNumbers){
					latexTable += axiomColumn + " & " + "\\num{" + frequency + "} & " + df + "\\\\\n";
				} else {
					latexTable += axiomColumn + " & " + frequency + " & " + df + "\\\\\n";
				}
			}
		}
		latexTable += "\\bottomrule\n\\end{tabular}\n";
		latexTable += "\\caption{" + title + "}\n";
		latexTable += "\\end{table}\n";
		return latexTable;
	}
	
	private String asLatexWithId(AxiomTypeCategory axiomTypeCategory, Map<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> topNAxiomPatterns, Collection<OntologyRepository> repositories, int n){
		StringWriter sw = new StringWriter();
		LatexWriter w = new LatexWriter(sw);
		LatexObjectVisitor renderer = new LatexObjectVisitor(w, df);
		String latexTable = "\\begin{table}\n";
		latexTable += "\\begin{tabular}{rlrr";
		for (int i = 0; i < repositories.size(); i++) {
			latexTable += "r";
		}
		latexTable += "}\n";
		latexTable += "\\toprule\n";
		latexTable += " & Pattern & Frequency & \\#Ontologies";
		for (OntologyRepository repository : repositories) {
			latexTable += " & " + repository.getName();
		}
		latexTable += "\\\\\\midrule\n";
		
		int i = 0;
		for (Entry<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> entry : topNAxiomPatterns.entrySet()) {
			i++;
			int patternId = entry.getKey();
			OWLAxiom axiom = entry.getValue().keySet().iterator().next();
			Integer frequency = entry.getValue().values().iterator().next().getKey();
			Integer df = entry.getValue().values().iterator().next().getValue();
			
			if(axiom != null){
				String axiomColumn = axiomRenderer.render(axiom);
				if(fancyLatex){
					axiomColumn = "\\begin{lstlisting}[language=manchester]" + axiomColumn + "\\end{lstlisting}";
				}
				if(dlSyntax){
					axiom.accept(renderer);
					axiomColumn = sw.toString();sw.getBuffer().setLength(0);
					
				}
				if(formatNumbers){
					latexTable += i + ". & " + axiomColumn + " & " + "\\num{" + frequency + "} & " + df;
					for (OntologyRepository repository : repositories) {
						int rank = 0;
						boolean contained = false;
						Map<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> topNAxiomPatternsWithId = getTopNAxiomPatternsWithId(repository, axiomTypeCategory, 100);
						for (Entry<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> entry2 : topNAxiomPatternsWithId.entrySet()) {
							rank++;
							if(entry2.getKey() == patternId){
								contained = true;
								break;
							}
						}
						latexTable += " & " + (contained ? rank : "n/a");
					}		
					latexTable += "\\\\\n";
				} else {
					latexTable += axiomColumn + " & " + frequency + " & " + df + "\\\\\n";
				}
			}
		}
		latexTable += "\\bottomrule\n\\end{tabular}\n";
		latexTable += "\\caption{" + "Top " + n + " " + axiomTypeCategory.name() + " axiom patterns." + "}\n";
		latexTable += "\\end{table}\n";
		return latexTable;
	}
	
	private Map<OWLAxiom, Pair<Integer, Integer>> getTopNAxiomPatterns(AxiomTypeCategory axiomType, int n){
		Map<OWLAxiom, Pair<Integer, Integer>> topN = new LinkedHashMap<OWLAxiom, Pair<Integer, Integer>>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = conn.prepareStatement("SELECT pattern,SUM(occurrences),COUNT(ontology_id) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND P.axiom_type=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, axiomType.name());
			ps.setInt(2, n);
			rs = ps.executeQuery();
			while(rs.next()){
				topN.put(asOWLAxiom(rs.getString(1)), new Pair<Integer, Integer>(rs.getInt(2), rs.getInt(3)));
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	private Map<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> getTopNAxiomPatternsWithId(AxiomTypeCategory axiomType, int n){
		Map<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> topN = new LinkedHashMap<Integer, Map<OWLAxiom, Pair<Integer, Integer>>>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = conn.prepareStatement("SELECT P.id, pattern,SUM(occurrences),COUNT(ontology_id) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND P.axiom_type=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, axiomType.name());
			ps.setInt(2, n);
			rs = ps.executeQuery();
			while(rs.next()){
				Map<OWLAxiom, Pair<Integer, Integer>> m = new LinkedHashMap<OWLAxiom, Pair<Integer,Integer>>();
				m.put(asOWLAxiom(rs.getString(2)), new Pair<Integer, Integer>(rs.getInt(3), rs.getInt(4)));
				topN.put(rs.getInt(1), m);
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	private Map<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> getTopNAxiomPatternsWithId(OntologyRepository repository, AxiomTypeCategory axiomType, int n){
		Map<Integer, Map<OWLAxiom, Pair<Integer, Integer>>> topN = new LinkedHashMap<Integer, Map<OWLAxiom, Pair<Integer, Integer>>>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			ps = conn.prepareStatement("SELECT P.id, pattern,SUM(occurrences),COUNT(ontology_id) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND P.axiom_type=? AND O.repository=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, axiomType.name());
			ps.setString(2, repository.getName());
			ps.setInt(3, n);
			rs = ps.executeQuery();
			while(rs.next()){
				Map<OWLAxiom, Pair<Integer, Integer>> m = new LinkedHashMap<OWLAxiom, Pair<Integer,Integer>>();
				m.put(asOWLAxiom(rs.getString(2)), new Pair<Integer, Integer>(rs.getInt(3), rs.getInt(4)));
				topN.put(rs.getInt(1), m);
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	private Map<OWLAxiom, Pair<Integer, Integer>> getTopNAxiomPatterns(OntologyRepository repository, AxiomTypeCategory axiomType, int n){
		Map<OWLAxiom, Pair<Integer, Integer>> topN = new LinkedHashMap<OWLAxiom, Pair<Integer, Integer>>();
		PreparedStatement ps;
		ResultSet rs;
		try {
			//get number of ontologies
			ps = conn.prepareStatement("SELECT pattern,SUM(occurrences),COUNT(ontology_id) FROM " +
					"Ontology_Pattern OP, Pattern P, Ontology O WHERE " +
					"(P.id=OP.pattern_id AND O.id=OP.ontology_id AND O.repository=? AND P.axiom_type=?) " +
					"GROUP BY P.id ORDER BY SUM(`OP`.`occurrences`) DESC LIMIT ?");
			ps.setString(1, repository.getName());
			ps.setString(2, axiomType.name());
			ps.setInt(3, n);
			rs = ps.executeQuery();
			while(rs.next()){
				topN.put(asOWLAxiom(rs.getString(1)), new Pair<Integer, Integer>(rs.getInt(2), rs.getInt(3)));
			}
		} catch(SQLException e){
			e.printStackTrace();
		}
		return topN;
	}
	
	private Map<OntologyRepository, Map<OWLAxiom, Integer>> getTopNAxiomPatterns(Collection<OntologyRepository> repositories, AxiomTypeCategory axiomType, int n){
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
	
	private OWLAxiom asOWLAxiom(String functionalSyntaxAxiomString){
		try {
			StringDocumentSource s = new StringDocumentSource("Ontology(<http://www.pattern.org> " + functionalSyntaxAxiomString + ")");
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
			System.err.println("Parsing failed for axiom " + functionalSyntaxAxiomString);
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
	
	private double tf_idf(int nrOfOccurrences, int nrOfOntologies, int nrOfOntologiesWithAxiom){
		double tf = nrOfOccurrences;
		double idf = Math.log10(nrOfOntologies / nrOfOntologiesWithAxiom);
		return tf * idf;
	}
	
	private double popularity(int totalNrOfOntologies, int patternFrequency, int nrOfOntologiesWithPattern){
		return (double)Math.log10(patternFrequency) * (nrOfOntologiesWithPattern / (double)totalNrOfOntologies);
	}
	
	public static void main(String[] args) throws Exception {
//		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		boolean analyzeRepositories = true;
		if(args.length == 1){
			analyzeRepositories = Boolean.parseBoolean(args[0]);
		}
		new OWLAxiomPatternDetectionEvaluation().run(analyzeRepositories, Arrays.asList(
				new TONESRepository(), new BioPortalRepository(), new OxfordRepository()));
	}
	

}
