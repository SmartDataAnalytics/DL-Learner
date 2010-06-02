package org.dllearner.tools.evaluationplugin;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.table.JTableHeader;

import org.dllearner.learningproblems.EvaluatedDescriptionClass;
import org.jdesktop.swingx.JXTable;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponent;
import org.protege.editor.owl.ui.renderer.LinkedObjectComponentMediator;
import org.protege.editor.owl.ui.renderer.OWLCellRenderer;
import org.semanticweb.owlapi.model.OWLObject;

public class EvaluationTable extends JXTable implements LinkedObjectComponent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 6293382971051635859L;
	
	private LinkedObjectComponentMediator mediator;
	private boolean allColumnsEnabled = true;
	
	private static final String[] TOOLTIPS = {
		"The learned equivalent class expression.",
		"Adding the definition improves the ontology.",
		"The learned definition is as good as the existing one. Both are good.",
		"The learned definition is as good as the existing one. Both are rather bad.",
		"The learned definition is not as good as the existing one, but still acceptable.",
		"The learned definition is not acceptable.",
		"Adding the definition would be a bad mistake. "		};
	
	public EvaluationTable(OWLEditorKit editorKit){
		super(new EvaluationTableModel());
		mediator = new LinkedObjectComponentMediator(editorKit, this);
		getTableHeader().setReorderingAllowed(false);
		setRenderers(editorKit);
		setColumnSizes();
	}
	
	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
            /**
			 * 
			 */
			private static final long serialVersionUID = -3386641672808329591L;

			@Override
			public String getToolTipText(MouseEvent e) {
             
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = 
                        columnModel.getColumn(index).getModelIndex();
                return TOOLTIPS[realIndex];
            }
			
			
        };

	}
	
	private void setRenderers(OWLEditorKit editorKit){
		//set manchester syntax renderer for the first column
		OWLCellRenderer renderer = new OWLCellRenderer(editorKit, false, false);
		renderer.setHighlightKeywords(true);
		renderer.setWrap(false);
		getColumn(0).setCellRenderer(renderer);
		
		//set for the remaining columns a radiobutton renderer and editor
		//and let the header text show vertical
		for(int i = 1; i < getColumnCount(); i++){
			getColumn(i).setCellRenderer(new RadioButtonRenderer());
			getColumn(i).setCellEditor(new RadioButtonEditor());
			getColumn(i).setHeaderRenderer(new VerticalHeaderRenderer());
		}
	}
	
	private void setColumnSizes(){
		for(int i = 1; i < getColumnCount(); i++){
			getColumn(i).setMaxWidth(30);
		}
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public OWLObject getLinkedObject() {
		return mediator.getLinkedObject();
	}

	@Override
	public Point getMouseCellLocation() {
		Point mouseLoc = getMousePosition();
        if (mouseLoc == null) {
            return null;
        }
        int index = rowAtPoint(mouseLoc);
        Rectangle cellRect = getCellRect(index, 0, true);
       
        return new Point(mouseLoc.x - cellRect.x, mouseLoc.y - cellRect.y);
	}

	@Override
	public Rectangle getMouseCellRect() {
		Point loc = getMousePosition();
        if (loc == null) {
            return null;
        }
        int index = rowAtPoint(loc);
        return getCellRect(index, 0, true);
	}

	@Override
	public void setLinkedObject(OWLObject object) {
		mediator.setLinkedObject(object);
	}
	
	public void setDescriptions(List<EvaluatedDescriptionClass> descriptions){
		((EvaluationTableModel)getModel()).setDescriptions(descriptions);
	}
	
	public EvaluatedDescriptionClass getSelectedEvaluatedDescription(){
		return ((EvaluationTableModel)getModel()).getSelectedEvaluatedDescription(getSelectedRow());
	}
	
	public void dispose(){
		mediator.dispose();
	}
	
	public void setAllColumnsEnabled(boolean value){
		allColumnsEnabled = value;
		((EvaluationTableModel)getModel()).setAllColumnsEnabled(value);
	}
	
	public boolean isAllColumnsEnabled(){
		return allColumnsEnabled;
	}
	
	public Map<EvaluatedDescriptionClass, Integer> getUserInputMap(){
		return ((EvaluationTableModel)getModel()).getUserInputMap();
	}
	
	public void setUserInput(Map<EvaluatedDescriptionClass, Integer> inputMap){
		((EvaluationTableModel)getModel()).setUserInput(inputMap);
	}

	
}
