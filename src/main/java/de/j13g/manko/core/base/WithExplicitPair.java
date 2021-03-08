package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.exceptions.EntrantNotPendingException;
import de.j13g.manko.core.exceptions.NoSuchEntrantException;

public interface WithExplicitPair<E> {

    /**
     * Creates a pairing for two entrants.
     * @param entrant1 The first entrant.
     * @param entrant2 The second entrant.
     * @return The created pairing.
     * @throws NoSuchEntrantException One or both entrants are not part of this round.
     * @throws EntrantNotPendingException One or both entrants are not in pending state.
     */
    Pairing<E> createPairing(E entrant1, E entrant2) throws NoSuchEntrantException, EntrantNotPendingException;
}
