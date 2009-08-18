package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.protege.editor.owl.OWLEditorKit;


/**
 * Author: Matthew Horridge<br>
 * The University Of Manchester<br>
 * Medical Informatics Group<br>
 * Date: 10-Oct-2006<br><br>
 * <p/>
 * matthew.horridge@cs.man.ac.uk<br>
 * www.cs.man.ac.uk/~horridgm<br><br>
 */
public class ReasonerProgressUI implements org.mindswap.pellet.utils.progress.ProgressMonitor{

    private OWLEditorKit owlEditorKit;

    private JLabel label;

    private JProgressBar progressBar;

    private JDialog window;

    private boolean cancelled;

    private Action cancelledAction;

    private String currentClass;

    private static final int CANCEL_TIMEOUT_MS = 5000;

    private Timer cancelTimeout;


    public ReasonerProgressUI(final OWLEditorKit owlEditorKit) {
        this.owlEditorKit = owlEditorKit;
        JPanel panel = new JPanel(new BorderLayout(7, 7));
        progressBar = new JProgressBar();
        panel.add(progressBar, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        label = new JLabel("Classifying...");
        panel.add(label, BorderLayout.NORTH);

        window = new JDialog((Frame) (SwingUtilities.getAncestorOfClass(Frame.class, owlEditorKit.getWorkspace())),
                             "Reasoner progress",
                             true);
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
                owlEditorKit.getOWLModelManager().getOWLReasonerManager().killCurrentClassification();
            }
        });
        cancelTimeout.setRepeats(false);
    }


    public void setSize(final long size) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue(0);
                progressBar.setMaximum((int) size);
            }
        });
    }


    public void setStarted() {
        label.setText("Classifying ontology                    ");
        currentClass = null;
        setCancelled(false);
        showWindow();
    }


    public void setProgress(final long progress) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                progressBar.setValue((int) progress);
                double percentDone = (progress * 100.0) / progressBar.getMaximum();
//                if(percentDone / 100.0 == 0) {
                label.setText("Classifying ontology " + ((int) percentDone) + " %");
//                }
            }
        });
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


    public void setMessage(final String message) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                label.setText(message);
            }
        });
    }


    public void setProgressIndeterminate(boolean b) {
        progressBar.setIndeterminate(b);
    }


    public void setFinished() {
        cancelTimeout.stop();
        hideWindow();
        currentClass = null;
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
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getProgressPercent() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void incrementProgress() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean isCanceled() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void setProgress(int arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setProgressLength(int arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setProgressMessage(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setProgressTitle(String arg0) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void taskFinished() {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void taskStarted() {
		// TODO Auto-generated method stub
		
	}
}
