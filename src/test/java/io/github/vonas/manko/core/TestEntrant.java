package io.github.vonas.manko.core;

import io.github.vonas.manko.util.Identifiable;

import java.io.Serializable;

public class TestEntrant extends Identifiable<Integer> implements Serializable {

    public TestEntrant(Integer id) {
        super(id);
    }
}
