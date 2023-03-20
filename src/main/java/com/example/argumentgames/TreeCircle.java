package com.example.argumentgames;

import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Random;

public class TreeCircle extends StackPane {
    private final String name;
    private double radius;
    private final Circle circle;
    private final ArrayList<TreeArrow> connectedArrows = new ArrayList<>();
    private boolean isGameSelectEnabled = false;

    public TreeCircle(String n, double radius) {
        super();
        this.radius = radius;
        name = n;
        circle = new Circle();
        circle.setRadius(radius);
        circle.setFill( Color.CORNSILK );
        circle.setStrokeWidth(5);
        Text text = new Text(name);
        //
        // Prepare the stack (self)
        setId(name);
        setTranslateX(-radius);
        setTranslateY(-radius);
        getChildren().addAll(circle,text);
    }

    public TreeCircle(TreeArgument a, double radius) {
        super();
        this.radius = radius;
        a.setVisualTCircle(this);
        name = a.getName();
        circle = new Circle();
        circle.setRadius(radius);
        circle.setFill( Color.CORNSILK );
        circle.setStrokeWidth(5);
        Text text = new Text(name);
        //
        // Prepare the stack (self)
        setId(name);
        setTranslateX(-radius);
        setTranslateY(-radius);
        getChildren().addAll(circle,text);
    }

    public void makeGameSelectable() {
        gameSelectable();
        setMouseTransparent(false);
        isGameSelectEnabled = true;
    }

    public void makeGameUnselectable() {
        baseVisual();
        setMouseTransparent(true);
        isGameSelectEnabled = false;
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

    public void highlight(Color c) { circle.setStroke(c); circle.setFill(Color.CORNSILK);}
    public void baseVisual() { circle.setStroke(Color.TRANSPARENT); circle.setFill(Color.CORNSILK); }
    public void gameSelected() { circle.setStroke(Color.DARKRED); circle.setFill(Color.INDIANRED);}
    public void gameSelectable() { circle.setStroke(Color.TRANSPARENT); circle.setFill(Color.INDIANRED);}
    public void select() { highlight(Color.YELLOW); }
    public void deselect() { baseVisual(); }
    public void setColor(Color c) {circle.setFill(c); circle.setStroke(Color.TRANSPARENT); }

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
