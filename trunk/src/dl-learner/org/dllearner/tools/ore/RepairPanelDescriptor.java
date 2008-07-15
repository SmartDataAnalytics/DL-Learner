package org.dllearner.tools.ore;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.dllearner.core.owl.Individual;
import org.dllearner.core.owl.NamedClass;
import org.semanticweb.owl.model.OWLOntologyChange;




public class RepairPanelDescriptor extends WizardPanelDescriptor implements ActionListener, ListSelectionListener, MouseListener{
    
    public static final String IDENTIFIER = "REPAIR_PANEL";
    
    RepairPanel panel4;
    private Set<OWLOntologyChange> ontologyChanges;
    private ORE ore;
    private OntologyModifierOWLAPI modi;
   
    
    public RepairPanelDescriptor() {
        
        panel4 = new RepairPanel();
       
        panel4.addActionListeners(this);
        panel4.addSelectionListeners(this);
        panel4.addMouseListeners(this);
       
        setPanelDescriptorIdentifier(IDENTIFIER);
        setPanelComponent(panel4);
        ontologyChanges = new HashSet<OWLOntologyChange>();
        
     
    }
    
    @Override
	public Object getNextPanelDescriptor() {
        return RepairPanelDescriptor.IDENTIFIER;
    }
    
    @Override
	public Object getBackPanelDescriptor() {
        return LearningPanelDescriptor.IDENTIFIER;
    }
    
   
    
   

	public void valueChanged(ListSelectionEvent e) {
		
//		if (!e.getValueIsAdjusting()) 
//			 System.err.println(panel4.getNegFailureList().getSelectedValue());
		
	}

	public void actionPerformed(ActionEvent event) {
		ore = getWizardModel().getOre();
        modi = ore.getModi();       
		String actionName = ((JButton)event.getSource()).getName();
		String actionType = ((JButton)event.getSource()).getParent().getName();
		
		if(actionType.equals("negative")){
			Individual ind = (Individual)panel4.getNegFailureList().getSelectedValue();
				if(actionName.equals("negRepair")){
					RepairDialog negDialog = new RepairDialog(ind, getWizard().getDialog(), ore, "neg");
					int returncode = negDialog.showDialog();
					if(returncode == 2){
						ontologyChanges.addAll(negDialog.getAllChanges());
					}
					else if(returncode == 3){
						ontologyChanges.addAll(negDialog.getAllChanges());
						panel4.getNegFailureModel().removeElement(ind);
					}
				}
				else if(actionName.equals("negAdd")){
					ontologyChanges.addAll(modi.addClassAssertion(ind, ore.getIgnoredConcept()));
					panel4.getNegFailureModel().removeElement(ind);
					
				}
				else if(actionName.equals("negDelete")){
					ontologyChanges.addAll(modi.deleteIndividual(ind));
					panel4.getNegFailureModel().removeElement(ind);
				
				}
		}
		else if(actionType.equals("positive")){
			Individual ind = (Individual)panel4.getPosFailureList().getSelectedValue();
			if(actionName.equals("posRepair")){
				RepairDialog posDialog = new RepairDialog(ind, getWizard().getDialog(), ore, "pos");
				int returncode = posDialog.showDialog();
				if(returncode == 2){
					ontologyChanges.addAll(posDialog.getAllChanges());
				}
				else if(returncode == 3){
					ontologyChanges.addAll(posDialog.getAllChanges());
					panel4.getPosFailureModel().removeElement(ind);
				}
			}
			else if(actionName.equals("posRemove")){
				ontologyChanges.addAll(modi.addClassAssertion(ind, ore.getIgnoredConcept()));
				panel4.getPosFailureModel().removeElement(ind);
				
			}
			else if(actionName.equals("posDelete")){
				ontologyChanges.addAll(modi.deleteIndividual(ind));
				panel4.getPosFailureModel().removeElement(ind);
				
			}
		}
		
		
	}
	
	public void mouseClicked(MouseEvent e) {
		
		if(e.getClickCount() == 2){
			if(e.getSource() == panel4.getNegFailureList() ){
				Individual ind = (Individual)panel4.getNegFailureList().getSelectedValue();
				RepairDialog negDialog = new RepairDialog(ind, getWizard().getDialog(), getWizardModel().getOre(), "neg");
				int returncode = negDialog.showDialog();
				if(returncode == 2){
					ontologyChanges.addAll(negDialog.getAllChanges());
					
				}
				else if(returncode == 3){
					ontologyChanges.addAll(negDialog.getAllChanges());
					panel4.getNegFailureModel().removeElement(ind);
				}
			}
			else if(e.getSource() == panel4.getPosFailureList()){
				Individual ind = (Individual)panel4.getPosFailureList().getSelectedValue();
				RepairDialog posDialog = new RepairDialog(ind, getWizard().getDialog(), getWizardModel().getOre(), "pos");
				int returncode = posDialog.showDialog();
				if(returncode == 2){
					ontologyChanges.addAll(posDialog.getAllChanges());
					
				}
				else if(returncode == 3){
					ontologyChanges.addAll(posDialog.getAllChanges());
					panel4.getPosFailureModel().removeElement(ind);
				}
			}
		}
		
	}

	public void mouseEntered(MouseEvent e) {
		JList negList = panel4.getNegFailureList();
		DefaultListModel negModel = panel4.getNegFailureModel();
		if(e.getSource() instanceof JList){
				int index = negList.locationToIndex(e.getPoint());
		        if (-1 < index) {
		        	Individual ind = (Individual)negModel.getElementAt(index);
		        	StringBuffer strBuf = new StringBuffer();
					strBuf.append("<html><b><u>classes:</b></u><br><br><BLOCKQUOTE>");
														
					for(NamedClass n: getWizardModel().getOre().reasoner2.getConcepts(ind))
						strBuf.append("<br>" + n );
					strBuf.append("</BLOCKQUOTE></html>");
					negList.setToolTipText(strBuf.toString());
		        }
		}
		
				
	}

	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	
	public Set<OWLOntologyChange> getOntologyChanges() {
		return ontologyChanges;
	}
	
	
	
}


