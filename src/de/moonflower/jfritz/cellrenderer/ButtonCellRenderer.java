package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.UIManager;

public class ButtonCellRenderer extends JButton implements TableCellRenderer {

	public static final long serialVersionUID = 100;

	public ButtonCellRenderer(){
		setOpaque(true);
	}

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if(isSelected){
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		}else{
			setForeground(table.getForeground());
			setBackground(UIManager.getColor("Button.background"));
		}
		setText((value == null) ? "" : value.toString());
		return this;

	}

}
