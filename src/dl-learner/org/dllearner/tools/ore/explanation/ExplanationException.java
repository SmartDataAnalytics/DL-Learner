

package org.dllearner.tools.ore.explanation;

import org.semanticweb.owlapi.model.OWLException;

public class ExplanationException extends OWLException
{

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ExplanationException(Throwable cause)
    {
        super(cause);
    }

    public ExplanationException(String message)
    {
        super(message);
    }

    public ExplanationException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
