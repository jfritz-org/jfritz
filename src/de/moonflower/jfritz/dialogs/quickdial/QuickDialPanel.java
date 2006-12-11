/*
 * Created on 05.06.2005
 *
 */
package de.moonflower.jfritz.dialogs.quickdial;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.struct.QuickDial;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.StatusBarController;

/**
 * Main panel for QuickDials
 *
  */
public class QuickDialPanel extends JPanel implements ActionListener,
		ListSelectionListener {
	private static final long serialVersionUID = 1;

	private QuickDials dataModel;

	private JTable quickdialtable;

	private JButton addButton, delButton;

	private StatusBarController statusBarController = new StatusBarController();

	public QuickDialPanel(QuickDials dataModel) {
		setLayout(new BorderLayout());
		this.dataModel = dataModel;
		// dataModel.getQuickDialDataFromFritzBox();

		this.dataModel.addTableModelListener(new TableModelListener() {
			public void tableChanged(TableModelEvent e) {
				updateButtons();
			}
		});
		add(createQuickDialToolBar(), BorderLayout.NORTH);
		add(createQuickDialTable(), BorderLayout.CENTER);
	}

	private JToolBar createQuickDialToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(true);

		addButton = new JButton(Main.getMessage("new_quickdial"));  //$NON-NLS-1$
		addButton.setActionCommand("addSIP");  //$NON-NLS-1$
		addButton.addActionListener(this);
		addButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/add.png"))));  //$NON-NLS-1$

		delButton = new JButton(Main.getMessage(
				"delete_quickdial"));  //$NON-NLS-1$
		delButton.setActionCommand("deleteSIP");  //$NON-NLS-1$
		delButton.addActionListener(this);
		delButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/delete.png"))));  //$NON-NLS-1$

		JButton fetchButton = new JButton(Main.getMessage(
				"fetch_from_box"));  //$NON-NLS-1$
		fetchButton.setActionCommand("fetchSIP");  //$NON-NLS-1$
		fetchButton.addActionListener(this);

		JButton storeButton = new JButton(Main.getMessage(
				"store_to_box"));  //$NON-NLS-1$
		storeButton.setEnabled(false);
		storeButton.setActionCommand("storeSIP");  //$NON-NLS-1$
		storeButton.addActionListener(this);

		toolBar.add(addButton);
		toolBar.add(delButton);
		toolBar.add(fetchButton);
		toolBar.add(storeButton);

		return toolBar;
	}

	private JScrollPane createQuickDialTable() {
		quickdialtable = new JTable(dataModel) {
			private static final long serialVersionUID = 1;
			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if ((rowIndex % 2 == 0) && !isCellSelected(rowIndex, vColIndex)) {
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
		quickdialtable.setRowHeight(24);
		quickdialtable.setFocusable(false);
		quickdialtable.setAutoCreateColumnsFromModel(true);
		quickdialtable.setColumnSelectionAllowed(false);
		quickdialtable.setCellSelectionEnabled(false);
		quickdialtable.setRowSelectionAllowed(true);
		quickdialtable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		quickdialtable.getSelectionModel().addListSelectionListener(this);

		if (quickdialtable.getRowCount() != 0) {
			quickdialtable.setRowSelectionInterval(0, 0);
		}

		// TODO new QuickDialFieldCellEditor());
		/*
		 * quickdialtable.getColumnModel().getColumn(0).setCellEditor( new
		 * TextFieldCellEditor());
		 * quickdialtable.getColumnModel().getColumn(1).setCellEditor( new
		 * TextFieldCellEditor());
		 * quickdialtable.getColumnModel().getColumn(2).setCellEditor( new
		 * TextFieldCellEditor());
		 * quickdialtable.getColumnModel().getColumn(3).setCellEditor( new
		 * TextFieldCellEditor());
		 */
		return new JScrollPane(quickdialtable);
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("deleteSIP")) {  //$NON-NLS-1$
			int row = quickdialtable.getSelectedRow();
			if (row >= 0) {
				dataModel.remove(row);
				dataModel.fireTableRowsDeleted(row, row);
			}
		} else if (e.getActionCommand().equals("addSIP")) {  //$NON-NLS-1$
			dataModel.addEntry(new QuickDial("99", "?", "?", "?"));  //$NON-NLS-1$,   //$NON-NLS-2$,   //$NON-NLS-3$,   //$NON-NLS-4$
			dataModel.fireTableDataChanged();
			updateButtons();
		} else if (e.getActionCommand().equals("fetchSIP")) {  //$NON-NLS-1$
			dataModel.getQuickDialDataFromFritzBox();
			dataModel.fireTableDataChanged();
		} else if (e.getActionCommand().equals("storeSIP")) {  //$NON-NLS-1$
			Debug.err("Not yet implemented");  //$NON-NLS-1$
			JOptionPane.showMessageDialog(null,
					Main.getMessage("not_implemented"));  //$NON-NLS-1$
		}

	}

	/**
	 * @see javax.swing.event.ListSelectionListener#valueChanged(javax.swing.event.ListSelectionEvent)
	 */
	public void valueChanged(ListSelectionEvent e) {
		if (!e.getValueIsAdjusting()) {
			updateButtons();
		}

	}

	public void updateButtons() {

		delButton.setEnabled((quickdialtable.getSelectedRow() > -1)
				&& (dataModel.getRowCount() > 0));

		Enumeration en = dataModel.getQuickDials().elements();
		boolean addEnabled = true;
		while (en.hasMoreElements()) {
			String nr = ((QuickDial) en.nextElement()).getQuickdial();
			if (nr.equals("99") || (nr.equals(""))) { //$NON-NLS-1$,  //$NON-NLS-2$
				addEnabled = false;
				break;
			}
		}
		addButton.setEnabled(addEnabled);
	}
	public void setStatus() {
		statusBarController.fireStatusChanged(Main.getMessage("entries").  //$NON-NLS-1$
				replaceAll("%N", Integer.toString(dataModel.getQuickDials().size())));  //$NON-NLS-1$
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}
}
