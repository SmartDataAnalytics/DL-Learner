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
package org.dllearner.reasoning;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.owlapiv3.XSD;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.*;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.annotations.OutVariable;
import org.dllearner.core.config.ConfigOption;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLOntologyKnowledgeSource;
import org.dllearner.utilities.OWLAPIUtils;
import org.dllearner.utilities.owl.OWLClassExpressionMinimizer;
import org.semanticweb.HermiT.Configuration;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.structural.StructuralReasonerFactory;
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import uk.ac.manchester.cs.owl.owlapi.alternateimpls.ThreadSafeOWLReasoner;

//import de.tudresden.inf.lat.cel.owlapi.CelReasoner;
//import eu.trowl.owlapi3.rel.reasoner.dl.RELReasonerFactory;
//import org.semanticweb.elk.owlapi.ElkReasonerFactory;

/**
 * Mapping to OWL API reasoner interface. The OWL API currently
 * supports the following OWL reasoners({@link ReasonerType}): FaCT++, HermiT, Pellet, ELK, CEL and TrOWL. FaCT++ is connected
 * using JNI and native libraries, while the others are pure Java
 * libraries.
 *
 * @author Jens Lehmann
 * @author Lorenz Buehmann
 */
@ComponentAnn(name = "OWL API Reasoner", shortName = "oar", version = 0.8)
public class OWLAPIReasoner extends AbstractReasonerComponent {

    private OWLReasoner reasoner;
    private OWLOntologyManager manager;

    private OWLOntology ontology;
    // the data factory is used to generate OWL API objects
    private OWLDataFactory df;

    // primitives
    Set<OWLClass> atomicConcepts = new TreeSet<>();
    Set<OWLObjectProperty> atomicRoles = new TreeSet<>();
    SortedSet<OWLDataProperty> datatypeProperties = new TreeSet<>();
    SortedSet<OWLIndividual> individuals = new TreeSet<>();

    // namespaces
	@OutVariable
    private Map<String, String> prefixes = new TreeMap<>();
	@OutVariable
    private String baseURI;

    // references to OWL API ontologies
    private Set<OWLOntology> owlAPIOntologies = new HashSet<>();

    private OWLClassExpressionMinimizer minimizer;

    private OWLReasoner fallbackReasoner;
    
    
 // default reasoner is Pellet
    @ConfigOption(defaultValue="pellet", description="specifies the used OWL API reasoner implementation")
    private ReasonerImplementation reasonerImplementation = ReasonerImplementation.PELLET;

    @ConfigOption(defaultValue="false", description="specifies whether to use a fallback reasoner if a reasoner call fails because it's not supported or results in a bug. (the fallback works only on the assertional level")
    private boolean useFallbackReasoner = false;

    @ConfigOption(defaultValue="null", description="specifies the URL of the remote OWLLink server")
    private String owlLinkURL;

    public OWLAPIReasoner() {

    }

    public OWLAPIReasoner(KnowledgeSource... sources) {
        super(new HashSet<>(Arrays.asList(sources)));
    }

    public OWLAPIReasoner(Set<KnowledgeSource> sources) {
        super(sources);
    }

    public OWLAPIReasoner(OWLReasoner reasoner) {
        this.reasoner = reasoner;
        KnowledgeSource ks = new OWLAPIOntology(reasoner.getRootOntology());
        sources = Collections.singleton(ks);
    }

    @Override
    public void init() throws ComponentInitException {
        // reset variables (otherwise subsequent initialisation with
        // different knowledge sources will merge both)
        atomicConcepts = new TreeSet<>();
        atomicRoles = new TreeSet<>();
        datatypeProperties = new TreeSet<>();
        individuals = new TreeSet<>();

        // create OWL API ontology manager - make sure we use a new data factory so that we don't default to the static one which can cause problems in a multi threaded environment.
        df = new OWLDataFactoryImpl();
        manager = OWLManager.createOWLOntologyManager();

        prefixes = new TreeMap<>();

        for (KnowledgeSource source : sources) {
            if (source instanceof OWLOntologyKnowledgeSource) {
                ontology = ((OWLOntologyKnowledgeSource) source).createOWLOntology(manager);
                owlAPIOntologies.add(ontology);
            }else{
                //This reasoner requires an ontology to process
                throw new ComponentInitException("OWL API Reasoner requires an OWLKnowledgeSource.  Received a KS of type: " + source.getClass().getName());
            }

            atomicConcepts.addAll(ontology.getClassesInSignature(Imports.INCLUDED));
            atomicRoles.addAll(ontology.getObjectPropertiesInSignature(Imports.INCLUDED));
            datatypeProperties.addAll(ontology.getDataPropertiesInSignature(Imports.INCLUDED));
            individuals.addAll(ontology.getIndividualsInSignature(Imports.INCLUDED));

            // if several knowledge sources are included, then we can only
            // guarantee that the base URI is from one of those sources (there
            // can't be more than one); but we will take care that all prefixes are
            // correctly imported
            OWLDocumentFormat format = manager.getOntologyFormat(ontology);
            if (format != null && format.isPrefixOWLOntologyFormat()) {
                prefixes.putAll(format.asPrefixOWLOntologyFormat().getPrefixName2PrefixMap());
                baseURI = format.asPrefixOWLOntologyFormat().getDefaultPrefix();
                prefixes.remove("");
            }
        }

        // Now merge all of the knowledge sources into one ontology instance.
        try {
            //The following line illustrates a problem with using different OWLOntologyManagers.  This can manifest itself if we have multiple sources who were created with different manager instances.
            //ontology = OWLManager.createOWLOntologyManager().createOntology(IRI.create("http://dl-learner/all"), new HashSet<OWLOntology>(owlAPIOntologies));
			// we have to do this because e.g. data range axioms from imports won't by found via EntitySearcher.getRange method
			Set<OWLAxiom> allAxioms = owlAPIOntologies.stream()
//					.flatMap(o -> o.getLogicalAxioms(Imports.INCLUDED).stream()) // and for whatever reason, Pellet fails when we just use logical axioms and declaration axioms are missing
					.flatMap(o -> o.getAxioms(Imports.INCLUDED).stream())
					.collect(Collectors.toSet());
			ontology = manager.createOntology(allAxioms, IRI.generateDocumentIRI());

			//we have to add all import declarations manually here, because these are not OWL axioms
            List<OWLOntologyChange> addImports = new ArrayList<>();
            for (OWLOntology ont : owlAPIOntologies) {
            	for (OWLImportsDeclaration importDeclaration : ont.getImportsDeclarations()) {
            		addImports.add(new AddImport(ontology, importDeclaration));
				}
            }
            manager.applyChanges(addImports);
            // free some memory. It is useless to keep two copies of the same 
            // ontology
            for (OWLOntology toRemove : owlAPIOntologies) {
                manager.removeOntology(toRemove);
            }
            owlAPIOntologies = new HashSet<>();
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
					InferenceType.DATA_PROPERTY_ASSERTIONS,
            		InferenceType.SAME_INDIVIDUAL);
        } else {
        	PelletExplanation expGen = new PelletExplanation(ontology);
        	logger.error("The loaded ontology is logically inconsistent! One explanation for this is the following minimal set of axioms: "
					+ expGen.getInconsistencyExplanation());
            throw new ComponentInitException("Inconsistent ontologies.");
        }

        df = manager.getOWLDataFactory();

        initDatatypes();

        // remove top and bottom properties (for backwards compatibility)
//		atomicRoles.remove(df.getOWLObjectProperty(IRI.create("http://www.w3.org/2002/07/owl#bottomObjectProperty"));
//		atomicRoles.remove(df.getOWLObjectProperty(IRI.create("http://www.w3.org/2002/07/owl#topObjectProperty"));

        // remove classes that are built-in entities
		atomicConcepts.removeIf(cls -> cls.getIRI().isReservedVocabulary());

		 minimizer = new OWLClassExpressionMinimizer(df, this);
		 logger.info("Loaded reasoner: " + reasoner.getReasonerName() + " (" + reasoner.getClass().getName() + ")");
		 
		 initialized = true;
    }
    
    private void initDatatypes() {
	    Set<OWLDataProperty> numericDataProperties = new HashSet<>();
	    for (OWLDataProperty dataProperty : datatypeProperties) {
//		    Collection<OWLDataRange> ranges = EntitySearcher.getRanges(dataProperty, owlAPIOntologies);
		    Collection<OWLDataRange> ranges = Collections.emptySet();
		    LinkedList<OWLDataProperty> superDataProperties = new LinkedList<>();
		    superDataProperties.add(dataProperty);
		    while (ranges.isEmpty() && !superDataProperties.isEmpty()) {
			    OWLDataProperty sDP = superDataProperties.removeFirst();
			    ranges = EntitySearcher.getRanges(sDP, ontology);
			    if (ranges.isEmpty()) {
				    final NodeSet<OWLDataProperty> sps = reasoner.getSuperDataProperties(sDP, true);
				    superDataProperties.addAll(sps.getFlattened());
			    }
		    }
		    Iterator<OWLDataRange> it = ranges.iterator();
		    if (it.hasNext()) {
			    OWLDataRange range = it.next();
			    if (range.isDatatype()) {
				    OWLDatatype datatype = range.asOWLDatatype();

				    if (datatype.isBuiltIn()) { // OWL 2 DL compliant datatypes
					    datatype2Properties.put(range.asOWLDatatype(), dataProperty);

					    dataproperty2datatype.put(dataProperty, range.asOWLDatatype());

					    if (OWLAPIUtils.isNumericDatatype(range.asOWLDatatype())) {
						    numericDataProperties.add(dataProperty);
					    }
				    } else if (OWLAPIUtils.dtDatatypes.contains(datatype)) { // support for other XSD datatypes, e.g. xsd:date
					    datatype2Properties.put(range.asOWLDatatype(), dataProperty);

					    dataproperty2datatype.put(dataProperty, range.asOWLDatatype());
				    } else { // TODO handle non-built-in data types
				    }
			    } else { // TODO handle complex data property ranges
			    }
		    } else { // TODO handle data properties without range assertion
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
			reasonerFactory = new org.semanticweb.HermiT.ReasonerFactory();
			Configuration c = new Configuration();
			c.ignoreUnsupportedDatatypes = true;
//			c.throwInconsistentOntologyException = false;
			conf = c;
			break;
//		case TROWL:
//			reasonerFactory = new RELReasonerFactory();
//			break;
//		case CEL:
//			reasoner = new CelReasoner(ontology, conf);
//			break;
		case OWLLINK:
			reasonerFactory = new OWLlinkHTTPXMLReasonerFactory();
			URL url;
			try {
				url = new URL(getOwlLinkURL());//Configure the server end-point
				conf = new OWLlinkReasonerConfiguration(url);
			} catch (MalformedURLException e) {
				logger.error("Illegal URL <" + getOwlLinkURL() + "> for OWL Link HTTP reasoner", e);
			}
			break;
		case STRUCTURAL : 
			reasonerFactory = new StructuralReasonerFactory();
			break;
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

	/**
	 * A convenience method that determines if the specified axiom is entailed by the set of reasoner axioms.
	 * @see OWLReasoner#isEntailed(OWLAxiom)
	 * @param axiom the axiom
	 * @return {@code true} if {@code axiom} is entailed by the reasoner axioms
	 *         or {@code false} if {@code axiom} is not entailed by the reasoner
	 *         axioms. {@code true} if the set of reasoner axioms is
	 *         inconsistent.
	 */
	public boolean isEntailed(OWLAxiom axiom) {
		try {
			return reasoner.isEntailed(axiom);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				return fallbackReasoner.isEntailed(axiom);
			} else {
				throw e;
			}
		}
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

		return isEntailed(df.getOWLSubClassOfAxiom(
							df.getOWLObjectIntersectionOf(clsA, clsB),
							df.getOWLNothing()));
	}

	@Override
	protected boolean isEquivalentClassImpl(OWLClassExpression class1, OWLClassExpression class2) {
		return isEntailed(df.getOWLEquivalentClassesAxiom(class1, class2));
	}

	@Override
	protected TreeSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression concept) {
		NodeSet<OWLClass> classes;
		try {
			classes = reasoner.getSuperClasses(concept, true);
		} catch (UnsupportedOperationException e) {
			e.printStackTrace();
			if (useFallbackReasoner) {
				classes = fallbackReasoner.getSubClasses(concept, true);
			} else {
				throw e;
			}
		}
		return new TreeSet<>(classes.getFlattened());
//		return getFirstClasses(classes);
	}

	@Override
	protected TreeSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression ce) {
		NodeSet<OWLClass> classes;

		try {
			classes = reasoner.getSubClasses(ce, true);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				classes = fallbackReasoner.getSubClasses(ce, true);
			} else {
				throw e;
			}
		}
		TreeSet<OWLClassExpression> subClasses = new TreeSet<>(classes.getFlattened());//getFirstClasses(classes);
		subClasses.remove(df.getOWLNothing());
		// remove built-in entities sometimes returned as subclasses of
		// owl:Thing
		if (ce.isOWLThing()) {
			subClasses.removeIf(_ce -> !_ce.isAnonymous() &&
					_ce.asOWLClass().getIRI().isReservedVocabulary());
		}
		return subClasses;
	}

    private <T extends OWLObject> SortedSet<T> getRepresentativeEntities(NodeSet<T> nodeSet){
    	SortedSet<T> representatives = new TreeSet<>();
    	for (Node<T> node : nodeSet) {
			if(!node.isBottomNode() && !node.isTopNode()){
				representatives.add(node.getRepresentativeElement());
			}
		}
    	return representatives;
    }

	protected SortedSet<OWLClassExpression> getEquivalentClassesImpl(OWLClassExpression ce) {
		SortedSet<OWLClassExpression> equivalentClasses = new TreeSet<>();
		Node<OWLClass> classNodes;
		try {
			classNodes = reasoner.getEquivalentClasses(ce);
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				classNodes = fallbackReasoner.getEquivalentClasses(ce);
			} else {
				throw e;
			}
		}

		equivalentClasses.addAll(classNodes.getEntitiesMinusTop());
		equivalentClasses.remove(ce);
		return equivalentClasses;
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
		logger.trace("getIndividuals for " + ce);
		try {
			individuals = reasoner.getInstances(ce, false).getFlattened();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				individuals = fallbackReasoner.getInstances(ce, false).getFlattened();
			} else {
				throw e;
			}
		}

		return new TreeSet<>(individuals);
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

		Set<OWLClassExpression> domains = new HashSet<>();

		// get all asserted domains
		domains.addAll(EntitySearcher.getDomains(objectProperty, ontology));

		// do the same for all super properties
		SortedSet<OWLObjectProperty> superProperties = getSuperProperties(objectProperty);
		for (OWLObjectProperty supProp : superProperties) {
			domains.addAll(EntitySearcher.getDomains(supProp, ontology));
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

		// several domains have to be treated as intersection
		OWLClassExpression domain = asIntersection(domains);

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

		Set<OWLClassExpression> domains = new HashSet<>();

		// get all asserted domains
		domains.addAll(EntitySearcher.getDomains(dataProperty, ontology));

		// do the same for all super properties
		SortedSet<OWLDataProperty> superProperties = getSuperProperties(dataProperty);
		for (OWLDataProperty supProp : superProperties) {
			domains.addAll(EntitySearcher.getDomains(supProp, ontology));
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

		// several domains have to be treated as intersection
		OWLClassExpression domain = asIntersection(domains);

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

		Set<OWLClassExpression> ranges = new HashSet<>();

		// get all asserted ranges
		ranges.addAll(EntitySearcher.getRanges(objectProperty, ontology));

		// do the same for all super properties
		SortedSet<OWLObjectProperty> superProperties = getSuperProperties(objectProperty);
		for (OWLObjectPropertyExpression supProp : superProperties) {
			ranges.addAll(EntitySearcher.getRanges(supProp, ontology));
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

		// several ranges have to be treated as intersection
		OWLClassExpression range = asIntersection(ranges);

		logger.trace("Range({},{})", objectProperty, range);
		return range;
	}

    @Override
    public OWLDataRange getRangeImpl(OWLDataProperty datatypeProperty) {
    	Set<OWLDataPropertyRangeAxiom> axioms = ontology.getDataPropertyRangeAxioms(datatypeProperty);
    	if(!axioms.isEmpty()){
    		OWLDataPropertyRangeAxiom axiom = axioms.iterator().next();
    		return axiom.getRange();
    	} else {
    		return df.getOWLDatatype(OWL2Datatype.RDFS_LITERAL.getIRI());
    	}
    }

    private OWLClassExpression asIntersection(Set<OWLClassExpression> classExpressions){
    	if(classExpressions.isEmpty()){
    		return df.getOWLThing();
    	} else if(classExpressions.size() == 1){
    		return classExpressions.iterator().next();
    	} else {
    		return df.getOWLObjectIntersectionOf(classExpressions);
    	}
    }

    private OWLClassExpression getDescriptionFromReturnedDomain(NodeSet<OWLClass> nodeSet) {
        if (nodeSet.isEmpty()){
        	return df.getOWLThing();
        }

        Set<OWLClassExpression> union = new HashSet<>();
        Set<OWLClassExpression> domains = new HashSet<>();

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
        Map<OWLIndividual, SortedSet<OWLIndividual>> map = new TreeMap<>();
        for (OWLIndividual ind : individuals) {
            Set<OWLIndividual> inds = getRelatedIndividuals(ind, objectProperty);
            map.put(ind, new TreeSet<>(inds));
        }
        return map;
    }

    @Override
    protected Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationshipsImpl(OWLIndividual individual) {
        Map<OWLObjectProperty, Set<OWLIndividual>> map = new HashMap<>();

        for (OWLObjectProperty prop : ontology.getObjectPropertiesInSignature(Imports.INCLUDED)) {
            map.put(prop, getRelatedIndividualsImpl(individual, prop));
        }

        return map;
    }

	@Override
	protected Map<OWLDataProperty, Set<OWLLiteral>> getDataPropertyRelationshipsImpl(OWLIndividual individual) throws ReasoningMethodUnsupportedException {
		Map<OWLDataProperty, Set<OWLLiteral>> map = new HashMap<>();

		for (OWLDataProperty prop : ontology.getDataPropertiesInSignature(Imports.INCLUDED)) {
			map.put(prop, getRelatedValuesImpl(individual, prop));
		}

		return map;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Set<OWLIndividual> getRelatedIndividualsImpl(
			OWLIndividual individual, OWLObjectProperty objectProperty) {

		Set<? extends OWLIndividual> namedIndividuals;
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

		return (Set<OWLIndividual>) namedIndividuals;
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
		Map<OWLIndividual, SortedSet<Double>> map = new TreeMap<>();

		for (OWLIndividual ind : individuals) {
			Set<OWLLiteral> literals = getRelatedValuesImpl(ind, dataProperty);

			if (!literals.isEmpty()) {
				SortedSet<Double> values = new TreeSet<>();
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

		Map<OWLIndividual, SortedSet<OWLLiteral>> map = new TreeMap<>();

		for (OWLIndividual ind : individuals) {
			Set<OWLLiteral> literals = getRelatedValuesImpl(ind, dataProperty);

			if (!literals.isEmpty()) {
				map.put(ind, new TreeSet<>(literals));
			}
		}
		return map;
	}

    // OWL API returns a set of nodes of classes, where each node
    // consists of equivalent classes; this method picks one class
    // from each node to flatten the set of nodes
    private TreeSet<OWLClassExpression> getFirstClasses(NodeSet<OWLClass> nodeSet) {
        TreeSet<OWLClassExpression> concepts = new TreeSet<>();
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
        Set<OWLClass> concepts = new HashSet<>();
        for (Node<OWLClass> node : nodeSet) {
        	if(!node.isBottomNode() && !node.isTopNode()){
        		concepts.add(node.getRepresentativeElement());
        	}
        }
        return concepts;
    }

    private TreeSet<OWLObjectProperty> getFirstObjectProperties(NodeSet<OWLObjectPropertyExpression> nodeSet) {
        TreeSet<OWLObjectProperty> roles = new TreeSet<>();
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
                roles.add(property.asOWLObjectProperty());
            }
        }
		// we ignore top and bottom properties
        roles.remove(df.getOWLTopObjectProperty());
        roles.remove(df.getOWLBottomObjectProperty());
        return roles;
    }

    private TreeSet<OWLDataProperty> getFirstDatatypeProperties(NodeSet<OWLDataProperty> nodeSet) {
        TreeSet<OWLDataProperty> roles = new TreeSet<>();
        for (Node<OWLDataProperty> node : nodeSet) {
            if (node.isBottomNode() || node.isTopNode()) {
                continue;
            }
            if(node.getSize() == 0){
            	logger.warn("Reasoner returned empty property node. Could be a bug.");
            	continue;
            }
            OWLDataProperty property = node.getRepresentativeElement();
            roles.add(property);
        }
		// we ignore top and bottom properties
        roles.remove(df.getOWLTopDataProperty());
        roles.remove(df.getOWLBottomDataProperty());
        return roles;
    }

    @Override
	public Set<OWLDataProperty> getBooleanDatatypePropertiesImpl() {
		return (Set<OWLDataProperty>) datatype2Properties.get(XSD.BOOLEAN);
	}

	@Override
	public Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
		Set<OWLDataProperty> properties = new TreeSet<>();
		
		for (OWLDatatype dt:OWLAPIUtils.floatDatatypes) {
			properties.addAll(datatype2Properties.get(dt));
		}

		return properties;
	}

	@Override
	public Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
		Set<OWLDataProperty> properties = new TreeSet<>();
		
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

//    public Set<OWLOntology> getOWLAPIOntologies() {
//        return owlAPIOntologies;
//    }

    /*public void setReasonerType(String type){
         configurator.setReasonerType(type);
     }*/

//	@Override
//	public boolean hasDatatypeSupport() {
//		return true;
//	}

	@Override
	public Set<OWLClass> getInconsistentClassesImpl() {
		Set<OWLClass> unsatisfiableClasses;

		try {
			unsatisfiableClasses = reasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				unsatisfiableClasses = fallbackReasoner.getUnsatisfiableClasses().getEntitiesMinusBottom();
			} else {
				throw e;
			}
		}

		return unsatisfiableClasses;
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
        Collection<OWLAnnotation> labelAnnotations = EntitySearcher.getAnnotations(entity, ontology, df.getRDFSLabel());
        Set<OWLLiteral> annotations = new HashSet<>();
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
		boolean consistent;

		manager.addAxiom(ontology, axiom);

		try {
			consistent = reasoner.isConsistent();
		} catch (UnsupportedOperationException e) {
			if (useFallbackReasoner) {
				consistent = fallbackReasoner.isConsistent();
			} else {
				throw e;
			}
		}

		manager.removeAxiom(ontology, axiom);

		return consistent;
	}

    /**
     * Returns asserted class definitions of given class
     *
     * @param cls the class
     * @return the asserted class definitions
     */
    @Override
    protected Set<OWLClassExpression> getAssertedDefinitionsImpl(OWLClass cls) {
        Collection<OWLClassExpression> definitions = EntitySearcher.getEquivalentClasses(cls, ontology);
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
	
	@Override
	public OWLDatatype getDatatype(OWLDataProperty dp) {
		return dataproperty2datatype.get(dp);
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.AbstractReasonerComponent#setSynchronized()
	 */
	@Override @NoConfigOption
	public void setSynchronized() {
		if(!(reasoner instanceof ThreadSafeOWLReasoner)) {
			reasoner = new ThreadSafeOWLReasoner(reasoner);
		}
	}

	public static void main(String[] args) throws Exception{
		OWLOntology o = OWLManager.createOWLOntologyManager().loadOntologyFromOntologyDocument(new File(System.getProperty("java.io.tmpdir") + File.separator + "test2.rdf"));
		System.out.println(o.getClassesInSignature());
		System.out.println(o.getDataPropertiesInSignature());
		System.out.println(o.getIndividualsInSignature().size());
	}
}
