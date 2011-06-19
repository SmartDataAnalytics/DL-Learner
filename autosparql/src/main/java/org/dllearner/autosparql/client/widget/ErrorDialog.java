package org.dllearner.autosparql.client.widget;

import com.extjs.gxt.ui.client.Style.Scroll;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Text;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class ErrorDialog extends Dialog{

	private Dialog dialog;
	
	private ContentPanel errorPanel;
	private Text errorMessage;
	private Text errorStackTrace;
	
	public ErrorDialog(Throwable throwable){
		super();
		
		setHideOnButtonClick(true);
		setLayout(new FitLayout());
		setSize(600, 300);
		setHeading("Error");
		setIconStyle("icon-heading-errorDialog");
		setButtons(Dialog.OK);
		setPlain(true);
		setModal(true);
		setBlinkModal(true);
		setResizable(false);
		
		errorPanel = new ContentPanel();
		errorPanel.setLayout(new FitLayout());
		errorPanel.setHeaderVisible(false);	
		errorPanel.setScrollMode(Scroll.AUTO);
		errorPanel.setFrame(true);	

		/**
		 * error message
		 */
		errorMessage = createErrorText(throwable.getMessage());    
		errorPanel.add(errorMessage);
	
		/**
		 * error stack trace
		 */
		StringBuffer buffer = new StringBuffer();
		for(StackTraceElement element : throwable.getStackTrace()) {
			buffer.append(element.toString()+"\n");
		}				
		errorStackTrace = createErrorText(buffer.toString());    
		errorPanel.add(errorStackTrace);

		
		add(errorPanel);
	}
	
	private Text createErrorText(String errorMsg){
		Text text = new Text(errorMsg);    
		text.setStyleAttribute("backgroundColor", "white");  
		text.setBorders(true);
		text.setAutoHeight(true);
		text.setAutoWidth(true);
		return text;
	}
	
	public void showDialog(){
		show();
	}
	
}
