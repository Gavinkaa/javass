package ch.epfl.javass.jass;


import ch.epfl.javass.Preconditions;

/**
 * Represents a Score in a match of Jass, with methods
 * to be update concurrently over the course of the game.
 *
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class Score {
    public static final Score INITIAL = Score.ofPacked(PackedScore.INITIAL);

    private final long packed;

    private Score(long packed) {
        this.packed = packed;
    }

    /**
     * Construct a new Score from its packed representation
     *
     * @param packed the binary representation to make a score from
     * @return a new score if the representation was valid
     * @throws IllegalArgumentException if the binary representation isn't valid
     */
    public static Score ofPacked(long packed) {
        Preconditions.checkArgument(PackedScore.isValid(packed));
        return new Score(packed);
    }

    /**
     * @return the binary representation of this Score
     */
    public long packed() {
        return packed;
    }

    /**
     * Return the turnTricks associated with a given team.
     *
     * @param t the team for which to fetch the turnTricks
     */
    public int turnTricks(TeamId t) {
        return PackedScore.turnTricks(packed, t);
    }

    /**
     * Return the turnPoints associated with a given team
     *
     * @param t the team for which to fetch the turnPoints
     */
    public int turnPoints(TeamId t) {
        return PackedScore.turnPoints(packed, t);
    }

    /**
     * Return the gamePoints associated with a given team
     *
     * @param t the team with which the gamePoints are associated.
     */
    public int gamePoints(TeamId t) {
        return PackedScore.gamePoints(packed, t);
    }

    /**
     * Return the total points, taking into account both the
     * turn points and the game points
     *
     * @param t the team to look at
     */
    public int totalPoints(TeamId t) {
        return PackedScore.totalPoints(packed, t);
    }

    /**
     * This method should be used to add the points won by a team after
     * a trick.
     *
     * @param winningTeam the team that won the trick
     * @param trickPoints the number of points that trick was worth
     * @return a new score representing the result of that win
     */
    public Score withAdditionalTrick(TeamId winningTeam, int trickPoints) {
        Preconditions.checkArgument(trickPoints >= 0);
        return new Score(PackedScore.withAdditionalTrick(packed, winningTeam, trickPoints));
    }

    /**
     * Return the new score after taking the results of the current turn into account.
     * The current turn points are added to the total points.
     *
     * @return the Score after this operation is applied
     */
    public Score nextTurn() {
        return new Score(PackedScore.nextTurn(packed));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Score score = (Score) o;
        return packed == score.packed;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(packed);
    }

    @Override
    public String toString() {
        return "Score(" + PackedScore.toString(packed) + ")";
    }
}
