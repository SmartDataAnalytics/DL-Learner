package org.dllearner.examples.pdb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.xml.sax.InputSource;

import com.dumontierlab.pdb2rdf.model.PdbRdfModel;
import com.dumontierlab.pdb2rdf.parser.PdbXmlParser;
import com.dumontierlab.pdb2rdf.util.Pdb2RdfInputIterator;
import com.dumontierlab.pdb2rdf.util.PdbsIterator;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class PDBIdRdfModel {

	private static Logger _logger = Logger.getRootLogger();
	
	private PdbRdfModel _pdbIdModel = new PdbRdfModel();
	private PdbRdfModel _removedFromModel = new PdbRdfModel();
	private PDBProtein _protein = null ;
	private ArrayList<Resource> _positives = null;
	private ArrayList<Resource> _negatives = null;
	private HashMap<Integer, Resource> _positionResource = null;
	
	public PDBIdRdfModel (PDBProtein protein) {
		
		this._protein = protein;
		this._pdbIdModel = this.getPdbRdfModel();
		this.getProtein().setSequence(extractSequence(_pdbIdModel));
		System.out.println("Sequence: " + this.getProtein().getSequence());
		this.getProtein().setSpecies(extractSpecies(_pdbIdModel));
		System.out.println("Species: " + this.getProtein().getSpecies());
		createPositivesAndNegatives();
		_positionResource = createPositionResidueMap();
	}
	
	public PdbRdfModel getModel(){
		return _pdbIdModel;
	}
	
	public PDBProtein getProtein() {
		return _protein;
	}

	public ArrayList<Resource> getPositives(){
		return _positives;
	}
	
	public ArrayList<Resource> getNegatives(){
		return _negatives;
	}
	
	public HashMap<Integer, Resource> getPositionResource(){
		return _positionResource;
	}

	private PdbRdfModel getPdbRdfModel() {
		String[] pdbIDs = {_protein.getPdbID()}; 
	    Pdb2RdfInputIterator i = new PdbsIterator(pdbIDs);
	    PdbXmlParser parser = new PdbXmlParser();
        PdbRdfModel model = new PdbRdfModel();
        try {
        	while (i.hasNext())
        	{
        		final InputSource input = i.next();
        		model = parser.parse(input, new PdbRdfModel());
        		/*
        		 *  jedes Model muss gleich nach den relevanten Daten durchsucht werden,
        		 *  da ansonsten Probleme mit der Speichergröße auftreten können. 
        		 */

        		_pdbIdModel.add(extractDataForPdbAndChain(model, _protein.getPdbID(), _protein.getChainID()));
        	}
        	try {
				PrintStream test = new PrintStream (new File("../test/pdb/" + this.getProtein().getPdbID() + ".rdf"));
				_pdbIdModel.write(test, "RDF/XML");
				test.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
        }
        catch (Exception e)
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    return _pdbIdModel;
	}
	
	private String extractSpecies(PdbRdfModel model) {
		String queryString ;
		queryString = 
			"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
			"PREFIX dcterms: <http://purl.org/dc/terms/> " +
			"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
			"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
			"PREFIX fn: <http://www.w3.org/2005/xpath-functions#> " +
			"CONSTRUCT { <http://bio2rdf.org/pdb:" + this.getProtein().getPdbID() + "/extraction/source/gene/organism> rdfs:label ?species. }" +
    		"WHERE { ?x1 dcterms:isPartOf ?x2 ." +
	    		" ?x1 rdf:type ?x3 ." +
	    		" ?x1 pdb:isImmediatelyBefore ?x4 ." +
				" OPTIONAL { ?x5 rdfs:label ?species FILTER (str(?x5) = fn:concat(str(?x2), '/extraction/source/gene/organism')) . } . }";
		
		// System.out.println(queryString);
		
		PdbRdfModel construct = new PdbRdfModel();
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	construct.add(qe.execConstruct());
		Resource organism = ResourceFactory.createResource("http://bio2rdf.org/pdb:" + this.getProtein().getPdbID() + "/extraction/source/gene/organism");
    	Property label = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#", "label");
		String species = "";
		try
		{
			NodeIterator niter = construct.listObjectsOfProperty(organism, label);
			while ( niter.hasNext() )
			{
				RDFNode nextRes = niter.next();
				species = nextRes.toString();
/*				QuerySolution soln = results.nextSolution() ;
				Literal l = soln.getLiteral("species") ;   // Get a result variable - must be a literal
				species = l.getString();*/
				System.out.println(species);
			}
		}
		finally 
		{
			qe.close() ;
		}
		return species;
	}

	private String extractSequence(PdbRdfModel model) {

		String sequence = null;
		
		Property type = ResourceFactory.createProperty("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "type");
		Property hasValue = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "hasValue");
		Resource polymerSequence = ResourceFactory.createResource("http://bio2rdf.org/pdb:PolymerSequence");
		
		ResIterator riter = model.listResourcesWithProperty(type, polymerSequence);
		while (riter.hasNext()){
			Resource nextRes = riter.next();
			if (model.contains(nextRes, hasValue)){
				NodeIterator niter = model.listObjectsOfProperty(nextRes, hasValue);
				sequence = niter.next().toString();
				
				System.out.println("Sequence: " + sequence);
			}
		}
    	return sequence;
	}

	private PdbRdfModel extractDataForPdbAndChain(PdbRdfModel model, String pdbID, String chainID) {
    	
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
			 * 			to check for in-helix amino acids
			*/ 
		/*
		 * ich brauche noch die selektion der chain und die info über den genursprungsorganismus
		 * rdf:resource="http://bio2rdf.org/pdb:3LQH/chain_A"
		 * http://bio2rdf.org/pdb:3LQH/chain_A/position_1596
		 */
		
		 String queryString = 
				"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
				"PREFIX dcterms: <http://purl.org/dc/terms/> " +
				"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> " +
				"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
				"PREFIX fn: <http://www.w3.org/2005/xpath-functions#> " +
				"CONSTRUCT { ?x1 pdb:beginsAt ?x2 ." +
	    		" ?x1 pdb:endsAt ?x3 ." +
	    		" ?x5 dcterms:isPartOf ?x4 ." +
	    		" ?x5 rdf:type ?x6 ." +
	    		" ?x5 pdb:isImmediatelyBefore ?x7 ." +
	    		" ?x5 pdb:hasChainPosition ?x8 ." +
	    		" ?x8 rdfs:label ?residuePosition ." +
	    		" ?organism rdfs:label ?organismName ." +
	    		" ?seq rdf:type pdb:PolymerSequence ." +
	    		" ?seq pdb:hasValue ?sequence . } " +	    		
	    		"WHERE { ?x1 rdf:type pdb:Helix ." +
	    		" ?x1 pdb:beginsAt ?x2 ." +
	    		" ?x1 pdb:endsAt ?x3 ." +
	    		" ?x3 dcterms:isPartOf ?x4 ." +
	    		" ?x4 rdf:type <http://bio2rdf.org/pdb:Polypeptide(L)> ." +
	    		" ?x5 dcterms:isPartOf ?x4 ." +
	    		" ?x5 rdf:type ?x6 ." +
	    		" ?x5 pdb:hasChainPosition ?x8 ." +
	    		" ?x8 dcterms:isPartOf ?x4 ." +
	    		" ?x8 rdfs:label ?residuePosition ." +
	    		" OPTIONAL { ?x5 pdb:isImmediatelyBefore ?x7 . } . ";
		 
		 if (chainID.length() == 1 && pdbID.length() == 4)
			{
				queryString +=
						" ?x8 dcterms:isPartOf <http://bio2rdf.org/pdb:" + 
								pdbID.toUpperCase() +
								"/chain_" + chainID.toUpperCase() + "> .";
			}
		 queryString +=
				" ?x4 pdb:hasPolymerSequence ?seq . " +
	    		" ?seq rdf:type pdb:PolymerSequence . " +
	    		" ?seq pdb:hasValue ?sequence . " +
	    		// with the Optional clause i get the information by which amino acid
	    		// a amino acid is followed
	    		" OPTIONAL { ?organism rdfs:label ?organismName " +
	    			"FILTER (str(?organism) = fn:concat(str(?x4), '/extraction/source/gene/organism')) . } . }";
		
		System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, model);
    	construct.add(qe.execConstruct()); 
    	qe.close();
    	return construct;
	}
	
	public ResIterator getFirstAA() {
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
		QueryExecution qe = QueryExecutionFactory.create(query, _pdbIdModel);
    	construct.add(qe.execConstruct()); 
    	qe.close();
    	ResIterator niter = construct.listSubjects();
    	return niter;
	}
	
	private HashMap<Integer, Resource> createPositionResidueMap(){
		
		HashMap<Integer, Resource> posres = new HashMap<Integer, Resource>(150);
		Property iib = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "isImmediatelyBefore");

		ResIterator firstAAs = this.getFirstAA();
		while ( firstAAs.hasNext()){
			Resource firstAA = firstAAs.next();
			Resource nextAA = firstAA;
			Resource currentAA = firstAA;
			do {
				currentAA = nextAA;
				posres.put(new Integer(this.getResiduePosition(currentAA)), currentAA);
				nextAA = _pdbIdModel.getProperty(currentAA, iib).getResource();
			} while (currentAA.hasProperty(iib));
		}
		
		return posres;
	}
	
	private int getResiduePosition(Resource res) {
		Property hasChainPosition = ResourceFactory.createProperty("http://bio2rdf.org/pdb:", "hasChainPosition");
		Property label = ResourceFactory.createProperty("http://www.w3.org/2000/01/rdf-schema#", "label");
		ResourceFactory.createResource();
		
		NodeIterator residuePosition = _pdbIdModel.listObjectsOfProperty(res, hasChainPosition );
		ArrayList<RDFNode> positionNodes = new ArrayList<RDFNode>();
		ArrayList<String> positionLabels = new ArrayList<String>();
		while ( residuePosition.hasNext() ) {
			RDFNode positionNode = residuePosition.next();
			positionNodes.add(positionNode);
			NodeIterator positionLabelNodes = _pdbIdModel.listObjectsOfProperty( positionNode.asResource(), label );
			while ( positionLabelNodes.hasNext() ) {
				positionLabels.add(positionLabelNodes.next().toString());
			}
			
		}
		
		
		Integer position = null;
		if ( positionNodes.size() == 1 && positionLabels.size() == 1 ) {
			String positionLabel = positionLabels.get(0);
			String a = new String( "Position " );
			String b = new String( " on chain" );
			position = Integer.parseInt(
					positionLabel.substring(positionLabel.indexOf(a) + a.length(), positionLabel.indexOf(b)));
		} else {
			position = new Integer(0);
			_logger.error("");
		}
		return position.intValue();
	}
	
	public void addDistanceInfo(){
		String queryString = 
			"PREFIX pdb: <http://bio2rdf.org/pdb:> " +
    		"CONSTRUCT { ?x1 pdb:isFourAminoAcidsBefore ?x5 . } " +
    		"WHERE { ?x1 pdb:isImmediatelyBefore ?x2 . " +
    		" ?x2 pdb:isImmediatelyBefore ?x3 . " +
    		" ?x3 pdb:isImmediatelyBefore ?x4 . " +
    		" ?x4 pdb:isImmediatelyBefore ?x5 . }";
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, _pdbIdModel);
    	_pdbIdModel.add(qe.execConstruct()); 
    	qe.close();
	}
	
	public void addRemovedStatements(){
		_pdbIdModel.add(_removedFromModel);
		_removedFromModel.removeAll();
	}
	
	public void removeStatementsWithPoperty(Property prop){
		String queryString = 
			"PREFIX x:<" + prop.getNameSpace() + "> " +
    		"CONSTRUCT { ?x1 x:" + prop.getLocalName()+ " ?x2 . } " +
    		"WHERE { ?x1 x:" + prop.getLocalName() + " ?x2 . }";
		//System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, _pdbIdModel);
    	StmtIterator stmtiter = qe.execConstruct().listStatements(); 
    	qe.close();
    	while(stmtiter.hasNext()){
    		Statement nextStmt = stmtiter.next();
    		_pdbIdModel.remove(nextStmt);
    		_removedFromModel.add(nextStmt);
    	}
    	
	}
	
	public void removeStatementsWithObject(Resource res){
		String queryString =
			"PREFIX x:<" + res.getNameSpace() + "> " +
    		"CONSTRUCT { ?x1 ?x2 x:" + res.getLocalName() + " . } " +
    		"WHERE { ?x1 ?x2 x:" + res.getLocalName() + " . }";
		// System.out.println(queryString);
		Query query = QueryFactory.create(queryString);
		QueryExecution qe = QueryExecutionFactory.create(query, _pdbIdModel);
    	StmtIterator stmtiter = qe.execConstruct().listStatements(); 
    	qe.close();
    	while(stmtiter.hasNext()){
    		Statement nextStmt = stmtiter.next();
    		_pdbIdModel.remove(nextStmt);
    		_removedFromModel.add(nextStmt);
    	}
	}
	
	private void createPositivesAndNegatives() {
		
		ResIterator riter = this.getFirstAA();
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
			Resource firstAA = riter.nextResource();
			System.out.println("First AA: " + firstAA.getLocalName());
			Resource currentAA  = firstAA;
			Resource nextAA = firstAA;
			boolean inHelix = false;
						
			// look if there is a next AA
			do {
				// looks weird, but is needed to enter loop even for the last AA which does not have a iib-Property
				currentAA = nextAA;
				// die Guten ins Töpfchen ...
				// if we get an non-empty iterator for pdb:beginsAt the next AAs are within a AA-chain
				if(_pdbIdModel.listResourcesWithProperty(ba, currentAA).hasNext() && !inHelix ){
					inHelix = true;
				}
				// die Schlechten ins Kröpfchen
				// if we get an non-empty iterator for pdb:endsAt and are already within a AA-chain
				// the AAs AFTER the current ones aren't within a helix
				if (_pdbIdModel.listResourcesWithProperty(ea, currentAA).hasNext() && inHelix){
					inHelix = false;
				}
				// get next AA if there is one
				if (_pdbIdModel.contains(currentAA, iib)){
					nextAA = _pdbIdModel.getProperty(currentAA, iib).getResource();
				}
				
				// add current amino acid to positives or negatives set
				if (inHelix){
					pos.add(currentAA);
				} else {
					neg.add(currentAA);
				}
				
			} while (currentAA.hasProperty(iib)) ;
		}
		_positives = pos;
		System.out.println("+++ Positive set +++");
		for (int i = 0; i < pos.size(); i++){
			System.out.println("Das " + i + "te Element: " + pos.get(i).getLocalName());
		}
		
		_negatives = neg;
		System.out.println("+++ Negatvie set +++");
		for (int i = 0; i < neg.size(); i++){
			System.out.println("Das " + i + "te Element: " + neg.get(i).getLocalName());
		}
	}
	
	public void createFastaFile(String dir){
		try {
			String fastaFilePath = dir + this.getProtein().getFastaFileName();
			PrintStream out = new PrintStream (new File(fastaFilePath));
			out.println(">" + this.getProtein().getPdbID() + "." + this.getProtein().getChainID() + "." + this.getProtein().getSpecies());
			String sequence = this.getProtein().getSequence();
			int seqLength = sequence.length();
			
			if (seqLength > 80) {
				// write sequence in 80 character blocks into file
				int beginIndex = 0;
				int endIndex = 80;
				while ( endIndex <= seqLength ){
					out.println(sequence.substring(beginIndex, endIndex));
					if (seqLength - endIndex < 80){
						out.println(sequence.substring(endIndex, seqLength));
					}
					beginIndex = endIndex;
					endIndex += 80;
				}
				
			} else {
				out.println(sequence);
			}
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void createFastaFile(String dir, String fastaFileName){
		this.getProtein().setFastaFileName(fastaFileName);
		this.createFastaFile(dir);
	}
	
	
	/*
	 * OLD STUFF
	 * 
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
			_logger.debug(currentaa.getURI());
			// look if there is a next AA
			do {
				++i;
				_logger.debug(i);
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
					_logger.info(aaType.getURI());
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
	 
	 
	 */
}
