package jframe_pkg.map;

import jframe_pkg.map.MapConstant;

public class Cell {
    private final int x;
    private final int y;
	private boolean is_obstacle;
	private boolean is_explored; 
	private boolean is_boundary; //virtual wall
	private boolean is_waypoint;
    
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int get_x() { // x coordinates, horizontal
        return this.x;
    }

    public int get_y() { // y coordinates, vertical
        return this.y;
    }

    public void setVirtualWall(boolean val) {
        if (val) {
            this.is_boundary = true;
        } else {
            if (x != 0 && x != MapConstant.MAP_X - 1 && y != 0 && y != MapConstant.MAP_Y - 1) {
                this.is_boundary = false;
            }
        }
    }
    
    //set a new boundary 
    public void setInnerVirtualWall(boolean val) {
        if (val) {
            this.is_boundary = true;
        } else {
        	// (x != 0 && x != MapConstant.MAP_X - 1 && y != 0 && y != MapConstant.MAP_Y - 1) ||
        	// (x != 1 && x != MapConstant.MAP_X - 2 && y != 1 && y != MapConstant.MAP_Y - 2) ||
        	// (x != 2 && x != MapConstant.MAP_X - 3 && y != 2 && y != MapConstant.MAP_Y - 3)
        	
            //if (x != 0 && x != MapConstant.MAP_X - 3 && y != 0 && y != MapConstant.MAP_Y - 3) 
        	if ((x != 0 && x != MapConstant.MAP_X - 1 && y != 0 && y != MapConstant.MAP_Y - 1) || (x != 1 && x != MapConstant.MAP_X - 2 && y != 1 && y != MapConstant.MAP_Y - 2) || (x != 2 && x != MapConstant.MAP_X - 3 && y != 2 && y != MapConstant.MAP_Y - 3))
        	//if (x != 0 && x != 1 && x != 2 && x != MapConstant.MAP_X - 1 && x != MapConstant.MAP_X - 2 && x != MapConstant.MAP_X - 3 && y != 0 && y != 1 && y != 2 && y != MapConstant.MAP_Y - 1 && y != MapConstant.MAP_Y - 2 && y != MapConstant.MAP_Y - 3) 
        	{
        		this.is_boundary = false;
        	}
        	
            //if((0 < x || x <= 2) && ((MapConstant.MAP_X - 1) > x || (x > MapConstant.MAP_X - 2)) && (2 >= y || y > 0) && ((MapConstant.MAP_Y - 1) > y || (y > (MapConstant.MAP_Y - 2))))
            //{
            //    this.is_boundary = false;
            //}
        }
    }

    public boolean getIsVirtualWall() {
        return this.is_boundary;
    }
    
    // set/get for is explored
    public void setIsExplored(boolean b_exp) {
        this.is_explored = b_exp;
    }

    public boolean getIsExplored() {
        return this.is_explored;
    }
    
    // set/get for waypoints
    public void setIsWaypoint(boolean b_wp) {
        this.is_waypoint = b_wp;
    }

    public boolean getIsWaypoint() {
        return this.is_waypoint;
    }
    
    // set/get for obstacle
    public void setIsObstacle(boolean b_obs) {
        this.is_obstacle = b_obs;
    }

    public boolean getIsObstacle() {
        return this.is_obstacle;
    }
}
