package org.dllearner.tools.ore.ui;

import java.awt.Graphics;

import javax.swing.JButton;

public class RolloverButton extends JButton 
{
  /**
	 * 
	 */
	private static final long serialVersionUID = -1491518775798752382L;


public RolloverButton()
  { 
    setRequestFocusEnabled( false );
    setRolloverEnabled( true );
  }


  protected void paintBorder( Graphics g ) 
  { 
    if( model.isRollover() )
    {
      super.paintBorder( g ); 
    } 
  }
}
