package de.moonflower.jfritz.utils;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

public class JOptionPaneHtml {

	public static void showMessageDialog(Component parentComponent, String message) {
		JEditorPane ep = createHtmlPane(message);
		JOptionPane.showMessageDialog(parentComponent, ep);
	}

	public static void showMessageDialog(Component parentComponent, String message, String title, int messageType) {
		JEditorPane ep = createHtmlPane(message);
		JOptionPane.showMessageDialog(parentComponent, ep, title, messageType);
	}

	public static void showMessageDialog(Component parentComponent, String message, String title, int messageType, Icon icon) {
		JEditorPane ep = createHtmlPane(message);
		JOptionPane.showMessageDialog(parentComponent, ep, title, messageType, icon);
	}

	private static JEditorPane createHtmlPane(String message) {
		JLabel label = new JLabel();
		JEditorPane ep = new JEditorPane("text/html", message);
		ep.addHyperlinkListener(new HyperlinkListener() {
			
			@Override
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (e.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)) {
					BrowserLaunch.openURL(e.getURL().toString());
				}
			}
		});
		ep.setEditable(false);
		ep.setBackground(label.getBackground());
		return ep;
	}
}
