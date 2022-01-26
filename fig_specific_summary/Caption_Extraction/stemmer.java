package Caption_Extraction;

 import java.io.*;
 import java.util.*;

 public class stemmer
 {

    private Hashtable irregular_words = new Hashtable(1500);
    private DataInputStream input;
    private PorterStemmer ptlem = new PorterStemmer();

   //***Constructor
    public stemmer() {
       loadData("irregular_words.txt");
    }

   //***Interface
    public String getStem(String token) {
       String lemtok = (String)irregular_words.get(token.toLowerCase());
       if(lemtok != null)
          return lemtok;
       else {
          lemtok=ptlem.stem(token.toLowerCase());
          if(lemtok != "Invalid term")
             return lemtok;
          else
             return token.toLowerCase();
       }
    }
 
    //***Workshop
    private void loadData(String fname){
       try {
          input = new DataInputStream(new FileInputStream(fname));
       }
          catch (IOException e) {
            // System.out.println("Error opening file \"" + fname + "\"");
             return;
          }
       try{
          String var = new String();
          int i = 0;
          while((var = input.readLine()) != null) {
             StringTokenizer st = new StringTokenizer(var, " \n\t");
             irregular_words.put(st.nextToken(), st.nextToken());
          }
       }
          catch (IOException e) {
            // System.out.println("Error reading file \"" + fname + "\"");
             return;
          }
    }
 }
