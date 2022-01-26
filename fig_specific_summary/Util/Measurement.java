package Util;

public class Measurement
{
	int tp = 0;
	int tn = 0;
	int fp = 0;
	int fn = 0;
	
	public Measurement(int _tp, int _tn, int _fp, int _fn)
	{
		tp = _tp;tn = _tn;fp = _fp; fn = _fn;
	}
	
	public double getPrecision()
	{	if(tp + fp == 0) return 0;
		return (double)tp/(double)(tp + fp);
	}
	
	public double getRecall()
	{	if(tp+fn == 0) return 0;
		return (double)tp/(double)(tp+fn);
	}
	
	public double getF1()
	{
		double precision = this.getPrecision();
		double recall = this.getRecall();
		return 2*precision*recall/(precision+recall);
	}
	
	public double getAccuracy()
	{
		return (double)(tp+tn)/(double)(tp+tn+fp+fn);
	}
	
	public int getNumInstances()
	{
		return tp + tn+ fp+fn;
	}
	
	@Override
	public String toString()
	{
		String result = "----------------\n";
		result += "1\t\t0\t\t<-classified as\n"
			+ tp+"|"+fn+"\t\t\t1\n"
			+ fp+"|"+tn+"\t\t0\n"
			+ "---------------\n";
		
		result += "Prec:"+this.getPrecision()+"|Recall:"+this.getRecall()+"|F1:"+this.getF1()+"|Acc:"+this.getAccuracy()+"\n------------------------\n";
		
		return result;
	}
}