package org.dllearner.utilities.examples;

import java.util.Set;

import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.expression.OWLEntityChecker;
import org.semanticweb.owlapi.expression.ShortFormEntityChecker;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.util.BidirectionalShortFormProvider;
import org.semanticweb.owlapi.util.BidirectionalShortFormProviderAdapter;
import org.semanticweb.owlapi.util.SimpleShortFormProvider;
import org.semanticweb.owlapi.util.mansyntax.ManchesterOWLSyntaxParser;

/**
 * @author Lorenz Buehmann
 */
public class DLQueryBasedExamplesProvider implements ExamplesProvider{

    private final OWLAPIReasoner rc;
    private final OWLClassExpression posExamplesCE;
    private final OWLClassExpression negExamplesCE;

    private Set<OWLIndividual> posExamples;
    private Set<OWLIndividual> negExamples;

    public DLQueryBasedExamplesProvider(OWLAPIReasoner rc,
                                        OWLClassExpression posExamplesCE,
                                        OWLClassExpression negExamplesCE) {
        this.rc = rc;
        this.posExamplesCE = posExamplesCE;
        this.negExamplesCE = negExamplesCE;
    }

    public DLQueryBasedExamplesProvider(OWLAPIReasoner rc,
                                        String posExamplesClassExpressionString,
                                        String negExamplesClassExpressionString) {
        this.rc = rc;
        this.posExamplesCE = parseClassExpression(posExamplesClassExpressionString);
        this.negExamplesCE = parseClassExpression(negExamplesClassExpressionString);
    }

    /**
     * Parses a class expression string to obtain a class expression.
     *
     * @param classExpressionString
     *        The class expression string
     * @return The corresponding class expression if the class expression string
     *         is malformed or contains unknown entity names.
     */
    private OWLClassExpression parseClassExpression(String classExpressionString) {
        BidirectionalShortFormProvider bidiShortFormProvider = new BidirectionalShortFormProviderAdapter(
                rc.getManager(),
                rc.getOntology().getImportsClosure(),
                new SimpleShortFormProvider());
        // Set up the real parser
        ManchesterOWLSyntaxParser parser = OWLManager.createManchesterParser();
        parser.setDefaultOntology(rc.getOntology());
        // Specify an entity checker that wil be used to check a class
        // expression contains the correct names.
        OWLEntityChecker entityChecker = new ShortFormEntityChecker(bidiShortFormProvider);
        parser.setOWLEntityChecker(entityChecker);
        // Do the actual parsing
        return parser.parseClassExpression();
    }

    @Override
    public Set<OWLIndividual> getPosExamples() {
        if(posExamples == null) {
            posExamples = rc.getIndividuals();
        }
        return posExamples;
    }

    @Override
    public Set<OWLIndividual> getNegExamples() {
        if(negExamples == null) {
            negExamples = rc.getIndividuals();
        }
        return negExamples;
    }
}
