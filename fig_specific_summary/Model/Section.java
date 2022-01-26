package Model;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.Range;

//place holder for a section

public class Section {
	
	public Set<DocumentNode.StdSection> sectionTypes = new HashSet<DocumentNode.StdSection>(); 
	public Range<Integer> range;
	public String sectionName; 
	
	public Section(String _name)
	{
		sectionName = _name;
	}
	
	
	public String toString()
	{
		StringBuilder str = new StringBuilder(); 
		str.append(range.toString()+"[");
		for(DocumentNode.StdSection s: sectionTypes)
		{
			str.append(s.name()+" ");
		}
		str.append("] "+sectionName);
		
		return str.toString();
	}
	
}
