package com.example.argumentgames;

import java.util.ArrayList;

public class Framework {

    private final ArrayList<FrameworkArgument> arguments = new ArrayList<>();

    public Framework() {

    }

    public void addArgument(String name) {
        FrameworkArgument newArg = new FrameworkArgument(name);
        arguments.add(newArg);
    }
    public ArrayList<FrameworkArgument> getArguments() { return arguments; }

    public void addAttack(FrameworkArgument from, FrameworkArgument to) {
        from.addToAttacks(to);
        to.addToAttackedBy(from);
    }
    public void removeAttack(FrameworkArgument from, FrameworkArgument to) {
        from.removeAttack(to);
        to.removeAttackedBy(from);
    }
}
