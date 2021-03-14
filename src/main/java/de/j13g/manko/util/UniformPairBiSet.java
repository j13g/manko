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

        index.putIfAbsent(first, new LinkedHashSet<>());
        index.putIfAbsent(second, new LinkedHashSet<>());

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

        // We have to remove empty keys because
        // getPairElementSet() relies on their correctness.
        if (pairsOfFirst.isEmpty()) index.remove(first);
        if (pairsOfSecond.isEmpty()) index.remove(second);

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
    public P findLastByElement(E element) {
        // This approach is generally inefficient, but consider this:
        // How many pairs will there realistically be with the same elements?
        // Pretty much in the 1-3 element area, especially in our case.
        // Also, this method isn't called that frequently anyway, so It's Fine(TM).
        LinkedHashSet<P> pairings = (LinkedHashSet<P>) index.get(element);
        P last = null;
        if (pairings != null)
            for (P pairing : pairings)
                last = pairing;
        return last;
    }

    @Override
    public Set<E> getPairElementSet() {
        return index.keySet();
    }
}
