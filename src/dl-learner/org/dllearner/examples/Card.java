package org.dllearner.examples;

public class Card {

	private int suit;
	private int rank;
	
	/**
	 * 
	 * @param suit Ordinal (1-4) representing {Hearts, Spades, Diamonds, Clubs}.
	 * @param rank Numerical (1-13) representing (Ace, 2, 3, ... , Queen, King)
	 */
	public Card(int suit, int rank) {
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
	
	public boolean hasSameSuit(Card card) {
		return (suit == card.getSuit());
	}

	public boolean hasSameRank(Card card) {
		return (rank == card.getRank());
	}
	
	// prüft, ob übergebene Karte den nächsten Rank hat
	public boolean hasNextRank(Card card) {
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
