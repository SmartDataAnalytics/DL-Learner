package org.dllearner.reasoning;

/**
 * Enumeration of available reasoner types
 * 
 * @author Jens Lehmann
 *
 */
public enum ReasonerType {
	DIG, OWLAPI_FACT, OWLAPI_PELLET, OWLAPI_HERMIT, OWLAPI_FUZZY, OWLAPI_JFACT, CLOSED_WORLD_REASONER, SPARQL_NATIVE;

	/**
	 * @return <code>true</code> if reasoner type is OWL API, otherwise <code>false</code>
	 */
	public boolean isOWLAPIReasoner() {
		return this.name().toUpperCase().startsWith("OWLAPI_");
	}
}