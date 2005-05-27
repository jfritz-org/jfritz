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
import java.awt.GridLayout;

import javax.swing.ImageIcon;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JRadioButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.ButtonGroup;

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

	JLabel labelFirstName, labelMiddleName, labelLastName,
			labelStreet, labelPostalCode, labelCity,
			labelHomeNumber, labelMobileNumber,
			labelBusinessNumber, labelOtherNumber,
			labelEmail, labelStandardNumber, textStandardNumber;
	JTextField textFieldFirstName, textFieldMiddleName, textFieldLastName,
			textFieldStreet, textFieldPostalCode, textFieldCity,
			textFieldHomeNumber, textFieldMobileNumber,
			textFieldBusinessNumber, textFieldOtherNumber,
			textFieldEmail;

	JButton setStandardHomeNumber, setStandardMobileNumber,
			setStandardBusinessNumber, setStandardOtherNumber;

	Vector oldPersonList;

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

		oldPersonList = new Vector();
		// make a copy of persons in Phonebook
		// and do operations on copied entries
		oldPersonList = (Vector) jfritz.getPhonebook().getPersons().clone();

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
				return false;
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

		JPanel panelLabelsAndTextFields = new JPanel();
		panelLabelsAndTextFields.setLayout(new GridLayout(11,2));
		System.out.println("Gridlayout");

		labelFirstName = new JLabel(messages.getString("firstName")+": ");
		textFieldFirstName = new JTextField();
		labelMiddleName = new JLabel(messages.getString("middleName")+": ");
		textFieldMiddleName = new JTextField();
		labelLastName = new JLabel(messages.getString("lastName")+": ");
		textFieldLastName = new JTextField();
		labelStreet = new JLabel(messages.getString("street")+": ");
		textFieldStreet = new JTextField();
		labelPostalCode = new JLabel(messages.getString("postalCode")+": ");
		textFieldPostalCode = new JTextField();
		labelCity = new JLabel(messages.getString("city")+": ");
		textFieldCity = new JTextField();
		labelHomeNumber = new JLabel(messages.getString("homeTelephoneNumber")+": ");
		textFieldHomeNumber = new JTextField();
		setStandardHomeNumber = new JButton("Standard");
		labelMobileNumber = new JLabel(messages.getString("mobileTelephoneNumber")+": ");
		textFieldMobileNumber = new JTextField();
		setStandardMobileNumber = new JButton("Standard");
		labelBusinessNumber = new JLabel(messages.getString("businessTelephoneNumber")+": ");
		textFieldBusinessNumber = new JTextField();
		setStandardBusinessNumber = new JButton("Standard");
		labelOtherNumber = new JLabel(messages.getString("otherTelephoneNumber")+": ");
		textFieldOtherNumber = new JTextField();
		setStandardOtherNumber = new JButton("Standard");

		labelEmail = new JLabel(messages.getString("emailAddress")+": ");
		textFieldEmail = new JTextField();
		labelStandardNumber = new JLabel("Standard Nummer"+": ");
		textStandardNumber = new JLabel("Not Set");

	    JRadioButton radioButtonHome = new JRadioButton("");
	    radioButtonHome.setActionCommand("standardHome");
	    JRadioButton radioButtonMobile = new JRadioButton("");
	    radioButtonMobile.setActionCommand("standardMobile");
	    JRadioButton radioButtonBusiness = new JRadioButton("");
	    radioButtonBusiness.setActionCommand("standardBusiness");
	    JRadioButton radioButtonOther = new JRadioButton("");
	    radioButtonOther.setActionCommand("standardOther");

		panelLabelsAndTextFields.add(labelFirstName);
		panelLabelsAndTextFields.add(textFieldFirstName);

		panelLabelsAndTextFields.add(labelMiddleName);
		panelLabelsAndTextFields.add(textFieldMiddleName);

		panelLabelsAndTextFields.add(labelLastName);
		panelLabelsAndTextFields.add(textFieldLastName);

		panelLabelsAndTextFields.add(labelStreet);
		panelLabelsAndTextFields.add(textFieldStreet);

		panelLabelsAndTextFields.add(labelPostalCode);
		panelLabelsAndTextFields.add(textFieldPostalCode);

		panelLabelsAndTextFields.add(labelCity);
		panelLabelsAndTextFields.add(textFieldCity);

		JPanel homeNumberPanel = new JPanel();
		homeNumberPanel.setLayout(new BoxLayout(homeNumberPanel,BoxLayout.X_AXIS));
		homeNumberPanel.add(radioButtonHome);
		homeNumberPanel.add(labelHomeNumber);

		panelLabelsAndTextFields.add(homeNumberPanel);
		panelLabelsAndTextFields.add(textFieldHomeNumber);

		JPanel mobileNumberPanel = new JPanel();
		mobileNumberPanel.setLayout(new BoxLayout(mobileNumberPanel,BoxLayout.X_AXIS));
		mobileNumberPanel.add(radioButtonMobile);
		mobileNumberPanel.add(labelMobileNumber);
		panelLabelsAndTextFields.add(mobileNumberPanel);
		panelLabelsAndTextFields.add(textFieldMobileNumber);

		JPanel businessNumberPanel = new JPanel();
		businessNumberPanel.setLayout(new BoxLayout(businessNumberPanel,BoxLayout.X_AXIS));
		businessNumberPanel.add(radioButtonBusiness);
		businessNumberPanel.add(labelBusinessNumber);
		panelLabelsAndTextFields.add(businessNumberPanel);
		panelLabelsAndTextFields.add(textFieldBusinessNumber);

		JPanel otherNumberPanel = new JPanel();
		otherNumberPanel.setLayout(new BoxLayout(otherNumberPanel,BoxLayout.X_AXIS));
		otherNumberPanel.add(radioButtonOther);
		otherNumberPanel.add(labelOtherNumber);
		panelLabelsAndTextFields.add(otherNumberPanel);
		panelLabelsAndTextFields.add(textFieldOtherNumber);
/**		panelLabelsAndTextFields.add(labelEmail);
		panelLabelsAndTextFields.add(textFieldEmail);
*/
		centerPane.setLayout(new BoxLayout(centerPane,BoxLayout.Y_AXIS));
		centerPane.add(new JScrollPane(table),BorderLayout.NORTH);
		centerPane.add(panelLabelsAndTextFields,BorderLayout.CENTER);

		getContentPane().add(topPane, BorderLayout.NORTH);
		getContentPane().add(centerPane, BorderLayout.CENTER);
		getContentPane().add(bottomPane, BorderLayout.SOUTH);

	    ButtonGroup group = new ButtonGroup();
	    group.add(radioButtonHome);
	    group.add(radioButtonMobile);
	    group.add(radioButtonBusiness);
	    group.add(radioButtonOther);

		setSize(new Dimension(480, 500));
	}

	void saveButton_actionPerformed(ActionEvent e) {
		jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
		oldPersonList = (Vector) jfritz.getPhonebook().getPersons().clone();
	}

	void cancelButton_actionPerformed(ActionEvent e) {
		jfritz.getPhonebook().updatePersons(oldPersonList);
		this.dispose();
	}

	void newButton_actionPerformed(ActionEvent e) {
		Person newEntry = new Person("New", "", "Entry", "", "", "", "", "", "",
				"", "123", "", "");
		AbstractTableModel model = (AbstractTableModel) table.getModel();
		jfritz.getPhonebook().getPersons().add(newEntry);
		model.fireTableRowsInserted(model.getRowCount(),model.getRowCount());
	}

	void deleteButton_actionPerformed(ActionEvent e) {
		int row = table.getSelectedRow();
		if (row >= 0) {
			AbstractTableModel model = (AbstractTableModel) table.getModel();
			jfritz.getPhonebook().getPersons().remove(row);

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
