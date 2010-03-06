package org.dllearner.tools.ore.ui.editor;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 11-Oct-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public interface OWLExpressionChecker<O> {

    public void check(String text) throws OWLExpressionParserException;

    O createObject(String text) throws OWLExpressionParserException;
}
