package org.dllearner.reasoning.spatial;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.owl.OWLObjectUnionOfImplExt;
import org.dllearner.reasoning.ReasonerType;
import org.dllearner.utils.spatial.SpatialKBPostGISHelper;
import org.dllearner.vocabulary.spatial.SpatialVocabulary;
import org.postgresql.util.PGobject;
import org.semanticweb.owlapi.model.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;
import uk.ac.manchester.cs.owl.owlapi.OWLObjectUnionOfImpl;

import javax.annotation.Nonnull;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpatialReasonerPostGIS extends AbstractReasonerComponent implements SpatialReasoner {
    // PostGIS settings
    private String hostname = "localhost";
    private String dbName = "dllearner";
    private int port = 5432;
    private String dbUser = "postgres";
    private String dbUserPW = "postgres";
    protected Connection conn;

    // OWL settings
    private Set<List<OWLProperty>> geometryPropertyPaths = new HashSet<>();
    private OWLClass areaFeatureClass = SpatialKBPostGISHelper.areaFeatureClass;
    private String areaFeatureTableName = SpatialKBPostGISHelper.areaGeomTableName;
    private OWLClass lineFeatureClass = SpatialKBPostGISHelper.lineFeatureClass;
    private String lineFeatureTableName = SpatialKBPostGISHelper.lineGeomTableName;
    private OWLClass pointFeatureClass = SpatialKBPostGISHelper.pointFeatureClass;
    private String pointFeatureTableName = SpatialKBPostGISHelper.pointGeomTableName;

    protected AbstractReasonerComponent baseReasoner;

    // spatial relations settings
    private double nearRadiusInMeters = 30;
    private double runsAlongToleranceInMeters = 20;

    // TODO: make this configurable
    // "Specifies the maximum number of entries the cache may contain"
    private int maxFeatureGeometryCacheSize = 1000000;
    private LoadingCache<OWLIndividual, OWLIndividual> feature2geom =
            CacheBuilder.newBuilder().maximumSize(maxFeatureGeometryCacheSize)
                    .build(new CacheLoader<OWLIndividual, OWLIndividual>() {
                        @Override
                        public OWLIndividual load(
                                @Nonnull OWLIndividual featureIndividual) throws Exception {
                            /*
                             * Gets the geo:Geometry OWL individual assigned to
                             * the input OWL individual via the geometry
                             * property path. In case there are multiple
                             * geometry OWL individuals assigned, I'll just pick
                             * the first one.
                             */
                            for (List<OWLProperty> propPath : geometryPropertyPaths) {
                                int pathLen = propPath.size();

                                // In case the geometry property path just
                                // contains one entry, one can assume it's a
                                // data property pointing to the geometry
                                // literal. So the input feature individual is
                                // already what's requested here.
                                if (pathLen == 1) {
                                    assert propPath.get(0).isOWLDataProperty();

                                    return featureIndividual;
                                }

                                // Strip off the last entry of the property
                                // path, which is a data property. All the
                                // preceding properties are assumed to be object
                                // properties.
                                List<OWLObjectProperty> objProps = propPath.stream()
                                        .limit(pathLen-1)
                                        .map(OWLProperty::asOWLObjectProperty)
                                        .collect(Collectors.toList());

                                // S --> subject position, O --> object position
                                // from an RDF triple point of view
                                Set<OWLIndividual> tmpS =
                                        Sets.newHashSet(featureIndividual);
                                Set<OWLIndividual> tmpO = new HashSet<>();

                                for (OWLObjectProperty objProp : objProps) {
                                    for (OWLIndividual i : tmpS) {
                                        tmpO.addAll(
                                                baseReasoner.getRelatedIndividuals(i, objProp));
                                    }

                                    tmpS = tmpO;
                                    tmpO = new HashSet<>();
                                }

                                if (!tmpS.isEmpty()) {
                                    return tmpS.iterator().next();
                                }
                            }
                            return null;
                        }
                    });

    private LoadingCache<OWLIndividual, OWLIndividual> geom2feature =
            CacheBuilder.newBuilder().maximumSize(maxFeatureGeometryCacheSize)
                    .build(new CacheLoader<OWLIndividual, OWLIndividual>() {
                        @Override
                        public OWLIndividual load(
                                @Nonnull OWLIndividual geometryIndividual) throws Exception {
                            /*
                             * Gets the geo:Feature OWL individual to which the
                             * input geo:Geometry OWL individual was assigned
                             * via the geometry property path. In case there are
                             * multiple feature OWL individuals to which the
                             * geometry OWL individual was assigned, I'll just
                             * pick the first one.
                             */
                            for (List<OWLProperty> propPath : geometryPropertyPaths) {
                                int pathLen = propPath.size();

                                // In case the geometry property path just
                                // contains one entry, one can assume it's a
                                // data property pointing to the geometry
                                // literal. So the input geometry individual is
                                // already what's requested here.
                                if (pathLen == 1) {
                                    assert propPath.get(0).isOWLDataProperty();

                                    return geometryIndividual;
                                }

                                // Strip off the last entry of the property
                                // path, which is a data property. All the
                                // preceding properties are assumed to be object
                                // properties.
                                List<OWLObjectProperty> objProps = propPath.stream()
                                        .limit(pathLen-1)
                                        .map(OWLProperty::asOWLObjectProperty)
                                        .collect(Collectors.toList());

                                List<OWLObjectProperty> revObjProps =
                                        Lists.reverse(objProps);

                                // S --> subject, O --> object (from RDF triple
                                // view)
                                Set<OWLIndividual> tmpS = new HashSet<>();
                                Set<OWLIndividual> tmpO =
                                        Sets.newHashSet(geometryIndividual);

                                for (OWLObjectProperty objProp : revObjProps) {
                                    /**
                                     * Looks sth like this:
                                     * {
                                     *   :bahnhof_dresden_neustadt_building : [
                                     *          :building_bhf_neustadt_geometry],
                                     *   :building_bhf_neustadt_geometry : [],
                                     *   :inside_building_bhf_neustadt_geometry : [],
                                     *   :on_turnerweg_geometry : [],
                                     *   :outside_building_bhf_neustadt_1_geometry : [],
                                     *   :outside_building_bhf_neustadt_2_geometry : [],
                                     *   :pos_inside_bhf_neustadt : [
                                     *          :inside_building_bhf_neustadt_geometry],
                                     *   :pos_on_turnerweg : [
                                     *          :on_turnerweg_geometry],
                                     *   :pos_outside_bhf_neustadt_1 : [
                                     *          :outside_building_bhf_neustadt_1_geometry],
                                     *   :pos_outside_bhf_neustadt_2 : [
                                     *          :outside_building_bhf_neustadt_2_geometry],
                                     *   :turnerweg : [
                                     *          :turnerweg_geometry],
                                     *   :turnerweg_geometry : [],
                                     *   :turnerweg_part : [
                                     *          :turnerweg_part_geometry],
                                     *   :turnerweg_part_geometry : []
                                     * }
                                     *
                                     * i.e. all geometry entries have an empty
                                     * set as values and all the feature entries
                                     * have a non-empty set as values
                                     */
                                    Map<OWLIndividual, SortedSet<OWLIndividual>> members =
                                            baseReasoner.getPropertyMembers(objProp);

                                    /**
                                     * `members` keys which have a non-empty
                                     * value, i.e. all feature OWL individuals
                                     *
                                     * [
                                     *   :bahnhof_dresden_neustadt_building,
                                     *   :pos_inside_bhf_neustadt,
                                     *   :pos_on_turnerweg,
                                     *   :pos_outside_bhf_neustadt_1,
                                     *   :pos_outside_bhf_neustadt_2,
                                     *   :turnerweg,
                                     *   :turnerweg_part
                                     * ]
                                     */
                                    List<OWLIndividual> sMembers = members.entrySet().stream()
                                            .filter((Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e) -> !e.getValue().isEmpty())
                                            .map((Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e) -> e.getKey())
                                            .collect(Collectors.toList());

                                    for (OWLIndividual s : sMembers) {
                                        // - `tmpO` contains all the OWL
                                        //   individuals on object position
                                        //   (viewed from an RDF triple
                                        //   perspective)
                                        // - `sMembers` contains all those OWL
                                        //   individuals that actually have
                                        //   values for property `objProp`
                                        // - `s` is an OWL individual that
                                        //   actually has values for property
                                        //   `objProp`

                                        // The values of the current OWL individual
                                        SortedSet<OWLIndividual> os = members.get(s);

                                        // If any of the values of the current
                                        // OWL individual is contained in the
                                        // set of value OWL individuals we're
                                        // interested in...
                                        if (os.stream().anyMatch(tmpO::contains)) {
                                            // ...then we'll consider this
                                            // current OWL individual in the
                                            // next round (or in the final
                                            // result set if this is the last
                                            // round)
                                            tmpS.add(s);
                                        }
                                    }

                                    tmpO = tmpS;
                                    tmpS = new HashSet<>();
                                }

                                if (!tmpO.isEmpty()) {
                                    return tmpO.iterator().next();
                                }
                            }

                            return null;
                        }
                    });
    /**
     * @param featureIndividual The feature OWL individual to get the PostGIS
     *                          table for, not the geometry OWL individual
     */
    private String getTable(OWLIndividual featureIndividual) {
        if (baseReasoner.hasType(areaFeatureClass, featureIndividual)) {
            return areaFeatureTableName;
        } else if (baseReasoner.hasType(lineFeatureClass, featureIndividual)) {
            return lineFeatureTableName;
        } else if (baseReasoner.hasType(pointFeatureClass, featureIndividual)) {
            return pointFeatureTableName;
        } else {
            throw new RuntimeException(
                    "Individual " + featureIndividual + " is neither an area " +
                            "feature, nor a line feature, nor a point feature");
        }
    }

    public SpatialReasonerPostGIS() {
        super();
    }

    // -------------------------------------------------------------------------
    // base reasoner/spatial reasoner switch methods

    @Override
    public boolean isSuperClassOf(OWLClassExpression superClass, OWLClassExpression subClass) {
        // TODO: Add spatial aspect-specific stuff
        return baseReasoner.isSuperClassOf(superClass, subClass);
    }

    @Override
    protected Map<OWLIndividual, SortedSet<OWLLiteral>> getDatatypeMembersImpl(
            OWLDataProperty datatypeProperty) {
        return baseReasoner.getDatatypeMembers(datatypeProperty);
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyDomains() {
        Map<OWLObjectProperty, OWLClassExpression> domainsMap = baseReasoner.getObjectPropertyDomains();

        // TODO: Add spatial aspect-specific stuff here
        domainsMap.put(SpatialVocabulary.isConnectedWith, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.overlapsWith, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.isPartOf, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.hasPart, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.isProperPartOf, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.hasProperPart, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.partiallyOverlapsWith, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.isTangentialProperPartOf, SpatialVocabulary.SpatialFeature);
        domainsMap.put(SpatialVocabulary.isNonTangentialProperPartOf, SpatialVocabulary.SpatialFeature);

        // TODO: isSpatiallyIdenticalWith
        // TODO: hasTangentialProperPart
        // TODO: hasNonTangentialProperPart
        // TODO: isExternallyConnectedWith
        // TODO: isDisconnectedFrom
        // TODO: isNear
        // TODO: startsNear
        // TODO: endsNear
        // TODO: crosses
        // TODO: runsAlong

        return domainsMap;
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRanges() {
        Map<OWLObjectProperty, OWLClassExpression> rangesMap = baseReasoner.getObjectPropertyRanges();

        // TODO: Add spatial aspect-specific stuff here
        rangesMap.put(SpatialVocabulary.isConnectedWith, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.overlapsWith, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.isPartOf, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.hasPart, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.isProperPartOf, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.hasProperPart, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.partiallyOverlapsWith, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.isTangentialProperPartOf, SpatialVocabulary.SpatialFeature);
        rangesMap.put(SpatialVocabulary.isNonTangentialProperPartOf, SpatialVocabulary.SpatialFeature);

        // TODO: isSpatiallyIdenticalWith
        // TODO: hasTangentialProperPart
        // TODO: hasNonTangentialProperPart
        // TODO: isExternallyConnectedWith
        // TODO: isDisconnectedFrom
        // TODO: isNear
        // TODO: startsNear
        // TODO: endsNear
        // TODO: crosses
        // TODO: runsAlong
        return rangesMap;
    }

    @Override
    public Map<OWLDataProperty, OWLClassExpression> getDataPropertyDomains() {
        Map<OWLDataProperty, OWLClassExpression> domainsMap = baseReasoner.getDataPropertyDomains();

        // TODO: Add spatial aspect-specific stuff here

        return domainsMap;
    }

    @Override
    protected SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression ce) {
        SortedSet<OWLClassExpression> subClasses = baseReasoner.getSubClasses(ce);

        // TODO: Add spatial aspect-specific stuff here

        return subClasses;
    }

    @Override
    protected SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression ce) {
        SortedSet<OWLClassExpression> superClasses = baseReasoner.getSubClasses(ce);

        // TODO: Add spatial aspect-specific stuff here

        return superClasses;
    }

    @Override
    protected Set<OWLDataProperty> getDatatypePropertiesImpl() {
        Set<OWLDataProperty> dataProperties = baseReasoner.getDatatypeProperties();

        dataProperties.addAll(SpatialVocabulary.spatialDataProperties);

        return dataProperties;
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty objectProperty) {
        SortedSet<OWLObjectProperty> subProperties = baseReasoner.getSubProperties(objectProperty);

        // TODO: add spatial aspect-specific stuff here

        if (objectProperty.equals(SpatialVocabulary.isConnectedWith)) {
            subProperties.add(SpatialVocabulary.overlapsWith);
            subProperties.add(SpatialVocabulary.isPartOf);
            subProperties.add(SpatialVocabulary.hasPart);
            subProperties.add(SpatialVocabulary.isProperPartOf);
            subProperties.add(SpatialVocabulary.hasProperPart);
            subProperties.add(SpatialVocabulary.partiallyOverlapsWith);
            subProperties.add(SpatialVocabulary.isTangentialProperPartOf);
            subProperties.add(SpatialVocabulary.isNonTangentialProperPartOf);
            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: isExternallyConnectedWith

        } else if (objectProperty.equals(SpatialVocabulary.overlapsWith)) {
            subProperties.add(SpatialVocabulary.isPartOf);
            subProperties.add(SpatialVocabulary.hasPart);
            subProperties.add(SpatialVocabulary.isProperPartOf);
            subProperties.add(SpatialVocabulary.hasProperPart);
            subProperties.add(SpatialVocabulary.partiallyOverlapsWith);
            subProperties.add(SpatialVocabulary.isTangentialProperPartOf);
            subProperties.add(SpatialVocabulary.isNonTangentialProperPartOf);
            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: isExternallyConnectedWith

        } else if (objectProperty.equals(SpatialVocabulary.isPartOf)) {
            subProperties.add(SpatialVocabulary.isProperPartOf);
            subProperties.add(SpatialVocabulary.isTangentialProperPartOf);
            subProperties.add(SpatialVocabulary.isNonTangentialProperPartOf);
            // TODO: isSpatiallyIdenticalWith

        } else if (objectProperty.equals(SpatialVocabulary.hasPart)) {
            subProperties.add(SpatialVocabulary.hasProperPart);
            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart

        } else if (objectProperty.equals(SpatialVocabulary.isProperPartOf)) {
            subProperties.add(SpatialVocabulary.isTangentialProperPartOf);
            subProperties.add(SpatialVocabulary.isNonTangentialProperPartOf);

        } else if (objectProperty.equals(SpatialVocabulary.hasProperPart)) {
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
        }

        // TODO: isSpatiallyIdenticalWith
        // TODO: hasTangentialProperPart
        // TODO: hasNonTangentialProperPart
        // TODO: isExternallyConnectedWith
        // TODO: isDisconnectedFrom
        // TODO: isNear
        // TODO: startsNear
        // TODO: endsNear
        // TODO: crosses
        // TODO: runsAlong

        return subProperties;
    }

    @Override
    protected SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty dataProperty) {
        SortedSet<OWLDataProperty> subProperties = baseReasoner.getSubProperties(dataProperty);

        // TODO: add spatial aspect-specific stuff here

        return subProperties;
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty objectProperty) {
        SortedSet<OWLObjectProperty> superProperties = baseReasoner.getSuperProperties(objectProperty);

        // TODO: add spatial aspect-specific stuff here

        if (objectProperty.equals(SpatialVocabulary.overlapsWith)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);

        } else if (objectProperty.equals(SpatialVocabulary.isPartOf)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);
            superProperties.add(SpatialVocabulary.overlapsWith);

        } else if (objectProperty.equals(SpatialVocabulary.hasPart)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);
            superProperties.add(SpatialVocabulary.overlapsWith);

        } else if (objectProperty.equals(SpatialVocabulary.isProperPartOf)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);
            superProperties.add(SpatialVocabulary.overlapsWith);
            superProperties.add(SpatialVocabulary.isPartOf);

        } else if (objectProperty.equals(SpatialVocabulary.hasProperPart)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);
            superProperties.add(SpatialVocabulary.overlapsWith);
            superProperties.add(SpatialVocabulary.hasPart);

        } else if (objectProperty.equals(SpatialVocabulary.partiallyOverlapsWith)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);
            superProperties.add(SpatialVocabulary.overlapsWith);

        } else if (objectProperty.equals(SpatialVocabulary.isTangentialProperPartOf)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);
            superProperties.add(SpatialVocabulary.overlapsWith);
            superProperties.add(SpatialVocabulary.isPartOf);
            superProperties.add(SpatialVocabulary.isProperPartOf);

        } else if (objectProperty.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
            superProperties.add(SpatialVocabulary.isConnectedWith);
            superProperties.add(SpatialVocabulary.overlapsWith);
            superProperties.add(SpatialVocabulary.isPartOf);
            superProperties.add(SpatialVocabulary.isProperPartOf);
        }

        // TODO: isSpatiallyIdenticalWith
        // TODO: hasTangentialProperPart
        // TODO: hasNonTangentialProperPart
        // TODO: isExternallyConnectedWith
        // TODO: isDisconnectedFrom
        // TODO: isNear
        // TODO: startsNear
        // TODO: endsNear
        // TODO: crosses
        // TODO: runsAlong

        return superProperties;
    }

    @Override
    protected SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty dataProperty) {
        SortedSet<OWLDataProperty> superProperties = baseReasoner.getSuperProperties(dataProperty);

        // TODO: add spatial aspect-specific stuff here

        return superProperties;
    }

    @Override
    protected Set<OWLObjectProperty> getObjectPropertiesImpl() {
        return Sets.union(
                baseReasoner.getObjectProperties(),
                SpatialVocabulary.spatialObjectProperties);
    }

    @Override
    protected OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
        // isConnectedWith
        if (objectProperty.equals(SpatialVocabulary.isConnectedWith)) {
            return SpatialVocabulary.SpatialFeature;

        // overlapsWith
        } else if (objectProperty.equals(SpatialVocabulary.overlapsWith)) {
            return SpatialVocabulary.SpatialFeature;

        // isPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // hasPart
        } else if (objectProperty.equals(SpatialVocabulary.hasPart)) {
            return SpatialVocabulary.SpatialFeature;

        // isProperPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isProperPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // hasProperPart
        } else if (objectProperty.equals(SpatialVocabulary.hasProperPart)) {
            return SpatialVocabulary.SpatialFeature;

        // partiallyOverlapsWith
        } else if (objectProperty.equals(SpatialVocabulary.partiallyOverlapsWith)) {
            return SpatialVocabulary.SpatialFeature;

        // isTangentialProperPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isTangentialProperPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // isNonTangentialProperPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // TODO: isSpatiallyIdenticalWith
        // TODO: hasTangentialProperPart
        // TODO: hasNonTangentialProperPart
        // TODO: isExternallyConnectedWith
        // TODO: isDisconnectedFrom
        // TODO: isNear
        // TODO: startsNear
        // TODO: endsNear
        // TODO: crosses
        // TODO: runsAlong

        // TODO: Add further spatial object properties here

        } else {
            return baseReasoner.getRange(objectProperty);
        }
    }

    @Override
    protected OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
        // isConnectedWith
        if (objectProperty.equals(SpatialVocabulary.isConnectedWith)) {
            return SpatialVocabulary.SpatialFeature;

        // overlapsWith
        } else if (objectProperty.equals(SpatialVocabulary.overlapsWith)) {
            return SpatialVocabulary.SpatialFeature;

        // isPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // hasPart
        } else if (objectProperty.equals(SpatialVocabulary.hasPart)) {
            return SpatialVocabulary.SpatialFeature;

        // isProperPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isProperPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // hasProperPart
        } else if (objectProperty.equals(SpatialVocabulary.hasProperPart)) {
            return SpatialVocabulary.SpatialFeature;

        // partiallyOverlapsWith
        } else if (objectProperty.equals(SpatialVocabulary.partiallyOverlapsWith)) {
            return SpatialVocabulary.SpatialFeature;

        // isTangentialProperPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isTangentialProperPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // isNonTangentialProperPartOf
        } else if (objectProperty.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
            return SpatialVocabulary.SpatialFeature;

        // TODO: isSpatiallyIdenticalWith
        // TODO: hasTangentialProperPart
        // TODO: hasNonTangentialProperPart
        // TODO: isExternallyConnectedWith
        // TODO: isDisconnectedFrom
        // TODO: isNear
        // TODO: startsNear
        // TODO: endsNear
        // TODO: crosses
        // TODO: runsAlong

        // TODO: Add further spatial object properties here
        } else {
            return baseReasoner.getDomain(objectProperty);
        }
    }

    @Override
    protected OWLClassExpression getDomainImpl(OWLDataProperty dataProperty) {
        // TODO: Add spatial data property handling here

        return baseReasoner.getDomain(dataProperty);
    }

    @Override
    protected Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
        // TODO: Add spatial int data properties here

        return baseReasoner.getIntDatatypeProperties();
    }

    @Override
    protected Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
        // TODO: Add spatial double data properties here

        return baseReasoner.getDoubleDatatypeProperties();
    }

    @Override
    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getPropertyMembersImpl(
            OWLObjectProperty objectProperty) {
        if (SpatialVocabulary.spatialObjectProperties.contains(objectProperty)) {
            // isConnectedWith
            if (objectProperty.equals(SpatialVocabulary.isConnectedWith)) {
                return getIsConnectedWithMembers();

            // overlapsWith
            } else if (objectProperty.equals(SpatialVocabulary.overlapsWith)) {
                return getOverlapsWithMembers();

            // isPartOf
            } else if (objectProperty.equals(SpatialVocabulary.isPartOf)) {
                return getIsPartOfMembers();

            // hasPart
            } else if (objectProperty.equals(SpatialVocabulary.hasPart)) {
                return getHasPartMembers();

            // isProperPartOf
            } else if (objectProperty.equals(SpatialVocabulary.isProperPartOf)) {
                return getIsProperPartOfMembers();

            // hasProperPart
            } else if (objectProperty.equals(SpatialVocabulary.hasProperPart)) {
                return getHasProperPartMembers();

            // partiallyOverlapsWith
            } else if (objectProperty.equals(SpatialVocabulary.partiallyOverlapsWith)) {
                return getPartiallyOverlapsWithMembers();

            // isTangentialProperPartOf
            } else if (objectProperty.equals(SpatialVocabulary.isTangentialProperPartOf)) {
                return getIsTangentialProperPartOfMembers();

            // isNonTangentialProperPartOf
            } else if (objectProperty.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
                return getIsNonTangentialProperPartOfMembers();

            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong

            // TODO: Add further spatial object properties here
            } else {
                throw new RuntimeException(
                        "Spatial object property " + objectProperty + "not " +
                                "handled in getPropertyMembersImpl( )");
            }
        } else {
            return baseReasoner.getPropertyMembers(objectProperty);
        }
    }

    @Override
    protected Set<OWLDataProperty> getBooleanDatatypePropertiesImpl() {
        // TODO: Add spatial boolean data properties here

        return baseReasoner.getBooleanDatatypeProperties();
    }

    @Override
    protected Set<OWLDataProperty> getStringDatatypePropertiesImpl() {
        // TODO: Add spatial string data properties here

        return baseReasoner.getDatatypeProperties();
    }

    @Override
    public boolean hasTypeImpl(OWLClassExpression ce, OWLIndividual individual) {
        if (containsSpatialExpressions(ce)) {
            return hasTypeSpatial(ce, individual);
        } else {
            return baseReasoner.hasType(ce, individual);
        }
    }

    // -------------------------------------------------------------------------
    // -- implemented methods from interface/(abstract) base class

    @Override
    protected Set<OWLClass> getInconsistentClassesImpl() {
        return baseReasoner.getInconsistentClasses();
    }

    @Override
    public ReasonerType getReasonerType() {
        throw new NotImplementedException();
    }

    @Override
    public void releaseKB() {
        throw new NotImplementedException();
    }

    @Override
    public OWLDatatype getDatatype(OWLDataProperty dp) {
        // TODO: Add spatial data property handling here

        return baseReasoner.getDatatype(dp);
    }

    @Override
    public void setSynchronized() {
        throw new NotImplementedException();
    }

    @Override
    public Set<OWLClass> getClasses() {
        return Sets.union(baseReasoner.getClasses(), SpatialVocabulary.spatialClasses);
    }

    @Override
    public SortedSet<OWLIndividual> getIndividuals() {
        return baseReasoner.getIndividuals();
    }

    @Override
    protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression concept) {
        if (!containsSpatialExpressions(concept)) {
            return baseReasoner.getIndividuals(concept);
        } else {
            if (concept instanceof OWLObjectIntersectionOf) {
                return getIndividualsOWLObjectIntersectionOf((OWLObjectIntersectionOf) concept);

            } else if (concept instanceof OWLObjectSomeValuesFrom) {
                return getIndividualsOWLObjectSomeValuesFrom((OWLObjectSomeValuesFrom) concept);

            } else if (concept instanceof OWLObjectMinCardinality) {
                return getIndividualsOWLObjectMinCardinality((OWLObjectMinCardinality) concept);

            } else if (concept instanceof OWLObjectAllValuesFrom) {
                return getIndividualsOWLObjectAllValuesFrom((OWLObjectAllValuesFrom) concept);

            } else if (concept instanceof OWLObjectMaxCardinality) {
                return getIndividualsOWLObjectMaxCardinality((OWLObjectMaxCardinality) concept);

            } else if (concept instanceof OWLObjectUnionOfImplExt) {
                return getIndividualsOWLObjectUnionOfImplExt((OWLObjectUnionOfImplExt) concept);

            } else {
                throw new RuntimeException(
                        "Support for class expression of type " + concept.getClass() +
                                " not implemented, yet");
            }
        }
    }

    @Override
    public String getBaseURI() {
        return baseReasoner.getBaseURI();
    }

    @Override
    public Map<String, String> getPrefixes() {
        return baseReasoner.getPrefixes();
    }

    @Override
    public void init() throws ComponentInitException {
        baseReasoner.init();

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new ComponentInitException(e);
        }
        StringBuilder url = new StringBuilder("jdbc:postgresql://")
                .append(hostname).append(":")
                .append(port).append("/")
                .append(dbName);

        try {
            conn = DriverManager.getConnection(url.toString(), dbUser, dbUserPW);
        } catch (SQLException e) {
            throw new ComponentInitException(e);
        }

        try {
            ((org.postgresql.PGConnection) conn).addDataType(
                    "geometry",
                    (Class<? extends PGobject>) Class.forName("org.postgis.PGgeometry"));
        } catch (SQLException | ClassNotFoundException e) {
            throw new ComponentInitException(e);
        }
    }

    @Override
    public boolean isConnectedWith(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {
        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;
        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        /* Two individuals are connected if they have at least one point in
         * common. This is pretty well covered by the ST_Intersects function for
         * all kinds of geo features.
         */
        String queryStr =
                "SELECT " +
                        "ST_Intersects(l.the_geom, r.the_geom) c " +
                "FROM " +
                        tableName1 + " l, " +
                        tableName2 + " r " +
                "WHERE " +
                        "l.iri=? " + // #1
                "AND " +
                        "r.iri=? ";  // #2
        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean areConnected = resSet.getBoolean("c");
            return areConnected;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsConnectedWith(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;
        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        /* Two individuals are connected if they have at least one point in
         * common. This is pretty well covered by the ST_Intersects function for
         * all kinds of geo features.
         */
        String queryStr =
                "SELECT " +
                        "r.iri c " +
                "FROM " +
                        tableName + " l, " +
                        pointFeatureTableName + " r " +
                "WHERE " +
                        "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                        "l.iri=? " + // #1
                "UNION " +
                "SELECT " +
                        "r.iri c " +
                "FROM " +
                        tableName + " l, " +
                        lineFeatureTableName + " r " +
                "WHERE " +
                        "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                        "l.iri=? " + // #2
                "UNION " +
                "SELECT " +
                        "r.iri c " +
                "FROM " +
                        tableName + " l, " +
                        areaFeatureTableName + " r " +
                "WHERE " +
                        "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                        "l.iri=?"; // #3

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());
            statement.setString(2, geomIndividual.toStringID());
            statement.setString(3, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("c");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getIsConnectedWithMembers() {
        String queryStr =
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    pointFeatureTableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "l.the_geom=r.the_geom " +
                "UNION " +
                "SELECT " +
                    "l.iri, " +
                    "r.iri " +
                "FROM " +
                    pointFeatureTableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "UNION " +
                "SELECT " +
                    "l.iri, " +
                    "r.iri " +
                "FROM " +
                    pointFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "UNION " +
                "SELECT " +
                    "l.iri, " +
                    "r.iri " +
                "FROM " +
                    lineFeatureTableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "UNION " +
                "SELECT " +
                    "l.iri, " +
                    "r.iri " +
                "FROM " +
                    lineFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "UNION " +
                "SELECT " +
                    "l.iri, " +
                    "r.iri " +
                "FROM " +
                    areaFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom)";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String geom1IRIStr = resultSet.getString("l_iri");
                String geom2IRIStr = resultSet.getString("r_iri");

                OWLIndividual geomIndividual1 =
                        new OWLNamedIndividualImpl(IRI.create(geom1IRIStr));
                OWLIndividual geomIndividual2 =
                        new OWLNamedIndividualImpl(IRI.create(geom2IRIStr));

                // convert geometries to features
                OWLIndividual featureIndividual1 =
                        geom2feature.get(geomIndividual1);
                OWLIndividual featureIndividual2 =
                        geom2feature.get(geomIndividual2);

                if (!members.containsKey(featureIndividual1)) {
                    members.put(featureIndividual1, new TreeSet<>());
                }
                members.get(featureIndividual1).add(featureIndividual2);

                if (!members.containsKey(featureIndividual2)) {
                    members.put(featureIndividual2, new TreeSet<>());
                }
                members.get(featureIndividual2).add(featureIndividual1);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean overlapsWith(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {
        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (tableName1.equals(pointFeatureTableName) || tableName2.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "ST_Intersects(l.the_geom, r.the_geom) o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";

        } else if (tableName1.equals(lineFeatureTableName) && tableName2.equals(areaFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "(" +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) " +
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) " +
                    ") o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";
        } else if (tableName1.equals(areaFeatureTableName) && tableName2.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "(" +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Overlaps(ST_Boundary(l.the_geom), r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    ") o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";
        } else {
            queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) " +
                    ") o " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r ";
        }

        queryStr +=
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean areOverlapping = resSet.getBoolean("o");
            return areOverlapping;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsOverlappingWith(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        /* Note: ST_Overlaps( ) doesn't do here. This is the description from
         * the PostGIS website:
         * "Geometry A contains Geometry B if and only if no points of B lie in
         *  the exterior of A, and at least one point of the interior of B lies
         *  in the interior of A. An important subtlety of this definition is
         *  that A does not contain its boundary, but A does contain itself."
         *
         * So the rather means 'partially overlaps' in our definition.
         */

        String queryStr = "";

        if (tableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "l.the_geom=r.the_geom " +
                "AND " +
                    "l.iri=?" +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? ";

        } else {
            queryStr +=
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom)" +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) ";
            if (tableName.equals(areaFeatureTableName)) {
                queryStr +=
                    "OR " +
                        "ST_Overlaps(ST_Boundary(l.the_geom), r.the_geom) ";
            } else {
                queryStr +=
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) ";
            }
            queryStr +=
                    ") " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri o " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) ";
            if (tableName.equals(lineFeatureTableName)) {
                queryStr +=
                    "OR " +
                        "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) ";
            }
            queryStr +=
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom)" +
                    ") " +
                "AND " +
                    "l.iri=?";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());
            statement.setString(2, geomIndividual.toStringID());
            statement.setString(3, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("o");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getOverlapsWithMembers() {
        String queryStr =
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    pointFeatureTableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "l.the_geom=r.the_geom " +
                "UNION " +
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    pointFeatureTableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "UNION " +
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    pointFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom) " +
                "UNION " +
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    lineFeatureTableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) " +
                    ") " +
                "UNION " +
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri l_iri " +
                "FROM " +
                    lineFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) " +
                    ") " +
                "UNION " +
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    areaFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(r.the_geom, l.the_geom) " +
                    ")";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String geom1IRIStr = resultSet.getString("l_iri");
                String geom2IRIStr = resultSet.getString("r_iri");

                OWLIndividual geomIndividual1 =
                        new OWLNamedIndividualImpl(IRI.create(geom1IRIStr));
                OWLIndividual geomIndividual2 =
                        new OWLNamedIndividualImpl(IRI.create(geom2IRIStr));

                // convert geometries to features
                OWLIndividual featureIndividual1 =
                        geom2feature.get(geomIndividual1);
                OWLIndividual featureIndividual2 =
                        geom2feature.get(geomIndividual2);

                if (!members.containsKey(featureIndividual1)) {
                    members.put(featureIndividual1, new TreeSet<>());
                }
                members.get(featureIndividual1).add(featureIndividual2);

                if (!members.containsKey(featureIndividual2)) {
                    members.put(featureIndividual2, new TreeSet<>());
                }
                members.get(featureIndividual2).add(featureIndividual1);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean isPartOf(OWLIndividual part, OWLIndividual whole) {
        String partTable = getTable(part);
        String wholeTable = getTable(whole);

        OWLIndividual partGeom;
        OWLIndividual wholeGeom;

        try {
            partGeom = feature2geom.get(part);
            wholeGeom = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        if (wholeTable.equals(pointFeatureTableName) &&
                (partTable.equals(lineFeatureTableName) || partTable.equals(areaFeatureTableName))) {
            return false;
        } else if (wholeTable.equals(lineFeatureTableName) && partTable.equals(areaFeatureTableName)) {
            return false;
        }

        String queryStr = "";

        if (wholeTable.equals(pointFeatureTableName)) {
            // point - point
            queryStr +=
                "SELECT " +
                    "part.the_geom=whole.the_geom p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=?";
        } else if (wholeTable.equals(lineFeatureTableName)) {
            // line - point
            if (partTable.equals(pointFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "ST_Intersects(part.the_geom, whole.the_geom) p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";

            // line - line
            } else {
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Equals(whole.the_geom, part.the_geom) " +
                    ") p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }

        } else if (wholeTable.equals(areaFeatureTableName)) {
            if (partTable.equals(pointFeatureTableName)) {
                // area - point
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else if (partTable.equals(lineFeatureTableName)) {
                // area - line
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else {
                // area - area
                queryStr +=
                "SELECT " +
                    "ST_Contains(whole.the_geom, part.the_geom) p " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeom.toStringID());
            statement.setString(2, wholeGeom.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean isPartOf = resSet.getBoolean("p");
            return isPartOf;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsPartOf(OWLIndividual whole) {
        String tableName = getTable(whole);

        OWLIndividual wholeGeom;

        try {
            wholeGeom = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";


        if (tableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "l.the_geom=r.the_geom " +
                "AND " +
                    "l.iri=?";
        } else if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, r.the_geom)" +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "(" +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Equals(l.the_geom, r.the_geom) " +
                    ") " +
                "AND " +
                    "l.iri=? ";
        } else {
            queryStr +=
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(l.the_geom), r.the_geom) " +
                    ") " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri p " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Contains(l.the_geom, r.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(l.the_geom), r.the_geom) " +
                    ") " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Contains(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=?";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, wholeGeom.toStringID());

            if (tableName.equals(lineFeatureTableName)) {
                statement.setString(2, wholeGeom.toStringID());
            } else if (tableName.equals(areaFeatureTableName)) {
                statement.setString(2, wholeGeom.toStringID());
                statement.setString(3, wholeGeom.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("p");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getIsPartOfMembers() {
        String queryStr =
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    pointFeatureTableName + " whole " +
                "WHERE " +
                    "part.the_geom=whole.the_geom " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(part.the_geom, whole.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Equals(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    areaFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom)";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String partGeomIRI = resultSet.getString("part");
                String wholeGeomIRI = resultSet.getString("whole");

                OWLIndividual partGeomIndividual1 =
                        new OWLNamedIndividualImpl(IRI.create(partGeomIRI));
                OWLIndividual wholeGeomIndividual2 =
                        new OWLNamedIndividualImpl(IRI.create(wholeGeomIRI));

                // convert geometries to features
                OWLIndividual partFeatureIndividual1 =
                        geom2feature.get(partGeomIndividual1);
                OWLIndividual wholeFeatureIndividual2 =
                        geom2feature.get(wholeGeomIndividual2);

                if (!members.containsKey(partFeatureIndividual1)) {
                    members.put(partFeatureIndividual1, new TreeSet<>());
                }
                members.get(partFeatureIndividual1).add(wholeFeatureIndividual2);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean hasPart(OWLIndividual whole, OWLIndividual part) {
        return isPartOf(part, whole);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsHavingPart(OWLIndividual part) {
        String tableName = getTable(part);

        OWLIndividual partGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (tableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    pointFeatureTableName + " whole " +
                "WHERE " +
                    "part.the_geom=whole.the_geom " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";
        } else if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Equals(whole.the_geom, part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";
        } else {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    tableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=?";
        }
//
        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());

            if (tableName.equals(lineFeatureTableName)) {
                statement.setString(2, partGeomIndividual.toStringID());
            } else if (tableName.equals(pointFeatureTableName)) {
                statement.setString(2, partGeomIndividual.toStringID());
                statement.setString(3, partGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("w");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getHasPartMembers() {
        String queryStr =
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    pointFeatureTableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "whole.iri=part.iri " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri " +
                "FROM " +
                    lineFeatureTableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    lineFeatureTableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Equals(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    areaFeatureTableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    areaFeatureTableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    areaFeatureTableName + " whole, " +
                    areaFeatureTableName + " part " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) ";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String partGeomIRI = resultSet.getString("whole");
                String wholeGeomIRI = resultSet.getString("part");

                OWLIndividual partGeomIndividual1 =
                        new OWLNamedIndividualImpl(IRI.create(partGeomIRI));
                OWLIndividual wholeGeomIndividual2 =
                        new OWLNamedIndividualImpl(IRI.create(wholeGeomIRI));

                // convert geometries to features
                OWLIndividual partFeatureIndividual1 =
                        geom2feature.get(partGeomIndividual1);
                OWLIndividual wholeFeatureIndividual2 =
                        geom2feature.get(wholeGeomIndividual2);

                if (!members.containsKey(partFeatureIndividual1)) {
                    members.put(partFeatureIndividual1, new TreeSet<>());
                }
                members.get(partFeatureIndividual1).add(wholeFeatureIndividual2);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean isProperPartOf(OWLIndividual part, OWLIndividual whole) {
        String partTable = getTable(part);
        String wholeTable = getTable(whole);

        if (wholeTable.equals(pointFeatureTableName)) {
            return false;

        } else if (wholeTable.equals(lineFeatureTableName) &&
                partTable.equals(areaFeatureTableName)) {

            return false;
        }

        OWLIndividual partGeomIndividual;
        OWLIndividual wholeGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (wholeTable.equals(lineFeatureTableName)) {
            if (partTable.equals(pointFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "ST_Intersects(whole.the_geom, part.the_geom) pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=?";

            } else {
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "AND " +
                        "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                    ") pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }
        } else if (wholeTable.equals(areaFeatureTableName)) {
            if (partTable.equals(pointFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else if (partTable.equals(lineFeatureTableName)) {
                queryStr +=
                "SELECT  " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else {
                // area feature
                queryStr +=
                "SELECT " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) pp " +
                "FROM " +
                    partTable + " part, " +
                    wholeTable + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=?";
            }
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean isProperPartOf = resSet.getBoolean("pp");
            return isProperPartOf;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsProperPartOf(OWLIndividual whole) {
        String tableName = getTable(whole);

        if (tableName.equals(pointFeatureTableName)) {
            // A point cannot have a proper part
            return Stream.empty();
        }

        OWLIndividual wholeGeomIndividual;

        try {
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";
        if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +  // as ST_ContainsProperly() has slightly different semantics
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? ";
        } else {
            // area feature
            queryStr +=
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ")" +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri pp " +
                "FROM " +
                    tableName + " whole, " +
                    areaFeatureTableName + " part " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, wholeGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            if (tableName.equals(areaFeatureTableName)) {
                statement.setString(3, wholeGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("pp");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getIsProperPartOfMembers() {
        String queryStr =
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part," +
                    "whole.iri whole " +
                "FROM " +
                    areaFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom)";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String partGeomIRI = resultSet.getString("part");
                String wholeGeomIRI = resultSet.getString("whole");

                OWLIndividual partGeomIndividual1 =
                        new OWLNamedIndividualImpl(IRI.create(partGeomIRI));
                OWLIndividual wholeGeomIndividual2 =
                        new OWLNamedIndividualImpl(IRI.create(wholeGeomIRI));

                // convert geometries to features
                OWLIndividual partFeatureIndividual1 =
                        geom2feature.get(partGeomIndividual1);
                OWLIndividual wholeFeatureIndividual2 =
                        geom2feature.get(wholeGeomIndividual2);

                if (!members.containsKey(partFeatureIndividual1)) {
                    members.put(partFeatureIndividual1, new TreeSet<>());
                }
                members.get(partFeatureIndividual1).add(wholeFeatureIndividual2);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean hasProperPart(OWLIndividual whole, OWLIndividual part) {
        return isProperPartOf(part, whole);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsHavingProperPart(OWLIndividual part) {
        String partTableName = getTable(part);

        OWLIndividual partGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (partTableName.equals(pointFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";

        } else if (partTableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +
                "UNION " +
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "OR " +
                        "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") " +
                "AND " +
                    "part.iri=? ";
        } else {
            queryStr +=
                "SELECT " +
                    "whole.iri w " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=?";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());

            if (partTableName.equals(lineFeatureTableName) ||
                    partTableName.equals(pointFeatureTableName)) {

                statement.setString(2, partGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("w");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getHasProperPartMembers() {
        String queryStr =
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    lineFeatureTableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    lineFeatureTableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    areaFeatureTableName + " whole, " +
                    pointFeatureTableName + " part " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    areaFeatureTableName + " whole, " +
                    lineFeatureTableName + " part " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "OR " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "whole.iri whole, " +
                    "part.iri part " +
                "FROM " +
                    areaFeatureTableName + " whole, " +
                    areaFeatureTableName + " part " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) ";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String wholeGeomIRI = resultSet.getString("whole");
                String partGeomIRI = resultSet.getString("part");

                OWLIndividual wholeGeomIndividual =
                        new OWLNamedIndividualImpl(IRI.create(wholeGeomIRI));
                OWLIndividual partGeomIndividual =
                        new OWLNamedIndividualImpl(IRI.create(partGeomIRI));

                // convert geometries to features
                OWLIndividual wholeFeatureIndividual =
                        geom2feature.get(wholeGeomIndividual);
                OWLIndividual partFeatureIndividual =
                        geom2feature.get(partGeomIndividual);

                if (!members.containsKey(wholeFeatureIndividual)) {
                    members.put(wholeFeatureIndividual, new TreeSet<>());
                }
                members.get(wholeFeatureIndividual).add(partFeatureIndividual);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean partiallyOverlapsWith(
            OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {

        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        if (tableName1.equals(pointFeatureTableName) ||
                tableName2.equals(pointFeatureTableName)) {

            // point features cannot overlap with anything
            return false;
        }

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);

        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";
        if (tableName1.equals(lineFeatureTableName)) {
            if (tableName2.equals(lineFeatureTableName)) {
                // line - line
                queryStr +=
                "SELECT " +
                    "ST_Overlaps(l.the_geom, r.the_geom) po " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else {
                // line - area
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) " +
                    "OR " +
                        "(ST_Intersects(l.the_geom, r.the_geom) " +
                        "AND NOT ST_Contains(r.the_geom, l.the_geom)) " +
                    ") po " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            }
        } else {
            if (tableName2.equals(lineFeatureTableName)) {
                // area - line
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Overlaps(ST_Boundary(l.the_geom), r.the_geom) " +
                    "OR " +
                        "(ST_Intersects(l.the_geom, r.the_geom) " +
                        "AND NOT ST_Contains(l.the_geom, r.the_geom))" +
                    ") po " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else {
                // area - area
                queryStr +=
                "SELECT " +
                    "ST_Overlaps(l.the_geom, r.the_geom) po " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            }
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean partiallyOverlaps = resSet.getBoolean("po");
            return partiallyOverlaps;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public  Stream<OWLIndividual> getIndividualsPartiallyOverlappingWith(
            OWLIndividual spatialFeatureIndividual) {

        String tableName = getTable(spatialFeatureIndividual);

        if (tableName.equals(pointFeatureTableName)) {
            return Stream.empty();
        }

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "r.iri po " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Overlaps(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri po " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) " +
                    "OR " +
                        "(ST_Intersects(l.the_geom, r.the_geom) " +
                        "AND NOT ST_Contains(r.the_geom, l.the_geom)) " +
                    ") " +
                "AND " +
                    "l.iri=? ";
        } else {
            // area feature
            queryStr +=
                "SELECT " +
                    "r.iri po " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "( " +
                        "ST_Overlaps(ST_Boundary(l.the_geom), r.the_geom) " +
                    "OR " +
                        "(ST_Intersects(l.the_geom, r.the_geom) " +
                        "AND NOT ST_Contains(l.the_geom, r.the_geom))" +
                    ") " +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri po " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Overlaps(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());
            statement.setString(2, geomIndividual.toStringID());


            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("po");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getPartiallyOverlapsWithMembers() {
        String queryStr =
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    lineFeatureTableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Overlaps(l.the_geom, r.the_geom) " +
                "UNION " +
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    lineFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Overlaps(l.the_geom, ST_Boundary(r.the_geom)) " +
                "OR " +
                    "(" +
                        "ST_Intersects(l.the_geom, r.the_geom) " +
                    "AND " +
                        "NOT ST_Contains(r.the_geom, l.the_geom)" +
                    ") " +
                "UNION " +
                "SELECT " +
                    "l.iri l_iri, " +
                    "r.iri r_iri " +
                "FROM " +
                    areaFeatureTableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Overlaps(l.the_geom, r.the_geom) ";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String geomIRI1 = resultSet.getString("l_iri");
                String geomIRI2 = resultSet.getString("r_iri");

                OWLIndividual geomIndividual1 =
                        new OWLNamedIndividualImpl(IRI.create(geomIRI1));
                OWLIndividual geomIndividual2 =
                        new OWLNamedIndividualImpl(IRI.create(geomIRI2));

                // convert geometries to features
                OWLIndividual featureIndividual1 =
                        geom2feature.get(geomIndividual1);
                OWLIndividual featureIndividual2 =
                        geom2feature.get(geomIndividual2);

                if (!members.containsKey(featureIndividual1)) {
                    members.put(featureIndividual1, new TreeSet<>());
                }
                members.get(featureIndividual1).add(featureIndividual2);

                if (!members.containsKey(featureIndividual2)) {
                    members.put(featureIndividual2, new TreeSet<>());
                }
                members.get(featureIndividual2).add(featureIndividual1);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean isTangentialProperPartOf(OWLIndividual part, OWLIndividual whole) {
        String partTableName = getTable(part);
        String wholeTableName = getTable(whole);

        if (wholeTableName.equals(pointFeatureTableName)) {

            return false;
        } else if (partTableName.equals(areaFeatureTableName)
                && wholeTableName.equals(lineFeatureTableName)) {

            return false;
        }

        OWLIndividual partGeomIndividual;
        OWLIndividual wholeGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (partTableName.equals(pointFeatureTableName)) {
            if (wholeTableName.equals(lineFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Intersects(whole.the_geom, part.the_geom) " +
                    "AND " +
                        "(" +
                            "ST_StartPoint(whole.the_geom)=part.the_geom " +
                        "OR " +
                            "ST_StartPoint(whole.the_geom)=part.the_geom " +
                        ")" +
                    ") tpp " +
                "FROM " +
                    partTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else {
                // point - area
                queryStr +=
                "SELECT " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) tpp " +
                "FROM " +
                    partTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }
        } else if (partTableName.equals(lineFeatureTableName)) {
            if (wholeTableName.equals(lineFeatureTableName)) {
                queryStr +=
                "SELECT " +
                    "( " +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "AND " +
                        "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                    "AND " +
                        "(" +
                            "ST_StartPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                        "OR " +
                            "ST_StartPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                        "OR " +
                            "ST_EndPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                        "OR " +
                            "ST_EndPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                        ")" +
                    ") tpp " +
                "FROM " +
                    partTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            } else {
                // line - area
                queryStr +=
                "SELECT " +
                    "(" +
                        "ST_Contains(whole.the_geom, part.the_geom) " +
                    "AND " +
                        "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                    ") tpp " +
                "FROM " +
                    partTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";
            }
        } else {
            // area feature
            queryStr +=
            "SELECT " +
                "(" +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), ST_Boundary(part.the_geom)) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                ") tpp " +
            "FROM " +
                partTableName + " part, " +
                wholeTableName + " whole " +
            "WHERE " +
                "part.iri=? " +
            "AND " +
                "whole.iri=? ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean isTangentialProperPart = resSet.getBoolean("tpp");
            return isTangentialProperPart;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsTangentialProperPartOf(OWLIndividual whole) {
        String wholeTableName = getTable(whole);

        if (wholeTableName.equals(pointFeatureTableName)) {
            return Stream.empty();
        }

        OWLIndividual wholeGeomIndividual;

        try {
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (wholeTableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "part.iri tpp " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "(" +
                        "ST_StartPoint(whole.the_geom)=part.the_geom " +
                    "OR " +
                        "ST_StartPoint(whole.the_geom)=part.the_geom " +
                    ")" +
                "AND " +
                    "whole.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "part.iri tpp " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "(" +
                        "ST_StartPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                    "OR " +
                        "ST_StartPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                    "OR " +
                        "ST_EndPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                    "OR " +
                        "ST_EndPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                    ")" +
                "AND " +
                    "whole.iri=? ";  // #2
        } else {
            queryStr +=
                "SELECT " +
                    "part.iri tpp " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                "AND " +
                    "whole.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "part.iri tpp " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                "AND " +
                    "whole.iri=? " +  // #2
                "UNION " +
                "SELECT " +
                    "part.iri tpp " +
                "FROM " +
                    areaFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), ST_Boundary(part.the_geom)) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=?";  // #3
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, wholeGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            if (wholeTableName.equals(areaFeatureTableName)) {
                statement.setString(3, wholeGeomIndividual.toStringID());
            }
            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("tpp");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getIsTangentialProperPartOfMembers() {
        String queryStr =
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "(" +
                        "ST_StartPoint(whole.the_geom)=part.the_geom " +
                    "OR " +
                        "ST_StartPoint(whole.the_geom)=part.the_geom " +
                    ")" +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "(" +
                        "ST_StartPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                    "OR " +
                        "ST_StartPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                    "OR " +
                        "ST_EndPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                    "OR " +
                        "ST_EndPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                    ")" +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    areaFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), ST_Boundary(part.the_geom)) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) ";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String partGeomIRI = resultSet.getString("part");
                String wholeGeomIRI = resultSet.getString("whole");

                OWLIndividual partGeomIndividual =
                        new OWLNamedIndividualImpl(IRI.create(partGeomIRI));
                OWLIndividual wholeGeomIndividual =
                        new OWLNamedIndividualImpl(IRI.create(wholeGeomIRI));

                // convert geometries to features
                OWLIndividual partFeatureIndividual =
                        geom2feature.get(partGeomIndividual);
                OWLIndividual wholeFeatureIndividual =
                        geom2feature.get(wholeGeomIndividual);

                if (!members.containsKey(partFeatureIndividual)) {
                    members.put(partFeatureIndividual, new TreeSet<>());
                }
                members.get(partFeatureIndividual).add(wholeFeatureIndividual);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }

    @Override
    public boolean isNonTangentialProperPartOf(OWLIndividual part, OWLIndividual whole) {
        String partTableName = getTable(part);
        String wholeTableName = getTable(whole);

        if (wholeTableName.equals(pointFeatureTableName)) {
            return false;
        } else if (partTableName.equals(areaFeatureTableName)
                && wholeTableName.equals(lineFeatureTableName)) {
            return false;
        }

        OWLIndividual partGeomIndividual;
        OWLIndividual wholeGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) ntpp " +
                "FROM " +
                    partTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "part.iri=? " +
                "AND " +
                    "whole.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean isNonTangentialProperPart = resSet.getBoolean("ntpp");
            return isNonTangentialProperPart;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsNonTangentialProperPartOf(OWLIndividual whole) {
        String wholeTableName = getTable(whole);

        if (wholeTableName.equals(pointFeatureTableName)) {
            return Stream.empty();
        }

        OWLIndividual wholeGeomIndividual;

        try {
            wholeGeomIndividual = feature2geom.get(whole);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr = "";

        if (wholeTableName.equals(lineFeatureTableName)) {
            queryStr +=
                "SELECT " +
                    "part.iri ntpp " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri ntpp " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? ";
        } else {
            // area feature
            queryStr +=
                "SELECT " +
                    "part.iri ntpp " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri ntpp " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? " +
                "UNION " +
                "SELECT " +
                    "part.iri ntpp " +
                "FROM " +
                    areaFeatureTableName + " part, " +
                    wholeTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "whole.iri=? ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, wholeGeomIndividual.toStringID());
            statement.setString(2, wholeGeomIndividual.toStringID());

            if (wholeTableName.equals(areaFeatureTableName)) {
                statement.setString(3, wholeGeomIndividual.toStringID());
            }
            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("ntpp");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    protected Map<OWLIndividual, SortedSet<OWLIndividual>> getIsNonTangentialProperPartOfMembers() {
        String queryStr =
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    pointFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    lineFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "UNION " +
                "SELECT " +
                    "part.iri part, " +
                    "whole.iri whole " +
                "FROM " +
                    areaFeatureTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) ";

        Map<OWLIndividual, SortedSet<OWLIndividual>> members = new HashMap<>();

        try {
            Statement statement = conn.createStatement();
            ResultSet resultSet = statement.executeQuery(queryStr);

            while (resultSet.next()) {
                String partGeomIRI = resultSet.getString("part");
                String wholeGeomIRI = resultSet.getString("whole");

                OWLIndividual partGeomIndividual =
                        new OWLNamedIndividualImpl(IRI.create(partGeomIRI));
                OWLIndividual wholeGeomIndividual =
                        new OWLNamedIndividualImpl(IRI.create(wholeGeomIRI));

                // convert geometries to features
                OWLIndividual partFeatureIndividual =
                        geom2feature.get(partGeomIndividual);
                OWLIndividual wholeFeatureIndividual =
                        geom2feature.get(wholeGeomIndividual);

                if (!members.containsKey(partFeatureIndividual)) {
                    members.put(partFeatureIndividual, new TreeSet<>());
                }
                members.get(partFeatureIndividual).add(wholeFeatureIndividual);
            }

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        return members;
    }
    @Override
    public boolean isSpatiallyIdenticalWith(
            OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {

        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        if (!tableName1.equals(tableName2)) {
            return false;
        }

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "ST_Equals(l.the_geom, r.the_geom) eq " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean spatiallyEquals = resSet.getBoolean("eq");
            return spatiallyEquals;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public  Stream<OWLIndividual> getIndividualsSpatiallyIdenticalWith(
            OWLIndividual spatialFeatureIndividual) {

        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "r.iri eq " +
                "FROM " +
                    tableName + " l, " +
                    tableName + " r " +
                "WHERE " +
                    "ST_Equals(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("eq");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasTangentialProperPart(OWLIndividual whole, OWLIndividual part) {
        return isTangentialProperPartOf(part, whole);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsHavingTangentialProperPart(OWLIndividual part) {
        String partTableName = getTable(part);

        OWLIndividual partGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr;

        if (partTableName.equals(pointFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "whole.iri tppi " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Intersects(whole.the_geom, part.the_geom) " +
                "AND " +
                    "(" +
                        "ST_StartPoint(whole.the_geom)=part.the_geom " +
                    "OR " +
                        "ST_StartPoint(whole.the_geom)=part.the_geom " +
                    ")" +
                "AND " +
                    "part.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "whole.iri tppi " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(ST_Boundary(whole.the_geom), part.the_geom) " +
                "AND " +
                    "part.iri=? ";  // #2

        } else if (partTableName.equals(lineFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "whole.iri tppi " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "(" +
                        "ST_StartPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                    "OR " +
                        "ST_StartPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                    "OR " +
                        "ST_EndPoint(whole.the_geom)=ST_StartPoint(part.the_geom) " +
                    "OR " +
                        "ST_EndPoint(whole.the_geom)=ST_EndPoint(part.the_geom) " +
                    ")" +
                "AND " +
                    "part.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "whole.iri tppi " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), part.the_geom) " +
                "AND " +
                    "part.iri=? ";  // #2

        } else {
            // area feature
            queryStr =
                "SELECT " +
                    "whole.iri tppi " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_Contains(whole.the_geom, part.the_geom) " +
                "AND " +
                    "ST_Intersects(ST_Boundary(whole.the_geom), ST_Boundary(part.the_geom)) " +
                "AND " +
                    "NOT ST_Equals(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? ";  // #1
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());

            if (partTableName.equals(pointFeatureTableName)
                    || partTableName.equals(lineFeatureTableName)) {

                statement.setString(2, partGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("tppi");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasNonTangentialProperPart(OWLIndividual whole, OWLIndividual part) {
        return isNonTangentialProperPartOf(part, whole);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsHavingNonTangentialProperPart(OWLIndividual part) {
        String partTableName = getTable(part);

        OWLIndividual partGeomIndividual;

        try {
            partGeomIndividual = feature2geom.get(part);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr;

        if (partTableName.equals(pointFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "whole.iri ntppi " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "whole.iri ntppi " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? ";  // #2

        } else if (partTableName.equals(lineFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "whole.iri ntppi " +
                "FROM " +
                    partTableName + " part, " +
                    lineFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "whole.iri ntppi " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? ";  // #2

        } else {
            // area feature
            queryStr =
                "SELECT " +
                    "whole.iri ntppi " +
                "FROM " +
                    partTableName + " part, " +
                    areaFeatureTableName + " whole " +
                "WHERE " +
                    "ST_ContainsProperly(whole.the_geom, part.the_geom) " +
                "AND " +
                    "part.iri=? ";  // #1
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, partGeomIndividual.toStringID());

            if (partTableName.equals(pointFeatureTableName)
                    || partTableName.equals(lineFeatureTableName)) {

                statement.setString(2, partGeomIndividual.toStringID());
            }

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("ntppi");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean isExternallyConnectedWith(OWLIndividual spatialIndividual1, OWLIndividual spatialIndividual2) {
        String tableName1 = getTable(spatialIndividual1);
        String tableName2 = getTable(spatialIndividual2);

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(spatialIndividual1);
            geomIndividual2 = feature2geom.get(spatialIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr;

        if (tableName1.equals(pointFeatureTableName)) {
            if (tableName2.equals(pointFeatureTableName)) {
                queryStr =
                "SELECT " +
                    "l.the_geom=r.the_geom ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else if (tableName2.equals(lineFeatureTableName)) {
                queryStr =
                "SELECT " +
                    "(" +
                        "l.the_geom=ST_StartPoint(r.the_geom) " +
                    "OR " +
                        "l.the_geom=ST_EndPoint(r.the_geom) " +
                    ") ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else {
                // point - area
                queryStr =
                "SELECT " +
                    "ST_Intersects(l.the_geom, ST_Boundary(r.the_geom)) ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            }
        } else if (tableName1.equals(lineFeatureTableName)) {
            if (tableName2.equals(pointFeatureTableName)) {
                queryStr =
                "SELECT " +
                    "(" +
                        "ST_StartPoint(l.the_geom)=r.the_geom " +
                    "OR " +
                        "ST_EndPoint(l.the_geom)=r.the_geom " +
                    ") ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else if (tableName2.equals(lineFeatureTableName)) {
                queryStr =
                "SELECT " +
                    "(" +
                        "NOT ST_Equals(l.the_geom, r.the_geom)" +
                    "AND " +
                        "( " +
                            "ST_StartPoint(l.the_geom)=ST_StartPoint(r.the_geom) " +
                        "OR " +
                            "ST_StartPoint(l.the_geom)=ST_EndPoint(r.the_geom) " +
                        "OR " +
                            "ST_EndPoint(l.the_geom)=ST_StartPoint(r.the_geom) " +
                        "OR " +
                            "ST_EndPoint(l.the_geom)=ST_EndPoint(r.the_geom) " +
                        ")" +
                    ") ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else {
                // line - area
                /* Corner case:
                 * - line string l from A to B
                 * - polygon p: A-B-C-A
                 *
                 * Here we don't consider l as externally connected since there
                 * is no point in l 'external' to p.
                 */
                queryStr =
                "SELECT " +
                    "( " +
                        "NOT ST_Contains(r.the_geom, l.the_geom) " +
                    "AND " +
                        "(" +
                            "ST_Intersects(ST_StartPoint(l.the_geom), ST_Boundary(r.the_geom)) " +
                        "OR " +
                            "ST_Intersects(ST_EndPoint(l.the_geom), ST_Boundary(r.the_geom)) " +
                        ")" +
                    ") ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            }
        } else {
            // area
            if (tableName2.equals(pointFeatureTableName)) {
                queryStr =
                "SELECT " +
                    "ST_Intersects(ST_Boundary(l.the_geom), r.the_geom) ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else if (tableName2.equals(lineFeatureTableName)) {
                queryStr =
                "SELECT " +
                    "( " +
                        "NOT ST_Contains(l.the_geom, r.the_geom) " +
                    "AND " +
                        "(" +
                            "ST_Intersects(ST_Boundary(l.the_geom), ST_StartPoint(r.the_geom)) " +
                        "OR " +
                            "ST_Intersects(ST_Boundary(l.the_geom), ST_Endpoint(r.the_geom))" +
                        ")" +
                    ") ec " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            } else {
                // area - area
                queryStr =
                "SELECT " +
                    "ST_Touches(l.the_geom, r.the_geom) ec " +
                "FROM " +
                    tableName1 + " l , " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
            }
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean areExternallyConnected = resSet.getBoolean("ec");
            return areExternallyConnected;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getExternallyConnectedIndividuals(OWLIndividual spatialIndividual) {
        String tableName = getTable(spatialIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr;

        if (tableName.equals(pointFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "l.the_geom=r.the_geom " +
                "AND " +
                    "l.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "(" +
                        "l.the_geom=ST_StartPoint(r.the_geom) " +
                    "OR " +
                        "l.the_geom=ST_EndPoint(r.the_geom) " +
                    ") " +
                "AND " +
                    "l.iri=? " +  // #2
                "UNION " +
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(l.the_geom, ST_Boundary(r.the_geom)) " +
                "AND " +
                    "l.iri=? ";  // #3
        } else if (tableName.equals(lineFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "(" +
                        "ST_StartPoint(l.the_geom)=r.the_geom " +
                    "OR " +
                        "ST_EndPoint(l.the_geom)=r.the_geom " +
                    ") " +
                "AND " +
                    "l.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "NOT ST_Equals(l.the_geom, r.the_geom)" +
                "AND " +
                    "( " +
                        "ST_StartPoint(l.the_geom)=ST_StartPoint(r.the_geom) " +
                    "OR " +
                        "ST_StartPoint(l.the_geom)=ST_EndPoint(r.the_geom) " +
                    "OR " +
                        "ST_EndPoint(l.the_geom)=ST_StartPoint(r.the_geom) " +
                    "OR " +
                        "ST_EndPoint(l.the_geom)=ST_EndPoint(r.the_geom) " +
                    ")" +
                "AND " +
                    "l.iri=? " +
                "UNION " +
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "NOT ST_Contains(r.the_geom, l.the_geom) " +
                "AND " +
                    "(" +
                        "ST_Intersects(ST_StartPoint(l.the_geom), ST_Boundary(r.the_geom)) " +
                    "OR " +
                        "ST_Intersects(ST_EndPoint(l.the_geom), ST_Boundary(r.the_geom)) " +
                    ")" +
                "AND " +
                    "l.iri=? ";  // #3
        } else {
            // area
            queryStr =
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "ST_Intersects(ST_Boundary(l.the_geom),r.the_geom) " +
                "AND " +
                    "l.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "NOT ST_Contains(l.the_geom, r.the_geom) " +
                "AND " +
                    "(" +
                        "ST_Intersects(ST_Boundary(l.the_geom), ST_StartPoint(r.the_geom)) " +
                    "OR " +
                        "ST_Intersects(ST_Boundary(l.the_geom), ST_EndPoint(r.the_geom)) " +
                    ")" +
                "AND " +
                    "l.iri=? " +  // #2
                "UNION " +
                "SELECT " +
                    "r.iri ec " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_Touches(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=?";  // #3
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());
            statement.setString(2, geomIndividual.toStringID());
            statement.setString(3, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("ec");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public  boolean isDisconnectedFrom(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {
        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
            "SELECT " +
                "NOT ST_Intersects(l.the_geom, r.the_geom) dc " +
            "FROM " +
                tableName1 + " l, " +
                tableName2 + " r " +
            "WHERE " +
                "l.iri=? " +
            "AND " +
                "r.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean areDisconnected = resSet.getBoolean("dc");
            return areDisconnected;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsDisconnectedFrom(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "r.iri dc " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "NOT ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? " +  // #1
                "UNION " +
                "SELECT " +
                    "r.iri dc " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "NOT ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? " +  // #2
                "UNION " +
                "SELECT " +
                    "r.iri dc " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "NOT ST_Intersects(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=?";  // #3

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());
            statement.setString(2, geomIndividual.toStringID());
            statement.setString(3, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("dc");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // ---

    @Override
    public boolean isInside(OWLIndividual inner, OWLIndividual container) {
        return isPartOf(inner, container);
    }

    @Override
    public Stream<OWLIndividual> getIndividualsInside(OWLIndividual container) {
        return getIndividualsPartOf(container);
    }

    @Override
    public boolean isNear(OWLIndividual spatialFeatureIndividual1, OWLIndividual spatialFeatureIndividual2) {
        String tableName1 = getTable(spatialFeatureIndividual1);
        String tableName2 = getTable(spatialFeatureIndividual2);

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(spatialFeatureIndividual1);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "ST_DWithin(l.the_geom::geography, r.the_geom, ?, false) nr " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, geomIndividual1.toStringID());
            statement.setString(3, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean areNear = resSet.getBoolean("nr");
            return areNear;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsNear(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "r.iri nr " +
                "FROM " +
                    tableName + " l, " +
                    pointFeatureTableName + " r " +
                "WHERE " +
                    "ST_DWithin(l.the_geom::geography, r.the_geom, ?, false) " +  // #1
                "AND " +
                    "l.iri=? " +  // #2
                "UNION " +
                "SELECT " +
                    "r.iri nr " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_DWithin(l.the_geom::geography, r.the_geom, ?, false) " +  // #3
                "AND " +
                    "l.iri=? " +  // #4
                "UNION " +
                "SELECT " +
                    "r.iri nr " +
                "FROM " +
                    tableName + " l, " +
                    areaFeatureTableName + " r " +
                "WHERE " +
                    "ST_DWithin(l.the_geom::geography, r.the_geom, ?, false) " +  // #5
                "AND " +
                    "l.iri=? ";  // #6

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, geomIndividual.toStringID());
            statement.setDouble(3, nearRadiusInMeters);
            statement.setString(4, geomIndividual.toStringID());
            statement.setDouble(5, nearRadiusInMeters);
            statement.setString(6, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("nr");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public  boolean startsNear(
            OWLIndividual lineStringFeatureIndividual, OWLIndividual spatialFeatureIndividual) {
        String tableName1 = getTable(lineStringFeatureIndividual);
        String tableName2 = getTable(spatialFeatureIndividual);

        if (!tableName1.equals(lineFeatureTableName)) {
            return false;
        }

        OWLIndividual lineStringGeomIndividual;
        OWLIndividual geomIndividual2;

        try {
            lineStringGeomIndividual = feature2geom.get(lineStringFeatureIndividual);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "(" +
                        "ST_DWithin(ST_StartPoint(l.the_geom), r.the_geom, ?, false) " +
                    "AND " +
                        "NOT l.the_geom=r.the_geom " +
                    ") sn " +  // #1
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +  // #2
                "AND " +
                    "r.iri=? ";  // #3

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, lineStringGeomIndividual.toStringID());
            statement.setString(3, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean startsNear = resSet.getBoolean("sn");
            return startsNear;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsStartingNear(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "r.iri sn " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_DWithin(l.the_geom, ST_StartPoint(r.the_geom), ?, false) " +  // #1
                "AND " +
                    "l.iri=? ";  // #2
        if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "AND " +
                    "NOT l.the_geom=r.the_geom ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("sn");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean endsNear(OWLIndividual lineStringFeatureIndividual, OWLIndividual spatialFeatureIndividual) {
        String tableName1 = getTable(lineStringFeatureIndividual);
        String tableName2 = getTable(spatialFeatureIndividual);

        if (!tableName1.equals(lineFeatureTableName)) {
            return false;
        }

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(lineStringFeatureIndividual);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "(" +
                        "ST_DWithin(ST_EndPoint(l.the_geom), r.the_geom, ?, false) " +
                    "AND " +
                        "NOT l.the_geom=r.the_geom " +
                    ") en " +  // #1
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, geomIndividual1.toStringID());
            statement.setString(3, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean endsNear = resSet.getBoolean("en");
            return endsNear;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsEndingNear(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "r.iri sn " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_DWithin(l.the_geom, ST_EndPoint(r.the_geom), ?, false) " +  // #1
                "AND " +
                    "l.iri=? ";  // #2
        if (tableName.equals(lineFeatureTableName)) {
            queryStr +=
                "AND " +
                    "NOT l.the_geom=r.the_geom ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, nearRadiusInMeters);
            statement.setString(2, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("sn");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean crosses(OWLIndividual lineStringFeatureIndividual, OWLIndividual spatialFeatureIndividual) {
        String tableName1 = getTable(lineStringFeatureIndividual);
        String tableName2 = getTable(spatialFeatureIndividual);

        if (!tableName1.equals(lineFeatureTableName)) {
            return false;
        }

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;

        try {
            geomIndividual1 = feature2geom.get(lineStringFeatureIndividual);
            geomIndividual2 = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr;

        if (tableName2.equals(pointFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "ST_ContainsProperly(l.the_geom, r.the_geom) cr " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +  // #1
                "AND " +
                    "r.iri=? ";  // #2
        } else if (tableName2.equals(lineFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "ST_Crosses(l.the_geom, r.the_geom) cr " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
        } else {
            queryStr =
                "SELECT " +
                    "( " +
                        "ST_Crosses(l.the_geom, r.the_geom) " +
                    "AND " +
                        "NOT ST_Intersects(ST_StartPoint(l.the_geom), r.the_geom) " +
                    "AND " +
                        "NOT ST_Intersects(ST_EndPoint(l.the_geom), r.the_geom) " +
                    ") cr " +
                "FROM " +
                    tableName1 + " l, " +
                    tableName2 + " r " +
                "WHERE " +
                    "l.iri=? " +
                "AND " +
                    "r.iri=? ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual1.toStringID());
            statement.setString(2, geomIndividual2.toStringID());

            ResultSet resSet = statement.executeQuery();

            resSet.next();

            boolean endsNear = resSet.getBoolean("cr");
            return endsNear;

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public Stream<OWLIndividual> getIndividualsCrossing(OWLIndividual spatialFeatureIndividual) {
        String tableName = getTable(spatialFeatureIndividual);

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(spatialFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr;

        if (tableName.equals(pointFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "r.iri cr " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_ContainsProperly(r.the_geom, l.the_geom) " +
                "AND " +
                    "l.iri=? ";
        } else if (tableName.equals(lineFeatureTableName)) {
            queryStr =
                "SELECT " +
                    "r.iri cr " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Crosses(r.the_geom, l.the_geom) " +
                "AND " +
                    "l.iri=? ";
        } else {
            queryStr =
                "SELECT " +
                    "r.iri cr " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Crosses(r.the_geom, l.the_geom) " +
                "AND " +
                    "NOT ST_Intersects(ST_StartPoint(r.the_geom), l.the_geom) " +
                "AND " +
                    "NOT ST_Intersects(ST_EndPoint(r.the_geom), l.the_geom) " +
                "AND " +
                    "l.iri=? ";
        }

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setString(1, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("cr");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean runsAlong(OWLIndividual lineStringFeatureIndividual1, OWLIndividual lineStringFeatureIndividual2) {
        String tableName1 = getTable(lineStringFeatureIndividual1);
        String tableName2 = getTable(lineStringFeatureIndividual2);

        if (!(tableName1.equals(lineFeatureTableName) && tableName2.equals(lineFeatureTableName))) {
            return false;
        }

        OWLIndividual geomIndividual1;
        OWLIndividual geomIndividual2;
        try {
            geomIndividual1 = feature2geom.get(lineStringFeatureIndividual1);
            geomIndividual2 = feature2geom.get(lineStringFeatureIndividual2);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "(" +
                        "ST_Length( " +
                            "ST_Intersection( " +
                                "ST_Buffer(l.the_geom::geography, ?, 'endcap=flat'), " +  // #1
                                "r.the_geom)) > ? " +  // #2
                    "AND " +
                        "NOT ST_Equals(l.the_geom, r.the_geom)" +
                    ") ra " +
                "FROM " +
                    lineFeatureTableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "l.iri=? " + // #3
                "AND " +
                    "r.iri=?"; // 4

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, runsAlongToleranceInMeters);
            // min expected length
            statement.setDouble(2, 2.5 * runsAlongToleranceInMeters);
            statement.setString(3, geomIndividual1.toStringID());
            statement.setString(4, geomIndividual2.toStringID());

            ResultSet resultSet = statement.executeQuery();

            resultSet.next();

            boolean runsAlong = resultSet.getBoolean("ra");

            return runsAlong;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Stream<OWLIndividual> getIndividualsRunningAlong(OWLIndividual lineStringFeatureIndividual) {
        String tableName = getTable(lineStringFeatureIndividual);

        if (!tableName.equals(lineFeatureTableName)) {
            return Stream.empty();
        }

        OWLIndividual geomIndividual;

        try {
            geomIndividual = feature2geom.get(lineStringFeatureIndividual);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }

        String queryStr =
                "SELECT " +
                    "r.iri ra " +
                "FROM " +
                    tableName + " l, " +
                    lineFeatureTableName + " r " +
                "WHERE " +
                    "ST_Length( " +
                        "ST_Intersection( " +
                            "ST_Buffer(l.the_geom::geography, ?, 'endcap=flat'), " +  // #1
                            "r.the_geom)) > ? " +  // #2
                "AND " +
                    "NOT ST_Equals(l.the_geom, r.the_geom) " +
                "AND " +
                    "l.iri=? ";  // #3

        try {
            PreparedStatement statement = conn.prepareStatement(queryStr);
            statement.setDouble(1, runsAlongToleranceInMeters);
            statement.setDouble(2, 2.5 * runsAlongToleranceInMeters);
            statement.setString(3, geomIndividual.toStringID());

            ResultSet resSet = statement.executeQuery();

            Set<OWLIndividual> resultFeatureIndividuals = new HashSet<>();
            while (resSet.next()) {
                String resIRIStr = resSet.getString("ra");

                resultFeatureIndividuals.add(
                        geom2feature.get(
                                df.getOWLNamedIndividual(IRI.create(resIRIStr))));
            }

            return resultFeatureIndividuals.stream();

        } catch (SQLException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    // --------- private/protected methods -------------------------------------
    private boolean containsSpatialExpressions(OWLClassExpression ce) {
        if (ce instanceof OWLClass) {
            return false;

        } else if (ce instanceof OWLObjectIntersectionOf) {
            return ((OWLObjectIntersectionOf) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectSomeValuesFrom) {
            OWLObjectPropertyExpression prop = ((OWLObjectSomeValuesFrom) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectSomeValuesFrom) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectMinCardinality) {
            OWLObjectPropertyExpression prop = ((OWLObjectMinCardinality) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectMinCardinality) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectUnionOfImpl) {
            return ((OWLObjectUnionOf) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectUnionOfImplExt) {
            return ((OWLObjectUnionOfImplExt) ce).getOperands().stream()
                    .anyMatch(this::containsSpatialExpressions);

        } else if (ce instanceof OWLObjectAllValuesFrom) {
            OWLObjectPropertyExpression prop = ((OWLObjectAllValuesFrom) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectAllValuesFrom) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLObjectMaxCardinality) {
            OWLObjectPropertyExpression prop =
                    ((OWLObjectMaxCardinality) ce).getProperty();
            OWLClassExpression filler = ((OWLObjectMaxCardinality) ce).getFiller();

            return ((prop instanceof OWLObjectProperty) && SpatialVocabulary.spatialObjectProperties.contains(prop))
                    || containsSpatialExpressions(filler);

        } else if (ce instanceof OWLDataSomeValuesFrom) {
            /* Possible cases that were observed:
             * http://www.opengis.net/ont/geosparql#isEmpty some {false}
             * http://www.opengis.net/ont/geosparql#isEmpty some {false}
             * http://www.opengis.net/ont/geosparql#isSimple some {false}
             *
             * --> TODO: Could be handled
             */
            return false;

        } else if (ce instanceof OWLDataHasValue) {
            /* Possible cases that were observed:
             * http://www.opengis.net/ont/geosparql#isEmpty value false
             * http://www.opengis.net/ont/geosparql#isEmpty value true
             *
             * --> TODO: Could be handled
             */
            return false;

        } else {
            throw new RuntimeException(
                    "Support for class expression of type " + ce.getClass() +
                            " not implemented, yet");
        }
    }

    protected boolean hasTypeSpatial(OWLClassExpression ce, OWLIndividual individual) {
        // TODO: Think about this again
        return getIndividuals(ce).contains(individual);
    }

    /**
     * Called in the getIndividualsImpl method in case the class expression to
     * get the instances for is an {@link OWLObjectIntersectionOf}. The
     * unraveling is needed to recusively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectIntersectionOf(OWLObjectIntersectionOf intersection) {
        Set<OWLIndividual> individuals = null;

        for (OWLClassExpression ce : intersection.getOperands()) {
            SortedSet<OWLIndividual> opIndividuals = getIndividualsImpl(ce);

            if (individuals == null) {
                individuals = opIndividuals;
            } else {
                individuals = Sets.intersection(individuals, opIndividuals);
            }
        }

        return new TreeSet<>(individuals);
    }

    private void updateCounterMap(
            Map<OWLIndividual, Integer> counterMap, Set<OWLIndividual> individuals) {

        for (OWLIndividual indivInsideFillerIndiv : individuals) {
            if (!counterMap.containsKey(indivInsideFillerIndiv)) {
                counterMap.put(indivInsideFillerIndiv, 1);

            } else {
                int tmpCnt = counterMap.get(indivInsideFillerIndiv);
                tmpCnt++;
                counterMap.put(indivInsideFillerIndiv, tmpCnt);
            }
        }
    }

    protected void updateWithSuperPropertyMembers(
            Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals, OWLObjectProperty prop) {

        for(OWLObjectProperty subProp : baseReasoner.getSuperProperties((OWLObjectProperty) prop)) {
            Map<OWLIndividual, SortedSet<OWLIndividual>> tmpPropIndividuals =
                    baseReasoner.getPropertyMembers(subProp);

            for (OWLIndividual keyIndividual : tmpPropIndividuals.keySet()) {
                Set<OWLIndividual> valIndividuals =
                        tmpPropIndividuals.get(keyIndividual);

                if (propIndividuals.containsKey(keyIndividual)) {
                    for (OWLIndividual valIndividual: valIndividuals) {
                        if (!propIndividuals.get(keyIndividual).contains(valIndividual)) {
                            propIndividuals
                                    .get(keyIndividual)
                                    .add(valIndividual);
                        }
                    }
                }
            }
        }
    }

    private boolean areAllValuesFromFiller(
            SortedSet<OWLIndividual> propertyValues,
            SortedSet<OWLIndividual> fillerIndividuals) {

        // Check if there is any individual i2 with
        // <property>(<individual>, i2) and not <filler>(i2), i.e.
        // i2 not in `propertyValues` .

        if (propertyValues.isEmpty()) {
            // Trivial case where individual has no values assigned via
            // <property> and thus there are no values not being an instance of
            // <filler> --> all values are an instance of <filler>
            return true;
        }

        // Create copy, since remove is called on it, later
        TreeSet<OWLIndividual> propertyValuesCopy = new TreeSet<>(propertyValues);

        propertyValuesCopy.removeAll(fillerIndividuals);
        // Now, `propertyValuesCopy` only contains individuals that are not of
        // filler-type. If `propertyValuesCopy` is empty all property members
        // were of filler-type and <individual> is an instance of the overall
        // OWLObjectAllValuesFrom class expression
        if (propertyValuesCopy.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectSomeValuesFrom}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectSomeValuesFrom(
            OWLObjectSomeValuesFrom concept) {

        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();

        // There are four cases to consider
        // 1) The property expression is atomic and a spatial property
        //    --> query property members through PostGIS and recurse to get
        //        filler instances
        // 2) The property expression is not atomic but contains a spatial
        //    property
        //    --> not implemented, yet
        // 3) The property expression is non-spatial
        //    --> recurse

        // 1) The property expression is atomic and a spatial property
        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {
            /* fillerIndivs might contain all kinds of individuals,
             * INCL. geometries!!! These need to be filtered out before calling
             * the getIndividuals<spatial relation>( ) methods below!
             */
            SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

            // isConnectedWith
            if (prop.equals(SpatialVocabulary.isConnectedWith)) {
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsConnectedWFillerIndiv =
                            getIndividualsConnectedWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsConnectedWFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // overlapsWith
            } else if (prop.equals(SpatialVocabulary.overlapsWith)) {
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsOverlappingWFillerIndiv =
                            getIndividualsOverlappingWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsOverlappingWFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // isPartOf
            } else if (prop.equals(SpatialVocabulary.isPartOf)) {
                // e.g. partOf some Airport
                // fillerIndivs: airport001, airport002, ...
                // Target individuals: all i with partOf(i, airportXXX)
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsPartOfFillerIndiv =
                            getIndividualsPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsPartOfFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // hasPart
            } else if (prop.equals(SpatialVocabulary.hasPart)) {
                // e.g. hasPart some Room
                // fillerIndivs: room001, room002, ...
                // Target individuals: all i with hasPart(i, roomXXX)
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsHavingPartFillerIndiv =
                            getIndividualsHavingPart(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsHavingPartFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // isProperPartOf
            } else if (prop.equals(SpatialVocabulary.isProperPartOf)) {
                // e.g. isProperPartOf some Airport
                // fillerIndivs: airport001, airport002, ...
                // Target individuals: all i with isProperPartOf(i, airportXXX)
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsProperPartOfFillerIndiv =
                            getIndividualsProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsProperPartOfFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // hasProperPart
            } else if (prop.equals(SpatialVocabulary.hasProperPart)) {
                // e.g. hasProperPart some Room
                // fillerIndivs: room001, room002, ...
                // Target individuals: all i with hasProperPart(i, roomXXX)
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsHavingProperPartFillerIndiv =
                            getIndividualsHavingProperPart(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsHavingProperPartFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // partiallyOverlapsWith
            } else if (prop.equals(SpatialVocabulary.partiallyOverlapsWith)) {
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsPartiallyOverlappingWFillerIndiv =
                            getIndividualsPartiallyOverlappingWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsPartiallyOverlappingWFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // isTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isTangentialProperPartOf)) {
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsBeingTangentialProperPartOfFillerIndiv =
                            getIndividualsTangentialProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsBeingTangentialProperPartOfFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // isNonTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
                Set<OWLIndividual> individuals = new HashSet<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsBeingNonTangentialProperPartOfFillerIndiv =
                            getIndividualsNonTangentialProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    individuals.addAll(indivsBeingNonTangentialProperPartOfFillerIndiv);
                }

                return new TreeSet<>(individuals);

            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong

            } else {
                throw new RuntimeException(
                        "spatial object property " + prop + " not supported, yet");
            }
        } else {
            if (prop instanceof OWLObjectInverseOf) {
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

            } else {
                SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

                Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals =
                        baseReasoner.getPropertyMembers(prop.asOWLObjectProperty());

                updateWithSuperPropertyMembers(propIndividuals, prop.asOWLObjectProperty());

                Set<OWLIndividual> resultIndividuals = new HashSet<>();
                assert propIndividuals != null;

                for (Map.Entry e : propIndividuals.entrySet()) {
                    // e: an entry of the shape
                    //    OWLIndividual -> SortedSet<OWLIndividual>

                    OWLIndividual keyIndiv = (OWLIndividual) e.getKey();
                    SortedSet<OWLIndividual> values =
                            (SortedSet<OWLIndividual>) e.getValue();

                    // set intersection with filler individuals
                    values.retainAll(fillerIndivs);

                    // now values only contains those OWL individuals,
                    // that
                    // - are instances of the filler class expressions
                    // - are assigned to another OWL individual through
                    //   the property `prop`

                    if (!values.isEmpty()) {
                        resultIndividuals.add(keyIndiv);
                    }
                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectMinCardinality}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectMinCardinality(OWLObjectMinCardinality concept) {
        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();
        int minCardinality = concept.getCardinality();

        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {

            /* fillerIndivs might contain all kinds of individuals,
             * INCL. geometries!!! These need to be filtered out before calling
             * the getIndividuals<spatial relation>( ) methods below!
             */
            SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

            // isConnectedWith
            if (prop.equals(SpatialVocabulary.isConnectedWith)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsConnectedWFillerIndiv =
                            getIndividualsConnectedWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsConnectedWFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // overlapsWith
            } else if (prop.equals(SpatialVocabulary.overlapsWith)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsOverlappingWFillerIndiv =
                            getIndividualsOverlappingWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsOverlappingWFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isPartOf
            } else if (prop.equals(SpatialVocabulary.isPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsPartOfFillerIndiv =
                            getIndividualsPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // hasPart
            } else if (prop.equals(SpatialVocabulary.hasPart)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsHavingPartFillerIndiv =
                            getIndividualsHavingPart(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsHavingPartFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isProperPartOf
            } else if (prop.equals(SpatialVocabulary.isProperPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsProperPartOfFillerIndiv =
                            getIndividualsProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsProperPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // hasProperPart
            } else if (prop.equals(SpatialVocabulary.hasProperPart)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsHavingProperPartFillerIndiv =
                            getIndividualsHavingProperPart(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsHavingProperPartFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // partiallyOverlapsWith
            } else if (prop.equals(SpatialVocabulary.partiallyOverlapsWith)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsPartiallyOverlappingWFillerIndiv =
                            getIndividualsPartiallyOverlappingWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsPartiallyOverlappingWFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isTangentialProperPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsBeingTangentialProperPartOfFillerIndiv =
                            getIndividualsTangentialProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsBeingTangentialProperPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isNonTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsBeingNonTangentialProperPartOfFillerIndiv =
                            getIndividualsNonTangentialProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsBeingNonTangentialProperPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() >= minCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong

            } else {
                throw new RuntimeException(
                        "spatial object property " + prop + " not supported, yet");
            }

        } else {
            if (prop instanceof OWLObjectInverseOf) {
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

            } else {
                SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

                Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals =
                        baseReasoner.getPropertyMembers(prop.asOWLObjectProperty());

                updateWithSuperPropertyMembers(propIndividuals, prop.asOWLObjectProperty());

                Set<OWLIndividual> resultIndividuals = new HashSet<>();
                assert propIndividuals != null;
                for (Map.Entry e : propIndividuals.entrySet()) {
                    OWLIndividual keyIndiv = (OWLIndividual) e.getKey();
                    SortedSet<OWLIndividual> values = (SortedSet<OWLIndividual>) e.getValue();

                    // set intersection with filler individuals
                    values.retainAll(fillerIndivs);

                    // now `values` only contains those OWL individuals, that
                    // - are instances of the filler class expressions
                    // - are assigned to another OWL individual through
                    //   the property `prop`

                    if (values.size() >= minCardinality) {
                        resultIndividuals.add(keyIndiv);
                    }
                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectAllValuesFrom}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectAllValuesFrom(
            OWLObjectAllValuesFrom concept) {

        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();

        // There are three cases to consider:
        // 1) `prop` is a known spatial property that requires special treatment
        // 2) `prop` is a non-atomic property --> still TODO
        // 3) `prop` ia a non-spatial atomic property and the filler contains
        //    spatial components

        // 1) `prop` is a known spatial property
        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {

            /* fillerIndivs might contain all kinds of individuals,
             * INCL. geometries!!!
             */
            SortedSet<OWLIndividual> fillerIndividuals = getIndividualsImpl(filler);

            // isConnectedWith
            if (prop.equals(SpatialVocabulary.isConnectedWith)) {
                // All individuals are instances of
                // \forall :isConnectedWith <filler>
                // as long as they aren't connected with something not being of
                // type <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getIsConnectedWithMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // overlapsWith
            } else if (prop.equals(SpatialVocabulary.overlapsWith)) {
                // All individuals are instances of
                // \forall :overlapsWith <filler>
                // as long as they aren't overlapping with something not being
                // of type <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getOverlapsWithMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // isPartOf
            } else if (prop.equals(SpatialVocabulary.isPartOf)) {
                // All individuals are instances of
                // \forall :isPartOf <filler>
                // as long as they aren't part of something not being of
                // type <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getIsPartOfMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // hasPart
            } else if (prop.equals(SpatialVocabulary.hasPart)) {
                // All individuals are instances of
                // \forall :hasPart <filler>
                // as long as they aren't part of something not having <filler>
                // as type
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getHasPartMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // isProperPartOf
            } else if (prop.equals(SpatialVocabulary.isProperPartOf)) {
                // All individuals are instances of
                // \forall :isProperPartOf <filler>
                // as long as they aren't proper part of something not being of
                // type <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getIsProperPartOfMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // hasProperPart
            } else if (prop.equals(SpatialVocabulary.hasProperPart)) {
                // All individuals are instances of
                // \forall :hasProperPart <filler>
                // as long as they aren't proper part of something not having
                // <filler> as type
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getHasProperPartMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // partiallyOverlapsWith
            } else if (prop.equals(SpatialVocabulary.partiallyOverlapsWith)) {
                // All individuals are instances of
                // \forall :partiallyOverlapsWith <filler>
                // as long as they aren't partially overlapping with something
                // not being of type <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getPartiallyOverlapsWithMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // isTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isTangentialProperPartOf)) {
                // All individuals are instances of
                // \forall :tangentialProperPartOf <filler>
                // as long as they aren't a tangential proper part of something
                // not being of type <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getIsTangentialProperPartOfMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // isNonTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
                // All individuals are instances of
                // \forall :nonTangentialProperPartOf <filler>
                // as long as they aren't a non-tangential proper part of
                // something not being of type <filler>
                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> entry :
                        getIsNonTangentialProperPartOfMembers().entrySet()) {

                    if (areAllValuesFromFiller(entry.getValue(), fillerIndividuals)) {
                        OWLIndividual resultIndividual = entry.getKey();
                        resultIndividuals.add(resultIndividual);
                    }
                }

                return new TreeSet<>(resultIndividuals);

            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong

            } else {
                throw new RuntimeException("No implementation for " + prop + ", yet");
            }

        } else {
            // 2) `prop` is a non-atomic property
            if (prop instanceof OWLObjectInverseOf) {
                // TODO: Implement
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

                // 3) `prop` ia a non-spatial atomic property
            } else {
                // Get filler individuals and property members and check whether
                // all the property member values (i.e. objects viewed from a
                // triple perspective) are contained in the filler individuals
                // set

                Set<OWLIndividual> fillerIndividuals = getIndividualsImpl(filler);
                Map<OWLIndividual, SortedSet<OWLIndividual>> propertyMembers =
                        baseReasoner.getPropertyMembers(prop.asOWLObjectProperty());

                updateWithSuperPropertyMembers(propertyMembers, prop.asOWLObjectProperty());

                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                assert propertyMembers != null;
                for (Map.Entry<OWLIndividual, SortedSet<OWLIndividual>> e : propertyMembers.entrySet()) {
                    // s --> subject in triple view; o --> object in triple view
                    OWLIndividual s = e.getKey();
                    Set<OWLIndividual> os = new HashSet<>(e.getValue());

                    if (os.isEmpty()) {
                        // Trivial case where `s` has no values assigned via
                        // `prop` and thus there are no values not being an
                        // instance of `filler` --> all values are an instance
                        // of `filler`
                        resultIndividuals.add(s);
                        continue;
                    }

                    os.removeAll(fillerIndividuals);
                    // Now, `os` only contains individuals that are not of
                    // filler-type. If `os` is empty all property members were
                    // of filler-type and `s` is an instance of the overall
                    // OWLObjectAllValuesFrom class expression
                    if (os.isEmpty()) {
                        resultIndividuals.add(s);
                    }
                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectMaxCardinality}. The
     * unraveling is needed to recursively call getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectMaxCardinality(
            OWLObjectMaxCardinality concept) {

        OWLObjectPropertyExpression prop = concept.getProperty();
        OWLClassExpression filler = concept.getFiller();
        int maxCardinality = concept.getCardinality();

        // There are three cases to consider:
        // 1) `prop` is a known spatial property that requires special treatment
        // 2) `prop` is a non-atomic property --> still TODO
        // 3) `prop` ia a non-spatial atomic property and the filler contains
        //    spatial components

        // 1) `prop` is a known spatial property
        if ((prop instanceof OWLObjectProperty)
                && SpatialVocabulary.spatialObjectProperties.contains(prop)) {

            /* fillerIndivs might contain all kinds of individuals,
             * INCL. geometries!!! These need to be filtered out before calling
             * the getIndividuals<spatial relation>( ) methods below!
             */
            SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

            // isConnectedWith
            if (prop.equals(SpatialVocabulary.isConnectedWith)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsConnectedWFillerIndiv =
                            getIndividualsConnectedWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsConnectedWFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // overlapsWith
            } else if (prop.equals(SpatialVocabulary.overlapsWith)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsOverlappingWFillerIndiv =
                            getIndividualsOverlappingWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsOverlappingWFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isPartOf
            } else if (prop.equals(SpatialVocabulary.isPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsPartOfFillerIndiv =
                            getIndividualsPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // hasPart
            } else if (prop.equals(SpatialVocabulary.hasPart)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsHavingPartFillerIndiv =
                            getIndividualsHavingPart(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsHavingPartFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isProperPartOf
            } else if (prop.equals(SpatialVocabulary.isProperPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsProperPartOfFillerIndiv =
                            getIndividualsProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsProperPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // hasProperPart
            } else if (prop.equals(SpatialVocabulary.hasProperPart)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsHavingProperPartFillerIndiv =
                            getIndividualsHavingProperPart(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsHavingProperPartFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // partiallyOverlapsWith
            } else if (prop.equals(SpatialVocabulary.partiallyOverlapsWith)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsPartiallyOverlappingWFillerIndiv =
                            getIndividualsPartiallyOverlappingWith(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsPartiallyOverlappingWFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isTangentialProperPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsBeingTangentialProperPartOfFillerIndiv =
                            getIndividualsTangentialProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsBeingTangentialProperPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // isNonTangentialProperPartOf
            } else if (prop.equals(SpatialVocabulary.isNonTangentialProperPartOf)) {
                Map<OWLIndividual, Integer> individualsWCounts = new HashMap<>();

                for (OWLIndividual fillerIndiv : fillerIndivs) {
                    if (!baseReasoner.hasType(SpatialVocabulary.SpatialFeature, fillerIndiv))
                        continue;

                    Set<OWLIndividual> indivsBeingNonTangentialProperPartOfFillerIndiv =
                            getIndividualsNonTangentialProperPartOf(fillerIndiv)
                                    .collect(Collectors.toSet());

                    updateCounterMap(individualsWCounts, indivsBeingNonTangentialProperPartOfFillerIndiv);
                }

                return individualsWCounts.entrySet()
                        .stream()
                        .filter((Map.Entry<OWLIndividual, Integer> e) -> e.getValue() <= maxCardinality)
                        .map(Map.Entry::getKey)
                        .collect(Collectors.toCollection(TreeSet::new));

            // TODO: isSpatiallyIdenticalWith
            // TODO: hasTangentialProperPart
            // TODO: hasNonTangentialProperPart
            // TODO: isExternallyConnectedWith
            // TODO: isDisconnectedFrom
            // TODO: isNear
            // TODO: startsNear
            // TODO: endsNear
            // TODO: crosses
            // TODO: runsAlong
            } else {
                throw new RuntimeException(
                        "spatial object property " + prop + " not supported, yet");
            }

        } else {
            // 2) `prop` is a non-atomic property
            if (prop instanceof OWLObjectIntersectionOf) {
                throw new RuntimeException(
                        "Handling of object property expressions not implemented, yet");

                // 3) `prop` ia a non-spatial atomic property and the filler contains
                //    spatial components
            } else {
                SortedSet<OWLIndividual> fillerIndivs = getIndividualsImpl(filler);

                Map<OWLIndividual, SortedSet<OWLIndividual>> propIndividuals =
                        baseReasoner.getPropertyMembers(prop.asOWLObjectProperty());

                updateWithSuperPropertyMembers(propIndividuals, prop.asOWLObjectProperty());

                Set<OWLIndividual> resultIndividuals = new HashSet<>();

                assert propIndividuals != null;
                for (Map.Entry e : propIndividuals.entrySet()) {
                    OWLIndividual keyIndiv = (OWLIndividual) e.getKey();
                    SortedSet<OWLIndividual> values = (SortedSet<OWLIndividual>) e.getValue();

                    // set intersection with filler individuals
                    values.retainAll(fillerIndivs);

                    // now `values` only contains those OWL individuals, that
                    // - are instances of the filler class expression
                    // - are assigned to another OWL individual through the
                    //   property `prop`

                    if (values.size() <= maxCardinality) {
                        resultIndividuals.add(keyIndiv);
                    }

                }

                return new TreeSet<>(resultIndividuals);
            }
        }
    }

    /**
     * Called from the getIndividualsImpl method in case the class expression
     * to get the instances for is {@link OWLObjectUnionOfImplExt}. The
     * unraveling is needed to recursively cal getIndividualsImpl on all parts
     * such that we can handle inner spatial expressions.
     */
    protected SortedSet<OWLIndividual> getIndividualsOWLObjectUnionOfImplExt(
            OWLObjectUnionOfImplExt concept) {

        Set<OWLClassExpression> unionParts = concept.getOperands();

        Set<OWLIndividual> resultIndividuals = new HashSet<>();

        for (OWLClassExpression unionPart : unionParts) {
            resultIndividuals.addAll(getIndividualsImpl(unionPart));
        }

        return new TreeSet<>(resultIndividuals);
    }

    // --------- getter/setter ------------------------------------------------
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setDBName(String dbName) {
        this.dbName = dbName;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public void setDBUser(String dbUser) {
        this.dbUser = dbUser;
    }

    public void setDBUserPW(String dbUserPW) {
        this.dbUserPW = dbUserPW;
    }

    public void setBaseReasoner(AbstractReasonerComponent baseReasoner) {
        this.baseReasoner = baseReasoner;
    }

    public void setNearRadiusInMeters(double nearRadiusInMeters) {
        this.nearRadiusInMeters = nearRadiusInMeters;
    }

    public void setRunsAlongToleranceInMeters(double runsAlongToleranceInMeters) {
        this.runsAlongToleranceInMeters = runsAlongToleranceInMeters;
    }

    public void addGeometryPropertyPath(List<OWLProperty> propertyPath) {
        assert propertyPath.size() > 0;
        int i = 0;
        int len = propertyPath.size();

        while (i < (len-1)) {
            assert propertyPath.get(i) instanceof OWLObjectProperty;
            i++;
        }

        assert propertyPath.get(i) instanceof OWLDataProperty;

        geometryPropertyPaths.add(propertyPath);
    }

    protected void clearGeometryPropertyPaths() {
        geometryPropertyPaths = new HashSet<>();
    }
}
