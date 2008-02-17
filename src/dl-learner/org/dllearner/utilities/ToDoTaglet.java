package org.dllearner.utilities;

import java.util.Map;

import com.sun.javadoc.Tag;
import com.sun.tools.doclets.Taglet;

/**
 * Small taglet for showing todo-markers in Javadoc-runs. You can insert
 * the following in comments:
 * 
 * @.todo task
 *   
 * (Note the dot, which avoids conflicts with a possible future @todo
 * standard tag.) Parts of the code are taken from the JDK Javadoc.
 * 
 * @author Jens Lehmann
 *
 */
public class ToDoTaglet implements Taglet {

    private static final String NAME = ".todo";
    private static final String HEADER = "To Do:";
    
    /**
     * Return the name of this custom tag.
     */
    public String getName() {
        return NAME;
    }
    
    /**
     * Will return true since <code>@.todo</code>
     * can be used in field documentation.
     * @return true since <code>@.todo</code>
     * can be used in field documentation and false
     * otherwise.
     */
    public boolean inField() {
        return true;
    }

    /**
     * Will return true since <code>@todo</code>
     * can be used in constructor documentation.
     * @return true since <code>@todo</code>
     * can be used in constructor documentation and false
     * otherwise.
     */
    public boolean inConstructor() {
        return true;
    }
    
    /**
     * Will return true since <code>@todo</code>
     * can be used in method documentation.
     * @return true since <code>@todo</code>
     * can be used in method documentation and false
     * otherwise.
     */
    public boolean inMethod() {
        return true;
    }
    
    /**
     * Will return true since <code>@todo</code>
     * can be used in method documentation.
     * @return true since <code>@todo</code>
     * can be used in overview documentation and false
     * otherwise.
     */
    public boolean inOverview() {
        return true;
    }

    /**
     * Will return true since <code>@todo</code>
     * can be used in package documentation.
     * @return true since <code>@todo</code>
     * can be used in package documentation and false
     * otherwise.
     */
    public boolean inPackage() {
        return true;
    }

    /**
     * Will return true since <code>@todo</code>
     * can be used in type documentation (classes or interfaces).
     * @return true since <code>@todo</code>
     * can be used in type documentation and false
     * otherwise.
     */
    public boolean inType() {
        return true;
    }
    
    /**
     * Will return false since <code>@todo</code>
     * is not an inline tag.
     * @return false since <code>@todo</code>
     * is not an inline tag.
     */
    
    public boolean isInlineTag() {
        return false;
    }
    
    /**
     * Register this Taglet.
     * @param tagletMap  the map to register this tag to.
     */
    @SuppressWarnings({"unchecked"})
    public static void register(Map tagletMap) {
       ToDoTaglet tag = new ToDoTaglet();
       Taglet t = (Taglet) tagletMap.get(tag.getName());
       if (t != null) {
           tagletMap.remove(tag.getName());
       }
       tagletMap.put(tag.getName(), tag);
    }

    /**
     * Given the <code>Tag</code> representation of this custom
     * tag, return its string representation.
     * @param tag   the <code>Tag</code> representation of this custom tag.
     */
    public String toString(Tag tag) {
        return "<DT><B>" + HEADER + "</B><DD>"
               + "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"#DC8080\">"
               + tag.text()
               + "</td></tr></table></DD>\n";
    }
    
    /**
     * Given an array of <code>Tag</code>s representing this custom
     * tag, return its string representation.
     * @param tags  the array of <code>Tag</code>s representing of this custom tag.
     */
    public String toString(Tag[] tags) {
        if (tags.length == 0) {
            return null;
        }
        String result = "\n<DT><B>" + HEADER + "</B><DD>";
        result += "<table cellpadding=2 cellspacing=0><tr><td bgcolor=\"#DC8080\">";
        for (int i = 0; i < tags.length; i++) {
            if (i > 0) {
                result += ", ";
            }
            result += tags[i].text();
        }
        return result + "</td></tr></table></DD>\n";
    }

}
