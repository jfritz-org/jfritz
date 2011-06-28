package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import say.swing.JFontChooser;

public class FontChangeGui implements ActionListener {

	private JFrame frame;

	public static void main(String[] args) {
		FontChangeGui gui = new FontChangeGui();
	}

	public FontChangeGui() {
		frame = createGui();
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public FontChangeGui(Font f) {
		frame = createGui();
		frame.setFont(f);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	private JFrame createGui() {
		JFrame frame = new JFrame();
		frame.setBounds(0, 0, 800, 600);
		frame.setLayout(new BorderLayout());
		JLabel label = new JLabel("Test");
		frame.add(label, BorderLayout.CENTER);

		JButton button = new JButton("Change font");
		frame.add(button, BorderLayout.SOUTH);
		button.addActionListener(this);

		return frame;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JFontChooser fontChooser = new JFontChooser();
		int result = fontChooser.showDialog(this.frame);
		if (result == JFontChooser.OK_OPTION) {
			Font font = fontChooser.getSelectedFont();
			System.out.println(font);
			JFrame newFrame = createGui();
			Component[] components = newFrame.getComponents();
			for (int i=0; i<components.length; i++) {
				components[i].setFont(font);
			}
			newFrame.setFont(font);
			newFrame.repaint();
			frame.setVisible(false);
			frame = null;
			frame = newFrame;
			newFrame.setVisible(true);
		}
	}
}
