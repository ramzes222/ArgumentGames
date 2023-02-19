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
    private Node controlNode;
    private QuadCurve curve;
    private Polygon arrow;

    public GraphArrow(GraphNode a, GraphNode b) {
        startNode = a;
        endNode = b;
        double[] shape = new double[] { 0,0,10,15,-10,15 };
        arrow = new Polygon(shape);
        arrow.setFill(Color.PALEGOLDENROD);

        //create Control Node
        Circle controlNode = new Circle();
        controlNode.setRadius(20);
        controlNode.setFill(Color.AZURE);
        controlNode.setLayoutX(100);
        controlNode.setLayoutY(200);
        controlNode.setTranslateX(-20);
        controlNode.setTranslateY(-20);

        curve = new QuadCurve();
        curve.startXProperty().bind(startNode.getCenterXProperty());
        curve.startYProperty().bind(startNode.getCenterYProperty());
        curve.endXProperty().bind(endNode.getCenterXProperty());
        curve.endYProperty().bind(endNode.getCenterYProperty());

        curve.setControlX(controlNode.getLayoutX());
        curve.setControlY(controlNode.getLayoutY());
        curve.setStroke(Color.FORESTGREEN);
        curve.setStrokeWidth(4);
        curve.setStrokeLineCap(StrokeLineCap.ROUND);
        curve.setFill(Color.TRANSPARENT);
    }

    public void moveArrowPoint(int startOrEnd, Double xTrans, Double yTrans) {
        if (startOrEnd == 0) {
            curve.setStartX(xTrans);
            curve.setStartY(yTrans);
        } else {
            curve.setEndX(xTrans);
            curve.setEndY(yTrans);
        }

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
