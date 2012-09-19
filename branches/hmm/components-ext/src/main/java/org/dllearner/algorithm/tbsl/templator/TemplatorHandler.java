package org.dllearner.algorithm.tbsl.templator;

import java.util.Set;

import org.dllearner.algorithm.tbsl.sparql.BasicQueryTemplate;
import org.dllearner.algorithm.tbsl.sparql.Template;

public class TemplatorHandler {

    Templator templator;
    BasicTemplator basictemplator;
    
    public TemplatorHandler(String[] files) {
    	templator = new Templator();	
    	basictemplator = new BasicTemplator();
    	templator.setGrammarFiles(files);
        basictemplator.setGrammarFiles(files);
    }
    
    public void setVerbose(boolean b) {
        templator.setVERBOSE(b);
    }
    
    public Set<Template> buildTemplates(String s) { 
   		return templator.buildTemplates(s);                       
    }
    
    public Set<BasicQueryTemplate> buildBasicTemplates(String s) {
    	return basictemplator.buildBasicQueries(s);
    }
    
}
