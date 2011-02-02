package javatools.datatypes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javatools.administrative.D;

/** 
This class is part of the Java Tools (see http://mpii.de/yago-naga/javatools).
It is licensed under the Creative Commons Attribution License 
(see http://creativecommons.org/licenses/by/3.0) by 
the YAGO-NAGA team (see http://mpii.de/yago-naga).


  
  Fabian M. Suchanek, Milan Vojnovic, Dinan Gunawardena
  "Social Tags: Meaning and Suggestions" (pdf, bib)
  ACM Conference on Information and Knowledge Management (CIKM 2008)
  
The class represents a vector of terms with their frequencies. 
See the paper for explanations.
 
Also provides Wilson interval computation.
 
@author Fabian M. Suchanek
*/
public class FrequencyVector<T, V extends Number & Comparable<V>> {

  /** Holds the L2 norm of this vector*/
  protected double norm = 0;

  /** Holds the sum of all elements*/
  protected double sum = 0;

  /** Holds the maximum value of all elements*/
  protected double max = Double.MIN_VALUE;

  /** Holds the vector itself*/
  protected Map<T, V> data;

  /** Holds the terms of the vector sorted by decreasing frequency*/
  protected List<T> sortedTerms;

  /** Constructs a frequency vector. The frequency vector is backed by the map. ZERO-entries are removed! */
  public FrequencyVector(Map<T, V> applications) {
    data = applications;    
    Iterator<Map.Entry<T, V>> entries=applications.entrySet().iterator();
    while(entries.hasNext()){
      Map.Entry<T, V> term=entries.next();
      double d=term.getValue().doubleValue();
      if(d<=0) {
        entries.remove();
        continue;
      }
      sum += d;
      norm += d*d;
      if (d > max) max = d;
    }
    sortedTerms = sortedTerms(applications);
    norm = Math.sqrt(norm);
  }

  // ---------------------------------------------------------------------------
  //                   Accessor methods
  // ---------------------------------------------------------------------------
  
  /** Returns the first position in sortedTerms that has equal number of applications to its successor*/
  public int firstTiePos() {
    for (int i = 1; i < numTerms(); i++) {
      if (doubleValueFor(termAtRank(i)) == doubleValueFor(termAtRank(i - 1))) return (i - 1);
    }
    return (-1);
  }

  /** Returns the sum of elements*/
  public double sum() {
    return (sum);
  }

  /** Returns the L2 norm of elements*/
  public double norm() {
    return (norm);
  }

  /** Returns the maximum of elements*/
  public double max() {
    return (max);
  }

  /** Returns the term at rank i*/
  public T termAtRank(int i) {
    if (i < sortedTerms().size()) return (sortedTerms().get(i));
    else return (null);
  }

  /** Returns the number of terms in the support*/
  public int numTerms() {
    return (data.size());
  }

  /** Returns the frequency for a term (or null)*/
  public V valueFor(T term) {
    return (data.get(term));
  }

  /** Returns the frequency for a term as double (or 0)*/
  public double doubleValueFor(T term) {
    V value = data.get(term);
    if (value == null) return (0);
    return (value.doubleValue());
  }

  /** Returns the frequency for a term, divided by the sum (or 0)*/
  public double normalizedValueFor(T term) {
    return (doubleValueFor(term) / this.sum());
  }

  /** Returns the frequency for a term, divided by the maximum (or 0)*/
  public double maxNormalizedValueFor(T term) {
    return (doubleValueFor(term) / this.max());
  }

  /** Returns the frequency for a term, divided by the sum and smoothed*/
  public double smoothedValueFor(T term) {    
    return ((doubleValueFor(term) + 1) / (this.sum() + 2));
  }

  /** Returns the terms sorted by decreasing frequency*/
  public List<T> sortedTerms() {
    return (sortedTerms);
  }

  /** Returns the terms. This collection may be more efficient for membership checks than the sortedTerms*/
  public Collection<T> terms() {
    return (data.keySet());
  }

  /** Returns the terms of the application data sorted by their number of applications*/
  protected static <C, V extends Comparable<V>> List<C> sortedTerms(final Map<C, V> applications) {
    List<C> sorted = new ArrayList<C>(applications.keySet());
    Collections.sort(sorted, new Comparator<C>() {

      public int compare(C o1, C o2) {
        return applications.get(o2).compareTo(applications.get(o1));
      }
    });
    return (sorted);
  }

  @Override
  public String toString() {
    StringBuilder result=new StringBuilder("[");
    for(T term : sortedTerms()) result.append(term).append(" (").append(valueFor(term)).append("), ");
    if(result.length()>2) result.setLength(result.length()-2);
    result.append("]");
    return result.toString();
  }
  
  @Override
  public int hashCode() {  
    return data.hashCode();
  }
  
  @Override
  public boolean equals(Object obj) {   
    return (obj!=null && obj instanceof FrequencyVector && ((FrequencyVector)obj).data.equals(data));
  }
  
  // ---------------------------------------------------------------------------
  //                   Normalization
  // ---------------------------------------------------------------------------

  /** Normalizes this vector */
  @SuppressWarnings("unchecked")
  public FrequencyVector<T,Double> normalized() {
    Map<T, Double> result= null;
    try {
      result= this.data.getClass().newInstance();
    } catch (Exception e) {}
    for(Map.Entry<T, V> entry : data.entrySet()) {
      result.put(entry.getKey(), entry.getValue().doubleValue()/norm());
    }
    return(new FrequencyVector<T, Double>(result));
  }

  /** Max-Normalizes this vector */
  @SuppressWarnings("unchecked")
  public FrequencyVector<T,Double> maxNormalized() {
    Map<T, Double> result= null;
    try {
      result= this.data.getClass().newInstance();
    } catch (Exception e) {}
    for(Map.Entry<T, V> entry : data.entrySet()) {
      result.put(entry.getKey(), entry.getValue().doubleValue()/max());
    }
    return(new FrequencyVector<T, Double>(result));
  }

  // ---------------------------------------------------------------------------
  //                   Intersection
  // ---------------------------------------------------------------------------
  
  /** Computes the common supports*/
  public Set<T> intersection(FrequencyVector<T, ?> other) {
    Set<T> intersection = new TreeSet<T>(data.keySet());
    intersection.retainAll(other.data.keySet());
    return (intersection);
  }

  /** Computes the intersection of the top k elements*/
  public Set<T> topKIntersection(FrequencyVector<T, V> trueFrequencies, int k) {
    Set<T> result = new TreeSet<T>(this.sortedTerms().subList(0, Math.min(k, this.numTerms())));
    result.retainAll(trueFrequencies.sortedTerms().subList(0, Math.min(k, trueFrequencies.numTerms())));
    return (result);
  }

  // ---------------------------------------------------------------------------
  //                   Cosine
  // ---------------------------------------------------------------------------

  /** Computes the cosine to another vector, if the intersection is already available*/
  public double cosine(FrequencyVector<T, ?> other, Collection<T> intersection) {
    if(this.norm()==0 || other.norm()==0) return(0);
    double cosine = 0;
    for (T term : intersection) {
      cosine += data.get(term).doubleValue() * other.data.get(term).doubleValue();
    }
    return (cosine / this.norm() / other.norm());
  }

  /** Computes the cosine to another vector*/
  public double cosine(FrequencyVector<T, ?> other) {
    if(this.norm()==0 || other.norm()==0) return(0);
    double cosine = 0;
    for (T term : this.terms()) {
      cosine += doubleValueFor(term) * other.doubleValueFor(term);
    }
    return (cosine / this.norm() / other.norm());
  }

  // ---------------------------------------------------------------------------
  //                   Precision
  // ---------------------------------------------------------------------------

  /** Computes the standard precision at k*/
  public double precisionAtKWithRespectTo(Collection<T> groundTruth, int k) {
    if (k > this.numTerms()) k = this.numTerms();
    if (k == 0) return (1);
    int counter = 0;
    for (int i = 0; i < k; i++) {
      if (groundTruth.contains(this.termAtRank(i))) counter++;
    }
    return ((double) counter / k);
  }

  /** Computes the precision at k to a set, weighted with this vector's frequencies*/
  public double weightedPrecisionAtKWithRespectTo(Collection<T> groundTruth, int k) {
    if (k > this.numTerms()) k = this.numTerms();
    if (k == 0) return (1);
    double counter = 0;
    double total = 0;
    for (int i = 0; i < k; i++) {
      double val = this.doubleValueFor(this.termAtRank(i));
      if (groundTruth.contains(this.termAtRank(i))) counter += val;
      total += val;
    }
    return ((double) counter / total);
  }

  /** Computes standard precision*/
  public double precisionWithRespectTo(Collection<T> groundTruth) {
    return(precisionAtKWithRespectTo(groundTruth, this.numTerms()));
  }

  /** Computes the standard precision*/
  public double precisionWithRespectTo(FrequencyVector<T, ?> trueFrequencies) {
    return (precisionWithRespectTo(trueFrequencies.terms()));
  }

  /** Computes the standard precision if the intersection is known*/
  public double precisionWithRespectToIntersection(Collection<T> intersection) {
    return (this.numTerms() == 0 ? 1 : (double) intersection.size() / this.numTerms());
  }

  /** Computes the standard precision to a set, weighted with this vector's frequencies*/
  public double weightedPrecisionWithRespectTo(Collection<T> groundTruth) {
    return(weightedPrecisionAtKWithRespectTo(groundTruth, this.numTerms()));
  }

  /** Computes the standard precision to a set, weighted with this vector's frequencies*/
  public double weightedPrecisionWithRespectTo(FrequencyVector<T, ?> trueFrequencies) {
    return (precisionWithRespectTo(trueFrequencies.terms()));
  }
    
  /** Computes the standard precision at k*/
  public double precisionAtKWithRespectTo(FrequencyVector<T, ?> groundTruth, int k) {
    return (precisionAtKWithRespectTo(groundTruth.terms(), k));
  }

  /** Computes the weighted precision at k*/
  public double weightedPrecisionAtKWithRespectTo(FrequencyVector<T, ?> groundTruth, int k) {
    return (weightedPrecisionAtKWithRespectTo(groundTruth.terms(), k));
  }
  
  /** Computes the average precision (MAP)*/
  public double averagePrecision(Collection<T> groundTruth) {
    if (groundTruth.size() == 0) return (1);
    double avep = 0;
    int intersectionSoFar = 0;
    for (int i = 0; i < this.sortedTerms.size(); i++) {
      if (groundTruth.contains(this.termAtRank(i))) {
        intersectionSoFar++;
        avep += intersectionSoFar / (i + 1);
      }
    }
    return (avep / groundTruth.size());
  }

  /** Computes the average precision, shuffling subsets to get an optimal value*/
  public double optimalAveragePrecision(FrequencyVector<T, ?> trueFrequencies) {
    double bestAP = 0;
    for (int i = 1; i < trueFrequencies.sortedTerms.size(); i++) {
      double ap = averagePrecision(trueFrequencies.sortedTerms().subList(0, i));
      if (ap > bestAP) bestAP = ap;
    }
    return (bestAP);
  }

  // ---------------------------------------------------------------------------
  //                   Recall
  // ---------------------------------------------------------------------------

  /** Computes the standard recall at k*/
  public double recallAtKWithRespectTo(Collection<T> groundTruth, int k) {
    if (groundTruth.size() == 0) return (1);
    if (k > this.numTerms()) k = this.numTerms();
    int counter=0;
    for(int i=0;i<k;i++) {
      if(groundTruth.contains(termAtRank(i))) counter++;
    }
    return ((double)counter/groundTruth.size());
  }

  /** Computes the standard recall*/
  public double recallWithRespectTo(Collection<T> groundTruth) {
    if (groundTruth.size() == 0) return (1);
    int counter=0;
    for(T term : groundTruth) {
      if(doubleValueFor(term)!=0) counter++;
    }
    return ((double)counter/groundTruth.size());
  }
  
  /** Computes the standard recall, weighted with the true frequencies*/
  public double weightedRecallWithRespectTo(FrequencyVector<T, V> trueFrequencies) {
    return (weightedRecallAtKWithRespectTo(trueFrequencies,this.numTerms()));
  }
  
  /** Computes the standard recall at k, weighted with the true frequencies*/
  public double weightedRecallAtKWithRespectTo(FrequencyVector<T, V> trueFrequencies, int k) {
    if (trueFrequencies.numTerms() == 0) return (1);
    double nominator = 0;
    for (int i = 0; i < this.numTerms() && i < k; i++)
      nominator += trueFrequencies.doubleValueFor(this.termAtRank(i));
    return (nominator / trueFrequencies.sum());
  }

  /** Computes standard recall*/
  public double recallWithRespectTo(FrequencyVector<T, ?> trueFrequencies) {
    return (recallWithRespectTo(trueFrequencies.terms()));
  }  

  /** Computes standard recall, if the intersection is already available*/
  public double recallWithRespectTo(Collection<T> trueSet, Collection<T> intersection) {
    return (trueSet.size() == 0 ? 1 : (double) intersection.size() / trueSet.size());
  }

  /** Computes standard recall, if the intersection is already available*/
  public double recallWithRespectTo(FrequencyVector<T, ?> trueFrequencies, Collection<T> intersection) {
    return (recallWithRespectTo(trueFrequencies.terms(),intersection));
  }

  /** Computes the standard recall at k*/
  public double recallAtKWithRespectTo(FrequencyVector<T, V> trueFrequencies, int k) {
    return(recallAtKWithRespectTo(trueFrequencies.terms(), k));
  }

  // ---------------------------------------------------------------------------
  //                   NDCG
  // ---------------------------------------------------------------------------
  
  /** Computes the NDCG with respect to a gain*/
  public double ndcgWithRespectToGain(FrequencyVector<T, ?> trueFrequencies) {
    double dcg = 0;
    for (int i = 0; i < this.numTerms(); i++) {
      dcg += trueFrequencies.maxNormalizedValueFor(termAtRank(i)) / Math.log(i + 2);
    }
    double truedcg = 0;
    for (int i = 0; i < trueFrequencies.numTerms(); i++) {
      truedcg += trueFrequencies.maxNormalizedValueFor(trueFrequencies.termAtRank(i)) / Math.log(i + 2);
    }
    if (truedcg == 0) return (0);
    return (dcg / truedcg);
  }

  /** Computes the NDCG with respect to a gain, with weighting 2^x*/
  public double ndcg2WithRespectToGain(FrequencyVector<T, ?> trueFrequencies) {
    double dcg = 0;
    for (int i = 0; i < this.numTerms(); i++) {
      dcg += (Math.pow(2, trueFrequencies.maxNormalizedValueFor(termAtRank(i))) - 1) / Math.log(i + 2);
    }
    double truedcg = 0;
    for (int i = 0; i < trueFrequencies.numTerms(); i++) {
      truedcg += (Math.pow(2, trueFrequencies.maxNormalizedValueFor(trueFrequencies.termAtRank(i))) - 1) / Math.log(i + 2);
    }
    if (truedcg == 0) return (0);
    return (dcg / truedcg);
  }

  // ---------------------------------------------------------------------------
  //                   Mean
  // ---------------------------------------------------------------------------

  /** Computes the mean vector of this vector and the other one*/
  @SuppressWarnings("unchecked")
  public FrequencyVector<T, Double> normalizedMeanWith(FrequencyVector<T, V> other) {
    Map<T, Double> mean = null;
    try {
      mean = this.data.getClass().newInstance();
    } catch (Exception e) {
    }
    for (T term : this.sortedTerms()) {
      mean.put(term, doubleValueFor(term) / 2);
    }
    for (T term : other.sortedTerms()) {
      Double d = mean.get(term);
      if (d == null) mean.put(term, other.doubleValueFor(term) / 2);
      else mean.put(term, other.doubleValueFor(term) / 2 + d);
    }
    return (new FrequencyVector<T, Double>(mean));
  }

  // ---------------------------------------------------------------------------
  //                   Fuzzy Precision and Recall
  // ---------------------------------------------------------------------------

  /** Computes the fuzzy recall */
  public double fuzzyRecallWithRespectTo(FrequencyVector<T, V> trueFrequencies) {
    if (trueFrequencies.sum() == 0) return (1);
    double fuzzyRecall = 0;
    for (T trueTerm : trueFrequencies.sortedTerms()) {
      double trueValue = trueFrequencies.maxNormalizedValueFor(trueTerm);
      double guessedValue = this.maxNormalizedValueFor(trueTerm);
      if (trueValue > guessedValue) {
        fuzzyRecall += trueValue - guessedValue;
      }
    }
    fuzzyRecall = 1 - fuzzyRecall / trueFrequencies.sum() * trueFrequencies.max();
    if (fuzzyRecall < 0) fuzzyRecall = 0; //Small rounding errors may occur
    return (fuzzyRecall);
  }

  /** Computes the fuzzy Precision */
  public double fuzzyPrecisionWithRespectTo(FrequencyVector<T, V> trueFrequencies) {
    return (trueFrequencies.fuzzyRecallWithRespectTo(this));
  }

  // ---------------------------------------------------------------------------
  //                   Wilson
  // ---------------------------------------------------------------------------

  /** Computes the Wilson Interval 
   * (see http://en.wikipedia.org/wiki/Binomial_proportion_confidence_interval#Wilson_score_interval)
   * Given the total number of events and the number of "correct" events, returns in a double-array
   * in the first component the center of the Wilson interval and in the second component the
   * width of the interval. alpha=95%. 
   */
  public double[] wilson(int total, int correct) {
    double z = 1.96;
    double p = (double) correct / total;
    double center = (p + 1 / 2.0 / total * z * z) / (1 + 1.0 / total * z * z);
    double d = z * Math.sqrt((p * (1 - p) + 1 / 4.0 / total * z * z) / total)
            / (1 + 1.0 / total * z * z);
    return(new double[]{center,d});
  }
  
  // ---------------------------------------------------------------------------
  //                   Test method
  // ---------------------------------------------------------------------------

  /** Test*/
  public static void main(String[] args) {
    FrequencyVector<String,Integer> groundTruth=new FrequencyVector<String, Integer>(
        new FinalMap<String, Integer>("A",3,"B",1,"C",2)
        );
    FrequencyVector<String,Integer> guessed=new FrequencyVector<String, Integer>(
        new FinalMap<String, Integer>("A",3,"C",1,"D",1,"E",1)
        );
    D.p("Comparing");
    D.p("  Ground truth:",groundTruth);
    D.p("  Guessed     :",guessed);
    D.p();
    D.p("Precision:", guessed.precisionWithRespectTo(groundTruth));
    D.p("Weighted precision:", guessed.weightedPrecisionWithRespectTo(groundTruth));
    D.p("Fuzzy precision:", guessed.fuzzyPrecisionWithRespectTo(groundTruth));
    D.p("Precision at 2:", guessed.precisionAtKWithRespectTo(groundTruth,2));
    D.p("Weighted precision at 2:", guessed.weightedPrecisionAtKWithRespectTo(groundTruth,2));
    D.p("Recall:", guessed.recallWithRespectTo(groundTruth));
    D.p("Weighted recall:", guessed.weightedRecallWithRespectTo(groundTruth));
    D.p("Fuzzy recall:", guessed.fuzzyRecallWithRespectTo(groundTruth));
    D.p("Recall at 2:", guessed.recallAtKWithRespectTo(groundTruth,2));
    D.p("Weighted recall at 2:", guessed.weightedRecallAtKWithRespectTo(groundTruth,2));
    D.p("\nCosine:",guessed.cosine(groundTruth));
    D.p("Intersection:",guessed.intersection(groundTruth));
    D.p("NDCG:",guessed.ndcgWithRespectToGain(groundTruth));    
  }
}
