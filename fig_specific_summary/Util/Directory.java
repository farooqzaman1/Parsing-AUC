package Util;
import java.util.Vector;
import java.io.*;



/**
 * 
 * @author suppawong
 * 
 * Handle directory, listing files, and stuff
 *
 */
public class Directory {
	String mainDir;
	
	public Directory(String _mainDir)
	{
		mainDir = _mainDir;
	}
	
	public static Vector<String> listAllFiles(String path, String fileExtension, int depth)
	//depth -1: recursive
	//depth 1: all files with extension fileExtension in mainDir
	//path must not have '/' at the end
	{	
		Vector<String> fileList = new Vector<String>();
		
		if(depth == 0) return fileList;
		
		
		try{
			
			File dir = new File(path);
			String[] children = dir.list();
			if (children == null) {
			    return fileList;
			} 
			else 
			{
			    for (int i=0; i<children.length; i++) {
			        // Get filename of file or directory
			        String filename = children[i];
			        String newPath = path+"/"+filename;
			        File f = new File(newPath);
			        if(f.isDirectory())
			        {
			        	fileList.addAll(listAllFiles(newPath, fileExtension, depth - 1));
			        }
			        else if(f.isFile())
			        {	if(filename.endsWith(fileExtension))
			        	{	fileList.add(newPath);
			        	}
			        }
			        
			    }
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return fileList;
		
	}
	
	public static String getFileName(String fullPath)
	{
		String[] items = fullPath.split("/");
		return items[items.length - 1];
	}
	
	public static String getFileID(String fileName)
	//just filename without extension
	{
		//get file name
		String[] tokens = fileName.split("/");
		String actualFilename = tokens[tokens.length-1];
		return actualFilename.substring(0, actualFilename.lastIndexOf('.'));
	}
	
	public static void mkdir(String dir, boolean clearContentIfExists)
	{
		if(exists(dir) && !clearContentIfExists) return;
		
		if(exists(dir))	//delete it files
		{
			deleteDir(new File(dir));
		}
		
		//make dir
		(new File(dir)).mkdirs();
		
	}
	
	public static boolean deleteDir(File dir) {
	    if (dir.isDirectory()) {
	      String[] children = dir.list();
	      for (int i = 0; i < children.length; i++) {
	        boolean success = deleteDir(new File(dir, children[i]));
	        if (!success) {
	          return false;
	        }
	      }
	    }
	    return dir.delete();
	  }
	
	public static boolean exists(String filename)
	{
		try{
			File f = new File(filename);
			if(f.exists()) return true;
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		return false;
	}
	
	public static void main(String[] argv)
	{
		/*if(argv.length > 0)
		{
			Vector<String> files = listAllFiles(argv[0], ".txt", -1);
			for(int i = 0; i < files.size(); i++)
			{
				System.out.println(files.elementAt(i));
			}
		}
		*/
		Util.jout(Directory.getFileID("dajdja.afda.fsdfds.body"));
	}

}
