/*
 *
 * Created on 14.05.2005
 *
 */
package de.moonflower.jfritz.dialogs.stats;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.HeadlessException;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.StringReader;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.network.AddonInfosListener;
import de.moonflower.jfritz.utils.network.AddonInfosXMLHandler;

/**
 * Displays a dialog with some statistical data
 * @author Arno Willig
 *
 */
public class StatsDialog extends JDialog implements AddonInfosListener {
	private static final long serialVersionUID = 1;

	JButton okButton, cancelButton, refreshButton;

	JLabel totalBytesSentLabel, totalBytesReceivedLabel, dns1Label, dns2Label,
			voipDnsLabel1, voipDnsLabel2, upnpLabel, routedBridgeMode,
			autoDisconnectLabel, idleTimeLabel, externalIPLabel;

	private boolean pressed_OK = false;

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public StatsDialog(JFritzWindow owner) throws HeadlessException {
		super(owner, true);
		drawDialog();
		getStats();

		if (owner != null) {
			setLocationRelativeTo(owner);
		}
	}

	/**
	 *
	 */
	private void getStats() {

		JFritz.getFritzBox().getInternetStats(this);
		JFritz.getFritzBox().getWebservice();
		externalIPLabel.setText(JFritz.getFritzBox().getExternalIPAddress());

	}

	/**
	 *
	 */
	private void drawDialog() {
		super.dialogInit();

		setTitle(Main.getMessage("stats")); //$NON-NLS-1$
		setModal(true);
		getContentPane().setLayout(new BorderLayout());
		JPanel topPane = new JPanel();
		JPanel mainPane = new JPanel();
		JPanel bottomPane = new JPanel();
		mainPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
		mainPane.setLayout(new GridBagLayout());

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				Debug.msg("KEY: " + e); //$NON-NLS-1$
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
				if (e.getSource() == refreshButton) {
					getStats();
				}
			}
		};

		okButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$
		refreshButton = new JButton(Main.getMessage("actualize_statistics")); //$NON-NLS-1$
		refreshButton
				.setIcon(new ImageIcon(
						Toolkit.getDefaultToolkit().getImage(getClass()
								.getResource("/de/moonflower/jfritz/resources/images/modify.png")))); //$NON-NLS-1$
		topPane.add(refreshButton);
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		refreshButton.addActionListener(actionListener);

		//set default confirm button (Enter)
        getRootPane().setDefaultButton(okButton);

        //set default close button (ESC)
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        Action escapeAction = new AbstractAction()
        {
            private static final long serialVersionUID = 3L;

            public void actionPerformed(ActionEvent e)
            {
                 cancelButton.doClick();
            }
        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$

        bottomPane.add(okButton);
		bottomPane.add(cancelButton);

		totalBytesSentLabel = new JLabel();
		totalBytesReceivedLabel = new JLabel();
		dns1Label = new JLabel();
		dns2Label = new JLabel();
		voipDnsLabel1 = new JLabel();
		voipDnsLabel2 = new JLabel();
		upnpLabel = new JLabel();
		routedBridgeMode = new JLabel();
		autoDisconnectLabel = new JLabel();
		idleTimeLabel = new JLabel();
		externalIPLabel = new JLabel();

		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.insets.right = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		mainPane.add(new JLabel("Total Bytes Sent:"), c);
		c.gridx = 1;
		mainPane.add(totalBytesSentLabel, c);
		c.gridx = 2;
		mainPane.add(new JLabel("Total Bytes Received:"), c);
		c.gridx = 3;
		mainPane.add(totalBytesReceivedLabel, c);

		c.gridx = 0;
		c.gridy = 1;
		mainPane.add(new JLabel("DNS 1:"), c);
		c.gridx = 1;
		mainPane.add(dns1Label, c);
		c.gridx = 2;
		mainPane.add(new JLabel("DNS 2:"), c);
		c.gridx = 3;
		mainPane.add(dns2Label, c);

		c.gridx = 0;
		c.gridy = 2;
		mainPane.add(new JLabel("VoipDNS 1:"), c);
		c.gridx = 1;
		mainPane.add(voipDnsLabel1, c);
		c.gridx = 2;
		mainPane.add(new JLabel("VoipDNS 2:"), c);
		c.gridx = 3;
		mainPane.add(voipDnsLabel2, c);

		c.gridx = 0;
		c.gridy = 3;
		mainPane.add(new JLabel("Auto Disconnect Time:"), c);
		c.gridx = 1;
		mainPane.add(autoDisconnectLabel, c);
		c.gridx = 2;
		mainPane.add(new JLabel("Connection Idle Time:"), c);
		c.gridx = 3;
		mainPane.add(idleTimeLabel, c);

		c.gridx = 0;
		c.gridy = 4;
		mainPane.add(new JLabel("UPnP Control Enabled: "), c);
		c.gridx = 1;
		mainPane.add(upnpLabel, c);
		c.gridx = 2;
		mainPane.add(new JLabel("Routed Bridge Mode Both: "), c);
		c.gridx = 3;
		mainPane.add(routedBridgeMode, c);

		c.gridx = 0;
		c.gridy = 5;
		mainPane.add(new JLabel("External IP:"), c);
		c.gridx = 1;
		mainPane.add(externalIPLabel, c);


		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		panel.add(mainPane, BorderLayout.CENTER);
		panel.add(bottomPane, BorderLayout.SOUTH);
		getContentPane().add(panel);

		setSize(new Dimension(600, 310));
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

	public void setBytesRate(String sent, String received){
		//not needed here
	}

	public void setTotalBytesInfo(String sent, String received){
		totalBytesSentLabel.setText(sent);
		totalBytesReceivedLabel.setText(received);
	}

	public void setDNSInfo(String dns1, String dns2){
		dns1Label.setText(dns1);
		dns2Label.setText(dns2);
	}

	public void setVoipDNSInfo(String voipDns1, String voipDns2){
		voipDnsLabel1.setText(voipDns1);
		voipDnsLabel2.setText(voipDns2);
	}

	public void setDisconnectInfo(String disconnectTime, String idleTime){
		autoDisconnectLabel.setText(disconnectTime);
		idleTimeLabel.setText(idleTime);
	}

	public void setOtherInfo(String upnpControl, String routedMode){
		upnpLabel.setText(upnpControl);
		routedBridgeMode.setText(routedMode);
	}



}
