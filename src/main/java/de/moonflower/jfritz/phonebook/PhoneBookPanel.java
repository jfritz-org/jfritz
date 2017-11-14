/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.phonebook;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzDataDirectory;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.VCardList;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.utils.StatusBarController;

/**
 *
 */
public class PhoneBookPanel extends JPanel implements ListSelectionListener,
		ActionListener, KeyListener {
	class PopupListener extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			if (e.getClickCount() > 1) {
				int loc = splitPane.getDividerLocation();
				if (loc < PERSONPANEL_WIDTH) {
					splitPane.setDividerLocation(PERSONPANEL_WIDTH);
				} else {
					splitPane.setDividerLocation(0);
				}
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
				adaptGoogleLink();
				popupMenu.show(e.getComponent(), e.getX(), e.getY());
			}
		}
	}
	private final static Logger log = Logger.getLogger(PhoneBookPanel.class);

	private static final long serialVersionUID = 1;

	private final int PERSONPANEL_WIDTH = 350;

	private PhoneBookTable phoneBookTable;

	private PersonPanel personPanel;

	private JSplitPane splitPane;

	private JPopupMenu popupMenu;
	private JFritzWindow parentFrame;
	public JButton resetButton;
	public JTextField searchFilter;
	public JToggleButton privateFilter;

	private JMenuItem googleItem;
	private String googleLink = null;

	private PhoneBook phonebook;
	private StatusBarController statusBarController = new StatusBarController();

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public PhoneBookPanel(PhoneBook phonebook, JFritzWindow parentFrame) {
		this.phonebook = phonebook;
		this.parentFrame = parentFrame;
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
	 * Added the code from haeusler DATE: 04.02.06, added by Brian
	 *
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("addPerson")) { //$NON-NLS-1$
			Person newPerson = new Person("", ""); //$NON-NLS-1$,  //$NON-NLS-2$
			Vector<Person> persons = new Vector<Person>();
			persons.add(newPerson);
			phonebook.addFilterException(newPerson);
			phonebook.addEntries(persons);
			phonebook.fireTableDataChanged();
			int index = phonebook.indexOf(newPerson);
			phoneBookTable.getSelectionModel().setSelectionInterval(index,
					index);
			int loc = splitPane.getDividerLocation();
			if (loc < PERSONPANEL_WIDTH) {
				splitPane.setDividerLocation(PERSONPANEL_WIDTH);
			}
			personPanel.focusFirstName();
		} else if (e.getActionCommand().equals("deletePerson")) { //$NON-NLS-1$
			removeSelectedPersons();
		} else if (e.getActionCommand().equals("editPerson")) { //$NON-NLS-1$
			// Edit Panel anzeigen, falls verborgen
			int loc = splitPane.getDividerLocation();
			if (loc < PERSONPANEL_WIDTH) {
				splitPane.setDividerLocation(PERSONPANEL_WIDTH);
			}
			personPanel.focusFirstName();
			;
		} else if (e.getActionCommand().equals("filter_private")) { //$NON-NLS-1$
			setPrivateFilter(privateFilter.isSelected());
			phonebook.updateFilter();
		} else if (e.getActionCommand().equals("export_vcard")) { //$NON-NLS-1$
			exportVCard();
		} else if (e.getActionCommand().equals("import_xml")) { //$NON-NLS-1$
			importFromXML();
		} else if (e.getActionCommand().equals("clearFilter")) { //$NON-NLS-1$
			clearAllFilter();
		} else if (e.getActionCommand().equals("google")) { //$NON-NLS-1$
			if (googleLink != null) {
				log.debug(googleLink);
				BrowserLaunch.openURL(googleLink);
			}
		} else {
			log.warn("Unsupported Command: " + e.getActionCommand()); //$NON-NLS-1$
		}
	}

	public JScrollPane createPhoneBookTable() {
		popupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem(messages.getMessage("phonebook_delPerson")); //$NON-NLS-1$
		menuItem.setActionCommand("deletePerson"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);
		menuItem = new JMenuItem(messages.getMessage("phonebook_editPerson")); //$NON-NLS-1$
		menuItem.setActionCommand("editPerson"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);
		menuItem = new JMenuItem(messages.getMessage("phonebook_vcardExport")); //$NON-NLS-1$
		menuItem.setActionCommand("export_vcard"); //$NON-NLS-1$
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);
		googleItem = new JMenuItem(messages.getMessage("show_on_google_maps"));
		googleItem.setActionCommand("google");
		googleItem.addActionListener(this);
		popupMenu.add(googleItem);

		// Add listener to components that can bring up popup menus.
		MouseAdapter popupListener = new PopupListener();

		phoneBookTable = new PhoneBookTable(this, phonebook);
		phoneBookTable.getSelectionModel().addListSelectionListener(this);
		phoneBookTable.addMouseListener(popupListener);
		return new JScrollPane(phoneBookTable);
	}

	public JToolBar createPhoneBookToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);
		JButton addButton = new JButton(messages.getMessage("new_entry")); //$NON-NLS-1$
		addButton.setIcon(getImage("add_person.png")); //$NON-NLS-1$
		addButton.setActionCommand("addPerson"); //$NON-NLS-1$
		addButton.addActionListener(this);
		toolBar.add(addButton);

		JButton editButton = new JButton(messages.getMessage("edit_entry")); //$NON-NLS-1$
		editButton.setToolTipText(messages.getMessage("edit_entry")); //$NON-NLS-1$
		editButton.setIcon(getImage("edit_person.png")); //$NON-NLS-1$
		editButton.setActionCommand("editPerson"); //$NON-NLS-1$
		editButton.addActionListener(this);
		toolBar.add(editButton);

		JButton delButton = new JButton(messages.getMessage("delete_entry")); //$NON-NLS-1$
		delButton.setToolTipText(messages.getMessage("delete_entry")); //$NON-NLS-1$
		delButton.setIcon(getImage("delete_person.png")); //$NON-NLS-1$
		delButton.setActionCommand("deletePerson"); //$NON-NLS-1$
		delButton.addActionListener(this);
		toolBar.add(delButton);

		toolBar.addSeparator();

		JButton exportVCardButton = new JButton();
		exportVCardButton.setIcon(getImage("vcard.png")); //$NON-NLS-1$
		exportVCardButton.setToolTipText(messages.getMessage("export_vcard")); //$NON-NLS-1$
		exportVCardButton.setActionCommand("export_vcard"); //$NON-NLS-1$
		exportVCardButton.addActionListener(this);
		toolBar.add(exportVCardButton);

		toolBar.addSeparator();
		// toolBar.addSeparator();
		// toolBar.addSeparator();

		privateFilter = new JToggleButton(getImage("addbook_grey.png"), true); //$NON-NLS-1$
		privateFilter.setSelectedIcon(getImage("addbook.png")); //$NON-NLS-1$
		privateFilter.setActionCommand("filter_private"); //$NON-NLS-1$
		privateFilter.addActionListener(this);
		privateFilter.setToolTipText(messages.getMessage("private_entry")); //$NON-NLS-1$
		privateFilter.setSelected(JFritzUtils.parseBoolean(properties
				.getStateProperty("filter_private"))); //$NON-NLS-1$,  //$NON-NLS-2$
		toolBar.add(privateFilter);

		// toolBar.addSeparator();
		toolBar.addSeparator();

		JButton importXMLButton = new JButton();
		importXMLButton.setIcon(getImage("import.gif")); //$NON-NLS-1$
		importXMLButton.setToolTipText(messages.getMessage("phonebook_import")); //$NON-NLS-1$
		importXMLButton.setActionCommand("import_xml"); //$NON-NLS-1$
		importXMLButton.addActionListener(this);
		toolBar.add(importXMLButton);

		toolBar.addSeparator();

		resetButton = new JButton();
		toolBar.add(new JLabel(messages.getMessage("search") + ": ")); //$NON-NLS-1$, //$NON-NLS-2$
		searchFilter = new JTextField(properties
				.getStateProperty("filter.Phonebook.search"), //$NON-NLS-1$,  //$NON-NLS-2$
				10);
		searchFilter.addKeyListener(this);
		toolBar.add(searchFilter);

		resetButton = new JButton(messages.getMessage("clear")); //$NON-NLS-1$
		resetButton.setActionCommand("clearFilter"); //$NON-NLS-1$
		resetButton.addActionListener(this);
		toolBar.add(resetButton);

		return toolBar;
	}

	/**
	 * Exports VCard or VCardList
	 */
	public void exportVCard() {
		VCardList list = new VCardList();
		JFileChooser fc = new JFileChooser(properties
				.getStateProperty("options.exportVCARDpath")); //$NON-NLS-1$
		fc.setDialogTitle(messages.getMessage("export_vcard")); //$NON-NLS-1$
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
			Person person = phonebook.getPersonAt(rows[i]);
			if ((person != null) && !person.getFullname().equals("")) { //$NON-NLS-1$
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
				path = path.substring(0, path.length()
						- fc.getSelectedFile().getName().length());
				properties.setStateProperty("options.exportVCARDpath", path); //$NON-NLS-1$
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane
							.showConfirmDialog(
									this,
									messages
											.getMessage("overwrite_file").replaceAll("%F", file.getName()), //$NON-NLS-1$,  //$NON-NLS-2$
									messages
											.getMessage("dialog_title_overwrite_file"), //$NON-NLS-1$
									JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						list.saveToFile(file);
					}
				} else {
					list.saveToFile(file);
				}
			}
		} else {
			String message = messages.getMessage("error_no_row_chosen"); //$NON-NLS-1$
			log.error(message);
			Debug.errDlg(message);
		}
	}

	public ImageIcon getImage(String filename) {
		return new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getClassLoader().getResource("images/" + filename))); //$NON-NLS-1$
	}

	/**
	 * @return Returns the personPanel.
	 */
	public final PersonPanel getPersonPanel() {
		return personPanel;
	}

	/**
	 * @return Returns the phoneBookTable.
	 */
	public final PhoneBookTable getPhoneBookTable() {
		return phoneBookTable;
	}

	public void importFromXML() {
		JFileChooser fc = new JFileChooser(properties
				.getStateProperty("option.phonebook.import_xml_path")); //$NON-NLS-1$
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".xml"); //$NON-NLS-1$
			}

			public String getDescription() {
				return messages.getMessage("xml_files"); //$NON-NLS-1$
			}
		});
		if (fc.showOpenDialog(parentFrame) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		properties
				.setStateProperty(
						"option.phonebook.import_xml_path", fc.getSelectedFile().getAbsolutePath()); //$NON-NLS-1$
		phonebook.loadFromXMLFile(fc.getSelectedFile().getAbsolutePath());
		phonebook.saveToXMLFile(JFritzDataDirectory.getInstance().getDataDirectory() + JFritz.PHONEBOOK_FILE);
	}

	/**
	 * added code form haeusler DATE: 04.02.06 Brian
	 *
	 */
	public void keyPressed(KeyEvent arg0) {
		if (arg0.getSource() == searchFilter
				&& arg0.getKeyCode() == KeyEvent.VK_ENTER) {
			JTextField search = (JTextField) arg0.getSource();

			// ignore whitespaces at the beginning and the end
			doSearchFilter(search.getText().trim());
		} else if (arg0.getSource() == searchFilter
				&& arg0.getKeyCode() == KeyEvent.VK_ESCAPE) {
			doSearchFilter("");
			phoneBookTable.requestFocus();
		}
	}

	private void doSearchFilter(final String filter) {
		// only update filter when the search expression has changed
		if (!filter.equals(properties
				.getStateProperty("filter.Phonebook.search"))) { //$NON-NLS-1$,  //$NON-NLS-2$
			properties.setStateProperty("filter.Phonebook.search", filter); //$NON-NLS-1$
			if ((phoneBookTable != null)
					&& (phoneBookTable.getCellEditor() != null)) {
				phoneBookTable.getCellEditor().cancelCellEditing();
			}
			phonebook.clearFilterExceptions();
			phonebook.updateFilter();
			phonebook.fireTableDataChanged();
		}

	}

	public void keyReleased(KeyEvent arg0) {
		// unnötig

	}

	public void keyTyped(KeyEvent arg0) {
		// unnötig

	}

	/**
	 * Removes selected persons from phonebook
	 *
	 */
	public void removeSelectedPersons() {
		if (getPhoneBookTable().getSelectedRowCount() > 0) {
			String message;
			if (getPhoneBookTable().getSelectedRowCount() == 1) {
				message = messages.getMessage("delete_entry");
			} else {
				message = messages.getMessage("delete_entries").replaceAll(
						"%N",
						Integer.toString(getPhoneBookTable()
								.getSelectedRowCount()));
			}
			if (JOptionPane.showConfirmDialog(this, message, //$NON-NLS-1$,  //$NON-NLS-2$,  //$NON-NLS-3$
					ProgramConstants.PROGRAM_NAME, JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

				personPanel.cancelEditing();
				phonebook.removePersons(getPhoneBookTable().getSelectedRows());
				phoneBookTable.getSelectionModel().setSelectionInterval(0, 0);
			}
		}
	}

	public void setSearchFilter(String text) {
		searchFilter.setText(text);
	}

	private void setPrivateFilter(boolean enabled) {
		properties.setStateProperty("filter_private", Boolean //$NON-NLS-1$
				.toString(enabled));
		privateFilter.setSelected(enabled);
	}

	public void setStatus() {
		PhoneBook pb = (PhoneBook) phoneBookTable.getModel();
		int entries = pb.getFilteredPersons().size();
		statusBarController.fireStatusChanged(messages.getMessage("entries"). //$NON-NLS-1$
				replaceAll("%N", Integer.toString(entries))); //$NON-NLS-1$
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
				if (p != null) {
					personPanel.setPerson(p, false);
				}
				setStatus();
			} else {
				statusBarController.fireStatusChanged(messages.getMessage(
						"phonebook_chosenEntries") //$NON-NLS-1$
						.replaceAll("%N", Integer.toString(rows.length))); //$NON-NLS-1$,
			}
		}
	}

	private void clearAllFilter() {
		setSearchFilter(""); //$NON-NLS-1$
		properties.setStateProperty("filter.Phonebook.search", ""); //$NON-NLS-1$,   //$NON-NLS-2$
		setPrivateFilter(false);
		phonebook.clearFilterExceptions();
		phonebook.updateFilter();
		phonebook.fireTableDataChanged();
	}

	/**
	 * @return editPanel
	 */
	private JPanel createEditPanel() {
		JPanel editPanel = new JPanel(new BorderLayout());

		personPanel = new PersonPanel(new Person(), phonebook, parentFrame);
		editPanel.add(personPanel, BorderLayout.CENTER);
		return editPanel;
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}

	public void adaptGoogleLink() {
		int[] selRows = phoneBookTable.getSelectedRows();

		googleLink = null;
		if ((selRows.length == 0) || (selRows.length > 1)) {
			updateGoogleItem(false);
		} else {
			Person person = phonebook.getFilteredPersons().get(
					phoneBookTable.getSelectedRow());
			googleLink = person.getGoogleLink();
			updateGoogleItem(true);
		}
	}

	public void updateGoogleItem(boolean status) {
		googleItem.setEnabled(status);
		JFritz.getJframe().setGoogleItem(status);
	}

	public void showEditPerson() {
		splitPane.setDividerLocation(PERSONPANEL_WIDTH);
	}

	public void hideEditPerson() {
		splitPane.setDividerLocation(0);
	}

	public void activateSearchFilter() {
		this.requestFocus();
		searchFilter.requestFocus();
		searchFilter.selectAll();
		doSearchFilter(searchFilter.getText());
	}
}
