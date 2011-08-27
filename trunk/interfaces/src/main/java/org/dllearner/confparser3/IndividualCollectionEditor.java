package org.dllearner.confparser3;

import org.dllearner.core.owl.Individual;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: Chris
 * Date: 8/27/11
 * Time: 11:42 AM
 *
 * Property Editor for Collections of Individuals.
 */
public class IndividualCollectionEditor extends PropertyEditorSupport {

    public IndividualCollectionEditor() {
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(convert(text));
    }


    //    @Override
    protected Collection<Individual> convert(String value) {
        Collection<Individual> result = new TreeSet<Individual>();
        StringTokenizer tokenizer = new StringTokenizer(value, "{}\", ");
        while (tokenizer.hasMoreElements()) {
            result.add(new Individual(tokenizer.nextToken()));
        }
        return result;
    }

}
