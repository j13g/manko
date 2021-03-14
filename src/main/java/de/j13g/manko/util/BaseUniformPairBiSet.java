package de.j13g.manko.util;

import java.io.Serializable;
import java.util.*;

/**
 * Base for a UniformPairBiSet.
 * @param <E> The pair's element type
 * @param <P> The pair type.
 * @param <C> The type container that stores the pairs.
 */
public abstract class BaseUniformPairBiSet<E, P extends UniformPair<E>, C>
        extends BaseSet<P> implements Serializable {

    protected final HashMap<E, C> index = new HashMap<>();

    protected BaseUniformPairBiSet(Set<P> elements) {
        super(elements);
    }

    /**
     * Removes all pairs containing this element.
     * @param element The element.
     * @return The removed pair(s).
     */
    public abstract C removeByElement(E element);

    /**
     * @param element The element.
     * @return The pair(s) that contain(s) this element.
     */
    public abstract C findByElement(E element);

    /**
     * @param element The element.
     * @return The last inserted pair that contains this element or null.
     */
    public abstract P findLastByElement(E element);

    /**
     * Returns a view set of all elements that are in pairs of this set.
     * @return The elements of all stored pairs.
     */
    public abstract Set<E> getPairElementSet();
}
