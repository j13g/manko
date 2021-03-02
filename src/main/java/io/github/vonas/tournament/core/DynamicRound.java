package io.github.vonas.tournament.core;

import io.github.vonas.tournament.exceptions.*;

import java.util.*;

public class DynamicRound extends Round {

    /**
     * Holds the entrants that have not yet appeared in a pairing.
     * The order is arbitrary and is shuffled whenever a new pairing is generated.
     * Changes to the order will likely be overwritten.
     */
    private final ArrayList<Entrant> pendingEntrants = new ArrayList<>();

    private final HashSet<Entrant> advancedEntrants = new HashSet<>();
    private final HashSet<Entrant> eliminatedEntrants = new HashSet<>();

    /**
     * Creates a new round for a tournament. No entrants are known in advance.
     * This is the default for the first round of a dynamic tournament.
     */
    public DynamicRound() {}

    /**
     * Creates a new round for a tournament with initial entrants.
     * @param entrants The initial entrants.
     */
    public DynamicRound(List<Entrant> entrants) {
        for (Entrant entrant : entrants)
            this.addEntrant(entrant);
    }

    /**
     * Add a new entrant to this round.
     * @param entrant The entrant.
     */
    public void addEntrant(Entrant entrant) {
        entrants.add(entrant);
        pendingEntrants.add(entrant);
    }

    /**
     * Revives an eliminated entrant.
     * @param entrant The entrant.
     * @throws NoSuchEntrantException The entrant is not part of this round.
     * @throws NotEliminatedException The entrant was not eliminated by previous pairings.
     */
    public void reviveEntrant(Entrant entrant) throws NoSuchEntrantException, NotEliminatedException {
        if (!entrants.contains(entrant))
            throw new NoSuchEntrantException();

        if (!eliminatedEntrants.contains(entrant))
            throw new NotEliminatedException();

        eliminatedEntrants.remove(entrant);
        pendingEntrants.add(entrant);
    }

    @Override
    public void removeEntrant(Entrant entrant) {
        if (!entrants.contains(entrant))
            return;

        // Only remove the finished pairing(s) of this entrant
        // if they have advanced to the next round.
        // We only want to keep history of someone who advanced.
        if (advancedEntrants.contains(entrant))
            finishedPairings.removeIf(pairing -> pairing.hasEntrant(entrant));

        entrants.remove(entrant);
        advancedEntrants.remove(entrant);
        eliminatedEntrants.remove(entrant);

        // The linear time penalty and shifting (copying)
        // elements to their new positions should be fine,
        // as entrants will not be deleted that frequently.
        pendingEntrants.remove(entrant);

        if (!hasActivePairing()) return;
        if (!activePairing.hasEntrant(entrant)) return;

        Entrant other;
        try {
            other = activePairing.otherEntrant(entrant);
        }
        catch (NoSuchEntrantException e) {
            throw new RuntimeException(e);
        }

        // Add the other entrant of which the opponent
        // was removed to the list of pending entrants.
        pendingEntrants.add(other);
    }

    @Override
    public Pairing nextPairing() throws NoEntrantsException, NoOpponentException {
        if (pendingEntrants.size() == 0) throw new NoEntrantsException();
        if (pendingEntrants.size() == 1) throw new NoOpponentException();

        Collections.shuffle(pendingEntrants);
        Entrant first = popPendingEntrant();
        Entrant second = popPendingEntrant();

        activePairing = new Pairing(first, second);
        return activePairing;
    }

    @Override
    public void declareWinner(Entrant entrant) throws NoSuchEntrantException, MissingPairingException {
        if (!entrants.contains(entrant))
            throw new NoSuchEntrantException();

        if (activePairing == null || !activePairing.hasEntrant(entrant))
            throw new MissingPairingException();

        // Categorize the entrants of the corresponding pairing.
        advancedEntrants.add(entrant);
        eliminatedEntrants.add(activePairing.otherEntrant(entrant));

        // Add the pairing to the finished pairings.
        finishedPairings.add(activePairing);
        activePairing = null;

        // This invariant should always hold true.
        assert pendingEntrants.size() + advancedEntrants.size()
            + eliminatedEntrants.size() == entrants.size();
    }

    /**
     * @return Entrants that have advanced to the next round.
     */
    public Set<Entrant> getAdvancedEntrants() {
        return Collections.unmodifiableSet(advancedEntrants);
    }

    /**
     * @return Entrants that were eliminated in a finished pairing.
     */
    public Set<Entrant> getEliminatedEntrants() {
        // TODO: Decide for List or Set as storage container.
        return Collections.unmodifiableSet(eliminatedEntrants);
    }

    /**
     * @return Entrants that have not yet appeared in a previous pairing.
     */
    public List<Entrant> getPendingEntrants() {
        return Collections.unmodifiableList(pendingEntrants);
    }

    @Override
    public boolean isEliminated(Entrant entrant) {
        return eliminatedEntrants.contains(entrant);
    }

    /**
     * Checks if the current round is finished.
     * This is the case when there are no more pending entrants.
     * @return If the current round is finished.
     */
    @Override
    public boolean isFinished() {
        return pendingEntrants.isEmpty();
    }

    private Entrant popPendingEntrant() throws IndexOutOfBoundsException {
        int index = pendingEntrants.size() - 1;
        return pendingEntrants.remove(index);
    }
}
