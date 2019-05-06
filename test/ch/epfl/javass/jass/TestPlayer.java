package ch.epfl.javass.jass;

import java.util.List;
import java.util.Map;

public class TestPlayer implements Player {

    public final PlayerId ownId;
    public final List<PlayerId> playingOrderLog;

    public int cardToPlayCallCount = 0;
    public TurnState cardToPlayState = null;
    public CardSet cardToPlayHand = null;
    public Card cardToPlayReturnedCard = null;

    public int setPlayersCallCount = 0;
    public PlayerId setPlayersOwnId = null;
    public Map<PlayerId, String> setPlayersPlayerNames = null;

    public int updateHandCallCount = 0;
    public CardSet updateHandNewHand = null;
    public CardSet updateHandInitialHand = null;

    public int setTrumpCallCount = 0;
    public Card.Color setTrumpTrump = null;

    public int updateTrickCallCount = 0;
    public Trick updateTrickNewTrick = null;

    public int updateScoreCallCount = 0;
    public Score updateScoreScore = null;

    public int setWinningTeamCallCount = 0;
    public TeamId setWinningTeamWinningTeam = null;

    public TestPlayer(PlayerId ownId, List<PlayerId> playingOrderLog) {
        this.ownId = ownId;
        this.playingOrderLog = playingOrderLog;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        if (playingOrderLog != null)
            playingOrderLog.add(ownId);

        cardToPlayCallCount += 1;
        cardToPlayState = state;
        cardToPlayHand = hand;
        cardToPlayReturnedCard = state.trick().playableCards(hand).get(0);
        return cardToPlayReturnedCard;
    }

    @Override
    public Card.Color chooseTrump(CardSet hand, boolean canDelegate) {
        return null;
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.setPlayersCallCount += 1;
        this.setPlayersOwnId = ownId;
        this.setPlayersPlayerNames = playerNames;
    }

    @Override
    public void updateHand(CardSet newHand) {
        updateHandCallCount += 1;
        updateHandNewHand = newHand;
        if (updateHandInitialHand == null)
            updateHandInitialHand = newHand;
    }

    @Override
    public void setTrump(Card.Color trump) {
        setTrumpCallCount += 1;
        setTrumpTrump = trump;
    }

    @Override
    public void updateTrick(Trick newTrick) {
        updateTrickCallCount += 1;
        updateTrickNewTrick = newTrick;
    }

    @Override
    public void updateScore(Score score) {
        updateScoreCallCount += 1;
        updateScoreScore = score;
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        setWinningTeamCallCount += 1;
        setWinningTeamWinningTeam = winningTeam;
    }

}
