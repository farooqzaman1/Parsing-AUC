package Util;

import java.io.File;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Vector;


import com.aliasi.util.Files;


public class DocumentUtil {
	public static String getPaperTitle(String paperid, LocalDBConnector localdb, CsxDBConnector csxdb)
	{	
		try{
				String t = null;
				//CsxDBConnector csxdb = new CsxDBConnector();
				ResultSet r = csxdb.executeQuery("SELECT title FROM papers WHERE id = '"+paperid+"';");
				while(r.next())
				{
					t =  r.getString("title");break;
				}
				r.close();
				
				return t;
				//csxdb.close();
			
		}catch(Exception e)
		{
			e.printStackTrace();
			Util.errlog(e.toString());
		}
		return null;
		
	}
	
	public static String readText(String inputFile)
	{	//Util.jout(inputFile);
		String text = "";
		try{
		
			File file = new File(inputFile);
			
			//text = Files.readFromFile(file,"UTF-8");
			//text = Files.readFromFile(file,"iso-8859-1");
			text = Files.readFromFile(file,"UTF-8");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return text;
	}
	
	//check if mainStr contains subStr, word by word
	public static boolean tokenizingContain(String mainStr, String subStr)
	{	boolean result = false;
		
	
		Vector<String> mainTokens = getTokens(mainStr);
		Vector<String> subTokens = getTokens(subStr);
		
		
		for(int i = 0; i < mainTokens.size(); i++)
		{
			if(i+subTokens.size() > mainTokens.size()) break;
			boolean match = true;
			for(int j = 0; j < subTokens.size(); j++)
			{
				if(!mainTokens.elementAt(i+j).equalsIgnoreCase(subTokens.elementAt(j)))
				{
					match = false;
					break;
				}
			}
			
			if(match) return true;
		}
		
		return result;
	}
	
	public static Vector<String> getTokens(String str)
	{
		if(str.endsWith(".")) str = str.substring(0, str.lastIndexOf("."));
		Vector<String> tokens = new Vector<String>(Arrays.asList(str.split("[\\s-,]\\{}")));
		Vector<String> result = new Vector<String>();
		
		for(String token: tokens)
		{
			token = token.trim();
			if(token.isEmpty()) continue;
			result.add(token);
		}
		return result;
	}
	
	public static void main(String[] args)
	{
		Util.jout(""+tokenizingContain("a {cat} die.","a cat "));
	}
}
