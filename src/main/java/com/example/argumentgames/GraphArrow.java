package com.example.argumentgames;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

public class GraphArrow extends QuadCurve implements GraphNode {

    private final GraphCircle startNode, endNode;
    private final Circle controlPoint;
    private final Polygon arrow;
    private double dragOriginX, dragOriginY;
    private final Rotate arrowRotate;

    public GraphArrow(GraphCircle a, GraphCircle b) {
        startNode = a;
        endNode = b;

        //create Control Node at halfway point
        controlPoint = new Circle();
        controlPoint.setRadius(10);
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
            double newY = e.getSceneY() - dragOriginY;
            setControlPointXY(newX, newY);
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
        centerControlPoint();

        // Rotate the arrow tip
        rotateArrowShape();
    }

    // Place the control point halfway between the nodes
    public void centerControlPoint() {
        setControlPointXY( (startXProperty().get() + endXProperty().get()) / 2 , (startYProperty().get() + endYProperty().get()) / 2 );
    }

    public void translateControlPointXY(double xDiff, double yDiff) {
        double newY = yDiff + controlPoint.getLayoutY();
        double newX = xDiff + controlPoint.getLayoutX();
        controlPoint.layoutYProperty().add(yDiff); controlPoint.layoutXProperty().add(xDiff);
        controlPoint.setLayoutY(newY); controlPoint.setLayoutX(newX);
        rotateArrowShape();
    }

    public void setControlPointXY(double x, double y) {
        controlPoint.layoutYProperty().set(y); controlPoint.layoutXProperty().set(x);
        controlPoint.setLayoutY(y); controlPoint.setLayoutX(x);
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
    public void selectGray() {
        setStroke(Color.SLATEGRAY);
        controlPoint.setVisible(true);
        controlPoint.toFront();
    }

    public void deselectGray() {
        setStroke(Color.DARKGRAY);
        controlPoint.setVisible(false);
    }
    public void disable() { arrow.setFill(Color.DARKGRAY); setStroke(Color.DARKGRAY);}
    public void enable() { arrow.setFill(Color.FORESTGREEN); setStroke(Color.FORESTGREEN); }
    public void highlight() { arrow.setFill(Color.INDIANRED); setStroke(Color.INDIANRED); }

    public void rotateArrowShape() {
        double x_diff = controlPoint.getLayoutX() - getEndX();
        double y_diff = getEndY() - controlPoint.getLayoutY();
        arrowRotate.setAngle( -GeometricHelper.x_y_toAngle(x_diff, y_diff) );
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
    @Override
    public boolean isCircle() { return false; }
    public String getName() { return null; }
    public String getFromName() {return startNode.getName(); }
    public String getToName() {return endNode.getName(); }

    public void delete() {
        startNode.removeArrow(this);
        endNode.removeArrow(this);
    }
}
