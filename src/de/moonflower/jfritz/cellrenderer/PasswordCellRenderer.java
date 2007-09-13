package de.moonflower.jfritz.cellrenderer;

import java.awt.Component;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.JPasswordField;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import javax.swing.UIManager;

public class PasswordCellRenderer extends JPasswordField implements
		TableCellRenderer {

	public static final long serialVersionUID = 100;

	protected static Border normalBorder = new EmptyBorder(1,1,1,1);

	public Component getTableCellRendererComponent(JTable table, Object value,
			boolean isSelected, boolean hasFocus, int row, int column) {

		if(isSelected){
			setForeground(table.getSelectionForeground());
			setBackground(table.getSelectionBackground());
		}else{
			setForeground(table.getForeground());
			setBackground(table.getBackground());
		}

		if(hasFocus){
			setBorder(UIManager.getBorder("Table.focusCellHighlightBorder"));
		}else{
			setBorder(normalBorder);
		}

		setText((String)value);
		return this;
	}

}
