package org.dllearner.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Testklasse um Ontology Class Learning Interface zu testen.
 * 
 * @author jl
 * 
 */
public class DLLearnerClientTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Socket server = null;

		try {
			server = new Socket("localhost", 3141);
			InputStream in = server.getInputStream();
			OutputStream out = server.getOutputStream();

			out.write(4);
			out.write(9);
			int result = in.read();
			System.out.println(result);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (server != null)
				try {
					server.close();
				} catch (IOException e) {
				}
		}
	}

}
