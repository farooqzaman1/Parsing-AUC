package PdfSegmentation;

import java.util.Vector;

import Model.HierarchicalDocumentStructure;
import Model.PdfDocument;
import Model.TextBox;
import Model.TextLine;
import Util.Directory;
import Util.Util;

/**
 * This is the main class responsible for taking a PDF file, process it
 * and output a PdfDocument which contains all sort of information of that
 * PDF file such as extracted text, hierarchical document, etc.
 * 
 * The product PdfDocument will be used extensively in further tasks
 * @author aum
 *
 */
public class PdfExtractor {
	
	/**
	 * Main method
	 * @param pdfFilename
	 * @return
	 */
	public static PdfDocument getPdfDocument(String pdfFilename)
	{	PdfDocument result = new PdfDocument();
	result.fulltext = "";
	result.reformText = "";
	result.filename = pdfFilename;
		try{
		
		String infoText = TextExtractor.extractPDFInfo(pdfFilename);
		
		String lines[] = infoText.split("\n");
		int curPage = 0;
		
		//get textLines
		Vector<TextLine> textLines = new Vector<TextLine>();
		for(String line:lines)
		{	
			line = line.trim();
			
			if(line.startsWith("[::line_info_start::]"))//normal line
			{	TextLine textLine = new TextLine(line);
				textLine.pageNumber = curPage+1;
				
				result.fulltext += textLine.text+"\n";
				result.reformText += textLine.text+"\n";
				
				textLines.add(textLine);
			}
			else if(line.startsWith("[::PAGE::]"))//page delimiter
			{
				curPage = Integer.parseInt(line.replace("[::PAGE::]", ""));
				result.reformText += line+"\n";
			}
		}
		
		
		
		result.textBoxes = BoxCutter.getTextBoxesUsingLineMerging(textLines);
		
		//get Hierarchical document
		result.hd = DocumentSegmentator.parseDocument(result, Directory.getFileID(pdfFilename), "regex");
		if(result.hd.getAllSections().size() <= 1)	//if textbox method fail
		{	
			result.hd = DocumentSegmentator.parseDocument(result, Directory.getFileID(pdfFilename), "textbox_and_regex");
		}
		
		result.generateTextLines();	//for experimental puroses
		
	}catch(Exception e)
	{ e.printStackTrace();}
		//return null;
		return result;
		
		
	}
	
	
	public static Vector<TextLine> extractTextLinesFromPDF(String pdfFile)
	{
		String infoText = TextExtractor.extractPDFInfo(pdfFile);
		String lines[] = infoText.split("\n");
		int curPage = 0;
		
		//get textLines
		Vector<TextLine> textLines = new Vector<TextLine>();
		for(String line:lines)
		{	
			line = line.trim();
			
			if(line.startsWith("[::line_info_start::]"))//normal line
			{	TextLine textLine = new TextLine(line);
				//if(textLine.lineNumber == 0) Util.jout(line+"\n");
				textLine.pageNumber = curPage+1;
				textLines.add(textLine);
			}
			else if(line.startsWith("[::PAGE::]"))//page delimiter
			{
				curPage = Integer.parseInt(line.replace("[::PAGE::]", ""));
			}
		}
		
		return textLines;
	}
	
	
	
	
	public static void main(String[] args)
	{	/*String path = "./sample/sample_algo_pdf";
		Vector<String> fileList = Directory.listAllFiles(path, ".pdf", 1);
		for(String filename: fileList)
		{
			PdfDocument pdfDoc = getPdfDocument(filename);
			pdfDoc.printTextBoxesToHTML(filename+".html");
		}
		*/
		/*String filename = "./sample/10.1.1.66.6837.pdf";
		PdfDocument pdfDoc = getPdfDocument(filename);
		pdfDoc.printTextBoxesToHTML(filename.replace(".pdf", ".html"));*/
		
		//pdfDoc.hd.getGraph("./sample/test.sections.pdf");
		//Util.jout(pdfDoc.hd.printDoc("chunk"));
		//pdfDoc.printExperimentalResult(filename.replace(".pdf", ".goalstandard.txt"));
		
		Util.printVector(extractTextLinesFromPDF("./sample/10.1.1.66.6837.pdf"));
	}
}
