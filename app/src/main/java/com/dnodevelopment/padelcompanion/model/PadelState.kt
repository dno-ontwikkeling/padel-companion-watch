package com.dnodevelopment.padelcompanion.model

data class PadelState(
    val team1Score: Int,
    val team2Score: Int,
    val team1Advantage: Boolean,
    val team2Advantage: Boolean,
    val team1Serving: Boolean,
    val team1ServeRight: Boolean,
    val team2ServeRight: Boolean,
    val team1GamePoints: Int,
    val team2GamePoints: Int,
    val team1SetScore: Int,
    val team2SetScore: Int,
    val isTiebreak: Boolean
) 