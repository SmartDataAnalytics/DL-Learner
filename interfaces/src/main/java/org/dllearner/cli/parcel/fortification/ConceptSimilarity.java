package org.dllearner.cli.parcel.fortification;

import java.io.File;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.dllearner.algorithms.parcel.ParCELExtraNode;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.utilities.owl.OWLClassExpressionUtils;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

import org.semanticweb.owlapi.util.DefaultPrefixManager;
import uk.ac.manchester.cs.owl.owlapi.*;

import com.clarkparsia.pellet.owlapiv3.PelletReasoner;
import com.clarkparsia.pellet.owlapiv3.PelletReasonerFactory;



/**
 * This class implements methods for calculating the concept similarity.
 *  
 * @author An C. Tran
 *
 */
public class ConceptSimilarity { 

	private AbstractReasonerComponent reasoner;
	private Set<OWLIndividual> instances;
	
	private static Logger logger = Logger.getLogger(ConceptSimilarity.class);

	private static final OWLDataFactory df = OWLManager.getOWLDataFactory();
	
	public ConceptSimilarity() {
		this.reasoner = null;
	}
	
	
	
	public static double getConceptOverlapSimple(Set<OWLIndividual> coverC, Set<OWLIndividual> coverD) {
		
		//compute the intersection between C and D
		Set<OWLIndividual> intersection = new HashSet<>();
		intersection.addAll(coverC);
		intersection.retainAll(coverD);
		
		int commonInstances = intersection.size();
		int allInstances = coverC.size() + coverD.size() - commonInstances;
		
		double dissimilarity = commonInstances / (double)coverC.size();
		if (dissimilarity < (commonInstances / (double)coverD.size()))
			dissimilarity = commonInstances / (double)coverD.size();
		
		return commonInstances / (double)allInstances * dissimilarity;
	}

	
	public ConceptSimilarity(AbstractReasonerComponent reasoner, Set<OWLIndividual> instances) {
		this.reasoner = reasoner;
		this.instances = new HashSet<>();
		this.instances.addAll(instances);
	}
	
	/**
	 * Flatten disjunctive description into set of descriptions, e.g. A or (B and C) into {A, B and C}
	 * Note that this methods does not normalise the description. Therefore, it should be called after a normalisation call.
	 * 
	 * @param description Description to be flattened 
	 * 
	 * @return List of description in conjunctive normal form
	 */
	/*
	public static List<OWLClassExpression> disjunctiveNormalFormFlatten(OWLClassExpression description) {
		List<OWLClassExpression> result = new LinkedList<OWLClassExpression>();
		
		//check if the given description is in disjunctive normal form? return NULL if it is not
		if (!isDisjunctiveNormalForm(description))
			return null;
		
		if (description instanceof Union) {
			for (OWLClassExpression child : description.getChildren()) {
				result.addAll(disjunctiveNormalFormFlatten(child));
			}
		}
		else
			result.add(description);
		
		return result;
	}
	*/
	

	/**
	 * Check if a given description is in disjunctive normal form or not.
	 * 
	 * @param description Description to be checked
	 *  
	 * @return true if the given description is in disjunctive normal form, false otherwise
	 */
	public static boolean isDisjunctiveNormalForm(OWLClassExpression description) {
		
		if ((description instanceof OWLClass) || (description.isOWLThing()) || description.isOWLNothing())
			return true;
		else if (description instanceof OWLObjectComplementOfImpl)
			return isDisjunctiveNormalForm(((OWLObjectComplementOfImpl) description).getOperand());
		else if (description instanceof OWLObjectUnionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description))
				return isDisjunctiveNormalForm(child);
		}
		else if (description instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description))
				if (containDisjunction(child))
					return false;
			
			return true;
		}
		else if (description instanceof OWLRestriction) {

			if (((OWLRestriction) description).isDataRestriction() || (description instanceof OWLObjectHasValue))
				return true;

			return !(containDisjunction(((OWLQuantifiedObjectRestriction)description).getFiller()));
		}
		
		return false;
	}	//isDisjunctiveNormalForm
	
	
	/**
	 * Check if the given description contain disjunction or not. This method aims to support the disjunctive normal form checking
	 * 
	 * @param description Description to check
	 * 
	 * @return true if the given description contains disjunction, false otherwise  
	 */
	public static boolean containDisjunction(OWLClassExpression description) {
		if (OWLClassExpressionUtils.getLength(description) <= 2)
			return false;
		else if (description instanceof OWLObjectUnionOf)
			return true;
		else if (description instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description))
				if (containDisjunction(child))
					return true;
			return false;
		}
		else if (description instanceof OWLRestriction) {

			if (((OWLRestriction) description).isDataRestriction() || (description instanceof OWLObjectHasValue))
				return false;
			else { 	//object properties
				for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description))
					return containDisjunction(child);	
			}
		}
		return false;
	}	//containDisjunction
	
	
	
	/**
	 * Flatten a disjunctive normal form into list of descriptions, e.g. (A or B or (A and D)) --> {A, B, (A and D)}
	 * This method does not perform the normalisation. Therefore, it should be called after a disjunctive normalisation.
	 * 
	 * @param description
	 * @return
	 */
	public static List<OWLClassExpression> flattenDisjunctiveNormalDescription(OWLClassExpression description) {
		List<OWLClassExpression> result = new LinkedList<OWLClassExpression>();
		
		/*
		if (!isDisjunctiveNormalForm(description)) {
			System.out.println("**ERROR - " + description + " is not in disjunctive normal form");
			return null;
		}
		*/
		
		if (description instanceof OWLObjectUnionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
				if (child instanceof OWLObjectUnionOf)
					result.addAll(flattenDisjunctiveNormalDescription(child));
				else
					result.add(child);
			}	
		}
		else {
			result.add(description);
		}
			
		return result;
	}
	
	
	
	/**
	 * Normalise a description into disjunctive normal form
	 * 
	 * @param description Description to be normalised
	 * 
	 * @return Description in disjunctive normal form
	 */
	public static OWLClassExpression normalise(int level, OWLClassExpression description) {
		
		/*
		for (int i=0; i<level; i++)
			System.out.print("-");
		
		System.out.println("-normalise (l= " + description.getLength() + "): " + description);
		*/
		
		//class, Thing, Nothing
		if (!description.isAnonymous())
			return description;

		//Negation
		else if (description instanceof OWLObjectComplementOf) {
			OWLClassExpression norm = normalise(level+1, ((OWLObjectComplementOf) description).getOperand());
			
			if (norm instanceof OWLObjectIntersectionOf) {	//not(and(A, B, C)) = or(not(A), not(B), not(C))
				Set<OWLClassExpression> children = new HashSet<>();
				
				for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(norm))
					children.add(df.getOWLObjectComplementOf(child));
				return df.getOWLObjectUnionOf(children);
			}
			else if (norm instanceof OWLObjectUnionOf) {
				Set<OWLClassExpression> children = new HashSet<>();
				
				for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(norm))
					children.add(df.getOWLObjectComplementOf(child));
				return df.getOWLObjectIntersectionOf(children);
				
			}
			else 
				return df.getOWLObjectComplementOf(norm);
		} //negation
		
		//Union
		else if (description instanceof OWLObjectUnionOf) {	//A or B or C ...
			Set<OWLClassExpression> children = new HashSet<>();
			
			//normalise all description's children
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
				OWLClassExpression norm = normalise(level+1,child);
				
				if (norm instanceof OWLObjectUnionOf)
					children.addAll(OWLClassExpressionUtils.getChildren(norm));
				else
					children.add(norm);
			}
			
			return df.getOWLObjectUnionOf(children);
		} //union		
		
		//Intersection
		else if (description instanceof OWLObjectIntersectionOf) {	//A and B and C ...
			Set<OWLClassExpression> children = new HashSet<>();

			OWLClassExpression firstUnion = null;
			
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
				OWLClassExpression norm = normalise(level+1, child);
				
				if (norm instanceof OWLObjectIntersectionOf)	//A and B
					children.addAll(OWLClassExpressionUtils.getChildren(norm));
				else if (norm instanceof OWLObjectUnionOf) {
					
					//if the first Union is found, keep it for transformation: A and (B or C) = (A and B) or (A and C)
					if (firstUnion == null)		 
						firstUnion = norm;
					else 
						children.add(norm);
				}
				else
					children.add(norm);
			}	//for each child of the description
			
			if (firstUnion == null)
				return df.getOWLObjectIntersectionOf(children);
			else {	//transform: A and (B or C) ==> (A and B) or (A and C)				
				
				Set<OWLClassExpression> unionChildren = new HashSet<>();
				for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(firstUnion)) {
					Set<OWLClassExpression> tmp = new HashSet<>();	//contains Intersections
					tmp.add(child);
					tmp.addAll(children);
					
					unionChildren.add(df.getOWLObjectIntersectionOf(tmp));
				}
				
				return df.getOWLObjectUnionOf(unionChildren);	//normalise(level+1, df.getOWLObjectUnionOf(unionChildren));
			}
				
		} //intersection
		
		//restrictions
		else if (description instanceof OWLRestriction) {

			if (!(description instanceof OWLQuantifiedObjectRestriction))	//datatype property does not need to be normalised
				return description;		
			else { 	//object properties, normalise its Range
				//normalise the range of restriction and replace it with the normalised range
				if (OWLClassExpressionUtils.getChildren(description).size() == 0)
					logger.warn("**** ERROR: Restriction [" + description + "] has no child");

				OWLClassExpression newChild = normalise(level + 1, ((OWLQuantifiedObjectRestriction)description).getFiller());
				return OWLClassExpressionUtils.replaceFiller((OWLQuantifiedObjectRestriction)description, newChild);
			}
		}
		
		return null;
	} //normalise()
	
	
	/**
	 * Extract the atomic/primary concepts at the top level of a description
	 * Why List, not Set????
	 * 
	 * @param description
	 * @return
	 */
	public static List<OWLClassExpression> prim(OWLClassExpression description) {
		List<OWLClassExpression> result = new LinkedList<OWLClassExpression>();
		
		if (!description.isAnonymous())
			result.add(description);		
		else if (description instanceof OWLObjectComplementOf) {
			List<OWLClassExpression> primNegated = prim(((OWLObjectComplementOf) description).getOperand());
			if (primNegated.size() > 0)
				result.add(description);	//TODO: wrong here???
		}
		else if ((description instanceof OWLObjectIntersectionOf) || (description instanceof OWLObjectUnionOf)) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
			
				List<OWLClassExpression> tmp = prim(child);
				for (OWLClassExpression des : tmp)
					if (!result.contains(des))
							result.add(des);
			}
		}		
		
		return result;
	}

	public static Set<OWLClassExpression> primSet(OWLClassExpression description) {
		return new HashSet<>(prim(description));
	}
	
	/**
	 * Return a list of properties used in a given description.
	 * Why List, not Set???
	 *  
	 * @param description Description
	 * 
	 * @return List of properties in the description
	 */
	public static List<OWLPropertyExpression> getProperty(OWLClassExpression description) {
		List<OWLPropertyExpression> result = new LinkedList<OWLPropertyExpression>();
		
		if ((description instanceof OWLRestriction))
				result.add(((OWLRestriction)description).getProperty());
		else if (description instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
				
				//do not use addAll to avoid duplicate
				List<OWLPropertyExpression> tmp = getProperty(child);
				for (OWLPropertyExpression pro : tmp)
					if (!result.contains(pro))
						result.add(pro);
			}
		}
		
		return result;
	}
	
	
	
	public static Set<OWLPropertyExpression> getPropertySet(OWLClassExpression description) {
		return new HashSet<>(getProperty(description));
	}
	
	/**
	 * Get the Range of a property in a given description 
	 * 
	 * @param property Property
	 * @param description Description 
	 * 
	 * @return Range of the given property in the description
	 */
	public static OWLClassExpression val(OWLPropertyExpression property, OWLClassExpression description) {
		List<OWLClassExpression> innerVal = valInner(property, description);
		
		if (innerVal.size() == 0)
			return df.getOWLThing();
		else if (innerVal.size() == 1)
			return innerVal.get(0);
		else			
			return df.getOWLObjectIntersectionOf(new HashSet<>(innerVal)); // TODO why not using sets directly instead of lists?
	} //val()
	
	
	private static List<OWLClassExpression> valInner(OWLPropertyExpression property, OWLClassExpression description) {
		List<OWLClassExpression> result = new LinkedList<>();

		//restriction
		if (description instanceof OWLRestriction) {
			OWLPropertyExpression pro = ((OWLRestriction) description).getProperty();
			if (pro == property) {
				if (pro instanceof OWLDataProperty) {    //for datatype property, val(.) = {Thing}
					if (!result.contains(df.getOWLThing()))
						result.add(df.getOWLThing());
				} else if (!(description instanceof OWLObjectHasValue)) {    //object property ==> get its range
					if (OWLClassExpressionUtils.getChildren(description).size() == 0)
						logger.warn("***ERROR: Description [" + description + "] has no child");

					result.add(((OWLQuantifiedObjectRestriction)description).getFiller());
				}

			}

		}
		
		//intersection
		else if (description instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
				
				//add each element to avoid the duplication
				List<OWLClassExpression> tmp = new LinkedList<OWLClassExpression>();
				tmp = valInner(property, child);
				for (OWLClassExpression t : tmp) {
					if (!result.contains(t))
						result.add(t);
				}
			}
		}
		
		//other types of Description is not taken into consideration
		
		return result;
	}
	
	
	public static int min(OWLPropertyExpression property, OWLClassExpression description) {
		int m = minInner(property, description);
		if (m == Integer.MAX_VALUE)
			m = 0;
		
		return m;
	}


	private static int minInner(OWLPropertyExpression property, OWLClassExpression description) {
		int m = Integer.MAX_VALUE;

		//restriction
		if ((description instanceof OWLObjectMinCardinality) ||
				(description instanceof OWLObjectExactCardinality) ||
				(description instanceof OWLDataMinCardinality) ||
				(description instanceof OWLDataExactCardinality)) {

			OWLPropertyExpression pro = ((OWLRestriction) description).getProperty();

			if (pro == property) {
				int cardinality = ((OWLCardinalityRestriction) description).getCardinality();
				m = Math.min(cardinality, m);
			}
		}

		//intersection
		else if (description instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
				int cardinality = minInner(property, child);
				m = Math.min(cardinality, m);
			}
		}
		return m;
	}

	
	private static int max(OWLPropertyExpression property, OWLClassExpression description) {
		int m = maxInner(property, description);
		if (m == Integer.MIN_VALUE)
			m = 0;
		
		return m;
	}
	

	private static int maxInner(OWLPropertyExpression property, OWLClassExpression description) {
		int m = Integer.MIN_VALUE;
		
		//restriction
		if ((description instanceof OWLObjectMaxCardinality) ||
				(description instanceof OWLObjectExactCardinality) ||
				(description instanceof OWLDataMaxCardinality) ||
				(description instanceof OWLDataExactCardinality)) {

			OWLPropertyExpression pro = ((OWLRestriction)description).getProperty();
			
			if (pro == property) {	
				int cardinary = ((OWLCardinalityRestriction)description).getCardinality();
				m = Math.max(cardinary, m);
			}
		}
					
		//intersection
		else if (description instanceof OWLObjectIntersectionOf) {
			for (OWLClassExpression child : OWLClassExpressionUtils.getChildren(description)) {
				int cardinary = maxInner(property, child);
				m = Math.max(cardinary, m);
			}
		}
		return m;
	}

	
	
	/**
	 * Calculate Sp(C, D) - the similarity between atomic concepts of two descriptions
	 * 
	 * @param C
	 * @param D
	 * 
	 * @return
	 */
	public double simPrim(OWLClassExpression C, OWLClassExpression D) {
		
		//System.out.print("  ** sPrim(" + C + ", " + D +") = ");
				
		Set<OWLIndividual> coverC = PE(C);
		Set<OWLIndividual> coverD = PE(D);
		
		if ((coverC == null) || (coverD == null)) {
			//System.out.println(" 0 - null coverage returned");
			return 0;
		}
		
		Set<OWLIndividual> copyCoverC = new HashSet<>();
		copyCoverC.addAll(coverC);
		
		copyCoverC.removeAll(coverD);
		
		int intersection = coverC.size() - copyCoverC.size();
		int union = (coverC.size() + coverD.size() - intersection);
		
		double result = intersection / (double) union; 
	
		//System.out.println(intersection + "/" + union + " = " + result);
		
		return result;
	}
	
	
	public double simPrim(List<OWLClassExpression> C, List<OWLClassExpression> D) {
		OWLClassExpression intersectC, interesctD;
		
		if (C.size() < 1 || D.size() < 1)
			return -1;
		
		if (C.size() > 1)
			intersectC = df.getOWLObjectIntersectionOf(new HashSet<>(C)); // TODO why not using sets directly instead of lists?
		else 
			intersectC = C.get(0);
		
		if (D.size() > 1)
			interesctD = df.getOWLObjectIntersectionOf(new HashSet<>(D)); // TODO why not using sets directly instead of lists?
		else
			interesctD = D.get(0);
		
		return simPrim(intersectC, interesctD);
	} //simPrim()
	

	/**
	 * Calculate the similarity of the properties/roles.<br>
	 * <ol>
	 *   <li>find all properties of C, D</li>
	 *   <li>for each property p: calculate s_p = sim(p, val(p, C), val(p, D))</li>
	 *   <li>return 1/n(sum(s_p))
	 * </ol>
	 * 
	 * @param C
	 * @param D
	 * @return
	 */
	public double simRole(List<OWLPropertyExpression> allPro, OWLClassExpression C, OWLClassExpression D) {
		
		if (allPro.size() == 0) {
			//System.out.println("  ** simRole([] , " + C + ", " + D +") = 1");
			return 1;	
		}

		double sum = 0;
		
		/*
		List<PropertyExpression> proC = getProperty(C);
		List<PropertyExpression> proD = getProperty(D);
		List<PropertyExpression> allPro = new LinkedList<PropertyExpression>(proC);
				
		//calculate allPro
		for (PropertyExpression p : proD) {
			if (!allPro.contains(p))
				allPro.add(p);
		}
		*/
		
		//calculate simRole for each property
		for (OWLPropertyExpression p : allPro) {
			OWLClassExpression valCP = val(p, C);
			OWLClassExpression valDP = val(p, D);
			
			//System.out.println("  ** simRole(" + p + ", " + C + ", " + D +"):");
			sum += similarity(valCP, valDP);
		}		
		
		return 1d/allPro.size()*sum;
	} //simRole()
	
	
	/**
	 * Calculate similarity between two descriptions.<br>
	 * sim(C, D) = a * ( simPrim(prim(C), prim(D)) + 1/n * sum_pi(simRole(pi, C, D)) + 1/n * sum_pi(simNum(pi, C, D))) 
	 * 
	 * @param C
	 * @param D
	 * @return
	 */
	private double similarityInner(List<OWLPropertyExpression> allPro, OWLClassExpression C, OWLClassExpression D) {
		
		double sp = 0;
		double sr = 1;
		double sn = 1;
		
		List<OWLClassExpression> primC = prim(C);
		List<OWLClassExpression> primD = prim(D);
		
		sp = simPrim(primC, primD);
		
		if (allPro.size() > 0) {
			sn = simNum(allPro, C, D);		
			sr = simRole(allPro, C, D);
			return (1/3d)*(sp + sr + sn);
		}
		else
			return sp;
		
		//return (1/3d)*(sp + sr + sn);
		
	}
	
	
	public double similarity(OWLClassExpression C, OWLClassExpression D) {
		
		/*
		if (containDisjunction(C)) {
			//System.out.println("ERROR - [" + C + "] contains disjunction");
			return -1;
		}
		
		if (containDisjunction(D)) {
			//System.out.println("ERROR - [" + D + "] contains disjunction");
			return -1;
		}
		*/
		
		List<OWLPropertyExpression> proC = getProperty(C);
		List<OWLPropertyExpression> proD = getProperty(D);
		
		List<OWLPropertyExpression> allPro= new LinkedList<>();
		allPro.addAll(proC);
		for (OWLPropertyExpression p : proD) {
			if (!allPro.contains(p)) 
				allPro.add(p);
		}

		return similarityInner(allPro, C, D);
	}
	

	/**
	 * 
	 * @param C
	 * @param D
	 * @return
	 */
	public double disjunctiveSimilarity(OWLClassExpression C, OWLClassExpression D) {
		double sim  = 0;
				
		//System.out.println("****normalise (l=" + C.getLength() + "): " + C);
		OWLClassExpression normaliseC = normalise(0, C);
		
		//System.out.println("****flattening (l=" + normaliseC.getLength() + "): " + normaliseC);
		List<OWLClassExpression> flattenC = flattenDisjunctiveNormalDescription(normaliseC);
		
		//System.out.println("****flattening result (l=" + flattenC.size() + "): " + flattenC);
		

		//System.out.println("****normalise (l=" + D.getLength() + "): " + D);
		OWLClassExpression normaliseD = normalise(0, D);
		
		//System.out.println("****flattening (l=" + normaliseC.getLength() + "): " + normaliseD);
		List<OWLClassExpression> flattenD = flattenDisjunctiveNormalDescription(normaliseD);
		
		//System.out.println("****flattening result (l=" + flattenD.size() + "): " + flattenD);

		

		for (OWLClassExpression childC : flattenC) {
			for (OWLClassExpression childD : flattenD) {
				//System.out.println("* similarity(" + childC + ", " + childD + "):");
				double sim_i = similarity(childC, childD);
				sim = (sim < sim_i)? sim_i : sim;
				//System.out.println(" ==> return: " + sim);
			}
		}
		
		return sim;
	} //disjunctiveSimilarity()
	
	
	/**
	 * Compute similarity between a description and a set of descriptions (max similarity will be returned)
	 * 
	 * @param descriptions Set of descriptions
	 * @param D A description
	 * @return
	 */
	public double disjunctiveSimilarity(Set<OWLClassExpression> descriptions, OWLClassExpression D) {
		double similarity = 0;
		
		for (OWLClassExpression C : descriptions) {
			double s_tmp = disjunctiveSimilarity(C, D);
			similarity = Math.max(s_tmp, similarity);
		}
		
		return similarity;
	}
	
	
	/**
	 * Compute similarity between a description and a set of ParCELNodes (max)
	 *   
	 * @param descriptions Set of descriptions form of PerCELExNode
	 * @param D A description
	 * 
	 * @return Maximal similarity between D and the set of descriptions
	 */
	public double disjunctiveSimilarityEx(Set<ParCELExtraNode> descriptions, OWLClassExpression D) {
		double similarity = 0;
		
		for (ParCELExtraNode C : descriptions) {
			double s_tmp = disjunctiveSimilarity(C.getDescription(), D);
			similarity = Math.max(s_tmp, similarity);
		}
		
		return similarity;
	}
	/**
	 * Calculate similarity between numeric roles/properties of two descriptions<br>
	 * 
	 * simNum(C, D) = 1/n * sum_pi(sn(pi, min(pi, C), max(pi, C), min(pi, D), max(pi, D))) 
	 * 
	 * @param C
	 * @param D
	 * 
	 * @return
	 */
	public static double simNum(List<OWLPropertyExpression> allPro, OWLClassExpression C, OWLClassExpression D) {
		
		//System.out.println("  ** simNum(" + C + ", " + D +"):");
		
		if (allPro.size() == 0) {
			//System.out.println("\t==> return: 1");
			return 1;	
		}
		
		double sn = 0;
		
				
		/*
		List<PropertyExpression> roleC = getProperty(C);
		List<PropertyExpression> roleD = getProperty(D);
		
		Set<PropertyExpression> allProperty = new HashSet<PropertyExpression>();
		allProperty.addAll(roleC);
		allProperty.addAll(roleD);
		*/
		
			
		for (OWLPropertyExpression property : allPro) {
			int minC = min(property, C);
			int maxC = max(property, C);
			int minD = min(property, D);
			int maxD = max(property, D);
			
			double tmp = simNumInner(minC, maxC, minD, maxD);
			sn += tmp;
			
			//System.out.println("\tsn(" + property + ", (" + minC + ", " + maxC +") , (" + minD + ", " + maxD +")) = " + tmp);
		}
		
		//System.out.println("\t==> return: " + sn + "/" + allPro.size() + "=" + (sn/(double)allPro.size()));
		
		return (sn/(double)allPro.size());	
	} //simNum()
	
	/**
	 * Ref. paper "A similarity measure for the ALN Description Logic"
	 * 
	 * @param minC
	 * @param maxC
	 * @param minD
	 * @param maxD
	 * @return (minMax - maxMin + 1)/(double)(maxMax - minMin + 1);
	 */
	private static double simNumInner(int minC, int maxC, int minD, int maxD) {
		int minMax = Math.min(maxC, maxD);
		int maxMin = Math.max(minC, minD);
		
		int maxMax = Math.max(maxC, maxD);
		int minMin = Math.min(minC, minD);
		
		//if (minMax > maxMin)
			return (minMax - maxMin + 1)/(double)(maxMax - minMin + 1);
		//else
			//return 0;
	}

	
	/**
	 * Simulates the reasoner to return the number of instances covered by a description
	 *  
	 * @param description
	 * 
	 * @return Number of instances covered by the given description
	 */
	private Set<OWLIndividual> PE(OWLClassExpression description) {
		
		if (reasoner != null) {
			return reasoner.hasType(description, instances);			
		}
		
		//below is for testing this class
		Set<OWLIndividual> result = new HashSet<>();
		
		if ((description.isOWLThing()) || description.equals(Example2.person)) {
			
			String[] tmp = {"meg", "bod", "pat", "gwen", "ann", "sue", "tom"};
			for (String s : tmp)
				result.add(df.getOWLNamedIndividual(s, pm));
		}
		
		else if (description.equals(Example2.male)) {
			String[] tmp = {"bod", "pat", "tom"};
			for (String s : tmp)
				result.add(df.getOWLNamedIndividual(s, pm));
		}
		
		else if ((description.equals(Example2.notMale)) ||
				(description.equals(Example2.person_and_not_male))) {
			
			String[] tmp = {"meg", "gwen", "ann", "sue"};
			for (String s : tmp)
				result.add(df.getOWLNamedIndividual(s, pm));
		}
		else if (description.equals(Example2.notMale)) {
			String[] tmp = {"dog", "cat"};
			for (String s : tmp)
				result.add(df.getOWLNamedIndividual(s, pm));
		}
		else 
			return null;

		return result;
	}
	
	
	
	/**
	 * MAIN: for testing
	 * 
	 * @param args
	 * @throws OWLOntologyCreationException 
	 */
	public static void main(String[] args) throws OWLOntologyCreationException {

		
		ConceptSimilarity similarityChecker = new ConceptSimilarity();
		

		
		
		
		System.out.println("----------------");
		System.out.println("Example 1:");
		System.out.println("----------------");
		
		System.out.println("C = " + Example1.C);
		System.out.println("D = " + Example1.D);
		
		System.out.println("prim(C1) = " + prim(Example1.C1));
		System.out.println("prim(D1) = " + prim(Example1.D1));
		
		System.out.println("Properties(C1) = " + getProperty(Example1.C1));
		System.out.println("Properties(D1) = " + getProperty(Example1.D1));
		
		List<OWLClassExpression> flattenC = flattenDisjunctiveNormalDescription(Example1.C);
		List<OWLClassExpression> flattenD = flattenDisjunctiveNormalDescription(normalise(0, Example1.D));
		
		System.out.println("Flatten(C) = " + flattenC);
		System.out.println("Flatten(D) = " + flattenD);
	
		System.out.println("similarity(C, D) = " + similarityChecker.disjunctiveSimilarity(Example1.C, Example1.D));
		
		//------------------------------------------------
		//example 2: A similarity measure for the ALN DL
		//------------------------------------------------
		/*
		Thing thing = Thing.instance;
		NamedClass person = new NamedClass("Person");
		NamedClass male = new NamedClass("Male");
		ObjectProperty marriedTo = new ObjectProperty("marriedTo");
		ObjectProperty hasChild = new ObjectProperty("hasChild");
		Negation notMale = new Negation(male);
		Intersection person_and_not_male = df.getOWLObjectIntersectionOf(person, notMale);
		
		ObjectAllRestriction marriedTo_all_Person = new ObjectAllRestriction(marriedTo, person);		
		ObjectAllRestriction marriedTo_all_person_and_not_male = new ObjectAllRestriction(marriedTo, person_and_not_male);
		ObjectCardinalityRestriction hasChild_less_1 = new ObjectMaxCardinalityRestriction(1, hasChild, thing);
		ObjectCardinalityRestriction hasChild_less_2 = new ObjectMaxCardinalityRestriction(2, hasChild, thing);
		
		OWLClassExpression CC = df.getOWLObjectIntersectionOf(person, marriedTo_all_Person, hasChild_less_1);
		OWLClassExpression DD = df.getOWLObjectIntersectionOf(male, marriedTo_all_person_and_not_male, hasChild_less_2);
		*/
		
		System.out.println("\n----------------");
		System.out.println("Example 2:");
		System.out.println("----------------");
		
		System.out.println(" C = " + Example2.C);
		System.out.println(" D = " + Example2.D);
		
		
		//List<OWLClassExpression> flattenC2 = flattenDisjunctiveNormalDescription(Example2.C);
		//List<OWLClassExpression> flattenD2 = flattenDisjunctiveNormalDescription(normalise(0, Example2.D));
		
		//System.out.println("Flatten(C) = " + flattenC2);
		//System.out.println("Flatten(D) = " + flattenD2);
		
		System.out.println("prim(C) = " + prim(Example2.C));
		System.out.println("prim(D) = " + prim(Example2.D));
		
		List<OWLPropertyExpression> proC = getProperty(Example2.C);
		List<OWLPropertyExpression> proD = getProperty(Example2.D);
		
		System.out.println("Properties(C) = " + proC);
		System.out.println("Properties(D) = " + proD);
		
		List<OWLPropertyExpression> allPro = new LinkedList<>();
		allPro.addAll(proC);
		for (OWLPropertyExpression p : proD) {
			if (!proC.contains(proD))
				proC.add(p);
		}
		System.out.println("Properties(C, D) = " + allPro);
		
		System.out.println("val(marriedTo, C) = " + val(Example2.marriedTo, Example2.C));
		System.out.println("val(marriedTo, D) = " + val(Example2.marriedTo, Example2.D));
		
		System.out.println("val(hasChild, C) = " + val(Example2.hasChild, Example2.C));
		System.out.println("val(hasChild, D) = " + val(Example2.hasChild, Example2.D));
		//val(Example2.hasParent, Example2.D);
		System.out.println("val(hasParent, D) = " + val(Example2.hasParent, Example2.D));
		
		System.out.println("sp(prim(C), prim(D)) = " + similarityChecker.simPrim(prim(Example2.C), prim(Example2.D)));
		System.out.println("sp(val(C), val(D)) = " + 
				similarityChecker.simPrim(val(Example2.marriedTo, Example2.C), val(Example2.marriedTo, Example2.D)));
		
		System.out.println("min(hasChild, C) = " + min(Example2.hasChild, Example2.C));
		System.out.println("min(hasChild, D) = " + min(Example2.hasChild, Example2.D));
		System.out.println("min(marriedTo, C) = " + min(Example2.marriedTo, Example2.C));
		System.out.println("min(marriedTo, D) = " + min(Example2.marriedTo, Example2.D));

		
		System.out.println("max(hasChild, C) = " + max(Example2.hasChild, Example2.C));
		System.out.println("max(hasChild, D) = " + max(Example2.hasChild, Example2.D));
		System.out.println("max(marriedTo, C) = " + max(Example2.marriedTo, Example2.C));
		System.out.println("max(marriedTo, D) = " + max(Example2.marriedTo, Example2.D));
		
		System.out.println("----------------");
		System.out.println("calculation");
		System.out.println("----------------");
		
		//System.out.println("sn (C, D) = " + simNum(Example2.C, Example2.D));
		//System.out.println("similarity(person, animal) = " + similarityChecker.disjunctiveSimilarity(Example2.person, Example2.animal));
		System.out.println("similarity(C, D) = " + similarityChecker.disjunctiveSimilarity(Example2.C, Example2.D));
		
		OWLObjectUnionOf person_or_not_male_and_person = df.getOWLObjectUnionOf(Example2.person_and_not_male, Example2.person);
		
		System.out.println(normalise(0, person_or_not_male_and_person));
		
	}

	private static final PrefixManager pm = new DefaultPrefixManager("http://example.org/");
	
	private static class Example1 {
		//---------------------------------------------------
		// example 1: a dissimilarity measure for the ALC DL 
		//---------------------------------------------------
		public static OWLClass A1 = df.getOWLClass("A1", pm);
		public static OWLClass A2 = df.getOWLClass("A2", pm);
		public static OWLClass A3 = df.getOWLClass("A3", pm);
		public static OWLClass A4 = df.getOWLClass("A4", pm);
		public static OWLClass B1 = df.getOWLClass("B1", pm);
		public static OWLClass B2 = df.getOWLClass("B2", pm);
		public static OWLClass B3 = df.getOWLClass("B3", pm);
		public static OWLClass B4 = df.getOWLClass("B4", pm);
		public static OWLClass B5 = df.getOWLClass("B5", pm);
		public static OWLClass B6 = df.getOWLClass("B6", pm);
		
		public static OWLObjectProperty Q = df.getOWLObjectProperty("Q", pm);
		public static OWLObjectProperty R = df.getOWLObjectProperty("R", pm);
		public static OWLObjectProperty S = df.getOWLObjectProperty("S", pm);
		public static OWLObjectProperty T = df.getOWLObjectProperty("T", pm);
		
		// \some R. B1
		public static OWLObjectSomeValuesFrom R_some_B1 = df.getOWLObjectSomeValuesFrom(R, B1);
		
		// \all Q. (A4 and B5)
		public static OWLObjectIntersectionOf A4_and_B5 = df.getOWLObjectIntersectionOf(A4, B5);
		public static OWLObjectAllValuesFrom Q_all__A4_and_B5 = df.getOWLObjectAllValuesFrom(Q, A4_and_B5);
		
		// \all T. (\all Q. (A4 and B5))
		public static OWLObjectAllValuesFrom T_all__Q_all__A4_and_B5 = df.getOWLObjectAllValuesFrom(T, Q_all__A4_and_B5);
		
		// \some R. A3
		public static OWLObjectSomeValuesFrom R_some_A3 = df.getOWLObjectSomeValuesFrom(R, A3);
		
		// \some R. B2
		public static OWLObjectSomeValuesFrom R_some_B2 = df.getOWLObjectSomeValuesFrom(R, B2);
		
		// \all S. B3
		public static OWLObjectAllValuesFrom S_all_B3 = df.getOWLObjectAllValuesFrom(S, B3);
		
		
		// \all T. (B6 and B4)
		public static OWLObjectIntersectionOf B6_and_b4 = df.getOWLObjectIntersectionOf(B6, B4);
		public static OWLObjectAllValuesFrom T_all__B6_and_B4 = df.getOWLObjectAllValuesFrom(T, B6_and_b4);

		public static OWLClassExpression C1 = df.getOWLObjectIntersectionOf(A2, R_some_B1, T_all__Q_all__A4_and_B5);
		public static OWLClassExpression C = df.getOWLObjectUnionOf(C1, A1);

		public static OWLClassExpression D1 = df.getOWLObjectIntersectionOf(A1, B2, R_some_A3, R_some_B2, S_all_B3, T_all__B6_and_B4);
		public static OWLClassExpression D = df.getOWLObjectUnionOf(D1, B2);
	}

	
	private static class Example2 {
		public static OWLClass thing = df.getOWLThing();
		public static OWLClass person = df.getOWLClass("Person", pm);
		public static OWLClass male = df.getOWLClass("Male", pm);
		public static OWLObjectProperty marriedTo = df.getOWLObjectProperty("marriedTo", pm);
		public static OWLObjectProperty hasChild = df.getOWLObjectProperty("hasChild", pm);
		public static OWLObjectComplementOf notMale = df.getOWLObjectComplementOf(male);
		public static OWLObjectIntersectionOf person_and_not_male = df.getOWLObjectIntersectionOf(person, notMale);
		public static OWLObjectUnionOf person_or_not_male = df.getOWLObjectUnionOf(person, notMale);
		
		public static OWLClass animal = df.getOWLClass("animal", pm);
		
		public static OWLObjectSomeValuesFrom marriedTo_all_Person = df.getOWLObjectSomeValuesFrom(marriedTo, person);
		public static OWLObjectAllValuesFrom marriedTo_all_person_and_not_male = df.getOWLObjectAllValuesFrom(marriedTo, person_and_not_male);
		public static OWLObjectAllValuesFrom marriedTo_all_person_or_not_male = df.getOWLObjectAllValuesFrom(marriedTo, person_or_not_male);
		public static OWLObjectCardinalityRestriction hasChild_less_1 = df.getOWLObjectMaxCardinality(1, hasChild, thing);
		public static OWLObjectCardinalityRestriction hasChild_less_2 = df.getOWLObjectMaxCardinality(2, hasChild, thing);

		public static OWLClassExpression C = df.getOWLObjectIntersectionOf(person, marriedTo_all_Person, hasChild_less_1);
		public static OWLClassExpression D = df.getOWLObjectIntersectionOf(male, marriedTo_all_person_or_not_male, hasChild_less_2, marriedTo_all_Person);
		
		public static OWLObjectProperty hasParent = df.getOWLObjectProperty("hasParent", pm);
	}
	
	

}
 