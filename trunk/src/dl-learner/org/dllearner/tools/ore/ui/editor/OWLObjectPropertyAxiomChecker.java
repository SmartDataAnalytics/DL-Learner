package org.dllearner.tools.ore.ui.editor;

import org.coode.manchesterowlsyntax.ManchesterOWLSyntaxEditorParser;
import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.expression.ParserException;
import org.semanticweb.owl.model.OWLObjectPropertyAxiom;

public class OWLObjectPropertyAxiomChecker implements OWLExpressionChecker<OWLObjectPropertyAxiom>{
	private OREManager mngr;


    public OWLObjectPropertyAxiomChecker(OREManager mngr) {
        this.mngr = mngr;
    }


    public void check(String text) throws OWLExpressionParserException {
        createObject(text);
    }


    public OWLObjectPropertyAxiom createObject(String text) throws OWLExpressionParserException {
        ManchesterOWLSyntaxEditorParser parser = new ManchesterOWLSyntaxEditorParser(mngr.getOWLDataFactory(), text);
        parser.setOWLEntityChecker(new OREOWLEntityChecker(mngr.getOWLEntityFinder()));
        try {System.out.println(parser.parseObjectPropertyAxiom());
            return parser.parseObjectPropertyAxiom();
        }
        catch (ParserException e) {
        	
            throw ParserUtil.convertException(e);
        }
    }
}
