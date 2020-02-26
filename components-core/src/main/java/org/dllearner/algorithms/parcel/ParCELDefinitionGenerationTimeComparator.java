package org.dllearner.algorithms.parcel;

/**
 * Compare two node based on their generation time.
 * This will be used in the Generation Time Greedy Compactness strategy
 * GOLR
 *
 * @author An C. Tran
 */

import java.util.Comparator;

import org.dllearner.utilities.owl.OWLClassExpressionLengthCalculator;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;

public class ParCELDefinitionGenerationTimeComparator implements
        Comparator<ParCELExtraNode> {

    @Override
    public int compare(ParCELExtraNode node1, ParCELExtraNode node2) {
        double genTime1 = node1.getGenerationTime();
        double genTime2 = node2.getGenerationTime();

        if (genTime1 < genTime2)
            return -1;
        else if (genTime1 > genTime2)
            return 1;
        else {
            if (OWLClassExpressionUtils.getLength(node1.getDescription()) <
                    OWLClassExpressionUtils.getLength(node2.getDescription()))
                return -1;
            else if (OWLClassExpressionUtils.getLength(node1.getDescription()) >
                    OWLClassExpressionUtils.getLength(node2.getDescription()))
                return 1;
            else
                return node1.getDescription().compareTo(node2.getDescription());
        }
    }

}
