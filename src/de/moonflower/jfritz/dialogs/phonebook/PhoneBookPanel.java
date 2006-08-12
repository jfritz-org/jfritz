/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.JPopupMenu;
import javax.swing.JMenuItem;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.VCardList;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.Debug;

/**
 * @author Arno Willig
 *
 */
public class PhoneBookPanel extends JPanel implements ListSelectionListener,
		PropertyChangeListener, ActionListener, KeyListener {
	private static final long serialVersionUID = 1;

	private final int PERSONPANEL_WIDTH = 350;

	private JFritz jfritz;

	private PhoneBookTable phoneBookTable;

	private PersonPanel personPanel;

	private JSplitPane splitPane;

	private JButton saveButton, cancelButton, resetButton;

	private JPopupMenu popupMenu;

	private JTextField searchFilter;

	public PhoneBookPanel(JFritz jfritz) {
		this.jfritz = jfritz;
		setLayout(new BorderLayout());

		JPanel editPanel = createEditPanel();

		add(createPhoneBookToolBar(), BorderLayout.NORTH);
		splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitPane.setLeftComponent(editPanel);
		splitPane.setRightComponent(createPhoneBookTable());

		splitPane.setOneTouchExpandable(true);
		splitPane.setContinuousLayout(true);
		add(splitPane, BorderLayout.CENTER);
		splitPane.setDividerLocation(0);
	}

	/**
	 * @return editPanel
	 */
	private JPanel createEditPanel() {
		JPanel editPanel = new JPanel(new BorderLayout());
		JPanel editButtonPanel = new JPanel();
		saveButton = new JButton(JFritz.getMessage("accept")); //$NON-NLS-1$
		saveButton.setActionCommand("save"); //$NON-NLS-1$
		saveButton.addActionListener(this);
		saveButton.setIcon(getImage("okay.png")); //$NON-NLS-1$
		cancelButton = new JButton(JFritz.getMessage("reset")); //$NON-NLS-1$
		cancelButton.setActionCommand("cancel"); //$NON-NLS-1$
		cancelButton.addActionListener(this);
		editButtonPanel.add(saveButton);
		editButtonPanel.add(cancelButton);

		personPanel = new PersonPanel(jfritz, new Person());
		personPanel.addPropertyChangeListener("hasChanged", this); //$NON-NLS-1$
		editPanel.add(personPanel, BorderLayout.CENTER);
		editPanel.add(editButtonPanel, BorderLayout.SOUTH);
		return editPanel;
	}

	public JToolBar createPhoneBookToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);
		JButton addButton = new JButton(JFritz.getMessage("new_entry")); //$NON-NLS-1$
		addButton.setIcon(getImage("add.png")); //$NON-NLS-1$
		addButton.setActionCommand("addPerson"); //$NON-NLS-1$
		addButton.addActionListener(this);
		toolBar.add(addButton);

		JButton delButton = new JButton(JFritz.getMessage("delete_entry")); //$NON-NLS-1$
		delButton.setToolTipText(JFritz.getMessage("delete_entry")); //$NON-NLS-1$
		delButton.setIcon(getImage("delete.png")); //$NON-NLS-1$
		delButton.setActionCommand("deletePerson"); //$NON-NLS-1$
		delButton.addActionListener(this);
		toolBar.add(delButton);

		toolBar.addSeparator();

		JButton exportVCardButton = new JButton();
		exportVCardButton.setIcon(getImage("vcard.png")); //$NON-NLS-1$
		exportVCardButton.setToolTipText(JFritz.getMessage("export_vcard")); //$NON-NLS-1$
		exportVCardButton.setActionCommand("export_vcard"); //$NON-NLS-1$
		exportVCardButton.addActionListener(this);
		toolBar.add(exportVCardButton);

		toolBar.addSeparator();
		//toolBar.addSeparator();
		//toolBar.addSeparator();

		JToggleButton tb = new JToggleButton(getImage("addbook_grey.png"), true); //$NON-NLS-1$
		tb.setSelectedIcon(getImage("addbook.png")); //$NON-NLS-1$
		tb.setActionCommand("filter_private"); //$NON-NLS-1$
		tb.addActionListener(this);
		tb.setToolTipText(JFritz.getMessage("private_entry")); //$NON-NLS-1$
		tb.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter_private", "false"))); //$NON-NLS-1$,  //$NON-NLS-2$
		toolBar.add(tb);

		//toolBar.addSeparator();
		toolBar.addSeparator();

		JButton importXMLButton = new JButton();
		importXMLButton.setIcon(getImage("import.gif")); //$NON-NLS-1$
		importXMLButton.setToolTipText(JFritz.getMessage("phonebook_import")); //$NON-NLS-1$
		importXMLButton.setActionCommand("import_xml"); //$NON-NLS-1$
		importXMLButton.addActionListener(this);
		toolBar.add(importXMLButton);

		toolBar.addSeparator();

		resetButton = new JButton();
		toolBar.add(new JLabel(JFritz.getMessage("search") + ": ")); //$NON-NLS-1$, //$NON-NLS-2$
		searchFilter = new JTextField(JFritz.getProperty("filter.Phonebook.search", ""), //$NON-NLS-1$,  //$NON-NLS-2$
				10);
		searchFilter.addKeyListener(this);
		toolBar.add(searchFilter);

		resetButton = new JButton(JFritz.getMessage("clear")); //$NON-NLS-1$
		resetButton.setActionCommand("clearFilter"); //$NON-NLS-1$
		resetButton.addActionListener(this);
		toolBar.add(resetButton);

		return toolBar;
	}

	public JScrollPane createPhoneBookTable() {
		popupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(JFritz.getMessage("phonebook_delPerson")); //$NON-NLS-1$
		menuItem.setActionCommand("deletePerson"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);
		menuItem = new JMenuItem(JFritz.getMessage("phonebook_editPerson")); //$NON-NLS-1$
		menuItem.setActionCommand("editPerson"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);
		menuItem = new JMenuItem(JFritz.getMessage("phonebook_vcardExport")); //$NON-NLS-1$
		menuItem.setActionCommand("export_vcard"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);

		//Add listener to components that can bring up popup menus.
		MouseAdapter popupListener = new PopupListener();

		phoneBookTable = new PhoneBookTable(jfritz);
		phoneBookTable.getSelectionModel().addListSelectionListener(this);
		phoneBookTable.addMouseListener(popupListener);
		return new JScrollPane(phoneBookTable);
	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			int rows[] = phoneBookTable.getSelectedRows();
			if (rows.length == 1) {
				Person p = ((PhoneBook) phoneBookTable.getModel())
						.getPersonAt(rows[0]);
				personPanel.cancelEditing();
				personPanel.setPerson(p);
				setStatus();
			}
			else {
				jfritz.getJframe().setStatus( JFritz.getMessage("phonebook_chosenEntries")  //$NON-NLS-1$
						.replaceAll("%N", Integer.toString(rows.length))); //$NON-NLS-1$,  //$NON-NLS-2$
			}
		}
	}

	/**
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	public void propertyChange(PropertyChangeEvent evt) {
		saveButton.setEnabled(personPanel.hasChanged());
		cancelButton.setEnabled(personPanel.hasChanged());
	}

	/**
     * Added the code from haeusler
     * DATE: 04.02.06, added by Brian
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cancel")) { //$NON-NLS-1$
			personPanel.cancelEditing();
			personPanel.updateGUI();
		} else if (e.getActionCommand().equals("save")) { //$NON-NLS-1$
			personPanel.updatePerson();
			jfritz.getPhonebook().fireTableDataChanged();
			jfritz.getPhonebook().saveToXMLFile(JFritz.SAVE_DIR + JFritz.PHONEBOOK_FILE);
		} else if (e.getActionCommand().equals("addPerson")) { //$NON-NLS-1$
			Person newPerson = new Person("", JFritz.getMessage("new")); //$NON-NLS-1$,  //$NON-NLS-2$
			jfritz.getPhonebook().addFilterException(newPerson);
			jfritz.getPhonebook().addEntry(newPerson);
			jfritz.getPhonebook().fireTableDataChanged();
			int index = jfritz.getPhonebook().indexOf(newPerson);
			phoneBookTable.getSelectionModel().setSelectionInterval(index, index);
			int loc = splitPane.getDividerLocation();
			if (loc < PERSONPANEL_WIDTH)
				splitPane.setDividerLocation(PERSONPANEL_WIDTH);
			personPanel.focusFirstName();
		} else if (e.getActionCommand().equals("deletePerson")) { //$NON-NLS-1$
			removeSelectedPersons();
		} else if (e.getActionCommand().equals("editPerson")) { //$NON-NLS-1$
			// Edit Panel anzeigen, falls verborgen
			int loc = splitPane.getDividerLocation();
			if (loc < PERSONPANEL_WIDTH)
				splitPane.setDividerLocation(PERSONPANEL_WIDTH);
			;
		} else if (e.getActionCommand().equals("filter_private")) { //$NON-NLS-1$
			JFritz.setProperty("filter_private", Boolean //$NON-NLS-1$
					.toString(((JToggleButton) e.getSource()).isSelected()));
			jfritz.getPhonebook().updateFilter();
		} else if (e.getActionCommand().equals("export_vcard")) { //$NON-NLS-1$
			exportVCard();
		} else if (e.getActionCommand().equals("import_xml")) { //$NON-NLS-1$
			importFromXML ();
		} else if (e.getActionCommand() == "clearFilter") { //$NON-NLS-1$
			clearAllFilter();
		} else {
			Debug.msg("Unsupported Command: " + e.getActionCommand()); //$NON-NLS-1$
		}
		propertyChange(null);
	}

	/**
	 * Removes selected persons from phonebook
	 *
	 */
	public void removeSelectedPersons() {
		if (JOptionPane.showConfirmDialog(this,
				JFritz.getMessage("delete_entries").replaceAll("%N",""), //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
				JFritz.PROGRAM_NAME,
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

			int row[] = jfritz.getJframe().getPhoneBookPanel()
					.getPhoneBookTable().getSelectedRows();
			if (row.length > 0) {
				// Markierte Einträge löschen
				Vector personsToDelete = new Vector();
				for (int i = 0; i < row.length; i++) {
					personsToDelete.add(jfritz.getPhonebook()
							.getFilteredPersons().get(row[i]));
				}
				Enumeration en = personsToDelete.elements();
				while (en.hasMoreElements()) {
					jfritz.getPhonebook()
							.deleteEntry((Person) en.nextElement());
				}
				jfritz.getPhonebook().fireTableDataChanged();
				jfritz.getPhonebook().saveToXMLFile(JFritz.SAVE_DIR + JFritz.PHONEBOOK_FILE);
			}
		}
	}

	/**
	 * @return Returns the phoneBookTable.
	 */
	public final PhoneBookTable getPhoneBookTable() {
		return phoneBookTable;
	}

	/**
	 * @return Returns the personPanel.
	 */
	public final PersonPanel getPersonPanel() {
		return personPanel;
	}

	public void showPersonPanel() {
		splitPane.setDividerLocation(PERSONPANEL_WIDTH);
	}

	public ImageIcon getImage(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/" + filename))); //$NON-NLS-1$
	}

	/**
	 * Exports VCard or VCardList
	 */
	public void exportVCard() {
		VCardList list = new VCardList();
		JFileChooser fc = new JFileChooser(JFritz.getProperty("options.exportVCARDpath",null)); //$NON-NLS-1$
		fc.setDialogTitle(JFritz.getMessage("export_vcard")); //$NON-NLS-1$
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".vcf"); //$NON-NLS-1$
			}

			public String getDescription() {
				return "VCard (.vcf)"; //$NON-NLS-1$
			}
		});
		int rows[] = getPhoneBookTable().getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			Person person = (Person) jfritz.getPhonebook().getPersonAt(rows[i]);
			if (person != null && person.getFullname() != "") { //$NON-NLS-1$
				list.addVCard(person);
			}
		}
		if (list.getCount() > 0) {
			if (list.getCount() == 1) {
				fc.setSelectedFile(new File(list.getPerson(0)
						.getStandardTelephoneNumber()
						+ ".vcf")); //$NON-NLS-1$
			} else if (list.getCount() > 1) {
				fc.setSelectedFile(new File("jfritz.vcf")); //$NON-NLS-1$
			}
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			    String path = fc.getSelectedFile().getPath();
			    path = path.substring(0,path.length()-fc.getSelectedFile().getName().length());
			    JFritz.setProperty("options.exportVCARDpath", path);  //$NON-NLS-1$
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(this,
							JFritz.getMessage("overwrite_file").replaceAll("%F", file.getName()),  //$NON-NLS-1$,  //$NON-NLS-2$
							JFritz.getMessage("dialog_title_overwrite_file"),  //$NON-NLS-1$
							JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						list.saveToFile(file);
					}
				} else {
					list.saveToFile(file);
				}
			}
		} else {
		Debug.errDlg(JFritz.getMessage("error_no_row_chosen"));  //$NON-NLS-1$
		}
	}

	public void setStatus() {
		PhoneBook pb = (PhoneBook) phoneBookTable.getModel();
		int entries = pb.getFilteredPersons().size();
		jfritz.getJframe().setStatus(JFritz.getMessage("entries").  //$NON-NLS-1$
				replaceAll("%N", Integer.toString(entries)));  //$NON-NLS-1$
	}

	public void importFromXML () {
		JFileChooser fc = new JFileChooser(JFritz.getProperty("option.phonebook.import_xml_path"));  //$NON-NLS-1$
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".xml");  //$NON-NLS-1$
			}

			public String getDescription() {
				return JFritz.getMessage("xml_files");  //$NON-NLS-1$
			}
		});
		if (fc.showOpenDialog(jfritz.getJframe()) != JFileChooser.APPROVE_OPTION) return;
		JFritz.setProperty("option.phonebook.import_xml_path", fc.getSelectedFile().getAbsolutePath());  //$NON-NLS-1$
		jfritz.getPhonebook().loadFromXMLFile(fc.getSelectedFile().getAbsolutePath());
		jfritz.getPhonebook().saveToXMLFile(JFritz.SAVE_DIR + JFritz.PHONEBOOK_FILE);
	}
	class PopupListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				int loc = splitPane.getDividerLocation();
				if (loc < PERSONPANEL_WIDTH)
					splitPane.setDividerLocation(PERSONPANEL_WIDTH);
				else
					splitPane.setDividerLocation(0);
			}
		}

		public void mousePressed(MouseEvent e) {
			maybeShowPopup(e);
		}

		public void mouseReleased(MouseEvent e) {
			maybeShowPopup(e);
		}

		private void maybeShowPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.show(e
						.getComponent(), e.getX(), e.getY());
			}
		}
	}
	/**
	 *	added code form haeusler
	 *  DATE: 04.02.06 Brian
	 *
	 */
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			JTextField search = (JTextField) arg0.getSource();

			// ignore whitespaces at the beginning and the end
			String filter = search.getText().trim();

			// only update filter when the search expression has changed
			if (! filter.equals(JFritz.getProperty("filter.Phonebook.search",""))) {  //$NON-NLS-1$,  //$NON-NLS-2$
				JFritz.setProperty("filter.Phonebook.search", filter);  //$NON-NLS-1$
				jfritz.getPhonebook().clearFilterExceptions();
				jfritz.getPhonebook().updateFilter();
				jfritz.getPhonebook().fireTableDataChanged();
			}
		}

	}

	public void setSearchFilter(String text) {
		searchFilter.setText(text);
	}

	private void clearAllFilter() {
		setSearchFilter("");  //$NON-NLS-1$
		JFritz.setProperty("filter.Phonebook.search", "");  //$NON-NLS-1$,   //$NON-NLS-2$
		jfritz.getPhonebook().clearFilterExceptions();
		jfritz.getPhonebook().updateFilter();
		jfritz.getPhonebook().fireTableDataChanged();
	}

	public void keyReleased(KeyEvent arg0) {
		// unnötig

	}

	public void keyTyped(KeyEvent arg0) {
		// unnötig

	}

}
