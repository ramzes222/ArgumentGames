package com.example.argumentgames;

import javafx.scene.paint.Color;

import java.util.ArrayList;

// Class to hold the data of an argument
// Used in game trees
// Can only attack one other argument (parent in tree)
public class TreeArgument {
    private final String name;
    private final TreeArgument parent;
    private TreeCircle visualTCircle = null;
    private final ArrayList<TreeArgument> children = new ArrayList<>();
    private final ArrayList<TreeArgument> pastInBranch = new ArrayList<>();
    private int width;

    // Describes the argument being currently in or out
    // 0 - undecided
    // 1 - in
    // 2 - out
    private int state = 0;
    // Signifies whether the argument lays on the Proponent or Opponent layer
    private final boolean isPro;

    public TreeArgument(String name, TreeArgument parent) {
        this.parent = parent;
        if (parent != null) {
            parent.addChild(this);
            pastInBranch.addAll(parent.getPastInBranch()); pastInBranch.add(parent);
            this.isPro = !parent.isPro();
        } else {
            this.isPro = true;
        }
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

    public void appear() { visualTCircle.setVisible(true);}
    public void disappear() { visualTCircle.setVisible(false);}

    public void highlight() { visualTCircle.highlight(Color.ORANGE);}
    public void setColor(Color c) { visualTCircle.setColor(c);}
    public void dehighlight() { visualTCircle.highlight(Color.TRANSPARENT);}

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
    public ArrayList<TreeArgument> getPastInBranch() { return pastInBranch; }

    public void setState(int newState) {
        if (state != newState) {
            state = newState;
            if (parent != null) parent.updateState();
        }
    }

    public int getState() { return state; }

    // Rechecks what this argument's state should be
    // Always updates to 1 or 2 (0 is meant for nodes not yet added to the tree)
    public void updateState() {
        int newState = 1;
        for (TreeArgument a : children) {
            if (a.getState() == 1) {
                newState = 2;
                break;
            }
        }
        setState(newState);
    }

    public boolean isPro() {return  isPro;}

    public ArrayList<TreeArgument> getAllArguments() {
        ArrayList<TreeArgument> res = new ArrayList<>();
        res.add(this);
        for (TreeArgument arg : children) { res.addAll(arg.getAllArguments()); }
        return res;
    }

    public ArrayList<TreeArgument> getOfStateAndLayer(int stateRequested, boolean isProRequested) {
        ArrayList<TreeArgument> res = new ArrayList<>();
        if (this.state == stateRequested && this.isPro == isProRequested) res.add(this);
        for (TreeArgument arg : children) { res.addAll(arg.getOfStateAndLayer(stateRequested, isProRequested)); }
        return res;
    }
    public boolean pastInBranchIncludes(String name) {
        for (TreeArgument arg : pastInBranch) {
            if ( arg.getName().equals(name) ) return true;
        }
        return false;
    }
    public String getName() {return name; }
    public TreeArgument getParent() {return parent; }
    public int getWidth() { return width; }
    public boolean isLeaf() { return children.size() == 0; }
}
