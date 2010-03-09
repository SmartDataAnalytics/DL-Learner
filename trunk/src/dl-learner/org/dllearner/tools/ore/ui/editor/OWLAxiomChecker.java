package org.dllearner.tools.ore.ui.editor;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.model.OWLAxiom;

public class OWLAxiomChecker  implements OWLExpressionChecker<OWLAxiom>{
	
	private OREManager mngr;


    public OWLAxiomChecker(OREManager mngr) {
        this.mngr = mngr;
    }


    public void check(String text) throws OWLExpressionParserException {
        createObject(text);
    }


    public OWLAxiom createObject(String text) throws OWLExpressionParserException {
        ManchesterOWLSyntaxParser parser = new ManchesterOWLSyntaxParser(mngr.getOWLDataFactory(), text);
        parser.setOWLEntityChecker(new OREOWLEntityChecker(mngr.getOWLEntityFinder()));
        try {
            return parser.parseAxiom();
        }
        catch (ParserException e) {
            throw ParserUtil.convertException(e);
        }
    }
}
