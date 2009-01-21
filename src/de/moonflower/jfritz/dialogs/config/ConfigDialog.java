/*
 *
 * Password dialog box
 */

package de.moonflower.jfritz.dialogs.config;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.JFritzWindow;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.dialogs.sip.SipProvider;
import de.moonflower.jfritz.exceptions.InvalidFirmwareException;
import de.moonflower.jfritz.exceptions.WrongPasswordException;
import de.moonflower.jfritz.utils.BrowserLaunch;
import de.moonflower.jfritz.utils.Debug;

/**
 * JDialog for JFritz configuration.
 * Completely refactored in January 2009 by Robert
 *
 * @author Arno Willig, Robert Palmer
 *
 */
public class ConfigDialog extends JDialog {

	private static final long serialVersionUID = 1;

	private JButton okButton, cancelButton;

	private JFritzWindow parent;

	private ConfigPanelPhone phonePanel;
	private ConfigPanelFritzBox fritzBoxPanel;
	private ConfigPanelMessage messagePanel;
	private ConfigPanelCallerList callerListPanel;
	private ConfigPanelCallerListAppearance callerListAppearancePanel;
	private ConfigPanelCallMonitor callMonitorPanel;
	private ConfigPanelLang languagePanel;
	private ConfigPanelOther otherPanel;
	private ConfigPanelNetwork networkPanel;
	private ConfigPanelSip sipPanel;

	private boolean pressed_OK = false;

    static final String FILESEP = System.getProperty("file.separator");			//$NON-NLS-1$
	final String langID = FILESEP + "lang";										//$NON-NLS-1$

	private JTree tree;
	private ConfigTreeNode rootNode;
	private JPanel configPanel;

	private JLabel helpLinkLabel;
	private String helpUrl = "";

	public ConfigDialog(JFritzWindow parent) {
		super(parent, true);
		this.parent = parent;
		setTitle(Main.getMessage("config")); //$NON-NLS-1$

		phonePanel = new ConfigPanelPhone();
		fritzBoxPanel = new ConfigPanelFritzBox();
		messagePanel = new ConfigPanelMessage();
		callerListPanel = new ConfigPanelCallerList();
		callerListAppearancePanel = new ConfigPanelCallerListAppearance();
		callMonitorPanel = new ConfigPanelCallMonitor(this, true, fritzBoxPanel);
		sipPanel = new ConfigPanelSip(fritzBoxPanel);
		languagePanel = new ConfigPanelLang();
		otherPanel = new ConfigPanelOther(fritzBoxPanel);
		networkPanel = new ConfigPanelNetwork(this);

		rootNode = new ConfigTreeNode(Main.getMessage("config"));
		tree = new JTree(rootNode);
		tree.setRootVisible(true);
		TreeSelectionModel tsm = new DefaultTreeSelectionModel();
		tsm.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setSelectionModel(tsm);
		configPanel = fritzBoxPanel;
		helpUrl = fritzBoxPanel.getHelpUrl();

	    DefaultTreeCellRenderer noneRenderer = new DefaultTreeCellRenderer();
	    noneRenderer.setOpenIcon(null);
	    noneRenderer.setClosedIcon(null);
	    noneRenderer.setLeafIcon(null);
	    tree.setCellRenderer(noneRenderer);


		drawDialog();
		setValues();
		if (parent != null) {
			setLocationRelativeTo(parent);
		}

		TreeSelectionListener selectionListener = new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				ConfigTreeNode node = (ConfigTreeNode)e.getNewLeadSelectionPath().getLastPathComponent();
				getContentPane().remove(configPanel);
				configPanel = node.getPanel().getPanel();
				helpUrl = node.getPanel().getHelpUrl();
				if (helpUrl.equals(""))
				{
					helpLinkLabel.setVisible(false);
				}
				else
				{
					helpLinkLabel.setVisible(true);
				}
				getContentPane().add(configPanel, BorderLayout.CENTER);
				configPanel.updateUI();
			}
		};
		tree.addTreeSelectionListener(selectionListener);

		tree.setSelectionRow(1);
	}

	public void addConfigPanel(ConfigPanel panel)
	{
		ConfigTreeNode currentNode = rootNode;
		String path = panel.getPath();
		String rest = "none";

		while (!rest.equals(""))
		{
			if (path.contains("::"))
			{
				rest = path.substring(path.indexOf("::")+2);
				path = path.substring(0, path.indexOf("::"));
			}
			else
			{
				rest = "";
			}

			Enumeration<ConfigTreeNode> en = currentNode.children();
			ConfigTreeNode child = rootNode;
			boolean found = false;
			while (en.hasMoreElements() && !found)
			{
				child = en.nextElement();
				if (child.toString().equals(path))
				{
					found = true;
				}
			}
			if (!found && !rest.equals("")) // we found no entry, but are still not at the end of our path, just add a subpath-entry.
			{
				ConfigTreeNode newNode = new ConfigTreeNode(path);
				currentNode.add(newNode);
				currentNode = newNode;
			}
			if (!found && rest.equals("")) // we found no entry, but we are at end of our path. Add panel.
			{
				ConfigTreeNode newNode = new ConfigTreeNode(panel, path);
				currentNode.add(newNode);
				currentNode = newNode;
			}
			if (found && !rest.equals("")) // we found an entry, but we are still not at the end of our path.
			{
				currentNode = child;
			}
			if (found && rest.equals("")) // we found an entry with the same name!!!
			{
				if (child.getPanel() != null) // there already exists a panel. Add a new one with en _ERROR suffix.
				{
					ConfigTreeNode newNode = new ConfigTreeNode(panel, path + "_ERROR");
					currentNode.add(newNode);
					currentNode = newNode;
				}
				else // an empty panel, just add our panel instead
				{
					child.setPanel(panel);
				}
			}

			path = rest;
		}
	}

    // If expand is true, expands all nodes in the tree.
    // Otherwise, collapses all nodes in the tree.
    public void expandAll(JTree tree, boolean expand) {
        TreeNode root = (TreeNode)tree.getModel().getRoot();

        // Traverse tree from root
        expandAll(tree, new TreePath(root), expand);
    }
    private void expandAll(JTree tree, TreePath parent, boolean expand) {
        // Traverse children
        TreeNode node = (TreeNode)parent.getLastPathComponent();
        if (node.getChildCount() >= 0) {
            for (Enumeration e=node.children(); e.hasMoreElements(); ) {
                TreeNode n = (TreeNode)e.nextElement();
                TreePath path = parent.pathByAddingChild(n);
                expandAll(tree, path, expand);
            }
        }

        // Expansion or collapse must be done bottom-up
        if (expand) {
            tree.expandPath(parent);
        } else {
            tree.collapsePath(parent);
        }
    }

	public boolean okPressed() {
		return pressed_OK;
	}

	/**
	 * Sets properties to dialog components
	 */
	public void setValues() {
		fritzBoxPanel.loadSettings();
		phonePanel.loadSettings();
		messagePanel.loadSettings();
		callMonitorPanel.loadSettings();
		languagePanel.loadSettings();
		otherPanel.loadSettings();
		networkPanel.loadSettings();
		callerListPanel.loadSettings();
		callerListAppearancePanel.loadSettings();
		sipPanel.loadSettings();
	}

	/**
	 * Stores values in dialog components to programm properties
	 */
	public void storeValues() {
		try {
			fritzBoxPanel.saveSettings();
		} catch (WrongPasswordException e) {
			parent.setDisconnectedStatus();
		} catch (InvalidFirmwareException e) {
			parent.setDisconnectedStatus();
		} catch (IOException e) {
			parent.setDisconnectedStatus();
		}
		phonePanel.saveSettings();
		messagePanel.saveSettings();
		callMonitorPanel.saveSettings();
		languagePanel.saveSettings();
		otherPanel.saveSettings();
		networkPanel.saveSettings();
		callerListPanel.saveSettings();
		callerListAppearancePanel.saveSettings();
		sipPanel.saveSettings();

		JFritz.getFritzBox().setAddress(fritzBoxPanel.getAddress());
		JFritz.getFritzBox().setPassword(fritzBoxPanel.getPassword());
		JFritz.getFritzBox().setPort(fritzBoxPanel.getPort());
		try {
			JFritz.getFritzBox().detectFirmware();
		} catch (WrongPasswordException e) {
			parent.setDisconnectedStatus();
		} catch (InvalidFirmwareException e) {
			parent.setDisconnectedStatus();
		} catch (IOException e) {
			parent.setDisconnectedStatus();
		}

		Debug.msg("Saved config"); //$NON-NLS-1$
		JFritz.getSIPProviderTableModel()
				.saveToXMLFile(Main.SAVE_DIR + JFritz.SIPPROVIDER_FILE);
		JFritz.getCallerList().saveToXMLFile(Main.SAVE_DIR+JFritz.CALLS_FILE, true);
		JFritz.getPhonebook().saveToXMLFile(Main.SAVE_DIR+JFritz.PHONEBOOK_FILE);
        Main.saveUpdateProperties();
	}

	protected void drawDialog() {
		helpLinkLabel = new JLabel(Main.getMessage("help_menu"));

		okButton = new JButton(Main.getMessage("okay")); //$NON-NLS-1$
		okButton.setIcon(new ImageIcon(Toolkit.getDefaultToolkit().getImage(
				getClass().getResource(
						"/de/moonflower/jfritz/resources/images/okay.png")))); //$NON-NLS-1$
		cancelButton = new JButton(Main.getMessage("cancel")); //$NON-NLS-1$

		KeyListener keyListener = (new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE
						|| (e.getSource() == cancelButton && e.getKeyCode() == KeyEvent.VK_ENTER)) {
					pressed_OK = false;
					closeWindow();
				}
				if (e.getSource() == okButton
						&& e.getKeyCode() == KeyEvent.VK_ENTER) {
					pressed_OK = true;
					closeWindow();
				}
			}
		});

		ActionListener actionListener = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Object source = e.getSource();
				pressed_OK = (source == okButton);
				if (source == okButton
						|| source == cancelButton) {
					closeWindow();
				} else if (e.getActionCommand().equals("fetchSIP")) { //$NON-NLS-1$
					try {
						JFritz.getFritzBox().setAddress(fritzBoxPanel.getAddress());
						JFritz.getFritzBox().setPassword(fritzBoxPanel.getPassword());
						JFritz.getFritzBox().setPort(fritzBoxPanel.getPort());
						JFritz.getFritzBox().detectFirmware();
						Vector<SipProvider> data = JFritz.getFritzBox().retrieveSipProvider();
						JFritz.getSIPProviderTableModel().updateProviderList(
								data);
						JFritz.getSIPProviderTableModel()
								.fireTableDataChanged();
						JFritz.getCallerList().fireTableDataChanged();

					} catch (WrongPasswordException e1) {
						JFritz.errorMsg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("box.wrong_password")); //$NON-NLS-1$
					} catch (IOException e1) {
						JFritz.errorMsg(Main.getMessage("box.not_found")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("box.not_found")); //$NON-NLS-1$
					} catch (InvalidFirmwareException e1) {
						JFritz.errorMsg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
						Debug.errDlg(Main.getMessage("unknown_firmware")); //$NON-NLS-1$
					}
				}
			}
		};

		// Create OK/Cancel Panel
		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.anchor = GridBagConstraints.CENTER;
		JPanel okcancelpanel = new JPanel();
		okButton.addActionListener(actionListener);
		okButton.addKeyListener(keyListener);
		okcancelpanel.add(okButton, c);
		cancelButton.addActionListener(actionListener);
		cancelButton.addKeyListener(keyListener);
		cancelButton.setMnemonic(KeyEvent.VK_ESCAPE);
		okcancelpanel.add(cancelButton);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BorderLayout());
		buttonPanel.add(okcancelpanel, BorderLayout.CENTER);
		buttonPanel.add(helpLinkLabel, BorderLayout.WEST);

		helpLinkLabel.setForeground(Color.BLUE);
		helpLinkLabel.setHorizontalAlignment(SwingConstants.CENTER);
		helpLinkLabel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		helpLinkLabel.setCursor( new Cursor(Cursor.HAND_CURSOR));
		helpLinkLabel.addMouseListener(new MouseListener(){
			public void mouseClicked(MouseEvent e) {
			}

			public void mouseReleased(MouseEvent e) {
			}

			public void mouseEntered(MouseEvent e) {
			}

			public void mouseExited(MouseEvent e) {
			}

			public void mousePressed(MouseEvent e) {
				BrowserLaunch.openURL(helpUrl);
			}

		});

        //set default confirm button (Enter)
        getRootPane().setDefaultButton(okButton);

        //set default close button (ESC)
        KeyStroke escapeKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false);
        KeyStroke helpKeyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0, true);
        Action escapeAction = new AbstractAction()
        {
			private static final long serialVersionUID = 4043321314432066705L;

			public void actionPerformed(ActionEvent e)
            {
                 cancelButton.doClick();
            }
        };
        Action helpAction = new AbstractAction()
        {
			private static final long serialVersionUID = -5044333644283489649L;

			public void actionPerformed(ActionEvent e) {
				BrowserLaunch.openURL(helpUrl);
			}

        };
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escapeKeyStroke, "ESCAPE"); //$NON-NLS-1$
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(helpKeyStroke, "HELP"); //$NON-NLS-1$
        getRootPane().getActionMap().put("ESCAPE", escapeAction); //$NON-NLS-1$
        getRootPane().getActionMap().put("HELP", helpAction); //$NON-NLS-1$

        BorderLayout layout = new BorderLayout();
//        layout.setHgap(15);
//        layout.setVgap(100);
		getContentPane().setLayout(layout);
		getContentPane().add(configPanel, BorderLayout.CENTER);
//		getContentPane().add(tpane, BorderLayout.EAST);
		getContentPane().add(buttonPanel, BorderLayout.SOUTH);

		JScrollPane treePane = new JScrollPane(tree);
		treePane.setPreferredSize(new Dimension(150,100));
		treePane.setMaximumSize(new Dimension(200, 100));
		treePane.setBorder(BorderFactory.createEmptyBorder(20, 10, 0, 0));
		getContentPane().add(treePane, BorderLayout.WEST);
		c.fill = GridBagConstraints.HORIZONTAL;

		addKeyListener(keyListener);

		// new config dialog
		this.addConfigPanel(fritzBoxPanel);
		this.addConfigPanel(phonePanel);
		this.addConfigPanel(sipPanel);
		this.addConfigPanel(callerListPanel);
		this.addConfigPanel(callMonitorPanel);
		this.addConfigPanel(messagePanel);
		this.addConfigPanel(languagePanel);
		this.addConfigPanel(networkPanel);
		this.addConfigPanel(otherPanel);
		this.addConfigPanel(callerListAppearancePanel);

		int width = Integer.parseInt(Main.getStateProperty("configDialog.width", "700"));
		int height = Integer.parseInt(Main.getStateProperty("configDialog.height", "470"));
		setSize(width, height);
		setResizable(true);
		if (parent != null)
		{
			setLocationRelativeTo(parent);
		}
		// pack();
	}

	public boolean showDialog() {
		expandAll(tree, false);
		expandAll(tree, true);
		setVisible(true);
		return okPressed();
	}

	/**
	 * function used to adjust the size of the options dialog
	 */
	public void stateChanged(ChangeEvent e){
		int width = Integer.parseInt(Main.getStateProperty("configDialog.width", "700"));
		int height = Integer.parseInt(Main.getStateProperty("configDialog.height", "470"));
		setSize(width, height);
		if (parent != null) {
			setLocationRelativeTo(parent);
		}
	}

	private void closeWindow()
	{
		if (pressed_OK)
		{
			// save window position and size
			Main.setStateProperty("configDialog.width", Integer.toString(this.getWidth()));
			Main.setStateProperty("configDialog.height", Integer.toString(this.getHeight()));
		}
		ConfigDialog.this.setVisible(false);
	}

}