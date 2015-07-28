/**
 * Copyright (C) 2007-2011, Jens Lehmann
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

package org.dllearner.reasoning;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLEquivalentClassesAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyChangeException;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyFormat;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.BufferingMode;
import org.semanticweb.owlapi.reasoner.FreshEntityPolicy;
import org.semanticweb.owlapi.reasoner.IndividualNodeSetPolicy;
import org.semanticweb.owlapi.reasoner.InferenceType;
import org.semanticweb.owlapi.reasoner.Node;
import org.semanticweb.owlapi.reasoner.NodeSet;
import org.semanticweb.owlapi.reasoner.NullReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;
import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;
import org.semanticweb.owlapi.reasoner.SimpleConfiguration;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat;
import org.semanticweb.owlapi.vocab.XSDVocabulary;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.owl.owlapi.OWL2DatatypeImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLDatatypeImpl;
import uk.ac.manchester.cs.owl.owlapi.alternateimpls.ThreadSafeOWLReasoner;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapiv3.XSD;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;

import de.tudresden.inf.lat.cel.owlapi.CelReasoner;
import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;

/**
 * Mapping to OWL API reasoner interface. The OWL API currently
 * supports three reasoners: FaCT++, HermiT, Pellet, ELK, CEL and TrOWL. FaCT++ is connected
 * using JNI and native libraries, while the others are pure Java
 * libraries.
 *
 * @author Jens Lehmann
 */
@ComponentAnn(name = "OWL API Reasoner", shortName = "oar", version = 0.8)
public class OWLAPIReasoner extends AbstractReasonerComponent {

    private OWLReasoner reasoner;
    private OWLOntologyManager manager;

    private OWLOntology ontology;
    // the data factory is used to generate OWL API objects
    private OWLDataFactory df;

    // primitives
    Set<OWLClass> atomicConcepts = new TreeSet<OWLClass>();
    Set<OWLObjectProperty> atomicRoles = new TreeSet<OWLObjectProperty>();
    SortedSet<OWLDataProperty> datatypeProperties = new TreeSet<OWLDataProperty>();
    SortedSet<OWLIndividual> individuals = new TreeSet<OWLIndividual>();

    // namespaces
    private Map<String, String> prefixes = new TreeMap<String, String>();
    private String baseURI;

    // references to OWL API ontologies
    private Set<OWLOntology> owlAPIOntologies = new HashSet<OWLOntology>();


    private OWLClassExpressionMinimizer minimizer;

    private OWLReasoner fallbackReasoner;
    
    
 // default reasoner is Pellet
    @ConfigOption(name = "reasonerImplementation", defaultValue="pellet", description="specifies the used OWL API reasoner implementation")
    private ReasonerImplementation reasonerImplementation = ReasonerImplementation.PELLET;

    @ConfigOption(name = "useFallbackReasoner", defaultValue="false", description="specifies whether to use a fallback reasoner if a reasoner call fails because it's not supported or results in a bug. (the fallback works only on the assertional level")
    private boolean useFallbackReasoner = false;

    @ConfigOption(name = "owlLinkURL", defaultValue="null", description="specifies the URL of the remote OWLLink server")
    private String owlLinkURL;

    public OWLAPIReasoner() {

    }

    public OWLAPIReasoner(KnowledgeSource... sources) {
        super(new HashSet<KnowledgeSource>(Arrays.asList(sources)));
    }

    public OWLAPIReasoner(Set<KnowledgeSource> sources) {
        super(sources);
    }

    public OWLAPIReasoner(OWLReasoner reasoner) {
        this.reasoner = reasoner;
        KnowledgeSource ks = new OWLAPIOntology(reasoner.getRootOntology());
        sources = Collections.singleton(ks);
    }

    public static String getName() {
        return "OWL API reasoner";
    }

    @Override
    public void init() throws ComponentInitException {
        // reset variables (otherwise subsequent initialisation with
        // different knowledge sources will merge both)
        atomicConcepts = new TreeSet<OWLClass>();
        atomicRoles = new TreeSet<OWLObjectProperty>();
        datatypeProperties = new TreeSet<OWLDataProperty>();
        individuals = new TreeSet<OWLIndividual>();

        // create OWL API ontology manager - make sure we use a new data factory so that we don't default to the static one which can cause problems in a multi threaded environment.
        df = new OWLDataFactoryImpl();
        manager = OWLManager.createOWLOntologyManager();

        prefixes = new TreeMap<String, String>();

        for (KnowledgeSource source : sources) {
            if (source instanceof OWLOntologyKnowledgeSource) {
                ontology = ((OWLOntologyKnowledgeSource) source).createOWLOntology(manager);
                owlAPIOntologies.add(ontology);
            }else{
                //This reasoner requires an ontology to process
                throw new ComponentInitException("OWL API Reasoner requires an OWLKnowledgeSource.  Received a KS of type: " + source.getClass().getName());
            }

            atomicConcepts.addAll(ontology.getClassesInSignature(true));
            atomicRoles.addAll(ontology.getObjectPropertiesInSignature(true));
            datatypeProperties.addAll(ontology.getDataPropertiesInSignature(true));
            individuals.addAll(ontology.getIndividualsInSignature(true));

            // if several knowledge sources are included, then we can only
            // guarantee that the base URI is from one of those sources (there
            // can't be more than one); but we will take care that all prefixes are
            // correctly imported
            OWLOntologyFormat format = manager.getOntologyFormat(ontology);
            if (format instanceof PrefixOWLOntologyFormat) {
                prefixes.putAll(((PrefixOWLOntologyFormat) format).getPrefixName2PrefixMap());
                baseURI = ((PrefixOWLOntologyFormat) format).getDefaultPrefix();
                prefixes.remove("");
            }
        }

        //Now merge all of the knowledge sources into one ontology instance.
        try {
            //The following line illustrates a problem with using different OWLOntologyManagers.  This can manifest itself if we have multiple sources who were created with different manager instances.
            //ontology = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://dl-learner/all"), new HashSet<OWLOntology>(owlAPIOntologies));
            ontology = manager.createOntology(IRI.create("http://dl-learner/all"), new HashSet<OWLOntology>(owlAPIOntologies));
            //we have to add all import declarations manually here, because these are not OWL axioms
            List<OWLOntologyChange> addImports = new ArrayList<OWLOntologyChange>();
            for (OWLOntology ont : owlAPIOntologies) {
            	for (OWLImportsDeclaration importDeclaration : ont.getImportsDeclarations()) {
            		addImports.add(new AddImport(ontology, importDeclaration));
				}
            }
            manager.applyChanges(addImports);
        } catch (OWLOntologyCreationException e1) {
            e1.printStackTrace();
        }

        //set up OWL reasoner
        if(reasoner == null) {
        	initBaseReasoner();
        }

        // compute class hierarchy and types of individuals
        // (done here to speed up later reasoner calls)
        boolean inconsistentOntology = !reasoner.isConsistent();

        if (!inconsistentOntology) {
            reasoner.precomputeInferences(
            		InferenceType.CLASS_HIERARCHY, 
            		InferenceType.CLASS_ASSERTIONS, 
            		InferenceType.OBJECT_PROPERTY_HIERARCHY,
            		InferenceType.DATA_PROPERTY_HIERARCHY,
            		InferenceType.OBJECT_PROPERTY_ASSERTIONS,
            		InferenceType.SAME_INDIVIDUAL);
        } else {
        	PelletExplanation expGen = new PelletExplanation(ontology);
        	System.out.println(expGen.getInconsistencyExplanation());
        	reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            throw new ComponentInitException("Inconsistent ontologies.");
        }

        df = manager.getOWLDataFactory();

        initDatatypes();

        // remove top and bottom properties (for backwards compatibility)
//		atomicRoles.remove(df.getOWLObjectProperty(IRI.create("http://www.w3.org/2002/07/owl#bottomObjectProperty"));
//		atomicRoles.remove(df.getOWLObjectProperty(IRI.create("http://www.w3.org/2002/07/owl#topObjectProperty"));


        // remove classes that are built-in entities
		Iterator<OWLClass> it = atomicConcepts.iterator();
		while (it.hasNext()) {
			OWLClass cls = it.next();
			if(cls.getIRI().isReservedVocabulary()){
				it.remove();
			}
		}

		 minimizer = new OWLClassExpressionMinimizer(df, this);
		 logger.info("Loaded reasoner: " + reasoner.getReasonerName() + " (" + reasoner.getClass().getName() + ")");
    }
    
    private void initDatatypes() {
    	Set<OWLDataProperty> numericDataProperties = new HashSet<OWLDataProperty>();
        for (OWLDataProperty dataProperty : datatypeProperties) {
            Collection<OWLDataRange> ranges = dataProperty.getRanges(owlAPIOntologies);
			Iterator<OWLDataRange> it = ranges.iterator();
			if (it.hasNext()) {
				OWLDataRange range = it.next();
				if (range.isDatatype()) {
					OWLDatatype datatype = range.asOWLDatatype();
					
					if(datatype.isBuiltIn()) { // OWL 2 DL compliant datatypes
						datatype2Properties.put(range.asOWLDatatype(), dataProperty);

						dataproperty2datatype.put(dataProperty, range.asOWLDatatype());
						
						if(OWLAPIUtils.isNumericDatatype(range.asOWLDatatype())) {
							numericDataProperties.add(dataProperty);
						}
					} else if(OWLAPIUtils.dtDatatypes.contains(datatype)) { // support for other XSD datatypes, e.g. xsd:date
						datatype2Properties.put(range.asOWLDatatype(), dataProperty);

						dataproperty2datatype.put(dataProperty, range.asOWLDatatype());
					} else {
						datatype2Properties.put(XSD.STRING, dataProperty);
						dataproperty2datatype.put(dataProperty, XSD.STRING);
					}
				} else { // TODO handle complex data property ranges
					
				}
			} 
        }
    }

    private void initBaseReasoner() {
    	ReasonerProgressMonitor progressMonitor = new NullReasonerProgressMonitor();
        FreshEntityPolicy freshEntityPolicy = FreshEntityPolicy.ALLOW;
        long timeOut = Integer.MAX_VALUE;
        IndividualNodeSetPolicy individualNodeSetPolicy = IndividualNodeSetPolicy.BY_NAME;
        OWLReasonerConfiguration conf = new SimpleConfiguration(progressMonitor, freshEntityPolicy, timeOut, individualNodeSetPolicy);

        OWLReasonerFactory reasonerFactory = null;
        // create actual reasoner
		switch (reasonerImplementation) {
		case PELLET:
			reasonerFactory = PelletReasonerFactory.getInstance();
			// change log level to WARN for Pellet, because otherwise log
			// output will be very large
			Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
			pelletLogger.setLevel(Level.WARN);
			break;
		case JFACT:
			reasonerFactory = new JFactFactory();
			break;
		case FACT:
			reasonerFactory = new FaCTPlusPlusReasonerFactory();
			break;
		case ELK:
			reasonerFactory = new ElkReasonerFactory();
			break;
		case HERMIT:
			reasonerFactory = new ReasonerFactory();
			break;
		case TROWL:
			reasonerFactory = new RELReasonerFactory();
			break;
		case CEL:
			reasoner = new CelReasoner(ontology, conf);
			break;
		case OWLLINK:
			reasonerFactory = new OWLlinkHTTPXMLReasonerFactory();
			URL url = null;
			try {
				url = new URL(getOwlLinkURL());//Configure the server end-point
				conf = new OWLlinkReasonerConfiguration(url);
			} catch (MalformedURLException e) {
				logger.error("Illegal URL <" + url + "> for OWL Link HTTP reasoner", e);
			}
		default:
			reasonerFactory = PelletReasonerFactory.getInstance();
		}

		if (null != reasonerFactory) {
			reasoner = reasonerFactory.createNonBufferingReasoner(ontology, conf);
		}

        if(useFallbackReasoner){
        	fallbackReasoner = new StructuralReasonerExtended(ontology, conf, BufferingMode.NON_BUFFERING);
        }
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getAtomicConcepts()
      */
    @Override
    public Set<OWLClass> getClasses() {
        return Collections.unmodifiableSet(atomicConcepts);
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getAtomicRoles()
      */
    @Override
    public Set<OWLObjectProperty> getObjectPropertiesImpl() {
        return Collections.unmodifiableSet(atomicRoles);
    }

    @Override
    public Set<OWLDataProperty> getDatatypePropertiesImpl() {
        return datatypeProperties;
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getIndividuals()
      */
    @Override
    public SortedSet<OWLIndividual> getIndividuals() {
        return individuals;
    }

    /**
	 * @param prefixes the prefixes to set
	 */
	public void setPrefixes(Map<String, String> prefixes) {
		this.prefixes = prefixes;
	}

	/**
	 * @param baseURI the baseURI to set
	 */
	public void setBaseURI(String baseURI) {
		this.baseURI = baseURI;
	}

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getReasonerType()
      */
    @Override
    public ReasonerType getReasonerType() {
    	if (reasoner instanceof org.semanticweb.HermiT.Reasoner) {
    		return ReasonerType.OWLAPI_HERMIT;
    	}
    	else if (reasoner instanceof com.clarkparsia.pellet.owlapiv3.PelletReasoner) {
    		return ReasonerType.OWLAPI_PELLET;
    	}
    	else if (reasoner instanceof uk.ac.manchester.cs.jfact.JFactReasoner) {
    		return ReasonerType.OWLAPI_JFACT;
    	}
    	else if (reasoner instanceof uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasoner) {
    		return ReasonerType.OWLAPI_FACT;
    	}
    	return ReasonerType.OWLAPI_FUZZY; // TODO
    }

	@Override
	public boolean isSuperClassOfImpl(OWLClassExpression superConcept,
			OWLClassExpression subConcept) {
		if (superConcept.isOWLThing() || subConcept.isOWLNothing()) {
			return true;
		}
		boolean res;
		try {
			res = reasoner.isEntailed(df.getOWLSubClassOfAxiom(subConcept,
					superConcept));
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				res = fallbackReasoner.isEntailed(df.getOWLSubClassOfAxiom(
						subConcept, superConcept));
			} else {
				throw e;
			}
		}
		return res;
	}

    /* (non-Javadoc)
     * @see org.dllearner.core.Reasoner#isDisjoint(OWLClass class1, OWLClass class2)
     */
	@Override
	public boolean isDisjointImpl(OWLClass clsA, OWLClass clsB) {
		// we have two ways, not sure which one is more efficient
		// 1. get all disjoint classes and check for set containment (could be fast if taxonomy
		// is cached somewhere in the reasoner internals)
//		return reasoner.getDisjointClasses(clsA).containsEntity(clsB);

		// 2. check for entailment of DisjointClass(A, B) resp.
		// SubClassOf(OWLIntersectionOf(A, B), owl:Nothing)
//		OWLAxiom axiom = df.getOWLDisjointClassesAxiom(clsA, clsB);
		OWLAxiom axiom = df.getOWLSubClassOfAxiom(
				df.getOWLObjectIntersectionOf(clsA, clsB), df.getOWLNothing());

		boolean res;
		try {
			res = reasoner.isEntailed(axiom);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				res = fallbackReasoner.isEntailed(axiom);
			} else {
				throw e;
			}
		}
		return res;
	}

	@Override
	protected boolean isEquivalentClassImpl(OWLClassExpression class1, OWLClassExpression class2) {
		OWLEquivalentClassesAxiom axiom = df.getOWLEquivalentClassesAxiom(class1, class2);
		boolean res;
		try {
			res = reasoner.isEntailed(axiom);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				res = fallbackReasoner.isEntailed(axiom);
			} else {
				throw e;
			}
		}
		return res;
	}

	@Override
	protected TreeSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression concept) {
		NodeSet<OWLClass> classes;
		try {
			classes = reasoner.getSuperClasses(concept, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				classes = fallbackReasoner.getSubClasses(concept, true);
			} else {
				throw e;
			}
		}
		return getFirstClasses(classes);
	}

	@Override
	protected TreeSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression concept) {
		NodeSet<OWLClass> classes;

		try {
			classes = reasoner.getSubClasses(concept, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				classes = fallbackReasoner.getSubClasses(concept, true);
			} else {
				throw e;
			}
		}
		TreeSet<OWLClassExpression> subClasses = getFirstClasses(classes);
		subClasses.remove(df.getOWLNothing());
		// remove built-in entities sometimes returned as subclasses of
		// owl:Thing
		if (concept.isOWLThing()) {
			Iterator<OWLClassExpression> it = subClasses.iterator();
			while (it.hasNext()) {
				OWLClassExpression ce = it.next();
				if (!ce.isAnonymous() &&
						ce.asOWLClass().getIRI().isReservedVocabulary()) {
					it.remove();
				}
			}
		}
		return subClasses;
	}

    private <T extends OWLObject> SortedSet<T> getRepresentativeEntities(NodeSet<T> nodeSet){
    	SortedSet<T> representatives = new TreeSet<T>();
    	for (Node<T> node : nodeSet) {
			if(!node.isBottomNode() && !node.isTopNode()){
				representatives.add(node.getRepresentativeElement());
			}
		}
    	return representatives;
    }

	protected SortedSet<OWLClassExpression> getEquivalentClassesImpl(OWLClassExpression concept) {
		SortedSet<OWLClassExpression> equivalentclasses = new TreeSet<>();
		Node<OWLClass> classNodes;
		try {
			classNodes = reasoner.getEquivalentClasses(concept);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				classNodes = fallbackReasoner.getEquivalentClasses(concept);
			} else {
				throw e;
			}
		}

		for (OWLClass eqCls : classNodes.getEntitiesMinusTop()) {
			equivalentclasses.add(eqCls);
		}
		equivalentclasses.remove(concept);
		return equivalentclasses;
	}

	@Override
	protected TreeSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty objectProperty) {
		NodeSet<OWLObjectPropertyExpression> properties;
		try {
			properties = reasoner
					.getSuperObjectProperties(objectProperty, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				properties = fallbackReasoner.getSubObjectProperties(
						objectProperty, true);
			} else {
				throw e;
			}
		}
		return getFirstObjectProperties(properties);
	}

	@Override
	protected TreeSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty objectProperty) {
		NodeSet<OWLObjectPropertyExpression> properties;

		try {
			properties = reasoner.getSubObjectProperties(objectProperty, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				properties = fallbackReasoner.getSubObjectProperties(
						objectProperty, true);
			} else {
				throw e;
			}
		}
		return getFirstObjectProperties(properties);
	}

	@Override
	protected TreeSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty dataProperty) {
		NodeSet<OWLDataProperty> properties;

		try {
			properties = reasoner.getSuperDataProperties(dataProperty, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				properties = fallbackReasoner.getSuperDataProperties(dataProperty, true);
			} else {
				throw e;
			}
		}
		return getFirstDatatypeProperties(properties);
	}

	@Override
	protected TreeSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty dataProperty) {
		NodeSet<OWLDataProperty> properties;

		try {
			properties = reasoner.getSubDataProperties(dataProperty, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				properties = fallbackReasoner.getSubDataProperties(dataProperty, true);
			} else {
				throw e;
			}
		}
		return getFirstDatatypeProperties(properties);
	}

	@Override
	public boolean hasTypeImpl(OWLClassExpression concept, OWLIndividual individual) {
		if (concept.isOWLThing()) {
			return true;

		} else if (concept.isOWLNothing()) {
			return false;

		} else {
			OWLClassAssertionAxiom axiom = df.getOWLClassAssertionAxiom(
					concept, individual);
			boolean res;
			try {
				res = reasoner.isEntailed(axiom);
			} catch (UnsupportedOperationException e) {
				if (useFallbackReasoner) {
					res = fallbackReasoner.isEntailed(axiom);
				} else {
					throw e;
				}
			}

			return res;
		}
	}

	@Override
	public SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression ce) {
		Set<OWLNamedIndividual> individuals;
		try {
			individuals = reasoner.getInstances(ce, false).getFlattened();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				individuals = fallbackReasoner.getInstances(ce, false).getFlattened();
			} else {
				throw e;
			}
		}

		SortedSet<OWLIndividual> inds = new TreeSet<OWLIndividual>();
		for (OWLNamedIndividual ind : individuals) {
			inds.add(ind);
		}
		return inds;
	}

	@Override
	public Set<OWLClass> getTypesImpl(OWLIndividual individual) {
		NodeSet<OWLClass> nodeSet;
		try {
			nodeSet = reasoner.getTypes(individual.asOWLNamedIndividual(), false);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				nodeSet = fallbackReasoner.getTypes(individual.asOWLNamedIndividual(), false);
			} else {
				throw e;
			}
		}
		return getFirstClassesNoTopBottom(nodeSet);
	}

	@Override
	public boolean isSatisfiableImpl() {
		boolean res;
		try {
			res = reasoner.isSatisfiable(df.getOWLThing());
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				res = fallbackReasoner.isSatisfiable(df.getOWLThing());
			} else {
				throw e;
			}
		}
		return res;
	}

	@Override
	public OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
		// this is a bit tricky because the reasoner interface only returns
		// atomic classes, but it might be the case that in the ontology complex
		// domain definitions are contained

		Set<OWLClassExpression> domains = new HashSet<OWLClassExpression>();

		// get all asserted domains
		domains.addAll(objectProperty.getDomains(ontology));

		// do the same for all super properties
		NodeSet<OWLObjectPropertyExpression> superProperties;
		try {
			superProperties = reasoner.getSuperObjectProperties(objectProperty,
					false);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				superProperties = fallbackReasoner.getSuperObjectProperties(
						objectProperty, false);
			} else {
				throw e;
			}
		}
		for (OWLObjectPropertyExpression supProp : superProperties.getFlattened()) {
			domains.addAll(supProp.getDomains(ontology));
		}

		// last but not least, call a reasoner
		NodeSet<OWLClass> nodeSet;
		try {
			nodeSet = reasoner.getObjectPropertyDomains(objectProperty, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				nodeSet = fallbackReasoner.getObjectPropertyDomains(objectProperty, true);
			} else {
				throw e;
			}
		}

		domains.addAll(nodeSet.getFlattened());

		domains.remove(df.getOWLThing());

		OWLClassExpression domain;

		// several domains have to be treated as intersection
		if (domains.size() > 1) {
			domain = df.getOWLObjectIntersectionOf(domains);

			// simplify expression, e.g. keep the most specific class in
			// expressions
			// like A AND B
			domain = minimizer.minimize(domain);
		} else if (domains.size() == 1) {
			domain = domains.iterator().next();
		} else {
			domain = df.getOWLThing();
		}

		logger.trace("Domain({},{})", objectProperty, domain);
		return domain;
	}

//    @Override
//    public OWLClassExpression getDomainImpl(OWLDataProperty objectProperty) {
//    	return asIntersection(reasoner.getDataPropertyDomains(objectProperty, true));
//    }

	@Override
	public OWLClassExpression getDomainImpl(OWLDataProperty dataProperty) {
		// this is a bit tricky because the reasoner interface only returns
		// atomic classes, but it might be the case that in the ontology complex
		// domain definitions are contained

		Set<OWLClassExpression> domains = new HashSet<OWLClassExpression>();

		// get all asserted domains
		domains.addAll(dataProperty.getDomains(ontology));

		// do the same for all super properties
		NodeSet<OWLDataProperty> superProperties;
		try {
			superProperties = reasoner.getSuperDataProperties(dataProperty, false);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				superProperties = fallbackReasoner.getSuperDataProperties(dataProperty, false);
			} else {
				throw e;
			}
		}

		for (OWLDataProperty supProp : superProperties.getFlattened()) {
			domains.addAll(supProp.getDomains(ontology));
		}

		// last but not least, call a reasoner
		NodeSet<OWLClass> nodeSet;

		try {
			nodeSet = reasoner.getDataPropertyDomains(dataProperty, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				nodeSet = fallbackReasoner.getDataPropertyDomains(dataProperty, true);
			} else {
				throw e;
			}
		}
		domains.addAll(nodeSet.getFlattened());

		domains.remove(df.getOWLThing());

		OWLClassExpression domain;

		// several domains have to be treated as intersection
		if (domains.size() > 1) {
			domain = df.getOWLObjectIntersectionOf(domains);

			// simplify expression, e.g. keep the most specific class in
			// expressions
			// like A AND B
			domain = minimizer.minimize(domain);
		} else if (domains.size() == 1) {
			domain = domains.iterator().next();
		} else {
			domain = df.getOWLThing();
		}

		logger.trace("Domain({},{})", dataProperty, domain);
		return domain;
	}

//    @Override
//    public OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
//    	return asIntersection(reasoner.getObjectPropertyRanges(objectProperty, true));
//    }

	@Override
	public OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
		// this is a little bit tricky because the reasoner interface only
		// returns
		// atomic classes, but it might be the case that in the ontology complex
		// range definitions are contained

		Set<OWLClassExpression> ranges = new HashSet<OWLClassExpression>();

		// get all asserted ranges
		ranges.addAll(objectProperty.getRanges(ontology));

		// do the same for all super properties
		NodeSet<OWLObjectPropertyExpression> superProperties;
		try {
			superProperties = reasoner.getSuperObjectProperties(objectProperty, false);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				superProperties = fallbackReasoner.getSuperObjectProperties(
						objectProperty, false);
			} else {
				throw e;
			}
		}

		for (OWLObjectPropertyExpression supProp : superProperties
				.getFlattened()) {
			ranges.addAll(supProp.getRanges(ontology));
		}

		// last but not least, call a reasoner
		NodeSet<OWLClass> nodeSet;
		try {
			nodeSet = reasoner.getObjectPropertyRanges(objectProperty, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				nodeSet = fallbackReasoner.getObjectPropertyRanges(objectProperty, true);
			} else {
				throw e;
			}
		}
		ranges.addAll(nodeSet.getFlattened());

		OWLClassExpression range;

		// several domains have to be treated as intersection
		if (ranges.size() > 1) {
			range = df.getOWLObjectIntersectionOf(ranges);

			// simplify expression, e.g. keep the most specific class in
			// expressions
			// like A AND B
			range = minimizer.minimize(range);
		} else if (!ranges.isEmpty()) {
			range = ranges.iterator().next();
		} else {
			range = df.getOWLThing();
		}

		logger.trace("Range({},{})", objectProperty, range);
		return range;
	}

    @Override
    public OWLDataRange getRangeImpl(OWLDataProperty datatypeProperty) {
    	Set<OWLDataPropertyRangeAxiom> axioms = ontology.getDataPropertyRangeAxioms(datatypeProperty);
    	if(!axioms.isEmpty()){
    		OWLDataPropertyRangeAxiom axiom = axioms.iterator().next();
    		OWLDataRange range = axiom.getRange();
    		return range;
    	} else {
    		return df.getOWLDatatype(OWL2Datatype.RDFS_LITERAL.getIRI());
    	}
    }

    private OWLClassExpression asIntersection(NodeSet<OWLClass> nodeSet){
    	if(nodeSet.isEmpty() || nodeSet.isTopSingleton()){
    		return df.getOWLThing();
    	} else if(nodeSet.isSingleton()){
    		return nodeSet.iterator().next().getRepresentativeElement();
    	} else {
    		Set<OWLClassExpression> operands = new HashSet<OWLClassExpression>(nodeSet.getNodes().size());
    		for (Node<OWLClass> node : nodeSet) {
    			if(node.getSize() != 0) {
    				if(!node.isTopNode() && !node.isBottomNode()){
        				operands.add(node.getRepresentativeElement());
        			}
    			} else {
    				logger.warn("Reasoner returned empty node. Seems to be a bug.");
    			}

            }
    		if(operands.size() == 1) {
    			return operands.iterator().next();
    		}
    		return df.getOWLObjectIntersectionOf(operands);
    	}
    }

    private OWLClassExpression getDescriptionFromReturnedDomain(NodeSet<OWLClass> nodeSet) {
        if (nodeSet.isEmpty()){
        	return df.getOWLThing();
        }

        Set<OWLClassExpression> union = new HashSet<OWLClassExpression>();
        Set<OWLClassExpression> domains = new HashSet<OWLClassExpression>();

        for (Node<OWLClass> node : nodeSet) {
            union.add(node.getRepresentativeElement());
        }
        for (OWLClassExpression desc : union) {
            boolean isSuperClass = false;
            for (OWLClassExpression d : getClassHierarchy().getSubClasses(desc)) {
                if (union.contains(d)) {
                    isSuperClass = true;
                    break;
                }
            }
            if (!isSuperClass) {
                domains.add(desc);
            }
        }

        OWLClass oc = (OWLClass) domains.iterator().next();
        if (oc.isOWLThing()) {
            return df.getOWLThing();
        } else {
            return df.getOWLClass(IRI.create(oc.toStringID()));
        }
    }

    @Override
    public Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(OWLObjectProperty objectProperty) {
        Map<OWLIndividual, SortedSet<OWLIndividual>> map = new TreeMap<OWLIndividual, SortedSet<OWLIndividual>>();
        for (OWLIndividual ind : individuals) {
            Set<OWLIndividual> inds = getRelatedIndividuals(ind, objectProperty);
            map.put(ind, new TreeSet<OWLIndividual>(inds));
        }
        return map;
    }

    @Override
    protected Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationshipsImpl(OWLIndividual individual) {
        Map<OWLObjectProperty, Set<OWLIndividual>> map = new HashMap<OWLObjectProperty, Set<OWLIndividual>>();

        for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature(true)) {
            map.put(prop, getRelatedIndividualsImpl(individual, prop));
        }

        return map;
    }

	@Override
	public Set<OWLIndividual> getRelatedIndividualsImpl(
			OWLIndividual individual, OWLObjectProperty objectProperty) {

		Set<OWLNamedIndividual> namedIndividuals;
		try {
			namedIndividuals = reasoner.getObjectPropertyValues(
					individual.asOWLNamedIndividual(), objectProperty).getFlattened();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				namedIndividuals = fallbackReasoner.getObjectPropertyValues(
						individual.asOWLNamedIndividual(), objectProperty).getFlattened();
			} else {
				throw e;
			}
		}

		Set<OWLIndividual> values = new HashSet<OWLIndividual>(namedIndividuals.size());

		for (OWLNamedIndividual namedIndividual : namedIndividuals) {
			values.add(namedIndividual);
		}
		return values;
	}

	@Override
	public Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual,
			OWLDataProperty datatypeProperty) {

		Set<OWLLiteral> propVals;
		try {
			propVals = reasoner.getDataPropertyValues(
					individual.asOWLNamedIndividual(), datatypeProperty);

		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				propVals = fallbackReasoner.getDataPropertyValues(
						individual.asOWLNamedIndividual(), datatypeProperty);
			} else {
				throw e;
			}
		}

		return propVals;
	}

//    @Override
//    public Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual, OWLDataProperty datatypeProperty) {
//        return reasoner.getDataPropertyValues(individual.asOWLNamedIndividual(), datatypeProperty);
//    }

	public Map<OWLIndividual, SortedSet<Double>> getDoubleValues(OWLDataProperty dataProperty) {
		Map<OWLIndividual, SortedSet<Double>> map = new TreeMap<OWLIndividual, SortedSet<Double>>();

		for (OWLIndividual ind : individuals) {
			Set<OWLLiteral> literals;
			try {
				literals = reasoner.getDataPropertyValues(
						ind.asOWLNamedIndividual(), dataProperty);

			} catch (UnsupportedOperationException e) {
				if (useFallbackReasoner) {
					literals = fallbackReasoner.getDataPropertyValues(
							ind.asOWLNamedIndividual(), dataProperty);
				} else {
					throw e;
				}
			}

			if (!literals.isEmpty()) {
				SortedSet<Double> values = new TreeSet<Double>();
				for (OWLLiteral lit : literals) {
					if (lit.isDouble()) {
						values.add(lit.parseDouble());
					}
				}
				map.put(ind, values);
			}
		}
		return map;
	}

	@Override
	public Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembersImpl(OWLDataProperty dataProperty) {

		Map<OWLIndividual, SortedSet<OWLLiteral>> map =
				new TreeMap<OWLIndividual, SortedSet<OWLLiteral>>();

		for (OWLIndividual ind : individuals) {
			Set<OWLLiteral> literals;

			try {
				literals = reasoner.getDataPropertyValues(ind.asOWLNamedIndividual(), dataProperty);
			} catch (UnsupportedOperationException e) {
				if (useFallbackReasoner) {
					literals = fallbackReasoner.getDataPropertyValues(
							ind.asOWLNamedIndividual(), dataProperty);
				} else {
					throw e;
				}
			}

			if (!literals.isEmpty()) {
				map.put(ind, new TreeSet<OWLLiteral>(literals));
			}
		}
		return map;
	}


    // OWL API returns a set of nodes of classes, where each node
    // consists of equivalent classes; this method picks one class
    // from each node to flatten the set of nodes
    private TreeSet<OWLClassExpression> getFirstClasses(NodeSet<OWLClass> nodeSet) {
        TreeSet<OWLClassExpression> concepts = new TreeSet<OWLClassExpression>();
        for (Node<OWLClass> node : nodeSet) {
            // take one element from the set and ignore the rest
            // (TODO: we need to make sure we always ignore the same concepts)
        	if(node.getSize() != 0) {
        		OWLClass concept = node.getRepresentativeElement();
                concepts.add(concept);
        	} else {
        		logger.warn("Reasoner returned empty node. Seems to be a bug.");
        	}
        }
        return concepts;
    }

    private Set<OWLClass> getFirstClassesNoTopBottom(NodeSet<OWLClass> nodeSet) {
        Set<OWLClass> concepts = new HashSet<OWLClass>();
        for (Node<OWLClass> node : nodeSet) {
        	if(!node.isBottomNode() && !node.isTopNode()){
        		concepts.add(node.getRepresentativeElement());
        	}
        }
        return concepts;
    }

    private TreeSet<OWLObjectProperty> getFirstObjectProperties(NodeSet<OWLObjectPropertyExpression> nodeSet) {
        TreeSet<OWLObjectProperty> roles = new TreeSet<OWLObjectProperty>();
        for (Node<OWLObjectPropertyExpression> node : nodeSet) {
            if (node.isBottomNode() || node.isTopNode()) {
                continue;
            }
            if(node.getSize() == 0){
            	logger.warn("Reasoner returned empty property node. Could be a bug.");
            	continue;
            }
            // take one element from the set and ignore the rest
            // (TODO: we need to make sure we always ignore the same concepts)
            OWLObjectPropertyExpression property = node.getRepresentativeElement();
            if (!property.isAnonymous()) {
                roles.add(df.getOWLObjectProperty(IRI.create(property.asOWLObjectProperty().toStringID())));
            }
        }
        roles.remove(df.getOWLObjectProperty(IRI.create(df.getOWLTopObjectProperty().toStringID())));
        roles.remove(df.getOWLObjectProperty(IRI.create(df.getOWLBottomObjectProperty().toStringID())));
        return roles;
    }

    private TreeSet<OWLDataProperty> getFirstDatatypeProperties(NodeSet<OWLDataProperty> nodeSet) {
        TreeSet<OWLDataProperty> roles = new TreeSet<OWLDataProperty>();
        for (Node<OWLDataProperty> node : nodeSet) {
            if (node.isBottomNode() || node.isTopNode()) {
                continue;
            }
            if(node.getSize() == 0){
            	logger.warn("Reasoner returned empty property node. Could be a bug.");
            	continue;
            }
            OWLDataProperty property = node.getRepresentativeElement();
            roles.add(df.getOWLDataProperty(IRI.create(property.toStringID())));
        }
        roles.remove(df.getOWLDataProperty(IRI.create(df.getOWLTopDataProperty().toStringID())));
        roles.remove(df.getOWLDataProperty(IRI.create(df.getOWLBottomDataProperty().toStringID())));
        return roles;
    }

    @Override
	public Set<OWLDataProperty> getBooleanDatatypePropertiesImpl() {
		return (Set<OWLDataProperty>) datatype2Properties.get(XSD.BOOLEAN);
	}

	@Override
	public Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
		Set<OWLDataProperty> properties = new TreeSet<OWLDataProperty>();
		
		for (OWLDatatype dt:OWLAPIUtils.floatDatatypes) {
			properties.addAll(datatype2Properties.get(dt));
		}

		return properties;
	}

	@Override
	public Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
		Set<OWLDataProperty> properties = new TreeSet<OWLDataProperty>();
		
		for (OWLDatatype dt:OWLAPIUtils.intDatatypes) {
			properties.addAll(datatype2Properties.get(dt));
		}

		return properties;
	}

	@Override
	public Set<OWLDataProperty> getStringDatatypePropertiesImpl() {
		return (Set<OWLDataProperty>) datatype2Properties.get(XSD.STRING);
	}

	/* (non-Javadoc)
	  * @see org.dllearner.core.Reasoner#getBaseURI()
	  */
	@Override
	public String getBaseURI() {
		return baseURI;
	}

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getPrefixes()
      */
    @Override
    public Map<String, String> getPrefixes() {
        return prefixes;
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.ReasonerComponent#releaseKB()
      */
    @Override
    public void releaseKB() {
        reasoner.dispose();
    }

    public Set<OWLOntology> getOWLAPIOntologies() {
        return owlAPIOntologies;
    }

    /*public void setReasonerType(String type){
         configurator.setReasonerType(type);
     }*/

//	@Override
//	public boolean hasDatatypeSupport() {
//		return true;
//	}

	@Override
	public Set<OWLClass> getInconsistentClassesImpl() {
		Set<OWLClass> concepts = new HashSet<OWLClass>();

		Node<OWLClass> unsatClsNodes;
		try {
			unsatClsNodes = reasoner.getUnsatisfiableClasses();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				unsatClsNodes = fallbackReasoner.getUnsatisfiableClasses();
			} else {
				throw e;
			}
		}

		for (OWLClass concept : unsatClsNodes.getEntities()) {
			concepts.add(df.getOWLClass(IRI.create(concept.toStringID())));
		}

		return concepts;
	}

	public Set<OWLClass> getInconsistentOWLClasses() {
		Node<OWLClass> inconsClsNodes;
		try {
			inconsClsNodes = reasoner.getUnsatisfiableClasses();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				inconsClsNodes = fallbackReasoner.getUnsatisfiableClasses();
			} else {
				throw e;
			}
		}
		return inconsClsNodes.getEntities();
	}

    @Override
    public Set<OWLLiteral> getLabelImpl(OWLEntity entity) {
        Collection<OWLAnnotation> labelAnnotations = entity.getAnnotations(ontology, df.getRDFSLabel());
        Set<OWLLiteral> annotations = new HashSet<OWLLiteral>();
        for (OWLAnnotation label : labelAnnotations) {
            annotations.add((OWLLiteral) label.getValue());
        }
        return annotations;
    }

	/*
	 * (non-Javadoc)
	 *
	 * @see org.dllearner.core.BaseReasoner#remainsSatisfiable(org.dllearner.core.owl.Axiom)
	 */
	@Override
	public boolean remainsSatisfiableImpl(OWLAxiom axiom) {
		boolean consistent = true;

		try {
			manager.applyChange(new AddAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e1) {
			e1.printStackTrace();
		}

		try {
			consistent = reasoner.isConsistent();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				consistent = fallbackReasoner.isConsistent();
			} else {
				throw e;
			}
		}

		try {
			manager.applyChange(new RemoveAxiom(ontology, axiom));
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}

		return consistent;
	}

    /**
     * Returns asserted class definitions of given class
     *
     * @param nc the class
     * @return the asserted class definitions
     */
    @Override
    protected Set<OWLClassExpression> getAssertedDefinitionsImpl(OWLClass cls) {
        Collection<OWLClassExpression> definitions = cls.getEquivalentClasses(ontology);
        return new HashSet<>(definitions);
    }

    /**
     * Gets the OWL API ontology manager. Use with great caution.
     *
     * @return The OWL API ontology manager.
     */
    public OWLOntologyManager getManager() {
        return manager;
    }

    /**
     * Gets the internal OWL API ontology. Use with great caution.
     *
     * @return The internal OWL API ontology.
     */
    public OWLOntology getOntology() {
        return ontology;
    }

    /**
     * Gets the internal OWL API reasoner. Use with great caution.
     *
     * @return The internal OWL API reasoner.
     */
    public OWLReasoner getReasoner() {
        return reasoner;
    }

    /**
	 * @param reasonerImplementation the reasonerImplementation to set
	 */
	public void setReasonerImplementation(ReasonerImplementation reasonerImplementation) {
		this.reasonerImplementation = reasonerImplementation;
	}

    public String getOwlLinkURL() {
        return owlLinkURL;
    }

    /**
     * set the URL of the remote OWLLink server
     * @param owlLinkURL the URL of the remote OWLLink server
     */
    public void setOwlLinkURL(String owlLinkURL) {
        this.owlLinkURL = owlLinkURL;
    }

    /**
     * Some reasoner implementations do not support all operations yet.
     * In that case a fallback reasoner based only on the asserted
     * axioms can be enabled.
	 * @param useFallbackReasoner whether to enable a fallback reasoner
	 */
	public void setUseFallbackReasoner(boolean useFallbackReasoner) {
		this.useFallbackReasoner = useFallbackReasoner;
	}
	
	public OWLDatatype getDatatype(OWLDataProperty dp) {
		return dataproperty2datatype.get(dp);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractReasonerComponent#setSynchronized()
	 */
	@Override
	public void setSynchronized() {
		reasoner = new ThreadSafeOWLReasoner(reasoner);
	}
}
