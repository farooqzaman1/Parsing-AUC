package Model;

import java.util.HashSet;
import java.util.Vector;

import Util.Util;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

/**
 * Placeholder for a line in a document.
 * A useful line is either a section header, a sentence, or a caption.
 * @author aum
 *
 */
public class TextLine {
	
	public TextLine(){}
	/**
	 * infoLine is of the form 
	 * [::line_info_start::]line=409|fs_avg=6.898824638790554|fs_mode=6.9940266609191895|fs_first=6.9940266609191895|fs_max=6.9940266609191895|fs_min=6.734988689422607|num_words=2[::line_info_stop::] 5.1% 22.6%
	 * @param infoLine
	 */
	public TextLine(String infoLine)
	{
		infoLine = infoLine.trim();
		if(infoLine.startsWith("[::line_info_start::]"))//normal line
		{	
			String[] tokens = infoLine.split("(\\[::line_info_start::\\])|(\\[::line_info_stop::\\])");
			if(tokens.length < 2) return;
			//TextLine textLine = new TextLine();
			//process arguments
			String[] paramList = tokens[1].split("\\|");
			
			if(tokens.length >= 3) this.text = tokens[2].trim();
			else this.text = "";
			
			for(String param: paramList)
			{
				String[] tempPair = param.split("=");
				if(tempPair.length != 2) continue;
				String paramName = tempPair[0].trim().toLowerCase();
				String value = tempPair[1].trim();
				if(paramName.equals("fs_avg")) this.fs_avg = Double.parseDouble(value);
				else if(paramName.equals("fs_mode")) this.fs_mode = Double.parseDouble(value);
				else if(paramName.equals("fs_first")) this.fs_first = Double.parseDouble(value);
				else if(paramName.equals("fs_max")) this.fs_max = Double.parseDouble(value);
				else if(paramName.equals("fs_min")) this.fs_min = Double.parseDouble(value);
				else if(paramName.equals("fs_variance")) this.fs_variance = Double.parseDouble(value);
				else if(paramName.equals("num_words")) this.num_words = Integer.parseInt(value);
				else if(paramName.equals("line")) this.lineNumber = Integer.parseInt(value);
				else if(paramName.equals("indentation")) this.indentation = Double.parseDouble(value);
				else if(paramName.equals("indentation_avgoffirst4")) this.indentation_avgoffirst4 = Double.parseDouble(value);
				else if(paramName.equals("font_styles"))
				{	value = value.trim();
					if(value.isEmpty()) continue;
					String[] fonts = value.split(",");
					for(String f: fonts)
					{
						this.fontStyleSet.add(f);
					}
				}
				else if(paramName.equals("num_fontstyle_switches")) this.numFontStylesSwitches = Integer.parseInt(value);
				else if(paramName.equals("are_all_chars_bold")) this.are_all_chars_bold = Integer.parseInt(value);
				else if(paramName.equals("first_char_pos_y")) this.first_char_pos_y = Double.parseDouble(value);
				else if(paramName.equals("avg_pos_y")) this.avg_pos_y = Double.parseDouble(value);
				else if(paramName.equals("pos_x_start")) this.pos_x_start = Double.parseDouble(value);
				else if(paramName.equals("pos_x_end")) this.pos_x_end = Double.parseDouble(value);
				
			}
		}
	}
	
	
	/**
	 * Use TF-IDF to compute the similarity and return the best matched textline
	 * @param textLines
	 * @param str
	 * @return
	 */
	public static TextLine getBestMatchedTextLine(Vector<TextLine> textLines, String str)
	{
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		TfIdfDistance tfIdf = new TfIdfDistance(tokenizerFactory);
		
		for(TextLine tl: textLines)
		{	if(tl.text == null) continue;
			tfIdf.handle(tl.text);
		}
		
		TextLine bestTL = null;
		double bestSim = 0.0;
		
		for(TextLine tl: textLines)
		{	 if(tl.text == null) continue;
			double sim = tfIdf.proximity(tl.text, str);
			
			if(sim > bestSim)
			{
				bestTL = tl;
				bestSim = sim;
			}
		}
			
		return bestTL;
	}
	
	@Override
	public String toString()
	{
		return "["+lineNumber+"]"+text;
	}
	
	public String text = null;
	
	//font size property
	public double fs_avg = 0;
	public double fs_mode= 0;
	public double fs_first= 0;
	public double fs_max= 0;
	public double fs_min= 0;
	public double fs_variance = 0;
	public double num_words= 0;
	
	//font style property
	public HashSet<String> fontStyleSet = new HashSet<String>();
	public int numFontStylesSwitches = 0;
	public int are_all_chars_bold = 0;
	
	//property set
	public int lineNumber = 0;
	public int pageNumber = 0;
	public boolean isSentence = false;
	public boolean isCaption = false;
	public boolean isSectionHeader = false;
	public boolean isGabage = false;
	
	//indentation
	public double indentation = 0;
	public double indentation_avgoffirst4 = 0;
	
	//tagging proterties
	public int pseudoNumber = -1;
	public int stepNumber = -1;
	public int sectNumber = -1;
	
	
	public double first_char_pos_y = 0.0;
	public double avg_pos_y = 0.0;
	public double pos_x_start = 0.0;
	public double pos_x_end = 0.0;
	
	//others
	public String comment = null;
	public int label = 0;
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + lineNumber;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TextLine other = (TextLine) obj;
		if (lineNumber != other.lineNumber)
			return false;
		return true;
	}
	public static void main(String[] args)
	{
		String[] tokens = "[::line_info_start::]line=30|fs_avg=8.112000465393066|fs_mode=8.112000465393066|fs_first=8.112000465393066|fs_max=8.112000465393066|fs_min=8.112000465393066|fs_variance=0.0|are_all_chars_bold=0|num_words=1|indentation=302.9200134277344|indentation_avgoffirst4=302.9200134277344|num_fontstyle_switches=0|font_styles=Times-Roman|first_char_pos_y=764.1599731445312|avg_pos_y=764.1599731445312|pos_x_start=302.9200134277344|pos_x_end=302.9200134277344[::line_info_stop::]".split("(\\[::line_info_start::\\])|(\\[::line_info_stop::\\])");
		Util.jout(""+tokens[2]);
		
	}
}
