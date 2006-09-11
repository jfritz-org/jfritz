/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.callerlist.CallCellEditor;
import de.moonflower.jfritz.cellrenderer.CallTypeDateCellRenderer;

/**
 * @author Arno Willig
 *
 */
public class PhoneBookTable extends JTable {
	private static final long serialVersionUID = 1;

	/**
	 *
	 */
	public PhoneBookTable() {
		setModel(JFritz.getPhonebook());
		setRowHeight(24);
		setFocusable(false);
		setAutoCreateColumnsFromModel(true);
		setColumnSelectionAllowed(false);
		setCellSelectionEnabled(false);
		setRowSelectionAllowed(true);
		setFocusable(true);
		setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		final PhoneBookTable table = this;
		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					// remove selection
					table.clearSelection();
					JFritz.getJframe().getPhoneBookPanel().setStatus();
				}
				else if (e.getKeyCode() == KeyEvent.VK_DELETE) {
					// Delete selected entries
					JFritz.getJframe().getPhoneBookPanel().removeSelectedPersons();
				}
			}
		});

		addKeyListener(keyListener);

		// setDefaultRenderer(Call.class, new CallTypeDateCellRenderer());

		getColumnModel().getColumn(5).setCellRenderer(
				new CallTypeDateCellRenderer());
		getColumnModel().getColumn(0).setMinWidth(50);
		getColumnModel().getColumn(0).setMaxWidth(50);

		getColumnModel().getColumn(2).setCellEditor(new CallCellEditor());

		getTableHeader().addMouseListener(new ColumnHeaderListener(getModel()));

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
