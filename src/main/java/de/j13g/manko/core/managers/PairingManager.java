package de.j13g.manko.core.managers;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.exceptions.NoSuchPairingException;
import de.j13g.manko.util.UniformPairLinkedBiSet;
import de.j13g.manko.util.UniformPairUniqueBiSet;

import java.util.Set;

public class PairingManager<E> {

    private final UniformPairUniqueBiSet<E, Pairing<E>> activePairings = new UniformPairUniqueBiSet<>();
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

    public Pairing<E> findActiveByEntrant(E entrant) {
        return activePairings.findByElement(entrant);
    }

    public Set<Pairing<E>> findFinishedByEntrant(E entrant) {
        return finishedPairings.findByElement(entrant);
    }

    public Pairing<E> removeActiveByEntrant(E entrant) {
        return activePairings.removeByElement(entrant);
    }

    public Set<Pairing<E>> removeFinishedByEntrant(E entrant) {
        return finishedPairings.removeByElement(entrant);
    }

    public boolean removeActive(Pairing<E> activePairing) {
        return activePairings.remove(activePairing);
    }

    public boolean removeFinished(Pairing<E> finishedPairing) {
        return finishedPairings.remove(finishedPairing);
    }

    public Set<Pairing<E>> getActive() {
        return activePairings.elements();
    }

    public Set<Pairing<E>> getFinished() {
        return finishedPairings.elements();
    }

    public Set<E> getActiveEntrants() {
        return activePairings.getPairElementSet();
    }

    public Set<E> getFinishedEntrants() {
        return finishedPairings.getPairElementSet();
    }

    public boolean isEmpty() {
        return !hasActive() && !hasFinished();
    }

    public boolean hasActive() {
        return !activePairings.isEmpty();
    }

    public boolean hasFinished() {
        return !finishedPairings.isEmpty();
    }

    public boolean contains(Pairing<E> pairing) {
        return activePairings.contains(pairing) || finishedPairings.contains(pairing);
    }

    public boolean isActive(Pairing<E> pairing) {
        return activePairings.contains(pairing);
    }

    public boolean isFinished(Pairing<E> pairing) {
        return finishedPairings.contains(pairing);
    }

    public boolean hasEntrant(E entrant) {
        return hasActiveEntrant(entrant) || hasFinishedEntrant(entrant);
    }

    public boolean hasActiveEntrant(E entrant) {
        return activePairings.findByElement(entrant) != null;
    }

    public boolean hasFinishedEntrant(E entrant) {
        return finishedPairings.findByElement(entrant) != null;
    }
}
