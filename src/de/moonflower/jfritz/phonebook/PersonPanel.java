/*
 * Created on 03.06.2005
 *
 */
package de.moonflower.jfritz.phonebook;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
import javax.swing.filechooser.FileFilter;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.Call;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.PhoneNumber;

/**
 * This class is used in the phone book to edit individual entries
 *
 *
 * @author Arno Willig
 *
 * TODO: Änderung des Typs einer Rufnummer soll Undo-Button aktivieren TODO:
 * Änderung der Standard-Rufnummer soll Undo-Button aktivieren
 *
 * TODO: Auf Tasten reagieren
 *
 * TODO: Default-Button TODO: Überprüfen, ob Undo-Button auch die Rufnummern,
 * Rufnummerntyp korrekt zurücksetzt
 */
public class PersonPanel extends JPanel implements ActionListener,
		ListSelectionListener, CaretListener {
	private static final long serialVersionUID = 1;

	PhoneTypeModel typeModel;

	private Person originalPerson;

	private static int scaleWidth = 171;

	private static int scaleHeight = 221;

	private Person clonedPerson;

	private JTextField tfFirstName, tfCompany, tfLastName, tfStreet,
			tfPostalCode, tfCity, tfEmail;

	private JButton addButton, delButton, okButton, cancelButton, undoButton, pictureButton;

	private JTable numberTable;

	private JComboBox numberTypesComboBox;

	private boolean hasChanged = false;

	private boolean numberHasChanged = false;

	private JCheckBox chkBoxPrivateEntry;

	private PhoneBook phoneBook;

	private Vector<ActionListener> actionListener;

	private JFritzWindow parentFrame;

	/**
	 *
	 */
	public PersonPanel(Person person, PhoneBook phoneBook, JFritzWindow parentFrame) {
		super();
		this.originalPerson = person;
		this.clonedPerson = person.clone();
		this.phoneBook = phoneBook;
		this.parentFrame = parentFrame;
		actionListener = new Vector<ActionListener>();
		createPanel();
		setPerson(person, false);
	}

	private void createPanel() {
		setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));

		JPanel configPanel = createConfigPanel();
		JPanel numberPanel = createNumberPanel();
		numberPanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		centerPanel.add(configPanel);
		centerPanel.add(numberPanel);

		JPanel buttonPanel = new JPanel();

		okButton = new JButton(Main.getMessage("save")); //$NON-NLS-1$
		okButton.setActionCommand("ok"); //$NON-NLS-1$
		okButton.setIcon(getImage("okay.png")); //$NON-NLS-1$
		okButton.addActionListener(this);

		cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$
		cancelButton.setActionCommand("cancel"); //$NON-NLS-1$
		cancelButton.addActionListener(this);

		undoButton = new JButton(Main.getMessage("undo")); //$NON-NLS-1$
		undoButton.setActionCommand("undo"); //$NON-NLS-1$
		undoButton.addActionListener(this);
		undoButton.setEnabled(false);

		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(undoButton);

		setLayout(new BorderLayout());
		add(new JScrollPane(centerPanel), BorderLayout.CENTER);//));
		add(buttonPanel, BorderLayout.SOUTH);

		setPreferredSize(new Dimension(350, -1));
		setMinimumSize(new Dimension(350, 0));
		setMaximumSize(new Dimension(350, 0));
	}


	/**
	 * @return Returns number panel with number table
	 */
	private JPanel createConfigPanel() {
		JPanel configPanel = new JPanel();
		configPanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		c.insets.bottom= 10;
		pictureButton = new JButton();
		pictureButton.addActionListener(this);
		pictureButton.setActionCommand("setPicture");
		pictureButton.setToolTipText("Click to set image");
		pictureButton.setBorder(null);
		configPanel.add(pictureButton, c);

		c.insets.bottom= 1;
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		JLabel label = new JLabel(Main.getMessage("private_entry") + ": "); //$NON-NLS-1$,   //$NON-NLS-2$
		configPanel.add(label, c);
		c.gridx = 1;
		c.gridy = 1;
		chkBoxPrivateEntry = new JCheckBox();
		ChangeListener changeListener = new ChangeListener() {
			public void stateChanged(ChangeEvent changeEvent) {
				hasChanged = chkBoxPrivateEntry.isSelected() != originalPerson
						.isPrivateEntry();
				firePropertyChange();
			}
		};
		chkBoxPrivateEntry.addChangeListener(changeListener);
		configPanel.add(chkBoxPrivateEntry, c);

		c.gridx = 0;
		c.gridy = 2;
		label = new JLabel(Main.getMessage("firstName") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		configPanel.add(label,c );
		c.gridx = 1;
		c.gridy = 2;
		tfFirstName = new JTextField(25);
		tfFirstName.setMinimumSize(new Dimension(150, 20));
		tfFirstName.addCaretListener(this);
		configPanel.add(tfFirstName, c);

		c.gridx = 0;
		c.gridy = 3;
		label = new JLabel(Main.getMessage("lastName") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		configPanel.add(label, c);
		c.gridx = 1;
		c.gridy = 3;
		tfLastName = new JTextField();
		tfLastName.addCaretListener(this);
		configPanel.add(tfLastName, c);

		c.gridx = 0;
		c.gridy = 4;
		label = new JLabel(Main.getMessage("company") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		configPanel.add(label, c);
		c.gridx = 1;
		c.gridy = 4;
		tfCompany = new JTextField();
		tfCompany.addCaretListener(this);
		configPanel.add(tfCompany, c);

		c.gridx = 0;
		c.gridy = 5;
		label = new JLabel(Main.getMessage("street") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		configPanel.add(label, c);
		c.gridx = 1;
		c.gridy = 5;
		tfStreet = new JTextField();
		tfStreet.addCaretListener(this);
		configPanel.add(tfStreet, c);

		c.gridx = 0;
		c.gridy = 6;
		label = new JLabel(Main.getMessage("postalCode") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		configPanel.add(label, c);
		c.gridx = 1;
		c.gridy = 6;
		tfPostalCode = new JTextField();
		tfPostalCode.addCaretListener(this);
		configPanel.add(tfPostalCode, c);

		c.gridx = 0;
		c.gridy = 7;
		label = new JLabel(Main.getMessage("city") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		configPanel.add(label, c);
		c.gridx = 1;
		c.gridy = 7;
		tfCity = new JTextField();
		tfCity.addCaretListener(this);
		configPanel.add(tfCity, c);

		c.gridx = 0;
		c.gridy = 8;
		label = new JLabel(Main.getMessage("emailAddress") + ": "); //$NON-NLS-1$,  //$NON-NLS-2$
		configPanel.add(label, c);
		c.gridx = 1;
		c.gridy = 8;
		tfEmail = new JTextField();
		tfEmail.addCaretListener(this);
		configPanel.add(tfEmail, c);
		configPanel.setPreferredSize(new Dimension(100, 400));

		return configPanel;
	}

	/**
	 * @return Returns number panel with number table
	 */
	private JPanel createNumberPanel() {
		JPanel numberPanel = new JPanel(new BorderLayout());
		typeModel = new PhoneTypeModel(clonedPerson);
		NumberTableModel numberModel = new NumberTableModel(clonedPerson,
				typeModel);
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

		numberTypesComboBox = new JComboBox(typeModel);
		numberTypesComboBox.setEditable(false);

		DefaultCellEditor checkBoxEditor = new DefaultCellEditor(checkBox);
		DefaultCellEditor comboEditor = new DefaultCellEditor(
				numberTypesComboBox);

		numberTable.getColumnModel().getColumn(0).setCellEditor(checkBoxEditor);
		numberTable.getColumnModel().getColumn(1).setCellEditor(comboEditor);
		numberTable.getColumnModel().getColumn(2).setCellEditor(
				new NumberCellEditor(this));

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
		if (clonedPerson.getNumbers().size() == 1)
			delButton.setEnabled((false));

		JLabel label = new JLabel(
				Main.getMessage("telephoneNumbers") + ":", JLabel.LEFT); //$NON-NLS-1$,  //$NON-NLS-2$

		JPanel numberButtonPanel = new JPanel(new GridLayout(0, 2));
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(addButton);
		buttonPanel.add(delButton);

		numberButtonPanel.add(label);
		numberButtonPanel.add(buttonPanel);

		numberPanel.add(numberButtonPanel, BorderLayout.NORTH);
		numberPanel.add(new JScrollPane(numberTable), BorderLayout.CENTER);

		updateAddDelButtons();

		numberPanel.setPreferredSize(new Dimension(100, 200));

		return numberPanel;
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("setPicture")) //$NON-NLS-1$
		{
			JFileChooser fc = new JFileChooser(Main.getStateProperty("option.picture.default_path"));  //$NON-NLS-1$
			fc.setFileFilter(new FileFilter() {
				public boolean accept(File f) {
					return f.isDirectory()
							|| f.getName().toLowerCase().endsWith(".jpg")   //$NON-NLS-1$
							|| f.getName().toLowerCase().endsWith(".gif")   //$NON-NLS-1$
							|| f.getName().toLowerCase().endsWith(".png");  //$NON-NLS-1$
				}

				public String getDescription() {
					return Main.getMessage("picture_files");  //$NON-NLS-1$
				}
			});
			if (fc.showOpenDialog(parentFrame) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			Main.setStateProperty("option.picture.default_path", fc.getSelectedFile().getAbsolutePath());  //$NON-NLS-1$
			clonedPerson.setPictureUrl(fc.getSelectedFile().getAbsolutePath());
			hasChanged = true;
			updateUndoButton();
			updateGUI();
		} else if (e.getActionCommand().equals("add")) { //$NON-NLS-1$
			clonedPerson.getNumbers().add(new PhoneNumber("")); //$NON-NLS-1$,  //$NON-NLS-2$
			typeModel.setTypes();
			numberHasChanged = true;
			firePropertyChange();
		} else if (e.getActionCommand().equals("del")) { //$NON-NLS-1$
			int row = numberTable.getSelectedRow();
			cancelEditing();
			// Shift standard number if deleted
			if (clonedPerson.getStandard().equals(
					((PhoneNumber) clonedPerson.getNumbers().get(row))
							.getType())) {
				clonedPerson.getNumbers().removeElementAt(row);
				clonedPerson.setStandard(((PhoneNumber) clonedPerson
						.getNumbers().get(0)).getType());
			} else { // Just remove the number
				clonedPerson.getNumbers().removeElementAt(row);
			}
			numberHasChanged = true;
			firePropertyChange();
		} else if (e.getActionCommand().equals("undo")) { //$NON-NLS-1$
			undo();
		} else if (e.getActionCommand().equals("ok")) {
			save(e);
		} else if (e.getActionCommand().equals("cancel")) {
			cancel(e);
		}
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
				&& clonedPerson.getNumbers().size() > 1);

		Enumeration<PhoneNumber> en = clonedPerson.getNumbers().elements();
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

	private void updateUndoButton() {
		undoButton.setEnabled(hasChanged || numberHasChanged);
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
	public final void setPerson(Person person, boolean resetFilter) {
		if (!clonedPerson.equals(person))
		{
			this.cancelEditing();
			this.originalPerson = person;
			clonedPerson = originalPerson.clone();
			numberHasChanged = false;
			hasChanged = false;
			updateGUI();
			JFritz.getJframe().getPhoneBookPanel().getPhoneBookTable()
			.showAndSelectPerson(originalPerson, resetFilter);
		}
	}

	/**
	 * Updates display of GUI
	 *
	 */
	public final void updateGUI() {
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		scaleHeight = (int)screen.getHeight() / 5;
		scaleWidth = (scaleHeight / 4) * 3;
		ImageIcon pictureIcon;
		if (clonedPerson.getPictureUrl().equals(""))
		{
			pictureIcon = new ImageIcon("pictures/NoPic.jpg");
		} else {
			pictureIcon = new ImageIcon(clonedPerson.getPictureUrl());
		}

		// if we don't find the image, display the default one
		if (pictureIcon.getIconWidth() == -1 || pictureIcon.getIconHeight() == -1)
		{
			pictureIcon = new ImageIcon("pictures/NoPic.jpg");
		}
		float pictureWidthFactor = (float)pictureIcon.getIconWidth() / (float)scaleWidth;
		float pictureHeightFactor = (float)pictureIcon.getIconHeight() / (float)scaleHeight;

		int scaleToWidth = 0;
		int scaleToHeight = 0;
		if ( pictureWidthFactor > pictureHeightFactor )
		{
			scaleToWidth = (int)((float)pictureIcon.getIconWidth() / pictureWidthFactor);
			scaleToHeight = (int)((float)pictureIcon.getIconHeight() / pictureWidthFactor);
		}
		else
		{
			scaleToWidth = (int)((float)pictureIcon.getIconWidth() / pictureHeightFactor);
			scaleToHeight = (int)((float)pictureIcon.getIconHeight() / pictureHeightFactor);
		}

		Image scaledImage = pictureIcon.getImage().getScaledInstance(scaleToWidth, scaleToHeight, Image.SCALE_SMOOTH);

		pictureIcon.setImage(scaledImage);
		pictureButton.setSize(pictureIcon.getIconWidth(),pictureIcon.getIconHeight());
		pictureButton.setIcon(pictureIcon);

		chkBoxPrivateEntry.setSelected(clonedPerson.isPrivateEntry());
		tfFirstName.setText(clonedPerson.getFirstName());
		tfCompany.setText(clonedPerson.getCompany());
		tfLastName.setText(clonedPerson.getLastName());
		tfStreet.setText(clonedPerson.getStreet());
		tfPostalCode.setText(clonedPerson.getPostalCode());
		tfCity.setText(clonedPerson.getCity());
		tfEmail.setText(clonedPerson.getEmailAddress());

		typeModel = new PhoneTypeModel(clonedPerson);
		typeModel.setTypes();
		numberTypesComboBox.setModel(typeModel);
		((NumberTableModel) numberTable.getModel()).setTypeModel(typeModel);
		((NumberTableModel) numberTable.getModel()).setPerson(clonedPerson);

		updateAddDelButtons();
		updateUndoButton();
	}

	public final Person updatePerson() {
		terminateEditing();
		Person unchanged = originalPerson.clone();

		//remove the person reference in the call list, if one was present!
		Call call = originalPerson.getLastCall();
		Person newPerson = null;
		if(call != null){
			newPerson = phoneBook.findPerson(call);
			JFritz.getCallerList().setPerson(newPerson, call);
		}
		JFritz.getCallerList().updatePersonInCalls(newPerson, originalPerson.getNumbers());

		synchronized(phoneBook){
			originalPerson.setPrivateEntry(chkBoxPrivateEntry.isSelected());
			originalPerson.setFirstName(tfFirstName.getText());
			originalPerson.setCompany(tfCompany.getText());
			originalPerson.setLastName(tfLastName.getText());
			originalPerson.setStreet(tfStreet.getText());
			originalPerson.setPostalCode(tfPostalCode.getText());
			originalPerson.setCity(tfCity.getText());
			originalPerson.setEmailAddress(tfEmail.getText());
			originalPerson.setPictureUrl(clonedPerson.getPictureUrl());

			originalPerson.setNumbers((Vector<PhoneNumber>) clonedPerson
					.getNumbers().clone(), clonedPerson.getStandard());

			phoneBook.notifyListenersOfUpdate(unchanged, originalPerson);
		}

		originalPerson.setLastCall(JFritz.getCallerList().findLastCall(originalPerson));
		JFritz.getCallerList().updatePersonInCalls(originalPerson, originalPerson.getNumbers());

		hasChanged = false;
		numberHasChanged = false;



		return originalPerson;
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
		hasChanged = !tfFirstName.getText().equals(
				originalPerson.getFirstName())
				|| !tfCompany.getText().equals(originalPerson.getCompany())
				|| !tfLastName.getText().equals(originalPerson.getLastName())
				|| !tfStreet.getText().equals(originalPerson.getStreet())
				|| !tfPostalCode.getText().equals(
						originalPerson.getPostalCode())
				|| !tfCity.getText().equals(originalPerson.getCity())
				|| !tfEmail.getText().equals(originalPerson.getEmailAddress())
				|| !clonedPerson.getPictureUrl().equals(originalPerson.getPictureUrl())
				|| numberHasChanged;

		firePropertyChange();
	}

	public void firePropertyChange() {
		if (numberHasChanged) {
			((NumberTableModel) numberTable.getModel()).fireTableDataChanged();
		}
		updateAddDelButtons();
		updateUndoButton();
	}

	public void terminateEditing() {
		if (numberTable.isEditing()) {
			int row = numberTable.getEditingRow();
			int column = numberTable.getEditingColumn();
			numberTable.editingStopped(new ChangeEvent(numberTable
					.getComponentAt(row, column)));
		}
	}

	public void cancelEditing() {
		int result = -1;
		if ( hasChanged || numberHasChanged )
		{
			String[] options = {Main.getMessage("save"), Main.getMessage("discard")};

			result = JOptionPane.showOptionDialog(parentFrame, Main.getMessage("save_changes"), Main.getMessage("unsaved_changes"),
					JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

		}
		hasChanged = false;
		if (numberTable.isEditing()) {
			int row = numberTable.getEditingRow();
			int column = numberTable.getEditingColumn();
			if ( result == 0)
			{
				numberTable.editingStopped(new ChangeEvent(numberTable
						.getComponentAt(row, column)));
			}
			else
			{
				numberTable.editingCanceled(new ChangeEvent(numberTable
						.getComponentAt(row, column)));
			}
			numberHasChanged = false;
		}
	    if (result == 0) // YES
	    {
	    	ActionEvent e = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "ok");
	    	e.setSource(okButton);
	    	save(e);
	    }
	    else if (result == 1)// NO
	    {
	    	undo();
	    }
	}

	/**
	 * @author haeusler DATE: 02.04.06, added by Brian moves the focus to the
	 *         JTextField for the first name
	 */
	public boolean focusFirstName() {
		return tfFirstName.requestFocusInWindow();
	}

	/**
	 * Get an image from file
	 *
	 * @param filename
	 * @return a image
	 */
	public ImageIcon getImage(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
	}

	/**
	 * Set numberHasChanged
	 *
	 * @param numberHasChanged
	 */
	public void setNumberHasChanged(boolean numberHasChanged) {
		this.numberHasChanged = numberHasChanged;
	}

	/**
	 * Adds an button-listener
	 *
	 * @param listener
	 */
	public void addActionListener(ActionListener listener) {
		if (!actionListener.contains(listener))
			actionListener.add(listener);
	}

	/**
	 * Removes an button-listener
	 *
	 * @param listener
	 */
	public void removeButtonListener(ActionListener listener) {
		if (actionListener.contains(listener))
			actionListener.remove(listener);
	}

	private void undo()
	{
		this.setPerson(originalPerson, false);
		hasChanged = false;
		numberHasChanged = false;
		firePropertyChange();
	}

	private void cancel(ActionEvent e)
	{
		this.setPerson(originalPerson, false);
		Enumeration<ActionListener> en = actionListener.elements();
		while (en.hasMoreElements()) {
			ActionListener al = en.nextElement();
			al.actionPerformed(e);
		}
	}

	private void save(ActionEvent e)
	{
		updatePerson();

		phoneBook.sortAllFilteredRows();
		phoneBook.updateFilter();
		phoneBook.saveToXMLFile(Main.SAVE_DIR + JFritz.PHONEBOOK_FILE);

		Enumeration<ActionListener> en = actionListener.elements();
		while (en.hasMoreElements()) {
			ActionListener al = en.nextElement();
			al.actionPerformed(e);
		}
	}
}