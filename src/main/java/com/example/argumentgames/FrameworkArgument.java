package com.example.argumentgames;

import java.util.ArrayList;

public class FrameworkArgument {

    private String name;
    private final ArrayList<FrameworkArgument> attacks = new ArrayList<>();
    private final ArrayList<FrameworkArgument> attackedBy = new ArrayList<>();

    public FrameworkArgument(String name) {
       this.name = name;
    }

    public String getName() { return name; }
    public void rename(String s) {this.name = s; }
    public void addToAttacks(FrameworkArgument arg) {attacks.add(arg);}
    public void addToAttackedBy(FrameworkArgument arg) {attackedBy.add(arg);}
    public void removeAttack(FrameworkArgument arg) {attacks.remove(arg);}
    public void removeAttackedBy(FrameworkArgument arg) {attackedBy.remove(arg);}
    public ArrayList<FrameworkArgument> getAttacks() { return attacks; }
    public ArrayList<FrameworkArgument> getAttackedBy() { return attackedBy; }

}
