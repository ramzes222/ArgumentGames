package com.example.argumentgames;

import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.Random;

public class GraphCircle extends StackPane implements GraphNode {
    private final String name;
    private Text text;
    private double radius;
    private final Circle circle;
    private final ArrayList<GraphArrow> connectedArrows = new ArrayList<>();
    private boolean isGameSelectEnabled = false;

    public GraphCircle(String n, double radius) {
        super();
        this.radius = radius;
        name = n;

        circle = new Circle();
        circle.setRadius(radius);
        //circle.setFill(Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        circle.setFill( Color.CORNSILK );
        circle.setStrokeWidth(5);
        text = new Text(name);
        text.setFill(Color.BLACK);
        text.setStyle("-fx-font: 20 arial;");
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

    public void setXY(double x, double y) {
        getCenterYProperty().set(y); getCenterXProperty().set(x);
        setLayoutY(y); setLayoutX(x);
        rotateArrows();
    }

    public void setRadius(double r) {
        this.radius = r;
        setTranslateX(-radius);
        setTranslateY(-radius);
        circle.setRadius(radius);
        if (r < 10) { circle.setStrokeWidth(2); }
    }

    public void makeGameSelectable() {
        gameSelectable();
        setMouseTransparent(false);
        isGameSelectEnabled = true;
    }

    public void makeGameUnselectable() {
        disable();
        setMouseTransparent(true);
        isGameSelectEnabled = false;
    }

    public void highlight(Color c) { circle.setStroke(c); }
    public void baseVisual() {
        circle.setStroke(Color.TRANSPARENT); circle.setFill(Color.CORNSILK); text.setFill(Color.BLACK); }
    public void gameSelectable() { circle.setStroke(Color.TRANSPARENT); circle.setFill(Color.PEACHPUFF); text.setFill(Color.BLACK);}
    public void gameAttackable() { circle.setStroke(Color.PERU); circle.setFill(Color.LIGHTCORAL); text.setFill(Color.BLACK);}
    public void select() { highlight(Color.YELLOW); }
    public void deselect() { highlight(Color.TRANSPARENT); }
    public void disable() { circle.setFill(Color.LIGHTGRAY); circle.setStroke(Color.DARKGRAY); text.setFill(Color.DARKGRAY);}
    public void enable() { baseVisual(); text.setFill(Color.BLACK);}

    public void addArrow(GraphArrow arr) { connectedArrows.add(arr); }
    public void rotateArrows() { for (GraphArrow arrow: connectedArrows) { arrow.rotateArrowShape(); } }

    public String getName() { return name; }
    public DoubleProperty getCenterXProperty() { return circle.centerXProperty(); }
    public DoubleProperty getCenterYProperty() { return circle.centerYProperty(); }

    public boolean isGameSelectEnabled() {return isGameSelectEnabled;}
    @Override
    public boolean isCircle() { return true; }
}
