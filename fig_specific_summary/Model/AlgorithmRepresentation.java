package Model;
import java.util.Vector;



public class AlgorithmRepresentation {
	//types of algorithm representation
	public static enum RepType{TEXT, STEP, PSEUDO, DIAGRAM, UNKNOWN}
	
	public RepType repType = RepType.UNKNOWN;
	
	//pseudo code metadata
	private DocumentElement pseudo = null;
	
	//step algorithm metadata
	private String stepIndicationSentnece = null;
	private Vector<String> steps = new Vector<String>();
	private DocumentNode stepSection = null;
	
	//section algorithm metadata
	private DocumentNode sectSection =  null;
	private String sectIndicationSentence = null;
	//modification
	public String captionText="";
	public String RedSentenceText="";
	public String SecText="";
	
	public String toString()
	{	String result = "";
		if(repType == RepType.STEP)
		{	result	+= ">> SEC: "+getStepSection().getRawSectName()+"\n";
			result += "["+getStepIndicationSentnece()+"]\n";
			for(String step: getSteps())
			{
				result += "\t"+step+"\n";
			}
			result += "-------------------\n";
			//result += stepSection.text+"\n";
		}
		else if(repType == RepType.TEXT)
		{
			result += ">> SEC: "+getSectSection().getRawSectName()+"\n";
			result += "\t["+getSectIndicationSentence()+"\n----------------\n";
			
			for(int i = 0; i < sectSection.getSentences().size() && i < 10; i++)
			{
				result += "[>> "+ sectSection.getSentences().elementAt(i)+"\n";
			}
		}
		else if(repType == RepType.PSEUDO)
		{
			result += getPseudo().display();
			// modification
			this.captionText=getPseudo().caption;
			this.RedSentenceText=getPseudo().Ref_All_sentences;
			this.SecText=getPseudo().bestRefSect.getRawSectName();
			setCaptionText(getPseudo().caption);
			setRefTextSentence(getPseudo().Ref_All_sentences);
			System.out.println("##############caption from: "+this.captionText);
			System.out.println("##############ref from: "+this.RedSentenceText);
			System.out.println("##############SEC from: "+this.SecText);			
		}
		
		return result;
	}


	public void setCaptionText(String Caption)
	{
		this.captionText=Caption;
	}
	public void setSecText(String sec)
	{
		this.SecText=sec;
	}
	public void setRefTextSentence(String RefTextSentence)
	{
		this.RedSentenceText=RefTextSentence;
	}
	public String getCaptionText()
	{
		return captionText;
	}
	public String getSecText()
	{
		return SecText;
	}
	public String getRefTextSentence()
	{
		return RedSentenceText;
	}
	public void setSectSection(DocumentNode sectSection) {
		this.sectSection = sectSection;
	}


	public DocumentNode getSectSection() {
		return sectSection;
	}


	public void setStepSection(DocumentNode stepSection) {
		this.stepSection = stepSection;
	}


	public DocumentNode getStepSection() {
		return stepSection;
	}


	public void setPseudo(DocumentElement pseudo) {
		this.pseudo = pseudo;
	}

	

	public DocumentElement getPseudo() {
		return pseudo;
	}


	public void setStepIndicationSentnece(String stepIndicationSentnece) {
		this.stepIndicationSentnece = stepIndicationSentnece;
	}


	public String getStepIndicationSentnece() {
		return stepIndicationSentnece;
	}


	public void setSteps(Vector<String> steps) {
		this.steps = steps;
	}


	public Vector<String> getSteps() {
		return steps;
	}


	public void setSectIndicationSentence(String sectIndicationSentence) {
		this.sectIndicationSentence = sectIndicationSentence;
	}


	public String getSectIndicationSentence() {
		return sectIndicationSentence;
	}
}
