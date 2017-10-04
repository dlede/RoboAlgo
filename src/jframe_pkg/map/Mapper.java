package jframe_pkg.map;

//import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.TimeUnit;

import javax.swing.JPanel;
import javax.swing.Timer;

import jframe_pkg.map.Gridder;
import jframe_pkg.map.MapConstant;
import jframe_pkg.robot.Robot;
import jframe_pkg.robot.RobotConstants;
import jframe_pkg.robot.RobotConstants.DIRECTION;

import static java.lang.Math.PI;
import static java.lang.Math.sin;
import static java.lang.Math.cos;

public class Mapper extends JPanel {

	public Gridder gridder = new Gridder();
	private final Robot bot;
	private float angle = 0;
	
	public Mapper(Robot bot) //bot, gridder
	{
		this.bot = bot;
		this.gridder = gridder;
		//this.repaint();
	}
    
    /**
     * Overrides JComponent's paintComponent() method. It creates a two-dimensional array of _DisplayCell objects
     * to store the current map state. Then, it paints square cells for the grid with the appropriate colors as
     * well as the robot on-screen.
     */
    public void paintComponent(Graphics g) {
    	super.paintComponent(g);
    	
    	
        // Just use the transform that's already in the graphics.
        Graphics2D g2 = (Graphics2D) g.create();
        Point p = null;
        //g2.setToIdentity();
        // The code for your other transforms is garbled in my browser. Fill in here.
        //g2.rotate(angle);
        //g2.drawImage(arm, t, null);
        
        // Create a two-dimensional array of _DisplayCell objects for rendering.
        _DisplayCell[][] _mapCells = new _DisplayCell[MapConstant.MAP_X][MapConstant.MAP_Y];
        for (int x = 0; x < MapConstant.MAP_X; x++) {
            for (int y = 0; y < MapConstant.MAP_Y; y++) {
                _mapCells[x][y] = new _DisplayCell(y * GraphicsConstant.CELL_SIZE, x * GraphicsConstant.CELL_SIZE, GraphicsConstant.CELL_SIZE);
            }
        }

        // Paint the cells with the appropriate colors.
        for (int x = 0; x < MapConstant.MAP_X; x++) {
            for (int y = 0; y < MapConstant.MAP_Y; y++) {
                Color cellColor;

                if (gridder.in_start(x, y))
                    cellColor = GraphicsConstant.C_START;
                else if (gridder.in_goal(x, y)) //TODO: fix the goal state paint 3x3
                    cellColor = GraphicsConstant.C_GOAL;
                else if (gridder.in_waypoint(x, y)) 
                		cellColor = GraphicsConstant.C_WAYPOINT; 
                else {
                    if (!gridder.get_Grid()[x][y].getIsExplored())
                        cellColor = GraphicsConstant.C_UNEXPLORED;
                    else if (gridder.get_Grid()[x][y].getIsObstacle())
                        cellColor = GraphicsConstant.C_OBSTACLE;
                    else
                        cellColor = GraphicsConstant.C_FREE;
                }

                g.setColor(cellColor);
                g.fillRect(_mapCells[x][y].cellX + GraphicsConstant.MAP_X_OFFSET, _mapCells[x][y].cellY, _mapCells[x][y].cellSize, _mapCells[x][y].cellSize);
            }
        }

        // Paint the robot on-screen.
        g.setColor(GraphicsConstant.C_ROBOT);
        int r = bot.getRobotPosRow();
        int c = bot.getRobotPosCol();
        g.fillOval((c - 1) * GraphicsConstant.CELL_SIZE + GraphicsConstant.ROBOT_X_OFFSET + GraphicsConstant.MAP_X_OFFSET, GraphicsConstant.MAP_H - (r * GraphicsConstant.CELL_SIZE + GraphicsConstant.ROBOT_Y_OFFSET), GraphicsConstant.ROBOT_W, GraphicsConstant.ROBOT_H);
		
        //TODO: rotation test
        int diameter = Math.min(GraphicsConstant.ROBOT_W, GraphicsConstant.ROBOT_H);
        int x = (getWidth() - diameter) / 2;
        int y = (getHeight() - diameter) / 2;

        g2.setColor(Color.RED);
        float innerDiameter = 20;
        
        //Paint the robot's direction indicator on-screen.
        g.setColor(GraphicsConstant.C_ROBOT_DIR);
        RobotConstants.DIRECTION d = bot.getRobotCurDir();
        switch (d) {
            case NORTH:
            	//depending how fast the turn speed delay is
            	//TimeUnit.MILLISECONDS.sleep(turnSpeed);
            	
            	//TODO: Project Polygon Rotation
            	//targetted_angle = 0
            	// bot angle can only be -90 (West), or 90 (East)
            	//if bot.angle >= targetted_angle, North to East, Clockwise, turnRight()
            	//if bot.angle <= targetted_angle, North to West, Counter Clockwise, turnLeft()

            	DIRECTION.getPrevious(bot.getRobotDir());
            	System.out.println("Direction: " + DIRECTION.getPrevious(bot.getRobotDir()));
            	
            	//p = getPointOnCircle(90, (GraphicsConstant.ROBOT_DIR_W / 2f));
            	
            	Point n_p = new Point((c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+6),(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-25));
            	Point w_p = new Point((c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+6),(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-15));
            	Point e_p = new Point((c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+26),(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-15));
            	
            	Point north_temp_point = rotate_point(bot.getRobotPosRow(),bot.getRobotPosCol(),90,n_p); // shift to EAST?
            	Point west_temp_point = rotate_point(bot.getRobotPosRow(),bot.getRobotPosCol(),90,w_p);
            	Point east_temp_point = rotate_point(bot.getRobotPosRow(),bot.getRobotPosCol(),90,e_p);
            	
            	g.fillPolygon(new int[] {c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+6, // West Point x
            							(c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+26), // East Point X
            							c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+16}, // North Point x
            				new int[] {(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-15), // West Point y
            						(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-15),//  East Point y
	            						(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-25)}, // North Point y
            				3);
            	
            	//System.out.println("North Rotate Point: " + north_temp_point);
            	//System.out.println("West Rotate Point: " + west_temp_point);
            	//System.out.println("East Rotate Point: " + east_temp_point);
            	
            	//System.out.println("North, Polygon North Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+16) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-25) + ")");
            	//System.out.println("North, Polygon West Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+6) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-15) + ")");
            	//System.out.println("North, Polygon East Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+26) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE-15) + ")");
            	
                break;
            case EAST:
                //g.fillOval(c * GraphicsConstant.CELL_SIZE + 35 + GraphicsConstant.MAP_X_OFFSET, GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE + 10, GraphicsConstant.ROBOT_DIR_W, GraphicsConstant.ROBOT_DIR_H);
                
            	DIRECTION.getPrevious(bot.getRobotDir());
            	System.out.println("Direction: " + DIRECTION.getPrevious(bot.getRobotDir()));
            	
            	//p = getPointOnCircle(90, (diameter / 2f) - (innerDiameter / 2));
            	
            	g.fillPolygon(new int[] {c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+46, // West Point x
						(c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+46), // East Point X
						c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+56}, // North Point x
						new int[] {(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+5), // West Point y
								(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+25),//  East Point y
									(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+15)}, // North Point y
						3);
                
            	//System.out.println("North, Polygon North Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+56) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+15) + ")");
            	//System.out.println("North, Polygon West Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+46) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+5) + ")");
            	//System.out.println("North, Polygon East Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+46) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+25) + ")");
            	
            	
            	//TODO: if turning to North, rotate -90, turning counter clockwise
            	
            	//TODO: if turning to South, rotate 90, turning clockwise
            	
                break;
            case SOUTH:
                //g.fillOval(c * GraphicsConstant.CELL_SIZE + 10 + GraphicsConstant.MAP_X_OFFSET, GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE + 35, GraphicsConstant.ROBOT_DIR_W, GraphicsConstant.ROBOT_DIR_H);
                
            	DIRECTION.getPrevious(bot.getRobotDir());
            	System.out.println("Direction: " + DIRECTION.getPrevious(bot.getRobotDir()));
            	
            	//p = getPointOnCircle(90, (diameter / 2f) - (innerDiameter / 2));
            	
            	//South Pointing Arrow
            	g.fillPolygon(new int[] {c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+6, // West Point x
						(c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+26), // East Point X
						c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+16}, // North Point x
						new int[] {(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+46), // West Point y
								(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+46),//  East Point y
									(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+56)}, // North Point y
						3);
            	
            	//System.out.println("North, Polygon North Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+16) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+56) + ")");
            	//System.out.println("North, Polygon West Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+6) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+46) + ")");
            	//System.out.println("North, Polygon East Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET+26) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+46) + ")");
            	
            	//TODO: if turning to East, rotate -90, turning counter clockwise
            	
            	//TODO: if turning to West, rotate 90, turning clockwise
                
                break;
            case WEST:
                //g.fillOval(c * GraphicsConstant.CELL_SIZE - 15 + GraphicsConstant.MAP_X_OFFSET, GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE + 10, GraphicsConstant.ROBOT_DIR_W, GraphicsConstant.ROBOT_DIR_H);
            	
            	DIRECTION.getPrevious(bot.getRobotDir());
            	System.out.println("Direction: " + DIRECTION.getPrevious(bot.getRobotDir()));
            	
            	//p = getPointOnCircle(90, (diameter / 2f) - (innerDiameter / 2));
            	
            	g.fillPolygon(new int[] {c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET-16, // West Point x
						(c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET-16), // East Point X
						c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET-26}, // North Point x
						new int[] {(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+5), // West Point y
								(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+25),//  East Point y
									(GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+15)}, // North Point y
						3);
            	
            	//System.out.println("North, Polygon North Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET-26) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+15) + ")");
            	//System.out.println("North, Polygon West Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET-16) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+5) + ")");
            	//System.out.println("North, Polygon East Point: " + "(" + (c * GraphicsConstant.CELL_SIZE + GraphicsConstant.MAP_X_OFFSET-16) + ", " + (GraphicsConstant.MAP_H - r * GraphicsConstant.CELL_SIZE+15) + ")");
            	
            	//TODO: if turning to South, rotate -90, turning counter clockwise
            	
            	//TODO: if turning to North, rotate 90, turning clockwise
            	
            	break;
        }

        //g2.dispose();
    }
    
    
    
    
    
    private class _DisplayCell {
        public final int cellX;
        public final int cellY;
        public final int cellSize;

        public _DisplayCell(int borderX, int borderY, int borderSize) {
            this.cellX = borderX + GraphicsConstant.CELL_LINE_WEIGHT;
            this.cellY = GraphicsConstant.MAP_H - (borderY - GraphicsConstant.CELL_LINE_WEIGHT);
            this.cellSize = borderSize - (GraphicsConstant.CELL_LINE_WEIGHT * 2);
        }
    }
    
    public void turnRight(Graphics g)
    {
    	
    	//TODO: turn right function, turn clockwise
        //angle += Math.toRadians(5); // 5 degrees per 100 ms = 50 degrees/second
        //while (angle > 2 * Math.PI) 
         //   angle -= 2 * Math.PI;  // keep angle in reasonable range.
        Timer timer = new Timer(40, new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                angle += 0.5f;
                //g.paint();
            }
        });
        timer.start();
        
    }
    
    public void turnLeft()
    {
    	//TODO: turn left function, turn counter-clockwise
    	
    }
    
    protected Point getPointOnCircle(float degress, float radius) {

        int x = Math.round(getWidth() / 2);
        int y = Math.round(getHeight() / 2);

        double rads = Math.toRadians(degress - 90); // 0 becomes the top

        // Calculate the outer point of the line
        int xPosy = Math.round((float) (x + Math.cos(rads) * radius));
        int yPosy = Math.round((float) (y + Math.sin(rads) * radius));

        return new Point(xPosy, yPosy);

    }
    
    Point rotate_point(float cx,float cy,float angle,Point p)
    {
      float s = (float) sin(angle);
      float c = (float) cos(angle);

      // translate point back to origin:
      p.x -= cx;
      p.y -= cy;

      // rotate point
      float xnew = p.x * c - p.y * s;
      float ynew = p.x * s + p.y * c;

      // translate point back:
      p.x = (int) (xnew + cx);
      p.y = (int) (ynew + cy);
      return p;
    }

}
