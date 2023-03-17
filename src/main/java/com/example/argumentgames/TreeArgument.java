package com.example.argumentgames;

import java.util.ArrayList;

// Class to hold the data of an argument
// Used in game trees
// Can only attack one other argument (parent in tree)
public class TreeArgument {
    private final String name;
    private final TreeArgument parent;
    private TreeCircle visualTCircle = null;
    private final ArrayList<TreeArgument> children = new ArrayList<>();
    private int width;

    public TreeArgument(String name, TreeArgument parent) {
        this.parent = parent;
        if (parent != null) parent.addChild(this);
        this.name = name;
        width = 1;
    }

    public void updateWidth() {
        int nonLeafChildSum = 0;
        for (TreeArgument child: children) {
            // Tell child to update themselves
            child.updateWidth();
            // Then sum it
            if (!child.isLeaf()) { nonLeafChildSum += child.getWidth(); }
        }
        width = Math.max(nonLeafChildSum, children.size());
    }

    // Clears the visualTCircles for self and all children
    // Via recurrence, clears the whole tree
    public void clearVisualTCircles() {
        visualTCircle = null;
        for (TreeArgument child: children) {child.clearVisualTCircles();}
    }

    public void setVisualTCircle(TreeCircle c) {visualTCircle = c;}
    public TreeCircle getVisualTCircle() {return visualTCircle;}
    public void addChild(TreeArgument t) { children.add(t); }

    public ArrayList<TreeArgument> getChildren() { return children; }
    public String getName() {return name; }
    public TreeArgument getParent() {return parent; }
    public int getWidth() { return width; }
    public boolean isLeaf() { return children.size() == 0; }
}
