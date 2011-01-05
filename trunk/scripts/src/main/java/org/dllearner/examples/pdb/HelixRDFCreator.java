package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.rdf.model.Model;

import com.dumontierlab.pdb2rdf.model.PdbRdfModel;
import com.dumontierlab.pdb2rdf.parser.PdbXmlParser;
import com.dumontierlab.pdb2rdf.util.Pdb2RdfInputIterator;
import com.dumontierlab.pdb2rdf.util.PdbsIterator;


public class HelixRDFCreator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		TrainAndTestSet sets = new TrainAndTestSet(2);
		String[] trainIDs = sets.getTrainset();
		PdbRdfModel trainmodel = new PdbRdfModel();
		
		for(int i = 0; i < trainIDs.length; i++){
			System.out.println(trainIDs[i]);
			String[] ID = {trainIDs[i]};
			trainmodel.add(getRdfModelForIds(ID));
		}

		getRdfModelForIds(sets.getTrainset());
		// PdbRdfModel testmodel =  getRdfModelForIds(sets.getTestset());
		
		try
    	{
    	String queryString = 
    		"PREFIX pdb: <http://bio2rdf.org/pdb> " +
    		"CONSTRUCT { ?x1 ?x2 ?x3 .} " +
    		"WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Helix> . ?x1 ?x2 ?x3 .}";
    	Query query = QueryFactory.create(queryString);

    	// Execute the query and obtain results
    	QueryExecution qe = QueryExecutionFactory.create(query, trainmodel);
    	Model construct = qe.execConstruct();
    	OutputStream out = new FileOutputStream (new File("qwertzu_iop.rdf"));
    	// Output query results	
    	construct.write(out, "RDF/XML");




    	// Important - free up resources used running the query
    	}
    	catch (FileNotFoundException e)
    	{
    		System.err.println("Datei nicht gefunden!");
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
        		PdbRdfModel model = new PdbRdfModel();
        		model = parser.parse(input, new PdbRdfModel());
        		// jedes Model muss gleich nach den relevanten Daten durchsucht werden, 
        		// da ansonsten Probleme mit der Speichergröße auftreten können.
        		allmodels.add(getHelices(model));
        		
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
	
	private static PdbRdfModel collectData (PdbRdfModel inmodel) {
		PdbRdfModel collectmodel = new PdbRdfModel();
		collectmodel.add(getHelices(inmodel));
		
		
		return collectmodel;
	}
	
	private static Model getHelices(PdbRdfModel model) {
    	// Zweimal dasselbe Ergebnis einmal als SELECT und einmal als CONSTRUCT (für weitere Bearbeitung) Statement
		// SELECT Abfrage
		String queryString = 
    		" SELECT ?x1 ?x2 ?x3 WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Helix> . ?x1 ?x2 ?x3 .}";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	ResultSet select = qe.execSelect();
    	ResultSetFormatter.out (System.out, select, query);
		// CONSTRUCT Abfrage
    	queryString = 
    		"PREFIX pdb: <http://bio2rdf.org/pdb> " +
    		"CONSTRUCT { ?x1 ?x2 ?x3 .} " +
    		"WHERE { ?x1 <http://www.w3.org/1999/02/22-rdf-syntax-ns#type> <http://bio2rdf.org/pdb:Helix> . ?x1 ?x2 ?x3 .}";
    	query = QueryFactory.create(queryString);

    	// Execute the query and obtain results
    	qe = QueryExecutionFactory.create(query, model);
    	Model construct = qe.execConstruct();
    	qe.close();
    	return construct;
	}
	

}