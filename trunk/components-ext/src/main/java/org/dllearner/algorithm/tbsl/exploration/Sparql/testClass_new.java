package org.dllearner.algorithm.tbsl.exploration.Sparql;

import java.net.MalformedURLException;
import java.sql.SQLException;

public class testClass_new {

	/**
	 * @param args
	 * @throws SQLException 
	 * @throws ClassNotFoundException 
	 * @throws MalformedURLException 
	 */
	public static void main(String[] args) throws MalformedURLException, ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		TemplateBuilder testobject = new TemplateBuilder();
		String question = "Which books are written by Daniele Steel?";
		testobject.createTemplates(question);
	}

}
