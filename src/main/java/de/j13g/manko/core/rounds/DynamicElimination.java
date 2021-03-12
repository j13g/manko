package de.j13g.manko.core.rounds;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.managers.PairingManager;
import de.j13g.manko.core.managers.ResultManager;
import de.j13g.manko.core.exceptions.*;
import de.j13g.manko.core.base.EliminationRound;
import de.j13g.manko.util.ShuffledSet;
import de.j13g.manko.util.exceptions.EmptySetException;
import de.j13g.manko.util.exceptions.NoSuchElementException;

import java.io.Serializable;
import java.util.*;

public class DynamicElimination<E extends Serializable> implements EliminationRound<E>, Serializable {

    private final HashSet<E> entrants = new HashSet<>();
    private final ShuffledSet<E> pendingEntrants = new ShuffledSet<>();

    private final ResultManager<E> results = new ResultManager<>();
    private final ResultManager<E> floatingResults = new ResultManager<>();

    private final PairingManager<E> pairings = new PairingManager<>();

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
        if (!hasEntrant(winningEntrant))
            throw new NoSuchEntrantException();

        Pairing<E> pairing = pairings.findActiveByEntrant(winningEntrant);
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

        if (!hasEntrant(winningEntrant))
            throw new NoSuchEntrantException();
        if (!pairings.isActive(pairing))
            throw new NoSuchPairingException();

        results.advance(winningEntrant);
        results.eliminate(getOtherUnsafe(pairing, winningEntrant));

        finishPairing(pairing);
    }

    @Override
    public void declareTie(Pairing<E> pairing)
            throws NoSuchPairingException {

        if (!pairings.isActive(pairing))
            throw new NoSuchPairingException();

        results.eliminate(pairing.getFirst());
        results.eliminate(pairing.getSecond());

        finishPairing(pairing);
    }

    @Override
    public boolean replayPairing(Pairing<E> pairing)
            throws NoSuchPairingException, MissingEntrantException, OrphanedPairingException {

        if (pairings.isActive(pairing))
            return false;
        if (!pairings.isFinished(pairing))
            throw new NoSuchPairingException();

        E first = pairing.getFirst();
        E second = pairing.getSecond();

        // One of the entrants could be removed,
        // since finished pairings are only removed if both entrants are gone.
        if (!hasEntrant(first) || !hasEntrant(second))
            throw new MissingEntrantException();

        if (isPairingOrphaned(pairing))
            throw new OrphanedPairingException();

        results.reset(first);
        results.reset(second);
        pairings.remove(pairing);

        pendingEntrants.remove(first);
        pendingEntrants.remove(second);
        registerPairing(first, second);

        return true;
    }

    @Override
    public boolean resetEntrant(E entrant) {
        if (!hasStateAbout(entrant) || isEntrantPending(entrant))
            return false;

        if (isEntrantPaired(entrant)) {
            Pairing<E> pairing = pairings.removeActiveByEntrant(entrant);
            resetOtherUnsafe(pairing, entrant);
            pendingEntrants.add(entrant);
        }
        else if (isEntrantAdvanced(entrant) || isEntrantEliminated(entrant)) {
            results.reset(entrant);
            pendingEntrants.add(entrant);
        }
        else if (floatingResults.isAdvanced(entrant) || floatingResults.isEliminated(entrant)) {
            assert !entrants.contains(entrant);
            floatingResults.reset(entrant);
        }

        Set<Pairing<E>> entrantPairingSet = pairings.findFinishedByEntrant(entrant);

        // Create a copy because removing elements from finishedPairings
        // in the loop below will modify the original set.
        List<Pairing<E>> entrantPairings = new ArrayList<>(entrantPairingSet);

        // Remove all pairings that this entrant is part of
        // and where the other entrant does not have any results
        // i.e. the other entrant was reset before too.
        for (Pairing<E> pairing : entrantPairings) {
            E other = getOtherUnsafe(pairing, entrant);
            if (!hasEntrantResult(other) && !floatingResults.contains(other))
                pairings.removeFinished(pairing);
        }

        return true;
    }

    @Override
    public boolean removeEntrant(E entrant) {
        if (isEntrantPending(entrant)) {
            pendingEntrants.remove(entrant);
        }
        else if (isEntrantPaired(entrant)) {
            Pairing<E> pairing = pairings.findActiveByEntrant(entrant);
            resetOtherUnsafe(pairing, entrant);
            pairings.removeActive(pairing);
        }
        else if (isEntrantAdvanced(entrant) || isEntrantEliminated(entrant)) {
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
    public Set<E> getEntrants() {
        return Collections.unmodifiableSet(entrants);
    }

    @Override
    public Set<Pairing<E>> getActivePairings() {
        return pairings.getActive();
    }

    @Override
    public Set<Pairing<E>> getFinishedPairings() {
        return pairings.getFinished();
    }

    @Override
    public Set<E> getPendingEntrants() {
        return pendingEntrants.elements();
    }

    @Override
    public Set<E> getAdvancedEntrants() {
        return results.getAdvanced();
    }

    @Override
    public Set<E> getEliminatedEntrants() {
        return results.getEliminated();
    }

    @Override
    public boolean hasEntrant(E entrant) {
        return entrants.contains(entrant);
    }

    @Override
    public boolean hasEntrantResult(E entrant) {
        return results.contains(entrant);
    }

    @Override
    public boolean isEntrantPending(E entrant) {
        return pendingEntrants.contains(entrant);
    }

    @Override
    public boolean isEntrantPaired(E entrant) {
        return pairings.hasActiveEntrant(entrant);
    }

    @Override
    public boolean isFinished() {
        // Assert either not finished or proper entrant distribution.
        assert !(pendingEntrants.isEmpty() && !pairings.hasActive())
                || entrants.size() == results.getAdvanced().size() + results.getEliminated().size();

        return pendingEntrants.isEmpty() && !pairings.hasActive();
    }

    @Override
    public boolean isEntrantAdvanced(E entrant) {
        return results.isAdvanced(entrant);
    }

    @Override
    public boolean isEntrantEliminated(E entrant) {
        return results.isEliminated(entrant);
    }

    @Override
    public boolean isPairingOrphaned(Pairing<E> pairing)
            throws NoSuchPairingException {

        if (pairings.isActive(pairing))
            return false;
        if (!pairings.isFinished(pairing))
            throw new NoSuchPairingException();

        E first = pairing.getFirst();
        E second = pairing.getSecond();

        // They're not in the same pairing,
        // but one of them is in another pairing.
        if (isEntrantPaired(first) || isEntrantPaired(second))
            return true;

        int nFinishedFirst = pairings.findFinishedByEntrant(first).size();
        int nFinishedSecond = pairings.findFinishedByEntrant(second).size();

        return nFinishedFirst > 1 && !isEntrantPending(first)
                || nFinishedSecond > 1 && !isEntrantPending(second);
    }

    /**
     * Checks if the entrant participates in this round
     * or has won or lost a pairing before and was not reset since then.
     * @param entrant The entrant.
     * @return If any state is associated to this entrant.
     */
    public boolean hasStateAbout(E entrant) {
        return hasEntrant(entrant) || floatingResults.contains(entrant);
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

        assert !pairings.contains(pairing);
        assert !isEntrantPending(first) && !isEntrantPending(second);
        assert !hasEntrantResult(first) && !hasEntrantResult(second);

        pairings.add(pairing);
        return pairing;
    }

    private void finishPairing(Pairing<E> pairing) {
        assert pairings.isActive(pairing);

        try {
            pairings.finish(pairing);
        } catch (NoSuchPairingException e) {
            throw new RuntimeException(e);
        }

        // Check that entrants don't end up where they shouldn't.
        assert entrants.size() == pairings.getActiveEntrants().size() +
                pairings.getFinishedEntrants().size() + pendingEntrants.size();
        assert entrants.size() == pairings.getActiveEntrants().size() +
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
