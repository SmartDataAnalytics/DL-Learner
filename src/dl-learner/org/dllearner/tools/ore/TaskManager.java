package org.dllearner.tools.ore;

import java.awt.Cursor;

import javax.swing.JFrame;
import javax.swing.SwingWorker;

import org.dllearner.tools.ore.ui.StatusBar;

public class TaskManager {
	
	private static TaskManager instance;
	
	private SwingWorker<?, ?> currentTask;

	
	private StatusBar statusBar;
	private JFrame dialog;
	
	public static synchronized TaskManager getInstance(){
		if(instance == null){
			instance = new TaskManager();
		}
		return instance;
	}
	
	public void setStatusBar(StatusBar statusBar){
		this.statusBar = statusBar;
	}
	
	public StatusBar getStatusBar(){
		return statusBar;
	}
	
	public void setDialog(JFrame dialog){
		this.dialog = dialog;
	}
	
	public JFrame getDialog(){
		return dialog;
	}
	
	public void setCurrentTask(SwingWorker<?, ?> task){
		this.currentTask = task;
	}
	
	public void cancelCurrentTask(){
		if(currentTask != null && !currentTask.isCancelled() && !currentTask.isDone()){
			currentTask.cancel(true);
		}
//		statusBar.setProgressTitle("Canceled");
//		dialog.setCursor(null);
	}
	

	
	public void setTaskStarted(String message){
		dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		statusBar.setProgressTitle(message);
		statusBar.showProgress(true);
	}
	
	public void setTaskFinished(){
		dialog.setCursor(null);
		statusBar.setProgressTitle("Done");
		statusBar.showProgress(false);
	}
	
	
}
