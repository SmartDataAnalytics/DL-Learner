package org.dllearner.algorithm.tbsl.templator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.dllearner.algorithm.tbsl.converter.DRS2SPARQL_Converter;
import org.dllearner.algorithm.tbsl.converter.DUDE2UDRS_Converter;
import org.dllearner.algorithm.tbsl.ltag.data.TreeNode;
import org.dllearner.algorithm.tbsl.ltag.parser.LTAGLexicon;
import org.dllearner.algorithm.tbsl.ltag.parser.LTAG_Lexicon_Constructor;
import org.dllearner.algorithm.tbsl.ltag.parser.Parser;
import org.dllearner.algorithm.tbsl.ltag.parser.Preprocessor;
import org.dllearner.algorithm.tbsl.sem.drs.DRS;
import org.dllearner.algorithm.tbsl.sem.drs.UDRS;
import org.dllearner.algorithm.tbsl.sem.dudes.data.Dude;
import org.dllearner.algorithm.tbsl.sem.dudes.reader.ParseException;
import org.dllearner.algorithm.tbsl.sparql.Slot;
import org.dllearner.algorithm.tbsl.sparql.Template;

public class Templator {
	
	String[] GRAMMAR_FILES = {"src/main/resources/tbsl/lexicon/english.lex"};
	
	POStagger tagger;
	LTAGLexicon g;
	LTAG_Lexicon_Constructor LTAG_Constructor = new LTAG_Lexicon_Constructor();
	Parser p;
	
	
	public Templator() {
		
        g = LTAG_Constructor.construct(Arrays.asList(GRAMMAR_FILES));

        tagger = null;
		try {
			tagger = new POStagger();
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
		}
		
	    p = new Parser();
	    p.SHOW_GRAMMAR = true;
	    p.USE_DPS_AS_INITTREES = true;
	    p.CONSTRUCT_SEMANTICS = true;
	}

	public Set<Template> buildTemplates(String s) {
		
        DUDE2UDRS_Converter d2u = new DUDE2UDRS_Converter();
        DRS2SPARQL_Converter d2s = new DRS2SPARQL_Converter();
		boolean clearAgain = true;
        
	    s = Preprocessor.normalize(s);
        String tagged = tagger.tag(s);
        System.out.println("Tagged input: " + tagged);
        
		String newtagged = Preprocessor.condenseNominals(tagged);
        newtagged = Preprocessor.condense(newtagged);
		System.out.println("Preprocessed: " + newtagged);
        
        p.parse(newtagged,g);
        
        if (p.getDerivationTrees().isEmpty()) {
            p.clear(g,p.getTemps());
            clearAgain = false;
            System.out.println("[Templator.java] '" + s + "' could not be parsed.");
        }
        else {
        try {
            for (TreeNode dtree : p.buildDerivedTrees(g)) {
                if (!dtree.getAnchor().trim().equals(tagged.toLowerCase())) {
                    System.err.println("[Templator.java] Anchors don't match the input. (Nevermind...)");
                    break;
                }
            }
        } catch (ParseException e) {
            System.err.println("[Templator.java] ParseException at '" + e.getMessage() + "'");
        }
        }

        List<DRS> drses;
        Set<Template> templates = new HashSet<Template>();
        
        for (Dude dude : p.getDudes()) {
//        	System.out.println("DUDE: " + dude); // DEBUG
            UDRS udrs = d2u.convert(dude);
            if (udrs != null) { 
                drses = new ArrayList<DRS>();
                drses.addAll(udrs.initResolve());
                for (DRS drs : drses) {
//                	System.out.println("DRS:  " + drs); // DEBUG
                	List<Slot> slots = new ArrayList<Slot>();
                	slots.addAll(dude.getSlots());
//                	//DEBUG 
//                	for (Slot sl : slots) {
//                		System.out.println(sl);
//                	}
//                	//
                    try {
                        Template temp = d2s.convert(drs,slots);
                        templates.add(temp);
                    } catch (java.lang.ClassCastException e) {
                        continue;
                    }
                }
            }
        }

        if (clearAgain) {
        	p.clear(g,p.getTemps());
        }
        System.gc();
        
        return templates;
    }

}
