package org.dllearner.algorithms.qtl.operations;

import java.util.concurrent.TimeUnit;

/**
 * Any kind of operation that can timeout.
 *
 * @author Lorenz Buehmann
 */
@FunctionalInterface
public interface TimeoutableOperation {

	void setTimeout(long timeout, TimeUnit timeoutUnits);
}
