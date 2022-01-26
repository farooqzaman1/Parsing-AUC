package Util;

public class getSampleCitingPaper
{	String repodir = "/opt/data/repositories/rep1/10/1/1";
	static String outputdir = "../sample";
	public static void main(String[] args)
	{	int seedClusterID = 0;
		if(args.length == 1)
		{
			seedClusterID = Integer.parseInt(args[0]);
			String outdir= outputdir+seedClusterID;
			
		}
	}
}
