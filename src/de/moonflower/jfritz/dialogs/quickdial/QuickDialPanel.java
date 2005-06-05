/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author Arno Willig
 *
 */
public class QuickDialPanel extends JPanel implements ActionListener {

	private JFritz jfritz;

	private QuickDialTableModel dataModel;

	private JTable quickdialtable;

	public QuickDialPanel(JFritz jfritz) {
		this.jfritz = jfritz;
		setLayout(new BorderLayout());
		dataModel = new QuickDialTableModel(jfritz);
		// dataModel.getQuickDialDataFromFritzBox();
		dataModel.loadFromXMLFile(JFritz.QUICKDIALS_FILE);
		add(createQuickDialToolBar(), BorderLayout.NORTH);
		add(createQuickDialTable(), BorderLayout.CENTER);
	}

	public JToolBar createQuickDialToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);
		JButton okButton = new JButton("Okay");
		okButton.addActionListener(this);

		JButton cancelButton = new JButton("Abbruch");
		cancelButton.addActionListener(this);

		JButton newButton = new JButton("Neue Kurzwahl");
		newButton.setActionCommand("addSIP");
		newButton.addActionListener(this);
		newButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/modify.png"))));
		JButton delButton = new JButton("Kurzwahl löschen");
		delButton.setActionCommand("deleteSIP");
		delButton.addActionListener(this);
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));

		JButton b1 = new JButton("Von der Box holen");
		b1.setActionCommand("fetchSIP");
		b1.addActionListener(this);
		// b1.setIcon(new
		// ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/de/moonflower/jfritz/resources/images/import.png"))));

		JButton b2 = new JButton("Auf die Box speichern");
		b2.setActionCommand("storeSIP");
		b2.addActionListener(this);
		// b2.setIcon(new
		// ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/de/moonflower/jfritz/resources/images/export.png"))));

		toolBar.add(newButton);
		toolBar.add(delButton);
		toolBar.add(b1);
		toolBar.add(b2);

		return toolBar;
	}

	public JScrollPane createQuickDialTable() {
		quickdialtable = new JTable(dataModel) {
			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
					c.setBackground(new Color(255, 255, 200));
				} else if (!isCellSelected(rowIndex, vColIndex)) {
					c.setBackground(getBackground());
				} else {
					c.setBackground(new Color(204, 204, 255));
				}
				return c;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return true;
			}

		};
		quickdialtable.setRowHeight(24);
		quickdialtable.setFocusable(false);
		quickdialtable.setAutoCreateColumnsFromModel(true);
		quickdialtable.setColumnSelectionAllowed(false);
		quickdialtable.setCellSelectionEnabled(false);
		quickdialtable.setRowSelectionAllowed(true);
		quickdialtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (quickdialtable.getRowCount() != 0)
			quickdialtable.setRowSelectionInterval(0, 0);

		// TODO new QuickDialFieldCellEditor());
/*
		quickdialtable.getColumnModel().getColumn(0).setCellEditor(
				new TextFieldCellEditor());
		quickdialtable.getColumnModel().getColumn(1).setCellEditor(
				new TextFieldCellEditor());
		quickdialtable.getColumnModel().getColumn(2).setCellEditor(
				new TextFieldCellEditor());
		quickdialtable.getColumnModel().getColumn(3).setCellEditor(
				new TextFieldCellEditor());
*/
		return new JScrollPane(quickdialtable);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand() == "deleteSIP") {
			int row = quickdialtable.getSelectedRow();
			if (row >= 0) {
				dataModel.remove(row);
				dataModel.fireTableRowsDeleted(row, row);
			}
		} else if (e.getActionCommand() == "addSIP") {
			dataModel.addEntry(new QuickDial("99", "?", "?", "?"));
			dataModel.fireTableDataChanged();
		} else if (e.getActionCommand() == "fetchSIP") {
			// FIXME: Preserve description data!
			dataModel.getQuickDialDataFromFritzBox();
			dataModel.fireTableDataChanged();
		} else if (e.getActionCommand() == "storeSIP") {
			Debug.err("Not yet implemented");
			JOptionPane.showMessageDialog(null,
					"Geduld!\n\nFunktion noch nicht implementiert.");
		}

			}
}
