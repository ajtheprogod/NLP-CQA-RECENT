import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.cmu.lti.jawjaw.pobj.POS;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.lexical_db.data.Concept;
import edu.cmu.lti.ws4j.Relatedness;
import edu.cmu.lti.ws4j.RelatednessCalculator;
import edu.cmu.lti.ws4j.impl.HirstStOnge;
import edu.cmu.lti.ws4j.impl.JiangConrath;
import edu.cmu.lti.ws4j.impl.LeacockChodorow;
import edu.cmu.lti.ws4j.impl.Lesk;
import edu.cmu.lti.ws4j.impl.Lin;
import edu.cmu.lti.ws4j.impl.Path;
import edu.cmu.lti.ws4j.impl.Resnik;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;


public class QuestionAnswer {

	private static ILexicalDatabase db = new NictWordNet();
	
    private static RelatednessCalculator[] rcs = {
                    new HirstStOnge(db), new LeacockChodorow(db), new Lesk(db),  new WuPalmer(db),
                    new Resnik(db), new JiangConrath(db), new Lin(db), new Path(db)
                    };
    static RelatednessCalculator rc = new HirstStOnge(db);
	 
	   static List<POS[]> posPairs = rc.getPOSPairs();
	   static double maxScore = -1D;
	   static double BipartiteMaxScore=-1D;
	   static double MatchingAvgMaxScore=-1D;
	   static double DiceMaxScore=-1D;
    
		static HashMap<String,Integer> totalDocWordCount=new HashMap<String,Integer>();
		static HashMap<String,Double> IDF=new HashMap<String,Double>();
		static HashMap<String,Integer> stopwords=new HashMap<String,Integer>();
		static WordSimilarity WS=new WordSimilarity();
		static MaxWeightedBipartiteMatching Bipartite = new MaxWeightedBipartiteMatching();
	    static MatchingAverage MA=new MatchingAverage();
		private static void run( String word1, String word2 ) {
            WS4JConfiguration.getInstance().setMFS(true);
            for ( RelatednessCalculator rc : rcs ) {
                    double s = rc.calcRelatednessOfWords(word1, word2);
                    System.out.println( rc.getClass().getName()+"\t"+s );
            }
    }

		public static void main(String[] args) throws Exception {
			XmlParse xml=new XmlParse();
			StopWords SW = new StopWords();
			InvertedDocumentFrequency idf=new InvertedDocumentFrequency();
			PartOfSpeechTagger post = new PartOfSpeechTagger();
			CosineSimilarity cosSim=new CosineSimilarity();
			BuildSimilarityMatrix bsm=new BuildSimilarityMatrix();
			StemSentence stem=new StemSentence();
			int count =0;
			String result=null;
			int resultcount=0;
			String answercopy=null;
			String BipartiteAnswer=null;
			String MatchAvgAnswer=null;
			String DiceAnswer=null;
			int word_strt;
			double min=0.0;
			String query="If i look at porn but dont download any will it effect my computer?";
			String queryactual=query;
			
			for (File file : new File("/home/amal/Downloads/NewCategoryIdentification/xml").listFiles()) 
			{
		
		    String question =xml.GetQuestion(file);
		    String answer=xml.GetAnswer(file);
//		    System.out.println("question is "+question);
//		    System.out.println("answer is "+answer);
		   // question=stem.StemSent(question, stopwords);
		   // if(!answer.equals(""))
		   // answer=stem.StemSent(answer, stopwords);
		    
		    String[] ques = question.split("\\s+");
		    stopwords=SW.populateStopwords(ques,stopwords);
		    String[] ans= answer.split("\\s+");
		    stopwords=SW.populateStopwords(ques,stopwords);
		    count++;
			}
			query=stem.StemSent(query, stopwords);
			
			for (File file : new File("/home/amal/Downloads/NewCategoryIdentification/xml").listFiles()) 
			{
				HashMap<String,Integer> EachDocWordCount=new HashMap<String,Integer>();
				String question =xml.GetQuestion(file);
			    String answer=xml.GetAnswer(file);
			    question=stem.StemSent(question, stopwords);
			    if(!answer.equals(""))
			    answer=stem.StemSent(answer, stopwords);
			    String[] ques = question.split("\\s+");
			    String[] ans= answer.split("\\s+");
			    
			    
				for(word_strt=0;word_strt<ques.length;word_strt++)
		    	{
					if(stopwords.containsKey(ques[word_strt]))
		    	if(stopwords.get(ques[word_strt])<=2 || stopwords.get(ques[word_strt])>=60) continue;
		    		if(totalDocWordCount.containsKey(ques[word_strt]) && !EachDocWordCount.containsKey(ques[word_strt]) )
		    			totalDocWordCount.put(ques[word_strt],totalDocWordCount.get(ques[word_strt])+1);
		    		else if(!EachDocWordCount.containsKey(ques[word_strt]) )
		    			totalDocWordCount.put(ques[word_strt],1);
		    	EachDocWordCount.put(ques[word_strt],1);
		    	
		    	}
			}
			
			
			System.out.println("stage 1 over");
			
			IDF=idf.PopulateIDF(totalDocWordCount, count, IDF);
			
			for (File file : new File("/home/amal/Downloads/NewCategoryIdentification/xml").listFiles()) 
			{
				HashMap<String,Double> EachDocWordWeight=new HashMap<String,Double>();
				String question =xml.GetQuestion(file);
			    String answer=xml.GetAnswer(file);
			    answercopy=answer;
			    question=stem.StemSent(question, stopwords);
			    if(!answer.equals(""))
			    answer=stem.StemSent(answer, stopwords);
			    String[] ques = question.split("\\s+");
			    String[] ans= answer.split("\\s+");
//				
//				String res="";
//				
//				for(word_strt=0;word_strt<ques.length;word_strt++)
//		    	{
//					if(stopwords.containsKey(ques[word_strt]))
//		    		if(stopwords.get(ques[word_strt])<=2 || stopwords.get(ques[word_strt])>=60) continue;
//		    			res=res+new PorterStemmer().stripAffixes(ques[word_strt])+" ";
//		    	
//		    	}
//				
				
				for(word_strt=0;word_strt<ans.length;word_strt++)
		    	{
		    		if(stopwords.containsKey(ans[word_strt])) 
		    			if(stopwords.get(ans[word_strt])<=2 || stopwords.get(ans[word_strt])>=25) 
		    				continue;
		    		if(EachDocWordWeight.containsKey(ans[word_strt])  )
		    			EachDocWordWeight.put(ans[word_strt],EachDocWordWeight.get(ans[word_strt])+1);
		    		else if(!EachDocWordWeight.containsKey(ans[word_strt]) )
		    			EachDocWordWeight.put(ans[word_strt],1.0);
		    
		    	
		    	}
				double Dabs=cosSim.Dabsolute(EachDocWordWeight, stopwords, EachDocWordWeight);
				
			
				String[] query_words_stem=query.split("\\s+");
		    	String querystem=query;
				
				    	
				String[] query_words=querystem.split("\\s+");
				Double QdotD=cosSim.DinnerprodQ(query_words, EachDocWordWeight, stopwords, EachDocWordWeight);
				Double Qabs=cosSim.Qabsolute(query_words, EachDocWordWeight, stopwords, EachDocWordWeight);
				 
				 
				// if(QdotD>min)
				if(QdotD>0.2)
		    	    {
		    	    	min=QdotD;
		    	    System.out.println("cosine similarity is "+QdotD/(Qabs*Dabs));
		    	    System.out.println("resultcount is"+resultcount);
//		    	    System.out.println("query  is"+queryactual);
//		    	    System.out.println("question is"+question);
		    	    result=answercopy;
		    	//    System.out.println("result " + result);
		    	    System.out.println("================");
		    	    Double[][] SimMatrix=bsm.buildSimMatrix(question, query);
		    	    
		    	    
		    	    int m = SimMatrix.length;
		    	    int n=SimMatrix[0].length;
		    	    double BipartiteScore=Bipartite.getScore(SimMatrix,m,n);
		    	    if(BipartiteScore>0.0)
		    	    System.out.println("bipartite score is "+BipartiteScore);
		    	    if(BipartiteScore>BipartiteMaxScore)
		    	    {
		    	    	BipartiteMaxScore=BipartiteScore;
		    	    	BipartiteAnswer=answercopy;
		    	    }
		    	    if(BipartiteScore>0.0)
		    	    {
		    	    	Double MatchingScore=(Double) MA.GetScore(BipartiteScore, m, n);
		    	    	if(MatchingScore>MatchingAvgMaxScore)
		    	    	{
		    	    		MatchAvgAnswer=answercopy;
		    	    		MatchingAvgMaxScore=MatchingScore;
		    	    	}
		    	    }
		    	    if(BipartiteScore>0.0)
		    	    {
		    	    	Double DiceScore=(Double) (2.0*Bipartite.matches)/(m*n);
		    	    	if(DiceScore>DiceMaxScore)
		    	    	{
		    	    		DiceAnswer=answercopy;
		    	    		DiceMaxScore=DiceScore;
		    	    	}
		    	    }
		    	    
		    	    resultcount++;
		    	    if(BipartiteScore>0.0)
		    	    {
		    	    System.out.println("Biparite Score "+BipartiteMaxScore);
		    	    System.out.println("Matching avg Score "+MatchingAvgMaxScore);
		    	    System.out.println("Dice max Score "+DiceMaxScore);
		    	    System.out.println("answer for query -- "+answercopy);
		    	    
		    	    
		    	    }
		    	    }
			}
			
			
			//demo link
			System.out.println("Parse over");
			
			
			System.out.println("Biparite Score "+BipartiteMaxScore);
			System.out.println("answer for query -- "+BipartiteAnswer);
    	    System.out.println("Matching avg Score "+MatchingAvgMaxScore);
    	    System.out.println("answer for query -- "+MatchAvgAnswer);
    	    System.out.println("Dice max Score "+DiceMaxScore);
    	    System.out.println("answer for query -- "+DiceAnswer);
		
//			long t0 = System.currentTimeMillis();
//            run( "act","moderate" );
//            long t1 = System.currentTimeMillis();
//            System.out.println( "Done in "+(t1-t0)+" msec." );
            
            

            
//            for (File file : new File("/home/amal/Downloads/NewCategoryIdentification/xml").listFiles()) 
//			{
//		
//		    String question =xml.GetQuestion(file);
//		    String answer=xml.GetAnswer(file);
//		 //   Double[][] SimMatrix=bsm.buildSimMatrix(question, query);
//
//			}


            
//            String word1="load";
//            String word2="how";
//            
//            
//            for(POS[] posPair: posPairs) {
//                List<Concept> synsets1 = (List<Concept>)db.getAllConcepts(word1, posPair[0].toString());
//                List<Concept> synsets2 = (List<Concept>)db.getAllConcepts(word2, posPair[1].toString());
//
//                for(Concept synset1: synsets1) {
//                    for (Concept synset2: synsets2) {
//                        Relatedness relatedness = rc.calcRelatednessOfSynset(synset1, synset2);
//                        double score = relatedness.getScore();
//                        if (score > maxScore) { 
//                            maxScore = score;
//                        }
//                    }
//                }
//            }
//
//            if (maxScore == -1D) {
//                maxScore = 0.0;
//            }
//            System.out.println("sim('" + word1 + "', '" + word2 + "') =  " + maxScore);
//          maxScore=  WS.ComputeWordSimilarity(word1, word2);
//            System.out.println("sim('" + word1 + "', '" + word2 + "') =  " + maxScore);

//			String a = "I like watching movies";
//			String taggedSentence =post.tagSentence(a);
//			System.out.println(taggedSentence);
	}
		
		
}