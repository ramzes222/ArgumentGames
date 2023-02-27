package com.example.argumentgames;

import javafx.beans.property.DoubleProperty;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GraphNode {
    private String name;
    private StackPane stack = new StackPane();
    private double radius = 45;
    private Circle circle;
    private ArrayList<GraphArrow> connectedArrows = new ArrayList<>();

    public GraphNode(String n) {
        name = n;

        Random rand = new Random(System.currentTimeMillis());
        circle = new Circle();
        circle.setRadius(radius);
        circle.setFill(Color.color(rand.nextDouble(), rand.nextDouble(), rand.nextDouble()));

        Text text = new Text(name);
        stack.setId(name);
        stack.setTranslateX(-radius);
        stack.setTranslateY(-radius);
        stack.getChildren().addAll(circle,text);
    }

    public String getName() {
        return name;
    }

    public double[] getMiddle() {
        double[] coordinates = new double[2];
        coordinates[0] = stack.getLayoutX() + (radius/2);
        coordinates[1] = stack.getLayoutY() + (radius/2);

        return coordinates;
    }

    public DoubleProperty getCenterXProperty() { return circle.centerXProperty(); }

    public DoubleProperty getCenterYProperty() { return circle.centerYProperty(); }

    public StackPane getStack() {
        return stack;
    }

    public void addArrow(GraphArrow arr) {
        connectedArrows.add(arr);
    }
}
