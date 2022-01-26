package Model;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Vector;

import org.apache.pdfbox.util.TextPosition;


/**
 * Contains information of a PDF file and its extracted versions
 * @author aum
 *
 */

public class PdfDocument {
	
	public String fulltext = null;	//full raw extracted text, no page number
	public String reformText = null; //clean, reformatted text, with pageNumber
	public Vector<String> reformTextByPage = new Vector<String> ();	
	public Vector<TextBox> textBoxes = new Vector<TextBox>();	//a unit is a text interpreted as having the same intention i.e. header, a whole paragraph
	public HierarchicalDocumentStructure hd = null;	//h-doc representation
	public String filename;
	
	//for experiments
	public Vector<TextLine> textLines = new Vector<TextLine>();	//lines of text in the document with its metadata
	
	/*
	 * Flatten the document into lines, and classify each line whether it is
	 * a sentence, a section header, or a caption
	 */
	public void generateTextLines()
	{
		textLines =  new Vector<TextLine>();
		
		Vector<DocumentNode> orderedNodes = hd.getAllSections();
		int countLines = 0;
		for(DocumentNode node: orderedNodes)
		{
			TextLine tl = new TextLine();
			tl.text = node.getRawSectName();
			tl.isSectionHeader  =true;
			tl.lineNumber = ++countLines;
			
			textLines.add(tl);
			
			for(String sentence: node.getSentences())
			{
				//check if this is a caption
				tl = new TextLine();
				tl.text = sentence;
				if(DocumentElement.isCaption(sentence))
				{
					tl.isCaption = true;
				}
				else
				{
					tl.isSentence = true;
				}
				tl.lineNumber = ++countLines;
				textLines.add(tl);
				//TODO should also consider detecting garbage lines
			}
		}
	}
	
	/**
	 * Generate a text file of the processed (after section segmentation is done) PDF with line numbers marked in front.
	 * File takes this format:
	 * [::doc_metadata::]
	 * [::id::] <id>
	 * [::title::] <title>
	 * [::year::] <year>
	 * [::/doc_metadata::]
	 * [::unique_algorithms::]
	 * <pseudo1_id>, <pseudo2_id>, ...|<step1_id>, <step2_id>, ...|<section1_id>, <section2_id>, .. //algorithm 1
	 * [::/unique_algorithms::]
	 * [::captions::]
	 * (<captionid>)[::-::] <caption>
	 * [::/captions::]
	 * [::begin_body::]
	 * (<line_number>(:sec_id))[pseudo#][step#][section#][diagram#][::].....
	 * [::/begin_body::]
	 * @param outFilename
	 */
	public void printExperimentalResult(String outFilename)
	{	String text = "";
		//obtain metadata from csxDatabase
		String metaText = "";
		//TODO: implement
		text += "[::doc_metadata::]\n"+metaText+"\n[::/doc_metadata::]\n\n";
		
		text += "[::unique_algorithms::]\n[::unique_algorithms::]\n\n";
		text += "[::body::]\n";
		
		//get linear representation of document
		//Vector<String> lines = hd.flatten();
		//int countLines = 0;
		text += "(line_number)[pseu_id][step_id][sect_id][diag_id][::-::]<content>\n";
		for(TextLine tl: textLines)
		{	
			String lineClass = "";
			if(tl.isCaption) lineClass = "CAP";
			else if(tl.isGabage) lineClass = "GRB";
			else if(tl.isSectionHeader) lineClass = "HDR";
			else if(tl.isSentence) lineClass = "SNT";
			
			String pseudocodeNumber = "";
			if(tl.pseudoNumber != -1) pseudocodeNumber = ""+tl.pseudoNumber;
			
			String stepNumber = "";
			if(tl.stepNumber != -1) stepNumber = ""+tl.stepNumber;
			
			String sectionNumber = "";
			if(tl.sectNumber != -1) sectionNumber = ""+tl.sectNumber;
			
			text += "["+tl.lineNumber+":"+lineClass+"]["+pseudocodeNumber+"]["+stepNumber+"]["+sectionNumber+"][][::-::]"+tl.text+"\n";
		}
		text += "[::/body::]\n";
		
		//write out
		try{
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename));
			writer.write(text);
			writer.close();
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public void printTextBoxesToHTML(String outfile)
	{	
		String[] colors = {"red", "blue", "green", "BlueViolet", "Brown", "CadetBlue", "Crimson", "purple", "Fuchsia"};
    	int currentColorIndex = 0;
    	
    	HashMap<Double, String> colorMap = new HashMap<Double, String>();
    	
    	for(TextBox tb: textBoxes)
    	{	//Double key = new Double(tb.getAvgHeight());
    		//Double key = new Double(tb.firstFontSize);
    		Double key = new Double(tb.modeFontSize);
    		
    		//Double key = new Double(tb.getModeWidth());
    		
    		String color = colorMap.get(key);
    		if(color == null)
    		{	currentColorIndex++;
    			if(currentColorIndex >= colors.length - 1) currentColorIndex = 0;
    			colorMap.put(key, colors[currentColorIndex]);
    		}
    	}
    			
		String temp = "";
		try{
			BufferedWriter htmlBoxWriter = new BufferedWriter(new FileWriter(outfile));
	    	temp = "<html>\n";
	    	temp += "<head>\n";
	    	temp += "<meta http-equiv=\"Content-Type\" content=\"text/html; charset=utf-8\" />\n";
	    	temp += "<title>"+outfile+"</title>\n";
	    	temp += "</head>\n<body>";
	    	
	    	for(TextBox tb: textBoxes)
	    	{
	    		// double size = tb.firstFontSize;
	    		double size = tb.modeFontSize;
	    		
	    		temp+="<font size = \""+size+"\" color =\""+colorMap.get(new Double(size))+"\" >";
	    		temp += "<br>"+tb.text.replace("\n", "<br>");
	    		temp+="<br></font>\n<br>\n";
	    	}
	    	
	    	temp += "</body>\n</html>\n";
	    	htmlBoxWriter.write(temp);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
}
