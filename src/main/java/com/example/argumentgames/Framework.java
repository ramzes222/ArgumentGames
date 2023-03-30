package com.example.argumentgames;

import java.util.ArrayList;

public class Framework {

    private final ArrayList<FrameworkArgument> arguments = new ArrayList<>();
    private final ArrayList<FrameworkAttack> attacks = new ArrayList<>();
    private final ArrayList<FrameworkMetaAttack> metaAttacks = new ArrayList<>();

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
                    for (FrameworkArgument attackOfFrArg: getAttackedBy(frArg)) {
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

    public FrameworkAttack getAttackByName(String attName) {
        for (FrameworkAttack att : attacks) { if (att.getName().equals(attName)) return att; }
        return null;
    }

    public FrameworkAttack getAttack(FrameworkArgument from, FrameworkArgument to) {
        for (FrameworkAttack att: attacks) {
            if (att.getTo() == to && att.getFrom() == from) return att;
        }
        return null;
    }
    public FrameworkMetaAttack getMetaAttack(FrameworkArgument from, FrameworkAttack to) {
        for (FrameworkMetaAttack att: metaAttacks) {
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
            attacks.add(att);
        }
    }

    public void addAttack(String fromName, String toName, Double x, Double y) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            FrameworkAttack att = new FrameworkAttack(from, to, x, y);
            attacks.add(att);
        }
    }

    public void addMetaAttack(String fromName, String toAttackName) {
        FrameworkArgument from = getArgumentByName(fromName);
        FrameworkAttack to = getAttackByName(toAttackName);
        if (from!=null && to!=null) {
            FrameworkMetaAttack metAtt = new FrameworkMetaAttack(from, to);
            metaAttacks.add(metAtt);
        }
    }
    public void addMetaAttack(String fromName, String toAttackName, Double x, Double y) {
        FrameworkArgument from = getArgumentByName(fromName);
        FrameworkAttack to = getAttackByName(toAttackName);
        if (from!=null && to!=null) {
            FrameworkMetaAttack metAtt = new FrameworkMetaAttack(from, to, x, y);
            metaAttacks.add(metAtt);
        }
    }

    public void removeAttack(FrameworkArgument from, FrameworkArgument to) {
        FrameworkAttack att = getAttack(from, to);
        attacks.remove(att);
    }

    public void removeAttack(String fromName, String toName) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            removeAttack(from, to);
        }
    }

    public void removeMetaAttack(FrameworkArgument from, FrameworkAttack to) {
        FrameworkMetaAttack att = getMetaAttack(from, to);
        metaAttacks.remove(att);
    }

    public void removeMetaAttack(String fromName, String toAttackName) {
        FrameworkArgument from = getArgumentByName(fromName);
        FrameworkAttack to = getAttackByName(toAttackName);
        if (from!=null && to!=null) {
            removeMetaAttack(from, to);
        }
    }

    public boolean nameExists(String name) {
        for (FrameworkArgument arg : arguments) { if (arg.getName().equals(name)) return true; }
        return false;
    }

    public boolean attackExists(String fromName, String toName) {
        FrameworkArgument from = getArgumentByName(fromName), to = getArgumentByName(toName);
        if (from!=null && to!=null) {
            for (FrameworkAttack att: attacks) {
                if (att.getFrom() == from && att.getTo() == to) return true;
            }
        }
        return false;
    }

    public boolean metaAttackExists(String fromName, String toAttackName) {
        FrameworkArgument from = getArgumentByName(fromName);
        FrameworkAttack to = getAttackByName(toAttackName);
        if (from!=null && to!=null) {
            for (FrameworkMetaAttack att: metaAttacks) {
                if (att.getFrom() == from && att.getTo() == to) return true;
            }
        }
        return false;
    }

    public ArrayList<FrameworkArgument> getAttackedBy(FrameworkArgument arg) {
        ArrayList<FrameworkArgument> res = new ArrayList<>();
        for (FrameworkAttack att: attacks) {
            if (att.getTo() == arg) res.add(att.getFrom());
        }
        return res;
    }

    public ArrayList<FrameworkArgument> getAdjacent(FrameworkArgument arg) {
        ArrayList<FrameworkArgument> res = new ArrayList<>();
        for (FrameworkAttack att: attacks) {
            if (att.getTo() == arg) res.add(att.getFrom());
            else if (att.getFrom() == arg) res.add(att.getTo());
        }
        return res;
    }

    // Returns the set of all arguments connected to the parameter via regular attacks
    public ArrayList<FrameworkArgument> getArgumentConnectedSet(FrameworkArgument source) {
        ArrayList<FrameworkArgument> nextSet = new ArrayList<>(), connectedSet = new ArrayList<>();
        nextSet.add(source); connectedSet.add(source);
        boolean continueLoop = true;
        while (continueLoop) {
            continueLoop = false;
            ArrayList<FrameworkArgument> nextRound = new ArrayList<>();
            for (FrameworkArgument nextArg: nextSet) {
                ArrayList<FrameworkArgument> adjacentSet = getAdjacent(nextArg);
                for (FrameworkArgument adjArg: adjacentSet) {
                    if (!connectedSet.contains(adjArg)) {
                        // Not yet tracked - add
                        connectedSet.add(adjArg);
                        nextRound.add(adjArg);
                        continueLoop = true;
                    }
                }
            }
            nextSet = nextRound;
        }
        return connectedSet;
    }

    // Checks whether adding a new Meta Attack would keep the framework stratified
    // The parameters:
    //      a - The from argument of the new Meta Attack
    //      b - Any of the sides of the attack attacked by the meta attack
    public boolean isMetaAttackAllowed(String fromName, String toAttackName) {
        FrameworkArgument from = getArgumentByName(fromName);
        FrameworkAttack to = getAttackByName(toAttackName);
        if (from == null || to == null) return false;
        // Construct sets of all arguments connected to a and b via regular attacks
        ArrayList<FrameworkArgument> aRegion = getArgumentConnectedSet(from);

        for (FrameworkArgument a : aRegion) System.out.println(a.getName());

        if (aRegion.contains(to.getFrom())) return false;
        // a and b are in two different regions - make sure there are no meta attacks going the opposite direction
        ArrayList<FrameworkArgument> bRegion = getArgumentConnectedSet(to.getFrom());
        for (FrameworkMetaAttack metAt: metaAttacks) {
            if (bRegion.contains(metAt.getFrom()) && aRegion.contains(metAt.getTo().getFrom())) return false;
        }
        return true;
    }
}
