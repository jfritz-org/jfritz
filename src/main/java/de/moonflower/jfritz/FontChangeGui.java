package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import io.github.dheid.fontchooser.FontChooser;
import io.github.dheid.fontchooser.FontDialog;

public class FontChangeGui implements ActionListener {

	private JFrame frame;

	public static void main(String[] args) {
		@SuppressWarnings("unused")
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
		FontDialog fontDialog = new FontDialog(this.frame, true);
		fontDialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		fontDialog.setVisible(true);
		if (!fontDialog.isCancelSelected()) {
			Font font = fontDialog.getSelectedFont();
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
