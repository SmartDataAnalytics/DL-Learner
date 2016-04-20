/**
 * 
 */
package org.dllearner.examples;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.riot.Lang;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryBase;
import org.dllearner.cli.CLI;
import org.dllearner.cli.CrossValidation;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractClassExpressionLearningProblem;
import org.dllearner.core.AbstractKnowledgeSource;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.learningproblems.ClassLearningProblem;
import org.dllearner.reasoning.ClosedWorldReasoner;
import org.dllearner.refinementoperators.RhoDRDown;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.collect.Sets;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.XSD;

import uk.ac.manchester.cs.owl.owlapi.OWLClassImpl;
import uk.ac.manchester.cs.owl.owlapi.mansyntaxrenderer.ManchesterOWLSyntaxOWLObjectRendererImpl;

/**
 * A basic example how to use DL-Learner.
 * 
 * Knowledge base: a family ontology
 * Target Concept: father
 * 
 * @author Lorenz Buehmann
 *
 */
public class TenFoldCateg {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		ToStringRenderer.getInstance().setRenderer(new ManchesterOWLSyntaxOWLObjectRendererImpl());
		String conffileName="../examples/carcinogenesis/tenfold.conf";
		File file = new File("../examples/carcinogenesis/carcinogenesis.owl");
		//String conffileName="../examples/mutagenesis/train2.conf";
		//File file = new File("../examples/mutagenesis/mutagenesis.owl");
		
		//////////////////////////////////////////////////////////////////////////////
		
		
		File confFile= new File(conffileName);
		CLI cl = new CLI(confFile);
		cl.init();
		
		// read Owl file 
		KnowledgeSource ks = cl.getKnowledgeSource();
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				
		if(ks instanceof OWLFile) {
			model.read(new FileInputStream(((OWLFile) ks).getURL().getPath()), null, Lang.RDFXML.getLabel());
		} else {
			throw new Exception("No ontology file.");
		}
		
		
		QueryTreeFactory factory = new QueryTreeFactoryBase();
		
	
		for(DatatypeProperty p : model.listDatatypeProperties().toList()){
			
			OntResource range = p.getRange();
			if(range.equals(XSD.xdouble)) {
//				List<RDFNode> values = model.listObjectsOfProperty(p).toList();
				List<Statement> statements = model.listStatements(null, p, (RDFNode)null).toList();
				
				double min =0, max=0,tot=0, avg=0;
				
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
					//System.out.println(v+"  "+temp);
				}
				model.add(toAdd);
				
				p.removeRange(XSD.xdouble);
				p.addRange(XSD.xstring);
			} 
			
			
			
		}
		//model.write(new FileWriter("../Dtree/TRRRRYYYY.owl"));
		// convert into owl ontolgy
		//OwlApiJenaUtils ontology = new OwlApiJenaUtils();
		OwlApiJenaUtils.getOWLOntology(model);
		/////////////////////////////////////////////////////////////////////////////
				
		OWLOntology ontology = OwlApiJenaUtils.getOWLOntology(model);;//OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(file);
		ks = new OWLAPIOntology(ontology);
		ks.init();
		
		// setup the reasoner
		ClosedWorldReasoner rc = new ClosedWorldReasoner(ks);
		rc.init();
		// read Owl file 
		//ontology.getClassAssertionAxioms()
		
		// create the class of the property range
		//OWLClass cls = df.getOWLClass(IRI.create(NS + classNames[col]));
		RhoDRDown op = new RhoDRDown();
		op.setReasoner(rc);
		op.setUseStringDatatypes(true); // sent by lorenz
		op.setFrequencyThreshold(5);
		op.setUseDataHasValueConstructor(true);
		op.setUseHasValueConstructor(true);
		op.init();
		
		AbstractClassExpressionLearningProblem lp = cl.getLearningProblem();
		lp.setReasoner(rc);
		lp.init();
		
		CELOE alg = new CELOE(lp, rc);
		alg.setMaxExecutionTimeInSeconds(10);
		alg.setWriteSearchTree(true);
		alg.setSearchTreeFile("/tmp/dllearner/search-treel.log");
		alg.setReplaceSearchTree(true);
		alg.setOperator(op);
		alg.init();
		// deleting class information from ontology ....
		//		OWLClassImpl cls1 = new OWLClassImpl(IRI.create("http://www.example.org/lymphography#Target1_NormalFind"));
//		OWLClassImpl cls2 = new OWLClassImpl(IRI.create("http://www.example.org/lymphography#Target2_Metastases"));
//		OWLClassImpl cls3 = new OWLClassImpl(IRI.create("http://www.example.org/lymphography#Target3_MalignLymph"));
//		OWLClassImpl cls4 = new OWLClassImpl(IRI.create("http://www.example.org/lymphography#Target4_Fibrosis"));
//		Set<OWLClass> ignoredConcepts = Sets.newHashSet();
//		ignoredConcepts.add(cls1);
//		ignoredConcepts.add(cls2);
//		ignoredConcepts.add(cls3);
//		ignoredConcepts.add(cls4);
//		alg.setIgnoredConcepts(ignoredConcepts);
		
		CrossValidation cv= new CrossValidation(alg,lp,rc,10,false);
		// run the learning algorithm
//		alg.start();
	}

}
