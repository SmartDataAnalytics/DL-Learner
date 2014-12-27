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
import org.semanticweb.HermiT.Reasoner.ReasonerFactory;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.PrefixDocumentFormat;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDocumentFormat;
import org.semanticweb.owlapi.model.OWLEntity;
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
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.RemoveAxiom;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.owllink.OWLlinkHTTPXMLReasonerFactory;
import org.semanticweb.owlapi.owllink.OWLlinkReasonerConfiguration;
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
import org.semanticweb.owlapi.search.EntitySearcher;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

import uk.ac.manchester.cs.factplusplus.owlapiv3.FaCTPlusPlusReasonerFactory;
import uk.ac.manchester.cs.jfact.JFactFactory;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import com.clarkparsia.owlapi.explanation.PelletExplanation;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

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

//	private static Logger logger = Logger
//	.getLogger(OWLAPIReasoner.class);	

    //private String reasonerType = "pellet";
    private OWLReasoner reasoner;
    private OWLOntologyManager manager;

    private OWLOntology ontology;
    // the data factory is used to generate OWL API objects
    private OWLDataFactory df;
    // static factory
//	private static OWLDataFactory staticFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();

//	private ClassHierarchy subsumptionHierarchy;
//	private ObjectPropertyHierarchy roleHierarchy;	
//	private OWLDataPropertyHierarchy datatypePropertyHierarchy;
//	private Set<OWLClassExpression> allowedConceptsInSubsumptionHierarchy;

    // primitives
    Set<OWLClass> atomicConcepts = new TreeSet<OWLClass>();
    Set<OWLObjectProperty> atomicRoles = new TreeSet<OWLObjectProperty>();
    SortedSet<OWLDataProperty> datatypeProperties = new TreeSet<OWLDataProperty>();
//    SortedSet<OWLDataProperty> booleanDatatypeProperties = new TreeSet<OWLDataProperty>();
//    SortedSet<OWLDataProperty> doubleDatatypeProperties = new TreeSet<OWLDataProperty>();
//    SortedSet<OWLDataProperty> intDatatypeProperties = new TreeSet<OWLDataProperty>();
//    SortedSet<OWLDataProperty> stringDatatypeProperties = new TreeSet<OWLDataProperty>();
    SortedSet<OWLIndividual> individuals = new TreeSet<OWLIndividual>();
    
    private Multimap<OWL2Datatype, OWLDataProperty> datatype2Properties = HashMultimap.create();

    // namespaces
    private Map<String, String> prefixes = new TreeMap<String, String>();
    private String baseURI;

    // references to OWL API ontologies
    private Set<OWLOntology> owlAPIOntologies = new HashSet<OWLOntology>();
    @ConfigOption(name = "reasonerType", description = "The name of the OWL APIReasoner to use {\"fact\", \"hermit\", \"owllink\", \"pellet\", \"elk\", \"cel\"}", defaultValue = "pellet", required = false, propertyEditorClass = StringTrimmerEditor.class)
    private String reasonerTypeString = "pellet";
    @ConfigOption(name = "owlLinkURL", description = "The URL to the owl server", defaultValue = "", required = false, propertyEditorClass = StringTrimmerEditor.class)
    private String owlLinkURL;
    
    // default reasoner is Pellet
    private ReasonerImplementation reasonerImplementation = ReasonerImplementation.PELLET;


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

            atomicConcepts.addAll(ontology.getClassesInSignature(Imports.INCLUDED));
            atomicRoles.addAll(ontology.getObjectPropertiesInSignature(Imports.INCLUDED));
            datatypeProperties.addAll(ontology.getDataPropertiesInSignature(Imports.INCLUDED));
            individuals.addAll(ontology.getIndividualsInSignature(Imports.INCLUDED));

            // if several knowledge sources are included, then we can only
            // guarantee that the base URI is from one of those sources (there
            // can't be more than one); but we will take care that all prefixes are
            // correctly imported
            OWLDocumentFormat format = manager.getOntologyFormat(ontology);
            if (format instanceof PrefixDocumentFormat) {
                prefixes.putAll(((PrefixDocumentFormat) format).getPrefixName2PrefixMap());
                baseURI = ((PrefixDocumentFormat) format).getDefaultPrefix();
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
        initBaseReasoner();

        // compute class hierarchy and types of individuals
        // (done here to speed up later reasoner calls)
        boolean inconsistentOntology = !reasoner.isConsistent();

        if (!inconsistentOntology) {
            reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY, InferenceType.CLASS_ASSERTIONS);
        } else {
        	PelletExplanation expGen = new PelletExplanation(ontology);
        	System.out.println(expGen.getInconsistencyExplanation());
        	reasoner.precomputeInferences(InferenceType.CLASS_HIERARCHY);
            throw new ComponentInitException("Inconsistent ontologies.");
        }

        df = manager.getOWLDataFactory();

        Set<OWLDataProperty> numericDataProperties = new HashSet<OWLDataProperty>();
        for (OWLDataProperty dataProperty : datatypeProperties) {
            Collection<OWLDataRange> ranges = EntitySearcher.getRanges(dataProperty, owlAPIOntologies);
			Iterator<OWLDataRange> it = ranges.iterator();
			if (it.hasNext()) {
				OWLDataRange range = it.next();
				if (range.isDatatype() && range.asOWLDatatype().isBuiltIn()) {
					datatype2Properties.put(range.asOWLDatatype().getBuiltInDatatype(), dataProperty);
					
					if(isNumericDatatype(range.asOWLDatatype())) {
						numericDataProperties.add(dataProperty);
					}
				}
			} else {
				datatype2Properties.put(OWL2Datatype.XSD_STRING, dataProperty);
			}
        }

        // remove top and bottom properties (for backwards compatibility)
//		atomicRoles.remove(df.getOWLObjectProperty(IRI.create("http://www.w3.org/2002/07/owl#bottomObjectProperty"));
//		atomicRoles.remove(df.getOWLObjectProperty(IRI.create("http://www.w3.org/2002/07/owl#topObjectProperty"));
        
        
        // remove classes that are built-in entities
		Iterator<OWLClass> it = atomicConcepts.iterator();
		while (it.hasNext()) {
			OWLClass cls = (OWLClass) it.next();
			if(cls.getIRI().isReservedVocabulary()){
				it.remove();
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
       
        reasoner = reasonerFactory.createNonBufferingReasoner(ontology, conf);
    }
    
    private boolean isNumericDatatype(OWLDatatype datatype){
    	if(!datatype.isBuiltIn()){
    		return false;
    	}
    	Set<OWL2Datatype> numericDatatypes = Sets.newHashSet(
    			OWL2Datatype.XSD_BYTE, 
    			OWL2Datatype.XSD_SHORT,
    			OWL2Datatype.XSD_INT, 
    			OWL2Datatype.XSD_INTEGER,
    			OWL2Datatype.XSD_LONG,
    			OWL2Datatype.XSD_DOUBLE, 
    			OWL2Datatype.XSD_FLOAT
    			);
    	OWL2Datatype builtInDatatype = datatype.getBuiltInDatatype();
		return numericDatatypes.contains(builtInDatatype);
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getAtomicConcepts()
      */
    public Set<OWLClass> getClasses() {
        return Collections.unmodifiableSet(atomicConcepts);
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getAtomicRoles()
      */
    public Set<OWLObjectProperty> getObjectProperties() {
        return Collections.unmodifiableSet(atomicRoles);
    }

    @Override
    public Set<OWLDataProperty> getDatatypePropertiesImpl() {
        return datatypeProperties;
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getIndividuals()
      */
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
        if (getReasonerTypeString().equals("fact")) {
            return ReasonerType.OWLAPI_FACT;
        } else if (getReasonerTypeString().equals("hermit")) {
            return ReasonerType.OWLAPI_HERMIT;
        } else {
            return ReasonerType.OWLAPI_PELLET;
        }
    }

    @Override
    public boolean isSuperClassOfImpl(OWLClassExpression superConcept, OWLClassExpression subConcept) {
        return reasoner.isEntailed(df.getOWLSubClassOfAxiom(subConcept, superConcept));
    }

    @Override
    protected boolean isEquivalentClassImpl(OWLClassExpression class1, OWLClassExpression class2) {
        return reasoner.isEntailed(df.getOWLEquivalentClassesAxiom(class1, class2));
    }

    @Override
    protected TreeSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression concept) {
    	NodeSet<OWLClass> classes = reasoner.getSuperClasses(concept, true);
        return getFirstClasses(classes);
    }

    @Override
    protected TreeSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression concept) {
        NodeSet<OWLClass> classes = reasoner.getSubClasses(concept, true);
        TreeSet<OWLClassExpression> subClasses = getFirstClasses(classes);
        subClasses.remove(df.getOWLNothing());
        // remove built-in entites sometimes returned as subclasses of owl:Thing
        if(concept.isOWLThing()){
        	Iterator<OWLClassExpression> it = subClasses.iterator();
        	while (it.hasNext()) {
				OWLClassExpression ce = (OWLClassExpression) it.next();
				if(!ce.isAnonymous() && ce.asOWLClass().getIRI().isReservedVocabulary()){
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
    	for (OWLClass eqCls : reasoner.getEquivalentClasses(concept).getEntitiesMinusTop()) {
    		equivalentclasses.add(eqCls);
		}
    	equivalentclasses.remove(concept);
        return equivalentclasses;
    }

    @Override
    protected TreeSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty objectProperty) {
        NodeSet<OWLObjectPropertyExpression> properties = reasoner.getSuperObjectProperties(objectProperty, true);
        return getFirstObjectProperties(properties);
    }

    @Override
    protected TreeSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty objectProperty) {
        NodeSet<OWLObjectPropertyExpression> properties = reasoner.getSubObjectProperties(objectProperty, true);
        return getFirstObjectProperties(properties);
    }

    @Override
    protected TreeSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty dataProperty) {
    	NodeSet<OWLDataProperty> properties = reasoner.getSuperDataProperties(dataProperty, true);
        return getFirstDatatypeProperties(properties);
    }

    @Override
    protected TreeSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty dataProperty) {
        NodeSet<OWLDataProperty> properties = reasoner.getSubDataProperties(dataProperty, true);
        return getFirstDatatypeProperties(properties);
    }

    @Override
    public boolean hasTypeImpl(OWLClassExpression concept, OWLIndividual individual) {
        if(concept.isOWLThing()){
        	return true;
        } else if(concept.isOWLNothing()){
        	return false;
        } else {
        	return reasoner.isEntailed(df.getOWLClassAssertionAxiom(concept, individual));
        }
    }

    @Override
    public SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression ce) {
        Set<OWLNamedIndividual> individuals = reasoner.getInstances(ce, false).getFlattened();
        SortedSet<OWLIndividual> inds = new TreeSet<OWLIndividual>();
        for (OWLNamedIndividual ind : individuals){
        	inds.add(ind);
        }
        return inds;
    }

    @Override
    public Set<OWLClass> getTypesImpl(OWLIndividual individual) {
        NodeSet<OWLClass> nodeSet = reasoner.getTypes(individual.asOWLNamedIndividual(), false);
        return getFirstClassesNoTopBottom(nodeSet);
    }

    @Override
    public boolean isSatisfiableImpl() {
        return reasoner.isSatisfiable(df.getOWLThing());
    }

    @Override
    public OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
    	NodeSet<OWLClass> nodeSet = reasoner.getObjectPropertyDomains(objectProperty, true);
        OWLClassExpression domain = asIntersection(nodeSet);
        logger.trace("Domain(" + objectProperty + "," + domain + ")");
		return domain;
    }

    @Override
    public OWLClassExpression getDomainImpl(OWLDataProperty datatypeProperty) {
        NodeSet<OWLClass> nodeSet = reasoner.getDataPropertyDomains(datatypeProperty, true);
        OWLClassExpression domain = asIntersection(nodeSet);
        logger.trace("Domain(" + datatypeProperty + "," + domain + ")");
		return domain;
    }

    @Override
    public OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
        NodeSet<OWLClass> nodeSet = reasoner.getObjectPropertyRanges(objectProperty, true);
        return asIntersection(nodeSet);
    }
    
    @Override
    public OWLDataRange getRangeImpl(OWLDataProperty datatypeProperty) {
    	Set<OWLDataPropertyRangeAxiom> axioms = ontology.getDataPropertyRangeAxioms(datatypeProperty);
    	if(!axioms.isEmpty()){
    		OWLDataPropertyRangeAxiom axiom = axioms.iterator().next();
    		OWLDataRange range = axiom.getRange();
    		return range;
    	} else {
    		return df.getOWLDatatype(org.semanticweb.owlapi.vocab.OWL2Datatype.RDFS_LITERAL.getIRI());
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
    public Set<OWLIndividual> getRelatedIndividualsImpl(OWLIndividual individual, OWLObjectProperty objectProperty) {
    	Set<OWLNamedIndividual> namedIndividuals = reasoner.getObjectPropertyValues(individual.asOWLNamedIndividual(), objectProperty).getFlattened();
    	Set<OWLIndividual> values = new HashSet<OWLIndividual>(namedIndividuals.size());
    	for (OWLNamedIndividual namedIndividual : namedIndividuals) {
			values.add(namedIndividual);
		}
    	return values;
    }

    @Override
    public Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual, OWLDataProperty datatypeProperty) {
        return reasoner.getDataPropertyValues(individual.asOWLNamedIndividual(), datatypeProperty);
    }

    public Map<OWLIndividual, SortedSet<Double>> getDoubleValues(OWLDataProperty dataProperty) {
        Map<OWLIndividual, SortedSet<Double>> map = new TreeMap<OWLIndividual, SortedSet<Double>>();
        for (OWLIndividual ind : individuals) {
        	Set<OWLLiteral> literals = reasoner.getDataPropertyValues(ind.asOWLNamedIndividual(), dataProperty);
            if (!literals.isEmpty()) {
            	SortedSet<Double> values = new TreeSet<Double>();
                for (OWLLiteral lit : literals) {
                	if(lit.isDouble()){
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
        Map<OWLIndividual, SortedSet<OWLLiteral>> map = new TreeMap<OWLIndividual, SortedSet<OWLLiteral>>();
        for (OWLIndividual ind : individuals) {
        	Set<OWLLiteral> literals = reasoner.getDataPropertyValues(ind.asOWLNamedIndividual(), dataProperty);
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
		return (Set<OWLDataProperty>) datatype2Properties.get(OWL2Datatype.XSD_BOOLEAN);
	}

	@Override
	public Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
		return (Set<OWLDataProperty>) datatype2Properties.get(OWL2Datatype.XSD_DOUBLE);
	}

	@Override
	public Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
		return (Set<OWLDataProperty>) datatype2Properties.get(OWL2Datatype.XSD_INT);
	}

	@Override
	public Set<OWLDataProperty> getStringDatatypePropertiesImpl() {
		return (Set<OWLDataProperty>) datatype2Properties.get(OWL2Datatype.XSD_STRING);
	}

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getBaseURI()
      */
    public String getBaseURI() {
        return baseURI;
    }

    /* (non-Javadoc)
      * @see org.dllearner.core.Reasoner#getPrefixes()
      */
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

        for (OWLClass concept : reasoner.getUnsatisfiableClasses().getEntities()) {
            concepts.add(df.getOWLClass(IRI.create(concept.toStringID())));
        }

        return concepts;
    }


    public Set<OWLClass> getInconsistentOWLClasses() {
        return reasoner.getUnsatisfiableClasses().getEntities();
    }

    @Override
    public Set<OWLLiteral> getLabelImpl(OWLEntity entity) {
        Collection<OWLAnnotation> labelAnnotations = EntitySearcher.getAnnotations(df.getRDFSLabel(), ontology);
        Set<OWLLiteral> annotations = new HashSet<OWLLiteral>();
        for (OWLAnnotation label : labelAnnotations) {
            annotations.add((OWLLiteral) label.getValue());
        }
        return annotations;
    }

    /* (non-Javadoc)
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

        consistent = reasoner.isConsistent();

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

    public String getReasonerTypeString() {
        return reasonerTypeString;
    }

    public void setReasonerTypeString(String reasonerTypeString) {
        this.reasonerTypeString = reasonerTypeString;
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

    public void setOwlLinkURL(String owlLinkURL) {
        this.owlLinkURL = owlLinkURL;
    }
}
