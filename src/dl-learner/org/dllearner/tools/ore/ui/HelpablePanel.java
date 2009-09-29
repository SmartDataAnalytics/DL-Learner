package org.dllearner.tools.ore.ui;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.border.TitledBorder;


public class HelpablePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2511480671795808029L;
	private JButton helpButton;
	
	private String helpText = "TODO";
	private GridBagConstraints c;
	
	public HelpablePanel(){
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();
//		setLayout(new BorderLayout());
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
//		JPanel holderPanel = new JPanel();
//		holderPanel.setLayout(new BorderLayout());
//		holderPanel.add(helpButton, BorderLayout.EAST);
//		add(holderPanel, BorderLayout.NORTH);
		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 1;
		c.gridy = 0;
		add(helpButton, c);
	}
	
	public HelpablePanel(JPanel content){
		this();
		c.gridx = 0;
		c.gridy = 1;
		c.weightx = 1.0;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.BOTH;
		add(content, c);
	}
	
	public void setHelpText(String helpText){
		this.helpText = helpText;
	}
	
	public static void main(String[] args){
		JFrame frame = new JFrame();
		
		JPanel learnTypePanel = new JPanel();
		learnTypePanel.setLayout(new GridLayout(0, 1));
		JRadioButton equivalentClassButton = new JRadioButton("Learn equivalent class expressions", true);
		equivalentClassButton.setActionCommand("equivalent");
		equivalentClassButton.setSelected(true);
		JRadioButton superClassButton = new JRadioButton("Learn super class expressions");
		superClassButton.setActionCommand("super");
			
		ButtonGroup learningType = new ButtonGroup();
		learningType.add(equivalentClassButton);
		learningType.add(superClassButton);
		
		learnTypePanel.add(equivalentClassButton);
		learnTypePanel.add(superClassButton);
		HelpablePanel learnTypeHelpPanel = new HelpablePanel(learnTypePanel);
		learnTypeHelpPanel.setBorder(new TitledBorder("Learning type"));
		
	
		
		frame.add(learnTypeHelpPanel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
}
