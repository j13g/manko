package de.j13g.manko.core.base;

import java.util.Set;

public interface EliminationRound<E> extends Round<E>, WithExplicitPair<E> {

    /**
     * Add an entrant to this round.
     * @param entrant The entrant.
     * @return If the entrant was not already in this round.
     */
    boolean add(E entrant);

    /**
     * Resets an entrant back to the pending state.
     * If the entrant was removed left over state is cleared.
     * @param entrant The entrant.
     * @return If the reset lead to any changes.
     */
    boolean reset(E entrant);

    // NOTE: One cannot reset a player in every type of round.
    //  In the finale a reset is not necessary as you won't pair
    //  entrants manually (to give someone a second chance for instance)
    //  and replaying a pairing has a dedicated method.

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

    Set<E> getAdvancedEntrants();

    Set<E> getEliminatedEntrants();
}
