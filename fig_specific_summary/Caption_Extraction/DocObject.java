package Caption_Extraction;

public class DocObject implements Comparable<DocObject> { 
	 private String name; 
	 private double score; 
	 private double length; 
	 private String content; 
	 private String[] contentArray; 
	 
	 public DocObject(String name) { 
	  this.name = name; 
	  this.score = 0.0; 
	  this.length = 0.0; 
	 } 
	 
	 public double getLength() { 
	  return this.length; 
	 } 
	 
	 public void setLength(double len) { 
	  this.length = len; 
	 } 
	 
	 public String getName() { 
	  return name; 
	 } 
	 
	 public void setName(String name) { 
	  this.name = name; 
	 } 
	 
	 public double getScore() { 
	  return score; 
	 } 
	 
	 public void setScore(double score) { 
	  this.score = score; 
	 } 
	 
	 @Override 
	 public int compareTo(DocObject o) { 
	  return (int) (o.getScore() - this.getScore()); 
	 } 
	 
	 public String getContent() { 
	  return content; 
	 } 
	 
	 public void setContent(String content) { 
	  this.content = content.toLowerCase(); 
	  this.contentArray = this.content.split(" "); 
	 } 
	  
	 public String[] getContentArray(){ 
	  return this.contentArray; 
	 } 
	}
