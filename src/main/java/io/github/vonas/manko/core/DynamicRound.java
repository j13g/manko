package io.github.vonas.manko.core;

import io.github.vonas.manko.exceptions.*;

import java.util.*;

public class DynamicRound extends Round {

    /**
     * Holds the entrants that have not yet appeared in a pairing.
     */
    private final ArrayList<Entrant> pendingEntrants = new ArrayList<>();
    private final transient Random random = new Random();

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
    public DynamicRound(Set<Entrant> entrants) {
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
     * Resets an entrant to its initial state.
     * Previous pairings will not be removed.
     * @param entrant The entrant.
     * @throws NoSuchEntrantException The entrant is not part of this round.
     */
    public void resetEntrant(Entrant entrant) throws NoSuchEntrantException {
        if (!entrants.contains(entrant))
            throw new NoSuchEntrantException();

        advancedEntrants.remove(entrant);
        eliminatedEntrants.remove(entrant);

        pendingEntrants.add(entrant);
    }

    @Override
    public void removeEntrant(Entrant entrant) {
        if (!entrants.contains(entrant))
            return;

        entrants.remove(entrant);
        advancedEntrants.remove(entrant);
        eliminatedEntrants.remove(entrant);

        // The linear time penalty and shifting (copying)
        // elements to their new positions should be fine,
        // as entrants will not be deleted that frequently.
        pendingEntrants.remove(entrant);

        // Remove all pairing where no entrant
        // is part of this round anymore.
        finishedPairings.removeIf(pairing ->
            !entrants.contains(pairing.getEntrant1()) && !entrants.contains(pairing.getEntrant2()));

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

        Entrant first = popRandomPendingEntrant();
        Entrant second = popRandomPendingEntrant();

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

    private Entrant popRandomPendingEntrant() throws IndexOutOfBoundsException {
        int index = random.nextInt(pendingEntrants.size());
        int lastIndex = pendingEntrants.size() - 1;

        Entrant declared = pendingEntrants.get(index);
        pendingEntrants.set(index, pendingEntrants.get(lastIndex));
        pendingEntrants.remove(lastIndex);

        return declared;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DynamicRound))
            return false;

        DynamicRound other = (DynamicRound)o;
        return pendingEntrants.equals(other.pendingEntrants)
            && advancedEntrants.equals(other.advancedEntrants)
            && eliminatedEntrants.equals(other.eliminatedEntrants);
    }
}
