package com.example.argumentgames;

// Class for math-focused functions to help with calculations
// such as angles
public class GeometricHelper {

    public GeometricHelper() {}

    // Given the x and y distance to a point, returns the angle towards that point (in degrees)
    // For two zeros returns a zero
    public static double x_y_toAngle(double x_length, double y_length) {
        double degree;
        if (x_length == 0) {
            if (y_length > 0) { degree = 90;} else { degree = 270; }
        } else {
            double tan = y_length / x_length;
            degree = Math.toDegrees(Math.atan(tan));
            if (x_length < 0) degree = degree + 180;
        }
        return degree;
    }

    public static double x_y_toDistance(double x_length, double y_length) {
        return Math.sqrt( ( Math.pow(x_length, 2) ) + ( Math.pow(y_length, 2) ) );
    }

    public static double gCircle_gCircle_toDistance(GraphCircle a, GraphCircle b) {
        double x_diff = a.getLayoutX() - b.getLayoutX();
        double y_diff = a.getLayoutY() - b.getLayoutY();
        return GeometricHelper.x_y_toDistance(x_diff, y_diff);
    }

    public static double angle_distance_toX(double degree, double distance) {
        double radians = Math.toRadians(degree);
        return Math.cos(radians) * distance;
    }

    public static double angle_distance_toY(double degree, double distance) {
        double radians = Math.toRadians(degree);
        return Math.sin(radians) * distance;
    }
}
