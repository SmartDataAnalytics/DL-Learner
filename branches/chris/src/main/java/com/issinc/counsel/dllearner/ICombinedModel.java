package com.issinc.counsel.dllearner;

import com.hp.hpl.jena.rdf.model.Model;

import java.util.Date;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Chris.Shellenbarger
 * Date: Aug 3, 2010
 * Time: 3:15:32 PM
 *
 * A Combined Model Interface.
 *
 * Wraps the snapshot namespaces to date.
 */
public interface ICombinedModel {


    /**
     * Get the underlying model.
     *
     * @return the underlying model.
     */
    public Model getModel();


    /**
     * Get the unmodifiable namespace map so that we can look up dates associated with namespaces.
     *
     * @return The namespace map so that we can look up dates associated with namespaces.
     */
    public Map<String, Date> getNamespaceMap();
}
