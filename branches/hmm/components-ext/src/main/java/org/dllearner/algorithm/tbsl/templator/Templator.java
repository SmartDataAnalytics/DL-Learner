package org.dllearner.algorithm.tbsl.templator;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import net.didion.jwnl.data.POS;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.tbsl.converter.DRS2SPARQL_Converter;
import org.dllearner.algorithm.tbsl.converter.DUDE2UDRS_Converter;
import org.dllearner.algorithm.tbsl.ltag.parser.LTAGLexicon;
import org.dllearner.algorithm.tbsl.ltag.parser.LTAG_Lexicon_Constructor;
import org.dllearner.algorithm.tbsl.ltag.parser.Parser;
import org.dllearner.algorithm.tbsl.ltag.parser.Preprocessor;
import org.dllearner.algorithm.tbsl.nlp.LingPipeLemmatizer;
import org.dllearner.algorithm.tbsl.nlp.PartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.StanfordPartOfSpeechTagger;
import org.dllearner.algorithm.tbsl.nlp.WordNet;
import org.dllearner.algorithm.tbsl.sem.drs.DRS;
import org.dllearner.algorithm.tbsl.sem.drs.UDRS;
import org.dllearner.algorithm.tbsl.sem.dudes.data.Dude;
import org.dllearner.algorithm.tbsl.sem.dudes.reader.ParseException;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.SlotType;
import org.dllearner.algorithm.tbsl.sparql.Template;

public class Templator {

	private static final Logger logger = Logger.getLogger(Templator.class);

	String[] GRAMMAR_FILES = {"tbsl/lexicon/english.lex","tbsl/lexicon/english_oxford.lex"};

	private String[] noun = {"NN","NNS","NNP","NNPS","NPREP","JJNN","JJNPREP"};
	private String[] adjective = {"JJ","JJR","JJS","JJH"};
	private String[] verb = {"VB","VBD","VBG","VBN","VBP","VBZ","PASSIVE","PASSPART","VPASS","VPASSIN","GERUNDIN","VPREP","WHEN","WHERE"};

	PartOfSpeechTagger tagger;
	LTAGLexicon g;
	LTAG_Lexicon_Constructor LTAG_Constructor = new LTAG_Lexicon_Constructor();

	Parser parser;
	Preprocessor pp;

	WordNet wordnet;
	LingPipeLemmatizer lem = new LingPipeLemmatizer();

	DUDE2UDRS_Converter d2u = new DUDE2UDRS_Converter();
	DRS2SPARQL_Converter d2s = new DRS2SPARQL_Converter();

	boolean ONE_SCOPE_ONLY = true;
	boolean UNTAGGED_INPUT = true;
	boolean USE_NER = false;
	boolean USE_WORDNET = true;
	boolean VERBOSE = true;

	private String taggedInput;

	private Set<Template> templates;
	private Set<DRS> drses;

	public Templator() {
		this(new StanfordPartOfSpeechTagger(), new WordNet());
	}

	public Templator(final PartOfSpeechTagger tagger) {
		this(tagger, new WordNet());
	}

	public Templator(final PartOfSpeechTagger tagger, WordNet wordnet) {
		this.tagger = tagger;
		this.wordnet = wordnet;

		List<InputStream> grammarFiles = new ArrayList<InputStream>();
		for(int i = 0; i < GRAMMAR_FILES.length; i++){
			grammarFiles.add(this.getClass().getClassLoader().getResourceAsStream(GRAMMAR_FILES[i]));
		}

		g = LTAG_Constructor.construct(grammarFiles);

		parser = new Parser();
		parser.SHOW_GRAMMAR = true;
		parser.USE_DPS_AS_INITTREES = true;
		parser.CONSTRUCT_SEMANTICS = true;
		parser.MODE = "LEIPZIG";

		pp = new Preprocessor(USE_NER);
	}

	public Templator(final PartOfSpeechTagger tagger, WordNet wordnet, String[] GRAMMAR_FILES) {
		this.tagger = tagger;
		this.wordnet = wordnet;
		this.GRAMMAR_FILES = GRAMMAR_FILES;

		List<InputStream> grammarFiles = new ArrayList<InputStream>();
		for(int i = 0; i < GRAMMAR_FILES.length; i++) {
			grammarFiles.add(this.getClass().getClassLoader().getResourceAsStream(GRAMMAR_FILES[i]));
		}

		g = LTAG_Constructor.construct(grammarFiles);

		parser = new Parser();
		parser.SHOW_GRAMMAR = true;
		parser.USE_DPS_AS_INITTREES = true;
		parser.CONSTRUCT_SEMANTICS = true;
		parser.MODE = "LEIPZIG";

		pp = new Preprocessor(USE_NER);
	}

	public Templator(boolean b) {
		this.tagger = new StanfordPartOfSpeechTagger();
		this.USE_WORDNET = false;
		VERBOSE = b;

		List<InputStream> grammarFiles = new ArrayList<InputStream>();
		for(int i = 0; i < GRAMMAR_FILES.length; i++){
			grammarFiles.add(this.getClass().getClassLoader().getResourceAsStream(GRAMMAR_FILES[i]));
		}

		g = LTAG_Constructor.construct(grammarFiles);

		parser = new Parser();
		parser.SHOW_GRAMMAR = false;
		parser.VERBOSE = b;
		parser.USE_DPS_AS_INITTREES = true;
		parser.CONSTRUCT_SEMANTICS = true;
		parser.MODE = "LEIPZIG";

		pp = new Preprocessor(USE_NER);
		pp.setVERBOSE(b);
	}

	public void setUNTAGGED_INPUT(boolean b) {
		UNTAGGED_INPUT = b;
	}
	public void setUSE_NER(boolean b) {
		USE_NER = b;
	}
	public void setVERBOSE(boolean b) {
		VERBOSE = b;
	}
	public void setGrammarFiles(String[] gf) {
		GRAMMAR_FILES = gf;
		List<InputStream> grammarFiles = new ArrayList<InputStream>();
		for(int i = 0; i < GRAMMAR_FILES.length; i++){
			grammarFiles.add(this.getClass().getClassLoader().getResourceAsStream(GRAMMAR_FILES[i]));
		}	
		g = LTAG_Constructor.construct(grammarFiles);
	}

	public Set<Template> buildTemplates(String s) {return buildTemplates(s,true);}
	public Set<Template> buildTemplates(String s, boolean createSynonyms) {

		d2s.setInputString(s);

		boolean clearAgain = true;

		String tagged;
		if (UNTAGGED_INPUT) {		
			s = pp.normalize(s);
			tagged = tagger.tag(s);
			logger.debug("Tagged input: " + tagged);
		}
		else {
			tagged = s;
			s = extractSentence(tagged);
		}
		taggedInput = tagged;
		String newtagged;
		if (USE_NER) {
			newtagged = pp.condenseNominals(pp.findNEs(tagged,s));
		} 
		else newtagged = pp.condenseNominals(tagged);

		newtagged = pp.condense(newtagged);
		logger.debug("Preprocessed: " + newtagged); 

		parser.parse(newtagged,g);

		if (parser.getDerivationTrees().isEmpty()) {
			parser.clear(g,parser.getTemps());
			clearAgain = false;
			if (VERBOSE) logger.error("[Templator.java] '" + s + "' could not be parsed.");
		}
		else {
			try {
				parser.buildDerivedTrees(g);
			} catch (ParseException e) {
				if (VERBOSE) logger.error("[Templator.java] ParseException at '" + e.getMessage() + "'", e);
			}
		}

		// build pairs <String,POStag> from tagged
		Hashtable<String,String> postable = new Hashtable<String,String>();
		for (String st : newtagged.split(" ")) {
			postable.put(st.substring(0,st.indexOf("/")).toLowerCase(),st.substring(st.indexOf("/")+1));;
		}
		//

		Set<DRS> drses = new HashSet<DRS>();
		Set<Template> templates = new HashSet<Template>();

		for (Dude dude : parser.getDudes()) {
			UDRS udrs = d2u.convert(dude);
			if (udrs != null) { 

				for (DRS drs : udrs.initResolve()) {

					List<Slot> slots = new ArrayList<Slot>();
					slots.addAll(dude.getSlots());
					d2s.setSlots(slots);
					d2s.redundantEqualRenaming(drs);

					if (!containsModuloRenaming(drses,drs)) {
						//                    	// DEBUG
						if (VERBOSE) {
							logger.debug(">>> DUDE:\n" + dude.toString());
							logger.debug("\n>>> DRS:\n"+ drs.toString());
							for (Slot sl : slots) {
								logger.debug(sl.toString());
							}
						}
						//                		//
						drses.add(drs);

						try {
							Template temp = d2s.convert(drs,slots);
							if (temp == null) { continue; }
							temp = temp.checkandrefine(); 
							if (temp == null) { continue; }         

							if (USE_WORDNET) { // find WordNet synonyms
								List<String> newwords;
								String word; 
								String pos;
								for (Slot slot : temp.getSlots()) {
									if (!slot.getWords().isEmpty()) {

										word = slot.getWords().get(0);
										pos = postable.get(word.toLowerCase().replace(" ","_"));

										POS wordnetpos = null;
										if (pos != null) {
											if (equalsOneOf(pos,noun)) {
												wordnetpos = POS.NOUN;
											}
											else if (equalsOneOf(pos,adjective)) {
												wordnetpos = POS.ADJECTIVE;
											}
											else if (equalsOneOf(pos,verb)) {
												wordnetpos = POS.VERB;
											}
										}

										List<String> strings = new ArrayList<String>();
										if(createSynonyms)
										{
											if (wordnetpos != null && wordnetpos.equals(POS.ADJECTIVE)) {strings = wordnet.getAttributes(word);}
										}

										newwords = new ArrayList<String>();
										newwords.addAll(slot.getWords());
										newwords.addAll(strings);            					

										if(createSynonyms)
										{
											if (wordnetpos != null && !slot.getSlotType().equals(SlotType.RESOURCE)) {
												newwords.addAll(wordnet.getBestSynonyms(wordnetpos,getLemmatizedWord(word)));
												for (String att : getLemmatizedWords(strings)) {
													newwords.addAll(wordnet.getBestSynonyms(wordnetpos,att));
												}
											}
										}
										if (newwords.isEmpty()) {
											newwords.add(slot.getWords().get(0));
										}
										List<String> newwordslist = new ArrayList<String>();
										newwordslist.addAll(newwords);
										slot.setWords(newwordslist);
									}
								}
							}
							// 

							templates.add(temp);
						} catch (java.lang.ClassCastException e) {
							continue;
						}
						if (ONE_SCOPE_ONLY) { break; }
					}	
				}
			}
		}

		if (clearAgain) {
			parser.clear(g,parser.getTemps());
		}
		//        System.gc();

		return templates;
	}

	public Set<Template> buildTemplatesMultiThreaded(String s) {return buildTemplates(s,true);}
	public Set<Template> buildTemplatesMultiThreaded(String s,boolean createSynonyms) {

		boolean clearAgain = true;

		String tagged;
		if (UNTAGGED_INPUT) {		
			s = pp.normalize(s);
			tagged = tagger.tag(s);
			logger.debug("Tagged input: " + tagged);
		}
		else {
			tagged = s;
			s = extractSentence(tagged);
		}
		taggedInput = tagged;
		String newtagged;
		if (USE_NER) {
			newtagged = pp.condenseNominals(pp.findNEs(tagged,s));
		} 
		else newtagged = pp.condenseNominals(tagged);

		newtagged = pp.condense(newtagged);
		logger.debug("Preprocessed: " + newtagged); 

		parser.parseMultiThreaded(newtagged,g);

		if (parser.getDerivationTrees().isEmpty()) {
			parser.clear(g,parser.getTemps());
			clearAgain = false;
			logger.error("[Templator.java] '" + s + "' could not be parsed.");
		}
		else {
			try {
				parser.buildDerivedTreesMultiThreaded(g);
			} catch (ParseException e) {
				logger.error("[Templator.java] ParseException at '" + e.getMessage() + "'", e);
			}
		}

		// build pairs <String,POStag> from tagged
		Hashtable<String,String> postable = new Hashtable<String,String>();
		for (String st : newtagged.split(" ")) {
			postable.put(st.substring(0,st.indexOf("/")).toLowerCase(),st.substring(st.indexOf("/")+1));;
		}
		//

		drses = new HashSet<DRS>();
		templates = new HashSet<Template>();

		//        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		//        for (Dude dude : p.getDudes()) {
		//           threadPool.execute(new DudeProcessor(dude, postable));
		//        }
		//        threadPool.shutdown();
		//		while(!threadPool.isTerminated()){}

		for (Dude dude : parser.getDudes()) {

			UDRS udrs = d2u.convert(dude);
			if (udrs != null) { 

				for (DRS drs : udrs.initResolve()) {

					List<Slot> slots = new ArrayList<Slot>();
					slots.addAll(dude.getSlots());
					d2s.setSlots(slots);
					d2s.redundantEqualRenaming(drs);

					if (!containsModuloRenaming(drses,drs)) {
						//                   	// DEBUG
						logger.debug(dude);
						logger.debug(drs);
						for (Slot sl : slots) {
							logger.debug(sl.toString());
						}
						//               		//
						drses.add(drs);

						try {
							Template temp = d2s.convert(drs,slots);
							if (temp == null) { continue; }
							temp = temp.checkandrefine(); 
							if (temp == null) { continue; }   


							if (USE_WORDNET) { // find WordNet synonyms
								List<String> newwords;
								String word; 
								String pos;
								for (Slot slot : temp.getSlots()) {
									if (!slot.getWords().isEmpty()) {

										word = slot.getWords().get(0);
										pos = postable.get(word.toLowerCase().replace(" ","_"));

										POS wordnetpos = null;
										if (pos != null) {
											if (equalsOneOf(pos,noun)) {
												wordnetpos = POS.NOUN;
											}
											else if (equalsOneOf(pos,adjective)) {
												wordnetpos = POS.ADJECTIVE;
											}
											else if (equalsOneOf(pos,verb)) {
												wordnetpos = POS.VERB;
											}
										}

										List<String> strings = new ArrayList<String>();
										if(createSynonyms)
										{
											if (wordnetpos != null && wordnetpos.equals(POS.ADJECTIVE)) {
												strings = wordnet.getAttributes(word);
											}
										}
										newwords = new ArrayList<String>();
										newwords.addAll(slot.getWords());
										newwords.addAll(strings);            					

										if(createSynonyms)
										{
											if (wordnetpos != null && !slot.getSlotType().equals(SlotType.RESOURCE)) {
												newwords.addAll(wordnet.getBestSynonyms(wordnetpos,getLemmatizedWord(word)));
												for (String att : getLemmatizedWords(strings)) {
													newwords.addAll(wordnet.getBestSynonyms(wordnetpos,att));
												}
											}
										}
										if (newwords.isEmpty()) {
											newwords.add(slot.getWords().get(0));
										}
										List<String> newwordslist = new ArrayList<String>();
										newwordslist.addAll(newwords);
										slot.setWords(newwordslist);
									}
								}
							}
							// 

							templates.add(temp);
						} catch (java.lang.ClassCastException e) {
							continue;
						}
						if (ONE_SCOPE_ONLY) { break; }
					}	
				}

			}
		}


		if (clearAgain) {
			parser.clear(g,parser.getTemps());
		}
		//        System.gc();

		return templates;
	}

	public String getTaggedInput() {
		return taggedInput;
	}

	public List<String> getUnknownWords(){
		return parser.getUnknownWords();
	}

	private List<String> getLemmatizedWords(List<String> words){
		List<String> stemmed = new ArrayList<String>();
		for(String word : words){
			//currently only stem single words
			if(word.contains(" ")){
				stemmed.add(word);
			} else {
				stemmed.add(getLemmatizedWord(word));
			}

		}
		return stemmed;
	}

	private String getLemmatizedWord(String word){
		return lem.stem(word);
	}

	private boolean containsModuloRenaming(Set<DRS> drses, DRS drs) {

		for (DRS d : drses) {
			if (d.equalsModuloRenaming(drs)) {
				return true;
			}
		}
		return false;
	}

	private boolean equalsOneOf(String string,String[] strings) {
		for (String s : strings) {
			if (string.equals(s)) {
				return true;
			}
		}
		return false;
	}

	private String extractSentence(String taggedSentence){
		int pos = taggedSentence.indexOf("/");
		while(pos != -1){
			String first = taggedSentence.substring(0, pos);
			int endPos = taggedSentence.substring(pos).indexOf(" ");
			if(endPos == -1){
				endPos = taggedSentence.substring(pos).length();
			}
			String rest = taggedSentence.substring(pos + endPos);

			taggedSentence = first + rest;
			pos = taggedSentence.indexOf("/");

		}
		return taggedSentence;

	}

	class DudeProcessor implements Runnable{

		private Dude dude;
		private Hashtable<String,String> postable;

		public DudeProcessor(Dude dude, Hashtable<String,String> postable) {
			this.dude = dude;
			this.postable = postable;
		}

		@Override
		public void run() {
			UDRS udrs = d2u.convert(dude);
			if (udrs != null) { 

				for (DRS drs : udrs.initResolve()) {

					List<Slot> slots = new ArrayList<Slot>();
					slots.addAll(dude.getSlots());
					d2s.setSlots(slots);
					d2s.redundantEqualRenaming(drs);

					if (!containsModuloRenaming(drses,drs)) {
						//	                    	// DEBUG
						if (VERBOSE) {
							logger.debug(dude);
							logger.debug(drs);
							for (Slot sl : slots) {
								logger.debug(sl.toString());
							}
						}
						//	                		//
						drses.add(drs);

						try {
							Template temp = d2s.convert(drs,slots);
							temp = temp.checkandrefine();
							if (temp == null) {
								continue;
							}

							if (USE_WORDNET) { // find WordNet synonyms
								List<String> newwords;
								String word; 
								String pos;
								for (Slot slot : temp.getSlots()) {
									if (!slot.getWords().isEmpty()) {

										word = slot.getWords().get(0);
										pos = postable.get(word.toLowerCase().replace(" ","_"));

										POS wordnetpos = null;
										if (pos != null) {
											if (equalsOneOf(pos,noun)) {
												wordnetpos = POS.NOUN;
											}
											else if (equalsOneOf(pos,adjective)) {
												wordnetpos = POS.ADJECTIVE;
											}
											else if (equalsOneOf(pos,verb)) {
												wordnetpos = POS.VERB;
											}
										}

										List<String> strings = new ArrayList<String>();
										if (wordnetpos != null && wordnetpos.equals(POS.ADJECTIVE)) {
											strings = wordnet.getAttributes(word);
										}

										newwords = new ArrayList<String>();
										newwords.addAll(slot.getWords());
										newwords.addAll(strings);            					

										if (wordnetpos != null && !slot.getSlotType().equals(SlotType.RESOURCE)) {
											newwords.addAll(wordnet.getBestSynonyms(wordnetpos,getLemmatizedWord(word)));
											for (String att : getLemmatizedWords(strings)) {
												newwords.addAll(wordnet.getBestSynonyms(wordnetpos,att));
											}
										}
										if (newwords.isEmpty()) {
											newwords.add(slot.getWords().get(0));
										}
										List<String> newwordslist = new ArrayList<String>();
										newwordslist.addAll(newwords);
										slot.setWords(newwordslist);
									}
								}
							}
							// 

							templates.add(temp);
						} catch (java.lang.ClassCastException e) {
							continue;
						}
						if (ONE_SCOPE_ONLY) { break; }
					}	
				}
			}
		}

	}

}
