package org.dllearner.gui;

import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

import org.dllearner.Info;

/**
 * Window displaying some information about DL-Learner and DL-Learner GUI.
 * 
 * @author Jens Lehmann
 * 
 */
public class AboutWindow extends JFrame {

	private static final long serialVersionUID = -5448814141333659068L;

	public AboutWindow() {
		setTitle("About");
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
		setLocationByPlatform(true);
		setSize(400, 400);

		URL imgURL = AboutWindow.class.getResource("dl-learner.gif");
		
		String html = "<html><p align=\"center\"><img src=\""+imgURL+"\" alt=\"DL-Learner logo\"/></p>";
		html += "<p align=\"center\"><i>DL-Learner</i><br />Build " + Info.build + "</p>";
		html += "<br /><p align=\"center\"><i>License</i><br />GNU General Public License Version 3</p>";
		html += "<br /><p align=\"center\"><i>Homepage</i><br /><a href=\"http://dl-learner.org\">http://dl-learner.org</a></p>";		
		html += "<br /><p align=\"center\"><i>DL-Learner GUI developers</i><br />";
		html += "Jens Lehmann<br />Tilo Hielscher</p>";
		html += "<br /><p align=\"center\"><i>DL-Learner contributors in general</i><br />";
		html += "Jens Lehmann<br />";
		html += " &nbsp; Sebastian Hellmann (SPARQL component and more)<br />";
		html += "Sebastian Knappe (DBpedia Navigator)<br />";
		html += "Tilo Hielscher (DL-Learner GUI)<br />";
		html += "Lorenz Bühmann (ORE Tool)<br />";
		html += "Maria Moritz, Vu Duc Minh (OntoWiki plugin)<br />";
		html += "Christian Kötteritzsch (Protégé 4 plugin)<br />";
		html += "Collette Hagert (DB to OWL converter)</p>";		
		html += "</html>";
		
		JLabel label = new JLabel(html);
//		label.setFont(new Font("Serif", Font.PLAIN, 14));
		
		add(label);
		
//		SimpleAttributeSet bold = new SimpleAttributeSet();
//		StyleConstants.setBold(bold, true);
//		StyleConstants.setAlignment(bold, StyleConstants.ALIGN_CENTER);
//		
//		JTextPane textPane = new JTextPane();
//		StyledDocument doc = textPane.getStyledDocument();
//		
//
//		textPane.setText("DL-Learner");
//		doc.setParagraphAttributes(0, doc.getLength(), bold, false);
//		add(textPane);
//		
//		

		// Put the editor pane in a scroll pane.
//		JTextPane textPane = createTextPane();
		JScrollPane editorScrollPane = new JScrollPane(label);
		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
//		editorScrollPane.setPreferredSize(new Dimension(250, 145));
//		editorScrollPane.setMinimumSize(new Dimension(10, 10));

		add(editorScrollPane);

		setVisible(true);
	}

	@SuppressWarnings("unused")
	private JTextPane createTextPane() {
		String[] initString = { " ", "DL-Learner      ", "Build " + Info.build,
				"DL-Learner GUI developers:", "Jens Lehmann\nTilo Hielscher\n" };

		String[] initStyles = { "icon", "bold", "regular", "italic", "regular" };

		JTextPane textPane = new JTextPane();
//		textPane.setSize(300, 200);
		textPane.setEditable(false);
		StyledDocument doc = textPane.getStyledDocument();
		addStylesToDocument(doc);

		try {
			for (int i = 0; i < initString.length; i++) {
				doc.insertString(doc.getLength(), initString[i] + "\n", doc.getStyle(initStyles[i]));
			}
		} catch (BadLocationException ble) {
			System.err.println("Couldn't insert initial text into text pane.");
		}

		return textPane;
	}

	protected void addStylesToDocument(StyledDocument doc) {
		Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);

		Style regular = doc.addStyle("regular", def);
//		StyleConstants.setFontFamily(def, "SansSerif");

		Style s = doc.addStyle("italic", regular);
		StyleConstants.setItalic(s, true);

		s = doc.addStyle("bold", regular);
		StyleConstants.setAlignment(s, StyleConstants.ALIGN_RIGHT);
		StyleConstants.setBold(s, true);

		s = doc.addStyle("icon", regular);
		StyleConstants.setAlignment(s, StyleConstants.ALIGN_CENTER);
		ImageIcon pigIcon = createImageIcon("dl-learner.gif", "DL-Learner logo");
		StyleConstants.setIcon(s, pigIcon);
	}
	

	protected static ImageIcon createImageIcon(String path, String description) {
		URL imgURL = AboutWindow.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL, description);
		} else {
			System.err.println("Couldn't find file: " + path);
			return null;
		}
	}

}
