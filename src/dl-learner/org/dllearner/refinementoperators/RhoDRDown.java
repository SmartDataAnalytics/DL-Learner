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

import org.dllearner.algorithms.refinement.RefinementOperator;
import org.dllearner.core.ReasoningService;
import org.dllearner.core.owl.BooleanValueRestriction;
import org.dllearner.core.owl.DatatypeProperty;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Intersection;
import org.dllearner.core.owl.NamedClass;
import org.dllearner.core.owl.Negation;
import org.dllearner.core.owl.Nothing;
import org.dllearner.core.owl.ObjectAllRestriction;
import org.dllearner.core.owl.ObjectProperty;
import org.dllearner.core.owl.ObjectPropertyExpression;
import org.dllearner.core.owl.ObjectQuantorRestriction;
import org.dllearner.core.owl.ObjectSomeRestriction;
import org.dllearner.core.owl.SubsumptionHierarchy;
import org.dllearner.core.owl.Thing;
import org.dllearner.core.owl.Union;
import org.dllearner.utilities.ConceptComparator;
import org.dllearner.utilities.ConceptTransformation;
import org.dllearner.utilities.Helper;

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

	private ReasoningService rs;
	
	// hierarchies
	private SubsumptionHierarchy subHierarchy;
	
	// domains and ranges
	private Map<ObjectProperty,Description> opDomains = new TreeMap<ObjectProperty,Description>();
	private Map<DatatypeProperty,Description> dpDomains = new TreeMap<DatatypeProperty,Description>();
	private Map<ObjectProperty,Description> opRanges = new TreeMap<ObjectProperty,Description>();
	
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
	private static int mMaxLength = 3;
	
	// the sets M_\top and M_A
	private Map<Integer,SortedSet<Description>> m = new TreeMap<Integer,SortedSet<Description>>();
	private Map<NamedClass,Map<Integer,SortedSet<Description>>> mA = new TreeMap<NamedClass,Map<Integer,SortedSet<Description>>>();
	
	// @see MathOperations.getCombos
	private Map<Integer, List<List<Integer>>> combos = new HashMap<Integer, List<List<Integer>>>();

	// refinements of the top concept ordered by length
	private Map<Integer, SortedSet<? extends Description>> topRefinements = new TreeMap<Integer, SortedSet<? extends Description>>();
	private Map<NamedClass,Map<Integer, SortedSet<? extends Description>>> topARefinements = new TreeMap<NamedClass,Map<Integer, SortedSet<? extends Description>>>();
	
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
	
	// comparator für Konzepte
	private ConceptComparator conceptComparator = new ConceptComparator();
	
	// Statistik
	private long mComputationTimeNs = 0;
	private long topComputationTimeNs = 0;
	
	private boolean applyAllFilter = true;
	private boolean applyExistsFilter = true;
	private boolean useAllConstructor = true;
	private boolean useExistsConstructor = true;
	private boolean useNegation = true;
	private boolean useBooleanDatatypes = true;	
	
	public RhoDRDown(ReasoningService rs) {
		this(rs, null);
	}
	
	public RhoDRDown(ReasoningService rs, NamedClass startClass) {
		this.rs = rs;
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
		// TODO: check whether using list or set makes more sense 
		// here; and whether HashSet or TreeSet should be used
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
					computeTopRefinements(maxLength);
				refinements = (TreeSet<Description>) topRefinementsCumulative.get(maxLength).clone();					
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
					
					refinements.add(mc);	
				}
				
			}
				
		} else if (description instanceof Union) {
			// refine one of the elements
			for(Description child : description.getChildren()) {
				
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
			
		}
		
		// if a refinement is neither Bottom nor Top a refinement of top can be appended
		if(!(description instanceof Thing) && !(description instanceof Nothing)) {
			// -1 because of the AND symbol which is appended
			int topRefLength = maxLength - description.getLength() - 1; 
			
			// maybe we have to compute new top refinements here
			if(currDomain instanceof Thing && topRefLength > topRefinementsLength)
				computeTopRefinements(topRefLength);
			else if(topRefLength > topARefinementsLength.get(currDomain))
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
					
					if(!skip) {
						Intersection mc = new Intersection();
						mc.addChild(description);
						mc.addChild(c);				
						
						// clean and transform to ordered negation normal form
						ConceptTransformation.cleanConceptNonRecursive(mc);
						ConceptTransformation.transformToOrderedNegationNormalFormNonRecursive(mc, conceptComparator);
											
						refinements.add(mc);
					}
				}
			}
		}
		
		return refinements;		
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
		
		// compute all possible combinations of the disjunction
		int refinementsLength = (domain == null) ? topRefinementsLength : topARefinementsLength.get(domain);
		for(int i = refinementsLength+1; i <= maxLength; i++) {
			combos.put(i,MathOperations.getCombos(i, mMaxLength));

			for(List<Integer> combo : combos.get(i)) {
				
				// combination is a single number => try to use M
				if(combo.size()==1) {
					if(domain == null)
						topRefinements.put(i,m.get(i));
					else
						topARefinements.get(domain).put(i,mA.get(domain).get(i));
				// combinations has several numbers => generate disjunct
				} else {
					SortedSet<Union> baseSet = new TreeSet<Union>(conceptComparator);
					for(Integer j : combo) {
						baseSet = MathOperations.incCrossProduct(baseSet, m.get(j));
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
						topRefinements.put(new Integer(i), baseSet);
					else
						topARefinements.get(domain).put(new Integer(i), baseSet);
				}
			}
			
			// create cumulative versions of refinements such that they can
			// be accessed easily
			TreeSet<Description> cumulativeRefinements = new TreeSet<Description>(conceptComparator);
			for(int j=1; j<=i; j++) {
				if(domain == null)
					cumulativeRefinements.addAll(topRefinements.get(j));
				else
					cumulativeRefinements.addAll(topARefinements.get(domain).get(j));
			}			
			if(domain == null)
				topRefinementsCumulative.put(i, cumulativeRefinements);
			else
				topARefinementsCumulative.get(domain).put(i, cumulativeRefinements);
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
		for(int i=1; i<=3; i++) {
			m.put(i, new TreeSet<Description>(conceptComparator));
		}
		
		SortedSet<Description> m1 = rs.getMoreSpecialConcepts(new Thing()); 
		m.put(1,m1);		
		
		if(useNegation) {
			Set<Description> m2tmp = rs.getMoreGeneralConcepts(new Nothing());
			SortedSet<Description> m2 = new TreeSet<Description>(conceptComparator);
			for(Description c : m2tmp) {
				m2.add(new Negation(c));
			}
			m.put(2,m2);
		}
			
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
		
		// boolean datatypes, e.g. testPositive = true
		if(useBooleanDatatypes) {
			Set<DatatypeProperty> booleanDPs = rs.getBooleanDatatypeProperties();
			for(DatatypeProperty dp : booleanDPs) {
				m3.add(new BooleanValueRestriction(dp,true));
				m3.add(new BooleanValueRestriction(dp,false));
			}
		}
		
		m.put(3,m3);
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
	
	// computation of the set M_A
	// a major difference compared to the ILP 2007 \rho operator is that
	// M is finite and contains elements of length (currently) at most 3
	private void computeM(NamedClass nc) {
		long mComputationTimeStartNs = System.nanoTime();

		mA.put(nc, new TreeMap<Integer,SortedSet<Description>>());
		// initialise all possible lengths (1 to 3)
		for(int i=1; i<=3; i++) {
			mA.get(nc).put(i, new TreeSet<Description>(conceptComparator));
		}
		
		SortedSet<Description> m1 = rs.getMoreSpecialConcepts(nc); 
		m.put(1,m1);
		
		if(useNegation) {
			// the definition in the paper is more complex, but acutally
			// we only have to insert the most specific concepts satisfying
			// the mentioned restrictions; there is no need to implement a
			// recursive method because for A subClassOf A' we have not A'
			// subClassOf A and thus: if A and B are disjoint then also A'
			// and B; if not A AND B = B then also not A' AND B = B
			SortedSet<Description> m2tmp = rs.getMoreGeneralConcepts(new Nothing());
			SortedSet<Description> m2 = new TreeSet<Description>(conceptComparator);
			for(Description c : m2tmp) {
				if(c instanceof Thing)
					m2.add(c);
				else {
					NamedClass a = (NamedClass) c;
					if(!isNotADisjoint(a, nc) && !isNotAMeaningFul(a, nc))
						m2.add(new Negation(a));
				}
			}
			m.put(2,m2);
		}
			
		// compute applicable properties
		computeMg(nc);
		
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
		
		// boolean datatypes, e.g. testPositive = true
		if(useBooleanDatatypes) {
			Set<DatatypeProperty> booleanDPs = mgbd.get(nc);
			for(DatatypeProperty dp : booleanDPs) {
				m3.add(new BooleanValueRestriction(dp,true));
				m3.add(new BooleanValueRestriction(dp,false));
			}
		}
		
		m.put(3,m3);
		
		mComputationTimeNs += System.nanoTime() - mComputationTimeStartNs;
	}
		
	private void computeMg(NamedClass domain) {
		// compute the applicable properties if this has not been done yet
		if(appOP.get(domain) == null)
			computeApp(domain);	
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
			NamedClass nc = (NamedClass) rs.getDomain(role);
			if(!isDisjoint(domain,nc))
				applicableRoles.add(role);
		}
		appOP.put(domain, applicableRoles);
		
		// boolean datatype properties
		Set<DatatypeProperty> mostGeneralBDPs = rs.getBooleanDatatypeProperties();
		Set<DatatypeProperty> applicableBDPs = new TreeSet<DatatypeProperty>();
		for(DatatypeProperty role : mostGeneralBDPs) {
			NamedClass nc = (NamedClass) rs.getDomain(role);
			if(!isDisjoint(domain,nc))
				applicableBDPs.add(role);
		}
		appBD.put(domain, applicableBDPs);	
		
		// double datatype properties
		Set<DatatypeProperty> mostGeneralDDPs = rs.getBooleanDatatypeProperties();
		Set<DatatypeProperty> applicableDDPs = new TreeSet<DatatypeProperty>();
		for(DatatypeProperty role : mostGeneralDDPs) {
			NamedClass nc = (NamedClass) rs.getDomain(role);
			if(!isDisjoint(domain,nc))
				applicableDDPs.add(role);
		}
		appDD.put(domain, applicableDDPs);			
	}
	
	// computes whether two classes are disjoint; this should be computed
	// by the reasoner only ones and otherwise taken from a matrix
	// => this has low importance in the long run, because M is cached anyway,
	// but avoids many duplicate queries when computing M
	private boolean isDisjoint(NamedClass a, NamedClass b) {
		// we need to test whether A AND B is equivalent to BOTTOM
		Description d = new Intersection(a, b);
		return rs.subsumes(new Nothing(), d);
	}
	
	// we need to test whether NOT A AND B is equivalent to BOTTOM
	private boolean isNotADisjoint(NamedClass a, NamedClass b) {
		Description notA = new Negation(a);
		Description d = new Intersection(notA, b);
		return rs.subsumes(new Nothing(), d);
	}
	
	// we need to test whether NOT A AND B = B
	// (if not then NOT A is not meaningful in the sense that it does
	// not semantically add anything to B) 	
	private boolean isNotAMeaningFul(NamedClass a, NamedClass b) {
		Description notA = new Negation(a);
		Description d = new Intersection(notA, b);
		return !rs.subsumes(b, d);
	}	
	
}