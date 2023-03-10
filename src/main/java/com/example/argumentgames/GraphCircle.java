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
    private double radius;
    private final Circle circle;
    private final ArrayList<GraphArrow> connectedArrows = new ArrayList<>();

    public GraphCircle(String n, double radius) {
        super();
        this.radius = radius;
        name = n;

        Random rand = new Random(System.currentTimeMillis());
        circle = new Circle();
        circle.setRadius(radius);
        //circle.setFill(Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));
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

    public void setRadius(double r) {
        this.radius = r;
        setTranslateX(-radius);
        setTranslateY(-radius);
        circle.setRadius(radius);
        if (r < 10) { circle.setStrokeWidth(2); }
    }

    public void highlight(Color c) { circle.setStroke(c); }
    public void dehighlight() { circle.setStroke(Color.TRANSPARENT); }
    public void select() { highlight(Color.YELLOW); }
    public void deselect() { dehighlight(); }

    public void addArrow(GraphArrow arr) { connectedArrows.add(arr); }
    public void rotateArrows() { for (GraphArrow arrow: connectedArrows) { arrow.rotateArrowShape(); } }

    public String getName() { return name; }
    public DoubleProperty getCenterXProperty() { return circle.centerXProperty(); }
    public DoubleProperty getCenterYProperty() { return circle.centerYProperty(); }
}
