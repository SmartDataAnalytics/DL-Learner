package org.dllearner.tools.ore.ui.rendering;

import java.awt.Color;
import java.util.StringTokenizer;

import org.dllearner.core.owl.Description;
import org.dllearner.core.owl.Individual;
import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.utilities.owl.OWLAPIConverter;
import org.dllearner.utilities.owl.OWLAPIDescriptionConvertVisitor;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLObject;

import com.clarkparsia.owlapi.explanation.io.manchester.Keyword;

public class ManchesterSyntaxRenderer {
	
	static private ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();

	
	public static String render(Description value){
		OWLClassExpression desc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression((Description)value);
		return render(desc);
	}
	
	public static String renderSimple(OWLAxiom ax){	
		String renderedString = renderer.render(ax);
		return renderedString;
	}
	
	public static String renderSimple(Description value){
		OWLClassExpression desc = OWLAPIDescriptionConvertVisitor.getOWLClassExpression((Description)value);
		String renderedString = renderer.render(desc);
		return renderedString;
	}
	
	public static String renderSimple(Individual value){	
		OWLIndividual ind = OWLAPIConverter.getOWLAPIIndividual(value);
		String renderedString = renderer.render(ind);
		return renderedString;
	}
	
	public static String render(Individual value){
		OWLIndividual ind = OWLAPIConverter.getOWLAPIIndividual((Individual) value);
		return render(ind);
	}
	
	public static String render(OWLAxiom value, boolean removed, int depth){
		String renderedString = OREManager.getInstance().getManchesterSyntaxRendering(value);
//		String renderedString = renderer.render(value, null);
		StringTokenizer st = new StringTokenizer(renderedString);
		StringBuffer bf = new StringBuffer();
		
		bf.append("<html>");
		for(int i = 0; i < depth; i++){
			bf.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		if(removed){
			bf.append("<strike>");
		}
		
		
		String token;
		while(st.hasMoreTokens()){
			token = st.nextToken();
			boolean unsatClass = false;
			if(OREManager.getInstance().consistentOntology()){
				for(OWLClass cl : ExplanationManager.getInstance(OREManager.getInstance()).getUnsatisfiableClasses()){
					if(cl.toString().equals(token)){
						unsatClass = true;
						break;
					}
				}
			}
			String color = "black";
			if(unsatClass){
				color = "red";
			} 
			
			boolean isReserved = false;
			Color c = OREManager.getInstance().getKeywordColorMap().get(token);
			if(c != null){
				color = "#" + Integer.toHexString(c.getRed()) + 
				Integer.toHexString(c.getGreen()) + Integer.toHexString(c.getBlue());
				isReserved = true;
			}
//			for(Keyword key : Keyword.values()){
//				if(token.equals(key.getLabel())){
//					color = key.getColor();
//					isReserved = true;
//					break;
//				} 
//			}
			if(isReserved || unsatClass){
				bf.append("<b><font color=" + color + ">" + token + " </font></b>");
			} else {
				bf.append(" " + token + " ");
			}
		}
		if(removed){
			bf.append("</strike>");
		}
		bf.append("</html>");
		renderedString = bf.toString();

		return renderedString;
	}
	
	public static String render(OWLAxiom value, boolean removed, int depth, Explanation explanation){
		
		String renderedString = renderer.render(value);
		StringTokenizer st = new StringTokenizer(renderedString);
		StringBuffer bf = new StringBuffer();
		
		bf.append("<html>");
		for(int i = 0; i < depth; i++){
			bf.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
		}
		if(removed){
			bf.append("<strike>");
		}
		
		
		String token;
		while(st.hasMoreTokens()){
			token = st.nextToken();
			boolean unsatClass = false;
			for(OWLClass cl : ExplanationManager.getInstance(OREManager.getInstance()).getUnsatisfiableClasses()){
				if(cl.toString().equals(token)){
					unsatClass = true;
					break;
				}
			}
			String color = "black";
			if(unsatClass){
				color = "red";
			} 
			
			boolean isReserved = false;
			for(Keyword key : Keyword.values()){
				if(token.equals(key.getLabel())){
					color = key.getColor();
					isReserved = true;break;
				} 
			}
			boolean isRelevant = false;
			if(!isReserved){
				for(OWLAxiom ax : explanation.getAxioms()){
					for(OWLEntity ent : ax.getSignature()){
						if(token.equals(ent.toString())){
							isRelevant = true;break;
						}
					}
					
				}
			} else {
				isRelevant = true;
			}
			if(isReserved || unsatClass){
				if(isRelevant){
					bf.append("<b><font color=" + color + ">" + token + " </font></b>");
				} else {
					bf.append("<strike<b><font color=" + color + ">" + token + " </font></b></strike>");
				}
				
			} else {
				if(isRelevant){
					bf.append(" " + token + " ");
				} else {
					bf.append("<strike> " + token + " </strike>");
				}
				
			}
		}
		if(removed){
			bf.append("</strike>");
		}
		bf.append("</html>");
		renderedString = bf.toString();
		return renderedString;
	}
	
	private static String render(OWLObject obj){
		String renderedString = renderer.render(obj);
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
		return renderedString;
		
	}
}
