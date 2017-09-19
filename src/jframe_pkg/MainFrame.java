package jframe_pkg;

import static jframe_pkg.utils.MapDescriptor.loadMapFromDisk;
import jframe_pkg.map.Mapper;
import jframe_pkg.robot.RobotConstants;
import jframe_pkg.robot.Robot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MainFrame extends JFrame {
	//TODO: create waypoint click system... input waypoints
	//TODO: speed orientation input as well, speed of robot
	
    private static JFrame _appFrame = null;         // application JFrame

    private static JPanel _mapCards = null;         // JPanel for map views
    private static JPanel _buttons = null;          // JPanel for buttons

    private static Robot bot; // TODO: init your robot here

    private static Mapper r_Mapper = null;   
    private static Mapper e_Mapper = null;  
    
    private static Mapper realMap = null;              // TODO: real map
    private static Mapper exploredMap = null;          // TODO: exploration map

    private static int timeLimit = 3600;            // TODO: time limit
    private static int coverageLimit = 300;         // TODO: coverage limit

    //private static final CommMgr comm = CommMgr.getCommMgr(); // TODO: commsMgr
    private static final boolean realRun = false;
    
    //private boolean auto_mode = false; //auto mode false = manual, can use keystroke to move
    //private boolean fast_mode = false; //fast mode false = exploration
	
	//private JFrame contentPane;

	public static void main(String[] args) {
        bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, realRun);

		e_Mapper = new Mapper(bot); //argument bot
		//e_Mapper.gridder.setAllUnexplored();
		e_Mapper.gridder.setAllExplored();
        
		MainFrame frame = new MainFrame();
		//frame.setVisible(true);
		
		//if (!auto_mode)
		//controller
		//else
		//auto explore
		
        //if (realRun) comm.openConnection();

        bot = new Robot(RobotConstants.START_ROW, RobotConstants.START_COL, realRun);

        if (!realRun) {
            realMap = new Mapper(bot);
            realMap.gridder.setAllUnexplored();
        }
	}
	
	//show main frame
	public MainFrame() {
        // Initialise main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("MDP Group 15 Main Frame");
        _appFrame.setSize(new Dimension(800, 800));
        _appFrame.setResizable(false);

        // Center the main frame in the middle of the screen
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        //_appFrame.setLocation(dim.width / 2 - _appFrame.getSize().width / 2, dim.height / 2 - _appFrame.getSize().height / 2);

        // Create the CardLayout for storing the different maps
        _mapCards = new JPanel(new CardLayout());

        // Create the JPanel for the buttons
        _buttons = new JPanel();
        
        //JPanel test_panel = new JPanel();
        //JToggleButton auto_button = new JToggleButton("Auto");

        //test_panel.setLayout(new GridLayout(9, 9, 5, 5));
        //test_panel.add(auto_button);

        // Add _mapCards & _buttons to the main frame's content pane
        Container contentPane = _appFrame.getContentPane();
        contentPane.add(_mapCards, BorderLayout.CENTER);
        contentPane.add(_buttons, BorderLayout.PAGE_END);
        
        // Initialize the main map view
        initMainLayout();
        
        //buttons
        initButtonsLayout();
        
        //_appFrame.add(test_panel);
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
    private static void initMainLayout() {
        //if (!realRun) {
            //_mapCards.add(r_Mapper, "REAL_MAP");
        //}
        _mapCards.add(e_Mapper, "EXPLORATION");
         
        CardLayout cl = ((CardLayout) _mapCards.getLayout());
        //if (!realRun) {
            //cl.show(_mapCards, "REAL_MAP");
        //} else {
            cl.show(_mapCards, "EXPLORATION");
        //}
    }

    /**
     * Initialises the JPanel for the buttons.
     **/
    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout());
        addButtons();
    }
    
    private static void formatButton(JButton btn) {
        btn.setFont(new Font("Arial", Font.BOLD, 13));
        btn.setFocusPainted(false);
    }
    
    private static void addButtons() {
        //if (!realRun) {
            // Load Map Button
            JButton btn_LoadMap = new JButton("Load Map");
            formatButton(btn_LoadMap);
            btn_LoadMap.addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    JDialog loadMapDialog = new JDialog(_appFrame, "Load Map", true);
                    loadMapDialog.setSize(400, 100);
                    loadMapDialog.setLayout(new FlowLayout());

                    final JTextField loadTF = new JTextField(15);
                    JButton loadMapButton = new JButton("Load");

                    loadMapButton.addMouseListener(new MouseAdapter() {
                        public void mousePressed(MouseEvent e) {
                            loadMapDialog.setVisible(false);
                            loadMapFromDisk(r_Mapper, loadTF.getText());
                            CardLayout cl = ((CardLayout) _mapCards.getLayout());
                            cl.show(_mapCards, "REAL_MAP");
                            r_Mapper.repaint();
                        }
                    });

                    loadMapDialog.add(new JLabel("File Name: "));
                    loadMapDialog.add(loadTF);
                    loadMapDialog.add(loadMapButton);
                    loadMapDialog.setVisible(true);
                }
            });
            _buttons.add(btn_LoadMap);
        //}
    }
    

}