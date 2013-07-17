/**
 * 
 */
package org.dllearner.algorithms.isle.index;

/**
 * This gets a syntactic index and returns a semantic index by applying WSD etc.
 * @author Lorenz Buehmann
 *
 */
public class SemanticIndexCreator {

	private SyntacticIndex syntacticIndex;

	public SemanticIndexCreator(SyntacticIndex syntacticIndex) {
		this.syntacticIndex = syntacticIndex;
	}
	
	public SemanticIndex createSemanticIndex(){
		return null;
	}
}
