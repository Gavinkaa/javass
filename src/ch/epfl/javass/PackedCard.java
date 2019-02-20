package ch.epfl.javass;

import ch.epfl.javass.jass.Card;

public final class PackedCard {
    public static final int INVALID = 0b111111;

    private PackedCard() {
    }

    public static boolean isValid(int pkCard) {
        int rank = Bits32.extract(pkCard, 0, 4);
        if (rank > 8) {
            return false;
        }
        int zeroed = Bits32.extract(pkCard, 6, 26);
        return zeroed == 0;
    }

    public static int pack(Card.Color c, Card.Rank r)  {
        return Bits32.pack(r.ordinal(), 4, c.ordinal(), 2);
    }

    public static Card.Color color(int pkCard) {
        assert isValid(pkCard);

        return Card.Color.ALL.get(Bits32.extract(pkCard, 4, 2));
    }

    public static Card.Rank rank(int pkCard) {
        assert isValid(pkCard);

        return Card.Rank.ALL.get(Bits32.extract(pkCard, 0, 4));
    }

    public static boolean isBetter(Card.Color trump, int pkCardL, int pkCardR) {
        assert isValid(pkCardL);
        assert isValid(pkCardR);

        boolean leftTrump = color(pkCardL) == trump;
        boolean rightTrump = color(pkCardR) == trump;
        if (leftTrump && !rightTrump) {
            return true;
        }
        if (!leftTrump && rightTrump) {
            return false;
        }
        if (leftTrump && rightTrump) {
            return rank(pkCardL).trumpOrdinal() > rank(pkCardR).trumpOrdinal();
        }
        if (color(pkCardL) == color(pkCardR)) {
            return rank(pkCardL).ordinal() > rank(pkCardR).ordinal();
        }
        return false;
    }

    public static int points(Card.Color trump, int pkCard) {
        assert isValid(pkCard);

        Card.Color color = color(pkCard);
        boolean isTrump = color == trump;
        switch (rank(pkCard)) {
            case SIX:
                return 0;
            case SEVEN:
                return 0;
            case EIGHT:
                return 0;
            case NINE:
                return isTrump ? 14 : 0;
            case TEN:
                return 10;
            case JACK:
                return isTrump ? 20 : 2;
            case QUEEN:
                return 3;
            case KING:
                return 4;
            case ACE:
                return 11;
        }
        // Unreachable
        throw new RuntimeException("Unreachable code, invalid Rank");
    }

    public static String toString(int pkCard) {
        assert isValid(pkCard);

        return color(pkCard).toString() + " " + rank(pkCard).toString();
    }
}
