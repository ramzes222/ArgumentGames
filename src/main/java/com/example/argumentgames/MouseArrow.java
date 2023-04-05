package com.example.argumentgames;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.transform.Rotate;

// Produces a straight arrow that originates from a point, and always ends in the mouse
public class MouseArrow {

    private final Line arrowLine;
    private final Polygon arrowPointer;
    private final Rotate arrowRotate;
    private final Pane graphPane;

    public MouseArrow(double originX, double originY, Pane graphPane) {
        this.graphPane = graphPane;
        arrowLine = new Line(originX, originY, originX, originY);
        arrowLine.setStroke(new Color(0.3,0.3,0.3,0.3));
        arrowLine.setStrokeWidth(5);
        arrowLine.setMouseTransparent(true);

        // Build the pointing shape
        double[] shape = new double[] { 0,0,50,20,50,-20 };
        arrowPointer = new Polygon(shape);
        arrowPointer.setFill(new Color(0.3,0.3,0.3,0.5));
        arrowPointer.layoutXProperty().bind(arrowLine.endXProperty());
        arrowPointer.layoutYProperty().bind(arrowLine.endYProperty());
        arrowRotate = new Rotate( 0, 0, 0 );
        arrowPointer.getTransforms().add(arrowRotate);
        arrowPointer.setMouseTransparent(true);

        // Add elements to the Pane
        graphPane.getChildren().addAll(arrowLine, arrowPointer);
        arrowPointer.toFront();

        // Add event to follow the mouse
        graphPane.setOnMouseMoved(e -> {
            arrowLine.endXProperty().set(e.getX());
            arrowLine.setEndX(e.getX());
            arrowLine.endYProperty().set(e.getY());
            arrowLine.setEndY(e.getY());

            // Rotate the arrow towards the origin
            double x_diff = arrowLine.getStartX() - arrowLine.getEndX();
            double y_diff = arrowLine.getStartY() - arrowLine.getEndY();
            if (x_diff == 0) x_diff = 1;
            double tan = y_diff / x_diff;
            double degree = Math.toDegrees(Math.atan(tan));
            if (x_diff < 0) degree = degree + 180;
            arrowRotate.setAngle(degree);
        });
    }

    public void delete() { graphPane.getChildren().removeAll(arrowLine, arrowPointer); }
}
