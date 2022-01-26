package Caption_Extraction;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.stream.IntStream;

import com.opencsv.CSVReader;

import Util.Directory;
import Util.Util;

public class synopsisGeneration {
	public static double k1 = 2.0; 
	public static double k3 = 2.0; 
	public static double b = 0.75;
	public static int range=10;
	public static double word_density=0;
	public static String CaptionText="";
	public static String taggedTextFilename="";
	public static String pdfFilename="";
	public static int theroshold=8;
	public static int numOfSentences=0;
	public static String[] Captionquery=null;
	public static String[] absquery=null;
	public static double average_num_word_line=0.0;
	public static int words_perline= 0;
	public static String linewithoutStopWords="";

	public static double idf(int num_sentences, int sentenceFrequency) { 
		double numerator = num_sentences - sentenceFrequency + 0.5; 
		double denominator = sentenceFrequency + 0.5; 
		return Math.log(numerator / denominator); 
	} 

	public static int[] computeDoclength_SFwithT(String f,String[] que)
	{
		int[]  SF= new int[que.length];
		List<Integer> num_words_per_line1 = new ArrayList<Integer>();
		try(BufferedReader br = new BufferedReader(new FileReader(new File(f)))){

			for(String line; (line = br.readLine()) != null; ) {

				linewithoutStopWords=Preprocesing.StemWords(Preprocesing.removeStopWords(line));
				num_words_per_line1.add(Preprocesing.count_words(line));
				//numOfSentences++;
				//String[] l = linewithoutStopWords.split(" ");
				for(int i=0;i<SF.length;i++)
				{

					if(linewithoutStopWords.contains(que[i]))
					{
						SF[i]=SF[i]+1;
					}
					//}
				}


			}
			//System.out.println("query:"+CaptionText);
			for(int i=0;i<SF.length;i++)
			{
				//System.out.println("Term:"+query[i]+" "+i+" "+SFwithT[i]);
			}
			br.close();
			//This line is converting Arraylist to integer array and calculating 
			// Sum of all of it's values
			
			if(num_words_per_line1.size()==0){
				words_perline=num_words_per_line1.size()+1;
			}else{words_perline=num_words_per_line1.size();}
			
				
			average_num_word_line = (IntStream.of(num_words_per_line1.stream().filter(i -> i != null).mapToInt(i -> i).toArray()).sum())/(words_perline);
		
			numOfSentences=words_perline;


		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//int[] a={1,2};
		return SF;
	}

	public static int[] frequency_TFwithQ(List<String> text,String[] que)
	{
		int j=0;
		int[] TFQ =new int[que.length];
		for(String str: text ) {
			int occurrences = Collections.frequency(text, str);
			TFQ[j]=occurrences;
			j++;
		}
		return TFQ;
	}

	public static int[] frequency_TFwithS(List<String> S, List<String> I,String[] que)
	{
		int j=0;
		int[] TFS=new int[que.length];
		//System.out.println("query here:" +I);
		for(String str: I ) {
			int occurrences = Collections.frequency(S, str);
			TFS[j]=occurrences;
			//System.out.println("Occurence of " + str+ " is "+occurrences);
			j++;
		}
		//linewithoutStopWords="algorithm init";
		for(int i=0;i<TFS.length;i++)
		{
			if(TFS[i]>0){
				//System.out.println("Term: "+que[i]+ " in sentence: "+ linewithoutStopWords+" :count: "+TFS[i]);
			}
		}
		return TFS;
	}
	public static void main(String args []) {

		//Writer writer = null;
		int iteration=1;
	//	System.out.println("Hafsa" + iteration);

		//Vector<String> TaggedTextFiles = Directory.listAllFiles("ACL txt/headerandReferences_ACK", ".pdf.txt", 1);
		Vector<String> TaggedTextFiles = Directory.listAllFiles("Textfiles_resultsec/", ".pdf.txt", 1);

		PorterStemmer stemer=new PorterStemmer();
		String file1=null;//for caption synopsis
		String file2=null;//for reference synopsis
		try {
			//writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("Top20matchedwithCaption.csv"), "UTF-8"));
			System.out.println("In Try");
			CSVReader reader = new CSVReader(new FileReader("./Synonpsis_ACL.csv"));
			//CSVReader reader = new CSVReader(new FileReader("./Check1.csv"));
			String [] nextLine;
			int count=0;
			while ((nextLine = reader.readNext()) != null) {
				count++;
				System.out.println("count = "+ count);
				//taggedTextFilename="ACL txt/headerandReferences_ACK/"+nextLine[0]+".pdf.txt";
				taggedTextFilename="Textfiles_resultsec//"+nextLine[0]+".pdf.txt";
				
				System.out.println("TTF="+taggedTextFilename);
				//file="ACL txt/Synopsis/"+nextLine[0]+".txt";
				//if(iteration == 1){
				//file="ACL txt/Synopsis/"+nextLine[0]+".tagged.txt";
				//}
				//else{
				//System.out.println(DetectPattern.isIncideRAD(nextLine[1]));
				//System.out.println("Hafsa" + iteration);
				boolean isRAD=false;
				isRAD=DetectPattern.isIncideRAD(nextLine[1]);
				if(isRAD==false)
				{
					System.out.println("INSIDE OTHERSECTION!");
					
				
					continue;
				}
				iteration++;
				//file1="Textfiles/synopsis_captions/"+nextLine[0]+"_"+iteration+".txt";
				//file2="Textfiles/synopsis_references/"+nextLine[0]+"_"+iteration+".txt";
				file1="Textfiles/synopsis_captions_rad/"+nextLine[0]+"_"+iteration+".txt";
				//file2="Textfiles/synopsis_references_rad/"+nextLine[0]+"_"+iteration+".txt";
				
				//}
			//	System.out.println("Hafsa" + iteration);
				List<Integer> num_words_per_line1 = new ArrayList<Integer>();
				System.out.println("In reading");
				CaptionText=nextLine[2];
				String abs=nextLine[3];
				//System.out.println("--------::::abstract::::::-----"+ abs+ "-------::::::abstract::::::-----");
				//IF WE USE ABS INSTEAD OF CAPTIONTEXT THEN WE WILL BE GENERATING SYNOPSIS FOR REFRENCES!!!!!!!!!!
				//UNCOMMENT THIS WHOLE CHUNCK FROM LINE 164 TO 210
			String q=Preprocesing.StemWords(Preprocesing.removeStopWords(CaptionText));
				Captionquery = q.split(" ");
				int[] SFwithT= new int[Captionquery.length];
				int[] TFwithS= new int[Captionquery.length];
				int[] TFwithQ= new int[Captionquery.length];
				//System.out.println("-----Caption-----:"+CaptionText+ " size of SFwithT:"+SFwithT.length);
				for(String f: TaggedTextFiles){
					//Util.jout("Going to extract 1:::: " + f + "\n");
					System.out.println("F="+f);
					if(f.equals(taggedTextFilename)){
						System.out.println("IM HERE!");
						//Util.jout("Matched file:::: Going to extract: " + f + "\n");
						///iteration++;
						List<String> input= Arrays.asList(Captionquery);
						SFwithT=computeDoclength_SFwithT(f,Captionquery);
						TFwithQ=frequency_TFwithQ(input,Captionquery);
						BufferedReader br1 = new BufferedReader(new FileReader(new File(f)));
						//Writer bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
						//PrintWriter bw1 = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
						Map<String,Double> mapper = new HashMap<>(); 
						for(String line; (line = br1.readLine()) != null; ) {
							linewithoutStopWords=Preprocesing.StemWords(Preprocesing.removeStopWords(line));
							List<String> Sentence= Arrays.asList(linewithoutStopWords.split(" "));
							TFwithS=frequency_TFwithS(Sentence,input,Captionquery);
							double score = 0.0;
							for(int i=0;i< Captionquery.length;i++)
							{
								double inverseDocumentFrequency = idf(numOfSentences,SFwithT[i]); 
								double SentenceTFwithS = TFwithS[i] * (k1 + 1); 

								double denominatorTFwithS = SentenceTFwithS
										+ (k1 * (1 - b + (b * Preprocesing.count_words(line) / average_num_word_line))); 
								double queryTFwithQ= TFwithQ[i]*(k3+1);
								double denominatorTFwithQ= k3 +TFwithQ[i];
								score += inverseDocumentFrequency * SentenceTFwithS / denominatorTFwithS *(queryTFwithQ/denominatorTFwithQ); 
							}
							
							//System.out.println("line= "+line+ " Score : "+ score);
							line=line+" ";
							mapper.put(line,score);
						    OrderByMapvalue.mapperfunction(mapper,5,file1,abs,CaptionText);
							//bw1.write(line+" ");
							//writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(""), "UTF-8"));
						}
						//bw1.flush();
						//bw1.close();
						br1.close(); 
					}
				}
				//THE FOLLOWING SECTION FROM LINE 213 TO 259 IS FOR SYNOPSIS OF REFERENCES(abs), WE CAN UNCOMMENT 
				//IT TO GET SYNOPSIS OF ANYOTHER SECTION!!!!
				/*String q1=Preprocesing.StemWords(Preprocesing.removeStopWords(abs));
				absquery = q1.split(" ");
				int[] SFwithT1= new int[absquery.length];
				int[] TFwithS1= new int[absquery.length];
				int[] TFwithQ1= new int[absquery.length];
				//uncomment //System.out.println("-----refrence-----:"+abs+ " size of SFwithT:"+SFwithT1.length);
				for(String f: TaggedTextFiles){
					Util.jout("Going to extract 1:::: " + f + "\n");
					if(f.equals(taggedTextFilename)){
						Util.jout("Matched file:::: Going to extract: " + f + "\n");
						///iteration++;
						List<String> input= Arrays.asList(absquery);
						SFwithT1=computeDoclength_SFwithT(f,absquery);
						TFwithQ1=frequency_TFwithQ(input,absquery);
						BufferedReader br1 = new BufferedReader(new FileReader(new File(f)));
						//Writer bw1 = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file),"UTF-8"));
						//PrintWriter bw1 = new PrintWriter(new BufferedWriter(new FileWriter(file,true)));
						Map<String,Double> mapper = new HashMap<>(); 
						for(String line; (line = br1.readLine()) != null; ) {
							linewithoutStopWords=Preprocesing.StemWords(Preprocesing.removeStopWords(line));
							List<String> Sentence= Arrays.asList(linewithoutStopWords.split(" "));
							TFwithS1=frequency_TFwithS(Sentence,input,absquery);
							double score = 0.0;
							for(int i=0;i< absquery.length;i++)
							{
								double inverseDocumentFrequency = idf(numOfSentences,SFwithT1[i]); 
								double SentenceTFwithS = TFwithS1[i] * (k1 + 1); 

								double denominatorTFwithS = SentenceTFwithS
										+ (k1 * (1 - b + (b * Preprocesing.count_words(line) / average_num_word_line))); 
								double queryTFwithQ= TFwithQ1[i]*(k3+1);
								double denominatorTFwithQ= k3 +TFwithQ1[i];
								score += inverseDocumentFrequency * SentenceTFwithS / denominatorTFwithS *(queryTFwithQ/denominatorTFwithQ); 
							}
							
					//uncomment		//System.out.println("line= "+line+ " Score : "+ score);
							line=line+" ";
							 mapper.put(line,score);
						     OrderByMapvalue.mapperfunction(mapper,5,file2,abs,CaptionText);
							//bw1.write(line+" ");
							//writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(""), "UTF-8"));
						}
						//bw1.flush();
						//bw1.close();
						br1.close(); 
					}
				}*/
				
				
			}


			//System.out.println("Done");


		}catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		}



		
}




