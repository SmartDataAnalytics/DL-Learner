package org.dllearner.reasoning;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.xmlbeans.XmlException;
import org.dllearner.Config;
import org.dllearner.Main;
import org.kr.dl.dig.v1_1.GetIdentifierDocument;
import org.kr.dl.dig.v1_1.IdRespType;
import org.kr.dl.dig.v1_1.IdentifierDocument;
import org.kr.dl.dig.v1_1.NewKBDocument;
import org.kr.dl.dig.v1_1.ReleaseKBDocument;
import org.kr.dl.dig.v1_1.ResponseDocument;
import org.kr.dl.dig.v1_1.ResponsesDocument;
import org.kr.dl.dig.v1_1.KbDocument.Kb;

/**
 * Methods for sending messages to a DIG-capable reasoner and receiving answers
 * using Apache XML Beans.
 * 
 * @author jl
 * 
 */
public class DIGHTTPConnector {

	private URL url;

	public DIGHTTPConnector(URL url) {
		this.url = url;
	}
 
	public URI newKB() {
		NewKBDocument newKB = NewKBDocument.Factory.newInstance();
		newKB.addNewNewKB();
		String answer = sendAndReceive(newKB.toString());
		ResponseDocument rd = parse(answer);
		
		IdRespType rt = rd.getResponse();
		Kb kb = rt.getKb();
		String uriStr = kb.getUri();
		
		URI uri = null;
		try {
			uri = new URI(uriStr);
		} catch (URISyntaxException e) {
			System.out.println("Reasoner did not provide a valid URI.");
			e.printStackTrace();
			System.exit(0);
		}
		
		return uri;
	}

	public boolean releaseKB(URI kbURI) {
		ReleaseKBDocument releaseKB = ReleaseKBDocument.Factory.newInstance();
		releaseKB.addNewReleaseKB().setUri(kbURI.toString());
		String answer = sendAndReceive(releaseKB.toString());
		ResponseDocument rd = parse(answer);
		return rd.getResponse().isSetOk();
	}

	public String getIdentifier() {
		GetIdentifierDocument gid = GetIdentifierDocument.Factory.newInstance();
		gid.addNewGetIdentifier();
		String answer = sendAndReceive(gid.toString());
		IdentifierDocument id = null;
		try {			
			id = IdentifierDocument.Factory.parse(answer);
		} catch (XmlException e) {
			System.out.println("DIG-Reasoner does not identify properly.");
			e.printStackTrace();
			System.exit(0);
		}
		
		return id.getIdentifier().getName() + " (version " + id.getIdentifier().getVersion() + ")";
	}

	// tell-Anfrage als XML-String schicken
	public ResponseDocument tells(String tells) {
		return parse(sendAndReceive(tells));
	}

	private int askCounter = 0;
	
	// asks-Anfrage als XML-String schicken
	public ResponsesDocument asks(String asks) {
		askCounter++;
		
		String answer = sendAndReceive(asks);
		ResponsesDocument rd = null;
		try {
			rd = ResponsesDocument.Factory.parse(answer);
		} catch (XmlException e) {
			e.printStackTrace();
			System.err.println("Exception occured when receiving the following string:\n" + answer);
		}
		return rd;
	}

	private ResponseDocument parse(String answer) {
		ResponseDocument rd = null;
		try {
			rd = ResponseDocument.Factory.parse(answer);
		} catch (XmlException e) {
			e.printStackTrace();
		}
		return rd;
	}
	
	private String sendAndReceive(String send) {
		StringBuilder answer = new StringBuilder();	
		
		// String an DIG-Reasoner schicken
		HttpURLConnection connection;
			
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			
			OutputStream os = connection.getOutputStream();
			OutputStreamWriter osw = new OutputStreamWriter(os);
			osw.write(send);
			osw.close();
			
			if(Config.writeDIGProtocol)
				Main.appendFile(Config.digProtocolFile, "DIG code send to reasoner:\n\n"+send+"\n\n");
			
			// Antwort empfangen
			InputStream is = connection.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			
			String line;
			do {
				line = br.readLine();
				if(line!=null)
					answer.append(line);
			} while (line != null);
			
			br.close();

		} catch (IOException e) {		
			System.out.println("Communication problem with DIG Reasoner. Please make sure there is a DIG reasoner running at " + url + " and try again.");
			System.exit(0);
		}	
		
		// DIG-Kommunikation protokollieren, falls das eingestellt ist
		if(Config.writeDIGProtocol)
			Main.appendFile(Config.digProtocolFile, "DIG code received from reasoner:\n\n"+answer+"\n\n");
		
		return answer.toString();
	}
	
}
