package com.example.argumentgames;

import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphCircle extends StackPane implements GraphNode {
    private final String name;
    private final Text text;
    private final Circle circle;
    private final ArrayList<GraphArrow> connectedArrows = new ArrayList<>();
    private boolean isGameSelectEnabled = false;
    private final HashMap<String, Color> colorLookup;
    private String currVisual;

    public GraphCircle(String n, double radius, HashMap<String, Color> colorLookup) {
        super();
        this.colorLookup = colorLookup;
        name = n;

        circle = new Circle();
        circle.setRadius(radius);
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

        setVisual("base");
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

    public void makeGameSelectable() {
        setVisual("gameSelectable");
        setMouseTransparent(false);
        isGameSelectEnabled = true;
    }

    public void makeGameUnselectable() {
        disable();
        setMouseTransparent(true);
        isGameSelectEnabled = false;
    }

    public void reloadVisual() {setVisual(currVisual);}

    // Sets the visual style of the circle, according to the color lookup table
    public void setVisual(String s) {
        currVisual = s;
        switch (s) {
            case "base" -> {
                circle.setStroke(Color.TRANSPARENT);
                circle.setFill(colorLookup.get("argumentBaseColor"));
                text.setFill(Color.BLACK);
            }
            case "gameSelectable" -> {
                circle.setStroke(Color.TRANSPARENT);
                circle.setFill(colorLookup.get("attackingArgColor"));
                text.setFill(Color.BLACK);
            }
            case "gameAttackable" -> {
                circle.setStroke(colorLookup.get("attackingArgColor"));
                circle.setFill(colorLookup.get("attackedArgColor"));
                text.setFill(Color.BLACK);
            }
            case "selected" -> {
                circle.setStroke(colorLookup.get("selectionColor"));
                circle.setFill(colorLookup.get("argumentBaseColor"));
                text.setFill(Color.BLACK);
            }
        }
    }
    public void highlight(Color c) {circle.setStroke(c);}
    public void select() { setVisual("selected");}
    public void deselect() { circle.setStroke(Color.TRANSPARENT); }
    public void disable() { circle.setFill(Color.LIGHTGRAY); circle.setStroke(Color.DARKGRAY); text.setFill(Color.DARKGRAY);}
    public void enable() { setVisual("base"); text.setFill(Color.BLACK);}

    public void addArrow(GraphArrow arr) { connectedArrows.add(arr); }
    public void removeArrow(GraphArrow arr) { connectedArrows.remove(arr); }
    public ArrayList<GraphArrow> getConnectedArrows() { return connectedArrows; }
    public void rotateArrows() { for (GraphArrow arrow: connectedArrows) { arrow.rotateArrowShape(); } }

    public String getName() { return name; }
    public DoubleProperty getCenterXProperty() { return circle.centerXProperty(); }
    public DoubleProperty getCenterYProperty() { return circle.centerYProperty(); }

    public boolean isGameSelectEnabled() {return isGameSelectEnabled;}
    @Override
    public boolean isCircle() { return true; }
}
