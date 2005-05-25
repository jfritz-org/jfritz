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
import java.awt.FlowLayout;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.JFritzUtils;
import de.moonflower.jfritz.window.JFritzWindow;
import de.moonflower.jfritz.window.cellrenderer.ParticipantCellEditor;

/**
 * @author Arno Willig
 *
 */
public class QuickDialDialog extends JDialog {

	JFritzProperties properties;

	ResourceBundle messages;

	Vector quickDialData;

	JButton okButton, cancelButton, newButton, delButton;

	JTable table;

	private boolean pressed_OK = false;

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public QuickDialDialog(JFritzWindow owner) throws HeadlessException {
		super(owner, true);
		if (owner != null) {
			setLocationRelativeTo(owner);
			this.properties = owner.getProperties();
			this.messages = owner.getMessages();
		}
		getQuickDialData();
		drawDialog();
	}

	/**
	 *
	 */
	private void getQuickDialData() {
		try {
			quickDialData = JFritzUtils.retrieveQuickDialsFromFritzBox(
					properties.getProperty("box.address"), properties
							.getProperty("box.password"), JFritzUtils
							.detectBoxType(properties
									.getProperty("box.firmware"),properties
									.getProperty("box.address"), properties
									.getProperty("box.password")));
		} catch (WrongPasswordException e) {
			Debug.err("getQuickDialData: Wrong password");
		} catch (IOException e) {
			Debug.err("getQuickDialData: Box not found");
		}
	}

	/**
	 * @param owner
	 */
	private void drawDialog() {
		super.dialogInit();

		setTitle(messages.getString("quickdial"));
		setModal(true);
		setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		JPanel bottomPane = new JPanel();
		JPanel topPane = new JPanel();
		topPane.setLayout(new FlowLayout(FlowLayout.CENTER));

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
				if (e.getSource() == delButton) {
					int row = table.getSelectedRow();
					if (row >= 0) {
						quickDialData.remove(row);
						AbstractTableModel model = (AbstractTableModel) table
								.getModel();
						model.fireTableRowsDeleted(row, row);
					}
				}
			}
		};

		okButton = new JButton("Okay");
		okButton.setEnabled(JFritz.DEVEL_VERSION);
		cancelButton = new JButton("Abbruch");
		newButton = new JButton("Neue Kurzwahl");
		newButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/modify.png"))));
		delButton = new JButton("Kurzwahl löschen");
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));

		topPane.add(newButton);
		topPane.add(delButton);

		JButton b1 = new JButton("Von der Box holen");
		b1.setActionCommand("fetchSIP");
		b1.addActionListener(actionListener);
		JButton b2 = new JButton("Auf die Box speichern");
		b2.setEnabled(false);
		topPane.add(b1);
		topPane.add(b2);



		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		newButton.addActionListener(actionListener);
		delButton.addActionListener(actionListener);
		bottomPane.add(okButton);
		bottomPane.add(cancelButton);

		AbstractTableModel model = new AbstractTableModel() {

			public int getRowCount() {
				return quickDialData.size();
			}

			public int getColumnCount() {
				return 4;
			}

			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return messages.getString("description");
				case 1:
					return messages.getString("quickdial");
				case 2:
					return messages.getString("vanity");
				case 3:
					return messages.getString("number");
				default:
					return null;
				}
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				QuickDial quick = (QuickDial) quickDialData.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return quick.getDescription();
				case 1:
					return quick.getQuickdial();
				case 2:
					return quick.getVanity();
				case 3:
					return quick.getNumber();
				default:
					return null;

				}
			}

			/**
			 * Sets a value to a specific position
			 */
			public void setValueAt(Object object, int rowIndex, int columnIndex) {
				if (rowIndex < table.getRowCount()) {
					QuickDial dial = (QuickDial) quickDialData.get(rowIndex);

					switch (columnIndex) {
					case 0:
						dial.setDescription(object.toString());
						break;
					case 1:
						dial.setQuickdial(object.toString());
						break;
					case 2:
						dial.setVanity(object.toString());
						break;
					case 3:
						dial.setNumber(object.toString());
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
		if (table.getRowCount() != 0) table.setRowSelectionInterval(0, 0);
		table.getColumnModel().getColumn(0).setCellEditor(
				new ParticipantCellEditor());
		table.getColumnModel().getColumn(1).setCellEditor(
				new ParticipantCellEditor());
		table.getColumnModel().getColumn(2).setCellEditor(
				new ParticipantCellEditor());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		panel.add(bottomPane, BorderLayout.SOUTH);
		getContentPane().add(panel);

		setSize(new Dimension(400,350));
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
}
