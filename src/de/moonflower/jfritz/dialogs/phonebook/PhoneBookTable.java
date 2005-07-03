/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.cellrenderer.CallTypeDateCellRenderer;

/**
 * @author Arno Willig
 *
 */
public class PhoneBookTable extends JTable {
	private JFritz jfritz;

	/**
	 *
	 */
	public PhoneBookTable(JFritz jfritz) {
		this.jfritz = jfritz;
		setModel(jfritz.getPhonebook());
		setRowHeight(24);
		setFocusable(false);
		setAutoCreateColumnsFromModel(true);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);


		// setDefaultRenderer(Call.class, new CallTypeDateCellRenderer());

		getColumnModel().getColumn(5).setCellRenderer(
				new CallTypeDateCellRenderer());
		getColumnModel().getColumn(0).setMinWidth(50);
		getColumnModel().getColumn(0).setMaxWidth(50);
	}

	public Component prepareRenderer(TableCellRenderer renderer, int rowIndex,
			int vColIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, vColIndex);
		if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(new Color(255, 255, 200));
		} else if (!isCellSelected(rowIndex, vColIndex)) {
			c.setBackground(getBackground());
		} else {
			c.setBackground(new Color(204, 204, 255));
		}
		return c;
	}
}
