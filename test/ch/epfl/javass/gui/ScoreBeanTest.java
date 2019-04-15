package ch.epfl.javass.gui;

import ch.epfl.javass.jass.TeamId;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ScoreBeanTest {
    @Test
    void everthingZeroAtStart() {
        ScoreBean scoreBean = new ScoreBean();
        for (TeamId teamId : TeamId.ALL) {
            assertEquals(0, scoreBean.gamePointsProperty(teamId).get());
            assertEquals(0, scoreBean.totalPointsProperty(teamId).get());
            assertEquals(0, scoreBean.turnPointsProperty(teamId).get());
        }
    }

    @Test
    void WinningTeamStartNull() {
        assertNull(new ScoreBean().winningTeamProperty().get());
    }

    @Test
    void allScoresCanBeSet() {
        ScoreBean scoreBean = new ScoreBean();
        for (TeamId teamId : TeamId.ALL) {
            scoreBean.setGamePoints(teamId,2);
            scoreBean.setTotalPoints(teamId,2);
            scoreBean.setTurnPoints(teamId,2);
        }
        for (TeamId teamId : TeamId.ALL) {
            assertEquals(2, scoreBean.gamePointsProperty(teamId).get());
            assertEquals(2, scoreBean.totalPointsProperty(teamId).get());
            assertEquals(2, scoreBean.turnPointsProperty(teamId).get());
        }
    }

    @Test
    void canSetWinningTeam(){
        ScoreBean scoreBean = new ScoreBean();
        for(TeamId teamId : TeamId.ALL){
            scoreBean.setWinningTeam(teamId);
            assertEquals(teamId, scoreBean.winningTeamProperty().get());
        }
    }
}
