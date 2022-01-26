package Util;

import java.util.Vector;

import Model.TextLine;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

public class TextLineMatcher {
	
	Vector<TextLine> textLines = null;
	TfIdfDistance tfIdf = null;
	
	public TextLineMatcher(Vector<TextLine> textLines)
	{
		initTDIDF(textLines);
	}
	
	public void initTDIDF(Vector<TextLine> textLines)
	{	this.textLines = textLines;
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
		tfIdf = new TfIdfDistance(tokenizerFactory);
		
		for(TextLine tl: textLines)
		{	if(tl.text == null) continue;
			tfIdf.handle(tl.text);
		}
	}
	
	public TextLine getBestMatchedTextLine(String str)
	{
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
}
