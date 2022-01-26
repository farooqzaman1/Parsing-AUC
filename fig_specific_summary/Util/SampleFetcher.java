package Util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Vector;

import PdfSegmentation.DocumentSegmentator;



public class SampleFetcher {
	public static void getSampleAlgorithmCitingPapers(int numSamplePapers, String outDir)
	{	ConfigReader config = new ConfigReader();
		try
		{
			Random generator = new Random();
			String repoPath = config.getValue("repo_path");
			int numSubdirs = Integer.parseInt(config.getValue("num_subdirs"));
			int count = 0;
			HashSet<String> sampleIDs = new HashSet<String>();
			while(count < numSamplePapers)
			{	//randomly select subdir
				int randSubdir = generator.nextInt(numSubdirs) + 1;
				String subdirPath = repoPath +"/"+randSubdir;
				
				//list all file dir
				File f = new File(subdirPath);
				String[] fds = f.list();
				
				Vector<String> fileDirs = new Vector<String>();
				
				for(String fileDir:fds)
				{
					if(!fileDir.startsWith("\\."))
					{
						fileDirs.add(fileDir);
					}
				}
				int randFileIndex = generator.nextInt(fileDirs.size());
				
				//randomly select file
				//Vector<String> textFiles = Directory.listAllFiles(subdirPath, ".body", -1);
				String fileDirPath = subdirPath+"/"+fileDirs.elementAt(randFileIndex);
				Vector<String> components = Directory.listAllFiles(fileDirPath, ".body", -1);
				if(components.isEmpty()) continue;
				String bodyFilename = components.elementAt(0);
				
				
				
				
				//see if the selected file has algorithm citing sentences
				String text = DocumentUtil.readText(bodyFilename);
				Util.jout("@@@ Reading file: "+bodyFilename+"\n");
				SentenceProducer sp = new SentenceProducer(DocumentSegmentator.reformat(text), 2);
				
				String sentence = null;
				boolean hasAlgoCitation = false;
				while((sentence = sp.nextSentence()) != null)
				{
					if(sentence.matches(CitationUtil.CITATION_SENTENCE_PATTERN))
					{	
						//check if this citation sentence cites an algorithm
						sentence = sentence.toLowerCase();
						for(String keyword: CitationUtil.ALGORITHM_KEYWORDS)
						{
							if(sentence.contains(keyword))
							{
								hasAlgoCitation = true;
								Util.jout("@@@ "+sentence+"\n");
								break;
							}
						}
						
						if(hasAlgoCitation) break;
					}
				}
				
				if(hasAlgoCitation)
				{
					//get id and build the path
					String docID = Directory.getFileID(bodyFilename);
					if(!sampleIDs.add(docID))
					{
						continue;
					}
					
					//String fileDirPath = subdirPath+"/"+docID;
					String cmd = "cp -R "+fileDirPath+" "+outDir;
					CommandExecutor.exec(cmd, true);
					cmd = "mv "+outDir+"/"+fileDirs.elementAt(randFileIndex)+" "+outDir+"/"+docID;
					CommandExecutor.exec(cmd, true);
					count++;
					Util.jout(""+count+"\n");
				}
				
			}
			
			
		}catch(Exception e)
		{
			e.printStackTrace();
			Util.errlog(e.toString());
		}
	}
	
	public static void getSampleCitationContext(int numSamplePapers, String repoPath, String outFilename)
	{
		try
		{
			BufferedWriter writer = new BufferedWriter(new FileWriter(outFilename));
			LocalDBConnector localdb = new LocalDBConnector();
			String query = "SELECT nodea, use_paper_id FROM cooccurrence_graph ORDER BY RANDOM() LIMIT "+numSamplePapers+";";
			ResultSet r = localdb.executeQuery(query);
			CsxDBConnector csxdb = new CsxDBConnector();
			Random generator = new Random();
			while(r.next())
			{
				String citedCluster = r.getString("nodea").split("#")[1];
				String[] tokens = r.getString("use_paper_id").split(":");
				String citingPaperID = tokens[generator.nextInt(tokens.length)];
				String citeText = null;
				String citeSymbol = null;
				String paperTitle = null;
				//get citeText
				query = "SELECT raw FROM citations WHERE cluster = "+citedCluster+" AND paperid = '"+citingPaperID+"';";
				ResultSet rr = csxdb.executeQuery(query);
				while(rr.next())
				{
					citeText = rr.getString("raw");
					break;
				}
				
				assert(citeText != null);
				
				//get citeSymbol
				String subdir = citingPaperID.split("\\.")[3];
				String docdir = citingPaperID.split("\\.")[4];
				String citeFile = repoPath+"/"+subdir+"/"+docdir+"/"+citingPaperID+".cite";
				String bodyFile = repoPath+"/"+subdir+"/"+docdir+"/"+citingPaperID+".body";
				
				citeSymbol = CitationUtil.getCiteSymbol(citeText, citeFile);
				Util.jout("@@@ CiteSymbol: "+citeSymbol+"\n");
				if(citeSymbol == null || citeSymbol.isEmpty())
				//no citation -- cant get citation contexts
				{	
					continue;
				}
				//assume for now that citeSymbol is in the form of [x]
				Vector<Vector<String>> contexts = CitationUtil.getCitationContext(citeSymbol, bodyFile, 2, 4);
				paperTitle = DocumentUtil.getPaperTitle(citingPaperID, localdb, csxdb);
				//writing output
				writer.write("[PAPER:"+citingPaperID+"]"+paperTitle+"\n");
				writer.write("[CITEATION:"+citeSymbol+"]"+citeText+"\n\n");
				
				for(Vector<String> context: contexts)
				{	writer.write("$$$\n");
					for(String s: context)
					{
						writer.write(s.replace(citeSymbol, "*****"+citeSymbol+"*****")+"||");
					}
					writer.write("\n\n");
				}
				writer.write("--------------------------------------------------------------\n");
				
			}
			
			csxdb.close();
			localdb.close();
			writer.close();
		}catch(Exception e)
		{
			e.printStackTrace();
			Util.errlog(e.toString());
		}
	}
	
	public static void main(String[] args)
	{
		if(args[0].equals("gencitationcontextsample"))
		{
			getSampleCitationContext(Integer.parseInt(args[1]), args[2], args[3]);
		}
		else if(args[0].equals("fetchsamplefiles"))
		{
			getSampleAlgorithmCitingPapers(Integer.parseInt(args[1]), args[2]);
		}
		
	}
}
