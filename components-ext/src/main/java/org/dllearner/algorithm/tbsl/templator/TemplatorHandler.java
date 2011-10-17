package org.dllearner.algorithm.tbsl.templator;

import java.util.Set;

import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Template;

public class TemplatorHandler {

    static String[] GRAMMAR_FILES;
    Templator templator;
    BasicTemplator basictemplator;
    
    public TemplatorHandler(String[] files) {
    	templator = new Templator();	
    	basictemplator = new BasicTemplator();
    	GRAMMAR_FILES = files;
    }
    
    public void setGRAMMAR_FILES(String[] g) {
    	GRAMMAR_FILES = g;
    }
    
    public Set<Template> buildTemplates(String s) { 
   		return templator.buildTemplates(s);                       
    }
    
    public Set<BasicQueryTemplate> buildBasicTemplates(String s) {
    	return basictemplator.buildBasicQueries(s);
    }
    
}
