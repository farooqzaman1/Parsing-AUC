package Caption_Extraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Remove_HeaderAndReferences {
	public static void main(String args[]) {

		// TODO Auto-generated method stub

		File folder = new File("Textfiles\\");
		File[] listOfFiles = folder.listFiles();
		int positve_lines = 0;
		int neg_lines = 0;

		// Writer negative_line_writter = new BufferedWriter(new
		// OutputStreamWriter(new FileOutputStream("./negative.txt"),
		// "UTF-8"));
		for (int i = 0; i < listOfFiles.length; i++) {
			// writer = new BufferedWriter(new OutputStreamWriter(new
			// FileOutputStream("output_without_abstract/" +
			// listOfFiles[i].getName()), "UTF-8"));
			DetectPattern.abstract_found = false;
			DetectPattern.ref_section_found = false;
			DetectPattern.ack_section_found = false;
			DetectPattern.RAD_section_found = false;
			DetectPattern.intro_section_found = false;
			DetectPattern.background_section_found = false;
			DetectPattern.RAD_section_found = false;
			DetectPattern. beforeIntro_found = false;
			DetectPattern. AfterBCK_found = false;
			DetectPattern. AfterCON_found = false;
			DetectPattern.AfterIntro_found = false;
			DetectPattern.beforeRAD_found= false;
			DetectPattern.beforeMTH_found = false;
			

			if (listOfFiles[i].isFile() && listOfFiles[i].getName().endsWith(".txt")) {
				System.out.println("File " + listOfFiles[i].getName());

				// BufferedReader br = null;
				// Writer positive_line_writter = null;
				try {
					//String req_file = "pseudocode_and_sbs/test/" + listOfFiles[i].getName();
					//String req_file2 = "pseudocode_and_sbs/test/Without_Ref_abs/" + listOfFiles[i].getName();\
					
					//String req_file = "citation paper/Cited by_text/" + listOfFiles[i].getName();
					//String req_file2 = "citation paper/Cited by_withoutHeaderandReferences/" + listOfFiles[i].getName();
					
					String req_file = "Textfiles/" + listOfFiles[i].getName();
					String req_file2 = "Textfiles_resultsec/" + listOfFiles[i].getName();
					BufferedReader br = new BufferedReader(new FileReader(req_file));
					Writer positive_line_writter = new BufferedWriter(
							new OutputStreamWriter(new FileOutputStream(req_file2), "UTF-8"));

					String line = "";
					String line2 = "";
					boolean do_not_insert = true;
					while ((line = br.readLine()) != null) {
						// System.out.println("condition reading lines------" +
						// req_file2);
						// Decide weather the line should be part of file or
						// not
						//if (line.contains("::]")) 
						//{
							
							if ((
									///*DetectPattern.isBeforeAbstract(line) ||
									//DetectPattern.isBeforeIntro(line)
								// DetectPattern.isBeforeMTH(line)
									/// || DetectPattern.isBeforeBAK( line)
									//||DetectPattern.isBeforeRAD(line)
									 ///*||  DetectPattern.isIncideIntro(line)
						      // DetectPattern.isAfterIntro(line)
								DetectPattern.isIncideRAD(line)
					///*|| DetectPattern.isIncideBackground(line)
								///DetectPattern.isAfterCON(line)
									///* ||DetectPattern.isAfterRef(line)
								///	|| (DetectPattern.num_words(line.substring(line.indexOf("::]") + 3).trim()) 
									///		<= DetectPattern.min_word_threshold))
									)) {
							//	System.out.println("HERE:");
								//do_not_insert = true;
								do_not_insert = false;
								System.out.println("True lines:" +line);
								// System.out.println("condition for file
								// true------" + req_file2);
							} else {
								//do_not_insert = false;
								do_not_insert = true;
								// System.out.println("condition for file
								// false------" + req_file2);
							}
							if (!do_not_insert) // ||
												// line.contains("|41[::-::]")
												// ||
												// line.contains("|44[::-::]"))]
							{
								System.out.println("line:"+line);
								//*int index = line.indexOf("::]");
								//*String line_chunk = line.substring(0, index + 3);
								//*line = line.substring(index + 3);
								// line2 =
								// line.substring(line.indexOf("::]") +
								// 3).trim();
								// String
								// line_new=line.substring(line.indexOf("::]")
								// + 3).trim();
								line = line.replaceAll("\\W+", " ");
								// line2=line2.substring(line2.indexOf("::]")).trim();
								//*line = line_chunk + line;
								// System.out.println("line 2"+line2);
								// System.out.println("writing in file for
								// file------" + req_file2);
								positive_line_writter.write(line + "\n");
								positve_lines++;
							}

						}
						// positive_line_writter.flush();
						// positive_line_writter.close();
					//}
					// positive_line_writter.flush();
					// positive_line_writter.close();

					if (br != null) {
						try {
							br.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
					positive_line_writter.close();
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}
		// positive_line_writter.flush();
		// positive_line_writter.close();
		// negative_line_writter.flush();
		// negative_line_writter.close();
		// positive_line_writter.flush();
		// positive_line_writter.close();
		System.out.println("Positive Lines: " + positve_lines);
		System.out.println("Negative Lines: " + neg_lines);

	}
}
