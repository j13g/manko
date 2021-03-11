package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.exceptions.NoSuchPairingException;

import java.util.Set;

public interface EliminationRound<E> extends Round<E> {

    /**
     * Checks if a pairing is orphaned.
     * An orphaned pairing is a pairing where one entrant
     * is involved in another pairing that is finished.
     * @param pairing The pairing to check.
     * @return If the pairing is orphaned.
     */
    boolean isPairingOrphaned(Pairing<E> pairing)
            throws NoSuchPairingException;

    /**
     * @param entrant The entrant.
     * @return If the entrant is either advanced or eliminated.
     */
    boolean hasResult(E entrant);

    /**
     * @param entrant The entrant.
     * @return If the entrant has advanced to the next round.
     */
    boolean isAdvanced(E entrant);

    /**
     * @param entrant The entrant.
     * @return If the entrant is eliminated.
     */
    boolean isEliminated(E entrant);

    /**
     * @return Entrants that have advanced to the next round.
     */
    Set<E> getAdvancedEntrants();

    /**
     * @return Entrants that were eliminated.
     */
    Set<E> getEliminatedEntrants();
}
