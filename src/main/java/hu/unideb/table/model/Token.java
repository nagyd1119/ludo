package hu.unideb.table.model;

import java.awt.*;

public class Token {
    public Point origin;
    public Point current;
    public Point potential;
    public Player owner;

    public Token(Point origin, Point current, Point potential, Player owner) {
        this.origin = origin;
        this.current = current;
        this.potential = potential;
        this.owner = owner;
    }

    @Override
    public String toString() {
        return "Token{" + "origin=" + origin +
                ", current=" + current +
                ", potential=" + potential +
                ", owner=" + owner +
                '}';
    }
}
