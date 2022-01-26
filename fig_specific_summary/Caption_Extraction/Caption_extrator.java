package Caption_Extraction;
import com.opencsv.CSVWriter;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import Model.Algorithm;
import Model.AlgorithmRepresentation;
import Model.DocumentElement;
import Model.DocumentNode;
import Model.HierarchicalDocumentStructure;
import Model.PdfDocument;
import Model.Proposal;
import Model.AlgorithmRepresentation.RepType;
import Model.DocumentElement.ElType;
import PdfSegmentation.DocumentSegmentator;
import PdfSegmentation.PdfExtractor;
import PdfSegmentation.TextExtractor;
import Util.ConfigReader;
import Util.Directory;
import Util.DocumentUtil;
import Util.SentenceProducer;
import Util.Util;


public class Caption_extrator {
	public static ConfigReader keyword = new ConfigReader("./code/keywords.txt");
	public static ConfigReader config = new ConfigReader("./configs/config.txt");
	//extract and merge algorithms
	public static Vector<Algorithm> extract(PdfDocument pdfDoc)
	{	
		Util.log("@@@ Extracting: "+Directory.getFileID(pdfDoc.filename)+"\n");
		//pdf file is for pseudocode extraction only
		//PdfDocument pdfDoc = PdfExtractor.getPdfDocument(pdfFilename);
		
		Vector<Algorithm> result = new Vector<Algorithm>();
		
		
		
		//HierarchicalDocumentStructure hd = DocumentSegmentator.parseDocument(text, Directory.getFileID(pdfFilename));
		//try regex
		//extracting textual algorithms
		Vector<AlgorithmRepresentation> textAlgos = extractTextDescriptions(pdfDoc);
		
		//extracting step algorithm
		Vector<AlgorithmRepresentation> stepAlgos = extractStepDescriptions(pdfDoc);
		//Util.log(stepAlgos.toString());
		//extracting pseudo-code
		
		Vector<AlgorithmRepresentation> pseudo_codes = extractPseudoCodes(pdfDoc);
		//Util.log(pseudo_codes.toString());
		 
		//merging algorithms
		
		//right now only merge same algorithms in sections
		Vector<DocumentNode> nodes = pdfDoc.hd.getAllSections();
		for(DocumentNode node: nodes)
		{	
			//ignore stadard sections
			if(!node.getStdLabels().isEmpty()) continue;
			
			Algorithm algo = new Algorithm();
			for(AlgorithmRepresentation ar: textAlgos)
			{	
				if(ar.getSectSection().getNodeID() == node.getNodeID())
				{
					algo.getReps().add(ar);
				}
			}
			
			for(AlgorithmRepresentation ar: stepAlgos)
			{
				if(ar.getStepSection().getNodeID() == node.getNodeID())
				{
					algo.getReps().add(ar);
				}
			}
			
			for(AlgorithmRepresentation ar: pseudo_codes)
			{
				if(ar.getPseudo().getBestRefSect().getNodeID() == node.getNodeID())
				{
					algo.getReps().add(ar);
				}
			}
			
			if(!algo.getReps().isEmpty())
			{	
				result.add(algo);
			}
		}
		
		return result; 
	}
	
	public static Vector<AlgorithmRepresentation> extractPseudoCodes(PdfDocument pdfDoc)
	{
		//String text = pdfDoc.reformText;
		String textFilenameWithPageNumbers = pdfDoc.reformText; 
		Vector<AlgorithmRepresentation> results = new Vector<AlgorithmRepresentation>();
		Vector<DocumentElement> docEls = new Vector<DocumentElement>();
		
		Vector<String> pages = new Vector<String>();	//content of each page
		//read text into pages
		String[] lines = textFilenameWithPageNumbers.split("\n");
		String pageContent = "";
		int curPage = 0;
		pages.add(null);	//page 0
		for(String line: lines)
		{	line = line.trim();
			if(line.isEmpty()) continue;
			
			if(line.startsWith("[::PAGE::]"))
			{	
				System.out.println(":PAGE:!!!!");
				curPage = Integer.parseInt(line.replace("[::PAGE::]", ""));
				//save prev page
				if(curPage > 0)
				{	pages.add(null);//prepare an empty bucket
					pages.add(curPage, DocumentSegmentator.reformat(pageContent));	//reformat so that caption in two lines are int he same line
					pageContent = "";
				}
			}
			else
			{
				pageContent += line+"\n";
			}
		}
		
		//detect document element
		for(int pageNum = 1; pageNum <= curPage; pageNum++)
		{
			for(String line: pages.elementAt(pageNum).split("\n"))
			{
				DocumentElement docEl = DocumentElement.getDocElFromSentence(line, pdfDoc.hd);
				if(docEl != null)
				{
					docEl.setPageNum(pageNum);
					docEls.add(docEl);
				}
			}
		}
		
		//add into representation
		for(DocumentElement de: docEls)
		{	//filter only pseudo-code
			
			//testing
			//Util.jout(de.display());
			//done testing
			
			///Modification
			//if(isPseudoCodeCaption(de.caption) || de.getElType() == DocumentElement.ElType.ALGO)
			//if(isPseudoCodeCaption(de.caption) && de.getElType() == DocumentElement.ElType.FIGURE)
			if( de.getElType() == DocumentElement.ElType.FIGURE)
			{
				System.out.println("FIGURE!!!");
				AlgorithmRepresentation ar = new AlgorithmRepresentation();
				ar.repType = AlgorithmRepresentation.RepType.PSEUDO;
				ar.setPseudo(de);
				results.add(ar);
			}
		}
		
		return results;
	}
	
	public static Vector<AlgorithmRepresentation> extractStepDescriptions(PdfDocument pdfDoc)
	{
		Vector<AlgorithmRepresentation> result = new Vector<AlgorithmRepresentation>();
		Vector<DocumentNode> nodes = pdfDoc.hd.getRoot().getOrderedNodes();
		
		AlgorithmRepresentation bucket = null;
		
		for(DocumentNode node: nodes)
		{	if(node.getText() == null) continue;
			String[] lines = node.getText().split("\n");
			
			int stage = 0;
			//0: not detecting anything
			//1: detecting indication sentence
			//2: reading bullet point steps
			//3: reading numeric steps
			//0->1->(2 or 3)
			//String tag = "";
			//String tempText = fullText;
			int stepCount = 0;
			for(String line: lines)
			{	line = line.trim();
				if(line.isEmpty()) continue;
				//this line may contain multiple sentences, split them up
				Vector<String> sentences = SentenceProducer.getSentnecesFromText(line);
				String lastSent = "";
				if(sentences.isEmpty()) lastSent = line;
				else lastSent = sentences.lastElement();
				
				String sentLower = lastSent.toLowerCase();
				
				String algoStepPattern = "Algorithm(\\s)[A-Z][a-zA-Z0-1\\.]*(\\.|:)(.*)";
				if(lastSent.matches(algoStepPattern)
						||sentLower.endsWith("algorithm:")||
						(	(sentLower.contains("algorithm ") || sentLower.contains("method "))
							&&
							(		sentLower.contains("follows:")
									|| sentLower.contains("following:")
									|| sentLower.contains("follows.")
									|| sentLower.contains("steps:")
									|| sentLower.contains("below:")
									|| sentLower.contains("consists of:")
							)
						)
					)
				{	
					//save old bucket and reset
					if(stage > 0)
					{
						result.add(bucket);
						stepCount = 0;
					}
					
					bucket = new AlgorithmRepresentation();
					if(sentences == null || sentences.size() == 0)
					{
						bucket.setStepIndicationSentnece(line);
					}
					else
					{
						bucket.setStepIndicationSentnece(sentences.lastElement());
					}
					
					stage = 1;
					
					bucket.repType = AlgorithmRepresentation.RepType.STEP;
					bucket.setStepSection(node);
					
				}
				else if(stage == 1)
				{
					
					if(line.startsWith("\u2022")|| line.startsWith("*"))
					{
						stage = 2;
						
					}
					else if(line.matches("(Step\\s*)?(\\()?"+(stepCount+1)+"(\\.|:|\\))?\\s+.*"))
					{	
						stage = 3;
						
					}
				}
				
				if(stage == 2) //detecting steps
				{
					//Pattern bulletP = Pattern.compile("");
					if(line.startsWith("\u2022") || line.startsWith("*"))
					{
						bucket.getSteps().add(line);
					}
				}
				else if(stage == 3)
				{
					if(line.matches("(Step\\s*)?(\\()?"+(stepCount+1)+"(\\.|:|\\))?\\s+.*"))
					{	bucket.getSteps().add(line);
						stepCount++;
					}
				}
			}
			
			if(stage > 0)
			{
				result.add(bucket);
			}
		}
		return result;
	}
	
	/**
	 * Extract sections that talk about algorithm
	 * Check two things: 
	 * 		1. Algorithm header
	 * 		2. Explain/proposal sentence
	 * @param fullText
	 * @param hd
	 * @return
	 */
	public static Vector<AlgorithmRepresentation> extractTextDescriptions(PdfDocument pdfDoc)
	{	
		Vector<AlgorithmRepresentation> result = new Vector<AlgorithmRepresentation>();
		Vector<DocumentNode> nodes  = pdfDoc.hd.getRoot().getNodeList();
		for(DocumentNode node: nodes)
		{	String sectLower = node.getRawSectName().toLowerCase();
			boolean algoSec = false;
			String indicationSent = null;
			
			
			if(sectLower.endsWith("algorithm")
				//| sectLower.endsWith("algorithms")
				| sectLower.endsWith("method")
				//| sectLower.endsWith("methods")
				| sectLower.endsWith("approach")
				//| sectLower.endsWith("approaches")
			)
			{
				algoSec = true;
			}
			
			for(String sent: node.getSentences())
			{
				/*Proposal prop = Proposal.extractProposalFromSentence(sent, "regex");
				
				if(prop != null && prop.getTense().equals("present"))
				{
					indicationSent = sent;
					break;
				}
				*/
				if(isExplainSentence(sent))
				{
					algoSec = true;
					indicationSent = sent;
					break;
				}
			}
			
			if(algoSec)
			{
				AlgorithmRepresentation a = new AlgorithmRepresentation();
				a.repType = AlgorithmRepresentation.RepType.TEXT;
				a.setSectIndicationSentence(indicationSent);
				a.setSectSection(node);
				result.add(a);
			}
		}
		
		return result;
	}
	
	public static boolean isPseudoCodeCaption(String caption)
	{	
		//first check if it is a caption
		if(DocumentElement.isCaption(caption)) return true;
		
		//a pseudo code caption if it contains at least on algo word, and does not follow a pro
		String[] prepWords = keyword.getValue("PREPOSITION").split("\\|");
		String[] algoKeywords = {"algorithm", "pseudo-code", "pseudocode", "procedure", "algo"};
		caption = caption.toLowerCase();
		
		String[] blackListWords = {"results"};
		
		String[] tokens = caption.split("\\s");
		
		for(String bWord: blackListWords)
		{
			for(String token: tokens)
			{
				if(token.toLowerCase().matches(bWord))
				{
					return false;
				}
			}
		}
		
		
		Vector<String> algoWordsFound = new Vector<String>(); 
		for(String algoKeyword: algoKeywords)
		{
			for(String token: tokens)
			{	if(token.toLowerCase().matches(algoKeyword))
				{
					algoWordsFound.add(token);
				}
			}
		}
		
		//not a pseudo code caption if there is no algorithm keyword
		if(algoWordsFound.isEmpty()) return false;
		
		//check whether it has a preposition in front of all the algorithm words found
		
		boolean foundPrep = false;
		for(String token: tokens)
		{	
			if(!foundPrep)
			{	for(String prepWord: prepWords)
				{	if(token.matches(prepWord))
					{
						foundPrep = true;
					}
				}
			}
			else
			{
				algoWordsFound.remove(token);
			}	
		}
		
		if(!algoWordsFound.isEmpty()) return true;
		
		return false;
	}
	
	/*
	 * Check whether the given text is a stepwise sentence. e.g.
	 * 
	 * \u2022 P 6< H: P cannot be a proper subtype of H since inheritance cannot be cyclic.
	 * 1. Build a repository of RT Infoinstances: T...
	 * Step 1. Set C to 2 or 3 (depending on the illumination variance of the mouth image).
	 */
	public static boolean isStepwiseSentence(String text)
	{	if(text == null) return false;
		if(text.startsWith("\u2022")) return true;
		if(text.matches("[1-9][0-9]*(\\.)?(\\s)(.*)")) return true;
		if(text.startsWith("Step")) return true;
		
		return false;
	}
	
	/**
	 * Check whether sentence is an 'explain' sentence
	 * @param sentence
	 * @return
	 */
	public static boolean isExplainSentence(String sentence)
	{	
		String[] activeExplainVerbs = keyword.getValue("EXPLAIN_present").split("\\|");
		String[] passiveExplainVerbs = keyword.getValue("EXPLAIN_past").split("\\|");
		String[] algoKeywords = {"algorithm", "method", "procedure", "approach", "technique", 
				"algorithms", "methods", "procedures", "approachs", "techniques"};
		
		String activePattern = "(.*)(We|we|In this section,|This section)(\\s)(.*)\\s(<VERB>)\\s(.*?)(<OBJECT>)([^A-Za-z].*\\.|\\.)(.*)";
		String passivePattern = "(.*)(\\s)(<OBJECT>)(\\s)(.*is.*|.*are.*)\\s(<VERB>)(.*)(in this section|here)([^A-Za-z].*\\.|\\.)(.*)";
		
		
		for(String v: activeExplainVerbs)
		{	for(String object: algoKeywords)
			{
				String pattern = activePattern.replace("<VERB>", v).replace("<OBJECT>", object);
				//Util.jout(pattern+"\n");
				if(sentence.matches(pattern)) return true;
			}
		}
		
		for(String v: passiveExplainVerbs)
		{	for(String object: algoKeywords)
			{
				String pattern = passivePattern.replace("<VERB>", v).replace("<OBJECT>", object);
				if(sentence.matches(pattern)) return true;
			}
		}
		return false;
	}
	
	public static Vector<String> getFunctionsFromString(String text)
	{
		Vector<String> functions = new Vector<String>();
		text = text.replace('\n', ' ');
		String functionPatternStr = "([a-zA-Z_0-9\\.]+\\s*\\(.*?\\))";
		Pattern functionPattern = Pattern.compile(functionPatternStr);
		Matcher m = functionPattern.matcher(text);
		while(m.find())
		{
			functions.add(m.group(1));
		}
		return functions;
	}
	
	public static void main(String[] args) throws IOException, Exception
	{	
		System.out.println("text2");
		//Vector<String> pdfFiles = Directory.listAllFiles("Textfiles_resultsec/", ".pdf", 1);
		Vector<String> pdfFiles = Directory.listAllFiles("pdfs_765/", ".pdf", 1);
		//extractPseudoCodes(pdfDoc);
		int i = 0;
		String fnew="";
		boolean flag = false;
		////*File fout = new File("figureCaptions_refSentences_ACL.txt");
		////*FileOutputStream fos = new FileOutputStream(fout);
		//BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
		BufferedWriter bw = new BufferedWriter(new FileWriter("figureCaptions_refSentences_ACL.txt"));
		
		//Writer writer = null;
		try{
			//writer = new BufferedWriter(
					//new OutputStreamWriter(new FileOutputStream("Synonpsis.csv"), "UTF-8"));
			@SuppressWarnings("resource")
			int j=0;
			CSVWriter writer = new CSVWriter(new FileWriter(("Synonpsis_ACL.csv")));
			//CSVUtils.writeLine(writer, Arrays.asList("File Name", "Caption","Reference Text"));
			List<String[]> data = new ArrayList<String[]>();
			data.add(new String[] {"file name","Section", "caption text"," reference text"});
			
			for(String f: pdfFiles)
			{
				flag = false;
				//writer = new BufferedWriter(
						//new OutputStreamWriter(new FileOutputStream("Output/APRuleBasedLineNo.csv"), "UTF-8"));	
				Util.jout("Going to extract: " + f + "\n");
				//bw.write(f+"\n");
				//fnew = f.substring(f.indexOf("Textfiles_resultsec/")+21,f.indexOf(".pdf")).trim();
				fnew = f.substring(f.indexOf("pdfs_765/")+10,f.indexOf(".pdf")).trim();
				Util.jout("File names: "+ fnew+"/n");
				//f.Split("/")
				PdfDocument pdfDoc = PdfExtractor.getPdfDocument(f);
				Vector<AlgorithmRepresentation> ps = extractPseudoCodes(pdfDoc);
				
				//System.out.println("text");
				
				for(AlgorithmRepresentation stepAlgo: ps)
				{
					flag = true;
					Util.jout(stepAlgo.toString());
					bw.write(f+"/n");
					bw.write(stepAlgo.toString());
					System.out.println("STEP!"+stepAlgo.toString());
					//bw.newLine();
					bw.write("count:"+j);
					j++;
					
					System.out.println("SECT text "+stepAlgo.getSecText());
					
					System.out.println("reference text"+stepAlgo.getRefTextSentence());
					
					System.out.println("caption text"+stepAlgo.getCaptionText());
					data.add(new String[] {fnew,stepAlgo.getSecText(), stepAlgo.getCaptionText(),stepAlgo.getRefTextSentence()});
					
					//CSVUtils.writeLine(writer, Arrays.asList(f, stepAlgo.getCaptionText(),stepAlgo.getRefTextSentence()));
					
					System.out.println("after CSV util");
					
				}
				
				if(flag == true){
					i = i + 1;

				}
				
			}
			System.out.println("HERE!");
			writer.writeAll(data);
			bw.close();
			writer.close();

		}

		catch (Exception e) {
			e.printStackTrace();
		}


	}




	//writer.close();
	//System.out.println("Number of files with algos = " + i);


	/*
		String text = "In this section we show that evaluation of the interval power function for arbitrary bases and exponents can be reduced to the case of non-negative bases, and give the corresponding algorithm.";
		//String text = "This section outlines algorithm.";
		Util.jout(""+isExplainSentence(text));
	 */

	//Util.jout(""+isStepwiseSentence("2 Transform the above traf?c matrix into a BR to BR traf?c matrix,"));

	//Util.printVector(getFunctionsFromString("102|1[::-::]and calls to Oracle(constraints\nN\n) to evaluate splits\nS := best binary split"));
	//}

}
