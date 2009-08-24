package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Cursor;
import java.io.File;
import java.net.URI;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import org.dllearner.tools.ore.ORE;
import org.mindswap.pellet.owlapi.Reasoner;
import org.mindswap.pellet.utils.Timer;
import org.semanticweb.owl.apibinding.OWLManager;
import org.semanticweb.owl.model.OWLDataFactory;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyCreationException;
import org.semanticweb.owl.model.OWLOntologyManager;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

public class StatusBarSimulator {
	
	private StatusBar statusBar;
	private ORE ore;
	private Reasoner reasoner;
	
	public StatusBarSimulator(ORE ore, Reasoner reasoner){
		this.ore = ore;
		this.reasoner = reasoner;
		ore.initPelletReasoner();
		
	}
	
	public void createandShowGUI(){
		try {
		      UIManager.setLookAndFeel(new WindowsLookAndFeel());
		    } catch (Exception e) {

		    }

		    JFrame frame = new JFrame();
		    frame.setBounds(200, 200, 600, 200);
		    frame.setTitle("Status bar simulator");

		    Container contentPane = frame.getContentPane();
		    contentPane.setLayout(new BorderLayout());

		    statusBar = new StatusBar();
		    statusBar.setMessage("loading ontology");
//		    ore.getPelletReasoner().getReasoner().getKB().getTaxonomyBuilder().setProgressMonitor(statusBar);
//		    reasoner.getKB().getTaxonomyBuilder().setProgressMonitor(statusBar);
		   
		    contentPane.add(statusBar, BorderLayout.SOUTH);

		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.setVisible(true);
		    new Task(frame).execute();
	}
	
	class Task extends SwingWorker<Void, Void> {
		
		private JFrame frame;
		public Task(JFrame frame){
			this.frame = frame;
		}
	      /*
	       * Main task. Executed in background thread.
	       */
	      @Override
	      public Void doInBackground() {
	         
	          statusBar.showProgress(true);
//	          statusBar.setMessage("loading ontology");
	          Timer t1 = new Timer("load");t1.start();
//	          reasoner.classify();
	          frame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
	          ore.loadOntology();
	          ore.getPelletReasoner().classify();
	         
	          t1.stop();
	          System.out.println(t1.getTotal());

	          return null;
	      }

	      /*
	       * Executed in event dispatching thread
	       */
	      @Override
	      public void done() {frame.setCursor(null);
	          statusBar.showProgress(false);
	          statusBar.setMessage("ontology classified");
	      }
	  }

  public static void main(String[] args) throws OWLOntologyCreationException {
	  String file = "file:examples/ore/tambis.owl";
	  ORE ore = new ORE();
	  ore.setKnowledgeSource(new File("examples/ore/tambis.owl"));
	  OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology( URI.create( file ) );
		OWLDataFactory factory = manager.getOWLDataFactory();
		
		// Create the reasoner and load the ontology
		Reasoner reasoner = new Reasoner( manager );
		reasoner.loadOntology( ontology );
		
	  new StatusBarSimulator(ore, reasoner).createandShowGUI();
  }
  
  
  
  
  


  
  

}
