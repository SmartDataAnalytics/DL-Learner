package org.dllearner.algorithms.versionspace.complexity;

import com.google.common.collect.Lists;
import org.semanticweb.owlapi.model.OWLClassExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * A complexity model that combines a list of complexity models.
 *
 * @author Lorenz Buehmann
 */
public class HybridComplexityModel implements ComplexityModel {

    private List<ComplexityModel> complexityModels = new ArrayList<>();

    public HybridComplexityModel(ComplexityModel... complexityModels) {
        this.complexityModels = Lists.newArrayList(complexityModels);
    }

    @Override
    public boolean isValid(OWLClassExpression ce) {
        // check whether it's not valid for one of the complexity models
        for (ComplexityModel complexityModel : complexityModels) {
            if(!complexityModel.isValid(ce)) {
                return false;
            }
        }
        return true;
    }
}
