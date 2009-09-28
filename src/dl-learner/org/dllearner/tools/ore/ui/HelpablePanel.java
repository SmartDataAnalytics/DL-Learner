package org.dllearner.tools.ore.ui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;


public class HelpablePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2511480671795808029L;
	private JButton helpButton;
	
	private String helpText = "TODO";

	
	public HelpablePanel(){
		setLayout(new BorderLayout());
		helpButton = new JButton(new ImageIcon(this.getClass().getResource("Help-16x16.png")));
		helpButton.setBorderPainted(false);
		helpButton.setContentAreaFilled(false);
//		helpButton.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
		helpButton.setPreferredSize(new Dimension(16, 16));
		helpButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseEntered(MouseEvent e) {
				helpButton.setBorderPainted(true);
				helpButton.setContentAreaFilled(true);
			}
			
			@Override
			public void mouseExited(MouseEvent e) {
				helpButton.setBorderPainted(false);
				helpButton.setContentAreaFilled(false);
			}
			
		});
		helpButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
					    helpText,
					    "",
					    JOptionPane.PLAIN_MESSAGE);


				
			}
		});
		JPanel holderPanel = new JPanel();
		holderPanel.setLayout(new BorderLayout());
		holderPanel.add(helpButton, BorderLayout.NORTH);
		add(holderPanel, BorderLayout.EAST);
	}
	
	public HelpablePanel(JPanel content){
		this();
		add(content, BorderLayout.CENTER);
	}
	
	public void setHelpText(String helpText){
		this.helpText = helpText;
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		
		JPanel endPointHolderPanel = new JPanel();
		endPointHolderPanel.setLayout(new GridLayout(0, 1));
//		endPointHolderPanel.setBorder(new TitledBorder("SPARQL endpoint"));
		JComboBox comboBox = new JComboBox();
		comboBox.setEditable(true);
		comboBox.setActionCommand("endpoints");
		
		
		endPointHolderPanel.add(new JLabel("URL"));
		endPointHolderPanel.add(comboBox);
		JTextField defaultGraphField = new JTextField();
		endPointHolderPanel.add(new JLabel("Default graph URI (optional)"));
		endPointHolderPanel.add(defaultGraphField);
		HelpablePanel endPointHelpPanel = new HelpablePanel(endPointHolderPanel);
		endPointHelpPanel.setBorder(new TitledBorder("SPARQL endpoint"));
		
		
	
		
		frame.add(endPointHelpPanel);
		frame.setSize(600, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
}
