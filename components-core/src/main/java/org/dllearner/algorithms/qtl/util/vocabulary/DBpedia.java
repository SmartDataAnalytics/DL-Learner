package org.dllearner.algorithms.qtl.util.vocabulary;

import com.google.common.collect.Sets;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.sparql.vocabulary.FOAF;

import java.util.Collections;
import java.util.Set;

/**
 * @author Lorenz Buehmann
 */
public class DBpedia {

    // These will use ResourceFactory which creates Resource etc without a specific model.
    // This is safer for complex initialization paths.
    protected static final Resource resource(String uri )
    { return ResourceFactory.createResource( NS+uri ); }

    protected static final Property property(String uri )
    { return ResourceFactory.createProperty( NS, uri ); }

    /** The namespace of the vocabulary as a string. */
    public static final String NS = "http://dbpedia.org/";

    /** The namespace of the mapping based properties as a string. */
    public static final String DBO = "http://dbpedia.org/ontology/";

    /** The namespace of the raw infobox properties as a string. */
    public static final String DBP = "http://dbpedia.org/property/";

    /** The namespace of the resources vocabulary as a string. */
    public static final String DBR = "http://dbpedia.org/resource/";

    /** The namespace of the categories as a string. */
    public static final String DBC = "http://dbpedia.org/resource/Category:";

    /** The namespace of the vocabulary as a string */
    public static String getURI() {return NS;}

    /** The namespace of the vocabulary as a resource */
    public static final Resource NAMESPACE = ResourceFactory.createResource( NS );

    /**
     * Some properties (<code>http://dbpedia.org/ontology/wiki*</code>) which are usually blacklisted in applications.
     */
    public static final Set<String> BLACKLIST_PROPERTIES = Collections.unmodifiableSet(Sets.newHashSet(
            "http://dbpedia.org/ontology/wikiPageWikiLink",
            "http://dbpedia.org/ontology/wikiPageExternalLink",
            "http://dbpedia.org/ontology/wikiPageRedirects",
            "http://dbpedia.org/ontology/wikiPageDisambiguates",
            "http://dbpedia.org/ontology/wikiPageEditLink",
            "http://dbpedia.org/ontology/wikiPageHistoryLink",
            "http://dbpedia.org/ontology/wikiPageInterLanguageLink",
            "http://dbpedia.org/ontology/wikiPageRevisionLink",
            "http://dbpedia.org/ontology/wikiPageWikiLinkText",
            "http://dbpedia.org/ontology/wikidataSplitIri",
            "http://dbpedia.org/ontology/abstract",
            "http://www.w3.org/ns/prov#wasDerivedFrom",
            FOAF.isPrimaryTopicOf.getURI()
    ));

    public static final String BASE_IRI = "http://dbpedia.org/resource/";
    public static final PrefixMapping PM = PrefixMapping.Factory.create();
    static {
        PM.setNsPrefixes(PrefixMapping.Standard);
        PM.setNsPrefix("dbr", DBR);
        PM.setNsPrefix("dbo", DBO);
        PM.setNsPrefix("dbp", DBP);
        PM.setNsPrefix("dbc", DBC);
        PM.setNsPrefix("foaf", FOAF.NS);
    }
    public static final SerializationContext CONTEXT = new SerializationContext(PM);
    static {
        CONTEXT.setBaseIRI(BASE_IRI);
    }

}
