package org.dllearner.tools.ore.ui.editor;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLClassAxiom;


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


    public void check(String text) throws ParserException {
        createObject(text);
    }


    public OWLClassAxiom createObject(String text) throws ParserException{
        ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParser(mngr.getOWLDataFactory(), text);
        parser.setOWLEntityChecker(new OREOWLEntityChecker(mngr.getOWLEntityFinder()));
            
        return parser.parseClassAxiom();
        
    }
}
