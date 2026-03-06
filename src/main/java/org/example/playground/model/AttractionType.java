package org.example.playground.model;

import lombok.Getter;

@Getter
public enum AttractionType {
    DOUBLE_SWING(2),
    CAROUSEL(1),
    SLIDE(1),
    BALL_PIT(10);

    private final int capacity;

    AttractionType(int capacity) {
        this.capacity = capacity;
    }
}
