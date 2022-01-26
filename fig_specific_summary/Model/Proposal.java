package Model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Model.DocumentNode.StdSection;
import PdfSegmentation.DocumentSegmentator;
import PdfSegmentation.PdfExtractor;
import PdfSegmentation.TextExtractor;
import Util.ConfigReader;
import Util.Directory;
import Util.DocumentUtil;
import Util.LingPipe_PhraseChunker;
import Util.SentenceProducer;
import Util.Util;



/**
 * This class holds information about a proposal proposed in a paper. 
 * For a given sentence this class provide a method that determine whether it
 * is a proposal sentence. This class also provides a mechanism to extract metadata
 * about the proposal given a proposal sentence.  
 * @author Suppawong
 *
 */
public class Proposal {
	public enum ProposalType {	UNKNOWN,
						//algorithm equivalent types
						ALGORITHM,
						METHOD,
						APPROACH,
						PROCEDURE,
						
						//algorithm inference types (likely to have some algorithms embedded in the work)
						MECHANISM,
						TECHNIQUE,
						FRAMEWORK,
						MODEL,
						SCHEME,
						SIGNATURE, //in cryptography
						SYSTEM,	//i.e. crypto system
						STRUCTURE,
						
						//Compound (i.e. modification of this approach)
						//needs to be treated specially
						COMPOUND,
						
						//Other
						WAY,
						CALCULATION,
						MATERIAL,
						WAVEGUIDE,
						REPRESENTATION,
						INDEX,
						PROBLEM,
						TRANSLATION,
						CATEGORIZING,
						ENTITY,
						EQUATION,
						RELATIONSHIP,
						REFORMULATION,
						ANALYSIS

						};
	public static LingPipe_PhraseChunker phraseChunker = new LingPipe_PhraseChunker(true, null, null);
	public static boolean libInit = false;
	//mapping keyword to type
	public static HashMap<String, ProposalType> keywordTypeMap = new HashMap<String, ProposalType>();
	
	//mapping keyword to plural(-1)/singular (1)
	public static HashMap<String, Integer> keywordNumberMap = new HashMap<String, Integer>();
	
	//keep all the proposal keywords
	public static HashSet<String> propKeywords = new HashSet<String>();
	
	public static HashSet<String> presentPropVerbs = new HashSet<String>();
	public static HashSet<String> pastPropVerbs = new HashSet<String>();
	
	//map keyword to level of inference of algorithm
	//currently tree level (1,2,3)
	//1: keyword itself means algorithm
	//2: keyword imply that some algorithms can be embedded in the work
	//3: something else unrelated
	public static HashMap<ProposalType, Integer> algoKeywordLevelMap = new HashMap<ProposalType, Integer>();


	int num = 0;	//-1 mean more than 1 (but don't know the exact number)
	private ProposalType type = ProposalType.UNKNOWN;
	private String propSentence;
	private String tense = null;	//present or past for now
	
	int propKeyDistance = -1;	//distance between proposal verb and proposal word
								//useful when there are  2+ proposal word in the sentence
								//like This paper proposes a ... algorithm for ... representations ...
	
	//std section in the document in which this proposal is found
	private DocumentNode.StdSection section = DocumentNode.StdSection.UNKNOWN; 
	
	public Proposal()
	{
		init();
	}
	
	private static void initLibrary()
	{
		//init keywordMap
		ConfigReader keywordFile = new ConfigReader("./code/keywords.txt");
		for(ProposalType pType: Proposal.ProposalType.values())
		{	String typeStr = pType.toString();
			
			//singular or plural
			String typeStrSing = typeStr+"_sing";
			String typeStrPlu = typeStr+"_plu";
			
			String temp = keywordFile.getValue(typeStrSing);
			String[] singTokens = null;
			if(temp!=null)
			{ 	temp = temp.trim();
				singTokens = temp.split("\\|");
				
				for(String w: singTokens)
				{
					propKeywords.add(w);
					keywordTypeMap.put(w, pType);
					keywordNumberMap.put(w, 1);
				}
			}
			
			temp = keywordFile.getValue(typeStrPlu);
			String[] pluTokens = null;
			if(temp != null)
			{	temp = temp.trim(); 
				pluTokens = temp.split("\\|");
				
				for(String w: pluTokens)
				{
					propKeywords.add(w);
					keywordTypeMap.put(w, pType);
					keywordNumberMap.put(w, -1);
				}
			}
			
		}
		
		//init algoKeywordLevelMap
		algoKeywordLevelMap.put(ProposalType.ALGORITHM, new Integer(1));
		algoKeywordLevelMap.put(ProposalType.METHOD, new Integer(1));
		algoKeywordLevelMap.put(ProposalType.APPROACH, new Integer(1));
		algoKeywordLevelMap.put(ProposalType.PROCEDURE, new Integer(1));
		
		algoKeywordLevelMap.put(ProposalType.MECHANISM, new Integer(2));
		algoKeywordLevelMap.put(ProposalType.TECHNIQUE, new Integer(2));
		algoKeywordLevelMap.put(ProposalType.FRAMEWORK, new Integer(2));
		algoKeywordLevelMap.put(ProposalType.MODEL, new Integer(2));
		algoKeywordLevelMap.put(ProposalType.SCHEME, new Integer(2));
		algoKeywordLevelMap.put(ProposalType.SIGNATURE, new Integer(2));
		algoKeywordLevelMap.put(ProposalType.SYSTEM, new Integer(2));
		algoKeywordLevelMap.put(ProposalType.STRUCTURE, new Integer(2));
		
		algoKeywordLevelMap.put(ProposalType.WAY, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.CALCULATION, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.MATERIAL, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.WAVEGUIDE, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.REPRESENTATION, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.INDEX, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.PROBLEM, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.TRANSLATION, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.CATEGORIZING, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.ENTITY, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.EQUATION, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.RELATIONSHIP, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.REFORMULATION, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.ANALYSIS, new Integer(3));
		algoKeywordLevelMap.put(ProposalType.UNKNOWN, new Integer(3));
		
		//loading in proposal verbs
		String temp = keywordFile.getValue("PROPOSE_present");
		if(temp != null)
		{
			String[] tokens = temp.trim().split("\\|");
			presentPropVerbs.addAll(Arrays.asList(tokens));
		}
		
		temp = keywordFile.getValue("PROPOSE_past");
		if(temp != null)
		{
			String[] tokens = temp.trim().split("\\|");
			pastPropVerbs.addAll(Arrays.asList(tokens));
		}
		
		//init regex patterns
		
		
	}
	
	private void init()
	{
		if(!Proposal.libInit)
		{
			Proposal.initLibrary();
			Proposal.libInit = true;
		}
	}
	
	/**
	 * 1. Detect if the given sentence is a proposal sentence
	 * 2. if it is, then extract metadata about the proposal
	 * 
	 * method is the method of detection and can be either "regex" or "pos"
	 * @param sentence
	 * @param method
	 * @return
	 */
	public static Proposal extractProposalFromSentence(String sentence, String method)
	{	if(!Proposal.libInit)
		{
			Proposal.initLibrary();
			Proposal.libInit = true;
		}
	
		
		Proposal result = null;
		
		//using regex approach
		if(method.equalsIgnoreCase("regex"))
		{	String presentPropVerbStr = "";
			for(String v: presentPropVerbs)
			{
				presentPropVerbStr+=v+" ";
			}
			presentPropVerbStr = presentPropVerbStr.trim().replace(' ', '|');
			
			String pastPropVerbStr = "";
			for(String v: pastPropVerbs)
			{
				pastPropVerbStr+=v+" ";
			}
			pastPropVerbStr = pastPropVerbStr.trim().replace(' ', '|');
			
			Proposal prop = null;
			for(String w: propKeywords)
			{	
				//Note that there (.*) at the end of each regex because the sentence segmentator sometimes
				//piggyback noise at the end of a sentence
				String aprPattern = "(.*)(We|we|paper|article)(.*)\\s("+presentPropVerbStr+")\\s(.*?)("+w+")([^A-Za-z].*\\.|\\.)(.*)";
				String pprPattern = "(.*)(\\s)("+w+")(\\s)(.*is.*|.*are.*)\\s("+pastPropVerbStr+")([^A-Za-z].*\\.|\\.)(.*)";
				String apaPattern = "(.*)(We|we|paper|article)(.*)\\s("+pastPropVerbStr+")\\s(.*?)("+w+")([^A-Za-z].*\\.|\\.)(.*)";
				String ppaPattern = "(.*)(\\s)("+w+")(\\s)(.*was.*|.*were.*)\\s("+pastPropVerbStr+")([^A-Za-z].*\\.|\\.)(.*)";
				Proposal tempProp = null;
				if(sentence.matches(aprPattern))
				{	tempProp = new Proposal();
					tempProp.num = keywordNumberMap.get(w).intValue();
					tempProp.setTense("present");
					
					//calculating distance
					Pattern p = Pattern.compile(aprPattern);
					Matcher m = p.matcher(sentence);
					
					tempProp.propKeyDistance = sentence.split("\\s").length;
					int tempDis = 0;
					/*
					while(m.find())
					{	
						//Util.jout(""+m.group(5)+"\n");
						tempDis = m.group(5).trim().split("\\s").length;
						if(tempDis < tempProp.propKeyDistance) tempProp.propKeyDistance = tempDis;
						
					}
					*/
					
					while(m.find())
					{	tempProp.propKeyDistance = m.group(5).trim().split("\\s").length;
						break;
					}
					
				}
				else if(sentence.matches(pprPattern))
				{
					tempProp = new Proposal();
					tempProp.num = keywordNumberMap.get(w).intValue();
					tempProp.setTense("present");
					
					//calculating distance
					Pattern p = Pattern.compile(pprPattern);
					Matcher m = p.matcher(sentence);
					while(m.find())
					{	tempProp.propKeyDistance = m.group(5).trim().split("\\s").length;
						break;
					}
				}
				else if(sentence.matches(ppaPattern))
				{
					tempProp = new Proposal();
					tempProp.num = keywordNumberMap.get(w).intValue();
					tempProp.setTense("past");
					
					//calculating distance
					Pattern p = Pattern.compile(ppaPattern);
					Matcher m = p.matcher(sentence);
					while(m.find())
					{	tempProp.propKeyDistance = m.group(5).trim().split("\\s").length;
						break;
					}
				}
				else if(sentence.matches(apaPattern))
				{	tempProp = new Proposal();
					tempProp.num = keywordNumberMap.get(w).intValue();
					tempProp.setTense("past");
					
					//calculating distance
					Pattern p = Pattern.compile(apaPattern);
					Matcher m = p.matcher(sentence);
					while(m.find())
					{	tempProp.propKeyDistance = m.group(5).trim().split("\\s").length;
						break;
					}
				}
				
				if(tempProp != null)
				{	
					tempProp.setPropSentence(sentence);
					tempProp.setType(keywordTypeMap.get(w));
					
					if(prop == null) prop = tempProp;
					else
					//compare the word distance and get the closer one
					{
						if(tempProp.propKeyDistance < prop.propKeyDistance) prop = tempProp;
					}
				}
			}
			
			result = prop;
			
		}
		//using part of speech tagging approach
		else if (method.equalsIgnoreCase("pos"))
		{
			HashSet<String> nPhrases = new HashSet<String>(phraseChunker.getNounPhrasesFromSentence(sentence));
			HashSet<String> vPhrases = new HashSet<String>(phraseChunker.getVerbPhrasesFromSentence(sentence));
			
			Vector<String> vContained = new Vector<String>();
			Vector<String> nContained = new Vector<String>();
			
			for(String v: presentPropVerbs)
			{	for(String vp: vPhrases)
				{	HashSet<String> tempSet = new HashSet<String>(Arrays.asList(vp.split("\\s")));
					if(tempSet.contains(v))
					{	vContained.add(v);
						break;
					}
				}
			}
			
			for(String v: pastPropVerbs)
			{
				for(String vp: vPhrases)
				{	HashSet<String> tempSet = new HashSet<String>(Arrays.asList(vp.split("\\s")));
					if(tempSet.contains(v))
					{	vContained.add(v);
						break;
					}
				}
			}
			
			for(String n:propKeywords)
			{
				for(String np: nPhrases)
				{	HashSet<String> tempSet = new HashSet<String>(Arrays.asList(np.split("\\s")));
					if(tempSet.contains(n))
					{	nContained.add(n);
						break;
					}
				}
			}
			
			//calculate the distance between each pair of v-n and choose the pair with least absolute distance
			String curV = null;
			String curN = null;
			String[] tokens = sentence.split("\\s");
			int curDis = tokens.length+1;
			
			for(String v:vContained)
			{	int vIndex = -1;
				for(int i = 0; i < tokens.length;i++)
				{
					if(tokens[i].equalsIgnoreCase(v))
					{
						vIndex = i;
						break;
					}
				}
			
				for(String n: nContained)
				{
					int nIndex = tokens.length;
					for(int i = 0; i < tokens.length;i++)
					{
						if(tokens[i].equalsIgnoreCase(n))
						{
							nIndex = i;
							break;
						}
					}
					
					int tempDis = vIndex - nIndex;
					if(Math.abs(tempDis) < Math.abs(curDis))
					{
						curV = v;
						curN = n;
						curDis = tempDis;
					}
				}
			}
			if(curV != null && curN != null)
			{	
				//Util.jout("Sent: " +sentence+"\n");
				//Util.jout("V:"+curV+" N:"+curN+"\n");
				
				
				Proposal tempProp = new Proposal();
				tempProp.num = keywordNumberMap.get(curN).intValue();
				tempProp.propKeyDistance = Math.abs(curDis);
				tempProp.setPropSentence(sentence);
				
				String vTense = "present";
				if(pastPropVerbs.contains(curV)) vTense = "past";
				
				if(vTense.equals("present") && curDis < 0)
				{
					tempProp.setTense("present");
				}
				else if(vTense.equals("past") && curDis < 0)
				{
					tempProp.setTense("past");
				}
				else
				{
					tempProp.setTense("present");
					//will miss the case like ...this algorithm was presented
				}
				
				tempProp.setType(keywordTypeMap.get(curN));
				result = tempProp;
			}
			
		}
		
		return result;
	}
	
	/**
	 * Extract proposals from given text.
	 * 
	 * section is the standard section if known, null otherwise
	 * 
	 * method is the detection method, can be either 'regex' or 'pos'
	 * 
	 * 'past' is meant for conclusion part
	 * @param text
	 * @param mode
	 * @return
	 */
	public static Vector<Proposal> extractProposalsFromText(String text, DocumentNode.StdSection section, String method)
	{	
		Vector<Proposal> result = new Vector<Proposal>();
		
		Vector<String> sentences = SentenceProducer.getSentnecesFromText(text);
		for(String sentence: sentences)
		{
			Proposal tempProp = Proposal.extractProposalFromSentence(sentence, method);
			if(tempProp == null) continue;
			tempProp.setSection(section);
			result.add(tempProp);
		}
		
		return result;
	}
	
	/**
	 * Like extractProposalsFromText, but the text string has already been sentence'ized.
	 * @param node
	 * @param section
	 * @param method
	 * @return
	 */
	public static Vector<Proposal> extractProposalsFromDocNode(DocumentNode node, DocumentNode.StdSection section, String method)
	{	
		Vector<Proposal> result = new Vector<Proposal>();
		
		Vector<String> sentences = node.getSentences();
		for(String sentence: sentences)
		{
			Proposal tempProp = Proposal.extractProposalFromSentence(sentence, method);
			if(tempProp == null) continue;
			tempProp.setSection(section);
			result.add(tempProp);
		}
		
		return result;
	}
	
	/**
	 * Extract proposals from a file (already parsed in text string).
	 * 
	 * method is the detection method, can be either 'regex' or 'pos'
	 * @param text
	 * @param method
	 * @return
	 */
	public static Vector<Proposal> extractProposalsFromFile(PdfDocument pdfDoc, String method)
	{	
		Vector<Proposal> result = new Vector<Proposal>();
		HierarchicalDocumentStructure hDoc = pdfDoc.hd;//DocumentSegmentator.parseDocument(text, "tempid");
		HashMap<DocumentNode.StdSection, Vector<DocumentNode>> stdSections = hDoc.getStdSections();
		for(DocumentNode.StdSection sec: stdSections.keySet())
		{
			Vector<DocumentNode> nodes = stdSections.get(sec);
			for(DocumentNode node: nodes)
			{
				Vector<Proposal> props = Proposal.extractProposalsFromDocNode(node, sec, method);
				result.addAll(props);
			}
		}
		return result;
	}
	
	/**
	 * dirName is the directory where text/pdf files reside (recursive)
	 * method is the detection method, can be either 'regex' or 'pos'
	 * 
	 * Return the number algorithm proposing papers
	 * @param dirName
	 * @param method
	 */
	public static int batchExtract(String dirName, String method) throws Exception
	{	
		Vector<String> filenames = Directory.listAllFiles(dirName, ".pdf", 1);
		int count = 0;
		for(String filename: filenames)
		{
			/*String text = null;
			if(filename.endsWith(".txt"))
			{
				text = DocumentUtil.readText(filename);
			}
			else if(filename.endsWith(".pdf"))
			{
				text = TextExtractor.PDFBox_readPDFFile(filename);
			}
			*/
			PdfDocument pdfDoc = PdfExtractor.getPdfDocument(filename);
			
			Vector<Proposal> props = Proposal.extractProposalsFromFile(pdfDoc, method);
			
			//determine if this document contains some proposed algorithms
			boolean proposing = false;
			for(Proposal p: props)
			{
				if(	(p.getSection() == DocumentNode.StdSection.ABS
						|| p.getSection() == DocumentNode.StdSection.HDR)	//sometime abstract section
																		//is included in the header
																		//due to error of the document
																		//segmentator
			// try to add some coments on data and try to understand it more reqoriously.
						&& p.getTense().equals("present")
					&& (algoKeywordLevelMap.get(p.getType()) <= 2) )
				{	proposing = true;
					count++;
					break;
				}
				/*else if(((p.section == DocumentNode.StdSection.ABS
						|| p.section == DocumentNode.StdSection.HDR)
						&& p.tense.equals("present")
						&& (algoKeywordLevelMap.get(p.type) <= 2))
						
						&&
						
						(p.section == DocumentNode.StdSection.CON
						//sometime abstract section
						//is included in the header
						//due to error of the document
						//segmentator
						&& p.tense.equals("past")
						&& (algoKeywordLevelMap.get(p.type) <= 1)))
				{
					proposing = true;
					count++;
					break;
				}*/
			}
			
			Util.jout("**********File: "+Directory.getFileID(filename)+" Proposing: "+proposing+"\n");
			Util.printVector(props);
		}
		
		Util.jout("@@@Proposing "+count+"/"+filenames.size()+"\n");
		
		return count;
	}
	
	public String toString()
	{
		String result = "";
		result+="{\""+getPropSentence()+"\"\n";
		result+="what="+getType().toString()+", tense="+getTense()+", num="+num+", dis="+propKeyDistance+", sec= "+getSection().toString();
		result+="}\n";
		
		return result;
	}
	
	public static void main(String[] argv)
	{	
		//prepare test
		/*Vector<String> samplePropSent = new Vector<String>();
		try{
			String temp = DocumentUtil.readText("./sample/sample_present_proposal_sentences.txt");
			String[] sent = temp.split("\n");
			for(String s: sent)
			{	s = s.trim();
				if(!s.isEmpty())
				{
					samplePropSent.add(s);
				}
			}
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		//Util.printVector(samplePropSent);
		
		
		for(String sent: samplePropSent)
		{	//Util.jout(sent+"\n");
			Proposal temp = Proposal.extractProposalFromSentence(sent, "pos");
			
			if(temp!=null)
			{	
				Util.jout(temp.toString());
			}
			else 
			{
				Util.jout("MISS: "+sent+"\n");
			}
			
		}
		*/
		
		
		Util.jout("###############REGEX####################\n");
		int regexPropPositive = 0;
		int regexNonPropPositive = 0;
		int posPropPositive = 0;
		int posNonPropPositive = 0;
		
		int numPropPapers = 43;
		int numNonPropPapers = 57;
		try{
		regexPropPositive = Proposal.batchExtract("../sample/algo_proposing_textfiles", "regex");
		Util.jout("--------@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@--------\n--------@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@--------\n");
		regexNonPropPositive = Proposal.batchExtract("../sample/non_algo_proposing_textfiles", "regex");
		Util.jout("#######################POS#########################\n");
		posPropPositive = Proposal.batchExtract("../sample/algo_proposing_textfiles", "pos");
		Util.jout("--------@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@--------\n--------@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@--------\n");
		posNonPropPositive = Proposal.batchExtract("../sample/non_algo_proposing_textfiles", "pos");
		
		double regexPrec = ((double)(regexPropPositive)/(double)(regexPropPositive+regexNonPropPositive))*100.00;
		double regexRecall = ((double)(regexPropPositive)/(double)(numPropPapers))*100.00;
		
		double posPrec = ((double)(posPropPositive)/(double)(posPropPositive+posNonPropPositive))*100.00;
		double posRecall = ((double)(posPropPositive)/(double)(numPropPapers))*100.00;
		
		Util.jout("REGEX precision = "+regexPrec+"| recall = "+regexRecall+"\n");
		Util.jout("POS precision = "+posPrec+"| recall = "+posRecall+"\n");
		/*
		String text = "A three-step algorithm is presented for discrete gate sizing problem of delay/area optimization under double-sided timing constraints.";
		Proposal temp = Proposal.extractProposalFromSentence(text, "regex");
		
		if(temp!=null)
		{	
			Util.jout(temp.toString());
		}
		else 
		{
			Util.jout("MISS: "+text+"\n");
		}
		*/
		}catch(Exception e){}
		
	}

	public void setSection(DocumentNode.StdSection section) {
		this.section = section;
	}

	public DocumentNode.StdSection getSection() {
		return section;
	}

	public void setTense(String tense) {
		this.tense = tense;
	}

	public String getTense() {
		return tense;
	}

	public void setType(ProposalType type) {
		this.type = type;
	}

	public ProposalType getType() {
		return type;
	}

	public void setPropSentence(String propSentence) {
		this.propSentence = propSentence;
	}

	public String getPropSentence() {
		return propSentence;
	}
}
