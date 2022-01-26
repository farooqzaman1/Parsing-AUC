package Util;
import java.io.BufferedReader;
import java.io.InputStreamReader;


public class CommandExecutor {
	public static void exec(String cmd, boolean verbose)
	{
		//String cmd = "ls -al";
		Runtime run = Runtime.getRuntime();
		try{
			Process pr = run.exec(cmd);
			//pr.waitFor();
			
			if(verbose)
			{
				BufferedReader buf = new BufferedReader(new InputStreamReader(pr.getInputStream()));
				String line = "";
				while ((line=buf.readLine())!=null) {
					System.out.println(line);
				}
			}
		}catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
