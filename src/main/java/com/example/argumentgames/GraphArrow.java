package com.example.argumentgames;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

public class GraphArrow extends QuadCurve implements GraphNode {

    private GraphCircle startNode, endNode;
    private Circle controlPoint;
    private Polygon arrow;
    private double dragOriginX, dragOriginY;
    private Rotate arrowRotate;

    public GraphArrow(GraphCircle a, GraphCircle b) {
        startNode = a;
        endNode = b;

        //create Control Node at halfway point
        controlPoint = new Circle();
        controlPoint.setRadius(10);
        //controlPoint.setTranslateX(-10);
        //controlPoint.setTranslateY(-10);
        controlPoint.setFill(Color.ALICEBLUE);
        controlPoint.setVisible(false);

        //make the Control Node draggable
        controlPoint.setOnMousePressed(e -> {
            controlPoint.toFront();
            dragOriginX = e.getSceneX() - controlPoint.getLayoutX();
            dragOriginY = e.getSceneY() - controlPoint.getLayoutY();
        });
        controlPoint.setOnMouseDragged(e -> {
            double newX = e.getSceneX() - dragOriginX;
            controlPoint.setLayoutX(newX);
            double newY = e.getSceneY() - dragOriginY;
            controlPoint.setLayoutY(newY);
            rotateArrowShape();
        });

        // Setup the curve
        startXProperty().bind(startNode.layoutXProperty());
        startYProperty().bind(startNode.layoutYProperty());
        endXProperty().bind(endNode.layoutXProperty());
        endYProperty().bind(endNode.layoutYProperty());
        controlXProperty().bind(controlPoint.layoutXProperty());
        controlYProperty().bind(controlPoint.layoutYProperty());
        setStroke(Color.FORESTGREEN);
        setStrokeWidth(4);
        setStrokeLineCap(StrokeLineCap.ROUND);
        setFill(Color.TRANSPARENT);
        toBack();

        // Setup the arrow tip
        double[] shape = new double[] { 20,0,60,20,60,-20 };
        arrow = new Polygon(shape);
        arrow.setFill(Color.FORESTGREEN);
        arrow.layoutXProperty().bind(endNode.layoutXProperty());
        arrow.layoutYProperty().bind(endNode.layoutYProperty());
        arrow.toBack();
        arrowRotate = new Rotate( 0, 0, 0 );
        arrow.getTransforms().add(arrowRotate);

        // Place the control point halfway between the nodes
        controlPoint.setLayoutX( (startXProperty().get() + endXProperty().get()) / 2 );
        controlPoint.setLayoutY( (startYProperty().get() + endYProperty().get()) / 2 );

        // Rotate the arrow tip
        rotateArrowShape();
    }

    public void select() {
        setStroke(Color.YELLOW);
        controlPoint.setVisible(true);
        controlPoint.toFront();
    }

    public void deselect() {
        setStroke(Color.FORESTGREEN);
        controlPoint.setVisible(false);
    }

    public void rotateArrowShape() {
        double x_diff = controlPoint.getLayoutX() - getEndX();
        double y_diff = getEndY() - controlPoint.getLayoutY();
        if (x_diff == 0) x_diff = 1;
        double tan = y_diff / x_diff;
        double degree = -Math.toDegrees(Math.atan(tan));
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
    public Circle getControlPoint() { return controlPoint; }
}
