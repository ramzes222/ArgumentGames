package com.example.argumentgames;

import java.util.ArrayList;

public class Framework {

    private final ArrayList<FrameworkArgument> arguments = new ArrayList<>();

    public Framework() {

    }

    // Builds a game tree rooted in the supplied argument
    // Can be a preferred or grounded game (defined by parameter)
    // In preferred games, proponent can repeat arguments in a dispute
    // In grounded games, opponent can repeat arguments in a dispute
    public TreeArgument buildGameTree(FrameworkArgument arg, boolean isGrounded) {
        // Create root
        TreeArgument root = new TreeArgument(arg.getName(), null);
        // Iterate over game rounds
        // Even rounds are made by opponent, odd by proponent
        int round = 1;
        ArrayList<TreeArgument> lastRoundArguments = new ArrayList<>(); lastRoundArguments.add(root);
        while (!lastRoundArguments.isEmpty()) {
            round++;
            boolean canArgumentsBeRepeated = (round%2==0) == isGrounded;
            ArrayList<TreeArgument> thisRoundArguments = new ArrayList<>();
            for (TreeArgument previousArg: lastRoundArguments) {
                // For each argument in last round, find all arguments that attack it
                // Create a TreeArgument for each, then add them to next round
                FrameworkArgument frArg = getArgumentByName(previousArg.getName());
                if (frArg != null) {
                    for (FrameworkArgument attackOfFrArg: frArg.getAttackedBy()) {
                        String attackerName = attackOfFrArg.getName();
                        if (canArgumentsBeRepeated || !previousArg.pastInBranchIncludes(attackerName)) {
                            TreeArgument newTreeAttack = new TreeArgument(attackerName, previousArg);
                            thisRoundArguments.add(newTreeAttack);
                        }
                    }
                }
            }
            lastRoundArguments = thisRoundArguments;
        }
        return root;
    }

    public void addArgument(String name) {
        FrameworkArgument newArg = new FrameworkArgument(name);
        arguments.add(newArg);
    }
    public ArrayList<FrameworkArgument> getArguments() { return arguments; }

    public FrameworkArgument getArgumentByName(String name) {
        for (FrameworkArgument arg : arguments) { if (arg.getName().equals(name)) return arg; }
        return null;
    }

    public void addAttack(FrameworkArgument from, FrameworkArgument to) {
        from.addToAttacks(to);
        to.addToAttackedBy(from);
    }

    public void addAttack(String fromName, String toName) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            from.addToAttacks(to);
            to.addToAttackedBy(from);
        }
    }
    public void removeAttack(FrameworkArgument from, FrameworkArgument to) {
        from.removeAttack(to);
        to.removeAttackedBy(from);
    }

    public boolean nameExists(String name) {
        for (FrameworkArgument arg : arguments) { if (arg.getName().equals(name)) return true; }
        return false;
    }
    public boolean attackExists(String fromName, String toName) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            return from.getAttacks().contains(to);
        }
        return false;
    }
}
