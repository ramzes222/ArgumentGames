package com.example.argumentgames;

public class FrameworkMetaAttack {
    double prefControlX = 0,prefControlY = 0;
    private final FrameworkAttack to;
    private final FrameworkArgument from;

    public FrameworkMetaAttack(FrameworkArgument from, FrameworkAttack to) {
        this.from = from;
        this.to = to;
    }
    public FrameworkMetaAttack(FrameworkArgument from, FrameworkAttack to, double x, double y) {
        this.from = from;
        this.to = to;
        this.prefControlX = x;
        this.prefControlY = y;
    }

    public FrameworkAttack getTo() { return to; }
    public FrameworkArgument getFrom() { return from; }
    public String getName() { return  from.getName() + "->(" + to.getName() + ")"; }

}
