package Caption_Extraction;

import java.io.File; 
import java.nio.file.Files; 
import java.nio.file.Paths; 
import java.util.ArrayList; 
import java.util.Arrays; 
import java.util.List; 
 
public class DocParser { 
 
 public ArrayList<DocObject> getDocList(String path) { 
  ArrayList<DocObject> docList = new ArrayList<>(); 
  File[] files = new File(path).listFiles(); 
  for (File file : files) { 
   try { 
	   System.out.println("In getDocList");
    String content = new String(Files.readAllBytes(Paths.get(file 
      .getAbsolutePath()))); 
    content = content.replaceAll("[,]", ""); 
    content = content.replaceAll("[^a-zA-Z0-9']", "-"); 
    content = content.replaceAll("-", " "); 
    String[] words = content.split(" "); 
 
    List<String> list = new ArrayList<String>(Arrays.asList(words)); 
    list.removeAll(Arrays.asList(null, "")); 
 
    content = ""; 
    int i = 0; 
    for (i = 0; i < list.size() - 1; i++) { 
     content += (list.get(i).toLowerCase() + " "); 
    } 
    content += list.get(i); 
 
    words = content.split(" "); 
     
    DocObject doc = new DocObject(file.getName()); 
    doc.setLength(words.length); 
    doc.setContent(content); 
     
    docList.add(doc); 
     
   } catch (Exception e) { 
    e.printStackTrace(); 
   } 
  } 
   
  return docList; 
 } 
 
}
