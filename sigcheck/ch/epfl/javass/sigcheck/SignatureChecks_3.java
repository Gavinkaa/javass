package ch.epfl.javass.sigcheck;

import ch.epfl.javass.jass.Card;
import ch.epfl.javass.jass.CardSet;
import ch.epfl.javass.jass.PackedCardSet;

import java.util.List;

public final class SignatureChecks_3 {
    private SignatureChecks_3() {}

    @SuppressWarnings("unused")
    void checkPackedCardSet() {
        Card.Color c = null;
        Card.Rank r = null;
        int i = 0;
        long l;
        boolean b;
        String s;
        l = PackedCardSet.EMPTY;
        l = PackedCardSet.ALL_CARDS;
        b = PackedCardSet.isValid(l);
        l = PackedCardSet.trumpAbove(i);
        l = PackedCardSet.singleton(i);
        b = PackedCardSet.isEmpty(l);
        i = PackedCardSet.size(l);
        i = PackedCardSet.get(l, i);
        l = PackedCardSet.add(l, i);
        l = PackedCardSet.remove(l, i);
        b = PackedCardSet.contains(l, i);
        l = PackedCardSet.complement(l);
        l = PackedCardSet.union(l, l);
        l = PackedCardSet.intersection(l, l);
        l = PackedCardSet.difference(l, l);
        l = PackedCardSet.subsetOfColor(l, c);
        s = PackedCardSet.toString(l);
    }

    @SuppressWarnings("unused")
    void checkCardSet() {
        CardSet s;
        long l = 0;
        List<Card> cs = null;
        Card c = null;
        Card.Color c2 = null;
        boolean b;
        int i;
        s = CardSet.EMPTY;
        s = CardSet.ALL_CARDS;
        s = CardSet.of(cs);
        s = CardSet.ofPacked(l);
        l = s.packed();
        b = s.isEmpty();
        i = s.size();
        c = s.get(i);
        s = s.add(c);
        s = s.remove(c);
        b = s.contains(c);
        s = s.complement();
        s = s.union(s);
        s = s.intersection(s);
        s = s.difference(s);
        s = s.subsetOfColor(c2);
    }
}
