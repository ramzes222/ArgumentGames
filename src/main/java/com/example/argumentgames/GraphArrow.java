package com.example.argumentgames;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

import java.util.ArrayList;
import java.util.HashMap;

public class GraphArrow extends QuadCurve implements GraphNode {

    private final GraphCircle startNode, endNode;
    private final Circle controlPoint, midPoint;
    private final Polygon arrow;
    private double dragOriginX, dragOriginY;
    private final Rotate arrowRotate;
    private final ArrayList<GraphMetaArrow> connectedMetaArrows = new ArrayList<>();
    private final HashMap<String, Color> colorLookup;
    private String currVisual;

    public GraphArrow(GraphCircle a, GraphCircle b, HashMap<String, Color> colorLookup) {
        startNode = a;
        endNode = b;
        this.colorLookup = colorLookup;

        //create Control Node at halfway point
        controlPoint = new Circle();
        controlPoint.setRadius(10);
        controlPoint.setVisible(false);

        //create Test Node at halfway point
        midPoint = new Circle();
        midPoint.setRadius(10);
        midPoint.setVisible(false);
        midPoint.setFill(colorLookup.get("attackControlColor"));

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

        // Set up the curve
        startXProperty().bind(startNode.layoutXProperty());
        startYProperty().bind(startNode.layoutYProperty());
        endXProperty().bind(endNode.layoutXProperty());
        endYProperty().bind(endNode.layoutYProperty());
        controlXProperty().bind(controlPoint.layoutXProperty());
        controlYProperty().bind(controlPoint.layoutYProperty());
        setStrokeWidth(4);
        setStrokeLineCap(StrokeLineCap.ROUND);
        setFill(Color.TRANSPARENT);
        toBack();

        // Set up the arrow tip
        double[] shape = new double[] { 20,0,60,20,60,-20 };
        arrow = new Polygon(shape);
        arrow.layoutXProperty().bind(endNode.layoutXProperty());
        arrow.layoutYProperty().bind(endNode.layoutYProperty());
        arrow.toBack();
        arrowRotate = new Rotate( 0, 0, 0 );
        arrow.getTransforms().add(arrowRotate);

        setVisual("base");

        // Place the control point halfway between the nodes
        centerControlPoint();

        // Rotate the arrow tip
        rotateArrowShape();
    }

    public void reloadVisual() {setVisual(currVisual);}
    public void setVisual(String s) {
        currVisual = s;
        switch (s) {
            case "base" -> {
                controlPoint.setFill(colorLookup.get("attackControlColor"));
                setStroke(colorLookup.get("attackArrowColor"));
                arrow.setFill(colorLookup.get("attackArrowColor"));
            }
            case "selected" -> {
                controlPoint.setFill(colorLookup.get("attackControlColor"));
                setStroke(colorLookup.get("selectionColor"));
                arrow.setFill(colorLookup.get("attackArrowColor"));
            }
            case "disabledSelect" -> {
                controlPoint.setFill(colorLookup.get("attackControlColor"));
                setStroke(Color.SLATEGRAY);
                arrow.setFill(Color.SLATEGRAY);
            }
            case "disabledBase" -> {
                arrow.setFill(Color.DARKGRAY);
                setStroke(Color.DARKGRAY);
                controlPoint.setFill(colorLookup.get("attackControlColor"));
            }
            case "attacking" -> {
                controlPoint.setFill(colorLookup.get("attackControlColor"));
                setStroke(colorLookup.get("attackingArgColor"));
                arrow.setFill(colorLookup.get("attackingArgColor"));
            }
        }
    }

    // Place the control point halfway between the nodes
    public void centerControlPoint() {
        setControlPointXY( (startXProperty().get() + endXProperty().get()) / 2 , (startYProperty().get() + endYProperty().get()) / 2 );
    }

    public void setCenterPoint() {
        double centerX = 0.25*startXProperty().get() + 0.5*controlPoint.getLayoutX() + 0.25*endXProperty().get();
        double centerY = 0.25*startYProperty().get() + 0.5*controlPoint.getLayoutY() + 0.25*endYProperty().get();
        midPoint.setLayoutX(centerX);
        midPoint.setLayoutY(centerY);
        for (GraphMetaArrow metArr: connectedMetaArrows) { metArr.rotateArrowShape(); }
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
        setVisual("selected");
        controlPoint.setVisible(true);
        controlPoint.toFront();
    }

    public void deselect() {
        setVisual("base");
        controlPoint.setVisible(false);
    }
    public void selectGray() {
        setVisual("disabledSelect");
        controlPoint.setVisible(true);
        controlPoint.toFront();
    }

    public void deselectGray() {
        setVisual("disabledBase");
        controlPoint.setVisible(false);
    }
    public void disable() { setVisual("disabledBase");}
    public void enable() { setVisual("base"); }
    public void highlight() { setVisual("attacking"); }

    public void rotateArrowShape() {
        double x_diff = controlPoint.getLayoutX() - getEndX();
        double y_diff = getEndY() - controlPoint.getLayoutY();
        arrowRotate.setAngle( -GeometricHelper.x_y_toAngle(x_diff, y_diff) );
        setCenterPoint();
    }

    public Polygon getArrowTip() { return arrow; }
    public Circle getControlPoint() { return controlPoint; }
    public Circle getMidPoint() { return midPoint; }
    @Override
    public boolean isCircle() { return false; }
    public String getName() { return  startNode.getName() + "->" + endNode.getName(); }
    public String getFromName() {return startNode.getName(); }
    public String getToName() {return endNode.getName(); }

    public void addMetaArrow(GraphMetaArrow arr) {
        connectedMetaArrows.add(arr);
        midPoint.setVisible(true);
        midPoint.toFront();
    }
    public void removeMetaArrow(GraphMetaArrow arr) {
        connectedMetaArrows.remove(arr);
        if (connectedMetaArrows.size()==0) midPoint.setVisible(false);
    }

    public void delete() {
        startNode.removeArrow(this);
        endNode.removeArrow(this);
        for (GraphMetaArrow a: connectedMetaArrows) {a.delete();}
    }
}
