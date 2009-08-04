package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.util.Random;

import javax.swing.JFrame;
import javax.swing.SwingWorker;
import javax.swing.UIManager;

import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

public class StatusBarSimulator {
	
	private StatusBar statusBar;
	
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
		    contentPane.add(statusBar, BorderLayout.SOUTH);

		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.setVisible(true);
		    new Task().execute();
	}
	
	class Task extends SwingWorker<Void, Void> {
	      /*
	       * Main task. Executed in background thread.
	       */
	      @Override
	      public Void doInBackground() {
	          Random random = new Random();
	          statusBar.showProgress(true);
	          statusBar.setMessage("loading ontology");
	         for(int i = 0; i<=10; i++) {
	              //Sleep for up to one second.
	              try {
	                  Thread.sleep(random.nextInt(1000));
	              } catch (InterruptedException ignore) {}
	              //Make random progress.
	             
	          }
	          return null;
	      }

	      /*
	       * Executed in event dispatching thread
	       */
	      @Override
	      public void done() {
	          statusBar.showProgress(false);
	          statusBar.setMessage("ontology loaded");
	      }
	  }

  public static void main(String[] args) {
	  new StatusBarSimulator().createandShowGUI();
  }
  
  
  
  


  
  

}
