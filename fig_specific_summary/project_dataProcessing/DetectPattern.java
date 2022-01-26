package project_dataProcessing;


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
	public static boolean RAD_found = false;
	public static boolean ref_section_found = false;
	public static boolean ack_section_found = false;
	public static boolean intro_section_found = false;
	public static boolean background_section_found = false;
	// We will ignore those lines in which number of words are less then or
	// equal to this threshold value this will help remove lines containing no information
	public static int min_word_threshold = 5;
	
	public static boolean isBeforeAbstract(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.ABS)){
			abstract_found = true;
		}
		return !abstract_found;
	}
	///
	public static boolean isBeforeRAD(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.RAD)){
			RAD_found = true;
		}
		return !RAD_found;
	}
	///
	public static boolean isAfterRef(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.REF)){
			ref_section_found = true;
		}
		return ref_section_found;
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
	public static boolean isIncideIntro(String line){
		if(line.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)){
			line = line.replaceAll("\\d","").trim();
		}
		if(!(line.split("\\s").length > 15) && DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)){
			intro_section_found = true;
		}
		if((!(line.split("\\s").length > 15) && 
		!DocumentSegmentator.isStandardSection(line, DocumentNode.StdSection.INT)) && 
		DocumentSegmentator.isSectionName(line) &&
		intro_section_found == true)
		{
			intro_section_found = false;
		}
		return intro_section_found;
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
		
		File folder = new File("pseudocode_and_sbs/complexity dataset/");
		File[] listOfFiles = folder.listFiles();
		int positve_lines = 0;
		int neg_lines = 0;
		
		BufferedReader br = null;
		
		try {
			Writer positive_line_writter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./positive.txt"), "UTF-8"));
			Writer negative_line_writter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("./negative.txt"), "UTF-8"));
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
					
					String req_file = "pseudocode_and_sbs/complexity dataset/" + listOfFiles[i].getName();
					br = new BufferedReader(new FileReader(req_file));

					String line = "";
				    String line2="";
				    boolean do_not_insert = true;
					while ((line = br.readLine()) != null) 
					{
						// Decide weather the line should be part of file or not
						if(line.contains("::]")){
							if(
									( 
										isBeforeAbstract(line.substring(line.indexOf("::]") + 3).trim())
										||isBeforeRAD(line.substring(line.indexOf("::]") + 3).trim())
										|| isAfterRef(line.substring(line.indexOf("::]") + 3).trim())
										|| isIncideAck(line.substring(line.indexOf("::]") + 3).trim())
										|| isIncideIntro(line.substring(line.indexOf("::]") + 3).trim())
										|| isIncideBackground(line.substring(line.indexOf("::]") + 3).trim())
										|| (num_words(line.substring(line.indexOf("::]") + 3).trim()) <= min_word_threshold)
									)
									&& (
											!line.contains("|45[::-::]") // || line.contains("|44[::-::]")
									) 
							)
							{
								do_not_insert = true;
							}
							else{
								do_not_insert = false;
							}
							if(!do_not_insert && line.contains("|45[::-::]")){ //|| line.contains("|44[::-::]")){
								System.out.println("line:"+line);
								line2 = line.substring(line.indexOf("::]") + 3).trim();
								positive_line_writter.write(line2+",1\n");
								positve_lines++;
							}
							else if(!do_not_insert)
							{
								System.out.println("line:"+line);
								line = line.substring(line.indexOf("::]") + 3).trim();
								negative_line_writter.write(line+",0\n");
								neg_lines++;
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
			System.out.println("Negative Lines: " + neg_lines);
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
	
	
	

}