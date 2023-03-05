package com.example.argumentgames;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

public class Graph {
    @FXML
    private Button selectButton;

    private double dragOriginX = 0;
    private double dragOriginY = 0;
    private double width, height;
    private ArrayList<GraphCircle> nodes = new ArrayList<>();
    private ArrayList<GraphArrow> arrows = new ArrayList<>();
    private GraphNode selected = null;
    private MainController.InteractMode interactMode = MainController.InteractMode.SELECT_MODE;

    Polygon arrow;

    public Graph(Pane graphPane) {
        this.width = graphPane.getPrefWidth();
        this.height = graphPane.getPrefHeight();

        // Construct the menu Bar
        // Select, Move mode buttons
        ToggleGroup modeToggleGroup = new ToggleGroup();
        RadioButton selectModeButton = new RadioButton("selectModeButton");
        selectModeButton.getStyleClass().remove("radio-button");
        selectModeButton.getStyleClass().add("toggle-button");
        selectModeButton.setToggleGroup(modeToggleGroup);
        System.out.println(System.getProperty("user.dir"));
        Image selectIcon = new Image(getClass().getResourceAsStream("img\\Select.png"));
        ImageView selectImage = new ImageView(selectIcon);
        selectImage.setLayoutX(600);
        selectImage.setLayoutY(400);
        selectImage.prefHeight(400);
        selectImage.prefWidth(400);
        selectImage.setVisible(true);
        graphPane.getChildren().add(selectImage);
        selectModeButton.setGraphic(selectImage);

        RadioButton moveModeButton = new RadioButton("moveModeButton");
        moveModeButton.getStyleClass().remove("radio-button");
        moveModeButton.getStyleClass().add("toggle-button");
        moveModeButton.setToggleGroup(modeToggleGroup);


        HBox menuHBox = new HBox(selectModeButton, moveModeButton);
        graphPane.getChildren().add(menuHBox);
    }

    public void setInteractMode(MainController.InteractMode m) {
        if (m == MainController.InteractMode.SELECT_MODE) {
            interactMode = m;
        }
    }

    public void addNode(String name, Pane graphPane) {
        GraphCircle n = new GraphCircle(name);
        Random rand = new Random(System.currentTimeMillis());
        //stack.setLayoutX(rand.nextDouble(500));
        //stack.setLayoutY(rand.nextDouble(200));
        n.setLayoutX(200);
        n.setLayoutY(100);
        makeDraggable(n);
        graphPane.getChildren().add(n);

        // Implement selection
        n.setOnMouseClicked(e -> {
            if (interactMode == MainController.InteractMode.SELECT_MODE) {
                if (selected == n) {
                    selected.deselect();
                    selected = null;
                } else {
                    if (selected != null) selected.deselect();
                    selected = n;
                    selected.select();
                }
            }
        });

        nodes.add(n);
    }

    public GraphCircle getNode(String name) {
        for (GraphCircle n:
             nodes) {
            if (n.getName().equals(name)) return n;
        }
        return null;
    }

    public void addArrow(GraphCircle a, GraphCircle b, Pane graphPane) {
        GraphArrow arrow = new GraphArrow(a, b);
        graphPane.getChildren().addAll(arrow, arrow.getArrowTip());
        arrows.add(arrow);
        arrow.setOnMouseClicked(e -> {
            if (interactMode == MainController.InteractMode.SELECT_MODE) {
                if (selected == arrow) {
                    selected.deselect();
                    selected = null;
                } else {
                    if (selected != null) selected.deselect();
                    selected = arrow;
                    selected.select();
                }
            }
        });
        a.addArrow(arrow);
        b.addArrow(arrow);
    }

    private void makeDraggable(GraphCircle node) {
        node.setOnMousePressed(e -> {
            if (interactMode == MainController.InteractMode.MOVE_MODE) {
                node.toFront();
                dragOriginX = e.getSceneX() - node.getLayoutX();
                dragOriginY = e.getSceneY() - node.getLayoutY();
            }
        });
        node.setOnMouseDragged(e -> {
            if (interactMode == MainController.InteractMode.MOVE_MODE) {
                double newX = e.getSceneX() - dragOriginX;
                node.getCenterXProperty().set(newX);
                node.setLayoutX(newX);
                double newY = e.getSceneY() - dragOriginY;
                node.getCenterYProperty().set(newY);
                node.setLayoutY(newY);
                node.rotateArrows();
            }
        });
    }

    public void addShape(Pane graphpane) {
        addNode("test", graphpane);

        double[] shape = new double[] { 0,0,-25,10,-25,-10 };
        arrow = new Polygon(shape);
        arrow.setFill(Color.BLACK);
        arrow.setLayoutX(300);
        arrow.setLayoutY(300);
        arrow.setId("arr");
        graphpane.getChildren().addAll(arrow);
    }

    public void turnToA(Pane graphPane) {
        GraphCircle n = getNode("test");

        double x_diff = n.getCenterXProperty().get() - arrow.getLayoutX();
        double y_diff = arrow.getLayoutY() - n.getCenterYProperty().get();
        if (x_diff == 0) x_diff = 1;
        double tan = y_diff / x_diff;
        double degree = -Math.toDegrees(Math.atan(tan));
        if (x_diff < 0) degree = degree + 180;
        System.out.println("x, y diffs: " + x_diff + " " + y_diff + "    tan: " + tan + "       atan: " + Math.atan(tan) + "       deg: " + degree);
        arrow.setRotate(degree);
    }
}
