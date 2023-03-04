package com.example.argumentgames;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

public class GraphArrow extends QuadCurve implements GraphNode {

    private GraphCircle startNode, endNode;
    private Circle controlNode;
    private Polygon arrow;
    private double dragOriginX, dragOriginY;
    private Rotate arrowRotate;

    public GraphArrow(GraphCircle a, GraphCircle b) {
        startNode = a;
        endNode = b;

        //create Control Node
        controlNode = new Circle();
        controlNode.setRadius(20);
        controlNode.setFill(Color.AZURE);
        controlNode.setLayoutX(100);
        controlNode.setLayoutY(200);
        controlNode.setTranslateX(-20);
        controlNode.setTranslateY(-20);
        controlNode.setVisible(false);

        //make the Control Node draggable
        controlNode.setOnMousePressed(e -> {
            controlNode.toFront();
            dragOriginX = e.getSceneX() - controlNode.getLayoutX();
            dragOriginY = e.getSceneY() - controlNode.getLayoutY();
        });
        controlNode.setOnMouseDragged(e -> {
            double newX = e.getSceneX() - dragOriginX;
            controlNode.setLayoutX(newX);
            double newY = e.getSceneY() - dragOriginY;
            controlNode.setLayoutY(newY);
        });

        // Setup the curve
        startXProperty().bind(startNode.layoutXProperty());
        startYProperty().bind(startNode.layoutYProperty());
        endXProperty().bind(endNode.layoutXProperty());
        endYProperty().bind(endNode.layoutYProperty());
        controlXProperty().bind(controlNode.layoutXProperty());
        controlYProperty().bind(controlNode.layoutYProperty());
        setStroke(Color.FORESTGREEN);
        setStrokeWidth(4);
        setStrokeLineCap(StrokeLineCap.ROUND);
        setFill(Color.TRANSPARENT);
        toBack();

        // Setup the arrow tip
        double[] shape = new double[] { 20,0,70,20,70,-20 };
        arrow = new Polygon(shape);
        arrow.setFill(Color.FORESTGREEN);
        arrow.layoutXProperty().bind(endNode.layoutXProperty());
        arrow.layoutYProperty().bind(endNode.layoutYProperty());
        arrow.toBack();
        arrowRotate = new Rotate( 0, 0, 0 );
        arrow.getTransforms().add(arrowRotate);

        // Put the connected nodes back in front
        a.toFront();
        b.toBack();
    }

    public void select() {
        setStroke(Color.YELLOW);
        controlNode.setVisible(true);
        controlNode.toFront();
    }

    public void deselect() {
        setStroke(Color.FORESTGREEN);
        controlNode.setVisible(false);
    }

    public void rotateArrowShape() {
        double x_diff = controlNode.getLayoutX() - getEndX();
        double y_diff = getEndY() - controlNode.getLayoutY();
        if (x_diff == 0) x_diff = 1;
        double tan = y_diff / x_diff;
        // double tan = 2.0 *
        double degree = -Math.toDegrees(Math.atan(tan));
        System.out.println("Tan: " + tan + "   Degree: " + degree);
        if (x_diff < 0) degree = degree + 180;
        arrowRotate.setAngle(degree);
    }

    // Determines if the arrow is connected to a node
    // Returns 0 if it's the start, 1 if the end
    // Returns -1 if the Node is not connected at all
    public int getConnectionType(GraphCircle n) {
        if (startNode == n) return 0;
        if (endNode == n) return 1;
        return -1;
    }

    public Polygon getArrowTip() { return arrow; }
}
