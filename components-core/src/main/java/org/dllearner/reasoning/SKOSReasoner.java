package org.dllearner.reasoning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hp.hpl.jena.reasoner.ReasonerException;
import com.hp.hpl.jena.reasoner.ReasonerFactory;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

/**
 * @author Lorenz Buehmann
 *
 */
public class SKOSReasoner extends GenericRuleReasoner {
	
	 /** Constant: used to indicate default SKOS processing level */
    public static final String DEFAULT_RULES = "default";
    
    /** Constant: used to indicate full SKOS processing level */
    public static final String FULL_RULES = "full";
    
    /** Constant: used to indicate minimal SKOS processing level */
    public static final String SIMPLE_RULES = "simple";
    
    /** The location of the default SKOS rule definitions on the class path */
    protected static final String RULE_FILE = "etc/skos.rules";
    
    /** The location of the full SKOS rule definitions on the class path */
    protected static final String FULL_RULE_FILE = "etc/skos-full.rules";
    
    /** The location of the simple SKOS rule definitions on the class path */
    protected static final String SIMPLE_RULE_FILE = "etc/skos-simple.rules";
    
    /** The cached rule sets, indexed by processing level */
    protected static Map<String, List<Rule>> ruleSets = new HashMap<>();
    
    /** The rule file names, indexed by processing level */
    protected static Map<String, String> ruleFiles;

	/**
	 * @param factory the Jena reasoner factory
	 */
	public SKOSReasoner(ReasonerFactory factory) {
		super(loadRulesLevel(DEFAULT_RULES), factory);
		setMode(HYBRID);
		setTransitiveClosureCaching(true);
	}
	
	 /**
     * Return the SKOS rule set, loading it in if necessary.
     * @param level a string defining the processing level required
     */
    public static List<Rule> loadRulesLevel(String level) {
        List<Rule> ruleSet = ruleSets.get(level);
        if (ruleSet == null) {
            String file = ruleFiles.get(level);
            if (file == null) {
                throw new ReasonerException("Illegal SKOS conformance level: " + level);
            }
            ruleSet = loadRules( file );
            ruleSets.put(level, ruleSet);
        }
        return ruleSet;
    }
}
