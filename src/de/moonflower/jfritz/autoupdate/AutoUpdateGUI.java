package de.moonflower.jfritz.autoupdate;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import de.moonflower.jfritz.Main;

public class AutoUpdateGUI extends JFrame {

	private static final long serialVersionUID = 5720893048026504125L;

	private JButton okButton, cancelButton;

	private int currentFileSize = 0;

	private int currentFileProgress = 0;

	private int  totalProgress = 0;

	private int totalSize = 0;

	private JLabel currentFileLabel, currentFileProgressLabel, totalLabel, totalProgressLabel;

	private JProgressBar currentFileProgressBar, totalProgressBar;

	public AutoUpdateGUI() {

        setLocation(0, 0);
        setSize(500, 200);

        setTitle(Main.getMessage("autoupdate_title"));

		createGUI();
	}

	private void createGUI() {

		// Create Current-File-Panel
		JPanel currentFilePanel = new JPanel();
		currentFilePanel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		currentFileLabel = new JLabel(Main.getMessage("autoupdate_current_file"));
		currentFilePanel.add(currentFileLabel, c);

		c.gridy = 1;
		currentFileProgressBar = new JProgressBar();
		currentFileProgressBar.setSize(200,10);
		currentFilePanel.add(currentFileProgressBar, c);

		c.gridy = 2;
		currentFileProgressLabel = new JLabel(currentFileProgress + " / " + currentFileSize + " Bytes");
		currentFilePanel.add(currentFileProgressLabel, c);


		// Create total-Panel
		JPanel totalPanel = new JPanel();
		totalPanel.setLayout(new GridBagLayout());
		c.anchor = GridBagConstraints.WEST;

		c.gridy = 0;
		totalLabel = new JLabel(Main.getMessage("autoupdate_total_file"));
		totalPanel.add(totalLabel, c);

		c.gridy = 1;
		totalProgressBar = new JProgressBar();
		totalProgressBar.setSize(200,10);
		totalPanel.add(totalProgressBar, c);

		c.gridy = 2;
		totalProgressLabel = new JLabel(totalProgress + " / " + totalSize + " Bytes");
		totalPanel.add(totalProgressLabel, c);


		// Create OK/Cancel Panel
		JPanel okcancelpanel = new JPanel();
		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source == okButton
						|| source == cancelButton) {
					setVisible(false);
				}
			}
		};

		// Create OK/Cancel Panel
		c.insets.top = 5;
		c.insets.bottom = 5;

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		okButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$
		okButton.addActionListener(actionListener);
		okButton.setEnabled(false);
//		okButton.addKeyListener(keyListener);
		okcancelpanel.add(okButton, c);
		cancelButton.addActionListener(actionListener);
//		cancelButton.addKeyListener(keyListener);
		cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
		okcancelpanel.add(cancelButton);

        //set default confirm button (Enter)
        getRootPane().setDefaultButton(okButton);

        getContentPane().setLayout(new BorderLayout());

		getContentPane().add(currentFilePanel, BorderLayout.NORTH);
		getContentPane().add(totalPanel, BorderLayout.CENTER);
		getContentPane().add(okcancelpanel, BorderLayout.SOUTH);
	}

	public void setCurrentFile(String fileName) {
		currentFileLabel.setText(Main.getMessage("autoupdate_current_file")+" "+fileName);
	}

	public void setCurrentFileSize(int size) {
		this.currentFileSize = size;
		currentFileProgressBar.setMaximum(size);
	}

	public long getCurrentFileSize() {
		return currentFileSize;
	}

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
		totalProgressBar.setMaximum(totalSize);
	}

	public long getTotalSize() {
		return totalSize;
	}

	public void setCurrentFileProgress(int progress) {
		currentFileProgress = progress;
		updateProgress();
	}

	public void setTotalProgress(int progress) {
		totalProgress = progress;
		updateProgress();
	}

	public int getTotalProgress() {
		return totalProgress;
	}

	private void updateProgress() {
		currentFileProgressBar.setValue(currentFileProgress);
		currentFileProgressLabel.setText(currentFileProgress + " / " + currentFileSize + " Bytes");

		totalProgressBar.setValue(totalProgress);
		totalProgressLabel.setText(totalProgress + " / " + totalSize + " Bytes");
	}
}
