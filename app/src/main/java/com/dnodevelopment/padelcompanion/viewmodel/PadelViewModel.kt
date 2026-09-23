package com.dnodevelopment.padelcompanion.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dnodevelopment.padelcompanion.model.PadelState

class PadelViewModel {
    var matchStarted by mutableStateOf(false)
    var team1Score by mutableStateOf(0)
    var team2Score by mutableStateOf(0)
    var team1Advantage by mutableStateOf(false)
    var team2Advantage by mutableStateOf(false)
    var team1Serving by mutableStateOf(true)
    var team1GamePoints by mutableStateOf(0)
    var team2GamePoints by mutableStateOf(0)
    var team1SetScore by mutableStateOf(0)
    var team2SetScore by mutableStateOf(0)
    var isTiebreak by mutableStateOf(false)

    // Which of a team's two players serves the next time that team serves.
    // Partners take turns, so this flips each time their team finishes serving
    // a game. True means the right-side (deuce court) player.
    var team1RightSidePlayerServes by mutableStateOf(true)
    var team2RightSidePlayerServes by mutableStateOf(true)

    // Points played in the current game or tiebreak. Drives which box the serve
    // is taken from, and is counted separately from the score because deuce and
    // advantage points do not always change team1Score/team2Score.
    private var pointsPlayedInGame by mutableStateOf(0)

    // Which team served the opening point of the current tiebreak. Serve then
    // rotates in blocks of two points, so this is the reference for working out
    // who serves next.
    private var tiebreakStarterIsTeam1 by mutableStateOf(true)

    private var history by mutableStateOf(listOf<PadelState>())

    /**
     * Which service box the next point is served from.
     *
     * The first point of a game or tiebreak is served from the right, and the
     * box alternates on every point after that. This is independent of which
     * player is serving.
     */
    val serveFromRight: Boolean
        get() = pointsPlayedInGame % 2 == 0

    /**
     * Whether the serving team's right-side player is the one serving.
     *
     * Within a game the server never changes. Partners alternate across their
     * team's service games, and during a tiebreak the same alternation carries
     * on each time a team's turn to serve comes round, so all four players
     * serve in rotation.
     */
    val servingPlayerIsRightSide: Boolean
        get() = if (isTiebreak) {
            tiebreakServingPlayerIsRightSide()
        } else if (team1Serving) {
            team1RightSidePlayerServes
        } else {
            team2RightSidePlayerServes
        }

    /**
     * Whether the teams should swap ends right now.
     *
     * Outside a tiebreak ends change after every odd game in the set, counting
     * both teams' games together. In a tiebreak ends change every six points.
     * Only true between points, so the prompt clears as soon as play resumes.
     */
    val shouldChangeSides: Boolean
        get() = if (isTiebreak) {
            pointsPlayedInGame > 0 && pointsPlayedInGame % 6 == 0
        } else {
            val gamesPlayed = team1GamePoints + team2GamePoints
            gamesPlayed > 0 && gamesPlayed % 2 == 1 && pointsPlayedInGame == 0
        }

    fun getScoreDisplay(score: Int): String {
        return when (score) {
            0 -> "0"
            1 -> "15"
            2 -> "30"
            3 -> "40"
            else -> "40"
        }
    }

    private fun saveState() {
        history = history + PadelState(
            team1Score, team2Score, team1Advantage, team2Advantage,
            team1Serving, team1RightSidePlayerServes, team2RightSidePlayerServes,
            team1GamePoints, team2GamePoints,
            team1SetScore, team2SetScore, isTiebreak,
            tiebreakStarterIsTeam1, pointsPlayedInGame
        )
    }

    fun updateScore(team: Int) {
        saveState()
        pointsPlayedInGame++

        when (team) {
            1 -> {
                if (isTiebreak) {
                    team1Score++
                    if (team1Score >= 7 && team1Score - team2Score >= 2) {
                        winSetViaTiebreak(team1Wins = true)
                    } else {
                        updateTiebreakServingTeam()
                    }
                } else if (team1Score >= 3 && team2Score >= 3) {
                    if (team2Advantage) {
                        team2Advantage = false
                    } else if (team1Advantage) {
                        team1GamePoints++
                        checkSetWinner()
                        resetGame()
                        startTiebreakIfNeeded()
                    } else {
                        team1Advantage = true
                    }
                } else {
                    team1Score++
                    if (team1Score > 3) {
                        team1GamePoints++
                        checkSetWinner()
                        resetGame()
                        startTiebreakIfNeeded()
                    }
                }
            }
            2 -> {
                if (isTiebreak) {
                    team2Score++
                    if (team2Score >= 7 && team2Score - team1Score >= 2) {
                        winSetViaTiebreak(team1Wins = false)
                    } else {
                        updateTiebreakServingTeam()
                    }
                } else if (team1Score >= 3 && team2Score >= 3) {
                    if (team1Advantage) {
                        team1Advantage = false
                    } else if (team2Advantage) {
                        team2GamePoints++
                        checkSetWinner()
                        resetGame()
                        startTiebreakIfNeeded()
                    } else {
                        team2Advantage = true
                    }
                } else {
                    team2Score++
                    if (team2Score > 3) {
                        team2GamePoints++
                        checkSetWinner()
                        resetGame()
                        startTiebreakIfNeeded()
                    }
                }
            }
        }
    }

    /**
     * The tiebreak serving turn covering a given point number, counting points
     * from one. The opening team serves turn 0, a single point; every turn after
     * that is two points long.
     */
    private fun tiebreakTurnFor(pointNumber: Int): Int = pointNumber / 2

    /** Works out which team serves the next tiebreak point. */
    private fun updateTiebreakServingTeam() {
        val turn = tiebreakTurnFor(pointsPlayedInGame + 1)
        val starterServes = turn % 2 == 0
        team1Serving = if (starterServes) tiebreakStarterIsTeam1 else !tiebreakStarterIsTeam1
    }

    /**
     * Works out which of the serving team's two players takes the next tiebreak
     * point. Each time a team's turn comes round again their other player serves,
     * which continues the rotation established during the set.
     */
    private fun tiebreakServingPlayerIsRightSide(): Boolean {
        val turn = tiebreakTurnFor(pointsPlayedInGame + 1)
        val starterServes = turn % 2 == 0
        // How many turns this team has already had, counting from zero.
        val teamTurnIndex = if (starterServes) turn / 2 else (turn - 1) / 2
        val servingTeamIsTeam1 =
            if (starterServes) tiebreakStarterIsTeam1 else !tiebreakStarterIsTeam1
        val firstServerOfTiebreak =
            if (servingTeamIsTeam1) team1RightSidePlayerServes else team2RightSidePlayerServes
        return if (teamTurnIndex % 2 == 0) firstServerOfTiebreak else !firstServerOfTiebreak
    }

    /**
     * Flips into tiebreak mode as soon as the set reaches six games all, so the
     * scoreboard and serve indicator are right before the first tiebreak point
     * rather than only after it. Whoever's turn it is to serve opens the
     * tiebreak, from the right.
     */
    private fun startTiebreakIfNeeded() {
        if (team1GamePoints == 6 && team2GamePoints == 6) {
            isTiebreak = true
            tiebreakStarterIsTeam1 = team1Serving
        }
    }

    private fun winSetViaTiebreak(team1Wins: Boolean) {
        if (team1Wins) team1SetScore++ else team2SetScore++
        // Clear the 6-6 games, otherwise the next set starts in a tiebreak.
        team1GamePoints = 0
        team2GamePoints = 0
        resetGame()
    }

    private fun checkSetWinner() {
        if (team1GamePoints >= 6 && team1GamePoints - team2GamePoints >= 2) {
            team1SetScore++
            team1GamePoints = 0
            team2GamePoints = 0
        } else if (team2GamePoints >= 6 && team2GamePoints - team1GamePoints >= 2) {
            team2SetScore++
            team1GamePoints = 0
            team2GamePoints = 0
        }
    }

    private fun resetGame() {
        team1Score = 0
        team2Score = 0
        team1Advantage = false
        team2Advantage = false
        pointsPlayedInGame = 0
        // The team that just served hands over to their partner for next time
        if (team1Serving) {
            team1RightSidePlayerServes = !team1RightSidePlayerServes
        } else {
            team2RightSidePlayerServes = !team2RightSidePlayerServes
        }
        // Other team serves next game
        team1Serving = !team1Serving
        isTiebreak = false
    }

    fun startMatch(team1ServesFirst: Boolean) {
        team1Serving = team1ServesFirst
        matchStarted = true
    }

    fun resetScore() {
        saveState()
        team1Score = 0
        team2Score = 0
        team1Advantage = false
        team2Advantage = false
        team1Serving = true
        team1RightSidePlayerServes = true
        team2RightSidePlayerServes = true
        team1GamePoints = 0
        team2GamePoints = 0
        team1SetScore = 0
        team2SetScore = 0
        isTiebreak = false
        tiebreakStarterIsTeam1 = true
        pointsPlayedInGame = 0
        matchStarted = false
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val prev = history.last()
            team1Score = prev.team1Score
            team2Score = prev.team2Score
            team1Advantage = prev.team1Advantage
            team2Advantage = prev.team2Advantage
            team1Serving = prev.team1Serving
            team1RightSidePlayerServes = prev.team1RightSidePlayerServes
            team2RightSidePlayerServes = prev.team2RightSidePlayerServes
            team1GamePoints = prev.team1GamePoints
            team2GamePoints = prev.team2GamePoints
            team1SetScore = prev.team1SetScore
            team2SetScore = prev.team2SetScore
            isTiebreak = prev.isTiebreak
            tiebreakStarterIsTeam1 = prev.tiebreakStarterIsTeam1
            pointsPlayedInGame = prev.pointsPlayedInGame
            history = history.dropLast(1)
        }
    }
}
