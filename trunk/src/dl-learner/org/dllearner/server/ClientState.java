
package org.dllearner.server;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.Config;
import org.dllearner.ConfigurationManager;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.cli.ConfFileOption;
import org.dllearner.core.Reasoner;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.dl.AtomicConcept;
import org.dllearner.core.dl.AtomicRole;
import org.dllearner.core.dl.Individual;
import org.dllearner.kb.OntologyFileFormat;
import org.dllearner.utilities.Helper;

public class ClientState {
	
	// private String  reasonerURL="http://localhost:8081";
	//private String  reasonerURL="http://localhost:3490";
	
	private SortedSet<Individual> positiveExamples = new TreeSet<Individual>();
	public SortedSet<Individual> getPosExamples() {	return this.positiveExamples;}
	
	private SortedSet<Individual> negativeExamples = new TreeSet<Individual>();
	public SortedSet<Individual> getNegExamples() {	return this.negativeExamples;}
	
	private SortedSet<String> ignoredConcept = new TreeSet<String>();
	
	
	private Reasoner reasoner;
	public Reasoner getReasoner() {	return reasoner;}
	
	private ReasoningService rs;
	public ReasoningService getRs() {	return rs;	}
	
	private String currentOntologyURL=null; 
	private boolean debug_flag=true;
	private LearnMonitor lm=null;
	
	private  String lastResult="";
	public String getLastResult(){	
		try{
			return ROL.getBestSolution().toString();
		}catch (Exception e) {}
		return this.lastResult;	}
	public void setLastResult(String lastResult) {this.lastResult = lastResult;}
	
	private  String status="maiden-like";
	public void setStatus(String status) {this.status = status;}
	
	ConfigurationManager confMgr;
	public void addOption(ConfFileOption c){confMgr.applyConfigurationOption(c);}
	ROLearner ROL;

	public ClientState() {
		
		
		TreeSet<ConfFileOption> s=new TreeSet<ConfFileOption>();
		//s.add(new ConfigurationOption("refinement","quiet","true"));
		/*s.add(new ConfigurationOption());
		s.add(new ConfigurationOption());
		s.add(new ConfigurationOption());
		s.add(new ConfigurationOption());
		s.add(new ConfigurationOption());
		s.add(new ConfigurationOption());*/
		
		confMgr = new ConfigurationManager(s);
		addOption(new ConfFileOption("refinement","quiet","true"));
		//confMgr.applyOptions();

	}
	
	
	
	
	public void addPositiveExample(String posExample) {
		positiveExamples.add(new Individual(posExample));
		p("added pos: "+posExample);
	}
	
	
	public void addNegativeExample(String negExample) {
		negativeExamples.add(new Individual(negExample));
		p("added neg: "+negExample);
	}	
	
	public void addIgnoredConcept(String concept) {
		ignoredConcept.add(concept);
		p("added ignoredConcepts: "+concept);
	}
	
	public String[] selectInstancesForAConcept(String Concept)throws NoOntologySelectedException{
		if(Concept.endsWith("#Thing"))return new String[]{};
		AtomicConcept SelectedConcept=new AtomicConcept(Concept);
		ArrayList<String> al=new ArrayList<String>();
		Individual indtmp=null;
		//Set<AtomicConcept> ConceptSet=null;
		AtomicConcept ac=null;
		
		
		System.out.println("selectInstancesForAConcept: "+Concept);
		// add all positives
		Set<Individual> positives=rs.retrieval(SelectedConcept);
		Iterator<Individual> i = positives.iterator();
		while(i.hasNext()){
			indtmp=i.next();
			p("added "+indtmp.getName()+" to positives");
			al.add("added "+indtmp.getName()+" to positives");
			positiveExamples.add(indtmp);
		}
		
		//find more general concepts
		ArrayList<AtomicConcept> superConcepts=new ArrayList<AtomicConcept>();
		try{
			//ConceptSet = rs.getSubsumptionHierarchy().getMoreGeneralConcepts(new AtomicConcept(Concept));
			//System.out.println(ConceptSet);
			//Concept c=new AtomicConcept(Concept);
			/*Set<AtomicConcept> s=rs.getAtomicConcepts();
			Set<Concept> sc=new TreeSet<Concept>();
			Iterator a=s.iterator();
			while (a.hasNext()) {
				sc.add((Concept) a.next());
			}
			sc=rs.subsumes(sc,new AtomicConcept(Concept));*/
			superConcepts=subsumesAll(SelectedConcept);
		
			//System.out.println("sizebefore: "+"size after:"+sc);
		}catch (Exception e) {e.printStackTrace();}
		
		
//		remove top
		for (int j = 0; j < superConcepts.size(); ) {
			//TODO no unique name assumption? 
			if(superConcepts.get(j).getName().equals(SelectedConcept.getName())){
				superConcepts.remove(j);
				j=0;
				continue;
			}
			if(superConcepts.get(j).getName().equals("TOP")){
				superConcepts.remove(j);
				j=0;
				continue;
			}
			j++;
		}
		
		System.out.println("Found all those:"+ superConcepts);
		
		ac=null;
		
		if(superConcepts.size()==0){return al2s(al); }//TODO
		else 
		{	// add all negatives of all superclasses;
			
			//Iterator it=ConceptSet.iterator();
			for (int jj = 0; jj < superConcepts.size(); jj++) 
			{
				ac=superConcepts.get(jj);
				p("next this: "+ac.getName());
				Set<Individual> negatives=rs.retrieval(ac);
				Iterator<Individual> i2 = negatives.iterator();
				indtmp=null;
				while(i2.hasNext()){
					
					indtmp=(Individual)i2.next();
					
					if(!positives.contains(indtmp)){
						
						p("added "+indtmp.getName()+" to NEGATIVES");
						al.add("added "+indtmp.getName()+" to NEGATIVES");
						negativeExamples.add(indtmp);
					}
					else{
						p("skipped "+indtmp.getName());
						al.add("skipped "+indtmp.getName());
					}
					
				}
			}//endfor
			return al2s(al);
		}
		
			
	}
	
	
	
	public String[] selectAConcept(String Concept, int Percentage)throws NoOntologySelectedException{
		if(Concept.endsWith("#Thing"))return new String[]{};
		AtomicConcept SelectedConcept=new AtomicConcept(Concept);
		ArrayList<String> ret=new ArrayList<String>();
		Individual indtmp=null;
		//Set<AtomicConcept> ConceptSet=null;
		//AtomicConcept ac=null;
		Random r=new Random();
		
		
		System.out.println("selectAConcept: "+Concept);
		// add all positives
		Set<Individual> positives=rs.retrieval(SelectedConcept);
		Iterator<Individual> i = positives.iterator();
		while(i.hasNext()){
			indtmp=(Individual)i.next();
			p("added "+indtmp.getName()+" to positives");
			ret.add("added "+indtmp.getName()+" to positives");
			positiveExamples.add(indtmp);
		}
		
		//find All other Instances concepts
		Set<Individual> otherInstances=rs.getIndividuals();
		Iterator<Individual> it=otherInstances.iterator();
		while(it.hasNext()){
			indtmp=(Individual)it.next();
			
			if(!positives.contains(indtmp) && (r.nextDouble()*100)<=Percentage){
				
				p("added "+indtmp.getName()+" to NEGATIVES");
				ret.add("added "+indtmp.getName()+" to NEGATIVES");
				negativeExamples.add(indtmp);
			}
		}//while
		
		return al2s(ret);
			
	}
	
	
	public ArrayList<AtomicConcept> subsumesAll(AtomicConcept c){
		Set<AtomicConcept> s=rs.getAtomicConcepts();
		ArrayList<AtomicConcept> ret=new ArrayList<AtomicConcept>();
		Iterator<AtomicConcept> i=s.iterator();
		while (i.hasNext()) {
			AtomicConcept element = (AtomicConcept) i.next();
			if(rs.subsumes(element, c))
				{
				ret.add(element);
				}
			
		}
		return ret;
	}
	public String[] getPositiveExamples(){
		String[] ret=new String[positiveExamples.size()];
		Iterator<Individual> i=positiveExamples.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=((Individual)i.next()).getName();
		}
		//p("getPositiveEx");
		return ret;
	}
	public String[] getNegativeExamples(){
		String[] ret=new String[negativeExamples.size()];
		Iterator<Individual> i=negativeExamples.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=((Individual)i.next()).getName();
		}
		//p("getNegativeEx");
		return ret;
	}
	
	public String[] getIgnoredConcepts() {	
		String[] ret=new String[ignoredConcept.size()];
		Iterator<String> i=ignoredConcept.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=((String)i.next());
		}
		//p("getNegativeEx");
		return ret;
		}
	
	public String getSubsumptionHierarchy() throws NoOntologySelectedException {	
		try{
		return this.rs.getSubsumptionHierarchy().toString();
		}catch (Exception e) {throw new NoOntologySelectedException("Subsumptionhierarchy",e.getMessage());}
		
		}
	
	
	
	public boolean removeNegativeExample(String NegExample){
		p("removed from neg: "+NegExample);
		return negativeExamples.remove(new Individual(NegExample));
	}
	public boolean removePositiveExample(String PosExample){
		p("removed from pos: "+PosExample);
		return positiveExamples.remove(new Individual(PosExample));
	}
	
	public boolean removeAllPositiveExamples(){
		positiveExamples = new TreeSet<Individual>();
		p("removing all positive examples");
		return true;
	}
	public boolean removeAllNegativeExamples(){
		negativeExamples = new TreeSet<Individual>();
		p("removing all negative examples");
		return true;
	}
	
	public boolean removeAllExamples(){
		positiveExamples = new TreeSet<Individual>();
		negativeExamples = new TreeSet<Individual>();
		p("removing all examples");
		return true;
	}
	
	public void removeIgnoredConcept(String concept) {
		//ignoredConcept.add(concept);
		this.ignoredConcept.remove(concept);
		p("removed ignoredConcepts: "+concept);
	}
	
	public String[] getInstances()throws NoOntologySelectedException{
		try{
		SortedSet<Individual> s=rs.getIndividuals();
		//System.out.println(s);
		String[] ret=new String[s.size()];
		Iterator<Individual> i=s.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=((Individual)i.next()).getName();
		}
		Arrays.sort(ret);
		//p("getInstances");
		return ret;
		}catch (Exception e) {throw new NoOntologySelectedException("Failed to get instances, no ontology selected","");}
	}
	
	public String getCurrentOntologyURL()throws NoOntologySelectedException{
		p("getCurrentOntology: "+currentOntologyURL);
		if(currentOntologyURL==null)throw new NoOntologySelectedException("Select Ontology First","quatsch");
		else return currentOntologyURL;
	}
	
	public String getAlgorithmStatus(){
		return this.status;
	}
	
	/**
	 * Specifies the URI of the ontology containing the background 
	 * knowledge. Reads the ontology and sends it to the reasoner.
	 * 
	 * @param ontologyURI The URI of the ontology to use.
	 */
	// gleiche Methoden mit verschiedenen Parametern sind offenbar problematisch
	/*
	@WebMethod
	public void readOntology(String ontologyURI) {
		readOntology(ontologyURI, "RDF/XML");
	}
	*/
	
	
	public void removeOntology() {
		this.currentOntologyURL=null;
		this.reasoner=null;
		this.rs=null;
		this.positiveExamples = new TreeSet<Individual>();
		this.negativeExamples = new TreeSet<Individual>();
		this.ignoredConcept=new TreeSet<String>();
		p("removing Ontology");
		
	}
	
	/**
	 * Specifies the URI of the ontology containing the background 
	 * knowledge and its format. Reads the ontology and sends it to
	 * the reasoner.
	 * 
	 * @param ontologyURI The URI of the ontology to use.
	 * @param format "RDF/XML" or "N-TRIPLES".
	 */
	
	
	public void readOntology(String ontologyURL, String format) throws OntologyURLNotValid{
		this.currentOntologyURL=ontologyURL;
		p("trying to read: "+ontologyURL+" ::"+format);
		try{
		// this.ontologyURL = ontologyURL;
		// this.ontologyFormat = format;
		
		// TODO: potentielles Sicherheitsrisiko, da man damit derzeit auch lokale Dateien
		// laden könnte (Fix: nur http:// zulassen, kein file://)
		URL ontology = null;
		try {
			ontology = new URL(ontologyURL);
		} catch (MalformedURLException e1) {
			this.removeOntology();
			throw new OntologyURLNotValid("The URL of the Ontology is not correct<br>\nCheck settings and URL","OntologyURLNotValid");
			
		}
		
		OntologyFileFormat ofFormat;
		if (format.equals("RDF/XML"))
			ofFormat = OntologyFileFormat.RDF_XML;
		else
			ofFormat = OntologyFileFormat.N_TRIPLES;
		
		Map<URL, OntologyFileFormat> m = new HashMap<URL, OntologyFileFormat>();
		m.put(ontology, ofFormat);
		
		// Default-URI für DIG-Reasoner setzen
		
//		try {
//			Config.digReasonerURL = new URL(reasonerURL);
//		} catch (MalformedURLException e) {
//			// Exception tritt nie auf, da URL korrekt
//			e.printStackTrace();
//		}		
		
		 // reasoner = Main.createReasoner(new KB(), m);
		 System.err.println("TODO: rewrite webservice code");
		 rs = new ReasoningService(reasoner);
		
		 Helper.autoDetectConceptsAndRoles(rs);
			reasoner.prepareSubsumptionHierarchy();
			if (Config.Refinement.improveSubsumptionHierarchy) {
				try {
					reasoner.prepareRoleHierarchy();
					reasoner.getSubsumptionHierarchy().improveSubsumptionHierarchy();
				} catch (ReasoningMethodUnsupportedException e) {
					// solange DIG-Reasoner eingestellt ist, schlägt diese Operation nie fehl
					e.printStackTrace();
				}
			}
			p(rs.getSubsumptionHierarchy().toString());
			//rs.getRoleMembers(arg0)
		}
		catch (Exception e) { //TODO
			this.removeOntology();
			throw new OntologyURLNotValid("The URL of the Ontology is not correct<br>\nCheck settings and URL","OntologyURLNotValid");
			}
		/*catch (JenaException e) {
			e.printStackTrace();}
		*/
		
		/*catch(Exception e2) {
			
			//p("exception:"+e.getMessage());
			e2.printStackTrace();
		}*/
		p("Ontology read: "+currentOntologyURL);
	}
	
	
	public void learnMonitored(){
		addOption(new ConfFileOption("refinement","ignoredConcepts",ignoredConcept));
		this.lm=new LearnMonitor(this);
		this.lm.start();
		//this.lm.learn(this);
	}
	
	/*public void relearn(){
		//TreeSet<String> s=new TreeSet<String>();
		//new ConfigurationOption();
		this.lm=new LearnMonitor(this);
		this.lm.start();
		//this.lm.learn(this);
	}*/
	
	
	public String[] getAtomicConcepts()throws NoOntologySelectedException{
		try{
		return SortedSet2StringListConcepts( rs.getAtomicConcepts());
		}catch (Exception e) {throw new NoOntologySelectedException("Select Ontology First","ddddd");}
	}
	
	public String[] retrieval(String Concept)throws NoOntologySelectedException{
		return SortedSet2StringListIndividuals(rs.retrieval(new AtomicConcept(Concept)));
	}
	
	public String[] getAtomicRoles()throws NoOntologySelectedException{
		return SortedSet2StringListRoles( rs.getAtomicRoles());
	}
	
	public String[] getIndividualsForARole(String Role)throws NoOntologySelectedException{
		Map<Individual,SortedSet<Individual>> m=rs.getRoleMembers(new AtomicRole(Role));
		Set<Individual> s=m.keySet();
		return SortedSet2StringListIndividuals(s);
	}
	
	public  synchronized void stop(){
			System.out.println("ROL"+this.ROL);
		
			System.out.println("lm"+lm);
			System.out.println("lmstate"+lm.getState());
			System.out.println("lmalive"+lm.isAlive());
			System.out.println("lminterrupt"+lm.isInterrupted());
			this.ROL.stop();
			//lm.end();
			/*try{
				synchronized (this.lm) {
					//this.lm.yield();
				}
				}catch (Exception e) {e.printStackTrace();}*/
			System.out.println("lmstate"+lm.getState());
			System.out.println("lmalive"+lm.isAlive());
			System.out.println("lminterrupt"+lm.isInterrupted());
			
			//this.ROL.stop();
			//this.lm.interrupt();
			
			//this.lm.end();
			//this.lm.notify();
	}
	
	public String[] SortedSet2StringListIndividuals(Set<Individual> s){
		
		String[] ret=new String[s.size()];
		Iterator<Individual> i=s.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=((Individual)i.next()).getName();
		}
		Arrays.sort(ret);
		return ret;
	}
	
	public String[] SortedSet2StringListConcepts(Set<AtomicConcept> s){
		
		String[] ret=new String[s.size()];
		Iterator<AtomicConcept> i=s.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=((AtomicConcept)i.next()).getName();
		}
		Arrays.sort(ret);
		return ret;
	}
	public String[] SortedSet2StringListRoles(Set<AtomicRole> s){
		
		String[] ret=new String[s.size()];
		Iterator<AtomicRole> i=s.iterator();
		int a=0;
		while (i.hasNext()){
			ret[a++]=((AtomicRole)i.next()).getName();
		}
		Arrays.sort(ret);
		return ret;
	}
	
	public String[] al2s(ArrayList<String> al){
		String[] ret=new String[al.size()];
		for (int i = 0; i < al.size(); i++) {
			ret[i]=al.get(i);
		}
		return ret;
	}
	public void p(String s){
		if(debug_flag){
			System.out.println("\t"+s);
		}
		
	}
}
