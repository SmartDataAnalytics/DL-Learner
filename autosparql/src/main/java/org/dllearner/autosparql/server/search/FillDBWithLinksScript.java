package org.dllearner.autosparql.server.search;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.ini4j.IniFile;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandler;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;

public class FillDBWithLinksScript {

	/**
	 * @param args
	 * @throws ClassNotFoundException 
	 * @throws SQLException 
	 * @throws BackingStoreException 
	 * @throws IOException 
	 * @throws RDFHandlerException 
	 * @throws RDFParseException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, SQLException, BackingStoreException, RDFParseException, RDFHandlerException, IOException {
		long startTime = System.currentTimeMillis();
		System.out.println("Writing links to DB...");
		if(args.length == 0){
			System.out.println("Please give the NTriples file with the links as argument.");
			System.exit(0);
		}
		String pagelinks_file = args[0];
		InputStream is = new BufferedInputStream(new FileInputStream(new File(pagelinks_file)));
		
		String iniFile = "settings.ini";
		Preferences prefs = new IniFile(new File(iniFile));
		String dbServer = prefs.node("database").get("server", null);
		String dbName = "pagerank";prefs.node("database").get("name", null);
		String dbUser = prefs.node("database").get("user", null);
		String dbPass = prefs.node("database").get("pass", null);
		Class.forName("com.mysql.jdbc.Driver");
		String url = "jdbc:mysql://"+dbServer+"/"+dbName;
		Connection conn = DriverManager.getConnection(url, dbUser, dbPass);
		Statement stmt = conn.createStatement();
		stmt.execute("DROP TABLE IF EXISTS links");
		stmt.execute("CREATE TABLE IF NOT EXISTS links (node1 VARCHAR(4000), node2 VARCHAR(4000)) ENGINE = MyISAM");
		final PreparedStatement ps = conn.prepareStatement("INSERT INTO links(node1,node2) VALUES(?,?)");
		RDFParser parser = Rio.createParser(RDFFormat.NTRIPLES);
		parser.setRDFHandler(new RDFHandler() {
			String from;
			String to;
			int cnt;
			@Override
			public void startRDF() throws RDFHandlerException {}
			
			@Override
			public void handleStatement(org.openrdf.model.Statement stmt) throws RDFHandlerException {
				try {
					from = stmt.getSubject().stringValue();
					to = stmt.getObject().stringValue();
					ps.setString(1, from);
					ps.setString(2, to);
					ps.addBatch();
					cnt++;
					if(cnt == 10000){
						ps.executeBatch();
						cnt = 0;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
			@Override
			public void handleNamespace(String arg0, String arg1) throws RDFHandlerException {}
			@Override
			public void handleComment(String arg0) throws RDFHandlerException {}
			@Override
			public void endRDF() throws RDFHandlerException {}
		});
		parser.parse(new BufferedInputStream(is), "http://dbpedia.org");
		ps.executeBatch();
		System.out.println("Done in " + (System.currentTimeMillis()-startTime) + "ms.");
	}

}
