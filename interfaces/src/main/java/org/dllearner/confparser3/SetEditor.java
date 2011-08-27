package org.dllearner.confparser3;

import java.beans.PropertyEditorSupport;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/27/11
 * Time: 1:57 PM
 * <p/>
 * Property Editor for Sets.
 */
public class SetEditor extends PropertyEditorSupport {

    @Override
    public void setAsText(String text) throws IllegalArgumentException {

        StringTokenizer tokenizer = new StringTokenizer(text, "[(,\") ]");

        Set<String> result = new HashSet<String>();

        while (tokenizer.hasMoreTokens()) {
            result.add(tokenizer.nextToken());
        }

        setValue(result);
    }
}
