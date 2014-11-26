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
 
    
		static HashMap<String,Integer> totalDocWordCount=new HashMap<String,Integer>();
		static HashMap<String,Double> IDF=new HashMap<String,Double>();
		static HashMap<String,Integer> stopwords=new HashMap<String,Integer>();
		
	
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
			int count =0;
			String result=null;
			String answercopy=null;
			int word_strt;
			double min=0.0;
			String query="Is there anyway I could load faster the page I vist so many times, in Firefox?";
			
			for (File file : new File("/home/amal/Downloads/NewCategoryIdentification/xml").listFiles()) 
			{
		
		    String question =xml.GetQuestion(file);
		    String answer=xml.GetAnswer(file);
//		    System.out.println("question is "+question);
//		    System.out.println("answer is "+answer);
		    String[] ques = question.split("\\s+");
		    stopwords=SW.populateStopwords(ques,stopwords);
		    String[] ans= answer.split("\\s+");
		    stopwords=SW.populateStopwords(ques,stopwords);
		    count++;
			}
			
			
			for (File file : new File("/home/amal/Downloads/NewCategoryIdentification/xml").listFiles()) 
			{
				HashMap<String,Integer> EachDocWordCount=new HashMap<String,Integer>();
				String question =xml.GetQuestion(file);
			    String answer=xml.GetAnswer(file);
			    String[] ques = question.split("\\s+");
			    String[] ans= answer.split("\\s+");
			    
			    
				for(word_strt=0;word_strt<ques.length;word_strt++)
		    	{
		    	if(stopwords.get(ques[word_strt])<=2 || stopwords.get(ques[word_strt])>=40) continue;
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
			    String[] ques = question.split("\\s+");
			    String[] ans= answer.split("\\s+");
				answercopy=answer;
				String res="";
				
				for(word_strt=0;word_strt<ques.length;word_strt++)
		    	{
		    		if(stopwords.get(ques[word_strt])<=2 || stopwords.get(ques[word_strt])>=40) continue;
		    			res=res+new PorterStemmer().stripAffixes(ques[word_strt])+" ";
		    	
		    	}
				
				
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
		    	String querystem="";
				for(word_strt=0;word_strt<query_words_stem.length;word_strt++)	
				    querystem=querystem+new PorterStemmer().stripAffixes(query_words_stem[word_strt])+" ";
				    	
				String[] query_words=querystem.split("\\s+");
				Double QdotD=cosSim.DinnerprodQ(query_words, EachDocWordWeight, stopwords, EachDocWordWeight);
				Double Qabs=cosSim.Qabsolute(query_words, EachDocWordWeight, stopwords, EachDocWordWeight);
				 
				 
				 if(QdotD>min)
		    	    {
		    	    	min=QdotD;
		    	    System.out.println("similarity is "+QdotD/(Qabs*Dabs));
		    	    result=answercopy;
		    	    System.out.println("result " + result);
		    	    System.out.println("================");
		    	    }
			}
			
			
			//demo link
			System.out.println("post tagging");
			long t0 = System.currentTimeMillis();
            run( "act","moderate" );
            long t1 = System.currentTimeMillis();
            System.out.println( "Done in "+(t1-t0)+" msec." );
            
            

            
            for (File file : new File("/home/amal/Downloads/NewCategoryIdentification/xml").listFiles()) 
			{
		
		    String question =xml.GetQuestion(file);
		    String answer=xml.GetAnswer(file);
		    Double[][] SimMatrix=bsm.buildSimMatrix(question, query);

			}



//			String a = "I like watching movies";
//			String taggedSentence =post.tagSentence(a);
//			System.out.println(taggedSentence);
	}
		
		
}