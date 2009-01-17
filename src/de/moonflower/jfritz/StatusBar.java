package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.JPanel;

public class StatusBar extends JPanel {

	private Vector<StatusBarPanel> leftPaneVector;
	private Vector<StatusBarPanel> centerPaneVector;
	private Vector<StatusBarPanel> rightPaneVector;

	private JPanel iconPanel;
	private JPanel statusPanel;
	private JPanel mainPanel;

	/**
	 *
	 */
	private static final long serialVersionUID = -3883780431076770249L;

	public StatusBar()
	{
		super();
		leftPaneVector = new Vector<StatusBarPanel>();
		centerPaneVector = new Vector<StatusBarPanel>();
		rightPaneVector = new Vector<StatusBarPanel>();
		statusPanel = new JPanel();
		mainPanel = new JPanel();
		iconPanel = new JPanel();
	}

	public void registerStatusIcon(StatusBarPanel panel)
	{
		if (!rightPaneVector.contains(panel))
		{
			rightPaneVector.add(panel);
		}
		Collections.sort((Vector<StatusBarPanel>)rightPaneVector);
		updateStatusBar();
	}

	public void unregisterStatusIcon(StatusBarPanel panel)
	{
		if ( rightPaneVector.contains(panel))
		{
			rightPaneVector.remove(panel);
		}
		updateStatusBar();
	}

	public void registerFixStatusPanel(StatusBarPanel panel)
	{
		if (!leftPaneVector.contains(panel))
		{
			leftPaneVector.add(panel);
		}
		Collections.sort((Vector<StatusBarPanel>)leftPaneVector);
		updateStatusBar();
	}

	public void unregisterFixStatusPanel(StatusBarPanel panel)
	{
		if ( leftPaneVector.contains(panel))
		{
			leftPaneVector.remove(panel);
		}
		updateStatusBar();
	}

	public void registerDynamicStatusPanel(StatusBarPanel panel)
	{
		if (!centerPaneVector.contains(panel))
		{
			centerPaneVector.add(panel);
		}
		Collections.sort((Vector<StatusBarPanel>)centerPaneVector);
		updateStatusBar();
	}

	public void unregisterDynamicStatusPanel(StatusBarPanel panel)
	{
		if ( centerPaneVector.contains(panel))
		{
			centerPaneVector.remove(panel);
		}
		updateStatusBar();
	}

	public void updateStatusBar()
	{
		this.removeAll();
		Enumeration<StatusBarPanel> en = leftPaneVector.elements();
		while ( en.hasMoreElements() )
		{
			statusPanel.add(en.nextElement());
		}

		en = centerPaneVector.elements();
		while ( en.hasMoreElements() )
		{
			mainPanel.add(en.nextElement());
		}

		en = rightPaneVector.elements();
		while ( en.hasMoreElements() )
		{
			iconPanel.add(en.nextElement());
		}

		this.setLayout(new BorderLayout());
		this.add(statusPanel, BorderLayout.WEST);
		this.add(mainPanel, BorderLayout.CENTER);
		this.add(iconPanel, BorderLayout.EAST);
		validate();
	}

	public void refresh()
	{
		boolean centerVisible = false;
		Enumeration<StatusBarPanel> en = centerPaneVector.elements();
		while ( en.hasMoreElements() )
		{
			StatusBarPanel panel = ((StatusBarPanel)en.nextElement());
			if ( panel.isVisible() )
			{
				centerVisible = true;
				break;
			}
		}

		mainPanel.setVisible(centerVisible);
		validate();
		repaint();
	}
}
