package io.github.vonas.manko.core;

import io.github.vonas.manko.exceptions.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.*;

public class DynamicRound extends Round {

    /**
     * Holds the entrants that have not yet appeared in a pairing.
     */
    private final ArrayList<Entrant> pendingEntrants = new ArrayList<>();
    private transient Random random;

    private final HashSet<Entrant> advancedEntrants = new HashSet<>();
    private final HashSet<Entrant> eliminatedEntrants = new HashSet<>();

    /**
     * Creates a new round for a tournament. No entrants are known in advance.
     * This is the default for the first round of a dynamic tournament.
     */
    public DynamicRound() {
        initTransient();
    }

    /**
     * Creates a new round for a tournament with initial entrants.
     * @param entrants The initial entrants.
     */
    public DynamicRound(Set<Entrant> entrants) {
        this();
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

        removeEntrant(entrant);
        addEntrant(entrant);
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

        // TODO Don't remove anything from finishedPairings. Make it an array.
        // Remove all pairings where no entrant is part of this round anymore.
        finishedPairings.removeIf(pairing ->
            !entrants.contains(pairing.getEntrant1()) && !entrants.contains(pairing.getEntrant2()));

        if (!hasActivePairing()) return;
        if (!activePairing.hasEntrant(entrant))
            return;

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

        // Destroy the active pairing.
        activePairing = null;
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

    /**
     * Resets a pairing to the state before the winner was declared.
     * The entrants of the pairing are reset and the pairing becomes the active pairing.
     * @param pairing The pairing.
     */
    public void redoPairing(Pairing pairing)
            throws PairingNotFinishedException, NoSuchPairingException, MissingEntrantException {

        // TODO Currently we need to check if a pairing is not already running
        //  since there can only be one pairing at a time at the time of this writing.
        //  This needs to be addressed.
        if (pairing == activePairing)
            throw new PairingNotFinishedException();

        if (!finishedPairings.contains(pairing))
            throw new NoSuchPairingException();

        if (!entrants.contains(pairing.getEntrant1()) || !entrants.contains(pairing.getEntrant2()))
            throw new MissingEntrantException();

        try {
            resetEntrant(pairing.getEntrant1());
            resetEntrant(pairing.getEntrant2());
        } catch (NoSuchEntrantException e) {
            throw new RuntimeException(e);
        }

        // resetEntrant adds the entrants back to the end of
        // the pendingEntrants array. We need to remove them again.
        assert pendingEntrants.get(pendingEntrants.size() - 1).equals(pairing.getEntrant2());
        assert pendingEntrants.get(pendingEntrants.size() - 2).equals(pairing.getEntrant1());
        pendingEntrants.remove(pendingEntrants.size() - 1);
        pendingEntrants.remove(pendingEntrants.size() - 1);

        finishedPairings.remove(pairing);
        activePairing = pairing;
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
     * Checks if an entrant is waiting for a pairing.
     * @param entrant The entrant.
     * @return If the entrant is pending.
     */
    public boolean isPending(Entrant entrant) {
        // Since pendingEntrants is an array, it's faster to check if
        // the entrant has not advanced or wasn't eliminated yet,
        // since the containers holding those entrants are sets.
        boolean result = !advancedEntrants.contains(entrant)
            && !eliminatedEntrants.contains(entrant)
            && (activePairing == null || !activePairing.hasEntrant(entrant));

        assert result == pendingEntrants.contains(entrant);
        return result;
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

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        initTransient();
    }

    private void initTransient() {
        random = new Random();
    }
}
