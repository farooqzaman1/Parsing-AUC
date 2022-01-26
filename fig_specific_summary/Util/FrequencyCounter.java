package Util;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

/**
 * This class handle frequency count and stuff
 * @author aum
 *
 */
public class FrequencyCounter<T>
{
	Vector<ValuePair<T>> vals = new Vector<ValuePair<T>>();
	
	public Vector<T> getTopKElements(int k)
	{
		Collections.sort(vals);
		
		Vector<T> result = new Vector<T>();
		
		for(int i = 0; i < k && i < vals.size(); i++)
		{
			result.add(vals.elementAt(i).value);
		}
		return result;
	}
	
	public Vector<T> getBottomKElements(int k)
	{
		
		Collections.sort(vals);	//low -> high, need to reverse
		Collections.reverse(vals);
		Vector<T> result = new Vector<T>();
		
		for(int i = 0; i < k && i < vals.size(); i++)
		{
			result.add(vals.elementAt(i).value);
		}
		return result;
	}
	
	public void remove(T el)
	{
		for(int i = 0; i < vals.size(); i++)
		{
			if(vals.elementAt(i).equals(el))
			{
				vals.remove(i);
				i--;
			}
		}
		
	}
	
	/**
	 * Add the item wth specifi amount
	 * @param el
	 * @param count
	 */
	public void add(T el, int count)
	{
		for(ValuePair<T> val: vals)
		{
			if(val.equals(el))
			{
				val.addFreq(count);
				return;
			}
		}
		
		//new element
		vals.add(new ValuePair<T>(el, count));
	}
	
	public void add(T el)
	{
		add(el, 1);
	}
	
	public Vector<ValuePair<T>> getSortedRawValues()
	{
		Collections.sort(vals);
		return vals;
	}
	
	/**
	 * 
	 * @return Sum of freqeuncy counts
	 */
	public int getSumFreq()
	{
		int result = 0;
		
		for(ValuePair<T> val: vals)
		{
			result += val.getFreq();
		}
		
		return result;
	}
	
	public boolean isEmpty()
	{
		return this.getSumFreq() == 0;
	}
	
	public static void main(String[] args)
	{
		FrequencyCounter<String> f = new FrequencyCounter<String>();
		
		f.add("10.1");
		f.add("10");
		f.add("10.2");
		f.add("10.1");
		f.add("10.1");
		f.add("10");
		
		Util.printVector(f.getTopKElements(2));
	}
}


