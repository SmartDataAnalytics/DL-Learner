package org.dllearner.reasoning.spatial.model;

import com.google.common.collect.Sets;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.OWLObjectTypeIndexProvider;
import uk.ac.manchester.cs.owl.owlapi.OWLNamedIndividualImpl;

import javax.annotation.Nonnull;
import java.util.Set;

public class UniversalSpatialRegion extends SpatialIndividual {
    private final IRI iri =
            IRI.create("http://dl-learner.org/ont/spatial/universal_spatial_region");

    public UniversalSpatialRegion() {
        super();
    }

    @Override
    public boolean isNamed() {
        return true;
    }

    @Override
    public boolean isAnonymous() {
        return false;
    }

    @Nonnull
    @Override
    public OWLNamedIndividual asOWLNamedIndividual() {
        return new OWLNamedIndividualImpl(iri);
    }

    @Nonnull
    @Override
    public OWLAnonymousIndividual asOWLAnonymousIndividual() {
        throw new OWLRuntimeException(
                "Not an anonymous individual! This method should only be " +
                        "called on anonymous individuals");
    }

    @Nonnull
    @Override
    public String toStringID() {
        return iri.toString();
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
        return OWLObjectTypeIndexProvider.ENTITY_TYPE_INDEX_BASE + 334;
    }

    @Override
    protected int compareObjectOfSameType(@Nonnull OWLObject object) {
        if (object instanceof UniversalSpatialRegion) {
            return 1;
        } else {
            return 0;
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
        return Sets.newHashSet(new OWLNamedIndividualImpl(iri));
    }

    @Override
    public void addSignatureEntitiesToSet(@Nonnull Set<OWLEntity> entities) {
        entities.addAll(getSignature());
    }

    @Override
    public void addAnonymousIndividualsToSet(@Nonnull Set<OWLAnonymousIndividual> anons) {
        // nothing to do here
    }
}
