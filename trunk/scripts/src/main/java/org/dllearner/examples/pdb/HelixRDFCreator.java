package org.dllearner.examples.pdb;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
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

	

	private static String saveDir = "../test/pdb/";

	/**
	 * @param args
	 * TODO: remove beginsAt, endsAt from model
	 */
	public static void main(String[] args) {
		
		TrainAndTestSet sets;
		PdbRdfModel trainmodel = new PdbRdfModel();
		do{
			sets = new TrainAndTestSet(1);
			trainmodel.add(getRdfModelForIds(sets.getTrainset()));
			/* 
			 * String[] id = {"200L"};
			 * trainmodel.add(getRdfModelForIds(id));
			 */
		
			// PdbRdfModel testmodel =  getRdfModelForIds(sets.getTestset());
		
			/* 
			 * as we have to handle several amino acid chains we need the first
			 * amino acid of every chain, they are returned as ResIterator
			 */
			ResIterator niter = getFirstAA(trainmodel);
		
			/*
			 * we add some distance Information to out model
			 */
			trainmodel = addDistanceInfo(trainmodel);
		
			/* 
			 * take all amino acids which are in helices and put them into the
			 * global positives ArrayList, and all others in the global negatives ArrayList
			 */
			createPositivesAndNegatives(niter, trainmodel);
		} while(positives.size() > 100 && negatives.size() > 100);
		
		/*
		 * remove all triples that contain information about begin and end of helices
		 */
		Property ba = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "beginsAt");
		trainmodel = removeStatementsWithPoperty(trainmodel, ba);
		Property ea = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "endsAt");
		trainmodel = removeStatementsWithPoperty(trainmodel, ea);
		Resource residue = ResourceFactory.createResource("http://bio2rdf.org/pdb:Residue");
		trainmodel = removeStatementsWithObject(trainmodel, residue);
		
		/*
		 * writes the conf-Files
		 */

		
		try
    	{
			SimpleDateFormat df = new SimpleDateFormat("_yyyy_MM_dd_HH:mm");
			String date = df.format(new Date());
			String rdffile= "Helixtrainer" + date + ".rdf";
			String filename = saveDir + rdffile; 
			createConfFile(date, rdffile, trainmodel);
			PrintStream out = new PrintStream (new File(filename));
			
			// Output query results
			trainmodel.write(out, "RDF/XML");

			// Important - free up resources used running the query
			out.close();
    	}
    	catch (IOException e)
    	{
    		System.err.println("OutputStream konnte nicht geschlossen werden!");
    	}
	}

	private static PdbRdfModel getRdfModelForIds(String[] pdbIDs) {

        // i is an Iterator over an XML InputSource
	    Pdb2RdfInputIterator i = new PdbsIterator(pdbIDs);
	    PdbXmlParser parser = new PdbXmlParser();
        PdbRdfModel allmodels = new PdbRdfModel();
        
        try {
        	
        	while (i.hasNext()) {
        		final InputSource input = i.next();
        		PdbRdfModel model = parser.parse(input, new PdbRdfModel());
        		// jedes Model muss gleich nach den relevanten Daten durchsucht werden, 
        		// da ansonsten Probleme mit der Speichergröße auftreten können.
        		allmodels.add(getData(model));
        		
        		}
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return allmodels;
	}
	
	private static PdbRdfModel getData(PdbRdfModel model) {
    	
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
			/* i do it kind of difficult, but i want to be certain that i only get the sequences of
			 * Polypeptides(L) which contain at least one Helix. Furthermore i collect the information
			 * about at which position helices begin and end.
			 * NOTE:	this information has to be removed before oututing the model. But i will use this
			 * 			to check for positive and negative train amino acids
			*/ 
		String queryString = 
			"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
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
		System.out.println(queryString);
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
	
	
	private static void createConfFile(String date, String rdffile, PdbRdfModel model){
		try
    	{
			// the file with all amino acids
			String pdbname = saveDir + "pdb" + date + ".conf";
			PrintStream out = new PrintStream (new File(pdbname));
			// add import statements
			out.println("import(\"AA_properties.owl\");");
			out.println("import(\"" + rdffile + "\");");
			out.println();
			
			// put all amino acid resources and the their conf-files together
			HashMap<Resource, PrintStream> resprint = new HashMap<Resource, PrintStream>(30); 
			resprint.put(ala, new PrintStream(new File(saveDir + ala.getLocalName() + date + ".conf")));
			resprint.put(cys, new PrintStream(new File(saveDir + cys.getLocalName() + date + ".conf")));
			resprint.put(asp, new PrintStream(new File(saveDir + asp.getLocalName() + date + ".conf")));
			resprint.put(glu, new PrintStream(new File(saveDir + glu.getLocalName() + date + ".conf")));
			resprint.put(phe, new PrintStream(new File(saveDir + phe.getLocalName() + date + ".conf")));
			resprint.put(gly, new PrintStream(new File(saveDir + gly.getLocalName() + date + ".conf")));
			resprint.put(his, new PrintStream(new File(saveDir + his.getLocalName() + date + ".conf")));
			resprint.put(ile, new PrintStream(new File(saveDir + ile.getLocalName() + date + ".conf")));
			resprint.put(lys, new PrintStream(new File(saveDir + lys.getLocalName() + date + ".conf")));
			resprint.put(leu, new PrintStream(new File(saveDir + leu.getLocalName() + date + ".conf")));
			resprint.put(met, new PrintStream(new File(saveDir + met.getLocalName() + date + ".conf")));
			resprint.put(asn, new PrintStream(new File(saveDir + asn.getLocalName() + date + ".conf")));
			resprint.put(pro, new PrintStream(new File(saveDir + pro.getLocalName() + date + ".conf")));
			resprint.put(gln, new PrintStream(new File(saveDir + gln.getLocalName() + date + ".conf")));
			resprint.put(arg, new PrintStream(new File(saveDir + arg.getLocalName() + date + ".conf")));
			resprint.put(ser, new PrintStream(new File(saveDir + ser.getLocalName() + date + ".conf")));
			resprint.put(thr, new PrintStream(new File(saveDir + thr.getLocalName() + date + ".conf")));
			resprint.put(val, new PrintStream(new File(saveDir + val.getLocalName() + date + ".conf")));
			resprint.put(trp, new PrintStream(new File(saveDir + trp.getLocalName() + date + ".conf")));
			resprint.put(tyr, new PrintStream(new File(saveDir + tyr.getLocalName() + date + ".conf")));
			resprint.put(sel, new PrintStream(new File(saveDir + sel.getLocalName() + date + ".conf")));
			
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
}