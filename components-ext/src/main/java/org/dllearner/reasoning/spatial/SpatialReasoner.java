package org.dllearner.reasoning.spatial;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ReasonerComponent;
import org.dllearner.core.annotations.Unstable;
import org.dllearner.reasoning.spatial.model.SpatialIndividual;
import org.dllearner.reasoning.spatial.model.SpatialSum;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;

import java.sql.ResultSet;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Spatial reasoner interface specifying a reasoner which is capable of
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
 * - MULTIPOINT ((long1 lat1), (long2 lat2), (long3 lat3), (long4 lat4))
 * - MULTIPOINT (long1 lat1, long2 lat2, long3 lat3, long4 lat4)
 * - MULTILINESTRING ((long1 lat1, long2 lat2, long3 lat3), \
 *                    (long4 lat4, long5 lat5, long6 lat6, long7 lat7))
 * - MULTIPOLYGON (((long1 lat1, long2 lat2, long3 lat3, long4 lat4)), \
 *                 ((long5 lat5, long6 lat6, long7 lat7, long8 lat8, long9 lat9)))
 *
 * The implementation of the Region Connection Calculus relations should follow
 * the relation definitions as e.g. presented in Table 1 in 'Towards Spatial
 * Reasoning in the Semantic Web: A Hybrid Knowledge Representation System
 * Architecture' by Grüttler and Bauer-Messmer,
 * https://www.wsl.ch/fileadmin/user_upload/WSL/Projekte/dnl/Grutter_Bauer-Messmer_AGILE_2007.pdf
 */
public interface SpatialReasoner extends ReasonerComponent {
    // <RCC area feature relations>
    // See: https://en.wikipedia.org/wiki/Region_connection_calculus

    // C
    /**
     * Returns a stream of OWL individuals which are connected with the input
     * individual in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getConnectedIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the input OWL individuals are connected in terms of
     * their spatial extension and w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus), and `false`
     * otherwise.
     *
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean areConnected(OWLIndividual individual1, OWLIndividual individual2);

    // DC
    /**
     * Returns a stream of OWL individuals which are disconnected from the input
     * individual in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * Disconnected(x, y) iff. not Connected(x, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getDisconnectedIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the input OWL individuals are disconnected from each
     * other in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus),
     * and `false` otherwise.
     *
     * Disconnected(x, y) iff. not Connected(x, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean areDisconnected(OWLIndividual individual1, OWLIndividual individual2);

    // P
    /**
     * Returns a stream of OWL individuals which are part of the input OWL
     * individual in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getIndividualsWhichArePartOf(OWLIndividual individual);

    /**
     * Returns `true` if the first input OWL individual is part of the second
     * input OWL individual in terms of their respective spatial extension and
     * w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus), and `false`
     * otherwise.
     *
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean isPartOf(OWLIndividual part, OWLIndividual whole);

    // PP
    /**
     * Returns a stream of OWL individuals which are a proper part of the input
     * OWL individual in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * ProperPartOf(x, y) iff. PartOf(x, y) and not PartOf(y, x) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getIndividualsWhichAreProperPartOf(OWLIndividual individual);

    /**
     * Returns `true` if the first input OWL individual is a proper part of the
     * second input OWL individual in terms of their respective spatial
     * extension and w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus), and `false`
     * otherwise.
     *
     * ProperPartOf(x, y) iff. PartOf(x, y) and not PartOf(y, x) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean isProperPartOf(OWLIndividual part, OWLIndividual whole);

    // EQ
    /**
     * Returns a stream of OWL individuals which are equal to the input OWL
     * individual in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus) .
     *
     * Equal(x, y) iff. PartOf(x, y) and PartOf(y, x) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getSpatiallyEqualIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the input OWL individuals are equal in terms of their
     * spatial extension and w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus) and `false`
     * otherwise.
     *
     * Equal(x, y) iff. PartOf(x, y) and PartOf(y, x) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean areSpatiallyEqual(OWLIndividual individual1, OWLIndividual individual2);

    // O
    /**
     * Returns a stream of OWL individuals which are overlapping with the input
     * OWL individual in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getOverlappingIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the input OWL individuals are overlapping in terms of
     * their spatial extension and w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus), and `false`
     * otherwise.
     *
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean areOverlapping(OWLIndividual individual1, OWLIndividual individual2);

    // DC
    /**
     * Returns a stream of OWL individuals which are discrete from the input
     * OWL individual in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * DiscreteFrom(x, y) iff. not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getIndividualsDiscreteFrom(OWLIndividual individual);

    /**
     * Returns `true` if the input OWL individuals are discrete from each other
     * in terms of their spatial extension and w.r.t. the Region Connection
     * Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus), and
     * `false` otherwise.
     *
     * DiscreteFrom(x, y) iff. not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean areDiscreteFromEachOther(OWLIndividual individual1, OWLIndividual individual2);

    // PO
    /**
     * Returns a stream of OWL individuals which are partially overlapping with
     * the input OWL individual in terms of their spatial extension and w.r.t.
     * the Region Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * PartiallyOverlaps(x, y) iff. Overlaps(x, y) and not PartOf(x, y) and
     *      not PartOf(y, x) .
     * DiscreteFrom(x, y) iff. not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getPartiallyOverlappingIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the input OWL individuals are partially overlapping in
     * terms of their spatial extension and w.r.t. the Region Connection
     * Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus), and
     * `false` otherwise.
     *
     * PartiallyOverlaps(x, y) iff. Overlaps(x, y) and not PartOf(x, y) and
     *      not PartOf(y, x) .
     * DiscreteFrom(x, y) iff. not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean arePartiallyOverlapping(OWLIndividual individual1, OWLIndividual individual2);

    // EC
    /**
     * Returns a stream of OWL individuals which are externally connected to the
     * input OWL individual in terms of their spatial extension and w.r.t. the
     * Region Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * ExternallyConnected(x, y) iff. Connected(x, y) and not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getExternallyConnectedIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the input OWL individuals are externally connected with
     * each other in terms of their spatial extension and w.r.t. the Region
     * Connection Calculus (https://en.wikipedia.org/wiki/Region_connection_calculus),
     * and `false` otherwise.
     *
     * ExternallyConnected(x, y) iff. Connected(x, y) and not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean areExternallyConnected(OWLIndividual individual1, OWLIndividual individual2);

    // TPP
    /**
     * Returns a stream of OWL individuals which are a tangential proper part
     * of the input OWL individual in terms of their spatial extension and
     * w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * TangentialProperPartOf(x, y) iff. ProperPartOf(x, y) and
     *      exists z: (ExternallyConnected(z, x) and ExternallyConnected(z, y))
     * ProperPartOf(x, y) iff. PartOf(x, y) and not PartOf(y, x) .
     * ExternallyConnected(x, y) iff. Connected(x, y) and not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getIndividualsWhichAreTangentialProperPartOf(OWLIndividual individual);

    /**
     * Returns `true` if the first input OWL individual is a tangential proper
     * part of the second input OWL individual in terms of their respective
     * spatial extension and w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus), and `false`
     * otherwise.
     *
     * TangentialProperPartOf(x, y) iff. ProperPartOf(x, y) and
     *      exists z: (ExternallyConnected(z, x) and ExternallyConnected(z, y))
     * ProperPartOf(x, y) iff. PartOf(x, y) and not PartOf(y, x) .
     * ExternallyConnected(x, y) iff. Connected(x, y) and not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean isTangentialProperPartOf(OWLIndividual part, OWLIndividual whole);


    // NTPP
    /**
     * Returns `true` if the first input OWL individual is a non-tangential
     * proper part of the second input OWL individual in terms of their
     * respective spatial extension and w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus), and `false`
     * otherwise.
     *
     * NonTangentialProperPartOf(x, y) iff. ProperPartOf(x, y) and
     *   not exists z: (ExternallyConnected(z, x) and ExternallyConnected(z, y))
     * ProperPartOf(x, y) iff. PartOf(x, y) and not PartOf(y, x) .
     * ExternallyConnected(x, y) iff. Connected(x, y) and not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y)
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    boolean isNonTangentialProperPartOf(OWLIndividual part, OWLIndividual whole);

    /**
     * Returns a stream of OWL individuals which are a non-tangential proper
     * part of the input OWL individual in terms of their spatial extension and
     * w.r.t. the Region Connection Calculus
     * (https://en.wikipedia.org/wiki/Region_connection_calculus).
     *
     * NonTangentialProperPartOf(x, y) iff. ProperPartOf(x, y) and
     *   not exists z: (ExternallyConnected(z, x) and ExternallyConnected(z, y))
     * ProperPartOf(x, y) iff. PartOf(x, y) and not PartOf(y, x) .
     * ExternallyConnected(x, y) iff. Connected(x, y) and not Overlaps(x, y) .
     * Overlaps(x, y) iff. exists z: PartOf(z, x) and PartOf(z, y) .
     * PartOf(x, y) iff. forall z: Connected(z, x) --> Connected(z, y) .
     * Connected(x, y) iff. x and y have at least one point in common .
     */
    @Unstable
    Stream<OWLIndividual> getIndividualsWhichAreNonTangentialProperPartOf(OWLIndividual individual);
    // </RCC area feature relations>

    // <RANDELL relations/functions>
    /*
     * Spatial relations and functions mentioned in the paper
     * 'A Spatial Logic based on Regions and Connection', Randell, Cui, Cohn,
     * 1992
     */

    // sum
    /**
     * Returns `true` if the first input parameter is the spatial sum of the
     * whole set of OWL individuals given as second parameter and `false`
     * otherwise.
     *
     * sum(x, y) = the unique z such that
     *      forall w ( C(w, z) <--> (C(w, x) or C(w, y)) )
     */
    @Unstable
    boolean isSpatialSumOf(OWLIndividual sum, Set<OWLIndividual> parts);

    // universal spatial relation
    /**
     * Returns `true` if the input OWL individual is *equivalent* to the
     * universal spatial region and `false` otherwise.
     *
     * us = the unique y such that
     *      forall z ( C(z, y) )
     */
    @Unstable
    boolean isEquivalentToUniversalSpatialRegion(OWLIndividual individual);

    // complement of
    /**
     * Returns `true` if the first input OWL individual is the complement of
     * the second input OWL individual and `false` otherwise.
     *
     * compl(x) = the unique y such that
     *      forall z ( ( C(z, y) <--> not NTPP(z, x) ) and
     *          ( O(z, y) <--> not P(z, x) ) )
     */
    @Unstable
    boolean isComplementOf(OWLIndividual individual1, OWLIndividual individual2);

    // product/intersection
    /**
     * Returns `true` if the first input OWL individual is the intersection of
     * the second and third input OWL individual and `false` otherwise.
     *
     * TODO: Generalize to Set<OWLIndividual> as second parameter
     */
    @Unstable
    boolean isIntersectionOf(
            OWLIndividual intersection,
            OWLIndividual individual1,
            OWLIndividual individual2);

    // difference
    /**
     * Returns `true` if the first input OWL individual is the difference of
     * the second and third OWL individual and `false` otherwise.
     */
    @Unstable
    boolean isDifferenceOf(
            OWLIndividual difference,
            OWLIndividual individual1,
            OWLIndividual individual2);
    // </RANDELL relations/functions>

    /**
     * Returns `true` if the input OWL individuals are near to each other (by
     * some measure not defined here) and `false` otherwise.
     */
    @Unstable
    boolean isNear(OWLIndividual individual1, OWLIndividual individual2);

    /**
     * Returns a set of OWL individuals that are near the input OWL individual
     * (by some measure not defined here).
     */
    @Unstable
    Set<OWLIndividual> getNearSpatialIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the first input OWL individual is spatially inside the
     * second input OWL individual and `false` otherwise.
     */
    @Unstable
    boolean isInside(OWLIndividual containedIndividual, OWLIndividual containingIndividual);

    /**
     * Returns a set of OWL individuals that are contained in the input OWL
     * individual.
     */
    @Unstable
    Set<OWLIndividual> getContainedSpatialIndividuals(OWLIndividual individual);

    /**
     * Returns `true` if the first input OWL individual (being a line feature)
     * runs along the second input OWL individual (also being a line feature)
     * and `false` otherwise.
     */
    @Unstable
    boolean runsAlong(OWLIndividual individual1, OWLIndividual individual2);

    /**
     * Returns a set of OWL individuals (all being line features) which run
     * along the input OWL individual (which is a line feature as well).
     */
    @Unstable
    Set<OWLIndividual> getSpatialIndividualsRunningAlong(OWLIndividual individual);

    /**
     * Returns `true` if the first input OWL individual (being a line feature
     * describing e.g. a route) passes the second input OWL individual.
     */
    @Unstable
    boolean passes(OWLIndividual passingIndividual, OWLIndividual passedIndividual);

    /**
     * Returns a set of OWL individuals which are passed by the input OWL
     * individual (being a line feature and representing e.g. a route).
     */
    @Unstable
    Set<OWLIndividual> getPassedSpatialIndividuals(OWLIndividual passingIndividual);

    /**
     * Returns a set of OWL individuals (being line features representing e.g.
     * routes) which are passing the input OWL individuals.
     */
    @Unstable
    Set<OWLIndividual> getPassingSpatialIndividuals(OWLIndividual passedIndividual);

    AbstractReasonerComponent getBaseReasoner();

    boolean isSuperClassOf(OWLClassExpression superClassExpression, OWLClassExpression subClassExpression);
}
