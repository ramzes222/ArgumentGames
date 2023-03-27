package com.example.argumentgames;

import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class TreeCircle extends StackPane {
    private final String name;
    private double radius;
    private final Circle circle;
    private final ArrayList<TreeArrow> connectedArrows = new ArrayList<>();
    private boolean isGameSelectEnabled = false;
    // Signifies whether the argument lays on the Proponent or Opponent layer
    private boolean isPro;
    private HashMap<String, Color> colorLookup = new HashMap<>();
    private String currVisual;

    public TreeCircle(TreeArgument a, double radius, HashMap<String, Color> colorLookup) {
        super();
        this.isPro = a.isPro();
        this.radius = radius;
        this.colorLookup = colorLookup;

        a.setVisualTCircle(this);
        name = a.getName();
        circle = new Circle();
        circle.setRadius(radius);
        setVisual("base");
        circle.setStrokeWidth(5);
        Text text = new Text(name);
        //
        // Prepare the stack (self)
        setId(name);
        setTranslateX(-radius);
        setTranslateY(-radius);
        getChildren().addAll(circle,text);
    }

    public void translateXY(double xDiff, double yDiff) {
        double newY = yDiff + getLayoutY();
        double newX = xDiff + getLayoutX();
        getCenterYProperty().add(yDiff); getCenterXProperty().add(xDiff);
        setLayoutY(newY); setLayoutX(newX);
        rotateArrows();
    }

    public void moveToXY(double x, double y) {
        setLayoutX(x); setLayoutY(y);
    }

    public void setRadius(double r) {
        this.radius = r;
        setTranslateX(-radius);
        setTranslateY(-radius);
        circle.setRadius(radius);
        if (r < 10) { circle.setStrokeWidth(2); } else { circle.setStrokeWidth(5); }
    }

    public void reloadVisual() {setVisual(currVisual);}
    // Sets the visual style of the circle, according to the color lookup table
    public void setVisual(String s) {
        currVisual = s;
        switch (s){
            case "base":
                if (isPro) circle.setFill(colorLookup.get("proponentArgColor"));
                else circle.setFill(colorLookup.get("opponentArgColor"));
                circle.setStroke(Color.TRANSPARENT);
                break;
            case "gameSelectable":
                circle.setStroke(Color.TRANSPARENT);
                circle.setFill(colorLookup.get("attackingArgColor"));
                break;
            case "gameSelected":
                circle.setStroke(colorLookup.get("attackedArgColor"));
                circle.setFill(colorLookup.get("attackingArgColor"));
                break;
            case "computerSelectable":
                circle.setStroke(Color.TRANSPARENT);
                circle.setFill(colorLookup.get("computerSelectableColor"));
                break;
        }
    }

    public void makeGameSelectable() {
        setVisual("gameSelectable");
        setMouseTransparent(false);
        isGameSelectEnabled = true;
    }

    public void makeGameUnselectable() {
        setVisual("base");
        setMouseTransparent(true);
        isGameSelectEnabled = false;
    }

    public void highlight(Color c) { setVisual("base"); circle.setStroke(c); }
    public void select() {  }
    public void deselect() { setVisual("base"); }

    public void setDisplayVisible( boolean b) {
        setVisible(b);
        for (TreeArrow a : connectedArrows) { if (a.startNode == this) a.setDisplayVisible(b); }
    }

    public void addArrow(TreeArrow arr) { connectedArrows.add(arr); }
    public void rotateArrows() { for (TreeArrow arrow: connectedArrows) { arrow.rotateArrowShape(); } }

    public String getName() { return name; }

    public boolean isGameSelectEnabled() {return isGameSelectEnabled;}
    public DoubleProperty getCenterXProperty() { return circle.centerXProperty(); }
    public DoubleProperty getCenterYProperty() { return circle.centerYProperty(); }
}
