package de.moonflower.jfritz;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Calendar;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.moonflower.jfritz.constants.ProgramConstants;
import de.moonflower.jfritz.messages.MessageProvider;

public class AboutJFritz extends JDialog implements ActionListener {
	private static final long serialVersionUID = 7540730441733152650L;
	private Frame parent;

	public AboutJFritz(Frame parent) {
		super(parent, true);
		this.parent = parent;

		initGui();
	}

	private void initGui() {
		setTitle("About JFritz"); //$NON-NLS-1$

		JPanel mainPanel = initMainPanel();
		JPanel buttonPanel = initButtons();

		this.getContentPane().setLayout(new BorderLayout());
		this.getContentPane().add(mainPanel, BorderLayout.CENTER);
		this.getContentPane().add(buttonPanel, BorderLayout.SOUTH);
		this.pack();

		if (parent != null) {
			setLocationRelativeTo(parent);
		}
		setResizable(false);
	}

	private JPanel initMainPanel() {
		Calendar cal = Calendar.getInstance();
		cal.getTime();

		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new GridBagLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		GridBagConstraints gbc = new GridBagConstraints();

		JLabel version = new JLabel("Version: " + ProgramConstants.PROGRAM_VERSION);
		JLabel revision = new JLabel("Revision: " + ProgramConstants.REVISION);
		JLabel buildDate = new JLabel("Build: " + ProgramConstants.BUILD_DATE);
		JLabel newLine = new JLabel(" ");
		JLabel admin = new JLabel("Project-admin: Robert Palmer <robert@jfritz.org>");
		JLabel initiator = new JLabel("Project-initiator: Arno Willig <akw@thinkwiki.org>");
		JLabel newLine2 = new JLabel(" ");
		JLabel copyright = new JLabel("(c) 2005 - " + cal.get(Calendar.YEAR) + " by all members of the JFritz-Team");
		JLabel newLine3 = new JLabel(" ");
		JLabel gnu1 = new JLabel("This tool is developed and released under");
		JLabel gnu2 = new JLabel("the terms of the GNU General Public License");

		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets( 2, 2, 2, 2 );
		gbc.gridx = 0;
		gbc.gridy = 0;
		mainPanel.add(version, gbc);

		gbc.anchor = GridBagConstraints.EAST;
		gbc.gridx = 1;
		gbc.gridy = 0;
		mainPanel.add(revision, gbc);

		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 2;
		mainPanel.add(buildDate, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		mainPanel.add(newLine, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
		mainPanel.add(admin, gbc);

		gbc.gridx = 0;
		gbc.gridy = 5;
		mainPanel.add(initiator, gbc);

		gbc.gridx = 0;
		gbc.gridy = 6;
		mainPanel.add(newLine2, gbc);

		gbc.gridx = 0;
		gbc.gridy = 7;
		mainPanel.add(copyright, gbc);

		gbc.gridx = 0;
		gbc.gridy = 8;
		mainPanel.add(newLine3, gbc);

		gbc.gridx = 0;
		gbc.gridy = 9;
		mainPanel.add(gnu1, gbc);

		gbc.gridx = 0;
		gbc.gridy = 10;
		mainPanel.add(gnu2, gbc);

		return mainPanel;
	}

	private JPanel initButtons() {
		JPanel buttonPanel = new JPanel();

		JButton buttonOk = new JButton(MessageProvider.getInstance().getMessage("okay"));
		buttonOk.addActionListener(this);
		buttonOk.setActionCommand("ok");
		buttonOk.setMnemonic(KeyEvent.VK_O);
		buttonOk.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					setVisible(false);
				}
			}
		});

		JButton buttonCreds = new JButton("Credits");
		buttonCreds.addActionListener(this);
		buttonCreds.setActionCommand("creds");
		buttonCreds.setMnemonic(KeyEvent.VK_C);

		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;

		buttonPanel.add(buttonOk, c);
		buttonPanel.add(buttonCreds, c);
		return buttonPanel;
	}

	@Override
	public void actionPerformed(ActionEvent action) {
		if ("ok".equals(action.getActionCommand())) {
			this.setVisible(false);
		} else if ("creds".equals(action.getActionCommand())) {
			this.showCredits();
		}
	}

	private void showCredits() {
		JOptionPane.showMessageDialog(this, "Active Developers:\n"
		+ "Robert Palmer <robert@jfritz.org>\n" 	//$NON-NLS-1$
		+ "\n"													//$NON-NLS-1$
		+ "Former Developers:\n" 								//$NON-NLS-1$
		+ "Arno Willig <akw@thinkwiki.org>\n"					//$NON-NLS-1$
		+ "Christian Klein <kleinch@users.sourceforge.net>\n" 	//$NON-NLS-1$
		+ "Benjamin Schmitt <little_ben@users.sourceforge.net>\n" //$NON-NLS-1$
		+ "Bastian Schaefer <baefer@users.sourceforge.net>\n" 	//$NON-NLS-1$
		+ "Marc Waldenberger <MarcWaldenberger@gmx.net>\n"		//$NON-NLS-1$
		+ "Simeon Faensen (Klingeling-Idee)\n"					//$NON-NLS-1$
		+ "Brian Jensen <capncrunch@users.sourceforge.net>\n" 	//$NON-NLS-1$
		+ "Rainer Ullrich <jfritz@rainerullrich.de>\n" 			//$NON-NLS-1$
		, "JFritz - Credits", JOptionPane.PLAIN_MESSAGE); 	//$NON-NLS-1$
	}
}
