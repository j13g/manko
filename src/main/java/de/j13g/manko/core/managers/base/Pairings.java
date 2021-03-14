package de.j13g.manko.core.managers.base;

import de.j13g.manko.core.Pairing;

import java.util.Iterator;
import java.util.Set;

public interface Pairings<E> {

    Iterator<Pairing<E>> getActivePairingIterator();

    Pairing<E> getLastPairingOfEntrant(E entrant);

    Pairing<E> findActiveByEntrant(E entrant);

    Set<Pairing<E>> findFinishedByEntrant(E entrant);

    Set<Pairing<E>> getActive();

    Set<Pairing<E>> getFinished();

    Set<E> getActiveEntrants();

    Set<E> getFinishedEntrants();

    boolean isEmpty();

    boolean hasActive();

    boolean hasFinished();

    boolean contains(Pairing<E> pairing);

    boolean isActive(Pairing<E> pairing);

    boolean isFinished(Pairing<E> pairing);

    boolean hasEntrant(E entrant);

    boolean hasActiveEntrant(E entrant);

    boolean hasFinishedEntrant(E entrant);
}
