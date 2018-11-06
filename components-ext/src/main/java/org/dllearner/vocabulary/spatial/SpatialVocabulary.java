package org.dllearner.vocabulary.spatial;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLObjectProperty;

import java.util.Set;

public class SpatialVocabulary {
    private static final OWLDataFactory df = OWLManager.getOWLDataFactory();
    public static final String prefix = "http://dl-learner.org/ont/spatial#";

    // ---- atomic classes ----
    public static final OWLClass spatialFeature =
            df.getOWLClass(IRI.create("http://www.opengis.net/ont/geosparql#Feature"));
    // ---- object properties ----
    public static final OWLObjectProperty isInside =
            df.getOWLObjectProperty(IRI.create(prefix + "isInside"));

    public static Set<OWLObjectProperty> spatialObjectProperties = Sets.newHashSet(
            isInside);

}
