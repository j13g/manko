package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.exceptions.NoSuchPairingException;

import java.util.Set;
import java.util.stream.Stream;

public interface EliminationRound<E> extends Round<E> {

    Set<E> getPendingEntrants();

    /**
     * @return Entrants that have advanced to the next round.
     */
    Set<E> getAdvancedEntrants();

    /**
     * Returns all entrants that have state associated with them.
     * That can either be active entrants or entrants that were removed
     * previously but still have a finished pairing associated with them.
     * @return An iterator over all entrants with state information.
     */
    Stream<E> getEntrantsWithState();

    /**
     * @return Entrants that were eliminated.
     */
    Set<E> getEliminatedEntrants();

    boolean isEntrantPending(E entrant);

    /**
     * @param entrant The entrant.
     * @return If the entrant has advanced to the next round.
     */
    boolean isEntrantAdvanced(E entrant);

    /**
     * @param entrant The entrant.
     * @return If the entrant is eliminated.
     */
    boolean isEntrantEliminated(E entrant);

    /**
     * Checks if a pairing is orphaned.
     * An orphaned pairing is a pairing where one entrant
     * is involved in another pairing that is finished.
     * @param pairing The pairing to check.
     * @return If the pairing is orphaned.
     */
    boolean isPairingOrphaned(Pairing<E> pairing)
            throws NoSuchPairingException;
}
