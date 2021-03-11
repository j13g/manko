package de.j13g.manko.core.rounds;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.ResultsManager;
import de.j13g.manko.core.exceptions.*;
import de.j13g.manko.core.base.EliminationRound;
import de.j13g.manko.util.ShuffledSet;
import de.j13g.manko.util.UniformPairLinkedBiSet;
import de.j13g.manko.util.UniformPairUniqueBiSet;
import de.j13g.manko.util.exceptions.EmptySetException;
import de.j13g.manko.util.exceptions.NoSuchElementException;

import java.io.Serializable;
import java.util.*;

public class DynamicElimination<E extends Serializable> implements EliminationRound<E>, Serializable {

    private final HashSet<E> entrants = new HashSet<>();
    private final ShuffledSet<E> pendingEntrants = new ShuffledSet<>();

    private final ResultsManager<E> results = new ResultsManager<>();
    private final ResultsManager<E> floatingResults = new ResultsManager<>();

    private final UniformPairUniqueBiSet<E, Pairing<E>> activePairings = new UniformPairUniqueBiSet<>();
    private final UniformPairLinkedBiSet<E, Pairing<E>> finishedPairings = new UniformPairLinkedBiSet<>();

    public DynamicElimination() {}

    public DynamicElimination(Collection<E> entrants) {
        entrants.forEach(this::addEntrant);
    }

    @Override
    public boolean addEntrant(E entrant) {
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

    @Override
    public Pairing<E> nextPairing() throws NoEntrantsException, NoOpponentException {
        if (pendingEntrants.size() == 0) throw new NoEntrantsException();
        if (pendingEntrants.size() == 1) throw new NoOpponentException();

        try {
            E entrant1 = pendingEntrants.removeRandom();
            E entrant2 = pendingEntrants.removeRandom();
            return registerPairing(entrant1, entrant2);
        }
        catch (EmptySetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Pairing<E> declareWinner(E winningEntrant) throws NoSuchEntrantException, MissingPairingException {
        if (!contains(winningEntrant))
            throw new NoSuchEntrantException();

        Pairing<E> pairing = activePairings.findByElement(winningEntrant);
        if (pairing == null)
            throw new MissingPairingException();

        try {
            declareWinner(winningEntrant, pairing);
        } catch (NoSuchPairingException e) {
            throw new RuntimeException(e);
        }

        return pairing;
    }

    @Override
    public void declareWinner(E winningEntrant, Pairing<E> pairing)
            throws NoSuchEntrantException, NoSuchPairingException {

        if (!pairing.contains(winningEntrant))
            throw new IllegalArgumentException("The entrant is not part of the pairing");

        if (!contains(winningEntrant))
            throw new NoSuchEntrantException();

        if (!activePairings.contains(pairing))
            throw new NoSuchPairingException();

        results.advance(winningEntrant);
        results.eliminate(getOtherUnsafe(pairing, winningEntrant));

        finishPairing(pairing);
    }

    @Override
    public void declareTie(Pairing<E> pairing)
            throws NoSuchPairingException {

        if (!activePairings.contains(pairing))
            throw new NoSuchPairingException();

        finishPairing(pairing);
    }

    @Override
    public boolean replayPairing(Pairing<E> pairing)
            throws NoSuchPairingException, MissingEntrantException, OrphanedPairingException {

        if (activePairings.contains(pairing))
            return false;
        if (!finishedPairings.contains(pairing))
            throw new NoSuchPairingException();

        E first = pairing.getFirst();
        E second = pairing.getSecond();

        // One of the entrants could be removed,
        // since finished pairings are only removed if both entrants are gone.
        if (!contains(first) || !contains(second))
            throw new MissingEntrantException();

        if (isPairingOrphaned(pairing))
            throw new OrphanedPairingException();

        results.reset(first);
        results.reset(second);
        finishedPairings.remove(pairing);

        pendingEntrants.remove(first);
        pendingEntrants.remove(second);
        registerPairing(first, second);

        return true;
    }

    @Override
    public boolean resetEntrant(E entrant) {
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

        Set<Pairing<E>> entrantPairingSet = finishedPairings.findByElement(entrant);

        // Create a copy because removing elements from finishedPairings
        // in the loop below will modify the original set.
        List<Pairing<E>> entrantPairings = new ArrayList<>(entrantPairingSet);

        // Remove all pairings that this entrant is part of
        // and where the other entrant does not have any results
        // i.e. the other entrant was reset before too.
        for (Pairing<E> pairing : entrantPairings) {
            E other = getOtherUnsafe(pairing, entrant);
            if (!hasResult(other) && !floatingResults.contains(other))
                finishedPairings.remove(pairing);
        }

        return true;
    }

    @Override
    public boolean removeEntrant(E entrant) {
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

    @Override
    public boolean isPairingOrphaned(Pairing<E> pairing)
            throws NoSuchPairingException {

        if (activePairings.contains(pairing))
            return false;
        if (!finishedPairings.contains(pairing))
            throw new NoSuchPairingException();

        E first = pairing.getFirst();
        E second = pairing.getSecond();

        // They're not in the same pairing,
        // but one of them is in another pairing.
        if (isPaired(first) || isPaired(second))
            return true;

        int nFinishedFirst = finishedPairings.findByElement(first).size();
        int nFinishedSecond = finishedPairings.findByElement(second).size();

        return nFinishedFirst > 1 && !isPending(first)
            || nFinishedSecond > 1 && !isPending(second);
    }

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

    @Override
    public boolean hasResult(E entrant) {
        return results.contains(entrant);
    }

    public boolean isPending(E entrant) {
        return pendingEntrants.contains(entrant);
    }

    public boolean isPaired(E entrant) {
        return activePairings.findByElement(entrant) != null;
    }

    @Override
    public boolean isAdvanced(E entrant) {
        return results.isAdvanced(entrant);
    }

    @Override
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

    @Override
    public Set<E> getAdvancedEntrants() {
        return results.getAdvanced();
    }

    @Override
    public Set<E> getEliminatedEntrants() {
        return results.getEliminated();
    }

    /**
     * Creates a new pairing with two participants.
     * Does not check if the participants are part of the round or are pending.
     * @param first The first entrant.
     * @param second The second entrant.
     * @return The created pairing containing both entrants.
     */
    private Pairing<E> registerPairing(E first, E second) {

        Pairing<E> pairing = new Pairing<>(first, second);

        assert !activePairings.contains(pairing);
        assert !finishedPairings.contains(pairing);
        assert !isPending(first) && !isPending(second);
        assert !hasResult(first) && !hasResult(second);

        activePairings.add(pairing);
        return pairing;
    }

    private void finishPairing(Pairing<E> pairing) {
        assert activePairings.contains(pairing);

        finishedPairings.add(pairing);
        activePairings.remove(pairing);

        // Check that entrants don't end up where they shouldn't.
        assert entrants.size() == 2 * activePairings.size() +
                finishedPairings.getPairElementSet().size() + pendingEntrants.size();
        assert entrants.size() == 2 * activePairings.size() +
                results.getAdvanced().size() + results.getEliminated().size() + pendingEntrants.size();
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
