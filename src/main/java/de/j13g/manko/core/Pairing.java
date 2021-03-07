package de.j13g.manko.core;

import de.j13g.manko.util.UniformPair;

import java.io.Serializable;

/**
 * A pairing between two entrants.
 * @param <E> The type of the entrant.
 */
public class Pairing<E> extends UniformPair<E> implements Serializable {

    public Pairing(E entrant1, E entrant2) {
        super(entrant1, entrant2);
    }

    public E getEntrant1() {
        return getFirst();
    }

    public E getEntrant2() {
        return getSecond();
    }
}
