package de.j13g.manko.core;

import de.j13g.manko.core.exceptions.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.*;

import static de.j13g.manko.Helper.assertSuppliesAll;
import static org.junit.jupiter.api.Assertions.*;

public class DynamicRoundTest {

    private final Random random = new Random(0);

    private final TestEntrant first = createEntrant();
    private final TestEntrant second = createEntrant();
    private final TestEntrant third = createEntrant();
    private final TestEntrant invalidEntrant = createEntrant();

    private final List<TestEntrant> entrants = Arrays.asList(
        first, second, third, createEntrant(),
        createEntrant(), createEntrant(), createEntrant(),
        createEntrant(), createEntrant(), createEntrant()
    );

    private final TestEntrant winner = first;
    private final TestEntrant loser = second;

    private DynamicRound<TestEntrant> emptyRound;
    private DynamicRound<TestEntrant> oneEntrantRound;
    private DynamicRound<TestEntrant> twoEntrantRound;
    private DynamicRound<TestEntrant> multiEntrantRound;

    private DynamicRound<TestEntrant> singlePairRound;
    private DynamicRound<TestEntrant> singlePairFinishedRound;

    @BeforeEach
    void init() {
        emptyRound = new DynamicRound<>();

        oneEntrantRound = new DynamicRound<>();
        oneEntrantRound.add(first);

        twoEntrantRound = new DynamicRound<>();
        twoEntrantRound.add(first);
        twoEntrantRound.add(second);

        multiEntrantRound = createMultiEntrantRound();

        singlePairRound = new DynamicRound<>();
        singlePairRound.add(first);
        singlePairRound.add(second);
        assertDoesNotThrow(() -> singlePairRound.nextPairing());

        singlePairFinishedRound = createSinglePairFinishedRound();
    }

    private TestEntrant createEntrant() {
        return new TestEntrant(random.nextInt());
    }

    private DynamicRound<TestEntrant> createMultiEntrantRound() {
        DynamicRound<TestEntrant> round = new DynamicRound<>();
        for (TestEntrant entrant : entrants)
            round.add(entrant);
        return round;
    }

    private DynamicRound<TestEntrant> createSinglePairFinishedRound() {
        DynamicRound<TestEntrant> round = new DynamicRound<>();
        round.add(first);
        round.add(second);
        assertDoesNotThrow(round::nextPairing);
        assertDoesNotThrow(() -> round.declareWinner(first));
        return round;
    }

    private Pairing<TestEntrant> getFinishedPairing(DynamicRound<TestEntrant> round) {
        assertEquals(1, round.getFinishedPairings().size());
        return round.getFinishedPairings().iterator().next();
    }

    // add()

    @Test
    void emptyRound_addEntrant_returnsTrue() {
        assertTrue(emptyRound.add(first));
    }

    @Test
    void oneEntrantRound_addEntrantAgain_returnsFalse() {
        assertFalse(oneEntrantRound.add(first));
    }

    @Test
    void emptyRound_addEntrant_isPending() {
        emptyRound.add(first);
        assertTrue(emptyRound.isPending(first));
    }

    @Test
    void singlePairFinishedRound_removeAdvancedThenAddBack_isAdvanced() {
        singlePairFinishedRound.remove(winner);
        singlePairFinishedRound.add(winner);
        assertTrue(singlePairFinishedRound.isAdvanced(winner));
        assertFalse(singlePairFinishedRound.isPending(winner));
    }

    // nextPairing()

    @Test
    void emptyRound_nextPairing_throwsNoEntrantsException() {
        assertThrows(NoEntrantsException.class, () -> emptyRound.nextPairing());
    }

    @Test
    void oneEntrantRound_nextPairing_throwsNoOpponentException() {
        assertThrows(NoOpponentException.class, () -> oneEntrantRound.nextPairing());
    }

    @Test
    void twoEntrantRound_nextPairing_noPendingEntrants() throws Exception {
        twoEntrantRound.nextPairing();
        assertTrue(twoEntrantRound.getPendingEntrants().isEmpty());
    }

    @Test
    void twoEntrantRound_nextPairing_bothEntrantsArePaired() {
        Assertions.assertDoesNotThrow(twoEntrantRound::nextPairing);
        assertFalse(twoEntrantRound.isPending(first));
        assertFalse(twoEntrantRound.isPending(second));
        assertTrue(twoEntrantRound.isPaired(first));
        assertTrue(twoEntrantRound.isPaired(second));
    }

    @Test
    void twoEntrantRound_nextPairing_returnedPairingContainsBothEntrants() throws Exception {
        Pairing<TestEntrant> pairing = twoEntrantRound.nextPairing();
        assertTrue(pairing.contains(first));
        assertTrue(pairing.contains(second));
    }

    @Test
    void singlePairFinishedRound_resetPairedEntrantsAndPairRandomAgain_entrantsArePaired() {
        singlePairFinishedRound.reset(winner);
        singlePairFinishedRound.reset(loser);
        Assertions.assertDoesNotThrow(singlePairFinishedRound::nextPairing);
        assertTrue(singlePairFinishedRound.isPaired(winner));
        assertTrue(singlePairFinishedRound.isPaired(loser));
    }

    @Test
    void multiEntrantRound_finishParallelPairingsOutOfOrder_identicalToInOrder() throws Exception {
        Pairing<TestEntrant> p1 = multiEntrantRound.nextPairing();
        Pairing<TestEntrant> p2 = multiEntrantRound.nextPairing();
        multiEntrantRound.declareWinner(p2.getEntrant1());
        multiEntrantRound.declareWinner(p1.getEntrant2());
        assertTrue(multiEntrantRound.isAdvanced(p1.getEntrant2()));
        assertTrue(multiEntrantRound.isAdvanced(p2.getEntrant1()));
        assertTrue(multiEntrantRound.isEliminated(p1.getEntrant1()));
        assertTrue(multiEntrantRound.isEliminated(p2.getEntrant2()));
    }

    // declareWinner()

    @Test
    void emptyRound_declareWinner_throwsNoSuchEntrantException() {
        assertThrows(NoSuchEntrantException.class, () -> emptyRound.declareWinner(winner));
    }

    @Test
    void oneEntrantRound_declareWinner_throwsMissingPairingException() {
        assertThrows(MissingPairingException.class, () -> oneEntrantRound.declareWinner(winner));
    }

    @Test
    void singlePairRound_declareFirstWinner_winnerAdvancedAndLoserEliminated() throws Exception {
        singlePairRound.declareWinner(winner);
        assertFalse(singlePairRound.isPaired(winner));
        assertFalse(singlePairRound.isPaired(loser));
        assertTrue(singlePairRound.isAdvanced(winner));
        assertTrue(singlePairRound.isEliminated(loser));
    }

    @Test
    void singlePairRound_declareWinner_returnsFinishedPairing() throws Exception {
        Pairing<TestEntrant> expectedPairing = twoEntrantRound.nextPairing();
        Pairing<TestEntrant> finishedPairing = twoEntrantRound.declareWinner(winner);
        assertSame(expectedPairing, finishedPairing);
    }

    @Test
    void singlePairRound_declareWinner_pairingFinished() throws Exception {
        Pairing<TestEntrant> pairing = singlePairRound.declareWinner(winner);
        assertTrue(singlePairRound.getFinishedPairings().contains(pairing));
    }

    @Test
    @Disabled
    void multiPairRound_declareAllWinners_finishedPairingsAreOrderedChronologically() {
        assert false; // TODO
    }

    // replayPairing()

    @Test
    void singlePairRound_replayActivePairing_returnsFalse() throws Exception {
        Pairing<TestEntrant> pairing = twoEntrantRound.nextPairing();
        assertFalse(assertDoesNotThrow(() -> twoEntrantRound.replayPairing(pairing)));
    }

    @Test
    void twoEntrantRound_replayInvalidPairing_throwsNoSuchPairingException() {
        assertThrows(NoSuchPairingException.class, () ->
            twoEntrantRound.replayPairing(new Pairing<>(first, second)));
    }

    @Test
    void singlePairFinishedRound_replayPairing_bothEntrantsArePaired() {
        Pairing<TestEntrant> pairing = getFinishedPairing(singlePairFinishedRound);
        assertDoesNotThrow(() -> singlePairFinishedRound.replayPairing(pairing));
        assertTrue(singlePairFinishedRound.getActivePairings().contains(pairing));
        assertFalse(singlePairFinishedRound.hasResult(first));
        assertFalse(singlePairFinishedRound.hasResult(second));
    }

    @Test
    void singlePairFinishedRound_resetFirstEntrantAndReplayPairing_bothEntrantsArePaired() {
        Pairing<TestEntrant> pairing = getFinishedPairing(singlePairFinishedRound);
        singlePairFinishedRound.reset(first);
        assertDoesNotThrow(() -> singlePairFinishedRound.replayPairing(pairing));
        assertTrue(singlePairFinishedRound.getActivePairings().contains(pairing));
        assertFalse(singlePairFinishedRound.hasResult(first));
        assertFalse(singlePairFinishedRound.hasResult(second));
    }

    @Test
    void singlePairFinishedRound_resetFirstAndPairWithThirdThenReplayFirstPairing_throwsEntrantNotPendingException() {
        Pairing<TestEntrant> pairing = getFinishedPairing(singlePairFinishedRound);
        singlePairFinishedRound.add(third);
        singlePairFinishedRound.reset(first);
        assertDoesNotThrow(() -> singlePairFinishedRound.createPairing(first, third));
        assertThrows(EntrantNotPendingException.class, () -> singlePairFinishedRound.replayPairing(pairing));

        // Resetting that entrant and then replaying should work.
        singlePairFinishedRound.reset(first);
        assertDoesNotThrow(() -> singlePairFinishedRound.replayPairing(pairing));
    }

    @Test
    void singlePairFinishedRound_removeBothEntrantsAndReplayPairing_throwsMissingEntrantException() {
        Pairing<TestEntrant> pairing = getFinishedPairing(singlePairFinishedRound);
        singlePairFinishedRound.remove(first);
        singlePairFinishedRound.remove(second);
        assertThrows(MissingEntrantException.class, () -> singlePairFinishedRound.replayPairing(pairing));
    }

    @Test
    void singlePairFinishedRound_removeFirstEntrantAndReplayPairing_throwsMissingEntrantException() {
        Pairing<TestEntrant> pairing = getFinishedPairing(singlePairFinishedRound);
        singlePairFinishedRound.remove(first);
        assertThrows(MissingEntrantException.class, () -> singlePairFinishedRound.replayPairing(pairing));
    }

    // reset()

    @Test
    void singleEntrantRound_resetEntrant_isPending() {
        singlePairRound.reset(first);
        assertTrue(singlePairRound.isPending(first));
    }

    @Test
    void singleEntrantRound_resetInvalidEntrant_returnsFalse() {
        assertFalse(singlePairRound.reset(invalidEntrant));
        assertFalse(singlePairRound.isPending(invalidEntrant));
    }

    @Test
    void singlePairRound_resetFirst_isPending() {
        singlePairRound.reset(first);
        assertTrue(singlePairRound.isPending(first));
        assertFalse(singlePairRound.isPaired(first));
    }

    @Test
    void singlePairRound_resetFirst_secondIsPending() {
        singlePairRound.reset(second);
        assertTrue(singlePairRound.isPending(second));
        assertFalse(singlePairRound.isPaired(second));
    }

    @Test
    void singlePairFinishedRound_resetAdvanced_isPending() {
        singlePairFinishedRound.reset(winner);
        assertFalse(singlePairFinishedRound.isAdvanced(winner));
        assertTrue(singlePairFinishedRound.isPending(winner));
    }

    @Test
    void singlePairFinishedRound_resetAdvanced_eliminatedIsStillEliminated() {
        singlePairFinishedRound.reset(winner);
        assertTrue(singlePairFinishedRound.isEliminated(loser));
    }

    @Test
    void singlePairFinishedRound_resetFloatingAdvanced_isCompletelyRemoved() {
        singlePairFinishedRound.remove(winner);
        singlePairFinishedRound.reset(winner);
        assertFalse(singlePairFinishedRound.contains(winner));
        assertFalse(singlePairFinishedRound.isAdvanced(winner));
    }

    @Test
    void singlePairFinishedRound_resetAdvanced_keepsFinishedPairing() {
        singlePairFinishedRound.reset(winner);
        assertEquals(1, singlePairFinishedRound.getFinishedPairings().size());
    }

    @Test
    void singlePairFinishedRound_resetAdvancedAndEliminated_removesFinishedPairing() {
        singlePairFinishedRound.reset(winner);
        singlePairFinishedRound.reset(loser);
        assertTrue(singlePairFinishedRound.getFinishedPairings().isEmpty());
    }

    @Test
    void singlePairFinishedRound_removeWinnerAndResetLoser_finishedPairingStillExists() {
        singlePairFinishedRound.remove(winner);
        singlePairFinishedRound.reset(loser);
        assertEquals(1, singlePairFinishedRound.getFinishedPairings().size());
    }

    @Test
    void threeEntrantRound_resetLoserThenPairAgainstOther_hasTwoFinishedPairings() throws Exception {
        twoEntrantRound.add(third);
        DynamicRound<TestEntrant> threeEntrantRound = twoEntrantRound;

        Pairing<TestEntrant> firstPairing = threeEntrantRound.nextPairing();
        TestEntrant firstWinner = firstPairing.getEntrant1();
        TestEntrant firstLoser = firstPairing.getEntrant2();

        threeEntrantRound.declareWinner(firstWinner);
        threeEntrantRound.reset(firstLoser);

        Pairing<TestEntrant> secondPairing = threeEntrantRound.nextPairing();
        TestEntrant secondLoser = secondPairing.getOther(firstLoser);

        threeEntrantRound.declareWinner(firstLoser);

        assertTrue(threeEntrantRound.isAdvanced(firstLoser));
        assertTrue(threeEntrantRound.isAdvanced(firstWinner));
        assertTrue(threeEntrantRound.isEliminated(secondLoser));
        assertEquals(2, threeEntrantRound.getFinishedPairings().size());

        // After resetting this player which participated in both pairings,
        // those pairings should still exist.
        threeEntrantRound.reset(firstLoser);
        assertEquals(2, threeEntrantRound.getFinishedPairings().size());
    }

    // remove()

    @Test
    void singlePairRound_removeFirst_isRemoved() {
        singlePairRound.remove(first);
        assertFalse(singlePairRound.contains(first));
        assertFalse(singlePairRound.isPending(first));
        assertFalse(singlePairRound.isPaired(first));
    }

    @Test
    void singlePairFinishedRound_removeWinnerAgain_returnsFalse() {
        singlePairFinishedRound.remove(winner);
        assertFalse(singlePairFinishedRound.remove(winner));
    }

    @Test
    void singlePairRound_removeFirst_secondIsPending() {
        singlePairRound.remove(first);
        assertTrue(singlePairRound.isPending(second));
        assertFalse(singlePairRound.isPaired(second));
    }

    @Test
    void singlePairFinishedRound_removeAdvanced_isRemoved() {
        assertTrue(singlePairFinishedRound.remove(winner));
        assertFalse(singlePairFinishedRound.contains(winner));
    }

    @Test
    void singlePairFinishedRound_removeAndResetAllEntrants_completelyEmptyRound() {
        singlePairFinishedRound.remove(first);
        singlePairFinishedRound.remove(second);
        singlePairFinishedRound.reset(first);
        singlePairFinishedRound.reset(second);
        assertFalse(singlePairFinishedRound.hasStateAbout(first));
        assertFalse(singlePairFinishedRound.hasStateAbout(second));
        assertTrue(singlePairFinishedRound.getPendingEntrants().isEmpty());
    }

    // isFinished()

    @Test
    void singlePairFinishedRound_isFinished_returnsTrue() {
        assertTrue(singlePairFinishedRound.isFinished());
    }

    @Test
    void singlePairRound_isFinished_returnsFalse() {
        // The round is not finished since there is an active pairing.
        assertFalse(singlePairRound.isFinished());
        assertTrue(singlePairRound.hasActivePairings());
    }

    @Test
    void singlePairFinishedRound_removeAdvanced_isStillFinished() {
        singlePairFinishedRound.remove(winner);
        assertTrue(singlePairFinishedRound.isFinished());
    }

    // Identities

    @Test
    void singlePairFinishedRound_resetThenRemoveAdvanced_identicalToRemoveThenResetAdvanced() {
        DynamicRound<TestEntrant> otherRound = createSinglePairFinishedRound();

        singlePairFinishedRound.reset(winner);
        singlePairFinishedRound.remove(winner);
        otherRound.remove(winner);
        otherRound.reset(winner);

        assertEquals(singlePairFinishedRound.contains(winner), otherRound.contains(winner));
        assertEquals(singlePairFinishedRound.hasStateAbout(winner), otherRound.hasStateAbout(winner));
    }

    // Miscellaneous

    @Test
    void testEntrants_uniqueIds() {
        ArrayList<TestEntrant> allEntrants = new ArrayList<>(entrants);
        allEntrants.add(invalidEntrant);
        assertEquals(allEntrants.size(), new HashSet<>(allEntrants).size());
    }

    @Test
    void multiEntrantRound_nextPairing_isRandom() {
        assertSuppliesAll(entrants, () ->
            assertDoesNotThrow(() -> {
                DynamicRound<TestEntrant> round = createMultiEntrantRound();
                Pairing<TestEntrant> pairing = round.nextPairing();
                return pairing.getEntrant1();
            })
        );
    }
}
