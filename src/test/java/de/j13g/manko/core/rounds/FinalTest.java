package de.j13g.manko.core.rounds;

import de.j13g.manko.RoundTest;
import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Placement;
import de.j13g.manko.core.TestEntrant;
import de.j13g.manko.core.exceptions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FinalTest extends RoundTest {

    private final List<TestEntrant> entrants = Arrays.asList(
            first, second, third, fourth
    );

    private final Pairing<TestEntrant> firstPlacePairing = new Pairing<>(first, second);
    private final Pairing<TestEntrant> thirdPlacePairing = new Pairing<>(third, fourth);

    private Final<TestEntrant> newFinal;
    private Final<TestEntrant> finalAtThirdPlace;
    private Final<TestEntrant> finalAtFirstPlace;
    private Final<TestEntrant> finishedFinal;

    @BeforeEach
    void init() {
        newFinal = createFinal();

        finalAtThirdPlace = createFinal();
        finalAtThirdPlace.nextPairing();

        finalAtFirstPlace = createFinal();
        finalAtFirstPlace.nextPairing();
        finalAtFirstPlace.declareWinner(third);
        finalAtFirstPlace.nextPairing();

        finishedFinal = createFinal();
        finishedFinal.nextPairing();
        finishedFinal.declareWinner(third);
        finishedFinal.nextPairing();
        finishedFinal.declareWinner(first);
    }

    private Final<TestEntrant> createFinal() {
        return new Final<>(firstPlacePairing, thirdPlacePairing);
    }

    // construct

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void overlappingPairings_createWithOverlappingPairings_throwsIllegalArgumentException(int i) {
        TestEntrant entrant = entrants.get(i);
        TestEntrant otherEntrant = entrants.get(i + 1);
        Pairing<TestEntrant> invalidPairing = new Pairing<>(entrant, otherEntrant);
        assertThrows(IllegalArgumentException.class, () -> new Final<>(firstPlacePairing, invalidPairing));
    }

    @Test
    void newFinal_getUpcomingPairings_thirdPlacePairingComesFirst() {
        List<Pairing<TestEntrant>> upcomingPairings = newFinal.getUpcomingPairings();
        assertEquals(2, upcomingPairings.size());
        assertEquals(thirdPlacePairing, upcomingPairings.get(0));
        assertEquals(firstPlacePairing, upcomingPairings.get(1));
    }

    @Test
    void newFinal_getPlacements_allEntrantsAreToBeDetermined() {
        entrants.forEach(e -> assertEquals(Placement.TBD, newFinal.getPlacement(e)));
    }

    // addEntrant

    @Test
    void newFinal_addNonParticipatingEntrant_throwsInvalidEntrantException() {
        assertThrows(NewEntrantsNotAllowedException.class, () -> newFinal.addEntrant(invalidEntrant));
    }

    @Test
    void newFinal_addAlreadyContainedEntrant_returnsFalse() {
        assertFalse(assertDoesNotThrow(() -> newFinal.addEntrant(first)));
    }

    // removeEntrant

    @Test
    void newFinal_removeEntrant_isRemoved() {
        newFinal.removeEntrant(first);
        assertEquals(entrants.size() - 1, newFinal.getEntrants().size());
        assertFalse(newFinal.hasEntrant(first));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void newFinal_removeEntrant_entrantPlacementIsNone(int i) {
        TestEntrant entrant = entrants.get(i);
        newFinal.removeEntrant(entrant);
        assertEquals(Placement.NONE, newFinal.getPlacement(entrant));
    }

    @Test
    void final_removePendingOrActiveEntrant_pairingNeitherUpcomingNorFinished() {
        newFinal.removeEntrant(first);
        finalAtThirdPlace.removeEntrant(third);
        assertFalse(newFinal.getFinishedPairings().contains(thirdPlacePairing));
        assertFalse(finalAtThirdPlace.getFinishedPairings().contains(thirdPlacePairing));
    }

    @Test
    void newFinal_removeEntrant_pairingFinishedAndOtherWon() {
        newFinal.removeEntrant(first);
        assertFalse(newFinal.getUpcomingPairings().contains(firstPlacePairing));
        assertEquals(Placement.FIRST, newFinal.getPlacement(second));
    }

    @Test
    void finalAtThirdPlace_removeActiveEntrant_pairingIsFinishedAndOtherWon() {
        finalAtThirdPlace.removeEntrant(third);
        assertTrue(finalAtThirdPlace.getActivePairings().isEmpty());
        assertEquals(1, finalAtThirdPlace.getUpcomingPairings().size());
        assertEquals(Placement.THIRD, finalAtThirdPlace.getPlacement(fourth));
        assertEquals(Placement.NONE, finalAtThirdPlace.getPlacement(third));
    }

    @Test
    void finalAtFirstPlace_removeThirdPlace_noChangeInPlacements() {
        finalAtFirstPlace.removeEntrant(third);
        assertEquals(Placement.THIRD, finalAtFirstPlace.getPlacement(third));
        assertEquals(Placement.NONE, finalAtFirstPlace.getPlacement(fourth));
    }

    // nextPairing

    @Test
    void newFinal_nextPairing_returnsThirdPlacePairing() {
        Pairing<TestEntrant> pairing = newFinal.nextPairing();
        assertEquals(thirdPlacePairing, pairing);
    }

    @Test
    void newFinal_nextPairing_upcomingPairingsContainsOnlyFirstPlacePairing() {
        newFinal.nextPairing();
        List<Pairing<TestEntrant>> upcomingPairings = newFinal.getUpcomingPairings();
        assertEquals(1, upcomingPairings.size());
        assertEquals(firstPlacePairing, upcomingPairings.get(0));
    }

    @Test
    void newFinal_nextPairingThreeTimes_throwsNoMorePairingsException() {
        newFinal.nextPairing();
        newFinal.nextPairing();
        assertThrows(NoMorePairingsException.class, newFinal::nextPairing);
    }

    // replayPairing

    @Test
    void finalAtFirstPlace_replayThirdPlace_everyEntrantIsPaired() {
        finalAtFirstPlace.replayPairing(thirdPlacePairing);
        entrants.forEach(e -> assertTrue(finalAtFirstPlace.isEntrantPaired(e)));
    }

    @Test
    void finalAtFirstPlace_removeThirdPlaceAndReplay_throwsMissingEntrantException() {
        finalAtFirstPlace.removeEntrant(third);
        assertThrows(MissingEntrantException.class, () -> finalAtFirstPlace.replayPairing(thirdPlacePairing));
    }

    // declareWinner

    @Test
    void newFinal_declareWinnerWithInvalidEntrant_throwsNoSuchEntrantException() {
        assertThrows(NoSuchEntrantException.class, () -> newFinal.declareWinner(invalidEntrant));
    }

    @Test
    void newFinal_declareWinnerWithoutActivePairing_throwsNoSuchPairingException() {
        assertThrows(NoSuchPairingException.class, () -> newFinal.declareWinner(third, thirdPlacePairing));
    }

    @Test
    void finalAtThirdPlace_declareWinner_returnsFinishedPairing() {
        Set<Pairing<TestEntrant>> oldActive = new HashSet<>(finalAtThirdPlace.getActivePairings());
        Pairing<TestEntrant> pairing = finalAtThirdPlace.declareWinner(third);
        assertTrue(oldActive.contains(pairing));
        assertTrue(finalAtThirdPlace.getFinishedPairings().contains(pairing));
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3})
    void finalAtThirdPlace_declareWinner_pairingIsFinished(int i) {
        TestEntrant entrant = entrants.get(i);
        Pairing<TestEntrant> pairing = finalAtThirdPlace.declareWinner(entrant);
        assertTrue(finalAtThirdPlace.getActivePairings().isEmpty());
        assertTrue(finalAtThirdPlace.getFinishedPairings().contains(pairing));
    }

    @Test
    void finalAtThirdPlace_declareThirdWinner_thirdIsThirdPlaceAndFourthIsNone() {
        finalAtThirdPlace.declareWinner(third);
        assertEquals(Placement.THIRD, finalAtThirdPlace.getPlacement(third));
        assertEquals(Placement.NONE, finalAtThirdPlace.getPlacement(fourth));

    }

    @Test
    void finalAtFirstPlace_declareFirstWinner_firstIsFirstPlaceAndSecondIsSecondPlace() {
        finalAtFirstPlace.declareWinner(first);
        assertEquals(Placement.FIRST, finalAtFirstPlace.getPlacement(first));
        assertEquals(Placement.SECOND, finalAtFirstPlace.getPlacement(second));
    }

    // Identities

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void newFinal_removeEntrantThenAddBack_stateLikeBefore(int i) {
        TestEntrant entrant = entrants.get(i);
        newFinal.removeEntrant(entrant);
        newFinal.addEntrant(entrant);
        entrants.forEach(e -> assertEquals(Placement.TBD, newFinal.getPlacement(e)));
        List<Pairing<TestEntrant>> upcomingPairings = newFinal.getUpcomingPairings();
        assertEquals(thirdPlacePairing, upcomingPairings.get(0));
        assertEquals(firstPlacePairing, upcomingPairings.get(1));
    }

    // Miscellaneous

    @Test
    void finishedFinal_finished_correctPlacementsAndFinishedPairings() {
        assertEquals(first, finishedFinal.getEntrantByPlacement(Placement.FIRST));
        assertEquals(second, finishedFinal.getEntrantByPlacement(Placement.SECOND));
        assertEquals(third, finishedFinal.getEntrantByPlacement(Placement.THIRD));
        assertEquals(Placement.NONE, finishedFinal.getPlacement(fourth));
        assertEquals(2, finishedFinal.getFinishedPairings().size());
    }
}
