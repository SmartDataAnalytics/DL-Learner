package org.dllearner.configuration.spring;

import org.dllearner.configuration.spring.editors.ClassExpressionPropertyEditor;
import org.dllearner.configuration.spring.editors.OWLEntityEditor;
import org.dllearner.configuration.spring.editors.ReasonerImplementationEditor;
import org.dllearner.reasoning.ReasonerImplementation;
import org.semanticweb.owlapi.model.*;
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
    	registry.registerCustomEditor(OWLClassExpression.class, new ClassExpressionPropertyEditor());
//		registry.registerCustomEditor(OWLClass.class, new OWLEntityEditor<EntityType<OWLClass>>());
		registry.registerCustomEditor(OWLObjectProperty.class, new OWLEntityEditor<>(EntityType.OBJECT_PROPERTY));
		registry.registerCustomEditor(OWLDataProperty.class, new OWLEntityEditor<>(EntityType.DATA_PROPERTY));
		registry.registerCustomEditor(OWLIndividual.class, new OWLEntityEditor<>(EntityType.NAMED_INDIVIDUAL));
    	registry.registerCustomEditor(ReasonerImplementation.class, new ReasonerImplementationEditor());
    	
    }
}
