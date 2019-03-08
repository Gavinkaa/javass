package ch.epfl.javass.sigcheck;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PackedTrick;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Trick;

public final class SignatureChecks_4 {
    private SignatureChecks_4() {}

    @SuppressWarnings("unused")
    void checkPackedTrick() {
        int i;
        boolean b;
        long l = 0;
        Card.Color c = null;
        PlayerId p = null;
        String s;
        i = PackedTrick.INVALID;
        b = PackedTrick.isValid(i);
        i = PackedTrick.firstEmpty(c, p);
        i = PackedTrick.nextEmpty(i);
        b = PackedTrick.isLast(i);
        b = PackedTrick.isEmpty(i);
        b = PackedTrick.isFull(i);
        i = PackedTrick.size(i);
        c = PackedTrick.trump(i);
        p = PackedTrick.player(i, i);
        i = PackedTrick.index(i);
        i = PackedTrick.card(i, i);
        i = PackedTrick.withAddedCard(i, i);
        c = PackedTrick.baseColor(i);
        l = PackedTrick.playableCards(i, l);
        i = PackedTrick.points(i);
        p = PackedTrick.winningPlayer(i);
        s = PackedTrick.toString(i);
    }

    @SuppressWarnings("unused")
    void checkTrick() {
        Trick t;
        Card.Color c = null;
        Card d = null;
        CardSet s = null;
        PlayerId p = null;
        int i = 0;
        boolean b;
        t = Trick.INVALID;
        t = Trick.firstEmpty(c, p);
        t = Trick.ofPacked(i);
        i = t.packed();
        t = t.nextEmpty();
        b = t.isLast();
        b = t.isEmpty();
        b = t.isFull();
        i = t.size();
        c = t.trump();
        i = t.index();
        p = t.player(i);
        d = t.card(i);
        t = t.withAddedCard(d);
        c = t.baseColor();
        s = t.playableCards(s);
        i = t.points();
        p = t.winningPlayer();
    }
}
