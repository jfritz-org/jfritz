package de.moonflower.jfritz.dialogs.config;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class LanguageComboBoxRenderer extends JLabel implements ListCellRenderer {
	private static final long serialVersionUID = -1992780721039481940L;
	public LanguageComboBoxRenderer() {
		setOpaque(true);
		setHorizontalAlignment(LEFT);
		setVerticalAlignment(CENTER);
	}
	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		if (isSelected) {
			setBackground(list.getSelectionBackground());
			setForeground(list.getSelectionForeground());
		} else {
			setBackground(list.getBackground());
			setForeground(list.getForeground());
		}

		ImageIcon icon = (ImageIcon) value;
		setText(icon.getDescription());
		setIcon(icon);
		return this;
	}
}
