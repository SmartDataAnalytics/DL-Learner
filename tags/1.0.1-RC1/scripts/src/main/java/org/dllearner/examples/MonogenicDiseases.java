/**
 * Copyright (C) 2007-2009, Jens Lehmann
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
 *
 */
package org.dllearner.examples;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypePropertyDomainAxiom;
import org.dllearner.core.owl.DatatypePropertyRangeAxiom;
import org.dllearner.core.owl.DoubleDatatypePropertyAssertion;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Files;
import org.dllearner.utilities.Helper;
import org.ini4j.IniFile;
import org.semanticweb.owlapi.model.IRI;

/**
 * Converts SM2PH database to an OWL ontology. To run the script, please 
 * copy mutant.ini.dist to mutant.ini first and adapt the database connection.
 * 
 * @author Jens Lehmann
 *
 */
public class MonogenicDiseases {

//---> OPTIONS:
	private static int algorithmus = 1;  				// 1 = pos/neg Algorithmus    2 = Celoe
	private static boolean writeAlephFiles = true;		// Add AlephFiles
	private static boolean writeYYFiles = true;			// Add YinYangFiles
	private static int posEx = 25;						// Number of pos-Ex.
	private static int negEx = 25;						// Number of neg-Ex.
	private static boolean cvAleph = true;				// add Files for Aleph 10-Fold Cross Validation
	
//<--- OPTIONS end	
	
	private static IRI ontologyURI = IRI.create("http://dl-learner.org/mutation");
	private static File owlFile = new File("examples/mutation/mutation.owl");
	private static File confFile = new File("examples/mutation/mutation.conf");
	private static File badFile = new File("examples/mutation/mutation1.n");	// AlephFile
	private static File posFile = new File("examples/mutation/mutation1.f");	// AlephFile
	private static File yybadFile = new File("examples/mutation/mutationNegatives.txt"); //YYFile
	private static File yyposFile = new File("examples/mutation/mutationPositives.txt"); //YYFile

	private static File split1n = new File("examples/mutation/split1.n");
	private static File split1f = new File("examples/mutation/split1.f");
	private static File split2n = new File("examples/mutation/split2.n");
	private static File split2f = new File("examples/mutation/split2.f");
	private static File split3n = new File("examples/mutation/split3.n");
	private static File split3f = new File("examples/mutation/split3.f");
	private static File split4n = new File("examples/mutation/split4.n");
	private static File split4f = new File("examples/mutation/split4.f");
	private static File split5n = new File("examples/mutation/split5.n");
	private static File split5f = new File("examples/mutation/split5.f");
	private static File split6n = new File("examples/mutation/split6.n");
	private static File split6f = new File("examples/mutation/split6.f");
	private static File split7n = new File("examples/mutation/split7.n");
	private static File split7f = new File("examples/mutation/split7.f");
	private static File split8n = new File("examples/mutation/split8.n");
	private static File split8f = new File("examples/mutation/split8.f");
	private static File split9n = new File("examples/mutation/split9.n");
	private static File split9f = new File("examples/mutation/split9.f");
	private static File split10n = new File("examples/mutation/split10.n");
	private static File split10f = new File("examples/mutation/split10.f");

	
	
	// whether to generate a class containing the positive examples
	private static boolean generatePosExampleClass = false;
	// set to true if accessing PostreSQL and false for MySQL
	private static boolean pgSQL = true;
	// generate fragment => limits the number of individuals in the ontology 
	// to speed up learning
	private static boolean learnMutation = true; 
	// private static boolean onlyFragment = true;
	
	public static void main(String[] args) throws ClassNotFoundException, BackingStoreException, SQLException {

		if (algorithmus == 1) {							// LearnAlgo-Check					
			generatePosExampleClass = false;
		}
		
		if (algorithmus == 2) {							// LearnAlgo-Check
			generatePosExampleClass = true;
		}
		
		
		// reading values for db connection from ini file
		String iniFile = "src/dl-learner/org/dllearner/examples/mutation.ini";
		Preferences prefs = new IniFile(new File(iniFile));
		String dbServer = prefs.node("database").get("server", null);
		String dbName = prefs.node("database").get("db", null);
		String dbPort = prefs.node("database").get("port", null);
		String dbUser = prefs.node("database").get("user", null);
		String dbPass = prefs.node("database").get("pass", null);
		String table = prefs.node("database").get("table", null);
		
		// connect to database
		String url = "jdbc:";
		if(pgSQL) {
			Class.forName("org.postgresql.Driver");
			// adapt the port if necessary
			url += "postgresql://"+dbServer+":"+dbPort+"/"+dbName;
		} else {
			Class.forName("com.mysql.jdbc.Driver");
			url += "mysql://"+dbServer+":"+dbPort+"/"+dbName;
		}
		Connection conn = DriverManager.getConnection(url, dbUser, dbPass);
		System.out.println("Successfully connected to database.");
		
		// prepare ontology
		long startTime = System.nanoTime();
		KB kb = new KB();
		NamedClass mutationClass = new NamedClass(getURI("Mutation")); 
		
		NamedClass deleteriousMutationClass = new NamedClass(getURI("DeletoriousMutation"));
		
		// size change
		NamedClass protSizeIncClass = new NamedClass(getURI("ProteinSizeIncreasingMutation"));
		NamedClass protSizeUnchangedClass = new NamedClass(getURI("ProteinSizeUnchangedMutation"));
		NamedClass protSizeDecClass = new NamedClass(getURI("ProteinSizeDecreasingMutation"));
		kb.addAxiom(new SubClassAxiom(protSizeIncClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protSizeUnchangedClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protSizeDecClass, mutationClass));
		
		// charge
		NamedClass protChargeIncClass = new NamedClass(getURI("ProteinChargeIncreasingMutation"));
		NamedClass protChargeUnchangedClass = new NamedClass(getURI("ProteinChargeUnchangedMutation"));
		NamedClass protChargeDecClass = new NamedClass(getURI("ProteinChargeDecreasingMutation"));
		NamedClass protChargeChangedClass = new NamedClass(getURI("ProteinChargeChangedMutation"));
		kb.addAxiom(new SubClassAxiom(protChargeIncClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protChargeUnchangedClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protChargeDecClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protChargeChangedClass, mutationClass));
		// TODO: maybe inc and plus are subclasses of changed
		
		// hydrophobicity
		NamedClass protHydroIncClass = new NamedClass(getURI("ProteinHydroIncreasingMutation"));
		NamedClass protHydroUnchangedClass = new NamedClass(getURI("ProteinHydroUnchangedMutation"));
		NamedClass protHydroDecClass = new NamedClass(getURI("ProteinHydroDecreasingMutation"));
		kb.addAxiom(new SubClassAxiom(protHydroIncClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protHydroUnchangedClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protHydroDecClass, mutationClass));
		
		// polarity
		NamedClass protPolIncClass = new NamedClass(getURI("ProteinPolarityIncreasingMutation"));
		NamedClass protPolUnchangedClass = new NamedClass(getURI("ProteinPolarityUnchangedMutation"));
		NamedClass protPolDecClass = new NamedClass(getURI("ProteinPolarityDecreasingMutation"));
		kb.addAxiom(new SubClassAxiom(protPolIncClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protPolUnchangedClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(protPolDecClass, mutationClass));		
		
		// score
		DatatypeProperty scoreProp = new DatatypeProperty(getURI("modifScore"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(scoreProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(scoreProp, Datatype.DOUBLE));
		
		// g_p
		NamedClass gpIncClass = new NamedClass(getURI("GPIncreasingMutation"));
		NamedClass gpUnchangedClass = new NamedClass(getURI("GPUnchangedMutation"));
		NamedClass gpDecClass = new NamedClass(getURI("GPDecreasingMutation"));
		kb.addAxiom(new SubClassAxiom(gpIncClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(gpUnchangedClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(gpDecClass, mutationClass));		
		
		// conservation_wt
		DatatypeProperty conservationWTProp = new DatatypeProperty(getURI("convservationWT"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(conservationWTProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(conservationWTProp, Datatype.DOUBLE));		
		
		// conservation_mut
		DatatypeProperty conservationMutProp = new DatatypeProperty(getURI("convservationMut"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(conservationMutProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(conservationMutProp, Datatype.DOUBLE));		
		
		// freq_at_pos
		DatatypeProperty freqAtPosProp = new DatatypeProperty(getURI("freqAtPos"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(freqAtPosProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(freqAtPosProp, Datatype.DOUBLE));			
		
		// cluster_5res_size
		DatatypeProperty cluster5ResSizeProp = new DatatypeProperty(getURI("cluster5resSize"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(cluster5ResSizeProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(cluster5ResSizeProp, Datatype.DOUBLE));		
		
		// secondary_struc
		NamedClass scHelixClass = new NamedClass(getURI("SCHelixMutation"));
		NamedClass scSheetClass = new NamedClass(getURI("SCSheetMutation"));
		NamedClass scUndeterminedClass = new NamedClass(getURI("SCUndeterminedMutation"));
		kb.addAxiom(new SubClassAxiom(scHelixClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(scSheetClass, mutationClass));
		kb.addAxiom(new SubClassAxiom(scUndeterminedClass, mutationClass));			
		
		// gain_contact
		DatatypeProperty gainContactProp = new DatatypeProperty(getURI("gainContact"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(gainContactProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(gainContactProp, Datatype.DOUBLE));				
		
		// lost_contact
		DatatypeProperty lostContactProp = new DatatypeProperty(getURI("lostContact"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(lostContactProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(lostContactProp, Datatype.DOUBLE));				
			
		// identical_contact
		DatatypeProperty identicalContactProp = new DatatypeProperty(getURI("identicalContact"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(identicalContactProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(identicalContactProp, Datatype.DOUBLE));				
			
		// gain_n1_contact
		DatatypeProperty gainN1ContactProp = new DatatypeProperty(getURI("gainN1Contact"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(gainN1ContactProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(gainN1ContactProp, Datatype.DOUBLE));				
		
		// lost_n1_contact
		DatatypeProperty lostN1ContactProp = new DatatypeProperty(getURI("lostN1Contact"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(lostN1ContactProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(lostN1ContactProp, Datatype.DOUBLE));				
			
		// identical_n1_contact
		DatatypeProperty identicalN1ContactProp = new DatatypeProperty(getURI("identicalN1Contact"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(identicalN1ContactProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(identicalN1ContactProp, Datatype.DOUBLE));				
			
		// wt_accessibility
		DatatypeProperty wtAccessibilityProp = new DatatypeProperty(getURI("wtAccessibility"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(wtAccessibilityProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(wtAccessibilityProp, Datatype.DOUBLE));				
					
		// mut_accessibility
		DatatypeProperty mutAccessibilityProp = new DatatypeProperty(getURI("mutAccessibility"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(mutAccessibilityProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(mutAccessibilityProp, Datatype.DOUBLE));				
					
		// cluster3d_10
		DatatypeProperty cluster3D10Prop = new DatatypeProperty(getURI("cluster3d10"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(cluster3D10Prop, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(cluster3D10Prop, Datatype.DOUBLE));				
				
		// cluster3d_20
		DatatypeProperty cluster3D20Prop = new DatatypeProperty(getURI("cluster3d20"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(cluster3D20Prop, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(cluster3D20Prop, Datatype.DOUBLE));				
		
		// cluster3d_30
		DatatypeProperty cluster3D30Prop = new DatatypeProperty(getURI("cluster3d30"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(cluster3D30Prop, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(cluster3D30Prop, Datatype.DOUBLE));				
				
		// TODO: stability missing
		
		// reliability_deltag
		DatatypeProperty reliabilityDeltagProp = new DatatypeProperty(getURI("reliabilityDeltag"));
		kb.addAxiom(new DatatypePropertyDomainAxiom(reliabilityDeltagProp, mutationClass));
		kb.addAxiom(new DatatypePropertyRangeAxiom(reliabilityDeltagProp, Datatype.DOUBLE));				
			
		if(generatePosExampleClass) {
			kb.addAxiom(new SubClassAxiom(deleteriousMutationClass, mutationClass));
		}		
		
		// select data (restricted to pos/neg examples for efficiency)
		Statement stmt = conn.createStatement();
		ResultSet rs = null;
		if(pgSQL) {
			// join tables
			rs = stmt.executeQuery("SELECT * FROM fiche_mutant, mutants WHERE fiche_mutant.id = mutants.id AND(gain_contact is not null)");
		} else {
			rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE (gain_contact is not null) AND (gain_contact != 0)");
		}
		
int count = 1;
int pos_count = 0;
int neg_count = 1;


	if (writeAlephFiles){			// Clear AlephFiles
		Files.clearFile(badFile);
		Files.clearFile(posFile);
	}

	if (writeYYFiles) {				// Clear YinYangFiles
		Files.clearFile(yybadFile);
		Files.clearFile(yyposFile);
	}
	
	if (cvAleph) {					// Clear Aleph CV-Files
		Files.clearFile(split1n);
		Files.clearFile(split1f);
		Files.clearFile(split2n);
		Files.clearFile(split2f);
		Files.clearFile(split3n);
		Files.clearFile(split3f);
		Files.clearFile(split4n);
		Files.clearFile(split4f);
		Files.clearFile(split5n);
		Files.clearFile(split5f);
		Files.clearFile(split6n);
		Files.clearFile(split6f);
		Files.clearFile(split7n);
		Files.clearFile(split7f);
		Files.clearFile(split8n);
		Files.clearFile(split8f);
		Files.clearFile(split9n);
		Files.clearFile(split9f);
		Files.clearFile(split10n);
		Files.clearFile(split10f);
	}
	
	int kp = 1;		// k for CV positiv
	int kn = 1;		// k for CV negativ
	int peps = posEx/10;	// positiv Examples per Splitfile
	int neps = negEx/10;   	// negative Examples per Splitfile
	int aneg = 0;			// aktuell negativ
	int apos = 0;			// aktuell positiv
	
	
	while((rs.next()) && ((pos_count < posEx) || (neg_count < negEx))) {
			

			
			// generate an individual for each entry in the table
			int mutationID = rs.getInt("id");
			Individual mutationInd = new Individual(getURI("mutation" + mutationID));

			
			String pt = rs.getString("phenotype");	
			if ((pt.toLowerCase().contains("polymorphism"))&&( neg_count<=negEx )) { 
				if (writeAlephFiles) {
					Files.appendFile(badFile, "deleterious(id"+mutationID+").\n");
				}
				if (writeYYFiles) {
					Files.appendFile(yybadFile, "http://example.com/mutation/mutation#mutation"+mutationID+"\n");
				}
				if (cvAleph){
					switch (kn) {					
						case 1: 
							aneg++;
							Files.appendFile(split1n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 2: 
							aneg++;
							Files.appendFile(split2n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 3: 
							aneg++;
							Files.appendFile(split3n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 4: 
							aneg++;
							Files.appendFile(split4n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 5: 
							aneg++;
							Files.appendFile(split5n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 6: 
							aneg++;
							Files.appendFile(split6n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 7: 
							aneg++;
							Files.appendFile(split7n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 8: 
							aneg++;
							Files.appendFile(split8n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 9: 
							aneg++;
							Files.appendFile(split9n, "deleterious(id"+mutationID+").\n");
							if (aneg == neps) {aneg = 0; kn++;}
							break;
						case 10: 
							aneg++;
							Files.appendFile(split10n, "deleterious(id"+mutationID+").\n");
//							if (aneg == neps) {aneg = 0; kn++;}
							break;			
//						case 11: 						// without comment its round negExamples / 10
//							break;
					}
				}
				
				neg_count++;
				}
			if ((!pt.toLowerCase().contains("polymorphism"))&& ( pos_count<=posEx)) { 
				if (writeAlephFiles) {
					Files.appendFile(posFile, "deleterious(id"+mutationID+").\n");
				}
				if (writeYYFiles) {
				Files.appendFile(yyposFile, "http://example.com/mutation/mutation#mutation"+mutationID+"\n");
				}
				if (cvAleph){
					switch (kp) {					
						case 1: 
							apos++;
							Files.appendFile(split1f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 2: 
							apos++;
							Files.appendFile(split2f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 3: 
							apos++;
							Files.appendFile(split3f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 4: 
							apos++;
							Files.appendFile(split4f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 5: 
							apos++;
							Files.appendFile(split5f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 6: 
							apos++;
							Files.appendFile(split6f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 7: 
							apos++;
							Files.appendFile(split7f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 8: 
							apos++;
							Files.appendFile(split8f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 9: 
							apos++;
							Files.appendFile(split9f, "deleterious(id"+mutationID+").\n");
							if (apos == peps) {apos = 0; kp++;}
							break;
						case 10: 
							apos++;
							Files.appendFile(split10f, "deleterious(id"+mutationID+").\n");
//							if (apos == peps) {apos = 0; kp++;}
							break;
//						case 11:				// without comments its round to posExamples / 10	
//							break;			
					}
				}

				
				pos_count++; 
			}
			
			if ((pt.toLowerCase().contains("polymorphism"))&&( neg_count > negEx )) { 
				continue;
				}
			if ((!pt.toLowerCase().contains("polymorphism"))&& ( pos_count > posEx)) { 
				continue;	
			}
			
			
			// size change is represented via 3 classes
			String modifSize = rs.getString("modif_size");
			convertThreeValuedColumn(kb, mutationInd, modifSize, protSizeIncClass, protSizeUnchangedClass, protSizeDecClass);
			
			// charge is done via 4 classes
			String modifCharge = rs.getString("modif_charge");
			if(modifCharge.equals("+")) {
				kb.addAxiom(new ClassAssertionAxiom(protChargeIncClass, mutationInd));
			} else if(modifCharge.equals("=")) {
				kb.addAxiom(new ClassAssertionAxiom(protChargeUnchangedClass, mutationInd));
			} else if(modifCharge.equals("-")) {
				kb.addAxiom(new ClassAssertionAxiom(protChargeDecClass, mutationInd));
			} else if(modifCharge.equals("!=")) {
				kb.addAxiom(new ClassAssertionAxiom(protChargeChangedClass, mutationInd));
			}		

			// hydro...
			String modifHydro = rs.getString("modif_hydrophobicity");
			convertThreeValuedColumn(kb, mutationInd, modifHydro, protHydroIncClass, protHydroUnchangedClass, protHydroDecClass);
					
			// polarity
			String modifPolarity = rs.getString("modif_polarity");
			convertThreeValuedColumn(kb, mutationInd, modifPolarity, protPolIncClass, protPolUnchangedClass, protPolDecClass);

			// modif_score
			double modifScore = rs.getDouble("modif_score");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(scoreProp, mutationInd, modifScore));
			
			// g_p
			String gp = rs.getString("g_p");
			convertThreeValuedColumn(kb, mutationInd, gp, gpIncClass, gpUnchangedClass, gpDecClass);			
			
			// modif_score
			double conservationWT = rs.getDouble("conservation_wt");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(conservationWTProp, mutationInd, conservationWT));
			
			// modif_score
			double conservationMut = rs.getDouble("conservation_mut");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(conservationMutProp, mutationInd, conservationMut));
			
			// freq_at_pos
			double freqAtPos = rs.getDouble("freq_at_pos");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(freqAtPosProp, mutationInd, freqAtPos));
						
			// freq_at_pos
			double cluster5ResSize = rs.getDouble("cluster_5res_size");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(cluster5ResSizeProp, mutationInd, cluster5ResSize));
						
			// secondary struc
			String secStruc = rs.getString("secondary_struc");
			if(secStruc.equals("HELIX")) {
				kb.addAxiom(new ClassAssertionAxiom(scHelixClass, mutationInd));
			} else if(secStruc.equals("SHEET")) {
				kb.addAxiom(new ClassAssertionAxiom(scSheetClass, mutationInd));
			} else if(secStruc.equals("0")) {
				kb.addAxiom(new ClassAssertionAxiom(scUndeterminedClass, mutationInd));
			}
			
			// TODO: Wert null soll hier auch vorkommen, aber existiert in der DB nicht.
			// gain_contact
			double gainContact = rs.getDouble("gain_contact");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(gainContactProp, mutationInd, gainContact));
						
			// lost_contact
			double lostContact = rs.getDouble("lost_contact");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(lostContactProp, mutationInd, lostContact));
				
			// identical_contact
			double identicalContact = rs.getDouble("identical_contact");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(identicalContactProp, mutationInd, identicalContact));
						
			// gain_n1_contact
			double gainN1Contact = rs.getDouble("gain_n1_contact");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(gainN1ContactProp, mutationInd, gainN1Contact));
						
			// lost_n1_contact
			double lostN1Contact = rs.getDouble("lost_n1_contact");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(lostN1ContactProp, mutationInd, lostN1Contact));
				
			// identical_n1_contact
			double identicalN1Contact = rs.getDouble("identical_n1_contact");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(identicalN1ContactProp, mutationInd, identicalN1Contact));
						
			// TODO Vorsicht bei 0-Werten in den weitern Feldern (klaeren, ob in dem
			// Fall gar nichts geschrieben werden soll)
			
			// wt_accessibility
			double wtAccessibility = rs.getDouble("wt_accessibility");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(wtAccessibilityProp, mutationInd, wtAccessibility));
						
			// mut_accessibility
			double mutAccessibility = rs.getDouble("mut_accessibility");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(mutAccessibilityProp, mutationInd, mutAccessibility));
				
			// cluster3d_10
			double cluster3D10 = rs.getDouble("cluster3d_10");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(cluster3D10Prop, mutationInd, cluster3D10));
					
			// cluster3d_20
			double cluster3D20 = rs.getDouble("cluster3d_20");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(cluster3D20Prop, mutationInd, cluster3D20));
			
			// cluster3d_30
			double cluster3D30 = rs.getDouble("cluster3d_30");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(cluster3D30Prop, mutationInd, cluster3D30));
					
			// TODO: stability missing
			
			// reliability_deltag
			double reliabilityDeltag = rs.getDouble("reliability_deltag");
			kb.addAxiom(new DoubleDatatypePropertyAssertion(reliabilityDeltagProp, mutationInd, reliabilityDeltag));
						
			// generate a class with all positive examples (optional)
			if(generatePosExampleClass) {
				String phenotype = rs.getString("phenotype");

						
				if(!phenotype.toLowerCase().contains("polymorphism")) {
					kb.addAxiom(new ClassAssertionAxiom(deleteriousMutationClass, mutationInd));
				}
				
			}

//			String pt = rs.getString("phenotype");	
//			if (pt.toLowerCase().contains("polymorphism")) { neg_count++; }
//			if (!pt.toLowerCase().contains("polymorphism")) { pos_count++; }
			
			
			count++;
		}
		
		// writing generated knowledge base
		System.out.print("Writing OWL file ... ");
		long startWriteTime = System.nanoTime();
		OWLAPIReasoner.exportKBToOWL(owlFile, kb, ontologyURI);
		long writeDuration = System.nanoTime() - startWriteTime;
		System.out.println("OK (entities: " + count + "; time: " + Helper.prettyPrintNanoSeconds(writeDuration) + "; file size: " + owlFile.length()/1024 + " KB).");		


		
		// selecting examples
		// -> only a fraction of examples are selected as positive/negative
		if(pgSQL) {
			rs = stmt.executeQuery("SELECT * FROM fiche_mutant, mutants WHERE fiche_mutant.id=mutants.id AND " //lower(phenotype) not like 'polymorphism' AND "
					+ " (gain_contact is not null)"); //  AND (gain_contact != 0)");
		} else {
			rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE " //lower(phenotype) not like 'polymorphism' AND "
					+ " (gain_contact is not null) AND (gain_contact != 0)");
		}
		List<Individual> posExamples = new LinkedList<Individual>();
		List<Individual> negExamples = new LinkedList<Individual>();
		
count = 1;
pos_count = 0;
neg_count = 0;



		while((rs.next()) && (pos_count < posEx) || (neg_count < negEx)) {
			int mutationID = rs.getInt("id");
			String phenotype = rs.getString("phenotype");
//			if(phenotype.toLowerCase().contains("polymorphism")) {
//				negExamples.add(new Individual(getURI("mutation" + mutationID))); neg_count++;
//			} else {
//				posExamples.add(new Individual(getURI("mutation" + mutationID))); pos_count++;
//			}

			if((phenotype.toLowerCase().contains("polymorphism")) && (neg_count < negEx)) {
				negExamples.add(new Individual(getURI("mutation" + mutationID))); neg_count++;
			} 
			if((!phenotype.toLowerCase().contains("polymorphism")) && (pos_count < posEx)) {
				posExamples.add(new Individual(getURI("mutation" + mutationID))); pos_count++;
			}
			
			
count++;
		}
			
		// writing conf file
		Files.clearFile(confFile);
		String confHeader = "import(\"" + owlFile.getName() + "\");\n\n";
		confHeader += "reasoner = fastInstanceChecker;\n";

		if (algorithmus == 1) {
			confHeader += "problem = posNegLPStandard;\n";
			confHeader += "posNegLPStandard.useApproximations = true;\n";
			confHeader += "posNegLPStandard.accuracyMethod = \"fmeasure\";\n";	
			confHeader += "posNegLPStandard.approxAccuracy = 0.03;\n";
			confHeader += "refexamples.noisePercentage = 15;\n";
			confHeader += "refexamples.startClass = \"" + getURI("Mutation") + "\";\n";
			confHeader += "refexamples.writeSearchTree = false;\n";
			confHeader += "refexamples.searchTreeFile = \"log/mutation/searchTree.log\";\n";
		
			confHeader += "\n";
		}
		
		if (algorithmus == 2)
		{
			confHeader += "problem = classLearning;\n";
			confHeader += "classLearning.classToDescribe = \"" + deleteriousMutationClass + "\";\n";
			confHeader += "classLearning.accuracyMethod = \"fmeasure\";\n";	
			confHeader += "classLearning.approxAccuracy = 0.03;\n";
			confHeader += "algorithm = celoe;\n";
			confHeader += "celoe.maxExecutionTimeInSeconds = 10;\n";
			confHeader += "celoe.noisePercentage = 10;\n";
			confHeader += "celoe.singleSuggestionMode = true;\n";
			confHeader += "celoe.useNegation = true;\n";

			confHeader += "\n";
		}
		
		Files.appendFile(confFile, confHeader);
		if(!generatePosExampleClass) {
			MonogenicDiseases.appendPosExamples(confFile, posExamples);
			MonogenicDiseases.appendNegExamples(confFile, negExamples);
		}	
		
		long runTime = System.nanoTime() - startTime;
		System.out.println("Conf file written with " + posExamples.size() + " positive and " + negExamples.size() + " negative examples.");
		System.out.println("Database successfully converted in " + Helper.prettyPrintNanoSeconds(runTime) + ".");
		
		if (writeAlephFiles) System.out.println("Added Aleph Files.");
		if (writeYYFiles) System.out.println("Added YinYang Files.");
		if (cvAleph) System.out.println("Added Aleph Split Filles for 10-Fold CrossValidation.");
		
	}
	
	// a table column with values "+", "=", "-" is converted to subclasses
	private static void convertThreeValuedColumn(KB kb, Individual mutationInd, String value, NamedClass plusClass, NamedClass equalClass, NamedClass minusClass) {
		if(value.equals("+")) {
			kb.addAxiom(new ClassAssertionAxiom(plusClass, mutationInd));
		} else if(value.equals("=")) {
			kb.addAxiom(new ClassAssertionAxiom(equalClass, mutationInd));
		} else if(value.equals("-")) {
			kb.addAxiom(new ClassAssertionAxiom(minusClass, mutationInd));
		}		
	}
	
	private static String getURI(String name) {
		return ontologyURI + "#" + name;
	}
	
	public static void appendPosExamples(File file, List<Individual> examples) {
		StringBuffer content = new StringBuffer();
		for(Individual example : examples) {
			if(learnMutation)
				content.append("+\""+example.toString()+"\"\n");
			else
				content.append("-\""+example.toString()+"\"\n");
		}
		Files.appendFile(file, content.toString());
	}
	
	public static void appendNegExamples(File file, List<Individual> examples) {
		StringBuffer content = new StringBuffer();
		for(Individual example : examples) {
			if(learnMutation)
				content.append("-\""+example.toString()+"\"\n");
			else
				content.append("+\""+example.toString()+"\"\n");
		}
		Files.appendFile(file, content.toString());
	}	
}
