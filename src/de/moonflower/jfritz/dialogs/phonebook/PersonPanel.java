/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.Vector;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

/**
 * @author Arno Willig
 *
 */
public class PersonPanel extends JPanel implements ActionListener,
		ListSelectionListener, CaretListener {
	PhoneTypeModel typeModel;

	private final class PhoneType {
		String type;

		public PhoneType(String type) {
			this.type = type;
		}

		public String getType() {
			return type;
		}

		public String toString() {
			try {
				return JFritz.getMessage("phone_" + type);
			} catch (MissingResourceException e) {
				return type;
			}
		}
	}

	private final class PhoneTypeModel extends AbstractListModel implements
			ComboBoxModel {

		private PhoneType sel;

		private Vector types;

		public PhoneTypeModel() {
			super();
			types = new Vector();
			types.add(new PhoneType("home"));
			types.add(new PhoneType("mobile"));
			types.add(new PhoneType("homezone"));
			types.add(new PhoneType("business"));
			types.add(new PhoneType("other"));
			types.add(new PhoneType("fax"));
			types.add(new PhoneType("sip"));
		}

		public Vector getTypes() {
			return types;
		}

		public int getSize() {
			return types.size();
		}

		public Object getElementAt(int index) {
			return types.get(index);
		}

		public void setSelectedItem(Object anItem) {
			sel = (PhoneType) anItem;
		}

		public Object getSelectedItem() {
			return sel;
		}

	}

	private final class NumberTableModel extends AbstractTableModel {
		private final String columnNames[] = { "Std", "Typ", "Nummer" };

		public int getRowCount() {
			return person.getNumbers().size();
		}

		public int getColumnCount() {
			return 3;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			return true;
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
			case 0:
				return new Boolean(((PhoneNumber) person.getNumbers()
						.elementAt(rowIndex)).getType().equals(
						person.getStandard()));
			case 1:
				return new PhoneType(((PhoneNumber) person.getNumbers()
						.elementAt(rowIndex)).getType());
			case 2:
				return ((PhoneNumber) person.getNumbers().elementAt(rowIndex))
						.getFullNumber();
			default:
				return null;

			}
		}

		public void setValueAt(Object value, int row, int column) {
			if (row < person.getNumbers().size()) {
				PhoneNumber p = (PhoneNumber) person.getNumbers()
						.elementAt(row);
				switch (column) {
				case 0:
					if (p.getType() != "")
						person.setStandard(p.getType());
					break;
				case 1:
					if (isValidType((PhoneType) value, p.getType())) {
						if (person.getStandard().equals(p.getType())
								|| person.getStandard().equals("")) {
							person.setStandard(((PhoneType) value).getType());
						}
						p.setType(((PhoneType) value).getType());
					}
					break;
				case 2:
					if (isValidNumber((String) value, p.getFullNumber())) {
						p.setNumber((String) value);
					}
				default:
				}
				fireTableDataChanged();
			}
		}

		protected boolean isValidNumber(String value, String oldvalue) {
			if (value.equals(oldvalue))
				return true;
			Enumeration en = person.getNumbers().elements();
			while (en.hasMoreElements()) {
				String nr = ((PhoneNumber) en.nextElement()).getFullNumber();
				if (value.equals(nr))
					return false;
			}
			return true;
		}

		protected boolean isValidType(PhoneType value, String oldvalue) {
			if (value == null)
				return false;
			if (value.getType().equals(oldvalue))
				return true;

			for (int i = 0; i < typeModel.getSize(); i++) {
				if (value.getType().equals(
						((PhoneType) typeModel.getElementAt(i)).getType())) {
					Enumeration en = person.getNumbers().elements();
					while (en.hasMoreElements()) {
						String type = ((PhoneNumber) en.nextElement())
								.getType();
						if (value.getType().equals(type))
							return false;
					}
					return true;
				}
			}
			return false;
		}

		/**
		 * @return Returns the column names
		 */
		public String getColumnName(int column) {
			return columnNames[column];
		}
	}

	private class CheckBoxRenderer extends JCheckBox implements
			TableCellRenderer {

		public CheckBoxRenderer() {
			setHorizontalAlignment(JLabel.CENTER);
		}

		/**
		 * @see javax.swing.table.TableCellRenderer#getTableCellRendererComponent(javax.swing.JTable,
		 *      java.lang.Object, boolean, boolean, int, int)
		 */
		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(table.getBackground());
			}
			setSelected((value != null && ((Boolean) value).booleanValue()));
			return this;
		}
	}

	private JFritz jfritz;

	private Person person;

	private JTextField tfFirstName, tfMiddleName, tfLastName, tfStreet,
			tfPostalCode, tfCity, tfEmail;

	private JButton addButton, delButton;

	private JTable numberTable;

	private boolean hasChanged = false;

	/**
	 *
	 */
	public PersonPanel(JFritz jfritz, Person person) {
		super();
		this.jfritz = jfritz;
		this.person = person;
		drawPanel();
	}

	private void drawPanel() {
		setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2));
		JLabel label = new JLabel(JFritz.getMessage("firstName")
				+ ": ");
		buttonPanel.add(label);
		tfFirstName = new JTextField(person.getFirstName());
		tfFirstName.addCaretListener(this);
		buttonPanel.add(tfFirstName);
		label = new JLabel(JFritz.getMessage("middleName") + ": ");
		buttonPanel.add(label);
		tfMiddleName = new JTextField(person.getMiddleName());
		tfMiddleName.addCaretListener(this);
		buttonPanel.add(tfMiddleName);
		label = new JLabel(JFritz.getMessage("lastName") + ": ");
		buttonPanel.add(label);
		tfLastName = new JTextField(person.getLastName());
		tfLastName.addCaretListener(this);
		buttonPanel.add(tfLastName);
		label = new JLabel(JFritz.getMessage("street") + ": ");
		buttonPanel.add(label);
		tfStreet = new JTextField(person.getStreet());
		tfStreet.addCaretListener(this);
		buttonPanel.add(tfStreet);
		label = new JLabel(JFritz.getMessage("postalCode") + ": ");
		buttonPanel.add(label);
		tfPostalCode = new JTextField(person.getPostalCode());
		tfPostalCode.addCaretListener(this);
		buttonPanel.add(tfPostalCode);
		label = new JLabel(JFritz.getMessage("city") + ": ");
		buttonPanel.add(label);
		tfCity = new JTextField(person.getCity());
		tfCity.addCaretListener(this);
		buttonPanel.add(tfCity);
		label = new JLabel(JFritz.getMessage("emailAddress")
				+ ": ");
		buttonPanel.add(label);
		tfEmail = new JTextField(person.getEmailAddress());
		tfEmail.addCaretListener(this);
		buttonPanel.add(tfEmail);

		JPanel numberPanel = createNumberPanel();
		numberPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		setLayout(new BorderLayout());
		add(buttonPanel, BorderLayout.NORTH);
		add(numberPanel, BorderLayout.CENTER);
		setPreferredSize(new Dimension(350, -1));
		setMinimumSize(new Dimension(350, 0));
		setMaximumSize(new Dimension(350, 0));
	}

	/**
	 * @return Returns number panel with number table
	 */
	private JPanel createNumberPanel() {
		JPanel numberPanel = new JPanel(new BorderLayout());
		NumberTableModel numberModel = new NumberTableModel();
		typeModel = new PhoneTypeModel();
		numberTable = new JTable(numberModel) {
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
		};
		numberTable.setRowHeight(20);
		numberTable.setFocusable(false);
		numberTable.setAutoCreateColumnsFromModel(false);
		numberTable.setColumnSelectionAllowed(false);
		numberTable.setCellSelectionEnabled(false);
		numberTable.setRowSelectionAllowed(true);
		numberTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		numberTable.getColumnModel().getColumn(0).setMinWidth(20);
		numberTable.getColumnModel().getColumn(0).setMaxWidth(20);
		numberTable.getSelectionModel().addListSelectionListener(this);
		// Renderers
		CheckBoxRenderer checkBoxRenderer = new CheckBoxRenderer();
		numberTable.getColumnModel().getColumn(0).setCellRenderer(
				checkBoxRenderer);

		// Editors
		JCheckBox checkBox = new JCheckBox();
		checkBox.setHorizontalAlignment(JLabel.CENTER);
		JComboBox comboBox = new JComboBox(typeModel);

		comboBox.setEditable(false);
		DefaultCellEditor checkBoxEditor = new DefaultCellEditor(checkBox);
		DefaultCellEditor comboEditor = new DefaultCellEditor(comboBox);
		numberTable.getColumnModel().getColumn(0).setCellEditor(checkBoxEditor);
		numberTable.getColumnModel().getColumn(1).setCellEditor(comboEditor);

		// Buttons
		addButton = new JButton();
		delButton = new JButton();
		addButton.setActionCommand("add");
		addButton.addActionListener(this);
		addButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/add.png"))));

		delButton.setActionCommand("del");
		delButton.addActionListener(this);
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));
		if (person.getNumbers().size() == 1)
			delButton.setEnabled((false));

		JLabel label = new JLabel("Telefonnummern:", JLabel.LEFT);

		JPanel numberButtonPanel = new JPanel(new GridLayout(0, 2));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addButton);
		buttonPanel.add(delButton);

		numberButtonPanel.add(label);
		numberButtonPanel.add(buttonPanel);

		numberPanel.add(numberButtonPanel, BorderLayout.NORTH);
		numberPanel.add(new JScrollPane(numberTable), BorderLayout.CENTER);

		updateAddDelButtons();

		return numberPanel;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("add")) {
			person.getNumbers().add(new PhoneNumber("", ""));
		} else if (e.getActionCommand().equals("del")) {
			int row = numberTable.getSelectedRow();
			// Shift standard number if deleted
			if (person.getStandard().equals(
					((PhoneNumber) person.getNumbers().get(row)).getType())) {
				person.getNumbers().removeElementAt(row);
				person.setStandard(((PhoneNumber) person.getNumbers().get(0))
						.getType());
			} else { // Just remove the number
				person.getNumbers().removeElementAt(row);
			}
		}
		((NumberTableModel) numberTable.getModel()).fireTableDataChanged();
		updateAddDelButtons();
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			updateAddDelButtons();
		}
	}

	/**
	 * Enables/Disables delete button
	 */
	private void updateAddDelButtons() {
		delButton.setEnabled(numberTable.getSelectedRow() > -1
				&& person.getNumbers().size() > 1);

		Enumeration en = person.getNumbers().elements();
		boolean addEnabled = true;
		while (en.hasMoreElements()) {
			String nr = ((PhoneNumber) en.nextElement()).getFullNumber();
			if (nr.equals("")) {
				addEnabled = false;
				break;
			}
		}
		addButton.setEnabled(addEnabled);
	}

	/**
	 * @return Returns the person.
	 */
	public final Person getPerson() {
		return person;
	}

	/**
	 * @return Returns the tfCity.
	 */
	public final String getCity() {
		return tfCity.getText();
	}

	/**
	 * @return Returns the MiddleName.
	 */
	public final String getMiddleName() {
		return tfMiddleName.getText();
	}

	/**
	 * @return Returns the FirstName.
	 */
	public final String getFirstName() {
		return tfFirstName.getText();
	}

	/**
	 * @return Returns the LastName.
	 */
	public final String getLastName() {
		return tfLastName.getText();
	}

	/**
	 * @return Returns the PostalCode.
	 */
	public final String getPostalCode() {
		return tfPostalCode.getText();
	}

	/**
	 * @return Returns the Street.
	 */
	public final String getStreet() {
		return tfStreet.getText();
	}

	/**
	 * @return Returns the eMail.
	 */
	public final String getEmail() {
		return tfEmail.getText();
	}

	/**
	 * @param person
	 *            The person to set.
	 */
	public final void setPerson(Person person) {
		this.person = person;
		updateGUI();
	}

	public final void updateGUI() {
		tfFirstName.setText(person.getFirstName());
		tfMiddleName.setText(person.getMiddleName());
		tfLastName.setText(person.getLastName());
		tfStreet.setText(person.getStreet());
		tfPostalCode.setText(person.getPostalCode());
		tfCity.setText(person.getCity());
		tfEmail.setText(person.getEmailAddress());
		((NumberTableModel) numberTable.getModel()).fireTableDataChanged();
	}

	public final Person updatePerson() {
		person.setFirstName(tfFirstName.getText());
		person.setMiddleName(tfMiddleName.getText());
		person.setLastName(tfLastName.getText());
		person.setStreet(tfStreet.getText());
		person.setPostalCode(tfPostalCode.getText());
		person.setCity(tfCity.getText());
		person.setEmailAddress(tfEmail.getText());
		hasChanged = false;
		return person;
	}

	/**
	 * @return Returns the hasChanged.
	 */
	public final boolean hasChanged() {
		return hasChanged;
	}

	/**
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	public void caretUpdate(CaretEvent e) {
		boolean hasChangedOld = hasChanged;
		hasChanged = !tfFirstName.getText().equals(person.getFirstName())
				|| !tfMiddleName.getText().equals(person.getMiddleName())
				|| !tfLastName.getText().equals(person.getLastName())
				|| !tfStreet.getText().equals(person.getStreet())
				|| !tfPostalCode.getText().equals(person.getPostalCode())
				|| !tfCity.getText().equals(person.getCity())
				|| !tfEmail.getText().equals(person.getEmailAddress());
		firePropertyChange("hasChanged", hasChangedOld, hasChanged);
	}
}
