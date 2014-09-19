/**
 * 
 */
package org.dllearner.algorithms.properties;

import static org.semanticweb.owlapi.model.AxiomType.ASYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.REFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUBCLASS_OF;
import static org.semanticweb.owlapi.model.AxiomType.SUB_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUB_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.TRANSITIVE_OBJECT_PROPERTY;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.algorithms.celoe.CELOE;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.dllearner.core.LearningAlgorithm;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import com.hp.hpl.jena.query.ParameterizedSparqlString;

/**
 * A utility class that holds links between entity type, axiom types and axiom algorithms.
 * @author Lorenz Buehmann
 *
 */
public class AxiomAlgorithms {
	
	static class AxiomTypeCluster {
		private final Set<AxiomType<? extends OWLAxiom>> axiomTypes = new HashSet<AxiomType<? extends OWLAxiom>>();
		private final ParameterizedSparqlString sampleQuery;
		
		public AxiomTypeCluster(Set<AxiomType<? extends OWLAxiom>> axiomTypes, ParameterizedSparqlString sampleQuery) {
			axiomTypes.addAll(axiomTypes);
			this.sampleQuery = sampleQuery;
		}
		
		/**
		 * @return the axiomTypes
		 */
		public Set<AxiomType<? extends OWLAxiom>> getAxiomTypes() {
			return axiomTypes;
		}
		
		/**
		 * @return the sampleQuery
		 */
		public ParameterizedSparqlString getSampleQuery() {
			return sampleQuery;
		}
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_HIERARCHY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUB_OBJECT_PROPERTY, EQUIVALENT_OBJECT_PROPERTIES, DISJOINT_OBJECT_PROPERTIES),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_CHARACTERISTICS_WITHOUT_TRANSITIVITY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SYMMETRIC_OBJECT_PROPERTY, ASYMMETRIC_OBJECT_PROPERTY,
						FUNCTIONAL_OBJECT_PROPERTY, INVERSE_FUNCTIONAL_OBJECT_PROPERTY, REFLEXIVE_OBJECT_PROPERTY, IRREFLEXIVE_OBJECT_PROPERTY),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_TRANSITITVITY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(TRANSITIVE_OBJECT_PROPERTY),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_DOMAIN_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(OBJECT_PROPERTY_DOMAIN),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_RANGE_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(OBJECT_PROPERTY_RANGE),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster DATA_PROPERTY_HIERARCHY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUB_DATA_PROPERTY, EQUIVALENT_DATA_PROPERTIES, DISJOINT_DATA_PROPERTIES),
				new ParameterizedSparqlString("CONSTRUCT {?s ?p ?o . ?s ?p1 ?o .} WHERE {?s ?p ?o . OPTIONAL {?s ?p1 ?o .}"));
		
		public static final AxiomTypeCluster DATA_PROPERTY_FUNCTIONALITY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(FUNCTIONAL_DATA_PROPERTY),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster DATA_PROPERTY_DOMAIN_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(DATA_PROPERTY_DOMAIN),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster DATA_PROPERTY_RANGE_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(DATA_PROPERTY_RANGE),
				new ParameterizedSparqlString(""));
		
		public static final AxiomTypeCluster CLASS_HIERARCHY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUBCLASS_OF, EQUIVALENT_CLASSES, DISJOINT_CLASSES),
				new ParameterizedSparqlString(""));
	}

	private static final Map<EntityType, Set<AxiomType<? extends OWLAxiom>>> entityType2AxiomTypes = 
			new HashMap<EntityType, Set<AxiomType<? extends OWLAxiom>>>();

	static {
		// class axiom types
		entityType2AxiomTypes.put(EntityType.CLASS, Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUBCLASS_OF, EQUIVALENT_CLASSES, DISJOINT_CLASSES));
		
		// object property axiom types
		entityType2AxiomTypes.put(EntityType.OBJECT_PROPERTY, Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUB_OBJECT_PROPERTY, EQUIVALENT_OBJECT_PROPERTIES, DISJOINT_OBJECT_PROPERTIES,
				SYMMETRIC_OBJECT_PROPERTY, ASYMMETRIC_OBJECT_PROPERTY,FUNCTIONAL_OBJECT_PROPERTY, 
				INVERSE_FUNCTIONAL_OBJECT_PROPERTY, REFLEXIVE_OBJECT_PROPERTY, IRREFLEXIVE_OBJECT_PROPERTY, TRANSITIVE_OBJECT_PROPERTY,
				OBJECT_PROPERTY_DOMAIN, OBJECT_PROPERTY_RANGE));
		
		// data property axiom types
		entityType2AxiomTypes.put(EntityType.CLASS, Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUB_DATA_PROPERTY, EQUIVALENT_DATA_PROPERTIES, DISJOINT_DATA_PROPERTIES,
				FUNCTIONAL_DATA_PROPERTY, DATA_PROPERTY_DOMAIN, DATA_PROPERTY_RANGE));
	}
	
	private static final Map<EntityType, Set<AxiomTypeCluster>> sameSampleCluster = 
			new HashMap<EntityType, Set<AxiomTypeCluster>>();
	
	static {
		// object properties
		sameSampleCluster.put(EntityType.OBJECT_PROPERTY,
				Sets.newHashSet(
						AxiomTypeCluster.OBJECT_PROPERTY_HIERARCHY_CLUSTER, 
						AxiomTypeCluster.OBJECT_PROPERTY_CHARACTERISTICS_WITHOUT_TRANSITIVITY_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_DOMAIN_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_RANGE_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_TRANSITITVITY_CLUSTER));
		
		// data properties
		sameSampleCluster.put(EntityType.DATA_PROPERTY,
				Sets.newHashSet(
						AxiomTypeCluster.DATA_PROPERTY_HIERARCHY_CLUSTER, 
						AxiomTypeCluster.DATA_PROPERTY_DOMAIN_CLUSTER,
						AxiomTypeCluster.DATA_PROPERTY_RANGE_CLUSTER,
						AxiomTypeCluster.DATA_PROPERTY_FUNCTIONALITY_CLUSTER));
		
		// classes
		sameSampleCluster.put(EntityType.CLASS,
				Sets.newHashSet(
						AxiomTypeCluster.OBJECT_PROPERTY_HIERARCHY_CLUSTER, 
						AxiomTypeCluster.OBJECT_PROPERTY_CHARACTERISTICS_WITHOUT_TRANSITIVITY_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_DOMAIN_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_RANGE_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_TRANSITITVITY_CLUSTER));
	}
	
	private static HashBiMap<AxiomType, Class<? extends AbstractAxiomLearningAlgorithm<? extends OWLAxiom, ? extends OWLObject, ? extends OWLEntity>>> axiomType2Class = 
			HashBiMap.create();
	
	static{
		axiomType2Class.put(AxiomType.SUBCLASS_OF, SimpleSubclassLearner.class);
//		axiomType2Class.put(AxiomType.EQUIVALENT_CLASSES, CELOE.class);
		axiomType2Class.put(AxiomType.DISJOINT_CLASSES, DisjointClassesLearner.class);
		axiomType2Class.put(AxiomType.SUB_OBJECT_PROPERTY, SubObjectPropertyOfAxiomLearner.class);
		axiomType2Class.put(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, EquivalentObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.DISJOINT_OBJECT_PROPERTIES, DisjointObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.OBJECT_PROPERTY_DOMAIN, ObjectPropertyDomainAxiomLearner2.class);
		axiomType2Class.put(AxiomType.OBJECT_PROPERTY_RANGE, ObjectPropertyRangeAxiomLearner.class);
		axiomType2Class.put(AxiomType.FUNCTIONAL_OBJECT_PROPERTY, FunctionalObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY, InverseFunctionalObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.REFLEXIVE_OBJECT_PROPERTY, ReflexiveObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.IRREFLEXIVE_OBJECT_PROPERTY, IrreflexiveObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.SYMMETRIC_OBJECT_PROPERTY, SymmetricObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.ASYMMETRIC_OBJECT_PROPERTY, AsymmetricObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.TRANSITIVE_OBJECT_PROPERTY, TransitiveObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.SUB_DATA_PROPERTY, SubDataPropertyOfAxiomLearner.class);
		axiomType2Class.put(AxiomType.EQUIVALENT_DATA_PROPERTIES, EquivalentDataPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.DISJOINT_DATA_PROPERTIES, DisjointDataPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.DATA_PROPERTY_DOMAIN, DataPropertyDomainAxiomLearner.class);
		axiomType2Class.put(AxiomType.DATA_PROPERTY_RANGE, DataPropertyRangeAxiomLearner.class);
		axiomType2Class.put(AxiomType.FUNCTIONAL_DATA_PROPERTY, FunctionalDataPropertyAxiomLearner.class);
	}
	
	public static Set<AxiomType<? extends OWLAxiom>> getAxiomTypes(EntityType entityType){
		return entityType2AxiomTypes.get(entityType);
	}
	
	public static Class<? extends AbstractAxiomLearningAlgorithm<? extends OWLAxiom, ? extends OWLObject, ? extends OWLEntity>> getAlgorithmClass(AxiomType<? extends OWLAxiom> axiomType){
		return axiomType2Class.get(axiomType);
	}
	
	public static Set<AxiomTypeCluster> getSameSampleClusters(EntityType entityType){
		return sameSampleCluster.get(entityType);
	}
}
