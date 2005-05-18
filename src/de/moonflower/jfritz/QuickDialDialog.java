/*
 *
 * Created on 14.05.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.*;
import javax.swing.table.*;

/**
 * @author Arno Willig
 *
 */
public class QuickDialDialog extends JDialog {

	Properties properties;

	ResourceBundle messages;

	Vector quickDialData;

	JButton okButton, cancelButton, newButton, delButton;

	JTable table;

	private boolean pressed_OK = false;

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public QuickDialDialog(JFritz owner) throws HeadlessException {
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
									.getProperty("box.address"), properties
									.getProperty("box.password")));
		} catch (WrongPasswordException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
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
		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				System.out.println("KEY: " + e);
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
				return 3;
			}

			public String getColumnName(int column) {
				switch (column) {
				case 0:
					return messages.getString("quickdial");
				case 1:
					return messages.getString("vanity");
				case 2:
					return messages.getString("number");
				default:
					return null;
				}
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				QuickDial quick = (QuickDial) quickDialData.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return quick.getQuickdial();
				case 1:
					return quick.getVanity();
				case 2:
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
						dial.setQuickdial(object.toString());
						break;
					case 1:
						dial.setVanity(object.toString());
						break;
					case 2:
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
		table.setRowSelectionInterval(0, 0);
		table.getColumnModel().getColumn(0).setCellEditor(
				new ParticipantCellEditor());
		table.getColumnModel().getColumn(1).setCellEditor(
				new ParticipantCellEditor());
		table.getColumnModel().getColumn(2).setCellEditor(
				new ParticipantCellEditor());

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		//panel.
		panel.add(new JScrollPane(table), BorderLayout.CENTER);

		panel.add(bottomPane, BorderLayout.SOUTH);
		getContentPane().add(panel);
		pack();
	}

	public boolean okPressed() {
		return pressed_OK;
	}

	public boolean showDialog() {
		setVisible(true);
		return okPressed();
	}
}
