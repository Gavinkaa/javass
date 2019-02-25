package ch.epfl.javass.sigcheck;

import java.util.List;

import ch.epfl.javass.bits.Bits64;
import ch.epfl.javass.jass.PackedScore;
import ch.epfl.javass.jass.PlayerId;
import ch.epfl.javass.jass.Score;
import ch.epfl.javass.jass.TeamId;

public final class SignatureChecks_2 {
    private SignatureChecks_2() {}

    void checkBits64() {
        long l;
        int i = 0;
        l = Bits64.mask(i, i);
        l = Bits64.extract(l, i, i);
        l = Bits64.pack(l, i, l, i);
    }

    @SuppressWarnings("unused")
    void checkTeamId() {
        TeamId t;
        List<TeamId> l;
        int i;
        t = TeamId.TEAM_1;
        t = TeamId.TEAM_2;
        t = t.other();
        l = TeamId.ALL;
        i = TeamId.COUNT;
    }

    @SuppressWarnings("unused")
    void checkPlayerId() {
        PlayerId p;
        List<PlayerId> l;
        int i;
        TeamId t;
        p = PlayerId.PLAYER_1;
        p = PlayerId.PLAYER_2;
        p = PlayerId.PLAYER_3;
        p = PlayerId.PLAYER_4;
        l = PlayerId.ALL;
        i = PlayerId.COUNT;
        t = p.team();
    }

    @SuppressWarnings("unused")
    void checkPackedScore() {
        long l;
        boolean b;
        int i = 0;
        TeamId t = null;
        String s;
        l = PackedScore.INITIAL;
        b = PackedScore.isValid(l);
        l = PackedScore.pack(i, i, i, i, i, i);
        i = PackedScore.turnTricks(l, t);
        i = PackedScore.turnPoints(l, t);
        i = PackedScore.gamePoints(l, t);
        i = PackedScore.totalPoints(l, t);
        l = PackedScore.withAdditionalTrick(l, t, i);
        l = PackedScore.nextTurn(l);
        s = PackedScore.toString(l);
    }

    @SuppressWarnings("unused")
    void checkScore() {
        Score s;
        String s2 = null;
        int i;
        TeamId t = null;
        long l = 0;
        s = Score.INITIAL;
        s = Score.ofPacked(l);
        l = s.packed();
        i = s.turnTricks(t);
        i = s.turnPoints(t);
        i = s.gamePoints(t);
        i = s.totalPoints(t);
        s = s.withAdditionalTrick(t, i);
        s = s.nextTurn();
    }
}
