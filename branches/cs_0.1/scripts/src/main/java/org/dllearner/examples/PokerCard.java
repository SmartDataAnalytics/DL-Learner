package org.dllearner.examples;

/**
 * A poker card has one of 4 suits and 13 ranks.
 * @author Jens Lehmann
 *
 */
public class PokerCard {

	private int suit;
	private int rank;
	
	/**
	 * 
	 * @param suit Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}.
	 * @param rank Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)
	 */
	public PokerCard(int suit, int rank) {
		this.suit = suit;
		this.rank = rank;
	}
	
	public String getSuitString() {
		switch(suit) {
		case 1: return "hearts";
		case 2: return "spades";
		case 3: return "diamonds";
		case 4: return "clubs";
		}
		throw new Error("Unknown suit code " + suit);
	}
	
	public String getRankString() {
		switch(rank) {
		case 1: return "ace";
		case 2: return "two";
		case 3: return "three";
		case 4: return "four";
		case 5: return "five";
		case 6: return "six";
		case 7: return "seven";
		case 8: return "eight";
		case 9: return "nine";
		case 10: return "ten";
		case 11: return "jack";
		case 12: return "queen";		
		case 13: return "king";			
		}
		throw new Error("Unknown rank code " + rank);		
	}
	
	public boolean hasSameSuit(PokerCard card) {
		return (suit == card.getSuit());
	}

	public boolean hasSameRank(PokerCard card) {
		return (rank == card.getRank());
	}
	
	// prüft, ob übergebene Karte den nächsten Rank hat
	public boolean hasNextRank(PokerCard card) {
		// Spezialfall Ass: Vorgänger von 2 und Nachfolger von König
		if(rank+1 == card.getRank() || (rank==13 && card.getRank()==1)) 
			return true;
		else
			return false;
	}
	
	public int getRank() {
		return rank;
	}

	public int getSuit() {
		return suit;
	}
}
