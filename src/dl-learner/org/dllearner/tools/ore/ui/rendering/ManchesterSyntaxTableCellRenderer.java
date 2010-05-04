package org.dllearner.tools.ore.ui.rendering;

import java.awt.Color;
import java.util.StringTokenizer;

import javax.swing.table.DefaultTableCellRenderer;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owlapi.model.OWLAxiom;

public class ManchesterSyntaxTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5718436702676075368L;
	
	@Override
	protected void setValue(Object value) {
		if(value instanceof Description){
			render(OREManager.getInstance().getManchesterSyntaxRendering((Description)value));
		} else if(value instanceof Individual){
			render(OREManager.getInstance().getManchesterSyntaxRendering((Individual)value));		
		} else if(value instanceof OWLAxiom){
			render(OREManager.getInstance().getManchesterSyntaxRendering((OWLAxiom)value));
		} else {
			super.setValue(value);
		}
	}
	
	private void render(String rendering){
		String renderedString;
		StringTokenizer st = new StringTokenizer(rendering);
		
		StringBuffer bf = new StringBuffer();
		bf.append("<html>");
		String token;
		while(st.hasMoreTokens()){
			token = st.nextToken();
			String color = "black";
			boolean isReserved = false;
			Color c = OREManager.getInstance().getKeywordColorMap().get(token);
			if(c != null){
				color = "#" + Integer.toHexString(c.getRed()) + 
				Integer.toHexString(c.getGreen()) + Integer.toHexString(c.getBlue());
				isReserved = true;
			}
			if(isReserved){
				bf.append("<b><font color=" + color + ">" + token + " </font></b>");
			} else {
				bf.append(" " + token + " ");
			}
		}
		bf.append("</html>");
		renderedString = bf.toString();
		setText(renderedString);
	}

	

}
