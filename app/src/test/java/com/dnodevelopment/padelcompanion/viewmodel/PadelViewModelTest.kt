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
    fun `server stays the same all game while the service box alternates`() {
        val viewModel = PadelViewModel()
        viewModel.startMatch(true)

        // Games 0-0, points 0-0: us serving, right-side player, from the right.
        assertTrue(viewModel.team1Serving)
        assertTrue(viewModel.servingPlayerIsRightSide)
        assertTrue(viewModel.serveFromRight)

        // Games 0-0, points 15-0: same player, now serving from the left.
        viewModel.updateScore(1)
        assertTrue(viewModel.team1Serving)
        assertTrue("the server does not change mid-game", viewModel.servingPlayerIsRightSide)
        assertFalse(viewModel.serveFromRight)
    }

    @Test
    fun `serve passes to the other team each game and partners alternate`() {
        val viewModel = PadelViewModel()
        viewModel.startMatch(true)

        viewModel.winGame(1) // games 1-0

        // Them serving, their right-side player, from the right.
        assertFalse(viewModel.team1Serving)
        assertTrue(viewModel.servingPlayerIsRightSide)
        assertTrue(viewModel.serveFromRight)
        viewModel.updateScore(1)
        assertFalse(viewModel.serveFromRight)
        repeat(3) { viewModel.updateScore(1) } // games 2-0

        // Back to us, and now our other player serves.
        assertTrue(viewModel.team1Serving)
        assertFalse("our left-side player serves our second game", viewModel.servingPlayerIsRightSide)
        assertTrue(viewModel.serveFromRight)
        viewModel.updateScore(1)
        assertFalse(viewModel.serveFromRight)
    }

    @Test
    fun `service box keeps alternating through deuce and advantage`() {
        val viewModel = PadelViewModel()
        viewModel.startMatch(true)

        repeat(3) { viewModel.updateScore(1) }
        repeat(3) { viewModel.updateScore(2) } // deuce, six points played
        assertTrue("deuce is served from the right", viewModel.serveFromRight)

        viewModel.updateScore(1) // advantage
        assertFalse("the advantage point is served from the left", viewModel.serveFromRight)

        viewModel.updateScore(2) // back to deuce
        assertTrue(viewModel.serveFromRight)
    }

    @Test
    fun `tiebreak starts at six games all and opens from the right`() {
        val viewModel = viewModelAtSixGamesAll()

        assertTrue("6-6 should be a tiebreak", viewModel.isTiebreak)
        assertEquals(6, viewModel.team1GamePoints)
        assertEquals(6, viewModel.team2GamePoints)
        assertTrue("opening tiebreak point is served from the right", viewModel.serveFromRight)
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
    fun `tiebreak service box alternates every point`() {
        val viewModel = viewModelAtSixGamesAll()

        repeat(8) { index ->
            assertEquals(
                "point ${index + 1} should be served from the ${if (index % 2 == 0) "right" else "left"}",
                index % 2 == 0,
                viewModel.serveFromRight
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
        assertFalse("point 2 from the left", viewModel.serveFromRight)

        viewModel.updateScore(2) // point 2 played
        assertEquals("opponent still serving for point 3", !starter, viewModel.team1Serving)
        assertTrue("point 3 from the right", viewModel.serveFromRight)

        viewModel.updateScore(1) // point 3 played
        assertEquals("serve returns to the opening team", starter, viewModel.team1Serving)
    }

    @Test
    fun `all four players serve in rotation during a tiebreak`() {
        val viewModel = viewModelAtSixGamesAll()

        // (serving team, is it that team's right-side player) for points 1..9
        val servers = mutableListOf<Pair<Boolean, Boolean>>()
        repeat(9) { index ->
            servers.add(viewModel.team1Serving to viewModel.servingPlayerIsRightSide)
            viewModel.updateScore(if (index % 2 == 0) 1 else 2)
        }

        val opener = servers[0]

        assertEquals("points 2 and 3 go to the other team", !opener.first, servers[1].first)
        assertEquals("the same player serves points 2 and 3", servers[1], servers[2])

        assertEquals("points 4 and 5 return to the opening team", opener.first, servers[3].first)
        assertEquals("but their other player serves", !opener.second, servers[3].second)
        assertEquals("the same player serves points 4 and 5", servers[3], servers[4])

        assertEquals("points 6 and 7 go to the other team", !opener.first, servers[5].first)
        assertEquals("their other player serves", !servers[1].second, servers[5].second)
        assertEquals("the same player serves points 6 and 7", servers[5], servers[6])

        assertEquals("point 8 comes back round to the opening player", opener, servers[7])
        assertEquals("who also serves point 9", servers[7], servers[8])
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
        assertTrue("and put the serve back on the right", viewModel.serveFromRight)
    }
}
