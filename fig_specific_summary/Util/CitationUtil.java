package Util;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import PdfSegmentation.DocumentSegmentator;

import com.aliasi.spell.TfIdfDistance;
import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;
import com.aliasi.util.Files;


public class CitationUtil {
	public static String SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN = "("
		+"([1-9][0-9]*)"	//10
		+"|([1-9][0-9]*\\.)" //10.
		+"|([1-9][0-9]*(\\.[0-9]+)+(\\.)?)"	//10.0.1 and 10.0.1.
		+"|([IVX]+(\\.)*)"	//V or III.
		+")"
		//+"(\\s+)([A-Z][^\\.]+[a-zA-Z\\-])";
		+"(\\s+)([A-Z][^\\.]+[^\\.|;|:])";
	public static String SECTION_NAME_PATTERN_WITHOUT_NUMBERS_PATTERN = 
		"([A-Z][a-zA-Z]*)(\\s+[A-Z][a-zA-Z]*)+";
	
	public static String CITATION_SENTENCE_PATTERN =
		//currently support citation in the form of [13] and [12, 1]
		".*\\[(\\d+(,(\\s)?\\d+)*)\\](.*)?[\\.\\?:]"
		+"(\\s[A-Z].*)?"; //--> in case something like this "...m oracle model. The"
	
	public static String CITATION_SENTENCE_WITHSYMBOL_PATTERN =
		".*("
		+"(\\[SYMBOL(,\\s?\\d+)*\\])|" //[17] or [17, 18]
		+"(\\[\\d+(,\\s?\\d+)*(,\\sSYMBOL)(,\\s?\\d+)*\\])" //[15, 17] or [15, 17, 16]
		+")(.*)?[\\.\\?:]"
		+"(\\s[A-Z].*)?";
	
	public static String CITATION_PATTERN =
		"\\[(\\d+(,\\s?\\d+)*)\\]";
	
	//public static String CITATION_SYMBOLS_PATTERN = ".*\\[(\\d+(,\\s\\d+)*)\\].*";
	
	public static String[] ALGORITHM_KEYWORDS = {"algorithm", "method", "procedure", "pseudo-code"};
	
	public static String romanToDecimal(String r)
	{	r = r.toLowerCase().trim();
		if(r.equals("i")) return "1";
		else if(r.equals("ii")) return "2";
		else if(r.equals("iii")) return "3";
		else if(r.equals("iv")) return "4";
		else if(r.equals("v")) return "5";
		else if(r.equals("vi")) return "6";
		else if(r.equals("vii")) return "7";
		else if(r.equals("viii")) return "8";
		else if(r.equals("ix")) return "9";
		else if(r.equals("x")) return "10";
		else if(r.equals("xi")) return "11";
		else if(r.equals("xii")) return "12";
		else if(r.equals("xiii")) return "13";
		else if(r.equals("xiv")) return "14";
		else if(r.equals("xv")) return "15";
		else if(r.equals("xvi")) return "16";
		else if(r.equals("xvii")) return "17";
		else if(r.equals("xviii")) return "18";
		else if(r.equals("xix")) return "19";
		else if(r.equals("xx")) return "20";
		else return null;
	}
	
	public static HashMap<String, String> extractCitationTexts(String citeFile)
	{	HashMap<String, String> citeSymbolTextMap = new HashMap<String, String>();
		String citeText = "";
		try
		{	
			//check if citation file exists
			File file=new File(citeFile);
			if(!file.exists())
			{
				Util.log("Cannot find cite file "+citeFile+"\n");
				
			}
			
			BufferedReader reader = new BufferedReader(new FileReader(citeFile));
			String line = "";
			while((line = reader.readLine()) != null)
			{
				citeText += line.trim()+" ";
			}
			
			citeText += "["; //specify end of string
			reader.close();
		}catch(FileNotFoundException fn)
		{	fn.printStackTrace();
			Util.errlog(fn.toString()+citeFile);
			//treat as if no algorithm is found
		}
		catch(Exception e)
		{
			e.printStackTrace();
			Util.errlog(e.toString());
		}
		
		Pattern p = Pattern.compile("\\[(\\s*)([^(\\[|\\])]+)(\\s*)\\]([^\\[]*)");
		Matcher m = p.matcher(citeText);
		
		while(m.find()) {
			String citeSymbol = m.group(2).trim();
			String text = m.group(4).trim();
			
			//Util.jout("Extracted: "+citeSymbol+">>"+text+"\n");
			
			citeSymbolTextMap.put(citeSymbol, text);
		}
		
		return citeSymbolTextMap;
	}
	
	public static String getCiteSymbol(String citeText, String citeFile)
	{	HashMap<String, String> citeSymbolTextMap = extractCitationTexts(citeFile);
		
		//initialize TFIDF
		TokenizerFactory tokenizerFactory = IndoEuropeanTokenizerFactory.INSTANCE;
        TfIdfDistance tfIdf = new TfIdfDistance(tokenizerFactory);
        for (String text : citeSymbolTextMap.values())
        {	tfIdf.handle(text);
        }
        tfIdf.handle(citeText);
		
		
			
			
		double[] tfidfDistances = new double[citeSymbolTextMap.size()];
		String[] keys = new String[citeSymbolTextMap.size()];
		
		int j = 0;
		for(String key: citeSymbolTextMap.keySet())
		{	
			tfidfDistances[j] = tfIdf.distance(citeText,citeSymbolTextMap.get(key));
			keys[j] = key;
			j++;
		}
				
		//pick the citation with smallest tfidf distance
		double smallestDist = 10.0;
		int index = -1;
		for(int i = 0; i < tfidfDistances.length; i++)
		{	
			if(tfidfDistances[i] < smallestDist) 
			{	smallestDist = tfidfDistances[i];
				index = i;
			}
		}
		
		if(index < 0) return null;
		return keys[index];
		
	}
	
	public static Vector < Vector<String> > getCitationContext(String citeSymbol, String bodyFile, int numPreSentences, int numPostSentences)
	{	Vector < Vector<String> > citeContexts = new Vector < Vector<String> >();
		Vector<String> sentences = new Vector<String>();
		String rawText = DocumentSegmentator.reformat(DocumentUtil.readText(bodyFile));
		SentenceProducer sProducer = new SentenceProducer(rawText, 2);
		String s = null;
		while((s = sProducer.nextSentence()) != null)
		{	s = s.trim();
			sentences.add(s);
		}
		
		Pattern p = Pattern.compile("(.*)((\\[(\\s*)"+citeSymbol+"(\\s*)\\])|(\\[(\\s*)"+citeSymbol+"(\\s*),)|(,(\\s*)"+citeSymbol+"(\\s*)\\]))(.*)");
		for(int i = 0; i < sentences.size(); i++)
		{
			Matcher m = p.matcher(sentences.elementAt(i));
			if(m.matches())
			//fetch pre cur and post sentences
			{	
				//filter only algorithm citation context
				/*String sent = sentences.elementAt(i).toLowerCase();
				boolean hasKeyword = false;
				for(String keyword: ALGORITHM_KEYWORDS)
				{
					if(sent.contains(keyword))
					{
						hasKeyword = true;
						break;
					}
				}
				
				if(!hasKeyword) continue;
				*/
				
				Vector<String> context = new Vector<String>();
				int start = i - numPreSentences; if(start < 0) start = 0;
				int end = i + numPostSentences; if(end >= sentences.size()) end = sentences.size() - 1;
				
				for(int j = start; j <= end; j++)
				{
					context.add(sentences.elementAt(j));
				}
				
				citeContexts.add(context);
			}
		}
		
		return citeContexts;
	}
	
	public static void main(String args[])
	{	/*String citeSymbol = "4";
		String s = "dfsdfs [5, 4 ] fsdfsgsfgs";
		Pattern p = Pattern.compile("(.*)((\\[(\\s*)"+citeSymbol+"(\\s*)\\])|(\\[(\\s*)"+citeSymbol+"(\\s*),)|(,(\\s*)"+citeSymbol+"(\\s*)\\]))(.*)");
		Matcher m = p.matcher(s);
		System.out.println(m.matches());
		*/
		/*String text = "Guaranteed delivery is addressed for instance by Murphy, Picco, and Moreau [10a, 12, 34].";
		Util.jout(""+text.matches(CITATION_SENTENCE_PATTERN));
		*/
		
		String text = "3 Aligned Template Estimation (ATE";
		Util.jout(""+text.matches(SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN ));
		//Util.jout(""+DocumentSegmentator.isStandardSection("Acknowledgments", DocumentNode.StdSection.ACK));
		
	}
	
	
}


