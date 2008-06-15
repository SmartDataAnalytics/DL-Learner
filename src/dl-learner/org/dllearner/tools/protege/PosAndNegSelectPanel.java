package org.dllearner.tools.protege;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.WindowConstants;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;

/**
 * This class is the Panel for the Check boxes where the positive and negative
 * examples are chosen.
 * @author Heero Yuy
 *
 */
public class PosAndNegSelectPanel extends JPanel{

	private static final long serialVersionUID = 23632947283479L;
	/**
	 * This is the Panel here the check boxes, the labels, and the help buttons are in.
	 */
    private JPanel posAndNegPanel;
    /**
     * this is the Panel where the check boxes are.
     */
    private JPanel posAndNegSelectPanel;
    /**
     * This is the DLLearner Model
     */
    private DLLearnerModel model;
    /**
     * This is the Scroll pane if there are more Check boxes than the view can show 
     */
    private JScrollPane scrollPanel;
    /**
     * This is the Label that shows "Positive Examples"
     */
    private JLabel pos;
    /**
     * This is the Label that shows "Negative Examples"
     */
	private JLabel neg;
	/**
	 * This is the Panel where the Label for Positive Examples and
	 * a help Button is in 
	 */
	private JPanel posLabelPanel;
	/**
	 * This is the Panel where the Label for Negative Examples and
	 * a help Button is in
	 */
	private JPanel negLabelPanel;
	/**
	 * This is the Help button for positive examples
	 */
	private JButton helpForPosExamples;
	/**
	 * This is the Help button for negative examples
	 */
	private JButton helpForNegExamples;
	/**
	 * This is the Text area where the help message is displayed.
	 */
	private JTextArea help;
	/**
	 * This is the frame that pops up when the help button is pressed.
	 */
	private JDialog hilfe;
    private final Color COLOR_BLACK = Color.black;
    /**
     * This is the constructor for the Panel that shows the check boxes.
     * @param model DLLearnerModel
     * @param action ActionHandler
     */
	public PosAndNegSelectPanel(DLLearnerModel model,ActionHandler action)
	{
		super();
		pos = new JLabel("Positive Examples");
    	neg = new JLabel("Negative Examples");
    	helpForPosExamples = new JButton("?");
    	helpForPosExamples.setSize(10, 10);
    	helpForNegExamples = new JButton("?");
    	helpForNegExamples.setSize(10, 10);
	    posLabelPanel = new JPanel();
	    negLabelPanel = new JPanel();
    	posLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    	negLabelPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
    	helpForPosExamples.setName("PosHelpButton");
    	posLabelPanel.add(pos);
    	posLabelPanel.add(helpForPosExamples);
    	helpForNegExamples.setName("NegHelpButton");
    	negLabelPanel.add(neg);
    	negLabelPanel.add(helpForNegExamples);
		this.model = model;
		posAndNegSelectPanel = new JPanel(new GridLayout(0,2));

		model.clearVector();
	  	model.unsetListModel();
	  	model.initReasoner();
	  	model.setPosVector();
		posAndNegPanel = new JPanel(new GridLayout(0,1));
		posAndNegPanel.add(posAndNegSelectPanel);
		scrollPanel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanel.setViewportView(posAndNegPanel);
		scrollPanel.setPreferredSize(new Dimension(490,248));
		add(scrollPanel);
		addListeners(action);
	}
	/**
	 * This method adds the check boxes, the labels and the help buttons for
	 * positive and negative examples.
	 */
	public void setJCheckBoxen()
	{
		posAndNegSelectPanel.add(posLabelPanel);
		posAndNegSelectPanel.add(negLabelPanel);
		for(int j=0; j<model.getPosVector().size();j++)
	    {
	    	posAndNegSelectPanel.add(model.getPositivJCheckBox(j));
	    	posAndNegSelectPanel.add(model.getNegativJCheckBox(j));
	    }
	    	
	}
	/**
	 * This method removes the Check boxes, the labels and the help buttons 
	 * after the DL-Learner tab is closed.
	 */
	public void unsetPosAndNegPanel()
	{
		posAndNegSelectPanel.removeAll();
	}
	/**
	 * This method adds the item listener for every check box
	 * @param action ActionHandler
	 */
	public void addListeners(ActionHandler action)
	{
		for(int i=0;i<model.getPosVector().size();i++)
		{
			model.getPositivJCheckBox(i).addItemListener(action);
			model.getNegativJCheckBox(i).addItemListener(action);
		}
 
	}
	/**
	 * This method returns the Panel where the check boxes, labels and help buttons are in.
	 * @return JPanel where check boxes, labels and help buttons are in.
	 */
	public JPanel getPosAndNegSelectPanel()
	{
		return posAndNegSelectPanel;
	}
	/**
	 * This method unselect the selected check boxes after learning
	 */
	public void unsetCheckBoxes()
	{
		model.unsetJCheckBoxen();
	}
	/**
	 * This message displays the help message after the help button is pressed.
	 * @param helfen hilfenachricht
	 */
	 public void renderHelpMessage(String helfen)
	  {
		  JScrollPane scrollHelp = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		  
		  help = new JTextArea();
		  hilfe = new JDialog();
		  help.setEditable(false);
		  hilfe.setName("Hilfe");
		  hilfe.setSize(300,100);
		  hilfe.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		  hilfe.setVisible(true);
		  hilfe.setResizable(false);
		  help.setForeground(COLOR_BLACK);
		  help.setText("Help: "+helfen);
		  scrollHelp.setViewportView(help);
		  scrollHelp.setBounds(0, 0, 300, 100);
		  hilfe.add(scrollHelp);
	  }
	  /**
	   * This method adds the Action listener to the help buttons.
	   * @param a ActionHandler
	   */
		public void addHelpButtonListener(ActionHandler a)
		{
			helpForPosExamples.addActionListener(a);
			helpForNegExamples.addActionListener(a);
		}
	 
}
