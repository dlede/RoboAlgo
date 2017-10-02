package jframe_pkg.robot;

import jframe_pkg.map.Mapper;
import jframe_pkg.map.Gridder;
import jframe_pkg.map.MapConstant;
import jframe_pkg.robot.RobotConstants.DIRECTION;
import jframe_pkg.robot.RobotConstants.MOVEMENT;
import jframe_pkg.utils.CommMgr;
//import jframe_pkg.utils.CommMgr;
import jframe_pkg.utils.MapDescriptor;
import jframe_pkg.robot.Sensor;

import java.util.concurrent.TimeUnit;

import javax.swing.JTextArea;
import javax.swing.JTextField;

// @formatter:off
/**
 * Represents the robot moving in the arena.
 *
 * The robot is represented by a 3 x 3 cell space as below:
 *
 *          ^   ^   ^
 *         SR  SR  SR
 *   	  [X] [X] [X] SR >
 *   < LR [X] [X] [X] 
 *   	  [X] [X] [X] SR >
 *
 * SR = Short Range Sensor, LR = Long Range Sensor
 *
 */
// @formatter:on

public class Robot {
    private int posRow; // center cell
    private int posCol; // center cell
    private DIRECTION robotDir;
    private int speed;
    private final Sensor SRFrontLeft;       // north-facing front-left SR
    private final Sensor SRFrontCenter;     // north-facing front-center SR
    private final Sensor SRFrontRight;      // north-facing front-right SR
    private final Sensor LRLeft;            // west-facing left SR
    private final Sensor SRRight2;           // east-facing right SR
    private final Sensor SRRight;            // west-facing left LR
    private boolean touchedGoal;
    private final boolean realBot;
    private JTextArea monitorScreen; 
	private JTextField field_cp;
	private double rotation; 

    public Robot(int row, int col, boolean realBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;
        rotation = 0.0; // for rotation animation
        
        this.realBot = realBot;

        SRFrontLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, this.robotDir, "SRFL");
        SRFrontCenter = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "SRFC");
        SRFrontRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "SRFR");
        LRLeft = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "LRL");
        // added
        SRRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRR");
        SRRight2 = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRR2");
        
    }

    public void setRobotPos(int row, int col) {
        posRow = row;
        posCol = col;
    }

    public int getRobotPosRow() {
        return posRow;
    }

    public int getRobotPosCol() {
        return posCol;
    }

    public void setRobotDir(DIRECTION dir) {
        robotDir = dir;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public DIRECTION getRobotCurDir() {
        return robotDir;
    }

    public boolean getRealBot() {
        return realBot;
    }
    
    //hk check if reached goal

    private void updateTouchedGoal() {
        if (this.getRobotPosRow() == MapConstant.GOAL_Y && this.getRobotPosCol() == MapConstant.GOAL_X)
            this.touchedGoal = true;
    }

    public boolean getTouchedGoal() {
        return this.touchedGoal;
    }

    /**
     * Takes in a MOVEMENT and moves the robot accordingly by changing its position and direction. Sends the movement
     * if this.realBot is set.
     */
    public void move(MOVEMENT m, boolean sendMoveToAndroid) {
        if (!realBot) {
            // Emulate real movement by pausing execution.
            try {
                TimeUnit.MILLISECONDS.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
        }

        switch (m) {
            case FORWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow++;
                        break;
                    case EAST:
                        posCol++;
                        break;
                    case SOUTH:
                        posRow--;
                        break;
                    case WEST:
                        posCol--;
                        break;
                }
                break;
            case BACKWARD:
                switch (robotDir) {
                    case NORTH:
                        posRow--;
                        break;
                    case EAST:
                        posCol--;
                        break;
                    case SOUTH:
                        posRow++;
                        break;
                    case WEST:
                        posCol++;
                        break;
                }
                break;
            case RIGHT:
            case LEFT:
                // what this
                robotDir = findNewDirection(m);
                break;
            case CALIBRATE:
                break;
            default:
                System.out.println("Error in Robot.move()!");
                break;
        }

        //if (realBot) sendMovement(m, sendMoveToAndroid);
        //else 
        System.out.println("Move: " + MOVEMENT.print(m));

        field_cp.setText("Row : " + posRow + ", Col: " + posCol);
        monitorScreen.append("Move: " + MOVEMENT.print(m) + "\n");
        
        updateTouchedGoal();
    }

    /**
     * Overloaded method that calls this.move(MOVEMENT m, boolean sendMoveToAndroid = true).
     */
    public void move(MOVEMENT m) {
        this.move(m, true);
    }

    /**
     * Sends a number instead of 'F' for multiple continuous forward movements.
     */
    
    // blank out below cause no commMgr
    
    public void moveForwardMultiple(int count) {
        if (count == 1) {
            move(MOVEMENT.FORWARD);
        } else {
            CommMgr comm = CommMgr.getCommMgr();
            if (count == 10) {
                //comm.sendMsg("0", CommMgr.INSTRUCTIONS);
                comm.sendMsg("0" + "" + CommMgr.INSTRUCTIONS);
            } else if (count < 10) {
                //comm.sendMsg(Integer.toString(count), CommMgr.INSTRUCTIONS);
            	comm.sendMsg(Integer.toString(count) + "" +  CommMgr.INSTRUCTIONS);
            }

            switch (robotDir) {
                case NORTH:
                    posRow += count;
                    break;
                case EAST:
                    posCol += count;
                    break;
                case SOUTH:
                    posRow += count;
                    break;
                case WEST:
                    posCol += count;
                    break;
            }

            //comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.BOT_POS);
            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()) + CommMgr.BOT_POS);
        }
    }
    

    /**
     * Uses the CommMgr to send the next movement to the robot.
     */
    
    // no commMgr yet
    
    /**
    private void sendMovement(MOVEMENT m, boolean sendMoveToAndroid) {
        CommMgr comm = CommMgr.getCommMgr();
        comm.sendMsg(MOVEMENT.print(m) + "", CommMgr.INSTRUCTIONS);
        if (m != MOVEMENT.CALIBRATE && sendMoveToAndroid) {
            comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()), CommMgr.BOT_POS);
        }
    }
    **/

    /**
     * Sets the sensors' position and direction values according to the robot's current position and direction.
     */
    public void setSensors() {
        switch (robotDir) {
            case NORTH:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow + 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                LRLeft.setSensor(this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
                //
                SRRight.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                break;
            case EAST:
                SRFrontLeft.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol + 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                LRLeft.setSensor(this.posRow + 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
                //
                SRRight.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));

                break;
            case SOUTH:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow - 1, this.posCol, this.robotDir);
                SRFrontRight.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                LRLeft.setSensor(this.posRow, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
                //
                SRRight.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow + 1 , this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                break;
                
            case WEST:
                SRFrontLeft.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
                SRFrontCenter.setSensor(this.posRow, this.posCol - 1, this.robotDir);
                SRFrontRight.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
                LRLeft.setSensor(this.posRow - 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
                //
                SRRight.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
                SRRight2.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
                break;
        }

    }

    /**
     * Uses the current direction of the robot and the given movement to find the new direction of the robot.
     */
    private DIRECTION findNewDirection(MOVEMENT m) {
        // only left or right
        if (m == MOVEMENT.RIGHT) {
            return DIRECTION.getNext(robotDir);
        } else {
            return DIRECTION.getPrevious(robotDir);
        }
    }

    /**
     * Calls the .sense() method of all the attached sensors and stores the received values in an integer array.
     *
     * @return [SRFrontLeft, SRFrontCenter, SRFrontRight, SRLeft, SRRight, LRLeft]
     */
    public int[] sense (Mapper explorationMap, Mapper realMap) {
        int[] result = new int[6];

        if (!realBot) {
            result[0] = SRFrontLeft.sense(explorationMap, realMap);
            result[1] = SRFrontCenter.sense(explorationMap, realMap);
            result[2] = SRFrontRight.sense(explorationMap, realMap);
            result[3] = LRLeft.sense(explorationMap, realMap);
            result[4] = SRRight.sense(explorationMap, realMap);
            result[5] = SRRight2.sense(explorationMap, realMap);

        } 
        // commMgr not needed now, sensereal is for real, sense if for simulation only
        /**
        else {
            CommMgr comm = CommMgr.getCommMgr();
            String msg = comm.recvMsg();
            String[] msgArr = msg.split(";");

            if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {
                result[0] = Integer.parseInt(msgArr[1].split("_")[1]);
                result[1] = Integer.parseInt(msgArr[2].split("_")[1]);
                result[2] = Integer.parseInt(msgArr[3].split("_")[1]);
                result[3] = Integer.parseInt(msgArr[4].split("_")[1]);
                result[4] = Integer.parseInt(msgArr[5].split("_")[1]);
                result[5] = Integer.parseInt(msgArr[6].split("_")[1]);
            }

            SRFrontLeft.senseReal(explorationMap, result[0]);
            SRFrontCenter.senseReal(explorationMap, result[1]);
            SRFrontRight.senseReal(explorationMap, result[2]);
            SRLeft.senseReal(explorationMap, result[3]);
            SRLeft2.senseReal(explorationMap, result[4]);
            LRRight.senseReal(explorationMap, result[5]);

            String[] mapStrings = MapDescriptor.generateMapDescriptor(explorationMap);
            comm.sendMsg(mapStrings[0] + " " + mapStrings[1], CommMgr.MAP_STRINGS);
        }
        **/
        return result;
    }
    
	public void setMonitorScreen(JTextArea info) {		
		
		this.monitorScreen = info;
		
	}
	
	public void setCurPostScreen( JTextField field_cp) {		
		
		this.field_cp = field_cp;
		
	}
	
	public JTextArea getMonitorScreen() {
		return this.monitorScreen;
	}
	
	public JTextField getCurPostScreen() {
		return this.field_cp;
	}
	
}
