package org.dllearner.tools.ore.ui.rendering;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.util.Map;
import java.util.StringTokenizer;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.UIManager;
import javax.swing.table.TableCellRenderer;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;
import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObject;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

public class OWLTableCellRenderer extends JTextPane implements
		TableCellRenderer {

	private static final Logger logger = Logger
			.getLogger(OWLTableCellRenderer.class);

	private boolean strikeThrough;

	private OWLOntology ontology;

	private Font plainFont;

	private Font boldFont;

	public static final Color SELECTION_BACKGROUND = UIManager.getDefaults()
			.getColor("List.selectionBackground");

	public static final Color SELECTION_FOREGROUND = UIManager.getDefaults()
			.getColor("List.selectionForeground");

	public static final Color FOREGROUND = UIManager.getDefaults().getColor(
			"List.foreground");

	private boolean highlightKeywords = true;

	private boolean wrap = false;

	private boolean highlightUnsatisfiableClasses = true;

	private boolean highlightUnsatisfiableProperties = true;

	private int plainFontHeight;

	private boolean opaque = false;

	private int preferredWidth;

	private JComponent componentBeingRendered;

	private OREManager oreManager;

	public OWLTableCellRenderer(OREManager oreManager) {
		this.oreManager = oreManager;
		setOpaque(false);
		setEditable(false);
		prepareStyles();
		// setupFont();
	}

	public void setHighlightKeywords(boolean hightlighKeywords) {
		this.highlightKeywords = hightlighKeywords;
	}

	public void setHighlightUnsatisfiableClasses(
			boolean highlightUnsatisfiableClasses) {
		this.highlightUnsatisfiableClasses = highlightUnsatisfiableClasses;
	}

	public void setHighlightUnsatisfiableProperties(
			boolean highlightUnsatisfiableProperties) {
		this.highlightUnsatisfiableProperties = highlightUnsatisfiableProperties;
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {
		preferredWidth = table.getParent().getWidth();
		componentBeingRendered = table;
		if(value instanceof OWLAxiom){
			if(ImpactManager.getInstance(oreManager).isSelected((OWLAxiom) value)){
				strikeThrough = true;
			} else {
				strikeThrough = false;
			}
		}
		
		// Set the size of the table cell
		// setPreferredWidth(table.getColumnModel().getColumn(column).getWidth());
		return prepareRenderer(value, isSelected, hasFocus);
	}

	private Component prepareRenderer(Object value, boolean isSelected,
			boolean hasFocus) {
		setOpaque(isSelected || opaque);
		prepareTextPane(getRendering(value), isSelected);
		if(isSelected){
			setBackground(SELECTION_BACKGROUND);
		} else {
			setBackground(componentBeingRendered.getBackground());
		}

		return this;
	}

	protected String getRendering(Object object) {
		if (object instanceof OWLObject) {
			String rendering = OREManager.getInstance().getManchesterSyntaxRendering(
					((OWLObject) object));
			return rendering;
		} else {
			if (object != null) {
				return object.toString();
			} else {
				return "";
			}
		}
	}

	private void prepareTextPane(Object value, boolean selected) {

		setBorder(null);
		String theVal = value.toString();
		if (!wrap) {
			theVal = theVal.replace('\n', ' ');
			theVal = theVal.replaceAll(" [ ]+", " ");
		}
		setText(theVal);
		// textPane.setSize(textPane.getPreferredSize());
		StyledDocument doc = getStyledDocument();
		// doc.setParagraphAttributes(0, doc.getLength(), linespacingStyle,
		// false);

		resetStyles(doc);
		if (selected) {
			doc.setParagraphAttributes(0, doc.getLength(), selectionForeground,
					false);
		} else {
			doc.setParagraphAttributes(0, doc.getLength(), foreground, false);
		}

		if (strikeThrough) {
			doc.setParagraphAttributes(0, doc.getLength(), strikeOutStyle,
					false);
		} else {
			doc.setParagraphAttributes(0, doc.getLength(), plainStyle,
					false);
		}
		

		highlightText(doc);
	}
	
	private void resetStyles(StyledDocument doc) {
        doc.setParagraphAttributes(0, doc.getLength(), nonBoldStyle, true);
    }

	protected void highlightText(StyledDocument doc) {
		// Highlight text
		StringTokenizer tokenizer = new StringTokenizer(getText(),
				" []{}(),\n\t'", true);
		int tokenStartIndex = 0;
		while (tokenizer.hasMoreTokens()) {
			// Get the token and determine if it is a keyword or
			// entity (or delimeter)
			String curToken = tokenizer.nextToken();
			if (curToken.equals("'")) {
				while (tokenizer.hasMoreTokens()) {
					String s = tokenizer.nextToken();
					curToken += s;
					if (s.equals("'")) {
						break;
					}
				}
			}
			renderToken(curToken, tokenStartIndex, doc);

			tokenStartIndex += curToken.length();
		}
	}

	private boolean parenthesisRendered = false;

	protected void renderToken(final String curToken,
			final int tokenStartIndex, final StyledDocument doc) {

		boolean enclosedByBracket = false;
		if (parenthesisRendered) {
			parenthesisRendered = false;
			enclosedByBracket = true;
		}

		int tokenLength = curToken.length();
		Color c = keyWordColorMap.get(curToken);
		if (c != null && highlightKeywords) {
			Style s = doc.getStyle(curToken);
			doc.setCharacterAttributes(tokenStartIndex, tokenLength, s, true);
		} else {
			// Not a keyword, so might be an entity (or delim)
			OWLEntity curEntity = OREManager.getInstance().getOWLEntityFinder()
					.getOWLEntity(curToken);
			if (curEntity != null) {
				if (curEntity instanceof OWLClass) {
					// If it is a class then paint the word red if the class
					// is inconsistent
					if (highlightUnsatisfiableClasses &&
					// !getOWLModelManager().getReasoner().isConsistent(getOWLModelManager().getActiveOntology())
					// ||
							!oreManager.getReasoner().isSatisfiable(
									(OWLClass) curEntity)) {
						// Paint red because of inconsistency
						doc.setCharacterAttributes(tokenStartIndex,
								tokenLength, inconsistentClassStyle, true);
					}
				} else if (highlightUnsatisfiableProperties
						&& curEntity instanceof OWLObjectProperty) {
					highlightPropertyIfUnsatisfiable(curEntity, doc,
							tokenStartIndex, tokenLength);
				}
			} else {
				if (curToken.equals("(")) {
					parenthesisRendered = true;
				}
			}
		}
	}

	private void highlightPropertyIfUnsatisfiable(OWLEntity entity,
			StyledDocument doc, int tokenStartIndex, int tokenLength) {
		OWLObjectProperty prop = (OWLObjectProperty) entity;
		OWLClassExpression d = OREManager.getInstance().getOWLDataFactory()
				.getOWLObjectMinCardinality(1, prop);
		if (!oreManager.getReasoner().isSatisfiable(d)) {
			doc.setCharacterAttributes(tokenStartIndex, tokenLength,
					inconsistentClassStyle, true);
		}
	}

	private Style plainStyle;

	private Style boldStyle;

	private Style nonBoldStyle;

	private Style selectionForeground;

	private Style foreground;

	private Style inconsistentClassStyle;

	private Style strikeOutStyle;

	private Style fontSizeStyle;

	Map<String, Color> keyWordColorMap = new KeywordColorMap();

	private void prepareStyles() {
		StyledDocument doc = getStyledDocument();
		for (String keyWord : keyWordColorMap.keySet()) {
			Style s = doc.addStyle(keyWord, null);
			Color color = keyWordColorMap.get(keyWord);
			StyleConstants.setForeground(s, color);
			StyleConstants.setBold(s, true);
		}
		plainStyle = doc.addStyle("PLAIN_STYLE", null);
		// StyleConstants.setForeground(plainStyle, Color.BLACK);
		StyleConstants.setItalic(plainStyle, false);
		StyleConstants.setSpaceAbove(plainStyle, 0);
		// StyleConstants.setFontFamily(plainStyle,
		// textPane.getFont().getFamily());

		boldStyle = doc.addStyle("BOLD_STYLE", null);
		StyleConstants.setBold(boldStyle, true);

		nonBoldStyle = doc.addStyle("NON_BOLD_STYLE", null);
		StyleConstants.setBold(nonBoldStyle, false);

		selectionForeground = doc.addStyle("SEL_FG_STYPE", null);
		StyleConstants.setForeground(selectionForeground, SELECTION_FOREGROUND);

		foreground = doc.addStyle("FG_STYLE", null);
		StyleConstants.setForeground(foreground, FOREGROUND);

		inconsistentClassStyle = doc.addStyle("INCONSISTENT_CLASS_STYLE", null);
		StyleConstants.setForeground(inconsistentClassStyle, Color.RED);

		strikeOutStyle = doc.addStyle("STRIKE_OUT", null);
		StyleConstants.setStrikeThrough(strikeOutStyle, true);
		StyleConstants.setBold(strikeOutStyle, false);

		fontSizeStyle = doc.addStyle("FONT_SIZE", null);
		StyleConstants.setFontSize(fontSizeStyle, 40);
	}

}
