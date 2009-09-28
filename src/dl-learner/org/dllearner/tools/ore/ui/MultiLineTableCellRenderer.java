package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;
import java.io.StringWriter;
import java.util.StringTokenizer;

import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.View;

import org.dllearner.tools.ore.ExplanationManager;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;

import com.clarkparsia.explanation.io.manchester.Keyword;
import com.clarkparsia.explanation.io.manchester.ManchesterSyntaxObjectRenderer;
import com.clarkparsia.explanation.io.manchester.TextBlockWriter;

public class MultiLineTableCellRenderer extends JTextPane implements TableCellRenderer{

        /**
	 * 
	 */
	private static final long serialVersionUID = -5375479462711405013L;
		protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);
		
		private StringWriter buffer;
		private TextBlockWriter writer;
		private ManchesterSyntaxObjectRenderer renderer;
		private ImpactManager impMan;

		private StyledDocument doc;
		Style style;
        public MultiLineTableCellRenderer() {
            super();

            
            setContentType("text/html");
            setBorder(noFocusBorder);
          
            buffer = new StringWriter();
    		writer = new TextBlockWriter(buffer);
    		renderer = new ManchesterSyntaxObjectRenderer(writer);
    		renderer.setWrapLines( false );
    		renderer.setSmartIndent( true );
    		impMan = ImpactManager.getInstance(OREManager.getInstance());
    		
//    		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
//            Style style = addStyle("regular", def);
//            StyleConstants.setLeftIndent(style, 20.0f);
    		
//    		SimpleAttributeSet sas = new SimpleAttributeSet();
//    		StyleConstants.setAlignment(sas, StyleConstants.ALIGN_RIGHT);
//    		setParagraphAttributes(sas, true);
    		
    		doc = (StyledDocument)getDocument();
            style = doc.addStyle("StyleName", null);
            StyleConstants.setItalic(style, true);
         
                

        }


        public Component getTableCellRendererComponent(JTable table,
                                                       Object value,
                                                       boolean isSelected,
                                                       boolean hasFocus,
                                                       int row,
                                                       int column)
        {
            if (isSelected)
            {
                super.setForeground(table.getSelectionForeground());
                super.setBackground(table.getSelectionBackground());
            }
            else
            {
                super.setForeground(table.getForeground());
                super.setBackground(table.getBackground());
            }

            setFont(table.getFont());

            if (hasFocus)
            {
                setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
                if (!isSelected && table.isCellEditable(row, column))
                {
                    Color col;
                    col = UIManager.getColor("Table.focusCellForeground");
                    if (col != null)
                    {
                        super.setForeground(col);
                    }
                    col = UIManager.getColor("Table.focusCellBackground");
                    if (col != null)
                    {
                        super.setBackground(col);
                    }
                }
            }
            else
            {
                setBorder(noFocusBorder);
            }

            setEnabled(table.isEnabled());

            setValue(table, row, column, value);

            return this;
        }

        protected void setValue(JTable table, int row, int column, Object value)
        {
            if (value != null)
            {	
            	String text = value.toString();
            	setText(text);
              
//            	try {
//					doc.insertString(doc.getLength(), text, style);
//				} catch (BadLocationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
                if(value instanceof OWLAxiom){
    				boolean striked = false;
    				if(impMan != null && impMan.isSelected((OWLAxiom)value)){
    					striked = true;
    				}
    				((OWLAxiom)value).accept(renderer);
    				
    				writer.flush();
    				String newAxiom = buffer.toString();

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
//    				oldAxioms.add(buffer.toString());
    				buffer.getBuffer().delete(0, buffer.toString().length());
    			}

                
                View view = getUI().getRootView(this);
                view.setSize((float) table.getColumnModel().getColumn(column).getWidth() - 3, -1);
                float y = view.getPreferredSpan(View.Y_AXIS);
                int h = (int) Math.ceil(y + 3);
                
                if (table.getRowHeight(row) != h)
                {
                    table.setRowHeight(row, h );
                }
            }
            else
            {
                setText("");
            }
        }
    
}
