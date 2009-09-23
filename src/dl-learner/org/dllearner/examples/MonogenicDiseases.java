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
import java.sql.SQLException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ini4j.IniFile;

/**
 * Converts SM2PH database to an OWL ontology. To run the script, please 
 * copy mutant.ini.dist to mutant.ini first and adapt the database connection.
 * 
 * @author Jens Lehmann
 *
 */
public class MonogenicDiseases {
	
	public static void main(String[] args) throws ClassNotFoundException, BackingStoreException, SQLException {
		
		// reading values for db connection from ini file
		String iniFile = "mutant.ini";
		Preferences prefs = new IniFile(new File(iniFile));
		String dbServer = prefs.node("database").get("server", null);
		String dbName = prefs.node("database").get("db", null);
		String dbUser = prefs.node("database").get("user", null);
		String dbPass = prefs.node("database").get("pass", null);
		
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://"+dbServer+":3306/"+dbName;
		Connection conn = DriverManager.getConnection(url, dbUser, dbPass);

		// TODO converter script
		
		System.out.println("Database successfully converted.");
	}
	
}
