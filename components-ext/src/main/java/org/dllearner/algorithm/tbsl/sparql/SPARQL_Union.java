/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dllearner.algorithm.tbsl.sparql;

import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author christina
 */
public class SPARQL_Union {
    
    Set<SPARQL_Triple> conditions_left;
    Set<SPARQL_Filter> filters_left;
    Set<SPARQL_Triple> conditions_right;
    Set<SPARQL_Filter> filters_right;

    public SPARQL_Union() {
        conditions_left = new HashSet<SPARQL_Triple>();
        filters_left = new HashSet<SPARQL_Filter>();
        conditions_right = new HashSet<SPARQL_Triple>();
        filters_right = new HashSet<SPARQL_Filter>();
    }
    public SPARQL_Union(Set<SPARQL_Triple> c_left,Set<SPARQL_Filter> f_left,Set<SPARQL_Triple> c_right,Set<SPARQL_Filter> f_right) {
        conditions_left = c_left;
        filters_left = f_left;
        conditions_right = c_right;
        filters_right = f_right;
    }

    public String toString() {
        String out = "{ ";
        for (SPARQL_Triple t : conditions_left) out += t.toString();
        for (SPARQL_Filter f : filters_left) out += f.toString();
        out += " } UNION {";
        for (SPARQL_Triple t : conditions_right) out += t.toString();
        for (SPARQL_Filter f : filters_right) out += f.toString();
        out += "}";
        return out;
    }
    
}
