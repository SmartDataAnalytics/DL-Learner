package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.mindswap.pellet.utils.progress.ProgressMonitor;




/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 10-Oct-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ReasonerProgressUI implements ProgressMonitor {


    private JLabel label;

    private JProgressBar progressBar;

    private JDialog window;

    private boolean cancelled;

    private Action cancelledAction;

    private String currentClass;

    private static final int CANCEL_TIMEOUT_MS = 5000;

    private Timer cancelTimeout;
    
    private int progress;
    private int progressLength;

    public ReasonerProgressUI(JFrame frame) {
        JPanel panel = new JPanel(new BorderLayout(7, 7));
        progressBar = new JProgressBar();
        panel.add(progressBar, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label = new JLabel("Classifying...");
        panel.add(label, BorderLayout.NORTH);
        
        window = new JDialog(frame,"Reasoner progress",true);
        cancelledAction = new AbstractAction("Cancel") {
            public void actionPerformed(ActionEvent e) {
                setCancelled(true);
            }
        };
        JButton cancelledButton = new JButton(cancelledAction);

        window.setLocation(400, 400);
        JPanel holderPanel = new JPanel(new BorderLayout(7, 7));
        holderPanel.add(panel, BorderLayout.NORTH);
        holderPanel.add(cancelledButton, BorderLayout.EAST);
        holderPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        window.getContentPane().setLayout(new BorderLayout());
        window.getContentPane().add(holderPanel, BorderLayout.NORTH);
        window.pack();
        Dimension windowSize = window.getSize();
        window.setSize(400, windowSize.height);
        window.setResizable(false);

        cancelTimeout = new Timer(CANCEL_TIMEOUT_MS, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            }
        });
        cancelTimeout.setRepeats(false);
    }




    public void setIndeterminate(boolean b) {
        progressBar.setIndeterminate(b);
    }


    private void setCancelled(boolean b) {
        cancelled = b;
        if (currentClass != null) {
            JOptionPane.showMessageDialog(window,
                                          "Cancelled while classifying " + currentClass,
                                          "Cancelled classification",
                                          JOptionPane.INFORMATION_MESSAGE);
        }
        if (b){
            label.setText("Cancelling...");
            cancelTimeout.start();
        }
        else{
            cancelTimeout.stop();
        }
    }

    public void setProgressIndeterminate(boolean b) {
        progressBar.setIndeterminate(b);
    }

    public boolean isCancelled() {
        return cancelled;
    }


    private void showWindow() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                window.setLocation(screenSize.width / 2 - window.getWidth() / 2,
                                   screenSize.height / 2 - window.getHeight() / 2);
                window.setVisible(true);
            }
        });
    }


    private void hideWindow() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (cancelled && currentClass != null) {
                    JOptionPane.showMessageDialog(window,
                                                  "Cancelled while classifying " + currentClass,
                                                  "Cancelled classification",
                                                  JOptionPane.INFORMATION_MESSAGE);
                }
                window.setVisible(false);
            }
        });
    }


	@Override
	public int getProgress() {
		return progress;
	}


	@Override
	public int getProgressPercent() {
		return (int)(progress * 100.0) / progressBar.getMaximum();
	}


	@Override
	public void incrementProgress() {
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(progress++);
//                double percentDone = (progress * 100.0) / progressBar.getMaximum();
//                if(percentDone / 100.0 == 0) {
//                label.setText("Classifying ontology " + getProgressPercent() + " %");
//                }
            }
        });
	}


	@Override
	public boolean isCanceled() {
		return cancelled;
	}


	@Override
	public void setProgress(int progr) {
		this.progress = progr;
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue((int) progress);
                double percentDone = (progress * 100.0) / progressBar.getMaximum();
//                if(percentDone / 100.0 == 0) {
//                label.setText("Classifying ontology " + ((int) percentDone) + " %");
//                }
            }
        });
		
	}


	@Override
	public void setProgressLength(int length) {
		this.progressLength = length;
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                progressBar.setMaximum((int) progressLength);
            }
        });
		
	}


	@Override
	public void setProgressMessage(final String message) {
		 SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	                label.setText(message);
	            }
	        });
		
	}


	@Override
	public void setProgressTitle(final String title) {
		 SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	window.setTitle(title);
	            }
	        });
		
		
	}


	@Override
	public void taskFinished() {
		cancelTimeout.stop();
        hideWindow();
        currentClass = null;
        progress = 0;
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	 progressBar.setValue(progress);
            }
        });
	
       
		
	}


	@Override
	public void taskStarted() {
//		label.setText("Classifying ontology                    ");
        currentClass = null;
        setCancelled(false);
        showWindow();
		
	}
}
