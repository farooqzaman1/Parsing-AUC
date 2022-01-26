package Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import org.apache.commons.math3.stat.Frequency;
import org.apache.pdfbox.util.TextPosition;

/**
 * A text box is a sequence of characters that are used in the same context. It is different than
 * a textpiece in the sense that, for example, if a symbol alpha (which usually have different font size/style)
 * is represented in a sentence, it will be treated as a separate textpiece, but is included in the same
 * textbox as the sentence
 * @author aum
 *
 */
public class TextBox {
	public String text = "";
	
	public int startLineNumber = 0;
	public int endLineNumber = 0;
	
	//other metadata
	public double avgFontSize = 0.0;
	public double modeFontSize = 0.0;
	public double maxFontSize = 0.0;
	public double firstFontSize = 0.0;
	public double minFontSize = 0.0;
	
	public int pageNumber = 0;	//page number in which this box first appears in
	public int numWords = 0;
	
	public TextBox getCopy()
	{
		TextBox tb = new TextBox();
		tb.avgFontSize = this.avgFontSize;
		tb.firstFontSize = this.firstFontSize;
		tb.maxFontSize = this.maxFontSize;
		tb.minFontSize= this.minFontSize;
		tb.modeFontSize = this.modeFontSize;
		tb.numWords = this.numWords;
		tb.pageNumber = this.pageNumber;
		tb.text = this.text;
		
		return tb;
	}
	
	
	
	//************************************ new version******************************************
	//A text box a collection of text line
	//All property calculations are done in the process
	public Vector<TextLine> textLines = new Vector<TextLine>();
	
	public String getText()
	{	String result = "";
		for(TextLine tl: textLines)
		{
			result += tl.text+"\n";
		}
		
		return result;
	}
	
	public int getStartLineNumber()
	{
		if(textLines.isEmpty()) return -1;
		else return textLines.elementAt(0).lineNumber;
	}
	
	public int getEndLineNumber()
	{
		if(textLines.isEmpty()) return -1;
		else return textLines.elementAt(textLines.size() - 1).lineNumber;
	}
	
	public void addTextLine(TextLine tl)
	{
		textLines.add(tl);
	}
	
	public double getModeFontSize()
	{
		Frequency freq = new Frequency();
		HashSet<Double> vals = new HashSet<Double>();
		for(TextLine tl: textLines)
		{	Double val = tl.fs_mode;
			
			freq.addValue(val);
			vals.add(val);
		}
		
		Double targetMode = (double) 0;
		long curMax = 0;
		for(Double val: vals)
		{
			if(freq.getCount(val) > curMax)
			{
				curMax = freq.getCount(val);
				targetMode = val;
			}
		}
		
		return targetMode.doubleValue();
	}
	
	public int getNumLines()
	{
		return textLines.size();
	}
	
	@Override
	public String toString()
	{
		String result = "--------------------------------------\n";
		for(TextLine tl: textLines)
		{
			result += "["+tl.lineNumber+"]"+tl.text+"\n";
		}
		return result;
	}
}
