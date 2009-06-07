/**
 * Copyright (C) 2007-2008, Jens Lehmann
 *
 * This file is part of DL-Learner.
 * 
 * DL-Learner is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * DL-Learner is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.dllearner.tools.ore;

import java.awt.Dimension;
import java.util.Locale;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;


/**
 * Main class starting the wizard and registering wizard panels.
 * @author Lorenz Buehmann
 *
 */
public class Main {
    
	/**
	 * main method.
	 * @param args possible is to use OWL-File as parameter
	 */
    public static void main(String[] args) {
    	try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Locale.setDefault(Locale.ENGLISH);
        Wizard wizard = new Wizard();
        wizard.getDialog().setTitle("DL-Learner ORE-Tool");
        Dimension dim = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        wizard.getDialog().setSize(dim);
        
        WizardPanelDescriptor descriptor1 = new IntroductionPanelDescriptor();
        wizard.registerWizardPanel(IntroductionPanelDescriptor.IDENTIFIER, descriptor1);

        WizardPanelDescriptor descriptor2 = new KnowledgeSourcePanelDescriptor();
        wizard.registerWizardPanel(KnowledgeSourcePanelDescriptor.IDENTIFIER, descriptor2);

        WizardPanelDescriptor descriptor3 = new ClassPanelOWLDescriptor();
        wizard.registerWizardPanel(ClassPanelOWLDescriptor.IDENTIFIER, descriptor3);
        
        WizardPanelDescriptor descriptor4 = new ClassPanelSparqlDescriptor();
        wizard.registerWizardPanel(ClassPanelSparqlDescriptor.IDENTIFIER, descriptor4);
        
        WizardPanelDescriptor descriptor5 = new LearningPanelDescriptor();
        wizard.registerWizardPanel(LearningPanelDescriptor.IDENTIFIER, descriptor5);
        
        WizardPanelDescriptor descriptor6 = new RepairPanelDescriptor();
        wizard.registerWizardPanel(RepairPanelDescriptor.IDENTIFIER, descriptor6);
        
        WizardPanelDescriptor descriptor7 = new SavePanelDescriptor();
        wizard.registerWizardPanel(SavePanelDescriptor.IDENTIFIER, descriptor7);
        
        if (!(args.length == 1)){
        	 wizard.setCurrentPanel(IntroductionPanelDescriptor.IDENTIFIER);
        } else{
        	((KnowledgeSourcePanelDescriptor) descriptor2).getPanel().setFileURL(args[0]); 
        	wizard.setCurrentPanel(KnowledgeSourcePanelDescriptor.IDENTIFIER);
        	wizard.setLeftPanel(1);
        	 
        }
			
       
        int ret = wizard.showModalDialog();
       
        
        System.out.println("Dialog return code is (0=Finish,1=Cancel,2=Error): " + ret);
       
        
        System.exit(0);
        
    }
    
}
