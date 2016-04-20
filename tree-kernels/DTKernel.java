package org.dllearner.experiments;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.jena.riot.Lang;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree;
import org.dllearner.algorithms.qtl.datastructures.impl.RDFResourceTree.Rendering;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class DTKernel {
	
//	private static String URI="http://www.example.org/lymphography#Name";
	private static String URI="http://www.example.org/lymphography#Target2_Metastases";
	//private static String URI="http://www.example.org/lymphography#Target1_NormalFind";
	//private static String URI="http://www.example.org/lymphography#Target4_Fibrosis";
	//private static String URI="http://dllearner.org/balance/#";
	private static String FormatTree(String tree)
	{
		//getclass();
		String temptree= tree.replace("http://www.example.org/lymphography#", "");
		temptree= temptree.replace("rdf:", "");
		temptree= temptree.replace("owl:", "");
		temptree= temptree.replace("xsd:", "");
		temptree= temptree.replace(">", "");
		temptree= temptree.replace("<", "");
		temptree= temptree.replace("^^double", "(isDouble)");
		temptree= temptree.replace("\"", "");
		return temptree;
	}
	public static void main(String[] args) throws FileNotFoundException {
		int count=0;
		QueryTreeFactory factory = new QueryTreeFactoryBase();
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		model.read(new FileInputStream("../examples/lymphography/lymphography.owl"), null, Lang.RDFXML.getLabel());
		//model.read(new FileInputStream("../Dtree/breasttissue.owl"), null, Lang.RDFXML.getLabel());	
		//model.read(new FileInputStream("../UCI/balancetry.owl"), null, Lang.RDFXML.getLabel());
		//model.read(new FileInputStream("../Dtree/alzheimer.owl"), null, Lang.RDFXML.getLabel());
		///home/hajira/Documents/DTrees/heart.owl
		//ExtendedIterator<Individual> it = model.listIndividuals(model.createOntResource("http://example.com/foo#train"));
		ExtendedIterator<Individual> it = model.listIndividuals(model.createOntResource(URI));
		//RDFResourceTree classTree = factory.getQueryTree(URI, model);
		
		while(it.hasNext()) {
			Individual ind = it.next();
			//System.out.println(ind.getURI());
			
			RDFResourceTree tree = factory.getQueryTree(ind.getURI(), model);
					
			String treeString = tree.getStringRepresentation(Rendering.BRACES);
			
			
			
			try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("../Dtree/Lymph.txt", true)))) {
			
				String formatedTree=FormatTree(treeString);
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
		//int a = 23;
		//System.out.println(count +""+ a);
	}

}
