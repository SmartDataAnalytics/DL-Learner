package org.dllearner.core.owl;

/**
 * This exception is thrown if an operation does not support
 * the required language. For instance, if a OWLClassExpression containing
 * a disjunction is passed to a method, which is only designed to
 * handle EL concepts, this exception can be thrown.
 * 
 * @author Jens Lehmann
 *
 */
public class UnsupportedLanguageException extends RuntimeException {

	private static final long serialVersionUID = -1271204878357422920L;

	public UnsupportedLanguageException(String unsupportedConstruct, String targetLanguage) {
		super("Unsupported construct \"" + unsupportedConstruct + "\". The target language is \"" + targetLanguage + "\".");
	}
	
}
