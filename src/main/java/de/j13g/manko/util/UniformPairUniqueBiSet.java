package de.j13g.manko.util;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

/**
 * A set that contains uniform pairs, i.e. pairs with values of equal type (UniformPair).
 * A value of a pair can only be inside that pair (once or twice) but in no other pairs (Unique).
 * Offers access to a single (Unique) contained pair by querying with either of its values (BiSet).
 * @param <E> The type of the pair elements.
 * @param <P> The type of the pair.
 */
public class UniformPairUniqueBiSet<E, P extends UniformPair<E>>
        extends BaseSet<P> implements Serializable {

    private final HashMap<E, P> index = new HashMap<>();

    public UniformPairUniqueBiSet() {
        super(new HashSet<>());
    }

    public UniformPairUniqueBiSet(Set<P> elementContainer) {
        super(elementContainer);
    }

    @Override
    public boolean add(P pair) throws IllegalArgumentException {
        if (contains(pair))
            return false;

        if (index.containsKey(pair.getFirst()) || index.containsKey(pair.getSecond()))
            throw new IllegalArgumentException("Pair element already present");

        index.put(pair.getFirst(), pair);
        index.put(pair.getSecond(), pair);
        return super.add(pair);
    }

    @Override
    public boolean remove(P pair) {
        if (!contains(pair))
            return false;

        super.remove(pair);
        index.remove(pair.getFirst());
        index.remove(pair.getSecond());
        return true;
    }

    public P removeByElement(E element) {
        P pair = findByElement(element);
        if (pair != null)
            remove(pair);
        return pair;
    }

    @Override
    public void clear() {
        super.clear();
        index.clear();
    }

    /**
     * Finds the pairing that contains this element
     * or null if there is no pair with this element.
     * @param element The element.
     * @return The pairing with this element or null.
     */
    public P findByElement(E element) {
        return index.get(element);
    }
}
