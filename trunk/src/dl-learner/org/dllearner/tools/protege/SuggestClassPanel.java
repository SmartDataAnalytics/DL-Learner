package org.dllearner.tools.protege;

import java.awt.Dimension;

import javax.swing.*;

/**
 * This class is the panel for the suggest list.
 * It shows the descriptions made by the DL-Learner.
 * @author Heero Yuy
 *
 */
public class SuggestClassPanel extends JPanel {
	
	private static final long serialVersionUID = 724628423947230L;
	/**
	 * Description List
	 */
	private JList descriptions;
	/**
	 * Panel for the description list
	 */
	private JPanel suggestPanel;
	/**
	 * Date for the description list
	 */
	private DefaultListModel model;
	/**
	 * Scroll panel if the suggestions are longer than the Panel itself
	 *  
	 */
	private JScrollPane suggestScroll;
	/**
	 * This is the constructor for the suggest panel.
	 * It creates a new Scroll panel and puts the Suggest List in it. 
	 */
	public SuggestClassPanel()
	{
		super();

		suggestScroll = new JScrollPane();
		suggestScroll = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		model = new DefaultListModel();
		descriptions = new JList(model);
		suggestPanel = new JPanel();
		descriptions.setVisible(true);
		suggestPanel.add(descriptions);
		suggestScroll.setPreferredSize(new Dimension(490,108));
		suggestScroll.setViewportView(descriptions);
		add(suggestScroll);
	}
	
	/**
	 * this method adds an new Scroll Panel and returns the updated SuggestClassPanel.
	 * @return updated SuggestClassPanel
	 */
	public SuggestClassPanel updateSuggestClassList()
	{
		add(suggestScroll);
		return this;
		
	}
	/**
	 * This method is called after the model for the suggest list is updated.
	 *  
	 * @param desc List model of descriptions made by the DL-Learner
	 */
	public void setSuggestList(DefaultListModel desc)
	{
		descriptions.setModel(desc);
	}
	/**
	 * This method returns the current Description list.
	 * @return JList of Descriptions
	 */
	public JList getSuggestList()
	{
		return descriptions;
	}
	
	/**
	 * this method adds the suggest list to the Mouse Listener.
	 * @param action ActionHandler
	 */
	public void addSuggestPanelMouseListener(ActionHandler action)
	{
		descriptions.addMouseListener(action);
		
	}
	
	
	
          
}