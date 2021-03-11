package de.j13g.manko.core;

import de.j13g.manko.util.UniformPair;

import java.io.Serializable;

/**
 * A pairing between two entrants.
 * @param <E> The entrant type.
 */
public class Pairing<E> extends UniformPair<E> implements Serializable {

    /**
     * Creates a pairing between two entrants.
     * @param first The first entrant.
     * @param second The second entrant.
     */
    public Pairing(E first, E second) {
        super(first, second);
    }
}
