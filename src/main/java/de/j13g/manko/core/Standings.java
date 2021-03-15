package de.j13g.manko.core;

import java.io.Serializable;

public class Standings<E> implements Serializable {

    private final E firstPlace;
    private final E secondPlace;
    private final E thirdPlace;

    public Standings(E firstPlace, E secondPlace, E thirdPlace) {
        this.firstPlace = firstPlace;
        this.secondPlace = secondPlace;
        this.thirdPlace = thirdPlace;
    }

    public E getFirstPlace() {
        return firstPlace;
    }

    public E getSecondPlace() {
        return secondPlace;
    }

    public E getThirdPlace() {
        return thirdPlace;
    }
}
