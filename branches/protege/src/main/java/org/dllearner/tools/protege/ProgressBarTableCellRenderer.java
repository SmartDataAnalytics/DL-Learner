package org.dllearner.tools.protege;

import java.awt.Component;

import javax.swing.JProgressBar;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.protege.editor.owl.ui.renderer.OWLRendererPreferences;

public class ProgressBarTableCellRenderer extends JProgressBar implements TableCellRenderer {
		
		  
	/**
	 * 
	 */
	private static final long serialVersionUID = 8523710265306561978L;
	

		public ProgressBarTableCellRenderer() {
		    super(JProgressBar.HORIZONTAL);
		    setBorderPainted(false);
		    setStringPainted(true);
		    setFont(OWLRendererPreferences.getInstance().getFont());
		  }

		  public ProgressBarTableCellRenderer(int min, int max) {
		    super(JProgressBar.HORIZONTAL, 0, 100);
		    setBorderPainted(false);
		  }
		  
		  public Component getTableCellRendererComponent(JTable table, Object value,
		                   boolean isSelected, boolean hasFocus, int row, int column) {
			 
		    int n = 0;
		    if (! (value instanceof Number)) {
		      String str;
		      if (value instanceof String) {
		        str = (String)value;
		      } else {
		        str = value.toString();
		      }
		      try {
		        n = Integer.valueOf(str).intValue();
		      } catch (NumberFormatException ex) {
		      }
		    } else {
		      n = ((Number)value).intValue();
		    }
		    
		    setValue(n);
		    return this;
		  }
		  
		 

}
