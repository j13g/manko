package de.j13g.manko.core;

import de.j13g.manko.util.Identifiable;

import java.io.Serializable;

public class TestEntrant extends Identifiable<Integer> implements Serializable {

    public TestEntrant(Integer id) {
        super(id);
    }
}
