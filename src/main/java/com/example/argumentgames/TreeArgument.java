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
    // Signifies whether the argument lies on the Proponent or Opponent layer
    private final boolean isPro;

    boolean isInWinningStrategy = false;

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

    public void highlight() { visualTCircle.highlight(Color.ORANGE);}
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

    public void resetState() {
        state = 0;
        for (TreeArgument child : children) {child.resetState();}
    }

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
    public ArrayList<TreeArgument> getOfLayer(boolean isProRequested) {
        ArrayList<TreeArgument> res = new ArrayList<>();
        if (this.isPro == isProRequested) res.add(this);
        for (TreeArgument arg : children) { res.addAll(arg.getOfLayer(isProRequested)); }
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

    // Updates the isInWinningStrategy to show
    // Whether this argument is part of proponent's winning strategy
    // We provide a "guess" about the parent to inform our choice
    // As an arg cannot be inStrat when it's parent is out,
    // we cut off whole branches when we determine something to be out
    public void updateWinningStrategy() {
        // Is it pro turn argument?
        if (isPro) {
            if (children.isEmpty()) {
                isInWinningStrategy = true;
            } else {
                // Update the children
                for (TreeArgument childArg : children) { childArg.updateWinningStrategy(); }
                // If any of the children are not inStrat, this has to be out
                for (TreeArgument childArg : children) {
                    if (!childArg.isInWinningStrategy) { cutOffStrategyBranch(); return; }
                }
                // All children are inStrat - so this has to be in strat
                isInWinningStrategy = true;
            }
        } else {
            // Easiest case - if opp turn and no children, it's always out
            if (children.isEmpty()) {
                cutOffStrategyBranch();
            } else {
                // Update the children
                for (TreeArgument childArg : children) { childArg.updateWinningStrategy(); }
                // If any of the children are inStrat, this is in
                for (TreeArgument childArg : children) {
                    if (childArg.isInWinningStrategy) { isInWinningStrategy = true; return;}
                }
                // Otherwise, all pro children are out, so this has to be out
                cutOffStrategyBranch();
            }
        }
    }

    // Removes the argument and all it's children from the winning strategy
    private void cutOffStrategyBranch() {
        isInWinningStrategy = false;
        for (TreeArgument arg : children) arg.cutOffStrategyBranch();
    }

}
