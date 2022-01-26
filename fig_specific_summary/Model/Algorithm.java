package Model;


import java.util.Vector;


public class Algorithm {
	Vector<String> names = new Vector<String>();
	private Vector<AlgorithmRepresentation> reps = new Vector<AlgorithmRepresentation>();
	public void setReps(Vector<AlgorithmRepresentation> reps) {
		this.reps = reps;
	}
	public Vector<AlgorithmRepresentation> getReps() {
		return reps;
	} 
	
	public String toString()
	{
		String result = "";
		for(AlgorithmRepresentation.RepType type:AlgorithmRepresentation.RepType.values())
		{	int count = 0;
			for(AlgorithmRepresentation ar: reps)
			{	String temp = "";
				if(ar.repType == type)
				{	count++;
					temp += "["+type.toString()+"]"+ar.toString();
				}
				
				if(count > 0) result += temp;
			}
		}
		
		return result;
	}
}
