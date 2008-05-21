package org.dllearner.tools.protege;

import org.dllearner.core.ComponentManager;
import org.dllearner.kb.OWLFile;

import java.util.Observable;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;
import org.dllearner.core.owl.Description;
import javax.swing.JCheckBox;
import org.dllearner.core.*;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.frame.OWLFrame;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.*;
import org.dllearner.learningproblems.*;
import java.net.URI;
import org.dllearner.core.owl.*;
import org.dllearner.core.owl.NamedClass;
import org.semanticweb.owl.model.OWLOntology;
import org.dllearner.reasoning.OWLAPIReasoner;
import org.semanticweb.owl.model.OWLOntologyManager;
import org.dllearner.reasoning.OWLAPIDescriptionConvertVisitor;
import java.io.File;
import org.semanticweb.owl.model.OWLDescription;
import java.util.HashSet;
import java.util.*;
import org.dllearner.algorithms.refinement.ROLearner;
import org.dllearner.core.KnowledgeSource;
import org.dllearner.core.LearningAlgorithm;
import org.dllearner.core.LearningProblem;
import org.dllearner.core.LearningProblemUnsupportedException;
import org.dllearner.core.ReasoningService;
import org.dllearner.learningproblems.PosNegDefinitionLP;


public class DLLearnerModel extends Observable implements Runnable{
	private String[] componenten={"org.dllearner.kb.OWLFile","org.dllearner.reasoning.OWLAPIReasoner",
			"org.dllearner.reasoning.DIGReasoner","org.dllearner.reasoning.FastRetrievalReasoner","org.dllearner.learningproblems.PosNegInclusionLP"
			,"org.dllearner.learningproblems.PosNegDefinitionLP","org.dllearner.algorithms.RandomGuesser","org.dllearner.algorithms.BruteForceLearner","org.dllearner.algorithms.refinement.ROLearner","org.dllearner.algorithms.refexamples.ExampleBasedROLComponent","org.dllearner.algorithms.gp.GP"};	
	private Vector<JCheckBox> positiv;
	private Vector<JCheckBox> negativ;
	private ComponentManager cm;
	private ReasoningService rs;
	private KnowledgeSource source;
	private OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view;
	private static final int anzahl = 6;
	private Description[] description = new Description[anzahl];
	private LearningProblem lp;
	private OWLOntology ontology;
	private LearningAlgorithm la = null;
	private OWLEditorKit editor;
	private OWLFrame<OWLClass> aktuell;
	private OWLAPIReasoner reasoner;
	private Set<OWLDescription> OWLDescription;
	private Set<String> positiveExamples;
	private Set<String> negativeExamples;
	private Vector<Individual> indis;
	private OWLDescription desc;
	private String id;
	private OWLDescription newConceptOWLAPI;
	private OWLDescription oldConceptOWLAPI;
	private Set<OWLDescription> ds;
	private Vector<Individual> individual;
	

	public DLLearnerModel(OWLEditorKit editorKit, OWLFrame<OWLClass> h,String id,OWLClassDescriptionEditorWithDLLearnerTab.DLLearnerView view)

	{
		editor=editorKit;
		aktuell=h;
		this.id=id;
		this.view=view;
		OWLDescription = new HashSet<OWLDescription>();
		positiv = new Vector<JCheckBox>();
		negativ = new Vector<JCheckBox>();
		indis = new Vector<Individual>();
		individual = new Vector<Individual>();
		ComponentManager.setComponentClasses(componenten);
		cm = ComponentManager.getInstance();
		ds = new HashSet<OWLDescription>();
		
	}
	public void initReasoner()
	{
		setKnowledgeSource();
		setReasoner();
		SortedSet<Individual> pos=rs.getIndividuals();
		while(pos.iterator().hasNext())
		{
			indis.add(pos.iterator().next());
			pos.remove(pos.iterator().next());
		}
		
		//this.neg=rs.getIndividuals();
	}
	public void setPositiveAndNegativeExamples()
	{
		positiveExamples = new TreeSet<String>();
		for(int i=0;i<positiv.size();i++)
		{
			if(positiv.get(i).isSelected())
			{
				positiveExamples.add(positiv.get(i).getText());
			}
		}
		negativeExamples = new TreeSet<String>();
		for(int i=0;i<negativ.size();i++)
		{
			if(negativ.get(i).isSelected())
			{
				negativeExamples.add(negativ.get(i).getText());
			}
		}
		
	}
	
	public Description[] getDescriptions()
	{
		return description;
	}
	
	public void setKnowledgeSource()
	{
		this.source = cm.knowledgeSource(OWLFile.class);
		String uri=getUri();
		cm.applyConfigEntry(source, "url", new File(uri).toURI().toString());
		try{
				source.init();
			}
				catch(ComponentInitException e){
					e.printStackTrace();
			}
	}
	
	public void setReasoner()
	{
		this.reasoner =cm.reasoner(OWLAPIReasoner.class,source);
		reasoner.init();
		rs = cm.reasoningService(reasoner);
	}
	
	public void setLearningProblem()
	{
		if(id.equals("Equivalent classes"))
		{
			lp = cm.learningProblem(PosNegDefinitionLP.class, rs);
		}
		if(id.equals("Superclasses"))
		{
			lp = cm.learningProblem(PosNegInclusionLP.class, rs);
		}
		
		cm.applyConfigEntry(lp, "positiveExamples", positiveExamples);
		cm.applyConfigEntry(lp, "negativeExamples", negativeExamples);
		try{
		lp.init();
		}
		catch(ComponentInitException e)
		{
			e.printStackTrace();
		}
	}
	
	public void setLearningAlgorithm()
	{
		try {
			this.la = cm.learningAlgorithm(ROLearner.class, lp, rs);
		} catch (LearningProblemUnsupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		cm.applyConfigEntry(la, "numberOfTrees", 100);
		cm.applyConfigEntry(la, "maxDepth", 5);
		try{
			la.init();
			}
			catch(ComponentInitException e){
				e.printStackTrace();
			}
	}

	public void run()
	{	
		setKnowledgeSource();
		setReasoner();
		setPositiveAndNegativeExamples();;
		setLearningProblem();
		setLearningAlgorithm();
		// start the algorithm and print the best concept found
		la.start();
		description = new Description[la.getBestSolutions(anzahl).size()];
		for(int j = 0;j<la.getBestSolutions(anzahl).size();j++)
		{
			description[j]=la.getBestSolutions(anzahl).get(j);
		}
		view.draw(description);
	}
	
	public Description[] getSolutions()
	{
		return description;
	}
	
	public Vector<JCheckBox> getPosVector()
	{
		return positiv;
	}
	
	public Vector<JCheckBox> getNegVector()
	{
		return negativ;
	}
	
	
	public void loadOntology(URI uri)
	{
		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		try{
		ontology = manager.loadOntology(uri);
		}
		catch(OWLOntologyCreationException e)
		{
			System.out.println("Can't create Ontology: "+ e);
		}
	}
	
	public void setPosVector()
	{	setPositiveConcept();
		for(int i = 0 ; i<indis.size() ; i++)
		{
			String ind = indis.get(i).toString();
			if(setPositivExamplesChecked(ind))
			{
				positiv.add(new JCheckBox(ind.toString(),true));
				
			}
			else
			{
				positiv.add(new JCheckBox(ind.toString(),false));
			}
			negativ.add(new JCheckBox(ind.toString()));
		}
	}

	
	public void setNegVector()
	{
		
	}
	
	public void setPositiveConcept()
	{
		Set<NamedClass> concepts = rs.getAtomicConcepts();
		SortedSet<Individual> individuals = null;
		while(concepts.iterator().hasNext()&&individuals==null)
		{
			NamedClass concept = concepts.iterator().next();
			if(concept.toString().endsWith("#"+aktuell.getRootObject().toString()))
			{
			individuals = rs.retrieval(concept);
			}
			concepts.remove(concept);
		}
		while(individuals.iterator().hasNext())
		{
			individual.add(individuals.iterator().next());
			individuals.remove(individuals.iterator().next());
		}
		
	}
	
	public boolean setPositivExamplesChecked(String indi)
	{
			boolean isChecked = false;
    		for(int i = 0; i<individual.size()&& isChecked==false;i++)
    		{
    			String indi1=individual.get(i).getName();
				if(indi1.toString().equals(indi.toString()))
				{
					isChecked = true;
				}
				else
				{
					isChecked = false;
				}
    		}
    	return isChecked;
	
	}

	public void clearVector()
	{
		positiv.removeAllElements();
		negativ.removeAllElements();
		indis.removeAllElements();
	}
	
	public String getUri()
    {
    	char[] test = editor.getOWLModelManager().getOntologyPhysicalURI(editor.getOWLModelManager().getActiveOntology()).toString().toCharArray();
    	String uri=""; 
    	for(int i =6; i<test.length;i++)
    	{
    		uri=uri+test[i];
    	}
    	return uri;
    }
	
	public void setDescriptionList(Description[] list)
	{
		description=list;
	}
	
	
	public LearningAlgorithm getLearningAlgorithm()
	{
		return la;
	}
	
	public JCheckBox getPositivJCheckBox(int i)
	{
		return positiv.get(i);
	}
	
	public JCheckBox getNegativJCheckBox(int i)
	{
		return negativ.get(i);
	}
	
	public void resetSuggestionList()
	{
		for(int i=0;i<description.length;i++)
		{
			description[i]=null;
		}
	}
	
	public void unsetJCheckBoxen()
	{
		for(int j=0;j<positiv.size();j++)
		   {
			   if(positiv.get(j).isSelected())
			   {
				   JCheckBox i = positiv.get(j);
				   i.setSelected(false);
				   positiv.set(j, i); 
			   }
		   }
		   for(int j=0;j<negativ.size();j++)
		   {
			   if(negativ.get(j).isSelected())
			   {
				   JCheckBox i = negativ.get(j);
				   i.setSelected(false);
				   negativ.set(j, i);
			   }
		   }
	}

	public OWLOntology getOWLOntology()
	{
		return ontology;
	}
	
	public Set<OWLDescription> getNewOWLDescription()
	{
		return OWLDescription;
	}
	
	public OWLDescription getOldConceptOWLAPI()
	{
		return oldConceptOWLAPI;
	}
	
	public Set<OWLDescription> getNewOWLDescriptions()
	{
		return null;
	}
	
	public OWLDescription getSollution()
	{
		return desc;
	}
	
	public void setNewConceptOWLAPI(Description desc)
	{
		for(int i = 0;i<description.length;i++)
		{
			
			if(desc.toString().equals(description[i].toString()))
			{
				newConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(desc);
				ds.add(newConceptOWLAPI);
			}
			this.desc = newConceptOWLAPI;
			OWLDescription.add(newConceptOWLAPI);
		}
	}
	
	public void setOldConceptOWLAPI()
	{
		SortedSet<Individual> indi=rs.getIndividuals();
		while(positiveExamples.iterator().hasNext())
		{
			String indi1=positiveExamples.iterator().next();
			
			for(int i = 0; i<indi.size();i++)
			{
				Individual indi2 = indi.iterator().next();
				if(indi2.toString().equals(indi1.toString()))
				{
					Set<NamedClass> concept=reasoner.getConcepts(indi2);
					while(concept.iterator().hasNext())
					{
					OWLDescription oldConceptOWLAPI = OWLAPIDescriptionConvertVisitor.getOWLDescription(concept.iterator().next());
					concept.remove(concept.iterator().next());
					ds.add(oldConceptOWLAPI);
					}
					
				}
				indi.remove(indi2);
			}
			indi=rs.getIndividuals();
			positiveExamples.remove(indi1);
		}
	}
	
	public void changeDLLearnerDescriptionsToOWLDescriptions(Description desc)
	{
		setNewConceptOWLAPI(desc);
		setOldConceptOWLAPI();
		//OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		
		//OWLDataFactory factory = manager.getOWLDataFactory();
		//System.out.println("Manager: "+manager);
		//OWLAxiom axiomOWLAPI = factory.getOWLEquivalentClassesAxiom(ds);
		//OWLOntologyFormat format = new OWLOntologyFormat();
		//format = manager.getOntologyFormat(ontology);
		//OWLOntology ontology = editor.getOWLModelManager().getActiveOntology();
		//System.out.println("Format: "+format);
		//AddAxiom axiom = new AddAxiom(ontology, axiomOWLAPI);
		/*try {
			manager.applyChange(axiom);
		} catch (OWLOntologyChangeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		try {
			manager.saveOntology(ontology,format,editor.getOWLModelManager().getActiveOntology().getURI());
		} catch (UnknownOWLOntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OWLOntologyStorageException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	*/
		}
	
	public ReasoningService getReasoningService()
	{
		return rs;
	}
		
		
	}