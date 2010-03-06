package org.dllearner.tools.ore.ui.editor;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.model.OWLObject;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: May 4, 2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class AutoCompleterMatcher{

    private OREManager oreManager;


    public AutoCompleterMatcher(OREManager oreManager) {
        this.oreManager = oreManager;
    }


    public Set<OWLObject> getMatches(String fragment, boolean classes, boolean objectProperties, boolean dataProperties,
    		                         boolean individuals, boolean datatypes) {
//        TreeSet<OWLObject> set = new TreeSet<OWLObject>(owlModelManager.getOWLObjectComparator());
    	 TreeSet<OWLObject> set = new TreeSet<OWLObject>(new OWLObjectComparator<OWLObject>());

        fragment = fragment + "*"; // look for strings that start with the given fragment

        if (classes) {
            set.addAll(oreManager.getOWLEntityFinder().getMatchingOWLClasses(fragment, false));
        }
        if (objectProperties) {
            set.addAll(oreManager.getOWLEntityFinder().getMatchingOWLObjectProperties(fragment, false));
        }
        if (dataProperties) {
            set.addAll(oreManager.getOWLEntityFinder().getMatchingOWLDataProperties(fragment, false));
        }
        if (individuals) {
            set.addAll(oreManager.getOWLEntityFinder().getMatchingOWLIndividuals(fragment, false));
        }
        if (datatypes) {
            set.addAll(oreManager.getOWLEntityFinder().getMatchingOWLDatatypes(fragment, false));
        }
        return set;
    }
    
    private class OWLObjectComparator<E extends OWLObject> implements Comparator<E>{

		@Override
		public int compare(E o1, E o2) {
			return o1.compareTo(o2);
		}
    	
    }
    
    
}
