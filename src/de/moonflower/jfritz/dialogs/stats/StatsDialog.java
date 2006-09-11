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
 * Displays a dialog with some statistical data
 * @author Arno Willig
 *
 */
public class StatsDialog extends JDialog {
	private static final long serialVersionUID = 1;

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
		final String server = "http://"+JFritz.getFritzBox().getAddress() //$NON-NLS-1$
		+":49000/upnp/control/WANCommonIFC1"; //$NON-NLS-1$
		final String urn = "urn:schemas-upnp-org:service:WANCommonInterfaceConfig:1#GetAddonInfos"; //$NON-NLS-1$

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

		setTitle(JFritz.getMessage("stats")); //$NON-NLS-1$
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

		okButton = new JButton(JFritz.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(JFritz.getMessage("cancel")); //$NON-NLS-1$
		refreshButton = new JButton(JFritz.getMessage("actualize_statistics")); //$NON-NLS-1$
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

		setSize(new Dimension(400, 210));
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
				"bytessendrate") //$NON-NLS-1$
				+ ": " + byteSendRate); //$NON-NLS-1$
		byteReceiveRateLabel.setText(JFritz.getMessage(
				"bytesreceivedrate") //$NON-NLS-1$
				+ ": " + byteReceiveRate); //$NON-NLS-1$
		totalBytesSendLabel.setText(JFritz.getMessage(
				"totaldatasent") //$NON-NLS-1$
				+ ": " + (totalBytesSent / 1024) + " KByte"); //$NON-NLS-1$,  //$NON-NLS-2$
		totalBytesReceivedLabel.setText(JFritz.getMessage(
				"totaldatareceived") //$NON-NLS-1$
				+ ": " + (totalBytesReceived / 1024) + " KByte"); //$NON-NLS-1$,  //$NON-NLS-2$
		dns1Label.setText("DNS Server 1: " + dns1); //$NON-NLS-1$
		dns2Label.setText("DNS Server 2: " + dns2); //$NON-NLS-1$
	}

}
