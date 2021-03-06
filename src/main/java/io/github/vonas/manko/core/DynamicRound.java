package io.github.vonas.manko.core;

import io.github.vonas.manko.core.exceptions.MissingPairingException;
import io.github.vonas.manko.core.exceptions.NoEntrantsException;
import io.github.vonas.manko.core.exceptions.NoOpponentException;
import io.github.vonas.manko.core.exceptions.NoSuchEntrantException;
import io.github.vonas.manko.util.ShuffledSet;
import io.github.vonas.manko.util.UniformPairUniqueBiSet;
import io.github.vonas.manko.util.UniformPairUniqueLinkedBiSet;
import io.github.vonas.manko.util.exceptions.EmptySetException;
import io.github.vonas.manko.util.exceptions.NoSuchElementException;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

public class DynamicRound<E extends Serializable> implements Serializable {

    private final HashSet<E> entrants = new HashSet<>();
    private final ShuffledSet<E> pendingEntrants = new ShuffledSet<>();

    private final Results<E> results = new Results<>();
    private final Results<E> floatingResults = new Results<>();

    private final UniformPairUniqueBiSet<E, Pairing<E>> activePairings = new UniformPairUniqueBiSet<>();
    private final UniformPairUniqueLinkedBiSet<E, Pairing<E>> finishedPairings = new UniformPairUniqueLinkedBiSet<>();

    public DynamicRound() {}

    public DynamicRound(Set<E> entrants) {
        entrants.forEach(this::add);
    }

    /**
     * Add an entrant to this round.
     * @param entrant The entrant.
     * @return If the entrant was not already in this round.
     */
    public boolean add(E entrant) {
        if (entrants.contains(entrant))
            return false;

        if (floatingResults.contains(entrant)) {
            floatingResults.moveTo(results, entrant);
            return true;
        }

        entrants.add(entrant);
        pendingEntrants.add(entrant);
        return true;
    }

    // TODO
//    public Pairing<E> pair(E entrant1, E entrant2) {
//        if (!contains(entrant1) || !contains(entrant2))
//            throw new NoSuchEntrantException();
//
//        if (!isPending(entrant1) || !isPending(entrant2))
//            throw new EntrantNotPendingException();
//    }

    /**
     * Creates a pairing between two randomly chosen entrants.
     * @return The created pairing.
     * @throws NoEntrantsException The round does not have any entrants.
     * @throws NoOpponentException The only entrant does not have an opponent.
     */
    public Pairing<E> pairRandom() throws NoEntrantsException, NoOpponentException {
        if (pendingEntrants.size() == 0) throw new NoEntrantsException();
        if (pendingEntrants.size() == 1) throw new NoOpponentException();

        Pairing<E> pairing;
        try {
            E entrant1 = pendingEntrants.removeRandom();
            E entrant2 = pendingEntrants.removeRandom();
            pairing = new Pairing<>(entrant1, entrant2);
        }
        catch (EmptySetException e) {
            throw new RuntimeException(e);
        }

        // A created pairing may not have been played before.
        assert !finishedPairings.contains(pairing);

        activePairings.add(pairing);
        return pairing;
    }

    /**
     * Cancels a pairing, effectively resetting both entrants.
     * @param pairing The pairing.
     * @return If the pairing existed.
     */
    public boolean cancelPairing(Pairing<E> pairing) {
        return reset(pairing.getEntrant1());
    }

    /**
     * Cancels the pairing that the entrant is part of.
     * @param entrant The entrant.
     * @return If that pairing existed.
     */
    public boolean cancelPairingByEntrant(E entrant) {
        return reset(entrant);
    }

    /**
     * Declares the winner of an active pairing.
     * @param entrant The winning entrant.
     * @return The pairing this entrant was part of.
     * @throws NoSuchEntrantException This entrant is not part of this round.
     * @throws MissingPairingException This entrant is not part of any active pairing.
     */
    public Pairing<E> declareWinner(E entrant) throws NoSuchEntrantException, MissingPairingException {
        if (!contains(entrant))
            throw new NoSuchEntrantException();

        Pairing<E> pairing = activePairings.findByElement(entrant);
        if (pairing == null)
            throw new MissingPairingException();

        results.advance(entrant);
        results.eliminate(getOtherUnsafe(pairing, entrant));

        finishedPairings.add(pairing);
        activePairings.remove(pairing);

        // Check that entrants don't end up where they shouldn't.
        assert entrants.size() == 2 * activePairings.size() + 2 * finishedPairings.size() + pendingEntrants.size();
        assert entrants.size() == 2 * activePairings.size() +
            results.getAdvanced().size() + results.getEliminated().size() + pendingEntrants.size();

        return pairing;
    }

    /**
     * Resets an entrant back to the pending state.
     * @param entrant The entrant.
     * @return If the entrant was not already in pending state.
     */
    public boolean reset(E entrant) {
        if (!hasStateAbout(entrant) || isPending(entrant))
            return false;

        if (isPaired(entrant)) {
            Pairing<E> pairing = activePairings.removeByElement(entrant);
            resetOtherUnsafe(pairing, entrant);
            pendingEntrants.add(entrant);
        }
        else if (isAdvanced(entrant) || isEliminated(entrant)) {
            results.reset(entrant);
            pendingEntrants.add(entrant);
        }
        else if (floatingResults.isAdvanced(entrant) || floatingResults.isEliminated(entrant)) {
            assert !entrants.contains(entrant);
            floatingResults.reset(entrant);
        }

        Pairing<E> finishedPairing = finishedPairings.findByElement(entrant);
        // Only true if the entrant has a result (active or floating) associated with it.
        if (finishedPairing != null) {
            E other = getOtherUnsafe(finishedPairing, entrant);
            if (!hasResult(other) && !floatingResults.contains(other))
                finishedPairings.remove(finishedPairing);
        }

        return true;
    }

    public boolean remove(E entrant) {
        if (isPending(entrant)) {
            pendingEntrants.remove(entrant);
        }
        else if (isPaired(entrant)) {
            Pairing<E> pairing = activePairings.findByElement(entrant);
            resetOtherUnsafe(pairing, entrant);
            activePairings.remove(pairing);
        }
        else if (isAdvanced(entrant) || isEliminated(entrant)) {
            boolean wasMoved = results.moveTo(floatingResults, entrant);
            // NOTE: Separate variable required so that
            // the assert does not have side effects.
            assert wasMoved;
        }
        else if (floatingResults.contains(entrant)) {
            return false; // Already removed.
        }

        return entrants.remove(entrant);
    }

    /**
     * Checks if this round contains the entrant.
     * The entrant must be an active participant.
     * @param entrant The entrant.
     * @return If the entrant participates in this round.
     */
    public boolean contains(E entrant) {
        return entrants.contains(entrant);
    }

    /**
     * Checks if the entrant participates in this round
     * or has won or lost a pairing before and was not reset since then.
     * @param entrant The entrant.
     * @return If any state is associated to this entrant.
     */
    public boolean hasStateAbout(E entrant) {
        return contains(entrant) || floatingResults.contains(entrant);
    }

    /**
     * Checks if this entrant has a result of a finished pairing associated to it.
     * @param entrant The entrant.
     * @return If the entrant appears in a finished pairing.
     */
    public boolean hasResult(E entrant) {
        return isAdvanced(entrant) || isEliminated(entrant);
    }

    public boolean isPending(E entrant) {
        return pendingEntrants.contains(entrant);
    }

    public boolean isPaired(E entrant) {
        return activePairings.findByElement(entrant) != null;
    }

    public boolean isAdvanced(E entrant) {
        return results.isAdvanced(entrant);
    }

    public boolean isEliminated(E entrant) {
        return results.isEliminated(entrant);
    }

    public boolean hasActivePairings() {
        return !getActivePairings().isEmpty();
    }

    public boolean isFinished() {
        // Assert either not finished or proper entrant distribution.
        assert !(pendingEntrants.isEmpty() && activePairings.isEmpty())
            || entrants.size() == results.getAdvanced().size() + results.getEliminated().size();

        return pendingEntrants.isEmpty() && activePairings.isEmpty();
    }

    public Set<E> getPendingEntrants() {
        return pendingEntrants.elements();
    }

    public Set<Pairing<E>> getActivePairings() {
        return activePairings.elements();
    }

    public Set<Pairing<E>> getFinishedPairings() {
        return finishedPairings.elements();
    }

    public Set<E> getAdvancedEntrants() {
        return results.getAdvanced();
    }

    public Set<E> getEliminatedEntrants() {
        return results.getEliminated();
    }

    private E getOtherUnsafe(Pairing<E> pairing, E entrant) {
        try {
            return pairing.getOther(entrant);
        } catch (NoSuchElementException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetOtherUnsafe(Pairing<E> pairing, E entrant) {
        E other = getOtherUnsafe(pairing, entrant);
        pendingEntrants.add(other);
    }
}
