package PdfSegmentation;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Range;


import MachineLearning.PseudocodeFeatureExtractor;
import Model.DocumentElement;
import Model.TextBox;
import Model.TextLine;
import Util.Directory;
import Util.FrequencyCounter;
import Util.Measurement;
import Util.Util;
import Util.ValuePair;

/**
 * Implements different algorithms for cutting text into boxes
 * @author aum
 *
 */

public class BoxCutter {
	
	/**
	 * Using 1-pass algorithm to find similarity between consecutive lines before merging.
	 * Currently using text mode as similarity 
	 * @param textLines
	 * @return
	 */
	public static Vector<TextBox> getTextBoxesUsingLineMerging(Vector<TextLine> textLines)
	{	Vector<TextBox> mergedTextBoxes = new Vector<TextBox>();
		//merge textBoxes
		//use modeFontsize to merge lines together
		TextBox curTb = null;
		for(TextLine tl: textLines)
		{	
			if(tl.text == null || tl.text.isEmpty()) continue;
	
			if(curTb == null)
			{
				curTb = new TextBox();
				curTb.addTextLine(tl);
				//curTb.text += "[W]";
			}
			else
			{	//Criteria to merge two text box
				//if(curTb.firstFontSize == tb.firstFontSize) //merge
				if(curTb.getModeFontSize() == tl.fs_mode) //merge
				{	//Util.jout("Do this\n");
					curTb.addTextLine(tl);
					//curTb.text += tb.text;
				}
				else	//save curTb 
				{
					mergedTextBoxes.add(curTb);
					curTb = new TextBox();
					curTb.addTextLine(tl);
				}
			}
		}
	
		//save the last curTB
		if(curTb != null)
		{
			mergedTextBoxes.add(curTb);
		}
	
		//trim text in each text box
		for(TextBox tb: mergedTextBoxes)
		{
			if(tb.text != null) tb.text = tb.text.trim();
		}

		return mergedTextBoxes;
	}
	
	/**
	 * Identify sparse lines, group them into boxes
	 * @param textLines
	 * @return
	 */
	public static Vector<TextBox> getSparseTextBoxes(Vector<TextLine> textLines)
	{
		//metadata of the document
		double NUM_CHAR_RATIO_THRESH = 0.9; //if num(char)/avgNumCharPerLine < NUM_CHAR_RATIO_THRESH --> sparse line
		int TEXT_LINE_MIN_WINDOW = 5;
		int SPARSE_LINE_MIN_WINDOW = 4;
		//int TEXT_LINE_MAX_SPIKE = 3;
		//int SPARSE_LINE_MAX_SPIKE = 2;
		double avgNumCharPerLine = 0;
		
		//get document metadata
		long numChars = 0;
		for(TextLine tl: textLines)
		{	if(tl.text == null) tl.text = "";
			//String text = tl.text.replaceAll("\\s", "");
			String text = tl.text.replaceAll("\\s", "");
			numChars += text.length();
		}
		avgNumCharPerLine = (double)numChars/(double)textLines.size();
		
		//creating a tag vector
		int[] tagVector = new int[textLines.size()];
		
	//pre-confirm lines to be excluded such as captions
		for(int i = 0; i < textLines.size(); i++)
		{	TextLine tl = textLines.elementAt(i);
			String text = tl.text;
			if(DocumentElement.isCaption(text)) 
			{
				tagVector[i] = -2;
			}
		}
		
	//1st pass tagging	-1 normal line, 1 sparse line
		for(int i = 0; i < textLines.size(); i++)
		{	TextLine tl = textLines.elementAt(i);
			//String text = tl.text.replaceAll("\\s", "");
			String text = tl.text.replaceAll("[^A-Za-z]*", "");
			int numCharsInLine = text.length();
			
			if(Math.abs(tagVector[i]) >= 1) continue;
			
			if((double)numCharsInLine/(double)avgNumCharPerLine < NUM_CHAR_RATIO_THRESH)
			{
				tagVector[i] = 1;
			}
			else tagVector[i] = -1;
		}
		
	//identify confirmed regions -2 confirmed normal line, 2 confirmed sparse line
		for(int i = 0; i < tagVector.length; i++)
		{	if(tagVector[i] == 1)	//sparse line
			{	
				int until = 0;
				for(int j = i; j < tagVector.length; j++)
				{	
					if(tagVector[j] != 1)
					{
						break;
					}
					until = j;
				}
				
				if(until - i + 1 >= SPARSE_LINE_MIN_WINDOW)
				{
					for(int j = i; j <= until; j++)
					{
						tagVector[j] = 2;
					}
					i = until;
				}
			}
			else if(tagVector[i] == -1) //normal line
			{
				int until = 0;
				for(int j = i; j < tagVector.length; j++)
				{	
					if(tagVector[j] != -1)
					{
						break;
					}
					until = j;
				}
				
				if(until - i + 1 >= TEXT_LINE_MIN_WINDOW)
				{
					for(int j = i; j <= until; j++)
					{
						tagVector[j] = -2;
					}
					i = until;
				}
			}
		}
		
	//splash up the spikes -- basically just cleaning up the 1/-1 lines
		//now only implement 2 basic rules
		//1. if peak/pit is bounded by 2 same confirmed lines, convert the peak/pit to the same type
		//2. otherwise sparse region expands
		
		int beginType = 0;
		int endType = 0;
		for(int i = 0; i < tagVector.length; i++)
		{	
			int until = i;
			
			
			if(Math.abs(tagVector[i]) >= 2)
			{
				beginType = tagVector[i];
			}
			else if(Math.abs(tagVector[i]) == 1)
			{
				//find peak/pit end bound
				int peakpitType = tagVector[i];
				for(int j = i; j < tagVector.length; j++)
				{
					if(tagVector[j] != peakpitType)
					{
						until = j - 1;
						break;
					}
				}
				
				if(until+1 < tagVector.length)
				{
					endType = tagVector[until+1];
				}
				
				if(beginType == endType)
				{
					for(int j = i; j <= until; j++)
					{
						tagVector[j] = beginType;
					}
					
					i = until;
				}
			}
		}
		
		//expand sparse region
		for(int i = 0; i < tagVector.length; i++)
		{	int until = i;
			if(tagVector[i] == 2)
			{
				//go up
				for(int j = i; j >= 0; j --)
				{
					if(tagVector[j] == 2 || Math.abs(tagVector[j]) == 1)
					{
						tagVector[j] = 2;
					}
					else break;
				}
				
				//go down
				for(int j = i; j < tagVector.length; j ++)
				{
					if(tagVector[j] == 2 || Math.abs(tagVector[j]) == 1)
					{
						tagVector[j] = 2;
					}
					else 
					{	until = j-1;
						break;
					}
				}
			}
			
			i = until;
		}
		
	//Post process: detect algorithm label as a pseudo-code boundary (tag = 3)
		for(int i = 0; i < tagVector.length; i++)
		{
			if(tagVector[i] == 2)
			{
				if(DocumentElement.isAlgorithmLabel(textLines.elementAt(i).text))
				{
					tagVector[i] = 3;
				}
			}
		}
			
		//print result for debugging
		/*for(int i = 0; i < tagVector.length; i++)
		{	TextLine tl = textLines.elementAt(i);
			String text = "[";
			if(tagVector[i] == 2) text+="*";
			text += " "+tagVector[i]+":"+tl.lineNumber+"]"+tl.text+"\n";
			Util.jout(text);
		}*/
		
	//package and return sparse boxes
		Vector<TextBox> sparseBoxes = new Vector<TextBox>();
		for(int i = 0; i < tagVector.length; i++)
		{
			int start = i;
			int end = i;
			boolean found = false;
			if(tagVector[i] >= 2)
			{	found = true;
				start = i;
				for(int j = start+1; j < tagVector.length; j++)
				{
					if(tagVector[j] != 2)
					{
						end = j-1;
						break;
					}
					else if(j == tagVector.length - 1)	//if this is the last line
					{
						end = j;
						break;
					}
				}
			}
			
			if(found)
			{
				TextBox sparseBox = new TextBox();
				for(int j = start; j <= end; j++)
				{
					sparseBox.addTextLine(textLines.elementAt(j));
				}
				
				sparseBoxes.add(sparseBox);
				i = end;
			}
			
		}
	
		return sparseBoxes;
	}
	
	/**
	 * This function evaluate how good the sparsebox extraction is. 
	 * Spurious boxes are OK as long as they contain pseudocode boxes
	 * 
	 * 2 Evaluations: 
	 * 	1. line-wise precision/recall, 
	 * 	2. boundary accuracy
	 * @param inDir
	 */
	public static void evaluateSparseBoxExtractionOnPseudocodes(String inDir, String reportDir)
	{
		Vector<String> pdfFiles = Directory.listAllFiles(inDir, ".pdf", 1);
		String report = "";
		
		//Parameters for line-wise precision/recall
		int globalLineTP = 0;
		int globalLineTN = 0;
		int globalLineFP = 0;
		int globalLineFN = 0;
		double avgPrecision = 0.0;
		double avgRecall = 0.0;
		
		//Parameters for pseudocode-wise deltas
		double MIN_PC_PICKUP_THRESH = 0.6; //if at least this % of pseudo-code lines are picked up, then this pseudocode is treated as being detected
		FrequencyCounter<Integer> upperDeltas = new FrequencyCounter<Integer>();
		FrequencyCounter<Integer> lowerDeltas = new FrequencyCounter<Integer>();
		int numGlobalVisiblePCs = 0;
		int numGlobalAllPCs = 0;
		int numGlobalBoxes = 0;
		
		
		for(String pdfFile: pdfFiles)
		{	int lineTP = 0;
			int lineTN = 0;
			int lineFP = 0;
			int lineFN = 0;
			
			int numLocalVisiblePCs = 0;
			
			String id = Directory.getFileID(pdfFile);
			Util.jout("@@@ Processing: "+id+"\n");
			report += "@@@ Processing: "+id+"------------------------------------\n";
			String taggedFile = inDir+"/"+id+".tagged.txt";
			Vector<Range<Integer>> goalStandards = PseudocodeFeatureExtractor.getPseudocodeLineBlock(taggedFile, "");
			
			Vector<TextBox> sparseBoxes = BoxCutter.getSparseTextBoxes(PdfExtractor.extractTextLinesFromPDF(pdfFile));
			Vector<Range<Integer>> results = new Vector<Range<Integer>>();
			for(TextBox sb: sparseBoxes)
			{	Range<Integer> r = Range.between(new Integer(sb.getStartLineNumber()), new Integer(sb.getEndLineNumber()));
				results.add(r);
			}
			numGlobalBoxes += results.size();
			
			//print out
			report += "GOLD:\n";
			report += Util.vectorToString(goalStandards);
			report +="RESULT:\n";
			report += Util.vectorToString(results);
			
			//get textlines
			Vector<TextLine> textLines = PdfExtractor.extractTextLinesFromPDF(pdfFile);
			
		//LINE-WISE PRECISION/RECALL	
			for(TextLine textLine: textLines)
			{
				Integer lineNum = textLine.lineNumber;
				if(lineNum <= 0) continue;
				
				boolean isTruePseudocodeLine = false;
				boolean isDetectedSparseLine = false;
				for(Range<Integer>i : goalStandards)
				{	if(i.contains(lineNum))
					{
						isTruePseudocodeLine = true;
						break;
					}
				}
				
				for(Range<Integer>i : results)
				{	if(i.contains(lineNum))
					{
						isDetectedSparseLine = true;
						break;
					}
				}
				
				if(isTruePseudocodeLine && isDetectedSparseLine) lineTP++;
				else if (!isTruePseudocodeLine && !isDetectedSparseLine) lineTN++;
				else if (isTruePseudocodeLine && !isDetectedSparseLine) lineFN++;
				else if (!isTruePseudocodeLine && isDetectedSparseLine) lineFP++;
			}
			
			Measurement m = new Measurement(lineTP, lineTN, lineFP, lineFN);
			report += m.toString();
			
			//accumulating globally
			globalLineTP += lineTP;
			globalLineTN += lineTN;
			globalLineFP += lineFP;
			globalLineFN += lineFN;
			
			avgPrecision += m.getPrecision();
			avgRecall += m.getRecall();
			
			
		//Compute Begin and End deltas
			HashMap<Range<Integer>, Range<Integer>> matchedVisiblePCs = new HashMap<Range<Integer>, Range<Integer>>();
			//store pc->box if ps region covered by the box is >= MIN_PC_PICKUP_THRESH
			report += "Visible PCs:\n";
			Vector<Range<Integer>> visiblePCsInThisFile = new Vector<Range<Integer>>();
			Vector<Range<Integer>> invisiblePCsInThisFile = new Vector<Range<Integer>>();
			for(Range<Integer> pc: goalStandards)
			{	int pcNumLines = pc.getMaximum() - pc.getMinimum() + 1;
				numGlobalAllPCs++;	//Statistics
				
				
				for(Range<Integer> box: results)
				{	int overlapRegion = 0;
					try{
						overlapRegion = box.intersectionWith(pc).getMaximum() -  box.intersectionWith(pc).getMinimum() +1;
					}catch(IllegalArgumentException noOverlapE)
					{
						//no overlap
						overlapRegion = 0;
					}
					
					if((double)overlapRegion/(double)pcNumLines >= MIN_PC_PICKUP_THRESH)
					{
						numGlobalVisiblePCs++; //Statistics
						matchedVisiblePCs.put(pc, box); 
						report += pc.toString()+"\n";
						numLocalVisiblePCs ++;
						break;
					}
				}
			}
			
			report += "Local Invisible Pseudocodes: \n";
			
			
			report += "Local Visible Pseudocodes: "+numLocalVisiblePCs+"/"+goalStandards.size()+"\n";
			
			//counting deltas
			for(Range<Integer> visiblePC: matchedVisiblePCs.keySet())
			{
				Range<Integer> box = matchedVisiblePCs.get(visiblePC);
				upperDeltas.add(visiblePC.getMinimum()-box.getMinimum());
				lowerDeltas.add(visiblePC.getMaximum()-box.getMaximum());
			}
			
		}
		
		avgPrecision = avgPrecision/(double)pdfFiles.size();
		avgRecall  = avgRecall/(double)pdfFiles.size();
		//writing out report
		report += "=============== Summary =================\n";
		
		Measurement globalM = new Measurement(globalLineTP, globalLineTN, globalLineFP, globalLineFN);
		report += pdfFiles.size() +" Documents "+globalM.getNumInstances()+" Lines\n";
		report += globalM.toString();
		
		report += "===============Document-wise Average =====================\n";
		report += "AvgPrecision:"+avgPrecision+" AvgRecall:"+avgRecall+"\n";
		
		report += "=============== Accuracy Measurement ==================\n";
		report += "NumBoxes:"+numGlobalBoxes+"\n";
		report += "Visible = "+numGlobalVisiblePCs+"/"+numGlobalAllPCs+"\n";
		
		String reportUpperCSV = "Upper Delta, Freq\n";
		for(ValuePair<Integer> delta: upperDeltas.getSortedRawValues())
		{
			reportUpperCSV += delta.getValue()+","+delta.getFreq()+"\n";
		}
		
		String reportLowerCSV = "Lower Delta, Freq\n";
		for(ValuePair<Integer> delta: lowerDeltas.getSortedRawValues())
		{
			reportLowerCSV += delta.getValue()+","+delta.getFreq()+"\n";
		}
		
		try {
			FileUtils.writeStringToFile(new File(reportDir+"/report.txt"), report);
			FileUtils.writeStringToFile(new File(reportDir+"/upper_deltas.csv"), reportUpperCSV);
			FileUtils.writeStringToFile(new File(reportDir+"/lower_deltas.csv"), reportLowerCSV);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
	}
	
	
	
	
	public static void main(String[] args)
	{
		//Util.printVector(getSparseTextBoxes(PdfExtractor.extractTextLinesFromPDF("./sample/p2.pdf")));
		
		evaluateSparseBoxExtractionOnPseudocodes("./sample/00_experiment_data/pseudocode_and_sbs", "./results/sparsebox_cutting");
		//evaluateSparseBoxExtractionOnPseudocodes("./sample/00_experiment_data/ml_pseudocode/done");
		//Util.jout(("threshold. If not, the algorithm recursively continues to").replaceAll("[^A-Za-z]*", ""));
	}

}
