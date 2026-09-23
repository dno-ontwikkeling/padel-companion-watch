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
    var team1ServeRight by mutableStateOf(true)
    var team2ServeRight by mutableStateOf(true)
    var team1GamePoints by mutableStateOf(0)
    var team2GamePoints by mutableStateOf(0)
    var team1SetScore by mutableStateOf(0)
    var team2SetScore by mutableStateOf(0)
    var isTiebreak by mutableStateOf(false)

    // Which team served the opening point of the current tiebreak. Serve then
    // rotates in blocks of two points, so this is the reference for working out
    // who serves next.
    private var tiebreakStarterIsTeam1 by mutableStateOf(true)

    private var history by mutableStateOf(listOf<PadelState>())

    /**
     * Which side the current server serves from.
     *
     * In a tiebreak the opening point is served from the right and the side
     * alternates every point, regardless of which team is serving. Outside a
     * tiebreak the side tracks which of the two partners is serving this game.
     */
    val currentServeRight: Boolean
        get() = when {
            isTiebreak -> (team1Score + team2Score) % 2 == 0
            team1Serving -> team1ServeRight
            else -> team2ServeRight
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
            val pointsPlayed = team1Score + team2Score
            pointsPlayed > 0 && pointsPlayed % 6 == 0
        } else {
            val gamesPlayed = team1GamePoints + team2GamePoints
            gamesPlayed > 0 && gamesPlayed % 2 == 1 &&
                team1Score == 0 && team2Score == 0 &&
                !team1Advantage && !team2Advantage
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
            team1Serving, team1ServeRight, team2ServeRight,
            team1GamePoints, team2GamePoints,
            team1SetScore, team2SetScore, isTiebreak,
            tiebreakStarterIsTeam1
        )
    }

    fun updateScore(team: Int) {
        saveState()

        when (team) {
            1 -> {
                if (isTiebreak) {
                    team1Score++
                    if (team1Score >= 7 && team1Score - team2Score >= 2) {
                        winSetViaTiebreak(team1Wins = true)
                    } else {
                        updateTiebreakServer()
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
                        updateTiebreakServer()
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
     * Works out who serves the next tiebreak point.
     *
     * The team that opened the tiebreak serves one point, then the teams take
     * two points each in turn. Numbering points from one, that means points
     * 1, 4, 5, 8, 9... belong to the opening team and 2, 3, 6, 7... to the other.
     */
    private fun updateTiebreakServer() {
        val nextPointNumber = team1Score + team2Score + 1
        val starterServes = (nextPointNumber / 2) % 2 == 0
        team1Serving = if (starterServes) tiebreakStarterIsTeam1 else !tiebreakStarterIsTeam1
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
        // Toggle the server within the current serving team for their next turn
        if (team1Serving) {
            team1ServeRight = !team1ServeRight
        } else {
            team2ServeRight = !team2ServeRight
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
        team1ServeRight = true
        team2ServeRight = true
        team1GamePoints = 0
        team2GamePoints = 0
        team1SetScore = 0
        team2SetScore = 0
        isTiebreak = false
        tiebreakStarterIsTeam1 = true
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
            team1ServeRight = prev.team1ServeRight
            team2ServeRight = prev.team2ServeRight
            team1GamePoints = prev.team1GamePoints
            team2GamePoints = prev.team2GamePoints
            team1SetScore = prev.team1SetScore
            team2SetScore = prev.team2SetScore
            isTiebreak = prev.isTiebreak
            tiebreakStarterIsTeam1 = prev.tiebreakStarterIsTeam1
            history = history.dropLast(1)
        }
    }
}
