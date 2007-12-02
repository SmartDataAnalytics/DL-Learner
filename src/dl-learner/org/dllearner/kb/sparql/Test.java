package org.dllearner.kb.sparql;

import java.io.File;
import java.io.FileWriter;
import java.net.URI;

public class Test {

	public static void main(String[] args) {
		System.out.println("Start");
		String test2 = "http://www.extraction.org/config#dbpediatest";
		String test = "http://www.extraction.org/config#localjoseki";
		try {
			URI u = new URI(test);
			Manager m = new Manager();
			m.usePredefinedConfiguration(u);

			URI u2 = new URI("http://dbpedia.org/resource/Angela_Merkel");

			String filename = System.currentTimeMillis() + ".nt";
			FileWriter fw = new FileWriter(new File(filename), true);
			fw.write(m.extract(u2));
			fw.flush();
			fw.close();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
