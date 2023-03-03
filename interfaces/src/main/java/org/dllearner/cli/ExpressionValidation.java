package org.dllearner.cli;

import com.github.jsonldjava.shaded.com.google.common.collect.Sets;
import org.apache.log4j.Level;
import org.dllearner.accuracymethods.AccMethodPredAcc;
import org.dllearner.algorithms.parcel.ParCELEvaluationResult;
import org.dllearner.algorithms.parcel.ParCELPosNegLP;
import org.dllearner.configuration.IConfiguration;
import org.dllearner.configuration.spring.ApplicationContextBuilder;
import org.dllearner.configuration.spring.DefaultApplicationContextBuilder;
import org.dllearner.confparser.ConfParserConfiguration;
import org.dllearner.confparser.ParseException;
import org.dllearner.core.*;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.utilities.OWLAPIUtils;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Evaluate a class expression on a PosNegLP
 */
@ComponentAnn(name = "Expression validator", version = 0, shortName = "")
public class ExpressionValidation extends CLIBase2 {

	private static Logger logger = LoggerFactory.getLogger(ExpressionValidation.class);

	private KnowledgeSource knowledgeSource;
	private AbstractReasonerComponent rs;
	private AbstractClassExpressionLearningProblem lp;
	private OWLDataFactory dataFactory = new OWLDataFactoryImpl();

	public OWLClassExpression getExpression() {
		return expression;
	}

	public void setExpression(OWLClassExpression expression) {
		this.expression = expression;
	}

	private OWLClassExpression expression;

	@Override
	public void init() throws IOException {
		if (context == null) {
			super.init();

            knowledgeSource = context.getBean(KnowledgeSource.class);
            rs = getMainReasonerComponent();
    		lp = context.getBean(AbstractClassExpressionLearningProblem.class);
		}
	}

	@Override
	public void run() {
    	try {
			org.apache.log4j.Logger.getLogger("org.dllearner").setLevel(Level.toLevel(logLevel.toUpperCase()));
		} catch (Exception e) {
			logger.warn("Error setting log level to " + logLevel);
		}
		lp = context.getBean(AbstractClassExpressionLearningProblem.class);
		rs = lp.getReasoner();

//		expression = OWLAPIUtils.classExpressionPropertyExpanderChecked(this.expression, rs, dataFactory, true, logger);
//		if (expression != null) {
//			EvaluatedHypothesis ev = lp.evaluate(expression);
//			if (ev instanceof EvaluatedDescriptionPosNeg) {
//				logger.info("#EVAL# tp: " + ((EvaluatedDescriptionPosNeg) ev).getCoveredPositives().size());
//				logger.info("#EVAL# fp: " + ((EvaluatedDescriptionPosNeg) ev).getCoveredNegatives().size());
//				logger.info("#EVAL# tn: " + ((EvaluatedDescriptionPosNeg) ev).getNotCoveredNegatives().size());
//				logger.info("#EVAL# fn: " + ((EvaluatedDescriptionPosNeg) ev).getNotCoveredPositives().size());
//			}
//		} else {
//			logger.error("Expression is empty.");
//			throw new RuntimeException("Expression is empty.");
//		}

//		expression = dataFactory.getOWLObjectSomeValuesFrom(
//			dataFactory.getOWLObjectProperty(IRI.create("https://orbis-security.com/pe-malware-ontology#has_section")),
//			dataFactory.getOWLObjectIntersectionOf(
//				dataFactory.getOWLClass(IRI.create("https://orbis-security.com/pe-malware-ontology#CodeSection")),
//				dataFactory.getOWLObjectMinCardinality(
//					2,
//					dataFactory.getOWLObjectProperty(IRI.create("https://orbis-security.com/pe-malware-ontology#has_section_feature")),
//					dataFactory.getOWLThing()
//				)
//			)
//		);

//		expression = dataFactory.getOWLObjectSomeValuesFrom(
//			dataFactory.getOWLObjectProperty(IRI.create("https://orbis-security.com/pe-malware-ontology#has_section")),
//			dataFactory.getOWLObjectMinCardinality(
//				2,
//				dataFactory.getOWLObjectProperty(IRI.create("https://orbis-security.com/pe-malware-ontology#has_section_feature")),
//				dataFactory.getOWLObjectComplementOf(dataFactory.getOWLClass(IRI.create("https://orbis-security.com/pe-malware-ontology#HighEntropy")))
//			)
//		);

//		expression = dataFactory.getOWLObjectIntersectionOf(
//			dataFactory.getOWLClass(IRI.create("https://orbis-security.com/pe-malware-ontology#ExecutableFile")),
//			dataFactory.getOWLObjectMinCardinality(
//				2,
//				dataFactory.getOWLObjectProperty(IRI.create("https://orbis-security.com/pe-malware-ontology#has_section")),
//				dataFactory.getOWLObjectIntersectionOf(
//					dataFactory.getOWLClass(IRI.create("https://orbis-security.com/pe-malware-ontology#CodeSection")),
//					dataFactory.getOWLObjectMinCardinality(
//						2,
//						dataFactory.getOWLObjectProperty(IRI.create("https://orbis-security.com/pe-malware-ontology#has_section_feature")),
//						dataFactory.getOWLThing()
//					)
//				)
//			)
//		);

		expression = dataFactory.getOWLObjectIntersectionOf(
			dataFactory.getOWLClass(IRI.create("https://orbis-security.com/pe-malware-ontology#ExecutableFile")),
			dataFactory.getOWLObjectSomeValuesFrom(
				dataFactory.getOWLObjectProperty(IRI.create("https://orbis-security.com/pe-malware-ontology#has_section")),
				dataFactory.getOWLObjectIntersectionOf(
					dataFactory.getOWLClass(IRI.create("https://orbis-security.com/pe-malware-ontology#CodeSection")),
					dataFactory.getOWLClass(IRI.create("https://orbis-security.com/pe-malware-ontology#UninitializedDataSection"))
				)
			)
		);

		logger.info("#EVAL# expression: " + expression);

		Set<OWLIndividual> pos = ((ParCELPosNegLP) lp).getPositiveTestExamples();
		Set<OWLIndividual> neg = ((ParCELPosNegLP) lp).getNegativeTestExamples();

		int tp = rs.hasType(expression, pos).size();
		int fp = rs.hasType(expression, neg).size();
		int tn = neg.size() - fp;
		int fn = pos.size() - tp;
		double acc = (new AccMethodPredAcc(true)).getAccOrTooWeak2(tp, fn, fp, tn, 1);

		logger.info("#EVAL# tp: " + tp);
		logger.info("#EVAL# fp: " + fp);
		logger.info("#EVAL# tn: " + tn);
		logger.info("#EVAL# fn: " + fn);
		logger.info("#EVAL# acc: " + acc);

		ParCELEvaluationResult ev =((ParCELPosNegLP) lp).getAccuracyAndCorrectness4(expression);
		System.out.println(ev);

		Set<OWLIndividual> coveredIndividuals = rs.getIndividuals(expression);
		System.out.println(Sets.intersection(pos, coveredIndividuals).size());
		System.out.println(Sets.intersection(neg, coveredIndividuals).size());
	}

	public static void main(String[] args) throws ParseException, IOException, ReasoningMethodUnsupportedException {
		System.out.println("DL-Learner expression validation");

		// currently, CLI has exactly one parameter - the conf file
		if(args.length == 0) {
			System.out.println("You need to give a conf file as argument.");
			System.exit(0);
		}

		// read file and print and print a message if it does not exist
		File file = new File(args[args.length - 1]);
		if(!file.exists()) {
			System.out.println("File \"" + file + "\" does not exist.");
			System.exit(0);
		}

		Resource confFile = new FileSystemResource(file);

		List<Resource> springConfigResources = new ArrayList<>();

		try {
			//DL-Learner Configuration Object
			IConfiguration configuration = new ConfParserConfiguration(confFile);

			ApplicationContextBuilder builder = new DefaultApplicationContextBuilder();
			ApplicationContext context =  builder.buildApplicationContext(configuration,springConfigResources);

			ExpressionValidation validation = new ExpressionValidation();
			validation.setContext(context);
			validation.setConfFile(file);
			validation.run();
			validation.close();
		} catch (Exception e) {
			String stacktraceFileName = "log/error.log";

			//Find the primary cause of the exception.
			Throwable primaryCause = findPrimaryCause(e);

			// Get the Root Error Message
			logger.error("An Error Has Occurred During Processing.");
			if (primaryCause != null) {
				logger.error(primaryCause.getMessage());
			}
			logger.debug("Stack Trace: ", e);
			logger.error("Terminating DL-Learner...and writing stacktrace to: " + stacktraceFileName);
			createIfNotExists(new File(stacktraceFileName));

			FileOutputStream fos = new FileOutputStream(stacktraceFileName);
			PrintStream ps = new PrintStream(fos);
			e.printStackTrace(ps);
		}
	}
}
