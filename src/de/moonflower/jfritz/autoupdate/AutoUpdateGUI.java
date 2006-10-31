package de.moonflower.jfritz.autoupdate;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

public class AutoUpdateGUI extends JDialog implements DownloadFilesListener {

	private static final long serialVersionUID = 5720893048026504125L;

	private JButton okButton, cancelButton;

	private JLabel currentFileNumLabel, currentFileLabel,
			currentFileProgressLabel, totalLabel, totalProgressLabel;

	private JProgressBar currentFileProgressBar, totalProgressBar;

	private UpdateFile currentUpdateFile;

	private int totalUpdateSize;

	private DownloadFilesThread downloadFilesThread;

	public AutoUpdateGUI(DownloadFilesThread downloadFilesThread) {
		setLocation(0, 0);
		setSize(500, 200);

		setTitle("autoupdate_title");
		this.downloadFilesThread = downloadFilesThread;
		createGUI();
	}

	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			downloadFilesThread.interrupt();
			setVisible(false);
		} else {
			super.processWindowEvent(e);
		}
	}

	private void createGUI() {

		this.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

		GridBagLayout thisLayout = new GridBagLayout();
		thisLayout.rowWeights = new double[] { 0.1, 0.1, 0.1, 0.1, 0.1 };
		thisLayout.rowHeights = new int[] { 7, 7, 7, 7, 7 };
		thisLayout.columnWeights = new double[] { 0.2, 0.5, 0.2 };
		thisLayout.columnWidths = new int[] { 7, 7, 7 };
		getContentPane().setLayout(thisLayout);

		// Add Current-File Items
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.SOUTH;

		currentFileNumLabel = new JLabel(UpdateLocale
				.getMessage("autoupdate_current_file"));
		getContentPane().add(currentFileNumLabel, c);

		c.gridx = 1;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.CENTER;
		currentFileProgressBar = new JProgressBar();
		currentFileProgressBar.setSize(200, 10);
		getContentPane().add(currentFileProgressBar, c);

		c.gridx = 2;
		c.gridheight = 2;
		currentFileProgressLabel = new JLabel();
		getContentPane().add(currentFileProgressLabel, c);

		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		c.gridheight = 1;
		c.anchor = GridBagConstraints.NORTH;
		currentFileLabel = new JLabel(UpdateLocale
				.getMessage("autoupdate_current_file"));
		getContentPane().add(currentFileLabel, c);

		// Add Total-File Items
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 1;
		c.gridheight = 2;
		c.anchor = GridBagConstraints.CENTER;
		totalLabel = new JLabel(UpdateLocale
				.getMessage("autoupdate_total_file"));
		getContentPane().add(totalLabel, c);

		c.gridx = 1;
		totalProgressBar = new JProgressBar();
		totalProgressBar.setSize(200, 10);
		getContentPane().add(totalProgressBar, c);

		c.gridx = 2;
		totalProgressLabel = new JLabel();
		getContentPane().add(totalProgressLabel, c);

		// Create OK/Cancel Panel
		JPanel okcancelpanel = new JPanel();

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				if (source == cancelButton) {
					downloadFilesThread.interrupt();
					setVisible(false);
				}
				if (source == okButton || source == cancelButton) {
					setVisible(false);
				}
			}
		};

		c.insets.top = 5;
		c.insets.bottom = 5;

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		okButton = new JButton(UpdateLocale.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(UpdateLocale.getMessage("cancel")); //$NON-NLS-1$
		okButton.addActionListener(actionListener);
		okButton.setEnabled(false);
		// okButton.addKeyListener(keyListener);
		okcancelpanel.add(okButton, c);
		cancelButton.addActionListener(actionListener);
		// cancelButton.addKeyListener(keyListener);
		cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
		okcancelpanel.add(cancelButton);

		// set default confirm button (Enter)
		getRootPane().setDefaultButton(okButton);

		getContentPane().add(
				okcancelpanel,
				new GridBagConstraints(0, 4, 3, 1, 0.0, 0.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
	}

	protected static int showConfirmDialog() {
		Object[] options = { UpdateLocale.getMessage("yes"),
				UpdateLocale.getMessage("no") };

		int ok = JOptionPane.showOptionDialog(null, UpdateLocale
				.getMessage("new_version_text"), UpdateLocale
				.getMessage("autoupdate_title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, // don't use a custom
				// Icon
				options, // the titles of buttons
				options[0]); // default button title

		return ok;
	}

	protected static void showNoNewVersionFoundDialog() {
		JOptionPane.showMessageDialog(null, UpdateLocale
				.getMessage("no_new_version_found"), UpdateLocale
				.getMessage("autoupdate_title"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	public void startNewDownload(int currentFileNum, int totalFileNum,
			UpdateFile newFile, int totalSize) {
		currentUpdateFile = newFile;
		totalUpdateSize = totalSize;

		currentFileNumLabel.setText(UpdateLocale
				.getMessage("autoupdate_current_file")
				+ " (" + currentFileNum + "/" + totalFileNum + ")");

		currentFileLabel.setText(currentUpdateFile.getName());
		currentFileProgressBar.setMaximum(newFile.getSize());
		totalProgressBar.setMaximum(totalSize);
		progress(0);
	}

	public void progress(int increment) {
		int currentFileProgress = currentFileProgressBar.getValue() + increment;
		setCurrentFileProgress(currentFileProgress);

		int totalProgress = totalProgressBar.getValue() + increment;
		setTotalProgress(totalProgress);
	}

	public void finished() {
		if (currentUpdateFile == null) {
			currentUpdateFile = new UpdateFile("", "", 100);
		}
		setCurrentFileProgress(currentFileProgressBar.getMaximum());
		setTotalProgress(totalProgressBar.getMaximum());

		okButton.setEnabled(true);
		cancelButton.setEnabled(false);
	}

	private void setCurrentFileProgress(int position) {
		currentFileProgressBar.setValue(position);
		currentFileProgressLabel.setText(position + " / "
				+ currentUpdateFile.getSize() + " Bytes");
	}

	private void setTotalProgress(int position) {
		totalProgressBar.setValue(position);
		totalProgressLabel.setText(position + " / " + totalUpdateSize
				+ " Bytes");
	}

	public static void showUpdateSuccessfulMessage() {
		JOptionPane.showMessageDialog(null, UpdateLocale
				.getMessage("update_successfull_msg"), UpdateLocale
				.getMessage("autoupdate_title"),
				JOptionPane.INFORMATION_MESSAGE);
	}
}
