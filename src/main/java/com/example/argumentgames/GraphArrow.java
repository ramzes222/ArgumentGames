package com.example.argumentgames;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Text;

public class GraphArrow {

    private GraphNode startNode, endNode;
    private Circle controlNode;
    private QuadCurve curve;
    private Polygon arrow;
    private double dragOriginX, dragOriginY;

    public GraphArrow(GraphNode a, GraphNode b) {
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

        curve = new QuadCurve();
        curve.startXProperty().bind(startNode.getCenterXProperty());
        curve.startYProperty().bind(startNode.getCenterYProperty());
        curve.endXProperty().bind(endNode.getCenterXProperty());
        curve.endYProperty().bind(endNode.getCenterYProperty());
        curve.controlXProperty().bind(controlNode.layoutXProperty());
        curve.controlYProperty().bind(controlNode.layoutYProperty());
        curve.setStroke(Color.FORESTGREEN);
        curve.setStrokeWidth(4);
        curve.setStrokeLineCap(StrokeLineCap.ROUND);
        curve.setFill(Color.TRANSPARENT);
    }

    public void selectArrow() {
        curve.setStroke(Color.YELLOW);
        controlNode.setVisible(true);
        controlNode.toFront();
    }

    public void deselectArrow() {
        curve.setStroke(Color.FORESTGREEN);
        controlNode.setVisible(false);
    }

    public void moveArrowPoint(int startOrEnd, Double xTrans, Double yTrans) {
        if (startOrEnd == 0) {
            curve.setStartX(xTrans);
            curve.setStartY(yTrans);
        } else {
            curve.setEndX(xTrans);
            curve.setEndY(yTrans);
            arrow.setLayoutY(yTrans);
            arrow.setLayoutX(xTrans);
        }
        rotateArrowShape();
    }

    private void rotateArrowShape() {
        double x_diff = controlNode.getLayoutX() - curve.getEndX();
        double y_diff = curve.getEndY() - controlNode.getLayoutY();
        if (x_diff == 0) x_diff = 1;
        double tan = y_diff / x_diff;
        double degree = -Math.toDegrees(Math.atan(tan));
        if (x_diff < 0) degree = degree + 180;
        arrow.setRotate(degree);
    }

    // Determines if the arrow is connected to a node
    // Returns 0 if it's the start, 1 if the end
    // Returns -1 if the Node is not connected at all
    public int getConnectionType(GraphNode n) {
        if (startNode == n) return 0;
        if (endNode == n) return 1;
        return -1;
    }

    public QuadCurve getCurve() {
        return curve;
    }
    public Node[] getNodes() { Node[] r = new Node[] {arrow, curve}; return r; }
}
