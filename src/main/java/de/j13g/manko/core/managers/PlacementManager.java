package de.j13g.manko.core.managers;

import de.j13g.manko.core.Placement;

import java.io.Serializable;
import java.util.HashMap;

// TODO Extract common methods compared to ScoreManager.
public class PlacementManager<E> implements Serializable {

    private static final Placement DEFAULT_PLACEMENT = Placement.TBD;

    private final HashMap<E, Placement> placements = new HashMap<>();
    private final HashMap<Placement, E> winners = new HashMap<>();

    public Placement setPlacement(E entrant, Placement placement) {
        Placement oldPlacement = getOrDefault(entrant);
        set(entrant, placement);

        return oldPlacement;
    }

    public Placement resetPlacement(E entrant) {
        Placement oldPlacement = getOrDefault(entrant);
        reset(entrant);

        return oldPlacement;
    }

    public Placement getPlacement(E entrant) {
        return getOrDefault(entrant);
    }

    public E getEntrantByPlacement(Placement placement) {
        if (!isValidWinnerPlacement(placement))
            throw new IllegalArgumentException();
        return winners.getOrDefault(placement, null);
    }

    private Placement getOrDefault(E entrant) {
        return placements.getOrDefault(entrant, DEFAULT_PLACEMENT);
    }

    private void set(E entrant, Placement placement) {
        placements.put(entrant, placement);
        if (isValidWinnerPlacement(placement))
            winners.put(placement, entrant);
    }

    private void reset(E entrant) {
        Placement placement = placements.get(entrant);
        placements.remove(entrant);
        winners.remove(placement);
    }

    private boolean isValidWinnerPlacement(Placement placement) {
        return placement == Placement.FIRST || placement == Placement.SECOND || placement == Placement.THIRD;
    }
}
