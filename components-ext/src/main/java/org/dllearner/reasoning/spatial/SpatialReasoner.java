package org.dllearner.reasoning.spatial;

import org.apache.lucene.search.PointInSetQuery;
import org.dllearner.core.ReasonerComponent;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.util.stream.Stream;

/**
 * Spatial baseReasoner interface specifying a baseReasoner which is capable of
 * reasoning over implicit spatial relations like 'near', 'inside', 'along' etc.
 *
 * For now only geo:asWKT literals will be supported. So, geographic geometries
 * can be expressed by means of the following primitives: (examples taken from
 * https://en.wikipedia.org/wiki/Well-known_text)
 *
 * - POINT (long1 lat1)
 * - LINESTRING (long1 lat1, long2 lat2, long3 lat3)
 * - POLYGON ((long1 lat1, long2 lat2, long3 lat3, long4 lat4, long1 lat1))
 * - POLYGON ((long1 lat1, long2 lat2, long3 lat3, long4 lat4, long1 lat1), \
 *            (long5 lat5, long6 lat6, long7 lat7, long5 lat5))
 *
 * We currently do not support the following primitives:
 *
 * - MULTIPOINT ((long1 lat1), (long2 lat2), (long3 lat3), (long4 lat4))
 * - MULTIPOINT (long1 lat1, long2 lat2, long3 lat3, long4 lat4)
 * - MULTILINESTRING ((long1 lat1, long2 lat2, long3 lat3), \
 *                    (long4 lat4, long5 lat5, long6 lat6, long7 lat7))
 * - MULTIPOLYGON (((long1 lat1, long2 lat2, long3 lat3, long4 lat4)), \
 *                 ((long5 lat5, long6 lat6, long7 lat7, long8 lat8, long9 lat9)))
 */
public interface SpatialReasoner extends ReasonerComponent {
    /* Relations of the region connection calculus (RCC) */

    // Connected with
    boolean isConnectedWith(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2);

    Stream<OWLIndividual> getIndividualsConnectedWith(OWLIndividual spatialFeatureIndividual);

    // Discrete from
//    boolean isDiscreteFrom(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2);

//    Stream<OWLIndividual> getIndividualsDiscreteFrom(OWLIndividual spatialFeatureIndividual);

    // Overlaps with
    boolean overlapsWith(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2);

    Stream<OWLIndividual> getIndividualsOverlappingWith(OWLIndividual spatialFeatureIndividual);

    // Part of
    boolean isPartOf(OWLIndividual part, OWLIndividual whole);

    Stream<OWLIndividual> getIndividualsPartOf(OWLIndividual whole);

    // Has part
    boolean hasPart(OWLIndividual whole, OWLIndividual part);

    Stream<OWLIndividual> getIndividualsHavingPart(OWLIndividual part);

    // Proper part of
    boolean isProperPartOf(OWLIndividual part, OWLIndividual whole);

    Stream<OWLIndividual> getIndividualsProperPartOf(OWLIndividual whole);

    // Has proper part
    boolean hasProperPart(OWLIndividual whole, OWLIndividual part);

    Stream<OWLIndividual> getIndividualsHavingProperPart(OWLIndividual part);

    // Partially overlaps
    boolean partiallyOverlaps(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2);

    Stream<OWLIndividual> getIndividualsPartiallyOverlappingWith(OWLIndividual spatialFeatureIndividual);

    // Tangential proper part of
    // Non-tangential proper part of
    // Identical with
    // Has tangential proper part
    // Has non-tangential proper part
    // Externally connected with
    // Disconnected from

    /* Further non-RCC relations */
    // ...
    boolean isInside(OWLIndividual inner, OWLIndividual container);

    Stream<OWLIndividual> getIndividualsInside(OWLIndividual container);
}
