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

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.network.AddonInfosXMLHandler;
import de.moonflower.jfritz.utils.network.UPNPUtils;

/**
 * @author Arno Willig
 *
 */
public class StatsDialog extends JDialog {
	private static final long serialVersionUID = 1;
	JFritz jfritz;

	JButton okButton, cancelButton, refreshButton;

	JLabel byteSendRateLabel, byteReceiveRateLabel, totalBytesSendLabel,
			totalBytesReceivedLabel, dns1Label, dns2Label;

	private boolean pressed_OK = false;

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public StatsDialog(JFritzWindow owner) throws HeadlessException {
		super(owner, true);
		if (owner != null) {
			setLocationRelativeTo(owner);
			this.jfritz = owner.getJFritz();
		}
		drawDialog();
		getStats();
	}

	/**
	 *
	 */
	private void getStats() {
		final String server = "http://"+JFritz.getProperty("box.address")+":49000/upnp/control/WANCommonIFC1";
		final String urn = "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetAddonInfos";

		String xml = UPNPUtils.getSOAPData(server, urn);
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
					.getXMLReader();
			reader.setContentHandler(new AddonInfosXMLHandler(this));
			reader.parse(new InputSource(new StringReader(xml)));

		} catch (ParserConfigurationException e1) {
			System.err.println(e1);
		} catch (SAXException e1) {
			System.err.println(e1);
		} catch (IOException e1) {
			System.err.println(e1);
		}

	}

	/**
	 *
	 */
	private void drawDialog() {
		super.dialogInit();

		setTitle(JFritz.getMessage("stats"));
		setModal(true);
		getContentPane().setLayout(new BorderLayout());
		JPanel topPane = new JPanel();
		JPanel mainPane = new JPanel();
		JPanel bottomPane = new JPanel();
		BoxLayout boxlayout = new BoxLayout(mainPane, BoxLayout.Y_AXIS);
		mainPane.setBorder(BorderFactory.createEmptyBorder(10, 20, 5, 20));
		mainPane.setLayout(boxlayout);

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
				if (e.getSource() == refreshButton) {
					getStats();
				}
			}
		};

		okButton = new JButton("Okay");
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png"))));
		cancelButton = new JButton("Abbruch");
		refreshButton = new JButton("Statistik aktualisieren");
		refreshButton
				.setIcon(new ImageIcon(
						Toolkit
								.getDefaultToolkit()
								.getImage(
										getClass()
												.getResource(
														"/de/moonflower/jfritz/resources/images/modify.png"))));
		topPane.add(refreshButton);
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		refreshButton.addActionListener(actionListener);

		bottomPane.add(okButton);
		bottomPane.add(cancelButton);

		byteSendRateLabel = new JLabel();
		byteReceiveRateLabel = new JLabel();
		totalBytesSendLabel = new JLabel();
		totalBytesReceivedLabel = new JLabel();
		dns1Label = new JLabel();
		dns2Label = new JLabel();

		mainPane.add(byteSendRateLabel);
		mainPane.add(byteReceiveRateLabel);
		mainPane.add(totalBytesSendLabel);
		mainPane.add(totalBytesReceivedLabel);
		mainPane.add(dns1Label);
		mainPane.add(dns2Label);

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		panel.add(mainPane, BorderLayout.CENTER);
		panel.add(bottomPane, BorderLayout.SOUTH);
		getContentPane().add(panel);

		setSize(new Dimension(400, 200));
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

	public void setAddonInfos(int byteSendRate, int byteReceiveRate,
			int totalBytesSent, int totalBytesReceived, String dns1, String dns2) {
		byteSendRateLabel.setText(JFritz.getMessage(
				"bytessendrate")
				+ ": " + byteSendRate);
		byteReceiveRateLabel.setText(JFritz.getMessage(
				"bytesreceivedrate")
				+ ": " + byteReceiveRate);
		totalBytesSendLabel.setText(JFritz.getMessage(
				"totaldatasent")
				+ ": " + (totalBytesSent / 1024) + " KByte");
		totalBytesReceivedLabel.setText(JFritz.getMessage(
				"totaldatareceived")
				+ ": " + (totalBytesReceived / 1024) + " KByte");
		dns1Label.setText("DNS Server 1: " + dns1);
		dns2Label.setText("DNS Server 2: " + dns2);
	}

}
