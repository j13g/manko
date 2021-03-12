package de.j13g.manko.core.base;

import de.j13g.manko.core.Pairing;

public abstract class BaseRankingRound<E> implements RankingRound<E> {

    @Override
    public boolean resetEntrant(E entrant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void declareTie(Pairing<E> pairing) {
        throw new UnsupportedOperationException();
    }
}
