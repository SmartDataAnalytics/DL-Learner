package org.dllearner.tools.ore.ui.editor;

import org.semanticweb.owl.expression.ParserException;

public class ParserUtil {

    public static OWLExpressionParserException convertException(ParserException ex) {
        int endPos = ex.getStartPos() + ex.getCurrentToken().length();
        if (ex.getCurrentToken().equals("<EOF>")){
            endPos = ex.getStartPos(); // because start + length of <EOF> would push us past the end of the document
        }
        return new OWLExpressionParserException(ex.getMessage(),
                                                ex.getStartPos(),
                                                endPos,
                                                ex.isClassNameExpected(),
                                                ex.isObjectPropertyNameExpected(),
                                                ex.isDataPropertyNameExpected(),
                                                ex.isIndividualNameExpected(),
                                                ex.isDatatypeNameExpected(),
                                                ex.getExpectedKeywords());
    }
}

