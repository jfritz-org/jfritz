/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.window.JFritzWindow;

/**
 * Shows a phone book dialog in which the entries can be edited.
 *
 * @author Robert Palmer
 *
 * TODO: Tabelle mit Einträgen - Eintrag suchen - Reverse Lookup - Eintäge
 * importieren (Outlook, Evolution) - Sortierung der Einträge
 *
 */

public class PhoneBookDialog extends JDialog {

	JFritz jfritz;

	ResourceBundle messages;

	JButton saveButton, cancelButton, newButton, delButton;

	JTable table;

	Vector personList;

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public PhoneBookDialog(JFritzWindow owner, JFritz jfritz)
			throws HeadlessException {
		super(owner, true);
		if (owner != null) {
			setLocationRelativeTo(owner);
			this.messages = owner.getMessages();
		}
		this.jfritz = jfritz;

		personList = new Vector();
		// make a copy of persons in Phonebook
		// and do operations on copied entries
		personList = (Vector) jfritz.getPhonebook().getPersons().clone();

		drawDialog();
	}

	private void createTable() {
		table = new JTable(jfritz.getPhonebook()) {
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
		table.setRowHeight(24);
		table.setFocusable(false);
		table.setAutoCreateColumnsFromModel(false);
		table.setColumnSelectionAllowed(false);
		table.setCellSelectionEnabled(false);
		table.setRowSelectionAllowed(true);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		if (table.getRowCount() != 0) {
			table.setRowSelectionInterval(0, 0);
		}

		// Edit nur durch Doppelklick, ansonsten hier auskommentieren und
		// erweitern
		/**
		 * table.getColumnModel().getColumn(0).setCellEditor( new
		 * ParticipantCellEditor());
		 * table.getColumnModel().getColumn(1).setCellEditor( new
		 * ParticipantCellEditor());
		 * table.getColumnModel().getColumn(2).setCellEditor( new
		 * ParticipantCellEditor());
		 */
	}

	private void drawDialog() {
		super.dialogInit();
		createTable();
		setTitle(messages.getString("phonebook"));
		setModal(true);
		setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		JPanel topPane = new JPanel();
		JPanel centerPane = new JPanel();
		JPanel bottomPane = new JPanel();

		saveButton = new JButton("Speichern"); // TODO I18N
		saveButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveButton_actionPerformed(e);
			}
		});

		cancelButton = new JButton("Abbruch");// TODO I18N
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButton_actionPerformed(e);
			}
		});
		newButton = new JButton("Hinzufügen");// TODO I18N
		newButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/add.png"))));
		newButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newButton_actionPerformed(e);
			}
		});
		delButton = new JButton("Löschen");// TODO I18N
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));
		delButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				deleteButton_actionPerformed(e);
			}
		});
		topPane.add(newButton);
		topPane.add(delButton);

		bottomPane.add(saveButton);
		bottomPane.add(cancelButton);

		getContentPane().add(topPane, BorderLayout.NORTH);
		getContentPane().add(new JScrollPane(table), BorderLayout.CENTER);
		getContentPane().add(bottomPane, BorderLayout.SOUTH);

		setSize(new Dimension(800, 350));
	}

	void saveButton_actionPerformed(ActionEvent e) {
		jfritz.getPhonebook().updatePersons(personList);
		jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		this.dispose();
	}

	void newButton_actionPerformed(ActionEvent e) {
		Person newEntry = new Person("New", "", "Entry", "", "", "", "", "",
				"", "", "", "");
		personList.add(newEntry);
		AbstractTableModel model = (AbstractTableModel) table.getModel();
		model.fireTableChanged(null);
	}

	void deleteButton_actionPerformed(ActionEvent e) {
		int row = table.getSelectedRow();
		if (row >= 0) {
			AbstractTableModel model = (AbstractTableModel) table.getModel();
			personList.remove(row);

			//		model.fireTableDataChanged();
			model.fireTableRowsDeleted(row, row);
			if (table.getRowCount() != 0) {
				if (table.getRowCount() > row) {
					table.setRowSelectionInterval(row, row);
				} else {
					table.setRowSelectionInterval(row - 1, row - 1);
				}
			}
		}
	}

	public boolean showDialog() {
		setVisible(true);
		return true;
	}
}
