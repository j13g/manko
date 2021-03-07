package de.j13g.manko.util;

import java.io.Serializable;
import java.util.*;

/**
 * A set that contains uniform pairs, i.e. pairs with values of equal type (UniformPair).
 * Offers access to a single (Unique) contained pair by querying with either of its values (BiSet).
 * @param <E> The type of the pair elements.
 * @param <P> The type of the pair.
 */
public class UniformPairBiSet<E, P extends UniformPair<E>>
        extends BaseUniformPairBiSet<E, P, Set<P>> implements Serializable {

    public UniformPairBiSet() {
        super(new HashSet<>());
    }

    public UniformPairBiSet(Set<P> elementContainer) {
        super(elementContainer);
    }

    @Override
    public boolean add(P pair) throws IllegalArgumentException {
        if (contains(pair))
            return false;

        E first = pair.getFirst();
        E second = pair.getSecond();

        index.putIfAbsent(first, new HashSet<>());
        index.putIfAbsent(second, new HashSet<>());

        index.get(first).add(pair);
        index.get(second).add(pair);

        return super.add(pair);
    }

    @Override
    public boolean remove(P pair) {
        if (!contains(pair))
            return false;

        super.remove(pair);

        E first = pair.getFirst();
        E second = pair.getSecond();

        Set<P> pairsOfFirst = index.get(first);
        Set<P> pairsOfSecond = index.get(second);

        boolean firstRemoved = pairsOfFirst.remove(pair);
        boolean secondRemoved = pairsOfSecond.remove(pair);
        assert firstRemoved && secondRemoved;

        // In case the set becomes empty after
        // removal of the pair it is kept anyway.
        // This just means that later no new set has to be created.

        return true;
    }

    @Override
    public void clear() {
        super.clear();
        index.clear();
    }

    @Override
    public Set<P> removeByElement(E element) {
        // Make a copy because we are removing while iterating.
        Set<P> pairs = new HashSet<>(findByElement(element));
        for (P pair : pairs)
            remove(pair);

        return pairs;
    }

    @Override
    public Set<P> findByElement(E element) {
        Set<P> pairings = index.get(element);
        return pairings == null
            ? Collections.emptySet()
            : Collections.unmodifiableSet(pairings);
    }

    @Override
    public Set<E> getPairElementSet() {
        return index.keySet();
    }
}
