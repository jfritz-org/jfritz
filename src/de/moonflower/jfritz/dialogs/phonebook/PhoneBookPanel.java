/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.phonebook;

import java.awt.BorderLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
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
		PropertyChangeListener, ActionListener {
	private static final long serialVersionUID = 1;

	private final int PERSONPANEL_WIDTH = 350;

	private JFritz jfritz;

	private PhoneBookTable phoneBookTable;

	private PersonPanel personPanel;

	private JSplitPane splitPane;

	private JButton saveButton, cancelButton;

	private JPopupMenu popupMenu;

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
		saveButton = new JButton(JFritz.getMessage("save"));
		saveButton.setActionCommand("save");
		saveButton.addActionListener(this);
		saveButton.setIcon(getImage("okay.png"));
		cancelButton = new JButton(JFritz.getMessage("reset"));
		cancelButton.setActionCommand("cancel");
		cancelButton.addActionListener(this);
		editButtonPanel.add(saveButton);
		editButtonPanel.add(cancelButton);

		personPanel = new PersonPanel(jfritz, new Person());
		personPanel.addPropertyChangeListener("hasChanged", this);
		editPanel.add(personPanel, BorderLayout.CENTER);
		editPanel.add(editButtonPanel, BorderLayout.SOUTH);
		return editPanel;
	}

	public JToolBar createPhoneBookToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);
		JButton addButton = new JButton(JFritz.getMessage("new_entry"));
		addButton.setIcon(getImage("add.png"));
		addButton.setActionCommand("addPerson");
		addButton.addActionListener(this);
		toolBar.add(addButton);

		JButton delButton = new JButton(JFritz.getMessage("delete_entry"));
		delButton.setToolTipText(JFritz.getMessage("delete_entry"));
		delButton.setIcon(getImage("delete.png"));
		delButton.setActionCommand("deletePerson");
		delButton.addActionListener(this);
		toolBar.add(delButton);

		toolBar.addSeparator();

		JButton exportVCardButton = new JButton();
		exportVCardButton.setIcon(getImage("vcard.png"));
		exportVCardButton.setToolTipText(JFritz.getMessage("export_vcard"));
		exportVCardButton.setActionCommand("export_vcard");
		exportVCardButton.addActionListener(this);
		toolBar.add(exportVCardButton);

		toolBar.addSeparator();
		toolBar.addSeparator();
		toolBar.addSeparator();

		JToggleButton tb = new JToggleButton(getImage("addbook_grey.png"), true);
		tb.setSelectedIcon(getImage("addbook.png"));
		tb.setActionCommand("filter_private");
		tb.addActionListener(this);
		tb.setToolTipText(JFritz.getMessage("private_entry"));
		tb.setSelected(JFritzUtils.parseBoolean(JFritz.getProperty(
				"filter_private", "false")));
		toolBar.add(tb);

		toolBar.addSeparator();
		toolBar.addSeparator();

		JButton importXMLButton = new JButton();
		importXMLButton.setIcon(getImage("xml_import_kl.png"));
		importXMLButton.setToolTipText(JFritz.getMessage("phonebook_import"));
		importXMLButton.setActionCommand("import_xml");
		importXMLButton.addActionListener(this);
		toolBar.add(importXMLButton);

		return toolBar;
	}

	public JScrollPane createPhoneBookTable() {
		popupMenu = new JPopupMenu();
		JMenuItem menuItem;
		menuItem = new JMenuItem("Markierte Einträge löschen");
		menuItem.setActionCommand("deletePerson");
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);
		menuItem = new JMenuItem("Markierten Eintrag bearbeiten");
		menuItem.setActionCommand("editPerson");
		menuItem.addActionListener(this);
		popupMenu.add(menuItem);
		menuItem = new JMenuItem("Als VCard exportieren");
		menuItem.setActionCommand("export_vcard");
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
				personPanel.setPerson(p);
				setStatus();
			}
			else {
				jfritz.getJframe().setStatus( rows.length + " Einträge ausgewählt");
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
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("cancel")) {
			personPanel.updateGUI();
		} else if (e.getActionCommand().equals("save")) {
			personPanel.updatePerson();
			jfritz.getPhonebook().fireTableDataChanged();
			jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
		} else if (e.getActionCommand().equals("addPerson")) {
			jfritz.getPhonebook().addEntry(new Person("", " NEU "));
			jfritz.getPhonebook().fireTableDataChanged();
		} else if (e.getActionCommand().equals("deletePerson")) {
			removeSelectedPersons();
		} else if (e.getActionCommand().equals("editPerson")) {
			// Edit Panel anzeigen, falls verborgen
			int loc = splitPane.getDividerLocation();
			if (loc < PERSONPANEL_WIDTH)
				splitPane.setDividerLocation(PERSONPANEL_WIDTH);
			;
		} else if (e.getActionCommand().equals("filter_private")) {
			JFritz.setProperty("filter_private", Boolean
					.toString(((JToggleButton) e.getSource()).isSelected()));
			jfritz.getPhonebook().updateFilter();
		} else if (e.getActionCommand().equals("export_vcard")) {
			exportVCard();
		} else if (e.getActionCommand().equals("import_xml")) {
			importFromXML ();
		} else {
			Debug.msg("Unsupported Command: " + e.getActionCommand());
		}
		propertyChange(null);
	}

	/**
	 * Removes selected persons from phonebook
	 *
	 */
	public void removeSelectedPersons() {
		if (JOptionPane.showConfirmDialog(this,
				"Markierte Einträge wirklich löschen?", JFritz.PROGRAM_NAME,
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
				jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
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
						"/de/moonflower/jfritz/resources/images/" + filename)));
	}

	/**
	 * Exports VCard or VCardList
	 */
	public void exportVCard() {
		VCardList list = new VCardList();
		JFileChooser fc = new JFileChooser(JFritz.getProperty("options.exportVCARDpath",null));
		fc.setDialogTitle(JFritz.getMessage("export_vcard"));
		fc.setDialogType(JFileChooser.SAVE_DIALOG);
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				return f.isDirectory()
						|| f.getName().toLowerCase().endsWith(".vcf");
			}

			public String getDescription() {
				return "VCard (.vcf)";
			}
		});
		int rows[] = getPhoneBookTable().getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			Person person = (Person) jfritz.getPhonebook().getPersonAt(rows[i]);
			if (person != null && person.getFullname() != "") {
				list.addVCard(person);
			}
		}
		if (list.getCount() > 0) {
			if (list.getCount() == 1) {
				fc.setSelectedFile(new File(list.getPerson(0)
						.getStandardTelephoneNumber()
						+ ".vcf"));
			} else if (list.getCount() > 1) {
				fc.setSelectedFile(new File("jfritz.vcf"));
			}
			if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			    String path = fc.getSelectedFile().getPath();
			    path = path.substring(0,path.length()-fc.getSelectedFile().getName().length());
			    JFritz.setProperty("options.exportVCARDpath", path);
				File file = fc.getSelectedFile();
				if (file.exists()) {
					if (JOptionPane.showConfirmDialog(this, "Soll die Datei "+file.getName()+ " überschrieben werden?", "Datei überschreiben?", JOptionPane.YES_NO_OPTION) == JOptionPane.OK_OPTION) {
						list.saveToFile(file);
					}
				} else {
					list.saveToFile(file);
				}
			}
		} else {
			jfritz.errorMsg("Keine einzige sinnvolle Zeile selektiert!\n\n"
					+ "Bitte eine oder mehrere Zeilen auswählen,\n"
					+ "um die Daten als VCard zu exportieren!");
		}
	}

	public void setStatus() {
		PhoneBook pb = (PhoneBook) phoneBookTable.getModel();
		int entries = pb.getFilteredPersons().size();
		jfritz.getJframe().setStatus(entries + " Einträge");
	}

	public void importFromXML () {
		JFileChooser fc = new JFileChooser();
		if (fc.showOpenDialog(jfritz.getJframe()) != JFileChooser.APPROVE_OPTION) return;
		File file = fc.getSelectedFile();
		jfritz.getPhonebook().loadFromXMLFile(file.getAbsolutePath());
		jfritz.getPhonebook().saveToXMLFile(JFritz.PHONEBOOK_FILE);
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
}
