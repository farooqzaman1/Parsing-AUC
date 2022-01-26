package Model;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import AlgorithmCitationAnalysis.CitationContext;
import AlgorithmExtraction.AlgorithmExtractor;
import PdfSegmentation.DocumentSegmentator;
import Util.CitationUtil;
import Util.CommandExecutor;
import Util.Directory;
import Util.SentenceProducer;
import Util.Util;


public class DocumentNode implements Comparable<DocumentNode>{
	public static int nodeCount = 0;
	public enum NodeType {ROOT, SECT}
	public enum StdSection {HDR,CHP,ABS,INT,BCK,MTH,RAD,CON,ACK,REF, UNKNOWN}
	//common parameters
	Vector<DocumentNode> children = new Vector<DocumentNode>();
	private NodeType nodeType = null;
	int depth = 0; //for display purpose
	private int nodeID = -1;	//for printing out DOT file
	private Vector<StdSection> stdLabels = new Vector<StdSection>(); 
	public String captionText = "";
	//ROOT parameters
	HashMap<StdSection, Vector<DocumentNode> > stdSections = new HashMap<StdSection, Vector<DocumentNode>>();
	
	//SECT parameters
	private String rawSectName = null;
	String sectName = null;
	String secID = "";
	private String text = null; //full text
	private Vector<String> sentences = new Vector<String>(); //meaningful sentences
	int order = -1;	//keep track of the order of this node in the document
	
	public DocumentNode()
	{
		nodeCount++;
		setNodeID(nodeCount);
	}
	
	public boolean isRoot()
	{
		return getNodeType() == NodeType.ROOT;
	}
	
	public static String getCannonicalSectionName(String sectName)
	//something like 10.2.8###<section name>
	{	
		Pattern p = Pattern.compile(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN);
		Matcher m = p.matcher(sectName);
		String id = "";
		String name = "";
		String result = "";
		while(m.find()) {
			id = m.group(1);
			name = m.group(9);
			//Util.jout(""+m.groupCount()+"\n");
		}
		
		if(id == null || id.isEmpty())
		{
			name = sectName;
		}
		else if(id.matches("([1-9][0-9]*)|([1-9][0-9]*\\.)|([1-9][0-9]*(\\.[0-9]+)+(\\.)?)"))
		{
			String[] tokens = id.split("\\.");
			for(String token: tokens)
			{
				token = token.trim();
				if(token.isEmpty()) continue;
				result += token + " ";
			}
			result = result.trim().replace(' ', '.');
		}
		else
		//Roman number
		{
			String[] tokens = id.split("\\.");
			for(String token: tokens)
			{
				token = token.trim();
				if(token.isEmpty()) continue;
				result += CitationUtil.romanToDecimal(token)+ " ";
			}
			result = result.trim().replace(' ', '.');
		}
		
		result+="###"+name;
		return result;
	}
	
	public void putNode(DocumentNode node) //throws Exception
	{	//Util.jout("@@@ Inserting "+node.rawSectName +" under " +this.rawSectName+"\n");
		if(node.secID == null || node.secID.isEmpty())
		{
			this.children.add(node);
			return;
		}
		
		//check if node is sub section
		
		String[] thisSecIDs = this.secID.split("\\.");
		String[] nodeSecIDs = node.secID.split("\\.");
		
		int thisSecIDsLen =  thisSecIDs.length;
		if(this.secID.isEmpty()) thisSecIDsLen = 0;
		int nodeSecIDsLen =  nodeSecIDs.length;
		if(node.secID.isEmpty()) nodeSecIDsLen = 0;
		//int i = Math.min(thisSecIDs.length, nodeSecIDs.length);
		if(nodeSecIDsLen <= thisSecIDsLen)
		{
			this.children.add(node);
			return;
		}
		int nextElement=0;
		try{
		 nextElement = Integer.parseInt(nodeSecIDs[thisSecIDsLen]);
		} catch(Exception e)
		{e.printStackTrace();}
		//choose which child to pass along this node
		for(DocumentNode child: this.children)
		{	String[] childIDs = child.secID.split("\\.");
			int childIDsLen = childIDs.length;
			if(child.secID.isEmpty()) childIDsLen = 0; 
			
			if(childIDsLen < nodeSecIDsLen)
			{
				int childNextElement = -1;
				try{
					childNextElement = Integer.parseInt(childIDs[thisSecIDsLen]);
				}catch ( java.lang.NumberFormatException ne)
				{
					//child id maybe empty
				}
				
				if(childNextElement == nextElement)
				{
					child.putNode(node);
					return;
				}
			}
			
		}
		
		//all other cases, just add this node as a child
		this.children.add(node);
	}
	
	public void addNode(String sectName, String text, int sectionOrder)
	//creates a new node for this section and recursively adds it to the right position in the tree 
	{	
		//first try to get id of the section
		String canSectName = getCannonicalSectionName(sectName);
		//Util.jout(canSectName+"\n");
		String id = canSectName.split("###")[0];
		String name = canSectName.split("###")[1];
		
		DocumentNode node = new DocumentNode();
		node.setNodeType(NodeType.SECT);
		node.secID = id;
		node.sectName = name;
		node.setRawSectName(sectName);
		//node.setText(text);
		node.order = sectionOrder;
		
		//extract sentences here
		
		//SentenceProducer sp = new SentenceProducer(DocumentSegmentator.reformat(text), 2);
		
		//text has already been reformatted
		
		//separate captions from texts, while extracting sentences
		String captions = "";
		String runningText = "";
		String tempRunningChunk = null;
		
		String[] lines = text.split("\n");
		for(String line: lines)
		{	line = line.trim();
			if(line.isEmpty()) continue;
			
			
			if(DocumentElement.isCaption(line))
			{
				captions += line+"\n";
				node.getSentences().add(line);
				if(tempRunningChunk!= null && !tempRunningChunk.isEmpty())
				{
					SentenceProducer sp = new SentenceProducer(tempRunningChunk, 2);
					String sentence = null;
					while((sentence = sp.nextSentence()) != null)
					{	
						if(sentence.startsWith("null"))
						{
							sentence = sentence.substring(4);
						}
						
						node.getSentences().add(sentence);
					}
					
					tempRunningChunk = "";
				}
				
			}
			//stepwise sentence, separate them
			else if(line.endsWith(":") || AlgorithmExtractor.isStepwiseSentence(line))
			{
				//flush text and interpret the line as a sentence
				if(tempRunningChunk!= null && !tempRunningChunk.isEmpty())
				{
					SentenceProducer sp = new SentenceProducer(tempRunningChunk, 2);
					String sentence = null;
					while((sentence = sp.nextSentence()) != null)
					{	
						if(sentence.startsWith("null"))
						{
							sentence = sentence.substring(4);
						}
						node.getSentences().add(sentence);
					}
					
					tempRunningChunk = "";
				}
				
				node.getSentences().add(line);
				
				runningText += line+"\n";
			}
			else
			{	tempRunningChunk += line+"\n";
				runningText += line+"\n";
			}
		}
		
		//left over sentences
		if(tempRunningChunk!= null && !tempRunningChunk.isEmpty())
		{
			SentenceProducer sp = new SentenceProducer(tempRunningChunk, 2);
			String sentence = null;
			while((sentence = sp.nextSentence()) != null)
			{	
				if(sentence.startsWith("null"))
				{
					sentence = sentence.substring(4);
				}
				
				node.getSentences().add(sentence);
			}
			
			tempRunningChunk = "";
		}
		
		node.captionText = captions;
		node.setText(text);
		//Util.jout(captions);
		
		//extract sentences including captions
		/*lines = text.split("\n");
		for(String line: lines)
		{
			SentenceProducer sp = new SentenceProducer(line, 2);
			String sentence = null;
			while((sentence = sp.nextSentence()) != null)
			{	
				node.getSentences().add(sentence);
			}
		}
		*/
		//put the node at the right position
		this.putNode(node);
		
		
	}
	
	public static void assignDepth(DocumentNode root, int startDepth)
	{	//Util.jout("### Assigning dept " +startDepth +" to "+this.rawSectName+"\n");
		root.depth = startDepth;
		for(DocumentNode child: root.children)
		{
			assignDepth(child, startDepth + 1);
		}
	}
	
	//print subtree starting from this node.
	/**
	 * WhatToPrint can be 'sentences', 'chunk' (text before being parsed into sentences)
	 */
	private String printNodeHelper(String whatToPrint)
	{
		String result = "";
		String pre = "";
		for(int i = 0; i < this.depth; i++)
		{
			pre+= "\t";
		}
		
		String labels = "";
		for(StdSection s: this.getStdLabels())
		{
			labels += s+"|";
		}
		
		result += pre+"<"+labels+this.secID+":"+this.sectName+"|"+this.depth+">\n";
		
		if(whatToPrint.equals("sentences"))
		{
			for(String sentence: this.getSentences())
			{
				result += pre+sentence+"\n";
			}
		}
		else if(whatToPrint.equals("chunk"))
		{	if(this.getText() != null)
			{
				String[] lines = this.getText().split("\n");
				for(String line: lines)
				{
					result += pre+line+"\n";
				}
			}
		}
		
		for(DocumentNode child: this.children)
		{
			result += child.printNodeHelper(whatToPrint);
		}
		
		return result;
	}
	
	/**
	 * get list of the nodes including 'this' node
	 * @return
	 */
	public Vector<DocumentNode> getNodeList()
	{
		Vector<DocumentNode> result = new Vector<DocumentNode>();
		result.add(this);
		for(DocumentNode child: this.children)
		{
			result.addAll(child.getNodeList());
		}
		
		return result;
	}
	
	/**
	 * run getNodeList and order the results by section order
	 * @return
	 */
	public Vector<DocumentNode> getOrderedNodes()
	{
		Vector<DocumentNode> result = this.getNodeList();
		Collections.sort(result);
		return result;
	}
	
	/**
	 * For ROOT node only
	 * Traverse though all nodes
	 * For each node, see if the section is one of the standard section
	 */
	public void findStdSections()
	{
		if(this.getNodeType() != NodeType.ROOT) return;
		StdSection[] stdSections = StdSection.values();
		Vector<DocumentNode> allNodes = this.getNodeList();
		for(StdSection s: stdSections)
		{	Vector<DocumentNode> nodes = new Vector<DocumentNode>();
			if(s == StdSection.UNKNOWN) continue;
			
			for(DocumentNode node: allNodes)
			{
				if(node == this) continue; // ROOT node not considered
				if(DocumentSegmentator.isStandardSection(node.getRawSectName(), s))
				{
					nodes.add(node);
					node.getStdLabels().add(s);
				}
			}
			this.stdSections.put(s, nodes);
		}
	}
	
	//only called by rootNode
	public HashMap<StdSection, Vector<DocumentNode> > getStdSections()
	{	
		return this.stdSections;
	}
	
	public String printNode(String whatToPrint)
	//must do in-order traversal
	{	assignDepth(this, -1);
		return this.printNodeHelper(whatToPrint);
	}
	
	/**
	 * outputs Dot language representation of the tree from this node.
	 * If withConetent is true, also output the text content within eat section
	 * @param withContent
	 * @return
	 */
	public String getDotRepresentation(boolean withContent)
	{	Vector<DocumentNode> nodes = this.getOrderedNodes();
		String result = "digraph g {\n";
		//set canvas properties
		result+= "size=\"7.5,10\"; ratio = auto;\n";
		
		//print labels
		for(DocumentNode node:nodes)
		{	String label = "[";
			for(DocumentNode.StdSection stdSec: node.getStdLabels())
			{
				label += stdSec.toString()+"|";
			}
			label += "]"+node.getRawSectName();
			result += ""+node.getNodeID()+" [label=\""+label+"\"];\n";
		}
		
		for(DocumentNode node: nodes)
		{	for(DocumentNode child: node.children)
			{	result += node.getNodeID()+"->"+child.getNodeID()+";\n";
			
			}
		}
		
		result += "}";
		return result;
	}
	
	public void getGraph(String outputPDFFile)
	{
		try
		{
			//write dot to file
			String tempDOTFile = "./temp/temp_"+Directory.getFileID(outputPDFFile)+".dot";
			BufferedWriter writer = new BufferedWriter(new FileWriter(tempDOTFile));
			writer.write(this.getDotRepresentation(false));
			writer.close();
			CommandExecutor.exec("dot -Tpdf "+tempDOTFile+" -o "+outputPDFFile, false);
			//CommandExecutor.exec("rm -f "+tempDOTFile, false);
		}catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	//Compare function
	public int compareTo(DocumentNode other)
	{
		return this.order - other.order;
	}
	
	//called by ROOT node only
	public Vector<CitationContext> getAlgorithmCitationContexts(String docID, int preWindowSize, int posWindowSize,HashSet<StdSection> labels)
	{
		Vector<CitationContext> result = new Vector<CitationContext>();
		if(this.getStdLabels().contains(StdSection.REF)) return result; //do not parse ref section
		HashSet<StdSection> myLabels = null;
		
		myLabels = new HashSet<StdSection>(this.getStdLabels());
		if(labels != null) myLabels.addAll(labels);
		
		//process this 
		for(int i = 0; i < this.getSentences().size(); i++)
		{	String sentence = this.getSentences().elementAt(i);
			boolean citingAlgorithm = false;
			if(sentence.matches(CitationUtil.CITATION_SENTENCE_PATTERN))
			{	String lower = sentence.toLowerCase();
				for(String keyword: CitationUtil.ALGORITHM_KEYWORDS)
				{
					if(lower.contains(keyword))
					{
						citingAlgorithm = true;break;
					}
				}
			}
			
			if(citingAlgorithm)
			{	
				CitationContext c = new CitationContext();
				c.setPaperID(docID);
				c.setPreWindow(preWindowSize);
				c.setPosWindow(posWindowSize);
				c.setCitationSentence(sentence);
				int begin = i - preWindowSize;
				if(begin < 0) begin = 0;
				int end = i + posWindowSize;
				if(end >= this.getSentences().size()) end = this.getSentences().size() - 1;
				Vector<String> pre = new Vector<String>();
				Vector<String> pos = new Vector<String>();
				
				for(int j = begin; j < i; j++)
				{
					pre.add(this.getSentences().elementAt(j));
				}
				for(int k = i+1; k <= end; k++)
				{
					pos.add(this.getSentences().elementAt(k));
				}
				
				c.setPreSentences(pre);
				c.setPosSentences(pos);
				c.getSectionLabels().addAll(myLabels);
				
				//extract cited symbols from sentence
				Pattern p = Pattern.compile(CitationUtil.CITATION_PATTERN);
				Matcher m = p.matcher(sentence);
				
				HashSet<String> citedSymbols = new HashSet<String>();
				while(m.find())
				{	//Util.jout("Groupcount:"+m.groupCount()+"\n");
					String temp = m.group(1);
					String[] symbols = temp.trim().split(",");
					for(String symbol: symbols)
					{	symbol = symbol.trim();
						if(symbol.isEmpty()) continue;
						citedSymbols.add(symbol);
					}
				}
				c.getSymbols().addAll(citedSymbols);
				
				result.add(c);
			}
			
		}
		
		//process children's
		for(DocumentNode child: this.children)
		{
			result.addAll(child.getAlgorithmCitationContexts(docID, preWindowSize, posWindowSize, myLabels));
		}
		
		return result;
	}
	
	public Vector<String> retrieveCaptions()
	{
				
		return retrieveCaptions_helper(this);
	}
	
	public static Vector<String> retrieveCaptions_helper(DocumentNode node)
	{
		Vector<String> result = new Vector<String>();
		String[] lines = node.captionText.split("\n");
		//Util.jout("AHA"+lines.length+"\n");
		for(String line: lines)
		{	line = line.trim();
			if(line.isEmpty()) continue;
			result.add(line);
		}
		
		for(DocumentNode child: node.children)
		{
			result.addAll(retrieveCaptions_helper(child));
		}
		
		return result;
	}
	
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public void setRawSectName(String rawSectName) {
		this.rawSectName = rawSectName;
	}

	public String getRawSectName() {
		return rawSectName;
	}

	public void setStdLabels(Vector<StdSection> stdLabels) {
		this.stdLabels = stdLabels;
	}

	public Vector<StdSection> getStdLabels() {
		return stdLabels;
	}

	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	public int getNodeID() {
		return nodeID;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getText() {
		return text;
	}

	public void setSentences(Vector<String> sentences) {
		this.sentences = sentences;
	}

	public Vector<String> getSentences() {
		return sentences;
	}
	
	public static void main(String[] args)
	{
		String text = "a\nb";
		Util.jout(""+text.split("\n").length);
	}
}
