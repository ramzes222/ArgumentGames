package com.example.argumentgames;

import java.util.ArrayList;

// Class to hold the data of an argument
// Used in game trees
// Can only attack one other argument (parent in tree)
public class TreeArgument {
    String name;
    TreeArgument parent;
    ArrayList<TreeArgument> children = new ArrayList<>();
    int width;

    public TreeArgument(String name, TreeArgument parent) {
        this.parent = parent;
        if (parent != null) parent.addChild(this);
        this.name = name;
        width = 1;
    }

    public void updateWidth() {
        int nonLeafChildSum = 0;
        for (TreeArgument arg: children) {
            if (!arg.isLeaf()) { nonLeafChildSum += arg.getWidth(); }
        }
        int newWidth = Math.max(nonLeafChildSum, children.size());
        if (newWidth != width) { width = newWidth; if (parent != null) parent.updateWidth(); }
    }

    public void addChild(TreeArgument t) { children.add(t); updateWidth(); }

    public ArrayList<TreeArgument> getChildren() { return children; }
    public String getName() {return name; }
    public int getWidth() { return width; }
    public boolean isLeaf() { return children.size() == 0; }
}
