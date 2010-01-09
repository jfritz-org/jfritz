package de.moonflower.jfritz.autoupdate;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 * Graphische Oberfläche zur Anzeige des Fortschritts des AutoUploads.
 * @author Robert Palmer
 *
 */
public class AutoUpdateGUI extends JDialog implements DownloadFilesListener {

	private static final long serialVersionUID = 5720893048026504125L;

	private JButton okButton, cancelButton;

	private JLabel currentFileNumLabel, currentFileLabel,
			currentFileProgressLabel, totalLabel, totalProgressLabel;

	private JProgressBar currentFileProgressBar, totalProgressBar;

	private UpdateFile currentUpdateFile;

	private int totalUpdateSize;

	private Thread downloadFilesThread;

	private static Vector<String> changelog;

	public AutoUpdateGUI(Thread thread) {
		setLocation(0, 0);
		setSize(500, 200);

		setTitle(UpdateLocale.getMessage("autoupdate_title"));
		this.downloadFilesThread = thread;
		createGUI();
	}

	/**
	 * Schließt Fenster und stoppt das Herunterladen der Dateien.
	 */
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			downloadFilesThread.interrupt();
			setVisible(false);
		} else {
			super.processWindowEvent(e);
		}
	}

	/**
	 * Erstellt graphische Oberfläche.
	 *
	 */
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

	/**
	 * Zeigt Bestätigungsdialog mit "Ja" und "Nein" an.
	 * @return
	 */
	protected static int showConfirmDialog() {
		Object[] options = { UpdateLocale.getMessage("yes"),
				UpdateLocale.getMessage("no"),
				 UpdateLocale.getMessage("changelog") };

		int ok = 99;
		while ((ok != JOptionPane.YES_OPTION) && (ok != JOptionPane.NO_OPTION))
		{
			ok = JOptionPane.showOptionDialog(null, UpdateLocale
				.getMessage("new_version_text"), UpdateLocale
				.getMessage("autoupdate_title"), JOptionPane.YES_NO_OPTION,
				JOptionPane.INFORMATION_MESSAGE, null, // don't use a custom Icon
				options, // the titles of buttons
				options[0]); // default button title

			if (ok == 2) // changelog
			{
				showChangelog();
			}
		}
		return ok;
	}

	private static void showChangelog()
	{
		final JDialog dialog = new JDialog();

		JPanel pane = new JPanel();
		pane.setLayout(new BorderLayout());
		JPanel buttonPane = new JPanel();

		JButton ok = new JButton(UpdateLocale.getMessage("okay"));
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialog.setVisible(false);
				dialog.dispose();
			}

		});
		buttonPane.add(ok);
		pane.add(buttonPane, BorderLayout.SOUTH);

		JTextArea textarea = new JTextArea();
		String text = "";
		for (int i=0; i<changelog.size(); i++)
		{
			text = text + changelog.get(i) + "\n";
		}
		textarea.setText(text);
		pane.add(new JScrollPane(textarea), BorderLayout.CENTER);

		dialog.getContentPane().add(pane);
		dialog.setTitle(UpdateLocale.getMessage("changelog_title"));
		dialog.pack();
		Dimension size = new Dimension(800, 400);
		dialog.setSize(size);

		Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();

		dialog.setLocation((screenDim.width / 2) - (dialog.getWidth() / 2),
				(screenDim.height / 2) - (dialog.getHeight() / 2));

		dialog.setModal(true);
		dialog.setVisible(true);
	}

	public static void setChangelog(final Vector<String> log)
	{
		changelog = log;
	}

	/**
	 * Zeige Meldung, dass keine neue Version gefunden wurde.
	 */
	protected static void showNoNewVersionFoundDialog() {
		JOptionPane.showMessageDialog(null, UpdateLocale
				.getMessage("no_new_version_found"), UpdateLocale
				.getMessage("autoupdate_title"),
				JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Beginne neuen Download.
	 */
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

	/**
	 * Update des Fortschrittsbalkens.
	 */
	public void progress(int increment) {
		int currentFileProgress = currentFileProgressBar.getValue() + increment;
		setCurrentFileProgress(currentFileProgress);

		int totalProgress = totalProgressBar.getValue() + increment;
		setTotalProgress(totalProgress);
	}

	/**
	 * Download fertig.
	 */
	public void finished() {
		if (currentUpdateFile == null) {
			currentUpdateFile = new UpdateFile("", "", 100);
		}
		setCurrentFileProgress(currentFileProgressBar.getMaximum());
		setTotalProgress(totalProgressBar.getMaximum());

		okButton.setEnabled(true);
		cancelButton.setEnabled(false);
	}

	/**
	 * Setze aktuellen Fortschritt der Datei.
	 * @param position
	 */
	private void setCurrentFileProgress(int position) {
		currentFileProgressBar.setValue(position);
		currentFileProgressLabel.setText(position + " / "
				+ currentUpdateFile.getSize() + " Bytes");
	}

	/**
	 * Setze aktuellen gesamten Fortschritt.
	 * @param position
	 */
	private void setTotalProgress(int position) {
		totalProgressBar.setValue(position);
		totalProgressLabel.setText(position + " / " + totalUpdateSize
				+ " Bytes");
	}

	/**
	 * Zeige Nachricht, dass das Update erfolgreich war.
	 */
	public static void showUpdateSuccessfulMessage() {
		JOptionPane.showMessageDialog(null, UpdateLocale
				.getMessage("update_successfull_msg"), UpdateLocale
				.getMessage("autoupdate_title"),
				JOptionPane.INFORMATION_MESSAGE);
	}
}
