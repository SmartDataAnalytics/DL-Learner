package org.dllearner.server;

import java.util.HashMap;
import java.util.Random;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;


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
public class DLLearnerWS {
	Random rand=new Random();
	private HashMap<Integer, ClientState> clients;
	
	// private String ontologyURL;
	// private String ontologyFormat;
	
	
	
	public DLLearnerWS(){
		this.clients=new HashMap<Integer, ClientState>();
		
	}
	
	/**
	 * 
	 * 
	 * 
	 */
	@WebMethod
	public int getID(){
		int id=rand.nextInt();
		while (id<=0){
			id=rand.nextInt();
		}
		
		// dont change to local function get, cause of exception
		ClientState c=this.clients.get(new Long(id));
		if(c!=null){
			return getID();
		}
		else {
			this.clients.put(new Integer(id), new ClientState());
			System.out.println("new Client with id: "+id);
			return id;	
		}
		
	}
	
	@WebMethod
	public void addPositiveExample(int id,String posExample) throws ClientNotKnownException{
		ClientState c=getClientState(id);
		c.addPositiveExample(posExample);
		//positiveExamples.add(new Individual(posExample));
	}
	@WebMethod
	public void addNegativeExample(int id,String negExample) throws ClientNotKnownException {
		ClientState c=getClientState(id);
		c.addNegativeExample(negExample);
	}	
	@WebMethod
	public void addIgnoredConcept(int id,String concept)throws ClientNotKnownException {
		getClientState(id).addIgnoredConcept(concept);
	}
	
	@WebMethod
	public  String[] selectInstancesForAConcept(int id,String Concept)throws ClientNotKnownException,NoOntologySelectedException{
		return getClientState(id).selectInstancesForAConcept(Concept);
	}
	
	@WebMethod
	public  String[] selectAConcept(int id,String Concept,int Percentage)throws ClientNotKnownException,NoOntologySelectedException{
		return getClientState(id).selectAConcept(Concept, Percentage);
	}
	
	@WebMethod
	public String[] getPositiveExamples(int id)throws ClientNotKnownException{
		ClientState c=getClientState(id);
		return c.getPositiveExamples();
	}
	@WebMethod
	public String[] getNegativeExamples(int id)throws ClientNotKnownException{
		ClientState c=getClientState(id);
		return c.getNegativeExamples();
	}
	@WebMethod
	public String[] getIgnoredConcepts(int id)throws ClientNotKnownException{
		return getClientState(id).getIgnoredConcepts();
	}
	
	@WebMethod
	public boolean removePositiveExample(int id, String pos)throws ClientNotKnownException{
		return getClientState(id).removePositiveExample(pos);
	}
	
	@WebMethod
	public boolean removeNegativeExample(int id, String neg)throws ClientNotKnownException{
		return getClientState(id).removeNegativeExample(neg);
	}
	@WebMethod
	public boolean removeAllExamples(int id)throws ClientNotKnownException{
		return getClientState(id).removeAllExamples();
	}
	@WebMethod
	public boolean removeAllPositiveExamples(int id)throws ClientNotKnownException{
		return getClientState(id).removeAllPositiveExamples();
	}
	@WebMethod
	public boolean removeAllNegativeExamples(int id)throws ClientNotKnownException{
		return getClientState(id).removeAllNegativeExamples();
	}
	@WebMethod
	public void removeIgnoredConcept(int id,String concept)throws ClientNotKnownException{
		getClientState(id).removeIgnoredConcept(concept);
	}
	
	@WebMethod
	public String getCurrentOntologyURL(int id)throws ClientNotKnownException,NoOntologySelectedException{
		return getClientState(id).getCurrentOntologyURL();
		
	}
	
	@WebMethod
	public String[] getInstances(int id)throws ClientNotKnownException,NoOntologySelectedException{
		return getClientState(id).getInstances();
	}
	
	@WebMethod
	public String[] getAtomicConcepts(int id)throws ClientNotKnownException,NoOntologySelectedException{
		return getClientState(id).getAtomicConcepts();
	}
	
	@WebMethod
	public String[] getAtomicRoles(int id)throws ClientNotKnownException,NoOntologySelectedException{
		return getClientState(id).getAtomicRoles();
	}
	@WebMethod
	public String[] getIndividualsForARole(int id,String Role)throws ClientNotKnownException, NoOntologySelectedException{
		return getClientState(id).getIndividualsForARole(Role);
	}
	@WebMethod
	public String getSubsumptionHierarchy(int id)throws ClientNotKnownException, NoOntologySelectedException{
		return getClientState(id).getSubsumptionHierarchy();
	}
	
	@WebMethod
	public String[] retrieval(int id,String Concept)throws ClientNotKnownException,NoOntologySelectedException{
		return getClientState(id).retrieval(Concept);
	}
	
	@WebMethod
	public void readOntology(int id,String ontologyURL, String format)throws ClientNotKnownException,OntologyURLNotValid{
		getClientState(id).readOntology(ontologyURL, format);
		
	}
	
	@WebMethod
	public void removeOntology(int id)throws ClientNotKnownException{
		getClientState(id).removeOntology();
	}
	
	@WebMethod
	public String learnConcept(int id)throws ClientNotKnownException{
		return "Deprecated method";
		
	}
	
	
	@WebMethod
	public String getAlgorithmStatus(int id ) throws ClientNotKnownException{
		
		return getClientState(id).getAlgorithmStatus();
	}
	
	
	@WebMethod
	public void learnMonitored(int id )throws ClientNotKnownException{
		getClientState(id).learnMonitored();
	}
	/*@WebMethod
	public void relearn(int id,String Concept )throws ClientNotKnownException{
		getClientState(id).relearn(Concept);
	}*/
	
	@WebMethod 
	public String getLastResult(int id)throws ClientNotKnownException{
		return getClientState(id).getLastResult();
	}
	
	@WebMethod 
	public void stop(int id)throws ClientNotKnownException{
		  getClientState(id).stop();
	}
	

	//*************************USER MANAGEMENT
	
	public ClientState getClientState(int id)throws ClientNotKnownException{
		System.out.println("Request from "+id);
		ClientState c=this.clients.get(new Integer(id));
		if(c==null){
			//System.out.println(clients.keySet().toString());
			// throw new ClientNotKnownException("Client with id: "+id+" is not known","ClientNotKnownException");
			};
		return c;
		
	}
	
	

	
	
}