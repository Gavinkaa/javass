package ch.epfl.javass.jass;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the worth of a given Announce.
 * <p>
 * It's necessary to have a wrapper class, because the worth of a given
 * announce is more complicated than just the number of points.
 * In fact, the number of cards in the announce as well as the highest rank
 * played is also taken into account.
 */
public final class AnnounceValue implements Comparable<AnnounceValue> {
    private static List<CardSet> suites(int size) {
        List<CardSet> sets = new ArrayList<>();
        for (Card.Color color : Card.Color.ALL) {
            for (int rankI = 0; rankI <= Card.Rank.COUNT - size; ++rankI) {
                List<Card> cards = new ArrayList<>(size);
                for (int i = 0; i < size; ++i) {
                    cards.add(Card.of(color, Card.Rank.ALL.get(rankI + i)));
                }
                sets.add(CardSet.of(cards));
            }
        }
        return sets;
    }

    private static CardSet sameRank(Card.Rank rank) {
        List<Card> cards = new ArrayList<>(Card.Color.COUNT);
        for (Card.Color color : Card.Color.ALL) {
            cards.add(Card.of(color, rank));
        }
        return CardSet.of(cards);
    }

    private final static long[] announces;
    private final static int[] points;

    static {
        announces = new long[78];
        points = new int[78];
        int i = 0;
        for (CardSet hand : suites(3)) {
            announces[i] = hand.packed();
            points[i] = 20;
            ++i;
        }
        for (CardSet hand : suites(4)) {
            announces[i] = hand.packed();
            points[i] = 50;
            ++i;
        }
        for (Card.Rank lowRank : Arrays.asList(Card.Rank.TEN, Card.Rank.QUEEN, Card.Rank.KING, Card.Rank.ACE)) {
            announces[i] = sameRank(lowRank).packed();
            points[i] = 100;
            ++i;
        }
        for (CardSet hand : suites(5)) {
            announces[i] = hand.packed();
            points[i] = 100;
            ++i;
        }
        announces[i] = sameRank(Card.Rank.NINE).packed();
        points[i] = 150;
        ++i;
        announces[i] = sameRank(Card.Rank.JACK).packed();
        points[i] = 200;
        ++i;
    }

    private final int pointsValue;
    private final int size;
    private final int highestOrdinal;

    private static class CardSetPair {
        public final CardSet cardSet;
        public final int points;

        public CardSetPair(CardSet cardSet, int points) {
            this.cardSet = cardSet;
            this.points = points;
        }
    }

    private static CardSetPair bestSet(CardSet set, boolean sameSize) {
        int size = set.size();
        long packed = set.packed();
        List<Integer> applying = new ArrayList<>();
        for (int i = 0; i < announces.length; ++i) {
            long packedAnnounce = announces[i];
            if (PackedCardSet.difference(packedAnnounce, packed) == PackedCardSet.EMPTY) {
                applying.add(i);
            }
        }
        int bestPoints = 0;
        long bestSet = PackedCardSet.EMPTY;
        for (int i = 0; i < (1 << applying.size()); ++i) {
            long packedAcc = PackedCardSet.EMPTY;
            int cardCount = 0;
            int pointCount = 0;
            boolean cont = false;
            int s = 1;
            for (int indexJ : applying) {
                if ((i & s) == 0) continue;
                long packedJ = announces[indexJ];
                if (PackedCardSet.intersection(packedAcc, packedJ) != PackedCardSet.EMPTY) {
                    cont = true;
                    break;
                }
                packedAcc = PackedCardSet.union(packedAcc, packedJ);
                cardCount += PackedCardSet.size(packedJ);
                pointCount += points[0];
                s <<= 1;
            }
            boolean badSize = sameSize && cardCount != size;
            if (cont || badSize) continue;
            if (pointCount > bestPoints) {
                bestPoints = pointCount;
                bestSet = packedAcc;
            }
        }
        return new CardSetPair(CardSet.ofPacked(bestSet), bestPoints);
    }

    private AnnounceValue(CardSet set) {
        this.size = set.size();
        CardSetPair pair = bestSet(set, true);
        this.pointsValue = pair.points;
        int maxOrdinal = 0;
        for (int i = 0; i < pair.cardSet.size(); ++i) {
            int ordinal = pair.cardSet.get(i).rank().ordinal();
            maxOrdinal = Math.max(maxOrdinal, ordinal);
        }
        this.highestOrdinal = maxOrdinal;
    }

    /**
     * Construct an announce value from a given set
     *
     * This calculates the worth of that set. This will calculate the
     * value of an announce consisting of exactly that cardset.
     * If extra cards are included that are worthless, then the entire CardSet
     * is worth nothing.
     *
     * @param set the set to calculate the value of
     * @return an announce value for that set
     */
    public static AnnounceValue fromSet(CardSet set) {
        return new AnnounceValue(set);
    }

    /**
     * This calculates the best subset of the CardSet.
     *
     * This is useful to automatically calculate the best announce
     * to make. This is also necessary because announce values require
     * us to announce the exact amount of cards, whereas this will just find
     * the best subset without caring about the amount.
     *
     * @param set the set to search for the best announce
     * @return a cardset representing the best announce to make
     */
    public static CardSet bestAnnounce(CardSet set) {
        return bestSet(set, false).cardSet;
    }

    /**
     * @return the number of points that this is worth
     */
    public int points() {
        return this.pointsValue;
    }

    @Override
    public int compareTo(AnnounceValue that) {
        int comparePoints = this.pointsValue - that.pointsValue;
        if (comparePoints != 0) return comparePoints;
        int compareSize = this.size - that.size;
        if (compareSize != 0) return compareSize;
        return this.highestOrdinal - that.highestOrdinal;
    }
}
