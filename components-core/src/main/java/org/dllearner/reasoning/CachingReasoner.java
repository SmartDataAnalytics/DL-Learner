package org.dllearner.reasoning;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.dllearner.core.annotations.NoConfigOption;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.core.owl.DatatypePropertyHierarchy;
import org.dllearner.core.owl.ObjectPropertyHierarchy;
import org.dllearner.core.owl.fuzzydll.FuzzyIndividual;
import org.jetbrains.annotations.Nullable;
import org.semanticweb.owlapi.model.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * A reasoner wrapper that does caching on instance queries.
 *
 * @author Lorenz Buehmann
 */
public class CachingReasoner extends AbstractReasonerComponent{

    private final AbstractReasonerComponent reasoner;

    private LoadingCache<OWLClassExpression, SortedSet<OWLIndividual>> instanceCheckCache;

    private int cacheSize = 100;

    public CachingReasoner(AbstractReasonerComponent reasoner) {
        this.reasoner = reasoner;
    }

    @Override
    public void init() throws ComponentInitException {
        instanceCheckCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(
                        new CacheLoader<OWLClassExpression, SortedSet<OWLIndividual>>() {
                            public SortedSet<OWLIndividual> load(OWLClassExpression ce) {
                                return reasoner.getIndividuals(ce);
                            }
                        });
    }

    @Nullable
    protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression ce) {
        if(ce instanceof OWLObjectIntersectionOf) {
            try {
                Iterator<OWLClassExpression> iterator = ((OWLObjectIntersectionOf) ce).getOperands().iterator();
                SortedSet<OWLIndividual> individuals = instanceCheckCache.get(ce);
                while (iterator.hasNext() && !individuals.isEmpty()) {
                    individuals.retainAll(getIndividualsImpl(iterator.next()));
                }
                return individuals;
            } catch (ExecutionException e) {
                throw new RuntimeException(String.format("Cache lookup for %s failed.", ce.toString()));
            }
        } else {
            return reasoner.getIndividuals(ce);
        }

    }

    /**
     * Set the size of the cache.
     *
     * @param cacheSize size of cache
     */
    public void setCacheSize(int cacheSize) {
        this.cacheSize = cacheSize;
    }

    @Override
    public void releaseKB() {
        instanceCheckCache.cleanUp();
        reasoner.releaseKB();
    }

    @Override
    public ReasonerType getReasonerType() {
        return reasoner.getReasonerType();
    }

    protected Set<OWLClass> getTypesImpl(OWLIndividual individual) {
        return reasoner.getTypes(individual);
    }

    @Override
    public OWLDatatype getDatatype(OWLDataProperty dp) {
        return reasoner.getDatatype(dp);
    }

    @Override
    public void setSynchronized() {
        reasoner.setSynchronized();
    }

    @Override
    public Set<OWLClass> getClasses() {
        return reasoner.getClasses();
    }

    @Override
    public SortedSet<OWLIndividual> getIndividuals() {
        return reasoner.getIndividuals();
    }

    @Override
    public String getBaseURI() {
        return reasoner.getBaseURI();
    }

    @Override
    public Map<String, String> getPrefixes() {
        return reasoner.getPrefixes();
    }


    @Override
    public Set<KnowledgeSource> getSources() {
        return reasoner.getSources();
    }

    @Override
    @Autowired
    public void setSources(Set<KnowledgeSource> sources) {
        reasoner.setSources(sources);
    }

    @Override
    @Autowired
    public void setSources(KnowledgeSource... sources) {
        reasoner.setSources(sources);
    }

    @Override
    public void changeSources(Set<KnowledgeSource> sources) {
        reasoner.changeSources(sources);
    }

    @Override
    public void resetStatistics() {
        reasoner.resetStatistics();
    }

    @Override
    @NoConfigOption
    public void setUpdated() {
        reasoner.setUpdated();
    }

    @Override
    public Set<OWLClassExpression> getAssertedDefinitions(OWLClass namedClass) {
        return reasoner.getAssertedDefinitions(namedClass);
    }

    @Override
    public ClassHierarchy prepareSubsumptionHierarchy() throws ReasoningMethodUnsupportedException {
        return reasoner.prepareSubsumptionHierarchy();
    }

    @Override
    public ObjectPropertyHierarchy prepareObjectPropertyHierarchy() throws ReasoningMethodUnsupportedException {
        return reasoner.prepareObjectPropertyHierarchy();
    }

    @Override
    public boolean isSubPropertyOf(OWLProperty subProperty, OWLProperty superProperty) {
        return reasoner.isSubPropertyOf(subProperty, superProperty);
    }

    @Override
    public DatatypePropertyHierarchy prepareDatatypePropertyHierarchy() throws ReasoningMethodUnsupportedException {
        return reasoner.prepareDatatypePropertyHierarchy();
    }

    @Override
    public List<OWLClass> getAtomicConceptsList() {
        return reasoner.getAtomicConceptsList();
    }

    @Override
    public List<OWLClass> getAtomicConceptsList(boolean removeOWLThing) {
        return reasoner.getAtomicConceptsList(removeOWLThing);
    }

    @Override
    public void setSubsumptionHierarchy(ClassHierarchy subsumptionHierarchy) {
        reasoner.setSubsumptionHierarchy(subsumptionHierarchy);
    }

    @Override
    public List<OWLObjectProperty> getAtomicRolesList() {
        return reasoner.getAtomicRolesList();
    }

    @Override
    public long getInstanceCheckReasoningTimeNs() {
        return reasoner.getInstanceCheckReasoningTimeNs();
    }

    @Override
    public long getRetrievalReasoningTimeNs() {
        return reasoner.getRetrievalReasoningTimeNs();
    }

    @Override
    public int getNrOfInstanceChecks() {
        return reasoner.getNrOfInstanceChecks();
    }

    @Override
    public int getNrOfRetrievals() {
        return reasoner.getNrOfRetrievals();
    }

    @Override
    public int getNrOfSubsumptionChecks() {
        return reasoner.getNrOfSubsumptionChecks();
    }

    @Override
    public long getSubsumptionReasoningTimeNs() {
        return reasoner.getSubsumptionReasoningTimeNs();
    }

    @Override
    public int getNrOfSubsumptionHierarchyQueries() {
        return reasoner.getNrOfSubsumptionHierarchyQueries();
    }

    @Override
    public long getOverallReasoningTimeNs() {
        return reasoner.getOverallReasoningTimeNs();
    }

    @Override
    public long getTimePerRetrievalNs() {
        return reasoner.getTimePerRetrievalNs();
    }

    @Override
    public long getTimePerInstanceCheckNs() {
        return reasoner.getTimePerInstanceCheckNs();
    }

    @Override
    public long getTimePerSubsumptionCheckNs() {
        return reasoner.getTimePerSubsumptionCheckNs();
    }

    @Override
    public int getNrOfMultiSubsumptionChecks() {
        return reasoner.getNrOfMultiSubsumptionChecks();
    }

    @Override
    public int getNrOfMultiInstanceChecks() {
        return reasoner.getNrOfMultiInstanceChecks();
    }

    @Override
    public void setPrecomputeClassHierarchy(boolean precomputeClassHierarchy) {
        reasoner.setPrecomputeClassHierarchy(precomputeClassHierarchy);
    }

    @Override
    public void setPrecomputeObjectPropertyHierarchy(boolean precomputeObjectPropertyHierarchy) {
        reasoner.setPrecomputeObjectPropertyHierarchy(precomputeObjectPropertyHierarchy);
    }

    @Override
    public void setPrecomputeDataPropertyHierarchy(boolean precomputeDataPropertyHierarchy) {
        reasoner.setPrecomputeDataPropertyHierarchy(precomputeDataPropertyHierarchy);
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyDomains() {
        return reasoner.getObjectPropertyDomains();
    }

    @Override
    public Map<OWLObjectProperty, OWLClassExpression> getObjectPropertyRanges() {
        return reasoner.getObjectPropertyRanges();
    }

    @Override
    public Map<OWLDataProperty, OWLClassExpression> getDataPropertyDomains() {
        return reasoner.getDataPropertyDomains();
    }

    @Override
    public String toString() {
        return reasoner.toString();
    }

    @Override
    public double hasTypeFuzzyMembership(OWLClassExpression description, FuzzyIndividual individual) {
        return reasoner.hasTypeFuzzyMembership(description, individual);
    }

    @Override
    public boolean isUseInstanceChecks() {
        return reasoner.isUseInstanceChecks();
    }

    @Override
    public void setUseInstanceChecks(boolean useInstanceChecks) {
        reasoner.setUseInstanceChecks(useInstanceChecks);
    }
}
