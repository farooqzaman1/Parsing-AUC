package Caption_Extraction;

import java.io.*;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import Util.Directory; 

public class MergeSynopsis {
	public static void main(String[] args) throws IOException 
	{ 
		
		
		Vector<String> TaggedTextFiles_cap = Directory.listAllFiles("Textfiles/synopsis_cap_merged/", ".txt", 1);
		Vector<String> TaggedTextFiles_ref = Directory.listAllFiles("Textfiles/synopsis_ref_merged/", ".txt", 1);
		
		BufferedWriter bw=null;
		BufferedReader br=null;
		BufferedReader br1=null;
		String fnew=null;
		String writefile=null;
		String readfile=null;
		try
		{
			for(String f: TaggedTextFiles_cap){
				System.out.println("f:"+f);
				fnew = f.substring(f.indexOf("Textfiles/synopsis_cap_merged/")+30,f.indexOf(".txt")).trim();
				File file= new File(f);
				System.out.println("fnew:"+fnew);
				writefile="Textfiles/synopsis_merged/"+fnew+".txt";
				br=new BufferedReader(new FileReader(new File(f)));
				bw=new BufferedWriter(new FileWriter(writefile,true));
				String file1Str = FileUtils.readFileToString(file);
				//String file2Str = FileUtils.readFileToString(file2);
				System.out.println(file1Str);
				bw.write(file1Str);
				bw.flush();
				bw.close();
			}
		/*	for(String f: TaggedTextFiles_cap){
				System.out.println("f:"+f);
				fnew = f.substring(f.indexOf("Textfiles/synopsis_ref_merged/")+30,f.indexOf(".txt")).trim();
				File file= new File(f);
				System.out.println("fnew:"+fnew);
				writefile="Textfiles/synopsis_merged/"+fnew+".txt";
				readfile="Textfiles/synopsis_merged/"+fnew+".txt";
				br=new BufferedReader(new FileReader(new File(f)));
				bw=new BufferedWriter(new FileWriter(writefile,true));
				br1=new BufferedReader(new FileReader(new File(readfile)));
				String line1;
				String line2;
				line1=br.readLine(); //
				line2=br1.readLine();
			 boolean equal=false;
			 System.out.println("line1 = "+line1);
			 System.out.println("line2 = "+line2);
			 
				while (line1 != null || line2 != null)
				{
				        if(line1 == null || line2 == null)
				        {
				                equal = false;
				               // break;
				        }
				        else if(line1.equals(line2))
				        {
				                equal = true;
				               // break;
				        }
				       System.out.println(equal);
				        if(!equal)
				        {
				        	bw.write(line1);	
				        }
						
				        line1 = br.readLine();
				        line2 = br1.readLine();
				        //lineNum++;
				}
				
				//String file2Str = FileUtils.readFileToString(file2);
			 String file1Str = FileUtils.readFileToString(file);
			// bw.write(line1);
				bw.flush();
				bw.close();
			}*/
			br.close();
			//br1.close();
		}
		catch(Exception e){
			System.out.println("Exception occurred:");
	    	 e.printStackTrace();
		}
		// PrintWriter object for file3.txt 
		/*PrintWriter pw = new PrintWriter("file3.txt"); 
		
		// BufferedReader object for file1.txt 
		//BufferedReader br = new BufferedReader(new FileReader("file1.txt")); 
		
		String line = br.readLine(); 
		
		// loop to copy each line of 
		// file1.txt to file3.txt 
		while (line != null) 
		{ 
			pw.println(line); 
			line = br.readLine(); 
		} 
		
		br = new BufferedReader(new FileReader("file2.txt")); 
		
		line = br.readLine(); 
		
		// loop to copy each line of 
		// file2.txt to file3.txt 
		while(line != null) 
		{ 
			pw.println(line); 
			line = br.readLine(); 
		} 
		
		pw.flush(); 
		
		// closing resources 
		br.close(); 
		pw.close(); 
		
		System.out.println("Merged file1.txt and file2.txt into file3.txt"); 
	} */
}}
