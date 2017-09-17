package jframe_pkg.map;

import jframe_pkg.map.Cell;
import jframe_pkg.map.MapConstant;

import java.awt.*;

import javax.swing.JFrame;

public class Gridder extends JFrame {
	//init variables	
	private final Cell[][] grid;
	//private MapConstant map_constant = new MapConstant();
	
	public Gridder()
	{
        grid = new Cell[MapConstant.MAP_X][MapConstant.MAP_Y];
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col] = new Cell(row, col);

                // Set the virtual walls of the arena
                if (row == 0 || col == 0 || row == MapConstant.MAP_X - 1 || col == MapConstant.MAP_Y - 1) {
                    grid[row][col].setVirtualWall(true);
                }
            }
        }
	}
	
	public Cell[][] get_Grid()
	{
		return this.grid;
	}
	
    public boolean coordinate_validator(int row, int col) 
    {
        return row >= 0 && col >= 0 && row < MapConstant.MAP_X && col < MapConstant.MAP_Y;
    }
    
    public boolean in_start(int x, int y) 
    {
        return x >= 0 && x <= 2 && y >= 0 && y <= 2;
    }

    public boolean in_goal(int x, int y) 
    {
        return((x <= MapConstant.MAP_X + 1 && x >= MapConstant.MAP_X - 1) && (y <= MapConstant.MAP_Y + 1 && y >= MapConstant.MAP_Y - 1));
    }
    
    public Cell getCell(int x, int y) {
        return grid[x][y];
    }

    public boolean isObstacleCell(int x, int y) {
        return grid[x][y].getIsObstacle();
    }

    public boolean isVirtualWallCell(int x, int y) {
        return grid[x][y].getIsVirtualWall();
    }
    
    public boolean getIsObstacleOrWall(int x, int y) {
        return !coordinate_validator(x, y) || getCell(x, y).getIsObstacle();
    }
    
    /**
     * Sets all cells in the grid to an explored state.
     */
    public void setAllExplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                grid[row][col].setIsExplored(true);
            }
        }
    }

    /**
     * Sets all cells in the grid to an unexplored state except for the START & GOAL zone.
     */
    public void setAllUnexplored() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[0].length; col++) {
                if (in_start(row, col) || in_goal(row, col)) {
                    grid[row][col].setIsExplored(true);
                } else {
                    grid[row][col].setIsExplored(false);
                }
            }
        }
    }

    /**
     * Sets a cell as an obstacle and the surrounding cells as virtual walls or resets the cell and surrounding
     * virtual walls.
     */
    public void setObstacleCell(int row, int col, boolean obstacle) {
        if (obstacle && (in_start(row, col) || in_goal(row, col)))
            return;

        grid[row][col].setIsObstacle(obstacle);

        if (row >= 1) {
            grid[row - 1][col].setVirtualWall(obstacle);            // bottom cell

            if (col < MapConstant.MAP_Y - 1) {
                grid[row - 1][col + 1].setVirtualWall(obstacle);    // bottom-right cell
            }

            if (col >= 1) {
                grid[row - 1][col - 1].setVirtualWall(obstacle);    // bottom-left cell
            }
        }

        if (row < MapConstant.MAP_X - 1) {
            grid[row + 1][col].setVirtualWall(obstacle);            // top cell

            if (col < MapConstant.MAP_Y - 1) {
                grid[row + 1][col + 1].setVirtualWall(obstacle);    // top-right cell
            }

            if (col >= 1) {
                grid[row + 1][col - 1].setVirtualWall(obstacle);    // top-left cell
            }
        }

        if (col >= 1) {
            grid[row][col - 1].setVirtualWall(obstacle);            // left cell
        }

        if (col < MapConstant.MAP_Y - 1) {
            grid[row][col + 1].setVirtualWall(obstacle);            // right cell
        }
    }
    
    
}