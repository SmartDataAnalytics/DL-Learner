package org.dllearner.algorithms.isle;


import java.io.*;
import java.util.*;

public class PMIRelevance {

	private LuceneSearcher m_searcher = null;
	
	private Set<String> m_classes;
	private Set<String> m_individuals;
	
	
	public static void main( String args[] ) throws Exception {
		PMIRelevance relevance = new PMIRelevance( args[0], args[1] );
		relevance.printScores();
	}
	
	public void printScores() throws Exception {
		for( String sInd: m_individuals )
		{
			Map<String,Double> hmClass2Score = getClassRelevance( sInd );
			for( String sClass : hmClass2Score.keySet() )
			{
				double dScore = hmClass2Score.get( sClass );
				if( dScore > 0 ){
					System.out.println( "PMI( "+ sInd +" , "+ sClass +" ) = "+ dScore );
				}
			}
		}
		/* for( String sClass: m_classes )
		{
			Map<String,Double> hmInd2Score = getIndividualRelevance( sClass );
			for( String sInd : hmInd2Score.keySet() )
			{
				double dScore = hmInd2Score.get( sInd );
				if( dScore > 0 ){
					System.out.println( "P( "+ sClass +" | "+ sInd +" ) = "+ dScore );
				}
			}
		} */		
		m_searcher.close();
	}

	public PMIRelevance( String sClasses, String sIndividuals ) throws Exception {
		m_searcher = new LuceneSearcher();
		m_classes = read( sClasses );
		m_individuals = read( sIndividuals );
	}

	public Map<String,Double> getClassRelevance( String sIndividual ) throws Exception {
		// computes relevance of classes for this individual
		// conditional probability: P(I|C)=f(I,C)/f(C)
		// PMI(I,C)=log( P(I|C) / P(I) )
		Map<String,Double> hmClass2Score = new HashMap<String,Double>();
		int iInd = m_searcher.count( sIndividual );
		int iAll = m_searcher.indexSize();
		double dPInd = (double) iInd / (double) iAll; 
		for( String sClass: m_classes )
		{
			int iClass = m_searcher.count( sClass );
			int iIndClass = m_searcher.count( sIndividual +" AND "+ sClass );
			double dPIndClass = (double) iIndClass / (double)iClass;
			double dPMI = Math.log( dPIndClass / dPInd ); 
			hmClass2Score.put( sClass, dPMI );
		}
		return hmClass2Score;
	}
	
	public Map<String,Double> getIndividualRelevance( String sClass ) throws Exception {
		// computes relevance of individuals for this class
		// conditional probability: P(C|I)=f(C,I)/f(I)
		// PMI(C|I)=log( P(C|I) / P(C) )
		Map<String,Double> hmInd2Score = new HashMap<String,Double>();
		int iClass = m_searcher.count( sClass );
		int iAll = m_searcher.indexSize();
		double dPClass = (double) iClass / (double) iAll;
		for( String sInd: m_individuals )
		{
			int iInd = m_searcher.count( sInd );
			int iIndClass = m_searcher.count( sClass +" AND "+ sInd );
			double dPClassInd = (double) iIndClass / (double)iInd;
			double dPMI = Math.log( dPClassInd / dPClass );
			hmInd2Score.put( sInd, dPMI );
		}
		return hmInd2Score;
	}
	
	private static Set<String> read( String sFile ) throws Exception {
		File file = new File( sFile );
		Set<String> lines = new HashSet<String>();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader( new FileReader( file ) );
			String sLine = null;
			while( ( sLine = reader.readLine() ) != null ) {
				lines.add( sLine.trim() );
			}
		}
		finally {
			if( reader != null ) {
				reader.close();
			}
		}
		return lines;
	}
}