package Util;

public class ValuePair<T> implements Comparable<ValuePair<T>>
{
	T value = null;
	int count = 0;
	
	public ValuePair(T _value, int _count)
	{
		value = _value;
		count = _count;
	}
	
	public T getValue()
	{
		return value;
	}
	
	@Override
	public int compareTo(ValuePair<T> o) {
		return o.count - this.count;
	}
	
	@Override
	public int hashCode()
	{
		return value.hashCode();
	}
	
	@Override
	public boolean equals(Object o)
	{
		return value.equals(o);
	}
	
	public void addFreq(int amount)
	{
		count += amount;
	}
	
	public void plusOne()
	{
		count++;
	}
	
	public int getFreq()
	{
		return count;
	}
	
	
	@Override
	public String toString()
	{
		return "["+value.toString() + ":" +count+"]";
	}
}