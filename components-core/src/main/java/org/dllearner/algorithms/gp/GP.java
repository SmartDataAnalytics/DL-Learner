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

package org.dllearner.algorithms.gp;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import org.dllearner.algorithms.hybridgp.Psi;
import org.dllearner.core.AbstractCELA;
import org.dllearner.core.AbstractLearningProblem;
import org.dllearner.core.AbstractReasonerComponent;
import org.dllearner.core.options.BooleanConfigOption;
import org.dllearner.core.options.ConfigEntry;
import org.dllearner.core.options.ConfigOption;
import org.dllearner.core.options.DoubleConfigOption;
import org.dllearner.core.options.IntegerConfigOption;
import org.dllearner.core.options.InvalidConfigOptionValueException;
import org.dllearner.core.options.StringConfigOption;
import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Thing;
import org.dllearner.learningproblems.EvaluatedDescriptionPosNeg;
import org.dllearner.learningproblems.PosNegLP;
import org.dllearner.learningproblems.ScorePosNeg;
import org.dllearner.utilities.Helper;

/**
 * This class implements the genetic programming (GP) algorithm and provides
 * methods to configure and run it.
 * 
 * @author Jens Lehmann
 * 
 */
public class GP extends AbstractCELA {
	
//	private GPConfigurator configurator;
//	@Override
//	public GPConfigurator getConfigurator(){
//		return configurator;
//	}
	
	
    // NumberFormat f;
	DecimalFormat df = new DecimalFormat("0.00");
 
	public enum AlgorithmType {
        /**
         * This is a type of algorithm, where the offspring completely replaces the
         * previous generation.
         */    	
    	GENERATIONAL,
        /**
         * In this type of algorithm offspring is produced by a number of indivuals.
         * The offspring then replaces the weakest individuals of the previous
         * generation.
         */    	
    	STEADY_STATE};     
    
    public enum SelectionType {
        /**
         * Rank selection is a selection type, where the probability of being
         * selected depends only on the order of the fitness of all individuals.
         */
    	RANK_SELECTION, 
        /**
         * FPS (Fitness Proportionate Selection) is a selection type, where the
         * probability of being selected is proportional to the fitness of an
         * individual.
         */    	
    	FPS,
    	TOURNAMENT_SELECTION};
	
    // configuration options
    private SelectionType selectionType = SelectionType.RANK_SELECTION;
	private int tournamentSize = 3;
	private boolean elitism = true;
	private AlgorithmType algorithmType = AlgorithmType.STEADY_STATE;
	private double mutationProbability = 0.03d;
	private double crossoverProbability = 0.95d;
	private double hillClimbingProbability = 0.0d;
	private double refinementProbability = 0.0;
	private int numberOfIndividuals = 100;
	private int numberOfSelectedIndividuals = 96;
	private boolean useFixedNumberOfGenerations = false;
	private int generations = 20;
	private int postConvergenceGenerations = 50;
	private boolean adc = false;
	private int initMinDepth = 4;
	private int initMaxDepth = 6;
	private int maxConceptLength = 75;
//	private boolean useMultiStructures = true;    	
    	
    private Program[] individuals;
    
    private Program fittestIndividual;
    
    public int fittestIndividualGeneration;

    private Comparator<Program> fitnessComparator;
    
    private static Random rand = new Random();
    
    private long startTime;

    private ScorePosNeg bestScore;
    private Description bestConcept;
    
    // private GeneticRefinementOperator psi;
    private Psi psi;
    
    
    /**
     * Creates an algorithm object. By default a steady state algorithm with
     * rank selection is used. This operates
     * on a population of 1000 individuals with a probability of crossover of
     * 1.0 and a probability of mutation of 0.01.
     * 
     */
    public GP(PosNegLP learningProblem, AbstractReasonerComponent rs) {
       	super(learningProblem, rs);
//    	this.configurator = new GPConfigurator(this);
    }
      
	public static String getName() {
		return "genetic programming learning algorithm";
	} 	    
    
	public static Collection<Class<? extends AbstractLearningProblem>> supportedLearningProblems() {
		Collection<Class<? extends AbstractLearningProblem>> problems = new LinkedList<Class<? extends AbstractLearningProblem>>();
		problems.add(PosNegLP.class);
		return problems;
	}	
	
	public static Collection<ConfigOption<?>> createConfigOptions() {
		Collection<ConfigOption<?>> options = new LinkedList<ConfigOption<?>>();
		StringConfigOption selectionType = new StringConfigOption("selectionType", "selection type", "rankSelection");
		selectionType.setAllowedValues(new String[] {"rankSelection", "fps", "tournament"});
		options.add(selectionType);
		IntegerConfigOption tournamentSize = new IntegerConfigOption("tournamentSize", "tournament size (applies only to tournament selection)", 3);
		tournamentSize.setLowerLimit(2);
		tournamentSize.setUpperLimit(20);
		options.add(tournamentSize);
		options.add(new BooleanConfigOption("elitism", "specifies whether to use elitism in selection", true));
		StringConfigOption algorithmType = new StringConfigOption("algorithmType", "algorithm type", "steadyState");
		algorithmType.setAllowedValues(new String[] {"generational", "steadyState"});
		options.add(algorithmType);
		DoubleConfigOption mutationProb = new DoubleConfigOption("mutationProbability", "mutation probability", 0.03);
		mutationProb.setLowerLimit(0.0);
		mutationProb.setUpperLimit(1.0);
		options.add(mutationProb);
		DoubleConfigOption crossoverProb = new DoubleConfigOption("crossoverProbability", "crossover probability", 0.95);
		crossoverProb.setLowerLimit(0.0);
		crossoverProb.setUpperLimit(1.0);
		options.add(crossoverProb);
		DoubleConfigOption hillClimbingProb = new DoubleConfigOption("hillClimbingProbability", "hill climbing probability", 0.0);
		hillClimbingProb.setLowerLimit(0.0);
		hillClimbingProb.setUpperLimit(1.0);
		options.add(hillClimbingProb);
		DoubleConfigOption refinementProb = new DoubleConfigOption("refinementProbability", "refinement operator probability (values higher than 0 turn this into a hybrid GP algorithm - see publication)", 0.00);
		refinementProb.setLowerLimit(0.0);
		refinementProb.setUpperLimit(1.0);
		options.add(refinementProb);
		IntegerConfigOption numberOfInd = new IntegerConfigOption("numberOfIndividuals", "number of individuals", 100);
		numberOfInd.setLowerLimit(1);
		options.add(numberOfInd);
		IntegerConfigOption numberOfSelInd = new IntegerConfigOption("numberOfSelectedIndividuals", "number of selected individuals", 92);
		numberOfSelInd.setLowerLimit(1);
		options.add(numberOfSelInd);
		options.add(new BooleanConfigOption("useFixedNumberOfGenerations", "specifies whether to use a fixed number of generations", false));
		IntegerConfigOption gen = new IntegerConfigOption("generations", "number of generations (only valid if a fixed number of generations is used)", 20);
		gen.setLowerLimit(1);
		options.add(gen);
		IntegerConfigOption post = new IntegerConfigOption("postConvergenceGenerations", "number of generations after which to stop if no improvement wrt. the best solution has been achieved", 50);
		post.setLowerLimit(1);
		options.add(post);		
		options.add(new BooleanConfigOption("adc", "whether to use automatically defined concept (this invents new helper concepts, but enlarges the search space", false));
		IntegerConfigOption initMin = new IntegerConfigOption("initMinDepth", "minimum depth to use when creating the initial population", 4);
		initMin.setLowerLimit(1);
		options.add(initMin);
		IntegerConfigOption initMax = new IntegerConfigOption("initMaxDepth", "maximum depth to use when creating the initial population", 6);
		initMax.setLowerLimit(1);
		options.add(initMax);		
		IntegerConfigOption max = new IntegerConfigOption("maxConceptLength", "maximum concept length (higher length means lowest possible fitness)", 75);
		max.setLowerLimit(1);
		options.add(max);		
//		options.add(new BooleanConfigOption("useMultiStructures", "specifies whether to use e.g. (a AND b AND c) instead of ((A and b) and c) - there is no apparent reason to set this to false", true));
		return options;
	}
	
	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#applyConfigEntry(org.dllearner.core.ConfigEntry)
	 */
	@Override
	public <T> void applyConfigEntry(ConfigEntry<T> entry) throws InvalidConfigOptionValueException {
		String name = entry.getOptionName();
		if(name.equals("selectionType")) {
			String value = (String) entry.getValue();
			if(value.equals("fps"))
				selectionType = SelectionType.FPS;
			else if(value.equals("tournament"))
				selectionType = SelectionType.TOURNAMENT_SELECTION;
			else
				selectionType = SelectionType.RANK_SELECTION;
		} else if(name.equals("tournamentSize")) {
			tournamentSize = (Integer) entry.getValue();
		} else if(name.equals("elitism")) {
			elitism = (Boolean) entry.getValue();
		} else if(name.equals("algorithmType")) {
			String value = (String) entry.getValue();
			if(value.equals("generational"))
				algorithmType = AlgorithmType.GENERATIONAL;
			else 
				algorithmType = AlgorithmType.STEADY_STATE;
		} else if(name.equals("mutationProbability")) {
			mutationProbability = (Double) entry.getValue();
		} else if(name.equals("crossoverProbability")) {
			crossoverProbability = (Double) entry.getValue();
		} else if(name.equals("refinementProbability")) {
			refinementProbability = (Double) entry.getValue();
		} else if(name.equals("hillClimbingProbability")) {
			hillClimbingProbability = (Double) entry.getValue();
		} else if(name.equals("numberOfIndividuals")) {
			numberOfIndividuals = (Integer) entry.getValue();
		} else if(name.equals("numberOfSelectedIndividuals")) {
			numberOfSelectedIndividuals = (Integer) entry.getValue();
		} else if(name.equals("useFixedNumberOfGenerations")) {
			useFixedNumberOfGenerations = (Boolean) entry.getValue();
		} else if(name.equals("generations")) {
			generations = (Integer) entry.getValue();
		} else if(name.equals("postConvergenceGenerations")) {
			postConvergenceGenerations = (Integer) entry.getValue();
		} else if(name.equals("adc")) {
			adc = (Boolean) entry.getValue();
		} else if(name.equals("initMinDepth")) {
			initMinDepth = (Integer) entry.getValue();
		} else if(name.equals("initMaxDepth")) {
			initMaxDepth = (Integer) entry.getValue();
		} else if(name.equals("maxConceptLength")) {
			maxConceptLength = (Integer) entry.getValue();
		}		
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.Component#init()
	 */
	@Override
	public void init() {
//		reasoner.prepareSubsumptionHierarchy();
//		reasoner.prepareRoleHierarchy();		
	}
	
	@Override
    public void start() {   	
    	// falls refinement-Wahrscheinlichkeit größer 0, dann erzeuge psi
    	psi = new Psi((PosNegLP)learningProblem, reasoner);
    	
    	System.out.println();
    	System.out.println("Starting Genetic Programming Learner");
    	System.out.println();
    	
    	System.out.println("Settings:");
    	System.out.println("algorithm type: " + algorithmType);
    	System.out.print("selection type: " + selectionType);
    	if(elitism)
    		System.out.println(" (elitism activated)");
    	else
    		System.out.println();
    	System.out.println("number of individuals: " + numberOfIndividuals);
    	if(algorithmType == AlgorithmType.STEADY_STATE)
    		System.out.println("number of selected individuals: " + numberOfSelectedIndividuals);
    	System.out.println("probability of crossover: " + df.format(crossoverProbability*100) + "%");
    	System.out.println("probability of mutation: " + df.format(mutationProbability*100) + "%");    	
    	System.out.println("probability of hill climbing: " + df.format(hillClimbingProbability*100) + "%");
    	System.out.println("probability of refinement: " + df.format(refinementProbability*100) + "%");     	
    	System.out.println("number of post convergence generations: " + postConvergenceGenerations);
    	System.out.println();
    	
        // represents the individuals in the current run
        individuals = new Program[numberOfIndividuals];

        if (algorithmType == AlgorithmType.GENERATIONAL) {
            if (elitism) {
                numberOfSelectedIndividuals = numberOfIndividuals - 1;
                // ensure that number of selected individuals is even
                if (numberOfSelectedIndividuals % 2 == 1)
                    error("Number of Individuals must be odd when elitism "
                            + "is used in a generational algorithm");

            } else {
                numberOfSelectedIndividuals = numberOfIndividuals;
                if (numberOfSelectedIndividuals % 2 == 1)
                    error("Number of Individuals must be even when elitism "
                            + "is not used in a generational algorithm");
            }
        }

        int numberOfNewIndividuals;
        if (elitism)
            numberOfNewIndividuals = numberOfSelectedIndividuals + 1;
        else
            numberOfNewIndividuals = numberOfSelectedIndividuals;

        // perform some simple checks to see if configured values make sense
        if (numberOfIndividuals < 2)
            error("Number of individuals must be at least 2.");
        // if (numberOfIndividuals % 8 != 0)
        //    error("The number of individuals must be divisible by 8.");        
        if (numberOfSelectedIndividuals % 2 == 1)
            error("The number of selected individuals must be even.");
        if ((numberOfSelectedIndividuals < 2)
                || (numberOfSelectedIndividuals > numberOfIndividuals))
            error("Number of selected individuals should be between 2 and "
                    + numberOfIndividuals);

        // a comparator for comparing two programs according to their fitness
        fitnessComparator = new Comparator<Program>() {
            public int compare(Program p1, Program p2) {
                double diff = p1.getFitness() - p2.getFitness();
                if (diff > 0)
                    return 1;
                else if (diff < 0)
                    return -1;
                else
                    return 0;
            }
        };

        startTime = System.nanoTime();        
        
        // System.out.println("If Java does not allocate enough memory on your system\n" +
        //        "use \"java -Xmx128m Start\" to start the program. This allocates\n" +
        //        "a maximum of 128 MB of memory. 64 MB are sufficient so it is usually\n" +
        //        "not necessary to do this.\n");
        
        createIndividuals();

        // keep track of the fittest individual
        // Arrays.sort(individuals, fitnessComparator);
        fittestIndividual = getFittestIndividual(); //individuals[0];
        fittestIndividualGeneration = 0;

        // create a population of individuals and print initial statistics
        System.out.println("Initial Population:");
        printStatistics(fittestIndividual);

        // variables used in the inner loop of the algorithm
        int[] selectedIndividuals = new int[numberOfSelectedIndividuals];
        Program[] newIndividuals = new Program[numberOfNewIndividuals];
        Program[] tmp = new Program[2];

        // long startTime = System.currentTimeMillis();


        int generation = 0;

        // MAIN LOOP
        do {

            // sort individuals if necessary
            if (selectionType == SelectionType.RANK_SELECTION
                    || algorithmType == AlgorithmType.STEADY_STATE)
                Arrays.sort(individuals, fitnessComparator);          
            
            boolean showIndividuals = false;
            
            // Individuen ausgeben
            if(showIndividuals) {
	            System.out.println("GENERATION " + generation);
	            for(Program p : individuals) {
	            	// TODO: rausfinden warum es Inkonsistenzen bei hybrid GP gibt
	            	// => liegt daran, dass DIG- und KAON2Reasoner bei getAtomicConcepts()
	            	// nicht klonen, sondern die Referenzen zurückgeben; dadurch können
	            	// die parent Links falsch sein; für hybrid GP nicht schlimm, aber für
	            	// gemischte Varianten schon
	            	// => alleine dort zu klonen reicht aber nicht, es müsste stattdessen
	            	// bei der Baumerzeugung geklont werden
	            	// GPUtilities.checkProgram(p);
	            	// if(generation % 5 == 0)
	            	System.out.println(p.getFitness() + " " + p.getTree());
	            	// System.out.println("      ADC " + p.getAdc());
	            }
	            System.out.println("<===>");
            }
            
            // apply the configured selection algorithm
            selectedIndividuals = selectIndividuals(generation);

            // produce offspring
            for (int i = 0; i < numberOfSelectedIndividuals; i++) {
            	double rand = Math.random();
            	
            	double crossoverBoundary = crossoverProbability;
            	double mutationBoundary = crossoverBoundary + mutationProbability;
            	double hillClimbingBoundary = mutationBoundary + hillClimbingProbability;
            	double refinementBoundary = hillClimbingBoundary + refinementProbability;
            	
            	// wenn nur noch ein Individual zur Auswahl ist, dann
            	// darf Crossover nicht genommen werden (falls es ausgewaehlt wird,
            	// dann wird stattdessen reproduction genommen)
                if (rand < crossoverBoundary && i+1 != numberOfSelectedIndividuals) {
                    // crossover
                    tmp = GPUtilities.crossover(learningProblem,
                            individuals[selectedIndividuals[i]],
                            individuals[selectedIndividuals[i + 1]]);

                    //System.out.println(tmp[0].getTree());
                    
                    newIndividuals[i] = tmp[0];
                    newIndividuals[i + 1] = tmp[1];
                    
                    // Incrementing i here is a significant code change! (2007/08/31)
                    // This is done, because crossover uses two individuals as input and 
                    // outputs two. Without the increment that second produced child is 
                    // overwritten, which caused a significant loss in performance.
                    i++;
                // mutation
                }  else if(rand >= crossoverBoundary && rand < mutationBoundary) {
                	newIndividuals[i] = GPUtilities.mutation(learningProblem, reasoner, individuals[selectedIndividuals[i]]);
                // hill climbing
                } else if(rand >= mutationBoundary && rand < hillClimbingBoundary) {
                	// System.out.println("hill climbing");
                	newIndividuals[i] = GPUtilities.hillClimbing(learningProblem, reasoner, individuals[selectedIndividuals[i]]);
                // refinement operator
                } else if(rand >= hillClimbingBoundary && rand < refinementBoundary) {
                	newIndividuals[i] = psi.applyOperator(individuals[selectedIndividuals[i]]);
                // reproduction
                } else {
                	newIndividuals[i] = individuals[selectedIndividuals[i]];
                }
                
                /* alter Code
                else {
                    // reproduction
                    newIndividuals[i] = individuals[selectedIndividuals[i]];
                    newIndividuals[i + 1] = individuals[selectedIndividuals[i + 1]];
                }

                // mutate individuals
                if (Math.random() < mutationProbability) {
                	// System.out.println(newIndividuals[i].getFitness() + " " +  newIndividuals[i].getTree());
                    newIndividuals[i] = Utilities.mutation(newIndividuals[i]);
                    // System.out.println(newIndividuals[i].getFitness() + " " +  newIndividuals[i].getTree());
                    // System.out.println("====");
                }

                if (Math.random() < mutationProbability)
                    newIndividuals[i+1] = Utilities.mutation(newIndividuals[i + 1]);
                */
            }

            // update fittest individual
            Program chr = getFittestIndividual();
            if (chr.getFitness() > fittestIndividual.getFitness()) {
                fittestIndividual = chr;
                fittestIndividualGeneration = generation;
            }
            // update fittest individual
            /*
            Program chr2 = getFittestValidIndividual();
            if (chr2.getFitness() > fittestValidIndividual.getFitness()) {
                fittestValidIndividual = chr2;
                fittestValidIndividualGeneration = generation;
            }
            */

            // if elitism is used, the fittest individual is copied over to the
            // new generation automatically
            if (elitism)
                newIndividuals[numberOfNewIndividuals - 1] = fittestIndividual; 
            
            // the steady state algorithm replaces the weakest individuals by
            // the new ones (note that the individuals are ordered by fitness,
            // so the new individuals can just be copied over at the start of
            // the array)
            if (algorithmType == AlgorithmType.STEADY_STATE)
                System.arraycopy(newIndividuals, 0, individuals, 0,
                        numberOfNewIndividuals);
            // the generational algorithm replaces the whole generation
            else
                System.arraycopy(newIndividuals, 0, individuals, 0,
                        numberOfIndividuals);
            
            if (generation % 5 == 0) {
                System.out.println("Generation " + generation);
                printStatistics(fittestIndividual);
            }
            
            // alle Individuen auf maximale Konzeptlänge überprüfen um mögliche
            // Speicherprobleme zu verhindern
            for(int i=0; i<numberOfIndividuals; i++) {
            	if(individuals[i].getTree().getLength()>maxConceptLength) {
            		System.out.println("Warning: GP produced concept longer then " + maxConceptLength + ". Replacing it with TOP.");
            		individuals[i] = GPUtilities.createProgram(learningProblem, new Thing());
            	}            		
            }
            
            generation++;
        } while ( (useFixedNumberOfGenerations && generation < generations)
        	|| (!useFixedNumberOfGenerations && (generation - fittestIndividualGeneration < postConvergenceGenerations)));

        // fittestIndividual.optimize();        
        
        long endTime = System.nanoTime(); // .currentTimeMillis();
        // R�ckgabewert des Algorithmus speichern
        bestScore = fittestIndividual.getScore();
        bestConcept = fittestIndividual.getTree();

        // nachschauen, ob ev. noch bessere Konzepte im Psi-Cache sind
        boolean betterValueFoundInPsiCache = false;
        double bestValue = bestScore.getScoreValue();
        
        if(refinementProbability > 0) {
        	// das Problem ist hier, dass die gecachte Score nicht unbedingt
        	// der echten Score entsprechen muss, d.h. hier muss die
        	// Konzeptlänge mit einberechnet werden => deswegen werden
        	// neue Score-Objekte generiert
        	Set<Entry<Description,ScorePosNeg>> entrySet = psi.evalCache.entrySet();
        	for(Entry<Description,ScorePosNeg> entry : entrySet) {
        		ScorePosNeg tmpScore = entry.getValue();
        		Description c = entry.getKey();
        		tmpScore = tmpScore.getModifiedLengthScore(c.getLength());
        		double tmpScoreValue = tmpScore.getScoreValue();
        		if(tmpScoreValue>bestValue) {
        			bestValue = tmpScoreValue;
        			betterValueFoundInPsiCache = true;
        			bestScore = tmpScore;
        			bestConcept = c;
        		}
        			
        	}
        }
        
        System.out.println("final report");
        System.out.println("============");
        // System.out.println("fittest individual: " + fittestIndividual.getTree());
        System.out.println("generations: " + generation);
        System.out.println("fittest individual found after "
                + fittestIndividualGeneration + " generations");
        System.out.println("runtime in ms: " + Helper.prettyPrintNanoSeconds(endTime - startTime));
        System.out.println("fitness evaluations: "
                + GPUtilities.fitnessEvaluations);
        if(refinementProbability > 0) {
        	System.out.println("operator applications: " + psi.getNrOfRequests() + " psi, " + GPUtilities.crossover + " crossover, " +
        			GPUtilities.mutation + " mutation, " + GPUtilities.hillClimbing + " hillClimbing");
        }
        
        //System.out.println("invalid-to-valid transformations by FROG optimization: "
        //        + Program.treesOptimized); 
        System.out.println();
        printStatistics(fittestIndividual);
        System.out.println(fittestIndividual.getScore());
        if(betterValueFoundInPsiCache) {
        	System.out.println("Found better solution in Psi-Cache:");
        	System.out.println(bestConcept);
        	int misClassifications = bestScore.getNotCoveredPositives().size()+
        	bestScore.getCoveredNegatives().size();
        	System.out.println("misclassifications: " + misClassifications + ", length " + bestConcept.getLength());
        }
          
        /*
        Collection<Concept> test = ReasonerComponent.retrievals;
//         for(Concept c : )
        
        test.removeAll(psi.evalCache.keySet());
        for(Concept c : test)
        	System.out.println(c);
        */
    }

    // Anmerkung: beim Lernen von DLs ist es besonders wichtig, dass schon bei der
    // Initialisierung gute Teilbaeume herauskommen, d.h. nicht notwendigerweise Teilbaeume
    // der gewuenschten Loesung, aber Baeume die solche Teilbaeume enthalten und außerdem
    // Fehlklassifikationen vermeiden
    private void createIndividuals() {
        // trees are created by the ramped half and half method with a tree depth
        // between 6 and 9
    	
    	// Lookuptable generieren
    	double[] functionValues = new double[initMaxDepth-initMinDepth+1];
    	double functionSum = 0;
    	double functionValue;
    	for(int i=initMinDepth; i<=initMaxDepth; i++) {
    		functionValue = initFunction(i);
    		functionSum += functionValue;
    		functionValues[i-initMinDepth] = functionSum;
    	}
    	
    	// function-based-half-and-half
    	for(int i = 0; i< numberOfIndividuals; i++) {
        	boolean grow = (Math.random()>0.5);
        	
        	int depth = getLookupTablePosition(functionValues,rand.nextDouble()*functionSum) + initMinDepth;
        	// int depth = rand.nextInt(initMaxDepth-initMinDepth)+initMinDepth;
        	
        	if(grow)
        		individuals[i] = GPUtilities.createGrowRandomProgram(learningProblem, reasoner, depth, adc);
        	else
        		individuals[i] = GPUtilities.createFullRandomProgram(learningProblem, reasoner, depth, adc);		
    	}    	
    	
    	/*
    	for(int i = 0; i< numberOfIndividuals; i++) {
        	double nr = Math.random();
        	if(Math.random()>0.5) {
        		if(nr<1/4d)
        			individuals[i]= GPUtilities.createFullRandomProgram(learningProblem, 2);
        		else if(nr<2/4d)
        			individuals[i]= GPUtilities.createFullRandomProgram(learningProblem, 3);
        		else if(nr<3/4d)
        			individuals[i]= GPUtilities.createFullRandomProgram(learningProblem, 4);
        		else
        			individuals[i]= GPUtilities.createFullRandomProgram(learningProblem, 5);
        	} else {
        		if(nr<1/4d)
        			individuals[i]= GPUtilities.createGrowRandomProgram(learningProblem,3);
        		else if(nr<2/4d)
        			individuals[i]= GPUtilities.createGrowRandomProgram(learningProblem,4);
        		else if(nr<3/4d)
        			individuals[i]= GPUtilities.createGrowRandomProgram(learningProblem,5);
        		else
        			individuals[i]= GPUtilities.createGrowRandomProgram(learningProblem,6);        		
        	}    		
    	}
    	*/
    	
    	
    	/*
        for (int i = 0; i < numberOfIndividuals / 2; i = i + 4) {
            individuals[i] = Utilities.createFullRandomProgram(2);
            individuals[i + 1] = Utilities.createFullRandomProgram(3);
            individuals[i + 2] = Utilities.createFullRandomProgram(4);
            individuals[i + 3] = Utilities.createFullRandomProgram(5);
        }

        for (int i = numberOfIndividuals / 2; i < numberOfIndividuals; i = i + 4) {
            individuals[i] = Utilities.createGrowRandomProgram(3);
            individuals[i + 1] = Utilities.createGrowRandomProgram(4);
            individuals[i + 2] = Utilities.createGrowRandomProgram(5);
            individuals[i + 3] = Utilities.createGrowRandomProgram(6);
        }
        */
    }
    
    private double initFunction(int x) {
    	// Funktion f(x)=x
    	return 1;
    }

    private int getIndividualsForRankSelection(int generation) {
        // return a value how many of the best individuals will actually
        // be used by rank selection (in the standard version this is
        // numberOfIndividuals)
        /*
        double factor = 0.5*(Math.tanh(0.125*(generation-30))+1);
        if(factor*generation<20)
            return 50;
        else
            return (int)Math.round(factor*generation);
        */
        return numberOfSelectedIndividuals;
    }

    // es wird einfach nur ein Array von Positionen zur�ckgegeben, d.h. es
    // werden keine Individuen kopiert etc.
    // TODO: man muss �berlegen, ob es Vorteile hat hier direkt Programme zu
    // kopieren => dann m�sste man bei Crossover und Mutation nicht aufpassen,
    // dass man keine Programmb�ume modifiziert
    private int[] selectIndividuals(int generation) {
        int[] positions = new int[numberOfSelectedIndividuals];
        if (selectionType == SelectionType.FPS) {

            // select individuals according to FPS
            double[] lookupTable = getFitnessLookupTable(false);
            double fitnessSum = lookupTable[numberOfIndividuals - 1];
            double rand;

            for (int i = 0; i < numberOfSelectedIndividuals; i++) {
                // create a random number between 0 and fitness sum
                rand = Math.random() * fitnessSum;
                positions[i] = getLookupTablePosition(lookupTable, rand);
            }
        } else if (selectionType == SelectionType.RANK_SELECTION) {
            int individualsForRankSelection = getIndividualsForRankSelection(generation);
            double[] lookupTable = new double[individualsForRankSelection];
            double sum = 0;
            double rand, val;
            // create a lookup-table
            for (int i = 0; i < individualsForRankSelection; i++) {
                // you can use any function here
            	// fittestes Individuum 10mal wahrscheinlicher als schlechtestes
            	// Individuum (relativ schwache Selektion für MLDM-Paper, da
            	// im hybrid GP immer stetig Richtung Lösung gegangen wird, also
            	// es sich oft auch lohnt weniger fitte Individuen zu wählen)
                val = i + individualsForRankSelection / 9; // 8
            	// einfachste rank funktion
            	// val = i;
                sum += val;
                lookupTable[i] = sum;
            }

            for (int i = 0; i < numberOfSelectedIndividuals; i++) {
                // create a random number between 0 and sum
                rand = Math.random() * sum;
                positions[i] = numberOfIndividuals - individualsForRankSelection
                        + getLookupTablePosition(lookupTable, rand);
            }
        } else if (selectionType == SelectionType.TOURNAMENT_SELECTION) {
        	for(int i = 0; i < numberOfSelectedIndividuals; i++) {
            	// choose the appropriate number of individuals randomly
            	int[] randomIndividuals = new int[tournamentSize];
            	for(int j=0; j<tournamentSize; j++) {
            		randomIndividuals[j] = rand.nextInt(numberOfIndividuals);
            	}
            	
                double fitness = individuals[randomIndividuals[0]].getFitness();
                int pos = 0;
                for (int j = 1; j < tournamentSize; j++) {
                    if (individuals[randomIndividuals[j]].getFitness() > fitness) {
                        fitness = individuals[randomIndividuals[j]].getFitness();
                        pos = j;
                    }
                }             	
            	positions[i] = pos;
        	}
        }

        return positions;
    }

    private double[] getFitnessLookupTable(boolean allowNegativeFitness) {
        // we sum up the fitness for all individuals and store the sums
        // such that we can later easily determine, which individual we
        // have chosen (the last entry in the lookup table is the sum
        // of the fitness of all individuals)
        double[] lookupTable = new double[numberOfIndividuals];
        double sum = 0, fitness;

        // sum up the fitness for all individuals
        for (int i = 0; i < numberOfIndividuals; i++) {
        	fitness = individuals[i].getFitness();
        	if(!allowNegativeFitness && fitness<0)
        		error("Negative fitness value " + fitness + " in FPS!");
            sum += fitness;
            lookupTable[i] = sum;
        }

        return lookupTable;
    }

    private int getLookupTablePosition(double[] lookupTable, double rand) {
        // look up the position of the random number in the table
        // for (int i = 0; i < numberOfIndividuals; i++) {
        for (int i = 0; i < lookupTable.length; i++) {
            if (rand <= lookupTable[i])
                return i;
        }
        // if no position was found, an error has occured
        throw new Error();
    }

    private double getFitnessSum() {
        double sum = 0;

        // sum up the fitness for all individuals
        for (int i = 0; i < numberOfIndividuals; i++) {
            sum += individuals[i].getFitness();
        }

        return sum;
    }

    private Program getFittestIndividual() {
        double fitness = individuals[0].getFitness();
        int pos = 0;
        for (int i = 1; i < numberOfIndividuals; i++) {
            if (individuals[i].getFitness() > fitness) {
                fitness = individuals[i].getFitness();
                pos = i;
            }
        }
        return individuals[pos];
    }
    
    /*
    private Program getFittestValidIndividual() {
        double fitness = 0;
        // individuals[0].getFitness();
        int pos = 0;
        for (int i = 0; i < numberOfIndividuals; i++) {
            if (individuals[i].getFitness() > fitness
                    && individuals[i].isValidSolution()) {
                fitness = individuals[i].getFitness();
                pos = i;
            }
        }
        return individuals[pos];
    }
    */
    
    private void printStatistics(Program fittestIndividual) {
        // output statistics: best individual, average fitness, etc.
        double averageFitness = getFitnessSum() / numberOfIndividuals;
        Description n = fittestIndividual.getTree();

        int misClassifications = fittestIndividual.getScore().getNotCoveredPositives().size()
        + fittestIndividual.getScore().getCoveredNegatives().size();
        
        System.out.println("average fitness: " + averageFitness);
        System.out.println("highest fitness: " + fittestIndividual.getFitness() + " ["+misClassifications+" misclassifcations, length "+n.getLength()+"]");
        
        // durchschnittliche Konzeptlänge berechnen (wahrscheinlich zeitaufwändig,
        // also nicht standardmäßig machen)
        int conceptLengthSum = 0;
        for(Program p : individuals)
        	conceptLengthSum += p.getTree().getLength();
        double conceptLengthAverage = conceptLengthSum/(double)individuals.length;
        System.out.println("average concept length: " + df.format(conceptLengthAverage));
        // long algorithmTime = System.nanoTime() - Main.getAlgorithmStartTime();
        long algorithmTime = System.nanoTime() - startTime;
        System.out.println("overall algorithm runtime: " + Helper.prettyPrintNanoSeconds(algorithmTime));
        // System.out.println("instance checks: " + learningProblem.getReasonerComponent().getNrOfInstanceChecks());
        // System.out.println("fitness evals: " + Program.fitnessEvaluations);
        // System.out.println("nr. of individuals: " + individuals.length + " (" + numberOfSelectedIndividuals + " selected)");
        
        
        // für temporäre Performance-Werte
        // double some = 100*(psi.someTime/(double)algorithmTime);
        // System.out.println("some: " + df.format(some) + "%");
        
        // System.out.println();
        System.out.println("best definition found: " + n);
        if(refinementProbability > 0) {
        	double cacheHitRate=0;
        	double pdCacheHitRate=0, puCacheHitRate=0;
        	if(psi.getNrOfRequests()>0) {
        		cacheHitRate = 100*(psi.getConceptCacheHits()/(double)psi.getNrOfRequests());
        		pdCacheHitRate = 100*(psi.getPdCacheHits()/(double)psi.getPdRequests());
        		puCacheHitRate = 100*(psi.getPuCacheHits()/(double)psi.getPuRequests());
        	}
        	System.out.println("Psi down cache: " + psi.getPdCache().size() + "; " + psi.getPdRequests() + " requests; hit rate " + df.format(pdCacheHitRate) + "%");
        	System.out.println("Psi up cache: " + psi.getPuCache().size() + "; " + psi.getPuRequests() + " requests; hit rate " + df.format(puCacheHitRate) + "%");
        	System.out.println("Psi cache: size " + psi.getCacheSize() + "; " + psi.getNrOfRequests() + " requests; hit rate " + df.format(cacheHitRate) + "%");
        	
        	double psiTimePercent = 100*psi.getPsiApplicationTimeNs()/(double)algorithmTime;
        	double psiWOReasoningTimePercent = 100*(psi.getPsiApplicationTimeNs()-psi.getPsiReasoningTimeNs())/(double)algorithmTime;
        	// System.out.println(Helper.prettyPrintNanoSeconds(psi.getPsiApplicationTimeNs()));
        	// System.out.println(Helper.prettyPrintNanoSeconds(psi.getPsiReasoningTimeNs()));
        	System.out.println("Psi application time percentage: " + df.format(psiTimePercent) + "%; " + df.format(psiWOReasoningTimePercent) + "% excluding reasoning");
        }
        	
        if(adc)
        	System.out.println("ADC: " + fittestIndividual.getAdc());
        // TODO: hier muss noch eine Zusammenfassung rein, die sowohl f�r zweiwertiges als auch
        // dreiwertiges Lernproblem funktioniert
        // System.out.println("classified positive: " + fittestIndividual.getScore().getDefPosSet());
        // System.out.println("classified negative: " + fittestIndividual.getScore().getDefNegSet());
        System.out.println();
    }

    /**
     * Sets the probability that crossover is performed. If crossover is not
     * performed individuals are cloned, i.e. copied over to the next
     * generation.
     * 
     * @param crossoverProbability
     *            A number between 0 and 1.
     */
    //public void setCrossoverProbability(float crossoverProbability) {
    //    if (crossoverProbability >= 0.0 && crossoverProbability <= 1.0)
    //        this.crossoverProbability = crossoverProbability;
    //    else
    //        error("Probability for crossover must be a value between 0 and 1.");
    //}

    /**
     * Sets the probability that an individual is mutated.
     * 
     * @param mutationProbability
     *            A number between 0 and 1.
     */
    //public void setMutationProbability(float mutationProbability) {
    //    if (mutationProbability >= 0.0 && mutationProbability <= 1.0)
    //        this.mutationProbability = mutationProbability;
    //    else
    //        error("Probability for mutation must be a value between 0 and 1.");
    //}

    /**
     * Switch for turning elitism on or off. Elitism means that the fittest
     * individual is copied over to the next generation automatically and cannot
     * be lost.
     * 
     * @param elitism
     *            True if elitism should be actived, false otherwise.
     */
    //public void setElitism(boolean elitism) {
    //    this.elitism = elitism;
    //}

    /**
     * This method controls how many individuals will actually be selected and
     * transfered to the mating pool in a steady state algorithm (it is ignored
     * in a generational algorithm).
     * 
     * @param numberOfSelectedIndividuals
     *            Number of individuals, which will be selected.
     */
    //public void setNumberOfSelectedIndividuals(int numberOfSelectedIndividuals) {
    //    if (algorithmType == AlgorithmType.STEADY_STATE)
    //        this.numberOfSelectedIndividuals = numberOfSelectedIndividuals;
    //    else
    //        System.out.println("Warning: number of selected individuals "
    //                + "will be ignored in a generational algorithm");
    //}

    private void error(String errorMessage) {
        // prints an error message and exits
        System.out.println("Error: " + errorMessage);
        System.out
                .println("Please correct all errors and run the algorithm again.");
        System.exit(0);
    }

//    @Override
	public ScorePosNeg getSolutionScore() {
		return bestScore;
	}

	@Override
	public Description getCurrentlyBestDescription() {
		return bestConcept;
	}    
    
	@Override
	public EvaluatedDescriptionPosNeg getCurrentlyBestEvaluatedDescription() {
		// return fittestIndividual.getTree();
		return new EvaluatedDescriptionPosNeg(bestConcept,bestScore);
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see org.dllearner.core.LearningAlgorithm#isRunning()
	 */
	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

    /**
     * This allows to set the number of individuals in the whole population. A
     * higher value slows down the algorithm, but will in general improve
     * accuracy of the results.
     * 
     * @param numberOfIndividuals
     *            Number of individuals in the population.
     */
    //public void setNumberOfIndividuals(int numberOfIndividuals) {
    //    this.numberOfIndividuals = numberOfIndividuals;
    //}

    /**
     * The algorithm converges, if it doesn't find a fitter individual after a
     * number of generations. This number controls for how many generations the
     * algorithm will try to find a fitter individual.
     * 
     * @param postConvergenceGenerations
     *            A number specifying after how many generations without a new
     *            fittest individual the algorithm should stop.
     */
    //public void setPostConvergenceGenerations(int postConvergenceGenerations) {
    //    this.postConvergenceGenerations = postConvergenceGenerations;
    //}

    /**
     * This method switches the type of selection, which is performed. It can be
     * either Fitness Proportionate Selection (FPS) or Rank Selection.
     * 
     * @param selectionType
     *            Either FPS or RANK_SELECTION.
     */
    //public void setSelectionType(SelectionType selectionType) {
    //    this.selectionType = selectionType;
    //}

    /**
     * This switch controls, which type of algorithm is used.
     * 
     * @param algorithmType
     *            STEADY_STATE or GENERATIONAL.
     */
    //public void setAlgorithmType(AlgorithmType algorithmType) {
    //    this.algorithmType = algorithmType;
    //}
	
}
