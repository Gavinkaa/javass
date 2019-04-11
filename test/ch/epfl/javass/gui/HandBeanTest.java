package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.Card.Color;
import ch.epfl.javass.jass.CardSet;
import javafx.collections.ListChangeListener;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class HandBeanTest {
    @Test
    void seeWhatIsHappening() {
        HandBean hb = new HandBean();
        ListChangeListener<Card> listener = System.out::println;
        hb.hand().addListener(listener);

        CardSet h = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Card.Rank.SIX))
                .add(Card.of(Color.SPADE, Card.Rank.NINE))
                .add(Card.of(Color.SPADE, Card.Rank.JACK))
                .add(Card.of(Color.HEART, Card.Rank.SEVEN))
                .add(Card.of(Color.HEART, Card.Rank.ACE))
                .add(Card.of(Color.DIAMOND, Card.Rank.KING))
                .add(Card.of(Color.DIAMOND, Card.Rank.ACE))
                .add(Card.of(Color.CLUB, Card.Rank.TEN))
                .add(Card.of(Color.CLUB, Card.Rank.QUEEN));
        hb.setHand(h);
        while (!h.isEmpty()) {
            h = h.remove(h.get(0));
            hb.setHand(h);
        }
    }

    @Test
    void AddingHandToEmptySetsAllCards() {
        HandBean handBean = new HandBean();
        Set<Card> added = new HashSet<>();
        ListChangeListener<Card> listener = l -> {
            l.next();
            assertTrue(l.wasAdded());
            assertEquals(1, l.getAddedSize());
            added.addAll(l.getAddedSubList());
            assertEquals(1, l.getRemovedSize());
        };
        handBean.hand().addListener(listener);
        Set<Card> cardSet = new HashSet<>();
        cardSet.add(Card.of(Color.SPADE, Card.Rank.SIX));
        cardSet.add(Card.of(Color.SPADE, Card.Rank.NINE));
        cardSet.add(Card.of(Color.SPADE, Card.Rank.JACK));
        cardSet.add(Card.of(Color.HEART, Card.Rank.SEVEN));
        cardSet.add(Card.of(Color.HEART, Card.Rank.ACE));
        cardSet.add(Card.of(Color.DIAMOND, Card.Rank.KING));
        cardSet.add(Card.of(Color.DIAMOND, Card.Rank.ACE));
        cardSet.add(Card.of(Color.CLUB, Card.Rank.TEN));
        cardSet.add(Card.of(Color.CLUB, Card.Rank.QUEEN));
        CardSet hand = CardSet.EMPTY;
        for (Card card : cardSet) {
            hand = hand.add(card);
        }
        handBean.setHand(hand);
        assertEquals(cardSet, added);
    }

    @Test
    void tooManyCardIsIllegal() {
        CardSet h = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Card.Rank.SIX))
                .add(Card.of(Color.SPADE, Card.Rank.NINE))
                .add(Card.of(Color.SPADE, Card.Rank.JACK))
                .add(Card.of(Color.HEART, Card.Rank.SEVEN))
                .add(Card.of(Color.HEART, Card.Rank.ACE))
                .add(Card.of(Color.DIAMOND, Card.Rank.KING))
                .add(Card.of(Color.DIAMOND, Card.Rank.ACE))
                .add(Card.of(Color.CLUB, Card.Rank.TEN))
                .add(Card.of(Color.CLUB, Card.Rank.QUEEN))
                .add(Card.of(Color.CLUB, Card.Rank.KING));
        HandBean handBean = new HandBean();
        assertThrows(IllegalArgumentException.class, () -> handBean.setHand(h));
    }

    @Test
    void setPlayableWorks(){
        CardSet h = CardSet.EMPTY
                .add(Card.of(Color.SPADE, Card.Rank.SIX))
                .add(Card.of(Color.SPADE, Card.Rank.NINE))
                .add(Card.of(Color.SPADE, Card.Rank.JACK))
                .add(Card.of(Color.HEART, Card.Rank.SEVEN))
                .add(Card.of(Color.HEART, Card.Rank.ACE))
                .add(Card.of(Color.DIAMOND, Card.Rank.KING));
        HandBean handBean = new HandBean();
        handBean.setPlayableCards(h);
        for(Card card : handBean.playableCards()){
            assertTrue(h.contains(card));
        }
    }

}