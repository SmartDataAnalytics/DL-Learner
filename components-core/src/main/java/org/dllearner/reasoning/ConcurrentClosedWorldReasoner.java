package org.dllearner.reasoning;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentAnn;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.config.ConfigOption;
import org.semanticweb.owlapi.model.*;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.function.Function;

@ComponentAnn(name = "concurrent closed world reasoner", shortName = "ccwr", version = 0.1)
public class ConcurrentClosedWorldReasoner extends ClosedWorldReasoner {

    @ConfigOption(description = "maximum number of concurrently working base reasoners", defaultValue = "4")
    private int maxNrOfBaseReasoners = 4;

    private ReasonerPool reasonerPool;

    @Override
    public void init() throws ComponentInitException {
        super.init();

        createReasonerPool();
    }

    private void createReasonerPool() {
        reasonerPool = new ReasonerPool(baseReasoner, maxNrOfBaseReasoners);
    }

    private AbstractReasonerComponent borrowReasoner() {
        try {
            return reasonerPool.borrowObject();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private void returnReasoner(AbstractReasonerComponent reasoner) {
        try {
            reasonerPool.returnObject(reasoner);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private <T> T queryReasoner(Function<AbstractReasonerComponent, T> query) {
        AbstractReasonerComponent reasoner = borrowReasoner();
        T result = query.apply(reasoner);
        returnReasoner(reasoner);

        return result;
    }

    public int getMaxNrOfBaseReasoners() {
        return maxNrOfBaseReasoners;
    }

    public void setMaxNrOfBaseReasoners(int maxNrOfBaseReasoners) {
        this.maxNrOfBaseReasoners = maxNrOfBaseReasoners;
    }

    @Override
    public Set<OWLClass> getClasses() {
        return queryReasoner(r -> r.getClasses());
    }

    @Override
    public Set<OWLDataProperty> getDatatypePropertiesImpl() {
        return queryReasoner(r -> r.getDatatypeProperties());
    }

    @Override
    public Set<OWLDataProperty> getBooleanDatatypePropertiesImpl() {
        return queryReasoner(r -> r.getBooleanDatatypeProperties());
    }

    @Override
    public Set<OWLDataProperty> getDoubleDatatypePropertiesImpl() {
        return queryReasoner(r -> r.getDoubleDatatypeProperties());
    }

    @Override
    public Set<OWLDataProperty> getIntDatatypePropertiesImpl() {
        return queryReasoner(r -> r.getIntDatatypeProperties());
    }

    @Override
    public Set<OWLDataProperty> getStringDatatypePropertiesImpl() {
        return queryReasoner(r -> r.getStringDatatypeProperties());
    }

    @Override
    protected SortedSet<OWLClassExpression> getSuperClassesImpl(OWLClassExpression concept) {
        return queryReasoner(r -> r.getSuperClasses(concept));
    }

    @Override
    protected SortedSet<OWLClassExpression> getSubClassesImpl(OWLClassExpression concept) {
        return queryReasoner(r -> r.getSubClasses(concept));
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSuperPropertiesImpl(OWLObjectProperty role) {
        return queryReasoner(r -> r.getSuperProperties(role));
    }

    @Override
    protected SortedSet<OWLObjectProperty> getSubPropertiesImpl(OWLObjectProperty role) {
        return queryReasoner(r -> r.getSubProperties(role));
    }

    @Override
    protected SortedSet<OWLDataProperty> getSuperPropertiesImpl(OWLDataProperty role) {
        return queryReasoner(r -> r.getSuperProperties(role));
    }

    @Override
    protected SortedSet<OWLDataProperty> getSubPropertiesImpl(OWLDataProperty role) {
        return queryReasoner(r -> r.getSubProperties(role));
    }

    @Override
    public boolean isSuperClassOfImpl(OWLClassExpression superConcept, OWLClassExpression subConcept) {
        return queryReasoner(r -> r.isSuperClassOf(superConcept, subConcept));
    }

    @Override
    public boolean isDisjointImpl(OWLClass clsA, OWLClass clsB) {
        if (getDisjointnessSemantics() == DisjointnessSemantics.Explicit) {
            return queryReasoner(r -> r.isDisjoint(clsA, clsB));
        }

        return super.isDisjointImpl(clsA, clsB);
    }

    @Override
    public void setBaseURI(String baseURI) {}

    @Override
    public void setPrefixes(Map<String, String> prefixes) {}

    @Override
    public OWLClassExpression getDomainImpl(OWLObjectProperty objectProperty) {
        return queryReasoner(r -> r.getDomain(objectProperty));
    }

    @Override
    public OWLClassExpression getDomainImpl(OWLDataProperty datatypeProperty) {
        return queryReasoner(r -> r.getDomain(datatypeProperty));
    }

    @Override
    public OWLClassExpression getRangeImpl(OWLObjectProperty objectProperty) {
        return queryReasoner(r -> r.getRange(objectProperty));
    }

    @Override
    public OWLDataRange getRangeImpl(OWLDataProperty datatypeProperty) {
        return queryReasoner(r -> r.getRange(datatypeProperty));
    }

    @Override
    protected Map<OWLDataProperty, Set<OWLLiteral>> getDataPropertyRelationshipsImpl(OWLIndividual individual) {
        return queryReasoner(r -> r.getDataPropertyRelationships(individual));
    }

    @Override
    public Set<OWLIndividual> getRelatedIndividualsImpl(OWLIndividual individual, OWLObjectProperty objectProperty) {
        return queryReasoner(r -> r.getRelatedIndividuals(individual, objectProperty));
    }

    @Override
    protected Map<OWLObjectProperty, Set<OWLIndividual>> getObjectPropertyRelationshipsImpl(OWLIndividual individual) {
        return queryReasoner(r -> r.getObjectPropertyRelationships(individual));
    }

    @Override
    public Set<OWLLiteral> getRelatedValuesImpl(OWLIndividual individual, OWLDataProperty datatypeProperty) {
        return queryReasoner(r -> r.getRelatedValues(individual, datatypeProperty));
    }

    @Override
    public boolean isSatisfiableImpl() {
        return queryReasoner(r -> r.isSatisfiable());
    }

    @Override
    public Set<OWLLiteral> getLabelImpl(OWLEntity entity) {
        return queryReasoner(r -> r.getLabel(entity));
    }

    @Override
    public void releaseKB() {}

    @Override
    protected Set<OWLClass> getTypesImpl(OWLIndividual individual) {
        return queryReasoner(r -> r.getTypes(individual));
    }

    @Override
    public boolean remainsSatisfiableImpl(OWLAxiom axiom) {
        return queryReasoner(r -> r.remainsSatisfiable(axiom));
    }

    @Override
    protected Set<OWLClassExpression> getAssertedDefinitionsImpl(OWLClass nc) {
        return queryReasoner(r -> r.getAssertedDefinitions(nc));
    }

    @Override
    protected Set<OWLClass> getInconsistentClassesImpl() {
        return queryReasoner(r -> r.getInconsistentClasses());
    }

    @Override
    public OWLDatatype getDatatype(OWLDataProperty dp) {
        return queryReasoner(r -> r.getDatatype(dp));
    }

    @Override
    public void setSynchronized() {}
}
