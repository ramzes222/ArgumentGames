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
    private final double radius = 45;
    private final Circle circle;
    private final ArrayList<GraphArrow> connectedArrows = new ArrayList<>();

    public GraphCircle(String n) {
        super();
        name = n;

        Random rand = new Random(System.currentTimeMillis());
        circle = new Circle();
        circle.setRadius(radius);
        //circle.setFill(Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
        circle.setFill( Color.CORNSILK );
        circle.setStrokeWidth(5);

        Text text = new Text(name);

        // Prepare the stack (self)
        setId(name);
        setTranslateX(-radius);
        setTranslateY(-radius);
        getChildren().addAll(circle,text);
    }

    public void highlight() { circle.setStroke(Color.YELLOW); }
    public void dehighlight() { circle.setStroke(Color.TRANSPARENT); }
    public void select() {
        highlight();
    }
    public void deselect() {
        dehighlight();
    }
    public String getName() { return name; }

    public DoubleProperty getCenterXProperty() { return circle.centerXProperty(); }
    public DoubleProperty getCenterYProperty() { return circle.centerYProperty(); }
    public void addArrow(GraphArrow arr) { connectedArrows.add(arr); }

    public void rotateArrows() {
        for (GraphArrow arrow: connectedArrows) { arrow.rotateArrowShape(); }
    }
}
