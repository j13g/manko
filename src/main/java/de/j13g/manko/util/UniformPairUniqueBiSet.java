package de.j13g.manko.util;

import java.io.Serializable;
import java.util.*;

/**
 * A set that contains uniform pairs, i.e. pairs with values of equal type (UniformPair).
 * A value of a pair can only be inside that pair (once or twice) but in no other pairs (Unique).
 * Offers access to a single (Unique) contained pair by querying with either of its values (BiSet).
 * @param <E> The type of the pair elements.
 * @param <P> The type of the pair.
 */
public class UniformPairUniqueBiSet<E, P extends UniformPair<E>>
        extends BaseUniformPairBiSet<E, P, P> implements Serializable {

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

    @Override
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

    @Override
    public P findByElement(E element) {
        return index.get(element);
    }

    @Override
    public P findLastByElement(E element) {
        return findByElement(element);
    }

    @Override
    public Set<E> getPairElementSet() {
        return index.keySet();
    }
}
