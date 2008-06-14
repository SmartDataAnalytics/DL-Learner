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


public class PosAndNegSelectPanel extends JPanel{

	private static final long serialVersionUID = 23632947283479L;
    private JPanel posAndNegPanel;
    private JPanel posAndNegSelectPanel;
    private DLLearnerModel model;
    private JScrollPane scrollPanel;
    private JLabel pos;
	private JLabel neg;
	private JPanel posLabelPanel;
	private JPanel negLabelPanel;
	private JButton helpForPosExamples;
	private JButton helpForNegExamples;
	private JTextArea help;
	private JDialog hilfe;
    private final Color COLOR_BLACK = Color.black;
    
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
		//setJCheckBoxen();
		posAndNegPanel = new JPanel(new GridLayout(0,1));
		posAndNegPanel.add(posAndNegSelectPanel);
		scrollPanel = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPanel.setViewportView(posAndNegPanel);
		scrollPanel.setPreferredSize(new Dimension(490,248));
		add(scrollPanel);
		addListeners(action);
	}
	
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
	public void unsetPosAndNegPanel()
	{
		posAndNegSelectPanel.removeAll();
	}
	public void addListeners(ActionHandler action)
	{
		for(int i=0;i<model.getPosVector().size();i++)
		{
			model.getPositivJCheckBox(i).addItemListener(action);
			model.getNegativJCheckBox(i).addItemListener(action);
		}
 
	}
	public JPanel getPosAndNegSelectPanel()
	{
		return posAndNegSelectPanel;
	}
	
	public void unsetCheckBoxes()
	{
		model.unsetJCheckBoxen();
	}
	
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
	  
		public void addHelpButtonListener(ActionHandler a)
		{
			helpForPosExamples.addActionListener(a);
			helpForNegExamples.addActionListener(a);
		}
	 
}
