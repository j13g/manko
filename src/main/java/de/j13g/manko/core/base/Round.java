package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;

import java.util.Set;

/**
 * Represents a round in a tournament.
 * @param <E> The entrant type.
 */
public interface Round<E> extends RoundHandle<E>, RoundInformation<E> {

    Set<E> getEntrants();

    Set<Pairing<E>> getActivePairings();

    Set<Pairing<E>> getFinishedPairings();

    boolean hasEntrant(E entrant);

    /**
     * Checks if there is a pairing result associated with an entrant.
     * @param entrant The entrant.
     * @return If there is a pairing result.
     */
    boolean hasEntrantResult(E entrant);

    boolean isEntrantPaired(E entrant);

    boolean isFinished();
}
