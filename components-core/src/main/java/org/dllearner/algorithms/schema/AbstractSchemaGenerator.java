/**
 * 
 */
package org.dllearner.algorithms.schema;

import java.util.Set;

import org.aksw.jena_sparql_api.core.QueryExecutionFactory;
import org.dllearner.algorithms.properties.AxiomAlgorithms;
import org.semanticweb.owlapi.model.AxiomType;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWLProfile;


/**
 * 
 * @author Lorenz Buehmann
 *
 */
public abstract class AbstractSchemaGenerator implements SchemaGenerator{
	
	protected QueryExecutionFactory qef;
	protected OWLProfile owlProfile = new OWL2DLProfile();
	
	protected Set<AxiomType<? extends OWLAxiom>> axiomTypes = AxiomAlgorithms.TBoxAndRBoxAxiomTypes;
	
	public AbstractSchemaGenerator(QueryExecutionFactory qef) {
		this.qef = qef;
	}
	
	/**
	 * @param owlProfile the owlProfile to set
	 */
	public void setOwlProfile(OWLProfile owlProfile) {
		this.owlProfile = owlProfile;
	}
	
	/**
	 * @return the owlProfile
	 */
	public OWLProfile getOwlProfile() {
		return owlProfile;
	}
	
	/**
	 * Set the types of axioms that are generated.
	 * @param axiomTypes the axiom types to set
	 */
	public void setAxiomTypes(Set<AxiomType<? extends OWLAxiom>> axiomTypes) {
		this.axiomTypes = axiomTypes;
	}

}
