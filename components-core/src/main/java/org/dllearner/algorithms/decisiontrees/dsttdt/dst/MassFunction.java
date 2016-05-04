/**
 * Copyright (C) 2007 - 2016, Jens Lehmann
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
package org.dllearner.algorithms.decisiontrees.dsttdt.dst;

import java.util.List;

import org.dllearner.algorithms.decisiontrees.dsttdt.dst.MassFunction;
import org.dllearner.algorithms.decisiontrees.utils.*;

/**
 * A class for representing a BBA
 * @author Giuseppe Rizzo
 *
 * @param <T>
 */
public class MassFunction <T extends Comparable<? super T>> {
	private  List<T> frameOfDiscernement;//frame of Discernement
	private  List<List<T>> powerSet;
	private double[] values;
	
	
//	public static void setFrameOfDiscernement(){
//		
//	}
//	
	/**
	 * Constructor
	 * @param set
	 */
	public MassFunction(List<T> set){
		frameOfDiscernement=set;
		generatePowerSet();
		values= new double[powerSet.size()];
		
	}
	/**
	 * method for generating the powerset of a frame Of Discernment
	 * @return
	 */
	public void  generatePowerSet(){

		powerSet=Combination.findCombinations(frameOfDiscernement);
	}
	
	
	/**
	 * Returns the subset of a frame of discernment
	 * @return insieme potenza
	 */
	@SuppressWarnings({ "unchecked", "rawtypes", "unused" })
	public  List<T>[] getSubsetsOfFrame(){
		List[] result= new List[powerSet.size()];
		int i=0;
		for(List<T> elem:powerSet){
			
			result[i]=powerSet.get(i);
			i++;
		}
		return result;
	}
	
	public List<T> getFrame(){
		return frameOfDiscernement;
		
	}
	/**
	 * Set a specific value for this BBA
	 */
	public void setValues(List<T> label,double value){
		int pos= SetUtils.find(label,powerSet);
		values[pos]=value;
		
		
	}
	
	
	/**
	 * Returns the value of a BBA for a specific element of class 
	 * @param label
	 * @return the value of a bba or NaN 
	 */
	public double getValue(List<T> label){
		//System.out.println(valori.get(categoria));
		int pos= SetUtils.find(label, powerSet);
		return values[pos];
	
	}
	
	
	public double getNonSpecificityMeasureValue(){
		double result=0;
		for(List<T> label: powerSet){
			if(!label.isEmpty())
				result+=(values[SetUtils.find(label, powerSet)]*Math.log(label.size())); 
		}
//		System.out.println("Non-sp: "+result);
		return result;
	}
	
	
	public double getRandomnessMeasure(){
		double result=0.0;
		for (List<T> c: powerSet){
			double pignisticValue=getPignisticTransformation(c);
			int posCategoria = SetUtils.find(c, powerSet);
			 result+= -1* (values[posCategoria]*Math.log(pignisticValue));
		
		}
		return result;
		
		
			
	}
	
	public double getPignisticTransformation(List<T> cl){
		// it works certainly for {-1,+1} as a frame of discernement
		 double result=0.0;
		for(T element: cl){
			double pignisticValueForElement=0; // initialization
			for(List<T> categoria: powerSet){

				if(!categoria.isEmpty()){
					if (categoria.contains(element)){
						int posCategoria = SetUtils.find(categoria, powerSet);
						pignisticValueForElement += values[posCategoria]/categoria.size();
					}
				}

			}
			result+=pignisticValueForElement;
			
		}
		return result;
	}
	
	
	public double getGlobalUncertaintyMeasure(){
		
		double nonSpecificity= this.getNonSpecificityMeasureValue();
		double randomness= this.getRandomnessMeasure();
		final double LAMBDA= 0.1;
		double result= ((1-LAMBDA)*nonSpecificity)+(LAMBDA*randomness);
		return result;
		
	}
	
	/**
	 * The method computes a confusion measure described in Smarandache et.al as discordant measure
	 * @return
	 */
	public double getConfusionMeasure(){
		double result=0;
		for(List<T> labels: powerSet){
			if(!labels.isEmpty())
				result-=(values[SetUtils.find(labels, powerSet)]*Math.log(this.computeBeliefFunction(labels))); 
		}
//		System.out.println("Non-sp: "+result);
		return result;
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	/**
	 * combine two BBAs according to the Dempster rule
	 * @param function
	 * @return 
	 */
	public MassFunction combineEvidences(MassFunction function){
		MassFunction result= new MassFunction(frameOfDiscernement);
		double conflitto=getConflict(function);
		
		for(List<T> elem:powerSet){
			int pos=SetUtils.find(elem, powerSet);
			
			for(List<T>hypothesis1: powerSet){
				for(List<T>hypothesis2:powerSet){
					List<T> hypothesis12=SetUtils.intersection(hypothesis1, hypothesis2);
						
						if(!(hypothesis12.isEmpty())&&(SetUtils.areEquals(hypothesis12, elem))){
							SetUtils.find(hypothesis1, powerSet);
							SetUtils.find(hypothesis2, powerSet);
							double massProduct=getValue(hypothesis1)*function.getValue(hypothesis2)/conflitto;	
							result.values[pos]+=massProduct;
							
						}
//						System.out.println("Valori"+pos+"----"+result.valori[pos]);
					}
					
				}
				
				
			}
			
		return result;
			
		}
		
		
		
	
	
	
	
	/**
	 * Dempster rule for more BBAs
	 * @param function
	 * @return
	 */
	@SuppressWarnings("rawtypes")
	public MassFunction combineEvidences(MassFunction... function){
		if(function.length==0)
			throw new RuntimeException("At least a mass function is required");
		MassFunction result=this.combineEvidences(function[0]);
		// associative operation
		for(int i=1;i<function.length;i++){
			
			
			result= result.combineEvidences(function[i]);
			
		}
		
		
	
		
		return result;
		
	}
	
	/**
	 * Implementation of Dubois-Prade combination rule 
	 * @param function
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public MassFunction<T> combineEvidencesDuboisPrade (MassFunction function){
		
		MassFunction<T> result= new MassFunction(frameOfDiscernement);
		
		// i-th hypothesis
		for(List<T> elem:powerSet){
			int pos=SetUtils.find(elem, powerSet);
			// intersection betwee
			for(List<T>hypothesis1: powerSet){
				for(List<T>hypothesis2:powerSet){
					List<T> hypothesis12=SetUtils.union(hypothesis1, hypothesis2);
						// 
						if((SetUtils.areEquals(hypothesis2, elem))){
							SetUtils.find(hypothesis1, powerSet);
							SetUtils.find(hypothesis2, powerSet);
							double massProduct=getValue(hypothesis1)*function.getValue(hypothesis2);	
							result.values[pos]+=massProduct;
							
						}
						
					}
					
				}
//				result.valori[pos]=result.valori[pos];
				
			}
			
		return result;
		
	}
	
	@SuppressWarnings("rawtypes")
	public MassFunction combineEvidencesDuboisPrade(MassFunction... function){
		if(function.length==0)
			throw new RuntimeException("More than 1 mass function are required");
		MassFunction result=this.combineEvidencesDuboisPrade(function[0]);
		// operative association
		for(int i=1;i<function.length;i++)
			result= result.combineEvidencesDuboisPrade(function[i]);
			
		
		
		
	
		
		return result;
		
	}
	
	
	
	/**
	 * Compute the conflict between two hypotheses
	 * @param function
	 * @return
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public double getConflict(MassFunction function){
		double emptyMass=0;
		for(List<T> hypothesis:powerSet){
//			System.out.println("***************");
//			System.out.println("Ipotesi 1:"+ipotesi1);
			for(List<T> hypothesis2:powerSet){
//				System.out.println("Ipotesi 2:"+ipotesi2);
				List<T>intersezione=SetUtils.intersection(hypothesis,hypothesis2);
				if(!intersezione.isEmpty()){
//					System.out.println("Intersezione vuota");
					emptyMass+= (getValue(hypothesis)*function.getValue(hypothesis2));
//					System.out.println(massaVuota);
				}
				
				
			}
			
			
		}
	
		return (emptyMass);
	}
	/**
	 * Compute the belief function value
	 * @param hypothesis
	 * @return
	 */
	public double computeBeliefFunction(List<T> hypothesis){
		double bel_hypothesis=0;
		for(List<T> elem:powerSet){
			// for all non-empty subsets
			if(!elem.isEmpty()&& hypothesis.containsAll(elem)){
				// somma le masse
//				System.out.println("m("+elem+")="+bel_ipotesi);
				bel_hypothesis+=getValue(elem);
				
			}
		}
//			System.out.println("Belief:"+bel_ipotesi);
		
		return bel_hypothesis;
	}
	/**
	 * Compute the plausibility function value
	 * @param ipotesi
	 * @return
	 */
	public double calcolaPlausibilityFunction(List<T> ipotesi){
		// 
		double pl_ipotesi=0;
		for(List<T> elem:powerSet){
			
			if(!(SetUtils.intersection(ipotesi,elem)).isEmpty())
			
				pl_ipotesi+=getValue(elem);
//			System.out.println(pl_ipotesi);
		}
			
//		System.out.println("Plausibility"+pl_ipotesi);
		return pl_ipotesi;
		
		
	}
	/**
	 * Computation of the confirmation function
	 * @param ipotesi
	 * @return
	 */
	public double getConfirmationFunctionValue(List<T>ipotesi){
		return (computeBeliefFunction(ipotesi)+calcolaPlausibilityFunction(ipotesi)-1);
		
	}
	
	public String toString(){
		String res="";
		for(int i=0;i<powerSet.size();i++){
			String string = ""+powerSet.get(i)+values[i];
			res+= string;
		}
		return res;
	}

	
	
	
	
	
}

