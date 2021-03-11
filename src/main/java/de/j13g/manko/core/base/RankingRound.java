package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Placement;

import java.util.Collection;
import java.util.List;

public interface RankingRound<E> extends Round<E> {

    /**
     * Returns the upcoming pairings.
     * These pairings appear in the given order.
     * @return The upcoming pairings.
     */
    List<Pairing<E>> getUpcomingPairings();

    /**
     * Returns pairings that are outstanding.
     * Outstanding pairings are pairings which are known to be carried out at some point,
     * but where the order might not be known in advance. They come after upcoming pairings.
     * @return The outstanding pairings, if any.
     */
    Collection<Pairing<E>> getOutstandingPairings();

    /**
     * Gets the current placement of this entrant.
     * @param entrant The entrant.
     * @return The entrant's placement
     */
    Placement getPlacement(E entrant);

    /**
     * Gets the entrant that occupies this placement or null.
     * @param placement The placement.
     * @return The entrant with this placement or null.
     */
    E getEntrantByPlacement(Placement placement);
}
