package ch.epfl.javass.gui;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;
import org.junit.jupiter.api.Test;

import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TrickBeanTest {
    @Test
    void setTrump() {
        TrickBean trickBean = new TrickBean();
        for (Card.Color color : Card.Color.ALL) {
            trickBean.setTrump(color);
            assertEquals(color, trickBean.trumpProperty().get());
        }
    }

    @Test
    void setTrickWorks() {
        TrickBean trickBean = new TrickBean();
        Trick trick = Trick.firstEmpty(Card.Color.DIAMOND, PlayerId.PLAYER_1);
        trick.withAddedCard(Card.of(Card.Color.HEART, Card.Rank.ACE));
        trick.withAddedCard(Card.of(Card.Color.HEART, Card.Rank.KING));
        trick.withAddedCard(Card.of(Card.Color.HEART, Card.Rank.SIX));
        trick.withAddedCard(Card.of(Card.Color.HEART, Card.Rank.SEVEN));

        Map<PlayerId, Card> target = new EnumMap<>(PlayerId.class);

        for (int i = 0; i < trick.size(); i++) {
            target.put(trick.player(i), trick.card(i));
        }

        trickBean.setTrick(trick);
        Map<PlayerId, Card> cards = trickBean.trick();
        for (PlayerId playerId : PlayerId.ALL) {
            assertEquals(target.get(playerId), cards.get(playerId));
        }
    }
}