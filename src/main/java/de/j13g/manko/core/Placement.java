package de.j13g.manko.core;

import java.io.Serializable;

public enum Placement implements Serializable {

    FIRST("1st"), // 1st place
    SECOND("2nd"), // 2nd place
    THIRD("3rd"), // 3rd place
    NONE, // No placement
    TBD; // To be determined

    private final String title;

    Placement(String title) {
        this.title = title;
    }

    Placement() {
        this(null);
    }

    public int getValue() {
        switch (this) {
            case FIRST: return 1;
            case SECOND: return 2;
            case THIRD: return 3;
            case NONE: return 0;
            case TBD: return -1;
            default: return -2;
        }
    }

    @Override
    public String toString() {
        return title == null
                ? super.toString()
                : title;
    }
}
