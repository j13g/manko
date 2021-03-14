package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Standings;
import de.j13g.manko.core.managers.base.Pairings;

import java.util.Set;

/**
 * Represents a round in a tournament.
 * @param <E> The entrant type.
 */
public interface Round<E> extends RoundHandle<E>, RoundInformation<E> {

    Set<E> getEntrants();

    Pairings<E> getPairings();

    // TODO Can be removed.
    Set<E> getPairedEntrants();

    // TODO Can be removed.
    Set<Pairing<E>> getActivePairings();

    // TODO Can be removed.
    Set<Pairing<E>> getFinishedPairings();

    // TODO Can be removed.
    /**
     * Gets the active pairing of an entrant
     * or null if no such pairing exists.
     * @param entrant The entrant.
     * @return The pairing of that entrant or null.
     */
    Pairing<E> getLastPairing(E entrant);

    // TODO This can be returned for every round.
//    Standings<E> getStandings();

    boolean hasEntrant(E entrant);

    /**
     * Checks if there is a pairing result associated with an entrant.
     * @param entrant The entrant.
     * @return If there is a pairing result.
     */
    boolean hasEntrantResult(E entrant);

    boolean hasWon(E entrant);

    boolean hasLost(E entrant);

    /**
     * Checks if there is any state about an entrant.
     * @param entrant The entrant.
     * @return If any state is associated to this entrant.
     */
    boolean hasStateAbout(E entrant);

    // TODO Can be removed.
    boolean isEntrantPaired(E entrant);

    boolean isFinished();
}
