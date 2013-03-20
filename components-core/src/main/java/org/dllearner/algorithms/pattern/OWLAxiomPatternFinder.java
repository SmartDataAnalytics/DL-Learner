package org.dllearner.algorithms.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.io.ToStringRenderer;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.util.IRIShortFormProvider;
import org.semanticweb.owlapi.util.OWLAxiomVisitorAdapter;
import org.semanticweb.owlapi.util.OWLClassExpressionVisitorAdapter;
import org.semanticweb.owlapi.util.SimpleIRIShortFormProvider;

import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owlapi.dlsyntax.DLSyntaxObjectRenderer;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.Multiset;

public class OWLAxiomPatternFinder {
	
	private static Queue<String> classVarQueue = new LinkedList<String>();
	private static Queue<String> propertyVarQueue = new LinkedList<String>();
	private static Queue<String> individualVarQueue = new LinkedList<String>();
	private static Queue<String> datatypeVarQueue = new LinkedList<String>();
	
	static{
		for(int i = 65; i <= 90; i++){
			classVarQueue.add(String.valueOf((char)i));
		}
		for(int i = 97; i <= 111; i++){
			individualVarQueue.add(String.valueOf((char)i));
		}
		for(int i = 112; i <= 122; i++){
			propertyVarQueue.add(String.valueOf((char)i));
		}
		
	};

	public OWLAxiomPatternFinder(OWLOntology ontology) {
		generalizeAxioms(ontology.getLogicalAxioms());
	}
	
	private void generalizeAxioms(Collection<? extends OWLAxiom> axioms){
		ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
		String NS = "http://dl-learner.org/pattern/";
		IRIShortFormProvider sfp = new SimpleIRIShortFormProvider();
//		renderer.setShortFormProvider(new FullIRIEntityShortFromProvider());
		for(OWLAxiom axiom : axioms){
			OWLAxiom nnfAxiom = axiom.getNNF();
			String renderedAxiom = renderer.render(nnfAxiom);
			final OWLDataFactory dataFactory = new OWLDataFactoryImpl();
			Map<OWLEntity, OWLEntity> map = new HashMap<OWLEntity, OWLEntity>();
			Queue<String> classVarQueue = new LinkedList<String>(this.classVarQueue);
			Queue<String> propertyVarQueue = new LinkedList<String>(this.propertyVarQueue);
			Queue<String> individualVarQueue = new LinkedList<String>(this.individualVarQueue);
			Queue<String> datatypeVarQueue = new LinkedList<String>(this.datatypeVarQueue);
			final Set<String> classNames = new HashSet<String>();
			final Set<String> objectPropertyNames = new HashSet<String>();
			final Set<String> dataPropertyNames = new HashSet<String>();
			final Set<String> individualNames = new HashSet<String>();
			final Set<String> datatypeNames = new HashSet<String>();
			for(OWLEntity entity : axiom.getSignature()){
				if(entity.isOWLClass()){
					if(entity.asOWLClass().isBuiltIn()){
						
					} else {
						OWLEntity newEntity = dataFactory.getOWLEntity(EntityType.CLASS, IRI.create(NS + classVarQueue.poll()));
						map.put(entity, newEntity);
						classNames.add(newEntity.toStringID());
					}
				} else if(entity.isOWLObjectProperty()){
					OWLEntity newEntity = dataFactory.getOWLEntity(EntityType.OBJECT_PROPERTY, IRI.create(NS + propertyVarQueue.poll()));
					map.put(entity, newEntity);
					objectPropertyNames.add(newEntity.toStringID());
				} else if(entity.isOWLDataProperty()){
					OWLEntity newEntity = dataFactory.getOWLEntity(EntityType.DATA_PROPERTY, IRI.create(NS + propertyVarQueue.poll()));
					map.put(entity, newEntity);
					dataPropertyNames.add(newEntity.toStringID());
				} else if(entity.isOWLNamedIndividual()){
					OWLEntity newEntity = dataFactory.getOWLEntity(EntityType.NAMED_INDIVIDUAL, IRI.create(NS + individualVarQueue.poll()));
					map.put(entity, newEntity);
					individualNames.add(newEntity.toStringID());
				} else if(entity.isOWLDatatype()){
//					OWLEntity newEntity = dataFactory.getOWLEntity(EntityType.DATATYPE, IRI.create(datatypeVarQueue.poll()));
					OWLEntity newEntity = dataFactory.getOWLEntity(EntityType.DATATYPE, IRI.create(sfp.getShortForm(entity.getIRI())));
//					OWLEntity newEntity = entity;
					map.put(entity, newEntity);
					datatypeNames.add(newEntity.toStringID());
				}
			}
//			System.out.println(renderedAxiom);
			for (Entry<OWLEntity, OWLEntity> entry : map.entrySet()) {
				OWLEntity key = entry.getKey();
				OWLEntity value = entry.getValue();
				renderedAxiom = renderedAxiom.replaceAll("\\b" + sfp.getShortForm(key.getIRI()) + "\\b", value.toStringID());
			}
			ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(dataFactory, renderedAxiom);
			parser.setOWLEntityChecker(new OWLEntityChecker() {
				
				@Override
				public OWLObjectProperty getOWLObjectProperty(String iri) {
					return objectPropertyNames.contains(iri) ? dataFactory.getOWLObjectProperty(IRI.create(iri)) : null;
				}
				
				@Override
				public OWLNamedIndividual getOWLIndividual(String iri) {
					return individualNames.contains(iri) ? dataFactory.getOWLNamedIndividual(IRI.create(iri)) : null;
				}
				
				@Override
				public OWLDatatype getOWLDatatype(String iri) {
					return datatypeNames.contains(iri) ? dataFactory.getOWLDatatype(IRI.create(iri)) : null;
				}
				
				@Override
				public OWLDataProperty getOWLDataProperty(String iri) {
					return dataPropertyNames.contains(iri) ? dataFactory.getOWLDataProperty(IRI.create(iri)) : null;
				}
				
				@Override
				public OWLClass getOWLClass(String iri) {
					if(iri.equals("Thing")){
						return dataFactory.getOWLThing();
					} else if(iri.equals("Nothing")){
						return dataFactory.getOWLNothing();
					} else {
						return classNames.contains(iri) ? dataFactory.getOWLClass(IRI.create(iri)) : null;
					}
				}
				
				@Override
				public OWLAnnotationProperty getOWLAnnotationProperty(String iri) {
					return null;
				}
			});
			try {
				OWLAxiom parsedAxiom = parser.parseAxiom();
				System.out.println(parsedAxiom);
			} catch (ParserException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		ToStringRenderer.getInstance().setRenderer(new DLSyntaxObjectRenderer());
		String ontologyURL = "http://protege.stanford.edu/plugins/owl/owl-library/koala.owl";
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		OWLDataFactory dataFactory = man.getOWLDataFactory();
		OWLOntology ontology = man.loadOntology(IRI.create(ontologyURL));
		
		OWLAxiomRenamer renamer = new OWLAxiomRenamer(dataFactory);
		Multiset<OWLAxiom> multiset = HashMultiset.create();
		for (OWLAxiom axiom : ontology.getLogicalAxioms()) {
			OWLAxiom renamedAxiom = renamer.rename(axiom);
			multiset.add(renamedAxiom);
//			System.out.println(axiom + "-->" + renamedAxiom);
		}
		for (OWLAxiom owlAxiom : multiset.elementSet()) {
			System.out.println(owlAxiom + ": " + multiset.count(owlAxiom));
		}
		
		
//		OWLAxiomPatternFinder pf = new OWLAxiomPatternFinder(ontology);
	}

}
