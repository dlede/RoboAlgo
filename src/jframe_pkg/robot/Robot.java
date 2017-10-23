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

import java.util.ArrayList;
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
 *  < LR  [X] [X] [X] SR_Top >
 *        [X] [X] [X] 
 *   	  [X] [X] [X] SR_Btm >
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
    private final Sensor SRRightBack;           // east-facing right SR
    private final Sensor SRRightFront;            // west-facing left LR
    
    private boolean touchedGoal;
    private final boolean realBot;
    private JTextArea monitorScreen; 
	private JTextField field_cp;
	private int counter =0; 
	private CommMgr comm;
	public int front_average = 0;
	public int right_average = 0;
    public int front_min = 0;
    public int right_min = 0;
    //private int front_arr[] = null;
    ArrayList<Integer> front_arr = new ArrayList<Integer>();
    //private ListArray front_la;
    private int right_arr[] = null;
	
    public int sr_FrontLeft_value;       // north-facing front-left SR value for comparing
    public int sr_FrontCenter_value;     // north-facing front-center SR value for comparing
    public int sr_FrontRight_value;      // north-facing front-right SR	value for comparing
    public int sr_RightTop_value;
    public int sr_RightBtm_value;
	
	private MOVEMENT prev_mov;
	private int fwdblock_count;

    public Robot(int row, int col, boolean realBot) {
        posRow = row;
        posCol = col;
        robotDir = RobotConstants.START_DIR;
        speed = RobotConstants.SPEED;
        comm = CommMgr.getCommMgr();
        
        this.realBot = realBot;
        //SENSOR_FRONT_SHORT_RANGE_L
        //SENSOR_FRONT_SHORT_RANGE_H
        SRFrontLeft = new Sensor(RobotConstants.SENSOR_FRONT_SHORT_RANGE_L, RobotConstants.SENSOR_FRONT_SHORT_RANGE_H, this.posRow + 1, this.posCol - 1, this.robotDir, "SRFL");
        SRFrontCenter = new Sensor(RobotConstants.SENSOR_FRONT_SHORT_RANGE_L, RobotConstants.SENSOR_FRONT_SHORT_RANGE_H, this.posRow + 1, this.posCol, this.robotDir, "SRFC");
        SRFrontRight = new Sensor(RobotConstants.SENSOR_FRONT_SHORT_RANGE_L, RobotConstants.SENSOR_FRONT_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, this.robotDir, "SRFR");
        
        SRRightBack = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRL");
        SRRightFront = new Sensor(RobotConstants.SENSOR_SHORT_RANGE_L, RobotConstants.SENSOR_SHORT_RANGE_H, this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT), "SRR");
        
        LRLeft = new Sensor(RobotConstants.SENSOR_LONG_RANGE_L, RobotConstants.SENSOR_LONG_RANGE_H, this.posRow, this.posCol + 1, findNewDirection(MOVEMENT.LEFT), "LRL");
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
    	//System.out.println("inupdategoaltouch");
        if (this.getRobotPosRow() == MapConstant.GOAL_Y && this.getRobotPosCol() == MapConstant.GOAL_X)
            this.touchedGoal = true;
        //System.out.println("outupdategoaltouch");
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
        /*
        	case FORWARD_M:
        		switch (robotDir) {
                case NORTH:
                	
                	if(this.fwdblock_count == 2)
					{
                		posRow += 2;
                		//setfwdblock_count(0);
					}
                	else if (this.fwdblock_count == 3)
                	{
                		posRow += 3;
                		//setfwdblock_count(0);
                	}
                	else if (this.fwdblock_count == 4)
                	{
                		posRow += 4;
                		//setfwdblock_count(0);
                	}
                	else
                	{
                		posRow += 1;
                		//setfwdblock_count(0);
                	}
                    break;
                case EAST:
                	if(this.fwdblock_count == 2)
					{
                		posCol += 2;
                		//setfwdblock_count(0);
					}
                	else if (this.fwdblock_count == 3)
                	{
                		posCol += 3;
                		//setfwdblock_count(0);
                	}
                	else if (this.fwdblock_count == 4)
                	{
                		posCol += 4;
                		//setfwdblock_count(0);
                	}
                	else
                	{
                		posCol += 1;
                		//setfwdblock_count(0);
                	}
                    break;
                	
                case SOUTH:
                	if(this.fwdblock_count == 2)
					{
                		posRow -= 2;
                		//setfwdblock_count(0);
					}
                	else if (this.fwdblock_count == 3)
                	{
                		posRow -= 3;
                		//setfwdblock_count(0);
                	}
                	else if (this.fwdblock_count == 4)
                	{
                		posRow -= 4;
                		//setfwdblock_count(0);
                	}
                	else
                	{
                		posRow -= 1;
                		//setfwdblock_count(0);
                	}
                    break;
                	
                case WEST:
                	if(this.fwdblock_count == 2)
					{
                		posCol -= 2;
                		//setfwdblock_count(0);
					}
                	else if (this.fwdblock_count == 3)
                	{
                		posCol -= 3;
                		//setfwdblock_count(0);
                	}
                	else if (this.fwdblock_count == 4)
                	{
                		posCol -= 4;
                		//setfwdblock_count(0);
                	}
                	else
                	{
                		posCol -= 1;
                		//setfwdblock_count(0);
                	}
                    break;
                	                	
	            }
	            break;*/
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
            case UTURN:
                // what this
                robotDir = findNewDirection(m);
                break;
            //case UTURN:
            	//if uturn, find new direction
            	//break;
            case CALIBRATE:
            case CALIBRATE_R:
            	//comm.sendMsg("calibrate");
                break;
            default:
                System.out.println("Error in Robot.move()!");
                break;
        }

        if (realBot) 
        {
        	sendMovement(m, sendMoveToAndroid);
        	//CommMgr.getCommMgr().revMsg();
        }
        else 
        	System.out.println("Move: " + MOVEMENT.print(m));
        	

        field_cp.setText("Row : " + posRow + ", Col: " + posCol);
        monitorScreen.append("Move: " + MOVEMENT.print(m) + "\n");
        
        //System.out.println("Row : " + posRow + ", Col: " + posCol);
        
        updateTouchedGoal();
    }

    public void setfwdblock_count(int fwd)
    {
    	this.fwdblock_count = fwd;
    	//System.out.println("fwdblock_count: " + fwdblock_count);
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
    public void moveForwardMultiple(int count) {
    	//System.out.println("inside moveForwardMultiple function");
        if (count == 1) {
            move(MOVEMENT.FORWARD);
        } else {
            CommMgr comm = CommMgr.getCommMgr();
            if (count >= 5) {
                //System.out.println("if front_min >= 5");
                comm.sendMsg("U,"+ 4); // max furthest distance covered in straight line is 40cm
                //comm.sendMsg("0" + "" + CommMgr.INSTRUCTIONS);
            } else if (count < 5) {
            	//System.out.println("if front_min < 5");
            	comm.sendMsg("U,"+ front_min);
            	//comm.sendMsg(Integer.toString(count) + "" +  CommMgr.INSTRUCTIONS);
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
	
    	//System.out.println(MOVEMENT.print(m) + "" + CommMgr.INSTRUCTIONS);
        //comm.sendMsg(MOVEMENT.print(m) + "" + CommMgr.INSTRUCTIONS);
    	
    	prev_mov = m;
    	//System.out.println("moving Robot.java sendMovement" + this.fwdblock_count + " steps");
    	//TODO: need to test this out, multiple movement out
    	//if (m == MOVEMENT.FORWARD_M)
    	//{
    		/*
    		if (fwdblock_count == 0 || fwdblock_count == 1)
    		{
    			comm.sendMsg(String.valueOf(MOVEMENT.print(m))+",1");
    		}
    		else
    		{
    			System.out.println("moving " + this.fwdblock_count + " steps");
    			comm.sendMsg(String.valueOf(MOVEMENT.print(m))+","+this.fwdblock_count);
    		}*/
    		//comm.sendMsg(String.valueOf(MOVEMENT.print(m))+",1");
    		//comm.sendMsg(String.valueOf(MOVEMENT.print(m)));
    	//}
    	//else
    	//{
		//System.out.println("Sendingggggg");
		
    	System.out.println(String.valueOf(MOVEMENT.print(m)));
		comm.sendMsg(String.valueOf(MOVEMENT.print(m)));
		
		System.out.println("Receving");
		
		while(true) {
		
			String msg = comm.revMsg();
			
			if (msg.equals("!")) //break when done 
            {
            	break;
            }
			
			
		}

		System.out.println("Done");
    }
    

    /**
     * Sets the sensors' position and direction values according to the robot's current position and direction.
     */
    public void setSensors() {
        switch (robotDir) {
        
        case NORTH:
            SRFrontLeft.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
            SRFrontCenter.setSensor(this.posRow + 1, this.posCol, this.robotDir);
            SRFrontRight.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
            
            SRRightBack.setSensor(this.posRow - 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
            SRRightFront.setSensor(this.posRow + 1 , this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
            LRLeft.setSensor(this.posRow, this.posCol + 1, findNewDirection(MOVEMENT.LEFT));
            
            break;
            
        case EAST:
            SRFrontLeft.setSensor(this.posRow + 1, this.posCol + 1, this.robotDir);
            SRFrontCenter.setSensor(this.posRow, this.posCol + 1, this.robotDir);
            SRFrontRight.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
            
            SRRightBack.setSensor(this.posRow -1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
            SRRightFront.setSensor(this.posRow - 1, this.posCol +1, findNewDirection(MOVEMENT.RIGHT));
            LRLeft.setSensor(this.posRow-1, this.posCol, findNewDirection(MOVEMENT.LEFT));
            
            break;
            
        case SOUTH:
            SRFrontLeft.setSensor(this.posRow - 1, this.posCol + 1, this.robotDir);
            SRFrontCenter.setSensor(this.posRow - 1, this.posCol, this.robotDir);
            SRFrontRight.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
            SRRightBack.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
            SRRightFront.setSensor(this.posRow - 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
            LRLeft.setSensor(this.posRow, this.posCol - 1, findNewDirection(MOVEMENT.LEFT));
            
            break;
        case WEST:
            SRFrontLeft.setSensor(this.posRow - 1, this.posCol - 1, this.robotDir);
            SRFrontCenter.setSensor(this.posRow, this.posCol - 1, this.robotDir);
            SRFrontRight.setSensor(this.posRow + 1, this.posCol - 1, this.robotDir);
            SRRightBack.setSensor(this.posRow + 1, this.posCol + 1, findNewDirection(MOVEMENT.RIGHT));
            SRRightFront.setSensor(this.posRow + 1, this.posCol - 1, findNewDirection(MOVEMENT.RIGHT));
            LRLeft.setSensor(this.posRow + 1, this.posCol, findNewDirection(MOVEMENT.LEFT));
            break;
    }
       

    }

    /**
     * Uses the current direction of the robot and the given movement to find the new direction of the robot.
     */
    private DIRECTION findNewDirection(MOVEMENT m) {
        // only left or right
        if (m == MOVEMENT.RIGHT) {
            return DIRECTION.getNext(robotDir); //turn clockwise
        } else if (m == MOVEMENT.UTURN)
        {
        	return DIRECTION.getReverse(robotDir); //turn clockwise, 180 degree, reverse
        }
        else { //if m == movement.left
            return DIRECTION.getPrevious(robotDir); //turn counter clockwise
        }
    }

    /**
     * Calls the .sense() method of all the attached sensors and stores the received values in an integer array.
     *
     * @return [SRFrontLeft, SRFrontCenter, SRFrontRight, SRRight_Top, SRRight_Btm, LRLeft]
     */
    public int[] sense (Mapper explorationMap, Mapper realMap) {
        int[] result = new int[6];

        if (!realBot) {
            result[0] = SRFrontLeft.sense(explorationMap, realMap);
            result[1] = SRFrontCenter.sense(explorationMap, realMap);
            result[2] = SRFrontRight.sense(explorationMap, realMap);
            result[3] = SRRightBack.sense(explorationMap, realMap);
            result[4] = SRRightFront.sense(explorationMap, realMap);
            result[5] = LRLeft.sense(explorationMap, realMap);
        }

        
        
        else {
            this.comm.sendMsg("GET SENSOR");
            System.out.println("WAITING FOR SENSOR VALUE: Robot.java, L371");
            
            String msg;

            while (true) {
            	msg = comm.revMsg(); 
				if (!msg.equals("!"))
				{
					break;
				}
			}

            System.out.println("Senses Value: "+ msg);
            
            String[] msgArr = msg.split(";");
            
            //front +x offsets
            result[0] = Integer.parseInt(msgArr[0]);
            result[1] = Integer.parseInt(msgArr[1]);
            result[2] = Integer.parseInt(msgArr[2]);
            
            //right +x offsets
            result[3] = Integer.parseInt(msgArr[3]);
            result[4] = Integer.parseInt(msgArr[4]);
            
            result[5] = Integer.parseInt(msgArr[5]) +10; // +10 offset if needed huang kai remove +12 weird
            
            //Dhaslie for comparing front sensors, raw value
            sr_FrontLeft_value = result[0];    
            sr_FrontCenter_value = result[1]; 
            sr_FrontRight_value = result[2];
            
            sr_RightTop_value = result[4]; //hk sensor 3
            sr_RightBtm_value = result[3];
            //Dhaslie
            
            for (int i = 0; i < result.length; i++)
            {
            	result[i] = setValue(result[i]);
            }
            
            //dhaslie compute the minimum front array
            for (int i = 0; i < 3; i++)
            {
            	front_arr.add(result[i]);
            }
            
            //Dhaslie add minValue again
            front_min = minValue(front_arr);
            //dhaslie compute minimum front array end
            
            SRFrontLeft.senseReal(explorationMap.gridder, result[0]);
            SRFrontCenter.senseReal(explorationMap.gridder, result[1]);
            SRFrontRight.senseReal(explorationMap.gridder, result[2]);
            
            SRRightFront.senseReal(explorationMap.gridder, result[4]);
            SRRightBack.senseReal(explorationMap.gridder, result[3]);
            LRLeft.senseReal(explorationMap.gridder, result[5]);
            
            counter++;
        }
        
        return result;
    }
    
    public boolean rangeValue(int target, int lower_range, int upper_range)
    {
    	if (target < upper_range && target > lower_range )
    	{
    		return true;
    	}
    	return false;
    }
    
	//dhaslie min array function
	public static int minValue(ArrayList<Integer> arr)
	{
		int temp = arr.get(0); 
		
		for(int i = 0; i < arr.size(); i++) 
		{
			if(arr.get(i) < temp)
			{
				temp = arr.get(i);
			}
		}
		return temp;
	}
    
    public int setValue(int val) // set value as rounded off function
    {
    	int temp = 0;
    	//huangkai change later
    	int tmp = val % 10;
    	if( (tmp == 5) ||(tmp == 6)|| (tmp == 7) || (tmp==8) || (tmp == 9)){
    		temp = (int) Math.floor(((val +5)/ 10));// based on arudino reading, if ending is 7,8,9,0,1, round to the closest 10s
    	}
    	else{
    		temp = (int) val/10; // 32-36 will be 30, 37-41 will be 40
    	}
    	
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
