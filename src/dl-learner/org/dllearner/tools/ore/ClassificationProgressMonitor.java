package org.dllearner.tools.ore;

import java.awt.Cursor;
import java.awt.Dimension;

import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import org.mindswap.pellet.utils.progress.ProgressMonitor;

public class ClassificationProgressMonitor extends JPanel implements ProgressMonitor{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4913267621100462227L;
	private javax.swing.ProgressMonitor monitor;
	private JProgressBar progressBar;
	private String progressMessage = "";
	private String	progressTitle	= "";
	private int		progress		= 0;
	private int		progressLength	= 0;
	private int		progressPercent	= -1;
	private long		startTime		= -1;
	private boolean	canceled		= false;

	
	public ClassificationProgressMonitor(){
		super();
		monitor = new javax.swing.ProgressMonitor(this, progressTitle, progressMessage, 0 ,progressLength);
		progressBar = new JProgressBar(0, progressLength);
		progressBar.setValue(progress);
		progressBar.setStringPainted(true);
		add(progressBar);
		setSize(new Dimension(200, 200));
		
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public int getProgressPercent() {
		return progressPercent;
	}

	@Override
	public void incrementProgress() {
		setProgress(progress + 1);
		
	}

	@Override
	public boolean isCanceled() {
		return monitor.isCanceled();
	}

	@Override
	public void setProgress(int progress) {
		this.progress = progress;
		updateProgress();
		
	}

	@Override
	public void setProgressLength(int length) {
		progressLength = length;
		monitor.setMaximum(length);
		progressBar.setMaximum(length);
		
	}

	@Override
	public void setProgressMessage(String message) {
		progressMessage = message;
		monitor.setNote(message);
		
	}

	@Override
	public void setProgressTitle(String title) {
		progressTitle = title;
		
	}

	@Override
	public void taskFinished() {
		monitor.close();
		setCursor(null);
		
	}

	@Override
	public void taskStarted() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
	}
	
	private void updateProgress(){
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				monitor.setProgress(progress);
				progressBar.setValue(progress);
				
			}
			
		});
	}
	
	

}
