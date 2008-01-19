package org.dllearner.kb.sparql.old;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;

import org.dllearner.kb.sparql.configuration.SparqlEndpoint;

public class SparqlQueryConventional {

	boolean print_flag = false;
	SparqlEndpoint sparqlEndpoint;

	public SparqlQueryConventional(SparqlEndpoint sE) {
		this.sparqlEndpoint = sE;
	}

	@Deprecated
	private String sendAndReceiveSPARQL(String sparql) throws IOException {
		p("sendAndReceiveSPARQL");
		StringBuilder answer = new StringBuilder();
		// sparql="SELECT * WHERE {?a ?b ?c}LIMIT 10";

		// String an Sparql-Endpoint schicken
		HttpURLConnection connection;

		// SpecificSparqlEndpoint specificSparqlEndpoint =
		// configuration.getSparqlEndpoint();
		p("URL: " + sparqlEndpoint.getURL());
		// p("Host: "+specificSparqlEndpoint.getHost());

		connection = (HttpURLConnection) sparqlEndpoint.getURL()
				.openConnection();
		connection.setDoOutput(true);

		// connection.addRequestProperty("Host",
		// specificSparqlEndpoint.getHost());
		connection.addRequestProperty("Connection", "close");
		connection
				.addRequestProperty(
						"Accept",
						"text/xml,application/xml,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
		connection.addRequestProperty("Accept-Language",
				"de-de,de;q=0.8,en-us;q=0.5,en;q=0.3");
		connection.addRequestProperty("Accept-Charset", "utf-8;q=1.0");
		connection
				.addRequestProperty(
						"User-Agent",
						"Mozilla/5.0 (Windows; U; Windows NT 5.1; de; rv:1.8.1.4) Gecko/20070515 Firefox/2.0.0.4 Web-Sniffer/1.0.24");

		OutputStream os = connection.getOutputStream();
		OutputStreamWriter osw = new OutputStreamWriter(os);

		// Set<String> s = specificSparqlEndpoint.getParameters().keySet();
		// Iterator<String> it = s.iterator();
		String FullURI = "";
		// while (it.hasNext()) {
		// String element = it.next();
		// FullURI += "" + URLEncoder.encode(element, "UTF-8") + "="
		// +
		// URLEncoder.encode(specificSparqlEndpoint.getParameters().get(element),
		// "UTF-8") + "&";
		// }

		// FullURI += "" + specificSparqlEndpoint.getHasQueryParameter() + "=" +
		// URLEncoder.encode(sparql, "UTF-8");
		p(FullURI);
		osw.write(FullURI);
		osw.close();

		// receive answer
		InputStream is = connection.getInputStream();
		InputStreamReader isr = new InputStreamReader(is, "UTF-8");
		BufferedReader br = new BufferedReader(isr);

		String line;
		do {
			line = br.readLine();
			if (line != null)
				answer.append(line);
		} while (line != null);

		br.close();
		p(answer.toString());
		return answer.toString();
	}

	@Deprecated
	public String getAsXMLString(String queryString) {
		try {
			return sendAndReceiveSPARQL(queryString);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public void p(String str) {
		if (print_flag) {
			System.out.println(str);
		}
	}

}
