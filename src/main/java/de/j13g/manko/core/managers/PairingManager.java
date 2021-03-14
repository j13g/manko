package de.j13g.manko.core.managers;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.exceptions.NoSuchPairingException;
import de.j13g.manko.core.managers.base.Pairings;
import de.j13g.manko.util.UniformPairLinkedBiSet;
import de.j13g.manko.util.UniformPairUniqueLinkedBiSet;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Set;

public class PairingManager<E> implements Pairings<E>, Serializable {

    private final UniformPairUniqueLinkedBiSet<E, Pairing<E>> activePairings = new UniformPairUniqueLinkedBiSet<>();
    private final UniformPairLinkedBiSet<E, Pairing<E>> finishedPairings = new UniformPairLinkedBiSet<>();

    public boolean add(Pairing<E> pairing) {
        if (isFinished(pairing))
            return false;
        return this.activePairings.add(pairing);
    }

    public boolean finish(Pairing<E> pairing) throws NoSuchPairingException {
        if (isFinished(pairing))
            return false;
        if (!isActive(pairing))
            throw new NoSuchPairingException();

        finishedPairings.add(pairing);
        activePairings.remove(pairing);
        return true;
    }

    public boolean remove(Pairing<E> pairing) {
        return removeActive(pairing) || removeFinished(pairing);
    }

    public boolean removeActive(Pairing<E> activePairing) {
        return activePairings.remove(activePairing);
    }

    public boolean removeFinished(Pairing<E> finishedPairing) {
        return finishedPairings.remove(finishedPairing);
    }

    public Pairing<E> removeActiveByEntrant(E entrant) {
        return activePairings.removeByElement(entrant);
    }

    public Set<Pairing<E>> removeFinishedByEntrant(E entrant) {
        return finishedPairings.removeByElement(entrant);
    }

    @Override
    public Iterator<Pairing<E>> getActivePairingIterator() {
        return activePairings.elements().iterator();
    }

    @Override
    public Pairing<E> getLastPairingOfEntrant(E entrant) {
        Pairing<E> lastActive = activePairings.findLastByElement(entrant);
        Pairing<E> lastFinished = finishedPairings.findLastByElement(entrant);
        return lastActive != null ? lastActive : lastFinished;
    }

    @Override
    public Pairing<E> findActiveByEntrant(E entrant) {
        return activePairings.findByElement(entrant);
    }

    @Override
    public Set<Pairing<E>> findFinishedByEntrant(E entrant) {
        return finishedPairings.findByElement(entrant);
    }

    @Override
    public Set<Pairing<E>> getActive() {
        return activePairings.elements();
    }

    @Override
    public Set<Pairing<E>> getFinished() {
        return finishedPairings.elements();
    }

    @Override
    public Set<E> getActiveEntrants() {
        return activePairings.getPairElementSet();
    }

    @Override
    public Set<E> getFinishedEntrants() {
        return finishedPairings.getPairElementSet();
    }

    @Override
    public boolean isEmpty() {
        return !hasActive() && !hasFinished();
    }

    @Override
    public boolean hasActive() {
        return !activePairings.isEmpty();
    }

    @Override
    public boolean hasFinished() {
        return !finishedPairings.isEmpty();
    }

    @Override
    public boolean contains(Pairing<E> pairing) {
        return activePairings.contains(pairing) || finishedPairings.contains(pairing);
    }

    @Override
    public boolean isActive(Pairing<E> pairing) {
        return activePairings.contains(pairing);
    }

    @Override
    public boolean isFinished(Pairing<E> pairing) {
        return finishedPairings.contains(pairing);
    }

    @Override
    public boolean hasEntrant(E entrant) {
        return hasActiveEntrant(entrant) || hasFinishedEntrant(entrant);
    }

    @Override
    public boolean hasActiveEntrant(E entrant) {
        return activePairings.findByElement(entrant) != null;
    }

    @Override
    public boolean hasFinishedEntrant(E entrant) {
        return finishedPairings.findByElement(entrant) != null;
    }
}
