package org.dllearner.tools.ore.ui.editor;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.expression.ParserException;
import org.semanticweb.owl.model.OWLClassAxiom;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 11-Oct-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 *
 */
class OWLClassAxiomChecker implements OWLExpressionChecker<OWLClassAxiom> {

    private OREManager mngr;


    public OWLClassAxiomChecker(OREManager mngr) {
        this.mngr = mngr;
    }


    public void check(String text) throws OWLExpressionParserException {
        createObject(text);
    }


    public OWLClassAxiom createObject(String text) throws OWLExpressionParserException {
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(mngr.getOWLDataFactory(), text);
        parser.setOWLEntityChecker(new OREOWLEntityChecker(mngr.getOWLEntityFinder()));
        try {
            return parser.parseClassAxiom();
        }
        catch (ParserException e) {
        	
            throw ParserUtil.convertException(e);
        }
    }
}
