package org.dllearner.learningproblems.sampling.r2v.tfidf.ngram;

import java.util.List;

/**
 * Break text into tokens
 */
public interface Tokenizer {
    List<String> tokenize(String text);
}
