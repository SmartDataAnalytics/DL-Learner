package org.dllearner.core.config;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 7/26/11
 * Time: 10:17 PM
 * <p/>
 * Making a basic extension so I can get a default constructor so that using reflection is simpler.
 *
 * I'm extending a Spring provided class for this.
 */
public class OurStringTrimmerEditor extends org.springframework.beans.propertyeditors.StringTrimmerEditor {


    /**
     * Default Constructor
     */
    public OurStringTrimmerEditor() {
        super(true);
    }
}
