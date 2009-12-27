package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.dllearner.tools.ore.OREManager;
import org.mindswap.pellet.utils.progress.ProgressMonitor;
import org.semanticweb.owl.model.OWLAxiom;

import com.clarkparsia.explanation.util.ExplanationProgressMonitor;

public class StatusBar2 extends JPanel implements ProgressMonitor, ExplanationProgressMonitor, PropertyChangeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel infoLabel;
	private JProgressBar progressBar;

	private boolean cancelled;

	private boolean indeterminate;

	private Action cancelledAction;

	private static final int CANCEL_TIMEOUT_MS = 5000;

	private Timer cancelTimeout;

	private int progress;
	private int progressLength;
	private String progressTitle;

	private static Icon cancelIcon = new ImageIcon(OREManager.class.getResource("close.png"));

	public StatusBar2() {
		setLayout(new BorderLayout());

		infoLabel = new JLabel("");
		progressBar = new JProgressBar();

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JLabel(new AngledLinesWindowsCornerIcon()), BorderLayout.SOUTH);
		rightPanel.setOpaque(false);
		JPanel leftPanel = new JPanel(new FlowLayout());
		RolloverButton cancelButton = new RolloverButton();
		cancelledAction = new AbstractAction() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -3239121794063572683L;

			public void actionPerformed(ActionEvent e) {
				setCancelled(true);
			}
		};
		cancelButton.setAction(cancelledAction);
		cancelButton.setIcon(cancelIcon);
		leftPanel.add(cancelButton);
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
				setCancelled(true);
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

	private void setCancelled(boolean b) {
		cancelled = b;

		if (b) {
			infoLabel.setText("Cancelling...");
			cancelTimeout.start();
		} else {
			cancelTimeout.stop();
		}
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
				double percentDone = (progress * 100.0) / progressBar.getMaximum();
				// if(percentDone / 100.0 == 0) {
				// label.setText("Classifying ontology " + ((int) percentDone) +
				// " %");
				// }
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

		setCancelled(false);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				progressBar.setIndeterminate(false);
			}
		});
	}

}
