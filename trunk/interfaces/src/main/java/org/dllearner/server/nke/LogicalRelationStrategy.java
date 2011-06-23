package org.dllearner.server.nke;

import com.clarkparsia.modularity.IncrementalClassifier;
import com.clarkparsia.modularity.PelletIncremantalReasonerFactory;
import org.apache.log4j.Logger;
import org.dllearner.core.owl.Axiom;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.EquivalentClassesAxiom;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.parser.KBParser;
import org.dllearner.parser.ParseException;
import org.dllearner.utilities.owl.OWLAPIAxiomConvertVisitor;
import org.json.simple.JSONObject;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.model.*;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class LogicalRelationStrategy {
    private static final Logger logger = Logger.getLogger(LogicalRelationStrategy.class);
    private final String ontologyIRI;
    OWLOntologyManager manager;
    OWLOntology ontology;
    private OWLDataFactory factory;
    IncrementalClassifier incReasoner;
    private String tmpClassName = "http://somethingsomething.darkside/TempClass0815sdlkjdfjdslkfdsjklffdslkjj";

    //Map<OWLClass, Integer> classPopularity = new HashMap<OWLClass, Integer>();

    OWLAnnotationProperty popularityAnnotationProperty;
    OWLAnnotationProperty kbSyntaxAnnotationProperty;
    OWLAnnotationProperty retrievalIdAnnotationProperty;
    OWLAnnotationProperty labelAnnotationProperty;

    public LogicalRelationStrategy(String ontologyIRI) {
        this.ontologyIRI = ontologyIRI;
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();

        try {
            ontology = manager.loadOntology(IRI.create(ontologyIRI));
        } catch (OWLOntologyCreationException e) {
            logger.error("Could not load or create ontology, using geizhals ontology", e);
            InputStream is = this.getClass().getClassLoader().getResourceAsStream("nke/geizhals.owl");
            try {
                ontology = manager.loadOntologyFromOntologyDocument(is);
            } catch (OWLOntologyCreationException e2) {
                logger.error("Could not load geizhals ontology", e2);
            }

        }

        popularityAnnotationProperty = factory.getOWLAnnotationProperty(IRI.create(Geizhals2OWL.prefixSave + "popularity"));
        kbSyntaxAnnotationProperty = factory.getOWLAnnotationProperty(IRI.create(Geizhals2OWL.prefixSave + "kbsyntax"));
        retrievalIdAnnotationProperty = factory.getOWLAnnotationProperty(IRI.create(Geizhals2OWL.prefixSave + "retrievalid"));
        labelAnnotationProperty = factory.getOWLAnnotationProperty(IRI.create(Geizhals2OWL.prefixSave + "label"));

        incReasoner = PelletIncremantalReasonerFactory.getInstance().createNonBufferingReasoner(ontology);
        incReasoner.prepareReasoner();

        /*for (OWLClass oc : ontology.getClassesInSignature()) {
            if (oc.getIRI().toString().startsWith(Geizhals2OWL.prefixSave)) {
                classPopularity.put(oc, getPopularity(oc));
            }
        }*/

        logger.debug("Successfully loaded ontology " + ontologyIRI + " NrOfAxioms: " + ontology.getAxiomCount());

    }

    public synchronized List<Concept> getMostPopular(int limit) {
        return toConcept(ontology.getClassesInSignature(), limit);
    }

    public synchronized void increasePopularity(String classUri, String kbSyntax, String retrievalId, String label) {

        //check if this class exists and increase pop
        if (ontology.containsClassInSignature(IRI.create(classUri))) {
            int pop = getPopularity(classUri);
            setPopularity(classUri, pop + 1);

            //if not create the class and set pop to 1
        } else {
            OWLAxiom target = null;
            try {
                Description d = KBParser.parseConcept(kbSyntax);
                Axiom a = new EquivalentClassesAxiom(new NamedClass(classUri), d);
                target = OWLAPIAxiomConvertVisitor.convertAxiom(a);
                manager.applyChange(new AddAxiom(ontology, target));

                setPopularity(classUri, 1);
                OWLClass oc = factory.getOWLClass(IRI.create(classUri));

                //kbsyntax
                OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(oc.getIRI(), factory.getOWLAnnotation(kbSyntaxAnnotationProperty, factory.getOWLLiteral(kbSyntax)));
                manager.applyChange(new AddAxiom(ontology, ax));

                //retrievalid
                ax = factory.getOWLAnnotationAssertionAxiom(oc.getIRI(), factory.getOWLAnnotation(retrievalIdAnnotationProperty, factory.getOWLLiteral(retrievalId)));
                manager.applyChange(new AddAxiom(ontology, ax));

                //label
                ax = factory.getOWLAnnotationAssertionAxiom(oc.getIRI(), factory.getOWLAnnotation(labelAnnotationProperty, factory.getOWLLiteral(label)));
                manager.applyChange(new AddAxiom(ontology, ax));

                logger.debug("Added class " + classUri + " == " + kbSyntax);
                incReasoner.prepareReasoner();

            } catch (ParseException e) {
                logger.error(kbSyntax + " " + classUri, e);
                throw new RuntimeException(e);

            } catch (Exception e) {
                logger.error(kbSyntax + " " + classUri, e);
                throw new RuntimeException(e);

            } catch (Error e) {
                logger.error(kbSyntax + " " + classUri, e);
                throw new RuntimeException(e);
            }

        }
        //save
        save();

    }

    private void setPopularity(String classIRI, int pop) {
        OWLClass oc = factory.getOWLClass(IRI.create(classIRI));
        //delete the previous annotation
        for (OWLAnnotation annotation : oc.getAnnotations(ontology, popularityAnnotationProperty)) {
            OWLAnnotationAssertionAxiom oaaa = factory.getOWLAnnotationAssertionAxiom(oc.getIRI(), annotation);
            RemoveAxiom ra = new RemoveAxiom(ontology, oaaa);
            manager.applyChange(ra);
        }
        //insert the new one
        OWLAnnotation commentAnno = factory.getOWLAnnotation(popularityAnnotationProperty, factory.getOWLLiteral(pop));
        OWLAxiom ax = factory.getOWLAnnotationAssertionAxiom(oc.getIRI(), commentAnno);
        manager.applyChange(new AddAxiom(ontology, ax));
        // classPopularity.put(oc, pop);
    }

    private int getPopularity(String classIRI) {
        OWLClass oc = factory.getOWLClass(IRI.create(classIRI));
        return getPopularity(oc);
    }

    private int getPopularity(OWLClass oc) {
        for (OWLAnnotation oa : oc.getAnnotations(ontology, popularityAnnotationProperty)) {
            //there should only be one
            return ((OWLLiteral) oa.getValue()).parseInteger();
        }
        logger.error("no popularity found for saved class: " + oc.getIRI());
        return 0;
    }


    private String get(OWLClass oc, OWLAnnotationProperty oap) {
        for (OWLAnnotation oa : oc.getAnnotations(ontology, oap)) {
            //there should only be one
            return ((OWLLiteral) oa.getValue()).getLiteral();
        }
        logger.error("no " + oap + " found for saved class: " + oc.getIRI());
        return "";
    }


    private synchronized void save() {
        try {
            manager.saveOntology(ontology, new OWLXMLOntologyFormat(), IRI.create(ontologyIRI));
            logger.info("saved ontology: " + ontologyIRI);
        } catch (OWLOntologyStorageException e) {
            logger.error("", e);
        }
    }

    private void removeClass(OWLAxiom target) {
        int before = ontology.getAxiomCount();
        manager.applyChange(new RemoveAxiom(ontology, target));
        incReasoner.prepareReasoner();
        int after = ontology.getAxiomCount();
        if (before - 1 != after) {
            logger.warn("Problem with removing axiom: Count before was: " + before + " after was: " + after + " Axiom:" + target);
        }

    }


    private OWLAxiom addClass(Description d, String classUri) {
        OWLAxiom target = null;
        try {
            Axiom a = new EquivalentClassesAxiom(new NamedClass(classUri), d);
            target = OWLAPIAxiomConvertVisitor.convertAxiom(a);

            manager.applyChange(new AddAxiom(ontology, target));

            logger.debug("Added axiom " + classUri + " == " + d.toKBSyntaxString());
            //XXX this seems unnecessary
            // incReasoner.prepareReasoner();
            // incReasoner.isConsistent();

        } catch (Exception e) {
            logger.error(d.toKBSyntaxString() + " " + classUri, e);
            throw new RuntimeException(e);

        }

        return target;

    }


    public synchronized List<Concept> getRelatedConcepts(EvaluatedDescriptionPosNeg ed) {
        logger.debug("Getting related concepts for: " + ed.getDescription().toKBSyntaxString());
        List<Concept> l = new ArrayList<Concept>();
        OWLAxiom target = addClass(ed.getDescription(), tmpClassName);
        try {

            OWLClass newCl = factory.getOWLClass(IRI.create(tmpClassName));

            Set<OWLClass> superClasses = incReasoner.getSuperClasses(newCl, true).getFlattened();
            Set<OWLClass> subClasses = incReasoner.getSubClasses(newCl, true).getFlattened();
            Set<OWLClass> parallelClasses = new TreeSet<OWLClass>();

            for (OWLClass clazz : superClasses) {
                parallelClasses.addAll(incReasoner.getSubClasses(clazz, true).getFlattened());
            }

            //remove the class itself
            parallelClasses.remove(newCl);

            l.addAll(toConcept(superClasses, 2));
            l.addAll(toConcept(parallelClasses, 2));
            l.addAll(toConcept(subClasses, 2));

            logger.debug("Found related " + l.size() + " " + l);
            logger.debug("Found SUPER " + superClasses.size() + " " + superClasses);
            logger.debug("Found SUB " + subClasses.size() + " " + subClasses);
            logger.debug("Found PARALLEL " + parallelClasses.size() + " " + parallelClasses);

            return l;

        } finally {

            // remove axiom again
            // (otherwise internal DL-Learner state would be corrupt!)
            removeClass(target);
        }

    }

    public List<Concept> toConcept(Set<OWLClass> classes, int limit) {
        TreeSet<Concept> s = new TreeSet<Concept>();
        for (OWLClass oc : classes) {
            if (oc.getIRI().toString().startsWith(Geizhals2OWL.prefixSave)) {
                s.add(new Concept(oc.getIRI().toString()));
            }
        }
        List<Concept> l = new ArrayList<Concept>();
        for (Concept c : s.descendingSet()) {
            l.add(c);
            if (l.size() >= limit) {
                break;
            }
        }
        return l;
    }

    public class Concept implements Comparable<Concept> {
        final String classIRI;
        final public int popularity;
        public final String retrievalLink;
        public final String label;
        public final String kbSyntax;


        public Concept(String classIRI) {
            this.classIRI = classIRI;
            OWLClass oc = factory.getOWLClass(IRI.create(classIRI));
            popularity = getPopularity(classIRI);
            retrievalLink = get(oc, retrievalIdAnnotationProperty);
            label = get(oc, labelAnnotationProperty);
            kbSyntax = get(oc, kbSyntaxAnnotationProperty);
        }

        public JSONObject getJSON() {
            JSONObject j = new JSONObject();
            j.put("uri", classIRI);
            j.put("link", retrievalLink);
            j.put("label", label);
            j.put("kbsyntax", kbSyntax);
            j.put("popularity", popularity);
            return j;
        }

        @Override
        public int compareTo(Concept o) {
            if (o.popularity != this.popularity) {
                return this.popularity - o.popularity;
            }
            return o.retrievalLink.compareTo(this.retrievalLink);
        }

        @Override
        public String toString() {
            return "Concept{" + "classIRI='" + classIRI + '\'' + ", popularity=" + popularity + ", retrievalLink='" + retrievalLink + '\'' + ", label='" + label + '\'' + ", kbSyntax='" + kbSyntax + '\'' + '}';
        }
    }

    //

    /* private MapGuard mapGuard = new MapGuard();

   private class MapGuard {
       private Map<String, StoredConceptDTO> uriToStoredConcepts = new HashMap<String, StoredConceptDTO>();

       public StoredConceptDTO put(StoredConceptDTO value) {
           StoredConceptDTO s = uriToStoredConcepts.put(value.getUri(), value);
           if (s != null) {
               logger.warn("overwrote concept " + s.getLabel() + " " + s.getKbSyntaxString());
           }
           return s;
       }

       public StoredConceptDTO get(String key) {
           return uriToStoredConcepts.get(key);
       }
   }



   OWLOntologyManager manager;
   OWLOntology ontology;
   IncrementalClassifier incReasoner;
    */

    /*

    private StrategyDTO getStrategy(String title, Set<OWLClass> owlClasses) {
      //  StrategyDTO s = new StrategyDTO();
        s.setTitle(title);

        for (OWLClass owlClass : owlClasses) {
            StoredConceptDTO stored = mapGuard.get(owlClass.getIRI().toString());
            // logger.debug(owlClass + "" + stored);
            // TODO untested
            if (stored != null) {
                s.addConcept(stored);
            } else {
                ConceptDTO c = new ConceptDTO("(\"" + owlClass.getIRI().toString() + "\")", owlClass.getIRI().toString());
                s.addConcept(c);
            }
        }
        logger.error("made Strategy " + s);
        return s;
    }


    public void update(StoredConceptDTO learningResultDTO) throws ConceptSavingFailedException {
        check();
        try {
            mapGuard.put(learningResultDTO);
            addClass(learningResultDTO.getKbSyntaxString(), learningResultDTO.getUri());
        } catch (Exception e) {
            String msg = "updating internal reasoner failed: " + learningResultDTO.toDebugString();
            logger.error(msg, e);
            throw new ConceptSavingFailedException(msg, e);
        }

    }

    public void update(List<StoredConceptDTO> learningResultDTOs) throws ConceptSavingFailedException {
        for (StoredConceptDTO learningResultDTO : learningResultDTOs) {
            update(learningResultDTO);
        }
    }*/

}
