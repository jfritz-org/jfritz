package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.TableCellRenderer;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.Debug;

public class ConfigPanelSip extends JPanel implements ConfigPanel {

	private static final long serialVersionUID = -630145657490186844L;

	private ConfigPanelFritzBox fBoxPanel;

	public ConfigPanelSip(ConfigPanelFritzBox fritzBoxPanel) {
		this.fBoxPanel = fritzBoxPanel;
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel sippane = new JPanel();
		sippane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		JPanel sipButtonPane = new JPanel();
		final JTable siptable = new JTable(JFritz.getSIPProviderTableModel()) {
			private static final long serialVersionUID = 1;

			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
					c.setBackground(new Color(255, 255, 200));
				} else if (!isCellSelected(rowIndex, vColIndex)) {
					// If not shaded, match the table's background
					c.setBackground(getBackground());
				} else {
					c.setBackground(new Color(204, 204, 255));
				}
				return c;
			}
		};
		siptable.setRowHeight(24);
		siptable.setFocusable(false);
		siptable.setAutoCreateColumnsFromModel(false);
		siptable.setColumnSelectionAllowed(false);
		siptable.setCellSelectionEnabled(false);
		siptable.setRowSelectionAllowed(true);
		siptable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		siptable.getColumnModel().getColumn(0).setMinWidth(20);
		siptable.getColumnModel().getColumn(0).setMaxWidth(20);
		siptable.getColumnModel().getColumn(1).setMinWidth(40);
		siptable.getColumnModel().getColumn(1).setMaxWidth(40);
//		siptable.setSize(200, 150);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("fetchSIP")) { //$NON-NLS-1$
					try {
						JFritz.getFritzBox().setAddress(fBoxPanel.getAddress());
						JFritz.getFritzBox().setPassword(fBoxPanel.getPassword());
						JFritz.getFritzBox().setPort(fBoxPanel.getPort());
						JFritz.getFritzBox().detectFirmware();
						Vector<SipProvider> data = JFritz.getFritzBox().retrieveSipProvider();
						JFritz.getSIPProviderTableModel().updateProviderList(
								data);
						JFritz.getSIPProviderTableModel()
								.fireTableDataChanged();
						JFritz.getCallerList().fireTableDataChanged();

					} catch (WrongPasswordException e1) {
						JFritz.errorMsg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
					} catch (IOException e1) {
						JFritz.errorMsg(Main.getMessage("box.not_found")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("box.not_found")); //$NON-NLS-1$
					} catch (InvalidFirmwareException e1) {
						JFritz.errorMsg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
					}
				}
			}
		};

		JButton b1 = new JButton(Main.getMessage("get_sip_provider_from_box")); //$NON-NLS-1$
		b1.setActionCommand("fetchSIP"); //$NON-NLS-1$
		b1.addActionListener(actionListener);
		JButton b2 = new JButton(Main.getMessage("save_sip_provider_on_box")); //$NON-NLS-1$
		b2.setEnabled(false);
		sipButtonPane.add(b1);
		sipButtonPane.add(b2);

		sippane.setLayout(new BorderLayout());
		sippane.add(sipButtonPane, BorderLayout.NORTH);
		sippane.add(siptable, BorderLayout.CENTER);

		add(new JScrollPane(sippane), BorderLayout.CENTER);
	}

	public void loadSettings() {
	}

	public void saveSettings() {
	}

	public String getPath()
	{
		return Main.getMessage("sip_numbers");
	}

	public JPanel getPanel() {
		return this;
	}

	public String getHelpUrl() {
		return "http://jfritz.org/wiki/JFritz_Handbuch:Deutsch#SIP-Nummern";
	}

	public void cancel() {
		// TODO Auto-generated method stub

	}
}
