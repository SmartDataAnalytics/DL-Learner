package org.dllearner.tools.ore.explanation;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.dllearner.tools.ore.explanation.laconic.OPlus;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLAsymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLAxiomVisitorEx;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLDifferentIndividualsAxiom;
import org.semanticweb.owlapi.model.OWLDisjointClassesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLDisjointUnionAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentDataPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLEquivalentObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLHasKeyAxiom;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLInverseFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLIrreflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLNegativeDataPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLNegativeObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLReflexiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLSameIndividualAxiom;
import org.semanticweb.owlapi.model.OWLSubAnnotationPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubClassOfAxiom;
import org.semanticweb.owlapi.model.OWLSubDataPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubObjectPropertyOfAxiom;
import org.semanticweb.owlapi.model.OWLSubPropertyChainOfAxiom;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLTransitiveObjectPropertyAxiom;
import org.semanticweb.owlapi.model.SWRLRule;

public class RemainingAxiomPartsGenerator {
	
	private OPlus oPlusGen;
	private OWLOntology ontology;
	private OWLDataFactory dataFactory;
	
	private Map<OWLAxiom, Set<OWLAxiom>> source2AxiomsMap = new HashMap<OWLAxiom, Set<OWLAxiom>>();
	
	private Map<OWLAxiom, Map<OWLAxiom, Set<OWLAxiom>>> axiom2RemainingAxiomsMap = new HashMap<OWLAxiom, Map<OWLAxiom, Set<OWLAxiom>>>();
	
	public RemainingAxiomPartsGenerator(OWLOntology ontology, OWLDataFactory factory){
		this.ontology = ontology;
		this.dataFactory = factory;
		
		oPlusGen = new OPlus(dataFactory);
		
	}
	
	public Map<OWLAxiom, Set<OWLAxiom>> getRemainingAxiomParts(OWLAxiom laconicAxiom){
		Set<OWLAxiom> oplus;
		Set<OWLAxiom> sourceAxioms = new HashSet<OWLAxiom>();
		Set<OWLAxiom> remainingAxioms;
		
		Map<OWLAxiom,Set<OWLAxiom>> source2RemainingAxiomsMap = axiom2RemainingAxiomsMap.get(laconicAxiom);
		if(source2RemainingAxiomsMap == null){
			
			source2RemainingAxiomsMap = new HashMap<OWLAxiom, Set<OWLAxiom>>();
			axiom2RemainingAxiomsMap.put(laconicAxiom, source2RemainingAxiomsMap);
			
//			System.out.println(laconicAxiom.accept(new AxiomFinder(ontologies)).size());
			for(OWLAxiom ax : laconicAxiom.accept(new AxiomFinder(ontology))){
				oplus = source2AxiomsMap.get(ax);
				if(oplus == null){
					oplus = ax.accept(oPlusGen);
					source2AxiomsMap.put(ax, oplus);
				}
				if(oplus.contains(laconicAxiom)){
					sourceAxioms.add(ax);
					continue;
				} else {
					for(OWLAxiom part : laconicAxiom.accept(oPlusGen)){
						if(oplus.contains(part)){
			    			sourceAxioms.add(ax);
			    			break;
			    		}
					}
				}
				
	    		
	    	}
			
//			for(OWLOntology ont : ontologies){
//				for(OWLAxiom ax : ont.getLogicalAxioms()){
//					oplus = source2AxiomsMap.get(ax);
//					if(oplus == null){
//						oplus = ax.accept(oPlusGen);
//						source2AxiomsMap.put(ax, oplus);
//					}
//					if(oplus.contains(laconicAxiom)){
//						sourceAxioms.add(ax);
//						continue;
//					} else {
//						for(OWLAxiom part : laconicAxiom.accept(oPlusGen)){
//							if(oplus.contains(part)){
//				    			sourceAxioms.add(ax);
//				    			break;
//				    		}
//						}
//					}
//					
//		    		
//		    	}
//			}
			
//			System.out.println("Source axioms: " + sourceAxioms);
			for(OWLAxiom sourceAx : sourceAxioms){
				 remainingAxioms = new HashSet<OWLAxiom>();
//	    		System.out.println("Source axiom: " + sourceAx);
	    		Set<OWLAxiom> temp = new HashSet<OWLAxiom>(source2AxiomsMap.get(sourceAx));
	    		Set<OWLAxiom> laconicAxiomParts = laconicAxiom.accept(oPlusGen);
	    		temp.removeAll(laconicAxiomParts);
	    		for(Iterator<OWLAxiom> i = temp.iterator();i.hasNext();){
	    			OWLAxiom ax = i.next();
	    			for(OWLAxiom laconicAxiomPart : laconicAxiomParts){
	    				if(ax.accept(oPlusGen).contains(laconicAxiomPart)){
//		    				System.out.println(ax);
		    				i.remove();
		    				break;
		    			}
	    			}
	    			
	    		}
	    		remainingAxioms.addAll(temp);
	    		for(OWLAxiom ax : temp){
//	    			System.out.println("Temp: " + ax);
	    			for(OWLAxiom a : ax.accept(oPlusGen)){
//	    				System.out.println(a);
	    				if(temp.contains(a) && !a.equals(ax)){
	    					remainingAxioms.remove(a);
	    				}
	    			}
	    		}
	    		source2RemainingAxiomsMap.put(sourceAx, remainingAxioms);
//	        	System.out.println("Remaining axioms: " + remainingAxioms);
	    	}
		}
		
		
		
		return source2RemainingAxiomsMap;
		
	}
	
	public void clear(){
		source2AxiomsMap.clear();
		axiom2RemainingAxiomsMap.clear();
	}
	
	private class AxiomFinder implements OWLAxiomVisitorEx<Set<OWLAxiom>>{

		private OWLOntology ontology;
		
		public AxiomFinder(OWLOntology ontology){
			this.ontology = ontology;
		}

		private Set<OWLAxiom> getClassAxioms(){
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			axioms.addAll(ontology.getAxioms(AxiomType.SUBCLASS_OF, true));
			axioms.addAll(ontology.getAxioms(AxiomType.EQUIVALENT_CLASSES, true));
			axioms.addAll(ontology.getAxioms(AxiomType.DISJOINT_CLASSES, true));
			axioms.addAll(ontology.getAxioms(AxiomType.DISJOINT_UNION, true));
			axioms.addAll(ontology.getAxioms(AxiomType.HAS_KEY, true));
			return axioms;
		}
		
		private Set<OWLAxiom> getObjectPropertyAxioms(){
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			axioms.addAll(ontology.getAxioms(AxiomType.FUNCTIONAL_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.TRANSITIVE_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.SYMMETRIC_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.DISJOINT_OBJECT_PROPERTIES, true));
			axioms.addAll(ontology.getAxioms(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, true));
			axioms.addAll(ontology.getAxioms(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.INVERSE_OBJECT_PROPERTIES, true));
			axioms.addAll(ontology.getAxioms(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.OBJECT_PROPERTY_DOMAIN, true));
			axioms.addAll(ontology.getAxioms(AxiomType.OBJECT_PROPERTY_RANGE, true));
			axioms.addAll(ontology.getAxioms(AxiomType.REFLEXIVE_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.SUB_OBJECT_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.SUB_PROPERTY_CHAIN_OF, true));
			return axioms;
		}
		
		private Set<OWLAxiom> getDataPropertyAxioms(){
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			axioms.addAll(ontology.getAxioms(AxiomType.DATA_PROPERTY_DOMAIN, true));
			axioms.addAll(ontology.getAxioms(AxiomType.DATA_PROPERTY_RANGE, true));
			axioms.addAll(ontology.getAxioms(AxiomType.DISJOINT_DATA_PROPERTIES, true));
			axioms.addAll(ontology.getAxioms(AxiomType.EQUIVALENT_DATA_PROPERTIES, true));
			axioms.addAll(ontology.getAxioms(AxiomType.FUNCTIONAL_DATA_PROPERTY, true));
			axioms.addAll(ontology.getAxioms(AxiomType.SUB_DATA_PROPERTY, true));
			return axioms;
		}
		
		private Set<OWLAxiom> getClassAssertionAxioms(OWLIndividual ind){
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			for(OWLOntology ont : ontology.getImportsClosure()){
				axioms.addAll(ont.getClassAssertionAxioms(ind));
			}
			return axioms;
		}
		
		private Set<OWLAxiom> getDifferentIndividualAxioms(Set<OWLIndividual> individuals){
			Set<OWLAxiom> axioms = new HashSet<OWLAxiom>();
			for(OWLOntology ont : ontology.getImportsClosure()){
				for(OWLIndividual ind : individuals){
					axioms.addAll(ont.getDifferentIndividualAxioms(ind));
				}
			}
			return axioms;
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLSubClassOfAxiom axiom) {
			return getClassAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLEquivalentClassesAxiom axiom) {
			return getClassAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLDisjointClassesAxiom axiom) {
			return getClassAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLDisjointUnionAxiom axiom) {
			return getClassAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLSubPropertyChainOfAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLInverseObjectPropertiesAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLAsymmetricObjectPropertyAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLReflexiveObjectPropertyAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLObjectPropertyDomainAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLEquivalentObjectPropertiesAxiom axiom) {
			return getObjectPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLDisjointObjectPropertiesAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLObjectPropertyRangeAxiom axiom) {
			return getObjectPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLFunctionalObjectPropertyAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLSubObjectPropertyOfAxiom axiom) {
			return getObjectPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLTransitiveObjectPropertyAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLIrreflexiveObjectPropertyAxiom axiom) {
			return getObjectPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLSymmetricObjectPropertyAxiom axiom) {
			return getObjectPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLInverseFunctionalObjectPropertyAxiom axiom) {
			return getObjectPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLSubDataPropertyOfAxiom axiom) {
			return getDataPropertyAxioms();
		}

		
		@Override
		public Set<OWLAxiom> visit(OWLDataPropertyRangeAxiom axiom) {
			return getDataPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLFunctionalDataPropertyAxiom axiom) {
			return getDataPropertyAxioms();
		}

		@Override
		public Set<OWLAxiom> visit(OWLEquivalentDataPropertiesAxiom axiom) {
			return getDataPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLDataPropertyDomainAxiom axiom) {
			return getDataPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLDisjointDataPropertiesAxiom axiom) {
			return getDataPropertyAxioms();
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLClassAssertionAxiom axiom) {
			return getClassAssertionAxioms(axiom.getIndividual());
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLObjectPropertyAssertionAxiom axiom) {
			return null;
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLNegativeObjectPropertyAssertionAxiom axiom) {
			return null;
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLDataPropertyAssertionAxiom axiom) {
			return null;
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLNegativeDataPropertyAssertionAxiom axiom) {
			return null;
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLSameIndividualAxiom axiom) {
			return null;
		}
		
		@Override
		public Set<OWLAxiom> visit(OWLDifferentIndividualsAxiom axiom) {
			return getDifferentIndividualAxioms(axiom.getIndividuals());
		}

		@Override
		public Set<OWLAxiom> visit(OWLDeclarationAxiom axiom) {
			return null;
		}

		@Override
		public Set<OWLAxiom> visit(OWLAnnotationAssertionAxiom axiom) {
			return null;
		}

		@Override
		public Set<OWLAxiom> visit(SWRLRule axiom) {
			return null;
		}

		@Override
		public Set<OWLAxiom> visit(OWLHasKeyAxiom axiom) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<OWLAxiom> visit(OWLDatatypeDefinitionAxiom axiom) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<OWLAxiom> visit(OWLSubAnnotationPropertyOfAxiom axiom) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<OWLAxiom> visit(OWLAnnotationPropertyDomainAxiom axiom) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Set<OWLAxiom> visit(OWLAnnotationPropertyRangeAxiom axiom) {
			// TODO Auto-generated method stub
			return null;
		}
	
	
	}
}
		
		
