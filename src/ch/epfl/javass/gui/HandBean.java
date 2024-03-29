package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.Jass;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.util.Collections;

/**
 * Represents an observable Hand of cards, where
 * we can subscribe to changes for a given Hand.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class HandBean {
    private final ObservableList<Card> hand = FXCollections.observableArrayList(
            Collections.nCopies(Jass.HAND_SIZE, null)
    );
    private final ObservableSet<Card> playableCards = FXCollections.observableSet();

    /**
     * @return a read-only observable lists of cards in this hand
     */
    public ObservableList<Card> hand() {
        return FXCollections.unmodifiableObservableList(this.hand);
    }

    /**
     * Set the list of cards using a CardSet.
     * If the set contains 9 cards, the entire hand is replaced,
     * otherwise, the cards that aren't present in that set are removed.
     *
     * @param newHand the newHand to replace our list of cars with.
     * @throws IllegalArgumentException if the set contains more than 9 cards
     */
    public void setHand(CardSet newHand) {
        int handSize = newHand.size();
        if (handSize > Jass.HAND_SIZE) throw new IllegalArgumentException("Hand too big");
        if (handSize == Jass.HAND_SIZE) {
            for (int i = 0; i < Jass.HAND_SIZE; ++i) {
                this.hand.set(i, newHand.get(i));
            }
        } else {
            for (int i = 0; i < Jass.HAND_SIZE; ++i) {
                Card card = this.hand.get(i);
                if (card != null && !newHand.contains(card)) {
                    this.hand.set(i, null);
                }
            }
        }
    }

    /**
     * @return an observable set of playable cards
     */
    public ObservableSet<Card> playableCards() {
        return FXCollections.unmodifiableObservableSet(this.playableCards);
    }

    /**
     * Remove all cards not in a new set of playable cards,
     * and then add all cards in that set. Changes only trigger
     * when we add a new card, or remove an old one.
     *
     * @param newPlayableCards the new set of playable cards
     */
    public void setPlayableCards(CardSet newPlayableCards) {
        this.playableCards.removeIf(c -> !newPlayableCards.contains(c));
        for (int i = 0; i < newPlayableCards.size(); ++i) {
            this.playableCards.add(newPlayableCards.get(i));
        }
    }
}
