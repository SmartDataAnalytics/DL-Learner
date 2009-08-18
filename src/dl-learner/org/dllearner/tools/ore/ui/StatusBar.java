package org.dllearner.tools.ore.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.SystemColor;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.mindswap.pellet.utils.progress.ProgressMonitor;

public class StatusBar extends JPanel implements ProgressMonitor{
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

	public StatusBar() {
		infoLabel = new JLabel("");
		progressBar = new JProgressBar();
		progressBar.setStringPainted(true);
		setLayout(new BorderLayout());
		setPreferredSize(new Dimension(10, 23));

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(new JLabel(new AngledLinesWindowsCornerIcon()),
				BorderLayout.SOUTH);
		rightPanel.setOpaque(false);
		JPanel leftPanel = new JPanel(new FlowLayout());
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
		progressBar.setIndeterminate(b);
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
		return false;
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
