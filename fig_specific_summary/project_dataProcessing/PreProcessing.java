package project_dataProcessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;

public class PreProcessing {
	public static boolean isUTF8MisInterpreted( String input, String encoding) {

	    CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
	    CharsetEncoder encoder = Charset.forName(encoding).newEncoder();
	    java.nio.ByteBuffer tmp;
	    try {
	        tmp = encoder.encode(CharBuffer.wrap(input));
	    }

	    catch(CharacterCodingException e) {
	        return false;
	    }

	    try {
	        decoder.decode(tmp);
	        return true;
	    }
	    catch(CharacterCodingException e){
	        return false;
	    }       
	}
	public static boolean isUTF8MisInterpreted( String input ) {
        //convenience overload for the most common UTF-8 misinterpretation
        //which is also the case in your question
		return isUTF8MisInterpreted( input, "UTF-8");  
	}
	
	public static ArrayList<String> readLines(String filename) throws IOException {
        FileReader fileReader = new FileReader(filename);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        ArrayList<String> lines = new ArrayList<String>();
        String line = null;
        while ((line = bufferedReader.readLine()) != null) {
        	String encoded = new String(line.getBytes("utf-8"), "iso8859-1");
            lines.add(encoded);
        }
        bufferedReader.close();
        return lines;
//        return lines.toArray(new String[lines.size()]);
    }

	public static void main(String[] args) throws IOException {
		//String file_path = "./DetectedPatterensoutputNew.txt";
		
		String possitive_file = "./positive_AllClass_ICDM.txt";
		String negative_file = "./negative_AllClass_ICDM.txt";
		
		//String training = "./training.txt";
		//String testing = "./testing.txt";
		
		ArrayList<String> positve_lines = readLines(possitive_file);
		ArrayList<String> negative_lines = readLines(negative_file);
		
		// Down sampling negative class instances
		java.util.Collections.shuffle(negative_lines);
		ArrayList<String> tmp = new ArrayList<String>();
		//double chozen_size_training = negative_lines.size() * 0.03;
		double chozen_size_training = negative_lines.size() ;
		for (int i = 0; i < chozen_size_training && negative_lines.size() > 0; i++) {
			tmp.add(negative_lines.remove(0));
		}
		negative_lines = tmp;
		
		java.util.Collections.shuffle(positve_lines);
		chozen_size_training = positve_lines.size() ;
		//chozen_size_training = positve_lines.size() * 0.88;

		ArrayList<String> training_positive = new ArrayList<String>();
		for (int i = 0; i < chozen_size_training && positve_lines.size() > 0; i++) {
			training_positive.add(positve_lines.remove(0));
		}
		ArrayList<String> testing_positive = positve_lines;
		
		
		java.util.Collections.shuffle(negative_lines);
		chozen_size_training = negative_lines.size() ;
		//chozen_size_training = negative_lines.size() * 0.3;
		ArrayList<String> training_negative = new ArrayList<String>();
		for (int i = 0; i < chozen_size_training && negative_lines.size() > 0; i++) {
			training_negative.add(negative_lines.remove(0));
		}
		ArrayList<String> testing_negative = negative_lines;
		
		
		
		ArrayList<String> total_training_positive=new ArrayList<String>();
		total_training_positive.addAll(training_positive);
		total_training_positive.addAll(training_positive);
		total_training_positive.addAll(training_positive);
		total_training_positive.addAll(training_positive);
		
		ArrayList<String> training_data = new ArrayList<String>();
		training_data.addAll(total_training_positive);
		training_data.addAll(training_negative);
		
		ArrayList<String> testing_data = new ArrayList<String>();
		testing_data.addAll(testing_positive);
		testing_data.addAll(testing_negative);
		//C:\IQRA DATA\Eclipse workspace\algorithm_flow.2017-01-17\dataset line files
		//String out_path = "E:\\Algo\\algorithm_flow.2017-01-17\\results\\training.txt";
		String out_path="./training_ICDM.txt";
		File fout = new File(out_path);
		FileOutputStream fos = new FileOutputStream(fout);
		OutputStreamWriter osw = new OutputStreamWriter(fos);
		for (int x=0; x<training_data.size(); x++)
		    osw.write( training_data.get(x) + "\n" );
		osw.flush();
		osw.close();
		
		//out_path = "E:\\Algo\\algorithm_flow.2017-01-17\\results\\testing.txt";
		out_path="./testing_ICDM.txt";
		fout = new File(out_path);
		fos = new FileOutputStream(fout);
		osw = new OutputStreamWriter(fos);
		for (int x=0; x<testing_data.size(); x++)
		    osw.write( testing_data.get(x) + "\n" );
		osw.flush();
		osw.close();
		
		System.out.println("Training Positive = " + total_training_positive.size());
		System.out.println("Testing Positive = " + testing_positive.size());
		System.out.println("Training Negative = " + training_negative.size());
		System.out.println("Testing Negative = " + testing_negative.size());
		
		// As positive lines are very less so we have to up sample them
		
//		BufferedReader br = new BufferedReader(new FileReader(new File(possitive_file)));
		// String out_path = "E:\\Algo\\algorithm_flow.2017-01-17\\results\\without_junk_lines.txt";
		// File fout = new File(out_path);
		// FileOutputStream fos = new FileOutputStream(fout);
		// OutputStreamWriter osw = new OutputStreamWriter(fos);
//		for(String line; (line = br.readLine()) != null; ) {
//			String encoded = new String(line.getBytes("utf-8"), "iso8859-1");
//			System.out.println(encoded);
//			//osw.append(encoded + "\n");
//		}
		//br.close();
		//osw.flush();
		//osw.close();
	}
}
