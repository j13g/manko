package io.github.vonas.manko.core;

import io.github.vonas.manko.exceptions.*;
import io.github.vonas.manko.util.TestEntrant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class DynamicRoundTest {

    private final Entrant entrantA = TestEntrant.createUnique();
    private final Entrant entrantB = TestEntrant.createUnique();
    private final Entrant invalidEntrant = TestEntrant.createUnique();

    private final Entrant winner = entrantA;
    private final Entrant loser = entrantB;

    private DynamicRound round;
    private DynamicRound twoEntrantRound;

    @BeforeEach
    void init() {
        round = new DynamicRound();
        twoEntrantRound = new DynamicRound(new HashSet<>(Arrays.asList(entrantA, entrantB)));
    }

    // TODO Add test that checks if nextPairing()
    //  shuffles the remaining pending entrants.

    @Test
    void noEntrants_addEntrant_isPending() {
        round.addEntrant(entrantA);
        List<Entrant> pendingEntrants = round.getPendingEntrants();
        assertEquals(1, pendingEntrants.size());
        assertEquals(entrantA, pendingEntrants.get(0));
    }

    @Test
    void noEntrants_nextPairing_throwsNoEntrants() {
        assertThrows(NoEntrantsException.class, () -> round.nextPairing());
    }

    @Test
    void oneEntrant_nextPairing_throwsNoOpponent() {
        round.addEntrant(entrantA);
        assertThrows(NoOpponentException.class, () -> round.nextPairing());
    }

    @Test
    void twoEntrants_nextPairing_noPendingEntrants() throws Exception {
        twoEntrantRound.nextPairing();
        assertTrue(twoEntrantRound.getPendingEntrants().isEmpty());
    }

    @Test
    void twoEntrants_nextPairing_returnsBothEntrants() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        Entrant first = pairing.getEntrant1();
        Entrant second = pairing.getEntrant2();
        if (first != entrantA) {
            Entrant tmp = first;
            first = second;
            second = tmp;
        }
        assertEquals(entrantA, first);
        assertEquals(entrantB, second);
    }

    @Test
    void twoEntrants_nextPairing_updatesCurrentPairing() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        assertTrue(twoEntrantRound.hasActivePairing());
        assertEquals(pairing, twoEntrantRound.getActivePairing());
    }

    @Test
    void pairing_declareInvalidEntrantAsWinner_throwsNoSuchEntrant() throws Exception {
        twoEntrantRound.nextPairing();
        assertThrows(NoSuchEntrantException.class, () -> twoEntrantRound.declareWinner(invalidEntrant));
    }

    @Test
    void noPairing_declareWinner_throwsMissingPairing() {
        assertThrows(MissingPairingException.class, () -> twoEntrantRound.declareWinner(entrantA));
    }

    @Test
    void pairing_declareWinner_winnerAdvancesAndLoserEliminated() throws Exception {
        twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        Set<Entrant> advancedEntrants = twoEntrantRound.getAdvancedEntrants();
        Set<Entrant> eliminatedEntrants = twoEntrantRound.getEliminatedEntrants();
        assertEquals(1, advancedEntrants.size());
        assertEquals(1, eliminatedEntrants.size());
        assertTrue(advancedEntrants.contains(winner));
        assertTrue(eliminatedEntrants.contains(loser));
    }

    @Test
    void pairing_declareWinner_pairingFinished() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        Set<Pairing> finishedPairings = twoEntrantRound.getFinishedPairings();
        assertTrue(finishedPairings.contains(pairing));
        assertEquals(1, finishedPairings.size());
    }

    @Test
    void twoEntrants_declareWinner_roundFinished() throws Exception {
        twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        assertTrue(twoEntrantRound.isFinished());
    }

    @Test
    void oneEntrant_removeEntrant_noEntrants() {
        round.addEntrant(entrantA);
        round.removeEntrant(entrantA);
        assertTrue(round.getEntrants().isEmpty());
    }

    @Test
    void pairingPending_removeOneEntrant_otherEntrantPending() throws Exception {
        twoEntrantRound.nextPairing();
        twoEntrantRound.removeEntrant(entrantA);
        assertTrue(twoEntrantRound.getPendingEntrants().contains(entrantB));
    }

    @Test
    void finishedRound_removeAllEntrants_noEntrants() throws Exception {
        twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(entrantA);
        twoEntrantRound.removeEntrant(entrantA);
        twoEntrantRound.removeEntrant(entrantB);
        assertEquals(0, twoEntrantRound.getEntrants().size());
        assertEquals(0, twoEntrantRound.getPendingEntrants().size());
        assertEquals(0, twoEntrantRound.getAdvancedEntrants().size());
        assertEquals(0, twoEntrantRound.getEliminatedEntrants().size());
        assertEquals(0, twoEntrantRound.getFinishedPairings().size());
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    void finishedRound_removeOneEntrant_keepAllPairings(int e) throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        twoEntrantRound.removeEntrant(e == 0 ? entrantA : entrantB);
        assertEquals(1, twoEntrantRound.getFinishedPairings().size());
    }

    @Test
    void eliminatedEntrant_resetEliminated_eliminatedPending() throws Exception {
        twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        twoEntrantRound.resetEntrant(loser);
        assertTrue(twoEntrantRound.getEliminatedEntrants().isEmpty());
        assertTrue(twoEntrantRound.getPendingEntrants().contains(loser));
    }

    @Test
    void loserRemoved_resetWinner_pastPairingExists() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        twoEntrantRound.removeEntrant(loser);
        twoEntrantRound.resetEntrant(winner);
        assertFalse(twoEntrantRound.getFinishedPairings().isEmpty());
    }

    @Test
    void finishedRound_resetAll_previousPairingsUnchanged() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        Set<Pairing> finishedPairings = twoEntrantRound.getFinishedPairings();
        twoEntrantRound.resetEntrant(winner);
        twoEntrantRound.resetEntrant(loser);
        assertEquals(finishedPairings, twoEntrantRound.getFinishedPairings());
    }

    @Test
    void pendingEntrant_resetPendingEntrant_noDuplicatePendingEntrant() throws Exception {
        round.addEntrant(entrantA);
        round.resetEntrant(entrantA);
        assertEquals(1, round.getPendingEntrants().size());
    }

    @Test
    void finishedPairing_removeEntrantAndRedoPairing_throwsMissingEntrantException() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        twoEntrantRound.removeEntrant(winner);
        assertThrows(MissingEntrantException.class, () -> twoEntrantRound.redoPairing(pairing));
    }

    @Test
    void finishedPairing_redoPairing_pairingBecomesActive() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        twoEntrantRound.declareWinner(winner);
        twoEntrantRound.redoPairing(pairing);
        assertEquals(twoEntrantRound.activePairing, pairing);
        assertTrue(twoEntrantRound.getFinishedPairings().isEmpty());
    }

    @Test
    void finishedPairing_passEqualButNonIdenticalPairingToRedo_redoesFinishedPairing() throws Exception {
        Pairing pairing = twoEntrantRound.nextPairing();
        Pairing equalPairing = new Pairing(pairing.getEntrant1(), pairing.getEntrant2());
        twoEntrantRound.declareWinner(winner);
        twoEntrantRound.redoPairing(equalPairing);
        assertTrue(twoEntrantRound.getFinishedPairings().isEmpty());
    }
}
