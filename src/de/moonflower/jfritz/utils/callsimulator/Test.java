package de.moonflower.jfritz.utils.callsimulator;

import java.awt.BorderLayout;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import de.moonflower.jfritz.utils.JFritzUtils;

public class Test {

	static String HTML_HEAD = "<head>" + "<link rel=STYLESHEET TYPE=\"text/css\" HREF=\""+ "file:/" +
	JFritzUtils.getFullPath(JFritzUtils.rootID) + "bfashion.css"+ "\"></head>";
	public static void main(String[] argv) {

		JEditorPane jep = new JEditorPane("text/html", HTML_HEAD + "<div id=\"divU\" height=\"" + "300" +
     	  "\" width=\"" +"300" + "\"" + ">The rain in <a href='http://foo.com/'>"
			+"Spain</a> falls mainly on the <a href='http://bar.com/'>plain</a>.");
		jep.setEditable(false);
		jep.setOpaque(false);
		jep.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent hle) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle.getEventType())) {
					System.out.println(hle.getURL());
				}
			}
		});

		JPanel p = new JPanel();
		p.add( new JLabel("Foo.") );
		p.add( jep );
		p.add( new JLabel("Bar.") );

		JFrame f = new JFrame("HyperlinkListener");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setContentPane(p);
		f.setSize(400, 150);
		f.setVisible(true);
	}
}
