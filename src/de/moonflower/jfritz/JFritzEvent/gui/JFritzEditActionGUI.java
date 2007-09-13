package de.moonflower.jfritz.JFritzEvent.gui;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;

import de.moonflower.jfritz.JFritzEvent.actions.JFritzAction;
import de.moonflower.jfritz.JFritzEvent.events.JFritzEvent;
import de.moonflower.jfritz.JFritzEvent.struct.JFritzEventParameter;

/**
* This code was edited or generated using CloudGarden's Jigloo
* SWT/Swing GUI Builder, which is free for non-commercial
* use. If Jigloo is being used commercially (ie, by a corporation,
* company or business for any purpose whatever) then you
* should purchase a license for each developer using Jigloo.
* Please visit www.cloudgarden.com for details.
* Use of Jigloo implies acceptance of these licensing terms.
* A COMMERCIAL LICENSE HAS NOT BEEN PURCHASED FOR
* THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED
* LEGALLY FOR ANY CORPORATE OR COMMERCIAL PURPOSE.
*/
public class JFritzEditActionGUI extends JDialog implements ActionListener {
	private static final long serialVersionUID = 7709799251088404796L;
	private JScrollPane scrollPane;
	private JButton cancelButton;
	private JButton okButton;
	private JPanel buttonPanel, editorPane;
	private DoubleClickList parameterList;
	private JPanel jPanel1;
	private JLabel parameterLabel;
	private JPanel parameterPanel;
	private JTextField descriptionTextField;
	private JLabel descriptionLabel;
	private JPanel descriptionPanel;

	private JFritzAction action;
	private JFritzEvent event;

	private boolean ok_pressed = false;

	public JFritzEditActionGUI(JDialog parent, JFritzEvent event, JFritzAction action) {
		super(parent, true);
		this.event = event;
		initGUI();
		if ( parent != null )
			setLocationRelativeTo(parent);
		setAction(action);
	}

	private void initGUI() {
		try {
			BorderLayout thisLayout = new BorderLayout();
			getContentPane().setLayout(thisLayout);

			scrollPane = new JScrollPane();
			getContentPane().add(scrollPane, BorderLayout.CENTER);
			scrollPane.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

			buttonPanel = new JPanel();
			getContentPane().add(buttonPanel, BorderLayout.SOUTH);
			{
				descriptionPanel = new JPanel();
				GridBagLayout descriptionPanelLayout = new GridBagLayout();
				descriptionPanelLayout.rowWeights = new double[] {0.1};
				descriptionPanelLayout.rowHeights = new int[] {7};
				descriptionPanelLayout.columnWeights = new double[] {0.1};
				descriptionPanelLayout.columnWidths = new int[] {7};
				descriptionPanel.setLayout(descriptionPanelLayout);
				getContentPane().add(descriptionPanel, BorderLayout.NORTH);
				descriptionPanel.setPreferredSize(new java.awt.Dimension(346, 32));
				{
					jPanel1 = new JPanel();
					BoxLayout jPanel1Layout = new BoxLayout(
						jPanel1,
						javax.swing.BoxLayout.X_AXIS);
					jPanel1.setLayout(jPanel1Layout);
					descriptionPanel.add(jPanel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
					jPanel1.setPreferredSize(new java.awt.Dimension(74, 31));
					{
						descriptionLabel = new JLabel();
						jPanel1.add(descriptionLabel);
						descriptionLabel.setText("description: ");
					}
					{
						descriptionTextField = new JTextField();
						jPanel1.add(descriptionTextField);
						descriptionTextField.setText("descriptionText");
					}
				}
			}
			{
				parameterPanel = new JPanel();
				GridBagLayout parameterPanelLayout = new GridBagLayout();
				parameterPanelLayout.rowWeights = new double[] {0.0, 0.0, 0.1};
				parameterPanelLayout.rowHeights = new int[] {19, 3, 7};
				parameterPanelLayout.columnWeights = new double[] {0.0, 0.0, 0.1};
				parameterPanelLayout.columnWidths = new int[] {10, 87, 7};
				parameterPanel.setLayout(parameterPanelLayout);
				getContentPane().add(parameterPanel, BorderLayout.EAST);
				parameterPanel.setPreferredSize(new java.awt.Dimension(107, 132));
				{
					parameterLabel = new JLabel();
					parameterPanel.add(parameterLabel, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					parameterLabel.setText("parameter: ");
				}
				{
					Vector<JFritzEventParameter> parameterVector = new Vector<JFritzEventParameter>();
					for ( byte i=0; i<event.getParameterCount(); i++)
						parameterVector.add(event.getParameter(i));
					parameterList = new DoubleClickList(parameterVector);
					parameterPanel.add(parameterList, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
					parameterList.setPreferredSize(new java.awt.Dimension(90,142));
				}
			}

			okButton = new JButton();
			buttonPanel.add(okButton);
			okButton.setText("ok");
			okButton.setActionCommand("ok");
			okButton.addActionListener(this);

			cancelButton = new JButton();
			buttonPanel.add(cancelButton);
			cancelButton.setText("cancel");
			cancelButton.setActionCommand("cancel");
			cancelButton.addActionListener(this);
			pack();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void setAction(JFritzAction action) {
		this.action = action;
		setTitle(action.getName() + " configuration");
		descriptionTextField.setText(action.getDescription());
		parameterList.addDoubleClickListener(action);
		JPanel panel = action.getConfigPanel();
		if ( editorPane != null )
			scrollPane.remove(editorPane);
		if ( panel != null ) {
			editorPane = panel;
			scrollPane.setViewportView(editorPane);
			pack();
		}
	}

	public void actionPerformed(ActionEvent e) {
		if ( e.getActionCommand().equals("ok")) {
			action.setDescription(descriptionTextField.getText());
			ok_pressed  = true;
			setVisible(false);
		} else {
			this.setVisible(false);
		}
	}

	public boolean showDialog() {
		setVisible(true);
		return ok_pressed;
	}

}
