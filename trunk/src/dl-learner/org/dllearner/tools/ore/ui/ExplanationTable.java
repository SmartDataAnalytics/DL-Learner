package org.dllearner.tools.ore.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.dllearner.tools.ore.ImpactManager;
import org.dllearner.tools.ore.OREManager;
import org.dllearner.tools.ore.RepairManager;
import org.dllearner.tools.ore.RepairManagerListener;
import org.dllearner.tools.ore.explanation.Explanation;
import org.dllearner.tools.ore.ui.editor.InputVerificationStatusChangedListener;
import org.dllearner.tools.ore.ui.editor.OWLAxiomEditor;
import org.dllearner.tools.ore.ui.editor.OWLAxiomsEditor;
import org.dllearner.tools.ore.ui.editor.VerifiedInputEditor;
import org.dllearner.tools.ore.ui.editor.VerifyingOptionPane;
import org.dllearner.tools.ore.ui.rendering.TextAreaRenderer;
import org.jdesktop.swingx.JXTable;
import org.jdesktop.swingx.decorator.ColorHighlighter;
import org.jdesktop.swingx.decorator.HighlightPredicate;
import org.protege.editor.core.Disposable;
import org.semanticweb.owl.model.AddAxiom;
import org.semanticweb.owl.model.OWLAxiom;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObject;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLOntologyChange;
import org.semanticweb.owl.model.RemoveAxiom;

public class ExplanationTable extends JXTable implements RepairManagerListener, Disposable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5580730282611559609L;
	
	private RepairManager repMan;
	private OREManager oreMan;
	
	
	protected String[] columnToolTips = {
		    null, 
		    "The number of already computed explanations wherein the axiom occurs.",
		    "The sum of all axioms, in which the entities of the current axiom are contained.",
		    "",
		    "If checked, the axiom is selected to remove from the ontology.",
		    "Edit the axiom."
		};

	
	public ExplanationTable(Explanation exp, OWLClass cl) {
		oreMan = OREManager.getInstance();
		repMan = RepairManager.getInstance(OREManager.getInstance());
		repMan.addListener(this);
		
		setBackground(Color.WHITE);
		setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		setModel(new ExplanationTableModel(exp,	cl));
		setRolloverEnabled(true);
		addHighlighter(new ColorHighlighter(HighlightPredicate.ROLLOVER_ROW, 
			      Color.YELLOW, Color.BLACK));  
		
		TableColumn column6 = getColumn(5);
		column6.setCellRenderer(new ButtonCellRenderer());
		column6.setCellEditor(new ButtonCellEditor());
		column6.setResizable(false);
//		setRowHeight(getRowHeight() + 4);
//		setRowHeightEnabled(true);
		setRowHeight(20);
	
		getColumn(0).setCellRenderer(new TextAreaRenderer());
//		getColumn(0).setCellRenderer(new OWLTableCellRenderer(OREManager.getInstance()));
		getColumn(1).setMaxWidth(60);
		getColumn(2).setMaxWidth(60);
		getColumn(3).setMaxWidth(60);
		getColumn(4).setMaxWidth(30);
		getColumn(5).setMaxWidth(30);
		getColumn(4).setHeaderRenderer(new TableCellRenderer() {
			
			@Override
			public Component getTableCellRendererComponent(JTable arg0, Object value,
					boolean arg2, boolean arg3, int arg4, int arg5) {
				JButton b = new JButton((Icon)value);
				return b;
			}
		});
		getColumn(4).setHeaderValue(new ImageIcon(this.getClass().getResource("../DeleteCross.gif")));
		getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					final ExplanationTable table;

					@Override
					public void valueChanged(ListSelectionEvent e) {

						table.changeSelection();

					}

					{
						table = ExplanationTable.this;

					}

				});

		addFocusListener(new FocusListener() {

			final ExplanationTable table;

			public void focusGained(FocusEvent focusevent) {
			}

			public void focusLost(FocusEvent e) {
				table.clearSelection();
				table.changeSelection();

			}

			{

				table = ExplanationTable.this;

			}
		});

	}
	
	@Override
	protected JTableHeader createDefaultTableHeader() {
		return new JTableHeader(columnModel) {
            /**
			 * 
			 */
			private static final long serialVersionUID = -3386641672808329591L;

			public String getToolTipText(MouseEvent e) {
             
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = 
                        columnModel.getColumn(index).getModelIndex();
                return columnToolTips[realIndex];
            }
        };

	}
	
	@Override
	public String getToolTipText(MouseEvent e){
		String tip = null;
        java.awt.Point p = e.getPoint();
        int rowIndex = rowAtPoint(p);
        if(rowIndex != -1){
        	tip = oreMan.getDLSyntaxRendering(((ExplanationTableModel)getModel()).getOWLAxiomAtRow(rowIndex));
        } else {
        	tip = super.getToolTipText(e);
        }
        return tip;
	}
	
	public void strikeOut(boolean strikeOut){
		((ExplanationTableModel)getModel()).setStriked(strikeOut);
	}
	
	private void changeSelection() {

	}
	
	class ButtonCellRenderer extends JButton implements TableCellRenderer{

		/**
		 * 
		 */
		private static final long serialVersionUID = 1962950956976967243L;

		
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value,
				boolean isSelected, boolean hasFocus, int row, int column) {
				if (isSelected) {
					setForeground(table.getSelectionForeground());
					setBackground(table.getSelectionBackground());
				} else {
					setForeground(table.getForeground());
					setBackground(UIManager.getColor("Button.background"));
				}
				setIcon(new ImageIcon(this.getClass().getResource("../Edit16.gif")));
				setText("");
				return this;
				}
				 
		
	}
	
	class ButtonCellEditor extends AbstractCellEditor implements 
			TableCellEditor, ActionListener {
		/**
		 * 
		 */
		private static final long serialVersionUID = 9017452102605141646L;
		JTable table;
		JButton editButton;
		String text;
		int row;
		

		public ButtonCellEditor() {
			super();
			
			editButton = new JButton();
			editButton.setFocusPainted(false);
			editButton.addActionListener(this);
		}

		
		@Override
		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			text = (value == null) ? "" : value.toString();
			editButton.setText("");
			this.row = row;
			return editButton;
		}

		@Override
		public Object getCellEditorValue() {
			return text;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			fireEditingStopped();
			OWLAxiom ax = ((ExplanationTableModel)getModel()).getOWLAxiomAtRow(row);
			OWLAxiomsEditor editor = new OWLAxiomsEditor(oreMan);
			editor.setEditedObject(ax);
			showEditorDialog(editor, ax);
//			if(ax instanceof OWLClassAxiom){
//				OWLClassAxiomEditor editor = new OWLClassAxiomEditor(OREManager.getInstance());
//				editor.setEditedObject((OWLClassAxiom) ax);
//				showEditorDialog(editor, ax);
//			} else if(ax instanceof OWLObjectPropertyAxiom){
//				OWLObjectPropertyAxiomEditor editor = new OWLObjectPropertyAxiomEditor(OREManager.getInstance());
//				editor.setEditedObject((OWLObjectPropertyAxiom) ax);
//				showEditorDialog(editor, ax);
//			}
			
		}
	}
	
	class IconRenderer extends DefaultTableCellRenderer {
		/**
		 * 
		 */
		private static final long serialVersionUID = -1729370486474583609L;

		public Component getTableCellRendererComponent(JTable table,
				Object obj, boolean isSelected, boolean hasFocus, int row,
				int column) {

			setIcon((Icon) obj);

			setBorder(UIManager.getBorder("TableHeader.cellBorder"));
			setHorizontalAlignment(JLabel.CENTER);
			return this;
		}
	}

	@Override
	public void repairPlanExecuted(List<OWLOntologyChange> changes) {
		repaint();	
	}

	@Override
	public void repairPlanChanged() {
		repaint();
	}

	@Override
	public void dispose() throws Exception {
		repMan.removeListener(this);
	}
	
	private void showEditorDialog(final OWLAxiomEditor editor, final OWLObject value) {
		if (editor == null) {
			return;
		}
		// Create the editing component dialog - we use an option pane
		// so that the buttons and keyboard actions are what are expected
		// by the user.
		final JComponent editorComponent = editor.getEditorComponent();
		final VerifyingOptionPane optionPane = new VerifyingOptionPane(
				editorComponent) {

			public void selectInitialValue() {
				// This is overriden so that the option pane dialog default
				// button
				// doesn't get the focus.
			}
		};
		final InputVerificationStatusChangedListener verificationListener = new InputVerificationStatusChangedListener() {
			public void verifiedStatusChanged(boolean verified) {
				optionPane.setOKEnabled(verified);
			}
		};
		// if the editor is verifying, will need to prevent the OK button from
		// being available
		if (editor instanceof VerifiedInputEditor) {
			((VerifiedInputEditor) editor)
					.addStatusChangedListener(verificationListener);
		}
		final Component parent = SwingUtilities.getAncestorOfClass(Frame.class, getParent());
		final JDialog dlg = optionPane.createDialog(parent, null);
		dlg.setModal(false);
		dlg.setResizable(true);
		dlg.pack();
		dlg.setLocationRelativeTo(parent);
		dlg.addComponentListener(new ComponentAdapter() {

			public void componentHidden(ComponentEvent e) {
				Object retVal = optionPane.getValue();
				editorComponent.setPreferredSize(editorComponent.getSize());
				if (retVal != null && retVal.equals(JOptionPane.OK_OPTION)) {
					handleEditFinished(editor, value);
				}
//				setSelectedValue(frameObject, true);
				if (editor instanceof VerifiedInputEditor) {
					((VerifiedInputEditor) editor)
							.removeStatusChangedListener(verificationListener);
				}
//					editor.dispose();
			}
		});
			
		dlg.setTitle(OREManager.getInstance().getManchesterSyntaxRendering(value));
		dlg.setVisible(true);
	}
	
        void handleEditFinished(OWLAxiomEditor editor, OWLObject value){
        	ImpactManager.getInstance(OREManager.getInstance()).addSelection((OWLAxiom)value);
        	List<OWLOntologyChange> changes = new ArrayList<OWLOntologyChange>();
        	for(OWLOntology ont : OREManager.getInstance().getOWLOntologiesForOWLAxiom((OWLAxiom)value)){
        		changes.add(new RemoveAxiom(ont, (OWLAxiom)value));
        		changes.add(new AddAxiom(ont, (OWLAxiom)editor.getEditedObject()));
        		
        	}
			repMan.addToRepairPlan(changes);	
        }
	

}
