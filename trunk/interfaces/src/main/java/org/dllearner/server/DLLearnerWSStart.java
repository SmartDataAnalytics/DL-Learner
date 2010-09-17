/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
package org.dllearner.server;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;

/**
 * Starts the DL-Learner web service.
 * 
 * @author Jens Lehmann
 * @author Sebastian Hellmann
 * 
 */
public class DLLearnerWSStart {

	/**
	 * DL-Learner web service startup method.
	 * 
	 * @param args
	 * --non-interactive starts the web service in a mode, where
	 * it does not wait for user input, i.e. it cannot be terminated
	 * using exit. Use this in conjunction with nohup.
	 */
	public static void main(String[] args) {

		// "interactive" means that the web service waits for the
		// user to type "exit" and exit gracefully; it 
		// non-interactive mode, the web service is started and has
		// to be terminated externally (e.g. killing its process);
		// when using nohup, please use noninteractive mode
		boolean interactive = true;
		if (args.length > 0 && args[0].equals("--non-interactive")) {
			interactive = false;
		}
		
		// create web service logger
		SimpleLayout layout = new SimpleLayout();
		ConsoleAppender consoleAppender = new ConsoleAppender(layout);
		Logger logger = Logger.getRootLogger();

		FileAppender fileAppenderNormal = null;
		File f = new File("log/sparql.txt");
		try {
			fileAppenderNormal = new FileAppender(layout, "log/log.txt", false);
			f.delete();
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.removeAllAppenders();
		logger.addAppender(consoleAppender);
		logger.addAppender(fileAppenderNormal);
		logger.setLevel(Level.INFO);

		InetSocketAddress isa = new InetSocketAddress("localhost", 8181);
		HttpServer server = null;
		try {
			server = HttpServer.create(isa, 0);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		ExecutorService threads = Executors.newFixedThreadPool(10);
		server.setExecutor(threads);
		server.start();

		System.out.print("Starting DL-Learner web service at http://" + isa.getHostName() + ":"
				+ isa.getPort() + "/services ... ");
		Endpoint endpoint = Endpoint.create(new DLLearnerWS());
		// Endpoint endpoint = Endpoint.create(new CustomDataClass());
		HttpContext context = server.createContext("/services");
		endpoint.publish(context);
		// Endpoint endpoint = Endpoint.publish(url, new DLLearnerWS());

		System.out.println("OK.");

		if(interactive) {
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
	
			server.stop(1);
			threads.shutdown();
			System.out.println("OK.");
		}

	}

}
