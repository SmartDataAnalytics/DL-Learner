package org.dllearner.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;

/**
 * Convert Poker dataset to OWL.
 *
 * @author Lorenz Buehmann
 */
public class PokerDatasetConverter {

    static OWLDataFactory df = OWLManager.getOWLDataFactory();
    static String NS = "http://dl-learner.org/examples/uci/poker#";
    static PrefixManager pm = new DefaultPrefixManager(NS);

    static final AtomicInteger index = new AtomicInteger();

    public static void main(String[] args) throws Exception {
        if(args.length != 2) {
            throw new RuntimeException("Usage: PokerDatasetConverter <source> <target>");
        }

        OWLOntologyManager man = OWLManager.createOWLOntologyManager();
        OWLOntology ont = man.createOntology();
        
        createSchema(ont);

        String fileName = args[0];

        int n = 400;

        for (int i = 1; i < target.length; i++) {
            final int idx = i;
            index.set(1);
            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

                stream.filter(l -> l.endsWith(String.valueOf(idx)))
                        .limit(n)
                        .forEach(line -> {
                            man.addAxioms(ont, convertLine(line));
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }

            ont.saveOntology(new FileOutputStream(new File(args[1],"poker_" + target[i] + "_" + n + ".owl")));
        }

    }

    static OWLClass hand;
    static OWLClass card;
    static OWLClass suit;
    static OWLClass rank;
    static OWLObjectProperty hasCard;
    static OWLObjectProperty hasRank;
    static OWLObjectProperty hasSuit;
    static OWLAnnotationProperty pokerHand;

    private static void createSchema(OWLOntology ont) {
        OWLOntologyManager man = ont.getOWLOntologyManager();

        hand = df.getOWLClass("Hand", pm);
        card = df.getOWLClass("Card", pm);
        suit = df.getOWLClass("Suit", pm);
        rank = df.getOWLClass("Rank", pm);
        hasCard = df.getOWLObjectProperty("hasCard", pm);
        hasRank = df.getOWLObjectProperty("hasRank", pm);
        hasSuit = df.getOWLObjectProperty("hasSuit", pm);
        pokerHand = df.getOWLAnnotationProperty("pokerHand", pm);

        man.addAxiom(ont, df.getOWLObjectPropertyDomainAxiom(hasRank, card));
        man.addAxiom(ont, df.getOWLObjectPropertyRangeAxiom(hasRank, rank));
        man.addAxiom(ont, df.getOWLFunctionalObjectPropertyAxiom(hasRank));

        man.addAxiom(ont, df.getOWLObjectPropertyDomainAxiom(hasCard, hand));
        man.addAxiom(ont, df.getOWLObjectPropertyRangeAxiom(hasCard, card));
        man.addAxiom(ont, df.getOWLSubClassOfAxiom(hand, df.getOWLObjectExactCardinality(5, hasCard, card)));

        man.addAxiom(ont, df.getOWLObjectPropertyDomainAxiom(hasSuit, card));
        man.addAxiom(ont, df.getOWLObjectPropertyRangeAxiom(hasSuit, suit));
        man.addAxiom(ont, df.getOWLFunctionalObjectPropertyAxiom(hasSuit));


        // suits
        for (int i = 0; i < suits.length; i++) {
            OWLNamedIndividual ind = df.getOWLNamedIndividual(suits[i], pm);
            man.addAxiom(ont, df.getOWLClassAssertionAxiom(suit, ind));
            suitMap.put(String.valueOf(i+1), ind);
        }
        System.out.println(suitMap);

        // ranks
        for (int i = 0; i < ranks.length; i++) {
            OWLNamedIndividual ind = df.getOWLNamedIndividual(ranks[i], pm);
            man.addAxiom(ont, df.getOWLClassAssertionAxiom(rank, ind));
            rankMap.put(String.valueOf(i+1), ind);
        }
        System.out.println(rankMap);

    }

    static Set<OWLAxiom> convertLine(String line) {
        Set<OWLAxiom> axioms = new HashSet<>();

        String[] split = line.split(",");

        int idx = index.incrementAndGet();
        OWLNamedIndividual handInd = df.getOWLNamedIndividual("hand" + idx, pm);
        axioms.add(df.getOWLClassAssertionAxiom(hand, handInd));
        for(int i = 0; i < 5; i++) {
            String suit = split[2*i];
            String rank = split[2*i+1];

            OWLIndividual cardInd = df.getOWLNamedIndividual("card" + idx + "_" + i, pm);
            axioms.add(df.getOWLClassAssertionAxiom(card, cardInd));
            axioms.add(df.getOWLObjectPropertyAssertionAxiom(hasCard, handInd, cardInd));
            axioms.add(df.getOWLObjectPropertyAssertionAxiom(hasRank, cardInd, rankMap.get(rank)));
            axioms.add(df.getOWLObjectPropertyAssertionAxiom(hasSuit, cardInd, suitMap.get(suit)));
        }


        axioms.add(df.getOWLAnnotationAssertionAxiom(pokerHand,
                                                    handInd.getIRI(),
                                                    df.getOWLLiteral(target[Integer.parseInt(split[10])]))
        );

        return axioms;
    }

    static String[] suits = {"hearts", "spades", "diamonds", "clubs"};
    static String[] ranks = {"ace", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king"};
    static String[] target = {"nothing", "one_pair", "two_pairs", "three", "straight", "flush", "full_house", "four", "straight_flush", "royal_flush"};
    static Map<String, OWLIndividual> suitMap = new HashMap<>();
    static Map<String, OWLIndividual> rankMap = new HashMap<>();

}
