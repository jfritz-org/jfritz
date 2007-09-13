package de.moonflower.jfritz.JFritzEvent.actions;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.moonflower.jfritz.utils.JFritzUtils;


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
public class PopupAction implements JFritzAction {

	private String description;

	private JLabel titleLabel;
	private JCheckBox alwaysTopCheckBox;
	private JTextArea messageTextArea;
	private JLabel messageLabel;
	private JTextField titleTextField;

	private String title;
	private String message;
	private boolean onTop;

	public PopupAction() {
		this.description = "";
		this.title = "";
		this.message = "";
		this.onTop = false;
	}

	public JPanel getConfigPanel() {
		JPanel configPanel = new JPanel();
		GridBagLayout configPanelLayout = new GridBagLayout();
		configPanelLayout.columnWeights = new double[] {0.1, 0.1, 0.0, 0.1};
		configPanelLayout.columnWidths = new int[] {20, 7, 136, 20};
		configPanelLayout.rowWeights = new double[] {0.1, 0.1, 0.0, 0.1, 0.0, 0.1, 0.1};
		configPanelLayout.rowHeights = new int[] {20, 7, 8, 20, 9, 20, 20};
		configPanel.setLayout(configPanelLayout);
		configPanel.setPreferredSize(new java.awt.Dimension(379, 269));

		titleLabel = new JLabel();
		FlowLayout titleLabelLayout = new FlowLayout();
		titleLabel.setLayout(titleLabelLayout);
		configPanel.add(titleLabel, new GridBagConstraints(1, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		titleLabel.setText("title: ");


		titleTextField = new JTextField();
		configPanel.add(titleTextField, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		titleTextField.setText("textfield");
		titleTextField.setPreferredSize(new java.awt.Dimension(160, 20));


		messageLabel = new JLabel();
		configPanel.add(messageLabel, new GridBagConstraints(1, 3, 1, 1, 0.0, 0.0, GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		messageLabel.setText("message: ");


		messageTextArea = new JTextArea(10, 30);
		messageTextArea.setText("Textarea");
		configPanel.add(new JScrollPane(messageTextArea), new GridBagConstraints(2, 3, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));


		alwaysTopCheckBox = new JCheckBox();
		configPanel.add(alwaysTopCheckBox, new GridBagConstraints(2, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		alwaysTopCheckBox.setText("always on top");

		return configPanel;
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return "Popup-Message";
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String toString() {
		return getName();
	}

	public PopupAction clone() {
	        try {
	            return (PopupAction) super.clone();
	        } catch (CloneNotSupportedException cnse) {
	            cnse.printStackTrace();
	            return null;
	        }
	}

	public void run() {
		// TODO do action
	}

	public void parameterClicked(Object o) {
		messageTextArea.setText( messageTextArea.getText()+o.toString());
	}

	public void loadSettings(Element settings) {
		// TODO load settings
	    XMLOutputter stdOutputter = new XMLOutputter(Format.getPrettyFormat());
		try {
			stdOutputter.output(settings, System.out);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Element actionRoot = settings.getChild("popup");
		Element title = actionRoot.getChild("title");
		this.title = title.getText();
		Element message = actionRoot.getChild("message");
		this.message = message.getText();
		Element alwaysOnTop = actionRoot.getChild("alwaysOnTop");
		this.onTop = JFritzUtils.parseBoolean(alwaysOnTop.getText());
	}

	public Element saveSettings() {
		// set current dialog settings if dialog has been shown
		if ( titleTextField != null )
			title = titleTextField.getText();
		if ( messageTextArea != null )
			message = messageTextArea.getText();
		if ( alwaysTopCheckBox != null )
			onTop = alwaysTopCheckBox.isSelected();

		// build XML subdocument
		Element actionRoot = new Element("popup");
		Element title = new Element("title");
		Element message = new Element("message");
		Element alwaysOnTop = new Element("alwaysOnTop");

		title.setText(this.title);
		message.setText(this.message);
		if ( onTop )
			alwaysOnTop.setText("true");
		else
			alwaysOnTop.setText("false");

		actionRoot.addContent(title);
		actionRoot.addContent(message);
		actionRoot.addContent(alwaysOnTop);

		return actionRoot;
	}
}
