package jframe_pkg;

import static jframe_pkg.utils.MapDescriptor.generateMapDescriptor;
import static jframe_pkg.utils.MapDescriptor.loadMapFromDisk;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.UnknownHostException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.text.DefaultCaret;

import jframe_pkg.algorithm.Explorer;
import jframe_pkg.algorithm.Sprinter;
import jframe_pkg.map.Mapper;
import jframe_pkg.robot.Robot;
import jframe_pkg.robot.RobotConstants;
import jframe_pkg.utils.CommMgr;
import jframe_pkg.utils.Stopwatch;

public class MainFrame extends JFrame {
	private static JFrame _appFrame = null; // application JFrame

	static Border border = BorderFactory.createLineBorder(Color.BLACK); // create
																		// border

	// Sub - Sections
	private static JPanel _mapCards = null; // JPanel for map views
	private static JPanel _settings = null; // JPanel for settings - right
											// portion of console

	private static JLabel map_label, wp_label, spd_label, timer_label;
	private static JTextField map_field, field_spd, field_timer;
	private static JTextField field_x, field_y;
	private static JButton btn_Waypoints, btn_LoadMap, reset_button;

	private static JPanel _monitor = null; // JPanel for monitor

	private static JTextArea info;
	private static JButton btn_NxtStep;
	private static JScrollPane infoScroll;
	private static JLabel cp_label;
	private static JTextField field_cp;
	private static DefaultCaret caret;

	private static JPanel _toggle = null; // JPanel for toggle btns

	private static JLabel exp_label, auto_label;
	private static JToggleButton expBtn, autoBtn;
	private static ItemListener expListener, autoBtnListener;
	private static Sprinter fastest_wp_Path;
	private static Sprinter fastest_goal_Path;
	private static Container _container = null;

	private static Stopwatch timer = null;

	private static Robot bot; // init robot

	private static Mapper r_Mapper = null; // real map
	private static Mapper e_Mapper = null; // explored map

	private static Explorer explorer = null; // explorer mount init
	
	private static Explorer exploration = null;
	public static boolean map_Load = false; // if map is loaded

	private static int timeLimit = 3600; // TODO: time limit
	private static int coverageLimit = 300; // TODO: coverage limit

	private static final CommMgr comm = CommMgr.getCommMgr();
	private static final boolean realRun = false;

	private static boolean auto_mode = false; // auto mode false = manual, can
	// use keystroke to move
	private static boolean fast_mode = false; // fast mode false = exploration

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, realRun);

		if (realRun) {
			try {
				comm.setUpConnection("192.168.2.1", 8088); // ip address and
															// port on rpi3
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
		}
		e_Mapper = new Mapper(bot); // argument bot
		e_Mapper.gridder.setAllUnexplored();
		explorer = new Explorer(e_Mapper, r_Mapper, bot, timeLimit, coverageLimit); // exmap,
																					// rmap,
																					// robot,
																					// coverage,
																					// time

		MainFrame frame = new MainFrame();

	}
	
	// show main frame
	public MainFrame() {

		// Initialise main frame for display
		_appFrame = new JFrame();
		_appFrame.setTitle("MDP Group 15 Main Frame");
		_appFrame.setSize(new Dimension(1200, 750));
		_appFrame.setResizable(false);
		 _mapCards = new JPanel(new CardLayout());
		 
		// Initialize the main map view
		initMainLayout();

		// settings panel
		initSettingsLayout();

		// monitor panel
		initMonitorLayout();

		// toggle panel
		initToggleLayout();

		// Add _mapCards & _settings to the main frame's content pane
		Container contentPane = _appFrame.getContentPane();

		_appFrame.setLayout(new BorderLayout());

		_appFrame.add(_mapCards, BorderLayout.CENTER);
		_appFrame.add(_settings, BorderLayout.EAST);
		_appFrame.add(_monitor, BorderLayout.WEST);
		_appFrame.add(_toggle, BorderLayout.SOUTH);

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

	// East Panel
	private static void initSettingsLayout() {

		_settings = new JPanel();
		_settings.setBorder(new EmptyBorder(40, 20, 40, 40));
		_settings.setLayout(new BoxLayout(_settings, BoxLayout.Y_AXIS));
		addWaypointPanel();
		addSpeedPanel();
		addLoadMapButton();
		addTimerPanel();

	}
    
	private static void formatButton(JButton btn) {
		btn.setFont(new Font("Arial", Font.BOLD, 13));
		btn.setFocusPainted(false);
		// btn.setPreferredSize(new Dimension(200, 20));
	}
    

    private static void addLoadMapButton() {
		if (!realRun) {
			// JPanel map_panel = new JPanel(new FlowLayout());
			// map_panel = new JPanel(new GridLayout(3, 1));
			map_label = new JLabel("Map: ");
			map_field = new JTextField(10);
			map_field.setMaximumSize(map_field.getPreferredSize());
			btn_LoadMap = new JButton("Load Map");
			formatButton(btn_LoadMap);

			
			 btn_LoadMap.addMouseListener(new MouseAdapter() {
	                public void mousePressed(MouseEvent e) {
	                	btn_LoadMap.setVisible(false);
	                    loadMapFromDisk(r_Mapper, map_field.getText());
	                    CardLayout cl = ((CardLayout) _mapCards.getLayout());
	                    cl.show(_mapCards, "EXPLORATION_MAP");
	                    e_Mapper.repaint();
	                    map_Load = true;
	                    System.out.println("Map Loaded: " + map_Load);
	                    info.append("Map Loaded: " + map_Load + "\n");
	                }
	            });
	            
			 
			_settings.add(map_label);
			map_label.setAlignmentX(Component.LEFT_ALIGNMENT);
			_settings.add(Box.createRigidArea(new Dimension(0, 10)));
			
			_settings.add(map_field);
			map_field.setAlignmentX(Component.LEFT_ALIGNMENT);
			_settings.add(Box.createRigidArea(new Dimension(0, 10)));
			
			_settings.add(btn_LoadMap);
			btn_LoadMap.setAlignmentX(Component.LEFT_ALIGNMENT);
			_settings.add(Box.createRigidArea(new Dimension(0, 10)));
			_settings.setVisible(true);
        }
    }
    
	private static void addWaypointPanel() {

		// create label
		wp_label = new JLabel("Waypoints: ");

		// Create field
		field_x = new JTextField(5);
		field_y = new JTextField(5);

		//field_x.setText("0");
		//field_y.setText("0");


		//Align items wp_textfield.setAlignmentX(Component.LEFT_ALIGNMENT);
		//wp_textfield.add(field_x); wp_textfield.add(field_y);

		// create btn
		btn_Waypoints = new JButton("Set Waypoints");

		formatButton(btn_Waypoints);
		btn_Waypoints.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// TODO: set waypoint function on mapper e.g.
				final String text_x = field_x.getText();
				final String text_y = field_y.getText();
				
				if (e_Mapper.gridder.waypoint_validator(Integer.parseInt(text_x), Integer.parseInt(text_y)) == true) {
					System.out.println(
							"(" + "Waypoint: " + Integer.parseInt(text_x) + ", " + Integer.parseInt(text_y) + ")");
					e_Mapper.gridder.set_waypoint(Integer.parseInt(text_x), Integer.parseInt(text_y));
					// e_Mapper.repaint();
					map_Load = true;
				}
				info.append("Waypoint have been set at " + Integer.parseInt(text_x) + ", " + Integer.parseInt(text_y) + "\n");
			}

		});

		// add items to panel
		_settings.add(wp_label);
		wp_label.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));
		_settings.add(field_x);
		
		//TODO: ui alignment for waypoints
		wp_label.setAlignmentX(Component.LEFT_ALIGNMENT);
		//_settings.add(Box.createRigidArea(new Dimension(0, 10)));
		_settings.add(field_y);
		wp_label.setAlignmentX(Component.LEFT_ALIGNMENT);
		//_settings.add(Box.createRigidArea(new Dimension(0, 10)));
		_settings.add(btn_Waypoints);
		btn_Waypoints.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));

	}
    
	private static void addSpeedPanel() {

		// create speed label
		spd_label = new JLabel("Speed: (1 - 100)");

		// create speed field
		field_spd = new JTextField(10);
		field_spd.setMaximumSize(field_spd.getPreferredSize());

		// create speed btn
		JButton btn_Speed = new JButton("Set Speed");
		formatButton(btn_Speed);
		btn_Speed.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// TODO: set speed function on robot e.g.
				bot.setSpeed((Integer.parseInt(field_spd.getText()) - 101) * -1);
				System.out.println("Speed: " + field_spd.getText());
				info.append("Speed has been set to " + field_spd.getText() + "\n");
			}
		});

		_settings.add(spd_label);
		spd_label.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));
		_settings.add(field_spd);
		field_spd.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));
		_settings.add(btn_Speed);
		btn_Speed.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));

	}

	private static void addTimerPanel() {

		// TODO: add timer
		timer_label = new JLabel("Timer ");
		field_timer = new JTextField(10);
		field_timer.setEditable(false);
		field_timer.setMaximumSize(field_timer.getPreferredSize());

		timer = new Stopwatch(field_timer);

		field_timer.setText(timer.getMinSec());

		reset_button = new JButton("Reset");

		reset_button.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// TODO: set speed function on robot e.g.
				timer = new Stopwatch(field_timer);
				field_timer.setText(timer.getMinSec());
				System.out.println("Reset Timer");
			}
		});

		_settings.add(timer_label);
		timer_label.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));
		
		_settings.add(field_timer);
		field_timer.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));
		
		_settings.add(reset_button);
		reset_button.setAlignmentX(Component.LEFT_ALIGNMENT);
		_settings.add(Box.createRigidArea(new Dimension(0, 10)));

	}
    
    
	// West Panel
	private static void initMonitorLayout() {

		_monitor = new JPanel();
		_monitor.setLayout(new BoxLayout(_monitor, BoxLayout.Y_AXIS));
		_monitor.setBorder(new EmptyBorder(40, 20, 40, 0));

		addConsolePanel();
		addNxtStepPanel();
		addCurPosPanel();
	}
    
	private static void addConsolePanel() {

		
		// Monitor Screen
		
		info = new JTextArea(25, 25);

		//bot.setMonitorScreen(info);
		info.setEditable(false);
		info.setWrapStyleWord(true);
		info.setLineWrap(true);
		//info.setBorder(BorderFactory.createCompoundBorder(border, BorderFactory.createEmptyBorder(0, 0, 0, 0)));
		info.setAlignmentX(Component.LEFT_ALIGNMENT);
		info.setMaximumSize(info.getPreferredSize());
		infoScroll = new JScrollPane(info);
		infoScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		//infoScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		
		//info.setCaretPosition(info.getDocument().getLength());
		//infoScroll.setAutoscrolls(true);
		
		 caret = (DefaultCaret)info.getCaret();
		 caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		
		
		_monitor.add(infoScroll);
		// padding- width, height
		_monitor.add(Box.createRigidArea(new Dimension(0, 15)));

	}

	private static void addNxtStepPanel() {

		// Next Step Btn
		btn_NxtStep = new JButton("Next Step");

		btn_NxtStep.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				// TODO: set waypoint function on mapper e.g.
				System.out.println("Next Step Button Clicked!");
			}
		});

		_monitor.add(btn_NxtStep);
		btn_NxtStep.setAlignmentX(Component.LEFT_ALIGNMENT);
		// padding- width, height
		_monitor.add(Box.createRigidArea(new Dimension(0, 10)));

	}

	private static void addCurPosPanel() {
		// TODO: add in the current position of robot

		JPanel cp_panel = new JPanel(new GridLayout(0, 1));

		// Current Position Info
		// TODO: add in the current position of robot

		cp_label = new JLabel("Current Position: ");

		field_cp = new JTextField(10);
		//bot.setCurPostScreen(field_cp);
		field_cp.setMaximumSize(field_cp.getPreferredSize());
		field_cp.setEditable(false);
		info.setLineWrap(false);
		field_cp.setLayout(new FlowLayout());
		_monitor.add(cp_label);
		cp_label.setAlignmentX(Component.LEFT_ALIGNMENT);
		_monitor.add(Box.createRigidArea(new Dimension(0, 10)));
		_monitor.add(field_cp);
		field_cp.setAlignmentX(Component.LEFT_ALIGNMENT);
		_monitor.setOpaque(false);

	}

	// South Panel
	private static void initToggleLayout() {

		_toggle = new JPanel();
		_toggle.setLayout(new FlowLayout());
		addModePanel();

	}
    
    private static void addModePanel(){
    	
    	// TODO: Change this portion onwards, the multithreading portion
    	
		// FastestPath Class for Multithreading
		class FastestPath extends SwingWorker<Integer, String> {
			protected Integer doInBackground() throws Exception {
				final String text_x = field_x.getText();
				final String text_y = field_y.getText();

				bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
				e_Mapper.repaint();

				if (realRun) {
					while (true) {
						
						System.out.println("Waiting for FP_START...");
						info.append("Waiting for FP_START...\n");
						String msg = comm.revMsg();
						if (msg.equals(CommMgr.FP_START))
							break;
					}
				}

				
				fastest_wp_Path = new Sprinter(e_Mapper, bot);
				//fastestPath.setMonitorScreen(info);
				
				fastest_wp_Path.runFastestPath(Integer.parseInt(text_x), Integer.parseInt(text_y));
				//fastestPath.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
				
				//bot.setRobotPos(Integer.parseInt(text_x), Integer.parseInt(text_y));
				//bot.setRobotDir(RobotConstants.DIRECTION.SOUTH);
				//fastestPath.runFastestPath(Integer.parseInt(text_x), Integer.parseInt(text_y));
				System.out.println("\n\nbreaker....");
				System.out.println("breaker....");
				System.out.println("breaker.... \n\n");
				
				bot.setRobotPos(Integer.parseInt(text_x), Integer.parseInt(text_y));
				bot.setRobotDir(RobotConstants.DIRECTION.NORTH);
				e_Mapper.repaint();
				
				fastest_goal_Path = new Sprinter(e_Mapper, bot);
				//e_Mapper.repaint();
				fastest_goal_Path.runFastestPath(RobotConstants.GOAL_ROW, RobotConstants.GOAL_COL);
				

				//private static Sprinter fastest_wp_Path;
				//private static Sprinter fastest_goal_Path;
				
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
				//e_Mapper.repaint();
				 r_Mapper.repaint();
				 
				exploration = new Explorer(e_Mapper, r_Mapper, bot, coverageLimit, timeLimit);
				//exploration.setMonitorScreen(info);
				//exploration.setTimer(timer);
				
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

		exp_label = new JLabel("Exploration Mode: ");

		expBtn = new JToggleButton("OFF");
    	
		expListener = new ItemListener() {
    		
    	    public void itemStateChanged(ItemEvent itemEvent) {
    	        int state = itemEvent.getStateChange();
    	        if (state == ItemEvent.SELECTED) {
    	            System.out.println("Exploration Mode On"); // show your message here
    	            info.append("Exploration Mode On\n");
    	            fast_mode = false; // if fast mode off, explore
    	            if (auto_mode==true && fast_mode == false && map_Load == true)
    	            {
    	            	//TODO: explore
    	        		//explorer.runExploration();
    	        		//generateMapDescriptor(e_Mapper);
    	        		//bot.setRobotPos(RobotConstants.START_ROW, RobotConstants.START_COL);
    	        		//e_Mapper.repaint();
    	        		
    	                CardLayout cl = ((CardLayout) _mapCards.getLayout());
    	                cl.show(_mapCards, "EXPLORATION");
    	                
    	                timer.start();//start timer
    	                new Exploration().execute();
    	                
    	        		if (auto_mode==false)
    	        		{
    	        			System.out.println("Break");
    	        			//break;
    	        		}
    	            }
    	            expBtn.setText("Exploration: ON");
    	            
    	        } else {
    	            System.out.println("Off"); // remove your message
    	            info.append("Exploration Mode Off\n");
    	            fast_mode = true; // if fast mode on, greedy
    	            if (auto_mode==true && fast_mode == true && map_Load == true)
    	            {
    	            	//TODO: sprint
    	                CardLayout cl = ((CardLayout) _mapCards.getLayout());
    	                cl.show(_mapCards, "EXPLORATION"); //not "REAL_MAP"
    	                new FastestPath().execute();
    	        		
    	        		if (auto_mode==false)
    	        		{
    	        			System.out.println("Break");
    	        			//break;
    	        		}
    	            }
    	            expBtn.setText("Exploration: OFF");
    	            
    	        }
    	    }
    	};
    	
    	expBtn.addItemListener(expListener);
    	
		 

		auto_label = new JLabel("Auto Mode: ");
		autoBtn = new JToggleButton("OFF");

		autoBtnListener = new ItemListener() {
    		
    	    public void itemStateChanged(ItemEvent itemEvent) {
    	        int state = itemEvent.getStateChange();
    	        if (state == ItemEvent.SELECTED) {
    	            System.out.println("Auto Mode On"); // show your message here
    	            info.append("Auto Mode On\n");
    	            auto_mode = true; // if auto mode on
    	            autoBtn.setText("Auto: ON");
    	            
    	        } else {
    	            System.out.println("Off"); // remove your message
    	            info.append("Auto Mode Off\n");
    	            auto_mode = false; // if auto mode off
    	            autoBtn.setText("Auto: OFF");
    	            
    	        }
    	    }
    	};
    	
    	autoBtn.addItemListener(autoBtnListener);

		_toggle.add(exp_label);
		_toggle.add(expBtn);
		_toggle.add(auto_label);
		_toggle.add(autoBtn);

	}

}