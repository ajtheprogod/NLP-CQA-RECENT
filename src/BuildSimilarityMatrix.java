
public class BuildSimilarityMatrix {

	
	public Double[][] buildSimMatrix(String question,String query)
	
	{
	
		WordSimilarity WS=new WordSimilarity();
		  String[] ques = question.split("\\s+");
		    String[] quer= query.split("\\s+");
		    Double[][] SimMat=new Double[quer.length][ques.length];
		    int i,j;
		    for(i=0;i<quer.length;i++)
		    {
		    	for(j=0;j<ques.length;j++)
		    	{
		    		SimMat[i][j]=WS.ComputeWordSimilarity(quer[i], ques[j]);
		    		System.out.println("word similar for "+quer[i] +" and "+ ques[j]+" = "+SimMat[i][j]);
		    	}
		    }
		    
		 
		    
		    
		    return SimMat;
	}
}
