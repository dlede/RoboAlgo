package jframe_pkg;

import static jframe_pkg.utils.MapDescriptor.loadMapFromDisk;
import static jframe_pkg.utils.MapDescriptor.generateMapDescriptor;

import jframe_pkg.algorithm.Explorer;
import jframe_pkg.map.Mapper;
import jframe_pkg.robot.RobotConstants;
import jframe_pkg.utils.CommMgr;
import jframe_pkg.robot.Robot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;

public class MainFrame extends JFrame {
	//TODO: create waypoint click system... input waypoints
	//TODO: speed orientation input as well, speed of robot
	
    private static JFrame _appFrame = null;         // application JFrame

    private static JPanel _mapCards = null;         // JPanel for map views
    private static JPanel _settings = null;          // JPanel for settings - right portion of console
    private static Container _container = null;
    
    private static Robot bot; //init robot

    private static Mapper r_Mapper = null;   //real map
    private static Mapper e_Mapper = null;  // explored map
    
    private static Explorer explorer = null; // explorer mount init
    public static boolean map_Load = true; // if map is loaded

    private static int timeLimit = 3600;            // TODO: time limit
    private static int coverageLimit = 300;         // TODO: coverage limit

    private static final CommMgr comm = CommMgr.getCommMgr(); // TODO: commsMgr
    private static final boolean realRun = false;
    
    //private boolean auto_mode = false; //auto mode false = manual, can use keystroke to move
    //private boolean fast_mode = false; //fast mode false = exploration

	public static void main(String[] args) {
        bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, realRun);
		
        if (realRun)
        {
			try {
				comm.setUpConnection("192.168.2.1", 8088); //ip address and port on rpi3
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }

        if (!realRun) {
            r_Mapper = new Mapper(bot);
            r_Mapper.gridder.setAllUnexplored();
            //r_Mapper.gridder.setAllExplored();
        }
    	e_Mapper = new Mapper(bot); //argument bot
		//e_Mapper.gridder.setAllUnexplored();
		explorer = new Explorer(e_Mapper, r_Mapper, bot, timeLimit, coverageLimit); //exmap, rmap, robot, coverage, time
        
		MainFrame frame = new MainFrame();
        if (map_Load) // TODO: fix is toggle map load is true, run robot
        {
    		explorer.runExploration();
    		generateMapDescriptor(e_Mapper);
    		//bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
    		//r_Mapper.repaint();
            //e_Mapper.repaint();
        }
	}
	
	//show main frame
	public MainFrame() {
        // Initialise main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("MDP Group 15 Main Frame");
        _appFrame.setSize(new Dimension(1000, 1000));
        _appFrame.setResizable(false);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());
        _settings = new JPanel();

        // Add _mapCards & _settings to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_settings, BorderLayout.EAST);
        //contentPane.add(_monitor, BorderLayout.WEST);
        //contentPane.add(_toggle, BorderLayout.PAGE_END);
        
        // Initialize the main map view
        initMainLayout();
        
        //buttons
        initSettingsLayout();
        
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
    private static void initMainLayout() {
        if (!realRun) {
        _mapCards.add(r_Mapper, "REAL_MAP");
	    }
	    _mapCards.add(e_Mapper, "EXPLORATION");
	     
	    CardLayout cl = ((CardLayout) _mapCards.getLayout());
	    if (!realRun) {
	        cl.show(_mapCards, "REAL_MAP");
	    } else {
	        cl.show(_mapCards, "EXPLORATION");
	    }
    }

    //Initialises the Right Panel: Settings Panel inclusive of Set Waypoints, Set Speed, Set Map.txt
    private static void initSettingsLayout() {
    	_settings.setLayout(new BoxLayout(_settings, BoxLayout.Y_AXIS));
    	addWaypointPanel();
    	addSpeedPanel();
    	addLoadMapButton();
    }
    
    private static void formatButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        //btn.setPreferredSize(new Dimension(200, 20));
    }
    
    private static void addLoadMapButton() {
        //if (!realRun) {
            // Load Map Button
    	JPanel map_panel = new JPanel(new FlowLayout());
    	JPanel map_input = new JPanel(new FlowLayout());
    	
    	JLabel map_label = new JLabel("Map: ");
    	
        JButton btn_LoadMap = new JButton("Load Map");
        formatButton(btn_LoadMap);
        
        final JTextField loadTF = new JTextField(15);
        loadTF.setText("map");
                 
        btn_LoadMap.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	//TODO: load map
                loadMapFromDisk(r_Mapper, loadTF.getText());
                map_Load = true; // set load map is true
                System.out.println("map loaded: " + map_Load);
            }
        });
        
        CardLayout cl = ((CardLayout) _mapCards.getLayout());
        cl.show(_mapCards, "REAL_MAP");
        //r_Mapper.repaint();
        
        e_Mapper.repaint();

        /**
        wp_input.add(wp_textfield);
        wp_panel.add(btn_Waypoints);
         **/
        	map_input.add(map_label);
        	map_input.add(loadTF);
            map_panel.add(btn_LoadMap);
            _settings.add(map_input);
            _settings.add(map_panel);
    }
    
    private static void addWaypointPanel() {
    	JPanel wp_panel = new JPanel(new FlowLayout());
    	//new BoxLayout(wp_panel, BoxLayout.PAGE_AXIS)
    	//wp_panel.setLayout(new BoxLayout(wp_panel, BoxLayout.Y_AXIS));
    	//wp_panel.add(Box.createRigidArea(new Dimension(0, 500)));
    	
    	JPanel wp_input = new JPanel(new FlowLayout());
    	
    	JLabel wp_label = new JLabel("Waypoints: ");
    	
    	JTextField field_x = new JTextField(5);
    	JTextField field_y = new JTextField(5);
    	
    	JPanel wp_textfield = new JPanel();
    	field_x.setText("0");
    	field_y.setText("0");
    	
    	String temp_x = field_x.getText(); 
    	String temp_y = field_y.getText(); 
    	int temp_ix = Integer.parseInt(temp_x);
    	int temp_iy = Integer.parseInt(temp_y);
    	
    	wp_textfield.add(field_x);
    	wp_textfield.add(field_y);
    	
    	//e_Mapper.set_waypoint(temp_ix, temp_iy);

        JButton btn_Waypoints = new JButton("Set Waypoints");
        formatButton(btn_Waypoints);
        btn_Waypoints.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	//TODO: set waypoint function on mapper e.g.
            	System.out.println("(" +"Waypoint: " + field_x.getText() + ", " + field_y.getText()+")");
            }
        });
        wp_input.add(wp_label);
        wp_input.add(wp_textfield);
        //wp_panel.add(wp_input);
        wp_panel.add(btn_Waypoints);
        _settings.add(wp_input);
        _settings.add(wp_panel);
    }
    
    private static void addSpeedPanel() {
    	JPanel spd_panel = new JPanel(new FlowLayout());
    	JPanel spd_input = new JPanel(new FlowLayout());
    	
    	JLabel spd_label = new JLabel("Speed: ");
    	
    	JTextField field_spd = new JTextField(20);
    	
    	JPanel spd_textfield = new JPanel();
    	spd_textfield.add(field_spd);
    	
        JButton btn_Speed = new JButton("Set Speed");
        formatButton(btn_Speed);
        btn_Speed.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	//TODO: set speed function on robot e.g.
            	System.out.println("Speed: " + field_spd.getText());
            }
        });
        spd_input.add(spd_label);
        spd_input.add(spd_textfield);
        spd_panel.add(btn_Speed);
        _settings.add(spd_input);
        _settings.add(spd_panel);
    }
    

}