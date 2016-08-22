package org.dllearner.algorithms.parcel;

import com.clarkparsia.owlapiv3.XSD;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.split.ParCELDoubleSplitterAbstract;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.ClassHierarchy;
import org.dllearner.refinementoperators.LengthLimitedRefinementOperator;
import org.dllearner.refinementoperators.RhoDRDown;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLLiteral;
import uk.ac.manchester.cs.owl.owlapi.OWLLiteralImplDouble;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Refinement operator factory RhoDRDown
 *
 * @author An C. Tran
 */
public class ParCELRefinementOperatorFactory extends BasePooledObjectFactory<LengthLimitedRefinementOperator> {

    private AbstractReasonerComponent reasoner;
    private ClassHierarchy classHierarchy;
    private OWLClassExpression startclass;
    private Map<OWLDataProperty, List<OWLLiteral>> splits;


    private boolean useNegation = true;
    private boolean useDisjunction = true;


    Logger logger = Logger.getLogger(this.getClass());

    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy, OWLClassExpression startclass) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startclass = startclass;
        this.splits = null;
    }


    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startclass, ParCELDoubleSplitterAbstract splitter) {
        this(reasoner, classHierarchy, startclass, splitter.computeSplits());
    }


    public ParCELRefinementOperatorFactory(AbstractReasonerComponent reasoner, ClassHierarchy classHierarchy,
                                           OWLClassExpression startclass, Map<OWLDataProperty, List<Double>> splits) {
        this.reasoner = reasoner;
        this.classHierarchy = classHierarchy;
        this.startclass = startclass;

        this.splits = splits.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().stream().map(v -> new OWLLiteralImplDouble(v, XSD.DOUBLE)).collect(Collectors.toList())));
    }

    @Override
    public LengthLimitedRefinementOperator create() throws Exception {

        //clone a new class heirarchy to avoid the competition between refinement operators
        ClassHierarchy clonedClassHierarchy = this.classHierarchy.clone();

        if (logger.isDebugEnabled())
            logger.info("A new refinement operator had been made");

        //create a new RhoDRDown and return
        RhoDRDown refinementOperator = new RhoDRDown();
        refinementOperator.setReasoner(reasoner);
        refinementOperator.setClassHierarchy(classHierarchy);

        refinementOperator.setUseNegation(this.useNegation);

        if (this.splits != null)
            refinementOperator.setSplits(splits);

        //init the refinement operator;
        refinementOperator.init();

        return refinementOperator;
    }

    @Override
    public PooledObject<LengthLimitedRefinementOperator> wrap(LengthLimitedRefinementOperator op) {
        return new DefaultPooledObject<>(op);
    }

    public boolean isUseNegation() {
        return useNegation;
    }


    public void setUseNegation(boolean useNegation) {
        this.useNegation = useNegation;
    }


    public boolean isUseDisjunction() {
        return useDisjunction;
    }


    public void setUseDisjunction(boolean useDisjunction) {
        this.useDisjunction = useDisjunction;
    }
}
