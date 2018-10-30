package org.dllearner.reasoning.spatial.model;

import com.google.common.collect.Sets;
import joptsimple.internal.Strings;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider.ENTITY_TYPE_INDEX_BASE;

public class SpatialIntersection extends SpatialIndividual {
    private final Set<OWLIndividual> individuals;

    public SpatialIntersection(Set<OWLIndividual> individuals) {
        super();

        this.individuals = individuals;
    }

    public SpatialIntersection(OWLIndividual individual1, OWLIndividual individual2) {
        this(Sets.newHashSet(individual1, individual2));
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
        List<String> individualStrings = individuals.stream()
                .map(OWLIndividual::toString).collect(Collectors.toList());
        return "(" + Strings.join(individualStrings, " and ") + ")";
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

    @Override
    protected int index() {
        return ENTITY_TYPE_INDEX_BASE + 336;
    }

    @Override
    protected int compareObjectOfSameType(@Nonnull OWLObject object) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public void accept(@Nonnull OWLObjectVisitor visitor) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Nonnull
    @Override
    public <O> O accept(@Nonnull OWLObjectVisitorEx<O> visitor) {
        throw new RuntimeException("Not implemented, yet");
    }

    @Override
    public void addSignatureEntitiesToSet(@Nonnull Set<OWLEntity> entities) {
        entities.addAll(
                individuals.stream()
                        .filter((OWLIndividual::isNamed))
                        .map((OWLIndividual i) -> (OWLEntity) i)
                        .collect(Collectors.toSet()));
    }

    @Override
    public void addAnonymousIndividualsToSet(@Nonnull Set<OWLAnonymousIndividual> anons) {
        anons.addAll(
                individuals.stream()
                        .filter(OWLIndividual::isAnonymous)
                        .map(OWLIndividual::asOWLAnonymousIndividual)
                        .collect(Collectors.toSet()));
    }
}
