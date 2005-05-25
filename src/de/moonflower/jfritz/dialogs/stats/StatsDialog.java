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
import java.util.ResourceBundle;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.JFritzProperties;
import de.moonflower.jfritz.utils.upnp.AddonInfosXMLHandler;
import de.moonflower.jfritz.utils.upnp.UPNPUtils;
import de.moonflower.jfritz.window.JFritzWindow;

/**
 * @author Arno Willig
 *
 */
public class StatsDialog extends JDialog {

	JFritzProperties properties;

	ResourceBundle messages;

	Vector quickDialData;

	JButton okButton, cancelButton, refreshButton;

	private boolean pressed_OK = false;

	/**
	 * @param owner
	 * @throws java.awt.HeadlessException
	 */
	public StatsDialog(JFritzWindow owner) throws HeadlessException {
		super(owner, true);
		if (owner != null) {
			setLocationRelativeTo(owner);
			this.properties = owner.getProperties();
			this.messages = owner.getMessages();
		}
		getStats();
		drawDialog();
	}

	/**
	 *
	 */
	private void getStats() {
		String xml = UPNPUtils.getSOAPData();
		try {
			XMLReader reader = SAXParserFactory.newInstance().newSAXParser()
					.getXMLReader();
			reader.setContentHandler(new AddonInfosXMLHandler());
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

		setTitle(messages.getString("stats"));
		setModal(true);
		setLayout(new BorderLayout());
		getContentPane().setLayout(new BorderLayout());
		JPanel bottomPane = new JPanel();
		JPanel topPane = new JPanel();
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
		okButton.setEnabled(JFritz.DEVEL_VERSION);
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

		JPanel panel = new JPanel();
		panel.setLayout(new BorderLayout());
		panel.add(topPane, BorderLayout.NORTH);
		//panel.
		// panel.add(new JScrollPane(table), BorderLayout.CENTER);

		panel.add(bottomPane, BorderLayout.SOUTH);
		getContentPane().add(panel);

		setSize(new Dimension(400, 350));
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
