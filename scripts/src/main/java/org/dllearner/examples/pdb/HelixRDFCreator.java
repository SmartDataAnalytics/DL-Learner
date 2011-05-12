package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.dllearner.cli.Start;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ComponentManager;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.OWLFile;
import org.dllearner.reasoning.FastInstanceChecker;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

import com.dumontierlab.pdb2rdf.model.PdbRdfModel;
import com.dumontierlab.pdb2rdf.parser.PdbXmlParser;
import com.dumontierlab.pdb2rdf.util.Pdb2RdfInputIterator;
import com.dumontierlab.pdb2rdf.util.PdbsIterator;

import edu.stanford.nlp.io.EncodingPrintWriter.out;


public class HelixRDFCreator {
	
	private static Resource ala = ResourceFactory.createResource("http://bio2rdf.org/pdb:Alanine");
	private static Resource cys = ResourceFactory.createResource("http://bio2rdf.org/pdb:Cysteine");
	private static Resource asp = ResourceFactory.createResource("http://bio2rdf.org/pdb:AsparticAcid");
	private static Resource glu = ResourceFactory.createResource("http://bio2rdf.org/pdb:GlutamicAcid");
	private static Resource phe = ResourceFactory.createResource("http://bio2rdf.org/pdb:Phenylalanine");
	private static Resource gly = ResourceFactory.createResource("http://bio2rdf.org/pdb:Glycine");
	private static Resource his = ResourceFactory.createResource("http://bio2rdf.org/pdb:Histidine");
	private static Resource ile = ResourceFactory.createResource("http://bio2rdf.org/pdb:Isoleucine");
	private static Resource lys = ResourceFactory.createResource("http://bio2rdf.org/pdb:Lysine");
	private static Resource leu = ResourceFactory.createResource("http://bio2rdf.org/pdb:Leucine");
	private static Resource met = ResourceFactory.createResource("http://bio2rdf.org/pdb:Methionine");
	private static Resource asn = ResourceFactory.createResource("http://bio2rdf.org/pdb:Asparagine");
	private static Resource pro = ResourceFactory.createResource("http://bio2rdf.org/pdb:Proline");
	private static Resource gln = ResourceFactory.createResource("http://bio2rdf.org/pdb:Glutamine");
	private static Resource arg = ResourceFactory.createResource("http://bio2rdf.org/pdb:Arginine");
	private static Resource ser = ResourceFactory.createResource("http://bio2rdf.org/pdb:Serine");
	private static Resource thr = ResourceFactory.createResource("http://bio2rdf.org/pdb:Threonine");
	private static Resource val = ResourceFactory.createResource("http://bio2rdf.org/pdb:Valine");
	private static Resource trp = ResourceFactory.createResource("http://bio2rdf.org/pdb:Tryptophan");
	private static Resource tyr = ResourceFactory.createResource("http://bio2rdf.org/pdb:Tyrosine");
	private static Resource sel = ResourceFactory.createResource("http://bio2rdf.org/pdb:Selenomethionine");
	
	private static ArrayList<Resource> positives;
	private static ArrayList<Resource> negatives;

	public void setPositives(ArrayList<Resource> pos){
		positives = pos;
	}

	public void setNegatives(ArrayList<Resource> neg){
		negatives = neg;
	}

	public ArrayList<Resource> getPositives(){
		return positives;
	}

	public ArrayList<Resource> getNegatives(){
		return negatives;
	}

	private static String dataDir = "../test/pdb/";
	private static HashMap<Resource, File> confFilePerResidue;
	private static File confFileForAll;

	/**
	 * @param args
	 * TODO: remove beginsAt, endsAt from model
	 */
	public static void main(String[] args) {
		Boolean test = true;
		int dataSet = 1;
		/*
		 * get dataset files
		 */
		String bt426 = dataDir + "bt426.list";
		File bt426List = new File(bt426);
		String plp273 = dataDir + "plp273.list";
		File plp273List = new File(plp273);
		String plp364 = dataDir + "plp364.list";
		File plp364List = new File(plp364);
		String plp399 = dataDir + "plp399.list";
		File plp399List = new File(plp399);

		/*
		 * data for test purpose
		 */
		String pdbID = "3LQH";
		String chainID = "";
		
		/*
		 * generate trainset and fill trainmodel
		 */
		PdbRdfModel trainmodel = new PdbRdfModel();
		TrainAndTestSet trainSet = new TrainAndTestSet();

		if (test)
		{
			trainSet = new TrainAndTestSet(pdbID, chainID);
		}
		else
		{
			 switch (dataSet) {
	            case 1:	trainSet = new TrainAndTestSet(bt426List);	break;
	            case 2:	trainSet = new TrainAndTestSet(plp273List);	break;
	            case 3:	trainSet = new TrainAndTestSet(plp364List);	break;
	            case 4:	trainSet = new TrainAndTestSet(plp399List);	break;
			 }
		}
		
	
		/*
		 * generate a PdbRdfModel for every pdbID
		 */
		
		
		for (int i = 0; i < trainSet.getTrainset().length; i++)
		{
			String[] pdbIDs = {trainSet.getTrainset()[i].getPdbID()};
			trainmodel.removeAll();
			trainmodel.add(getRdfModelForIds(trainSet.getTrainset()[i].getPdbID(), trainSet.getTrainset()[i].getChainID()));
			
			/* 
			 * as we have sometimes to handle several amino acid chains we need the first
			 * amino acid of every chain, they are returned within a ResIterator
			 */
			ResIterator niter = getFirstAA(trainmodel);
			ResIterator riter = niter;
		
			/*
			 * we add some distance Information to our model
			 */
			trainmodel = addDistanceInfo(trainmodel);
			
			/* 
			 * take all amino acids which are in helices and put them into the
			 * global positives ArrayList, and all others in the global negatives ArrayList
			 */
			createPositivesAndNegatives(niter, trainmodel);
			
			SimpleDateFormat df = new SimpleDateFormat("_yyyy_MM_dd_HH_mm");
			String date = df.format(new Date());
			String rdfFile;
			String arffFile;
			if (trainSet.getTrainset()[i].getChainID().length() == 0)
			{
				rdfFile =  trainSet.getTrainset()[i].getPdbID().toUpperCase() + date + ".rdf";
				arffFile =  trainSet.getTrainset()[i].getPdbID().toUpperCase() + date + ".arff";
			}
			else
			{
				rdfFile =  trainSet.getTrainset()[i].getPdbID().toUpperCase() + "." 
				+ trainSet.getTrainset()[i].getChainID().toUpperCase() + date + ".rdf";
				arffFile = trainSet.getTrainset()[i].getPdbID().toUpperCase() + "." 
				+ trainSet.getTrainset()[i].getChainID().toUpperCase() + date + ".arff";
			}
			String dir = dataDir +  trainSet.getTrainset()[i].getPdbID() + "/";
			File directory = new File(dir);
			directory.mkdir();
			String rdfFilePath = dir + rdfFile;
			String arffFilePath = dir + arffFile;
			
			createArffFile(arffFilePath, trainmodel, trainSet, riter);
			/*
			 * remove all triples that contain information about begin and end of helices
			 */
			Property ba = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "beginsAt");
			trainmodel = removeStatementsWithPoperty(trainmodel, ba);
			Property ea = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "endsAt");
			trainmodel = removeStatementsWithPoperty(trainmodel, ea);
			Resource residue = ResourceFactory.createResource("http://bio2rdf.org/pdb:Residue");
			trainmodel = removeStatementsWithObject(trainmodel, residue);
			

			try
	    	{
				/*
				 * creatConfFile()
				 * writes the conf-Files and saves there File-objects in:
				 * confFileForAll and confFilePerResidue
				 */
				createConfFile(dir, date, rdfFile, trainmodel);
				PrintStream out = new PrintStream (new File(rdfFilePath));
				
				// Output results
				trainmodel.write(out, "RDF/XML");

				// Important - free up resources used running the query
				out.close();
	    	}
	    	catch (IOException e)
	    	{
	    		System.err.println("OutputStream konnte nicht geschlossen werden!");
	    	}
			
		}
		
    	/*
    	 * load RDF file and perform learn algorithm for every .conf-file
    	 */

    	
    	/*
		ComponentManager cm = ComponentManager.getInstance();
		KnowledgeSource ks = cm.knowledgeSource(OWLFile.class);
		cm.applyConfigEntry(ks, "url","file://" + filename );
		ReasonerComponent rc = cm.reasoner(FastInstanceChecker.class);
		rc.init();
		*/
    	/*
    	Start start = null;
    	Iterator<Resource> aa = confFilePerResidue.keySet().iterator(); 
		while ( aa.hasNext() ){
			Resource nextRes = aa.next();
			System.out.println(confFilePerResidue.get(nextRes).getAbsolutePath());
    		try{
        		start = new Start(confFilePerResidue.get(nextRes));
    		} catch (ComponentInitException e) {
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (org.dllearner.confparser.ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            start.start(false);
            Description d = start.getLearningAlgorithm().getCurrentlyBestDescription(); 	
            System.out.println(d.toKBSyntaxString());
		}
		*/
	}

	private static PdbRdfModel getRdfModelForIds(String pdbID ,String chainID) {

        /*
         * i is an Iterator over an XML InputSource
         */
		String[] pdbIDs = {pdbID}; 
	    Pdb2RdfInputIterator i = new PdbsIterator(pdbIDs);
	    PdbXmlParser parser = new PdbXmlParser();
        PdbRdfModel allmodels = new PdbRdfModel();
        try {
        	while (i.hasNext())
        	{
        		final InputSource input = i.next();
        		PdbRdfModel model = parser.parse(input, new PdbRdfModel());
        		/*
        		 *  jedes Model muss gleich nach den relevanten Daten durchsucht werden,
        		 *  da ansonsten Probleme mit der Speichergröße auftreten können. 
        		 */
        		allmodels.add(getData(model, pdbID, chainID));
        	}
        }
        catch (IOException e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        catch (SAXException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return allmodels;
	}
	
	private static PdbRdfModel getData(PdbRdfModel model, String pdbID, String chainID) {
    	
		// Beispiel einer SELECT Abfrage
		/*	String selectQuery = 
		 *		"SELECT { ?x1 ?x2 ?x3 .} " +
		 *		"WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Helix> .}";
		 * 	Query query = QueryFactory.create(selectQuery);
		 * 	QueryExecution qe = QueryExecutionFactory.create(query, model);
		 * 	ResultSet select = qe.execSelect();
		 * 	ResultSetFormatter.out (System.out, select, query); 
		 * 
		 */
		
		// CONSTRUCT Abfrage
		 
		PdbRdfModel construct = new PdbRdfModel();
			/* 
			 * i do it kind of difficult, but i want to be certain that i only get the sequences of
			 * Polypeptides(L) which contain at least one Helix. Furthermore i collect the information
			 * about at which position helices begin and end.
			 * NOTE:	this information has to be removed before outputing the model. But i will use this
			 * 			to check for positive and negative train amino acids
			*/ 
		/*
		 * ich brauche noch die selektion der chain und die info über den genursprungsorganismus
		 * rdf:resource="http://bio2rdf.org/pdb:3LQH/chain_A"
		 * http://bio2rdf.org/pdb:3LQH/chain_A/position_1596
		 */
		
		String queryString = "";
		
		if (chainID.length() != 1 || pdbID.length() != 4)
		{
			queryString = 
				"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
				"PREFIX dcterms: <http://purl.org/dc/terms/> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	    		"CONSTRUCT { ?x1 <http://bio2rdf.org/pdb:beginsAt> ?x2 ." +
	    		" ?x1 <http://bio2rdf.org/pdb:endsAt> ?x3 . " +
	    		" ?x5 <http://purl.org/dc/terms/isPartOf> ?x4 . " +
	    		" ?x5 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x6 ." +
	    		" ?x5 <http://bio2rdf.org/pdb:isImmediatelyBefore> ?x7 . } " +
	    		"WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Helix> ." +
	    		" ?x1 <http://bio2rdf.org/pdb:beginsAt> ?x2 ." +
	    		" ?x1 <http://bio2rdf.org/pdb:endsAt> ?x3 ." +
	    		" ?x3 <http://purl.org/dc/terms/isPartOf> ?x4 ." +
	    		" ?x4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Polypeptide(L)> ." +
	    		" ?x5 <http://purl.org/dc/terms/isPartOf> ?x4 ." +
	    		" ?x5 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x6 ." +
	    		// with the Optional clause i get the information by which amino acid
	    		// a amino acid is followed
	    		" OPTIONAL { ?x5 <http://bio2rdf.org/pdb:isImmediatelyBefore> ?x7 . } .}";
		}
		else
		{
			queryString = 
				"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
				"PREFIX dcterms: <http://purl.org/dc/terms/> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
	    		"CONSTRUCT { ?x1 <http://bio2rdf.org/pdb:beginsAt> ?x2 ." +
	    		" ?x1 <http://bio2rdf.org/pdb:endsAt> ?x3 . " +
	    		" ?x5 <http://purl.org/dc/terms/isPartOf> ?x4 . " +
	    		" ?x5 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x6 ." +
	    		" ?x5 <http://bio2rdf.org/pdb:isImmediatelyBefore> ?x7 . } " +
	    		"WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Helix> ." +
	    		" ?x1 <http://bio2rdf.org/pdb:beginsAt> ?x2 ." +
	    		" ?x1 <http://bio2rdf.org/pdb:endsAt> ?x3 ." +
	    		" ?x3 <http://purl.org/dc/terms/isPartOf> ?x4 ." +
	    		" ?x4 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Polypeptide(L)> ." +
	    		" ?x5 <http://purl.org/dc/terms/isPartOf> ?x4 ." +
	    		" ?x5 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> ?x6 ." +
	    		" ?x5 <http://bio2rdf.org/pdb:hasChainPosition> ?x8 ." +
	    		" ?x8 <http://purl.org/dc/terms/isPartOf> <http://bio2rdf.org/pdb:" + pdbID.toUpperCase() +
	    		"/chain_" + chainID.toUpperCase() + "> ." +
	    		// with the Optional clause i get the information by which amino acid
	    		// a amino acid is followed
	    		" OPTIONAL { ?x5 <http://bio2rdf.org/pdb:isImmediatelyBefore> ?x7 . } .}";
		}

		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	construct.add(qe.execConstruct()); 
    	qe.close();
    	return construct;
	}

	private static ResIterator getFirstAA( PdbRdfModel model) {
		PdbRdfModel construct = new PdbRdfModel();
		/* i look for all amino acids (AA) that have a successor
		 * but do not have a predecessor -> it's the first AA of every
		 * polypeptide chain
		 */
		
		String queryString = 
			"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
    		"CONSTRUCT { ?x1 pdb:isImmediatelyBefore ?x2 . } " +
    		"WHERE { ?x1 pdb:isImmediatelyBefore ?x2 . " +
    		// NOT EXISTS can be used with SPARQL 1.1
    		//"NOT EXISTS { ?x3 pdb:isImmediatelyBefore ?x1 . } }";
			" OPTIONAL { ?x3 pdb:isImmediatelyBefore ?x1 . } " +
			" FILTER ( !BOUND(?x3) ) }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	construct.add(qe.execConstruct()); 
    	qe.close();
    	ResIterator niter = construct.listSubjects();
    	return niter;
	}
	
	private static PdbRdfModel addDistanceInfo(PdbRdfModel model){
		String queryString = 
			"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
    		"CONSTRUCT { ?x1 pdb:isFourAminoAcidsBefore ?x5 . } " +
    		"WHERE { ?x1 pdb:isImmediatelyBefore ?x2 . " +
    		" ?x2 pdb:isImmediatelyBefore ?x3 . " +
    		" ?x3 pdb:isImmediatelyBefore ?x4 . " +
    		" ?x4 pdb:isImmediatelyBefore ?x5 . }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	model.add(qe.execConstruct()); 
    	qe.close();
    	return model;
	}

	private static void createPositivesAndNegatives(ResIterator riter, PdbRdfModel model) {
		
		// Properties i have to check for while going through the AA-chain
		Property iib = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "isImmediatelyBefore");
		Property ba = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "beginsAt");
		Property ea = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "endsAt");
		ArrayList<Resource> pos = new ArrayList<Resource>();
		ArrayList<Resource> neg = new ArrayList<Resource>();
		
		// every element in riter stands for a AA-chain start
		// every first amino acid indicates a new AA-chain 
		while (riter.hasNext()) {
			// Initialization of variables needed
			Resource aaOne = riter.nextResource();
			Resource currentaa  = aaOne;
			Resource nextaa = aaOne;
			boolean inHelix = false;
						
			// look if there is a next AA
			do {
				// looks weird, but is needed to enter loop even for the last AA which does not have a iib-Property
				currentaa = nextaa;
				// die Guten ins Töpfchen ...
				// if we get an non-empty iterator for pdb:beginsAt the next AAs are within a AA-chain
				if(model.listResourcesWithProperty(ba, currentaa).hasNext() && !inHelix ){
					inHelix = true;
				}
				// die Schlechten ins Kröpfchen
				// if we get an non-empty iterator for pdb:endsAt and are already within a AA-chain
				// the AAs AFTER the current ones aren't within a helix
				if (model.listResourcesWithProperty(ea, currentaa).hasNext() && inHelix){
					inHelix = false;
				}
				// get next AA if there is one
				if (model.listObjectsOfProperty(currentaa, iib).hasNext()){
					nextaa = model.getProperty(currentaa, iib).getResource();
				}
				
				// add current amino acid to positives or negatives set
				if (inHelix){
					pos.add(currentaa);
				} else {
					neg.add(currentaa);
				}
				
			} while (currentaa.hasProperty(iib)) ;
		}
		positives = pos;
		negatives = neg;
	}
	

	
	
	private static PdbRdfModel removeStatementsWithPoperty(PdbRdfModel model, Property prop){
				
		String queryString = 
			"PREFIX x:<" + prop.getNameSpace() + "> " +
    		"CONSTRUCT { ?x1 x:" + prop.getLocalName()+ " ?x2 . } " +
    		"WHERE { ?x1 x:" + prop.getLocalName() + " ?x2 . }";
		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	StmtIterator stmtiter = qe.execConstruct().listStatements(); 
    	qe.close();
    	while(stmtiter.hasNext()){
    		model.remove(stmtiter.next());
    	}
    	
    	return model;
	}
	
	private static PdbRdfModel removeStatementsWithObject(PdbRdfModel model, Resource res){
		
		String queryString =
			"PREFIX x:<" + res.getNameSpace() + "> " +
    		"CONSTRUCT { ?x1 ?x2 x:" + res.getLocalName() + " . } " +
    		"WHERE { ?x1 ?x2 x:" + res.getLocalName() + " . }";
		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	StmtIterator stmtiter = qe.execConstruct().listStatements(); 
    	qe.close();
    	while(stmtiter.hasNext()){
    		model.remove(stmtiter.next());
    	}
    	
    	return model;
	}
	
	
	private static void createConfFile(String dir, String date, String rdffile, PdbRdfModel model){
		try
    	{
			// the file with all amino acids
			String pdbname = dir + "pdb" + date + ".conf";
			confFileForAll = new File(pdbname);
			PrintStream out = new PrintStream (confFileForAll);
			// add import statements
			out.println("import(\"AA_properties.owl\");");
			out.println("import(\"" + rdffile + "\");");
			out.println();
			
			HashMap<Resource, File> resConfFiles = new HashMap<Resource, File>(30);
			resConfFiles.put(ala, new File(dir + ala.getLocalName() + date + ".conf"));
			resConfFiles.put(cys, new File(dir + cys.getLocalName() + date + ".conf"));
			resConfFiles.put(asp, new File(dir + asp.getLocalName() + date + ".conf"));
			resConfFiles.put(glu, new File(dir + glu.getLocalName() + date + ".conf"));
			resConfFiles.put(phe, new File(dir + phe.getLocalName() + date + ".conf"));
			resConfFiles.put(gly, new File(dir + gly.getLocalName() + date + ".conf"));
			resConfFiles.put(his, new File(dir + his.getLocalName() + date + ".conf"));
			resConfFiles.put(ile, new File(dir + ile.getLocalName() + date + ".conf"));
			resConfFiles.put(lys, new File(dir + lys.getLocalName() + date + ".conf"));
			resConfFiles.put(leu, new File(dir + leu.getLocalName() + date + ".conf"));
			resConfFiles.put(met, new File(dir + met.getLocalName() + date + ".conf"));
			resConfFiles.put(asn, new File(dir + asn.getLocalName() + date + ".conf"));
			resConfFiles.put(pro, new File(dir + pro.getLocalName() + date + ".conf"));
			resConfFiles.put(gln, new File(dir + gln.getLocalName() + date + ".conf"));
			resConfFiles.put(arg, new File(dir + arg.getLocalName() + date + ".conf"));
			resConfFiles.put(ser, new File(dir + ser.getLocalName() + date + ".conf"));
			resConfFiles.put(thr, new File(dir + thr.getLocalName() + date + ".conf"));
			resConfFiles.put(val, new File(dir + val.getLocalName() + date + ".conf"));
			resConfFiles.put(trp, new File(dir + trp.getLocalName() + date + ".conf"));
			resConfFiles.put(tyr, new File(dir + tyr.getLocalName() + date + ".conf"));
			resConfFiles.put(sel, new File(dir + sel.getLocalName() + date + ".conf"));
			confFilePerResidue = resConfFiles;
			
			
			
			// put all amino acid resources and the their conf-files together
			HashMap<Resource, PrintStream> resprint = new HashMap<Resource, PrintStream>(30); 
			resprint.put(ala, new PrintStream(resConfFiles.get(ala)));
			resprint.put(cys, new PrintStream(resConfFiles.get(cys)));
			resprint.put(asp, new PrintStream(resConfFiles.get(asp)));
			resprint.put(glu, new PrintStream(resConfFiles.get(glu)));
			resprint.put(phe, new PrintStream(resConfFiles.get(phe)));
			resprint.put(gly, new PrintStream(resConfFiles.get(gly)));
			resprint.put(his, new PrintStream(resConfFiles.get(his)));
			resprint.put(ile, new PrintStream(resConfFiles.get(ile)));
			resprint.put(lys, new PrintStream(resConfFiles.get(lys)));
			resprint.put(leu, new PrintStream(resConfFiles.get(leu)));
			resprint.put(met, new PrintStream(resConfFiles.get(met)));
			resprint.put(asn, new PrintStream(resConfFiles.get(asn)));
			resprint.put(pro, new PrintStream(resConfFiles.get(pro)));
			resprint.put(gln, new PrintStream(resConfFiles.get(gln)));
			resprint.put(arg, new PrintStream(resConfFiles.get(arg)));
			resprint.put(ser, new PrintStream(resConfFiles.get(ser)));
			resprint.put(thr, new PrintStream(resConfFiles.get(thr)));
			resprint.put(val, new PrintStream(resConfFiles.get(val)));
			resprint.put(trp, new PrintStream(resConfFiles.get(trp)));
			resprint.put(tyr, new PrintStream(resConfFiles.get(tyr)));
			resprint.put(sel, new PrintStream(resConfFiles.get(sel)));
			
			Property type = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
			
			// add import statements to .conf files for amino acids
			Iterator<Resource> keys = resprint.keySet().iterator();
			
			while (keys.hasNext()){
				Resource k = keys.next();
				resprint.get(k).println("import(\"AA_properties.owl\");");
				resprint.get(k).println("import(\"" + rdffile + "\");");
				resprint.get(k).println();
			}
			
			/*
			 * the for-loops beneath may cause trouble, if there exists an amino acid within a structure that
			 * doesn't exists in our HashMap
			 */
			for (int i = 0 ; i < positives.size() ; i++ ) {
				out.println("+\"" + positives.get(i).getURI() + "\"");
				try{
					Statement spo = model.getProperty(positives.get(i), type);
					resprint.get(spo.getResource()).println("+\"" + positives.get(i).getURI() + "\"");
				} catch (NullPointerException e) {
					// What was the Object that probably caused the pain?
					System.err.println("Object probably not in our HashMap: " +
							model.getProperty(positives.get(i), type).getResource());
					e.getStackTrace();
				}
				// System.out.println("Couldn't find AA: " + positives.get(i).getURI());
				
			}
			
			for (int i = 0 ; i < negatives.size() ; i++ ) {
				out.println("-\"" + negatives.get(i).getURI() + "\"");
				try{
					Statement spo = model.getProperty(negatives.get(i), type);
					resprint.get(spo.getResource()).println("-\"" + negatives.get(i).getURI() + "\"");
				} catch (NullPointerException e) {
					// What was the Object that probably caused the pain?
					System.err.println("Object probably not in our HashMap: " +
							model.getProperty(negatives.get(i), type).getResource());
					e.getStackTrace();
				}
				
				// System.out.println("Couldn't find AA: " + positives.get(i).getURI());
				
			}

			// Important - free up resources used running the query
			out.close();
			
			Iterator<Resource> newkeys = resprint.keySet().iterator(); 
			while ( newkeys.hasNext() ){
				resprint.get(newkeys.next()).close();
			}
			
    	}
    	catch (IOException e)
    	{
    		System.err.println("OutputStream konnte nicht geschlossen werden!");
    	}
	}
	
	private static void createArffFile(String arffFilePath, PdbRdfModel model, TrainAndTestSet sets, ResIterator riter){
		
		String relation = "@RELATION ";
		for (int i = 0; i < sets.getTrainset().length ; i++){
			System.out.println("Element " + i + " = " + sets.getTrainset()[i].getPdbID());
			relation += sets.getTrainset()[i];
		}
		
		/*
		 * ATTRIBUTES
		 */
		
		// Integer declaring Position in chain
		String attrPosInChain = "@ATTRIBUTE position_in_chain NUMERIC\n";
		// Helix = 1 Other = 0
		String attrHelix = "@ATTRIBUTE in_helix NUMERIC\n";
		//  Hydrophilic = 0 Hydrophobic = 1 Very_hydrophobic = 2
		String attrHydrophob = "@ATTRIBUTE hydrophob NUMERIC\n";
		// Negative = -1 Neutral = 0 Positive = 1
		String attrCharge = "@ATTRIBUTE charge NUMERIC\n";
		// Large = 2 Small = 1 Tiny = 0.5 
		String attrSize = "@ATTRIBUTE size NUMERIC\n";
		// Aliphatic = 0 Aromatic = 1
		String attrAromaticity = "@ATTRIBUTE aromaticity NUMERIC\n";
		// Donor = 1 Donor/Acceptor = 0 Acceptor = -1
		String attrHydrogenbonding = "@ATTRIBUTE hydrogen_bonding NUMERIC\n";
		
		String attribute = attrPosInChain + attrHelix + attrHydrophob +
			attrCharge + attrSize + attrAromaticity + attrHydrogenbonding + "\n"; 
		
		String data = "@DATA\n";
		
		HashMap<Resource, String> resdata = new HashMap<Resource, String>(30); 
		resdata.put(ala, new String("2,0,0.5,?,?\n"));
		resdata.put(cys, new String("1,0,1,?,0\n"));
		resdata.put(asp, new String("0,-1,1,?,-1\n"));
		resdata.put(glu, new String("0,-1,2,?,-1\n"));
		resdata.put(phe, new String("2,0,2,1,?\n"));
		resdata.put(gly, new String("2,0,0.5,?,?\n"));
		resdata.put(his, new String("1,1,2,1,1\n"));
		resdata.put(ile, new String("2,0,2,0,?\n"));
		resdata.put(lys, new String("1,1,2,?,1\n"));
		resdata.put(leu, new String("2,0,2,0,?\n"));
		resdata.put(met, new String("2,0,2,?,?\n"));
		resdata.put(asn, new String("0,0,1,?,0\n"));
		resdata.put(pro, new String("?,0,1,?,?\n"));
		resdata.put(gln, new String("0,0,2,?,0\n"));
		resdata.put(arg, new String("0,1,2,?,1\n"));
		resdata.put(ser, new String("0,0,0.5,?,0\n"));
		resdata.put(thr, new String("1,0,1,?,0,\n"));
		resdata.put(val, new String("2,0,1,0,?\n"));
		resdata.put(trp, new String("1,0,2,1,1\n"));
		resdata.put(tyr, new String("1,0,2,1,0\n"));
		resdata.put(sel, new String("?,?,?,?,\n"));
		
		
		// Properties i have to check for while going through the AA-chain
		Property iib = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "isImmediatelyBefore");
		Property ba = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "beginsAt");
		Property ea = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "endsAt");
		
		Property type = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		
		// every element in riter stands for a AA-chain start
		// every first amino acid indicates a new AA-chain 
		while (riter.hasNext())
		{
			// Initialization of variables needed
			int i = 0;
			Resource aaOne = riter.nextResource();
			Resource currentaa  = aaOne;
			Resource nextaa = aaOne;
			boolean inHelix = false;
			System.out.println(currentaa.getURI());
			// look if there is a next AA
			do {
				++i;
				System.out.print(i + " ");
				//looks weird, but is needed to enter loop even for the last AA which does not have a iib-Property
				currentaa = nextaa;
				NodeIterator resType = model.listObjectsOfProperty(currentaa,type);
				
				// die Guten ins Töpfchen ...
				// if we get an non-empty iterator for pdb:beginsAt the next AAs are within a AA-helix
				if(model.listResourcesWithProperty(ba, currentaa).hasNext() && !inHelix )
				{
					inHelix = true;
				}
				// die Schlechten ins Kröpfchen
				// if we get an non-empty iterator for pdb:endsAt and are already within a AA-helix
				// the AAs AFTER the current ones aren't within a helix
				if (model.listResourcesWithProperty(ea, currentaa).hasNext() && inHelix)
				{
					inHelix = false;
				}
				// get next AA if there is one
				if (model.listObjectsOfProperty(currentaa, iib).hasNext())
				{
					nextaa = model.getProperty(currentaa, iib).getResource();
				}
				
				// add current amino acid to positives or negatives set
				while(resType.hasNext())
				{
					Resource aaType = resType.next().asResource();
					System.out.println(aaType.getURI());
					if (resdata.get(aaType) != null)
					{
						if (inHelix)
						{
							data += i + "," + 1 + "," + resdata.get(aaType);
						}
						else
						{
							data += i + "," + 0 + "," + resdata.get(aaType);
						}
					}
				}
				
			} while (currentaa.hasProperty(iib)) ;
		}
			
		try
		{
			PrintStream out = new PrintStream (new File(arffFilePath));
			out.println(relation);
			out.print(attribute);
			out.print(data);
			out.close();
		}
		catch (FileNotFoundException e )
		{
    		System.err.println("Datei " + arffFilePath + " konnte nicht angelegt werden!");
			e.printStackTrace();
		}
	}
}
