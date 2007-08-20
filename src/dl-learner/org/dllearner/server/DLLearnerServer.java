package org.dllearner.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class DLLearnerServer {

	private static void handleConnection(Socket client) throws IOException {
		InputStream in = client.getInputStream();
		OutputStream out = client.getOutputStream();

		int factor1 = in.read();
		int factor2 = in.read();

		out.write(factor1 * factor2);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		ServerSocket server = new ServerSocket(3141);

		while (true) {
			Socket client = null;

			try {
				client = server.accept();
				handleConnection(client);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (client != null)
					try {
						client.close();
					} catch (IOException e) {
					}
			}
		}
	}

}
