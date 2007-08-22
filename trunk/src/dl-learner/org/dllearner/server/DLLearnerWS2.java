package org.dllearner.server;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

import org.dllearner.server.exceptions.ClientNotKnownException;


/**
 * Offene Fragen:
 * 
 * Welche Rückgabetypen sind erlaubt?
 * Wie behandelt man Exceptions (z.B. aus angegebener URI kann keine Ontologie
 * gelesen werden)?
 * 
 * @author Jens Lehmann
 *
 */
@WebService(name = "DLLearnerWebService")

@SOAPBinding(style = SOAPBinding.Style.RPC)
public class DLLearnerWS2 {

	/*
	// String[] funktioniert leider noch nicht
	@WebMethod
	public void addPositiveExamples(String[] posExamples) {
		for(String example : posExamples)
			positiveExamples.add(new Individual(example));
	}
	
	@WebMethod
	public void addNegativeExamples(String[] negExamples) {
		for(String example : negExamples)
			negativeExamples.add(new Individual(example));
	}
	*/
	
	@WebMethod
	 /**
     * @param name
     * 
     * @return The sum
     * @throws AddNumbersException
     *             if any of the numbers to be added is negative.
     *  
     **/
	public String hellosimple(String name) throws ClientNotKnownException{
		/*for (int i = 0; i < name.length; i++) {
			name[i]=i+"nnnn<br>";
		}*/
		if(name.equals("aa"))throw new ClientNotKnownException("a","b");
		
		return "bbb";
	}	


	// Testmethode
	@WebMethod
	public String hello(String name){
		name=null;
		//name.substring(5);
		//throw new NullPointerException();
		return "Hello " + name + "!";
	}		
	
}