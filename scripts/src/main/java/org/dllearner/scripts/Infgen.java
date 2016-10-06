package org.dllearner.scripts;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.dllearner.utilities.OwlApiJenaUtils;
import org.mindswap.pellet.jena.PelletReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.TurtleDocumentFormat;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Infgen {
	public static Model getModel(final OWLOntology ontology) {
		Model model = ModelFactory.createDefaultModel();

		try (PipedInputStream is = new PipedInputStream(); PipedOutputStream os = new PipedOutputStream(is)) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						ontology.getOWLOntologyManager().saveOntology(ontology, new TurtleDocumentFormat(), os);
						os.close();
					} catch (OWLOntologyStorageException | IOException e) {
						e.printStackTrace();
					}
				}
			}).start();
			model.read(is, null, "TURTLE");
			return model;
		} catch (Exception e) {
			throw new RuntimeException("Could not convert OWL API ontology to JENA API model.", e);
		}
	}
	
	

//	private static void reasonWithPellet2(String in, String out) throws FileNotFoundException {
//		OntModel model = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );
//		model.read("file:///"+in);
//		System.out.println("rwp2; size:"+model.size());
//		model.write(new FileOutputStream(out));
//	}
	private static void reasonWithPellet(String in, String out) throws FileNotFoundException {
		//OntModel model = ModelFactory.createOntologyModel( PelletReasonerFactory.THE_SPEC );
		Model model = ModelFactory.createDefaultModel();
		model.read("file:///"+in);
		System.out.println("rwp; size:"+model.size());
//		InfModel inf = ModelFactory.createInfModel(PelletReasonerFactory.theInstance().create(), model);
//		inf.write(new FileOutputStream(out));
	}
	
	private static void reasonWithHermit(String in, String out, boolean copy) throws IOException, OWLOntologyStorageException, OWLOntologyCreationException {
        OWLOntologyManager manager=OWLManager.createOWLOntologyManager();
        File inputOntologyFile = new File(in);
        OWLOntology ontology=manager.loadOntologyFromOntologyDocument(inputOntologyFile);
        org.semanticweb.HermiT.ReasonerFactory factory
        = new org.semanticweb.HermiT.ReasonerFactory();
        // The factory can now be used to obtain an instance of HermiT as an OWLReasoner.
        org.semanticweb.HermiT.Configuration c
        = new org.semanticweb.HermiT.Configuration();
        OWLReasoner reasoner=factory.createReasoner(ontology, c);
        List<InferredAxiomGenerator<? extends OWLAxiom>> generators= new ArrayList<>();
        generators.add(new InferredSubClassAxiomGenerator());
        generators.add(new InferredClassAssertionAxiomGenerator());
        generators.add(new InferredDisjointClassesAxiomGenerator() {
            boolean precomputed=false;
            @Override
			protected void addAxioms(OWLClass entity, OWLReasoner reasoner, OWLDataFactory dataFactory, Set<OWLDisjointClassesAxiom> result) {
                if (!precomputed) {
                    reasoner.precomputeInferences(org.semanticweb.owlapi.reasoner.InferenceType.DISJOINT_CLASSES);
                    precomputed=true;
                }
                for (OWLClass cls : reasoner.getDisjointClasses(entity).getFlattened()) {
                    result.add(dataFactory.getOWLDisjointClassesAxiom(entity, cls));
                }
            }
        });
        generators.add(new InferredDataPropertyCharacteristicAxiomGenerator());
        generators.add(new InferredEquivalentClassAxiomGenerator());
        generators.add(new InferredEquivalentDataPropertiesAxiomGenerator());
        generators.add(new InferredEquivalentObjectPropertyAxiomGenerator());
        generators.add(new InferredInverseObjectPropertiesAxiomGenerator());
        generators.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
        generators.add(new InferredPropertyAssertionGenerator());
        generators.add(new InferredSubDataPropertyAxiomGenerator());
        generators.add(new InferredSubObjectPropertyAxiomGenerator());

        InferredOntologyGenerator iog=new InferredOntologyGenerator(reasoner,generators);
        OWLOntology inferredAxiomsOntology=manager.createOntology();
        iog.fillOntology(manager.getOWLDataFactory(), inferredAxiomsOntology);
        if (copy) {
        	manager.addAxioms(inferredAxiomsOntology, ontology.getAxioms());
        	Model m1 = OwlApiJenaUtils.getModel(inferredAxiomsOntology);
        	Model m0 = RDFDataMgr.loadModel("file://"+in);
        		m0.add(m1.listStatements());
        		m0.write(new FileOutputStream(out));
        } else {
        File inferredOntologyFile=new File(out);
        if (!inferredOntologyFile.exists())
            inferredOntologyFile.createNewFile();
        inferredOntologyFile=inferredOntologyFile.getAbsoluteFile();
        OutputStream outputStream=new FileOutputStream(inferredOntologyFile);
        manager.saveOntology(inferredAxiomsOntology, manager.getOntologyFormat(ontology), outputStream);
        }
        System.out.println("The ontology in "+out+" should now contain all inferred axioms ");
	}

	private static void loadThroughJena(String in, String out) throws OWLOntologyCreationException, FileNotFoundException {
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntologyFromOntologyDocument(new File(in));
		Model model = OwlApiJenaUtils.getModel(ontology);
		System.out.println("ltj; size:"+model.size());
		model.write(new FileOutputStream(out));
	}

	private static void loadThroughJena2(String in, String out) throws FileNotFoundException {
		
		Model model = RDFDataMgr.loadModel("file://"+in);
		System.out.println("ltj; size:"+model.size());
		model.write(new FileOutputStream(out));
	}

	public static void main(String[] args) throws Exception {
		String in = args.length > 0 ? args[0] : "../examples/carcinogenesis/carcinogenesis.owl";
		if (args.length <= 1) {
			loadThroughJena(in, in+".jena1");
			reasonWithHermit(in, in+".her0", false);
		}
		else {
			for (int i = 0; i < args[1].length(); ++i) {
				String step = in;
				String next = null;
				switch (args[1].charAt(i)) {
				case 'h':
					next = in + "." + i + "." + "her";
					reasonWithHermit(step, next, false);
					break;
				case 'H':
					next = in + "." + i + "." + "her2";
					reasonWithHermit(step, next, true);
					break;
				case 'j':
					next = in + "." + i + "." + "jena";
					loadThroughJena(step, next);
					break;
				case 'J':
					next = in + "." + i + "." + "jena2";
					loadThroughJena2(step, next);
					break;
				case 'p':
					next = in + "." + i + "." + "pel";
					reasonWithPellet(step, next);
					break;
				default:
					System.err.println("Unknown mode: " + args[1].charAt(i));
				}
				if (next != null) {
					step = next;
					next = null;
				}
			}
		}

	}

}
