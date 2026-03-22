package com.dnodevelopment.padelcompanion.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Undo
import androidx.compose.ui.res.painterResource
import com.dnodevelopment.padelcompanion.R
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dnodevelopment.padelcompanion.presentation.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun StartScreen(
    onTeam1Selected: () -> Unit,
    onTeam2Selected: () -> Unit
) {
    PadelCompanionTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScoreBackground),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Who serves first?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(PadelGreenDark)
                            .clickable(onClick = onTeam1Selected),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Us",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBackground)
                            .clickable(onClick = onTeam2Selected),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Them",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClockDisplay() {
    var currentTime by remember { mutableStateOf("") }
    val formatter = SimpleDateFormat("HH:mm", Locale.getDefault())

    LaunchedEffect(key1 = true) {
        while (true) {
            currentTime = formatter.format(Date())
            delay(1000)
        }
    }

    Text(
        text = currentTime,
        fontSize = 12.sp,
        fontWeight = FontWeight.Light,
        color = TextSecondary,
        textAlign = TextAlign.Center
    )
}

@Composable
fun ScoreDisplay(
    team1Score: Int,
    team2Score: Int,
    team1Advantage: Boolean,
    team2Advantage: Boolean,
    serveRight: Boolean,
    team1Serving: Boolean,
    team1GamePoints: Int,
    team2GamePoints: Int,
    team1SetScore: Int,
    team2SetScore: Int,
    isTiebreak: Boolean,
    onTeam1Click: () -> Unit,
    onTeam2Click: () -> Unit,
    onReset: () -> Unit,
    onUndo: () -> Unit,
    getScoreDisplay: (Int) -> String
) {
    PadelCompanionTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ScoreBackground)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Top bar: Reset and Undo buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    IconButton(
                        onClick = onUndo,
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Undo,
                            contentDescription = "Undo",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Set score row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 2.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SETS",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(
                        text = "$team1SetScore",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PadelGreenLight
                    )
                    Text(
                        text = " : ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$team2SetScore",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PadelGreenLight
                    )
                }

                // Game score row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 1.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "GAMES",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = "$team1GamePoints",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PadelBlue
                    )
                    Text(
                        text = " : ",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = "$team2GamePoints",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PadelBlue
                    )
                }

                // Main score display
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Team 1 score box
                    TeamScoreBox(
                        score = if (isTiebreak) team1Score.toString() else getScoreDisplay(team1Score),
                        isServing = team1Serving,
                        serveRight = serveRight,
                        hasAdvantage = team1Advantage,
                        onClick = onTeam1Click,
                        modifier = Modifier.weight(1f)
                    )

                    // Separator
                    Text(
                        text = ":",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Light,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )

                    // Team 2 score box
                    TeamScoreBox(
                        score = if (isTiebreak) team2Score.toString() else getScoreDisplay(team2Score),
                        isServing = !team1Serving,
                        serveRight = serveRight,
                        hasAdvantage = team2Advantage,
                        onClick = onTeam2Click,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Tiebreak indicator
                if (isTiebreak) {
                    Text(
                        text = "TIEBREAK",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = PadelAmber,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Clock at the bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    ClockDisplay()
                }
            }
        }
    }
}

@Composable
private fun TeamScoreBox(
    score: String,
    isServing: Boolean,
    serveRight: Boolean,
    hasAdvantage: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CardBackground,
                        CardBackground.copy(alpha = 0.7f)
                    )
                )
            )
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        // Serve indicator - position indicates which player in the team is serving
        if (isServing) {
            Icon(
                painter = painterResource(id = R.drawable.ic_serve_indicator),
                contentDescription = if (serveRight) "Serve Right" else "Serve Left",
                tint = PadelAmber,
                modifier = Modifier
                    .size(10.dp)
                    .align(if (serveRight) Alignment.TopEnd else Alignment.TopStart)
                    .offset(
                        x = if (serveRight) (-4).dp else 4.dp,
                        y = 2.dp
                    )
            )
        }

        // Score + AD in a column so they don't overlap
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = score,
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            if (hasAdvantage) {
                Text(
                    text = "AD",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = PadelAmber,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}
