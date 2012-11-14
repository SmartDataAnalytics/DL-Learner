package org.dllearner.algorithm.tbsl.ltag.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.log4j.Logger;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.sem.dudes.data.Dude;
import org.dllearner.algorithm.tbsl.sem.dudes.reader.ParseException;
import org.dllearner.algorithm.tbsl.sem.util.Pair;

import com.jamonapi.Monitor;
import com.jamonapi.MonitorFactory;

public class Parser {

	private static final Logger logger = Logger.getLogger(Parser.class);

	public boolean CONSTRUCT_SEMANTICS = false;
	public boolean USE_DPS_AS_INITTREES = false;
	public boolean USE_LESS_MEMORY = false;
	public boolean SHOW_GRAMMAR = false;
	public boolean SHOW_LEXICAL_COVERAGE = false;
	public boolean VERBOSE = true;
	public String MODE = "BASIC"; // MODE ::= BASIC | LEIPZIG (set by Templator and BasicTemplator)

	private String[] input;
	private List<DerivationTree> derivationTrees = new ArrayList<DerivationTree>();
	private List<TreeNode> derivedTrees = new ArrayList<TreeNode>();
	private List<Dude> dudes = new ArrayList<Dude>();
	private ParseGrammar parseGrammar = null;
	private List<Integer> temporaryEntries = new ArrayList<Integer>();

	private GrammarFilter grammarFilter = new GrammarFilter();

	@SuppressWarnings("unchecked")
	private final Class[] operations = { Scanner.class, MoveDotDown.class,
		MoveDotUp.class, LeftPredictor.class, LeftCompletor.class,
		RightPredictor.class, RightCompletor.class, SubstPredictor.class,
		SubstCompletor.class };

	/**
	 * parses given input string by using the given grammar. Behaviour of the
	 * Parser is controlled by USE_DPS_AS_INITTREES, USE_LESS_MEMORY,
	 * SHOW_GRAMMAR and SHOW_LEXICAL_COVERAGE;
	 * 
	 * @param input
	 *            Input string to be parsed
	 * @param grammar
	 *            LTAGLexicon
	 * @return List of DerivationTrees
	 */
	public List<DerivationTree> parse(String taggeduserinput, LTAGLexicon grammar) {

		derivationTrees.clear();
		derivedTrees.clear();
		dudes.clear();
		temporaryEntries.clear();

		if (!VERBOSE) GrammarFilter.VERBOSE = false;

		/*
		 * create a local copy of the grammar with own treeIDs. This is
		 * necessary since if an input string contains the same token multiple
		 * times, a tree for each token is added. Both trees need to have
		 * different treeIDs for the parser to work correctly.
		 */
		parseGrammar = grammarFilter.filter(taggeduserinput,grammar,temporaryEntries,MODE);

		String inputNoTags = "";		
		for (String s : taggeduserinput.split(" ")) {
			inputNoTags += s.substring(0,s.indexOf("/")) + " ";
		}

		this.input = ("# ".concat(inputNoTags.replaceAll("'","").trim())).split(" ");
		int n = this.input.length;


		if (SHOW_GRAMMAR) {
			logger.debug(parseGrammar);
		}
		if (SHOW_LEXICAL_COVERAGE) {
			logger.debug("# OF TREES FOUND: " + parseGrammar.size());
			logger.debug("# OF INPUT TOKENS: " + n);
		}

		List<Pair<TreeNode, Short>> initTrees = parseGrammar.getInitTrees();

		internalParse(initTrees, n);

		if (USE_DPS_AS_INITTREES && derivationTrees.isEmpty()) {
			internalParse(parseGrammar.getDPInitTrees(), n);
		}

		if (VERBOSE) logger.debug("Constructed " + derivationTrees.size() + " derivation trees.\n");
		return derivationTrees;

	}

	public List<String> getUnknownWords(){
		return grammarFilter.getUnknownWords();
	}

	public List<DerivationTree> parseMultiThreaded(String taggeduserinput, LTAGLexicon grammar) {

		derivationTrees.clear();
		derivedTrees.clear();
		dudes.clear();
		temporaryEntries.clear();

		if (!VERBOSE) GrammarFilter.VERBOSE = false;

		/*
		 * create a local copy of the grammar with own treeIDs. This is
		 * necessary since if an input string contains the same token multiple
		 * times, a tree for each token is added. Both trees need to have
		 * different treeIDs for the parser to work correctly.
		 */
		parseGrammar = grammarFilter.filter(taggeduserinput,grammar,temporaryEntries,MODE);

		String inputNoTags = "";		
		for (String s : taggeduserinput.split(" ")) {
			inputNoTags += s.substring(0,s.indexOf("/")) + " ";
		}

		this.input = ("# ".concat(inputNoTags.replaceAll("'","").trim())).split(" ");
		int n = this.input.length;


		if (SHOW_GRAMMAR) {
			logger.debug(parseGrammar);
		}
		if (SHOW_LEXICAL_COVERAGE) {
			logger.debug("# OF TREES FOUND: " + parseGrammar.size());
			logger.debug("# OF INPUT TOKENS: " + n);
		}

		List<Pair<TreeNode, Short>> initTrees = parseGrammar.getInitTrees();

		internalParseMultiThreaded(initTrees, n);

		if (USE_DPS_AS_INITTREES && derivationTrees.isEmpty()) {
			internalParseMultiThreaded(parseGrammar.getDPInitTrees(), n);
		}

		if (VERBOSE) logger.debug("Constructed " + derivationTrees.size() + " derivation trees.\n");
		return derivationTrees;

	}

	private void internalParse(List<Pair<TreeNode, Short>> initTrees, int n) {

		TREELOOP: for (int k = 0; k < initTrees.size(); k++) {

			TreeNode tree = initTrees.get(k).getFirst();
			short tid = initTrees.get(k).getSecond();

			List<List<ParseState>> stateSets = makeStateSets();

			ParseState start = new ParseState(tree, tid);
			// the inittree is already used
			start.getUsedTrees().add(tid);

			stateSets.get(0).add(start);

			for (int i = 0; i < n; i++) {

				if (i > 0) {
					stateSets.get(i - 1).clear();
					if (USE_LESS_MEMORY) {
						System.gc();
					}
				}

				List<ParseState> localStateSet = new ArrayList<ParseState>(
						stateSets.get(i));
				List<ParseState> localStateSet2 = new ArrayList<ParseState>();

				stateSets.get(i).clear();

				while (localStateSet.size() > 0) {

					for (int j = 0; j < localStateSet.size(); j++) {
						ParseState state = localStateSet.get(j);

						List<ParseState> newStates;

						OPLOOP: for (Class<?> c : operations) {

							try {

								ParserOperation op = (ParserOperation) c
										.newInstance();

								newStates = (op.go(i, state, this.input,
										parseGrammar));

								if (!newStates.isEmpty()) {

									for (ParseState newState : newStates) {
										if (newState.i.equals(i)) {
											localStateSet2.add(newState);
										}

										if ((op instanceof Scanner)
												|| (newState.isEndState() && newState.i == n - 1)) {
											stateSets.get(newState.i).add(
													newState);
										}
									}

									op = null;
									break OPLOOP;

								}

							} catch (InstantiationException e) {
								e.printStackTrace();

							} catch (IllegalAccessException e) {
								e.printStackTrace();

							}

						}

					}

					localStateSet = null;
					localStateSet = new ArrayList<ParseState>(localStateSet2);
					localStateSet2 = new ArrayList<ParseState>();

				}

				localStateSet = null;
				localStateSet2 = null;

				/*
				 * if the parser could not scan the next input token this run /
				 * initial tree is rejected
				 */
				if (i < n - 1 && stateSets.get(i + 1).isEmpty()) {

					stateSets.get(i).clear();
					System.gc();
					continue TREELOOP;

				}

			}

			for (ParseState state : stateSets.get(n - 1)) {


				//				if (state.isEndState() && state.t.equals(tree)) {
				if (state.isEndState()) {
					if (state.t.equals(tree)) {

						derivationTrees.add(createDerivationTree(state,
								parseGrammar));

					}
				}

			}

		}

	}

	private void internalParseMultiThreaded(List<Pair<TreeNode, Short>> initTrees, int n) {
		Monitor parseMon = MonitorFactory.getTimeMonitor("parse");
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		parseMon.start();
		for (int k = 0; k < initTrees.size(); k++) {
			Pair<TreeNode, Short> pair = initTrees.get(k);
			TreeNode tree = pair.getFirst();
			short tid = pair.getSecond();
			threadPool.execute(new TreeProcessor(tree, tid, n));
		}
		threadPool.shutdown();
		while(!threadPool.isTerminated()){

		}
		parseMon.start();
	}

	private List<List<ParseState>> makeStateSets() {

		List<List<ParseState>> output = new ArrayList<List<ParseState>>();

		for (int i = 0; i < input.length; i++) {

			output.add(new ArrayList<ParseState>());
		}

		return output;
	}

	private DerivationTree createDerivationTree(ParseState state,
			ParseGrammar temporaryGrammar) {

		DerivationTree out = new DerivationTree(state.tid);

		// first used tree is the inittree itself
		for (int i=1;i<state.getUsedTrees().size();i++) {

			short tid = state.getUsedTrees().get(i);

			Operation op = new Operation();

			OperationPointer ptr = state.pointer.get(tid);

			op.setType(OperationType.ADJUNCTION);

			if (ptr == null) {

				ptr = state.substPointer.get(tid);

				op.setType(OperationType.SUBSTITUTION);

			}

			out.getTreeMappings()
			.put(tid, temporaryGrammar.getIndex().get(tid));

			op.setTid1(ptr.getTid());

			op.setTid2(tid);

			op.setAddress(ptr.getDot());

			out.getOperations().add(op);
		}

		out.getTreeMappings().put(out.getInitTreeID(), state.t);

		return out;

	}

	/**
	 * builds the derived trees from the derivation trees obtained by
	 * Parser.parse(). Parallely, the Dudes for semantic construction are
	 * constructed. The derived Trees are saved in Parser.derivedTrees.
	 * The Dudes are saved in Parser.dudes.
	 * 
	 * @param G
	 *            - required for getting the semantics
	 */
	public List<TreeNode> buildDerivedTrees(LTAGLexicon G) throws ParseException {

		for (DerivationTree d : derivationTrees) {

			List<Pair<TreeNode, Dude>> pairs = DerivedTree.build(d, parseGrammar, G,
					CONSTRUCT_SEMANTICS);

			for (Pair<TreeNode,Dude> pair : pairs) {
				TreeNode x = pair.getFirst();
				Dude dude = pair.getSecond();

				if (!derivedTrees.contains(x) || !dudes.contains(dude)) {
					derivedTrees.add(x);
					dudes.add(dude);
				}

			}

		}

		return derivedTrees;

	}

	public List<TreeNode> buildDerivedTreesMultiThreaded(LTAGLexicon G) throws ParseException {
		ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		for (DerivationTree dTree : derivationTrees) {
			threadPool.execute(new DerivationTreeProcessor(dTree, G));
		}
		threadPool.shutdown();
		while(!threadPool.isTerminated()){

		}

		return derivedTrees;

	}

	/**
	 * get List of Dudes parallely constructed by Parser.buildDerivedTrees()
	 */
	public List<Dude> getDudes() {
		return dudes;
	}

	/**
	 * get List of DerivationTrees
	 */
	public List<DerivationTree> getDerivationTrees() {
		return derivationTrees;
	}

	/**
	 * get the List of DerivedTrees
	 */
	public List<TreeNode> getDerivedTrees() {
		return derivedTrees;
	}

	/**
	 * get the List of temporary entries in the LTAGLexicon
	 */
	public List<Integer> getTemps() {
		return temporaryEntries;
	}

	/**
	 * clears all results of this instance of Parser. The instance can then be
	 * used for a new parse
	 */
	public void clear(LTAGLexicon grammar,List<Integer> temps) {

		this.input = null;
		derivationTrees = new ArrayList<DerivationTree>();
		derivedTrees = new ArrayList<TreeNode>();
		new ArrayList<Dude>();
		parseGrammar = null;

		grammar.clear(temps);

	}

	class TreeProcessor implements Runnable{


		private TreeNode tree;
		private short tid;
		private int n;

		public TreeProcessor(TreeNode tree, short tid, int n) {
			this.tree = tree;
			this.tid = tid;
			this.n = n;
		}

		@Override
		public void run() {
			List<List<ParseState>> stateSets = makeStateSets();

			ParseState start = new ParseState(tree, tid);
			// the inittree is already used
			start.getUsedTrees().add(tid);

			stateSets.get(0).add(start);
			boolean skip = false;
			for (int i = 0; i < n; i++) {

				if (i > 0) {
					stateSets.get(i - 1).clear();
					if (USE_LESS_MEMORY) {
						System.gc();
					}
				}

				List<ParseState> localStateSet = new ArrayList<ParseState>(
						stateSets.get(i));
				List<ParseState> localStateSet2 = new ArrayList<ParseState>();

				stateSets.get(i).clear();

				while (localStateSet.size() > 0) {

					for (int j = 0; j < localStateSet.size(); j++) {
						ParseState state = localStateSet.get(j);

						List<ParseState> newStates;

						OPLOOP: for (Class<?> c : operations) {

							try {

								ParserOperation op = (ParserOperation) c
										.newInstance();

								newStates = (op.go(i, state, input,
										parseGrammar));

								if (!newStates.isEmpty()) {

									for (ParseState newState : newStates) {
										if (newState.i.equals(i)) {
											localStateSet2.add(newState);
										}

										if ((op instanceof Scanner)
												|| (newState.isEndState() && newState.i == n - 1)) {
											if(newState.i<stateSets.size())
											{
												stateSets.get(newState.i).add(newState);
											}
										}
									}

									op = null;
									break OPLOOP;

								}

							} catch (InstantiationException e) {
								e.printStackTrace();

							} catch (IllegalAccessException e) {
								e.printStackTrace();

							}

						}

					}

					localStateSet = null;
					localStateSet = new ArrayList<ParseState>(localStateSet2);
					localStateSet2 = new ArrayList<ParseState>();

				}

				localStateSet = null;
				localStateSet2 = null;

				/*
				 * if the parser could not scan the next input token this run /
				 * initial tree is rejected
				 */
				if (i < n - 1 && stateSets.get(i + 1).isEmpty()) {

					stateSets.get(i).clear();
					skip = true;
					break;

				}

			}

			if(!skip){
				for (ParseState state : stateSets.get(n - 1)) {


					//					if (state.isEndState() && state.t.equals(tree)) {
					if (state.isEndState()) {
						if (state.t.equals(tree)) {

							derivationTrees.add(createDerivationTree(state,
									parseGrammar));

						}
					}

				}
			}


		}

	}

	class DerivationTreeProcessor implements Runnable{

		private DerivationTree dTree;
		private LTAGLexicon lexicon;

		public DerivationTreeProcessor(DerivationTree dTree, LTAGLexicon lexicon) {
			this.dTree = dTree;
			this.lexicon = lexicon;
		}

		@Override
		public void run() {
			try {
				List<Pair<TreeNode, Dude>> pairs = DerivedTree.build(dTree, parseGrammar, lexicon, CONSTRUCT_SEMANTICS);

				for (Pair<TreeNode,Dude> pair : pairs) {
					TreeNode x = pair.getFirst();
					Dude dude = pair.getSecond();

					if (!derivedTrees.contains(x) || !dudes.contains(dude)) {
						derivedTrees.add(x);
						dudes.add(dude);
					}

				}
			} catch (ParseException e) {
				e.printStackTrace();
			}

		}

	}

}
