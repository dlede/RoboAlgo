package jframe_pkg.map;

import java.awt.Color;

public class GraphicsConstant {
	//Night Mode - Black Background
    public static final int CELL_LINE_WEIGHT = 3;

    public static final Color C_START = Color.BLUE;
    public static final Color C_GOAL = Color.GREEN;
    public static final Color C_UNEXPLORED = Color.LIGHT_GRAY;
    public static final Color C_FREE = Color.WHITE;
    public static final Color C_OBSTACLE = Color.DARK_GRAY; //BLACK
    public static final Color C_WAYPOINT = Color.MAGENTA;

    public static final Color C_ROBOT = Color.ORANGE;
    public static final Color C_ROBOT_DIR = Color.BLACK;

    public static final int ROBOT_W = 60; // origin 70
    public static final int ROBOT_H = 60; // origin 70

    public static final int ROBOT_X_OFFSET = 15; //origin 10
    public static final int ROBOT_Y_OFFSET = 15; //origin 20

    public static final int ROBOT_DIR_W = 10;
    public static final int ROBOT_DIR_H = 10;

    public static final int CELL_SIZE = 30;

    public static final int MAP_H = 600;
    public static final int MAP_X_OFFSET = 120;
}
