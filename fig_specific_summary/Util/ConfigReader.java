package Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;

/**
 * Read the configuration for the system from config.txt
 * @author Suppawong Tuatob
 *
 *Format of config file: field = value
 * Example
 *	csxdbhost = localhost
	csxdbname = citeseerx
	csxdbusername = csx-read
 */
public class ConfigReader {
	String configFilename = null;
	HashMap<String, String> configs = new HashMap<String, String>();
	public ConfigReader()
	{
		configFilename = "config.txt";
		readConfigFile();
	}
	public ConfigReader(String _configFilename)
	{
		configFilename = _configFilename;
		File test = new File(configFilename);
		if(!test.exists()) configFilename = "."+configFilename;
		readConfigFile();
	}
	
	private void readConfigFile()
	{	try{
			BufferedReader reader = new BufferedReader(new FileReader(configFilename));
			String line = "";
			while((line = reader.readLine()) != null)
			{
				line = line.trim();
				if(line.isEmpty()) continue;
				if(line.startsWith("%")) continue; //comment
				
				String[] pair = line.split("=");
				if(pair.length < 2) continue;
				configs.put(pair[0].trim(), pair[1].trim());
			}
		
			reader.close();
		}catch(Exception e)
		{
			e.printStackTrace();
			System.exit(-1);
		}
		
	}
	
	public String getValue(String field)
	{
		return configs.get(field);
	}
	
	public static void main(String[] args)
	{
		
	}
}
