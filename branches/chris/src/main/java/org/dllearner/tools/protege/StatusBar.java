package org.dllearner.tools.protege;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.semanticweb.owlapi.reasoner.ReasonerProgressMonitor;

public class StatusBar extends JPanel implements PropertyChangeListener, ReasonerProgressMonitor {
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
	private String progressTitle;

	public StatusBar() {
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
	public void propertyChange(PropertyChangeEvent evt) {
		if ("progress" == evt.getPropertyName()) {
			int progress = (Integer) evt.getNewValue();
			setProgress(progress);
		}

	}

	public boolean isCanceled() {
		return cancelled;
	}

	public void setProgress(int progr) {
		this.progress = progr;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setValue((int) progress);
			}
		});

	}

	public void setProgressTitle(String title) {
		this.progressTitle = title;
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				infoLabel.setText(progressTitle + "...");
			}
		});

	}

	@Override
	public void reasonerTaskBusy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void reasonerTaskProgressChanged(int value, int max) {
		progressBar.setMaximum(max);
		setProgress(value);
	}

	@Override
	public void reasonerTaskStarted(String message) {
		setProgress(0);
		setMessage(message);
	}

	@Override
	public void reasonerTaskStopped() {
		setMessage("");
		setProgress(0);
	}

}