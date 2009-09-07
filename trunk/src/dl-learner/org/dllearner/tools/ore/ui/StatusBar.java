package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Shape;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.dllearner.tools.ore.TaskManager;
import org.mindswap.pellet.utils.progress.ProgressMonitor;
import org.semanticweb.owl.model.OWLAxiom;

import com.clarkparsia.explanation.util.ExplanationProgressMonitor;

public class StatusBar extends JPanel implements ProgressMonitor, ExplanationProgressMonitor{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel infoLabel;
	private JProgressBar progressBar;
	private int		progress		= 0;
	private int		progressLength	= 0;
	private int		progressPercent	= -1;
	private String progressMessage;
	private boolean isIndeterminateMode;
	private boolean isCanceled = false;

	public StatusBar() {
		infoLabel = new JLabel("");
		progressBar = new JProgressBar();
//		progressBar.setStringPainted(true);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(10, 23));

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JLabel(new AngledLinesWindowsCornerIcon()),
				BorderLayout.SOUTH);
		rightPanel.setOpaque(false);
		JPanel leftPanel = new JPanel(new FlowLayout());
		CancelButton rB = new CancelButton("");
		rB.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				isCanceled = true;
			}
		});
		rB.setToolTipText("Abort");
		leftPanel.add(rB);
		leftPanel.add(progressBar);
		leftPanel.add(new JSeparator(JSeparator.VERTICAL));
		leftPanel.add(infoLabel);
		leftPanel.add(new JSeparator(JSeparator.VERTICAL));
		leftPanel.setOpaque(false);
		add(leftPanel, BorderLayout.WEST);
		add(rightPanel, BorderLayout.EAST);
		setBackground(SystemColor.control);
	}

	public void setMessage(String message) {
		infoLabel.setText(message);
	}

	public void showProgress(boolean b) {
		isCanceled = false;
		isIndeterminateMode = b;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressBar.setIndeterminate(isIndeterminateMode);
				
			}
		});
	}

	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int y = 0;
		g.setColor(new Color(156, 154, 140));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(196, 194, 183));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(218, 215, 201));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233, 231, 217));
		g.drawLine(0, y, getWidth(), y);

		y = getHeight() - 3;
		g.setColor(new Color(233, 232, 218));
		g.drawLine(0, y, getWidth(), y);
		y++;
		g.setColor(new Color(233, 231, 216));
		g.drawLine(0, y, getWidth(), y);
		y = getHeight() - 1;
		g.setColor(new Color(221, 221, 220));
		g.drawLine(0, y, getWidth(), y);

	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public int getProgressPercent() {
		return progressPercent;
	}

	@Override
	public void incrementProgress() {
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				setProgress(progress + 1);
				
			}
		});
		
	}

	@Override
	public boolean isCanceled() {
		return isCanceled;
	}

	@Override
	public void setProgress(int progress) {
		this.progress = progress;
		updateProgress();
		
	}

	@Override
	public void setProgressLength(int length) {
		progressLength = length;
		SwingUtilities.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				progressBar.setMaximum(progressLength);
				
			}
		});
		
	
		
		
		
	}

	@Override
	public void setProgressMessage(String message) {
		progressMessage = message;
		infoLabel.setText(message);
		
		
	}

	@Override
	public void setProgressTitle(String title) {
		infoLabel.setText(title);
		
	}

	@Override
	public void taskFinished() {
		setCursor(null);
		
	}

	@Override
	public void taskStarted() {
		setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		
	}
	
	private void updateProgress(){
		SwingUtilities.invokeLater(new Runnable(){

			@Override
			public void run() {
				progressBar.setValue(progress);
				
				
			}
			
		});
	}

	@Override
	public void foundAllExplanations() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void foundExplanation(Set<OWLAxiom> explanation) {
		System.out.println(explanation);
		
	}

	@Override
	public boolean isCancelled() {
		return isCanceled;
	}
	
	
}

class AngledLinesWindowsCornerIcon implements Icon {
	  private static final Color WHITE_LINE_COLOR = new Color(255, 255, 255);

	  private static final Color GRAY_LINE_COLOR = new Color(172, 168, 153);
	  private static final int WIDTH = 13;

	  private static final int HEIGHT = 13;

	  public int getIconHeight() {
	    return WIDTH;
	  }

	  public int getIconWidth() {
	    return HEIGHT;
	  }

	  public void paintIcon(Component c, Graphics g, int x, int y) {

	    g.setColor(WHITE_LINE_COLOR);
	    g.drawLine(0, 12, 12, 0);
	    g.drawLine(5, 12, 12, 5);
	    g.drawLine(10, 12, 12, 10);

	    g.setColor(GRAY_LINE_COLOR);
	    g.drawLine(1, 12, 12, 1);
	    g.drawLine(2, 12, 12, 2);
	    g.drawLine(3, 12, 12, 3);

	    g.drawLine(6, 12, 12, 6);
	    g.drawLine(7, 12, 12, 7);
	    g.drawLine(8, 12, 12, 8);

	    g.drawLine(11, 12, 12, 11);
	    g.drawLine(12, 12, 12, 12);

	  }
}

class CancelButton extends JButton {
	  /**
	 * 
	 */
	private static final long serialVersionUID = -8903797519798252577L;
	public CancelButton(String label) {
	    super(label);

	// These statements enlarge the button so that it 
	// becomes a circle rather than an oval.
	    Dimension size = getPreferredSize();
	    size.width = size.height = Math.max(size.width, 
	      size.height);
	    setPreferredSize(size);

	// This call causes the JButton not to paint 
	   // the background.
	// This allows us to paint a round background.
	    setContentAreaFilled(false);
	  }

	// Paint the round background and label.
	  protected void paintComponent(Graphics g) {
	    if (getModel().isArmed()) {
	// You might want to make the highlight color 
	   // a property of the RoundButton class.
	      g.setColor(Color.YELLOW);
	    } else {
	      g.setColor(Color.lightGray);
	    }
	    g.fillOval(0, 0, getSize().width-1, 
	      getSize().height-1);
	    
	    g.setColor(Color.white);
        g.drawLine(2, 2, 9 , 9);
        g.drawLine(2, 9, 9 , 2);

	// This call will paint the label and the 
	   // focus rectangle.
	    super.paintComponent(g);
	  }

	// Paint the border of the button using a simple stroke.
	  protected void paintBorder(Graphics g) {
	    g.setColor(Color.lightGray);
	    g.drawOval(0, 0, getSize().width-1, 
	      getSize().height-1);
	  }

	// Hit detection.
	  Shape shape;
	  public boolean contains(int x, int y) {
	// If the button has changed size, 
	   // make a new shape object.
	    if (shape == null || 
	      !shape.getBounds().equals(getBounds())) {
	      shape = new Ellipse2D.Float(0, 0, 
	        getWidth(), getHeight());
	    }
	    return shape.contains(x, y);
	  }
}

