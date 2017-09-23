package jframe_pkg;

import static jframe_pkg.utils.MapDescriptor.loadMapFromDisk;
import static jframe_pkg.utils.MapDescriptor.generateMapDescriptor;

import jframe_pkg.algorithm.Explorer;
import jframe_pkg.algorithm.Sprinter;
import jframe_pkg.map.Mapper;
import jframe_pkg.robot.RobotConstants;
import jframe_pkg.utils.CommMgr;
import jframe_pkg.robot.Robot;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
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
    private static JPanel _stepsInfo = null;		// JPanel for steps info
    private static JPanel _modeSettings = null;			//JPanel for end page
    private static Container _container = null;
    
    private static Robot bot; //init robot

    private static Mapper r_Mapper = null;   //real map
    private static Mapper e_Mapper = null;  // explored map
    
    private static Explorer explorer = null; // explorer mount init
    public static boolean map_Load = false; // if map is loaded

    private static int timeLimit = 3600;            // TODO: time limit
    private static int coverageLimit = 300;         // TODO: coverage limit

    private static final CommMgr comm = CommMgr.getCommMgr();
    private static final boolean realRun = false;
    
    private static boolean auto_mode = false; //auto mode false = manual, can use keystroke to move
    private static boolean fast_mode = false; //fast mode false = exploration

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
        frame.setVisible(true);
	}
	
	//show main frame
	public MainFrame() {
        // Initialise main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("MDP Group 15 Main Frame");
        _appFrame.setSize(new Dimension(1100, 750));
        _appFrame.setResizable(false);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());
        _settings = new JPanel();
        _stepsInfo = new JPanel();
        _modeSettings = new JPanel();
        //_mapCards.add(_settings, BorderLayout.WEST);

        // Add _mapCards & _settings to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_settings, BorderLayout.EAST);
        contentPane.add(_stepsInfo, BorderLayout.WEST);
        contentPane.add(_modeSettings, BorderLayout.SOUTH);
        //contentPane.add(_monitor, BorderLayout.WEST);
        //contentPane.add(_toggle, BorderLayout.PAGE_END);
        
        // Initialize the main map view
        initMainLayout();
        
        //buttons
        initSettingsLayout();
        
        //steps info
        initStepsLayout();
        
        //mode btns
        initModeBtnLayout();
        
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
       // _settings.setLayout(new GridLayout(3, 0, 10, 10)); // 3 rows: set waypoint, set speed, set map
        //_settings.setLayout(new FlowLayout());
    	
    	//_settings.setLayout(new BorderLayout());
    	_settings.setLayout(new BoxLayout(_settings, BoxLayout.Y_AXIS));
    	/*Border border = BorderFactory.createLineBorder(Color.BLACK); //create border
    	_settings.setBorder(BorderFactory.createCompoundBorder(border, 
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
    	*/
    	_settings.setBorder(new EmptyBorder(30, 10, 10, 20));
    	addWaypointPanel();
    	addSpeedPanel();
    	addLoadMapButton();
    	addTimerPanel();

    }
    
    private static void formatButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
        //btn.setPreferredSize(new Dimension(200, 20));
    }
    
    private static void addLoadMapButton() {
        if (!realRun) {
        
    	//JPanel map_panel = new JPanel(new FlowLayout());
    	JPanel map_panel = new JPanel(new GridLayout(3, 1));
    	JLabel map_label = new JLabel("Map: ");
    	JTextField map_field = new JTextField(15);
    	
            JButton btn_LoadMap = new JButton("Load Map");
            formatButton(btn_LoadMap);
            btn_LoadMap.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    loadMapFromDisk(r_Mapper, map_field.getText());
                    CardLayout cl = ((CardLayout) _mapCards.getLayout());
                    cl.show(_mapCards, "REAL_MAP");
                    e_Mapper.repaint();
                    map_Load = true;
                    System.out.println("Map Loaded: " + map_Load);
                }
            });
            map_panel.add(map_label);
            map_panel.add(map_field);
            map_panel.add(btn_LoadMap);
            map_panel.setBorder(new EmptyBorder(0, 0, 40, 0));
            //Set Padding Size
            map_panel.setMaximumSize(map_panel.getPreferredSize());
            map_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
            _settings.add(map_panel, BorderLayout.NORTH);
        }
    }
    
    private static void addWaypointPanel() {
    	
    	//create panel
    	JPanel wp_panel = new JPanel(new GridLayout(3, 1));
    	//wp_panel.setLayout(new BoxLayout(wp_panel, BoxLayout.Y_AXIS));
    	
    	//create label
    	JLabel wp_label = new JLabel("Waypoints: ");
    	
    	//Create field
    	JPanel wp_textfield = new JPanel();
    	JTextField field_x = new JTextField(5);
    	JTextField field_y = new JTextField(5);
    	
    	field_x.setText("0");
    	field_y.setText("0");
    	

        //Align items
    	wp_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
    	
    	
    	wp_textfield.add(field_x);
    	wp_textfield.add(field_y);
    	

    	//create btn
    	JButton btn_Waypoints = new JButton("Set Waypoints");
    	
        formatButton(btn_Waypoints);
        btn_Waypoints.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	//TODO: set waypoint function on mapper e.g.
            	System.out.println("(" +"Waypoint: " + field_x.getText() + ", " + field_y.getText()+")");
            }
        });
    	
        //add items to panel
        wp_panel.add(wp_label);
        wp_panel.add(wp_textfield);
        wp_panel.add(btn_Waypoints);

        
        //Align items
        wp_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        wp_panel.setBorder(new EmptyBorder(0, 0, 40, 0));
        //Set Padding Size
        wp_panel.setMaximumSize(wp_panel.getPreferredSize());
        _settings.add(wp_panel);
    	
    	
    	/*
    	//JPanel wp_panel = new JPanel(new FlowLayout());
    	JPanel wp_panel = new JPanel();
    	wp_panel.setLayout(new BoxLayout(wp_panel, BoxLayout.Y_AXIS));
    	
    	//new BoxLayout(wp_panel, BoxLayout.PAGE_AXIS)
    	//wp_panel.setLayout(new BoxLayout(wp_panel, BoxLayout.Y_AXIS));
    	//wp_panel.add(Box.createRigidArea(new Dimension(0, 500)));
    	
    	//JPanel wp_input = new JPanel(new FlowLayout());
    	
    	JLabel wp_label = new JLabel("Waypoints: ");
    	
    	JTextField field_x = new JTextField(5);
    	JTextField field_y = new JTextField(5);
    	
    	JPanel wp_textfield = new JPanel();
    	field_x.setText("0");
    	field_y.setText("0");
    	
    	wp_textfield.add(field_x);
    	wp_textfield.add(field_y);

    	wp_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JButton btn_Waypoints = new JButton("Set Waypoints");
        formatButton(btn_Waypoints);
        btn_Waypoints.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	//TODO: set waypoint function on mapper e.g.
            	System.out.println("(" +"Waypoint: " + field_x.getText() + ", " + field_y.getText()+")");
            }
        });
        
        wp_panel.add(wp_label);
        wp_panel.add(wp_textfield);
        //wp_panel.add(wp_input);
        wp_panel.add(btn_Waypoints);

        //Set Padding Size
        wp_panel.setMaximumSize(wp_panel.getPreferredSize());
        //wp_input.setAlignmentX(Component.LEFT_ALIGNMENT);
        wp_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        _settings.add(wp_panel);*/
    }
    
    private static void addSpeedPanel() {
    	
    	//create panel
    	JPanel spd_panel = new JPanel(new GridLayout(3, 1));
    	//spd_panel.setLayout(new BoxLayout(spd_panel,BoxLayout.Y_AXIS));
    
    	
    	
    	//create speed label
    	JLabel spd_label = new JLabel("Speed: ");
    	
    	//create speed field
    	JTextField field_spd = new JTextField(10);
    	
    	//create speed btn
    	JButton btn_Speed = new JButton("Set Speed");
        formatButton(btn_Speed);
        btn_Speed.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
            	//TODO: set speed function on robot e.g.
            	System.out.println("Speed: " + field_spd.getText());
            }
        });
        
        //align left
        spd_panel.setAlignmentX(Component.LEFT_ALIGNMENT);

        spd_panel.add(spd_label);
        spd_panel.add(field_spd);
        spd_panel.add(btn_Speed);

        spd_panel.setBorder(new EmptyBorder(0, 0, 40, 0));
        //Set Padding Size
        spd_panel.setMaximumSize(spd_panel.getPreferredSize());
        
        _settings.add(spd_panel);
        
    	
    	/*
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
        //Set Padding Size
        spd_input.setMaximumSize(spd_input.getPreferredSize());
        spd_panel.setMaximumSize(spd_panel.getPreferredSize());

        //align left
        spd_input.setAlignmentX(Component.LEFT_ALIGNMENT);
        spd_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        _settings.add(spd_input);
        _settings.add(spd_panel);*/
    }

    private static void addTimerPanel(){
    	//Timer stopwatch = new Timer();
    	
    	JPanel timer_panel = new JPanel(new FlowLayout());
    	
    	JLabel timer_label = new JLabel("Timer ");
    	
    	JTextField field_timer = new JTextField(10);
    	
    	timer_panel.add(timer_label);
    	timer_panel.add(field_timer);
    	timer_panel.setMaximumSize(timer_panel.getPreferredSize());

        //align left
    	timer_panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    	_settings.add(timer_panel);
    	
    	
    	
    }
    
    
    //West Panel    
    private static void initStepsLayout(){

    	_stepsInfo.setLayout(new BoxLayout(_stepsInfo, BoxLayout.Y_AXIS));
    	//_stepsInfo.setLayout(new FlowLayout());
    	_stepsInfo.setBorder(new EmptyBorder(30, 20, 10, 0));
    	
    	addConsolePanel();
    	addNxtStepPanel();
    	addCurPosPanel();
    }
    
    private static void addConsolePanel(){
    	
    	JPanel infoPanel = new JPanel(new BorderLayout());
        JTextArea info = new JTextArea(25, 20);
        Border border = BorderFactory.createLineBorder(Color.BLACK); //create border
        
        
        info.setLineWrap(true);
        info.setEditable(false);
        info.setWrapStyleWord(true);
        info.setBorder(BorderFactory.createCompoundBorder(border, 
                    BorderFactory.createEmptyBorder(10, 10, 10, 10)));
        info.setAutoscrolls(true);
        infoPanel.add(info);
        infoPanel.setBorder(new EmptyBorder(0, 0, 40, 0));
        //Set Padding Size
        infoPanel.setMaximumSize(infoPanel.getPreferredSize());
        
        _stepsInfo.add(infoPanel); 
        
    	
    }

    private static void addNxtStepPanel(){
    	JPanel ns_panel = new JPanel(new GridLayout(2, 1));
    	
    	//JLabel ns_label = new JLabel("Next Step: ");
    	
    	//JTextField field_ns = new JTextField(5);
    	
    	//ns_panel.add(ns_label);
    	//ns_panel.add(field_ns);
    	//ns_panel.setMaximumSize(ns_panel.getPreferredSize());
        //_stepsInfo.add(ns_panel);
    	
    	JButton btn_NxtStep = new JButton("Next Step");
        formatButton(btn_NxtStep);
        ns_panel.add(btn_NxtStep);
        ns_panel.setMaximumSize(ns_panel.getPreferredSize());
        _stepsInfo.add(ns_panel);
    	
    	
    }

    private static void addCurPosPanel(){
    	
    	JPanel cp_panel = new JPanel(new GridLayout(0, 1));
    	
    	JLabel cp_label = new JLabel("Current Position: ");
    	
    	JTextField field_cp = new JTextField(5);
    	
    	cp_panel.add(cp_label);
    	cp_panel.add(field_cp);
    	cp_panel.setMaximumSize(cp_panel.getPreferredSize());
        _stepsInfo.add(cp_panel);
    }

    //South Panel
    private static void initModeBtnLayout(){
    	
    	_modeSettings.setLayout(new FlowLayout());
    	addModePanel();
    	
    }
    
    private static void addModePanel(){
    	
        //TODO: Change this portion onwards
    	
        // FastestPath Class for Multithreading
        class FastestPath extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                e_Mapper.repaint();

                if (realRun) {
                    while (true) {
                        System.out.println("Waiting for FP_START...");
                        String msg = comm.revMsg();
                        if (msg.equals(CommMgr.FP_START)) break;
                    }
                }

                Sprinter fastestPath;
                fastestPath = new Sprinter(e_Mapper, bot);

                fastestPath.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);

                return 222;
            }
        }
        
        // Exploration Class for Multithreading
        class Exploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                int row, col;

                row = RobotConstants.START_ROW;
                col = RobotConstants.START_COL;

                bot.setRobotPos(row, col);
                e_Mapper.repaint();

                Explorer exploration;
                exploration = new Explorer(e_Mapper, r_Mapper, bot, coverageLimit, timeLimit);

                if (realRun) {
                    CommMgr.getCommMgr().sendMsg(CommMgr.BOT_START);
                }

                exploration.runExploration();
                generateMapDescriptor(e_Mapper);

                if (realRun) {
                    new FastestPath().execute();
                }

                return 111;
            }
        }
        
        // CoverageExploration Class for Multithreading
        class CoverageExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                e_Mapper.repaint();

                Explorer coverageExplo = new Explorer(e_Mapper, r_Mapper, bot, coverageLimit, timeLimit);
                coverageExplo.runExploration();

                generateMapDescriptor(e_Mapper);

                return 444;
            }
        }
        
        // TimeExploration Class for Multithreading
        class TimeExploration extends SwingWorker<Integer, String> {
            protected Integer doInBackground() throws Exception {
                bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
                e_Mapper.repaint();

                Explorer timeExplo = new Explorer(e_Mapper, r_Mapper, bot, coverageLimit, timeLimit);
                timeExplo.runExploration();

                generateMapDescriptor(e_Mapper);

                return 333;
            }
        }
    	
    	JPanel mode_panel = new JPanel(new FlowLayout());

    	JLabel exp_label = new JLabel("Exploration Mode: ");
    	
    	JToggleButton toggleButton = new JToggleButton("OFF");
    	ItemListener itemListener = new ItemListener() {
    		
    	    public void itemStateChanged(ItemEvent itemEvent) {
    	        int state = itemEvent.getStateChange();
    	        if (state == ItemEvent.SELECTED) {
    	            System.out.println("On"); // show your message here
    	            fast_mode = false; // if fast mode off, explore
    	            if (auto_mode==true && fast_mode == false && map_Load == true)
    	            {
    	            	//TODO: explore
    	        		//explorer.runExploration();
    	        		//generateMapDescriptor(e_Mapper);
    	        		//bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
    	        		//e_Mapper.repaint();
    	        		//r_Mapper.repaint();
    	                //e_Mapper.repaint();
    	        		
    	                CardLayout cl = ((CardLayout) _mapCards.getLayout());
    	                cl.show(_mapCards, "EXPLORATION");
    	                new Exploration().execute();
    	        		
    	        		if (auto_mode==false)
    	        		{
    	        			System.out.println("Break");
    	        			//break;
    	        		}
    	            }
    	            toggleButton.setText("ON");
    	            
    	        } else {
    	            System.out.println("Off"); // remove your message
    	            fast_mode = true; // if fast mode on, greedy
    	            if (auto_mode==true && fast_mode == false && map_Load == true)
    	            {
    	            	//TODO: sprint
    	                CardLayout cl = ((CardLayout) _mapCards.getLayout());
    	                cl.show(_mapCards, "EXPLORATION");
    	                new FastestPath().execute();
    	        		
    	        		if (auto_mode==false)
    	        		{
    	        			System.out.println("Break");
    	        			//break;
    	        		}
    	            }
    	            toggleButton.setText("OFF");
    	            
    	        }
    	    }
    	};
    	
    	toggleButton.addItemListener(itemListener);
    	
    	
    	JLabel auto_label = new JLabel("Auto Mode: ");
    	JToggleButton autoBtn = new JToggleButton("OFF");
    	ItemListener autoBtnListener = new ItemListener() {
    		
    	    public void itemStateChanged(ItemEvent itemEvent) {
    	        int state = itemEvent.getStateChange();
    	        if (state == ItemEvent.SELECTED) {
    	            System.out.println("On"); // show your message here
    	            auto_mode = true; // if auto mode on
    	            autoBtn.setText("ON");
    	            
    	        } else {
    	            System.out.println("Off"); // remove your message
    	            auto_mode = false; // if auto mode off
    	            autoBtn.setText("OFF");
    	            
    	        }
    	    }
    	};
    	
    	autoBtn.addItemListener(autoBtnListener);
    	
    	

    	mode_panel.add(exp_label);
    	mode_panel.add(toggleButton);
    	mode_panel.add(auto_label);
    	mode_panel.add(autoBtn);
    	
    	
    	
    	
    	_modeSettings.add(mode_panel);
    	
    	
    	
    }

}