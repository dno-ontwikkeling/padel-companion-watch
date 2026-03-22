package com.dnodevelopment.padelcompanion

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.remember
import com.dnodevelopment.padelcompanion.ui.components.ScoreDisplay
import com.dnodevelopment.padelcompanion.ui.components.StartScreen
import com.dnodevelopment.padelcompanion.viewmodel.PadelViewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel = remember { PadelViewModel() }
            if (!viewModel.matchStarted) {
                StartScreen(
                    onTeam1Selected = { viewModel.startMatch(true) },
                    onTeam2Selected = { viewModel.startMatch(false) }
                )
            } else {
                ScoreDisplay(
                    team1Score = viewModel.team1Score,
                    team2Score = viewModel.team2Score,
                    team1Advantage = viewModel.team1Advantage,
                    team2Advantage = viewModel.team2Advantage,
                    serveRight = viewModel.currentServeRight,
                    team1Serving = viewModel.team1Serving,
                    team1GamePoints = viewModel.team1GamePoints,
                    team2GamePoints = viewModel.team2GamePoints,
                    team1SetScore = viewModel.team1SetScore,
                    team2SetScore = viewModel.team2SetScore,
                    isTiebreak = viewModel.isTiebreak,
                    onTeam1Click = { viewModel.updateScore(1) },
                    onTeam2Click = { viewModel.updateScore(2) },
                    onReset = { viewModel.resetScore() },
                    onUndo = { viewModel.undo() },
                    getScoreDisplay = { viewModel.getScoreDisplay(it) }
                )
            }
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
} 