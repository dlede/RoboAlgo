package jframe_pkg.utils;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.Timer;

public class Stopwatch extends JFrame {

    private static final int N = 60;
    private static final String stop = "Stop";
    private static final String start = "Start";
    private static String str_minsec = "00:00";
    private final ClockListener cl = new ClockListener();
    private final Timer t = new Timer(1000, cl);
    //private final JTextField tf = new JTextField(3);
    private final JTextField tf;
    public int sec;
    public int min;
    private boolean state = false;
    
    public Stopwatch(JTextField tf) {
        //t.setInitialDelay(0);
    	this.tf = tf;
        //JPanel panel = new JPanel();
        //tf.setHorizontalAlignment(JTextField.RIGHT);
        //tf.setEditable(false);
        //panel.add(tf);

        str_minsec = String.valueOf(min) + ":" + String.valueOf(sec);
        
        //this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //this.add(panel);
        //this.setTitle("Timer");
        //this.pack();
        //this.setLocationRelativeTo(null);
        //this.setVisible(true);
    }
    
    public void start() {
        t.start();
        //update min and sec
        //str_minsec = String.valueOf(min) + ":" + String.valueOf(sec);
    }
    
    public void stop(){
    	t.stop();
    	//tf.setText(String.valueOf(0) + ":" + String.valueOf(0));
    }
    
    public void reset()
    {
    	resetSec();
    	resetMin();
    }

    
    private class ClockListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
        	if (sec != 0 && sec%N==0)
        	{
        		min++;
        	}
            sec %= N;
            //str_minsec = String.valueOf(min) + ":" + String.valueOf(sec);
            tf.setText(String.valueOf(min) + ":" + String.valueOf(sec));
        	str_minsec = String.valueOf(min) + ":" + String.valueOf(sec);
            sec++;
        }
    }
    
    public String getMinSec()
    {
    	return str_minsec;
    }
    
    private void resetMin()
    {
    	//min = Integer.toString(String.valueOf(min));//Integer.toString(timer.getMin())
    	this.min=0;
    }
    
    public void resetSec()
    {
    	this.sec=0;
    }
    
    public int getMin()
    {
    	//min = Integer.toString(String.valueOf(min));//Integer.toString(timer.getMin())
    	return min;
    }
    
    public int getSec()
    {
    	return sec;
    }
    
    public void setState(boolean state)
    {
    	this.state = state;
    }
    
}
