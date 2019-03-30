package ch.epfl.javass.sigcheck;

import ch.epfl.javass.jass.MctsPlayer;
import ch.epfl.javass.jass.Player;
import ch.epfl.javass.jass.PlayerId;

public final class SignatureChecks_6 {
    private SignatureChecks_6() {}

    @SuppressWarnings("unused")
    void checkMctsPlayer() {
        PlayerId pi = null;
        long l = 0;
        int i = 0;
        Player p = new MctsPlayer(pi, l, i);
    }
}
