package de.moonflower.jfritz.callerlist.filter;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;

import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.utils.Debug;

public class CheckboxPopupMenu extends JDialog implements ActionListener {

	private static final long serialVersionUID = 1L;

	private Vector<JCheckBox> objects;

	private boolean ok_pressed = false;

	public CheckboxPopupMenu(JFrame parent) {
		super(parent);
		this.setUndecorated(true);
		this.setModal(true);
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
		this.add(popupPanel, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		JButton okButton;
		JButton cancelButton;
		buttonPanel.add(okButton = new JButton("OK"));
		buttonPanel.add(cancelButton = new JButton("Cancel"));
		okButton.setActionCommand("ok");
		cancelButton.setActionCommand("cancel");
		okButton.addActionListener(this);
		cancelButton.addActionListener(this);
		this.add(buttonPanel, BorderLayout.SOUTH);
		this.pack();
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
			String sip_filter = "";
			for (JCheckBox checkBox: objects) {
				if (checkBox.isSelected()) {
					sip_filter = sip_filter.concat(checkBox.getText()).concat(" ");
				}
			}
			sip_filter = sip_filter.trim();
			Main.setStateProperty("filter_sip_providers", sip_filter);
			Main.saveStateProperties();
			ok_pressed = true;
			this.setVisible(false);
		} else if (e.getActionCommand().equals("cancel")) {
			ok_pressed = false;
			this.setVisible(false);
		} else {
			Debug.warning("ActionCommand unknown: " + e.toString());
		}
	}

	public boolean okPressed() {
		return ok_pressed;
	}
}
