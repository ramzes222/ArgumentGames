package com.example.argumentgames;

public class FrameworkArgument {

    double prefX = 0,prefY = 0;

    private final String name;

    public FrameworkArgument(String name) {
       this.name = name;
    }
    public FrameworkArgument(String name, double x, double y) {
        this.name = name; this.prefX = x; this.prefY = y;
    }

    public String getName() { return name; }
    // public ArrayList<FrameworkAttack> getAttacks() { return attacks; }


}
