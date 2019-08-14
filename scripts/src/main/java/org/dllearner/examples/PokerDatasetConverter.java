package org.dllearner.examples;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import static java.util.stream.Collectors.groupingBy;

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

        String fileName = args[0];

        int n = 50;

        Map<Integer, OWLOntology> ontologies = new HashMap<>();

        for (int i = 0; i < target.length; i++) {// if(i != 4) continue;
            OWLOntology ont = man.createOntology();
            createSchema(ont);
            final int idx = i;
//            index.set(0);
            try (Stream<String> stream = Files.lines(Paths.get(fileName))) {

                stream.filter(l -> l.endsWith(String.valueOf(idx)))
                        .limit(n)
                        .forEach(line -> {
                            man.addAxioms(ont, convertLine(line));
                        });

            } catch (IOException e) {
                e.printStackTrace();
            }

            ontologies.put(i, ont);
            ont.saveOntology(new FileOutputStream(new File(args[1],"poker_" + target[i] + "_" + n + ".owl")));
        }

        // sampling
        int pos = n;
        int negPerClass = n;

        for (int i = 0; i < target.length; i++) {
            int idx = i;
            OWLOntology merge = man.createOntology(ontologies.get(i).getAxioms());
            ontologies.forEach((key, value) -> {
                if (!key.equals(idx)) {
                    man.addAxioms(merge, value.getAxioms());
                }
            });
            merge.saveOntology(new FileOutputStream(new File(args[1],"poker_" + target[i] + "_p" + pos + "-n" + negPerClass + ".owl")));
        }

    }

    static OWLClass hand;
    static OWLClass card;
    static OWLClass suit;
    static OWLClass rank;
    static OWLObjectProperty hasCard;
    static OWLObjectProperty hasRank;
    static OWLObjectProperty sameRank;
    static OWLObjectProperty hasSuit;
    static OWLObjectProperty sameSuit;
    static OWLObjectProperty nextRank;
    static OWLAnnotationProperty pokerHand;

    private static void createSchema(OWLOntology ont) {
        OWLOntologyManager man = ont.getOWLOntologyManager();

        hand = df.getOWLClass("Hand", pm);
        card = df.getOWLClass("Card", pm);
        suit = df.getOWLClass("Suit", pm);
        rank = df.getOWLClass("Rank", pm);

        hasCard = df.getOWLObjectProperty("hasCard", pm);
        hasRank = df.getOWLObjectProperty("hasRank", pm);
        sameRank = df.getOWLObjectProperty("sameRank", pm);
        nextRank = df.getOWLObjectProperty("nextRank", pm);

        hasSuit = df.getOWLObjectProperty("hasSuit", pm);
        sameSuit = df.getOWLObjectProperty("sameSuit", pm);

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
            suitMap.put(i+1, ind);
        }
        System.out.println(suitMap);

        // ranks
        for (int i = 0; i < ranks.size(); i++) {
            OWLNamedIndividual ind = df.getOWLNamedIndividual(ranks.get(i), pm);
            man.addAxiom(ont, df.getOWLClassAssertionAxiom(rank, ind));
            rankMap.put(i+1, ind);
        }
        System.out.println(rankMap);

    }

    static Set<OWLAxiom> convertLine(String line) {
        Set<OWLAxiom> axioms = new HashSet<>();

        String[] split = line.split(",");

        int idx = index.incrementAndGet();
        OWLNamedIndividual handInd = df.getOWLNamedIndividual("hand" + idx, pm);
        axioms.add(df.getOWLClassAssertionAxiom(hand, handInd));

        List<Card> cards = new ArrayList<>();
        for(int i = 0; i < 5; i++) {
            int suitID = Integer.parseInt(split[2*i]);
            int rankID = Integer.parseInt(split[2*i+1]);

            OWLIndividual cardInd = df.getOWLNamedIndividual("card" + idx + "_" + i, pm);
            axioms.add(df.getOWLClassAssertionAxiom(card, cardInd));
            axioms.add(df.getOWLObjectPropertyAssertionAxiom(hasCard, handInd, cardInd));
            axioms.add(df.getOWLObjectPropertyAssertionAxiom(hasRank, cardInd, rankMap.get(rankID)));
            axioms.add(df.getOWLObjectPropertyAssertionAxiom(hasSuit, cardInd, suitMap.get(suitID)));

            cards.add(new Card(cardInd, suitID, rankID));
        }

        // sameSuit
        Map<Integer, List<Card>> suit2Cards = cards.stream().collect(groupingBy(Card::getSuitID));
        suit2Cards.values().stream().filter(l -> l.size() >= 2).forEach(cardList -> {
            Sets.combinations(Sets.newHashSet(cardList), 2).forEach(pair -> {
                Iterator<Card> it = pair.iterator();
                Card c1 = it.next();
                Card c2 = it.next();
                axioms.add(df.getOWLObjectPropertyAssertionAxiom(sameSuit, c1.ind, c2.ind));
                axioms.add(df.getOWLObjectPropertyAssertionAxiom(sameSuit, c2.ind, c1.ind));
            });
        });

        // sameRank
        Map<Integer, List<Card>> rank2Cards = cards.stream().collect(groupingBy(Card::getRankID));
        rank2Cards.values().stream().filter(l -> l.size() >= 2).forEach(cardList -> {
            Sets.combinations(Sets.newHashSet(cardList), 2).forEach(pair -> {
                Iterator<Card> it = pair.iterator();
                Card c1 = it.next();
                Card c2 = it.next();
                axioms.add(df.getOWLObjectPropertyAssertionAxiom(sameRank, c1.ind, c2.ind));
                axioms.add(df.getOWLObjectPropertyAssertionAxiom(sameRank, c2.ind, c1.ind));
            });
        });

       // nextRank
        cards.sort(Comparator
                .comparing(Card::getRankID)
                .thenComparing(Card::getSuitID));

        for (int i = 0; i < cards.size(); i++) {
            Card c1 = cards.get(i);
            int rank1 = c1.getRankID();
            if(rank1 == ranks.size() - 1) {
                rank1 = -1;
            }
            for (int j = i+1; j < cards.size(); j++) {
                Card c2 = cards.get(j);
                int rank2 = c2.getRankID();

                if(rank2 - rank1 == 1) {
                    axioms.add(df.getOWLObjectPropertyAssertionAxiom(nextRank, c1.ind, c2.ind));
                }
            }
        }

        axioms.add(df.getOWLAnnotationAssertionAxiom(pokerHand,
                                                    handInd.getIRI(),
                                                    df.getOWLLiteral(target[Integer.parseInt(split[10])]))
        );

        return axioms;
    }

    static class Card {
        final OWLIndividual ind;
        final int suit;
        final int rank;

        Card(OWLIndividual ind, int suit, int rank) {
            this.ind = ind;
            this.suit = suit;
            this.rank = rank;
        }

        public int getRankID() {
            return rank;
        }

        public int getSuitID() {
            return suit;
        }

        @Override
        public String toString() {
            return suit + ":" + rank;
        }
    }

    static String[] suits = {"hearts", "spades", "diamonds", "clubs"};
    static List<String> ranks = Lists.newArrayList("ace", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten", "jack", "queen", "king");
    static String[] target = {"nothing", "one_pair", "two_pairs", "three", "straight", "flush", "full_house", "four", "straight_flush", "royal_flush"};
    static Map<Integer, OWLIndividual> suitMap = new HashMap<>();
    static Map<Integer, OWLIndividual> rankMap = new HashMap<>();

}
