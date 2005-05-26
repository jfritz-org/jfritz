/*
 *
 * Created on 14.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ResourceBundle;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.window.JFritzWindow;
import de.moonflower.jfritz.window.TextFieldCellEditor;

/**
 * @author Arno Willig
 *
 */
public class QuickDialDialog extends JDialog {

	JFritzProperties properties;

	ResourceBundle messages;

	QuickDialTableModel dataModel;

	JButton okButton, cancelButton, newButton, delButton;

	JTable table;

	private boolean pressed_OK = false;

	/**
	 * @param jframe
	 * @throws java.awt.HeadlessException
	 */
	public QuickDialDialog(JFritzWindow jframe) throws HeadlessException {
		super(jframe, true);
		if (jframe != null) {
			setLocationRelativeTo(jframe);
			this.properties = jframe.getProperties();
			this.messages = jframe.getMessages();
		}
		dataModel = new QuickDialTableModel(jframe.getJFritz());
		// dataModel.getQuickDialDataFromFritzBox();
		dataModel.loadFromXMLFile(JFritz.QUICKDIALS_FILE);
		drawDialog();
	}

	/**
	 * @param owner
	 */
	private void drawDialog() {
		super.dialogInit();

		setTitle(messages.getString("quickdials"));
		setModal(true);
		setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		JPanel bottomPane = new JPanel();

		JToolBar toolbar = new JToolBar();

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				Debug.msg("KEY: " + e);
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE
						|| (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					pressed_OK = false;
					setVisible(false);
				}
				if (e.getSource() == okButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER) {
					pressed_OK = true;
					setVisible(false);
				}
			}
		});
		addKeyListener(keyListener);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				pressed_OK = (source == okButton);
				setVisible((source != okButton) && (source != cancelButton));
				AbstractTableModel model = (AbstractTableModel) table
				.getModel();

				if (e.getActionCommand() == "deleteSIP") {
					int row = table.getSelectedRow();
					if (row >= 0) {
						dataModel.remove(row);
						model.fireTableRowsDeleted(row, row);
					}
				} else if (e.getActionCommand() == "addSIP") {
					dataModel.addEntry(new QuickDial("99","?","?","?"));
					model.fireTableDataChanged();
				} else if (e.getActionCommand() == "fetchSIP") {
					// FIXME: Preserve description data!
					dataModel.getQuickDialDataFromFritzBox();
					model.fireTableDataChanged();
				} else if (e.getActionCommand() == "storeSIP") {
					Debug.err("Not yet implemented");
				}
			}
		};

		okButton = new JButton("Okay");
		okButton.setEnabled(JFritz.DEVEL_VERSION);
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);

		cancelButton = new JButton("Abbruch");
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);

		newButton = new JButton("Neue Kurzwahl");
		newButton.setActionCommand("addSIP");
		newButton.addActionListener(actionListener);
		newButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/modify.png"))));
		delButton = new JButton("Kurzwahl löschen");
		delButton.setActionCommand("deleteSIP");
		delButton.addActionListener(actionListener);
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));

		JButton b1 = new JButton("Von der Box holen");
		b1.setActionCommand("fetchSIP");
		b1.addActionListener(actionListener);

		// b1.setIcon(new
		// ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/de/moonflower/jfritz/resources/images/import.png"))));

		JButton b2 = new JButton("Auf die Box speichern");
		b2.setActionCommand("storeSIP");
		b2.addActionListener(actionListener);
		b2.setEnabled(false);
		// b2.setIcon(new
		// ImageIcon(Toolkit.getDefaultToolkit().getImage(getClass().getResource("/de/moonflower/jfritz/resources/images/export.png"))));

		toolbar.add(newButton);
		toolbar.add(delButton);
		toolbar.add(b1);
		toolbar.add(b2);

		bottomPane.add(okButton);
		bottomPane.add(cancelButton);

		table = new JTable(dataModel) {
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
		if (table.getRowCount() != 0)
			table.setRowSelectionInterval(0, 0);

		table.getColumnModel().getColumn(0).setCellEditor(
				new TextFieldCellEditor());
		table.getColumnModel().getColumn(1).setCellEditor(
				new TextFieldCellEditor());
		// TODO new NumberFieldCellEditor());
		table.getColumnModel().getColumn(2).setCellEditor(
				new TextFieldCellEditor());
		table.getColumnModel().getColumn(3).setCellEditor(
				new TextFieldCellEditor());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(toolbar, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		panel.add(bottomPane, BorderLayout.SOUTH);
		getContentPane().add(panel);

		setSize(new Dimension(600, 350));
		// setResizable(false);
		// pack();
	}

	public boolean okPressed() {
		return pressed_OK;
	}

	public boolean showDialog() {
		setVisible(true);
		return okPressed();
	}

	/**
	 * @return Returns the dataModel.
	 */
	public final QuickDialTableModel getDataModel() {
		return dataModel;
	}
}
