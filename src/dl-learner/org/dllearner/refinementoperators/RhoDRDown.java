/**
 * Copyright (C) 2007-2008, Jens Lehmann
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
 *
 */
package org.dllearner.refinementoperators;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.DataRange;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.DatatypeSomeRestriction;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.DoubleMaxValue;
import org.dllearner.core.owl.DoubleMinValue;
import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectCardinalityRestriction;
import org.dllearner.core.owl.ObjectMaxCardinalityRestriction;
import org.dllearner.core.owl.ObjectMinCardinalityRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.utilities.Helper;
import org.dllearner.utilities.owl.ConceptComparator;
import org.dllearner.utilities.owl.ConceptTransformation;

/**
 * A downward refinement operator, which makes use of domains
 * and ranges of properties. The operator is currently under
 * development. Its aim is to span a much "cleaner" and smaller search
 * tree compared to RhoDown by omitting many class descriptions,
 * which are obviously too weak, because they violate 
 * domain/range restrictions. Furthermore, it makes use of disjoint
 * classes in the knowledge base.
 * 
 * @author Jens Lehmann
 *
 */
public class RhoDRDown implements RefinementOperator {

	@SuppressWarnings({"unused"})
	private static Logger logger = Logger
	.getLogger(RhoDRDown.class);	
	
	private ReasoningService rs;
	
	// hierarchies
	private SubsumptionHierarchy subHierarchy;
	
	// domains and ranges
	private Map<ObjectProperty,Description> opDomains = new TreeMap<ObjectProperty,Description>();
	private Map<DatatypeProperty,Description> dpDomains = new TreeMap<DatatypeProperty,Description>();
	private Map<ObjectProperty,Description> opRanges = new TreeMap<ObjectProperty,Description>();
	
	// maximum number of fillers for eeach role
	private Map<ObjectProperty,Integer> maxNrOfFillers = new TreeMap<ObjectProperty,Integer>();
	// limit for cardinality restrictions (this makes sense if we e.g. have compounds with up to
	// more than 200 atoms but we are only interested in atoms with certain characteristics and do
	// not want something like e.g. >= 204 hasAtom.NOT Carbon-87; which blows up the search space
	private int cardinalityLimit = 5;
	
	// start concept (can be used to start from an arbitrary concept, needs
	// to be Thing or NamedClass), note that when you use e.g. Compound as 
	// start class, then the algorithm should start the search with class
	// Compound (and not with Thing), because otherwise concepts like
	// NOT Carbon-87 will be returned which itself is not a subclass of Compound
	private Description startClass = new Thing();
	
	// the length of concepts of top refinements, the first values is
	// for refinements of \rho_\top(\top), the second one for \rho_A(\top)
	private int topRefinementsLength = 0;
	private Map<NamedClass, Integer> topARefinementsLength = new TreeMap<NamedClass, Integer>();
	// M is finite and this value is the maximum length of any value in M
	private static int mMaxLength = 4;
	
	// the sets M_\top and M_A
	private Map<Integer,SortedSet<Description>> m = new TreeMap<Integer,SortedSet<Description>>();
	private Map<NamedClass,Map<Integer,SortedSet<Description>>> mA = new TreeMap<NamedClass,Map<Integer,SortedSet<Description>>>();
	
	// @see MathOperations.getCombos
	private Map<Integer, List<List<Integer>>> combos = new HashMap<Integer, List<List<Integer>>>();

	// refinements of the top concept ordered by length
	private Map<Integer, SortedSet<Description>> topRefinements = new TreeMap<Integer, SortedSet<Description>>();
	private Map<NamedClass,Map<Integer, SortedSet<Description>>> topARefinements = new TreeMap<NamedClass,Map<Integer, SortedSet<Description>>>();
	
	// cumulated refinements of top (all from length one to the specified length)
	private Map<Integer, TreeSet<Description>> topRefinementsCumulative = new HashMap<Integer, TreeSet<Description>>();
	private Map<NamedClass,Map<Integer, TreeSet<Description>>> topARefinementsCumulative = new TreeMap<NamedClass,Map<Integer, TreeSet<Description>>>();
	
	// app_A set of applicable properties for a given class (separte for
	// object properties, boolean datatypes, and double data types)
	private Map<NamedClass, Set<ObjectProperty>> appOP = new TreeMap<NamedClass, Set<ObjectProperty>>();
	private Map<NamedClass, Set<DatatypeProperty>> appBD = new TreeMap<NamedClass, Set<DatatypeProperty>>();
	private Map<NamedClass, Set<DatatypeProperty>> appDD = new TreeMap<NamedClass, Set<DatatypeProperty>>();
	
	// most general applicable properties
	private Map<NamedClass,Set<ObjectProperty>> mgr = new TreeMap<NamedClass,Set<ObjectProperty>>();
	private Map<NamedClass,Set<DatatypeProperty>> mgbd = new TreeMap<NamedClass,Set<DatatypeProperty>>();
	private Map<NamedClass,Set<DatatypeProperty>> mgdd = new TreeMap<NamedClass,Set<DatatypeProperty>>();
	
	// concept comparator
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	// splits for double datatype properties in ascening order
	private Map<DatatypeProperty,List<Double>> splits = new TreeMap<DatatypeProperty,List<Double>>();
	private int maxNrOfSplits = 10;
	
	// staistics
	public long mComputationTimeNs = 0;
	public long topComputationTimeNs = 0;
	
	private boolean applyAllFilter = true;
	private boolean applyExistsFilter = true;
	private boolean useAllConstructor = true;
	private boolean useExistsConstructor = true;
	private boolean useCardinalityRestrictions = true;
	private boolean useNegation = true;
	private boolean useBooleanDatatypes = true;
	private boolean useDoubleDatatypes = true;
	private boolean disjointChecks = true;
	private boolean instanceBasedDisjoints = true;
	
	// caches for reasoner queries
	private Map<Description,Map<Description,Boolean>> cachedDisjoints = new TreeMap<Description,Map<Description,Boolean>>(conceptComparator);

//	private Map<NamedClass,Map<NamedClass,Boolean>> abDisjoint = new TreeMap<NamedClass,Map<NamedClass,Boolean>>();
//	private Map<NamedClass,Map<NamedClass,Boolean>> notABDisjoint = new TreeMap<NamedClass,Map<NamedClass,Boolean>>();
//	private Map<NamedClass,Map<NamedClass,Boolean>> notABMeaningful = new TreeMap<NamedClass,Map<NamedClass,Boolean>>();
	
	public RhoDRDown(ReasoningService reasoningService) {
		this(reasoningService, true, true, true, true, true, true, true, true, null);
	}
	
	public RhoDRDown(ReasoningService reasoningService, boolean applyAllFilter, boolean applyExistsFilter, boolean useAllConstructor,
			boolean useExistsConstructor,boolean  useCardinalityRestrictions,boolean useNegation, boolean useBooleanDatatypes, boolean useDoubleDatatypes, NamedClass startClass) {
		this.rs = reasoningService;
		this.applyAllFilter = applyAllFilter;
		this.applyExistsFilter = applyExistsFilter;
		this.useAllConstructor = useAllConstructor;
		this.useExistsConstructor = useExistsConstructor;
		this.useCardinalityRestrictions = useCardinalityRestrictions;
		this.useNegation = useNegation;
		this.useBooleanDatatypes = useBooleanDatatypes;
		this.useDoubleDatatypes = useDoubleDatatypes;
		
		subHierarchy = rs.getSubsumptionHierarchy();
		
		// query reasoner for domains and ranges
		// (because they are used often in the operator)
		for(ObjectProperty op : rs.getAtomicRoles()) {
			opDomains.put(op, rs.getDomain(op));
			opRanges.put(op, rs.getRange(op));
		}
		for(DatatypeProperty dp : rs.getDatatypeProperties()) {
			dpDomains.put(dp, rs.getDomain(dp));
		}
		
		// compute splits for double datatype properties
		for(DatatypeProperty dp : rs.getDoubleDatatypeProperties()) {
			computeSplits(dp);
		}
		
		// determine the maximum number of fillers for each role
		// (up to a specified cardinality maximum)
		if(useCardinalityRestrictions) {
		for(ObjectProperty op : rs.getAtomicRoles()) {
			int maxFillers = 0;
			Map<Individual,SortedSet<Individual>> opMembers = rs.getRoleMembers(op);
			for(SortedSet<Individual> inds : opMembers.values()) {
				if(inds.size()>maxFillers)
					maxFillers = inds.size();
				if(maxFillers >= cardinalityLimit) {
					maxFillers = cardinalityLimit;
					break;
				}	
			}
			maxNrOfFillers.put(op, maxFillers);
		}
		}
		
		/*
		String conceptStr = "(\"http://dl-learner.org/carcinogenesis#Compound\" AND (>= 2 \"http://dl-learner.org/carcinogenesis#hasStructure\".\"http://dl-learner.org/carcinogenesis#Ar_halide\" OR ((\"http://dl-learner.org/carcinogenesis#amesTestPositive\" IS TRUE) AND >= 5 \"http://dl-learner.org/carcinogenesis#hasBond\". TOP)))";
		try {
			NamedClass struc = new NamedClass("http://dl-learner.org/carcinogenesis#Compound");
			Description d = KBParser.parseConcept(conceptStr);
			SortedSet<Description> ds = (SortedSet<Description>) refine(d,15,null,struc);
			System.out.println(ds);
			
			Individual i = new Individual("http://dl-learner.org/carcinogenesis#d101");
			rs.instanceCheck(ds.first(), i);
			
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.exit(0);
		*/
		
		/*
		NamedClass struc = new NamedClass("http://dl-learner.org/carcinogenesis#Atom");
		ObjectProperty op = new ObjectProperty("http://dl-learner.org/carcinogenesis#hasAtom");
		ObjectSomeRestriction oar = new ObjectSomeRestriction(op,Thing.instance);

		Set<Description> ds = refine(Thing.instance,3,null,struc);
//		Set<Description> improper = new HashSet<Description>();
		for(Description d : ds) {
//			if(rs.subsumes(d, struc)) {
//				improper.add(d);
				System.out.println(d);
//			}
		}
		System.out.println(ds.size());
//		System.out.println(improper.size());
		System.exit(0);
		*/
		 
		if(startClass != null)
			this.startClass = startClass;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.refinement.RefinementOperator#refine(org.dllearner.core.owl.Description)
	 */
	public Set<Description> refine(Description concept) {
		throw new RuntimeException();
	}

	/* (non-Javadoc)
	 * @see org.dllearner.algorithms.refinement.RefinementOperator#refine(org.dllearner.core.owl.Description, int, java.util.List)
	 */
	public Set<Description> refine(Description description, int maxLength,
			List<Description> knownRefinements) {
		return refine(description, maxLength, knownRefinements, startClass);
	}

	@SuppressWarnings({"unchecked"})
	public Set<Description> refine(Description description, int maxLength,
			List<Description> knownRefinements, Description currDomain) {
		
//		logger.trace(description + " " + currDomain + " " + maxLength);
		
		// actions needing to be performed if this is the first time the
		// current domain is used
		if(!(currDomain instanceof Thing) && !topARefinementsLength.containsKey(currDomain))
			topARefinementsLength.put((NamedClass)currDomain, 0);
		
		// check whether using list or set makes more sense 
		// here; and whether HashSet or TreeSet should be used
		// => TreeSet because duplicates are possible
		Set<Description> refinements = new TreeSet<Description>(conceptComparator);
		
		// used as temporary variable
		Set<Description> tmp = new HashSet<Description>();
		
		if(description instanceof Thing) {
			// extends top refinements if necessary
			if(currDomain instanceof Thing) {
				if(maxLength>topRefinementsLength)
					computeTopRefinements(maxLength);
				refinements = (TreeSet<Description>) topRefinementsCumulative.get(maxLength).clone();
			} else {
				if(maxLength>topARefinementsLength.get(currDomain))
					computeTopRefinements(maxLength, (NamedClass) currDomain);
				refinements = (TreeSet<Description>) topARefinementsCumulative.get(currDomain).get(maxLength).clone();
			}
			
//			refinements.addAll(subHierarchy.getMoreSpecialConcepts(description));
		} else if(description instanceof Nothing) {
			// cannot be further refined
		} else if(description instanceof NamedClass) {
			refinements.addAll(subHierarchy.getMoreSpecialConcepts(description));
			refinements.remove(new Nothing());
		} else if (description instanceof Negation && description.getChild(0) instanceof NamedClass) {
		
			tmp = rs.getMoreGeneralConcepts(description.getChild(0));
				
			for(Description c : tmp) {
				if(!(c instanceof Thing))
					refinements.add(new Negation(c));
			}
		
		} else if (description instanceof Intersection) {
				
			// refine one of the elements
			for(Description child : description.getChildren()) {
				
				// refine the child; the new max length is the current max length minus
				// the currently considered concept plus the length of the child
				// TODO: add better explanation
				tmp = refine(child, maxLength - description.getLength()+child.getLength(),null,currDomain);
				
				// create new intersection
				for(Description c : tmp) {
					List<Description> newChildren = (List<Description>)((LinkedList)description.getChildren()).clone();
					newChildren.add(c);
					newChildren.remove(child);
					Intersection mc = new Intersection(newChildren);
					
					// clean concept and transform it to ordered negation normal form
					// (non-recursive variant because only depth 1 was modified)
					ConceptTransformation.cleanConceptNonRecursive(mc);
					ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(mc, conceptComparator);
					
					// check whether the intersection is OK (sanity checks), then add it
					if(checkIntersection(mc))
						refinements.add(mc);
				}
				
			}
				
		} else if (description instanceof Union) {
			// refine one of the elements
			for(Description child : description.getChildren()) {
				
//				System.out.println("union child: " + child + " " + maxLength + " " + description.getLength() + " " + child.getLength());
				
				// refine child
				tmp = refine(child, maxLength - description.getLength()+child.getLength(),null,currDomain);
				
				// construct intersection (see above)
				for(Description c : tmp) {
					List<Description> newChildren = new LinkedList<Description>(description.getChildren());
					newChildren.remove(child);						
					newChildren.add(c);
					Union md = new Union(newChildren);
						
					// transform to ordered negation normal form
					ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(md, conceptComparator);
					// note that we do not have to call clean here because a disjunction will
					// never be nested in another disjunction in this operator
					
					refinements.add(md);	
				}
				
			}
			
		} else if (description instanceof ObjectSomeRestriction) {
			ObjectPropertyExpression role = ((ObjectQuantorRestriction)description).getRole();
			Description range = opRanges.get(role);
			
			// rule 1: EXISTS r.D => EXISTS r.E
			tmp = refine(description.getChild(0), maxLength-2, null, range);

			for(Description c : tmp)
				refinements.add(new ObjectSomeRestriction(((ObjectQuantorRestriction)description).getRole(),c));
			
			// rule 2: EXISTS r.D => EXISTS s.D or EXISTS r^-1.D => EXISTS s^-1.D
			// currently inverse roles are not supported
			ObjectProperty ar = (ObjectProperty) role;
			Set<ObjectProperty> moreSpecialRoles = rs.getMoreSpecialRoles(ar);
			for(ObjectProperty moreSpecialRole : moreSpecialRoles)
				refinements.add(new ObjectSomeRestriction(moreSpecialRole, description.getChild(0)));

			// rule 3: EXISTS r.D => >= 2 r.D
			// (length increases by 1 so we have to check whether max length is sufficient)
			if(useCardinalityRestrictions) {
				if(maxLength > description.getLength() && maxNrOfFillers.get(ar)>1) {
					ObjectMinCardinalityRestriction min = new ObjectMinCardinalityRestriction(2,role,description.getChild(0));
					refinements.add(min);
				}
			}
			
		} else if (description instanceof ObjectAllRestriction) {
			ObjectPropertyExpression role = ((ObjectQuantorRestriction)description).getRole();
			Description range = opRanges.get(role);
			
			// rule 1: ALL r.D => ALL r.E
			tmp = refine(description.getChild(0), maxLength-2, null, range);

			for(Description c : tmp) {
				refinements.add(new ObjectAllRestriction(((ObjectQuantorRestriction)description).getRole(),c));
			}		
			
			// rule 2: ALL r.D => ALL r.BOTTOM if D is a most specific atomic concept
			if(description.getChild(0) instanceof NamedClass && tmp.size()==0) {
				refinements.add(new ObjectAllRestriction(((ObjectQuantorRestriction)description).getRole(),new Nothing()));
			}
			
			// rule 3: ALL r.D => ALL s.D or ALL r^-1.D => ALL s^-1.D
			// currently inverse roles are not supported
			ObjectProperty ar = (ObjectProperty) role;
			Set<ObjectProperty> moreSpecialRoles = rs.getMoreSpecialRoles(ar);
			for(ObjectProperty moreSpecialRole : moreSpecialRoles) {
				refinements.add(new ObjectAllRestriction(moreSpecialRole, description.getChild(0)));
			}
			
			// rule 4: ALL r.D => <= (maxFillers-1) r.D
			// (length increases by 1 so we have to check whether max length is sufficient)
			// => commented out because this is acutally not a downward refinement
//			if(useCardinalityRestrictions) {
//				if(maxLength > description.getLength() && maxNrOfFillers.get(ar)>1) {
//					ObjectMaxCardinalityRestriction max = new ObjectMaxCardinalityRestriction(maxNrOfFillers.get(ar)-1,role,description.getChild(0));
//					refinements.add(max);
//				}
//			}
		} else if (description instanceof ObjectCardinalityRestriction) {
			ObjectPropertyExpression role = ((ObjectCardinalityRestriction)description).getRole();
			Description range = opRanges.get(role);	
			int number = ((ObjectCardinalityRestriction)description).getCardinality();
			if(description instanceof ObjectMaxCardinalityRestriction) {
				// rule 1: <= x r.C =>  <= x r.D
				tmp = refine(description.getChild(0), maxLength-3, null, range);

				for(Description d : tmp) {
					refinements.add(new ObjectMaxCardinalityRestriction(number,role,d));
				}				
				
				// rule 2: <= x r.C  =>  <= (x-1) r.C
				ObjectMaxCardinalityRestriction max = (ObjectMaxCardinalityRestriction) description;
//				int number = max.getNumber();
				if(number > 1)
					refinements.add(new ObjectMaxCardinalityRestriction(number-1,max.getRole(),max.getChild(0)));
				
			} else if(description instanceof ObjectMinCardinalityRestriction) {
				tmp = refine(description.getChild(0), maxLength-3, null, range);

				for(Description d : tmp) {
					refinements.add(new ObjectMinCardinalityRestriction(number,role,d));
				}
				
				// >= x r.C  =>  >= (x+1) r.C
				ObjectMinCardinalityRestriction min = (ObjectMinCardinalityRestriction) description;
//				int number = min.getNumber();
				if(number < maxNrOfFillers.get(min.getRole()))
					refinements.add(new ObjectMinCardinalityRestriction(number+1,min.getRole(),min.getChild(0)));				
			}
		} else if (description instanceof DatatypeSomeRestriction) {
			
			DatatypeSomeRestriction dsr = (DatatypeSomeRestriction) description;
			DatatypeProperty dp = (DatatypeProperty) dsr.getRestrictedPropertyExpression();
			DataRange dr = dsr.getDataRange();
			if(dr instanceof DoubleMaxValue) {
				double value = ((DoubleMaxValue)dr).getValue();
				// find out which split value was used
				int splitIndex = splits.get(dp).lastIndexOf(value);
				if(splitIndex == -1)
					throw new Error("split error");
				int newSplitIndex = splitIndex - 1;
				if(newSplitIndex >= 0) {
					DoubleMaxValue max = new DoubleMaxValue(splits.get(dp).get(newSplitIndex));
					DatatypeSomeRestriction newDSR = new DatatypeSomeRestriction(dp,max);
					refinements.add(newDSR);
//					System.out.println(description + " => " + newDSR);
				}
			} else if(dr instanceof DoubleMinValue) {
				double value = ((DoubleMinValue)dr).getValue();
				// find out which split value was used
				int splitIndex = splits.get(dp).lastIndexOf(value);
				if(splitIndex == -1)
					throw new Error("split error");
				int newSplitIndex = splitIndex + 1;
				if(newSplitIndex < splits.get(dp).size()) {
					DoubleMinValue min = new DoubleMinValue(splits.get(dp).get(newSplitIndex));
					DatatypeSomeRestriction newDSR = new DatatypeSomeRestriction(dp,min);
					refinements.add(newDSR);
				}
			}
		}
		
		// if a refinement is not Bottom, Top, ALL r.Bottom a refinement of top can be appended
		if(!(description instanceof Thing) && !(description instanceof Nothing) 
				&& !(description instanceof ObjectAllRestriction && description.getChild(0) instanceof Nothing)) {
			// -1 because of the AND symbol which is appended
			int topRefLength = maxLength - description.getLength() - 1; 
			
			// maybe we have to compute new top refinements here
			if(currDomain instanceof Thing) {
				if(topRefLength > topRefinementsLength)
					computeTopRefinements(topRefLength);
			} else if(topRefLength > topARefinementsLength.get(currDomain))
				computeTopRefinements(topRefLength,(NamedClass)currDomain);
			
			if(topRefLength>0) {
				Set<Description> topRefs;
				if(currDomain instanceof Thing)
					topRefs = topRefinementsCumulative.get(topRefLength);
				else
					topRefs = topARefinementsCumulative.get(currDomain).get(topRefLength);
				
				for(Description c : topRefs) {
					// true if refinement should be skipped due to filters,
					// false otherwise
					boolean skip = false;
					
					// if a refinement of of the form ALL r, we check whether ALL r
					// does not occur already
					if(applyAllFilter) {
						if(c instanceof ObjectAllRestriction) {
							for(Description child : description.getChildren()) {
								if(child instanceof ObjectAllRestriction) {
									ObjectPropertyExpression r1 = ((ObjectAllRestriction)c).getRole();
									ObjectPropertyExpression r2 = ((ObjectAllRestriction)child).getRole();
									if(r1.toString().equals(r2.toString()))
										skip = true;
								}
							}
						}
					}
					
					// check for double datatype properties
					/*
					if(c instanceof DatatypeSomeRestriction && 
							description instanceof DatatypeSomeRestriction) {
						DataRange dr = ((DatatypeSomeRestriction)c).getDataRange();
						DataRange dr2 = ((DatatypeSomeRestriction)description).getDataRange();
						// it does not make sense to have statements like height >= 1.8 AND height >= 1.7
						if((dr instanceof DoubleMaxValue && dr2 instanceof DoubleMaxValue)
							||(dr instanceof DoubleMinValue && dr2 instanceof DoubleMinValue))
							skip = true;
					}*/
					
					// perform a disjointness check when named classes are added;
					// this can avoid a lot of superfluous computation in the algorithm e.g.
					// when A1 looks good, so many refinements of the form (A1 OR (A2 AND A3))
					// are generated which are all equal to A1 due to disjointness of A2 and A3
					if(disjointChecks && c instanceof NamedClass && description instanceof NamedClass && isDisjoint(description, c)) {
						skip = true;
//						System.out.println(c + " ignored when refining " + description);
					}	
					
					if(!skip) {
						Intersection mc = new Intersection();
						mc.addChild(description);
						mc.addChild(c);				
						
						// clean and transform to ordered negation normal form
						ConceptTransformation.cleanConceptNonRecursive(mc);
						ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(mc, conceptComparator);
						
						// last check before intersection is added
						if(checkIntersection(mc))
							refinements.add(mc);
					}
				}
			}
		}
		
//		for(Description refinement : refinements) {
//			if((refinement instanceof Intersection || refinement instanceof Union) && refinement.getChildren().size()<2) {
//				System.out.println(description + " " + refinement + " " + currDomain + " " + maxLength);
//				System.exit(0);
//			}
//		}
		
		return refinements;		
	}
	
	// when a child of an intersection is refined and reintegrated into the
	// intersection, we can perform some sanity checks;
	// method returns true if everything is OK and false otherwise
	// TODO: can be implemented more efficiently if the newly added child
	// is given as parameter
	public static boolean checkIntersection(Intersection intersection) {
		// rule 1: max. restrictions at most once
		boolean maxDoubleOccurence = false;
		// rule 2: min restrictions at most once
		boolean minDoubleOccurence = false;
		// rule 3: no double boolean datatypes
		TreeSet<DatatypeProperty> occuredDP = new TreeSet<DatatypeProperty>();
		for(Description child : intersection.getChildren()) {
			if(child instanceof DatatypeSomeRestriction) {
				DataRange dr = ((DatatypeSomeRestriction)child).getDataRange();
				if(dr instanceof DoubleMaxValue) {
					if(maxDoubleOccurence)
						return false;
					else
						maxDoubleOccurence = true;
				} else if(dr instanceof DoubleMinValue) {
					if(minDoubleOccurence)
						return false;
					else
						minDoubleOccurence = true;
				}		
			} else if(child instanceof BooleanValueRestriction) {
				DatatypeProperty dp = (DatatypeProperty) ((BooleanValueRestriction)child).getRestrictedPropertyExpression();
//				System.out.println("dp: " + dp);
				// return false if the boolean property exists already
				if(!occuredDP.add(dp))
					return false;
			}
//			System.out.println(child.getClass());
		}
		return true;
	}
	
	private void computeTopRefinements(int maxLength) {
		computeTopRefinements(maxLength, null);
	}
	
	private void computeTopRefinements(int maxLength, NamedClass domain) {
		long topComputationTimeStartNs = System.nanoTime();
		
		if(domain == null && m.size() == 0)
			computeM();
		
		if(domain != null && !mA.containsKey(domain))
			computeM(domain);
		
		int refinementsLength;
		
		if(domain == null) {
			refinementsLength = topRefinementsLength;
		} else {
			if(!topARefinementsLength.containsKey(domain))
				topARefinementsLength.put(domain,0);

			refinementsLength = topARefinementsLength.get(domain);
		}

		// compute all possible combinations of the disjunction
		for(int i = refinementsLength+1; i <= maxLength; i++) {
			combos.put(i,MathOperations.getCombos(i, mMaxLength));

			// initialise the refinements with empty sets
			if(domain == null) {
				topRefinements.put(i, new TreeSet<Description>(conceptComparator));
			} else {
				if(!topARefinements.containsKey(domain))
					topARefinements.put(domain, new TreeMap<Integer,SortedSet<Description>>());
				topARefinements.get(domain).put(i, new TreeSet<Description>(conceptComparator));
			}
				
			for(List<Integer> combo : combos.get(i)) {
				
				// combination is a single number => try to use M
				if(combo.size()==1) {
					// note we cannot use "put" instead of "addAll" because there
					// can be several combos for one length
					if(domain == null)
						topRefinements.get(i).addAll(m.get(i));
					else
						topARefinements.get(domain).get(i).addAll(mA.get(domain).get(i));
				// combinations has several numbers => generate disjunct
				} else {
					
					// check whether the combination makes sense, i.e. whether
					// all lengths mentioned in it have corresponding elements
					// e.g. when negation is deactivated there won't be elements of
					// length 2 in M
					boolean validCombo = true;
					for(Integer j : combo) {
						if((domain == null && m.get(j).size()==0) || 
								(domain != null && mA.get(domain).get(j).size()==0))
							validCombo = false;
					}
					
					if(validCombo) {
						
						SortedSet<Union> baseSet = new TreeSet<Union>(conceptComparator);
						for(Integer j : combo) {
							if(domain == null)
								baseSet = MathOperations.incCrossProduct(baseSet, m.get(j));
							else
								baseSet = MathOperations.incCrossProduct(baseSet, mA.get(domain).get(j));
						}
						
						// convert all concepts in ordered negation normal form
						for(Description concept : baseSet) {
							ConceptTransformation.transformToOrderedNegationNormalForm(concept, conceptComparator);
						}
						
						// apply the exists filter (throwing out all refinements with
						// double \exists r for any r)
						// TODO: similar filtering can be done for boolean datatype
						// properties
						if(applyExistsFilter) {
							Iterator<Union> it = baseSet.iterator();
							while(it.hasNext()) {
								if(MathOperations.containsDoubleObjectSomeRestriction(it.next()))
									it.remove();							
							}
						}
							
						// add computed refinements
						if(domain == null)
							topRefinements.get(i).addAll(baseSet);
						else
							topARefinements.get(domain).get(i).addAll(baseSet);
						
					}
				}
			}
			
			// create cumulative versions of refinements such that they can
			// be accessed easily
			TreeSet<Description> cumulativeRefinements = new TreeSet<Description>(conceptComparator);
			for(int j=1; j<=i; j++) {
				if(domain == null) {
					cumulativeRefinements.addAll(topRefinements.get(j));
				} else {
					cumulativeRefinements.addAll(topARefinements.get(domain).get(j));
				}
			}			
			
			if(domain == null) {
				topRefinementsCumulative.put(i, cumulativeRefinements);
			} else {
				if(!topARefinementsCumulative.containsKey(domain))
					topARefinementsCumulative.put(domain, new TreeMap<Integer, TreeSet<Description>>());
				topARefinementsCumulative.get(domain).put(i, cumulativeRefinements);
			}
		}
		
		// register new top refinements length
		if(domain == null)
			topRefinementsLength = maxLength;
		else
			topARefinementsLength.put(domain,maxLength);
		
		topComputationTimeNs += System.nanoTime() - topComputationTimeStartNs;
	}
	
	// compute M_\top
	private void computeM() {
		long mComputationTimeStartNs = System.nanoTime();

		// initialise all possible lengths (1 to 3)
		for(int i=1; i<=mMaxLength; i++) {
			m.put(i, new TreeSet<Description>(conceptComparator));
		}
		
		SortedSet<Description> m1 = rs.getMoreSpecialConcepts(new Thing()); 
		m.put(1,m1);		
		
		SortedSet<Description> m2 = new TreeSet<Description>(conceptComparator);
		if(useNegation) {
			Set<Description> m2tmp = rs.getMoreGeneralConcepts(new Nothing());
			for(Description c : m2tmp) {
				m2.add(new Negation(c));
			}
		}
		
		// boolean datatypes, e.g. testPositive = true
		if(useBooleanDatatypes) {
			Set<DatatypeProperty> booleanDPs = rs.getBooleanDatatypeProperties();
			for(DatatypeProperty dp : booleanDPs) {
				m2.add(new BooleanValueRestriction(dp,true));
				m2.add(new BooleanValueRestriction(dp,false));
			}
		}		
		m.put(2,m2);
			
		SortedSet<Description> m3 = new TreeSet<Description>(conceptComparator);
		if(useExistsConstructor) {
			// only uses most general roles
			for(ObjectProperty r : rs.getMostGeneralRoles()) {
				m3.add(new ObjectSomeRestriction(r, new Thing()));
			}				
		}
		
		if(useAllConstructor) {
			// we allow \forall r.\top here because otherwise the operator
			// becomes too difficult to manage due to dependencies between
			// M_A and M_A' where A'=ran(r)
			for(ObjectProperty r : rs.getMostGeneralRoles()) {
				m3.add(new ObjectAllRestriction(r, new Thing()));
			}				
		}		
		
		if(useDoubleDatatypes) {
			Set<DatatypeProperty> doubleDPs = rs.getDoubleDatatypeProperties();
			for(DatatypeProperty dp : doubleDPs) {
				DoubleMaxValue max = new DoubleMaxValue(splits.get(dp).get(splits.get(dp).size()-1));
				DoubleMinValue min = new DoubleMinValue(splits.get(dp).get(0));
				m3.add(new DatatypeSomeRestriction(dp,max));
				m3.add(new DatatypeSomeRestriction(dp,min));
			}
		}		
		
		m.put(3,m3);
		
		SortedSet<Description> m4 = new TreeSet<Description>(conceptComparator);
		if(useCardinalityRestrictions) {
			for(ObjectProperty r : rs.getMostGeneralRoles()) {
				int maxFillers = maxNrOfFillers.get(r);
				m4.add(new ObjectMaxCardinalityRestriction(maxFillers-1, r, new Thing()));
			}			
		}
		m.put(4,m4);
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
	
	// computation of the set M_A
	// a major difference compared to the ILP 2007 \rho operator is that
	// M is finite and contains elements of length (currently) at most 3
	private void computeM(NamedClass nc) {
		long mComputationTimeStartNs = System.nanoTime();

		mA.put(nc, new TreeMap<Integer,SortedSet<Description>>());
		// initialise all possible lengths (1 to 3)
		for(int i=1; i<=mMaxLength; i++) {
			mA.get(nc).put(i, new TreeSet<Description>(conceptComparator));
		}
		
		SortedSet<Description> m1 = rs.getMoreSpecialConcepts(nc); 
		mA.get(nc).put(1,m1);
		
		SortedSet<Description> m2 = new TreeSet<Description>(conceptComparator);
		if(useNegation) {
			// the definition in the paper is more complex, but acutally
			// we only have to insert the most specific concepts satisfying
			// the mentioned restrictions; there is no need to implement a
			// recursive method because for A subClassOf A' we have not A'
			// subClassOf A and thus: if A and B are disjoint then also A'
			// and B; if not A AND B = B then also not A' AND B = B
			SortedSet<Description> m2tmp = rs.getMoreGeneralConcepts(new Nothing());
			
			for(Description c : m2tmp) {
				if(c instanceof Thing)
					m2.add(c);
				else {
					NamedClass a = (NamedClass) c;
					if(!isNotADisjoint(a, nc) && isNotAMeaningful(a, nc))
						m2.add(new Negation(a));
				}
			}	
		}
		
		// compute applicable properties
		computeMg(nc);		
		
		// boolean datatypes, e.g. testPositive = true
		if(useBooleanDatatypes) {
			Set<DatatypeProperty> booleanDPs = mgbd.get(nc);
			for(DatatypeProperty dp : booleanDPs) {
				m2.add(new BooleanValueRestriction(dp,true));
				m2.add(new BooleanValueRestriction(dp,false));
			}
		}
		
		mA.get(nc).put(2,m2);
			
		SortedSet<Description> m3 = new TreeSet<Description>(conceptComparator);
		if(useExistsConstructor) {
			for(ObjectProperty r : mgr.get(nc)) {
				m3.add(new ObjectSomeRestriction(r, new Thing()));
			}				
		}
		
		if(useAllConstructor) {
			// we allow \forall r.\top here because otherwise the operator
			// becomes too difficult to manage due to dependencies between
			// M_A and M_A' where A'=ran(r)
			for(ObjectProperty r : mgr.get(nc)) {
				m3.add(new ObjectAllRestriction(r, new Thing()));
			}				
		}		
		
		if(useDoubleDatatypes) {
			Set<DatatypeProperty> doubleDPs = mgdd.get(nc);
//			System.out.println("cached disjoints " + cachedDisjoints);
//			System.out.println("appOP " + appOP);
//			System.out.println("appBD " + appBD);
//			System.out.println("appDD " + appDD);
//			System.out.println("mgr " + mgr);
//			System.out.println("mgbd " + mgbd);
//			System.out.println("mgdd " + mgdd);
			
			for(DatatypeProperty dp : doubleDPs) {
				DoubleMaxValue max = new DoubleMaxValue(splits.get(dp).get(splits.get(dp).size()-1));
				DoubleMinValue min = new DoubleMinValue(splits.get(dp).get(0));
				m3.add(new DatatypeSomeRestriction(dp,max));
				m3.add(new DatatypeSomeRestriction(dp,min));
			}
		}			
		
		mA.get(nc).put(3,m3);
		
		SortedSet<Description> m4 = new TreeSet<Description>(conceptComparator);
		if(useCardinalityRestrictions) {
			for(ObjectProperty r : mgr.get(nc)) {
				int maxFillers = maxNrOfFillers.get(r);
				m4.add(new ObjectMaxCardinalityRestriction(maxFillers-1, r, new Thing()));
			}
		}
		mA.get(nc).put(4,m4);
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
		
	private void computeMg(NamedClass domain) {
		// compute the applicable properties if this has not been done yet
		if(appOP.get(domain) == null)
			computeApp(domain);	
		
		// initialise mgr, mgbd, mgdd
		mgr.put(domain, new TreeSet<ObjectProperty>());
		mgbd.put(domain, new TreeSet<DatatypeProperty>());
		mgdd.put(domain, new TreeSet<DatatypeProperty>());
		
		SortedSet<ObjectProperty> mostGeneral = rs.getMostGeneralRoles();
		computeMgrRecursive(domain, mostGeneral, mgr.get(domain));
		SortedSet<DatatypeProperty> mostGeneralDP = rs.getMostGeneralDatatypeProperties();
		// we make the (reasonable) assumption here that all sub and super
		// datatype properties have the same type (e.g. boolean, integer, double)
		Set<DatatypeProperty> mostGeneralBDP = Helper.intersection(mostGeneralDP, rs.getBooleanDatatypeProperties());
		Set<DatatypeProperty> mostGeneralDDP = Helper.intersection(mostGeneralDP, rs.getDoubleDatatypeProperties());
		computeMgbdRecursive(domain, mostGeneralBDP, mgbd.get(domain));	
		computeMgddRecursive(domain, mostGeneralDDP, mgdd.get(domain));
	}
	
	private void computeMgrRecursive(NamedClass domain, Set<ObjectProperty> currProperties, Set<ObjectProperty> mgrTmp) {
		for(ObjectProperty prop : currProperties) {
			if(appOP.get(domain).contains(prop))
				mgrTmp.add(prop);
			else
				computeMgrRecursive(domain, rs.getMoreSpecialRoles(prop), mgrTmp);
		}
	}
	
	private void computeMgbdRecursive(NamedClass domain, Set<DatatypeProperty> currProperties, Set<DatatypeProperty> mgbdTmp) {
		for(DatatypeProperty prop : currProperties) {
			if(appBD.get(domain).contains(prop))
				mgbdTmp.add(prop);
			else
				computeMgbdRecursive(domain, rs.getMoreSpecialDatatypeProperties(prop), mgbdTmp);
		}
	}	
	
	private void computeMgddRecursive(NamedClass domain, Set<DatatypeProperty> currProperties, Set<DatatypeProperty> mgddTmp) {
		for(DatatypeProperty prop : currProperties) {
			if(appDD.get(domain).contains(prop))
				mgddTmp.add(prop);
			else
				computeMgddRecursive(domain, rs.getMoreSpecialDatatypeProperties(prop), mgddTmp);
		}
	}		
	
	// computes the set of applicable properties for a given class
	private void computeApp(NamedClass domain) {
		// object properties
		Set<ObjectProperty> mostGeneral = rs.getAtomicRoles();
		Set<ObjectProperty> applicableRoles = new TreeSet<ObjectProperty>();
		for(ObjectProperty role : mostGeneral) {
			// TODO: currently we just rely on named classes as roles,
			// instead of computing dom(r) and ran(r)
			Description d = rs.getDomain(role);
			if(!isDisjoint(domain,d))
				applicableRoles.add(role);
		}
		appOP.put(domain, applicableRoles);
		
		// boolean datatype properties
		Set<DatatypeProperty> mostGeneralBDPs = rs.getBooleanDatatypeProperties();
		Set<DatatypeProperty> applicableBDPs = new TreeSet<DatatypeProperty>();
		for(DatatypeProperty role : mostGeneralBDPs) {
			Description d = (NamedClass) rs.getDomain(role);
			if(!isDisjoint(domain,d))
				applicableBDPs.add(role);
		}
		appBD.put(domain, applicableBDPs);	
		
		// double datatype properties
		Set<DatatypeProperty> mostGeneralDDPs = rs.getDoubleDatatypeProperties();
		Set<DatatypeProperty> applicableDDPs = new TreeSet<DatatypeProperty>();
		for(DatatypeProperty role : mostGeneralDDPs) {
			Description d = (NamedClass) rs.getDomain(role);
//			System.out.println("domain: " + d);
			if(!isDisjoint(domain,d))
				applicableDDPs.add(role);
		}
		appDD.put(domain, applicableDDPs);			
	}
	
	// returns true of the intersection contains elements disjoint
	// to the given description (if true adding the description to
	// the intersection results in a description equivalent to bottom)
	// e.g. OldPerson AND YoungPerson; Nitrogen-34 AND Tin-113
	// Note: currently we only check named classes in the intersection,
	// it would be interesting to see whether it makes sense to extend this
	// (advantage: less refinements, drawback: operator will need infinitely many
	// reasoner queries in the long run)
	@SuppressWarnings({"unused"})
	private boolean containsDisjoints(Intersection intersection, Description d) {
		List<Description> children = intersection.getChildren();
		for(Description child : children) {
			if(d instanceof Nothing)
				return true;
			else if(child instanceof NamedClass) {
				if(isDisjoint((NamedClass)child, d))
					return true;
			}
		}
		return false;
	}
	
	private boolean isDisjoint(Description d1, Description d2) {
		
//		System.out.println("| " + d1 + " " + d2);
//		System.out.println("| " + cachedDisjoints);
		
		// check whether we have cached this query
		Map<Description,Boolean> tmp = cachedDisjoints.get(d1);
		Boolean tmp2 = null;
		if(tmp != null)
			tmp2 = tmp.get(d2);
		
//		System.out.println("| " + tmp + " " + tmp2);
		
		if(tmp2==null) {
			Boolean result;
			if(instanceBasedDisjoints) {
				result = isDisjointInstanceBased(d1,d2);
			} else {
				Description d = new Intersection(d1, d2);
				result = rs.subsumes(new Nothing(), d);				
			}
			// add the result to the cache (we add it twice such that
			// the order of access does not matter)
			
//			System.out.println("| result: " + result);
			
			// create new entries if necessary
			Map<Description,Boolean> map1 = new TreeMap<Description,Boolean>(conceptComparator);
			Map<Description,Boolean> map2 = new TreeMap<Description,Boolean>(conceptComparator);
			if(tmp == null)
				cachedDisjoints.put(d1, map1);
			if(!cachedDisjoints.containsKey(d2))
				cachedDisjoints.put(d2, map2);
			
			// add result symmetrically in the description matrix
			cachedDisjoints.get(d1).put(d2, result);
			cachedDisjoints.get(d2).put(d1, result);
//			System.out.println("---");
			return result;
		} else {
//			System.out.println("===");
			return tmp2;
		}
	}	
	
	private boolean isDisjointInstanceBased(Description d1, Description d2) {
		SortedSet<Individual> d1Instances = rs.retrieval(d1);
		SortedSet<Individual> d2Instances = rs.retrieval(d2);
//		System.out.println(d1 + " " + d2);
//		System.out.println(d1 + " " + d1Instances);
//		System.out.println(d2 + " " + d2Instances);
		for(Individual d1Instance : d1Instances) {
			if(d2Instances.contains(d1Instance))
				return false;
		}
		return true;
	}
	
	/*
	// computes whether two classes are disjoint; this should be computed
	// by the reasoner only ones and otherwise taken from a matrix
	private boolean isDisjoint(NamedClass a, Description d) {
		// we need to test whether A AND B is equivalent to BOTTOM
		Description d2 = new Intersection(a, d);
		return rs.subsumes(new Nothing(), d2);
	}*/
	
	// we need to test whether NOT A AND B is equivalent to BOTTOM
	private boolean isNotADisjoint(NamedClass a, NamedClass b) {
//		Map<NamedClass,Boolean> tmp = notABDisjoint.get(a);
//		Boolean tmp2 = null;
//		if(tmp != null)
//			tmp2 = tmp.get(b);
//		
//		if(tmp2==null) {
			Description notA = new Negation(a);
			Description d = new Intersection(notA, b);
			Boolean result = rs.subsumes(new Nothing(), d);
			// ... add to cache ...
			return result;
//		} else
//			return tmp2;
	}
	
	// we need to test whether NOT A AND B = B
	// (if not then NOT A is not meaningful in the sense that it does
	// not semantically add anything to B) 	
	private boolean isNotAMeaningful(NamedClass a, NamedClass b) {
		Description notA = new Negation(a);
		Description d = new Intersection(notA, b);
		// check b subClassOf b AND NOT A (if yes then it is not meaningful)
		return !rs.subsumes(d, b);
	}
	
	private void computeSplits(DatatypeProperty dp) {
		Set<Double> valuesSet = new TreeSet<Double>();
//		Set<Individual> individuals = rs.getIndividuals();
		Map<Individual,SortedSet<Double>> valueMap = rs.getDoubleDatatypeMembers(dp);
		// add all values to the set (duplicates will be remove automatically)
		for(Entry<Individual,SortedSet<Double>> e : valueMap.entrySet())
			valuesSet.addAll(e.getValue());
		// convert set to a list where values are sorted
		List<Double> values = new LinkedList<Double>(valuesSet);
		Collections.sort(values);
		
		int nrOfValues = values.size();
		// create split set
		List<Double> splitsDP = new LinkedList<Double>();
		for(int splitNr=0; splitNr < Math.min(maxNrOfSplits,nrOfValues-1); splitNr++) {
			int index;
			if(nrOfValues<=maxNrOfSplits)
				index = splitNr;
			else
				index = (int) Math.floor(splitNr * (double)nrOfValues/(maxNrOfSplits+1));
			
			double value = 0.5*(values.get(index)+values.get(index+1));
			splitsDP.add(value);
		}
		splits.put(dp, splitsDP);
		
//		System.out.println(values);
//		System.out.println(splits);
//		System.exit(0);
	}
}