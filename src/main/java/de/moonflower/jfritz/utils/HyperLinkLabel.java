package de.moonflower.jfritz.utils;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.net.URL;
import java.util.Vector;

import javax.swing.JLabel;

public class HyperLinkLabel extends JLabel {
	private static final long serialVersionUID = 8903409313417427315L;
	private URL url;
	private Vector<LinkClickListener> listener;

	private static MouseListener linker = new MouseAdapter() {
		public void mouseClicked(MouseEvent e) {
			HyperLinkLabel self = (HyperLinkLabel) e.getSource();
			if (self.url == null)
				return;
			for (LinkClickListener list: self.getClickListener())
			{
				list.clicked(self.getURL());
			}
		}

		public void mouseEntered(MouseEvent e) {
			e.getComponent().setCursor(
					Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		}
	};

	public HyperLinkLabel(String label) {
		super(label);
		setForeground(Color.BLUE);
		addMouseListener(linker);
		listener = new Vector<LinkClickListener>(2);
	}

	public HyperLinkLabel(String label, String tip) {
		this(label);
		setToolTipText(tip);
	}

	public HyperLinkLabel(String label, URL url) {
		this(label);
		this.url = url;
	}

	public HyperLinkLabel(String label, String tip, URL url) {
		this(label, url);
		setToolTipText(tip);
	}

	public void setURL(URL url) {
		this.url = url;
	}

	public URL getURL() {
		return url;
	}

	public void addClickListener(LinkClickListener l) {
		if (!listener.contains(l)) {
			listener.add(l);
		}
	}

	public void removeClickListener(LinkClickListener l) {
		if (listener.contains(l)) {
			listener.remove(l);
		}
	}

	public void clearClickListener() {
		listener.clear();
	}

	public Vector<LinkClickListener> getClickListener() {
		return listener;
	}
}
