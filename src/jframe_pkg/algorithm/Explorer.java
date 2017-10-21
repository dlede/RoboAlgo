package jframe_pkg.algorithm;

import static jframe_pkg.utils.MapDescriptor.generateMapDescriptor;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import jframe_pkg.map.Cell;
import jframe_pkg.map.Mapper;
import jframe_pkg.map.MapConstant;
import jframe_pkg.robot.Robot;
import jframe_pkg.robot.RobotConstants;
import jframe_pkg.robot.RobotConstants.DIRECTION;
import jframe_pkg.robot.RobotConstants.MOVEMENT;
import jframe_pkg.utils.CommMgr;

/**
 * Exploration algorithm for the robot.
 */

public class Explorer {
    private final Mapper exMap;
    private final Mapper realMap;
    private final Robot bot;
    private final int coverageLimit;
    private final int timeLimit;
    private int areaExplored;
    private long startTime;
    private long endTime;
    //huangkai
    private int huangkai_turns=0;
    private int lastCalibrate;
    private boolean calibrationMode;
    private int counter;
    private boolean inner_start = false;
    private boolean quad_one = false;
    private boolean quad_two = false;
    private boolean quad_three = false;
    private boolean quad_four = false;
    private boolean turned = false;
    private boolean calibrated = false;
    private int turned_counter = 0;
    private int move_counter = 0;

	Scanner sc = new Scanner(System.in);
	Sprinter returnToStart;

    public Explorer(Mapper exMap, Mapper realMap, Robot bot, int coverageLimit, int timeLimit) {
        this.exMap = exMap;
        this.realMap = realMap;
        this.bot = bot;
        this.coverageLimit = coverageLimit;
        this.timeLimit = timeLimit;
    }

    /**
     * Main method that is called to start the exploration.
     */
    public void runExploration() {
    	if (bot.getRealBot()) {
            String waypoint = null;
	   		while(true)
	   		{
	   			System.out.println("Waiting for WAYPOINT...");
	   			waypoint = CommMgr.getCommMgr().revMsg(); // "10, 10"
	   			if (!waypoint.equals("!")) //if waypoint not null e.g
	   				break;
	   		}
	   		//set waypoint
	   		List<String> items = Arrays.asList(waypoint.split(","));
	   		int temp_x = Integer.parseInt(items.get(0));
	   		int temp_y = Integer.parseInt(items.get(1));
	   		
	   		System.out.println("my waypoint is " + temp_x + ", " + temp_y);
	   		exMap.gridder.set_waypoint(temp_x, temp_y);
           
	   	 System.out.println("Starting calibration...");
         if (bot.getRealBot()) {

             bot.move(MOVEMENT.LEFT, false);
             bot.move(MOVEMENT.CALIBRATE, false);
             bot.move(MOVEMENT.LEFT, false);
             bot.move(MOVEMENT.CALIBRATE, false);
             bot.move(MOVEMENT.LEFT, false);
        	 
      	   	 //bot.move(MOVEMENT.RIGHT, false);
     	   	 //bot.move(MOVEMENT.RIGHT, false);
             //bot.move(MOVEMENT.CALIBRATE, false);
             //bot.move(MOVEMENT.LEFT, false);
             //bot.move(MOVEMENT.CALIBRATE_R, false);
          }
	   		
            while (true) {
                System.out.println("Waiting for EX_START...");
                String msg = CommMgr.getCommMgr().revMsg();
                
                //System.out.println("msg: " + msg);
                
                if (msg.equals(CommMgr.EX_START)) 
                {
                	//System.out.println("im here2: Explorer.java, L97");
                	break;
                }
            }
        }

        System.out.println("Starting exploration...");
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        //System.out.println("timer start: Explorer.java, L109");
        
        //dhaslie, do we need this clause?
        if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg(CommMgr.BOT_START);
        }
        
        senseAndRepaint();
        
        //System.out.println("sense and repainted: Explorer.java, L120");

        areaExplored = calculateAreaExplored();
        // Here is moved
        System.out.println("Explored Area: " + areaExplored);

        explorationLoop(bot.getRobotPosRow(), bot.getRobotPosCol());
        //condition is if coverage < 300
        //explorationInnerLoop
    }

    /**
     * Loops through robot movements until one (or more) of the following conditions is met:
     * 1. Robot is back at (r, c)
     * 2. areaExplored > coverageLimit
     * 3. System.currentTimeMillis() > endTime
     */
    private void explorationLoop(int r, int c) {
        do {
        	//huangkai
        	//String msg = sc.nextLine();
        	
        	//CommMgr.getCommMgr().revMsg();
            nextMove();
            
            //send map to android every move
			String[] gmd = generateMapDescriptor(exMap);
			//System.out.println(Arrays.toString(gmd));
			System.out.println(gmd[0]);
			System.out.println(gmd[1]);
			
			//send to rpi, map stuffs
			//CommMgr.getCommMgr().sendMsg("UM,"+ gmd[0]);
			
			String msg_to_bt = "1" + gmd[0] +"!1" + gmd[1];
			//System.out.println(msg_to_bt);
			
			CommMgr.getCommMgr().sendMsg("UM,"+ msg_to_bt);
								
			while(true) {
				
				String umMsg = CommMgr.getCommMgr().revMsg();
				
				if (umMsg.equals("!")) //break when done 
	            {
	            	break;
	            }
			}
						
			try {
			Thread.sleep(350);
			} catch (InterruptedException e) {
				//Auto-generated catch block;
				e.printStackTrace();
			}
			
			String curAttr = (bot.getRobotPosRow() + ";" + bot.getRobotPosCol() + ";" + bot.getRobotCurDir());
			CommMgr.getCommMgr().sendMsg("CA," + curAttr);
            
			while(true) {
				
				String caMsg = CommMgr.getCommMgr().revMsg();
				
				if (caMsg.equals("!")) //break when done 
	            {
	            	break;
	            }
				
				
				
			}
			
			try {
				Thread.sleep(350);
			} catch (InterruptedException e) {
				//Auto-generated catch block;
				e.printStackTrace();
			}
			
			areaExplored = calculateAreaExplored();
            //System.out.println("Area explored: " + areaExplored);

            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c) { 
            	//System.out.println("back at starting point");
                if (areaExplored >= 50) { // if x amount of cells coverage
                	
                	System.out.println("going home");
                	//goHome();
                    break;
                }
            }
        }
        while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);
        //explorationInnerLoop();
        
        goHome();
        //System.out.println("after goHome()");
        //execute fasteest path (rec) - FP_START
    }
    
    //TODO: check if the middle part is explored
    // put this function after the do while loop in exploration loop
    private void explorationInnerLoop()//int r, int c
    {
    	if(areaExplored < 300) // if_outerloopcleared
    	{
    		System.out.println("I'm in, before NextStartPoint: Explorer.java, L170");
	    	//Fastest to next possible start point
	    	goNextStartPoint();
	    	
	    	do
	    	{
	    		goNextStartPoint();
	    		//nextMove();
	            areaExplored = calculateAreaExplored();
	            System.out.println("Area explored: " + areaExplored);
	            
	            if (bot.getRobotPosRow() == exMap.gridder.temp_row && bot.getRobotPosCol() == exMap.gridder.temp_col) { 
	                if (areaExplored >= 300) { // if fully 300 cells coverage
	                	//goHome();
	                    break;
	                }
	            }
	    	}
	    	while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);
    	}
    	
    	goHome();
    }

    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    private void nextMove() { //fwdblock_count
    	
    	move_counter++;
    	
    	if (move_counter >= 3) // if nextMove is called for 3 times or more, we calibrate
    	{
    		calibrated = false;
    		//move_counter=0;
    	}
    	
        if (lookRight()) {
            moveBot(MOVEMENT.RIGHT);
            if(lookForward()) {
            	moveBot(MOVEMENT.FORWARD);
            }
            /*if(bot.front_average < 5)
            {
            	moveBot(MOVEMENT.FORWARD);
            }
            else
            {
            	//moveBot(MOVEMENT.FORWARD_M);
            	moveBot(MOVEMENT.FORWARD);
            }
            */
            /*
            if (bot.front_average < 4) 
            {
            	//this.bot.setfwdblock_count(1);
            	moveBot(MOVEMENT.FORWARD);
            }           
            else
            {
            	//TODO: need to change something based on the average
            	if (bot.front_average >= 5) // move 4 steps
            	{
            		this.bot.setfwdblock_count(4);
            		System.out.println("moving 4 steps");
            		moveBot(MOVEMENT.FORWARD_M);
            	}
            	else if (bot.front_average > 4) // move 3 steps
            	{
            		this.bot.setfwdblock_count(3);
            		System.out.println("moving 3 steps");
            		moveBot(MOVEMENT.FORWARD_M);
            	}
            	else if (bot.front_average > 3) // move 2 steps
            	{
            		this.bot.setfwdblock_count(2);
            		System.out.println("moving 2 steps");
            		moveBot(MOVEMENT.FORWARD_M);
            	}
            	else
            	{
            		//this.bot.setfwdblock_count(1);
            		System.out.println("moving 1 steps");
            		moveBot(MOVEMENT.FORWARD);
            	}
            	
            }*/
            
            
        	/*if (bot.front_average > 5) 
        	{
        		System.out.println("moving multiple");
        		moveBot(MOVEMENT.FORWARD_M);
        	}
        	else
        	{
        		moveBot(MOVEMENT.FORWARD);
        	}*/
        } else if (lookForward()) {
        	moveBot(MOVEMENT.FORWARD);
        	/*if(bot.front_average < 4)
            {
            	moveBot(MOVEMENT.FORWARD);
            }
            else
            {
            	//System.out.println("moving multiple");
            	//moveBot(MOVEMENT.FORWARD_M);
            	moveBot(MOVEMENT.FORWARD);
            }*/
        	
        } else if (lookLeft()) {
            moveBot(MOVEMENT.LEFT);
            
            if (lookForward())
        	{
            	moveBot(MOVEMENT.FORWARD);
            	/*if(bot.front_average < 4)
                {
                	moveBot(MOVEMENT.FORWARD);
                }
                else
                {
                	//System.out.println("moving multiple");
                	//moveBot(MOVEMENT.FORWARD_M);
                	moveBot(MOVEMENT.FORWARD);
                }*/
        	}
        } else {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }

    /**
     * Returns true if the right side of the robot is free to move into.
     */
    private boolean lookRight() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return eastFree();
            case EAST:
                return southFree();
            case SOUTH:
                return westFree();
            case WEST:
                return northFree();
        }
        return false;
    }

    /**
     * Returns true if the robot is free to move forward.
     */
    private boolean lookForward() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return northFree();
            case EAST:
                return eastFree();
            case SOUTH:
                return southFree();
            case WEST:
                return westFree();
        }
        return false;
    }

    /**
     * * Returns true if the left side of the robot is free to move into.
     */
    private boolean lookLeft() {
        switch (bot.getRobotCurDir()) {
            case NORTH:
                return westFree();
            case EAST:
                return northFree();
            case SOUTH:
                return eastFree();
            case WEST:
                return southFree();
        }
        return false;
    }

    /**
     * Returns true if the robot can move to the north cell.
     */
    private boolean northFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 1, botCol - 1) && isExploredAndFree(botRow + 1, botCol) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the east cell.
     */
    private boolean eastFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 1) && isExploredAndFree(botRow, botCol + 1) && isExploredNotObstacle(botRow + 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the south cell.
     */
    private boolean southFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow - 1, botCol) && isExploredNotObstacle(botRow - 1, botCol + 1));
    }

    /**
     * Returns true if the robot can move to the west cell.
     */
    private boolean westFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 1) && isExploredAndFree(botRow, botCol - 1) && isExploredNotObstacle(botRow + 1, botCol - 1));
    }

    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void goHome() {
    	
    	//finish exploring every grid, but goal not explored
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
        	System.out.println("In goHome() of ExplorationAlgo first loop: Explorer.java, L345");
            Sprinter goToGoal = new Sprinter(exMap, bot, realMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }
        
        //finish exploration, time to go home
        System.out.println("In goHome() of ExplorationAlgo second loop: Explorer.java, L350");
        
        //stuck in the runFastestPath joey- check if not at home
        if(bot.getTouchedGoal() && bot.getRobotPosCol() != RobotConstants.START_COL || bot.getRobotPosRow() != RobotConstants.START_ROW) {
            Sprinter returnToStart = new Sprinter(exMap, bot, realMap);
            returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);//run fastest path home
        }
        

        System.out.println("EXPLORATION COMPLETED!\n\n");
        
        areaExplored = calculateAreaExplored();
        //System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        //System.out.println(", " + areaExplored + " Cells");
        //System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        
        //do we need to pause awhile before doing the calibration ?
        
        if (bot.getRealBot()) {
        	System.out.println("RealBot");
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.SOUTH);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.NORTH);
        }
        /*
        System.out.println("MyCode for goHome");
        turnBotDirection(DIRECTION.NORTH);
        if(bot.getRobotCurDir() == DIRECTION.SOUTH)
        {
        	System.out.println("Turning South to North");
        	moveBot(MOVEMENT.RIGHT);
        	moveBot(MOVEMENT.RIGHT);
        }
        else // if DIRECTION.WEST
        {
        	System.out.println("Turning West to North");
        	moveBot(MOVEMENT.RIGHT);
        }
        */
        System.out.println("Exiting goHome()");
    }
    
    private void goNextStartPoint()
    {
    	setNewSP();
    	//set bot on origin starting point
    	if (!inner_start)//if have not started
    	{
        	bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
        	bot.setRobotDir(RobotConstants.DIRECTION.NORTH);
        	inner_start = true;
    	}
    	
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600)
        {
        	//System.out.println("In goNextStartPoint() of ExplorationAlgo first loop: Explorer.java, L384");
            Sprinter goToGoal = new Sprinter(exMap, bot);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL); // run fastest path if 
        }
        //System.out.println("In goNextStartPoint() of ExplorationAlgo second loop: Explorer.java, L388");
        
        returnToStart = new Sprinter(exMap, bot, realMap);//, realMap
        returnToStart.runFastestPath(exMap.gridder.temp_row, exMap.gridder.temp_col);
        
        bot.setRobotPos(exMap.gridder.temp_row, exMap.gridder.temp_col);
        bot.setRobotDir(RobotConstants.DIRECTION.NORTH);
        
		System.out.println("\n\nbreaker....");
		System.out.println("breaker....");
		System.out.println("breaker.... \n\n");
    }
    
    private void setNewSP() {	
    	int r=2;
    	int c=2;
    	
    	//quadrant 1 not cleared
    	if(!quad_one)
    	{
    		//System.out.println("quad_one");
    		r=r+3;
    		c=c+3;
    		quad_one = true;
    	}
    	
    	//quadrant 2 not cleared
    	else if(!quad_two && quad_one)
    	{
    		//System.out.println("quad_two");
    		r=r+3;
    		c=c+9;
	    	quad_two = true;
    	}
    	//quadrant 3 not cleared
    	else if(!quad_three && quad_two && quad_one)
    	{
    		//System.out.println("quad_three");
    		r=r+14;
    		c=c+9;
    		quad_three = true;
    	}
    	//quadrant 4 not cleared
    	else if(!quad_four && quad_three && quad_two && quad_one)
    	{
    		//System.out.println("quad_four");
    		r=r+14;
    		c=c+3;
    		quad_four = true;
    	}
    	else //(quad_one && quad_two && quad_three && quad_four)
    	{
    		//System.out.println("home");
    		goHome();
    	}
    	
    	
    	//System.out.println("row: " + bot.getRobotPosRow()+1);
    	//System.out.println("col: " + bot.getRobotPosCol()+1);
    	
    	while (r<=16 && c<=11) {
    		if (r<16 && newSP_validator (r,c) == false) {
    			//System.out.println("r: " + r);
    			 r++;
    		}
    		else if (r==16 && newSP_validator(r,c) == false) {
    			//System.out.println("c: " + c);
    			r=5;
    			c++;
    		}
    		else {
    			//System.out.println("temp_row: " + exMap.gridder.temp_row);
    			//System.out.println("temp_col: " + exMap.gridder.temp_col);
    			exMap.gridder.temp_row = r;
    			exMap.gridder.temp_col = c;
    			break;
    		}   			
    	}
    }
    
    public boolean newSP_validator (int r, int c) {
    	for (int x = r-1; x<=(r+1); x++ ) {
    		for (int y = c-1; y<=(c+1); y++) {
    			if (exMap.gridder.getCell(x,y).getIsObstacle() || !exMap.gridder.getCell(x, y).getIsExplored()) {
    				//System.out.println("return false");
    				return false;
    			}
    		}
    	}
    	//System.out.println("return true");
    	return true;
    }

    /**
     * Returns true for cells that are explored and not obstacles.
     */
    private boolean isExploredNotObstacle(int r, int c) {
        if (exMap.gridder.coordinate_validator(r, c)) {
            Cell tmp = exMap.gridder.getCell(r, c);
            return (tmp.getIsExplored() && !tmp.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns true for cells that are explored, not virtual walls and not obstacles.
     */
    private boolean isExploredAndFree(int r, int c) {
        if (exMap.gridder.coordinate_validator(r, c)) {
            Cell b = exMap.gridder.getCell(r, c);
            return (b.getIsExplored() && !b.getIsVirtualWall() && !b.getIsObstacle());
        }
        return false;
    }

    /**
     * Returns the number of cells explored in the grid.
     */
    private int calculateAreaExplored() {
        int result = 0;
        for (int r = 0; r < MapConstant.MAP_X; r++) {
            for (int c = 0; c < MapConstant.MAP_Y; c++) {
                if (exMap.gridder.getCell(r, c).getIsExplored()) {
                    result++;
                }
            }
        }
        return result;
    }

    /**
     * Moves the bot, repaints the map and calls senseAndRepaint().
     */
    private void moveBot(MOVEMENT m) {
    	
    	//System.out.println("bot moved");
    	//System.out.println("move_counter: " + move_counter);
        bot.move(m);
        exMap.repaint();
        
        if (m != MOVEMENT.CALIBRATE || m != MOVEMENT.CALIBRATE_R) {
            senseAndRepaint();
        } 
        
        //dhaslie fast_forward
        //check for virtual and if there is a X amount of leeway for forward, fly forward
        /*if(exMap.gridder.isVirtualWallCell(this.bot.getRobotPosRow(), this.bot.getRobotPosCol()+1))
        {
        	//if front raw values are all more than 10 (not near front walls) and right raw values are all less than 10 (near right wall)
        	if(bot.sr_FrontLeft_value > 10 && bot.sr_FrontCenter_value > 10 && bot.sr_FrontRight_value > 10 && (bot.sr_RightTop_value < 10 && bot.sr_RightBtm_value < 10))
        	{
        		//bot.moveForwardMultiple(bot.front_min);
        		//bot.move(MOVEMENT.FORWARD_M);
        		//move forward based on the front min
	        	//if front raw values are all more than 10 (not near front walls) and right raw values are all less than 10 (near right wall)
	        	if (bot.rangeValue(sr_FrontLeft_value, 15, 23) && bot.rangeValue(sr_FrontCenter_value, 15, 23) && bot.rangeValue(sr_FrontRight_value, 15, 23))
	        	{
	        		// if all front values within raw value of 15 to 23
	        		// move 2 steps
	        	}
	        	if (bot.rangeValue(sr_FrontLeft_value, 25, 33) && bot.rangeValue(sr_FrontCenter_value, 25, 33) && bot.rangeValue(sr_FrontRight_value, 25, 33))
	        	{
	        		// if all front values within raw value of 25 to 33
	        		// move 3 steps
	        	}
	        	if (bot.angeValue(sr_FrontLeft_value, 35, 43) && bot.rangeValue(sr_FrontCenter_value, 35, 43) && bot.rangeValue(sr_FrontRight_value, 35, 43))
	        	{
	        		// if all front values within raw value of 35 to 43
	        		// move 4 steps
	        	} 
        		
        	}
        }*/
        
        //commented by dhaslie
	        /**
	        // huangkai at here its calibrate
	        if (bot.getRealBot() && !calibrationMode) {
	        	calibrationMode = true;
	            //only checks front
	            if (canCalibrateOnTheSpot(bot.getRobotCurDir())) {
	
	                //lastCalibrate = 0;
	                //if front sensor have near reading
	            	//System.out.println("bot.sr_FrontLeft_value: " + bot.sr_FrontLeft_value);
	            	//System.out.println("bot.sr_FrontCenter_value: " + bot.sr_FrontCenter_value);
	            	//System.out.println("bot.sr_FrontRight_value: " + bot.sr_FrontRight_value);
	            	
	            	if(bot.sr_FrontLeft_value < 10 || bot.sr_FrontCenter_value < 10 || bot.sr_FrontRight_value < 10)
	            	{
	            		//front is near or corner cases where right wall hug
	            		System.out.println("huang kai front calibrate");
	                    moveBot(MOVEMENT.CALIBRATE);
	                    calibrated=true;
	                    //huangkai_turns =0;
	                    //added this - joey
	                    move_counter = 0;
	                }
	            }
	            
	            else if(canCalibrateOnTheRight(bot.getRobotCurDir())){
	            	if(bot.sr_RightBtm_value < 10 || bot.sr_RightTop_value < 10)
	            	{
	            		if (calibrated == false) {
	            		System.out.println("huang kai right calibrate");
	            		moveBot(MOVEMENT.CALIBRATE_R);
	            		calibrated = true;
	            		move_counter =0;
	            		}	
	            	}
	            }
	        calibrationMode = false;
	    }
	    **/
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    private void senseAndRepaint() {
    	bot.setSensors();
        bot.sense(exMap, realMap);
        
        exMap.repaint();
    }

    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
    private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();
        System.out.println("botDir: " + botDir);
        switch (botDir) {
            case NORTH:
            	//huangkai
                return exMap.gridder.getIsObstacleOrWall(row + 2, col - 1) && exMap.gridder.getIsObstacleOrWall(row + 2, col) && exMap.gridder.getIsObstacleOrWall(row + 2, col + 1);
            case EAST:
                return exMap.gridder.getIsObstacleOrWall(row + 1, col + 2) && exMap.gridder.getIsObstacleOrWall(row, col + 2) && exMap.gridder.getIsObstacleOrWall(row - 1, col + 2);
            case SOUTH:
                return exMap.gridder.getIsObstacleOrWall(row - 2, col - 1) && exMap.gridder.getIsObstacleOrWall(row - 2, col) && exMap.gridder.getIsObstacleOrWall(row - 2, col + 1);
            case WEST:
                return exMap.gridder.getIsObstacleOrWall(row + 1, col - 2) && exMap.gridder.getIsObstacleOrWall(row, col - 2) && exMap.gridder.getIsObstacleOrWall(row - 1, col - 2);
        }

        return false;
    }
    
    ////huangkai
    private boolean canCalibrateOnTheRight(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();
        //System.out.println("botDir for right: " + botDir);
        switch (botDir) {
            case NORTH:
                return exMap.gridder.getIsObstacleOrWall(row + 1, col + 2) && exMap.gridder.getIsObstacleOrWall(row , col+2) && exMap.gridder.getIsObstacleOrWall(row -1, col + 2);
            case EAST:
                return exMap.gridder.getIsObstacleOrWall(row -2 , col + 1) && exMap.gridder.getIsObstacleOrWall(row-2, col ) && exMap.gridder.getIsObstacleOrWall(row - 2, col -1);
            case SOUTH:
                return exMap.gridder.getIsObstacleOrWall(row +1, col - 2) && exMap.gridder.getIsObstacleOrWall(row, col-2) && exMap.gridder.getIsObstacleOrWall(row -1, col -2);
            case WEST:
                return exMap.gridder.getIsObstacleOrWall(row + 2, col - 1) && exMap.gridder.getIsObstacleOrWall(row+2, col ) && exMap.gridder.getIsObstacleOrWall(row +2, col +1);
        }

        //System.out.println("right calibrate a");
        return false;
    }
    
    /**
     * Returns a possible direction for robot calibration or null, otherwise.
     */
    private DIRECTION getCalibrationDirection() {
        DIRECTION origDir = bot.getRobotCurDir();
        DIRECTION dirToCheck;

        dirToCheck = DIRECTION.getNext(origDir);                    // right turn
        if (canCalibrateOnTheSpot(dirToCheck)) 
    	{
        	//System.out.println("calibrate direction: in case of Right turn: Explorer.java, L624");
    		return dirToCheck;
    	}

        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
        if (canCalibrateOnTheSpot(dirToCheck)) 
    	{
        	//System.out.println("calibrate direction: in case of Left turn: Explorer.java, L631");
    		return dirToCheck;
    	}

        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
        if (canCalibrateOnTheSpot(dirToCheck)) 
    	{
        	//System.out.println("calibrate direction: in case of U turn: Explorer.java, L638");
    		return dirToCheck;
    	}

        return null;
    }

    /**
     * Turns the bot in the needed direction and sends the CALIBRATE movement. Once calibrated, the bot is turned back
     * to its original direction.
     */
    private void calibrateBot(DIRECTION targetDir) {
        DIRECTION origDir = bot.getRobotCurDir();

        turnBotDirection(targetDir);
        moveBot(MOVEMENT.CALIBRATE);
        turnBotDirection(origDir);
    }
    
    private void calibrateRightBot(DIRECTION targetDir) {
        DIRECTION origDir = bot.getRobotCurDir();

        turnBotDirection(targetDir);
        moveBot(MOVEMENT.CALIBRATE_R);
        turnBotDirection(origDir);
    }

    /**
     * Turns the robot to the required direction.
     */
    private void turnBotDirection(DIRECTION targetDir) {
        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
                moveBot(MOVEMENT.RIGHT);
                turned = true;
            } else {
                moveBot(MOVEMENT.LEFT);
                turned = true;
            }
        } else if (numOfTurn == 2) {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
            turned = true;
        }
    }
}
