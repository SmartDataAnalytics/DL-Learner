package org.dllearner.tools.ore.ui.editor;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owlapi.expression.ParserException;
import org.semanticweb.owlapi.model.OWLObjectPropertyAxiom;

public class OWLObjectPropertyAxiomChecker implements OWLExpressionChecker<OWLObjectPropertyAxiom>{
	private OREManager mngr;


    public OWLObjectPropertyAxiomChecker(OREManager mngr) {
        this.mngr = mngr;
    }


    public void check(String text) throws ParserException{
        createObject(text);
    }


    public OWLObjectPropertyAxiom createObject(String text) throws ParserException{
        ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParser(mngr.getOWLDataFactory(), text);
        parser.setOWLEntityChecker(new OREOWLEntityChecker(mngr.getOWLEntityFinder()));
        
        return parser.parseObjectPropertyAxiom();
    }
}
