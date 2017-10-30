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
    private int available_forward; //forward counter available infront
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
	   		
	   		//System.out.println("my waypoint is " + temp_x + ", " + temp_y);
	   		exMap.gridder.set_waypoint(temp_x, temp_y);
           
	   	 System.out.println("Starting calibration...");
         if (bot.getRealBot()) {

             bot.move(MOVEMENT.LEFT, false);
             bot.move(MOVEMENT.CALIBRATE, false);
             bot.move(MOVEMENT.LEFT, false);
             bot.move(MOVEMENT.CALIBRATE, false);
             bot.move(MOVEMENT.LEFT, false);
          }
	   		
            while (true) {
                System.out.println("Waiting for EX_START...");
                String msg = CommMgr.getCommMgr().revMsg();
                
                if (msg.equals(CommMgr.EX_START)) 
                {
                	break;
                }
            }
        }

        System.out.println("Starting exploration...");
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

       /* if (bot.getRealBot()) {
            CommMgr.getCommMgr().sendMsg(CommMgr.BOT_START);
        }*/
        
        senseAndRepaint();

        areaExplored = calculateAreaExplored();
        // Here is moved
        //System.out.println("Explored Area: " + areaExplored);

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
        	/*System.out.println("Continue?");
    		String msg = sc.nextLine();*/
        	
        	//CommMgr.getCommMgr().revMsg();
            nextMove();
            
            long startTime = System.currentTimeMillis();
            
            //send map to android every move
			String[] gmd = generateMapDescriptor(exMap);
			//System.out.println(Arrays.toString(gmd));
			System.out.println(gmd[0]);
			System.out.println(gmd[1]);
			
			//send to rpi, map stuffs
			//CommMgr.getCommMgr().sendMsg("UM,"+ gmd[0]);
			
			String msg_to_bt = "1" + gmd[0] +"!1" + gmd[1];
			//System.out.println(msg_to_bt);
				
			long startSend_Time = System.currentTimeMillis();

			CommMgr.getCommMgr().sendMsg("UM,"+ msg_to_bt);

			long endSend_Time = System.currentTimeMillis();
			long totalSend_Time = endSend_Time - startSend_Time;
			//System.out.println("totalTime taken for Map to be send finished: " + (totalSend_Time));

			while(true) {
				String umMsg = CommMgr.getCommMgr().revMsg();
				//long startTime = System.currentTimeMillis();

				if (umMsg.equals("!")) //break when done 
				{
					long endTime   = System.currentTimeMillis();
					long totalTime = endTime - startSend_Time;
					//System.out.println("totalTime taken for Map to be recieve finished: " + (totalTime - totalSend_Time));
					//System.out.println("totalTime taken for Map to be send and recieve finished: " + (totalTime));
								
					break;
				}
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
			
			areaExplored = calculateAreaExplored();
            //System.out.println("Area explored: " + areaExplored);

            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c) { 
            	//System.out.println("back at starting point");
                if (areaExplored >= 50) { // if x amount of cells coverage
                	
                	//System.out.println("going home");
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
    		//System.out.println("I'm in, before NextStartPoint: Explorer.java, L170");
	    	//Fastest to next possible start point
	    	goNextStartPoint();
	    	
	    	do
	    	{
	    		goNextStartPoint();
	    		//nextMove();
	            areaExplored = calculateAreaExplored();
	            //System.out.println("Area explored: " + areaExplored);
	            
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
        
        } else if (lookForward()) {
        
        	moveBot(MOVEMENT.FORWARD);
        	
        } else if (lookLeft()) {
            moveBot(MOVEMENT.LEFT);
            
            if (lookForward())
        	{
            	moveBot(MOVEMENT.FORWARD);
        	}
        } else {
        	//dhaslie - testing if fully walled, u turn
        	if (can_UTurn(bot.getRobotCurDir()))
        	{
        		//moveBot(MOVEMENT.UTURN);
        		moveBot(MOVEMENT.RIGHT);
                moveBot(MOVEMENT.RIGHT);
        	}
        	else //dhaslie - else turn twice right...
        	{
        		moveBot(MOVEMENT.RIGHT);
                moveBot(MOVEMENT.RIGHT);
        	}
        }
    }
    
    //dhaslie - added multiple steps when near walls only
	private boolean canWallMoveMult(DIRECTION botDir)
    {
        //if (exMap.gridder.coordinate_validator(r, c)) {
    	int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();

        switch (botDir) {
            case NORTH:
            	Cell a_north = exMap.gridder.getCell(row+1, col+2);
            	Cell b_north = exMap.gridder.getCell(row, col+2);
            	Cell c_north = exMap.gridder.getCell(row-1, col+2);
            	
                return a_north.getIsVirtualWall() && b_north.getIsVirtualWall() && c_north.getIsVirtualWall();
            case EAST:
            	Cell a_east = exMap.gridder.getCell(row-2, col+1);
            	Cell b_east = exMap.gridder.getCell(row-2, col);
            	Cell c_east = exMap.gridder.getCell(row-2, col-1);
            	
            	return a_east.getIsVirtualWall() && b_east.getIsVirtualWall() && c_east.getIsVirtualWall();
            case SOUTH:
            	Cell a_south = exMap.gridder.getCell(row+1, col-2);
            	Cell b_south = exMap.gridder.getCell(row, col-2);
            	Cell c_south = exMap.gridder.getCell(row-1, col-2);
            	
            	return a_south.getIsVirtualWall() && b_south.getIsVirtualWall() && c_south.getIsVirtualWall();
            case WEST:
            	Cell a_west = exMap.gridder.getCell(row+2, col+1);
            	Cell b_west = exMap.gridder.getCell(row+2, col);
            	Cell c_west = exMap.gridder.getCell(row+2, col-1);
            	
            	return a_west.getIsVirtualWall() && b_west.getIsVirtualWall() && c_west.getIsVirtualWall();
        }
    	return false;
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

    //dhaslie, attempt for multiple forward if the grids are clear infront
    // UNDER CONSTRUCTION
    private void multipleFree()
    {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        
        if(northTwoFree()) // second layer free
        {
        	if (northThreeFree()) // third layer free
        	{
        		if (northFourFree()) // fourth layer free
        		{
        			//set count = 4
        			available_forward = 4;
        		}
        		//set count = 3
        		available_forward = 3;
        	}
        	//set count = 2
        	available_forward = 2;
        }
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

    //dhaslie - attemping to create multiple move forward if explored and not obstacle
	/**
     * Returns true if the robot can move to 2 north cells.
     */
    private boolean northTwoFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 2, botCol - 1) && isExploredAndFree(botRow + 2, botCol) && isExploredNotObstacle(botRow + 2, botCol + 1));
    }

    /**
     * Returns true if the robot can move to 2 east cells.
     */
    private boolean eastTwoFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 2) && isExploredAndFree(botRow, botCol + 2) && isExploredNotObstacle(botRow + 1, botCol + 2));
    }

    /**
     * Returns true if the robot can move to 2 south cells.
     */
    private boolean southTwoFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 2, botCol - 1) && isExploredAndFree(botRow - 2, botCol) && isExploredNotObstacle(botRow - 2, botCol + 1));
    }

    /**
     * Returns true if the robot can move to 2 west cells.
     */
    private boolean westTwoFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 2) && isExploredAndFree(botRow, botCol - 2) && isExploredNotObstacle(botRow + 1, botCol - 2));
    }
	
	    /**
     * Returns true if the robot can move to 3 north cells.
     */
    private boolean northThreeFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 3, botCol - 1) && isExploredAndFree(botRow + 3, botCol) && isExploredNotObstacle(botRow + 3, botCol + 1));
    }

    /**
     * Returns true if the robot can move to 3 east cells.
     */
    private boolean eastThreeFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 3) && isExploredAndFree(botRow, botCol + 3) && isExploredNotObstacle(botRow + 1, botCol + 3));
    }

    /**
     * Returns true if the robot can move to 3 south cells.
     */
    private boolean southThreeFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 3, botCol - 1) && isExploredAndFree(botRow - 3, botCol) && isExploredNotObstacle(botRow - 3, botCol + 1));
    }

    /**
     * Returns true if the robot can move to 3 west cells.
     */
    private boolean westThreeFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 3) && isExploredAndFree(botRow, botCol - 3) && isExploredNotObstacle(botRow + 1, botCol - 3));
    }
	
	    /**
     * Returns true if the robot can move to 4 north cells.
     */
    private boolean northFourFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow + 4, botCol - 1) && isExploredAndFree(botRow + 4, botCol) && isExploredNotObstacle(botRow + 4, botCol + 1));
    }

    /**
     * Returns true if the robot can move to 4 east cells.
     */
    private boolean eastFourFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol + 4) && isExploredAndFree(botRow, botCol + 4) && isExploredNotObstacle(botRow + 1, botCol + 4));
    }

    /**
     * Returns true if the robot can move to 4 south cells.
     */
    private boolean southFourFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 4, botCol - 1) && isExploredAndFree(botRow - 4, botCol) && isExploredNotObstacle(botRow - 4, botCol + 1));
    }

    /**
     * Returns true if the robot can move to 4 west cells.
     */
    private boolean westFourFree() {
        int botRow = bot.getRobotPosRow();
        int botCol = bot.getRobotPosCol();
        return (isExploredNotObstacle(botRow - 1, botCol - 4) && isExploredAndFree(botRow, botCol - 4) && isExploredNotObstacle(botRow + 1, botCol - 4));
    }
    
    /**
     * Returns the robot to START after exploration and points the bot northwards.
     */
    private void goHome() {
    	
    	//finish exploring every grid, but goal not explored
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
        	//System.out.println("In goHome() of ExplorationAlgo first loop: Explorer.java, L345");
            Sprinter goToGoal = new Sprinter(exMap, bot, realMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }
        
        //finish exploration, time to go home
        //System.out.println("In goHome() of ExplorationAlgo second loop: Explorer.java, L350");
        
        //stuck in the runFastestPath joey- check if not at home
        if(bot.getTouchedGoal() && bot.getRobotPosCol() != RobotConstants.START_COL || bot.getRobotPosRow() != RobotConstants.START_ROW) {
            Sprinter returnToStart = new Sprinter(exMap, bot, realMap);
            returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);//run fastest path home
        }
        
        areaExplored = calculateAreaExplored();
        //System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        //System.out.println(", " + areaExplored + " Cells");
        //System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        if (bot.getRealBot()) {
        	//System.out.println("RealBot");
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.SOUTH);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.NORTH);
        }
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
        
		//System.out.println("\n\nbreaker....");
		//System.out.println("breaker....");
		//System.out.println("breaker.... \n\n");
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

        
        //dhaslie fast_forward movements
        /**if (canWallMoveMult(bot.getRobotCurDir()))
        {
        	if(bot.sr_FrontLeft_value > 10 && bot.sr_FrontCenter_value > 10 && bot.sr_FrontRight_value > 10)
        	{
        		System.out.println("I can move forward " + bot.front_min + " amount f blocks");
        		//bot.moveForwardMultiple(bot.front_min);
        	}
        	//bot.move(MOVEMENT.FORWARD_M);
        }**/
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
    
    //dhaslie can u turn?
	private boolean can_UTurn(DIRECTION botDir) {
        
		/*switch (botDir) {
            case NORTH:
                return !northFree() && !westFree() && !eastFree();
            case EAST:
                return !eastFree() && !northFree() && !southFree();
            case SOUTH:
                return !southFree() && !westFree() && !eastFree();
            case WEST:
                return !westFree() && !northFree() && !southFree();
        }*/

        //System.out.println("U Turn");
        return false;
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
        	//dhaslie - added uturn
        	//moveBot(MOVEMENT.UTURN);
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
            turned = true;
        }
    }
}
