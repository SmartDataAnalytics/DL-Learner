package org.dllearner.algorithms.qtl.exception;

public class EmptyLGGException extends QTLException {

	private static final long serialVersionUID = 1126660883468263774L;

	private static final String MESSAGE = "The LGG is empty, i.e. it's equal "
			+ "to owl:Thing and covers all instances in KB.";

	public EmptyLGGException() {
		super(MESSAGE);
	}

}
