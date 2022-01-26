package Caption_Extraction;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import Model.DocumentNode;
import PdfSegmentation.DocumentSegmentator;
import Util.CitationUtil;

public class DetectPattern {
	public static boolean abstract_found = false;
	public static boolean beforeRAD_found= false;
	public static boolean beforeBAK_found= false;
	public static boolean ref_section_found = false;
	public static boolean ack_section_found = false;
	public static boolean intro_section_found = false;
	public static boolean background_section_found = false;
	public static boolean RAD_section_found = false;
	public static boolean beforeIntro_found = false;
	public static boolean AfterBCK_found = false;
	public static boolean AfterCON_found = false;
	public static boolean AfterIntro_found = false;
	public static boolean beforeMTH_found =false;
	
	
	// We will ignore those lines in which number of words are less then or
	// equal to this threshold value this will help remove lines containing no information
	public static int min_word_threshold = 5;
	
	public static boolean isBeforeAbstract(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			
			// \\d to remove digit
			line = line.replaceAll("\\d","").trim();
					
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.ABS)){
			//System.out.println("check1 :  "+ line.split("\\s"));
			abstract_found = true;
		}
		return !abstract_found;
	}
	public static boolean isBeforeBAK(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			
			// \\d to remove digit
			line = line.replaceAll("\\d","").trim();
					
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.BCK)){
			//System.out.println("check1 :  "+ line.split("\\s"));
			beforeBAK_found= true;
		}
		return !beforeBAK_found;
	}
	public static boolean isBeforeMTH(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			System.out.println("INSIDE");
			line = line.replaceAll("\\d","").trim();
		}
		//if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)){
		if(!(line.split("\\s").length > 15)&& DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.MTH)){
			System.out.println("TRUE");
			beforeMTH_found = true;
		}
		//System.out.println("faseee");
		return !beforeMTH_found;
	}
	public static boolean isBeforeIntro(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			//System.out.println("INSIDE");
			line = line.replaceAll("\\d","").trim();
		}
		//if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)){
		if(!(line.split("\\s").length > 15)&& DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)){
			//System.out.println("TRUEE");
			beforeIntro_found = true;
		}
		//System.out.println("faseee");
		return !beforeIntro_found;
	}
	public static boolean isAfterIntro(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
			//System.out.println("after intro----"+ line);
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)){
			AfterIntro_found= true;
			//System.out.println("after intro found----"+ line);
		}
		return AfterIntro_found;
	}
	public static boolean isAfterRef(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.REF)){
			ref_section_found = true;
		}
		return ref_section_found;
	}
	public static boolean isAfterCON(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.CON)){
			AfterCON_found = true;
		}
		return AfterCON_found;
	}
	
	public static boolean isIncideAck(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.ACK)){
			ack_section_found = true;
		}
		if((!(line.split("\\s").length > 15) && 
		!DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.ACK)) && 
		DocumentSegmentator.isSectionName(line) &&
		ack_section_found == true)
		{
			ack_section_found = false;
		}
		return ack_section_found;
	}
	public static boolean isIncideCON(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.CON)){
			AfterCON_found = true;
		}
		if((!(line.split("\\s").length > 15) && 
		!DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.CON)) && 
		DocumentSegmentator.isSectionName(line) &&
		AfterCON_found == true)
		{
			AfterCON_found = false;
		}
		return AfterCON_found;
	}
	public static boolean isIncideIntro(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)){
			System.out.println("INTRO FOUND!!");
			intro_section_found = true;
		}
		if((!(line.split("\\s").length > 15) && 
		!DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)) && 
		DocumentSegmentator.isSectionName(line) &&
		intro_section_found == true)
		{
			System.out.println("INTRO ENDED!!");
			intro_section_found = false;
		}
		return intro_section_found;
	}
	public static boolean isIncideRAD(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			System.out.println("MATCHED!!");
			line = line.replaceAll("\\d","").trim();
			if (DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.RAD))
			{
				System.out.println("RAD SECTION!!");
			}
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.RAD)){
			//System.out.println("Rad FOUND!!");
			RAD_section_found = true;
		}
		if((!(line.split("\\s").length > 15) && 
		!DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.RAD)) && 
		DocumentSegmentator.isSectionName(line) &&
		RAD_section_found == true)
		{
			//System.out.println("rad ENDED!!");
			RAD_section_found = false;
		}
		return RAD_section_found;
	}


	public static boolean isIncideBackground(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.BCK)){
			intro_section_found = true;
		}
		if((!(line.split("\\s").length > 15) && 
		!DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.BCK)) && 
		DocumentSegmentator.isSectionName(line) &&
		intro_section_found == true)
		{
			intro_section_found = false;
		}
		return intro_section_found;
	}
	public static int num_words(String line){
		String trim = line.trim();
		if (trim.isEmpty())
		    return 0;
		return trim.split("\\s+").length;
	}
	public static void main(String[] args) throws UnsupportedEncodingException {
		// TODO Auto-generated method stub
		
		File folder = new File("pseudocode_and_sbs/Completefiles for Positive class/");
		File[] listOfFiles = folder.listFiles();
		int positve_lines = 0;
		int neg_lines = 0;
		int neglinesprint=0;
		Boolean var= true ;
		
		BufferedReader br = null;
		
		try {
			Writer positive_line_writter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./positive_ICADl_Class.txt"), "UTF-8"));
			Writer negative_line_writter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./negative_ICADL_ALLClass.txt"), "UTF-8"));
			for (int i = 0; i < listOfFiles.length; i++) 
			{
//				writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output_without_abstract/" + listOfFiles[i].getName()), "UTF-8"));
				abstract_found = false;
				ref_section_found = false;
				ack_section_found = false;
				intro_section_found = false;
				background_section_found = false;
				
				if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".txt")) 
				{
					System.out.println("File " + listOfFiles[i].getName());
					
					//String req_file = "pseudocode_and_sbs/introsectionOnlydata for Negative Class/" + listOfFiles[i].getName();
					String req_file = "pseudocode_and_sbs/Completefiles for Positive class/" + listOfFiles[i].getName();

					//Completefiles for Positive class
					br = new BufferedReader(new FileReader(req_file));

					String line = "";
				    String line2="";
				    boolean do_not_insert = true;
					while ((line = br.readLine()) != null) 
					{
						// Decide weather the line should be part of file or not
						if(line.contains("::]")){
							/*if(
									( 
										isBeforeAbstract(line.substring(line.indexOf("::]") + 3).trim())
										|| isAfterRef(line.substring(line.indexOf("::]") + 3).trim())
										|| isIncideAck(line.substring(line.indexOf("::]") + 3).trim())
										|| isIncideIntro(line.substring(line.indexOf("::]") + 3).trim())
										|| isIncideBackground(line.substring(line.indexOf("::]") + 3).trim())
										|| (num_words(line.substring(line.indexOf("::]") + 3).trim()) <= min_word_threshold)
									)
									&& (
											!line.contains("|41[::-::]") || line.contains("|44[::-::]") || line.contains("|45[::-::]")||line.contains("|47[::-::]")
									) 
							)
							{
								do_not_insert = true;
							}
							else{
								do_not_insert = false;
							}*/
							//!do_not_insert &&
							//"|41[::-::]") || line.contains("|44[::-::]" )
							//line.contains("|45[::-::]")||
							// || line.contains("|45[::-::]" ) ||line.contains("|47[::-::]" ) )
							if(var){
							if( line.contains("|44[::-::]" )|| line.contains("|41[::-::]") )  {
								System.out.println("line:"+line);
								line2 = line.substring(line.indexOf("::]") + 3).trim();
								line2=line2.replaceAll("\\W+", " ");
								positive_line_writter.write(line2+",1\n");
								positve_lines++;
							}
							if( line.contains("|47[::-::]" ) )  {
								System.out.println("line:"+line);
								line2 = line.substring(line.indexOf("::]") + 3).trim();
								line2=line2.replaceAll("\\W+", " ");
								positive_line_writter.write(line2+",2\n");
								positve_lines++;
							}
							if( line.contains("|45[::-::]" ) )  {
								System.out.println("line:"+line);
								line2 = line.substring(line.indexOf("::]") + 3).trim();
								line2=line2.replaceAll("\\W+", " ");
								positive_line_writter.write(line2+",3\n");
								positve_lines++;
							}
							}
							
							else 
							{
								neg_lines++;
								if (neg_lines<=6300){
								System.out.println("line:"+line);
								line = line.substring(line.indexOf("::]") + 3).trim();
								negative_line_writter.write(line+",0\n");
								neglinesprint++;
								
								}
							}
						}
					}
					
				}
			}
			positive_line_writter.flush();
			positive_line_writter.close();
			negative_line_writter.flush();
			negative_line_writter.close();
			System.out.println("Positive Lines: " + positve_lines);
			System.out.println("Negative Lines: " + neglinesprint);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	public static boolean isBeforeRAD(String line) {
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		//if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)){
		if(!(line.split("\\s").length > 15)&& DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.RAD)){
			beforeRAD_found = true;
		}
		return !beforeRAD_found;
	}
	
	
	

}
