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
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;

/**
 * Main panel for QuickDials
 * @author Arno Willig
 */
public class QuickDialPanel extends JPanel implements ActionListener,
		ListSelectionListener {

	private JFritz jfritz;

	private QuickDials dataModel;

	private JTable quickdialtable;

	private JButton addButton, delButton;

	public QuickDialPanel(JFritz jfritz) {
		this.jfritz = jfritz;
		setLayout(new BorderLayout());
		dataModel = new QuickDials(jfritz);
		// dataModel.getQuickDialDataFromFritzBox();
		dataModel.loadFromXMLFile(JFritz.QUICKDIALS_FILE);
		add(createQuickDialToolBar(), BorderLayout.NORTH);
		add(createQuickDialTable(), BorderLayout.CENTER);
	}

	private JToolBar createQuickDialToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);

		addButton = new JButton(jfritz.getMessages().getString("new_quickdial"));
		addButton.setActionCommand("addSIP");
		addButton.addActionListener(this);
		addButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/add.png"))));

		delButton = new JButton(jfritz.getMessages().getString("delete_quickdial"));
		delButton.setActionCommand("deleteSIP");
		delButton.addActionListener(this);
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));

		JButton fetchButton = new JButton(jfritz.getMessages().getString("fetch_from_box"));
		fetchButton.setActionCommand("fetchSIP");
		fetchButton.addActionListener(this);

		JButton storeButton = new JButton(jfritz.getMessages().getString("store_to_box"));
		storeButton.setActionCommand("storeSIP");
		storeButton.addActionListener(this);

		toolBar.add(addButton);
		toolBar.add(delButton);
		toolBar.add(fetchButton);
		toolBar.add(storeButton);

		return toolBar;
	}

	private JScrollPane createQuickDialTable() {
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
		quickdialtable.getSelectionModel().addListSelectionListener(this);

		if (quickdialtable.getRowCount() != 0)
			quickdialtable.setRowSelectionInterval(0, 0);

		// TODO new QuickDialFieldCellEditor());
		/*
		 * quickdialtable.getColumnModel().getColumn(0).setCellEditor( new
		 * TextFieldCellEditor());
		 * quickdialtable.getColumnModel().getColumn(1).setCellEditor( new
		 * TextFieldCellEditor());
		 * quickdialtable.getColumnModel().getColumn(2).setCellEditor( new
		 * TextFieldCellEditor());
		 * quickdialtable.getColumnModel().getColumn(3).setCellEditor( new
		 * TextFieldCellEditor());
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
			updateButtons();
		} else if (e.getActionCommand() == "fetchSIP") {
			dataModel.getQuickDialDataFromFritzBox();
			dataModel.fireTableDataChanged();
		} else if (e.getActionCommand() == "storeSIP") {
			Debug.err("Not yet implemented");
			JOptionPane.showMessageDialog(null,
					"Geduld!\n\nFunktion noch nicht implementiert.");
		}

	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			updateButtons();
		}

	}

	/**
	 * @return Returns the dataModel.
	 */
	public final QuickDials getDataModel() {
		return dataModel;
	}

	public void updateButtons() {

		delButton.setEnabled(quickdialtable.getSelectedRow() > -1
				&& dataModel.getRowCount() > 0);

		Enumeration en = dataModel.getQuickDials().elements();
		boolean addEnabled = true;
		while (en.hasMoreElements()) {
			String nr = ((QuickDial) en.nextElement()).getQuickdial();
			if (nr.equals("99") || (nr.equals(""))) {
				addEnabled = false;
				break;
			}
		}
		addButton.setEnabled(addEnabled);
	}
}
