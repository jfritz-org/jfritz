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
import java.awt.Rectangle;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.window.JFritzWindow;

/**
 * Shows a phone book dialog in which the entries can be edited.
 *
 * @author Robert Palmer
 *
 * TODO: Tabelle mit Einträgen - Eintrag suchen - Reverse Lookup - Eintäge
 * importieren (Outlook, Evolution) - Sortierung der Einträge
 *
 * BUGS: XML Einträge werden bei Sonderzeichen (Ä,Ö,Ü) nicht richtig eingelesen
 */

public class PhoneBookDialog extends JDialog {

	private JFritz jfritz;

	private ResourceBundle messages;

	private JButton changeButton, newButton, delButton, closeButton;

	private JTable table;

	private AbstractTableModel model;

	private JLabel labelFirstName, labelMiddleName, labelLastName, labelStreet,
			labelPostalCode, labelCity, labelHomeNumber, labelMobileNumber,
			labelBusinessNumber, labelOtherNumber, labelEmail;

	private JTextField textFieldFirstName, textFieldMiddleName,
			textFieldLastName, textFieldStreet, textFieldPostalCode,
			textFieldCity, textFieldHomeNumber, textFieldMobileNumber,
			textFieldBusinessNumber, textFieldOtherNumber, textFieldEmail;

	private JRadioButton radioButtonHome, radioButtonMobile,
			radioButtonBusiness, radioButtonOther;

	private Vector oldPersonList;

	/**
	 * @param owner
	 * @param jfritz
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
		// make a copy of current entries in phonebook
		// to make changes undone
		oldPersonList = (Vector) jfritz.getPhonebook().getPersons().clone();

		drawDialog();
	}

	/**
	 * Creates Table for phonebook entries
	 *
	 */
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
		SelectionListener listener = new SelectionListener(this);
		table.getSelectionModel().addListSelectionListener(listener);
		table.getColumnModel().getSelectionModel().addListSelectionListener(
				listener);
		if (table.getRowCount() != 0) {
			table.setRowSelectionInterval(0, 0);
		}
		model = (AbstractTableModel) table.getModel();
	}

	/**
	 * Draws Table and Textfields for editing the table entries
	 *
	 */
	private void drawDialog() {
		super.dialogInit();
		setTitle(messages.getString("phonebook"));
		setModal(true);
		setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		JPanel topPane = new JPanel();
		JPanel centerPane = new JPanel();
		JPanel bottomPane = new JPanel();

		changeButton = new JButton("Ändern"); // TODO I18N
		changeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeButton_actionPerformed(e);
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
		closeButton = new JButton("Schließen"); // TODO I18N
//		closeButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
//				getClass().getResource(
//						"/de/moonflower/jfritz/resources/images/exit.png"))));
		closeButton.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				closeButton_actionPerformed(e);
			}
		});

		// TODO: closeButton to the right
		topPane.add(newButton);
		topPane.add(delButton);
		topPane.add(closeButton);

		bottomPane.add(changeButton);

		JPanel panelLabelsAndTextFields = new JPanel();
		panelLabelsAndTextFields.setLayout(new GridLayout(11, 2));

		labelFirstName = new JLabel(messages.getString("firstName") + ": ");
		textFieldFirstName = new JTextField();
		labelMiddleName = new JLabel(messages.getString("middleName") + ": ");
		textFieldMiddleName = new JTextField();
		labelLastName = new JLabel(messages.getString("lastName") + ": ");
		textFieldLastName = new JTextField();
		labelStreet = new JLabel(messages.getString("street") + ": ");
		textFieldStreet = new JTextField();
		labelPostalCode = new JLabel(messages.getString("postalCode") + ": ");
		textFieldPostalCode = new JTextField();
		labelCity = new JLabel(messages.getString("city") + ": ");
		textFieldCity = new JTextField();
		labelHomeNumber = new JLabel(messages.getString("homeTelephoneNumber")
				+ ": ");
		textFieldHomeNumber = new JTextField();
		labelMobileNumber = new JLabel(messages
				.getString("mobileTelephoneNumber")
				+ ": ");
		textFieldMobileNumber = new JTextField();
		labelBusinessNumber = new JLabel(messages
				.getString("businessTelephoneNumber")
				+ ": ");
		textFieldBusinessNumber = new JTextField();
		labelOtherNumber = new JLabel(messages
				.getString("otherTelephoneNumber")
				+ ": ");
		textFieldOtherNumber = new JTextField();

		/**
		 * labelEmail = new JLabel(messages.getString("emailAddress")+": ");
		 * textFieldEmail = new JTextField();
		 */

		/**
		 * Radiobuttons for setting of standard telephone number TODO: implement
		 * Setting of standard telephone number
		 */
		radioButtonHome = new JRadioButton();
		radioButtonMobile = new JRadioButton();
		radioButtonBusiness = new JRadioButton();
		radioButtonOther = new JRadioButton();
		createTable();

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
		homeNumberPanel.setLayout(new BoxLayout(homeNumberPanel,
				BoxLayout.X_AXIS));
		homeNumberPanel.add(radioButtonHome);
		homeNumberPanel.add(labelHomeNumber);

		panelLabelsAndTextFields.add(homeNumberPanel);
		panelLabelsAndTextFields.add(textFieldHomeNumber);

		JPanel mobileNumberPanel = new JPanel();
		mobileNumberPanel.setLayout(new BoxLayout(mobileNumberPanel,
				BoxLayout.X_AXIS));
		mobileNumberPanel.add(radioButtonMobile);
		mobileNumberPanel.add(labelMobileNumber);
		panelLabelsAndTextFields.add(mobileNumberPanel);
		panelLabelsAndTextFields.add(textFieldMobileNumber);

		JPanel businessNumberPanel = new JPanel();
		businessNumberPanel.setLayout(new BoxLayout(businessNumberPanel,
				BoxLayout.X_AXIS));
		businessNumberPanel.add(radioButtonBusiness);
		businessNumberPanel.add(labelBusinessNumber);
		panelLabelsAndTextFields.add(businessNumberPanel);
		panelLabelsAndTextFields.add(textFieldBusinessNumber);

		JPanel otherNumberPanel = new JPanel();
		otherNumberPanel.setLayout(new BoxLayout(otherNumberPanel,
				BoxLayout.X_AXIS));
		otherNumberPanel.add(radioButtonOther);
		otherNumberPanel.add(labelOtherNumber);
		panelLabelsAndTextFields.add(otherNumberPanel);
		panelLabelsAndTextFields.add(textFieldOtherNumber);
		/**
		 * panelLabelsAndTextFields.add(labelEmail);
		 * panelLabelsAndTextFields.add(textFieldEmail);
		 */
		centerPane.setLayout(new BoxLayout(centerPane, BoxLayout.Y_AXIS));
		centerPane.add(new JScrollPane(table), BorderLayout.NORTH);
		centerPane.add(panelLabelsAndTextFields, BorderLayout.CENTER);

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

	void changeButton_actionPerformed(ActionEvent e) {
		int row = table.getSelectedRow();
		Person changePerson = jfritz.getPhonebook().getPersonAt(row);
		changePerson.setFirstName(textFieldFirstName.getText());
		changePerson.setMiddleName(textFieldMiddleName.getText());
		changePerson.setLastName(textFieldLastName.getText());
		changePerson.setStreet(textFieldStreet.getText());
		changePerson.setPostalCode(textFieldPostalCode.getText());
		changePerson.setCity(textFieldCity.getText());
		changePerson.setHomeTelNumber(textFieldHomeNumber.getText());
		changePerson.setMobileTelNumber(textFieldMobileNumber.getText());
		changePerson.setBusinessTelNumber(textFieldBusinessNumber.getText());
		changePerson.setOtherTelNumber(textFieldOtherNumber.getText());

		if (radioButtonHome.isSelected()) {
			changePerson.setStandardTelephoneNumber(textFieldHomeNumber
					.getText());
		} else if (radioButtonMobile.isSelected()) {
			changePerson.setStandardTelephoneNumber(textFieldMobileNumber
					.getText());
		} else if (radioButtonBusiness.isSelected()) {
			changePerson.setStandardTelephoneNumber(textFieldBusinessNumber
					.getText());
		} else if (radioButtonOther.isSelected()) {
			changePerson.setStandardTelephoneNumber(textFieldOtherNumber
					.getText());
		}
		oldPersonList = (Vector) jfritz.getPhonebook().getPersons().clone();
		model.fireTableRowsUpdated(row, row);

	}

	void closeButton_actionPerformed(ActionEvent e) {
		jfritz.getPhonebook().updatePersons(oldPersonList);
		jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
		this.dispose();
	}

	public void showCell(int row, int column) {
	    Rectangle rect =
	      table.getCellRect(row, column, true);
	    table.scrollRectToVisible(rect);
	    table.clearSelection();
	    table.setRowSelectionInterval(row, row);
	    model.fireTableDataChanged(); // notify the model
	    }

	void newButton_actionPerformed(ActionEvent e) {
		Person newEntry = new Person("New", "", "Entry", "", "", "", "123", "",
				"", "", "123", "", "");
		jfritz.getPhonebook().getPersons().add(newEntry);
		model.fireTableRowsInserted(model.getRowCount(), model.getRowCount());

		showCell(model.getRowCount()-1,0);
		table.setRowSelectionInterval(model.getRowCount()-1, model
		.getRowCount()-1);
	}

	void deleteButton_actionPerformed(ActionEvent e) {
		// TODO Abfrage, ob wirklich gelöscht werden soll, da sonst für immer
		// weg
		int row = table.getSelectedRow();
		if (row >= 0) {
			jfritz.getPhonebook().getPersons().remove(row);
			model.fireTableRowsDeleted(row, row);
			if (table.getRowCount() != 0) {
				if (table.getRowCount() > row) {
					table.setRowSelectionInterval(row, row);
				} else {
					table.setRowSelectionInterval(row - 1, row - 1);
				}
			}
		}
		oldPersonList = (Vector) jfritz.getPhonebook().getPersons().clone();
	}

	public boolean showDialog() {
		setVisible(true);
		return true;
	}

	/**
	 * @return Returns the table.
	 */
	public JTable getTable() {
		return table;
	}

	/**
	 * @param isSelected
	 *            Sets RadioButtonHome
	 */
	public void setRadioButtonHome(boolean isSelected) {
		radioButtonHome.setSelected(isSelected);
	}

	/**
	 * @param isSelected
	 *            Sets RadioButtonMobile
	 */
	public void setRadioButtonMobile(boolean isSelected) {
		radioButtonMobile.setSelected(isSelected);
	}

	/**
	 * @param isSelected
	 *            Sets RadioButtonBusiness
	 */
	public void setRadioButtonBusiness(boolean isSelected) {
		radioButtonBusiness.setSelected(isSelected);
	}

	/**
	 * @param isSelected
	 *            Sets RadioButtonOther
	 */
	public void setRadioButtonOther(boolean isSelected) {
		radioButtonOther.setSelected(isSelected);
	}

	/**
	 * Sets FirstName
	 *
	 * @param textToSet
	 */
	public void setTextFieldFirstName(String textToSet) {
		textFieldFirstName.setText(textToSet);
	}

	/**
	 * Sets MiddleName
	 *
	 * @param textToSet
	 */
	public void setTextFieldMiddleName(String textToSet) {
		textFieldMiddleName.setText(textToSet);
	}

	/**
	 * Sets LastName
	 *
	 * @param textToSet
	 */
	public void setTextFieldLastName(String textToSet) {
		textFieldLastName.setText(textToSet);
	}

	/**
	 * Sets street
	 *
	 * @param textToSet
	 */
	public void setTextFieldStreet(String textToSet) {
		textFieldStreet.setText(textToSet);
	}

	/**
	 * Sets postal code
	 *
	 * @param textToSet
	 */
	public void setTextFieldPostalCode(String textToSet) {
		textFieldPostalCode.setText(textToSet);
	}

	/**
	 * Sets City
	 *
	 * @param textToSet
	 */
	public void setTextFieldCity(String textToSet) {
		textFieldCity.setText(textToSet);
	}

	/**
	 * Sets HomeNumber
	 *
	 * @param textToSet
	 */
	public void setTextFieldHomeNumber(String textToSet) {
		textFieldHomeNumber.setText(textToSet);
	}

	/**
	 * Sets MobileNumber
	 *
	 * @param textToSet
	 */
	public void setTextFieldMobileNumber(String textToSet) {
		textFieldMobileNumber.setText(textToSet);
	}

	/**
	 * Sets BusinessNumber
	 *
	 * @param textToSet
	 */
	public void setTextFieldBusinessNumber(String textToSet) {
		textFieldBusinessNumber.setText(textToSet);
	}

	/**
	 * Sets OtherNumber
	 *
	 * @param textToSet
	 */
	public void setTextFieldOtherNumber(String textToSet) {
		textFieldOtherNumber.setText(textToSet);
	}

	/**
	 * Sets Email
	 *
	 * @param textToSet
	 */
	public void setTextFieldEmail(String textToSet) {
		textFieldEmail.setText(textToSet);
	}
}
