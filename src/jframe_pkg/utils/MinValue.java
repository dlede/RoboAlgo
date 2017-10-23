package jframe_pkg.utils;

import java.util.ArrayList;

public class MinValue {

    public static void main(String[] args) {
        //new Rotation();
    	//int front_arr[] = null;
    	ArrayList<Integer> front_arr = new ArrayList<Integer>();
    	
    	int arr[] = {6, 7, 4, 2, 1, 4, 5, 0};
    	
    	System.out.print("array: ");
        for (int i = 0; i < 3; i++)
        {
        	front_arr.add(arr[i]);

        	if (i == 2)
        	{
        		System.out.print(front_arr.get(i));
        	}
        	else
        	{
        		System.out.print(front_arr.get(i) + ",");
        	}
        }
        System.out.println("");
        
    	System.out.println("array: " + front_arr.toString());
    	System.out.println("minArr: " + minValue(front_arr));
    }
	
	//dhaslie min array function
	public static int minValue(ArrayList<Integer> arr)
	{
		//System.out.println("enter minValue");
		int temp = arr.get(0); 
		
		for(int i = 0; i < arr.size(); i++) 
		{
			//System.out.println("in minValue Loop");
			if(arr.get(i) < temp)
			{
				//System.out.println("swap minValue");
				temp = arr.get(i);
			}
		}
		//System.out.println("exit minValue");
		return temp;
	}
}

