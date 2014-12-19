/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.dllearner.utilities.owl;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.IntMaxValue;
import org.dllearner.core.owl.IntMinValue;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;

/**
 * Rewrites a description to an equivalent shorter description. Note that
 * minimizing is not a trivial operation and requires reasoning. The class
 * keeps an internal cache on reasoning results, i.e. if similar descriptions
 * are passed to the minimizer, then its performance will improve over time. 
 * 
 * @author Jens Lehmann
 * 
 */
public class DescriptionMinimizer {
	
	private static final NamedClass OWL_THING = new NamedClass(Thing.uri);

	private AbstractReasonerComponent reasoner;
	private ConceptComparator conceptComparator = new ConceptComparator();
	private Map<Description,Map<Description,Boolean>> cachedSubclassOf = new TreeMap<Description,Map<Description,Boolean>>(conceptComparator);	

	private boolean beautify = true;
	
	public DescriptionMinimizer(AbstractReasonerComponent reasoner) {
		this.reasoner = reasoner;
	}

	/**
	 * Method which minimizes the input description. The algorithm does not 
	 * replace subdescriptions with named classes, e.g.
	 * if the description "male \sqcap \exists hasChild.\top" is passed to the
	 * algorithm and a class "father" is defined in the obvious way 
	 * in the background knowledge, then
	 * it will intentionally not return father. Instead, it preserves the
	 * existing structure of the description, but tries to detect and delete
	 * redundant parts within it. For instance, the description "male \sqcap
	 * father" is minimized to "father".
	 * 
	 * @param description The description to minimize.
	 * @return Minimized description.
	 */
	public Description minimizeClone(Description description) {
		Description descriptionToMinimize = description.clone();
		return minimize(descriptionToMinimize);
	}
	
	/**
	 * Same as {@link #minimizeClone(Description)}, but with no guarantee that
	 * the input description remains unmodified.
	 * @see #minimizeClone(Description)
	 * @param description The description to minimize.
	 * @return Minimized description.
	 */
	public Description minimize(Description description) {
		// minimize all children of the description
		List<Description> children = description.getChildren();
		for(int i=0; i<children.size(); i++) {
			description.replaceChild(i, minimize(children.get(i)));
		}
		// make a case distinction based on the structure of the description
		// (note that we do not have to do anything for a number of cases:
		// a named class, Thing, Nothing, hasValue restrictions, boolean
		// value restrictions, double value restrictions)
		if(children.size()==0) {
			return description;
		} else if(description instanceof ObjectSomeRestriction) {
			// \exists r.\bot \equiv \bot
			if(description.getChild(0) instanceof Nothing) {
				return Nothing.instance;
			} 
			return description;
		} else if(description instanceof ObjectAllRestriction) {
			// \forall r.\top \equiv \top
			if(description.getChild(0) instanceof Thing) {
				return Thing.instance;
			} 		
			// we rewrite \forall r.\bot to \neg \exists r.\top
			// which is longer but easier to understand for humans
			if(beautify && description.getChild(0) instanceof Nothing) {
				ObjectProperty p = (ObjectProperty)((ObjectAllRestriction)description).getRole();
				return new Negation(new ObjectSomeRestriction(p, Thing.instance));
			}
			return description;
		} else if(description instanceof Negation) {
			// \neg \bot \equiv \top
			if(description.getChild(0) instanceof Nothing) {
				return Thing.instance;
			// \neg \top \equiv \bot
			} else if(description.getChild(0) instanceof Thing) {
				return Nothing.instance;
			} 		
			return description;
		} else if(description instanceof ObjectMaxCardinalityRestriction) {
			// <= n r.\bot \equiv \top
			if(description.getChild(0) instanceof Nothing) {
				return Thing.instance;
			} 			
			// we rewrite <= 0 r C to \neg \exists r C - easier to read for humans
			if(((ObjectMaxCardinalityRestriction)description).getCardinality() == 0) {
				ObjectProperty p = (ObjectProperty)((ObjectMaxCardinalityRestriction)description).getRole();
				return new Negation(new ObjectSomeRestriction(p, description.getChild(0)));
			}
			return description;
		} else if(description instanceof ObjectMinCardinalityRestriction) {
			// >= 0 r.C \equiv \top
			int number = ((ObjectMinCardinalityRestriction)description).getNumber();
			if(number == 0) {
				return Thing.instance;
			}
			// >= n r.\bot \equiv \bot if n != 0
			if(description.getChild(0) instanceof Nothing) {
				return Nothing.instance;
			}
			return description;
		} else if(description instanceof Intersection || description instanceof Union) {
			
			if(description instanceof Intersection) {
				for(int i=0; i<children.size(); i++) {
					for(int j=0; j<children.size(); j++) {
//						if(children.get(j) instanceof ObjectSomeRestriction && children.get(i) instanceof ObjectSomeRestriction){
//							System.out.println(children.get(j) + "::::" + children.get(i));
////							System.out.println(children.get(j).getClass().getSimpleName() + "::::" + children.get(i).getClass().getSimpleName());
//							System.out.println(children.get(j).getChild(0).getClass().getSimpleName() + "::::" + children.get(i).getChild(0).getClass().getSimpleName());
//							Description d1 = children.get(j);
//							Description d2 = children.get(i);
//							boolean subclassOf = isSubclassOf(d1, d2);
//							System.out.println(subclassOf);
//						}
						boolean isSubclassOf = isSubclassOf(children.get(j), children.get(i));
						if(i != j && isSubclassOf) {
							// remove element because it is super class of another element
							children.remove(i);
							// we apply the minimization procedure again after removal of the element
							// (this is not the fastest implementation but avoids bugs as in the previous code)
							if(children.size()==1) {
								return minimize(children.get(0));
							} else {
								return minimize(description);
							}
						}
					}
				}				
			} else {
				for(int i=0; i<children.size(); i++) {
					for(int j=0; j<children.size(); j++) {
						if(i != j && isSubclassOf(children.get(i), children.get(j))) {
							children.remove(i);
							if(children.size()==1) {
								return minimize(children.get(0));
							} else {
								return minimize(description);
							}
						}
					}
				}
			}
			
			// no subclass relationships => description is already minimal
			return description;
			
			// the code below is buggy because in "A AND A AND C", it removes both As
			
//			List<Integer> toRemove = new LinkedList<Integer>();
//			// intersection
//			if(description instanceof Intersection) {
//				// in an intersection, we have that D1 \sqcap D2 \equiv D1 if
//				// D1 \sqsubseteq D2; this means we first check whether the
//				// first element in an intersection is subclass of any other element in the
//				// intersection; if yes, then we delete it and proceed to the next element
//				for(int i=0; i<children.size(); i++) {
//					for(int j=0; j<children.size(); j++) {
//						if(i!=j)
//						System.out.println(children.get(i) + " -- " + children.get(j));						
//						if(i != j && isSubclassOf(children.get(j), children.get(i))) {
//							System.out.println("sub");
//							toRemove.add(i-toRemove.size());
//							break;
//						}
//					}
//				}
//			// union
//			} else {
//				// in a union, we have that D1 \sqcup D2 \equiv D2 if
//				// D1 \sqsubseteq D2; this means we first check whether the
//				// first element in a union is subclass of any other element in the
//				// union; if yes, then we delete it and proceed to the next element
//				// (note the difference to intersection)				
//				for(int i=0; i<children.size(); i++) {
//					for(int j=0; j<children.size(); j++) {
//						if(i != j && isSubclassOf(children.get(i), children.get(j))) {
//							toRemove.add(i-toRemove.size());
//							break;
//						}
//					}
//				}				
//			}
//			
//			System.out.println("to remove: " + toRemove);
//			
//			// if all elements are superfluous wrt. another element, then we need
//			// to keep at least one
//			if(toRemove.size() == children.size()) {
//				return children.get(0);
//			} else {
//				// remove all elements according to remove list
//				for(int pos : toRemove) {
//					children.remove(pos);
//				}
//				// dissolve intersection with only one element
//				if(children.size()==1) {
//					return children.get(0);
//				}	
//				return description;
//			}
		} else {
			throw new Error("Cannot minimize description " + description + ".");
		}
	}
	
	private boolean isSubclassOf(Description d1, Description d2) {
		if(beautify && (d1 instanceof DatatypeSomeRestriction) && (d2 instanceof DatatypeSomeRestriction)){
			DataRange dr1 = ((DatatypeSomeRestriction)d1).getDataRange();
			DataRange dr2 = ((DatatypeSomeRestriction)d2).getDataRange();
			if(dr1 instanceof IntMinValue && dr2 instanceof IntMinValue){
				return ((IntMinValue)dr1).getValue() >= ((IntMinValue)dr2).getValue();
			} else if(dr1 instanceof IntMaxValue && dr2 instanceof IntMaxValue){
				return ((IntMaxValue)dr1).getValue() <= ((IntMaxValue)dr2).getValue();
			}
		} 
		if(d1.equals(d2)) return true;
		if(d2 == Thing.instance || d2.equals(OWL_THING)) return true;
//		if(!(d1.isNamedClass() && d2.isNamedClass())) return false;
		// check whether we have cached this query
		Map<Description,Boolean> tmp = cachedSubclassOf.get(d1);
		Boolean tmp2 = null;
		if(tmp != null)
			tmp2 = tmp.get(d2);
		
		if(tmp2==null) {
			
			Boolean result = reasoner.isSuperClassOf(d2, d1);
						
			// create new entry if necessary
			Map<Description,Boolean> map1 = new TreeMap<Description,Boolean>(conceptComparator);
			if(tmp == null)
				cachedSubclassOf.put(d1, map1);
			
			cachedSubclassOf.get(d1).put(d2, result);
			return result;
		} else {
			return tmp2;
		}
	}	
	
	public static void main(String[] args) throws Exception {
		Description d1 = new ObjectSomeRestriction(new ObjectProperty("r"), Thing.instance);
		Description d2 = new ObjectSomeRestriction(new ObjectProperty("r"), new NamedClass(Thing.uri.toString()));
		
		System.out.println(d1.equals(d2));
	}
}
