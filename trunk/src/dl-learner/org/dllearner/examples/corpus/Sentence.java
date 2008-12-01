package org.dllearner.examples.corpus;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.dllearner.examples.Corpus;
import org.dllearner.utilities.URLencodeUTF8;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLClassAssertionAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLObjectProperty;

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
		this.sentenceURI = Corpus.factory.getOWLIndividual(URI.create(Corpus.namespace+"#"+"satz"+id));
	
		this.urisInOrder = new ArrayList<String>();
		this.wordsInOrder = new ArrayList<String>();
		
		element = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#Element"));
		structElement = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#StructureElement"));
		wordElement = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#WordElement"));
		sentenceClass = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#Sentence"));
		tagClass = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#Tag"));
		morphClass = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#Morph"));
		edgeClass = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#Edge"));
	
		hasElement = Corpus.factory.getOWLObjectProperty(URI.create(Corpus.namespace+"#hasElement"));
		
		Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(this.sentenceURI,sentenceClass ));
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
		OWLIndividual lineElement;
		StringTokenizer st = new StringTokenizer(line);
		
		//%String %% word			lemma			tag	morph		edge	parent	secedge comment
		String word = st.nextToken();
		String lemma =  st.nextToken();
		String tag =  st.nextToken();
		String morph =  st.nextToken();
		String edge =  st.nextToken();
		String parent =  st.nextToken();
		//word
		if(word.startsWith("#")){
			elementURL+="s_"+id+"_"+word.substring(1);
			lineElement = Corpus.factory.getOWLIndividual(URI.create(elementURL));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(lineElement, structElement));
			
		}else{
			elementURL+="s_"+id+"_"+pos+"_"+URLencodeUTF8.encode(word);
			wordsInOrder.add(word);
			urisInOrder.add(elementURL);
			lineElement = Corpus.factory.getOWLIndividual(URI.create(elementURL));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(lineElement, wordElement));
			Corpus.addAxiom(Corpus.factory.getOWLEntityAnnotationAxiom(lineElement, Corpus.factory.getCommentAnnotation(line)));
			Corpus.addAxiom(Corpus.factory.getOWLEntityAnnotationAxiom(lineElement, Corpus.factory.getOWLLabelAnnotation(word)));
		}
		
		Corpus.addAxiom(Corpus.factory.getOWLObjectPropertyAssertionAxiom(sentenceURI, hasElement, lineElement));
		
		//tag
		tag = (tag.equals("$("))?"SentenceBoundary":tag;
		//morph
		morph= "m_"+URLencodeUTF8.encode(morph);
		makeClasses(lineElement, tag,morph,edge);
		
	}
	
	void makeClasses(OWLIndividual lineElement, String tag, String morph, String edge){
		if(!tag.equals("--")){
			OWLDescription d = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#"+tag));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(lineElement,d ));
			Corpus.addAxiom(Corpus.factory.getOWLSubClassAxiom(d, tagClass));
		}
		if(!morph.equals("m_--")){
			
			OWLDescription d = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#"+morph));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(lineElement,d ));
			Corpus.addAxiom(Corpus.factory.getOWLSubClassAxiom(d, morphClass));
		}
		if(!edge.equals("--")){
			OWLDescription d = Corpus.factory.getOWLClass(URI.create(Corpus.namespace+"#"+edge));
			Corpus.addAxiom(Corpus.factory.getOWLClassAssertionAxiom(lineElement,d ));
			Corpus.addAxiom(Corpus.factory.getOWLSubClassAxiom(d, edgeClass));
		}
	}
}
