package com.example.argumentgames;

public class FrameworkAttack {

    double prefControlX = 0,prefControlY = 0;
    private final FrameworkArgument from, to;

    public FrameworkAttack(FrameworkArgument from, FrameworkArgument to) {
        this.from = from;
        this.to = to;
    }
    public FrameworkAttack(FrameworkArgument from, FrameworkArgument to, double x, double y) {
        this.from = from;
        this.to = to;
        this.prefControlX = x;
        this.prefControlY = y;
    }

    public FrameworkArgument getTo() { return to; }
    public FrameworkArgument getFrom() { return from; }
}
