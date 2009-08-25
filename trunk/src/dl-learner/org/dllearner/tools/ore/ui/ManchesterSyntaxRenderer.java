package org.dllearner.tools.ore.ui;

import java.io.StringWriter;
import java.util.StringTokenizer;

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

public class ManchesterSyntaxRenderer {
	
	static private StringWriter buffer = new StringWriter();
	static private TextBlockWriter writer = new TextBlockWriter(buffer);
	static private ManchesterSyntaxObjectRenderer renderer = new ManchesterSyntaxObjectRenderer(writer);
	
	public ManchesterSyntaxRenderer(){
	
		renderer.setWrapLines( false );
		renderer.setSmartIndent( true );
	}

	
	public static String render(Description value){
		OWLDescription desc = OWLAPIDescriptionConvertVisitor.getOWLDescription((Description)value);
		return render(desc);
	}
	
	public static String render(Individual value){
		OWLIndividual ind = OWLAPIConverter.getOWLAPIIndividual((Individual) value);
		return render(ind);
	}
	
	public static String render(OWLAxiom value, boolean striked){
		value.accept(renderer);
		writer.flush();
		String renderedString = buffer.toString();
		StringTokenizer st = new StringTokenizer(renderedString);
		StringBuffer bf = new StringBuffer();
		bf.append("<html>");
		if(striked){
			bf.append("<strike>");
		}
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
		if(striked){
			bf.append("</strike>");
		}
		bf.append("</html>");
		renderedString = bf.toString();
		buffer.getBuffer().delete(0, buffer.toString().length());
		return renderedString;
	}
	
	private static String render(OWLObject obj){
		obj.accept(renderer);
		writer.flush();
		String renderedString = buffer.toString();
		StringTokenizer st = new StringTokenizer(renderedString);
		System.out.println(renderedString);
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
		return renderedString;
		
	}
}
