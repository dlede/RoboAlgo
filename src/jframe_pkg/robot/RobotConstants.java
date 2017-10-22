package jframe_pkg.robot;

/**
 * Constants used in this package.
 *
 * @author Huang Kai
 */

public class RobotConstants {
    //public static final int GOAL_ROW = 4;                          // row no. of goal cell
    //public static final int GOAL_COL = 9;                          // col no. of goal cell
    public static final int GOAL_ROW = 18;                          // row no. of goal cell
    public static final int GOAL_COL = 13;                          // col no. of goal cell
    public static final int START_ROW = 1;                          // row no. of start cell
    public static final int START_COL = 1;                          // col no. of start cell
    public static final int MOVE_COST = 10;                         // cost of FORWARD, BACKWARD movement
    public static final int TURN_COST = 20;                         // cost of RIGHT, LEFT movement
    public static final int SPEED = 100;                            // delay between movements (ms)
    
    public static final DIRECTION START_DIR = DIRECTION.NORTH;      // start direction 
    
    public static final int SENSOR_SHORT_RANGE_L = 1;               // range of short range sensor (cells)
    public static final int SENSOR_SHORT_RANGE_H = 2;               // range of short range sensor (cells)
    
    public static final int SENSOR_FRONT_SHORT_RANGE_L = 1;         // range of short front range sensor (cells)
    public static final int SENSOR_FRONT_SHORT_RANGE_H = 2;         // range of short front range sensor (cells)
    
    
    //change to 3 , 5 
    public static final int SENSOR_LONG_RANGE_L = 3;                // range of long range sensor (cells)
    public static final int SENSOR_LONG_RANGE_H = 5;                // range of long range sensor (cells)

    public static final int INFINITE_COST = 9999;

    public enum DIRECTION {
        NORTH, EAST, SOUTH, WEST;
        
        // anti-clockwise
        public static DIRECTION getNext(DIRECTION curDirection) {
            return values()[(curDirection.ordinal() + 1) % values().length];
        }

        // clockwise
        public static DIRECTION getPrevious(DIRECTION curDirection) {
            return values()[(curDirection.ordinal() + values().length - 1) % values().length];
        }

        public static char print(DIRECTION d) {
            switch (d) {
                case NORTH:
                    return 'N';
                case EAST:
                    return 'E';
                case SOUTH:
                    return 'S';
                case WEST:
                    return 'W';
                default:
                    return 'X';
            }
        }
    }

    public enum MOVEMENT {
        FORWARD, /*FORWARD_M,*/ BACKWARD, RIGHT, LEFT, CALIBRATE, CALIBRATE_R, ERROR;

        public static char print(MOVEMENT m) {
            switch (m) {
                case FORWARD:
                    return 'F';
                case BACKWARD:
                    return 'B';
                case RIGHT:
                    return 'R';
                case LEFT:
                    return 'L';
                case CALIBRATE:
                    return 'C';
                case CALIBRATE_R:
                	return 'W';
                //case FORWARD_M: 
                //	return 'U';
                case ERROR:
                default:
                    return 'E';
            }
        }
    }
}
