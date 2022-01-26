package Util;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.io.FileUtils;


public class Util {
	public static void jout(String s)
	{	System.out.print(s);
	}
	
	public static void log(String s)
	{	try{
			FileWriter fstream = new FileWriter("./logs/log.txt",true);
	        BufferedWriter out = new BufferedWriter(fstream);
	        out.write("@@@ "+s+"\n");
	        out.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static void errlog(String s)
	{	try{
		FileWriter fstream = new FileWriter("./logs/errlog.txt",true);
        BufferedWriter out = new BufferedWriter(fstream);
        out.write("@@@ "+s+"\n");
        out.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		System.exit(-1);
	}
	
	public static void printHashMap(HashMap map)
	{
		Iterator iterator = map.keySet().iterator();  
		   
		while (iterator.hasNext()) {  
		   Object key = iterator.next();  
		   String value = map.get(key).toString();  
		   
		   System.out.println("{"+key + "->" + value+"}");  
		}  
	}
	
	public static void printSet(Set set)
	{
		Iterator iterator = set.iterator();  
		System.out.print("{");
		while (iterator.hasNext()) {  
		   String key = iterator.next().toString();  
		     
		   System.out.println(key+",");  
		}
		System.out.print("}");
	}
	
	public static void printVector(@SuppressWarnings("rawtypes") Vector v)
	{	Util.jout(vectorToString(v));
	}
	
	public static String vectorToString(@SuppressWarnings("rawtypes") Vector v)
	{
		int count = 1;
		String result = "";
		for(Object o: v)
		{
			result+="["+(count++)+"]"+o.toString()+"\n";
		}
		
		return result;
	}
	
	
	/**
	 * Each line has the form of:
	 * 
	 * 	22|[::-::]positive examples and background knowledge that are able to successfully tag a relatively large
		23|[::-::]corpus in Portuguese.
		24|[::-::]1
		26|1[::-::]Introduction
		27|[::-::]The task of Part-of-Speech Tagging consists in assigning to each word in a given body of text an
		
		Returns map of line number -> taggedValue
	 * @param taggedFile
	 * @return
	 */
	public static HashMap<Integer, Integer> getLinewiseTagData(String taggedFile)
	{
		HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();
		List<String> lines = null;
		try {
			lines = FileUtils.readLines(new File(taggedFile), "UTF-8");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(String line: lines)
		{
			String[] tokens = line.split("\\[::-::\\]");
			if(tokens.length != 2) continue;
			String[] options = tokens[0].split("\\|");
			Integer lineNum = 0;
			Integer value = 0;
			
			try{
				lineNum = Integer.parseInt(options[0]);
				if(options.length == 2) value = Integer.parseInt(options[1]);
			}catch(Exception e)
			{
				e.printStackTrace();
			}
			
			result.put(lineNum, value);
		}
		return result;
	}
	
	public static <T> HashSet<T> setMinus (HashSet<T> x, HashSet<T> y)
    {
            HashSet<T> t = new HashSet<T>();
            for (T o:x)
            if (!y.contains(o)) t.add( o);
            return t;
    }
	
	public static void main(String[] args)
	{
		Util.printHashMap(getLinewiseTagData("./sample/00_experiment_data/doc_seg/done/10.1.1.1.2823.tagged.txt"));
	}
}
