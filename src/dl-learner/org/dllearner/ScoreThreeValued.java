package org.dllearner;

import java.text.DecimalFormat;
import java.util.Set;
import java.util.SortedSet;

import org.dllearner.dl.Individual;
import org.dllearner.utilities.Helper;

/**
 * Berechnet die Punktzahl (negativ), indem es die Ergebnisse einer Definition
 * mit den Soll-Ergebnissen vergleicht.
 * 
 * TODO: die Implementierung ist momentan dahingehend unguenstig, dass viele Sachen
 * nur fuer die optische Aufbereitung berechnet werden; effizienter waere es
 * nur die Klassifikationsfehler zu beruecksichtigen und andere statistische Werte nur
 * dann wenn sie benoetigt werden
 * 
 * @author Jens Lehmann
 *
 */
public class ScoreThreeValued extends Score {
	
	public enum ScoreMethod {POSITIVE, FULL};
	
	private SortedSet<Individual> posClassified;
	private SortedSet<Individual> neutClassified;
	private SortedSet<Individual> negClassified;
	private SortedSet<Individual> posExamples;
	private SortedSet<Individual> neutExamples;
	private SortedSet<Individual> negExamples;
	
    private Set<Individual> posAsNeg;
    private Set<Individual> negAsPos;
    private Set<Individual> posAsNeut;
    private Set<Individual> neutAsPos;
    private Set<Individual> neutAsNeg;
    private Set<Individual> negAsNeut;
    private Set<Individual> posAsPos;
    private Set<Individual> negAsNeg;
    private Set<Individual> neutAsNeut;      
    
    private double score;
    private double accuracy;
    private double accuracyOnExamples;
    private double accuracyOnPositiveExamples;
    private double errorRate;
    
    private int nrOfExamples;
    private int conceptLength;
    
    public ScoreThreeValued(int conceptLength,
    		SortedSet<Individual> posClassified,
    		SortedSet<Individual> neutClassified,
    		SortedSet<Individual> negClassified,
    		SortedSet<Individual> posExamples,
    		SortedSet<Individual> neutExamples,
    		SortedSet<Individual> negExamples) {
    	this.conceptLength = conceptLength;
    	this.posClassified = posClassified;
    	this.neutClassified = neutClassified;
    	this.negClassified = negClassified;
    	this.posExamples = posExamples;
    	this.neutExamples = neutExamples;
    	this.negExamples = negExamples;
    	nrOfExamples = posExamples.size()+negExamples.size();
    	computeClassificationMatrix();
    	computeStatistics();
    }
    
    private void computeClassificationMatrix() {
        posAsNeg = Helper.intersection(posExamples,negClassified);
        negAsPos = Helper.intersection(negExamples,posClassified);
        posAsNeut = Helper.intersection(posExamples,neutClassified);
        neutAsPos = Helper.intersection(neutExamples,posClassified);
        neutAsNeg = Helper.intersection(neutExamples,negClassified);
        negAsNeut = Helper.intersection(negExamples,neutClassified);
        // die 3 Berechnungen sind nicht so wichtig f�r die Punktzahl, d.h. falls
        // es Performance bringt, dann kann man sie auch ausgliedern
        posAsPos = Helper.intersection(posExamples,posClassified);
        negAsNeg = Helper.intersection(negExamples,negClassified);
        neutAsNeut = Helper.intersection(neutExamples,neutClassified);     	
    }
    
    private void computeStatistics() {     
        score = - posAsNeg.size()*Config.errorPenalty
        - negAsPos.size()*Config.errorPenalty
        - posAsNeut.size()*Config.accuracyPenalty;
        
        if(Config.scoreMethod==ScoreMethod.FULL)
        	score -= negAsNeut.size()*Config.accuracyPenalty;
        
        if(Config.penalizeNeutralExamples)
        	score -= (neutAsPos.size()*Config.accuracyPenalty        
            + neutAsNeg.size()*Config.accuracyPenalty);
        
        // TODO: man könnte hier statt error penality auch accuracy penalty
        // nehmen
        double worstValue = nrOfExamples * Config.errorPenalty;
        // ergibt Zahl zwischen -1 und 0
        score = score / worstValue;
        score -= Config.percentPerLengthUnit * conceptLength;
        
        // die folgenden Berechnungen k�nnten aus Performancegr�nden auch
        // ausgegliedert werden
        // int domainSize = abox.domain.size();
        int numberOfExamples = posExamples.size()+negExamples.size();
        int domainSize = numberOfExamples + neutExamples.size(); 
        int correctlyClassified = posAsPos.size() + negAsNeg.size() + neutAsNeut.size();
        int correctOnExamples = posAsPos.size() + negAsNeg.size();
        int errors = posAsNeg.size() + negAsPos.size();
        
        // Accuracy = Quotient von richtig klassifizierten durch Anzahl Domainelemente
        accuracy = (double) correctlyClassified/domainSize;
        
        // Accuracy on Examples = Quotient von richtig klassifizierten durch Anzahl pos.
        // und neg. Beispiele
        accuracyOnExamples = (double) correctOnExamples/numberOfExamples;
        
        accuracyOnPositiveExamples = (double) posAsPos.size()/posExamples.size(); 
        
        // Error = Quotient von komplett falsch klassifizierten durch Anzahl pos.
        // und neg. Beispiele 
        errorRate = (double) errors/numberOfExamples;
    }

    @Override
    public double getScore() {
        return score;
    }
    
    @Override
    public String toString() {
        DecimalFormat df = new DecimalFormat("0.00");
        String str = "";
        str += "score method ";
        if(Config.scoreMethod == ScoreMethod.FULL)
        	str += "full";
        else
        	str += "positive";
        if(!Config.penalizeNeutralExamples)
        	str += " (neutral examples not penalized)";
        str += "\n";
        if(Config.showCorrectClassifications) {
            str += "Correctly classified:\n";
            str += "  positive --> positive: " + posAsPos + "\n";
            str += "  neutral --> neutral: " + neutAsNeut + "\n";
            str += "  negative --> negative: " + negAsNeg + "\n";
        }
        str += "Inaccurately classified (penalty of " + df.format(Config.accuracyPenalty) + " per instance):\n";
        str += "  positive --> neutral: " + posAsNeut + "\n";
        if(Config.penalizeNeutralExamples) {
        	str += "  neutral --> positive: " + neutAsPos + "\n";  
        	str += "  neutral --> negative: " + neutAsNeg + "\n";
        }
        if(Config.scoreMethod == ScoreMethod.FULL)
        	str += "  negative --> neutral: " + negAsNeut + "\n"; 
        str += "Classification errors (penalty of " + df.format(Config.errorPenalty) + " per instance):\n";
        str += "  positive --> negative: " + posAsNeg + "\n";
        str += "  negative --> positive: " + negAsPos + "\n";
        str += "Statistics:\n";
        str += "  Score: " + df.format(score) + "\n";
        str += "  Accuracy: " + df.format(accuracy*100) + "%\n";
        str += "  Accuracy on examples: " + df.format(accuracyOnExamples*100) + "%\n";
        str += "  Accuracy on positive examples: " + df.format(accuracyOnPositiveExamples*100) + "%\n";        
        str += "  Error rate: " + df.format(errorRate*100) + "%\n";
        return str;
    }

	public SortedSet<Individual> getNegClassified() {
		return negClassified;
	}

	public SortedSet<Individual> getPosClassified() {
		return posClassified;
	}

	@Override
	public Set<Individual> getCoveredNegatives() {
		return negAsPos;
	}

	@Override
	public Set<Individual> getCoveredPositives() {
		return posAsPos;
	}
	
	@Override
	public Set<Individual> getNotCoveredPositives() {
		return posAsNeg;
	}

	@Override
	public Score getModifiedLengthScore(int newLength) {
		return new ScoreThreeValued(newLength, posClassified, neutClassified, negClassified, posExamples, neutExamples, negExamples);
	}	
    
}
