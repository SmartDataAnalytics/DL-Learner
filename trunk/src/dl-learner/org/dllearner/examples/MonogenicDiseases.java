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
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.dllearner.core.owl.ClassAssertionAxiom;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.SubClassAxiom;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.dllearner.utilities.Helper;
import org.ini4j.IniFile;

/**
 * Converts SM2PH database to an OWL ontology. To run the script, please 
 * copy mutant.ini.dist to mutant.ini first and adapt the database connection.
 * 
 * @author Jens Lehmann
 *
 */
public class MonogenicDiseases {
	
	private static URI ontologyURI = URI.create("http://dl-learner.org/mutation");
	private static File owlFile = new File("examples/mutation/mutation.owl");
	
	public static void main(String[] args) throws ClassNotFoundException, BackingStoreException, SQLException {
		
		// reading values for db connection from ini file
		String iniFile = "src/dl-learner/org/dllearner/examples/mutation.ini";
		Preferences prefs = new IniFile(new File(iniFile));
		String dbServer = prefs.node("database").get("server", null);
		String dbName = prefs.node("database").get("db", null);
		String dbUser = prefs.node("database").get("user", null);
		String dbPass = prefs.node("database").get("pass", null);
		String table = prefs.node("database").get("table", null);
		
		// connect to database
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://"+dbServer+":3306/"+dbName;
		Connection conn = DriverManager.getConnection(url, dbUser, dbPass);
		System.out.println("Successfully connected to database.");
		
		// prepare ontology
		long startTime = System.nanoTime();
		KB kb = new KB();
		NamedClass mutationClass = new NamedClass(getURI("Mutation")); 
		
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
		
		// select all data
		Statement stmt = conn.createStatement();
		ResultSet rs = stmt.executeQuery("SELECT * FROM " + table);
		
		int count = 0;
		while(rs.next()) {
			// generate an individual for each entry in the table
			int mutationID = rs.getInt("id");
			Individual mutationInd = new Individual(getURI("mutation" + mutationID));
			
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

			// size change is represented via 3 classes
			String modifHydro = rs.getString("modif_hydrophobicity");
			convertThreeValuedColumn(kb, mutationInd, modifHydro, protHydroIncClass, protHydroUnchangedClass, protHydroDecClass);
									
			count++;
		}
		
		// writing generated knowledge base
		System.out.print("Writing OWL file ... ");
		long startWriteTime = System.nanoTime();
		OWLAPIReasoner.exportKBToOWL(owlFile, kb, ontologyURI);
		long writeDuration = System.nanoTime() - startWriteTime;
		System.out.println("OK (time: " + Helper.prettyPrintNanoSeconds(writeDuration) + "; file size: " + owlFile.length()/1024 + " KB).");		
		
		long runTime = System.nanoTime() - startTime;
		System.out.println("Database successfully converted in " + Helper.prettyPrintNanoSeconds(runTime) + ".");
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
}
