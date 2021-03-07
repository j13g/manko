package de.j13g.manko.util;

import java.util.LinkedHashSet;

public class UniformPairLinkedBiSet<E, P extends UniformPair<E>>
    extends UniformPairBiSet<E, P> {

    public UniformPairLinkedBiSet() {
        super(new LinkedHashSet<>());
    }
}
