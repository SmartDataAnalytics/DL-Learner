package org.dllearner.examples;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Transforms UCI-ML-Repository Poker example to a conf file.
 * 
 * @author jl
 *
 */
public class PokerTransformer {

	public static void main(String[] args) {
		PokerTransformer pt = new PokerTransformer();
		pt.transform();
	}
		
	public void transform() {
		
		String learningGoal = "pair";		
		// String learningGoal = "straight";
		
		String fileIn = "files/examples/poker/poker-hand-training-true.data";
		// String fileOut = "files/examples/poker/poker.preconf";
		String fileOut = "release_test/dllearner-2007-04-08/examples/poker/poker_"+learningGoal+".conf";
		
		String yinyangPosFile = "release_test/dllearner-2007-04-08/examples/poker/"+learningGoal+"Positives.txt";
		String yinyangNegFile = "release_test/dllearner-2007-04-08/examples/poker/"+learningGoal+"Negatives.txt";
		String dllExamplesFile = "release_test/dllearner-2007-04-08/examples/poker/"+learningGoal+"_examples.txt";
		
		// Datei öffnen
		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(fileIn));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		// Datei in eine String-Array einlesen 
		List<String> pokerExamples = new LinkedList<String>();
		String line = "";
		int lineNumber = 0;
		while(line!=null && lineNumber<50) {
			try {
				line = in.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(line!=null)
				pokerExamples.add(line);
			
			lineNumber++;
		}
		
		List<PokerHand> hands = new LinkedList<PokerHand>();
		for(String s : pokerExamples) {
			// System.out.println(s);
			StringTokenizer st = new StringTokenizer(s,",");
			
			// 5 Karten einlesen
			PokerCard[] cards = new PokerCard[5];
			for(int i=0; i<=4; i++) {
				String suit = st.nextToken();
				String rank = st.nextToken();
				cards[i] = new PokerCard(new Integer(suit),new Integer(rank));
			}
			
			// Klasse auslesen
			String handType = st.nextToken();
			PokerHand hand = new PokerHand(cards, new Integer(handType));
			
			// prüfen, ob Deck schon existiert (nicht gerade effizient,
			// aber OK)
			boolean handExists = false;
			for(PokerHand d : hands) {
				if(equalDecks(d,hand)) {
					handExists = true;
					break;
				}
			}
			
			if(!handExists) {
				// besondere Bedingungen um mehr straights zu bekommen
				if(learningGoal.equals("straight")) {
					// oversampling um mehr richtige straights zu bekommen
					// (am anfang stehen royal flushes, die auch nicht so
					// ideal zum lernen sind)
					if(hand.getHandType()==4 || Math.random()<0.05) {
						hands.add(hand);
					} 
					
				} else 
					hands.add(hand);
			}
				
		}
		
		// Ausgabe in Datei
		try {
			FileOutputStream fos = new FileOutputStream(fileOut);
			FileOutputStream yyPos = new FileOutputStream(yinyangPosFile);
			FileOutputStream yyNeg = new FileOutputStream(yinyangNegFile);	
			FileOutputStream ex = new FileOutputStream(dllExamplesFile);
			
			// für jede Karte die entsprechende Ausgabe machen
			int handNumber = 0;
			for(PokerHand deck : hands) {
				String handID = "hand" + handNumber;
				
				// Ausgabe der Hand (damit für Reasoner klar ist, dass es
				// sich um Individuen handelt)
				write(fos, "deck("+handID+").");
				
				int cardNumber = handNumber*5;
				PokerCard[] cards = deck.getCards();
				// for(Card card : deck.getCards()) {
				for(int i=0; i<5; i++) {
					PokerCard card = cards[i];
					String cardID = "card" + cardNumber;
					
					// Ausgabe der Karte (damit klar ist, dass es sich um ein
					// Individual handelt)
					write(fos, "card("+cardID+").");
					
					// hasCard-Ausgabe
					write(fos, "hasCard("+handID+"," + cardID + ").");
					
					// suit und rank ausgeben
					write(fos, "hasSuit("+cardID+"," + card.getSuitString()+").");
					write(fos, "hasRank("+cardID+"," + card.getRankString()+").");
					
					// sameSuit, nextRank und sameRank ausgeben
					for(int j=i+1; j<5; j++) {
						PokerCard otherCard = cards[j];
						String otherCardID = "card" + (handNumber*5+j);
						if(card.hasSameSuit(otherCard))
							write(fos, "sameSuit("+cardID+","+otherCardID+").");
						if(card.hasSameRank(otherCard))
							write(fos, "sameRank("+cardID+","+otherCardID+").");	
					}
					
					// nextRank ist nicht symmetrisch, deshalb müssen alle 
					// 4 anderen Karten geprüft werden
					for(int j=0; j<5; j++) {
						PokerCard otherCard = cards[j];
						String otherCardID = "card" + (handNumber*5+j);
						if(card.hasNextRank(otherCard)) {
							// Spezialfall: Ass
							if(card.getRank()==1) {
								// Regel: falls es keinen König gibt, dann darf
								// nextRank geschrieben werden
								if(!hasKing(deck))
									write(fos, "nextRank("+cardID+","+otherCardID+").");
							} else
								write(fos, "nextRank("+cardID+","+otherCardID+").");
						}
					}
					
					write(fos,"");
					
					cardNumber++;
				}
				
				// Lernziel ausgeben (hier: Paar, behinhaltet auch 2 Paar etc.)
				int hand = deck.getHandType();
				
				if(learningGoal.equals("pair")) {
					if(hand==1 || hand==2 || hand==3 || hand==6 || hand==7) { 
						write(fos, "+pair("+handID+").");
						write(yyPos, "http://localhost/foo#"+handID);
						write(ex, "+pair(\"http://localhost/foo#"+handID+"\").");
					} else {
						write(fos, "-pair("+handID+").");
						write(yyNeg, "http://localhost/foo#"+handID);
						write(ex, "-pair(\"http://localhost/foo#"+handID+"\").");
					}
				} else if(learningGoal.equals("straight")) {
					if(hand==4 || hand==8 || hand==9) { 
						write(fos, "+straight("+handID+").");
						write(yyPos, "http://localhost/foo#"+handID);
						write(ex, "+straight(\"http://localhost/foo#"+handID+"\").");
					} else {
						write(fos, "-straight("+handID+").");
						write(yyNeg, "http://localhost/foo#"+handID);
						write(ex, "-straight(\"http://localhost/foo#"+handID+"\").");
					}
				}
				
				write(fos,"");
				
				handNumber++;
			}
			
			// fos.write(content.getBytes());
			fos.close();
		} catch (Exception e) {
			System.out.println(e);
			System.exit(0);
		}
		
	}
	
	private void write(FileOutputStream fos, String content) {
		content += "\n";
		try {
			fos.write(content.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
		
	public boolean equalDecks(PokerHand deck1, PokerHand deck2) {
		PokerCard[] deck1Cards = deck1.getCards();
		for(int i=0; i<5; i++) {
			if(!isInCards(deck1Cards[i],deck2))
				return false;
		}
		return true;
	}
	
	public boolean isInCards(PokerCard card, PokerHand deck) {
		PokerCard[] cards = deck.getCards();
		for(int j=0; j<5; j++) {
			// Test auf Gleichheit
			if(card.getSuit()==cards[j].getSuit() && card.getRank()==cards[j].getRank())
				return true;
		}
		return false;
	}
	
	public boolean hasKing(PokerHand deck) {
		PokerCard[] cards = deck.getCards();
		for(int j=0; j<5; j++) {
			if(cards[j].getRank()==13)
				return true;
		}
		return false;		
	}
}
