/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.phonebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;
import de.moonflower.jfritz.struct.PhoneType;

/**
 * This class is used in the phone book to edit individual entries
 *
 *
 * @author Arno Willig
 *
 */
public class PersonPanel extends JPanel implements ActionListener,
		ListSelectionListener, CaretListener {
	private static final long serialVersionUID = 1;

	PhoneTypeModel typeModel;

	private final class PhoneTypeModel extends AbstractListModel implements
			ComboBoxModel {
		private static final long serialVersionUID = 1;

		private String[] basicTypes = { "home", "mobile", "homezone", //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
				"business", "other", "fax", "sip", "main"}; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$ , //$NON-NLS-4$ , //$NON-NLS-5$
		private transient PhoneType sel;

		private Vector types;


		public PhoneTypeModel() {
			super();
			types = new Vector();
			setTypes();
		}

		public void setTypes() {
			types.clear();
			int[] typeCount = new int[basicTypes.length];
			for (int i = 0; i < typeCount.length; i++)
				typeCount[i] = 0;

			Enumeration en = person.getNumbers().elements();
			while (en.hasMoreElements()) {
				String type = ((PhoneNumber) en.nextElement()).getType();
				Pattern p = Pattern.compile("([a-z]*)(\\d*)"); //$NON-NLS-1$
				Matcher m = p.matcher(type);
				if (m.find()) {
					for (int i = 0; i < typeCount.length; i++) {
						if (basicTypes[i].equals(m.group(1))) {
							if (m.group(2).equals("")) { //$NON-NLS-1$
								typeCount[i] = 1;
							} else if (typeCount[i] < Integer.parseInt(m
									.group(2))) {
								typeCount[i] = Integer.parseInt(m.group(2));
							}
							break;
						}
					}
				}
			}
			for (int i = 0; i < typeCount.length; i++) {
				if (typeCount[i] == 0) {
					types.add(new PhoneType(basicTypes[i]));
				} else {
					types
							.add(new PhoneType(basicTypes[i]
									+ (typeCount[i] + 1)));
				}
			}
			fireContentsChanged(this, 0, types.size() - 1);
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

		/**
		 * @author Bastian Schaefer
		 *
		 * @return all available types
		 */
		public String[] getBasicTypes(){
			return basicTypes;
		}

	}

	private final class NumberTableModel extends AbstractTableModel {
		private final String columnNames[] = { Main.getMessage("standard_short"), Main.getMessage("type"), Main.getMessage("number") }; //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$

		private static final long serialVersionUID = 1;

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
				return Boolean.valueOf(((PhoneNumber) person.getNumbers()
						.elementAt(rowIndex)).getType().equals(
						person.getStandard()));
			case 1:
				return new PhoneType(((PhoneNumber) person.getNumbers()
						.elementAt(rowIndex)).getType());
			case 2:
				return ((PhoneNumber) person.getNumbers().elementAt(rowIndex))
						.getIntNumber();
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
					if (!p.getType().equals("")) //$NON-NLS-1$
						person.setStandard(p.getType());
					break;
				case 1:
					if (isValidType((PhoneType) value, p.getType())) {
						if (person.getStandard().equals(p.getType())
								|| person.getStandard().equals("")) { //$NON-NLS-1$
							person.setStandard(((PhoneType) value).getType());
						}
						p.setType(((PhoneType) value).getType());
					}
					break;
				case 2:
					if (isValidNumber((String) value, p.getIntNumber())) {
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
				String nr = ((PhoneNumber) en.nextElement()).getIntNumber();
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
		private static final long serialVersionUID = 1;

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

	private Person person;

	private JTextField tfFirstName, tfCompany, tfLastName, tfStreet,
			tfPostalCode, tfCity, tfEmail;

	private JButton addButton, delButton;

	private JTable numberTable;

	private boolean hasChanged = false;

	private boolean numberHasChanged = false;

	private JCheckBox chkBoxPrivateEntry;

	/**
	 *
	 */
	public PersonPanel(Person person) {
		super();
		this.person = person;
		drawPanel();
	}

	private void drawPanel() {
		numberHasChanged = false;
		setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2));
		JLabel label = new JLabel(Main.getMessage("private_entry") + ": ");  //$NON-NLS-1$,   //$NON-NLS-2$
		buttonPanel.add(label);
		chkBoxPrivateEntry = new JCheckBox();
		chkBoxPrivateEntry.setSelected(person.isPrivateEntry());
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				boolean oldhasChanged = hasChanged;
				hasChanged = chkBoxPrivateEntry.isSelected() != person
						.isPrivateEntry();
				firePropertyChange("hasChanged", oldhasChanged, hasChanged); //$NON-NLS-1$
			}
		};
		chkBoxPrivateEntry.addChangeListener(changeListener);
		buttonPanel.add(chkBoxPrivateEntry);
		label = new JLabel(Main.getMessage("firstName") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		buttonPanel.add(label);
		tfFirstName = new JTextField(person.getFirstName());
		tfFirstName.addCaretListener(this);
		buttonPanel.add(tfFirstName);
		label = new JLabel(Main.getMessage("lastName") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		buttonPanel.add(label);
		tfLastName = new JTextField(person.getLastName());
		tfLastName.addCaretListener(this);
		buttonPanel.add(tfLastName);
		label = new JLabel(Main.getMessage("company") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		buttonPanel.add(label);
		tfCompany = new JTextField(person.getCompany());
		tfCompany.addCaretListener(this);
		buttonPanel.add(tfCompany);
		label = new JLabel(Main.getMessage("street") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		buttonPanel.add(label);
		tfStreet = new JTextField(person.getStreet());
		tfStreet.addCaretListener(this);
		buttonPanel.add(tfStreet);
		label = new JLabel(Main.getMessage("postalCode") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		buttonPanel.add(label);
		tfPostalCode = new JTextField(person.getPostalCode());
		tfPostalCode.addCaretListener(this);
		buttonPanel.add(tfPostalCode);
		label = new JLabel(Main.getMessage("city") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		buttonPanel.add(label);
		tfCity = new JTextField(person.getCity());
		tfCity.addCaretListener(this);
		buttonPanel.add(tfCity);
		label = new JLabel(Main.getMessage("emailAddress") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
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
			private static final long serialVersionUID = 1;

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
		numberTable.getColumnModel().getColumn(2).setCellEditor(new NumberCellEditor(this));

		// Buttons
		addButton = new JButton();
		delButton = new JButton();
		addButton.setActionCommand("add"); //$NON-NLS-1$
		addButton.addActionListener(this);
		addButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/add.png")))); //$NON-NLS-1$

		delButton.setActionCommand("del"); //$NON-NLS-1$
		delButton.addActionListener(this);
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png")))); //$NON-NLS-1$
		if (person.getNumbers().size() == 1)
			delButton.setEnabled((false));

		JLabel label = new JLabel(Main.getMessage("telephoneNumbers")+":", JLabel.LEFT); //$NON-NLS-1$,  //$NON-NLS-2$

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
		if (e.getActionCommand().equals("add")) { //$NON-NLS-1$
			person.getNumbers().add(new PhoneNumber("")); //$NON-NLS-1$,  //$NON-NLS-2$
			typeModel.setTypes();
		} else if (e.getActionCommand().equals("del")) { //$NON-NLS-1$
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
			firePropertyChange(true);
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
			String nr = ((PhoneNumber) en.nextElement()).getIntNumber();
			if (nr.equals("")) { //$NON-NLS-1$
				addEnabled = false;
				break;
			}
		}
		addButton.setEnabled(addEnabled);
		typeModel.setTypes();
	}

	/**
	 * @return Returns the person.
	 */
	public final Person getPerson() {
		return person;
	}

	/**
	 * @return Returns the City.
	 */
	public final String getCity() {
		return tfCity.getText();
	}

	/**
	 * @return Returns the company.
	 */
	public final String getCompany() {
		return tfCompany.getText();
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
	 *
	 * @return Returns if Person is a private entry
	 */
	public final boolean isPrivateEntry() {
		return chkBoxPrivateEntry.isSelected();
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
		chkBoxPrivateEntry.setSelected(person.isPrivateEntry());
		tfFirstName.setText(person.getFirstName());
		tfCompany.setText(person.getCompany());
		tfLastName.setText(person.getLastName());
		tfStreet.setText(person.getStreet());
		tfPostalCode.setText(person.getPostalCode());
		tfCity.setText(person.getCity());
		tfEmail.setText(person.getEmailAddress());
		((NumberTableModel) numberTable.getModel()).fireTableDataChanged();
		typeModel.setTypes();
	}

	public final Person updatePerson() {
		terminateEditing();
		person.setPrivateEntry(chkBoxPrivateEntry.isSelected());
		person.setFirstName(tfFirstName.getText());
		person.setCompany(tfCompany.getText());
		person.setLastName(tfLastName.getText());
		person.setStreet(tfStreet.getText());
		person.setPostalCode(tfPostalCode.getText());
		person.setCity(tfCity.getText());
		person.setEmailAddress(tfEmail.getText());
		hasChanged = false;
		numberHasChanged = false;
		JFritz.getPhonebook().sortAllFilteredRows();
		return person;
	}

	/**
	 * @return Returns the hasChanged.
	 */
	public final boolean hasChanged() {
		return hasChanged;
	}

	/**
	 * @return Returns the numberHasChanged.
	 */
	public final boolean numberHasChanged() {
		return numberHasChanged;
	}

	/**
	 * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
	 */
	public void caretUpdate(CaretEvent e) {
		boolean hasChangedOld = hasChanged;
		hasChanged = !tfFirstName.getText().equals(person.getFirstName())
				|| !tfCompany.getText().equals(person.getCompany())
				|| !tfLastName.getText().equals(person.getLastName())
				|| !tfStreet.getText().equals(person.getStreet())
				|| !tfPostalCode.getText().equals(person.getPostalCode())
				|| !tfCity.getText().equals(person.getCity())
				|| !tfEmail.getText().equals(person.getEmailAddress())
				|| numberHasChanged;

		firePropertyChange("hasChanged", hasChangedOld, hasChanged);  //$NON-NLS-1$
	}

	public void firePropertyChange(boolean boo){
		boolean hasChangedOld = hasChanged;
		hasChanged = boo;
		numberHasChanged = boo;
		if(numberHasChanged)
		firePropertyChange("hasChanged", hasChangedOld, hasChanged); //$NON-NLS-1$
	}

	public void terminateEditing(){
	    if (numberTable.isEditing())
	    {
	      int row = numberTable.getEditingRow();
	      int column = numberTable.getEditingColumn();
	      numberTable.editingStopped(new ChangeEvent (numberTable.getComponentAt(row, column)));
	    }
	}

	public void cancelEditing(){
		hasChanged = false;
	    if (numberTable.isEditing())
	    {
	      int row = numberTable.getEditingRow();
	      int column = numberTable.getEditingColumn();
	      numberHasChanged = false;
	      numberTable.editingCanceled(new ChangeEvent (numberTable.getComponentAt(row, column)));
	    }
	}

	/**
	*	@author haeusler
	*	DATE: 02.04.06, added by Brian
	* 	moves the focus to the JTextField for the first name
	*/
	public boolean focusFirstName() {
		return tfFirstName.requestFocusInWindow();
	}

}
