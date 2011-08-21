/**
 * Copyright (C) 2007-2011, Jens Lehmann
 *
 * This file is part of DL-Learner.
 *
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


package org.dllearner.algorithms.isle;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLNamedObject;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;


public class Relevances {

	private LuceneSearcher m_searcher = null;
	
	private OWLOntologyManager m_manager;
	private OWLOntology m_ontology;
	
	private Set<OWLEntity> m_entities;
	private Set<OWLClass> m_classes;
	
	
	public static void main( String args[] ) throws Exception {
		Relevances relevances = new Relevances( args[0] );
		relevances.printScores();
	}
	
	public void printScores() throws Exception {
		for( OWLClass c: m_classes )
		{
			Map<OWLEntity,Double> hmEntity2Score = getEntityRelevance(c);
			// normalization per class?
			hmEntity2Score = normalize( hmEntity2Score );
			for( OWLEntity e : hmEntity2Score.keySet() )
			{
				double dScore = hmEntity2Score.get(e);
				System.out.println( "P( "+ getLabel(c) +", "+ getLabel(e) +" ) = "+ dScore );
			}
		}		
		m_searcher.close();
	}

	public Relevances( String sOntologyURI ) throws Exception {
		m_searcher = new LuceneSearcher();
		loadOntology( sOntologyURI );
	}
	
	public Map<OWLEntity,Double> normalize( Map<OWLEntity,Double> hmEntity2Score ){
		Map<OWLEntity,Double> hmEntity2Norm = new HashMap<OWLEntity,Double>();
		double dMin = Double.MAX_VALUE;
		Double dMax = Double.MIN_VALUE;
		for( OWLEntity e : hmEntity2Score.keySet() )
		{
			double dValue = hmEntity2Score.get(e);
			if( dValue < dMin ){
				dMin = dValue;
			}
			else if( dValue > dMax ){
				dMax = dValue;
			}
		}
		// System.out.println( "min="+ dMin +" max="+ dMax );
		for( OWLEntity e : hmEntity2Score.keySet() )
		{
			double dValue = hmEntity2Score.get(e);
			double dNorm = 0;
			if( dMin == dMax ){
				dNorm = dValue;
			} 
			else {
				dNorm = ( dValue - dMin ) / ( dMax - dMin );
			}
			hmEntity2Norm.put( e, dNorm );
		}
		return hmEntity2Norm;
	}
	
	public Map<OWLEntity,Double> getEntityRelevance( OWLClass c ) throws Exception {
		// computes relevance of entity for this class
		// conditional probability: P(C,E)=f(C,E)/f(E)
		// PMI(C,E)=log( P(C,E) / P(C) )
		Map<OWLEntity,Double> hmEntity2Score = new HashMap<OWLEntity,Double>();
		String sClass = getLabel(c);
		int iClass = m_searcher.count( sClass );
		int iAll = m_searcher.indexSize();
		double dPClass = (double) iClass / (double) iAll;
		for( OWLEntity e: m_entities )
		{
			String sEntity = getLabel(e);
			int iEntity = m_searcher.count( sEntity );
			int iEntityClass = m_searcher.count( sClass +" AND "+ sEntity );
//			double dPEntity = (double)iEntity / (double)iAll;
			double dPClassEntity = (double) iEntityClass / (double)iEntity;
			double dPMI = Math.log( dPClassEntity / dPClass );
			if( !Double.isNaN( dPMI ) && !Double.isInfinite( dPMI ) ){
				hmEntity2Score.put( e, dPMI );
			}
		}
		return hmEntity2Score;
	}
	
	/* private String getLabel( OWLEntity e ){
		System.out.println( "getLabel: "+ e );
		OWLDataFactory factory = m_manager.getOWLDataFactory();
		OWLAnnotationProperty label = factory.getOWLAnnotationProperty( OWLRDFVocabulary.RDFS_LABEL.getIRI() );
		Set<OWLAnnotation> anns = e.getAnnotations( m_ontology, label );
		for( OWLAnnotation annotation: anns )
		{
			System.out.println( "annotation="+ annotation );
			if( annotation.getValue() instanceof OWLLiteral ) 
			{
				OWLLiteral val = (OWLLiteral) annotation.getValue();
				if( !val.isOWLTypedLiteral() ){
					if (val.asOWLStringLiteral().getLang().equals("en")) {
						return val.getLiteral();
					}
				}
				return val.getLiteral();
			}
		}		
		return null;
	} */
	
	private String getLabel( OWLEntity e ){
		if( e instanceof OWLNamedObject ){
			String sIRI = ((OWLNamedObject)e).getIRI().toString();
			return sIRI.substring( sIRI.indexOf( "#" )+1 );
		}
		return null;
	}
	
	private void loadOntology( String sOntologyURI ) throws Exception {
		m_manager = OWLManager.createOWLOntologyManager();
		IRI ontologyIRI = IRI.create( sOntologyURI );
		m_ontology = m_manager.loadOntology( ontologyIRI );
		m_classes = m_ontology.getClassesInSignature();
		m_entities = m_ontology.getSignature();
		System.out.println( "classes="+ m_classes.size() +" entities="+ m_entities.size() );
		// m_manager.removeOntology( ontology );
	}
}