package org.dllearner.examples;

public class Hand {

	private Card[] cards;
	int handType;
	
	/**
	 * 
	 * @param cards
	 * @param hand
	 *            Ordinal (0-9)
	 * 
	 * 0: Nothing in hand; not a recognized poker hand 
	 * 1: One pair; one pair of equal ranks within five cards 
	 * 2: Two pairs; two pairs of equal ranks within five cards 
	 * 3: Three of a kind; three equal ranks within five cards
	 * 4: Straight; five cards, sequentially ranked with no gaps 
	 * 5: Flush; five cards with the same suit 
	 * 6: Full house; pair + different rank three of a kind 
	 * 7: Four of a kind; four equal ranks within five cards 
	 * 8: Straight flush; straight + flush 
	 * 9: Royal flush; {Ace, King, Queen, Jack, Ten} + flush
	 */
	public Hand(Card[] cards, int hand) {
		this.cards = cards;
		this.handType = hand;
	}

	public Card[] getCards() {
		return cards;
	}

	public int getHandType() {
		return handType;
	}

}
