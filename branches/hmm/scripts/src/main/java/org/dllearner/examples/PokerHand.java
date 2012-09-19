package org.dllearner.examples;

/**
 * A poker hand consists of 5 cards.
 * 
 * @author Jens Lehmann
 *
 */
public class PokerHand {

	private PokerCard[] cards;
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
	public PokerHand(PokerCard[] cards, int hand) {
		this.cards = cards;
		this.handType = hand;
	}

	public PokerCard[] getCards() {
		return cards;
	}

	public int getHandType() {
		return handType;
	}

}
