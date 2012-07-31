package org.dllearner.algorithm.tbsl.nlp;

public class SynchronizedStanfordPartOfSpeechTagger extends StanfordPartOfSpeechTagger
{
	@Override public synchronized String tag(String sentence) {return super.tag(sentence);}
}