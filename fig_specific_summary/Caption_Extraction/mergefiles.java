package Caption_Extraction;
//package AlgorithmExtraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import org.apache.commons.io.FileUtils;

import Util.Directory;


public class mergefiles {
	
	public static void main(String args[]) throws IOException
	{
		Vector<String> TaggedTextFiles = Directory.listAllFiles("Textfiles/synopsis_captions_rad/", ".txt", 1);
		//Vector<String> TaggedTextFiles = Directory.listAllFiles("Textfiles/synopsis_references/", ".txt", 1);  //uncomment for references
		System.out.println("here");
		//f=
		//BufferedReader br=new BufferedReader(new FileReader(new File(f)));
		BufferedWriter bw=null;
		BufferedReader br=null;
		String fnew=null;
		String writefile=null;
		System.out.println("TF " + TaggedTextFiles);
		
		try{
			for(String f: TaggedTextFiles){
				System.out.println("f:"+f);
				fnew = f.substring(f.indexOf("Textfiles/synopsis_captions_rad/")+33,f.indexOf(".txt")).trim();
			//	fnew = f.substring(f.indexOf("Textfiles/synopsis_references/")+30,f.indexOf(".txt")).trim();  //uncomment for references
				System.out.println("fnew:"+fnew);
				//fnew = f.substring(f.indexOf("sis/")+4,f.indexOf(".txt")+4).trim();
				File file= new File(f);
				//System.out.println("fnew:"+fnew);
				fnew= fnew.substring(0,fnew.indexOf("_")).trim();
				System.out.println("fnew:"+fnew);
				//writefile="pdfs/synopsis-ref/"+fnew+".txt";
				writefile="Textfiles/synopsis_cap_merged/"+fnew+".txt";
				//writefile="Textfiles/synopsis_ref_merged/"+fnew+".txt"; //uncomment for references
		    br=new BufferedReader(new FileReader(new File(f)));
			bw=new BufferedWriter(new FileWriter(writefile,true));
			//new BufferedWriter(new FileWriter(f, true));
			String file1Str = FileUtils.readFileToString(file);
			//String file2Str = FileUtils.readFileToString(file2);
			System.out.println(file1Str);
			bw.write(file1Str);

			// Write the file
			//FileUtils.write(file3, file1Str);
			//FileUtils.write(file3, file2Str, true); // true for append
			//bw.write("hellow1");
			//bw.write("hellow2");
			//bw.write("hellow3");
			//bw.close();

			System.out.println("Data successfully appended at the end of file");
			bw.flush();
			bw.close();
			//br.close();
		}
			//bw.flush();
			//bw.close();
			br.close();
			
		}
		
		catch(Exception e){
			System.out.println("Exception occurred:");
	    	 e.printStackTrace();
		}
	}

}
