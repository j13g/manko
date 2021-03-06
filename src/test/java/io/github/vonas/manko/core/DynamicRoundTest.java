package io.github.vonas.manko.core;

import io.github.vonas.manko.core.exceptions.MissingPairingException;
import io.github.vonas.manko.core.exceptions.NoEntrantsException;
import io.github.vonas.manko.core.exceptions.NoOpponentException;
import io.github.vonas.manko.core.exceptions.NoSuchEntrantException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static io.github.vonas.manko.Helper.assertSuppliesAll;
import static org.junit.jupiter.api.Assertions.*;

public class DynamicRoundTest {

    private final Random random = new Random(0);

    private final TestEntrant first = createEntrant();
    private final TestEntrant second = createEntrant();
    private final TestEntrant invalidEntrant = createEntrant();

    private final Set<TestEntrant> entrants = new HashSet<>(Arrays.asList(
        first, second,
        createEntrant(), createEntrant(), createEntrant(), createEntrant(),
        createEntrant(), createEntrant(), createEntrant(), createEntrant()
    ));

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
        assertDoesNotThrow(() -> singlePairRound.pairRandom());

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
        assertDoesNotThrow(round::pairRandom);
        round.declareWinner(first);
        return round;
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

    // pairRandom()

    @Test
    void emptyRound_pairRandom_throwsNoEntrantsException() {
        assertThrows(NoEntrantsException.class, () -> emptyRound.pairRandom());
    }

    @Test
    void oneEntrantRound_pairRandom_throwsNoOpponentException() {
        assertThrows(NoOpponentException.class, () -> oneEntrantRound.pairRandom());
    }

    @Test
    void twoEntrantRound_pairRandom_noPendingEntrants() {
        twoEntrantRound.pairRandom();
        assertTrue(twoEntrantRound.getPendingEntrants().isEmpty());
    }

    @Test
    void twoEntrantRound_pairRandom_bothEntrantsArePaired() {
        assertDoesNotThrow(twoEntrantRound::pairRandom);
        assertFalse(twoEntrantRound.isPending(first));
        assertFalse(twoEntrantRound.isPending(second));
        assertTrue(twoEntrantRound.isPaired(first));
        assertTrue(twoEntrantRound.isPaired(second));
    }

    @Test
    void twoEntrantRound_pairRandom_returnedPairingContainsBothEntrants() {
        Pairing<TestEntrant> pairing = twoEntrantRound.pairRandom();
        assertTrue(pairing.contains(first));
        assertTrue(pairing.contains(second));
    }

    @Test
    void singlePairFinishedRound_resetPairedEntrantsAndPairRandomAgain_entrantsArePaired() {
        singlePairFinishedRound.reset(winner);
        singlePairFinishedRound.reset(loser);
        assertDoesNotThrow(singlePairFinishedRound::pairRandom);
        assertTrue(singlePairFinishedRound.isPaired(winner));
        assertTrue(singlePairFinishedRound.isPaired(loser));
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
    void singlePairRound_declareFirstWinner_winnerAdvancedAndLoserEliminated() {
        singlePairRound.declareWinner(winner);
        assertFalse(singlePairRound.isPaired(winner));
        assertFalse(singlePairRound.isPaired(loser));
        assertTrue(singlePairRound.isAdvanced(winner));
        assertTrue(singlePairRound.isEliminated(loser));
    }

    @Test
    void singlePairRound_declareWinner_returnsFinishedPairing() {
        Pairing<TestEntrant> expectedPairing = twoEntrantRound.pairRandom();
        Pairing<TestEntrant> finishedPairing = twoEntrantRound.declareWinner(winner);
        assertSame(expectedPairing, finishedPairing);
    }

    @Test
    void singlePairRound_declareWinner_pairingFinished() {
        Pairing<TestEntrant> pairing = singlePairRound.declareWinner(winner);
        assertTrue(singlePairRound.getFinishedPairings().contains(pairing));
    }

    @Test
    void multiPairRound_declareAllWinners_finishedPairingsAreOrderedChronologically() {
        assert true; // TODO
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

    // remove()

    @Test
    void singlePairRound_removeFirst_isRemoved() {
        singlePairRound.remove(first);
        assertFalse(singlePairRound.contains(first));
        assertFalse(singlePairRound.isPending(first));
        assertFalse(singlePairRound.isPaired(first));
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
        List<Integer> ids = Arrays.asList(first.id(), second.id(), invalidEntrant.id());
        assertEquals(ids.size(), new HashSet<>(ids).size());
    }

    @Test
    void multiEntrantRound_pairRandom_isRandom() {
        assertSuppliesAll(entrants, () -> {
            DynamicRound<TestEntrant> round = createMultiEntrantRound();
            Pairing<TestEntrant> pairing = round.pairRandom();
            return pairing.getEntrant1();
        });
    }
}
