package com.example.argumentgames;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;

import java.util.HashMap;

public class TreeArrow extends Line {
    // Unlike GraphArrows, the Tree Arrow is a straight line
    // Due to the layers of the tree, there's no need for curved arrows
    final TreeCircle startNode, endNode;
    private final Polygon arrow;
    private double dragOriginX, dragOriginY;
    private final Rotate arrowRotate;

    private HashMap<String, Color> colorLookup = new HashMap<>();
    private String currVisual;

    public TreeArrow(TreeCircle a, TreeCircle b, HashMap<String, Color> colorLookup) {
        startNode = a;
        endNode = b;
        this.colorLookup = colorLookup;

        // Setup the line
        startXProperty().bind(startNode.layoutXProperty());
        startYProperty().bind(startNode.layoutYProperty());
        endXProperty().bind(endNode.layoutXProperty());
        endYProperty().bind(endNode.layoutYProperty());
        setStrokeWidth(4);
        setStrokeLineCap(StrokeLineCap.ROUND);
        toBack();

        // Setup the arrow tip
        double[] shape = new double[] { 15,0,40,15,40,-15 };
        arrow = new Polygon(shape);
        arrow.layoutXProperty().bind(endNode.layoutXProperty());
        arrow.layoutYProperty().bind(endNode.layoutYProperty());
        arrow.toBack();
        arrowRotate = new Rotate( 0, 0, 0 );
        arrow.getTransforms().add(arrowRotate);

        // Save self to nodes
        a.addArrow(this);
        b.addArrow(this);

        // Rotate the arrow tip
        rotateArrowShape();

        setVisual("base");
    }

    public void reloadVisual() {setVisual(currVisual);}
    // Sets the visual style of the circle, according to the color lookup table
    public void setVisual(String s) {
        currVisual = s;
        switch (s){
            case "base":
                setStroke(colorLookup.get("attackArrowColor"));
                arrow.setFill(colorLookup.get("attackArrowColor"));
                break;
            case "selected":
                setStroke(colorLookup.get("selectionColor"));
                arrow.setFill(colorLookup.get("selectionColor"));
                break;
        }
    }

    public void rotateArrowShape() {
        double x_diff = getStartX() - getEndX();
        double y_diff = getEndY() - getStartY();
        arrowRotate.setAngle( -GeometricHelper.x_y_toAngle(x_diff, y_diff) );
    }

    public Polygon getArrowTip() { return arrow; }

    public void setDisplayVisible(boolean b) {
        setMouseTransparent(true);
        setVisible(b);
        arrow.setVisible(b);
        arrow.setMouseTransparent(true);
    }
}
