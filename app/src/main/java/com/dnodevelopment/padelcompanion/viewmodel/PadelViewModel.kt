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
    private var history by mutableStateOf(listOf<PadelState>())

    // Which side the current server is on
    val currentServeRight: Boolean
        get() = if (team1Serving) team1ServeRight else team2ServeRight

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
            team1SetScore, team2SetScore, isTiebreak
        )
    }

    fun updateScore(team: Int) {
        saveState()

        if (team1GamePoints >= 6 && team2GamePoints >= 6 && !isTiebreak) {
            isTiebreak = true
        }

        when (team) {
            1 -> {
                if (isTiebreak) {
                    team1Score++
                    if (team1Score >= 7 && team1Score - team2Score >= 2) {
                        team1SetScore++
                        resetGame()
                    }
                } else if (team1Score >= 3 && team2Score >= 3) {
                    if (team2Advantage) {
                        team2Advantage = false
                    } else if (team1Advantage) {
                        team1GamePoints++
                        checkSetWinner()
                        resetGame()
                    } else {
                        team1Advantage = true
                    }
                } else {
                    team1Score++
                    if (team1Score > 3) {
                        team1GamePoints++
                        checkSetWinner()
                        resetGame()
                    }
                }
            }
            2 -> {
                if (isTiebreak) {
                    team2Score++
                    if (team2Score >= 7 && team2Score - team1Score >= 2) {
                        team2SetScore++
                        resetGame()
                    }
                } else if (team1Score >= 3 && team2Score >= 3) {
                    if (team1Advantage) {
                        team1Advantage = false
                    } else if (team2Advantage) {
                        team2GamePoints++
                        checkSetWinner()
                        resetGame()
                    } else {
                        team2Advantage = true
                    }
                } else {
                    team2Score++
                    if (team2Score > 3) {
                        team2GamePoints++
                        checkSetWinner()
                        resetGame()
                    }
                }
            }
        }
    }

    private fun checkSetWinner() {
        if (team1GamePoints >= 6 && team1GamePoints - team2GamePoints >= 2) {
            team1SetScore++
            resetSet()
        } else if (team2GamePoints >= 6 && team2GamePoints - team1GamePoints >= 2) {
            team2SetScore++
            resetSet()
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

    private fun resetSet() {
        team1GamePoints = 0
        team2GamePoints = 0
        resetGame()
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
            history = history.dropLast(1)
        }
    }
}
