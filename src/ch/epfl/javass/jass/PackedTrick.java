package ch.epfl.javass.jass;

import ch.epfl.javass.bits.Bits32;

/**
 * Contains utility functions for working with a single trick,
 * i.e., a round of gameplay where each player has played one of their cards.
 * Each packed representation contains each of the 4 cards the player
 * put down,
 */
public final class PackedTrick {
    /**
     * Represents a completely invalid packed representation
     */
    public static final int INVALID = -1;

    private PackedTrick() {
    }

    /**
     * Check if the packed representation trick is valid
     *
     * @return true if the packed representation is valid
     */
    public static boolean isValid(int pkTrick) {
        // Check if the cards are correct
        boolean foundValid = false;
        for (int i = 3; i >= 0; --i) {
            int pkCard = PackedTrick.card(pkTrick, i);
            if (foundValid && !PackedCard.isValid(pkCard)) {
                return false;
            }
            if (PackedCard.isValid(pkCard)) {
                foundValid = true;
            }
        }
        int index = PackedTrick.index(pkTrick);
        if (index > 8) {
            return false;
        }
        return true;
    }

    /**
     * Construct a packed representation of a trick with no played cards,
     * and the given trump and firstPlayer.
     *
     * @param trump       the current trump color
     * @param firstPlayer the first player to play this turn
     */
    public static int firstEmpty(Card.Color trump, PlayerId firstPlayer) {
        return Bits32.pack(
                PackedCard.INVALID, 6,
                PackedCard.INVALID, 6,
                PackedCard.INVALID, 6,
                PackedCard.INVALID, 6,
                0, 4,
                firstPlayer.ordinal(), 2,
                trump.ordinal(), 2
        );
    }

    /**
     * Return the next trick after clearing all the cards and incrementing the index.
     * If the index is at the last one, this returns INVALID instead
     *
     * @param pkTrick the packed representation to use
     * @return the empty trick right after this one
     */
    public static int nextEmpty(int pkTrick) {
        assert isValid(pkTrick);

        int nextIndex = PackedTrick.index(pkTrick) + 1;
        if (nextIndex > 8) {
            return PackedTrick.INVALID;
        }
        Card.Color trump = PackedTrick.trump(pkTrick);
        int winningIndex = 0;
        int winningCard = PackedTrick.card(pkTrick, 0);
        for (int i = 1; i <= 3; ++i) {
            int card = PackedTrick.card(pkTrick, i);
            if (PackedCard.isBetter(trump, card, winningCard)) {
                winningCard = card;
                winningIndex = i;
            }
        }
        // Index is 0 so this works
        return firstEmpty(trump, PackedTrick.player(pkTrick, winningIndex)) | (nextIndex << 24);
    }

    /**
     * @return true if this is the last trick of a turn
     */
    public static boolean isLast(int pkTrick) {
        assert isValid(pkTrick);

        return PackedTrick.index(pkTrick) == 8;
    }

    /**
     * @return true if there is no valid card
     */
    public static boolean isEmpty(int pkTrick) {
        return size(pkTrick) == 0;
    }

    /**
     * @return true if every card is valid
     */
    public static boolean isFull(int pkTrick) {
        return size(pkTrick) == 4;
    }

    /**
     * @return the number of cards contained in this trick
     */
    public static int size(int pkTrick) {
        assert isValid(pkTrick);

        int count = 0;
        for (int i = 0; i < 4; ++i) {
            if (PackedTrick.card(pkTrick, i) != PackedCard.INVALID) {
                count++;
            }
        }
        return count;
    }

    /**
     * @return the trump color
     */
    public static Card.Color trump(int pkTrick) {
        return Card.Color.ALL.get(Bits32.extract(pkTrick, 30, 2));
    }

    /**
     * @param pkTrick the packed representation of the trick
     * @param index   the index of the player
     * @return the id of the player in the indexth slot
     */
    public static PlayerId player(int pkTrick, int index) {
        return PlayerId.ALL.get((Bits32.extract(pkTrick, 28, 2) + index) % 4);
    }

    /**
     * @return the index in pkTrick
     */
    public static int index(int pkTrick) {
        return Bits32.extract(pkTrick, 24, 4);
    }

    /**
     * @return the card represented at the given index
     */
    public static int card(int pkTrick, int index) {
        return Bits32.extract(pkTrick, index * 6, 6);
    }

    /**
     * @return return a new pkTrick with the given card in the next available slot
     */
    public static int withAddedCard(int pkTrick, int pkCard) {
        assert !isFull(pkTrick);

        int start = PackedTrick.size(pkTrick) * 6;
        return ((pkTrick & ~Bits32.mask(start, 6)) | (pkCard << start));
    }

    /**
     * Return the base color of this trick, i.e.,
     * the color of the first card played in this trick.
     * @param pkTrick the representation of the trick
     * @return the base color of that trick
     */
    public static Card.Color baseColor(int pkTrick) {
        assert isValid(pkTrick);
        return PackedCard.color(Bits32.extract(pkTrick, 0, 6));
    }

    // Return the best trump card played so far, or invalid
    private static int bestTrumpCard(int pkTrick, Card.Color trump) {
        int bestTrump = PackedCard.INVALID;
        for (int i = 0; i < PackedTrick.size(pkTrick); ++i) {
            int card = PackedTrick.card(pkTrick, i);
                if (PackedCard.color(card) == trump) {
                if (bestTrump == PackedCard.INVALID)  {
                    bestTrump = card;
                } else {
                    bestTrump = PackedCard.isBetter(trump, card, bestTrump) ? card : bestTrump;
                }
            }
        }
        return bestTrump;
    }

    /**
     * Return the set of cards that can be played next,
     * given the current state of the trick
     * @param pkTrick the binary representation of the trick
     * @param pkHand the packed card set representing the hand of the player
     * @return the subset of that hand that can be played
     */
    public static long playableCards(int pkTrick, long pkHand) {
        assert !isFull(pkTrick);
        if (isEmpty(pkTrick)) {
            return pkHand;
        }
        Card.Color trump = trump(pkTrick);
        Card.Color base = baseColor(pkTrick);

        long baseColored = PackedCardSet.subsetOfColor(pkHand, base);
        if (PackedCardSet.isEmpty(baseColored)) {
            return pkHand;
        }
        // If the base color is the same as the trump color,
        // we must play our trump cards, except the jack.
        if (base == trump) {
            // If the only trump card we have is a Jack, we're free to play anything
            if (baseColored == PackedCardSet.singleton(Card.of(trump, Card.Rank.JACK).packed())) {
                return pkHand;
            } else {
                return baseColored;
            }
        } else {
            long trumpsWeCanPlay = PackedCardSet.subsetOfColor(pkHand, trump);
            int bestTrumpCard = PackedTrick.bestTrumpCard(pkTrick, trump);
            for (Card.Rank rank : Card.Rank.ALL) {
                Card card = Card.of(trump, rank);
                if (Card.ofPacked(bestTrumpCard).isBetter(trump, card)) {
                    trumpsWeCanPlay = PackedCardSet.remove(trumpsWeCanPlay, card.packed());
                }
            }

            return PackedCardSet.union(baseColored, trumpsWeCanPlay);
        }
    }
}
