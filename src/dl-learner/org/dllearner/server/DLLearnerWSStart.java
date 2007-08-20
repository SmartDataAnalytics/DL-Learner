package org.dllearner.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.xml.ws.Endpoint;

public class DLLearnerWSStart {

	public static void main(String[] args) {
		String url = "http://localhost:8181/services";
		if (args.length > 0)
			url = args[0];

		System.out.print("Starting DL-Learner web service at " + url + " ... ");
		Endpoint endpoint = Endpoint.publish(url, new DLLearnerWS());
		System.out.println("OK.");

		System.out.println("Type \"exit\" to terminate web service.");
		boolean terminate = false;
		String inputString = "";
		do {
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			
			try {
				inputString = input.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if (inputString.equals("exit"))
				terminate = true;
			
		} while (!terminate);

		System.out.print("Stopping web service ... ");
		endpoint.stop();
		System.out.println("OK.");
	}

}
