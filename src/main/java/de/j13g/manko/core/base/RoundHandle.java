package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.exceptions.*;

/**
 * A handle for a tournament round. Used for managing the round of a tournament.
 * Implements methods for both a round and any class that handles a round.
 * All operations actively change the state of the round.
 * @param <E> The entrant type.
 */
public interface RoundHandle<E> {

    /**
     * Adds an entrant to the round.
     * @param entrant The entrant to add.
     * @return If that entrant was not already in the round.
     * @throws NewEntrantsNotAllowedException Adding a new entrant is not allowed.
     */
    boolean addEntrant(E entrant) throws NewEntrantsNotAllowedException;

    /**
     * Removes the entrant from the round if possible.
     * @param entrant The entrant to remove.
     * @return If the entrant was not already removed.
     */
    boolean removeEntrant(E entrant);

    /**
     * Resets an entrant back to the initial state.
     * If the entrant is not part of the round, left over state is cleared.
     * @param entrant The entrant to reset.
     * @return If the reset lead to any changes.
     */
    boolean resetEntrant(E entrant);

    /**
     * Generates the next pairing between two participants.
     * @return The pairing.
     * @throws NoEntrantsException There are no entrants in this round.
     * @throws NoOpponentException The only remaining entrant does not have an opponent.
     * @throws NoMorePairingsException The round is already finished.
     */
    Pairing<E> nextPairing()
            throws NoEntrantsException, NoOpponentException, NoMorePairingsException;

    /**
     * Reverts the results of a pairing, so that it can be carried out again.
     * @param pairing The pairing to replay.
     * @return If the pairing was not still running.
     * @throws NoSuchPairingException This pairing was never carried out.
     * @throws MissingEntrantException An entrant is not part of the round anymore.
     * @throws OrphanedPairingException The pairing is orphaned and cannot be replayed.
     */
    boolean replayPairing(Pairing<E> pairing)
            throws NoSuchPairingException, MissingEntrantException, OrphanedPairingException;

    /**
     * Declares the winner of an active pairing.
     * @param winningEntrant The winning entrant.
     * @throws NoSuchEntrantException The entrant is not part of the round.
     * @throws NoSuchPairingException The pairing is not part of the round or not active.
     */
    void declareWinner(E winningEntrant, Pairing<E> pairing)
            throws NoSuchEntrantException, NoSuchPairingException;

    /**
     * Declares an entrant winner of their active pairing.
     * The pairing must be the only active pairing this entrant is involved in.
     * @see RoundHandle#declareWinner(Object, Pairing)
     * @param winningEntrant The winning entrant.
     * @return The pairing that this entrant won.
     * @throws NoSuchEntrantException The entrant is not part of the round.
     * @throws MissingPairingException This entrant is not part of any active pairing.
     */
    Pairing<E> declareWinner(E winningEntrant)
            throws NoSuchEntrantException, MissingPairingException;

    /**
     * Declares a pairing a tie.
     * @param pairing The pairing.
     */
    void declareTie(Pairing<E> pairing)
            throws NoSuchPairingException;
}
