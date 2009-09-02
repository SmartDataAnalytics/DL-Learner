package org.dllearner.tools.ore.ui;

import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.swing.table.DefaultTableCellRenderer;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLObject;

import com.clarkparsia.explanation.io.manchester.Keyword;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxObjectRenderer;
import com.clarkparsia.explanation.io.manchester.TextBlockWriter;

public class ManchesterSyntaxTableCellRenderer extends DefaultTableCellRenderer {

	private StringWriter buffer;
	private TextBlockWriter writer;
	private ManchesterSyntaxObjectRenderer renderer;
	
	public ManchesterSyntaxTableCellRenderer(){
		buffer = new StringWriter();
		writer = new TextBlockWriter(buffer);
		renderer = new ManchesterSyntaxObjectRenderer(writer);
		renderer.setWrapLines( false );
		renderer.setSmartIndent( true );
	}
	
	@Override
	protected void setValue(Object value) {
		if(value instanceof Description){
			OWLDescription desc = OWLAPIDescriptionConvertVisitor.getOWLDescription((Description)value);
			render(desc);
		} else if(value instanceof Individual){
			OWLIndividual ind = OWLAPIConverter.getOWLAPIIndividual((Individual) value);
			render(ind);		
		} else if(value instanceof OWLAxiom){
			render((OWLAxiom)value);
		} else {
			super.setValue(value);
		}
	}
	
	private void render(OWLObject obj){
		obj.accept(renderer);
		writer.flush();
		String renderedString = buffer.toString();
		StringTokenizer st = new StringTokenizer(renderedString);
		
		StringBuffer bf = new StringBuffer();
		bf.append("<html>");
		String token;
		while(st.hasMoreTokens()){
			token = st.nextToken();
			String color = "black";
			boolean isReserved = false;
			for(Keyword key : Keyword.values()){
				if(token.equals(key.getLabel())){
					color = key.getColor();
					isReserved = true;break;
				} 
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
		buffer.getBuffer().delete(0, buffer.toString().length());
		
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = -5718436702676075368L;
	

}
