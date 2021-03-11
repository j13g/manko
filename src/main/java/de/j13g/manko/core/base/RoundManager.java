package de.j13g.manko.core.base;

import de.j13g.manko.core.exceptions.*;

/**
 * Manages rounds of a tournament.
 */
public interface RoundManager<E> {

    /**
     * Advances to the next round.
     * @throws RoundNotFinishedException The current round is not finished yet.
     * @throws FinalRoundException Already in the final round.
     */
    void nextRound() throws RoundNotFinishedException, FinalRoundException;

    /**
     * Goes back to the previous round if possible.
     * @throws AlreadyStartedException The round has already started.
     * @throws InitialRoundException There is no previous round.
     */
    void previousRound() throws AlreadyStartedException, InitialRoundException;
}
