package org.dllearner.tools.protege;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.mindswap.pellet.utils.progress.ProgressMonitor;
import org.semanticweb.owlapi.model.OWLAxiom;

import com.clarkparsia.owlapi.explanation.util.ExplanationProgressMonitor;

public class StatusBar2 extends JPanel implements ProgressMonitor, ExplanationProgressMonitor, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel infoLabel;
	private JProgressBar progressBar;

	private boolean cancelled;

	private boolean indeterminate;

	private static final int CANCEL_TIMEOUT_MS = 5000;

	private Timer cancelTimeout;

	private int progress;
	private int progressLength;
	private String progressTitle;

	public StatusBar2() {
		setLayout(new BorderLayout());

		infoLabel = new JLabel("");
		progressBar = new JProgressBar();

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.setOpaque(false);
		JPanel leftPanel = new JPanel(new FlowLayout());

		leftPanel.add(progressBar);
		leftPanel.add(new JSeparator(JSeparator.VERTICAL));
		leftPanel.add(infoLabel);
		leftPanel.add(new JSeparator(JSeparator.VERTICAL));
		leftPanel.setOpaque(false);
		add(leftPanel, BorderLayout.WEST);
//		add(rightPanel, BorderLayout.EAST);
//		setBackground(SystemColor.control);

		cancelTimeout = new Timer(CANCEL_TIMEOUT_MS, new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				
			}
		});
		cancelTimeout.setRepeats(false);
	}

	public void setMessage(String message) {
		infoLabel.setText(message);
	}

	public void showProgress(boolean b) {
		cancelled = false;
		indeterminate = b;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				progressBar.setIndeterminate(indeterminate);

			}
		});
	}

	public void setMaximumValue(int max) {
		progressBar.setMaximum(max);
	}

	@Override
	public void foundAllExplanations() {
		// TODO Auto-generated method stub

	}

	@Override
	public void foundExplanation(Set<OWLAxiom> explanation) {

	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			setProgress(progress);
		}

	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public int getProgressPercent() {
		return (int) (progress * 100.0) / progressBar.getMaximum();
	}

	@Override
	public void incrementProgress() {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progress++);
				// double percentDone = (progress * 100.0) /
				// progressBar.getMaximum();
				// if(percentDone / 100.0 == 0) {
				// label.setText("Classifying ontology " + getProgressPercent()
				// + " %");
				// }
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
	public void setProgressMessage(String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				// label.setText(message);
			}
		});

	}

	@Override
	public void setProgressTitle(String title) {
		this.progressTitle = title;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				infoLabel.setText(progressTitle + "...");
			}
		});

	}

	@Override
	public void taskFinished() {
		cancelTimeout.stop();
		progress = 0;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue(progress);
			}
		});

	}

	@Override
	public void taskStarted() {

		
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setIndeterminate(false);
			}
		});
	}

}
