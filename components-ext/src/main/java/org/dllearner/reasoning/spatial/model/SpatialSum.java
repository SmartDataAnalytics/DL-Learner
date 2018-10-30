package org.dllearner.reasoning.spatial.model;

import com.google.common.collect.Sets;
import com.google.common.collect.Streams;
import joptsimple.internal.Strings;
import org.jetbrains.annotations.NotNull;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider.ENTITY_TYPE_INDEX_BASE;

public class SpatialSum extends SpatialIndividual {
    private Set<OWLIndividual> individuals;

    public SpatialSum(Set<OWLIndividual> individuals) {
        super();
        this.individuals = individuals;
    }

    public SpatialSum(OWLIndividual individual1, OWLIndividual individual2) {
        this(Sets.newHashSet(individual1, individual2));
    }

    public SpatialSum(SpatialSum sum, OWLIndividual individual) {
        this(Sets.union(sum.individuals, Sets.newHashSet(individual)));
    }

    public SpatialSum(OWLIndividual individual, SpatialSum sum) {
        this(sum, individual);
    }

    @Override
    public boolean isNamed() {
        return false;
    }

    @Override
    public boolean isAnonymous() {
        return true;
    }

    @Nonnull
    @Override
    public OWLNamedIndividual asOWLNamedIndividual() {
        throw new OWLRuntimeException(
                "Not a named individual! This method should only be called " +
                        "on named individuals");
    }

    @Nonnull
    @Override
    public OWLAnonymousIndividual asOWLAnonymousIndividual() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Nonnull
    @Override
    public String toStringID() {
        return "sum(" + Strings.join(
                individuals.stream().map(OWLIndividual::toStringID)
                        .collect(Collectors.toList()), ", ") + ")";
    }

    @Override
    public void accept(@Nonnull OWLIndividualVisitor visitor) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLIndividualVisitorEx<O> visitor) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Nonnull
    @Override
    public Set<OWLClassExpression> getNestedClassExpressions() {
        return new HashSet<>();
    }

    @Override
    protected int index() {
        return ENTITY_TYPE_INDEX_BASE + 333;
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor owlObjectVisitor) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLObjectVisitorEx<O> owlObjectVisitorEx) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isTopEntity() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public boolean isBottomEntity() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Nonnull
    @Override
    public String toString() {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public int compareTo(@NotNull OWLObject owlObject) {
        if (owlObject instanceof SpatialSum) {
            Stream<Integer> thisIndivHashes =
                    individuals.stream().map(Object::hashCode).sorted();

            Stream<Integer> otherIndivHashes =
                    ((SpatialSum) owlObject).getParts().stream()
                            .map(OWLIndividual::hashCode).sorted();


            Stream<Integer> diffs = Streams.zip(
                    thisIndivHashes, otherIndivHashes,
                    (thisIndivHash, otherIndivHash) -> thisIndivHash - otherIndivHash);

            Optional<Integer> firstNonZero = diffs.filter((Integer i) -> i > 0).findFirst();

            if (firstNonZero.isPresent()) {
                return firstNonZero.get();

            } else {
                if (thisIndivHashes.count() > otherIndivHashes.count()) {
                    // return the hash of the element with the first index that
                    // is not present in the shorter list
                    return thisIndivHashes.collect(Collectors.toList())
                            .get((int) otherIndivHashes.count());
                } else if (otherIndivHashes.count() > thisIndivHashes.count()) {
                    // return the *negated* hash of the element with the first
                    // index that is not present in the shorter list
                    return - otherIndivHashes.collect(Collectors.toList())
                            .get((int) thisIndivHashes.count());
                } else {
                    // both objects are of same type and have part individuals
                    // with the same hashes
                    return 0;
                }
            }
        } else {
            return super.compareTo(owlObject);
        }
    }

    @Override
    protected int compareObjectOfSameType(@Nonnull OWLObject owlObject) {
        return compareTo(owlObject);
    }

    @Nonnull
    @Override
    public Set<OWLAnnotationProperty> getAnnotationPropertiesInSignature() {
        return new HashSet<>();
    }

    @Nonnull
    @Override
    public Set<OWLAnonymousIndividual> getAnonymousIndividuals() {
        return individuals.stream()
                .filter(OWLIndividual::isAnonymous)
                .map(OWLIndividual::asOWLAnonymousIndividual)
                .collect(Collectors.toSet());
    }

    @Override
    public boolean containsEntityInSignature(@Nonnull OWLEntity owlEntity) {
        if (owlEntity instanceof SpatialSum) {
            return individuals.contains(owlEntity);
        } else {
            return false;
        }
    }

    @Nonnull
    @Override
    public Set<OWLDataProperty> getDataPropertiesInSignature() {
        return new HashSet<>();
    }

    @Nonnull
    @Override
    public Set<OWLDatatype> getDatatypesInSignature() {
        return new HashSet<>();
    }

    @Nonnull
    @Override
    public Set<OWLNamedIndividual> getIndividualsInSignature() {
        return this.individuals.stream()
                .filter(OWLIndividual::isNamed)
                .map(OWLIndividual::asOWLNamedIndividual)
                .collect(Collectors.toSet());
    }

    public Set<OWLIndividual> getParts() {
        return individuals;
    }

    @Nonnull
    @Override
    public Set<OWLObjectProperty> getObjectPropertiesInSignature() {
        return new HashSet<>();
    }

    @Nonnull
    @Override
    public Set<OWLEntity> getSignature() {
        return individuals.stream()
                .filter(OWLIndividual::isNamed)
                .map(OWLIndividual::asOWLNamedIndividual)
                .map((OWLIndividual i) -> (OWLEntity) i)
                .collect(Collectors.toSet());
    }

    @Override
    public void addSignatureEntitiesToSet(@Nonnull Set<OWLEntity> set) {
        set.addAll(getSignature());
    }

    @Override
    public void addAnonymousIndividualsToSet(@Nonnull Set<OWLAnonymousIndividual> set) {
        set.addAll(getAnonymousIndividuals());
    }
}
