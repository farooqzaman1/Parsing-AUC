package Model;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import AlgorithmCitationAnalysis.CitationContext;
import Model.DocumentNode.StdSection;
import Util.CitationUtil;
import Util.ConfigReader;


public class HierarchicalDocumentStructure {
	String docID = null;
	private String originalText = null;
	String fullText = null;
	
	public Vector<String> captions = new Vector<String>();
	private DocumentNode root = null;
	public HierarchicalDocumentStructure(String _docID, String _fullText, DocumentNode _root)
	{
		init(_docID, _fullText, _root);
	}
	
	private void init(String _docID, String _fullText, DocumentNode _root)
	{
		docID = _docID;
		fullText = _fullText;
		setRoot(_root);
	}
	
	/**
	 * whatToPrint can be 'sentences' or 'chunk'
	 * @param whatToPrint
	 * @return
	 */
	public String printDoc(String whatToPrint)
	{	String result = "";
		
		//adding captions
		result += "CAPTIONS:\n";
		for(String caption: captions)
		{
			result += caption+"\n";
		}
		
		result += getRoot().printNode(whatToPrint); 
		return result;
	}
	
	public void findStdSections()
	{
		getRoot().findStdSections();
	}
	
	//returns all the nodes in which the citation symbol 'symbol' appears in a citation sentence in its text
	public Vector<DocumentNode> getNodesByCitationSymbol(String symbol)
	{
		Vector<DocumentNode> nodeList = getRoot().getNodeList();
		Vector<DocumentNode> result = new Vector<DocumentNode>();
		String symbolPattern = CitationUtil.CITATION_SENTENCE_WITHSYMBOL_PATTERN.replace("SYMBOL", symbol);
		
		for(DocumentNode node: nodeList)
		{	
			boolean found = false;
			for(String sentence: node.getSentences())
			{
				if(sentence.matches(symbolPattern))
				{	//Util.jout("MATCH:" +sentence+"\n");
					found = true;
					break;
				}
			}
			if(found)
			{
				result.add(node);
			}
		}
		
		return result;
	}
	
	public Vector<CitationContext> getAlgorithmCitationContexts()
	{	ConfigReader config = new ConfigReader();
		int preWindowSize = Integer.parseInt(config.getValue("pre_window"));
		int posWindowSize = Integer.parseInt(config.getValue("pos_window"));
		Vector<CitationContext> contexts =  getRoot().getAlgorithmCitationContexts(docID, preWindowSize, posWindowSize,null);
		
		//getting more info: for each citation symbol 'x' in a citation context
		//also see if 'x' is cited somewhere else in the paper regardless of whether 'x'
		//appears in an algorithm citation context or not.
		
		for(CitationContext context: contexts)
		{
			for(String symbol: context.getSymbols())
			{
				Vector<DocumentNode> nodes = this.getNodesByCitationSymbol(symbol);
				HashSet<DocumentNode.StdSection> sections = new HashSet<DocumentNode.StdSection>(); 
				
				for(DocumentNode node: nodes)
				{
					sections.addAll(node.getStdLabels());
				}
				sections.remove(DocumentNode.StdSection.REF);
				if(!sections.isEmpty())
				{
					context.getRelatedSymbolSectionMap().put(symbol, sections);
				}
			}
		}
		
		return contexts;
	}
	
	public HashMap<DocumentNode.StdSection, Vector<DocumentNode> > getStdSections()
	{	
		return getRoot().stdSections;
	}
	
	public Vector<DocumentNode> getAllSections()
	{
		Vector<DocumentNode> result = getRoot().getOrderedNodes();
		//filter out root node
		for(int i = 0; i< result.size(); i++)
		{
			DocumentNode temp = result.elementAt(i);
			if(temp.isRoot())
			{
				result.remove(i);break;
			}
		}
		
		return result;
	}
	
	public Vector<String> getAllSentences()
	{
		Vector<String> result = new Vector<String>();
		Vector<DocumentNode> nodes = this.getAllSections();
		for(DocumentNode node:nodes)
		{
			result.addAll(node.getSentences());
		}
		
		return result;
	}
	
	//exclude references
	public Vector<String> getBodySentences()
	{
		Vector<String> result = new Vector<String>();
		Vector<DocumentNode> nodes = this.getAllSections();
		for(DocumentNode node:nodes)
		{	if(node.getStdLabels().contains(DocumentNode.StdSection.REF) 
				|| node.getStdLabels().contains(DocumentNode.StdSection.HDR)) continue;
			result.addAll(node.getSentences());
		}
		
		return result;
	}
	
	public void getGraph(String outputPDFFile)
	{
		getRoot().getGraph(outputPDFFile);
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public String getOriginalText() {
		return originalText;
	}

	public void setRoot(DocumentNode root) {
		this.root = root;
	}

	public DocumentNode getRoot() {
		return root;
	}
	
	/**
	 * return a list of header and sentenses.
	 * @return
	 */
	public Vector<String> flatten()
	{
		return flatten_helper(this.getRoot());
	}
	
	public static Vector<String> flatten_helper(DocumentNode curNode)
	{
		Vector<String> result = new Vector<String>();
		if(curNode.getNodeType() != DocumentNode.NodeType.ROOT)
		{
			result.add(curNode.getRawSectName());
			
			//use sentences (without captions)
			result.addAll(curNode.getSentences());
			
			//use reformatted text
			/*String text = curNode.getText();
			if(text != null)
			{
				String[] lines = text.split("\n");
				for(String line: lines)
				{	result.add(line); }
			}
			*/
			
		}
		
		for(DocumentNode child: curNode.children)
		{
			result.addAll(flatten_helper(child));
		}
		
		return result;
	}
}

