package org.dllearner.reasoning;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.ComponentInitException;
import org.dllearner.core.ReasoningMethodUnsupportedException;
import org.jetbrains.annotations.Nullable;
import org.semanticweb.owlapi.model.*;

/**
 * @author Lorenz Buehmann
 */
public class CachingReasoner extends AbstractReasonerComponent{

    private final AbstractReasonerComponent reasoner;

    private LoadingCache<OWLClassExpression, SortedSet<OWLIndividual>> instanceCheckCache;

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


    @Override
    public ReasonerType getReasonerType() {
        return reasoner.getReasonerType();
    }

    @Override
    public void releaseKB() {
        instanceCheckCache.cleanUp();
        reasoner.releaseKB();
    }

    protected Set<OWLClass> getTypesImpl(OWLIndividual individual) {
        return reasoner.getTypes(individual);
    }

    @Nullable
    protected SortedSet<OWLIndividual> getIndividualsImpl(OWLClassExpression ce) {
        try {
            return instanceCheckCache.get(ce);
        } catch (ExecutionException e) {
            throw new RuntimeException(String.format("Cache lookup for %s failed.", ce.toString()));
        }
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


}
