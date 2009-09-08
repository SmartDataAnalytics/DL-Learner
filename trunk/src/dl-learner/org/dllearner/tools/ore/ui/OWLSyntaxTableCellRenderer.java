package org.dllearner.tools.ore.ui;

import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.swing.table.DefaultTableCellRenderer;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

import com.clarkparsia.explanation.io.manchester.Keyword;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxObjectRenderer;
import com.clarkparsia.explanation.io.manchester.TextBlockWriter;

public class OWLSyntaxTableCellRenderer extends DefaultTableCellRenderer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6528440084244154347L;
	
	
	private StringWriter buffer;
	private TextBlockWriter writer;
	private ManchesterSyntaxObjectRenderer renderer;
	private ImpactManager impMan;

	
	
	public OWLSyntaxTableCellRenderer(){
		super();
		buffer = new StringWriter();
		writer = new TextBlockWriter(buffer);
		renderer = new ManchesterSyntaxObjectRenderer(writer);
		renderer.setWrapLines( false );
		renderer.setSmartIndent( true );
		impMan = ImpactManager.getInstance(OREManager.getInstance());
	}
	
	@Override
	protected void setValue(Object value) {
			
			if(value instanceof OWLAxiom){
				boolean striked = false;
				if(impMan != null && impMan.isSelected((OWLAxiom)value)){
					striked = true;
				}
				((OWLAxiom)value).accept(renderer);
				
				writer.flush();
				String newAxiom = buffer.toString();
//				System.out.println("new axiom " + newAxiom);
//				if(!oldAxioms.isEmpty()){
//					StringTokenizer st = new StringTokenizer(newAxiom);
//					int index;
//					String token = st.nextToken();
//					for(String s : oldAxioms){System.out.println("old axiom " + s);
//						
//							
//							if(s.contains(token)){
//								index = s.indexOf(token);
//								if(index>0){
//									StringBuffer bf = new StringBuffer();
//									for(int i = 0;i<=index+10;i++){
//										bf.append(" ");
//									}
//									bf.append(newAxiom);
//									newAxiom = bf.toString();
//									break;
//								}
//							}
//						
//					}
//				}
				StringTokenizer st = new StringTokenizer(newAxiom);
			
				StringBuffer bf = new StringBuffer();
				bf.append("<html>");
				if(striked){
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
					if(isReserved || unsatClass){
						bf.append("<b><font color=" + color + ">" + token + " </font></b>");
					} else {
						bf.append(" " + token + " ");
					}
				}
				if(striked){
					bf.append("</strike>");
				}
				bf.append("</html>");
				newAxiom = bf.toString();
				setText(newAxiom);
//				oldAxioms.add(buffer.toString());
				buffer.getBuffer().delete(0, buffer.toString().length());
			} else {
				super.setValue(value);
			}
			
//			ManchesterOWLSyntaxOWLObjectRendererImpl renderer = new ManchesterOWLSyntaxOWLObjectRendererImpl();
			
			
////			DLSyntaxObjectRenderer renderer = new DLSyntaxObjectRenderer();
//			setText(renderer.render((OWLAxiom) value));
		
		
	}
}
