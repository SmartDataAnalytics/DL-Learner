/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.dllearner.reasoning;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.reasoner.ReasonerException;
import org.apache.jena.reasoner.ReasonerFactory;
import org.apache.jena.reasoner.rulesys.GenericRuleReasoner;
import org.apache.jena.reasoner.rulesys.Rule;

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
    protected static final Map<String, List<Rule>> ruleSets = new HashMap<>();
    
    /** The rule file names, indexed by processing level */
    protected static final Map<String, String> ruleFiles = new HashMap<>();

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
