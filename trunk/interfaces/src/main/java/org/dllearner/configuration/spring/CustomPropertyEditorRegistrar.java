package org.dllearner.configuration.spring;

import org.dllearner.confparser3.IndividualCollectionEditor;
import org.dllearner.confparser3.MapEditor;
import org.dllearner.confparser3.SetEditor;
import org.dllearner.learningproblems.PosNegLP;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/27/11
 * Time: 12:38 PM
 * <p/>
 * This is where we will register custom property editors for properties which can't be configured by the standard
 * PropertyEditors.
 */
public class CustomPropertyEditorRegistrar implements PropertyEditorRegistrar {

    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {

        if (registry instanceof BeanWrapper) {
            Object wrappedInstance = ((BeanWrapper) registry).getWrappedInstance();
            if (wrappedInstance instanceof PosNegLP) {
                registry.registerCustomEditor(Collection.class, "positiveExamples", new IndividualCollectionEditor());
                registry.registerCustomEditor(Collection.class, "negativeExamples", new IndividualCollectionEditor());
            }
        }

        //Wrappers for all beans
        registry.registerCustomEditor(Map.class,new MapEditor());
        registry.registerCustomEditor(Set.class,new SetEditor());


    }
}
