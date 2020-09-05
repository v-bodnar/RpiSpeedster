package com.omb.networking.dto;

import java.util.Objects;

public class Move {
    private final Direction direction;
    private final float speed;

    public Move(Direction direction, float speed) {
        this.direction = direction;
        this.speed = speed;
    }

    public Direction getDirection() {
        return direction;
    }

    public float getSpeed() {
        return speed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Move move = (Move) o;
        return Float.compare(move.speed, speed) == 0 &&
                direction == move.direction;
    }

    @Override
    public int hashCode() {
        return Objects.hash(direction, speed);
    }

    @Override
    public String toString() {
        return String.format("Move %s, speed: %s", direction, speed);
    }
}
