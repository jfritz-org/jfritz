/*
 *
 * Created on 06.05.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import java.util.Vector;
import javax.swing.ImageIcon;
import java.awt.Toolkit;

/**
 * Shows a phone book dialog in which the entries can be edited.
 *
 * @author Robert Palmer
 *
 * TODO: Tabelle mit Einträgen
 * - Eintrag suchen
 * - Reverse Lookup
 * - Eintäge importieren (Outlook, Evolution)
 * - Sortierung der Einträge
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
	public PhoneBookDialog(JFritzWindow owner, JFritz jfritz) throws HeadlessException {
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

	private void createTable(){
		AbstractTableModel model = new AbstractTableModel() {

			public int getRowCount() {
				return personList.size();
			}

			public int getColumnCount() {
				return 12;
			}

			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return messages.getString("firstName");
				case 1:
					return messages.getString("middleName");
				case 2:
					return messages.getString("lastName");
				case 3:
					return messages.getString("homeTelephoneNumber");
				case 4:
					return messages.getString("mobileTelephoneNumber");
				case 5:
					return messages.getString("businessTelephoneNumber");
				case 6:
					return messages.getString("otherTelephoneNumber");
				case 7:
					return messages.getString("emailAddress");
				case 8:
					return messages.getString("street");
				case 9:
					return messages.getString("postalCode");
				case 10:
					return messages.getString("city");
				case 11:
					return messages.getString("category");

				default:
					return null;
				}
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				Person currentEntry = (Person) personList.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return currentEntry.getFirstName();
				case 1:
					return currentEntry.getMiddleName();
				case 2:
					return currentEntry.getLastName();
				case 3:
					return currentEntry.getHomeTelNumber();
				case 4:
					return currentEntry.getMobileTelNumber();
				case 5:
					return currentEntry.getBusinessTelNumber();
				case 6:
					return currentEntry.getOtherTelNumber();
				case 7:
					return currentEntry.getEmailAddress();
				case 8:
					return currentEntry.getStreet();
				case 9:
					return currentEntry.getPostalCode();
				case 10:
					return currentEntry.getCity();
				case 11:
					return currentEntry.getCategory();
				default:
					return null;

				}
			}

			/**
			 * Sets a value to a specific position
			 */
			public void setValueAt(Object object, int rowIndex, int columnIndex) {
				if (rowIndex < table.getRowCount()) {
					Person currentEntry = (Person) personList.get(rowIndex);

					switch (columnIndex) {
					case 0:
						currentEntry.setFirstName(object.toString());
						break;
					case 1:
						currentEntry.setMiddleName(object.toString());
						break;
					case 2:
						currentEntry.setLastName(object.toString());
						break;
					case 3:
						currentEntry.setHomeTelNumber(object.toString());
						break;
					case 4:
						currentEntry.setMobileTelNumber(object.toString());
						break;
					case 5:
						currentEntry.setBusinessTelNumber(object.toString());
						break;
					case 6:
						currentEntry.setOtherTelNumber(object.toString());
						break;
					case 7:
						currentEntry.setEmailAddress(object.toString());
						break;
					case 8:
						currentEntry.setStreet(object.toString());
						break;
					case 9:
						currentEntry.setPostalCode(object.toString());
						break;
					case 10:
						currentEntry.setCity(object.toString());
						break;
					case 11:
						currentEntry.setCategory(object.toString());
						break;
					}
					fireTableCellUpdated(rowIndex, columnIndex);
				}
			}

		};
		table = new JTable(model) {
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
		if (table.getRowCount() != 0) { table.setRowSelectionInterval(0, 0); }

		// Edit nur durch Doppelklick, ansonsten hier auskommentieren und erweitern
/**		table.getColumnModel().getColumn(0).setCellEditor(
				new ParticipantCellEditor());
		table.getColumnModel().getColumn(1).setCellEditor(
				new ParticipantCellEditor());
		table.getColumnModel().getColumn(2).setCellEditor(
				new ParticipantCellEditor());
**/
	}

	/**
	 * @param owner
	 */
	private void drawDialog() {
		super.dialogInit();
		createTable();
		setTitle("Phonebook");
//		setTitle(messages.getString("phonebook"));
		setModal(true);
		setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		JPanel bottomPane = new JPanel();
		JPanel topPane = new JPanel();
		JPanel centerPane = new JPanel();

		saveButton = new JButton("Speichern");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        saveButton_actionPerformed(e);
		      }
		    });

		cancelButton = new JButton("Abbruch");
		cancelButton.addActionListener(new java.awt.event.ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        cancelButton_actionPerformed(e);
		      }
		    });
		newButton = new JButton("Hinzufügen");
		newButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
		getClass().getResource(
				"/de/moonflower/jfritz/resources/images/add.png"))));
		newButton.addActionListener(new java.awt.event.ActionListener() {
		      public void actionPerformed(ActionEvent e) {
		        newButton_actionPerformed(e);
		      }
		    });
		delButton = new JButton("Löschen");
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

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		//panel.
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		panel.add(bottomPane, BorderLayout.SOUTH);
		getContentPane().add(panel);

		setSize(new Dimension(800,350));
	}

    void saveButton_actionPerformed(ActionEvent e){
    	jfritz.getPhonebook().updatePersons(personList);
    	jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
    }

	void cancelButton_actionPerformed(ActionEvent e){
    	this.dispose();
    }

	void newButton_actionPerformed(ActionEvent e){
		Person newEntry = new Person("New","","Entry","","","","","","","","","");
		personList.add(newEntry);
		AbstractTableModel model = (AbstractTableModel) table
		.getModel();
		model.fireTableChanged(null);
    }

	void deleteButton_actionPerformed(ActionEvent e){
	int row = table.getSelectedRow();
	if (row >= 0) {
		AbstractTableModel model = (AbstractTableModel) table
		.getModel();
		personList.remove(row);

		//		model.fireTableDataChanged();
		model.fireTableRowsDeleted(row,row);
		if (table.getRowCount() != 0) {
			if (table.getRowCount() > row) {table.setRowSelectionInterval(row, row); }
			else { table.setRowSelectionInterval(row-1, row-1); }
		}
	}
	}


	public boolean showDialog() {
		setVisible(true);
		return true;
	}
}
