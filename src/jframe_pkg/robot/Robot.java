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
import java.lang.*;
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
 *   	  [X] [X] [X] SR2 >
 *
 * SR = Short Range Sensor, LR = Long Range Sensor
 * Correct input sensor reading should be: SRFL;	 SRFL;	SRFR;		SRR;	SRR2;	LRL
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
	private int counter =0; 
	private CommMgr comm;
	public int front_average = 0;
	public int right_average = 0;
	private MOVEMENT prev_mov;

    public Robot(int row, int col, boolean realBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;
        //angle = 0.0; // for rotation animation
        
        comm = CommMgr.getCommMgr();
        
        this.realBot = realBot;

        SRFrontLeft = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, this.robotDir, "SRFL");
        SRFrontCenter = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "SRFC");
        SRFrontRight = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "SRFR");
        LRLeft = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow+1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT), "LRL");
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
    
    public DIRECTION getRobotDir() {
        return robotDir;
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
    	System.out.println("inupdategoaltouch");
        if (this.getRobotPosRow() == MapConstant.GOAL_Y && this.getRobotPosCol() == MapConstant.GOAL_X)
            this.touchedGoal = true;
        System.out.println("outupdategoaltouch");
    }

    public boolean getTouchedGoal() {
        return this.touchedGoal;
    }

    public int averageSense(int sense_val)
    {
    	return sense_val;
    }
    
    /**
     * Takes in a MOVEMENT and moves the robot accordingly by changing its position and direction. Sends the movement
     * if this.realBot is set.
     */
    public void move(MOVEMENT m, boolean sendMoveToAndroid) {
        //CommMgr comm = CommMgr.getCommMgr();//testing
    	
        if (!realBot) {
            // Emulate real movement by pausing execution.
            try {
                TimeUnit.MILLISECONDS.sleep(speed);
            } catch (InterruptedException e) {
                System.out.println("Something went wrong in Robot.move()!");
            }
        }

        switch (m) {
        	case FORWARD_M:
        		switch (robotDir) {
                case NORTH:
                    posRow += 3;
                    break;
                case EAST:
                    posCol += 3;
                    break;
                case SOUTH:
                    posRow -= 3;
                    break;
                case WEST:
                    posCol -= 3;
                    break;
	            }
	            break;
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
            case CALIBRATE_R:
            	//comm.sendMsg("calibrate");
                break;
            default:
                System.out.println("Error in Robot.move()!");
                break;
        }

        if (realBot) 
        	sendMovement(m, sendMoveToAndroid);
        else 
        	System.out.println("Move: " + MOVEMENT.print(m));
        	

        field_cp.setText("Row : " + posRow + ", Col: " + posCol);
        monitorScreen.append("Move: " + MOVEMENT.print(m) + "\n");
        
        System.out.println("Row : " + posRow + ", Col: " + posCol);
        
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
            	System.out.println("msg1");
                comm.sendMsg("0" + "" + CommMgr.INSTRUCTIONS);
            } else if (count < 10) {
                //comm.sendMsg(Integer.toString(count), CommMgr.INSTRUCTIONS);
            	System.out.println("msg2");
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

        	System.out.println("msg3");
        	//comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()));
        	//CommMgr.BOT_POS
        }
    }
    

    /**
     * Uses the CommMgr to send the next movement to the robot.
     */
    
    public MOVEMENT getMovement()
    {
    	return this.prev_mov;
    }
    
    private void sendMovement(MOVEMENT m, boolean sendMoveToAndroid) {
        //CommMgr comm = CommMgr.getCommMgr();

    	System.out.println(MOVEMENT.print(m) + "" + CommMgr.INSTRUCTIONS);
        //comm.sendMsg(MOVEMENT.print(m) + "" + CommMgr.INSTRUCTIONS);
    	
    	prev_mov = m;
    	
    	comm.sendMsg(String.valueOf(MOVEMENT.print(m)));

    	//comm.sendMsg("R");
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        if (m != MOVEMENT.CALIBRATE && sendMoveToAndroid) {
        	System.out.println("m != MOVEMENT.CALIBRATE && sendMoveToAndroid: Robot.java, L273");
        	//System.out.println(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()) + CommMgr.BOT_POS);
        	//comm.sendMsg(this.getRobotPosRow() + "," + this.getRobotPosCol() + "," + DIRECTION.print(this.getRobotCurDir()) + CommMgr.BOT_POS);
        }
    }
    

    /**
     * Sets the sensors' position and direction values according to the robot's current position and direction.
     */
    public void setSensors() {
    	System.out.println("set Sensors, directions: " + robotDir + ": Robot.java, L284");
        switch (robotDir) {
        
        case NORTH:
        	
        	SRFrontLeft.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
            
            SRFrontCenter.setSensor(this.posRow + 1, this.posCol, this.robotDir);
            
            SRFrontRight.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
            
            LRLeft.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
            //
            SRRight.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
            
            SRRight2.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
            break;
            
        case EAST:
            SRFrontLeft.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
            SRFrontCenter.setSensor(this.posRow, this.posCol + 1, this.robotDir);
            SRFrontRight.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
            LRLeft.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
            //
            SRRight.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
            SRRight2.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));

            break;
            
        case SOUTH:
            SRFrontLeft.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
            SRFrontCenter.setSensor(this.posRow - 1, this.posCol, this.robotDir);
            SRFrontRight.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
            LRLeft.setSensor(this.posRow+1, this.posCol, findNewDirection(MOVEMENT.LEFT));
            //
            SRRight.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
            SRRight2.setSensor(this.posRow + 1 , this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
            break;
            
        case WEST:
            SRFrontLeft.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
            SRFrontCenter.setSensor(this.posRow, this.posCol - 1, this.robotDir);
            SRFrontRight.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
            LRLeft.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
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
            result[3] = SRRight.sense(explorationMap, realMap); // LRLeft.sense(explorationMap, realMap); 
            result[4] = SRRight.sense(explorationMap, realMap); // SRRight.sense(explorationMap, realMap);
            result[5] = LRLeft.sense(explorationMap, realMap); // SRRight2.sense(explorationMap, realMap);
        } 
        // commMgr not needed now, sensereal is for real, sense if for simulation only
        else {
        	System.out.println("Getting sensors counter: " + counter + ": Robot.java, L360");
            this.comm.sendMsg("GET SENSOR");
            System.out.println("SENSOR MSG SENT: Robot.java, L362");
           /* 
        	try {
    			Thread.sleep(100);
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}*/

            System.out.println("WAITING FOR SENSOR VALUE: Robot.java, L371");
            String msg = comm.revMsg();

            System.out.println("GOT SENSOR VALUE: Robot.java, L374");
            //System.out.println("msg11: "+ msg);
            String[] msgArr = msg.split(";");
            for(String str: msgArr) {
            	System.out.println("msgArr: "+ str);
            }
            

//            floor(Double.parseDouble())
            
/*            if (msgArr[0].equals(CommMgr.SENSOR_DATA)) {
                result[0] = Integer.parseInt(msgArr[1].split(".")[1]);
                result[1] = Integer.parseInt(msgArr[2].split(".")[1]);
                result[2] = Integer.parseInt(msgArr[3].split(".")[1]);
                result[3] = Integer.parseInt(msgArr[4].split(".")[1]);
                result[4] = Integer.parseInt(msgArr[5].split(".")[1]);
                result[5] = Integer.parseInt(msgArr[6].split(".")[1]);
            }*/
            
            result[0] = (int)(Math.floor(Double.parseDouble(msgArr[0])));
            result[1] = (int)(Math.floor(Double.parseDouble(msgArr[1])));
            result[2] = (int)(Math.floor(Double.parseDouble(msgArr[2])));
            result[3] = (int)(Math.floor(Double.parseDouble(msgArr[3])));
            result[4] = (int)(Math.floor(Double.parseDouble(msgArr[4])));
            result[5] = (int)(Math.floor(Double.parseDouble(msgArr[5])));
            
            int temp = 0;
            for (int i = 0; i < result.length; i++)
            {
            	//temp = setValue(result[i]);
            	System.out.println("result: " + i);
            	result[i] = setValue(result[i]);
            }
            //if (result[])
            
            front_average = averageSense((result[0] + result[1] + result[2])/3);
            right_average = averageSense((result[3] + result[4]/2));
            
            /*result[0] = Integer.parseInt((msgArr[0].split("."))[0]);
            result[1] = Integer.parseInt((msgArr[1].split("."))[0]);
            result[2] = Integer.parseInt((msgArr[2].split("."))[0]);
            result[3] = Integer.parseInt((msgArr[3].split("."))[0]);
            result[4] = Integer.parseInt((msgArr[4].split("."))[0]);
            result[5] = Integer.parseInt((msgArr[5].split("."))[0]);*/
            
            System.out.println("SRFrontLeft: " + result[0]);
            System.out.println("SRFrontCenter: " + result[1]);
            System.out.println("SRFrontRight: " + result[2]);
            System.out.println("SRRight: " + result[3]);
            System.out.println("SRRight2: " + result[4]);
            System.out.println("LRLeft: " + result[5]);

            SRFrontLeft.senseReal(explorationMap.gridder, result[0]);
            SRFrontCenter.senseReal(explorationMap.gridder, result[1]);
            SRFrontRight.senseReal(explorationMap.gridder, result[2]);
            System.out.println("done front sense: Robot.java, L418");
            
            SRRight.senseReal(explorationMap.gridder, result[4]);
            SRRight2.senseReal(explorationMap.gridder, result[3]);
            System.out.println("done right sense: Robot.java, L422");

            LRLeft.senseReal(explorationMap.gridder, result[5]);
            System.out.println("done left sense: Robot.java, L425");
            
            System.out.println("done sensereal: Robot.java, L427");
            counter++;
            //String[] mapStrings = MapDescriptor.generateMapDescriptor(explorationMap);
            //System.out.println(mapStrings[0] + " " + mapStrings[1] + " " + CommMgr.MAP_STRINGS);
            //comm.sendMsg(mapStrings[0] + " " + mapStrings[1] + " " + CommMgr.MAP_STRINGS);
        }
        
        return result;
    }
    
    public int setValue(int val)
    {
    	int temp = 0;
    	//val = (((val+5)%10)*10); // round up to nearest 10
    	temp = (int) Math.floor(((val +5)/ 10));// if sensor value is divided by 10
    	System.out.println("val value: " + val);
    	System.out.println("temp value: " + temp);
    	return temp;
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
