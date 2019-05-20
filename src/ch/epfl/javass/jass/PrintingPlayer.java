package ch.epfl.javass.jass;

import java.util.Map;

/**
 * @author Lúcás Críostóir Meier (300831)
 * @author Ludovic Burnier (301308)
 */
public final class PrintingPlayer implements Player {
    private final Player underlyingPlayer;

    public PrintingPlayer(Player underlyingPlayer) {
        this.underlyingPlayer = underlyingPlayer;
    }

    @Override
    public Card cardToPlay(TurnState state, CardSet hand) {
        System.out.print("C'est à moi de jouer... Je joue : ");
        Card c = this.underlyingPlayer.cardToPlay(state, hand);
        System.out.println(c);
        return c;
    }

    @Override
    public void setPlayers(PlayerId ownId, Map<PlayerId, String> playerNames) {
        this.underlyingPlayer.setPlayers(ownId, playerNames);
        System.out.println("Les joueurs sont :");
        for (PlayerId id : PlayerId.ALL) {
            System.out.println("  " + playerNames.get(id) + (ownId == id ? " (moi)" : ""));
        }
    }

    @Override
    public void updateHand(CardSet newHand) {
        this.underlyingPlayer.updateHand(newHand);
        System.out.println("Ma nouvelle main : " + newHand);
    }

    @Override
    public void setTrump(Card.Color trump) {
        this.underlyingPlayer.setTrump(trump);
        System.out.println("Atout : " + trump);
    }

    @Override
    public void updateTrick(Trick newTrick) {
        this.underlyingPlayer.updateTrick(newTrick);
        System.out.println(newTrick);
    }

    @Override
    public void updateScore(Score score) {
        this.underlyingPlayer.updateScore(score);
        System.out.println("Scores: " + score);
    }

    @Override
    public void setWinningTeam(TeamId winningTeam) {
        this.underlyingPlayer.setWinningTeam(winningTeam);
        System.out.println("L'équipe " + winningTeam + " a gagné");
    }
}
