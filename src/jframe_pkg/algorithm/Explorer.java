package jframe_pkg.algorithm;

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
    private int lastCalibrate;
    private boolean calibrationMode;
    private int counter;
    private boolean inner_start = false;
    private boolean quad_one = false;
    private boolean quad_two = false;
    private boolean quad_three = false;
    private boolean quad_four = false;
    
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
            System.out.println("Starting calibration...");

         
            //CommMgr.getCommMgr().revMsg();
            if (bot.getRealBot()) {
                bot.move(MOVEMENT.LEFT, false);
                //CommMgr.getCommMgr().revMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                //CommMgr.getCommMgr().revMsg();
                bot.move(MOVEMENT.LEFT, false);
                //CommMgr.getCommMgr().revMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                //CommMgr.getCommMgr().revMsg();
                bot.move(MOVEMENT.RIGHT, false);
                //CommMgr.getCommMgr().revMsg();
                bot.move(MOVEMENT.CALIBRATE, false);
                //CommMgr.getCommMgr().revMsg();
                bot.move(MOVEMENT.RIGHT, false);
            }

            while (true) {
                System.out.println("Waiting for EX_START...");
                String msg = CommMgr.getCommMgr().revMsg();
                
                System.out.println("msg: " + msg);
                System.out.println("def: " + CommMgr.EX_START);
                //String[] msgArr = msg.split(";");
                //System.out.println("?????");
                /*Class cls = msg.getClass();
                Class cls2 = (CommMgr.EX_START).getClass();
                System.out.println("msg type: " + cls.getName());
                System.out.println("def type: " + cls2.getName());
                
                System.out.println("msg bytes: " + msg.getBytes());
                System.out.println("def bytes: " + (CommMgr.EX_START).getBytes());
                
                System.out.println("truth: " + ((String)msg == "EX_START"));
                System.out.println("truth2: " + (msg.equals(CommMgr.EX_START)));*/
                
               /* if ((String)msg == "EX_START") 
                {
                	System.out.println("im here1");
                	//break;
                }*/
                
                if (msg.equals(CommMgr.EX_START)) 
                {
                	System.out.println("im here2: Explorer.java, L97");
                	break;
                }
                	
            }
            
        }

        System.out.println("Starting exploration...");
        startTime = System.currentTimeMillis();
        endTime = startTime + (timeLimit * 1000);

        System.out.println("timer start: Explorer.java, L109");
        
        if (bot.getRealBot()) {
            //CommMgr.getCommMgr().sendMsg(null, CommMgr.BOT_START);
        	CommMgr.getCommMgr().sendMsg(CommMgr.BOT_START);
        }
        
        System.out.println("realbot send bot started: Explorer.java, L116");
        
        senseAndRepaint();
        
        System.out.println("sense and repainted: Explorer.java, L120");

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
            nextMove();
            areaExplored = calculateAreaExplored();
            System.out.println("Area explored: " + areaExplored);

            if (bot.getRobotPosRow() == r && bot.getRobotPosCol() == c) { 
                if (areaExplored >= 100) { // if 100 cells coverage
                	//goHome();
                    break;
                }
                /**else
                {
                	counter++;
                	
                	System.out.println("Counter: " +counter);
                	explorationLoop(r+3, c+3);
                }**/
            }
        }
        while (areaExplored <= coverageLimit && System.currentTimeMillis() <= endTime);
        explorationInnerLoop();
        
        //TODO gohome() have an issue, cannot go home, check Sprinter class on the tempbot
        //goHome();
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
	    	
	    	//TODO: fastest path with nextmove on the quadrant
	    	
	    	//System.out.println("temp_row: " + temp_row + ", temp_col: " + temp_col);
	    	/*
	        for (int row = 0; row < this.exMap.gridder.grid.length; row++) {
	            for (int col = 0; col < this.exMap.gridder.grid[0].length; col++) {
	            	//this.exMap.gridder.grid[row][col] = new Cell(row, col);
	
	            	// row == 0 || col == 0 || row == MapConstant.MAP_X - 1 || col == MapConstant.MAP_Y - 1
	            	//|| row == 1 || col == 1 || row == MapConstant.MAP_X - 2 || col == MapConstant.MAP_Y - 2
	            	//|| row == 2 || col == 2 || row == MapConstant.MAP_X - 3 || col == MapConstant.MAP_Y - 3
	            	//System.out.println("row: " + row + ", col: " + col + ", value: " + this.exMap.gridder.grid[row][col].getIsVirtualWall());
	            	// Set the extra padding virtual walls of the arena
	                if (row == 1 || col == 1 || row == MapConstant.MAP_X - 2 || col == MapConstant.MAP_Y - 2 || row == 2 || col == 2 || row == MapConstant.MAP_X - 3 || col == MapConstant.MAP_Y - 3) {
	                	this.exMap.gridder.grid[row][col].setVirtualWall(true); //reduce the virtualwall, setInnerVirtualWall(true)
	                }
	            }
	        }*/
	    	
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
    	
    	//Setbackvirtualwall();
    	/*
        for (int row = 0; row < this.exMap.gridder.grid.length; row++) 
        {
            for (int col = 0; col < this.exMap.gridder.grid[0].length; col++) 
            {
            	//this.exMap.gridder.grid[row][col] = new Cell(row, col);

                // Set the virtual walls of the arena
                if (row == 0 || col == 0 || row == MapConstant.MAP_X - 1 || col == MapConstant.MAP_Y - 1) {
                	this.exMap.gridder.grid[row][col].setVirtualWall(true);
                }
            }
        }*/
    	
    	goHome();
    }
    
    
    
    private void quadrant()
    {
    	//TODO: 0, 0
    }

    /**
     * Determines the next move for the robot and executes it accordingly.
     */
    private void nextMove() {
        if (lookRight()) {
            moveBot(MOVEMENT.RIGHT);
            if (lookForward()) moveBot(MOVEMENT.FORWARD);
        } else if (lookForward()) {
            moveBot(MOVEMENT.FORWARD);
        } else if (lookLeft()) {
            moveBot(MOVEMENT.LEFT);
            if (lookForward()) moveBot(MOVEMENT.FORWARD);
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
        if (!bot.getTouchedGoal() && coverageLimit == 300 && timeLimit == 3600) {
        	System.out.println("In goHome() of ExplorationAlgo first loop: Explorer.java, L345");
            Sprinter goToGoal = new Sprinter(exMap, bot, realMap);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
        }
        
        System.out.println("In goHome() of ExplorationAlgo second loop: Explorer.java, L350");
        Sprinter returnToStart = new Sprinter(exMap, bot, realMap);
        returnToStart.runFastestPath(RobotConstants.START_ROW, RobotConstants.START_COL);

        System.out.println("Exploration complete!");
        areaExplored = calculateAreaExplored();
        System.out.printf("%.2f%% Coverage", (areaExplored / 300.0) * 100.0);
        System.out.println(", " + areaExplored + " Cells");
        System.out.println((System.currentTimeMillis() - startTime) / 1000 + " Seconds");

        if (bot.getRealBot()) {
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.SOUTH);
            moveBot(MOVEMENT.CALIBRATE);
            turnBotDirection(DIRECTION.WEST);
            moveBot(MOVEMENT.CALIBRATE);
        }
        turnBotDirection(DIRECTION.NORTH);
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
        	System.out.println("In goNextStartPoint() of ExplorationAlgo first loop: Explorer.java, L384");
            Sprinter goToGoal = new Sprinter(exMap, bot);
            goToGoal.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL); // run fastest path if 
        }
        System.out.println("In goNextStartPoint() of ExplorationAlgo second loop: Explorer.java, L388");
        
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
    		System.out.println("quad_one");
    		r=r+3;
    		c=c+3;
    		//r=bot.getRobotPosRow()+3;
    		//c=bot.getRobotPosCol()+3;
    		quad_one = true;
    	}
    	
    	//quadrant 2 not cleared
    	else if(!quad_two && quad_one)
    	{
    		System.out.println("quad_two");
    		r=r+3;
    		c=c+9;
	    	//r=bot.getRobotPosRow()+9;
	    	//c=bot.getRobotPosCol()+3;
	    	quad_two = true;
    	}
    	//quadrant 3 not cleared
    	else if(!quad_three && quad_two && quad_one)
    	{
    		System.out.println("quad_three");
    		r=r+14;
    		c=c+9;
    		//r=bot.getRobotPosRow()+9;
    		//c=bot.getRobotPosCol()+14;
    		quad_three = true;
    	}
    	//quadrant 4 not cleared
    	else if(!quad_four && quad_three && quad_two && quad_one)
    	{
    		System.out.println("quad_four");
    		r=r+14;
    		c=c+3;
	    	//r=bot.getRobotPosRow()+3;
	    	//c=bot.getRobotPosCol()+14;
    		quad_four = true;
    	}
    	else //(quad_one && quad_two && quad_three && quad_four)
    	{
    		System.out.println("home");
    		goHome();
    	}
    	
    	
    	System.out.println("row: " + bot.getRobotPosRow()+1);
    	System.out.println("col: " + bot.getRobotPosCol()+1);
    	
    	while (r<=16 && c<=11) {
    		if (r<16 && newSP_validator (r,c) == false) {
    			System.out.println("r: " + r);
    			 r++;
    		}
    		else if (r==16 && newSP_validator(r,c) == false) {
    			System.out.println("c: " + c);
    			r=5;
    			c++;
    		}
    		else {
    			System.out.println("temp_row: " + exMap.gridder.temp_row);
    			System.out.println("temp_col: " + exMap.gridder.temp_col);
    			exMap.gridder.temp_row = r;
    			exMap.gridder.temp_col = c;
    			break;
    		}   			
    	}
    }
    
    public boolean newSP_validator (int r, int c) {
    	for (int x = r-1; x<=(r+1); x++ ) {
    		for (int y = c-1; y<=(c+1); y++) {
    			//System.out.println("grid: " + x + "," + y);
    			//System.out.println("obstacle status: " + exMap.gridder.getCell(x,y).getIsObstacle());
    			//System.out.println("explored status: " + !exMap.gridder.getCell(x, y).getIsExplored());
    			if (exMap.gridder.getCell(x,y).getIsObstacle() || !exMap.gridder.getCell(x, y).getIsExplored()) {
    				System.out.println("return false");
    				return false;
    			}
    		}
    	}
    	System.out.println("return true");
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
    	System.out.println("bot moved");
        bot.move(m);
        exMap.repaint();
        System.out.println("exMap repainted");
        if (m != MOVEMENT.CALIBRATE) {
            senseAndRepaint();
            System.out.println("senseAndRepaint");
        } else {
        	System.out.println("am i trying to recieve message??: Explorer.java, L541");
            CommMgr commMgr = CommMgr.getCommMgr();
            
            commMgr.revMsg();
            System.out.println("recieved message: Explorer.java, L545");
        }

        if (bot.getRealBot() && !calibrationMode) {
            calibrationMode = true;

            if (canCalibrateOnTheSpot(bot.getRobotCurDir())) {
            	System.out.println("last calibrate=0: Explorer.java, L552");
                lastCalibrate = 0;
                moveBot(MOVEMENT.CALIBRATE);
            } else {
            	System.out.println("last calibrate++: Explorer.java, L556");
                lastCalibrate++;
                if (lastCalibrate >= 5) {
                	System.out.println("lastCalibrate >= 5: Explorer.java, L559");
                    DIRECTION targetDir = getCalibrationDirection();
                    if (targetDir != null) {
                        lastCalibrate = 0;
                        System.out.println("reset Calibrate Counter: Explorer.java, L563");
                        calibrateBot(targetDir);
                    }
                }
            }
            calibrationMode = false;
        }
        System.out.println("out of movebot(): Explorer.java, L571");
    }

    /**
     * Sets the bot's sensors, processes the sensor data and repaints the map.
     */
    private void senseAndRepaint() {
    	bot.setSensors();
    	System.out.println("sensor set: Explorer.java, L578");
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        bot.sense(exMap, realMap);
        System.out.println("realbot sensing: Explorer.java, L586");
        exMap.repaint();
        System.out.println("repainted: Explorer.java, L588");
    }

    /**
     * Checks if the robot can calibrate at its current position given a direction.
     */
    private boolean canCalibrateOnTheSpot(DIRECTION botDir) {
        int row = bot.getRobotPosRow();
        int col = bot.getRobotPosCol();
        System.out.println("in calbrating on the spot: Explorer.java, L597");
        System.out.println("botDir: " + botDir);
        switch (botDir) {
            case NORTH:
                return exMap.gridder.getIsObstacleOrWall(row + 2, col - 1) && exMap.gridder.getIsObstacleOrWall(row + 2, col) && exMap.gridder.getIsObstacleOrWall(row + 2, col + 1);
            case EAST:
                return exMap.gridder.getIsObstacleOrWall(row + 1, col + 2) && exMap.gridder.getIsObstacleOrWall(row, col + 2) && exMap.gridder.getIsObstacleOrWall(row - 1, col + 2);
            case SOUTH:
                return exMap.gridder.getIsObstacleOrWall(row - 2, col - 1) && exMap.gridder.getIsObstacleOrWall(row - 2, col) && exMap.gridder.getIsObstacleOrWall(row - 2, col + 1);
            case WEST:
                return exMap.gridder.getIsObstacleOrWall(row + 1, col - 2) && exMap.gridder.getIsObstacleOrWall(row, col - 2) && exMap.gridder.getIsObstacleOrWall(row - 1, col - 2);
        }

        System.out.println("done calibrating on the spot: Explorer.java, L610");
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
        	System.out.println("calibrate direction: in case of Right turn: Explorer.java, L624");
    		return dirToCheck;
    	}

        dirToCheck = DIRECTION.getPrevious(origDir);                // left turn
        if (canCalibrateOnTheSpot(dirToCheck)) 
    	{
        	System.out.println("calibrate direction: in case of Left turn: Explorer.java, L631");
    		return dirToCheck;
    	}

        dirToCheck = DIRECTION.getPrevious(dirToCheck);             // u turn
        if (canCalibrateOnTheSpot(dirToCheck)) 
    	{
        	System.out.println("calibrate direction: in case of U turn: Explorer.java, L638");
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

    /**
     * Turns the robot to the required direction.
     */
    private void turnBotDirection(DIRECTION targetDir) {
        int numOfTurn = Math.abs(bot.getRobotCurDir().ordinal() - targetDir.ordinal());
        if (numOfTurn > 2) numOfTurn = numOfTurn % 2;

        if (numOfTurn == 1) {
            if (DIRECTION.getNext(bot.getRobotCurDir()) == targetDir) {
                moveBot(MOVEMENT.RIGHT);
            } else {
                moveBot(MOVEMENT.LEFT);
            }
        } else if (numOfTurn == 2) {
            moveBot(MOVEMENT.RIGHT);
            moveBot(MOVEMENT.RIGHT);
        }
    }
}
