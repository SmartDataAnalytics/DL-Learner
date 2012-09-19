package org.dllearner.configuration.spring;

import org.dllearner.core.config.ClassExpressionPropertyEditor;
import org.dllearner.core.owl.Description;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

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
        //Register any custom editors here.
    	ClassExpressionPropertyEditor ce = new ClassExpressionPropertyEditor();
    	registry.registerCustomEditor(Description.class, ce);
    }
}
