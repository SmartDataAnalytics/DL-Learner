package org.dllearner.utilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.didion.jwnl.JWNL;
import net.didion.jwnl.JWNLException;
import net.didion.jwnl.data.IndexWord;
import net.didion.jwnl.data.POS;
import net.didion.jwnl.data.PointerTarget;
import net.didion.jwnl.data.PointerType;
import net.didion.jwnl.data.Synset;
import net.didion.jwnl.data.Word;
import net.didion.jwnl.dictionary.Dictionary;

public class WordnetSimilarity {
	
	public Dictionary dict;	
	
	public WordnetSimilarity(){
		try {
			JWNL.initialize(this.getClass().getClassLoader().getResourceAsStream("wordnet_properties.xml"));
			dict = Dictionary.getInstance();
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	public double computeSimilarity(String s1, String s2, POS pos){
		List<String> synonyms = new ArrayList<String>();
		
		try {
			IndexWord iw1 = dict.getIndexWord(pos, s1);
			IndexWord iw2 = dict.getIndexWord(pos, s2);//dict.getMorphologicalProcessor().lookupBaseForm(pos, s)
//			IndexWord iw = dict.getMorphologicalProcessor().lookupBaseForm(pos, s);
			getUpwardHierachy(s1, pos);
			getUpwardHierachy(s2, pos);
			
			ICFinder icFinder = new ICFinder("src/main/resources/ic-semcor.dat");
			Synset synset1 = iw1.getSenses()[0];
			Synset synset2 = iw2.getSenses()[0];
			Synset lcs = getLCS(synset1, synset2, "NN", icFinder);
			System.out.println(lcs);
			
			for(Synset synset : iw1.getSenses()){
				for(List<PointerTarget> tree : getHypernymTrees(synset, new HashSet<PointerTarget>())){
					for(PointerTarget t : tree){
						System.out.print(((Synset)t).getWords()[0].getLemma() + "-->");
					}
					System.out.println();
				}
			}
			
			
			
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		
		
		return -1;
	}
	
	private List<PointerTarget> getUpwardHierachy(PointerTarget target){
		List<PointerTarget> hierarchy = new ArrayList<PointerTarget>();
		try {
			PointerTarget[] targets = target.getTargets(PointerType.HYPERNYM);
			for (PointerTarget t : targets) {
				hierarchy.add(t);
				hierarchy.addAll(getUpwardHierachy(t));
			}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		return hierarchy;
		
	}
	
//	private List<List<PointerTarget>> getUpwardHierachies(List<List<PointerTarget>> targets){
//		List<List<PointerTarget>> hierarchies = new ArrayList<List<PointerTarget>>();
//		try {
//			PointerTarget[] targets = target.getTargets(PointerType.HYPERNYM);
//			for (PointerTarget t : targets) {
//				hierarchy.add(t);
//				hierarchy.addAll(getUpwardHierachy(t));
//			}
//		} catch (JWNLException e) {
//			e.printStackTrace();
//		}
//		return hierarchy;
//		
//	}
	
	private void getUpwardHierachy(String word, POS pos){
		try {
			IndexWord iw = dict.getIndexWord(pos, word);
			for(Synset synset : iw.getSenses()){
				for(PointerTarget t : getUpwardHierachy(synset)){
					System.out.print(((Synset)t).getWord(0).getLemma() + "-->");
				}
				System.out.println();
			}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
		
	}
	
	private void getHypernyms(IndexWord iw){
		try {
			if(iw != null){
				Synset[] synsets = iw.getSenses();
				for(Synset s : synsets){
					System.out.println(s);
					PointerTarget[] targets = s.getTargets(PointerType.HYPERNYM);
					for (PointerTarget target : targets) {
						Word[] words = ((Synset) target).getWords();
						for (Word word : words) {
							System.out.println(word);
						}
					}
				}
			}
		} catch (JWNLException e) {
			e.printStackTrace();
		}
	}
	
	public Synset getLCS(Synset synset1, Synset synset2, String pos, ICFinder icFinder) throws JWNLException
    {
            // synset1
            HashSet<Synset> s1 = new HashSet<Synset>(); s1.add(synset1);
            HashSet<Synset> h1 = new HashSet<Synset>();
            h1 = getHypernyms(s1,h1);
            // !!! important !!! we must add the original {synset} back in, as the 2 {synsets}(senses) we are comparing may be equivalent i.e. bthe same {synset}!
            h1.add(synset1);
            //System.out.println(">>>>>>>>>>>>>>>>>>>>>");
            // synset2
            HashSet<Synset> s2 = new HashSet<Synset>();  s2.add(synset2);
            HashSet<Synset> h2 = new HashSet<Synset>();
            h2  = getHypernyms(s2,h2);
            h2.add(synset2); // ??? don't really need this ???
            //System.out.println("JWNL,h1, "+toStr(synset1.getWords())+", :h2, "+toStr(synset2.getWords())+" ,=, "+h1.size()+", "+h2.size());
            // get the candidate <lcs>s i.e. the intersection of all <hypernyms> | {synsets} which subsume the 2 {synsets}
            /*System.out.println("========================");
            System.out.println(h1);
            System.out.println(h2);
            System.out.println("========================");*/
            h1.retainAll(h2);
            if(h1.isEmpty())
            {
                    return (null); // i.e. there is *no* <LCS> for the 2 synsets
            }

            // get *a* <lcs> with the highest Information Content
            double          max             = -Double.MAX_VALUE;
            Synset  maxlcs  =       null;
            for (Synset h : h1) 
            {
                    double ic = icFinder.getIC("" + h.getOffset(), pos); // use ICfinder to get the Information Content value
                    if(ic > max)
                    {
                            max             =       ic;
                            maxlcs  =       h;
                    }
            }
            return maxlcs; // return the <synset} with *a* highest IC value
    }


    // 1.1 GET <HYPERNYMS>
    private  HashSet<Synset> getHypernyms(HashSet<Synset> synsets, HashSet<Synset> allhypernms) throws JWNLException
    {
            if(allhypernms.size()>= 100){
                    return allhypernms;
            }
            
            //System.out.println("IP: " + synsets);
            HashSet<Synset>         hypernyms       =       new HashSet<Synset>();
            for(Synset s : synsets)
            {       

                    PointerTarget[] hyp = s.getTargets(PointerType.HYPERNYM);                                       // get the <hypernyms> if there are any
                    for (PointerTarget pointerTarget : hyp) {
                            if (pointerTarget instanceof Synset) {
                                    Synset poiSyn = (Synset) pointerTarget;
                                    hypernyms.add(poiSyn);
                            }/*else{
                                    //System.out.println("PointerTarget is not instanceof Synset: "+pointerTarget);
                            }*/
                    }
                    //System.out.println("\t"+hypernyms);
            }
            if(!hypernyms.isEmpty())
            {
                    if(allhypernms.size()+hypernyms.size()>= 100){
                            return allhypernms;
                    }
                    try {
                            allhypernms.addAll(hypernyms);
                    } catch (StackOverflowError e) {
                            //System.out.println(allhypernms.size());
                            //System.out.println(hypernyms.size());
                            //e.printStackTrace();
                            System.gc();
                            System.gc();
                            System.err.println(e.getMessage());
                            return allhypernms;
                    }
                    allhypernms = getHypernyms(hypernyms, allhypernms);
            }
            //System.out.println(allhypernms);
            return allhypernms;
    }
    
    /**
     * since this method is heavily used, inner cache would help for e.g.
     * calculating similarity matrix
     * 
     * Suroutine that returns an array of hypernym trees, given the offset of #
     * the synset. Each hypernym tree is an array of offsets.
     * 
     * @param synset
     * @param mode
     */
    public List<List<PointerTarget>> getHypernymTrees(PointerTarget synset, Set<PointerTarget> history) {
    	PointerTarget key = synset;
            
            // check if the input synset is one of the imaginary root nodes
            if (synset.equals(new Synset(POS.NOUN, 0, new Word[]{new Word("ROOT", "ROOT", 0)}, null, null, null))) {
                    List<PointerTarget> tree = new ArrayList<PointerTarget>();
                    tree.add(new Synset(POS.NOUN, 0, new Word[]{new Word("ROOT", "ROOT", 0)}, null, null, null));
                    List<List<PointerTarget>> trees = new ArrayList<List<PointerTarget>>();
                    trees.add(tree);
                    return trees;
            }

            List<PointerTarget> synlinks = null;
			try {
				synlinks = Arrays.asList(synset.getTargets(PointerType.HYPERNYM));
			} catch (JWNLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            List<List<PointerTarget>> returnList = new ArrayList<List<PointerTarget>>();
            if (synlinks.size() == 0) {
                    List<PointerTarget> tree = new ArrayList<PointerTarget>();
                    tree.add(synset);
                    tree.add(0, new Synset(POS.NOUN, 0, new Word[]{new Word("ROOT", "ROOT", 0)}, null, null, null));
                    returnList.add(tree);
            } else {
                    for (PointerTarget hypernym : synlinks) {
                            if ( history.contains(hypernym) ) continue;
                            history.add(hypernym);
                            
                            List<List<PointerTarget>> hypernymTrees = getHypernymTrees(hypernym, history);
                            if ( hypernymTrees!=null ) { 
                                    for (List<PointerTarget> hypernymTree : hypernymTrees) {
                                            hypernymTree.add(synset);
                                            returnList.add(hypernymTree);
                                    }
                            }
                            if (returnList.size() == 0) {
                                    List<PointerTarget> newList = new ArrayList<PointerTarget>();
                                    newList.add(synset);
                                    newList.add(0, new Synset(POS.NOUN, 0, new Word[]{new Word("ROOT", "ROOT", 0)}, null, null, null));
                                    returnList.add(newList);
                            }
                    }
            }

            return returnList;
    }
	
	
	public static void main(String[] args) {
		System.out.println(new WordnetSimilarity().computeSimilarity("writer", "teacher", POS.NOUN));
		
//		ILexicalDatabase db = new NictWordNet();
//		System.out.println(new Lin(db).calcRelatednessOfWords("writer", "teacher"));
	}

}
