package org.dllearner.gui;

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

import javax.swing.JPanel;

/**
 * AbstractWidgetPanel
 * 
 * @author Tilo Hielscher
 * 
 */
public abstract class AbstractWidgetPanel extends JPanel {

  
    /*
     * show label and tooltip
     */
    protected abstract void showLabel();

    /*
     * show textfield, setbutton and so on
     */
    protected abstract void showThingToChange();

    /*
     * set entry you got from showThingToChange()
     */
    protected abstract void setEntry();
    
}
