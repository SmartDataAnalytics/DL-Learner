package org.dllearner.reasoning;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.configurators.PelletReasonerConfigurator;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.Constant;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.Datatype;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinValue;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.KB;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.ObjectValueRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.TypedConstant;
import org.dllearner.core.owl.Union;
import org.dllearner.core.owl.UntypedConstant;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.kb.OWLFile;
import org.dllearner.kb.sparql.SparqlKnowledgeSource;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.dllearner.utilities.owl.RoleComparator;
import org.mindswap.pellet.PelletOptions;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.SetUtils;
import org.mindswap.pellet.utils.progress.ProgressMonitor;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAnnotation;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLConstant;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLDataRange;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLLabelAnnotation;
import org.semanticweb.owl.model.OWLNamedObject;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChangeException;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyFormat;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.semanticweb.owl.model.OWLOntologyStorageException;
import org.semanticweb.owl.model.OWLTypedConstant;
import org.semanticweb.owl.model.OWLUntypedConstant;
import org.semanticweb.owl.model.RemoveAxiom;
import org.semanticweb.owl.model.UnknownOWLOntologyException;
import org.semanticweb.owl.util.SimpleURIMapper;
import org.semanticweb.owl.vocab.NamespaceOWLOntologyFormat;

import com.clarkparsia.explanation.PelletExplanation;

public class PelletReasoner extends ReasonerComponent {
	
	private Reasoner reasoner;
	private OWLOntologyManager manager;
	private OWLOntology ontology;
	// the data factory is used to generate OWL API objects
	private OWLDataFactory factory;
	
	private PelletReasonerConfigurator configurator;
	

	
	private ConceptComparator conceptComparator = new ConceptComparator();
	private RoleComparator roleComparator = new RoleComparator();
	
	Set<NamedClass> atomicConcepts = new TreeSet<NamedClass>(conceptComparator);
	Set<ObjectProperty> atomicRoles = new TreeSet<ObjectProperty>(roleComparator);
	SortedSet<DatatypeProperty> datatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
	SortedSet<DatatypeProperty> intDatatypeProperties = new TreeSet<DatatypeProperty>();
	TreeSet<Individual> individuals = new TreeSet<Individual>();	
	
	
	//CWA
	// we use sorted sets (map indices) here, because they have only log(n)
	// complexity for checking whether an element is contained in them
	// instances of classes
	private Map<NamedClass, TreeSet<Individual>> classInstancesPos = new TreeMap<NamedClass, TreeSet<Individual>>();
	private Map<NamedClass, TreeSet<Individual>> classInstancesNeg = new TreeMap<NamedClass, TreeSet<Individual>>();
	// object property mappings
	private Map<ObjectProperty, Map<Individual, SortedSet<Individual>>> opPos = new TreeMap<ObjectProperty, Map<Individual, SortedSet<Individual>>>();
	// datatype property mappings
	// we have one mapping for true and false for efficiency reasons
	private Map<DatatypeProperty, TreeSet<Individual>> bdPos = new TreeMap<DatatypeProperty, TreeSet<Individual>>();
	private Map<DatatypeProperty, TreeSet<Individual>> bdNeg = new TreeMap<DatatypeProperty, TreeSet<Individual>>();
	// for int and double we assume that a property can have several values,
	// althoug this should be rare,
	// e.g. hasValue(object,2) and hasValue(object,3)
	private Map<DatatypeProperty, Map<Individual, SortedSet<Double>>> dd = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Double>>>();
	private Map<DatatypeProperty, Map<Individual, SortedSet<Integer>>> id = new TreeMap<DatatypeProperty, Map<Individual, SortedSet<Integer>>>();
	
	
	// namespaces
	private Map<String, String> prefixes = new TreeMap<String,String>();
	private String baseURI;
	
	// references to OWL API ontologies
	private List<OWLOntology> owlAPIOntologies = new LinkedList<OWLOntology>();

	public PelletReasoner(Set<KnowledgeSource> sources) {
		super(sources);
		this.configurator = new PelletReasonerConfigurator(this);
	}
	
	public void loadOntologies() {
		Comparator<OWLNamedObject> namedObjectComparator = new Comparator<OWLNamedObject>() {
			public int compare(OWLNamedObject o1, OWLNamedObject o2) {
				return o1.getURI().compareTo(o2.getURI());
			}
		};
		Set<OWLClass> classes = new TreeSet<OWLClass>(namedObjectComparator);
		Set<OWLObjectProperty> owlObjectProperties = new TreeSet<OWLObjectProperty>(
				namedObjectComparator);
		Set<OWLDataProperty> owlDatatypeProperties = new TreeSet<OWLDataProperty>(
				namedObjectComparator);
		Set<OWLIndividual> owlIndividuals = new TreeSet<OWLIndividual>(
				namedObjectComparator);

		Set<OWLOntology> allImports = new HashSet<OWLOntology>();
		prefixes = new TreeMap<String, String>();

		for (KnowledgeSource source : sources) {

			if (source instanceof OWLFile
					|| source instanceof SparqlKnowledgeSource
					|| source instanceof OWLAPIOntology) {
				URL url = null;
				if (source instanceof OWLFile) {
					url = ((OWLFile) source).getURL();
				}

				try {

					if (source instanceof OWLAPIOntology) {
						ontology = ((OWLAPIOntology) source).getOWLOntolgy();
					} else if (source instanceof SparqlKnowledgeSource) {
						ontology = ((SparqlKnowledgeSource) source)
								.getOWLAPIOntology();
					} else {
						ontology = manager.loadOntologyFromPhysicalURI(url
								.toURI());
					}

					owlAPIOntologies.add(ontology);
					// imports includes the ontology itself
					Set<OWLOntology> imports = manager
							.getImportsClosure(ontology);
					allImports.addAll(imports);
					// System.out.println(imports);
					for (OWLOntology ont : imports) {
						classes.addAll(ont.getReferencedClasses());
						owlObjectProperties.addAll(ont
								.getReferencedObjectProperties());
						owlDatatypeProperties.addAll(ont
								.getReferencedDataProperties());
						owlIndividuals.addAll(ont.getReferencedIndividuals());
					}

					// if several knowledge sources are included, then we can
					// only
					// guarantee that the base URI is from one of those sources
					// (there
					// can't be more than one); but we will take care that all
					// prefixes are
					// correctly imported
					OWLOntologyFormat format = manager
							.getOntologyFormat(ontology);
					if (format instanceof NamespaceOWLOntologyFormat) {
						prefixes.putAll(((NamespaceOWLOntologyFormat) format)
								.getNamespacesByPrefixMap());
						baseURI = prefixes.get("");
						prefixes.remove("");
					}
					
					// read in primitives
					for(OWLClass owlClass : classes)
						atomicConcepts.add(new NamedClass(owlClass.getURI().toString()));
					for(OWLObjectProperty owlProperty : owlObjectProperties)
						atomicRoles.add(new ObjectProperty(owlProperty.getURI().toString()));
					for(OWLDataProperty owlProperty : owlDatatypeProperties) {
						DatatypeProperty dtp = new DatatypeProperty(owlProperty.getURI().toString());
						Set<OWLDataRange> ranges = owlProperty.getRanges(allImports);
						Iterator<OWLDataRange> it = ranges.iterator();
						if(it.hasNext()) {
							OWLDataRange range = it.next();
							if(range.isDataType()) {
								URI uri = ((OWLDataType)range).getURI();
								if(uri.equals(Datatype.BOOLEAN.getURI()))
									booleanDatatypeProperties.add(dtp);
								else if(uri.equals(Datatype.DOUBLE.getURI()))
									doubleDatatypeProperties.add(dtp);
								else if(uri.equals(Datatype.INT.getURI()))
									intDatatypeProperties.add(dtp);				
							}
						}
						datatypeProperties.add(dtp);
					}
					for(OWLIndividual owlIndividual : owlIndividuals) {
						individuals.add(new Individual(owlIndividual.getURI().toString()));
					}		

				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				} catch (URISyntaxException e) {
					e.printStackTrace();
				}
				// all other sources are converted to KB and then to an
				// OWL API ontology
			} else {
				KB kb = source.toKB();
				// System.out.println(kb.toString(null,null));

				URI ontologyURI = URI.create("http://example.com");
				ontology = null;
				try {
					ontology = manager.createOntology(ontologyURI);
				} catch (OWLOntologyCreationException e) {
					e.printStackTrace();
				}
				OWLAPIAxiomConvertVisitor
						.fillOWLOntology(manager, ontology, kb);
				owlAPIOntologies.add(ontology);
				allImports.add(ontology);
				atomicConcepts.addAll(kb.findAllAtomicConcepts());
				atomicRoles.addAll(kb.findAllAtomicRoles());
				individuals.addAll(kb.findAllIndividuals());
				// TODO: add method to find datatypes
			}
		}
		reasoner.loadOntologies(allImports);
		dematerialise();
	}
	
	private void dematerialise(){
		long dematStartTime = System.currentTimeMillis();

		logger.debug("dematerialising concepts");

		for (NamedClass atomicConcept : atomicConcepts) {

			SortedSet<Individual> pos = getIndividualsWithPellet(atomicConcept);
			classInstancesPos.put(atomicConcept, (TreeSet<Individual>) pos);

			if (configurator.getDefaultNegation()) {
				classInstancesNeg.put(atomicConcept, (TreeSet<Individual>) Helper.difference(individuals, pos));
			} else {
				// Pellet needs approximately infinite time to answer
				// negated queries
				// on the carcinogenesis data set (and probably others), so
				// we have to
				// be careful here
				Negation negatedAtomicConcept = new Negation(atomicConcept);
				classInstancesNeg.put(atomicConcept, (TreeSet<Individual>) getIndividuals(negatedAtomicConcept));
			}

		}

		logger.debug("dematerialising object properties");

		for (ObjectProperty atomicRole : atomicRoles) {
			opPos.put(atomicRole, getPropertyMembers(atomicRole));
		}

		logger.debug("dematerialising datatype properties");

		for (DatatypeProperty dp : booleanDatatypeProperties) {
			bdPos.put(dp, (TreeSet<Individual>) getTrueDatatypeMembers(dp));
			bdNeg.put(dp, (TreeSet<Individual>) getFalseDatatypeMembers(dp));
		}

		for (DatatypeProperty dp : intDatatypeProperties) {
			id.put(dp, getIntDatatypeMembers(dp));
		}

		for (DatatypeProperty dp : doubleDatatypeProperties) {
			dd.put(dp, getDoubleDatatypeMembers(dp));
		}

		long dematDuration = System.currentTimeMillis() - dematStartTime;
		logger.debug("TBox dematerialised in " + dematDuration + " ms");
	}
	
	public boolean isConsistent(){
		return reasoner.isConsistent();
	}
	
	public Set<Set<OWLAxiom>> getInconsistencyReasons(){
		PelletExplanation expGen = new PelletExplanation(manager, reasoner.getLoadedOntologies());
		
		return expGen.getInconsistencyExplanations();
	}

	@Override
	public ReasonerType getReasonerType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void releaseKB() {
		reasoner.clearOntologies();
		reasoner.dispose();

	}

	@Override
	public PelletReasonerConfigurator getConfigurator() {
		return configurator;
	}
	
	/**
	 * @return The options of this component.
	 */
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		
		options.add(new BooleanConfigOption("defaultNegation", "Whether to use default negation, i.e. an instance not being in a class means that it is in the negation of the class.", true, false, true));
		
		return options;
	}
	
	public OWLOntologyManager getOWLOntologyManager(){
		return manager;
	}

	@Override
	public void init() throws ComponentInitException {
		// reset variables (otherwise subsequent initialisation with
		// different knowledge sources will merge both)
		atomicConcepts = new TreeSet<NamedClass>(conceptComparator);
		atomicRoles = new TreeSet<ObjectProperty>(roleComparator);
		datatypeProperties = new TreeSet<DatatypeProperty>();
		booleanDatatypeProperties = new TreeSet<DatatypeProperty>();
		doubleDatatypeProperties = new TreeSet<DatatypeProperty>();
		intDatatypeProperties = new TreeSet<DatatypeProperty>();
		individuals = new TreeSet<Individual>();	
				
		// create OWL API ontology manager
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		//set classification output to "none", while default is "console"
		PelletOptions.USE_CLASSIFICATION_MONITOR = PelletOptions.MonitorType.SWING;
		// change log level to WARN for Pellet, because otherwise log
		// output will be very large
		Logger pelletLogger = Logger.getLogger("org.mindswap.pellet");
		pelletLogger.setLevel(Level.WARN);
		reasoner = new Reasoner(manager);
		manager.addOntologyChangeListener(reasoner);

	}
	
	public void classify(){
		reasoner.classify();
	}
	
	public void refresh(){
		reasoner.refresh();
	}
	
	public void addProgressMonitor(ProgressMonitor monitor){
		reasoner.getKB().getTaxonomyBuilder().setProgressMonitor(monitor);
	}

	@Override
	public String getBaseURI() {
		return baseURI;
	}

	@Override
	public SortedSet<Individual> getIndividuals() {
		return individuals;
	}

	@Override
	public Set<NamedClass> getNamedClasses() {
		return Collections.unmodifiableSet(atomicConcepts);
	}

	@Override
	public Set<ObjectProperty> getObjectProperties() {
		return Collections.unmodifiableSet(atomicRoles);
	}

	@Override
	public Map<String, String> getPrefixes() {
		return prefixes;
	}
	
	public Set<Description> getComplementClasses(Description desc){
		OWLDescription owlDesc = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
		Set<Description> complements = new HashSet<Description>();
		for(OWLClass comp : SetUtils.union(reasoner.getDisjointClasses(owlDesc))){
			complements.add(OWLAPIConverter.convertClass(comp));
		}
		for(OWLClass comp : reasoner.getComplementClasses(owlDesc)){
			complements.add(OWLAPIConverter.convertClass(comp));
		}
		return complements;
		
	}
	


	@Override
	public SortedSet<DatatypeProperty> getDatatypePropertiesImpl() {
		return datatypeProperties;
	}
	
	

	
	@Override
	public boolean isSuperClassOfImpl(Description superConcept, Description subConcept) {
		
//			System.out.println("super: " + superConcept + "; sub: " + subConcept);
			return reasoner.isSubClassOf(OWLAPIDescriptionConvertVisitor.getOWLDescription(subConcept), OWLAPIDescriptionConvertVisitor.getOWLDescription(superConcept));			
		
	}
	
	@Override
	protected TreeSet<Description> getSuperClassesImpl(Description concept) {
		Set<Set<OWLClass>> classes = null;
		
		classes = reasoner.getSuperClasses(OWLAPIDescriptionConvertVisitor.getOWLDescription(concept));
		
		return getFirstClasses(classes);
	}
	
	@Override
	protected TreeSet<Description> getSubClassesImpl(Description concept) {
		Set<Set<OWLClass>> classes = null;
		
		classes = reasoner.getSubClasses(OWLAPIDescriptionConvertVisitor.getOWLDescription(concept));
		
		return getFirstClasses(classes);
	}
	
	@Override
	protected TreeSet<ObjectProperty> getSuperPropertiesImpl(ObjectProperty role) {
		Set<Set<OWLObjectProperty>> properties;
		
		properties = reasoner.getSuperProperties(OWLAPIConverter.getOWLAPIObjectProperty(role));
		 	
		return getFirstObjectProperties(properties);
	}
	
	@Override
	protected TreeSet<ObjectProperty> getSubPropertiesImpl(ObjectProperty role) {
		Set<Set<OWLObjectProperty>> properties;
		
		properties = reasoner.getSubProperties(OWLAPIConverter.getOWLAPIObjectProperty(role));
			
		return getFirstObjectProperties(properties);		
	}
	
	@Override
	protected TreeSet<DatatypeProperty> getSuperPropertiesImpl(DatatypeProperty role) {
		Set<Set<OWLDataProperty>> properties;
		
		properties = reasoner.getSuperProperties(OWLAPIConverter.getOWLAPIDataProperty(role));
		 
		return getFirstDatatypeProperties(properties);
	}
	
	@Override
	protected TreeSet<DatatypeProperty> getSubPropertiesImpl(DatatypeProperty role) {
		Set<Set<OWLDataProperty>> properties;
		
		properties = reasoner.getSubProperties(OWLAPIConverter.getOWLAPIDataProperty(role));
			
		return getFirstDatatypeProperties(properties);		
	}	
	
	@Override
	public boolean hasTypeImpl(Description description, Individual individual) throws ReasoningMethodUnsupportedException {
//		OWLDescription d = OWLAPIDescriptionConvertVisitor.getOWLDescription(concept);
//		OWLIndividual i = factory.getOWLIndividual(URI.create(individual.getName()));
//		try {
//			return reasoner.hasType(i,d,false);
//		} catch (OWLReasonerException e) {
//			e.printStackTrace();
//			throw new Error("Instance check error in OWL API.");
//		}
		if (description instanceof NamedClass) {
			return classInstancesPos.get((NamedClass) description).contains(individual);
		} else if (description instanceof Negation) {
			Description child = description.getChild(0);
			if (child instanceof NamedClass) {
				return classInstancesNeg.get((NamedClass) child).contains(individual);
			} else {
				// default negation
				if(configurator.getDefaultNegation()) {
					return !hasTypeImpl(child, individual);
				} else {
					logger.debug("Converting description to negation normal form in fast instance check (should be avoided if possible).");
					Description nnf = ConceptTransformation.transformToNegationNormalForm(child);
					return hasTypeImpl(nnf, individual);					
				}
//				throw new ReasoningMethodUnsupportedException("Instance check for description "
//						+ description
//						+ " unsupported. Description needs to be in negation normal form.");
			}
		} else if (description instanceof Thing) {
			return true;
		} else if (description instanceof Nothing) {
			return false;
		} else if (description instanceof Union) {
			// if the individual is instance of any of the subdescription of
			// the union, we return true
			List<Description> children = description.getChildren();
			for (Description child : children) {
				if (hasTypeImpl(child, individual)) {
					return true;
				}
			}
			return false;
		} else if (description instanceof Intersection) {
			// if the individual is instance of all of the subdescription of
			// the union, we return true
			List<Description> children = description.getChildren();
			for (Description child : children) {
				if (!hasTypeImpl(child, individual)) {
					return false;
				}
			}
			return true;
		} else if (description instanceof ObjectSomeRestriction) {
			ObjectPropertyExpression ope = ((ObjectSomeRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return false;
			}
			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			if (roleFillers == null) {
				return false;
			}
			for (Individual roleFiller : roleFillers) {
				if (hasTypeImpl(child, roleFiller)) {
					return true;
				}
			}
			return false;
		} else if (description instanceof ObjectAllRestriction) {
			ObjectPropertyExpression ope = ((ObjectAllRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return true;
			}
			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			if (roleFillers == null) {
				return true;
			}
			for (Individual roleFiller : roleFillers) {
				if (!hasTypeImpl(child, roleFiller)) {
					return false;
				}
			}
			return true;
		} else if (description instanceof ObjectMinCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return true;
			}

			int number = ((ObjectCardinalityRestriction) description).getNumber();
			int nrOfFillers = 0;

//			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			SortedSet<Individual> roleFillers = mapping.get(individual);
//			System.out.println(roleFillers);
			
			// special case: there are always at least zero fillers
			if (number == 0) {
				return true;
			}
			// return false if there are none or not enough role fillers
			if (roleFillers == null || roleFillers.size() < number) {
				return false;
			}

			int index = 0;
			for (Individual roleFiller : roleFillers) {
				index++;
				if (hasTypeImpl(child, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers == number) {
						return true;
					}
					// early abort: e.g. >= 10 hasStructure.Methyl;
					// if there are 11 fillers and 2 are not Methyl, the result
					// is false
				} else {
					if (roleFillers.size() - index < number) {
						return false;
					}
				}
			}
			return false;
		} else if (description instanceof ObjectMaxCardinalityRestriction) {
			ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
			if (!(ope instanceof ObjectProperty)) {
				throw new ReasoningMethodUnsupportedException("Instance check for description "
						+ description + " unsupported. Inverse object properties not supported.");
			}
			ObjectProperty op = (ObjectProperty) ope;
			Description child = description.getChild(0);
			Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);

			if (mapping == null) {
				logger.warn("Instance check of a description with an undefinied property (" + op
						+ ").");
				return true;
			}

			int number = ((ObjectCardinalityRestriction) description).getNumber();
			int nrOfFillers = 0;

			SortedSet<Individual> roleFillers = opPos.get(op).get(individual);
			// return true if there are none or not enough role fillers
			if (roleFillers == null || roleFillers.size() < number) {
				return true;
			}

			int index = 0;
			for (Individual roleFiller : roleFillers) {
				index++;
				if (hasTypeImpl(child, roleFiller)) {
					nrOfFillers++;
					if (nrOfFillers > number) {
						return false;
					}
					// early abort: e.g. <= 5 hasStructure.Methyl;
					// if there are 6 fillers and 2 are not Methyl, the result
					// is true
				} else {
					if (roleFillers.size() - index <= number) {
						return true;
					}
				}
			}
			return true;
		} else if (description instanceof ObjectValueRestriction) {
			Individual i = ((ObjectValueRestriction)description).getIndividual();
			ObjectProperty op = (ObjectProperty) ((ObjectValueRestriction)description).getRestrictedPropertyExpression();
			
			Set<Individual> inds = opPos.get(op).get(individual);
			return inds == null ? false : inds.contains(i);
		} else if (description instanceof BooleanValueRestriction) {
			DatatypeProperty dp = ((BooleanValueRestriction) description)
					.getRestrictedPropertyExpresssion();
			boolean value = ((BooleanValueRestriction) description).getBooleanValue();

			if (value) {
				// check whether the individual is in the set of individuals
				// mapped
				// to true by this datatype property
				return bdPos.get(dp).contains(individual);
			} else {
				return bdNeg.get(dp).contains(individual);
			}
		} else if (description instanceof DatatypeSomeRestriction) {
			DatatypeSomeRestriction dsr = (DatatypeSomeRestriction) description;
			DatatypeProperty dp = (DatatypeProperty) dsr.getRestrictedPropertyExpression();
			DataRange dr = dsr.getDataRange();
			SortedSet<Double> values = dd.get(dp).get(individual);

			// if there is no filler for this individual and property we
			// need to return false
			if (values == null) {
				return false;
			}

			if (dr instanceof DoubleMaxValue) {
				return (values.first() <= ((DoubleMaxValue) dr).getValue());
			} else if (dr instanceof DoubleMinValue) {
				return (values.last() >= ((DoubleMinValue) dr).getValue());
			}
		}

		throw new ReasoningMethodUnsupportedException("Instance check for description "
				+ description + " unsupported.");
	
	}
	
	private SortedSet<Individual> getIndividualsWithPellet(Description concept){

		OWLDescription d = OWLAPIDescriptionConvertVisitor.getOWLDescription(concept);
		Set<OWLIndividual> individuals = null;
		
		individuals = reasoner.getIndividuals(d, false);
		
		SortedSet<Individual> inds = new TreeSet<Individual>();
		for(OWLIndividual ind : individuals)
			inds.add(new Individual(ind.getURI().toString()));
		return inds;
	}
	
	@Override
	public SortedSet<Individual> getIndividualsImpl(Description concept)  throws ReasoningMethodUnsupportedException{
//
//		OWLDescription d = OWLAPIDescriptionConvertVisitor.getOWLDescription(concept);
//		Set<OWLIndividual> individuals = null;
//		
//		individuals = reasoner.getIndividuals(d, false);
//		
//		SortedSet<Individual> inds = new TreeSet<Individual>();
//		for(OWLIndividual ind : individuals)
//			inds.add(new Individual(ind.getURI().toString()));
//		return inds;
		return getIndividualsImplFast(concept);
	}
	
	public SortedSet<Individual> getIndividualsImplStandard(Description concept)
	throws ReasoningMethodUnsupportedException {
	if (concept instanceof NamedClass) {
 		return classInstancesPos.get((NamedClass) concept);
 	} else if (concept instanceof Negation && concept.getChild(0) instanceof NamedClass) {
 		return classInstancesNeg.get((NamedClass) concept.getChild(0));
 	}
 
 	// return rs.retrieval(concept);
 	SortedSet<Individual> inds = new TreeSet<Individual>();
 	for (Individual i : individuals) {
 		if (hasType(concept, i)) {
 			inds.add(i);
 		}
 	}
	return inds;
}

@SuppressWarnings("unchecked")
public SortedSet<Individual> getIndividualsImplFast(Description description)
		throws ReasoningMethodUnsupportedException {
	// policy: returned sets are clones, i.e. can be modified
	// (of course we only have to clone the leafs of a class description tree)
	if (description instanceof NamedClass) {
		return (TreeSet<Individual>) classInstancesPos.get((NamedClass) description).clone();
	} else if (description instanceof Negation) {
		if(description.getChild(0) instanceof NamedClass) {
			return (TreeSet<Individual>) classInstancesNeg.get((NamedClass) description.getChild(0)).clone();
		}
		// implement retrieval as default negation
		return Helper.difference((TreeSet<Individual>) individuals.clone(), getIndividualsImpl(description.getChild(0)));
	} else if (description instanceof Thing) {
		return (TreeSet<Individual>) individuals.clone();
	} else if (description instanceof Nothing) {
		return new TreeSet<Individual>();
	} else if (description instanceof Union) {
		// copy instances of first element and then subtract all others
		SortedSet<Individual> ret = getIndividualsImpl(description.getChild(0));
		int childNr = 0;
		for(Description child : description.getChildren()) {
			if(childNr != 0) {
				ret.addAll(getIndividualsImpl(child));
			}
			childNr++;
		}
		return ret;
	} else if (description instanceof Intersection) {
		// copy instances of first element and then subtract all others
		SortedSet<Individual> ret = getIndividualsImpl(description.getChild(0));
		int childNr = 0;
		for(Description child : description.getChildren()) {
			if(childNr != 0) {
				ret.retainAll(getIndividualsImpl(child));
			}
			childNr++;
		}
		return ret;
	} else if (description instanceof ObjectSomeRestriction) {
		SortedSet<Individual> targetSet = getIndividualsImpl(description.getChild(0));
		SortedSet<Individual> returnSet = new TreeSet<Individual>();
		
		ObjectPropertyExpression ope = ((ObjectSomeRestriction) description).getRole();
		if (!(ope instanceof ObjectProperty)) {
			throw new ReasoningMethodUnsupportedException("Retrieval for description "
					+ description + " unsupported. Inverse object properties not supported.");
		}
		ObjectProperty op = (ObjectProperty) ope;
		Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);			
		
		// each individual is connected to a set of individuals via the property;
		// we loop through the complete mapping
		for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
			SortedSet<Individual> inds = entry.getValue();
			for(Individual ind : inds) {
				if(targetSet.contains(ind)) {
					returnSet.add(entry.getKey());
					// once we found an individual, we do not need to check the others
					continue; 
				}
			}
		}
		return returnSet;
	} else if (description instanceof ObjectAllRestriction) {
		// \forall restrictions are difficult to handle; assume we want to check
		// \forall hasChild.male with domain(hasChild)=Person; then for all non-persons
		// this is satisfied trivially (all of their non-existing children are male)
//		if(!configurator.getForallRetrievalSemantics().equals("standard")) {
//			throw new Error("Only forallExists semantics currently implemented.");
//		}
		
		// problem: we need to make sure that \neg \exists r.\top \equiv \forall r.\bot
		// can still be reached in an algorithm (\forall r.\bot \equiv \bot under forallExists
		// semantics)
		
		SortedSet<Individual> targetSet = getIndividualsImpl(description.getChild(0));
					
		ObjectPropertyExpression ope = ((ObjectAllRestriction) description).getRole();
		if (!(ope instanceof ObjectProperty)) {
			throw new ReasoningMethodUnsupportedException("Instance check for description "
					+ description + " unsupported. Inverse object properties not supported.");
		}
		ObjectProperty op = (ObjectProperty) ope;
		Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
//		SortedSet<Individual> returnSet = new TreeSet<Individual>(mapping.keySet());
		SortedSet<Individual> returnSet = (SortedSet<Individual>) individuals.clone();
		
		// each individual is connected to a set of individuals via the property;
		// we loop through the complete mapping
		for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
			SortedSet<Individual> inds = entry.getValue();
			for(Individual ind : inds) {
				if(!targetSet.contains(ind)) {
					returnSet.remove(entry.getKey());
					continue; 
				}
			}
		}
		return returnSet;
	} else if (description instanceof ObjectMinCardinalityRestriction) {
		ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
		if (!(ope instanceof ObjectProperty)) {
			throw new ReasoningMethodUnsupportedException("Instance check for description "
					+ description + " unsupported. Inverse object properties not supported.");
		}
		ObjectProperty op = (ObjectProperty) ope;
		Description child = description.getChild(0);
		Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
		SortedSet<Individual> targetSet = getIndividualsImpl(child);
		SortedSet<Individual> returnSet = new TreeSet<Individual>();

		int number = ((ObjectCardinalityRestriction) description).getNumber();			

		for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
			int nrOfFillers = 0;
			int index = 0;
			SortedSet<Individual> inds = entry.getValue();
			
			// we do not need to run tests if there are not sufficiently many fillers
			if(inds.size() < number) {
				continue;
			}
			
			for(Individual ind : inds) {
				// stop inner loop when nr of fillers is reached
				if(nrOfFillers >= number) {
					returnSet.add(entry.getKey());
					break;
				}		
				// early abort when too many instance checks failed
				if (inds.size() - index < number) {
					break;
				}					
				if(targetSet.contains(ind)) {
					nrOfFillers++;
				}
				index++;
			}
		}			
		
		return returnSet;
	} else if (description instanceof ObjectMaxCardinalityRestriction) {
		ObjectPropertyExpression ope = ((ObjectCardinalityRestriction) description).getRole();
		if (!(ope instanceof ObjectProperty)) {
			throw new ReasoningMethodUnsupportedException("Instance check for description "
					+ description + " unsupported. Inverse object properties not supported.");
		}
		ObjectProperty op = (ObjectProperty) ope;
		Description child = description.getChild(0);
		Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);
		SortedSet<Individual> targetSet = getIndividualsImpl(child);
		// initially all individuals are in the return set and we then remove those
		// with too many fillers			
		SortedSet<Individual> returnSet = (SortedSet<Individual>) individuals.clone();

		int number = ((ObjectCardinalityRestriction) description).getNumber();			

		for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
			int nrOfFillers = 0;
			int index = 0;
			SortedSet<Individual> inds = entry.getValue();
			
			// we do not need to run tests if there are not sufficiently many fillers
			if(number < inds.size()) {
				returnSet.add(entry.getKey());
				continue;
			}
			
			for(Individual ind : inds) {
				// stop inner loop when nr of fillers is reached
				if(nrOfFillers >= number) {
					break;
				}		
				// early abort when too many instance are true already
				if (inds.size() - index < number) {
					returnSet.add(entry.getKey());
					break;
				}					
				if(targetSet.contains(ind)) {
					nrOfFillers++;
				}
				index++;
			}
		}			
		
		return returnSet;
	} else if (description instanceof ObjectValueRestriction) {
		Individual i = ((ObjectValueRestriction)description).getIndividual();
		ObjectProperty op = (ObjectProperty) ((ObjectValueRestriction)description).getRestrictedPropertyExpression();
		
		Map<Individual, SortedSet<Individual>> mapping = opPos.get(op);			
		SortedSet<Individual> returnSet = new TreeSet<Individual>();
		
		for(Entry<Individual, SortedSet<Individual>> entry : mapping.entrySet()) {
			if(entry.getValue().contains(i)) {
				returnSet.add(entry.getKey());
			}
		}
		return returnSet;
	} else if (description instanceof BooleanValueRestriction) {
		DatatypeProperty dp = ((BooleanValueRestriction) description)
				.getRestrictedPropertyExpresssion();
		boolean value = ((BooleanValueRestriction) description).getBooleanValue();

		if (value) {
			return (TreeSet<Individual>) bdPos.get(dp).clone();
		} else {
			return (TreeSet<Individual>) bdNeg.get(dp).clone();
		}
	} else if (description instanceof DatatypeSomeRestriction) {
		DatatypeSomeRestriction dsr = (DatatypeSomeRestriction) description;
		DatatypeProperty dp = (DatatypeProperty) dsr.getRestrictedPropertyExpression();
		DataRange dr = dsr.getDataRange();

		Map<Individual, SortedSet<Double>> mapping = dd.get(dp);			
		SortedSet<Individual> returnSet = new TreeSet<Individual>();			

		if (dr instanceof DoubleMaxValue) {
			for(Entry<Individual, SortedSet<Double>> entry : mapping.entrySet()) {
				if(entry.getValue().first() <= ((DoubleMaxValue)dr).getValue()) {
					returnSet.add(entry.getKey());
				}
			}				
		} else if (dr instanceof DoubleMinValue) {
			for(Entry<Individual, SortedSet<Double>> entry : mapping.entrySet()) {
				if(entry.getValue().last() >= ((DoubleMinValue)dr).getValue()) {
					returnSet.add(entry.getKey());
				}
			}
		}
	}
		
	throw new ReasoningMethodUnsupportedException("Retrieval for description "
				+ description + " unsupported.");		
		
	// return rs.retrieval(concept);
//	SortedSet<Individual> inds = new TreeSet<Individual>();
//	for (Individual i : individuals) {
//		if (hasType(concept, i)) {
//			inds.add(i);
//		}
//	}
//	return inds;
}
	
	@Override
	public Set<NamedClass> getTypesImpl(Individual individual) {
		Set<Set<OWLClass>> result = null;
		
		result = reasoner.getTypes(factory.getOWLIndividual(URI.create(individual.getName())),false);
		
		return getFirstClassesNoTopBottom(result);
	}
	
	@Override
	public boolean isSatisfiableImpl() {
		
		return reasoner.isSatisfiable(factory.getOWLThing());
		
	}
	
	@Override
	public Description getDomainImpl(ObjectProperty objectProperty) {
		OWLObjectProperty prop = OWLAPIConverter.getOWLAPIObjectProperty(objectProperty);
		
			// Pellet returns a set of sets of named class, which are more
			// general than the actual domain/range
			Set<Set<OWLDescription>> set = reasoner.getDomains(prop);
			return getDescriptionFromReturnedDomain(set);
		
	}
	
	@Override
	public Description getDomainImpl(DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = OWLAPIConverter
				.getOWLAPIDataProperty(datatypeProperty);

		Set<Set<OWLDescription>> set = reasoner.getDomains(prop);
		return getDescriptionFromReturnedDomain(set);

	}
	
	@Override
	public Description getRangeImpl(ObjectProperty objectProperty) {
		OWLObjectProperty prop = OWLAPIConverter
				.getOWLAPIObjectProperty(objectProperty);

		Set<OWLDescription> set = reasoner.getRanges(prop);
		if (set.size() == 0)
			return new Thing();
		OWLClass oc = (OWLClass) set.iterator().next();
		return new NamedClass(oc.getURI().toString());

	}
	
	private Description getDescriptionFromReturnedDomain(Set<Set<OWLDescription>> set) {
		if(set.size()==0)
			return new Thing();
		
		Set<OWLDescription> union = new HashSet<OWLDescription>();
		Set<OWLDescription> domains = new HashSet<OWLDescription>();
		
		for(Set<OWLDescription> descs : set){
			for(OWLDescription desc : descs){
				union.add(desc);
			}
		}
		for(OWLDescription desc : union){
			boolean isSuperClass = false;
			for(Description d : getClassHierarchy().getSubClasses(OWLAPIConverter.convertClass(desc.asOWLClass()))){
				if(union.contains(OWLAPIConverter.getOWLAPIDescription(d))){
					isSuperClass = true;
					break;
				}
			}
			if(!isSuperClass){
				domains.add(desc);
			}
		}
		
		OWLClass oc = (OWLClass) domains.iterator().next();
		String str = oc.getURI().toString();
		if(str.equals("http://www.w3.org/2002/07/owl#Thing")) {
			return new Thing();
		} else {
			return new NamedClass(str);
		}					
	}
	
	@Override
	public Map<Individual, SortedSet<Individual>> getPropertyMembersImpl(ObjectProperty atomicRole) {
		OWLObjectProperty prop = OWLAPIConverter.getOWLAPIObjectProperty(atomicRole);
		Map<Individual, SortedSet<Individual>> map = new TreeMap<Individual, SortedSet<Individual>>();
		for(Individual i : individuals) {
			OWLIndividual ind = factory.getOWLIndividual(URI.create(i.getName()));
			
			// get all related individuals via OWL API
			Set<OWLIndividual> inds = null;
			
			inds = reasoner.getRelatedIndividuals(ind, prop);
			
			
			// convert data back to DL-Learner structures
			SortedSet<Individual> is = new TreeSet<Individual>();
			for(OWLIndividual oi : inds)
				is.add(new Individual(oi.getURI().toString()));
			map.put(i, is);
		}
		return map;
	}
	
	@Override
	protected Map<ObjectProperty,Set<Individual>> getObjectPropertyRelationshipsImpl(Individual individual) {
		OWLIndividual ind = factory.getOWLIndividual(URI.create(individual.getName()));
		Map<OWLObjectProperty,Set<OWLIndividual>> mapAPI = null;
		
		mapAPI = reasoner.getObjectPropertyRelationships(ind);
		
		Map<ObjectProperty,Set<Individual>> map = new TreeMap<ObjectProperty, Set<Individual>>();
		for(Entry<OWLObjectProperty,Set<OWLIndividual>> entry : mapAPI.entrySet()) {
			ObjectProperty prop = OWLAPIConverter.convertObjectProperty(entry.getKey());
			Set<Individual> inds = OWLAPIConverter.convertIndividuals(entry.getValue());
			map.put(prop, inds);
		}
		return map;
	}
	
	@Override
	public Set<Individual> getRelatedIndividualsImpl(Individual individual, ObjectProperty objectProperty) {
		OWLIndividual ind = factory.getOWLIndividual(URI.create(individual.getName()));
		OWLObjectProperty prop = OWLAPIConverter.getOWLAPIObjectProperty(objectProperty);
		Set<OWLIndividual> inds = null;
		
		inds = reasoner.getRelatedIndividuals(ind, prop);
		
		// convert data back to DL-Learner structures
		SortedSet<Individual> is = new TreeSet<Individual>();
		for(OWLIndividual oi : inds) {
			is.add(new Individual(oi.getURI().toString()));
		}
		return is;
	}
	
	@Override
	public Set<Constant> getRelatedValuesImpl(Individual individual, DatatypeProperty datatypeProperty) {
		OWLIndividual ind = factory.getOWLIndividual(URI.create(individual.getName()));
		OWLDataProperty prop = OWLAPIConverter.getOWLAPIDataProperty(datatypeProperty);
		Set<OWLConstant> constants = null;
	
		constants = reasoner.getRelatedValues(ind, prop);
		
		return OWLAPIConverter.convertConstants(constants);	
	}	
	
	public Map<Individual, SortedSet<Double>> getDoubleValues(
			DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = OWLAPIConverter
				.getOWLAPIDataProperty(datatypeProperty);
		Map<Individual, SortedSet<Double>> map = new TreeMap<Individual, SortedSet<Double>>();
		for (Individual i : individuals) {
			OWLIndividual ind = factory.getOWLIndividual(URI
					.create(i.getName()));

			// get all related individuals via OWL API
			Set<OWLConstant> inds = null;

			inds = reasoner.getRelatedValues(ind, prop);

			// convert data back to DL-Learner structures
			SortedSet<Double> is = new TreeSet<Double>();
			for (OWLConstant oi : inds) {
				Double d = Double.parseDouble(oi.getLiteral());
				is.add(d);
			}
			map.put(i, is);
		}
		return map;
	}	
	
	@Override
	public Map<Individual, SortedSet<Constant>> getDatatypeMembersImpl(DatatypeProperty datatypeProperty) {
		OWLDataProperty prop = OWLAPIConverter.getOWLAPIDataProperty(datatypeProperty);
		Map<Individual, SortedSet<Constant>> map = new TreeMap<Individual, SortedSet<Constant>>();
		for(Individual i : individuals) {
			OWLIndividual ind = factory.getOWLIndividual(URI.create(i.getName()));
			
			// get all related values via OWL API
			Set<OWLConstant> constants = null;
			
			constants = reasoner.getRelatedValues(ind, prop);
			
			
			// convert data back to DL-Learner structures
			SortedSet<Constant> is = new TreeSet<Constant>();
			for(OWLConstant oi : constants) {
				// for typed constants we have to figure out the correct
				// data type and value
				if(oi instanceof OWLTypedConstant) {
					Datatype dt = OWLAPIConverter.convertDatatype(((OWLTypedConstant)oi).getDataType());
					is.add(new TypedConstant(oi.getLiteral(),dt));
				// for untyped constants we have to figure out the value
				// and language tag (if any)
				} else {
					OWLUntypedConstant ouc = (OWLUntypedConstant) oi;
					if(ouc.hasLang())
						is.add(new UntypedConstant(ouc.getLiteral(), ouc.getLang()));
					else
						is.add(new UntypedConstant(ouc.getLiteral()));
				}
			}	
			// only add individuals using the datatype property
			if(is.size()>0)
				map.put(i, is);
		}
		return map;
	}
	
	// OWL API often returns a set of sets of classes, where each inner
	// set consists of equivalent classes; this method picks one class
	// from each inner set to flatten the set of sets
	private TreeSet<Description> getFirstClasses(Set<Set<OWLClass>> setOfSets) {
		TreeSet<Description> concepts = new TreeSet<Description>(conceptComparator);
		for(Set<OWLClass> innerSet : setOfSets) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = innerSet.iterator().next();
			if(concept.isOWLThing()) {
				concepts.add(new Thing());
			} else if(concept.isOWLNothing()) {
				concepts.add(new Nothing());
			} else {
				concepts.add(new NamedClass(concept.getURI().toString()));
			}
		}
		return concepts;		
	}
	
	private Set<NamedClass> getFirstClassesNoTopBottom(Set<Set<OWLClass>> setOfSets) {
		Set<NamedClass> concepts = new HashSet<NamedClass>();
		for(Set<OWLClass> innerSet : setOfSets) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLClass concept = innerSet.iterator().next();
			if(!concept.isOWLThing() && !concept.isOWLNothing())
				concepts.add(new NamedClass(concept.getURI().toString()));
		}
		return concepts;			
	}
	
	private TreeSet<ObjectProperty> getFirstObjectProperties(Set<Set<OWLObjectProperty>> setOfSets) {
		TreeSet<ObjectProperty> roles = new TreeSet<ObjectProperty>(roleComparator);
		for(Set<OWLObjectProperty> innerSet : setOfSets) {
			// take one element from the set and ignore the rest
			// (TODO: we need to make sure we always ignore the same concepts)
			OWLObjectProperty property = innerSet.iterator().next();
			roles.add(new ObjectProperty(property.getURI().toString()));
		}
		return roles;		
	}	
	
	private TreeSet<DatatypeProperty> getFirstDatatypeProperties(Set<Set<OWLDataProperty>> setOfSets) {
		TreeSet<DatatypeProperty> roles = new TreeSet<DatatypeProperty>(roleComparator);
		for(Set<OWLDataProperty> innerSet : setOfSets) {
			OWLDataProperty property = innerSet.iterator().next();
			roles.add(new DatatypeProperty(property.getURI().toString()));
		}
		return roles;		
	}		
	
	@SuppressWarnings({"unused"})
	private Set<Description> owlClassesToAtomicConcepts(Set<OWLClass> owlClasses) {
		Set<Description> concepts = new HashSet<Description>();
		for(OWLClass owlClass : owlClasses)
			concepts.add(OWLAPIConverter.convertClass(owlClass));
		return concepts;
	}
	
	public static void exportKBToOWL(File owlOutputFile, KB kb, URI ontologyURI) {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		//URI ontologyURI = URI.create("http://example.com");
		URI physicalURI = owlOutputFile.toURI();
		SimpleURIMapper mapper = new SimpleURIMapper(ontologyURI, physicalURI);
		manager.addURIMapper(mapper);
		OWLOntology ontology;
		try {
			ontology = manager.createOntology(ontologyURI);
			// OWLAPIReasoner.fillOWLAPIOntology(manager, ontology, kb);
			OWLAPIAxiomConvertVisitor.fillOWLOntology(manager, ontology, kb);
			manager.saveOntology(ontology);
		} catch (OWLOntologyCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}	
	
	/**
	 * Test 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		String uri = "http://www.co-ode.org/ontologies/pizza/2007/02/12/pizza.owl";
		
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try {
			manager.loadOntologyFromPhysicalURI(URI.create(uri));
			new org.mindswap.pellet.owlapi.Reasoner(manager);
			System.out.println("Reasoner loaded succesfully.");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the booleanDatatypeProperties
	 */
	@Override
	public SortedSet<DatatypeProperty> getBooleanDatatypePropertiesImpl() {
		return booleanDatatypeProperties;
	}

	/**
	 * @return the doubleDatatypeProperties
	 */
	@Override
	public SortedSet<DatatypeProperty> getDoubleDatatypePropertiesImpl() {
		return doubleDatatypeProperties;
	}

	/**
	 * @return the intDatatypeProperties
	 */
	@Override
	public SortedSet<DatatypeProperty> getIntDatatypePropertiesImpl() {
		return intDatatypeProperties;
	}


	public OWLOntology getOWLAPIOntologies() {
		return reasoner.getLoadedOntologies().iterator().next();
	}
	
	/*public void setReasonerType(String type){
		configurator.setReasonerType(type);
	}*/

//	@Override
//	public boolean hasDatatypeSupport() {
//		return true;
//	}
	
	@Override
	public Set<NamedClass> getInconsistentClassesImpl() {
		Set<NamedClass> concepts = new HashSet<NamedClass>();

		for (OWLClass concept : reasoner.getInconsistentClasses()){
			concepts.add(new NamedClass(concept.getURI().toString()));
		}

		return concepts;
	}
	
	
	public Set<OWLClass> getInconsistentOWLClasses() {
		return reasoner.getInconsistentClasses();
	}

	@Override
	@SuppressWarnings("unchecked")
	public Set<Constant> getLabelImpl(Entity entity) {
		OWLEntity owlEntity = OWLAPIConverter.getOWLAPIEntity(entity);
		Set<OWLAnnotation> labelAnnotations = owlEntity.getAnnotations(owlAPIOntologies.get(0), URI.create("http://www.w3.org/2000/01/rdf-schema#label"));
		Set<Constant> annotations = new HashSet<Constant>();
		for(OWLAnnotation label : labelAnnotations) {
			OWLConstant c =  ((OWLLabelAnnotation)label).getAnnotationValue();
			annotations.add(OWLAPIConverter.convertConstant(c));
		}
		return annotations;
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.BaseReasoner#remainsSatisfiable(org.dllearner.core.owl.Axiom)
	 */
	@Override
	public boolean remainsSatisfiableImpl(Axiom axiom) {
		boolean consistent = true;
		OWLAxiom axiomOWLAPI = OWLAPIAxiomConvertVisitor.convertAxiom(axiom);
		
		try {
			manager.applyChange(new AddAxiom(ontology, axiomOWLAPI));
		} catch (OWLOntologyChangeException e1) {
			e1.printStackTrace();
		}
		
		consistent = reasoner.isConsistent(ontology);

		
		try {
			manager.applyChange(new RemoveAxiom(ontology, axiomOWLAPI));
		} catch (OWLOntologyChangeException e) {
			e.printStackTrace();
		}
		
		return consistent;
	}

	public Reasoner getReasoner() {
		return reasoner;
	}
	

}
