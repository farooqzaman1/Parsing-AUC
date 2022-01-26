package PdfSegmentation;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.Range;

import weka.classifiers.Classifier;

import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import MachineLearning.ClassifierFactory;
import Model.DocumentElement;
import Model.DocumentNode;
import Model.DocumentNode.StdSection;
import Model.Section;
import Model.TextLine;
import Util.CitationUtil;
import Util.Directory;
import Util.FrequencyCounter;
import Util.Util;

public class SectionHeaderClassifier {
	
	private static int absOrIntLineNumber = -1;
	private static int refLineNumber = -1;
	private static double avg_linefontsize = 1.0;
	private static double mode_linefontsize = 1.0;
	private static double mode_gap = 0.0;
	private static double avg_gap = 0.0;
	private static double avg_numwords = 0;
	
	//for usage with existing model
	Classifier c = null;
	Instances refInstancesWithID = null;
	
	static String[] FEATURES = {
			"id STRING",
		
			//RegEx Based
			"is_sec_header_with_number {0,1}",
			"is_upper_line_sec_header_with_number {0,1}",
			"is_lower_line_sec_header_with_number {0,1}",
			"is_sec_header_without_number {0,1}",
			"is_upper_line_sec_header_without_number {0,1}",
			"is_lower_line_sec_header_without_number {0,1}",
			"is_standard_section {0,1}",
			"is_caption {0,1}",
			
			//Style Based
			"mode_fontsize NUMERIC",
			"mode_fontsize_ratio_to_avg_fontsize NUMERIC",
			"mode_fontsize_ratio_to_mode_fontsize NUMERIC",
			
			"upper_gap_ratio_to_mode_gap NUMERIC",
			"upper_gap_ratio_to_avg_gap NUMERIC",
			"lower_gap_ratio_to_mode_gap NUMERIC",
			"lower_gap_ratio_to_avg_gap NUMERIC",
			
			"are_all_chars_bold {0,1}",
			
			//Structure Based
			"numwords_ratio_to_avg_numwords NUMERIC",
			"are_all_words_capitalized {0,1}",
			"is_after_abs_or_intro {0,1}",
			"is_before_ref {0,1}",
			"is_first_line_of_page {0,1}",
			"is_last_line_of_page {0,1}",
			
			"class {0,1}"
	};
	
	
	public static String getFeatureString(TextLine textLine, int currentIndex, int label, Vector<TextLine> textLines, String fileID)
	{
		StringBuilder result = new StringBuilder();
		TextLine upperTextLine = null;
		TextLine lowerTextLine = null;
		
		//if(currentIndex > 0 && textLines.get(currentIndex-1).text != null) upperTextLine = textLines.get(currentIndex-1);
		//if(currentIndex < textLines.size()-1 && textLines.get(textLines.size()-1).text != null) lowerTextLine = textLines.get(textLines.size()-1);
		
		for(int i = currentIndex - 1; i >= 0; i--)
		{
			if(textLines.elementAt(i).text != null && !textLines.elementAt(i).text.isEmpty())
			{
				upperTextLine = textLines.elementAt(i);
				break;
			}
		}
		
		for(int i = currentIndex + 1; i < textLines.size(); i++)
		{
			if(textLines.elementAt(i).text != null && !textLines.elementAt(i).text.isEmpty())
			{
				lowerTextLine = textLines.elementAt(i);
				break;
			}
		}
		
		//id
		result.append(fileID+"@"+textLine.lineNumber+",");
		
		//"is_sec_header_with_number {0,1}",
		int is_sec_header_with_number = 0;
		if(textLine.text.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)) is_sec_header_with_number = 1;
		result.append(is_sec_header_with_number+",");
		
		//"is_upper_line_sec_header_with_number {0,1}",
		int is_upper_line_sec_header_with_number = 0;
		if(upperTextLine != null && upperTextLine.text.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)) 
			is_upper_line_sec_header_with_number = 1;
		result.append(is_upper_line_sec_header_with_number+",");
		
		//"is_lower_line_sec_header_with_number {0,1}",
		int is_lower_line_sec_header_with_number = 0;
		if(lowerTextLine != null && lowerTextLine.text.matches(CitationUtil.SECTION_NAME_PATTERN_WITH_NUMBERS_PATTERN)) 
			is_lower_line_sec_header_with_number = 1;
		result.append(is_lower_line_sec_header_with_number+",");
		
		//"is_sec_header_without_number {0,1}",
		int is_sec_header_without_number = 0;
		if(textLine.text.matches(CitationUtil.SECTION_NAME_PATTERN_WITHOUT_NUMBERS_PATTERN))
			is_sec_header_without_number = 1;
		result.append(is_sec_header_without_number+",");
		
		//"is_upper_line_sec_header_without_number {0,1}",
		int is_upper_line_sec_header_without_number = 0;
		if(upperTextLine != null && upperTextLine.text.matches(CitationUtil.SECTION_NAME_PATTERN_WITHOUT_NUMBERS_PATTERN))
			is_upper_line_sec_header_without_number = 1;
		result.append(is_upper_line_sec_header_without_number+",");
		
		//"is_lower_line_sec_header_without_number {0,1}",
		int is_lower_line_sec_header_without_number = 0;
		if(lowerTextLine != null && lowerTextLine.text.matches(CitationUtil.SECTION_NAME_PATTERN_WITHOUT_NUMBERS_PATTERN))
			is_lower_line_sec_header_without_number = 1;
		result.append(is_lower_line_sec_header_without_number+",");
		
		//"is_standard_section {0,1}",
		int is_standard_section = 0;
		if(DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.ABS)
			|| DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.INT)
			|| DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.BCK)
			//|| DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.RAD)
			|| DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.CON)
			|| DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.ACK)
			|| DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.REF) )
			is_standard_section = 1;
		result.append(is_standard_section+",");
		
		//"is_caption {0,1}",
		int is_caption = 0;
		if(DocumentElement.isCaption(textLine.text)) is_caption = 1;
		result.append(is_caption+",");
		
		//Style Based
		
		//"mode_fontsize NUMERIC",
		result.append(textLine.fs_mode+",");
		
		//"mode_fontsize_ratio_to_avg_fontsize NUMERIC",
		double mode_fontsize_ratio_to_avg_fontsize = 0.0;
		mode_fontsize_ratio_to_avg_fontsize = textLine.fs_mode/avg_linefontsize;
		result.append(mode_fontsize_ratio_to_avg_fontsize+",");
			
		//"mode_fontsize_ratio_to_mode_fontsize NUMERIC",
		double mode_fontsize_ratio_to_mode_fontsize = textLine.fs_mode/mode_linefontsize;
		if(Double.isNaN(mode_fontsize_ratio_to_mode_fontsize)) mode_fontsize_ratio_to_mode_fontsize = 0;
		else if(Double.isInfinite(mode_fontsize_ratio_to_mode_fontsize)) mode_fontsize_ratio_to_mode_fontsize = 0;
		result.append( mode_fontsize_ratio_to_mode_fontsize+",");
	
		//"upper_gap_ratio_to_mode_gap NUMERIC",
		double upper_gap_ratio_to_mode_gap = 0.0;
		if(upperTextLine != null) upper_gap_ratio_to_mode_gap = Math.abs(textLine.first_char_pos_y - upperTextLine.first_char_pos_y);
		upper_gap_ratio_to_mode_gap /= mode_gap;
		result.append(upper_gap_ratio_to_mode_gap+",");
		
		//"upper_gap_ratio_to_avg_gap NUMERIC",
		double upper_gap_ratio_to_avg_gap = 0.0;
		if(upperTextLine != null) upper_gap_ratio_to_avg_gap = Math.abs(textLine.first_char_pos_y - upperTextLine.first_char_pos_y);
		upper_gap_ratio_to_avg_gap /= avg_gap;
		result.append(upper_gap_ratio_to_avg_gap+",");
		
		//"lower_gap_ratio_to_mode_gap NUMERIC",
		double lower_gap_ratio_to_mode_gap = 0.0;
		if(lowerTextLine != null) lower_gap_ratio_to_mode_gap = Math.abs(textLine.first_char_pos_y - lowerTextLine.first_char_pos_y);
		lower_gap_ratio_to_mode_gap /= mode_gap;
		result.append(lower_gap_ratio_to_mode_gap+",");
		
		//"lower_gap_ratio_to_avg_gap NUMERIC",
		double lower_gap_ratio_to_avg_gap = 0.0;
		if(lowerTextLine != null) lower_gap_ratio_to_avg_gap = Math.abs(textLine.first_char_pos_y - lowerTextLine.first_char_pos_y);
		lower_gap_ratio_to_avg_gap /= avg_gap;
		result.append(lower_gap_ratio_to_avg_gap+",");
		
		//"are_all_chars_bold {0,1}",
		result.append(textLine.are_all_chars_bold+",");
		
		//Structure Based
		//"numwords_ratio_to_avg_numwords NUMERIC",
		double numwords_ratio_to_avg_numwords = 0.0;
		numwords_ratio_to_avg_numwords = (double)textLine.num_words/avg_numwords;
		result.append(numwords_ratio_to_avg_numwords+",");
		
		//"are_all_words_capitalized {0,1}"
		int are_all_words_capitalized = 1;
		String[] words = textLine.text.split("\\s");
		for(String word: words)
		{	if(word.toLowerCase().matches("a|an|the|about|above|across|after|against|along|among|and|around|at|before|behind|below|beneath|beside|between|but|by|down|during|except|for|from|in|inside|into|like|near|nor|of|off|on|or|onto|outside|over|past|since|so|through|to|toward|under|underneath|until|up|upon|yet|with|within|without"))
				continue;
			if(word.toLowerCase().matches("[a-z].*") && !word.matches("[A-Z].*"))
			{
				are_all_words_capitalized = 0; break;
			}
		}
		result.append(are_all_words_capitalized+",");
		
		//"is_after_abs_or_intro {0,1}",
		int is_after_abs_or_intro = 0;
		if(textLine.lineNumber >= absOrIntLineNumber) is_after_abs_or_intro = 1;
		result.append(is_after_abs_or_intro+",");
		
		//"is_before_ref {0,1}",
		int is_before_ref = 0;
		if(textLine.lineNumber <= refLineNumber) is_before_ref = 1;
		result.append(is_before_ref+",");
		
		
	//"is_first_line_of_page {0,1}",
		int is_first_line_of_page = 0;
		if(upperTextLine==null || textLine.pageNumber > upperTextLine.pageNumber) is_first_line_of_page = 1;
		result.append(is_first_line_of_page+",");
		
	//"is_last_line_of_page {0,1}",
		int is_last_line_of_page = 0;
		if(lowerTextLine == null || textLine.pageNumber < lowerTextLine.pageNumber) is_last_line_of_page = 1;
		result.append(is_last_line_of_page+",");
		
		//"class {0,1}"
		result.append(label);
		
		return result.toString();
	}
	
	/**
	 * Read indir and output ARFF files
	 * @param dir
	 * @param arffOutFile
	 * @param balanceMode
	 */
	public static void getStructureFeatures(String dir, String arffOutFilename, String balanceMode)
	{	String arffHdrString = "@RELATION section_header\n\n";
		
		//insert header
		for(String feature: FEATURES)
		{
			arffHdrString += "@ATTRIBUTE "+feature+"\n";
		}
		arffHdrString += "\n@DATA\n";
		
		Vector<String> pdfFiles = Directory.listAllFiles(dir, ".pdf", 1);
		Vector<String> positiveLines = new Vector<String>();
		Vector<String> negativeLines = new Vector<String>();
		File arffOutFile = new File(arffOutFilename);
		
		try {
			FileUtils.write(arffOutFile, arffHdrString, false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		//here
		for(String pdfFile: pdfFiles)
		{	Util.jout("Processing : "+pdfFile+"\n");
			String taggedFile = pdfFile.replace(".pdf", ".tagged.txt");
			String fileID = Directory.getFileID(pdfFile);
			
			Vector<TextLine> textLines = PdfExtractor.extractTextLinesFromPDF(pdfFile);
			
			//initialize
			absOrIntLineNumber = textLines.size()*2;
			refLineNumber = textLines.size()*2;
			avg_linefontsize = 1.0;
			mode_linefontsize = 1.0;
			FrequencyCounter<Double> mode_linefontsize_counter = new FrequencyCounter<Double>();
			mode_gap = 1.0;
			FrequencyCounter<Double> mode_gap_counter = new FrequencyCounter<Double>();
			avg_gap = 0.0;
			avg_numwords = 0;
			
			//collecting metadata about this file
			for(int i = 0;i < textLines.size(); i++)
			{	TextLine textLine = textLines.elementAt(i);
				avg_linefontsize += textLine.fs_mode;
				
				double tempFontsize = (double)((int)(textLine.fs_mode*10))/10.0;
				mode_linefontsize_counter.add(tempFontsize);
				
				double gap = 0;
				//if(i > 0) gap = Math.abs(textLine.avg_pos_y - textLines.elementAt( i -1).avg_pos_y);
				if(i > 0) gap = Math.abs(textLine.first_char_pos_y - textLines.elementAt( i -1).first_char_pos_y);
				gap = (double)((int)(gap*10))/10.0;
				mode_gap_counter.add(gap);
				
				avg_gap += gap;
				avg_numwords += textLine.num_words;
			}
			
			avg_linefontsize /= (double)textLines.size();
			avg_gap /= (double)(textLines.size() - 1);
			avg_numwords /= (double)textLines.size();
			mode_linefontsize = mode_linefontsize_counter.getTopKElements(1).elementAt(0);
			mode_gap = mode_gap_counter.getTopKElements(1).elementAt(0);
			
			
			/*Util.jout("Gaps:\n");
			Util.printVector(mode_gap_counter.getSortedRawValues());
			Util.jout("Fontsize:\n");
			Util.printVector(mode_linefontsize_counter.getSortedRawValues());
			*/
			//get tagged data
			HashMap<Integer, Integer> taggedData = Util.getLinewiseTagData(taggedFile);
			
			for(int i = 0; i < textLines.size(); i++)
			{	TextLine textLine = textLines.elementAt(i);
				
				Integer taggedValue = taggedData.get(textLine.lineNumber);
				if(taggedValue == null) continue;
				String featureString = null;
				
			//update parameters
				if(absOrIntLineNumber > textLines.size() && (DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.ABS)
						|| DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.INT))
				)
				{
					absOrIntLineNumber = textLine.lineNumber;
				}
				
				if(refLineNumber > textLines.size() && DocumentSegmentator.isStandardSection(textLine.text, DocumentNode.StdSection.REF))
				{
					refLineNumber = textLine.lineNumber;
				}
				
				if(taggedValue > 0)
				{	featureString = getFeatureString(textLine, i, 1, textLines, fileID);
					positiveLines.add(featureString);
				}
				else
				{
					featureString = getFeatureString(textLine, i, 0, textLines, fileID);
					negativeLines.add(featureString);
				}
			}
		}
		
		
		Vector<String> smallerList = null;
		Vector<String> largerList = null;
		smallerList = positiveLines;
		largerList = negativeLines;
		if(negativeLines.size() < positiveLines.size())
		{
			smallerList = negativeLines;
			largerList = positiveLines;
		}
		int dupFactor = 1;
		
		if(balanceMode.equalsIgnoreCase("dupplicate") || balanceMode.equalsIgnoreCase("subset"))
		{
			dupFactor = Math.max(positiveLines.size(), negativeLines.size())/Math.min(positiveLines.size(), negativeLines.size());
			
		}
		
		/*if(balanceMode.equalsIgnoreCase("subset"))
		{
			String[] subsetList = new String[dupFactor];
			for(int i = 0; i < dupFactor; i++)
			{
				subsetList[i] = "";
			}
			
			int subsetIndex = 0;
			for(int i = 0; i < largerList.size(); i++)
			{
				if(subsetIndex >= dupFactor) subsetIndex = 0;
				subsetList[subsetIndex] += largerList.elementAt(i)+"\n";
				
				subsetIndex++;
			}
			
			for(int i = 0; i < dupFactor; i++)
			{
				String setArffString = arffString;
				
				for(String line: smallerList)
				{
					setArffString += line + "\n";
				}
				
				setArffString += subsetList[i];
				
				try {
					FileUtils.writeStringToFile(new File(arffOutFile.replace(".arff", "")+"_subset_"+i+".arff"), setArffString);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		}
		*/
		StringBuilder arffString = new StringBuilder();
		for(String line: smallerList)
		{
			for(int i = 0; i < dupFactor; i++)
			{
				arffString.append(line+"\n");
			}
		}
				
		for(String line: largerList)
		{
			arffString.append(line+"\n");
		}
		
		
		try {
			FileUtils.write(arffOutFile, arffString.toString(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Segment the pdfFile given a trained classifier
	 * @param pdfFile
	 * @param c
	 * @return
	 */
	public static Vector<Range<Integer>> getSectionRanges(String pdfFile, Classifier c)
	{	Vector<Range<Integer>> result = new Vector<Range<Integer>>();
		Instances dataTemplate = null;
		DataSource source;
		try {
			//source = new DataSource("./models/section_header_data_template.arff");
			source = new DataSource("./experiment/document_segmentation/arff/ALL.arff");	//23 features
			dataTemplate = source.getDataSet();
			dataTemplate.deleteAttributeAt(0); //delete ID
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		if (dataTemplate.classIndex() == -1)
		{
			dataTemplate.setClassIndex(dataTemplate.numAttributes() - 1);	//'class' attribute
		}
		
		Vector<TextLine> textLines = PdfExtractor.extractTextLinesFromPDF(pdfFile);
		String fileID = Directory.getFileID(pdfFile);
		int lastLine = 0;
		Vector<Integer> sectionHeaders = new Vector<Integer>();
		for(int i = 0; i < textLines.size(); i++)
		{	TextLine tl = textLines.elementAt(i);
			if(tl.lineNumber > lastLine) lastLine = tl.lineNumber;
			String featureString = getFeatureString(tl, i, 0, textLines, fileID);
			String[] tokens = featureString.split(",");
			double[] attr = new double[tokens.length];
			for(int j = 1; j < tokens.length; j++)
			{
				attr[j] = Double.parseDouble(tokens[j]);
			}
			Instance sample = new DenseInstance(1.0, attr);
			sample.setDataset(dataTemplate);
			
			double[] dist = null;
			try {
				dist = c.distributionForInstance(sample);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(dist[1] >= 0.5) 
			{	sectionHeaders.add(tl.lineNumber);
				//Util.jout("@@ "+tl.text+"\n");
			}
		}
		
		sectionHeaders.remove(new Integer(0));
		if(!sectionHeaders.contains(new Integer(1))) sectionHeaders.add(new Integer(1));
		Collections.sort(sectionHeaders);
		
		for(int i = 0; i < sectionHeaders.size() -1; i++)
		{
			result.add(Range.between(sectionHeaders.elementAt(i), sectionHeaders.elementAt(i+1) - 1));
		}
		result.add(Range.between(sectionHeaders.elementAt(sectionHeaders.size() - 1), lastLine));
		Util.printVector(result);
		return result;
	}
	
	public void loadDefaultModel()
	{
		this.loadModel("./models/section_header_23features_randomforest500_w_ThresholdSelector.model");
	}
	
	//for usage purposes with a trained model
	public void loadModel(String modelFilename)
	{	
		try {
			c = (Classifier) weka.core.SerializationHelper.read(modelFilename);
			//load training data for reference
			DataSource source = new DataSource("./experiment/document_segmentation/arff/ALL.arff");
			Instances data = source.getDataSet();
			 // setting class attribute if the data format does not provide this information
			 // For example, the XRFF format saves the class attribute information as well
			
			//delete ID field
			data.deleteAttributeAt(0);
			 if (data.classIndex() == -1)
			   data.setClassIndex(data.numAttributes() - 1);
			refInstancesWithID = data;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}
	
	
	public boolean isSectionHeader(TextLine tl, Vector<TextLine> ref)
	{	int currentIndex = 0;
		int label = 0;
		
		//find line index number
		for(currentIndex = 0; currentIndex <= ref.size(); currentIndex++)
		{
			if(tl.lineNumber == ref.get(currentIndex).lineNumber) break;
		}
		
		String featureString = getFeatureString(tl, currentIndex, label, ref, "temp");
		 
		//delete ID
		//form an instance
		String[] tokens = featureString.split(",");
		DenseInstance instance = new DenseInstance(tokens.length);
		
		for(int i = 1; i < tokens.length; i++)
		{	try{
			
				instance.setValue(i-1, Double.parseDouble(tokens[i]));

			}catch(Exception e)
			{
				instance.setValue(i-1, 0);
			}
		}
		instance.setDataset(refInstancesWithID);
		
		//classify
		try {
			double prediction = c.classifyInstance(instance);
			if(prediction > 0.5)
			{	return true;
				//Util.jout("Confidence "+c.distributionForInstance(instance)[1]+": "+tl.text+"\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	/**
	 * Note that the input is a text. This function only uses regex
	 * @param text
	 * @return
	 */
	public static Vector<DocumentNode.StdSection> getStandardSectionTags(String text)
	{
		Vector<DocumentNode.StdSection> tags = new Vector<DocumentNode.StdSection>();
		
		for(DocumentNode.StdSection tag: DocumentNode.StdSection.values())
		{
			if(DocumentSegmentator.isStandardSection(text, tag)) tags.add(tag);
		}
		
		return tags;
	}
	
	/**
	 * This method takes a set of ordered textlines and output a set of line numbers representing section headers. 
	 * 
	 * A post processing step is taken to merge two or more consecutive lines which are identified as section headers together. 
	 * 
	 * @param tls
	 * @return vector of sections, sorted by ordered
	 */
	public Vector<Section> ExtractSections(Vector<TextLine> tls)
	{
		Vector<Section> result = new Vector<Section>();
		
		Vector<TextLine> buffer = new Vector<TextLine>();
		int maxLineNum = 0;
		HashMap<Integer, String> sectionMarker = new HashMap<Integer, String>();
		
		//load classifier
		//SectionHeaderClassifier s = new SectionHeaderClassifier();
		//s.loadDefaultModel();
		
		for(TextLine tl: tls)
		{
			if(tl.lineNumber > maxLineNum) maxLineNum = tl.lineNumber;
			
			if(this.isSectionHeader(tl, tls))
			{
				buffer.add(tl);
			}
			else
			{
				
				if(!buffer.isEmpty())
				{
					StringBuilder str = new StringBuilder();
					int beginLineNum = buffer.get(0).lineNumber;
					for(TextLine buff: buffer)
					{
						str.append(buff.text+" ");
						if(buff.lineNumber < beginLineNum) beginLineNum = buff.lineNumber;
					}
					
					sectionMarker.put(new Integer(beginLineNum), str.toString().trim());
					
					buffer.clear();
				}
				
			}
		}
		
		//Process section
		TreeSet<Integer> sortedKeys = new TreeSet<Integer>();
		sortedKeys.addAll(sectionMarker.keySet());
		
		//init with section header
		Section currentSection = new Section("[Paper Title]");
		currentSection.range = Range.between(0, 0);
		currentSection.sectionTypes.add(StdSection.HDR);
		for(Integer lineNum: sortedKeys)
		{
			//close old section
			currentSection.range = Range.between(currentSection.range.getMinimum(), lineNum-1);
			result.add(currentSection);
			
			currentSection = new Section(sectionMarker.get(lineNum));
			currentSection.range = Range.between(lineNum, lineNum);
			currentSection.sectionTypes.addAll(getStandardSectionTags(currentSection.sectionName));
		}
		
		//closing section
		if(!result.isEmpty())
		{
			result.lastElement().range = Range.between(result.lastElement().range.getMinimum(), maxLineNum);
		}
		return result;
	}
	
	public static void main(String[] args)
	{
		//getARFF("sample/00_experiment_data/doc_seg/done", "sample/00_experiment_data/doc_seg/done/line_section.arff", "");
		//getARFF("sample/00_experiment_data/doc_seg/done/test", "sample/00_experiment_data/doc_seg/done/test/line_section_test2.arff", "");
		//Classifier c = ClassifierFactory.loadModel("./models/section_header_randomforest500.model");
		//getSectionRanges("./sample/p2.pdf", c);
		
		getStructureFeatures("./data/grotoap_docseg", "./experiment/document_segmentation/arff/GROTOAP-ALL.arff", "");
		
		//****Illustrate the usage of section classifier with a trained model
		/*String trainedModelFilename = "./models/section_header_23features_randomforest500_w_ThresholdSelector.model";
		String pdfFilename = "./sample/10.1.1.111.1009.pdf";
		Vector<TextLine> tls = PdfExtractor.extractTextLinesFromPDF(pdfFilename);
		
		SectionHeaderClassifier s = new SectionHeaderClassifier();
		s.loadModel(trainedModelFilename);
		
		for(TextLine tl: tls)
		{
			if(s.isSectionHeader(tl, tls))
			{
				Util.printVector(getStandardSectionTags(tl.text));
			}
			
		}
		
		
		Vector<Section> allSections = s.ExtractSections(tls);
		Util.printVector(allSections);
		*/
		//getSectionRanges(pdfFilename, s.c);
		
		Util.jout("Done\n");
	}
}
