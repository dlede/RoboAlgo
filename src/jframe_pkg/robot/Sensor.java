package jframe_pkg.robot;

import jframe_pkg.map.Mapper;
import jframe_pkg.map.Gridder;
import jframe_pkg.robot.RobotConstants.DIRECTION;

/**
 * Represents a sensor mounted on the robot.
 *
 * @author - MDP group 15 with references 
 */

public class Sensor {
    private final int lowerRange;
    private final int upperRange;
    private int sensorPosRow;
    private int sensorPosCol;
    private DIRECTION sensorDir;
    private final String id;

    public Sensor(int lowerRange, int upperRange, int row, int col, DIRECTION dir, String id) {
        this.lowerRange = lowerRange;
        this.upperRange = upperRange;
        this.sensorPosRow = row;
        this.sensorPosCol = col;
        this.sensorDir = dir;
        this.id = id;
    }

    public void setSensor(int row, int col, DIRECTION dir) {
        this.sensorPosRow = row;
        this.sensorPosCol = col;
        this.sensorDir = dir;
    }

    /**
     * Returns the number of cells to the nearest detected obstacle or -1 if no obstacle is detected.
     */
    public int sense(Mapper exploredMap, Mapper realMap) {
        switch (sensorDir) {
            case NORTH:
                return getSensorVal(exploredMap, realMap, 1, 0);
            case EAST:
                return getSensorVal(exploredMap, realMap, 0, 1);
            case SOUTH:
                return getSensorVal(exploredMap, realMap, -1, 0);
            case WEST:
                return getSensorVal(exploredMap, realMap, 0, -1);
        }
        return -1;
    }

    /**
     * Sets the appropriate obstacle cell in the map and returns the row or column value of the obstacle cell. Returns
     * -1 if no obstacle is detected.
     */
    private int getSensorVal(Mapper exploredMap, Mapper realMap, int rowInc, int colInc) {
        // Check if starting point is valid for sensors with lowerRange > 1.
        if (lowerRange > 1) {
            for (int i = 1; i < this.lowerRange; i++) {
                int row = this.sensorPosRow + (rowInc * i);
                int col = this.sensorPosCol + (colInc * i);

                if (!exploredMap.gridder.coordinate_validator(row, col)) return i;
                if (realMap.gridder.getCell(row, col).getIsObstacle()) return i;
            }
        }

        // Check if anything is detected by the sensor and return that value.
        for (int i = this.lowerRange; i <= this.upperRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            if (!exploredMap.gridder.coordinate_validator(row, col)) return i;

            exploredMap.gridder.getCell(row, col).setIsExplored(true);

            if (realMap.gridder.getCell(row, col).getIsObstacle()) {
                exploredMap.gridder.setObstacleCell(row, col, true);
                return i;
            }
        }
    	
    	/*
        for (int i = this.lowerRange; i <= this.upperRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            if (!exploredMap.gridder.coordinate_validator(row, col)) return i;
            
            // hk- Set the cell to be explored
            if(exploredMap.gridder.getCell(row, col).getIsObstacle()) // 
            {
            	break;
            	//exploredMap.gridder.getCell(row, col).setIsExplored(true); //to explore, to make the grid white, black...
            }
            
            //if(exploredMap.gridder.getCell(row, col).getIsObstacle()) // 
            //{
            	//break;
            	//exploredMap.gridder.getCell(row, col).setIsExplored(true); //to explore, to make the grid white, black...
            //}
            
            // hk- set the obstacles 
            if (realMap.gridder.getCell(row, col).getIsObstacle()) {
                exploredMap.gridder.setObstacleCell(row, col, true);
                return i;
            }
        }*/
        return -1;
    }

    /**
     * Uses the sensor direction and given value from the actual sensor to update the map.
     */
    public void senseReal(Gridder exploredMap, int sensorVal) {
    	//System.out.println("sensorDir: " + sensorDir);
        switch (sensorDir) {
            case NORTH:
                // direction determins the rowInc and colInc
                processSensorVal(exploredMap, sensorVal, 1, 0);
                break;
            case EAST:
                processSensorVal(exploredMap, sensorVal, 0, 1);
                break;
            case SOUTH:
                processSensorVal(exploredMap, sensorVal, -1, 0);
                break;
            case WEST:
                processSensorVal(exploredMap, sensorVal, 0, -1);
                break;
        }
    }

    /**
     * Sets the correct cells to explored and/or obstacle according to the actual sensor value.
     */
    private void processSensorVal(Gridder exploredMap, int sensorVal, int rowInc, int colInc) {
        if (sensorVal == 0) return;  // return value for LR sensor if obstacle before lowerRange
        //System.out.println("process Sensor Val");
        // If lowerRange > 1, exit from method if there is an obstacle before the sensor's range starts
        for (int i = 1; i < this.lowerRange; i++) {
            //hk - rowInc and colInc are multipliers to the sensor range (i think)
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);
            //System.out.println("lower-row: " + row + ", lower-col: " + col);
            //System.out.println("lower-ranging: " + i);

            if (exploredMap.getCell(row, col).getIsObstacle()) 
        	{
            	System.out.println("return if obstacle");
        		return;
        	}
        }
        //System.out.println("lower range");

        for (int i = this.lowerRange; i <= this.upperRange; i++) {
            int row = this.sensorPosRow + (rowInc * i);
            int col = this.sensorPosCol + (colInc * i);

            // makes sure within boundary
            if (!exploredMap.coordinate_validator(row, col)) continue;

            exploredMap.getCell(row, col).setIsExplored(true);

            if (sensorVal == i) {
                exploredMap.setObstacleCell(row, col, true);
                break;
            }

            //
            if (exploredMap.getCell(row, col).getIsObstacle()) {
                if (id.equals("SRFL") || id.equals("SRFC") || id.equals("SRFR") || id.equals("SRL")) {
                    exploredMap.setObstacleCell(row, col, false);
                } else {
                    break;
                }
            }
        }
        //System.out.println("upper range");
    }
}
