package Util;
import PdfSegmentation.TextExtractor;

import com.aliasi.chunk.Chunk;
import com.aliasi.chunk.Chunking;

import com.aliasi.sentences.MedlineSentenceModel;
import com.aliasi.sentences.SentenceChunker;
import com.aliasi.sentences.SentenceModel;

import com.aliasi.tokenizer.IndoEuropeanTokenizerFactory;
import com.aliasi.tokenizer.TokenizerFactory;

import com.aliasi.util.Files;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

/** Use SentenceModel to find sentence boundaries in text */
public class SentenceProducer {

    static final TokenizerFactory TOKENIZER_FACTORY = IndoEuropeanTokenizerFactory.INSTANCE;
    static final SentenceModel SENTENCE_MODEL  = new MedlineSentenceModel();
    static final SentenceChunker SENTENCE_CHUNKER 
	= new SentenceChunker(TOKENIZER_FACTORY,SENTENCE_MODEL);

    public static void old_main(String[] args) throws IOException {
	File file = new File(args[0]);
	String text = Files.readFromFile(file,"UTF-8");
	System.out.println("INPUT TEXT: ");
	System.out.println(text);

	Chunking chunking 
	    = SENTENCE_CHUNKER.chunk(text.toCharArray(),0,text.length());
	Set<Chunk> sentences = chunking.chunkSet();
	if (sentences.size() < 1) {
	    System.out.println("No sentence chunks found.");
	    return;
	}
	String slice = chunking.charSequence().toString();
	int i = 1;
	for (Iterator<Chunk> it = sentences.iterator(); it.hasNext(); ) {
	    Chunk sentence = it.next();
	    int start = sentence.start();
	    int end = sentence.end();
	    System.out.println("SENTENCE "+(i++)+":");
	    System.out.println(slice.substring(start,end));
	}
    }
//===========================Suppawong's Territory=====================
    Set<Chunk> sentences = null;
    Iterator<Chunk> it = null;
    String slice = null;
    
    public SentenceProducer(String _filename, int mode)
    //mode 1 = file
    //mode 2 = String
    {
   
    		defaultInit(_filename, mode);
    }
    
    
    public void defaultInit(String _filename, int mode)
    {	
    	
    	
		try{
			String text  = null;
			if(mode == 1)
			{
				File file = new File(_filename);
				text = Files.readFromFile(file,"ISO-8859-1");
			}else if(mode == 2)
			{
				text = _filename;	//here _filename is the actual text
			}
			
			Chunking chunking 
		    = SENTENCE_CHUNKER.chunk(text.toCharArray(),0,text.length());
			
			sentences = chunking.chunkSet();
			
			if (sentences.size() < 1) {
			    System.out.println("No sentence chunks found.");
			    return;
			}
			slice = chunking.charSequence().toString();
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		
		it = sentences.iterator();
    }
    
    public String nextSentence()
    //return next available sentence, null otherwise
    {
    	if(it == null || !it.hasNext()) return null;
    	
    	Chunk sentence = it.next();
	    int start = sentence.start();
	    int end = sentence.end();
	    //return java.text.Normalizer.normalize(slice.substring(start,end).replace("\n", "").trim(), java.text.Normalizer.Form.NFD);
	    return slice.substring(start,end).replace("\n", "").trim();
	    
    }
    
    /**
     * Generate a sentence file from input text file.
     * @param intextFile
     * @param outSentFile
     * @param toolname
     */
    public static void getSentenceFile(String inTextFile, String outSentFile, String toolname)
    {
    	if(toolname.equalsIgnoreCase("lingpipe"))
    	{	SentenceProducer sp = new SentenceProducer(inTextFile, 1);
    		try{
    			BufferedWriter writer = new BufferedWriter(new FileWriter(outSentFile));
    			String sent = "";
    			while((sent = sp.nextSentence()) != null)
    			{
    				sent = sent.trim();
    				if(sent.isEmpty()) continue;
    				writer.write(sent+"\n");
    			}
    			writer.close();
    		}catch(Exception e)
    		{
    			e.printStackTrace();
    			Util.errlog(e.toString());
    		}
    		
    	}else if(toolname.equalsIgnoreCase("sumit"))
    	{	/*String cmd = ""
    		CommandExecutor.exec(cmd, verbose)*/
    	}
    }
    
    //extract sentences from text
    public static Vector<String> getSentnecesFromText(String text)
    {
    	Vector<String> result = new Vector<String>();
    	SentenceProducer sp = new SentenceProducer(text, 2);
    	String sentence = "";
    	while((sentence = sp.nextSentence()) != null)
    	{
    		result.add(sentence);
    	}
    	return result;
    }
    
    public static void main(String[] args)
    {	TextExtractor.PDFBox_pdfToText("./code/sample/10.1.1.111.8323.pdf", "./code/sample/text.txt");
    	getSentenceFile("./code/sample/text.txt", "./code/sample/sent.txt", "lingpipe");
    }
    
//===========================End of Suppawong's Territory=====================
}


