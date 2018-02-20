package org.dllearner.core;

/**
 * Abstract DL-Learner Component Builder
 */
public interface Builder<T> {
	T build() throws ComponentInitException;
}
