package org.dllearner.algorithms.isle.index;

/**
 * Different levels of surface forms supported by the {@link TextDocument} class. Used for retrieving certain types
 * of texts.
 *
 * @author Daniel Fleischhacker
 */
public enum SurfaceFormLevel {
        RAW,
        POS_TAGGED,
        STEMMED
}