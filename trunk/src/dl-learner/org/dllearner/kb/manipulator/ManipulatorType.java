package org.dllearner.kb.manipulator;

import java.util.LinkedList;

import org.dllearner.utilities.datastructures.StringTuple;


/**
 * Used to get the right manipulator
 * 
 * @author Sebastian Knappe
 * 
 */
public class ManipulatorType {
	
	public static Manipulators getManipulatorByName(String predefinedManipulator,String blankNodeIdentifier, int	breakSuperClassRetrievalAfter, LinkedList<StringTuple> replacePredicate,LinkedList<StringTuple> replaceObject)
	{
		if (predefinedManipulator.equals("DBPEDIA-NAVIGATOR")) return new DBpediaNavigatorManipulator(blankNodeIdentifier,
				breakSuperClassRetrievalAfter, replacePredicate, replaceObject);
		else return new OldManipulator(blankNodeIdentifier,
				breakSuperClassRetrievalAfter, replacePredicate, replaceObject);
	}
}
