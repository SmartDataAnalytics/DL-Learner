package org.dllearner.examples.pdb;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

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

import com.dumontierlab.pdb2rdf.model.PdbRdfModel;
import com.dumontierlab.pdb2rdf.parser.PdbXmlParser;
import com.dumontierlab.pdb2rdf.util.Pdb2RdfInputIterator;
import com.dumontierlab.pdb2rdf.util.PdbsIterator;


public class HelixRDFCreator {
	
	private static ArrayList<Resource> positives;
	private static ArrayList<Resource> negatives;

	/**
	 * @param args
	 * TODO: remove beginsAt, endsAt from model
	 */
	public static void main(String[] args) {
		
		
		
		TrainAndTestSet sets = new TrainAndTestSet(1);
		PdbRdfModel trainmodel = new PdbRdfModel();
		trainmodel.add(getRdfModelForIds(sets.getTrainset()));
		/* 
		 * String[] id = {"200L"};
		 * trainmodel.add(getRdfModelForIds(id));
		 */
		
		// PdbRdfModel testmodel =  getRdfModelForIds(sets.getTestset());
		
		ResIterator niter = getFirstAA(trainmodel);
		
		/* take all amino acids which are in helices and put them into the
		 * positives ArrayList, and all others in the negatives ArrayList
		 */
		createPositivesAndNegatives(niter, trainmodel);
		
		/*
		 * writes the conf-File
		 */
		createConfFile();

		
		try
    	{
			SimpleDateFormat df = new SimpleDateFormat("_yyyy_MM_dd");
			String filename = "Helixtrainer" + df.format(new Date()) + ".rdf";
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
		String queryString = 
			/* i do it kind of difficult, but i want to be certain that i only get the sequences of
			 Polypeptides(L) which contain at least one Helix. Furthermore i collect the information
			 about at which position helices begin and end.
			 NOTE:	this information has to be removed before oututing the model. But i will use this
			 		to check for positive and negative train amino acids
			*/ 
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
		/* i search for all amino acids (AA) that have a successor
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

	private static void createPositivesAndNegatives(ResIterator riter, PdbRdfModel model) {
		
		// Properties i have to use to check for while going through the AA-chain
		Property iib = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "isImmediatelyBefore");
		Property ba = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "beginsAt");
		Property ea = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "endsAt");
		ArrayList<Resource> pos = new ArrayList<Resource>();
		ArrayList<Resource> neg = new ArrayList<Resource>();
		
		
		// every first amino acid indicates a new AA-chain 
		while (riter.hasNext()) {
			// Initialization of variables needed
			Resource aaOne = riter.nextResource();
			Resource obj  = aaOne;
			Resource nobj = aaOne;
			boolean inHelix = false;
						
			// look if there is a next AA
			do {
				// looks weird, but is needed to enter loop even for the last AA which does not have a iib-Property
				obj = nobj;
				// die Guten ins Töpfchen ...
				// if we get an non-empty iterator for pdb:beginsAt the next AAs are within a AA-chain
				if(model.listResourcesWithProperty(ba, obj).hasNext() && !inHelix ){
					inHelix = true;
					System.out.println("Entering Helix!");
				}
				// die Schlechten ins Kröpfchen
				// if we get an non-empty iterator for pdb:endsAt and are already within a AA-chain
				// the AAs AFTER the current ones aren't within a helix
				if (model.listResourcesWithProperty(ea, obj).hasNext() && inHelix){
					inHelix = false;
					System.out.println("Leaving Helix!");
				}
				// get next AA if there is one
				if (model.listObjectsOfProperty(obj, iib).hasNext()){
					nobj = model.getProperty(obj, iib).getResource();
				}
				
				// do something different if we are in a helix
				if (inHelix){
					pos.add(obj);
					System.out.println(obj.getURI() + " " + iib.getURI() + " " + nobj.getURI() + " we are in!");
				} else {
					neg.add(obj);
					System.out.println(obj.getURI() + " " + iib.getURI() + " " + nobj.getURI());
				}
				
			} while (obj.hasProperty(iib)) ;
		}
		positives = pos;
		negatives = neg;
	}
	
	private static void createConfFile(){
		try
    	{
			SimpleDateFormat df = new SimpleDateFormat("_yyyy_MM_dd");
			String filename = "pdb" + df.format(new Date()) + ".conf";
			PrintStream out = new PrintStream (new File(filename));
			
			out.println("import(\"AA_properties.owl\");");
			out.println("import(\"" + filename + "\");");
			out.println();
			
			for (int i = 0 ; i < positives.size() ; i++ ) {
				out.println("+\"" + positives.get(i).getURI() + "\"");
			}
			
			for (int i = 0 ; i < negatives.size() ; i++ ) {
				out.println("-\"" + negatives.get(i).getURI() + "\"");
			}

			// Important - free up resources used running the query
			out.close();
    	}
    	catch (IOException e)
    	{
    		System.err.println("OutputStream konnte nicht geschlossen werden!");
    	}
	}
}