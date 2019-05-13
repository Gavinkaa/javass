package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
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
            Collections.nCopies(9, null)
    );
    private final ObservableSet<Card> playableCards = FXCollections.observableSet();

    /**
     * @return a read-only observable lists of cards in this hand
     */
    public ObservableList<Card> hand() {
        return FXCollections.unmodifiableObservableList(hand);
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
        if (handSize > 9) throw new IllegalArgumentException("Hand too big");
        if (handSize == 9) {
            for (int i = 0; i < 9; ++i) {
                hand.set(i, newHand.get(i));
            }
        } else {
            for (int i = 0; i < 9; ++i) {
                Card card = hand.get(i);
                if (card != null && !newHand.contains(card)) {
                    hand.set(i, null);
                }
            }
        }
    }

    /**
     * @return an observable set of playable cards
     */
    public ObservableSet<Card> playableCards() {
        return FXCollections.unmodifiableObservableSet(playableCards);
    }

    /**
     * Remove all cards not in a new set of playable cards,
     * and then add all cards in that set. Changes only trigger
     * when we add a new card, or remove an old one.
     *
     * @param newPlayableCards the new set of playable cards
     */
    public void setPlayableCards(CardSet newPlayableCards) {
        playableCards.removeIf(c -> !newPlayableCards.contains(c));
        for (int i = 0; i < newPlayableCards.size(); ++i) {
            playableCards.add(newPlayableCards.get(i));
        }
    }
}
