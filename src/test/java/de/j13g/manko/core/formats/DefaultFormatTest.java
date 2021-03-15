package de.j13g.manko.core.formats;

import de.j13g.manko.RoundTest;
import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Placement;
import de.j13g.manko.core.TestEntrant;
import de.j13g.manko.core.base.Round;
import de.j13g.manko.core.base.TournamentFormat;
import de.j13g.manko.core.exceptions.FinalRoundException;
import de.j13g.manko.core.exceptions.RoundNotFinishedException;
import de.j13g.manko.core.rounds.Final;
import de.j13g.manko.core.rounds.SemiFinal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DefaultFormatTest extends RoundTest {

    private TournamentFormat<TestEntrant> format;

    private SemiFinal<TestEntrant> newSemiFinal;
    private SemiFinal<TestEntrant> pairedSemiFinal;
    private SemiFinal<TestEntrant> finishedSemiFinal;

    private TestEntrant firstWinner;
    private TestEntrant secondWinner;
    private TestEntrant firstLoser;
    private TestEntrant secondLoser;

    @BeforeEach
    void init() throws Exception {
        format = new DefaultFormat<>();

        newSemiFinal = createSemiFinal();

        pairedSemiFinal = createSemiFinal();
        Pairing<TestEntrant> firstPairing = pairedSemiFinal.nextPairing();
        Pairing<TestEntrant> secondPairing = pairedSemiFinal.nextPairing();
        firstWinner = firstPairing.getFirst();
        secondWinner = secondPairing.getFirst();
        firstLoser = firstPairing.getSecond();
        secondLoser = secondPairing.getSecond();
    }

    private SemiFinal<TestEntrant> createSemiFinal() {
        return new SemiFinal<>(first, second, third, fourth);
    }

    private Final<TestEntrant> createNextRound(SemiFinal<TestEntrant> semiFinal)
            throws FinalRoundException, RoundNotFinishedException {

        Round<TestEntrant> round = format.createNextRound(semiFinal);
        assertTrue(round instanceof Final);
        return (Final<TestEntrant>) round;
    }

    private void finishSemiFinal() {
        pairedSemiFinal.declareWinner(firstWinner);
        pairedSemiFinal.declareWinner(secondWinner);
        finishedSemiFinal = pairedSemiFinal;
        pairedSemiFinal = null;
    }

    // SemiFinal

    @Test
    void newSemiFinal_createNextRound_throwsRoundNotFinishedException() {
        assertThrows(RoundNotFinishedException.class, () -> createNextRound(newSemiFinal));
    }

    @Test
    void finishedSemiFinal_removedOneWinnerAndLoser_throwsFinalRoundException() {
        finishSemiFinal();
        finishedSemiFinal.removeEntrant(firstWinner);
        finishedSemiFinal.removeEntrant(firstLoser);

        assertThrows(FinalRoundException.class, () -> createNextRound(finishedSemiFinal));
    }

    @Test
    void finishedSemiFinal_removedOneWinnerAndBothLosers_throwsFinalRoundException() {
        finishSemiFinal();
        finishedSemiFinal.removeEntrant(firstWinner);
        finishedSemiFinal.removeEntrant(firstLoser);
        finishedSemiFinal.removeEntrant(secondLoser);

        assertThrows(FinalRoundException.class, () -> createNextRound(finishedSemiFinal));
    }

    @Test
    void finishedSemiFinal_createNextRound_pairsWinnersAndLosers() {
        finishSemiFinal();
        Final<TestEntrant> finalRound = assertDoesNotThrow(() -> createNextRound(finishedSemiFinal));
        List<Pairing<TestEntrant>> upcomingPairings = finalRound.getUpcomingPairings();

        assertTrue(upcomingPairings.contains(new Pairing<>(firstWinner, secondWinner)));
        assertTrue(upcomingPairings.contains(new Pairing<>(firstLoser, secondLoser)));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void finishedSemiFinal_removeOneWinner_winnerHasWonTheTournament(int i) {
        TestEntrant firstPlace = i == 0 ? firstWinner : secondWinner;
        TestEntrant secondPlace = i == 0 ? secondWinner : firstWinner;

        finishSemiFinal();
        finishedSemiFinal.removeEntrant(secondPlace);
        Final<TestEntrant> finalRound = assertDoesNotThrow(() -> createNextRound(finishedSemiFinal));
        List<Pairing<TestEntrant>> upcomingPairings = finalRound.getUpcomingPairings();

        assertTrue(finalRound.hasWon(firstPlace));
        assertTrue(finalRound.hasLost(secondPlace));
        assertEquals(Placement.FIRST, finalRound.getPlacement(firstPlace));
        assertEquals(Placement.SECOND, finalRound.getPlacement(secondPlace));
        assertEquals(1, upcomingPairings.size());
        assertEquals(new Pairing<>(firstLoser, secondLoser), upcomingPairings.get(0));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void finishedSemiFinal_removeOneLoser_otherLoserHasThirdPlace(int i) {
        TestEntrant thirdPlace = i == 0 ? firstLoser : secondLoser;
        TestEntrant fourthPlace = i == 0 ? secondLoser : firstLoser;

        finishSemiFinal();
        finishedSemiFinal.removeEntrant(fourthPlace);
        Final<TestEntrant> finalRound = assertDoesNotThrow(() -> createNextRound(finishedSemiFinal));
        List<Pairing<TestEntrant>> upcomingPairings = finalRound.getUpcomingPairings();

        assertTrue(finalRound.hasWon(thirdPlace));
        assertTrue(finalRound.hasLost(fourthPlace));
        assertEquals(Placement.THIRD, finalRound.getPlacement(thirdPlace));
        assertEquals(Placement.NONE, finalRound.getPlacement(fourthPlace));
        assertEquals(1, upcomingPairings.size());
        assertEquals(new Pairing<>(firstWinner, secondWinner), upcomingPairings.get(0));
    }
}
