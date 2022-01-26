package PdfSegmentation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Vector;

import Model.DocumentElement;
import Model.DocumentNode;
import Model.HierarchicalDocumentStructure;
import Model.DocumentNode.NodeType;
import Model.DocumentNode.StdSection;
import Model.PdfDocument;
import Model.TextBox;
import Util.*;

import com.aliasi.util.Files;

/**
 * This class provides two methods for segmenting a document:
 * 	1. Using only regex
 * 	2. Using fontsize information to cut text into boxes first then use regex
 * @author aum
 *
 */

public class DocumentSegmentator {
	
	public static HierarchicalDocumentStructure parseDocumentUsingTextBoxAndRegex(PdfDocument pdfDoc, String docID)
	{	HierarchicalDocumentStructure hDoc = null;
		int THRESH_NUMLINES = 2;
		int THRESH_NUMWORDS = 11;
		
		Vector<TextBox> sectionTextBoxes = new Vector<TextBox>();
		
		//get a copy of textboxes, so that we dont modify the original textboxes
		Vector<TextBox> textBoxes = new Vector<TextBox>();
		for(TextBox tb: pdfDoc.textBoxes)
		{	TextBox copy = tb.getCopy();
			textBoxes.add(copy);
			sectionTextBoxes.add(copy);
		}
		
		//filter out text boxes that are believed not to be sections
		for(int i = 0; i < sectionTextBoxes.size(); i++)
		{	TextBox tb = sectionTextBoxes.elementAt(i);
			int numLines = 0;
			if(tb.text != null) numLines = tb.text.split("\n").length;
			int numWords = tb.numWords;
			
			//exception for abstract where the word "ABSTRACT" can be embedded 
			//in the first line of the paragraph
			if(tb.text.toLowerCase().contains("abstract"))
			{	
				continue;
			}
			//if number of lines exceeds the threshold then this text box is not a section
			else if(numLines > THRESH_NUMLINES)
			{
				sectionTextBoxes.removeElementAt(i);
				i--;
			}
			//if number of words exceed the threshold then this text box is not a section
			else if(numWords > THRESH_NUMWORDS)
			{
				sectionTextBoxes.removeElementAt(i);
				i--;
			}
		}
		
		//reformat text in each text boxes
		for(TextBox tb: textBoxes)
		{
			tb.text = cleanText(tb.text);
			tb.text = reformat(tb.text);
		}
		
		//check each text box whether it is a section header
		int state = 1; //1 = pretext, 2 = intext, 3 = posttext
		int sectionOrder = 0; //keep track of order of sections
		DocumentNode root = new DocumentNode();
		root.setNodeType(DocumentNode.NodeType.ROOT);
		
		root.setRawSectName("ROOT");
		String curSection = "HEADER";
		String curText = "";
		
		for(TextBox tb: textBoxes)
		{	String line = tb.text;
			line = line.trim();
			
			if(state == 1)
			{
				if(isStandardSection(line, DocumentNode.StdSection.ABS) || isStandardSection(line, DocumentNode.StdSection.INT))
				{
					state = 2;
				}
				else
				{
					curText += line+"\n";
				}
			}
			
			if(state == 2)
			{	
				
				if(isSectionName(line))
				{
					//save prev section
					root.addNode(curSection, curText, sectionOrder++);
					curSection = "";
					curText = "";
					
					//some abstract line consume some text. need to make those text available 
					if(isStandardSection(line, DocumentNode.StdSection.ABS))
					{	
						
						String tempLine = line;
						tempLine = tempLine.replace("Abstract.", "");
						tempLine = tempLine.replace("ABSTRACT.", "");
						tempLine = tempLine.replace("Abstract—", "");
						tempLine = tempLine.replace("ABSTRACT—", "");
						tempLine = tempLine.replace("Abstract-", "");
						tempLine = tempLine.replace("ABSTRACT-", "");
						tempLine = tempLine.replace("Abstract–", "");
						tempLine = tempLine.replace("ABSTRACT–", "");
						tempLine = tempLine.replace("Abstract:", "");
						tempLine = tempLine.replace("ABSTRACT:", "");
						tempLine = tempLine.replace("Abstract", "");
						tempLine = tempLine.replace("ABSTRACT", "");
						curText+=tempLine+"\n";
					}
					
					
					if(isStandardSection(line, DocumentNode.StdSection.REF))
					{
						state = 3;
					}
						
					curSection = line;
					
				}
				else
				{
					curText += line+"\n";
				}
				
			}
			
			if(state == 3)
			{
				curText += line+"\n";
			}
		
		}
		
		//save the last section
		root.addNode(curSection, curText, sectionOrder++);
		curSection = "";
		curText = "";
		//Util.log(root.printNode());
		hDoc = new HierarchicalDocumentStructure(docID, pdfDoc.fulltext, root);
		hDoc.setOriginalText(pdfDoc.fulltext);
		hDoc.findStdSections();
		
		return hDoc;
	}
	
	public static HierarchicalDocumentStructure parseDocumentUsingRegex(String fulltext, String docID)
	{
		HierarchicalDocumentStructure hDoc = null;
		
		//new stuff
		String originalText = fulltext;
		fulltext = cleanText(fulltext);
		fulltext = reformat(fulltext);
		//Util.jout(fulltext);
		String[] lines = fulltext.split("\n");
		
		int state = 1; //1 = pretext, 2 = intext, 3 = posttext
		int sectionOrder = 0; //keep track of order of sections
		DocumentNode root = new DocumentNode();
		root.setNodeType(DocumentNode.NodeType.ROOT);
		
		root.setRawSectName("ROOT");
		String curSection = "HEADER";
		String curText = "";
		
		for(String line: lines)
		{	line = line.trim();
			
			if(state == 1)
			{
				if(isStandardSection(line, DocumentNode.StdSection.ABS) || isStandardSection(line, DocumentNode.StdSection.INT))
				{
					state = 2;
				}
				else
				{
					curText += line+"\n";
				}
			}
			
			if(state == 2)
			{	
				
				if(isSectionName(line))
				{
					//save prev section
					root.addNode(curSection, curText, sectionOrder++);
					curSection = "";
					curText = "";
					
					//some abstract line consume some text. need to make those text available 
					if(isStandardSection(line, DocumentNode.StdSection.ABS))
					{	
						
						String tempLine = line;
						tempLine = tempLine.replace("Abstract.", "");
						tempLine = tempLine.replace("ABSTRACT.", "");
						tempLine = tempLine.replace("Abstract—", "");
						tempLine = tempLine.replace("ABSTRACT—", "");
						tempLine = tempLine.replace("Abstract-", "");
						tempLine = tempLine.replace("ABSTRACT-", "");
						tempLine = tempLine.replace("Abstract–", "");
						tempLine = tempLine.replace("ABSTRACT–", "");
						tempLine = tempLine.replace("Abstract:", "");
						tempLine = tempLine.replace("ABSTRACT:", "");
						tempLine = tempLine.replace("Abstract", "");
						tempLine = tempLine.replace("ABSTRACT", "");
						curText+=tempLine+"\n";
					}
					
					
					if(isStandardSection(line, DocumentNode.StdSection.REF))
					{
						state = 3;
					}
						
					curSection = line;
					
				}
				else
				{
					curText += line+"\n";
				}
				
			}
			
			if(state == 3)
			{
				curText += line+"\n";
			}
		
		}
		
		//save the last section
		root.addNode(curSection, curText, sectionOrder++);
		curSection = "";
		curText = "";
		//Util.log(root.printNode());
		hDoc = new HierarchicalDocumentStructure(docID, fulltext, root);
		hDoc.setOriginalText(originalText);
		hDoc.findStdSections();
		return hDoc;
	}
	
	/**
	 * 
	 * @param fulltext String representation of the text file
	 * @param docID document ID
	 * @param method can be either "regex", "textbox_and_regex", "machine_learning"
	 * @return
	 */
	public static HierarchicalDocumentStructure parseDocument(PdfDocument pdfDoc, String docID, String method)
	{	HierarchicalDocumentStructure hd = null;
		
		//hd =  parseDocumentUsingRegex(pdfDoc.fulltext, docID);
	
		if(method.equals("regex"))
		{	hd =  parseDocumentUsingRegex(pdfDoc.fulltext, docID);
		}
		else if(method.equals("textbox_and_regex"))
		{
			hd =  parseDocumentUsingTextBoxAndRegex(pdfDoc, docID);
		}
		
		//get captions
		if(hd != null) hd.captions =  hd.getRoot().retrieveCaptions();
		//Util.jout("Numcaption"+hd.captions.size());
		return hd;
	}
	
	public static boolean partOfSentence(String text)
	{ 	//simple rule: if text is part of a sentence it must have more than two words 
		//or at least a word with '.'
		//or begin with a small character
		String[] words = text.split("\\s|\\'");
		int numWords = 0;
		
		//new stuff
		if(text.matches("[a-z](.*)")) return true;
		
		for(String word:words)
		{
			if(word.matches("[a-zA-Z][a-zA-Z]+"))
			{
				numWords ++;
			}
		}
		
		if(numWords > 1)
		{
			return true;
		}
		if(numWords == 1)
		{
			if(text.endsWith(".")) return true;
		}
		
		return false;
	}
	
	/**
	 * Scientific documents are full of spurious newline and incomplete words cause by premature newline.
	 * This method will try to eliminate unnecessary newlines to allow more efficient parsing.
	 * 
	 * Assume that text is a chunk or paragraph of plain text
	 */
	public static String reformat(String text)
	{
		String result = "";
		String[] lines = text.split("\n");
		//boolean stitch = false;
		Vector<String> chain = new Vector<String>();
		for(String line: lines)
		{
			line = line.trim();
			
			//System.out.println(line+":"+line.length()+"\n");
			
			if(line.isEmpty()) continue;
			
			//change . to : in caption (e.g. "Figure 1. Timer.fired in Surge." -> "Figure 1: Timer.fired in Surge.")
			if(DocumentElement.isCaption(line))
			{
				//expected to see : before ., if . is detected before :, change it to :
				String temp = "";
				boolean found = false;
				for(char c: line.toCharArray())
				{
					if(c == ':')
					{
						found = true;
					}
					
					if(c == '.' && !found)
					{
						found = true;
						temp += ':';
					}
					else
					{
						temp += c;
					}
				}
				
				line = temp;
			}
			
			//detect if this line is a section or a caption
			if(isSectionName(line) || DocumentElement.isCaption(line))
			{
				chain.add("\n");
			}
			
			if(partOfSentence(line))
			{	
				if(line.matches("[A-Z].*") || line.startsWith("•"))
				{
					chain.add("\n");
				}
				
				if(line.endsWith("-"))
				{
					line = line.substring(0, line.length() - 1);
					chain.add(line);
					chain.add("");
				}
				else if(line.endsWith(".") || line.endsWith(":"))
				{
					chain.add(line);
					chain.add("\n");
				}
				else 
				{
					chain.add(line);
					chain.add(" ");
				}
				
			}
			else
			{
				chain.add(line);
				chain.add("\n");
				
			}

		}
		
		for(String s: chain)
		{
			result += s;
		}
		
		//get rid of blank new lines
		lines = result.split("\n");
		result = "";
		for(String line: lines)
		{	line = line.trim();
			if(line.isEmpty()) continue;
			result += line+"\n";
		}
		result = result.trim();
		return result;
	}
	
	/**
	 * Use regular expresstion to determine if text is a section name
	 * @param text
	 * @return
	 */
	public static boolean isSectionName(String text)
	{	text = text.trim();
		if(isStandardSection(text, DocumentNode.StdSection.ABS)) return true;
		
		//section usually has fewer than 10 words
		if(text.split("\\s").length > 15) return false;
		
		if(isStandardSection(text, DocumentNode.StdSection.INT)) return true;
		if(isStandardSection(text, DocumentNode.StdSection.BCK)) return true;
		if(isStandardSection(text, DocumentNode.StdSection.RAD)) return true;
		if(isStandardSection(text, DocumentNode.StdSection.CON)) return true;
		if(isStandardSection(text, DocumentNode.StdSection.ACK)) return true;
		if(isStandardSection(text, DocumentNode.StdSection.REF)) return true;
		
		
		if(text.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)) return true;
		//if(text.matches(CitationUtil.SECTION_NAME_PATTERN_WITHOUT_NUMBERS_PATTERN)) return true;
		
		return false;
	}
	
	public static boolean isStandardSection(String text, DocumentNode.StdSection stdSectType)
	{
		text = text.trim();
		boolean result = false;
		switch(stdSectType)
		{	case HDR:
			{
				result = text.equals("HEADER");
			}break;
			case CHP:
			{
				String pattern = "(CHAPTER|Chapter)\\s+[0-9]+";
				result = text.matches(pattern);
			}break;
			case ABS:
			{
				String pattern = "Abstract"
						+"|ABSTRACT"
						+"|(Abstract|ABSTRACT)\\.\\s+.*"	//Abstract. (+text)
						//+"|(Abstract|ABSTRACT)(—|(\\65533))[A-Z].*"	//Abstract—Modern...
						+"|(Abstract|ABSTRACT)[\\s]?[\\—\\-\\–]?[\\s]?[A-Z].*"	//Abstract—Modern...
						+"|(Abstract|ABSTRACT)\\—" //Abstract—
						+"|ABSTRACT\\s+[A-Z].*"	//ABSTRACT The description...
						+"|(Abstract|ABSTRACT):\\s+.*"	//ABSTRACT: This paper
						;
				result = text.matches(pattern);
			}break;
			
			case INT:
			{
				String pattern = "(([1iI](\\.)?)(\\s+))?"
					+"(Introduction|INTRODUCTION|Introduct ion)"
					+"([:\\.]?(\\s+)(and|[A-Z]).*)?"
					;
				//1. Introduction: Link between Water and Poverty
			result = text.matches(pattern);
			}break;
			
			case BCK:
			{
				String pattern = "(([1-9][0-9]*(\\.[1-9][0-9]*)*)|[iIvVxX]+)(\\.?)(\\s+)"	//require numbers
						+ ".*("
						+"Background|BACKGROUND"
						+"|Previous Work|Previous work|PREVIOUS WORK"
						+"|Similar work|Similar Work|SIMILAR WORK"
						+"|Related Research|Related research|RELATED RESEARCH"
						+"|PRELIMINARIES|Preliminaries"
						+"|Preview|PREVIEW"
						+"|Previous Efforts CHAT 80 PRAT 89 and HSQL"
						+"|Related Work|Related work|RELATED WORK"
						+"|Motivation|MOTIVATION|motivation"
						+"|Overview|overview|OVERVIEW"
						+"|Review|review|REVIEW"
						+"|PREAMBLE|Preamble"
						+"|STATE OF THE ART|State of the Art|State of the art"
						+")[^\\.:]*";
				
				result = text.matches(pattern);
				//false if begin with small charater
				if(text.matches("[a-z].*")) result = false;
			}break;
			
			case RAD:
			{
				String pattern = "(([1iI](\\.)?)(\\s+))?"
						//"((([1-9][0-9]*(\\.[1-9][0-9]*)*)|[iIvVxX]+)(\\.?)(\\s+))"	//require numbers
					+ ".*("
					+"Evaluation|EVALUATION"
					+"|Experimental Results|Experimental results|EXPERIMENTAL RESULTS"
					+"|EXPERIMENTS|Experiments|Experiment"
					+"|Discussion|DISCUSSION"
					+"|Performance Tests|Performance tests"
					+"|Results|RESULTS"
					+")[^\\.:]*";
			result = text.matches(pattern);
			//false if begin with small charater
			if(text.matches("[a-z].*")) result = false;
			}break;
			
			case CON:
			{
				String pattern = "((([1-9][0-9]*(\\.[1-9][0-9]*)*)|[iIvVxX]+)(\\.?)(\\s+))?"	//require numbers
					+ ".*("
					+"Future Work|Future work|FUTURE WORK"
					+"|Further Work|Further work|FURTHER WORK"
					+"|CONCLUSION|Conclusion"
					+"|CONCLUSIONS|Conclusions"
					+"|CONCLUDING REMARKS|Concluding remarks|Concluding Remarks"
					+"|SUMMARY|Summary|Conc lus ions"
					+"|OPEN QUESTIONS|Open Questions|Open questions"
					+")[^\\.:]*";
			result = text.matches(pattern);
			
			//false if begin with small charater
			if(text.matches("[a-z].*")) result = false;
			}break;
			
			case ACK:
			{
				String pattern = "((([1-9][0-9]*(\\.[1-9][0-9]*)*)|[iIvVxX]+)(\\.?)(\\s+))?"	//require numbers
					+ "("
					+"Acknowledgments|ACKNOWLEDGMENTS|Acknowledgements|ACKNOWLEDGEMENTS"
					+"|Acknowledgment|ACKNOWLEDGMENT|Acknowledgement|ACKNOWLEDGEMENT"
					+")([\\—\\-\\–\\.].*)?";
			result = text.matches(pattern);
			}break;
			
			case REF:
			{
				String pattern = "((([1-9][0-9]*(\\.[1-9][0-9]*)*)|[iIvVxX]+)(\\.?)(\\s+))?"	//require numbers
					+ "("
					+"Reference|REFERENCE|References|REFERENCES|Re ferences"
					+"|Bibliography|BIBLIOGRAPHY"
					+")";
				result = text.matches(pattern);
			}break;
		}
		
		return result;
	}
	
	
	/**
	 * Attempt to clean meaningless line (example page number)
	 * @param text
	 * @return
	 */
	public static String cleanText(String text)
	{
		String[] lines = text.split("\n");
		String result = "";
		for(String line: lines)
		{	line = line.trim();
			boolean pass = false;
			if(line.isEmpty()) pass = true;
			
			if(!line.matches(".*[a-zA-z][a-zA-z].*")) pass=true;	//a meaningful line must contain least one word
			if(line.endsWith(".")) pass = false;
			
			if(!pass)
			{
				result+=line+"\n";
			}
		}
		
		return result;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{	
		/*
		Vector<String> fileList = Directory.listAllFiles("./sample/DocSegTestSample/test_three_methods_02", ".pdf", 1);
		for(String filename: fileList)
		{	Util.log("Processing "+filename+"\n");
			PdfDocument pdfDoc = PdfExtractor.getPdfDocument(filename);
			Util.log("has boxes = "+pdfDoc.textBoxes.size()+"\n");
			//text = DocumentUtil.readText("./sample/10.1.1.7.11.txt");
			HierarchicalDocumentStructure hd_regex = DocumentSegmentator.parseDocument(pdfDoc, "tempid", "regex");
			HierarchicalDocumentStructure hd_textbox_and_regex = DocumentSegmentator.parseDocument(pdfDoc, "tempid", "textbox_and_regex");
			
			HierarchicalDocumentStructure combineMethod = hd_textbox_and_regex;
			if(hd_textbox_and_regex.getAllSections().size() <= 1)	//if textbox method fail
			{
				combineMethod = hd_regex;
			}
			TextExtractor.PDFBox_pdfToText(filename, filename.replace(".pdf", ".txt"));
			hd_regex.getGraph(filename+".1regex.pdf");
			hd_textbox_and_regex.getGraph(filename+".2tb_and_regex.pdf");
			pdfDoc.printTextBoxesToHTML(filename+".2textbox.html");
			combineMethod.getGraph(filename+".3combine.pdf");
			
		}
		*/
		//Util.jout(hd.printDoc());
		
		//System.out.println(reformat(readText("sample/10.1.1.111.8323.txt")));
		//System.out.println(reformat(readText("sample/10.1.1.111.1009.txt")));
		//System.out.println(isSectionName("2.1 View-Dependent Simpliï¬�cation9\n"));
		//text = "RESULTS AND DISCUSSION";
		//System.out.println(isStandardSection(text, DocumentNode.StdSection.RAD));
		//Util.jout(""+"3.1 Fixed-lag Gibbs sampler for SLAM".matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN));
		//System.out.println(parseDocument(DocumentUtil.readText("sample/sample_citing_papers_text/10.1.1.69.8010.txt"), "texpid").printDoc());
		//Util.jout(""+"".split("\\.").length);
		//text = "Regression algorithm examines the relationship between a quantitative dependent variable and one or more quantitative independent variables [1]";
		//Util.jout(""+text.matches(CitationUtil.CITATION_SENTENCE_PATTERN));
		
		 
		String text = "This paper presents an asynchronous distributed algorithm\n"
				+"for solving the maximum flow problem which is based"
				+"on the preflow-push approach of Golberg-Tarjan. Each";
		
		Util.jout(reformat(text));
		
		
		Util.jout(""+isStandardSection("Chapter 6", DocumentNode.StdSection.CHP));
	
	}
	
	
}
