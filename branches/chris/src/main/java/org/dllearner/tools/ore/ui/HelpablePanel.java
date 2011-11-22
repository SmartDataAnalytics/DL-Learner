package org.dllearner.tools.ore.ui;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.TitledBorder;

import com.jgoodies.looks.plastic.PlasticLookAndFeel;


public class HelpablePanel extends JPanel {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2511480671795808029L;
	
	private RolloverButton helpButton;
	private static Icon helpIcon = new ImageIcon(HelpablePanel.class.getResource("Help-16x16.png"));
	
	private String helpText = "TODO";
	private GridBagConstraints c;
	
	public HelpablePanel(){
		setLayout(new GridBagLayout());
		c = new GridBagConstraints();

		c.anchor = GridBagConstraints.FIRST_LINE_END;
		c.weightx = 0.0;
		c.weighty = 0.0;
		c.gridx = 1;
		c.gridy = 0;
//		add(helpButton, c);
		helpButton = new RolloverButton();
		helpButton.setIcon(helpIcon);
		helpButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null,
					    helpText,
					    "",
					    JOptionPane.PLAIN_MESSAGE);


				
			}
		});
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
	
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException{
		UIManager.setLookAndFeel(new PlasticLookAndFeel());
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
		TitledBorder border = new TitledBorder("LEarning type"){

			/**
			 * 
			 */
			private static final long serialVersionUID = 1878007899412644256L;
			
			@Override
			public void paintBorder(Component c, Graphics g, int x, int y,
					int width, int height) {
				// TODO Auto-generated method stub
				super.paintBorder(c, g, x, y, width, height);
			}
			
		};
//		border.setTitleJustification(TitledBorder.ABOVE_BOTTOM);
		border.setTitlePosition(TitledBorder.ABOVE_TOP);
		learnTypeHelpPanel.setBorder(border);
	
		
		frame.add(learnTypeHelpPanel);
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}
	
	
}