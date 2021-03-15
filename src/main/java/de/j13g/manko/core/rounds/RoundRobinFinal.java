package de.j13g.manko.core.rounds;

import de.j13g.manko.core.Pairing;
import de.j13g.manko.core.Placement;
import de.j13g.manko.core.Standings;
import de.j13g.manko.core.annotations.UnsupportedOperation;
import de.j13g.manko.core.base.FinalRound;
import de.j13g.manko.core.base.RankingRound;
import de.j13g.manko.core.exceptions.*;
import de.j13g.manko.core.managers.PairingManager;
import de.j13g.manko.core.managers.ScoreManager;
import de.j13g.manko.core.managers.base.Pairings;
import de.j13g.manko.util.ShuffledSet;
import de.j13g.manko.util.UniformPairBiSet;

import java.io.Serializable;
import java.util.*;

public class RoundRobinFinal<E> implements RankingRound<E>, FinalRound<E>, Serializable {

    private static final int ENTRANT_COUNT = 3;
    private static final int MIN_SCORE = 0;
    private static final int MAX_SCORE = 2;

    private final Set<E> entrants = new HashSet<>();

    private final UniformPairBiSet<E, Pairing<E>> originalPairings = new UniformPairBiSet<>();

    private final ShuffledSet<Pairing<E>> outstandingPairings = new ShuffledSet<>();

    private final HashMap<Pairing<E>, E> pairingWinners = new HashMap<>();

    private final PairingManager<E> pairings = new PairingManager<>();
    private final ScoreManager<E> scores = new ScoreManager<>();

    public RoundRobinFinal(E first, E second, E third) {
        entrants.add(first);
        entrants.add(second);
        entrants.add(third);
        originalPairings.add(new Pairing<>(first, second));
        originalPairings.add(new Pairing<>(first, third));
        originalPairings.add(new Pairing<>(second, third));
        originalPairings.elements().forEach(outstandingPairings::add);
    }

    /**
     * Returns the pairings of a player, that have not been finished yet.
     * @param entrant The entrant.
     * @return The entrant's pairings that were not finished.
     */
    private Set<Pairing<E>> getUnfinishedPairingsByEntrant(E entrant) {

        Set<Pairing<E>> allPairings = originalPairings.findByElement(entrant);
        HashSet<Pairing<E>> unfinishedPairings = new HashSet<>(allPairings);
        unfinishedPairings.removeAll(pairings.getFinished());
//        unfinishedPairings.removeIf(p -> (!p.equals(entrant) || !forceWithEntrant) && (!hasEntrant(p.getFirst()) || !hasEntrant(p.getSecond())));
        return unfinishedPairings;
    }

    @Override
    public boolean removeEntrant(E entrant) {
        if (!hasEntrant(entrant))
            return false;

        Pairing<E> removedActivePairing = pairings.removeActiveByEntrant(entrant);

        for (Pairing<E> pairing : getUnfinishedPairingsByEntrant(entrant)) {

            // Unfinished pairings have to be finished.
            // So give the opponent the win when we remove this entrant.
            int score = scores.incrementScore(pairing.getOther(entrant));
            boolean isPairingRemoved = outstandingPairings.remove(pairing);

            // Either this pairing was active or we removed a pending one.
            assert pairing.equals(removedActivePairing) || isPairingRemoved;
            assert score <= MAX_SCORE;
        }

        entrants.remove(entrant);
        return true;
    }

    @Override
    public boolean addEntrant(E entrant) throws NewEntrantsNotAllowedException {
        if (hasEntrant(entrant))
            return false;
        if (originalPairings.findByElement(entrant).isEmpty())
            throw new NewEntrantsNotAllowedException();

        for (Pairing<E> pairing : getUnfinishedPairingsByEntrant(entrant)) {

            int score = scores.decrementScore(pairing.getOther(entrant));
            boolean wasAdded = outstandingPairings.add(pairing);

            assert wasAdded;
            assert score >= MIN_SCORE;
        }

        entrants.add(entrant);
        return true;
    }

    @Override
    public Pairing<E> nextPairing() throws UnfinishedPairingsException, NoMorePairingsException {
        if (isFinished())
            throw new NoMorePairingsException();
        if (pairings.hasActive())
            throw new UnfinishedPairingsException();

        Pairing<E> pairing = outstandingPairings.removeRandom();
        pairings.add(pairing);
        return pairing;
    }

    @Override
    public void declareWinner(E winningEntrant, Pairing<E> pairing)
            throws NoSuchEntrantException, NoSuchPairingException {

        if (!pairing.contains(winningEntrant))
            throw new IllegalArgumentException("The entrant is not part of the pairing");

        if (!hasEntrant(winningEntrant))
            throw new NoSuchEntrantException();
        if (!pairings.isActive(pairing))
            throw new NoSuchPairingException();

        pairings.finish(pairing);
        pairingWinners.put(pairing, winningEntrant);
        int score = scores.incrementScore(winningEntrant);

        assert score <= MAX_SCORE;
    }

    @Override
    public Pairing<E> declareWinner(E winningEntrant)
            throws NoSuchEntrantException, MissingPairingException {

        if (!hasEntrant(winningEntrant))
            throw new NoSuchEntrantException();
        if (!pairings.hasActiveEntrant(winningEntrant))
            throw new MissingPairingException();

        Pairing<E> pairing = pairings.findActiveByEntrant(winningEntrant);
        declareWinner(winningEntrant, pairing);
        return pairing;
    }

    @Override
    public boolean replayPairing(Pairing<E> pairing)
            throws NoSuchPairingException, MissingEntrantException, OrphanedPairingException {

        if (pairings.isActive(pairing))
            return false;

        if (!pairings.isFinished(pairing))
            throw new NoSuchPairingException();
        if (!hasEntrant(pairing.getFirst()) || !hasEntrant(pairing.getSecond()))
            throw new MissingEntrantException();

        if (pairings.hasActiveEntrant(pairing.getFirst()) || pairings.hasActiveEntrant(pairing.getSecond()))
            throw new OrphanedPairingException();

        assert pairingWinners.containsKey(pairing);

        E winningEntrant = pairingWinners.get(pairing);
        int score = scores.decrementScore(winningEntrant);
        assert score >= MIN_SCORE;

        pairingWinners.remove(pairing);
        boolean removedFinished = pairings.removeFinished(pairing);
        pairings.add(pairing);

        assert removedFinished;
        return true;
    }

    @Override
    @UnsupportedOperation
    public boolean resetEntrant(E entrant) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public void declareTie(Pairing<E> pairing) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Placement getPlacement(E entrant) {
        // There will always be a distribution of (0, 1, 2) or (1, 1, 1)
        // in case all entrants have finished their two pairings (or were removed).

        int entrantScore = scores.getScore(entrant);

        // 1: Won 2 pairings, definitely first place.
        if (entrantScore == 2)
            return Placement.FIRST;

        // 2: Won 0 or 1 pairings, but someone else could still win (or lose).
        // Thus it's not possible to say which placement will be definite.
        if (!isFinished())
            return Placement.TBD;

        // Now: Won 0 or 1 pairings, without outstanding pairings.

        int totalScore = 0;

        int withOne = 0;
        boolean hasZero = false;
        boolean hasTwo = false;

        for (E other : entrants) {
            int score = scores.getScore(other);
            if (!other.equals(entrant))
                switch (score) {
                    case 0: hasZero = true; break;
                    case 1: withOne += 1; break;
                    case 2: hasTwo = true; break;
                }

            totalScore += score;
        }

        // Since no outstanding pairings are left, the total score must be 3,
        // or more generally, equal to the number of original pairings.
        assert totalScore == originalPairings.size();

        // Note that the number of finished pairings might be less than 3,
        // in case someone has been removed (their pairings are not considered finished then).

        // 3: Won 1 pairing, like everyone else. It's a tie.
        // Return NONE because nothing can change these placements (except replays).
        if (entrantScore == 1 && withOne == 2)
            return Placement.NONE;

        // 4: Won 1 pairing, 1st and 3rd place are given.
        if (entrantScore == 1 && hasZero && hasTwo)
            return Placement.SECOND;

        // 5: Two opponents won their pairing against this entrant,
        // so we definitely have less points than anyone else.
        if (entrantScore == 0)
            return Placement.THIRD;

        // There should be no way to reach this.
        assert false;
        return Placement.TBD;
    }

    public Standings<E> getStandings() {
        HashMap<Placement, E> placements = new HashMap<>(Placement.values().length);

        for (E entrant : entrants) {
            Placement placement = getPlacement(entrant);
            assert !placements.containsKey(placement);
            placements.put(placement, entrant);
        }

        for (Placement placement : Placement.values())
            if (!placements.containsKey(placement))
                placements.put(placement, null);

        return new Standings<>(
                placements.get(Placement.FIRST),
                placements.get(Placement.SECOND),
                placements.get(Placement.THIRD)
        );
    }

    @Override
    public E getEntrantByPlacement(Placement placement) {
        for (E entrant : entrants)
            if (getPlacement(entrant) == placement)
                return entrant;
        return null;
    }

    @Override
    public Set<E> getEntrants() {
        return entrants;
    }

    @Override
    public Pairings<E> getPairings() {
        return pairings;
    }

    public int getScore(E entrant) {
        return scores.getScore(entrant);
    }

    @Override
    public Set<E> getPairedEntrants() {
        return pairings.getActiveEntrants();
    }

    @Override
    public Set<Pairing<E>> getActivePairings() {
        return pairings.getActive();
    }

    @Override
    public Set<Pairing<E>> getFinishedPairings() {
        return pairings.getFinished();
    }

    @Override
    public Pairing<E> getLastPairing(E entrant) {
        return pairings.getLastPairingOfEntrant(entrant);
    }

    @Override
    public boolean hasEntrant(E entrant) {
        return entrants.contains(entrant);
    }

    @Override
    public boolean hasEntrantResult(E entrant) {
        for (Pairing<E> pairing : originalPairings.elements())
            if (pairing.contains(entrant))
                if (pairingWinners.containsKey(pairing))
                    return true;
        return false;
    }

    @Override
    @UnsupportedOperation
    public boolean hasWon(E entrant) {
        throw new UnsupportedOperationException();
    }

    @Override
    @UnsupportedOperation
    public boolean hasLost(E entrant) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean hasStateAbout(E entrant) {
        return !originalPairings.findByElement(entrant).isEmpty();
    }

    @Override
    public boolean isEntrantPaired(E entrant) {
        return pairings.hasActiveEntrant(entrant);
    }

    /**
     * Checks if this round is a tie, i.e. if all entrants
     * have played in a pairing and they have equal scores.
     * @return If this round is finished and a tie.
     */
    public boolean isTie() {
        if (!isFinished())
            return false;

        boolean isTie = true;
        for (E entrant : entrants)
            if (getPlacement(entrant) != Placement.NONE) {
                isTie = false;
                break;
            }

        return isTie;
    }

    @Override
    public boolean isFinished() {
        return outstandingPairings.isEmpty() && !pairings.hasActive();
    }
}
