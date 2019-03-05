package ch.epfl.javass.jass;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TeamIdTest {

    @Test
    void otherDoesntReturnThis() {
        for (TeamId t : TeamId.ALL) {
            assertNotEquals(t, t.other());
        }

    }

    @Test
    void countSize() {
        assertEquals(2, TeamId.COUNT);
    }

    @Test
    void allContainsBothTeams() {
        assertTrue(TeamId.ALL.contains(TeamId.TEAM_1));
        assertTrue(TeamId.ALL.contains(TeamId.TEAM_2));
    }
}