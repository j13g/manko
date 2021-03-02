package io.github.vonas.tournament.core;

import io.github.vonas.tournament.exceptions.MissingPairingException;
import io.github.vonas.tournament.exceptions.NoEntrantsException;
import io.github.vonas.tournament.exceptions.NoOpponentException;
import io.github.vonas.tournament.exceptions.NoSuchEntrantException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Round {

    protected final HashSet<Entrant> entrants = new HashSet<>();

    // When a player randomly leaves we possibly need to reorder pairings,
    // which is especially the case in the finale. Consider 4 players and
    // one leaves. We then switch from K.O. to Round Robin. If two players
    // have played before we need to be able to check the existence of
    // such pairing and only play pairing that are new.
    protected final HashSet<Pairing> finishedPairings = new HashSet<>();

    // TODO Handle multiple pairings in parallel.
    /** Currently active pairing. */
    protected Pairing activePairing = null;

    /**
     * Removes an entrant from this round.
     * @param entrant The entrant.
     */
    public abstract void removeEntrant(Entrant entrant);

    /**
     * Create the next pairing among the pending entrants.
     * @return The next pairing.
     * @throws NoEntrantsException No entrants are in this round.
     * @throws NoOpponentException The last entrant does not have an opponent.
     */
    public abstract Pairing nextPairing()
        throws NoEntrantsException, NoOpponentException;

    /**
     * Declare the winner of an active pairing.
     * @param entrant The winning entrant.
     * @throws NoSuchEntrantException The entrant is not part of this round.
     * @throws MissingPairingException The entrant is not in an active pairing.
     */
    public abstract void declareWinner(Entrant entrant)
        throws NoSuchEntrantException, MissingPairingException;
    // TODO Also write a declareWinner method in Pairing that references the Round.

    /**
     * @return All entrants of this round.
     */
    public HashSet<Entrant> getEntrants() {
        return entrants;
    }

    /**
     * @return Pairings of which a winner has been declared.
     */
    public Set<Pairing> getFinishedPairings() {
        return Collections.unmodifiableSet(finishedPairings);
    }

    /**
     * @return The currently active pairing.
     */
    public Pairing getActivePairing() {
        return activePairing;
    }

    /**
     * @return If there is a currently running pairing.
     */
    public boolean hasActivePairing() {
        return activePairing != null;
    }

    /**
     * @param entrant An entrant
     * @return If the entrant participates in this round.
     */
    public boolean hasEntrant(Entrant entrant) {
        return entrants.contains(entrant);
    }

    /**
     * Checks if an entrant was eliminated by previous pairings.
     * @param entrant The entrant.
     * @return If the entrant is eliminated.
     */
    public abstract boolean isEliminated(Entrant entrant);

    /**
     * @return If the current round is finished.
     */
    public abstract boolean isFinished();
}
