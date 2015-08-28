package de.moonflower.jfritz.callerlist.filter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.Debug;
import de.moonflower.jfritz.utils.HyperLinkLabel;
import de.moonflower.jfritz.utils.LinkClickListener;

public class CheckboxPopupMenu extends JDialog implements ActionListener, LinkClickListener {
	private final static Logger log = Logger.getLogger(CheckboxPopupMenu.class);

	private static final long serialVersionUID = 1L;

	private static final String allUrlStr = "http://all";
	private static final String noneUrlStr = "http://none";

	private Vector<JCheckBox> objects;

	private boolean ok_pressed = false;

	private String filterStateProperty = "";

	private PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	public CheckboxPopupMenu(JFrame parent, String stateProperty) {
		super(parent);
		this.setUndecorated(true);
		this.setModal(true);
		this.filterStateProperty = stateProperty;
	}

	public void setObjects(Vector<String> obj) {
		objects = new Vector<JCheckBox>(obj.size());
		Collections.sort(obj);
		this.setLayout(new BorderLayout());
		JPanel popupPanel = new JPanel();
		popupPanel.setLayout(new BoxLayout(popupPanel, BoxLayout.Y_AXIS));
		for (int i=0; i<obj.size(); i++) {
			objects.add(new JCheckBox(obj.get(i)));
			popupPanel.add(objects.get(i));
		}
		JScrollPane scrollPane = new JScrollPane(popupPanel);
		scrollPane.setAutoscrolls(true);
		this.add(scrollPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
		JButton okButton;
		JButton cancelButton;
		buttonPanel.add(okButton = new JButton(messages.getMessage("okay")));
		buttonPanel.add(cancelButton = new JButton(messages.getMessage("cancel")));
		okButton.setActionCommand("ok");
		cancelButton.setActionCommand("cancel");
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);

		try {
			JPanel linkPanel = new JPanel();

			URL allUrl = new URL(allUrlStr);
			URL noneUrl = new URL(noneUrlStr);

			HyperLinkLabel allLink = new HyperLinkLabel(messages.getMessage("select_all"), allUrl);
			allLink.addClickListener(this);
			linkPanel.add(allLink);

			HyperLinkLabel noneLink = new HyperLinkLabel(messages.getMessage("select_none"), noneUrl);;
			noneLink.addClickListener(this);
			linkPanel.add(noneLink);

			this.add(linkPanel, BorderLayout.SOUTH);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		this.add(buttonPanel, BorderLayout.NORTH);
		this.pack();

		if (obj.size() > 8) {
			scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth()+40, 200));
			this.pack();
		} else {
			scrollPane.setPreferredSize(new Dimension(scrollPane.getWidth()+40, scrollPane.getHeight()));
			this.pack();
		}
		ok_pressed = false;
	}

	public void setSelected(String obj, boolean sel) {
		boolean allSelected = obj.equals("$ALL$");
		for (int i=0; i<objects.size(); i++) {
			if (allSelected || objects.get(i).getText().equals(obj)) {
				objects.get(i).setSelected(sel);
			}
		}
	}

	public Vector<String> getSelectedItems() {
		Vector<String> list = new Vector<String>(objects.size());
		for (JCheckBox checkBox: objects) {
			if (checkBox.isSelected()) {
				list.add(checkBox.getText());
			}
		}
		return list;
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ok")) {
			String filter = "";
			for (JCheckBox checkBox: objects) {
				if (checkBox.isSelected()) {
					filter = filter.concat(checkBox.getText()).concat(";");
				}
			}
			filter = filter.trim();
			properties.setStateProperty(filterStateProperty, filter);
			properties.saveStateProperties();
			ok_pressed = true;
			this.setVisible(false);
		} else if (e.getActionCommand().equals("cancel")) {
			ok_pressed = false;
			this.setVisible(false);
		} else {
			log.warn("ActionCommand unknown: " + e.toString());
		}
	}

	public boolean okPressed() {
		return ok_pressed;
	}

	public void clicked(URL url) {
		if (allUrlStr.equals(url.toString())) {
			Debug.debug(log, "Select all");
			this.setSelected("$ALL$", true);
		} else if (noneUrlStr.equals(url.toString())) {
			Debug.debug(log, "Select none");
			this.setSelected("$ALL$", false);
		} else {
			log.warn("Unknown url clicked");
		}
	}
}
