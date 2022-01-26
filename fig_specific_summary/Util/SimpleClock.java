package Util;
//for measuring execution time
public class SimpleClock {
	long startTime;
	long endTime;
	
	public SimpleClock()
	{
	}
	
	public void start()
	{
		startTime = System.currentTimeMillis();
	}
	
	public void stop()
	{
		endTime = System.currentTimeMillis();
	}
	
	public String getTime()
	{
		long miseconds = endTime - startTime;
		double seconds = (double)(miseconds)/1000.0;
		double minutes = seconds/60.0;
		double hours = minutes/60.0;
		double days = hours/24.0;
		String result = "";
		
		if(days >= 1.0) result = ""+days;
		else if(hours >= 1.0) result = ""+hours;
		else if(minutes >= 1.0) result = ""+minutes;
		else if(seconds >= 1.0) result = ""+seconds;
		else if(miseconds >= 1.0) result = ""+miseconds;
		
		return result;
	}
	
	public void displaylayInterval()
	{
		long miseconds = endTime - startTime;
		double seconds = (double)(miseconds)/1000.0;
		double minutes = seconds/60.0;
		double hours = minutes/60.0;
		double days = hours/24.0;
		
		if(days >= 1.0) System.out.printf("%.2f days\n", days);
		else if(hours >= 1.0) System.out.printf("%.2f hours\n", hours);
		else if(minutes >= 1.0) System.out.printf("%.2f mins\n", minutes);
		else if(seconds >= 1.0) System.out.printf("%.2f secs\n", seconds);
		else if(miseconds >= 1.0) System.out.printf("%.2f misecs\n", miseconds);
		
	}
}
