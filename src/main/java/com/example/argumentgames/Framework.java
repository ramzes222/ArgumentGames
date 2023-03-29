package com.example.argumentgames;

import java.util.ArrayList;

public class Framework {

    private final ArrayList<FrameworkArgument> arguments = new ArrayList<>();
    private final ArrayList<FrameworkAttack> attacks = new ArrayList<>();

    public Framework() {

    }

    public void clear() {
        arguments.clear();
        attacks.clear();
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
            // Whether arguments can be repeated depends on round and game type
            boolean canArgumentsBeRepeated = (round%2==0) == isGrounded;
            // Collect arguments added in current round
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
    public void addArgument(String name, double x, double y) {
        FrameworkArgument newArg = new FrameworkArgument(name, x, y);
        arguments.add(newArg);
    }

    public void removeArgument(String name) {
        FrameworkArgument argToDelete = getArgumentByName(name);
        arguments.remove(argToDelete);
    }

    public ArrayList<FrameworkArgument> getArguments() { return arguments; }
    public ArrayList<FrameworkAttack> getAttacks() { return attacks; }

    public FrameworkArgument getArgumentByName(String name) {
        for (FrameworkArgument arg : arguments) { if (arg.getName().equals(name)) return arg; }
        return null;
    }

    public FrameworkAttack getAttack(FrameworkArgument from, FrameworkArgument to) {
        for (FrameworkAttack att: attacks) {
            if (att.getTo() == to && att.getFrom() == from) return att;
        }
        return null;
    }
//    public FrameworkAttack getAttack(String fromName, String toName) {
//        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
//        if (from!=null && to!=null) {
//            for (FrameworkAttack att: attacks) {
//                if (att.getTo() == to && att.getFrom() == from) return att;
//            }
//        }
//        return null;
//    }

    public void addAttack(String fromName, String toName) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            FrameworkAttack att = new FrameworkAttack(from, to);
            from.addToAttacks(att);
            to.addToAttacks(att);
            attacks.add(att);
        }
    }

    public void addAttack(String fromName, String toName, Double x, Double y) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            FrameworkAttack att = new FrameworkAttack(from, to, x, y);
            from.addToAttacks(att);
            to.addToAttacks(att);
            attacks.add(att);
        }
    }
    public void removeAttack(FrameworkArgument from, FrameworkArgument to) {
        FrameworkAttack att = getAttack(from, to);
        from.removeAttack( att );
        to.removeAttack( att );
        attacks.remove(att);
    }

    public void removeAttack(String fromName, String toName) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            removeAttack(from, to);
        }
    }

    public boolean nameExists(String name) {
        for (FrameworkArgument arg : arguments) { if (arg.getName().equals(name)) return true; }
        return false;
    }
    public boolean attackExists(String fromName, String toName) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            return from.attackToExists(to);
        }
        return false;
    }
}
