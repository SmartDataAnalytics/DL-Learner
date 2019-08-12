/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.algorithms.properties;

import static org.semanticweb.owlapi.model.AxiomType.ASYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.CLASS_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.DATATYPE_DEFINITION;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.DATA_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.DIFFERENT_INDIVIDUALS;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.DISJOINT_UNION;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_CLASSES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_DATA_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.EQUIVALENT_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.HAS_KEY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_FUNCTIONAL_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.INVERSE_OBJECT_PROPERTIES;
import static org.semanticweb.owlapi.model.AxiomType.IRREFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.NEGATIVE_DATA_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.NEGATIVE_OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_ASSERTION;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_DOMAIN;
import static org.semanticweb.owlapi.model.AxiomType.OBJECT_PROPERTY_RANGE;
import static org.semanticweb.owlapi.model.AxiomType.REFLEXIVE_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SAME_INDIVIDUAL;
import static org.semanticweb.owlapi.model.AxiomType.SUBCLASS_OF;
import static org.semanticweb.owlapi.model.AxiomType.SUB_DATA_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUB_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.SUB_PROPERTY_CHAIN_OF;
import static org.semanticweb.owlapi.model.AxiomType.SYMMETRIC_OBJECT_PROPERTY;
import static org.semanticweb.owlapi.model.AxiomType.TRANSITIVE_OBJECT_PROPERTY;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.dllearner.algorithms.DisjointClassesLearner;
import org.dllearner.algorithms.SimpleSubclassLearner;
import org.dllearner.core.AbstractAxiomLearningAlgorithm;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.EntityType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;

import com.google.common.collect.HashBiMap;
import com.google.common.collect.Sets;
import org.apache.jena.query.ParameterizedSparqlString;

/**
 * A utility class that holds links between entity type, axiom types and axiom algorithms.
 * @author Lorenz Buehmann
 *
 */
public class AxiomAlgorithms {
	
	 /** set of tbox axiom types */
    public static final Set<AxiomType<? extends OWLAxiom>> TBoxAxiomTypes = Sets.<AxiomType<? extends OWLAxiom>>newHashSet(
            Arrays.asList(SUBCLASS_OF, EQUIVALENT_CLASSES, DISJOINT_CLASSES,
                    OBJECT_PROPERTY_DOMAIN, OBJECT_PROPERTY_RANGE,
                    INVERSE_OBJECT_PROPERTIES, FUNCTIONAL_OBJECT_PROPERTY,
                    INVERSE_FUNCTIONAL_OBJECT_PROPERTY,
                    SYMMETRIC_OBJECT_PROPERTY, ASYMMETRIC_OBJECT_PROPERTY,
                    TRANSITIVE_OBJECT_PROPERTY, REFLEXIVE_OBJECT_PROPERTY,
                    IRREFLEXIVE_OBJECT_PROPERTY, DATA_PROPERTY_DOMAIN,
                    DATA_PROPERTY_RANGE, FUNCTIONAL_DATA_PROPERTY,
                    DATATYPE_DEFINITION, DISJOINT_UNION, HAS_KEY));
    
    /** set of abox axiom types */
    public static final Set<AxiomType<? extends OWLAxiom>> ABoxAxiomTypes = Sets.<AxiomType<? extends OWLAxiom>>newHashSet(
            Arrays.asList(CLASS_ASSERTION, SAME_INDIVIDUAL,
                    DIFFERENT_INDIVIDUALS, OBJECT_PROPERTY_ASSERTION,
                    NEGATIVE_OBJECT_PROPERTY_ASSERTION,
                    DATA_PROPERTY_ASSERTION, NEGATIVE_DATA_PROPERTY_ASSERTION));
    
    /** set of rbox axiom types */
    @SuppressWarnings("unchecked")
	public static final Set<AxiomType<? extends OWLAxiom>> RBoxAxiomTypes = Sets.<AxiomType<? extends OWLAxiom>>newHashSet(
    				TRANSITIVE_OBJECT_PROPERTY, DISJOINT_DATA_PROPERTIES,
                    SUB_DATA_PROPERTY, EQUIVALENT_DATA_PROPERTIES,
                    DISJOINT_OBJECT_PROPERTIES, SUB_OBJECT_PROPERTY,
                    EQUIVALENT_OBJECT_PROPERTIES, SUB_PROPERTY_CHAIN_OF);
    
    /** set of tbox and rbox axiom types */
    public static final Set<AxiomType<? extends OWLAxiom>> TBoxAndRBoxAxiomTypes = tboxAndRbox();

    private static Set<AxiomType<? extends OWLAxiom>> tboxAndRbox() {
        Set<AxiomType<?>> axioms = new HashSet<>(TBoxAxiomTypes);
        axioms.addAll(RBoxAxiomTypes);
        return axioms;
    }
	
	static class AxiomTypeCluster {
		private final Set<AxiomType<? extends OWLAxiom>> axiomTypes;
		private final ParameterizedSparqlString sampleQuery;
		
		public AxiomTypeCluster(Set<AxiomType<? extends OWLAxiom>> axiomTypes, ParameterizedSparqlString sampleQuery) {
			this.axiomTypes = axiomTypes;
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
		
		@Override
		public String toString() {
			return axiomTypes.toString();
		}
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_HIERARCHY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUB_OBJECT_PROPERTY, EQUIVALENT_OBJECT_PROPERTIES, DISJOINT_OBJECT_PROPERTIES),
				new ParameterizedSparqlString("PREFIX owl:<http://www.w3.org/2002/07/owl#> CONSTRUCT {?s ?entity ?o . ?s ?p1 ?o . ?p1 a owl:ObjectProperty .} "
						+ "WHERE {?s ?entity ?o . OPTIONAL{?s ?p1 ?o . ?p1 a owl:ObjectProperty . FILTER(?entity !=?p1)} }"));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_CHARACTERISTICS_WITHOUT_TRANSITIVITY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SYMMETRIC_OBJECT_PROPERTY, ASYMMETRIC_OBJECT_PROPERTY,
						FUNCTIONAL_OBJECT_PROPERTY, INVERSE_FUNCTIONAL_OBJECT_PROPERTY, REFLEXIVE_OBJECT_PROPERTY, IRREFLEXIVE_OBJECT_PROPERTY),
				new ParameterizedSparqlString("CONSTRUCT {?s ?entity ?o.} WHERE {?s ?entity ?o}"));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_TRANSITIVITY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(TRANSITIVE_OBJECT_PROPERTY),
				new ParameterizedSparqlString("CONSTRUCT {?s ?entity ?o . ?o ?entity ?o1 . ?s ?entity ?o1 .} "
						+ "WHERE {?s ?entity ?o . OPTIONAL {?o ?entity ?o1 . ?s ?entity ?o1 .}}"));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_DOMAIN_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(OBJECT_PROPERTY_DOMAIN),
				new ParameterizedSparqlString("PREFIX owl:<http://www.w3.org/2002/07/owl#> CONSTRUCT {?s ?entity ?o; a ?cls . ?cls a owl:Class .} "
						+ "WHERE {?s ?entity ?o . OPTIONAL {?s a ?cls . ?cls a owl:Class .}}"));
		
		public static final AxiomTypeCluster OBJECT_PROPERTY_RANGE_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(OBJECT_PROPERTY_RANGE),
				new ParameterizedSparqlString("PREFIX owl:<http://www.w3.org/2002/07/owl#> CONSTRUCT {?s ?entity ?o . ?o a ?cls . ?cls a owl:Class .} "
						+ "WHERE {?s ?entity ?o . OPTIONAL {?o a ?cls . ?cls a owl:Class .}}"));
		
		public static final AxiomTypeCluster INVERSE_OBJECT_PROPERTIES_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(INVERSE_OBJECT_PROPERTIES),
				new ParameterizedSparqlString("CONSTRUCT {?s ?entity ?o . ?o ?p_inv ?s . } "
						+ "WHERE {?s ?entity ?o . OPTIONAL {?o ?p_inv ?s . }}"));
		
		public static final AxiomTypeCluster DATA_PROPERTY_HIERARCHY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUB_DATA_PROPERTY, EQUIVALENT_DATA_PROPERTIES, DISJOINT_DATA_PROPERTIES),
				new ParameterizedSparqlString("CONSTRUCT {?s ?entity ?o . ?s ?p1 ?o . ?p1 a <http://www.w3.org/2002/07/owl#DatatypeProperty> .} "
						+ "WHERE {?s ?entity ?o . OPTIONAL{?s ?p1 ?o . FILTER(!sameTerm(?entity, ?p1))} }"));
		
		public static final AxiomTypeCluster DATA_PROPERTY_RANGE_AND_FUNCTIONALITY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(DATA_PROPERTY_RANGE, FUNCTIONAL_DATA_PROPERTY),
				new ParameterizedSparqlString("CONSTRUCT {?s ?entity ?o.} WHERE {?s ?entity ?o}"));
		
		public static final AxiomTypeCluster DATA_PROPERTY_DOMAIN_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(DATA_PROPERTY_DOMAIN),
				new ParameterizedSparqlString("PREFIX owl:<http://www.w3.org/2002/07/owl#> CONSTRUCT {?s ?entity ?o; a ?cls . ?cls a owl:Class .} "
						+ "WHERE {?s ?entity ?o . OPTIONAL {?s a ?cls . ?cls a owl:Class .}}"));
		
		public static final AxiomTypeCluster CLASS_HIERARCHY_CLUSTER = new AxiomTypeCluster(
				Sets.<AxiomType<? extends OWLAxiom>>newHashSet(SUBCLASS_OF, EQUIVALENT_CLASSES, DISJOINT_CLASSES),
				new ParameterizedSparqlString("CONSTRUCT{?s a ?entity . ?s a ?cls1 .} WHERE {?s a ?entity . OPTIONAL {?s a ?cls1 .}"));
	}

	private static final Map<EntityType<? extends OWLEntity>, Set<AxiomType<? extends OWLAxiom>>> entityType2AxiomTypes =
			new HashMap<>();

	static {
		// class axiom types
		entityType2AxiomTypes.put(EntityType.CLASS, Sets.<AxiomType<? extends OWLAxiom>>newHashSet(
				SUBCLASS_OF, EQUIVALENT_CLASSES, DISJOINT_CLASSES));
		
		// object property axiom types
		entityType2AxiomTypes.put(EntityType.OBJECT_PROPERTY, Sets.<AxiomType<? extends OWLAxiom>>newHashSet(
				SUB_OBJECT_PROPERTY, EQUIVALENT_OBJECT_PROPERTIES, DISJOINT_OBJECT_PROPERTIES,
				SYMMETRIC_OBJECT_PROPERTY, ASYMMETRIC_OBJECT_PROPERTY,FUNCTIONAL_OBJECT_PROPERTY, 
				INVERSE_FUNCTIONAL_OBJECT_PROPERTY, REFLEXIVE_OBJECT_PROPERTY, IRREFLEXIVE_OBJECT_PROPERTY, TRANSITIVE_OBJECT_PROPERTY,
				OBJECT_PROPERTY_DOMAIN, OBJECT_PROPERTY_RANGE,
				INVERSE_OBJECT_PROPERTIES));
		
		// data property axiom types
		entityType2AxiomTypes.put(EntityType.DATA_PROPERTY, Sets.<AxiomType<? extends OWLAxiom>>newHashSet(
				SUB_DATA_PROPERTY, EQUIVALENT_DATA_PROPERTIES, DISJOINT_DATA_PROPERTIES,
				FUNCTIONAL_DATA_PROPERTY, DATA_PROPERTY_DOMAIN, DATA_PROPERTY_RANGE));
	}
	
	private static final Map<EntityType, Set<AxiomTypeCluster>> sameSampleCluster =
			new HashMap<>();
	
	static {
		// object properties
		sameSampleCluster.put(EntityType.OBJECT_PROPERTY,
				Sets.newHashSet(
						AxiomTypeCluster.OBJECT_PROPERTY_HIERARCHY_CLUSTER, 
						AxiomTypeCluster.OBJECT_PROPERTY_CHARACTERISTICS_WITHOUT_TRANSITIVITY_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_DOMAIN_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_RANGE_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_TRANSITIVITY_CLUSTER,
						AxiomTypeCluster.INVERSE_OBJECT_PROPERTIES_CLUSTER));
		
		// data properties
		sameSampleCluster.put(EntityType.DATA_PROPERTY,
				Sets.newHashSet(
						AxiomTypeCluster.DATA_PROPERTY_HIERARCHY_CLUSTER, 
						AxiomTypeCluster.DATA_PROPERTY_DOMAIN_CLUSTER,
						AxiomTypeCluster.DATA_PROPERTY_RANGE_AND_FUNCTIONALITY_CLUSTER));
		
		// classes
		sameSampleCluster.put(EntityType.CLASS,
				Sets.newHashSet(
						AxiomTypeCluster.OBJECT_PROPERTY_HIERARCHY_CLUSTER, 
						AxiomTypeCluster.OBJECT_PROPERTY_CHARACTERISTICS_WITHOUT_TRANSITIVITY_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_DOMAIN_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_RANGE_CLUSTER,
						AxiomTypeCluster.OBJECT_PROPERTY_TRANSITIVITY_CLUSTER));
	}
	
	private static HashBiMap<AxiomType<? extends OWLAxiom>, Class<? extends AbstractAxiomLearningAlgorithm<? extends OWLAxiom, ? extends OWLObject, ? extends OWLEntity>>> axiomType2Class = 
			HashBiMap.create();
	
	static{
		axiomType2Class.put(AxiomType.SUBCLASS_OF, SimpleSubclassLearner.class);
//		axiomType2Class.put(AxiomType.EQUIVALENT_CLASSES, CELOE.class);
		axiomType2Class.put(AxiomType.DISJOINT_CLASSES, DisjointClassesLearner.class);
		axiomType2Class.put(AxiomType.SUB_OBJECT_PROPERTY, SubObjectPropertyOfAxiomLearner.class);
		axiomType2Class.put(AxiomType.EQUIVALENT_OBJECT_PROPERTIES, EquivalentObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.DISJOINT_OBJECT_PROPERTIES, DisjointObjectPropertyAxiomLearner.class);
		axiomType2Class.put(AxiomType.OBJECT_PROPERTY_DOMAIN, ObjectPropertyDomainAxiomLearner.class);
		axiomType2Class.put(AxiomType.OBJECT_PROPERTY_RANGE, ObjectPropertyRangeAxiomLearner.class);
		axiomType2Class.put(AxiomType.INVERSE_OBJECT_PROPERTIES, InverseObjectPropertyAxiomLearner.class);
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
	
	public static <E extends OWLEntity> Set<AxiomType<? extends OWLAxiom>> getAxiomTypes(EntityType<E> entityType){
		return entityType2AxiomTypes.get(entityType);
	}
	
	public static <E extends OWLEntity> Class<? extends AbstractAxiomLearningAlgorithm<? extends OWLAxiom, ? extends OWLObject, ? extends OWLEntity>> getAlgorithmClass(AxiomType<? extends OWLAxiom> axiomType){
		return axiomType2Class.get(axiomType);
	}
	
	public static Set<AxiomTypeCluster> getSameSampleClusters(EntityType<? extends OWLEntity> entityType){
		return sameSampleCluster.get(entityType);
	}
}
