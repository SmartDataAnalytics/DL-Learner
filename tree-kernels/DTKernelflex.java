package org.dllearner.experiments;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.riot.Lang;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree.Rendering;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.core.KnowledgeSource;

import com.google.common.base.Charsets;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Files;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.XSD;
// File used to convert from OOLLDDD conf files into trees and may be classification too ..


public class DTKernelflex {
	
	
//	private static String fileName="../examples/mutagenesis/train1.conf";
//	private static String owlFilename="../examples/mutagenesis/mutagenesis.owl";
//	private static String treeFilename= "../Dtree/mutagenesistrain1.txt";
//	private static String replaceURI= "http://dl-learner.org/mutagenesis#";
	
//	private static String fileName="../test/breasttissue/train6.conf";
//	private static String owlFilename="../test/breasttissue/breasttissue.owl";
//	private static String treeFilename= "../Dtree/breasttissueclass6666.txt";
//	private static String replaceURI= "http://dl-learner.org/breasttissue#";
	
	
//	private static String fileName="../test/cardiotocography/train1.conf";
//	private static String owlFilename="../test/cardiotocography/cardiotocography.owl";
//	private static String treeFilename= "../Dtree/cardiotocography.txt";
//	private static String replaceURI= "http://dl-learner.org/cardiotocography#";
	
	
	
	private static String fileName="../examples/lymphography/lymphography_Class1.conf";
	private static String owlFilename="../examples/lymphography/lymphography.owl";
	private static String treeFilename= "../Dtree/cardiotocography.txt";
	private static String replaceURI= "http://www.example.org/lymphography#";
//	
	//private static String URI="http://www.example.org/lymphography#Target2_Metastases";
	//private static String URI="http://www.example.org/lymphography#Target1_NormalFind";
	//private static String URI="http://www.example.org/lymphography#Target4_Fibrosis";
	private static boolean positive;
	private static String URI="";
	private static String FormatTree(String tree)
	{
		//getclass();
		String temptree= tree.replace(replaceURI, "");
		temptree= temptree.replace("rdf:", "");
		temptree= temptree.replace("owl:", "");
		temptree= temptree.replace("xsd:", "");
		temptree= temptree.replace(">", "");
		temptree= temptree.replace("<", "");
		temptree= temptree.replace("^^double", "(isDouble)");
		temptree= temptree.replace("\"", "");
		//temptree= temptree.replace("^^double", "(isDouble)");
		return temptree;
	}
	public static void readfile() throws Exception
	{
		
		List<String> lines = Files.readLines(new File(fileName), Charsets.UTF_8);
	
	
		// we assume that the first line contains relation names
		String firstLine = lines.get(10);
		String[] relationNames = firstLine.split("\"");
		if (relationNames[0].equalsIgnoreCase("-"))
		{
			positive = false;
			
		}
		else
		{
			positive = true;
			
		}
		URI= relationNames[1];
		
		
		// we assume that second line contains class names that define the range of each relation
//		String secondLine = lines.get(1);
//		String[] classNames = secondLine.split(",");

		
	}
	public static void main(String[] args) throws FileNotFoundException {
		
		// read Owl file 
		 
		int count=0;
		QueryTreeFactory factory = new QueryTreeFactoryBase();
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read(new FileInputStream(owlFilename), null, Lang.RDFXML.getLabel());
		
		for(DatatypeProperty p : model.listDatatypeProperties().toList()){
			OntResource range = p.getRange();
			if(range.equals(XSD.xdouble)) {
//				List<RDFNode> values = model.listObjectsOfProperty(p).toList();
				List<Statement> statements = model.listStatements(null, p, (RDFNode)null).toList();
				
				double min =0, max=0,tot=0, avg=0;
				System.out.println(p);
				for (Statement st : statements) {
					RDFNode value = st.getObject();
					
					double v = value.asLiteral().getDouble();
					if( min > v){
						min = v;
					}
					if( max < v){
						max = v;
					} 
					
					tot=tot+v;
					//System.out.println(v);
				}
				avg=tot/statements.size();
				System.out.println("Min::"+min+" Max::"+max+" Sum"+tot+" Average"+avg+" Total"+statements.size());
				// Low = min -(min + avg)/2, medium = (min + avg)/2  - (max + avg)/2 , high = (max + avg)/2 - max
				String temp ="";
				// re-iterate to put the values
				
				List<Statement> toAdd = new ArrayList<>();
				for (Iterator<Statement> iter = model.listStatements(null, p, (RDFNode)null); iter.hasNext();) {
					Statement st = iter.next();
					
					RDFNode value = st.getObject();
					
					double v = value.asLiteral().getDouble();
					if( v <= ((min + avg)/2)){
						// put Low value in model with URI p
						temp = "Low";
						
					}else
					if( ( v > ((min + avg)/2)) && (v < ((max + avg)/2) ) ) {
						// put Medium value in model with URI p
						temp = "Medium";
					} else
					if( (v >= ((max + avg)/2) ) ) {
						// put High value in model with URI p
						temp = "High";
					}
					
					iter.remove();
					toAdd.add(model.createStatement(st.getSubject(), p, model.asRDFNode(NodeFactory.createLiteral(temp))));
					System.out.println(v+"  "+temp);
				}
				model.add(toAdd);
			}
		}
		// read conf file
		try
		{
		List<String> lines = Files.readLines(new File(fileName), Charsets.UTF_8);
		
		for(int i=10; i<lines.size();i++)
		{
					
					String firstLine = lines.get(i);
					String[] classInfo = firstLine.split("\"");
					if (classInfo[0].equalsIgnoreCase("-"))
					{
						positive = false;
						
					}
					else
					{
						positive = true;
						
					}
					URI= classInfo[1];
		
		// You have read the class label, read the URI and put the tree in file :::
			RDFResourceTree tree = factory.getQueryTree(URI, model);
			
			// here, you have both resource / URI and information if it is positive or negative
			
			String treeString = tree.getStringRepresentation(Rendering.BRACES);
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(treeFilename, true)))) {
			
				String formatedTree=FormatTree(treeString);
				out.println(positive);
				out.print(formatedTree);
				count ++;
			//	if (count>4)
				//{
					//break;
				//}
				out.println("\n\n\n");
			}catch (IOException e) {
			    //exception handling left as an exercise for the reader
			
		}
		}
			
		/*
		QueryTreeFactory factory = new QueryTreeFactoryBase();
		Model model = ModelFactory.createDefaultModel();
		String lang = "RDF/XML";
		model.read(new FileInputStream("/home/hajira/Documents/carcinogenesis.owl"), null, lang);
		String exampleURI = "http://dl-learner.org/carcinogenesis#bond1166";
		RDFResourceTree tree = factory.getQueryTree(exampleURI, model, 5);
		
		try{
			PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
			writer.println(exampleURI);
			writer.println(treeString);
			writer.close();
		}
	
catch (Exception e)
{
	
}
*/
	System.out.println(count);
		}
		catch(IOException e)
		{}
	
		int a = 23;
//		System.out.println(count +""+ a);
	}

}
