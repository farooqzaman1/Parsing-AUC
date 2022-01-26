package PdfSegmentation;

import java.io.BufferedWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Vector;


import org.apache.pdfbox.cos.COSDocument;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;


import Util.*;


//Main purpose is to extract text from a PDF file
//Current version uses PDFBox
public class TextExtractor {
	
	
	/**
	 * Extracts text from the pdf file and include page numbers
	 * @param fileName: path to input pdf filename
	 * @param outfilepath: path to out put text file
	 */
	public static void pdfToTextWithPageNumbers(String fileName, String outfilepath)
	{
		//using PDFTextStripper
		Writer output = null;
        PDDocument document = null;
        PDFTextStripper stripper = null;
        ConfigReader config = new ConfigReader();
        try{
        	document = PDDocument.load(fileName, true);
        }catch(Exception e)
        {
        	e.printStackTrace();
        	System.exit(-1);
        }
        
        try{
	        stripper = new PDFTextStripper(config.getValue("encoding"));
	        stripper.setForceParsing( true );
			//get number of pages 
	        int numPages = document.getNumberOfPages();
	        
	        //parse page by page
	       // BufferedWriter writer = new BufferedWriter(new FileWriter(outfilepath));
	        output = new OutputStreamWriter(new FileOutputStream( outfilepath ) );
	        for(int i = 1; i <= numPages; i++)
	        {
	        	stripper.setStartPage( i );
	            stripper.setEndPage( i );
	            stripper.writeText(document, output);
	            //writer.write(output.toString());
	            output.write("\n[::PAGE::]"+i+"\n");
	            
	            try{
	            	output.flush();
	            }catch(Exception e)
	            {
	            	
	            }
	        }
	        
	        //writer.close();
        }catch(Exception e)
        {
        	e.printStackTrace();
        }finally
        {
        	try{
		        if( output != null )
		        {
		            output.close();
		        }
		        if( document != null )
		        {
		            document.close();
		        }
        	}catch(Exception fe)
        	{
        		
        	}
        }
	}
	
	public static String PDFBox_readPDFFile(String fileName)
	{
		PDFParser parser;
		String parsedText = null;
		PDFTextStripper pdfStripper = null;
		PDDocument pdDoc = null;
		COSDocument cosDoc = null;
		File file = new File(fileName);
		if (!file.isFile()) {
			System.err.println("File " + fileName + " does not exist.");
			return "";
		}
		try {
			parser = new PDFParser(new FileInputStream(file));
		} catch (IOException e) {
			System.err.println("Unable to open PDF Parser. " + e.getMessage());
			return "";
		}
		try {
			parser.parse();
			cosDoc = parser.getDocument();
			pdDoc = new PDDocument(cosDoc);
			pdfStripper = new PDFTextStripper();
			pdfStripper.setStartPage(1);
			pdfStripper.setEndPage(pdDoc.getNumberOfPages());
			
			//test
			
			//pdfInfoStripper.setStartPage(1);
			//pdfInfoStripper.setEndPage(pdfInfoStripper.getEndPage());
			
			//pdfInfoStripper.getText(pdDoc);
			//Util.jout("Info:\n"+pdfInfoStripper.getText(pdDoc));
			PDFBoxInfoStripper p = new PDFBoxInfoStripper();
			p.setStartPage(1);
			p.setEndPage(8);
			p.getText(pdDoc);
			//p.getTextPiecesByPage(new File(fileName));
			
			/*ArrayList<ArrayList<TextPiece>> pages = p.getTextPiecesByPage(new File(fileName));
			

    		try{
    			
    			BufferedWriter writer = new BufferedWriter(new FileWriter("./sample/my_textpieces.txt"));
    			int pageNum = 0;
	    		for(ArrayList<TextPiece> page: pages)
				{	pageNum ++;
					writer.write("$$ Page "+pageNum+"\n");
					for(TextPiece tp: page)
					{
						writer.write("\t"+tp.getText()+"|"+tp.getHeight()+"|"+tp.getFontName()+"\n");
					}
				}
	    		
	    		writer.close();
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    		}
			*/
			//end test
			
			parsedText = pdfStripper.getText(pdDoc);
		} catch (Exception e) {
			System.err
					.println("An exception occured in parsing the PDF Document."
							+ e.getMessage());
		} finally {
			try {
				if (cosDoc != null)
					cosDoc.close();
				if (pdDoc != null)
					pdDoc.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return parsedText;
	}
	
	public static boolean PDFBox_pdfToText(String fileName, String outfilepath) {
		
		
		//writing out output file
		try{
			BufferedWriter out = new BufferedWriter(new FileWriter(outfilepath.replace("pdfs_765", "Textfiles")));
			out.write(PDFBox_readPDFFile(fileName));
			
			out.close();
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	
	public static void batch_PDFBox_textBoxExtract(String dirname)
	{
		Vector<String> files = Directory.listAllFiles(dirname, ".pdf", 1);
		for(String file: files)
		{
			//TextExtractor.PDFBox_textBoxExtract(file, file.replace(".pdf", ".txt"));
		}
	}
	
	public static void extractPDFInfo(String pdfFilename, String outFilename)
	{
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename));
			writer.write(extractPDFInfo(pdfFilename));
			writer.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static String extractPDFInfo(String pdfFilename)
	{
		FileInputStream fi;
		String result = null;
		try {
			fi = new FileInputStream(new File(pdfFilename));
		 

			PDFParser parser = new PDFParser(fi);  
			try{parser.parse();}
			catch(Exception e){ e.printStackTrace();}
			COSDocument cd = parser.getDocument();  
			PDDocument pDoc = new PDDocument(cd);
			PDFBoxInfoStripper stripper = new PDFBoxInfoStripper("UTF-8");  
			result = stripper.getText(pDoc);
			pDoc.close();
			fi.close();
			
			//reformat- a quick fix
			result = result.replace('\0', ' ').replaceAll("\\n|\\r", " ").replace("[::line_info_start::]", "\n[::line_info_start::]").replace("[::PAGE::]", "\n[::PAGE::]").trim();
			
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		return result;
	}
	
	public static void main(String args[]){
		//TextExtractor.PDFBox_pdfToText("sample/p3.pdf", "sample/p3.txt");
		//TextExtractor.PDFBox_pdfToText("sample/test.pdf", "sample/test.txt");
		//TextExtractor.PDFBox_textBoxExtract("sample/test.pdf", "sample/test.txt");
		//TextExtractor.tempTest("sample/10.1.1.111.1281.pdf", "sample/10.1.1.111.1281.txt");
		//batch_PDFBox_textBoxExtract("./sample/test_segmentator_v2_mode");
		//extractPDFInfo("sample/p3.pdf", "sample/p3.feature.txt");
		
		//extractPDFInfo("sample/10.1.1.111.1281.pdf", "sample/10.1.1.111.1281.feature.txt");

		Vector<String> pdfFiles = Directory.listAllFiles("C:\\Users\\Janjua\\eclipse-workspace\\algorithm_flow.2017-01-17\\pdfs_765", "pdf", 1);
		//E:\MY DATA\PhD\IQRA DATA\Eclipse workspace\algorithm_flow.2017-01-17\citation paper\Cited by
		//C:\Iqra Data\ACL papers\All data
		for(String f: pdfFiles)
		{
			//pdfToTextWithPageNumbers(f, "./data/bimbo/"+Directory.getFileID(f)+".bimbo");
		   
		    TextExtractor.PDFBox_pdfToText(f,f+".txt");
			System.out.println("file name:"+f);
			//extractPDFInfo(f, f+".feature.txt");
			
			//extractPDFInfo("sample/10.1.1.111.1281.pdf", "sample/10.1.1.111.1281.feature.txt");
		}
		
		Util.jout("Done\n");
	}
	
}


