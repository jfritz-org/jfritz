package de.moonflower.jfritz.JFritzEvent.gui;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

class TableButtonRenderer implements TableCellRenderer {
	  public Component getTableCellRendererComponent(JTable table, Object value,
	      boolean isSelected, boolean hasFocus, int row, int column) {
	    if (value == null)
	      return null;
	    return (Component) value;
	  }
	}
