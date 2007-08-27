package org.dllearner.server;



import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.xml.ws.Endpoint;

import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpServer;


public class DLLearnerWSStart {

	public static void main(String[] args) {
		//String url = "http://139.18.114.78:8181/services";
		/*String url="";
		if (args.length > 0)
			url = args[0];*/
		try{
		
		InetSocketAddress isa=new InetSocketAddress("localhost",8181);
		HttpServer server = HttpServer.create(isa, 5);
        ExecutorService threads  = Executors.newFixedThreadPool(5);
        server.setExecutor(threads);
        server.start();
		
		System.out.print("Starting DL-Learner web service at http://" + 
				isa.getHostName()+":"+isa.getPort()+ "/services ... ");
		 Endpoint endpoint = Endpoint.create(new DLLearnerWS());
		//Endpoint endpoint = Endpoint.create(new CustomDataClass());
		HttpContext context = server.createContext("/services");
	     endpoint.publish(context);
		//Endpoint endpoint = Endpoint.publish(url, new DLLearnerWS());
		
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
		
        server.stop(1);
        threads.shutdown();
		System.out.println("OK.");
		}catch (Exception e) {e.printStackTrace();}
	}

}
