package ch.epfl.javass.jass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerIdTest {

    @Test
    void countSize() {
        assertEquals(4, PlayerId.COUNT);
    }

    @Test
    void allContainsBothTeams() {
        assertTrue(PlayerId.ALL.contains(PlayerId.PLAYER_1));
        assertTrue(PlayerId.ALL.contains(PlayerId.PLAYER_2));
        assertTrue(PlayerId.ALL.contains(PlayerId.PLAYER_3));
        assertTrue(PlayerId.ALL.contains(PlayerId.PLAYER_4));
    }

    @Test
    void teamIsCorrect() {
        assertEquals(TeamId.TEAM_1, PlayerId.PLAYER_1.team());
        assertEquals(TeamId.TEAM_1, PlayerId.PLAYER_3.team());
        assertEquals(TeamId.TEAM_2, PlayerId.PLAYER_2.team());
        assertEquals(TeamId.TEAM_2, PlayerId.PLAYER_4.team());
    }
}