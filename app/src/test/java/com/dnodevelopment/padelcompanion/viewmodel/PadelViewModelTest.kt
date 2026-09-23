package com.dnodevelopment.padelcompanion.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PadelViewModelTest {

    /** Wins a game outright for [team] from 0-0: four straight points. */
    private fun PadelViewModel.winGame(team: Int) = repeat(4) { updateScore(team) }

    /** Plays out six games each, leaving the set at 6-6 and a tiebreak underway. */
    private fun viewModelAtSixGamesAll(team1ServesFirst: Boolean = true): PadelViewModel {
        val viewModel = PadelViewModel()
        viewModel.startMatch(team1ServesFirst)
        repeat(6) {
            viewModel.winGame(1)
            viewModel.winGame(2)
        }
        return viewModel
    }

    @Test
    fun `tiebreak starts at six games all and opens from the right`() {
        val viewModel = viewModelAtSixGamesAll()

        assertTrue("6-6 should be a tiebreak", viewModel.isTiebreak)
        assertEquals(6, viewModel.team1GamePoints)
        assertEquals(6, viewModel.team2GamePoints)
        assertEquals(0, viewModel.team1Score)
        assertEquals(0, viewModel.team2Score)
        assertTrue("opening tiebreak point is served from the right", viewModel.currentServeRight)
    }

    @Test
    fun `tiebreak serve rotates one point then two points each`() {
        val viewModel = viewModelAtSixGamesAll()
        val starter = viewModel.team1Serving

        // Point:                 1     2      3      4     5     6      7      8     9     10
        val openerServes = listOf(true, false, false, true, true, false, false, true, true, false)

        openerServes.forEachIndexed { index, expected ->
            assertEquals(
                "point ${index + 1} should be served by the ${if (expected) "opening" else "other"} team",
                expected,
                viewModel.team1Serving == starter
            )
            // Alternate the scorer so the tiebreak does not finish early.
            viewModel.updateScore(if (index % 2 == 0) 1 else 2)
        }
    }

    @Test
    fun `tiebreak serve side alternates every point`() {
        val viewModel = viewModelAtSixGamesAll()

        repeat(8) { index ->
            assertEquals(
                "point ${index + 1} should be served from the ${if (index % 2 == 0) "right" else "left"}",
                index % 2 == 0,
                viewModel.currentServeRight
            )
            viewModel.updateScore(if (index % 2 == 0) 1 else 2)
        }
    }

    @Test
    fun `opponent serves points two and three from left then right`() {
        val viewModel = viewModelAtSixGamesAll()
        val starter = viewModel.team1Serving

        viewModel.updateScore(1) // point 1 played
        assertEquals("opponent serves point 2", !starter, viewModel.team1Serving)
        assertFalse("point 2 from the left", viewModel.currentServeRight)

        viewModel.updateScore(2) // point 2 played
        assertEquals("opponent still serving for point 3", !starter, viewModel.team1Serving)
        assertTrue("point 3 from the right", viewModel.currentServeRight)

        viewModel.updateScore(1) // point 3 played
        assertEquals("serve returns to the opening team", starter, viewModel.team1Serving)
    }

    @Test
    fun `winning a tiebreak wins the set and does not start another tiebreak`() {
        val viewModel = viewModelAtSixGamesAll()

        repeat(7) { viewModel.updateScore(1) }

        assertEquals(1, viewModel.team1SetScore)
        assertFalse(viewModel.isTiebreak)
        assertEquals("games must reset after a tiebreak", 0, viewModel.team1GamePoints)
        assertEquals("games must reset after a tiebreak", 0, viewModel.team2GamePoints)

        viewModel.updateScore(1)
        assertFalse("the next set must not begin in a tiebreak", viewModel.isTiebreak)
    }

    @Test
    fun `serve changes hands after a set is won on games`() {
        val viewModel = PadelViewModel()
        viewModel.startMatch(true)
        repeat(5) {
            viewModel.winGame(1)
            viewModel.winGame(2)
        }
        viewModel.winGame(1) // 6-5

        val servingBeforeSet = viewModel.team1Serving
        viewModel.winGame(1) // 7-5, set to team 1

        assertEquals(1, viewModel.team1SetScore)
        assertEquals(0, viewModel.team1GamePoints)
        assertNotEquals(
            "serve must pass to the other team after the set",
            servingBeforeSet,
            viewModel.team1Serving
        )
    }

    @Test
    fun `ends change after every odd game`() {
        val viewModel = PadelViewModel()
        viewModel.startMatch(true)
        assertFalse("no change at 0-0", viewModel.shouldChangeSides)

        viewModel.winGame(1) // 1 game played
        assertTrue("change after game 1", viewModel.shouldChangeSides)

        viewModel.updateScore(1)
        assertFalse("prompt clears once the next point is played", viewModel.shouldChangeSides)

        repeat(3) { viewModel.updateScore(1) } // finishes game 2
        assertFalse("no change after game 2", viewModel.shouldChangeSides)

        viewModel.winGame(2) // 3 games played
        assertTrue("change after game 3", viewModel.shouldChangeSides)
    }

    @Test
    fun `ends change every six tiebreak points`() {
        val viewModel = viewModelAtSixGamesAll()
        assertFalse("no change at the start of the tiebreak", viewModel.shouldChangeSides)

        repeat(6) { index -> viewModel.updateScore(if (index % 2 == 0) 1 else 2) } // 3-3
        assertTrue("change after 6 points", viewModel.shouldChangeSides)

        viewModel.updateScore(1) // 4-3
        assertFalse("prompt clears once the next point is played", viewModel.shouldChangeSides)

        repeat(5) { index -> viewModel.updateScore(if (index % 2 == 0) 2 else 1) } // 6-6
        assertEquals(12, viewModel.team1Score + viewModel.team2Score)
        assertTrue("change after 12 points", viewModel.shouldChangeSides)
    }

    @Test
    fun `undo restores the tiebreak server`() {
        val viewModel = viewModelAtSixGamesAll()
        val starter = viewModel.team1Serving

        viewModel.updateScore(1)
        assertEquals(!starter, viewModel.team1Serving)

        viewModel.undo()
        assertEquals("undo must put the serve back", starter, viewModel.team1Serving)
        assertTrue(viewModel.isTiebreak)
        assertEquals(0, viewModel.team1Score)
    }
}
