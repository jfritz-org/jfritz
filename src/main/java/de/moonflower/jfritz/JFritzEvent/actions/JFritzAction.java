package de.moonflower.jfritz.JFritzEvent.actions;

import javax.swing.JPanel;

import org.jdom.Element;

import de.moonflower.jfritz.JFritzEvent.gui.ParameterClickedEvent;

public interface JFritzAction extends Runnable,Cloneable, ParameterClickedEvent {

	public  String getName();

	public  String getDescription();

	public  void setDescription(String description);

	public  JPanel getConfigPanel();

	public  String toString();

	public  JFritzAction clone();

	public  void run();

	public  void loadSettings(Element settings);

	public  Element saveSettings();
}
