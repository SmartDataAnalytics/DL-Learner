package org.dllearner.utilities.graph;

import java.util.Objects;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLIndividual;

/**
 * Wraps an OWL individual with its types.
 */
public class TypedOWLIndividual {
    private final OWLIndividual individual;
    private final Set<OWLClass> types;

    public TypedOWLIndividual(OWLIndividual individual, Set<OWLClass> types) {
        this.individual = individual;
        this.types = types;
    }

    public TypedOWLIndividual(OWLIndividual individual) {
        this(individual, null);
    }

    public OWLIndividual getIndividual() {
        return individual;
    }

    public Set<OWLClass> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TypedOWLIndividual that = (TypedOWLIndividual) o;
        return individual.equals(that.individual);
    }

    @Override
    public int hashCode() {
        return Objects.hash(individual);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", individual, types);
    }
}