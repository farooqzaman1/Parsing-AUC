package Model;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Util.ConfigReader;
import Util.Util;

/**
 * placeholder for figure, table, pseudocode (currently support those with captions)
 * @author aum
 *
 */
public class DocumentElement {
	public static enum ElType {TABLE, FIGURE, ALGO, UNKNOWN};
	
	//essential metadata
	private ElType elType = ElType.UNKNOWN;
	String id = null;
	public String caption = null;
	public String sec = null;
	//modification ############
	public String Ref_All_sentences ="";
	//#######
	private int pageNum = -1;
	Vector<RefSentence> refSentences = new Vector<RefSentence>();
	DocumentNode bestRefSect = null;
	
	public static String capPattern = "^(<EL_TYPE>)(\\s)([0-9]+(\\.[0-9]+)*)(\\s*(\\.|:)(\\s)(.*)|(\\s)+[A-Z](.*)|(\\s)*$)"; 
										//"|^(<EL_TYPE>)(\\s)([0-9]+(\\.[0-9]+)*)(\\s)+[A-Z](.*)" + //Algorithm 1 MaxPC-SF (* Shortest-First *)
										//"|^(<EL_TYPE>)(\\s)([0-9]+(\\.[0-9]+)*)(\\s)*$"; //Algorithm 1	//here
	//public static String capPattern = "(.*)(<EL_TYPE>)(\\s)([0-9]+(\\.[0-9]+)*)(\\.|:)(\\s)(.*)";
	public static String refSentPattern = "(.*)(<EL_TYPE>)(\\s)(<EL_ID>)(([^\\.0-9])(.*)(\\.)|(\\s)(.*)(\\.)|(\\.))";
	//public static String refSentPattern="Figure 1,";
	//public static String refSentPattern = "(.*)(\\s)(<EL_TYPE>)(\\s)(<EL_ID>)(([^\\.0-9])(.*)(\\.)|(\\s)(.*)(\\.)|(\\.))";	
	public static ConfigReader keywords = new ConfigReader("./code/keywords.txt");
	
	Vector<DocumentNode> refSections = new Vector<DocumentNode>();
	
	public String display()
	{
		String t = "===================\n";
		t += "Caption: "+caption+"\n";
		t += "Type: "+getElType().toString()+"|ID : "+id+"\n";
		t += "RefSent: \n";
		for(RefSentence sent: refSentences)
		{
			
			t += "\t["+sent.section.getRawSectName()+"]"+sent.sentence+"\n";
			//modification###########
			Ref_All_sentences=Ref_All_sentences+"...."+sent.sentence;
		}
		t += "BESTSECT: "+getBestRefSect().getRawSectName()+"\n";
		t += "PageNum: "+getPageNum()+"\n";
		return t;
	}
	
	/**
	 * 1. parse through each sentence to detect a caption, and page number
	 * 2. parse the whole HD for ref sentences and node
	 * @param textWPN
	 * @return
	 */
	public static Vector<DocumentElement> getDocElFromFile(String textWPN, HierarchicalDocumentStructure hd)
	{
		Vector<DocumentElement> results = new Vector<DocumentElement>();
		//not yet done
		return results;
	}
	
	//detect caption and produce document element
	//Note that caption might be embed in a line. 
	public static DocumentElement getDocElFromSentence(String sent, HierarchicalDocumentStructure hd)
	{	DocumentElement docEl = null;
		for(ElType eltype: ElType.values())//for each element type
		{
			String docElString = eltype.toString();
			String[] docElKeys = keywords.getValue(docElString+"_key").split("\\|");
			for(String key: docElKeys)
			{
				Pattern p = Pattern.compile(capPattern.replace("<EL_TYPE>", key));
				Matcher m = p.matcher(sent);
				while(m.find())
				{
					docEl = new DocumentElement();
					docEl.setElType(eltype);
					docEl.caption = sent;
					//docEl.sec=getBestRefSect().getRawSectName();
					docEl.id = m.group(3);
					//finding ref sent
					//Util.jout(key+"\n");
					String refSentP = refSentPattern.replace("<EL_TYPE>", key).replace("<EL_ID>", docEl.id);
					
					Vector<DocumentNode> nodes = hd.getAllSections(); 
					
					int[] numRefSents = new int[nodes.size()];
					for(int i = 0; i < nodes.size(); i++)
					{	DocumentNode node = nodes.elementAt(i);
						int refSentCount = 0;
						for(String sentence: node.getSentences())
						{	
							if(sentence.contains(docEl.caption)) continue;	
							//A simple check so that the line with the same caption doesnt get
							//added in
							
							if(sentence.matches(refSentP))
							{	RefSentence refSent = new RefSentence();
								refSent.section = node;
								refSent.sentence = sentence;
								docEl.refSentences.add(refSent);
								refSentCount++;
							}
						}
						numRefSents[i] = refSentCount;
					}
					
					//rank the node by reference sentences and assign it
					int nodeIndex = 0;
					int maxscore = -1;
					for(int i = 0; i < numRefSents.length; i++)
					{	
						if(numRefSents[i]>maxscore)
						{maxscore = numRefSents[i];
						nodeIndex = i;
						}
					}
					docEl.setBestRefSect(nodes.elementAt(nodeIndex));
					
					if(maxscore == 0) //no ref sentence, find the section where it appears in
					{
						for(DocumentNode node: nodes)
						{	boolean found = false;
							for(String sentence: node.getSentences())
							{
								if(sentence.equalsIgnoreCase(docEl.caption))
								{
									docEl.setBestRefSect(node);
									found = true;
									break;
								}
							}
							
							if(found) break;
						}
					}
					
					
					break;
				}
				
				//already found match
				if(docEl != null) break;
			}
		}
		
		return docEl;
	}
	
	
	public void setBestRefSect(DocumentNode bestRefSect) {
		this.bestRefSect = bestRefSect;
	}

	public DocumentNode getBestRefSect() {
		return bestRefSect;
	}

	public void setPageNum(int pageNum) {
		this.pageNum = pageNum;
	}

	public int getPageNum() {
		return pageNum;
	}

	public void setElType(ElType elType) {
		this.elType = elType;
	}

	public ElType getElType() {
		return elType;
	}
	
	/**
	 * Check whether the given piece of text matches caption regular expression
	 * @param sentence
	 */
	public static boolean isCaption(String text)
	{
		for(ElType eltype: ElType.values())//for each element type
		{
			String docElString = eltype.toString();
			String[] docElKeys = keywords.getValue(docElString+"_key").split("\\|");
			for(String key: docElKeys)
			{
				String p = capPattern.replace("<EL_TYPE>", key);
				if(text.matches(p))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	/**
	 * Determine whether this line is an algorithm label
	 * 
	 * Fine-grained localization algorithm
	 * Algorithm 1 Send-All Voting
	 * Algorithm Boost-and-Sample()
	 * Algorithm Seg_TS = Bottom_Up(T , max_error)
	 * Algorithm 2.2 (Munthe-Kaas Methods)
	 * @param text
	 * @return
	 */
	public static boolean isAlgorithmLabel(String text)
	{
		String pattern1 = "[A-Z].*\\s(ALGORITHM|Algorithm)\\s*$";
		String pattern2 = "^(Algorithm|ALGORITHM|Procedure|PROCEDURE)\\s+[^\\.\\:]*";
		if(text == null) return false;
		 
		if(text.matches(pattern1) || text.matches(pattern2)) return true;
		
		return false;
	}

	
	

	
	public static void main(String[] args)
	{
		Util.jout(""+"Figure 4.2: A Cracks Profile".matches(capPattern.replace("<EL_TYPE>", "Figure")));
		//Util.jout(""+"INTRODUCTION (a) Block Cracking (b) Longitudinal Cracking (c) Transverse Cracking (d) alligatorcrackingFigure 1.1: The different types of crackFigure 1.2: PMS mobile digital video system.".contains("Figure 1.1: The different types of crack"));
		
		//Util.jout(""+ isAlgorithmLabel("Figure 1: THE PAGERANK ALGORITHM"));
		
	}
}
