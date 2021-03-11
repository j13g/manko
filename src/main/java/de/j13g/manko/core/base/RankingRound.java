package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Placement;

import java.util.List;
import java.util.Set;

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
    Set<Pairing<E>> getOutstandingPairings();

    /**
     * Gets the final placement of this entrant
     * or null if this entrant does not have a definite placement yet.
     * @param entrant The entrant.
     * @return The entrant's placement or null.
     */
    Placement getPlacement(E entrant);

    /**
     * Gets the entrant that occupies this placement.
     * @param placement The placement.
     * @return The entrant with this placement.
     */
    E getEntrantByPlacement(Placement placement);
}
