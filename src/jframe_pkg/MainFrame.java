package jframe_pkg;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class MainFrame extends JFrame {

    private static JFrame _appFrame = null;         // application JFrame

    private static JPanel _mapCards = null;         // JPanel for map views
    private static JPanel _buttons = null;          // JPanel for buttons

    //private static Robot bot;

    //private static Map realMap = null;              // real map
    //private static Map exploredMap = null;          // exploration map

    private static int timeLimit = 3600;            // time limit
    private static int coverageLimit = 300;         // coverage limit

    //private static final CommMgr comm = CommMgr.getCommMgr();
    private static final boolean realRun = true;
	
	//private JFrame contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					//frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
        // Initialise main frame for display
        _appFrame = new JFrame();
        _appFrame.setTitle("MDP Group 2 Simulator");
        _appFrame.setSize(new Dimension(690, 700));
        _appFrame.setResizable(false);

        JPanel main_layout = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        
        JPanel test_panel_grid = new JPanel();
        JPanel test_panel = new JPanel();
        JButton button1 = new JButton();
        JButton button2 = new JButton();
        JButton button3 = new JButton();
        JButton button4 = new JButton();
        test_panel.setLayout(new GridLayout(5, 5, 3, 3));
        test_panel.add(button1);
        test_panel.add(button2);
        test_panel.add(button3);
        //test_panel.add(button4);
        
        _appFrame.add(test_panel);
        // Center the main frame in the middle of the screen
        //Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
       // _appFrame.setLocation(dim.width / 2 - _appFrame.getSize().width / 2, dim.height / 2 - _appFrame.getSize().height / 2);

        // Create the CardLayout for storing the different maps
        //_mapCards = new JPanel(new CardLayout());

        // Create the JPanel for the buttons
        //_buttons = new JPanel();

        // Add _mapCards & _buttons to the main frame's content pane
        //Container contentPane = _appFrame.getContentPane();
        //contentPane.add(_mapCards, BorderLayout.CENTER);
        //contentPane.add(_buttons, BorderLayout.PAGE_END);

        // Initialize the main map view
        //initMainLayout();

        // Initialize the buttons
        //initButtonsLayout();

        // Display the application
        _appFrame.setVisible(true);
        _appFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
	}
	
    private static void initMainLayout() {
        /*if (!realRun) {
            _mapCards.add(realMap, "REAL_MAP");
        }
        _mapCards.add(exploredMap, "EXPLORATION");
         */
        CardLayout cl = ((CardLayout) _mapCards.getLayout());
        if (!realRun) {
            cl.show(_mapCards, "REAL_MAP");
        } else {
            cl.show(_mapCards, "EXPLORATION");
        }
    }

    /**
     * Initialises the JPanel for the buttons.
     */
    private static void initButtonsLayout() {
        _buttons.setLayout(new GridLayout());
        //addButtons();
    }

}