package com.example.argumentgames;

import java.util.ArrayList;

public class FrameworkArgument {

    double prefX = 0,prefY = 0;

    private String name;
    private final ArrayList<FrameworkAttack> attacks = new ArrayList<>();

    public FrameworkArgument(String name) {
       this.name = name;
    }
    public FrameworkArgument(String name, double x, double y) {
        this.name = name; this.prefX = x; this.prefY = y;
    }

    public String getName() { return name; }
    public void rename(String s) {this.name = s; }
    public void addToAttacks(FrameworkAttack att) { attacks.add(att);}
    public void removeAttack(FrameworkAttack att) { if (att!=null) attacks.remove(att);}
    public ArrayList<FrameworkAttack> getAttacks() { return attacks; }
    public ArrayList<FrameworkArgument> getAttackedBy() {
        ArrayList<FrameworkArgument> res = new ArrayList<>();
        for (FrameworkAttack att: attacks) {
            if (att.getTo() == this) res.add(att.getFrom());
        }
        return res;
    }

}
