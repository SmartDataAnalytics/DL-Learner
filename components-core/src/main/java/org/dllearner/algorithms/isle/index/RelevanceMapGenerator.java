package org.dllearner.algorithms.isle.index;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.isle.metrics.RelevanceMetric;
import org.dllearner.algorithms.isle.metrics.RelevanceUtils;
import org.dllearner.core.owl.Entity;
import org.dllearner.core.owl.NamedClass;
import org.semanticweb.owlapi.model.OWLOntology;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

/**
 * Interface for an index which is able to resolve a given entity's URI to the set of documents containing
 * this entity, i.e., documents which contain words disambiguated to the given entity.
 *
 * @author Lorenz Buehmann
 * @author Daniel Fleischhacker
 */
public abstract class RelevanceMapGenerator {

	static HashFunction hf = Hashing.md5();
    private static final Logger logger = Logger.getLogger(RelevanceMapGenerator.class.getName());
    public static String cacheDirectory = "cache/relevance";
    
    public static Map<Entity, Double> generateRelevanceMap(NamedClass cls, OWLOntology ontology, RelevanceMetric relevanceMetric, boolean cached){
    	Map<Entity, Double> relevanceMap = null;
    	File folder = new File(cacheDirectory);
    	folder.mkdirs();
    	File file = null;
		try {
			file = new File(folder, URLEncoder.encode(cls.getName(), "UTF-8") + ".rel");
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
		}
    	if(cached && file.exists()){
    		try {
    			logger.info("Loading relevance map from disk...");
				ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
				relevanceMap = (Map<Entity, Double>) ois.readObject();
				ois.close();
				logger.info("...done.");
			} catch (Exception e) {
				e.printStackTrace();
			} 
    	} else {
    		logger.info("Building relevance map...");
    		relevanceMap = RelevanceUtils.getRelevantEntities(cls, ontology, relevanceMetric);
    		try {
    			ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file));
    			oos.writeObject(relevanceMap);
    			oos.close();
    		} catch (IOException e1) {
    			e1.printStackTrace();
    		}
    		logger.info("...done.");
    	}
    	return relevanceMap;
    }
    
    public static Map<Entity, Double> generateRelevanceMap(NamedClass cls, OWLOntology ontology, RelevanceMetric relevanceMetric){
    	return generateRelevanceMap(cls, ontology, relevanceMetric, false);
    }
}
