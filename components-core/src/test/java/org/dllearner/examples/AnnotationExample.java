package org.dllearner.examples;


import javax.xml.transform.Source;
import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 7/26/11
 * Time: 9:03 PM
 * <p/>
 * An example showing the use of Annotations with non basic types/classes.
 */
public class AnnotationExample {


    public static void main(String[] args) throws Exception {

        /** The Configuration map comes in from somewhere - The keys here correspond to the configuration options in ConfiguredObject*/
        Map<String, String> propertiesMap = new HashMap<String, String>();
        propertiesMap.put("initString", "My Initialization String");
        propertiesMap.put("objectProperty", "My Object Property");

        /** Here's my new configured object */
        ConfiguredObject obj = new ConfiguredObject();

        Method[] methods = obj.getClass().getMethods();
        for (Method method : methods) {
            ConfigOption option = method.getAnnotation(ConfigOption.class);
            /** If Annotated with Config Option*/
            if (option != null) {

                /** Retrieve the actual String value from the properties map */
                String configValue = propertiesMap.get(option.name());

                /** Might be a good idea to catch a class cast exception here if we're wrong */
                PropertyEditor editor = (PropertyEditor) option.propertyEditorClass().newInstance();

                /** This line causes the property editors to convert the string to the object*/
                editor.setAsText(configValue);

                /** This line grabs the object we want and invokes the method we're currently working on - with this scheme, only annotations should go on set methods */
                method.invoke(obj, editor.getValue());
            }
        }

        /** Now show that we actually stored the objects on the ConfiguredObject itself */
        System.out.println("Information on Configured Object:");
        System.out.println("Initialization String: " + obj.getInitializationString());
        System.out.println("ObjectProperty: " + obj.getObjectProperty().getName());

    }
}
