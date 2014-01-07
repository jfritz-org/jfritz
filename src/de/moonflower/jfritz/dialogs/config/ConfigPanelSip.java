package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
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
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.dialogs.sip.SipProviderTableModel;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.utils.Debug;
import de.robotniko.fboxlib.exceptions.FirmwareNotDetectedException;
import de.robotniko.fboxlib.exceptions.PageNotFoundException;

public class ConfigPanelSip extends JPanel implements ConfigPanel {

	private static final long serialVersionUID = -630145657490186844L;

	private String configPath;

	private SipProviderTableModel sipProviderTableModel;

	private ConfigPanelFritzBoxIP fritzBoxPanel;
	protected MessageProvider messages = MessageProvider.getInstance();

	public ConfigPanelSip() {
		this.sipProviderTableModel = new SipProviderTableModel();
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(20, 20, 0, 20));

		JPanel sippane = new JPanel();
		sippane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.anchor = GridBagConstraints.WEST;

		JPanel sipButtonPane = new JPanel();
		final JTable siptable = new JTable(sipProviderTableModel) {
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
		siptable.getColumnModel().getColumn(0).setMinWidth(30);
		siptable.getColumnModel().getColumn(0).setMaxWidth(30);
		siptable.getColumnModel().getColumn(1).setMinWidth(40);
		siptable.getColumnModel().getColumn(1).setMaxWidth(40);
//		siptable.setSize(200, 150);

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getActionCommand().equals("fetchSIP")) { //$NON-NLS-1$
				    Container c = getPanel(); // get the window's content pane
					try {
					    c.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						fritzBoxPanel.detectBoxType();
						if (fritzBoxPanel.getFritzBox().getFirmware() != null) {
							// TODO login using settings!
						}
						updateTable();
//					} catch (WrongPasswordException e1) {
//						JFritz.errorMsg(messages.getMessage("box.wrong_password")); //$NON-NLS-1$
//						Debug.errDlg(messages.getMessage("box.wrong_password")); //$NON-NLS-1$
					} catch (IOException e1) {
						JFritz.errorMsg(messages.getMessage("box.not_found")); //$NON-NLS-1$
						Debug.errDlg(messages.getMessage("box.not_found")); //$NON-NLS-1$
					} catch (PageNotFoundException e1) {
						JFritz.errorMsg(messages.getMessage("box.communication_error")); //$NON-NLS-1$
						Debug.errDlg(messages.getMessage("box.communication_error")); //$NON-NLS-1$
					} catch (FirmwareNotDetectedException e1) {
						JFritz.errorMsg(messages.getMessage("unknown_firmware")); //$NON-NLS-1$
						Debug.errDlg(messages.getMessage("unknown_firmware")); //$NON-NLS-1$
					}
					c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
			}
		};

		JButton b1 = new JButton(messages.getMessage("get_sip_provider_from_box")); //$NON-NLS-1$
		b1.setActionCommand("fetchSIP"); //$NON-NLS-1$
		b1.addActionListener(actionListener);
		
		JButton b2 = new JButton(messages.getMessage("save_sip_provider_on_box")); //$NON-NLS-1$
		b2.setEnabled(false);
		
		sipButtonPane.add(b1);
		sipButtonPane.add(b2);

		sippane.setLayout(new BorderLayout());
		sippane.add(sipButtonPane, BorderLayout.NORTH);
		sippane.add(new JScrollPane(siptable), BorderLayout.CENTER);

		add(new JScrollPane(sippane), BorderLayout.CENTER);
	}

	public void loadSettings() {
		updateTable();
	}

	public void saveSettings() {
	}

	public void setPath(String path) {
		this.configPath = path;
	}

	public String getPath()
	{
		return this.configPath;
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

	public void updateTable()
	{
		if ((fritzBoxPanel != null)
			&& (fritzBoxPanel.getFritzBox() != null))
		{
			Vector<SipProvider> sipProvider = fritzBoxPanel.getFritzBox().getSipProvider();
			if (sipProvider != null) {
				sipProviderTableModel.updateProviderList(sipProvider);
				sipProviderTableModel.fireTableDataChanged();
				JFritz.getCallerList().fireTableDataChanged();
			}
		}
	}

	public void setFritzBoxPanel(ConfigPanelFritzBoxIP fritzBoxPanel)
	{
		this.fritzBoxPanel = fritzBoxPanel;
	}

	public boolean shouldRefreshJFritzWindow() {
		return false;
	}

	public boolean shouldRefreshTrayMenu() {
		return false;
	}
}
