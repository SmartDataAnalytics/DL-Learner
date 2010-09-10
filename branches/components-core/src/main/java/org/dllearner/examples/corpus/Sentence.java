package org.dllearner.examples.corpus;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dllearner.examples.Corpus;
import org.dllearner.utilities.URLencodeUTF8;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;

public class Sentence {
	int id ;
	OWLIndividual sentenceURI;
	List<String> sentence;
	List<String> wordsInOrder;
	List<String> urisInOrder;
	
	OWLClass element;
	OWLClass structElement;
	OWLClass wordElement;
	OWLClass sentenceClass;
	
	OWLClass tagClass;
	OWLClass morphClass;
	OWLClass edgeClass;
	
	OWLObjectProperty hasElement;
	
	public Sentence(int id, List<String> sentence) {
		super();
		this.id = id;
		this.sentence = sentence;
		this.sentenceURI = Corpus.factory.getOWLNamedIndividual(IRI.create(Corpus.namespace+"#"+"satz"+id));
	
		this.urisInOrder = new ArrayList<String>();
		this.wordsInOrder = new ArrayList<String>();
		
		element = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#Element"));
		structElement = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#StructureElement"));
		wordElement = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#WordElement"));
		sentenceClass = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#Sentence"));
		tagClass = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#Tag"));
		morphClass = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#Morph"));
		edgeClass = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#Edge"));
	
		hasElement = Corpus.factory.getOWLObjectProperty(IRI.create(Corpus.namespace+"#hasElement"));
		
		Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(sentenceClass, this.sentenceURI));
	}
	
	public void processSentence(){
		
		int pos=0;
		for (String line : sentence) {
			
			processLine(line,pos);
			pos++;
		}
	}
	
	
	public void processLine(String line, int pos){
		String elementURL = Corpus.namespace+"#";
		OWLNamedIndividual lineElement;
		StringTokenizer st = new StringTokenizer(line);
		
		//%String %% word			lemma			tag	morph		edge	parent	secedge comment
		String word = st.nextToken();
//		String lemma =  st.nextToken();
		String tag =  st.nextToken();
		String morph =  st.nextToken();
		String edge =  st.nextToken();
//		String parent =  st.nextToken();
		//word
		if(word.startsWith("#")){
			elementURL+="s_"+id+"_"+word.substring(1);
			lineElement = Corpus.factory.getOWLNamedIndividual(IRI.create(elementURL));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(structElement, lineElement));
			
		}else{
			elementURL+="s_"+id+"_"+pos+"_"+URLencodeUTF8.encode(word);
			wordsInOrder.add(word);
			urisInOrder.add(elementURL);
			lineElement = Corpus.factory.getOWLNamedIndividual(IRI.create(elementURL));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(wordElement, lineElement));
			Corpus.addAxiom(Corpus.factory.getOWLAnnotationAssertionAxiom(Corpus.factory.getRDFSComment(),
					lineElement.getIRI(), Corpus.factory.getOWLStringLiteral(line)));
			Corpus.addAxiom(Corpus.factory.getOWLAnnotationAssertionAxiom(Corpus.factory.getRDFSLabel(),
					lineElement.getIRI(), Corpus.factory.getOWLStringLiteral(word)));
		}
		
		Corpus.addAxiom(Corpus.factory.getOWLObjectPropertyAssertionAxiom(hasElement, sentenceURI, lineElement));
		
		//tag
		tag = (tag.equals("$("))?"SentenceBoundary":tag;
		//morph
		morph= "m_"+URLencodeUTF8.encode(morph);
		makeClasses(lineElement, tag,morph,edge);
		
	}
	
	void makeClasses(OWLIndividual lineElement, String tag, String morph, String edge){
		if(!tag.equals("--")){
			OWLClassExpression d = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#"+tag));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(d, lineElement));
			Corpus.addAxiom(Corpus.factory.getOWLSubClassOfAxiom(d, tagClass));
		}
		if(!morph.equals("m_--")){
			
			OWLClassExpression d = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#"+morph));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(d, lineElement));
			Corpus.addAxiom(Corpus.factory.getOWLSubClassOfAxiom(d, morphClass));
		}
		if(!edge.equals("--")){
			OWLClassExpression d = Corpus.factory.getOWLClass(IRI.create(Corpus.namespace+"#"+edge));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(d, lineElement));
			Corpus.addAxiom(Corpus.factory.getOWLSubClassOfAxiom(d, edgeClass));
		}
	}
}
