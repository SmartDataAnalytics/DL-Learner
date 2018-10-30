package org.dllearner.reasoning.spatial.model;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;

import javax.annotation.Nonnull;
import java.util.Set;

import static org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider.ENTITY_TYPE_INDEX_BASE;

public class SpatialComplementOf extends SpatialIndividual {
    private final OWLIndividual individual;

    public SpatialComplementOf(OWLIndividual individual) {
        this.individual = individual;
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
        return "not (" + individual.toStringID() + ")";
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
        return ENTITY_TYPE_INDEX_BASE + 335;
    }

    @Override
    protected int compareObjectOfSameType(@Nonnull OWLObject object) {
        if (object instanceof SpatialComplementOf) {
            return individual.compareTo(((SpatialComplementOf) object).individual);
        } else {
            return -1;
        }
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
    @Nonnull
    public Set<OWLEntity> getSignature() {
        return Sets.newHashSet((OWLEntity) individual);
    }

    @Override
    public void addSignatureEntitiesToSet(@Nonnull Set<OWLEntity> entities) {
        if (individual.isNamed()) {
            entities.add((OWLEntity) individual);
        }
    }

    @Override
    public void addAnonymousIndividualsToSet(@Nonnull Set<OWLAnonymousIndividual> anons) {
        if (individual.isAnonymous()) {
            anons.add((OWLAnonymousIndividual) individual);
        }
    }
}
