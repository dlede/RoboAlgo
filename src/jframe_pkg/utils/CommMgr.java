package jframe_pkg.utils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class CommMgr {
    public static final String EX_START = "EX_START";       // Android --> PC
    public static final String FP_START = "FP_START";       // Android --> PC
    public static final String MAP_STRINGS = "MAP";         // PC --> Android
    public static final String BOT_POS = "BOT_POS";         // PC --> Android
    public static final String BOT_START = "BOT_START";     // PC --> Arduino
    public static final String INSTRUCTIONS = "INSTR";      // PC --> Arduino
    public static final String SENSOR_DATA = "SDATA";       // Arduino --> PC

	public static final String RPI_IP_ADDRESS = "192.168.15.15"; // MDPGrp 15's IP Address
	public static final int RPI_PORT = 1515; // Port used for transmitting of data

	private static CommMgr commMgr;
	private static Socket commSocket;
	private static BufferedWriter toRPi; // write to RPI
	private static BufferedReader fromRPi; // read from RPI

	private CommMgr() {
	}

	public static CommMgr getCommMgr() {
		if (commMgr == null) {
			commMgr = new CommMgr();
		}
		return commMgr;
	}

	// setting up connection
	public void setUpConnection() throws UnknownHostException, IOException {

		try {
			commSocket = new Socket(RPI_IP_ADDRESS, RPI_PORT);
			toRPi = new BufferedWriter(
					new OutputStreamWriter(new BufferedOutputStream(commSocket.getOutputStream())));
			fromRPi = new BufferedReader(new InputStreamReader(commSocket.getInputStream()));
			return;

		} catch (Exception e) {
			System.out.println("Exception: " + e.toString());
		}

	}

	//close connection
	public void closeConnection() throws IOException {
				
		if (!commSocket.isClosed()) {
			commSocket.close();
		}
	}

	//check if connection is established
	public boolean isConnected() {
        return commSocket.isConnected();
    }
	
	//receive msg from RPI
	public String revMsg(){
		
		/*try {
			String txtFromRPI = null;
			
			if (txtFromRPI == null)
			{
				System.out.println("null1");
			}
			
			txtFromRPI = fromRPi.readLine(); // read the text from RPI
			
			if (txtFromRPI == null)
			{
				System.out.println("null2");
			}
			
			Class cls = txtFromRPI.getClass();
            //Class cls2 = (CommMgr.EX_START).getClass();
            System.out.println("txtFromRPI type: " + cls.getName());
            //System.out.println("def type: " + cls2.getName());
			
		    System.out.println("Msg from pi: " + txtFromRPI);
		    
		   
			if (!txtFromRPI.isEmpty() && txtFromRPI != null) {
				System.out.println("Message from RPI: " + txtFromRPI);
				return txtFromRPI.trim();
			}
			else 
			{
				System.out.println("null");
			}*/

		while (true) {
		try {
			String txtFromRPI = null;
			txtFromRPI = fromRPi.readLine().trim(); // read the text from RPI
		
			if (!txtFromRPI.isEmpty()) {
				txtFromRPI = txtFromRPI.trim();
				System.out.println("Message from RPI: " + txtFromRPI);
				return txtFromRPI;
			}

		
		
		
		} catch (Exception ex) {
			Thread t = Thread.currentThread();
			t.getUncaughtExceptionHandler().uncaughtException(t, ex);
		}
			}
		//return null;
		
	}
	
	
	//send msg to RPI
	public void sendMsg(String msgToRPI) {

		/*
		if(msgToRPI.length() > 9) // if the length of 3, possible a map descriptor
		{
		    char[] chars = msgToRPI.toCharArray();
		    StringBuilder hex = new StringBuilder();
		    for (char ch : chars) {
		        hex.append(Integer.toHexString((int) ch));
		    }
		 
		    //return hex.toString();
			try {
				toRPi.write(hex);//transmit msg to RPI
				toRPi.flush();
				//System.out.println("Message sent: " + msgToRPI);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}*/
		
		System.out.println(msgToRPI);
		// Scanner in = new Scanner(System.in);
		// String msgToRPI;
		// System.out.println("Input message to android");
		// msgToRPI = in.next();
		try {
			toRPi.write(msgToRPI);//transmit msg to RPI
			toRPi.flush();
			//System.out.println("Message sent: " + msgToRPI);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	

	
}
