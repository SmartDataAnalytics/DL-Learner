package org.dllearner.tools.ore;

import java.awt.Cursor;

import javax.swing.JFrame;

import org.dllearner.tools.ore.ui.StatusBar2;

public class TaskManager {
	
	private static TaskManager instance;
	
	private StatusBar2 statusBar;
	private JFrame dialog;
	
	public static synchronized TaskManager getInstance(){
		if(instance == null){
			instance = new TaskManager();
		}
		return instance;
	}
	
	public void setStatusBar(StatusBar2 statusBar){
		this.statusBar = statusBar;
	}
	
	public StatusBar2 getStatusBar(){
		return statusBar;
	}
	
	public void setDialog(JFrame dialog){
		this.dialog = dialog;
	}
	
	public JFrame getDialog(){
		return dialog;
	}
	
	public void setTaskStarted(String message){
		dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		statusBar.setMessage(message);
		statusBar.showProgress(true);
	}
	
	public void setTaskFinished(){
		dialog.setCursor(null);
		statusBar.setMessage("Done");
		statusBar.showProgress(false);
		statusBar.setProgress(0);
	}
	
	
}
