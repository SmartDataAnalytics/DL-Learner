package org.dllearner.examples;

import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.examples.ConfigOption;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 7/26/11
 * Time: 8:57 PM
 *
 * In this example, this is the object that is actually getting configured.  It has the fields that we want to define
 * configuration on and hence that's why we use the annotations.
 */
public class ConfiguredObject {




    /** Two options that can be configured for this object */
    private String initializationString;
    private ObjectProperty objectProperty;


    /**
     * Default constructor
     */
    public ConfiguredObject() {

    }


    /**
     * Get the Initialization String.
     * @return The Initialization String
     */
    public String getInitializationString() {
        return initializationString;
    }

    /**
     * Set the Initialization String
     * @param initializationString
     */
    @ConfigOption(name="initString", description = "An Initialization String", propertyEditorClass = OurStringTrimmerEditor.class)
    public void setInitializationString(String initializationString) {
        this.initializationString = initializationString;
    }

    /** Corresponds with the name above */
    public ObjectProperty getObjectProperty() {
        return objectProperty;
    }
    @ConfigOption(name="objectProperty", description = "Some Object Property", propertyEditorClass = ObjectPropertyEditor.class)
    public void setObjectProperty(ObjectProperty objectProperty) {
        this.objectProperty = objectProperty;
    }
}
