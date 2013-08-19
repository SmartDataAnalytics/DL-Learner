package org.dllearner.algorithms.isle.index.semantic;

import java.io.File;

/**
 * Provides methods for creating semantic indexes.
 *
 * @author Daniel Fleischhacker
 */
public interface SemanticIndexFactory {
    /**
     * Returns a newly created semantic index for the collection of files contained in the given {@code directory}.
     *
     * @param inputDirectory directory containing files to create index from
     * @return semantic index for the files in the given input directory
     */
    public SemanticIndex createIndex(File inputDirectory);
}
