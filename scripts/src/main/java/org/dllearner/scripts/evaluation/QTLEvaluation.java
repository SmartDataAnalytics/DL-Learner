/**
 * 
 */
package org.dllearner.scripts.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;

import org.dllearner.algorithms.qtl.QTL2;
import org.dllearner.algorithms.qtl.QTL2Disjunctive;
import org.dllearner.algorithms.qtl.QueryTreeFactory;
import org.dllearner.algorithms.qtl.datastructures.QueryTree;
import org.dllearner.algorithms.qtl.datastructures.impl.QueryTreeImpl;
import org.dllearner.algorithms.qtl.impl.QueryTreeFactoryImpl;
import org.dllearner.cli.CrossValidation;
import org.dllearner.cli.SPARQLCrossValidation;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.owl.Individual;
import org.dllearner.kb.LocalModelBasedSparqlEndpointKS;
import org.dllearner.kb.OWLAPIOntology;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.PosNegLPStandard;
import org.dllearner.reasoning.FastInstanceChecker;
import org.dllearner.reasoning.SPARQLReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;
import com.google.common.collect.Lists;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

/**
 * @author Lorenz Buehmann
 *
 */
public class QTLEvaluation {
	
	int nrOfFolds = 10;
	private int nrOfPosExamples = 300;
	private int nrOfNegExamples = 300;
	
	List<String> posExamples = Lists.newArrayList(
			"http://dl-learner.org/carcinogenesis#d1",
			"http://dl-learner.org/carcinogenesis#d10",
			"http://dl-learner.org/carcinogenesis#d101",
			"http://dl-learner.org/carcinogenesis#d102",
			"http://dl-learner.org/carcinogenesis#d103",
			"http://dl-learner.org/carcinogenesis#d106",
			"http://dl-learner.org/carcinogenesis#d107",
			"http://dl-learner.org/carcinogenesis#d108",
			"http://dl-learner.org/carcinogenesis#d11",
			"http://dl-learner.org/carcinogenesis#d12",
			"http://dl-learner.org/carcinogenesis#d13",
			"http://dl-learner.org/carcinogenesis#d134",
			"http://dl-learner.org/carcinogenesis#d135",
			"http://dl-learner.org/carcinogenesis#d136",
			"http://dl-learner.org/carcinogenesis#d138",
			"http://dl-learner.org/carcinogenesis#d140",
			"http://dl-learner.org/carcinogenesis#d141",
			"http://dl-learner.org/carcinogenesis#d144",
			"http://dl-learner.org/carcinogenesis#d145",
			"http://dl-learner.org/carcinogenesis#d146",
			"http://dl-learner.org/carcinogenesis#d147",
			"http://dl-learner.org/carcinogenesis#d15",
			"http://dl-learner.org/carcinogenesis#d17",
			"http://dl-learner.org/carcinogenesis#d19",
			"http://dl-learner.org/carcinogenesis#d192",
			"http://dl-learner.org/carcinogenesis#d193",
			"http://dl-learner.org/carcinogenesis#d195",
			"http://dl-learner.org/carcinogenesis#d196",
			"http://dl-learner.org/carcinogenesis#d197",
			"http://dl-learner.org/carcinogenesis#d198",
			"http://dl-learner.org/carcinogenesis#d199",
			"http://dl-learner.org/carcinogenesis#d2",
			"http://dl-learner.org/carcinogenesis#d20",
			"http://dl-learner.org/carcinogenesis#d200",
			"http://dl-learner.org/carcinogenesis#d201",
			"http://dl-learner.org/carcinogenesis#d202",
			"http://dl-learner.org/carcinogenesis#d203",
			"http://dl-learner.org/carcinogenesis#d204",
			"http://dl-learner.org/carcinogenesis#d205",
			"http://dl-learner.org/carcinogenesis#d21",
			"http://dl-learner.org/carcinogenesis#d22",
			"http://dl-learner.org/carcinogenesis#d226",
			"http://dl-learner.org/carcinogenesis#d227",
			"http://dl-learner.org/carcinogenesis#d228",
			"http://dl-learner.org/carcinogenesis#d229",
			"http://dl-learner.org/carcinogenesis#d231",
			"http://dl-learner.org/carcinogenesis#d232",
			"http://dl-learner.org/carcinogenesis#d234",
			"http://dl-learner.org/carcinogenesis#d236",
			"http://dl-learner.org/carcinogenesis#d239",
			"http://dl-learner.org/carcinogenesis#d23_2",
			"http://dl-learner.org/carcinogenesis#d242",
			"http://dl-learner.org/carcinogenesis#d245",
			"http://dl-learner.org/carcinogenesis#d247",
			"http://dl-learner.org/carcinogenesis#d249",
			"http://dl-learner.org/carcinogenesis#d25",
			"http://dl-learner.org/carcinogenesis#d252",
			"http://dl-learner.org/carcinogenesis#d253",
			"http://dl-learner.org/carcinogenesis#d254",
			"http://dl-learner.org/carcinogenesis#d255",
			"http://dl-learner.org/carcinogenesis#d26",
			"http://dl-learner.org/carcinogenesis#d272",
			"http://dl-learner.org/carcinogenesis#d275",
			"http://dl-learner.org/carcinogenesis#d277",
			"http://dl-learner.org/carcinogenesis#d279",
			"http://dl-learner.org/carcinogenesis#d28",
			"http://dl-learner.org/carcinogenesis#d281",
			"http://dl-learner.org/carcinogenesis#d283",
			"http://dl-learner.org/carcinogenesis#d284",
			"http://dl-learner.org/carcinogenesis#d288",
			"http://dl-learner.org/carcinogenesis#d29",
			"http://dl-learner.org/carcinogenesis#d290",
			"http://dl-learner.org/carcinogenesis#d291",
			"http://dl-learner.org/carcinogenesis#d292",
			"http://dl-learner.org/carcinogenesis#d30",
			"http://dl-learner.org/carcinogenesis#d31",
			"http://dl-learner.org/carcinogenesis#d32",
			"http://dl-learner.org/carcinogenesis#d33",
			"http://dl-learner.org/carcinogenesis#d34",
			"http://dl-learner.org/carcinogenesis#d35",
			"http://dl-learner.org/carcinogenesis#d36",
			"http://dl-learner.org/carcinogenesis#d37",
			"http://dl-learner.org/carcinogenesis#d38",
			"http://dl-learner.org/carcinogenesis#d42",
			"http://dl-learner.org/carcinogenesis#d43",
			"http://dl-learner.org/carcinogenesis#d44",
			"http://dl-learner.org/carcinogenesis#d45",
			"http://dl-learner.org/carcinogenesis#d46",
			"http://dl-learner.org/carcinogenesis#d47",
			"http://dl-learner.org/carcinogenesis#d48",
			"http://dl-learner.org/carcinogenesis#d49",
			"http://dl-learner.org/carcinogenesis#d5",
			"http://dl-learner.org/carcinogenesis#d51",
			"http://dl-learner.org/carcinogenesis#d52",
			"http://dl-learner.org/carcinogenesis#d53",
			"http://dl-learner.org/carcinogenesis#d55",
			"http://dl-learner.org/carcinogenesis#d58",
			"http://dl-learner.org/carcinogenesis#d6",
			"http://dl-learner.org/carcinogenesis#d7",
			"http://dl-learner.org/carcinogenesis#d84",
			"http://dl-learner.org/carcinogenesis#d85_2",
			"http://dl-learner.org/carcinogenesis#d86",
			"http://dl-learner.org/carcinogenesis#d87",
			"http://dl-learner.org/carcinogenesis#d88",
			"http://dl-learner.org/carcinogenesis#d89",
			"http://dl-learner.org/carcinogenesis#d9",
			"http://dl-learner.org/carcinogenesis#d91",
			"http://dl-learner.org/carcinogenesis#d92",
			"http://dl-learner.org/carcinogenesis#d93",
			"http://dl-learner.org/carcinogenesis#d95",
			"http://dl-learner.org/carcinogenesis#d96",
			"http://dl-learner.org/carcinogenesis#d98",
			"http://dl-learner.org/carcinogenesis#d99",
			"http://dl-learner.org/carcinogenesis#d100",
			"http://dl-learner.org/carcinogenesis#d104",
			"http://dl-learner.org/carcinogenesis#d105",
			"http://dl-learner.org/carcinogenesis#d109",
			"http://dl-learner.org/carcinogenesis#d137",
			"http://dl-learner.org/carcinogenesis#d139",
			"http://dl-learner.org/carcinogenesis#d14",
			"http://dl-learner.org/carcinogenesis#d142",
			"http://dl-learner.org/carcinogenesis#d143",
			"http://dl-learner.org/carcinogenesis#d148",
			"http://dl-learner.org/carcinogenesis#d16",
			"http://dl-learner.org/carcinogenesis#d18",
			"http://dl-learner.org/carcinogenesis#d191",
			"http://dl-learner.org/carcinogenesis#d206",
			"http://dl-learner.org/carcinogenesis#d230",
			"http://dl-learner.org/carcinogenesis#d233",
			"http://dl-learner.org/carcinogenesis#d235",
			"http://dl-learner.org/carcinogenesis#d237",
			"http://dl-learner.org/carcinogenesis#d238",
			"http://dl-learner.org/carcinogenesis#d23_1",
			"http://dl-learner.org/carcinogenesis#d24",
			"http://dl-learner.org/carcinogenesis#d240",
			"http://dl-learner.org/carcinogenesis#d241",
			"http://dl-learner.org/carcinogenesis#d243",
			"http://dl-learner.org/carcinogenesis#d244",
			"http://dl-learner.org/carcinogenesis#d246",
			"http://dl-learner.org/carcinogenesis#d248",
			"http://dl-learner.org/carcinogenesis#d250",
			"http://dl-learner.org/carcinogenesis#d251",
			"http://dl-learner.org/carcinogenesis#d27",
			"http://dl-learner.org/carcinogenesis#d273",
			"http://dl-learner.org/carcinogenesis#d274",
			"http://dl-learner.org/carcinogenesis#d278",
			"http://dl-learner.org/carcinogenesis#d286",
			"http://dl-learner.org/carcinogenesis#d289",
			"http://dl-learner.org/carcinogenesis#d3",
			"http://dl-learner.org/carcinogenesis#d39",
			"http://dl-learner.org/carcinogenesis#d4",
			"http://dl-learner.org/carcinogenesis#d40",
			"http://dl-learner.org/carcinogenesis#d41",
			"http://dl-learner.org/carcinogenesis#d50",
			"http://dl-learner.org/carcinogenesis#d54",
			"http://dl-learner.org/carcinogenesis#d56",
			"http://dl-learner.org/carcinogenesis#d57",
			"http://dl-learner.org/carcinogenesis#d8",
			"http://dl-learner.org/carcinogenesis#d85_1",
			"http://dl-learner.org/carcinogenesis#d90",
			"http://dl-learner.org/carcinogenesis#d94",
			"http://dl-learner.org/carcinogenesis#d97",
			"http://dl-learner.org/carcinogenesis#d296",
			"http://dl-learner.org/carcinogenesis#d305",
			"http://dl-learner.org/carcinogenesis#d306",
			"http://dl-learner.org/carcinogenesis#d307",
			"http://dl-learner.org/carcinogenesis#d308",
			"http://dl-learner.org/carcinogenesis#d311",
			"http://dl-learner.org/carcinogenesis#d314",
			"http://dl-learner.org/carcinogenesis#d315",
			"http://dl-learner.org/carcinogenesis#d316",
			"http://dl-learner.org/carcinogenesis#d320",
			"http://dl-learner.org/carcinogenesis#d322",
			"http://dl-learner.org/carcinogenesis#d323",
			"http://dl-learner.org/carcinogenesis#d325",
			"http://dl-learner.org/carcinogenesis#d329",
			"http://dl-learner.org/carcinogenesis#d330",
			"http://dl-learner.org/carcinogenesis#d331",
			"http://dl-learner.org/carcinogenesis#d332",
			"http://dl-learner.org/carcinogenesis#d333",
			"http://dl-learner.org/carcinogenesis#d336",
			"http://dl-learner.org/carcinogenesis#d337"
			);
	
	List<String> negExamples = Lists.newArrayList(
			"http://dl-learner.org/carcinogenesis#d110",
			"http://dl-learner.org/carcinogenesis#d111",
			"http://dl-learner.org/carcinogenesis#d114",
			"http://dl-learner.org/carcinogenesis#d116",
			"http://dl-learner.org/carcinogenesis#d117",
			"http://dl-learner.org/carcinogenesis#d119",
			"http://dl-learner.org/carcinogenesis#d121",
			"http://dl-learner.org/carcinogenesis#d123",
			"http://dl-learner.org/carcinogenesis#d124",
			"http://dl-learner.org/carcinogenesis#d125",
			"http://dl-learner.org/carcinogenesis#d127",
			"http://dl-learner.org/carcinogenesis#d128",
			"http://dl-learner.org/carcinogenesis#d130",
			"http://dl-learner.org/carcinogenesis#d133",
			"http://dl-learner.org/carcinogenesis#d150",
			"http://dl-learner.org/carcinogenesis#d151",
			"http://dl-learner.org/carcinogenesis#d154",
			"http://dl-learner.org/carcinogenesis#d155",
			"http://dl-learner.org/carcinogenesis#d156",
			"http://dl-learner.org/carcinogenesis#d159",
			"http://dl-learner.org/carcinogenesis#d160",
			"http://dl-learner.org/carcinogenesis#d161",
			"http://dl-learner.org/carcinogenesis#d162",
			"http://dl-learner.org/carcinogenesis#d163",
			"http://dl-learner.org/carcinogenesis#d164",
			"http://dl-learner.org/carcinogenesis#d165",
			"http://dl-learner.org/carcinogenesis#d166",
			"http://dl-learner.org/carcinogenesis#d169",
			"http://dl-learner.org/carcinogenesis#d170",
			"http://dl-learner.org/carcinogenesis#d171",
			"http://dl-learner.org/carcinogenesis#d172",
			"http://dl-learner.org/carcinogenesis#d173",
			"http://dl-learner.org/carcinogenesis#d174",
			"http://dl-learner.org/carcinogenesis#d178",
			"http://dl-learner.org/carcinogenesis#d179",
			"http://dl-learner.org/carcinogenesis#d180",
			"http://dl-learner.org/carcinogenesis#d181",
			"http://dl-learner.org/carcinogenesis#d183",
			"http://dl-learner.org/carcinogenesis#d184",
			"http://dl-learner.org/carcinogenesis#d185",
			"http://dl-learner.org/carcinogenesis#d186",
			"http://dl-learner.org/carcinogenesis#d188",
			"http://dl-learner.org/carcinogenesis#d190",
			"http://dl-learner.org/carcinogenesis#d194",
			"http://dl-learner.org/carcinogenesis#d207",
			"http://dl-learner.org/carcinogenesis#d208_1",
			"http://dl-learner.org/carcinogenesis#d209",
			"http://dl-learner.org/carcinogenesis#d210",
			"http://dl-learner.org/carcinogenesis#d211",
			"http://dl-learner.org/carcinogenesis#d212",
			"http://dl-learner.org/carcinogenesis#d213",
			"http://dl-learner.org/carcinogenesis#d214",
			"http://dl-learner.org/carcinogenesis#d215",
			"http://dl-learner.org/carcinogenesis#d217",
			"http://dl-learner.org/carcinogenesis#d218",
			"http://dl-learner.org/carcinogenesis#d219",
			"http://dl-learner.org/carcinogenesis#d220",
			"http://dl-learner.org/carcinogenesis#d224",
			"http://dl-learner.org/carcinogenesis#d256",
			"http://dl-learner.org/carcinogenesis#d257",
			"http://dl-learner.org/carcinogenesis#d258",
			"http://dl-learner.org/carcinogenesis#d261",
			"http://dl-learner.org/carcinogenesis#d262",
			"http://dl-learner.org/carcinogenesis#d263",
			"http://dl-learner.org/carcinogenesis#d264",
			"http://dl-learner.org/carcinogenesis#d265",
			"http://dl-learner.org/carcinogenesis#d266",
			"http://dl-learner.org/carcinogenesis#d267",
			"http://dl-learner.org/carcinogenesis#d269",
			"http://dl-learner.org/carcinogenesis#d271",
			"http://dl-learner.org/carcinogenesis#d276",
			"http://dl-learner.org/carcinogenesis#d280",
			"http://dl-learner.org/carcinogenesis#d285",
			"http://dl-learner.org/carcinogenesis#d287",
			"http://dl-learner.org/carcinogenesis#d293",
			"http://dl-learner.org/carcinogenesis#d294",
			"http://dl-learner.org/carcinogenesis#d59",
			"http://dl-learner.org/carcinogenesis#d60",
			"http://dl-learner.org/carcinogenesis#d61",
			"http://dl-learner.org/carcinogenesis#d63",
			"http://dl-learner.org/carcinogenesis#d64",
			"http://dl-learner.org/carcinogenesis#d65",
			"http://dl-learner.org/carcinogenesis#d69",
			"http://dl-learner.org/carcinogenesis#d70",
			"http://dl-learner.org/carcinogenesis#d71",
			"http://dl-learner.org/carcinogenesis#d72",
			"http://dl-learner.org/carcinogenesis#d73",
			"http://dl-learner.org/carcinogenesis#d74",
			"http://dl-learner.org/carcinogenesis#d75",
			"http://dl-learner.org/carcinogenesis#d76",
			"http://dl-learner.org/carcinogenesis#d77",
			"http://dl-learner.org/carcinogenesis#d78",
			"http://dl-learner.org/carcinogenesis#d79",
			"http://dl-learner.org/carcinogenesis#d80",
			"http://dl-learner.org/carcinogenesis#d81",
			"http://dl-learner.org/carcinogenesis#d82",
			"http://dl-learner.org/carcinogenesis#d112",
			"http://dl-learner.org/carcinogenesis#d113",
			"http://dl-learner.org/carcinogenesis#d115",
			"http://dl-learner.org/carcinogenesis#d118",
			"http://dl-learner.org/carcinogenesis#d120",
			"http://dl-learner.org/carcinogenesis#d122",
			"http://dl-learner.org/carcinogenesis#d126",
			"http://dl-learner.org/carcinogenesis#d129",
			"http://dl-learner.org/carcinogenesis#d131",
			"http://dl-learner.org/carcinogenesis#d132",
			"http://dl-learner.org/carcinogenesis#d149",
			"http://dl-learner.org/carcinogenesis#d152",
			"http://dl-learner.org/carcinogenesis#d153",
			"http://dl-learner.org/carcinogenesis#d157",
			"http://dl-learner.org/carcinogenesis#d158",
			"http://dl-learner.org/carcinogenesis#d167",
			"http://dl-learner.org/carcinogenesis#d168",
			"http://dl-learner.org/carcinogenesis#d175",
			"http://dl-learner.org/carcinogenesis#d176",
			"http://dl-learner.org/carcinogenesis#d177",
			"http://dl-learner.org/carcinogenesis#d182",
			"http://dl-learner.org/carcinogenesis#d187",
			"http://dl-learner.org/carcinogenesis#d189",
			"http://dl-learner.org/carcinogenesis#d208_2",
			"http://dl-learner.org/carcinogenesis#d216",
			"http://dl-learner.org/carcinogenesis#d221",
			"http://dl-learner.org/carcinogenesis#d222",
			"http://dl-learner.org/carcinogenesis#d223",
			"http://dl-learner.org/carcinogenesis#d225",
			"http://dl-learner.org/carcinogenesis#d259",
			"http://dl-learner.org/carcinogenesis#d260",
			"http://dl-learner.org/carcinogenesis#d268",
			"http://dl-learner.org/carcinogenesis#d270",
			"http://dl-learner.org/carcinogenesis#d282",
			"http://dl-learner.org/carcinogenesis#d295",
			"http://dl-learner.org/carcinogenesis#d62",
			"http://dl-learner.org/carcinogenesis#d66",
			"http://dl-learner.org/carcinogenesis#d67",
			"http://dl-learner.org/carcinogenesis#d68",
			"http://dl-learner.org/carcinogenesis#d83",
			"http://dl-learner.org/carcinogenesis#d297",
			"http://dl-learner.org/carcinogenesis#d298",
			"http://dl-learner.org/carcinogenesis#d299",
			"http://dl-learner.org/carcinogenesis#d300",
			"http://dl-learner.org/carcinogenesis#d302",
			"http://dl-learner.org/carcinogenesis#d303",
			"http://dl-learner.org/carcinogenesis#d304",
			"http://dl-learner.org/carcinogenesis#d309",
			"http://dl-learner.org/carcinogenesis#d312",
			"http://dl-learner.org/carcinogenesis#d313",
			"http://dl-learner.org/carcinogenesis#d317",
			"http://dl-learner.org/carcinogenesis#d318",
			"http://dl-learner.org/carcinogenesis#d319",
			"http://dl-learner.org/carcinogenesis#d324",
			"http://dl-learner.org/carcinogenesis#d326",
			"http://dl-learner.org/carcinogenesis#d327",
			"http://dl-learner.org/carcinogenesis#d328",
			"http://dl-learner.org/carcinogenesis#d334",
			"http://dl-learner.org/carcinogenesis#d335"
			);
	
	private Model model;
	private OWLOntology ontology;
	private QueryTreeFactory<String> queryTreeFactory;
	private List<QueryTree<String>> posExampleTrees;
	private List<QueryTree<String>> negExampleTrees;
	private PosNegLP lp;

	
	
	public QTLEvaluation() throws ComponentInitException {
		queryTreeFactory = new QueryTreeFactoryImpl();
		queryTreeFactory.setMaxDepth(3);
		
		loadDataset();
		
		loadExamples();
	}
	
	private void loadDataset(){
		File file = new File("../examples/carcinogenesis/carcinogenesis.owl");
		model = ModelFactory.createDefaultModel();
		try {
			model.read(new FileInputStream(file), null, "RDF/XML");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		OWLOntologyManager man = OWLManager.createOWLOntologyManager();
		try {
			ontology = man.loadOntologyFromOntologyDocument(file);
		} catch (OWLOntologyCreationException e) {
			e.printStackTrace();
		}
	}
	
	private void loadExamples() throws ComponentInitException{
		
		Collections.shuffle(posExamples, new Random(1));
		Collections.shuffle(negExamples, new Random(2));	
		posExamples = posExamples.subList(0, Math.min(posExamples.size(), nrOfPosExamples));
		negExamples = negExamples.subList(0, Math.min(negExamples.size(), nrOfNegExamples));
		
//		posExamples.clear();
//		String string = "http://dl-learner.org/carcinogenesis#d101, http://dl-learner.org/carcinogenesis#d103, http://dl-learner.org/carcinogenesis#d107, http://dl-learner.org/carcinogenesis#d108, http://dl-learner.org/carcinogenesis#d135, http://dl-learner.org/carcinogenesis#d139, http://dl-learner.org/carcinogenesis#d14, http://dl-learner.org/carcinogenesis#d141, http://dl-learner.org/carcinogenesis#d143, http://dl-learner.org/carcinogenesis#d147, http://dl-learner.org/carcinogenesis#d17, http://dl-learner.org/carcinogenesis#d19, http://dl-learner.org/carcinogenesis#d193, http://dl-learner.org/carcinogenesis#d198, http://dl-learner.org/carcinogenesis#d228, http://dl-learner.org/carcinogenesis#d236, http://dl-learner.org/carcinogenesis#d242, http://dl-learner.org/carcinogenesis#d244, http://dl-learner.org/carcinogenesis#d273, http://dl-learner.org/carcinogenesis#d275, http://dl-learner.org/carcinogenesis#d28, http://dl-learner.org/carcinogenesis#d283, http://dl-learner.org/carcinogenesis#d286, http://dl-learner.org/carcinogenesis#d291, http://dl-learner.org/carcinogenesis#d292, http://dl-learner.org/carcinogenesis#d307, http://dl-learner.org/carcinogenesis#d31, http://dl-learner.org/carcinogenesis#d325, http://dl-learner.org/carcinogenesis#d33, http://dl-learner.org/carcinogenesis#d333, http://dl-learner.org/carcinogenesis#d34, http://dl-learner.org/carcinogenesis#d36, http://dl-learner.org/carcinogenesis#d38, http://dl-learner.org/carcinogenesis#d4, http://dl-learner.org/carcinogenesis#d40, http://dl-learner.org/carcinogenesis#d44, http://dl-learner.org/carcinogenesis#d51, http://dl-learner.org/carcinogenesis#d85_2, http://dl-learner.org/carcinogenesis#d98, http://dl-learner.org/carcinogenesis#d99";
//		String[] split = string.split(",");
//		for (String s : split) {
//			posExamples.add(s.trim());
//		}
//		negExamples.clear();
//		string = "http://dl-learner.org/carcinogenesis#d112, http://dl-learner.org/carcinogenesis#d116, http://dl-learner.org/carcinogenesis#d117, http://dl-learner.org/carcinogenesis#d119, http://dl-learner.org/carcinogenesis#d157, http://dl-learner.org/carcinogenesis#d160, http://dl-learner.org/carcinogenesis#d161, http://dl-learner.org/carcinogenesis#d162, http://dl-learner.org/carcinogenesis#d163, http://dl-learner.org/carcinogenesis#d167, http://dl-learner.org/carcinogenesis#d169, http://dl-learner.org/carcinogenesis#d175, http://dl-learner.org/carcinogenesis#d177, http://dl-learner.org/carcinogenesis#d184, http://dl-learner.org/carcinogenesis#d194, http://dl-learner.org/carcinogenesis#d208_2, http://dl-learner.org/carcinogenesis#d209, http://dl-learner.org/carcinogenesis#d217, http://dl-learner.org/carcinogenesis#d256, http://dl-learner.org/carcinogenesis#d257, http://dl-learner.org/carcinogenesis#d260, http://dl-learner.org/carcinogenesis#d271, http://dl-learner.org/carcinogenesis#d276, http://dl-learner.org/carcinogenesis#d282, http://dl-learner.org/carcinogenesis#d287, http://dl-learner.org/carcinogenesis#d294, http://dl-learner.org/carcinogenesis#d298, http://dl-learner.org/carcinogenesis#d300, http://dl-learner.org/carcinogenesis#d309, http://dl-learner.org/carcinogenesis#d319, http://dl-learner.org/carcinogenesis#d326, http://dl-learner.org/carcinogenesis#d328, http://dl-learner.org/carcinogenesis#d334, http://dl-learner.org/carcinogenesis#d60, http://dl-learner.org/carcinogenesis#d61, http://dl-learner.org/carcinogenesis#d66, http://dl-learner.org/carcinogenesis#d75, http://dl-learner.org/carcinogenesis#d79, http://dl-learner.org/carcinogenesis#d80, http://dl-learner.org/carcinogenesis#d83";
//		split = string.split(",");
//		for (String s : split) {
//			negExamples.add(s.trim());
//		}
		
		posExampleTrees = new ArrayList<QueryTree<String>>();
		for (String ex : posExamples) {
			QueryTreeImpl<String> tree = queryTreeFactory.getQueryTree(ex, model);
			posExampleTrees.add(tree);
		}
		
		negExampleTrees = new ArrayList<QueryTree<String>>();
		for (String ex : negExamples) {
			QueryTreeImpl<String> tree = queryTreeFactory.getQueryTree(ex, model);
			negExampleTrees.add(tree);
		}
		
		int cnt = 1;
		for(QueryTree<String> tree : posExampleTrees){
//			System.out.println("TREE " + cnt);
//			tree.dump();
//			
//			System.out.println("-----------------------------");
			cnt++;
//			System.out.println(((QueryTreeImpl<String>)tree).toQuery());
		}
		
		SortedSet<Individual> pos = new TreeSet<Individual>();
		for (String ex : posExamples) {
			pos.add(new Individual(ex));
		}
		SortedSet<Individual> neg = new TreeSet<Individual>();
		for (String ex : negExamples) {
			neg.add(new Individual(ex));
		}
		lp = new PosNegLPStandard();
		lp.setPositiveExamples(pos);
		lp.setNegativeExamples(neg);
	}
	
	public void run(boolean multiThreaded) throws ComponentInitException, LearningProblemUnsupportedException{
		long startTime = System.currentTimeMillis();
		FastInstanceChecker reasoner = new FastInstanceChecker(new OWLAPIOntology(ontology));
		reasoner.init();
		lp.setReasoner(reasoner);
		lp.init();
		QTL2Disjunctive la = new QTL2Disjunctive(lp, reasoner);
		la.init();
		la.start();
		
		CrossValidation.outputFile = new File("log/qtl-cv.log");
		CrossValidation.writeToFile = true;
		CrossValidation.multiThreaded = multiThreaded;
//		CrossValidation cv = new CrossValidation(la, lp, reasoner, nrOfFolds, false);
		long endTime = System.currentTimeMillis();
		System.err.println((endTime - startTime) + "ms");
	}

	
	public static void main(String[] args) throws Exception {
		boolean multiThreaded = Boolean.valueOf(args[0]);
		new QTLEvaluation().run(multiThreaded);
	}

}
