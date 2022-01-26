package Caption_Extraction;

import java.util.ArrayList; 
import java.util.HashMap; 
import java.util.Map;

import AlgorithmExtraction.DocObject;
import AlgorithmExtraction.Util; 

public class BM25 {
	private double k1 = 2.0; 
	 private double b = 0.75; 
	 private double numOfFiles = 0; 
	 private double avgLength = 0.0; 
	 private String[] keywords; 
	 private ArrayList<DocObject> docList; 
	 private Map<String, Integer> docFrequencyMap; 
	 
	 public BM25(String[] keywords, ArrayList<DocObject> docList, Map<String, Integer> docFrequencyMap) { 
	  this.keywords = keywords; 
	  this.docList = docList; 
	  this.docFrequencyMap = docFrequencyMap; 
	  this.numOfFiles = docList.size(); 
	  setAverageDocumentLength(); 
	 } 
	 
	 public void setAverageDocumentLength() { 
	  double sum = 0; 
	  for (DocObject doc : this.docList) { 
	   try { 
	    sum += doc.getLength(); 
	   } catch (Exception e) { 
	    e.printStackTrace(); 
	   } 
	  } 
	  this.avgLength = sum / this.numOfFiles; 
	 } 
	  
	 public DocObject getDoc(String name){ 
	  for (DocObject d: this.docList){ 
	   if (d.getName().equals(name)) 
	    return d; 
	  } 
	  return null; 
	 } 
	  
	 public void updateDoc(DocObject doc){ 
	  for(int i=0; i<this.docList.size(); i++){ 
	   DocObject d = this.docList.get(i); 
	   if (d.getName().equals(doc.getName())) 
	    this.docList.set(i, doc); 
	  } 
	 } 
	 
	 public double termFrequency(String term, DocObject doc) { 
	  double frequency = 0; 
	  try { 
	   String content = doc.getContent(); 
	   String[] words = content.split(" "); 
	   for (int i = 0; i < words.length; i++) { 
	    term = term.toLowerCase(); 
	    String word = words[i].toLowerCase(); 
	    if (term.equals(word)) { 
	     frequency++; 
	    } 
	   } 
	  } catch (Exception e) { 
	   e.printStackTrace(); 
	  } 
	  return frequency; 
	 } 
	 
	 public double idf(String term, DocObject doc) { 
	  int containingDoc = 0; 
	  if (this.docFrequencyMap.containsKey(term)) 
	   containingDoc = this.docFrequencyMap.get(term); 
	  double numerator = this.numOfFiles - containingDoc + 0.5; 
	  double denominator = containingDoc + 0.5; 
	  return Math.log(numerator / denominator); 
	 } 
	 
	 public Map<String, ArrayList<DocObject>> getResults() { 
	  Map<String, ArrayList<DocObject>> result = new HashMap<>(); 
	  try { 
	   for (String keyword : this.keywords) { 
	    keyword = keyword.toLowerCase(); 
	    String[] words = keyword.split(" "); 
	     
	    for (DocObject doc : this.docList) { 
	     calculateScore(words, doc); 
	    } 
	     
	    result.put(keyword, Util.retrieveTop10Documents(this.docList)); 
	   } 
	  } catch (Exception e) { 
	   e.printStackTrace(); 
	  } 
	  return result; 
	 } 
	 
	 private void calculateScore(String[] words, DocObject doc) { 
	  double score = 0.0; 
	  for (String term : words) { 
	   double inverseDocumentFrequency = this.idf(term, doc); 
	   double frequency = this.termFrequency(term, doc); 
	   double numerator = frequency * (this.k1 + 1); 
	   double denominator = frequency 
	     + (this.k1 * (1 - this.b + (this.b * doc.getLength() / this.avgLength))); 
	   score += inverseDocumentFrequency * numerator / denominator; 
	  } 
	  doc.setScore(score); 
	  this.updateDoc(doc); 
	 } 

}
