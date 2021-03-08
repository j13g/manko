package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.exceptions.*;

import java.util.Set;

public interface Round<E> {

    /**
     * Generates the next pairing between two participants.
     * Either picks random pending entrants to create the pairing or
     * picks a random pairing from a pre-determined list.
     * @return The pairing.
     * @throws NoEntrantsException There are no entrants in this round.
     * @throws NoOpponentException The only remaining entrant does not have an opponent.
     * @throws RoundFinishedException The round is already finished.
     */
    Pairing<E> nextPairing() throws NoEntrantsException, NoOpponentException, RoundFinishedException;

    /**
     * Declares the winner of an active pairing.
     * @param entrant The winning entrant.
     * @return The pairing this entrant was part of.
     * @throws NoSuchEntrantException This entrant is not part of this round.
     * @throws MissingPairingException This entrant is not part of any active pairing.
     */
    Pairing<E> declareWinner(E entrant) throws NoSuchEntrantException, MissingPairingException;

    /**
     * Replays a pairing and reverts the results of the corresponding entrants.
     * @param pairing The pairing to replay.
     * @return If the pairing was not still running.
     * @throws NoSuchPairingException This pairing does not exist.
     * @throws MissingEntrantException An entrant is not part of the round anymore.
     * @throws EntrantNotPendingException One of the entrants was reset but is not pending anymore.
     */
    boolean replayPairing(Pairing<E> pairing)
        throws NoSuchPairingException, MissingEntrantException, EntrantNotPendingException;

    /**
     * Removes an entrant from the round.
     * @param entrant The entrant.
     * @return If the entrant was not already removed.
     */
    boolean remove(E entrant);

    boolean contains(E entrant);

    boolean isPending(E entrant);

    boolean isPaired(E entrant);

    /**
     * @return If the round is finished.
     */
    boolean isFinished();

    Set<E> getPendingEntrants();

    Set<Pairing<E>> getActivePairings();

    Set<Pairing<E>> getFinishedPairings();
}
