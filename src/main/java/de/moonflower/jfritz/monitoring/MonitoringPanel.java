package de.moonflower.jfritz.monitoring;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.TableCellRenderer;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.cellrenderer.CallByCallCellRenderer;
import de.moonflower.jfritz.cellrenderer.CallTypeCellRenderer;
import de.moonflower.jfritz.cellrenderer.DateCellRenderer;
import de.moonflower.jfritz.cellrenderer.NumberCellRenderer;
import de.moonflower.jfritz.cellrenderer.PersonCellRenderer;
import de.moonflower.jfritz.cellrenderer.PortCellRenderer;
import de.moonflower.jfritz.cellrenderer.RouteCellRenderer;
import de.moonflower.jfritz.messages.MessageProvider;
import de.moonflower.jfritz.properties.PropertyProvider;
import de.moonflower.jfritz.utils.StatusBarController;
import de.moonflower.jfritz.utils.network.UPNPAddonInfosListener;
import de.moonflower.jfritz.utils.network.UPNPCommonLinkPropertiesListener;
import de.moonflower.jfritz.utils.network.UPNPExternalIpListener;
import de.moonflower.jfritz.utils.network.UPNPStatusInfoListener;

/**
 * Class for displaying monitoring information like current internet
 * or phone usage
 *
 * Class uses jfreechart to display internet usage as filled line chart
 *
 * @author brian jensen, Robert Palmer
 *
 */
public class MonitoringPanel extends JPanel implements ActionListener, UPNPAddonInfosListener
											, UPNPExternalIpListener, UPNPCommonLinkPropertiesListener
											, UPNPStatusInfoListener {
	private static final int PADDING_RIGHT = 50;

	private static final int PADDING_TOP = 20;

	public static final long serialVersionUID = 1;

	private XYSeries inSeries, outSeries;

	private XYSeriesCollection collectionIn, collectionOut;

	private JFreeChart inetChart;

	private JToggleButton enableInetMonitoring;

	private JButton actualizeStaticData;

	private Timer dynamicUpnpTimer = null;

	private TimerTask dynamicUpnpTask = null;

	private static int count = 0;

	private CurrentCallsTable currentCallsTable;

	private StatusBarController statusBarController = new StatusBarController();

	private JLabel externalIPLabel, uptimeLabel,
					dns1Label, dns2Label,
					syncDownLabel, syncUpLabel,
					sendRateLabel, receivedRateLabel,
					totalSentLabel, totalReceivedLabel;

	protected PropertyProvider properties = PropertyProvider.getInstance();
	protected MessageProvider messages = MessageProvider.getInstance();

	/**
	 * Creates the two monitoring sub panels and initializes everything
	 *
	 */
	public MonitoringPanel(){
		setLayout(new BorderLayout());

		JPanel innerPanel = new JPanel();
		innerPanel.setLayout(new BorderLayout());
		innerPanel.add(createInternetPanel(), BorderLayout.NORTH);
		innerPanel.add(createPhonePanel(), BorderLayout.CENTER);

		this.add(new JScrollPane(innerPanel), BorderLayout.CENTER);

		getStaticUPnPInfos();
	}

	public JPanel createInternetPanel(){
		JPanel inetPanel = new JPanel();
		inetPanel.setLayout(new BorderLayout());


		JPanel staticInformations = new JPanel();
		externalIPLabel = new JLabel();
		uptimeLabel = new JLabel();

		staticInformations.setLayout(new GridBagLayout());
		dns1Label = new JLabel();
		dns2Label = new JLabel();
		syncDownLabel = new JLabel();
		syncUpLabel = new JLabel();

		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.insets.right = 5;
		c.anchor = GridBagConstraints.EAST;

		c.gridx = 0;
		c.gridy = 0;
		staticInformations.add(new JLabel(messages.getMessage("external_ip")), c); //$NON-NLS-1$
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(externalIPLabel, c);

		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		staticInformations.add(new JLabel(messages.getMessage("uptime")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(uptimeLabel, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(messages.getMessage("sync_down")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(syncDownLabel, c);

		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		staticInformations.add(new JLabel(messages.getMessage("dns_server_1")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.insets.right = 0;
		staticInformations.add(dns1Label, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(messages.getMessage("sync_up")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(syncUpLabel, c);

		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		staticInformations.add(new JLabel(messages.getMessage("dns_server_2")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.insets.right = 0;
		staticInformations.add(dns2Label, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.CENTER;
		c.insets.top = PADDING_TOP;
		actualizeStaticData = new JButton(messages.getMessage("actualize")); //$NON-NLS-1$
		actualizeStaticData.setActionCommand("actualizeStaticUpnp"); //$NON-NLS-1$
		actualizeStaticData.addActionListener(this);
		staticInformations.add(actualizeStaticData, c);
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(
			       lowerEtched, messages.getMessage("dsl_information")); //$NON-NLS-1$
		title.setTitleJustification(TitledBorder.CENTER);
		Border upperPanelBorder = BorderFactory.createCompoundBorder(
				title, new EmptyBorder(15,0,0,0));
		staticInformations.setBorder(upperPanelBorder);



		JPanel dynamicInformations = new JPanel();
		dynamicInformations.setLayout(new BorderLayout());
		//initialize the data series, domain is time
		//range is the kb/s value
		inSeries = new XYSeries(messages.getMessage("upload")); //$NON-NLS-1$
		inSeries.add(System.currentTimeMillis(), 0.0);

		outSeries = new XYSeries(messages.getMessage("download")); //$NON-NLS-1$
		outSeries.add(System.currentTimeMillis(), 0.0);

		collectionIn = new XYSeriesCollection();
		collectionIn.addSeries(inSeries);

		collectionOut = new XYSeriesCollection();
		collectionOut.addSeries(outSeries);


		//create the filled chart plot
		XYDataset data1 = collectionOut;
		XYItemRenderer renderer1 = new XYAreaRenderer(XYAreaRenderer.AREA);
		renderer1.setToolTipGenerator(
				new StandardXYToolTipGenerator(
		                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
		                new SimpleDateFormat("yy"), new DecimalFormat("0.0") //$NON-NLS-1$, //$NON-NLS-2$
		        )
		);

		//setup the other chart options including gradient fill color
		DateAxis domainAxis = new DateAxis();
		domainAxis.setTickLabelsVisible(false);
		domainAxis.setTickMarksVisible(false);
		ValueAxis rangeAxis = new NumberAxis("kB/s"); //$NON-NLS-1$
	    renderer1.setPaint(new GradientPaint(0,0, new Color(175, 0, 30), 0, 215,
	    		new Color(250,220, 220), false));
		XYPlot plot = new XYPlot(data1, domainAxis, rangeAxis, renderer1);

		//create the line chart plot
		XYDataset data2 = collectionIn;
		XYItemRenderer renderer2 = new XYAreaRenderer(XYAreaRenderer.AREA);
		renderer2.setToolTipGenerator(
	            new StandardXYToolTipGenerator(
	                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
	                new SimpleDateFormat("yy"), new DecimalFormat("0.0") //$NON-NLS-1$, //$NON-NLS-2$
	            )
	    );
		renderer2.setPaint(new GradientPaint(0,0, new Color(0, 175, 30), 0, 215,
				new Color(220, 250, 220), false));

		//Add the new data set to the first plot
		plot.setDataset(1, data2);
		plot.setRenderer(1, renderer2);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		//finally create the chart object and post it to the panel
		inetChart =  new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		inetChart.setAntiAlias(true);
		ChartPanel cp = new ChartPanel(inetChart);
		cp.setPreferredSize(new Dimension(500, 175));
		dynamicInformations.add(cp, BorderLayout.NORTH);

		JPanel dynamicLabels = new JPanel();
		dynamicLabels.setLayout(new GridBagLayout());
		sendRateLabel = new JLabel();
		totalSentLabel = new JLabel();
		receivedRateLabel = new JLabel();
		totalReceivedLabel = new JLabel();

		c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.insets.right = 5;
		c.gridwidth = 1;

		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.insets.top = PADDING_TOP;
		dynamicLabels.add(new JLabel(messages.getMessage("send_rate")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		dynamicLabels.add(sendRateLabel, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.WEST;
		dynamicLabels.add(new JLabel(messages.getMessage("totaldatasent")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		dynamicLabels.add(totalSentLabel, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets.top = 0;
		dynamicLabels.add(new JLabel(messages.getMessage("receive_rate")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		dynamicLabels.add(receivedRateLabel, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.WEST;
		dynamicLabels.add(new JLabel(messages.getMessage("totaldatareceived")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		dynamicLabels.add(totalReceivedLabel, c);

		 //setup the monitoring toggle button
		enableInetMonitoring = new JToggleButton(messages.getMessage("enable_inet_monitoring")); //$NON-NLS-1$
		enableInetMonitoring.setActionCommand("toggleInetMonitoring"); //$NON-NLS-1$
		enableInetMonitoring.addActionListener(this);
//		enableInetMonitoring.setSize(new Dimension(200,30));
		if(properties.getStateProperty("inet.monitoring", "false").equals("true")){ //$NON-NLS-1$, //$NON-NLS-2$
			enableInetMonitoring.setSelected(true);
			setDynamicTimer();
			actualizeStaticData.setEnabled(false);
		}else{
			enableInetMonitoring.setSelected(false);
			actualizeStaticData.setEnabled(true);
		}

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = 4;
		c.insets.top = PADDING_TOP;
		dynamicLabels.add(enableInetMonitoring, c);

		dynamicInformations.add(dynamicLabels, BorderLayout.CENTER);

		//create the border for the panel
		lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		title = BorderFactory.createTitledBorder(
			       lowerEtched, messages.getMessage("inet_usgage")); //$NON-NLS-1$
		title.setTitleJustification(TitledBorder.CENTER);
		upperPanelBorder = BorderFactory.createCompoundBorder(
				title, new EmptyBorder(15,0,0,0));
		dynamicInformations.setBorder(upperPanelBorder);

		//we don't want to draw the chart every single update time
		inSeries.setNotify(false);
		outSeries.setNotify(false);

		inetPanel.add(staticInformations, BorderLayout.NORTH);
		inetPanel.add(dynamicInformations, BorderLayout.CENTER);

		return inetPanel;
	}

	/**
	 *
	 * @return
	 */
	private JPanel createPhonePanel(){
		JPanel phonePanel = new JPanel();
		phonePanel.setLayout(new BorderLayout());
		phonePanel.setBorder(new EmptyBorder(0,0,20,0));

		currentCallsTable = new CurrentCallsTable();

		final JTable currentCalls = new JTable(currentCallsTable) {
			private static final long serialVersionUID = 1;

			public Component prepareRenderer(TableCellRenderer renderer,
					int rowIndex, int vColIndex) {
				Component c = super.prepareRenderer(renderer, rowIndex,
						vColIndex);
				if ((rowIndex % 2 == 0) && !isCellSelected(rowIndex, vColIndex)) {
					c.setBackground(new Color(255, 255, 200));
				} else if (!isCellSelected(rowIndex, vColIndex)) {
					// If not shaded, match the table's background
					c.setBackground(getBackground());
				} else {
					c.setBackground(new Color(204, 204, 255));
				}
				return c;
			}
		};

		currentCalls.setRowHeight(24);
		currentCalls.setAutoCreateColumnsFromModel(true);

		currentCalls.getColumnModel().getColumn(0).setMinWidth(20);
		currentCalls.getColumnModel().getColumn(0).setMaxWidth(50);
		currentCalls.getColumnModel().getColumn(0).setCellRenderer(new CallTypeCellRenderer());

		currentCalls.getColumnModel().getColumn(1).setMinWidth(20);
		//currentCalls.getColumnModel().getColumn(1).setMaxWidth(90);
		currentCalls.getColumnModel().getColumn(1).setCellRenderer(new DateCellRenderer());

		currentCalls.getColumnModel().getColumn(2).setMinWidth(20);
		currentCalls.getColumnModel().getColumn(2).setMaxWidth(90);
		currentCalls.getColumnModel().getColumn(2).setCellRenderer(new CallByCallCellRenderer());

		currentCalls.getColumnModel().getColumn(3).setMinWidth(20);
		currentCalls.getColumnModel().getColumn(3).setMaxWidth(130);
		currentCalls.getColumnModel().getColumn(4).setCellRenderer(new NumberCellRenderer());

		currentCalls.getColumnModel().getColumn(4).setMinWidth(20);
		//currentCalls.getColumnModel().getColumn(4).setMaxWidth(140);
		currentCalls.getColumnModel().getColumn(4).setCellRenderer(new PersonCellRenderer());


		currentCalls.getColumnModel().getColumn(5).setMinWidth(20);
		currentCalls.getColumnModel().getColumn(5).setMaxWidth(90);
		currentCalls.getColumnModel().getColumn(5).setCellRenderer(new PortCellRenderer());

		currentCalls.getColumnModel().getColumn(6).setMinWidth(20);
		//currentCalls.getColumnModel().getColumn(6).setMaxWidth(200);
		currentCalls.getColumnModel().getColumn(6).setCellRenderer(new RouteCellRenderer());
		currentCalls.setSize(500, 150);

		//create the Border
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(
			       lowerEtched, messages.getMessage("calls_in_progress")); //$NON-NLS-1$
		title.setTitleJustification(TitledBorder.CENTER);
		Border lowerPanelBorder = BorderFactory.createCompoundBorder(
				title, new EmptyBorder(15,0,5,0));
		phonePanel.setBorder(lowerPanelBorder);

		phonePanel.add(new JScrollPane(currentCalls), BorderLayout.CENTER);
		phonePanel.setPreferredSize(new Dimension(500, 164));

		return phonePanel;

	}

	/**
	 * This function updates the internet monitor with current values
	 * on every 6th update (~5.7 Seconds), the graphic is redrawn
	 *
	 * @param in, the current amount of incoming bytes
	 * @param out, the current amount of outgoing bytes
	 */
	public void updateInternetUsage(String in, String out){

		inSeries.add(System.currentTimeMillis(), (Double.parseDouble(in)/1024) );
		outSeries.add(System.currentTimeMillis(), (Double.parseDouble(out)/1024));

		if(inSeries.getItemCount() >  200){
			inSeries.remove(0);
			outSeries.remove(0);
		}

		count++;

		if(count > 5){
			outSeries.setNotify(true);
			inSeries.fireSeriesChanged();
			outSeries.setNotify(false);
			count = 0;
		}

	}

	//TODO change me!
	public void setStatus(){
		//statusBarController.fireStatusChanged("!");
		statusBarController.fireStatusChanged("");
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("toggleInetMonitoring")){ //$NON-NLS-1$
			if(enableInetMonitoring.isSelected()){
				properties.setStateProperty("inet.monitoring", "true"); //$NON-NLS-1$, //$NON-NLS-2$
				setDynamicTimer();
				actualizeStaticData.setEnabled(false);
			}else{
				properties.setStateProperty("inet.monitoring", "false"); //$NON-NLS-1$, //$NON-NLS-2$
				if (dynamicUpnpTimer != null)
					dynamicUpnpTimer.cancel();
				actualizeStaticData.setEnabled(true);
			}
		}
		else if (e.getActionCommand().equals("actualizeStaticUpnp")) //$NON-NLS-1$
		{
			getStaticUPnPInfos();
		}

	}

	private void getStaticUPnPInfos()
	{
		try {
			JFritz.getBoxCommunication().getBox(0).getInternetStats(this);
			JFritz.getBoxCommunication().getBox(0).getCommonLinkInfo(this);
			JFritz.getBoxCommunication().getBox(0).getStatusInfo(this);
			JFritz.getBoxCommunication().getBox(0).getExternalIPAddress(this);
		} catch (Exception e) {
			// nothing to do, ignore silently
		}
	}

	/**
	 * This function sets up the timer for automatic updates
	 * of the current internet status
	 *
	 */
	private void setDynamicTimer(){
		//Create timer for updating the values
		if (dynamicUpnpTimer != null)
			dynamicUpnpTimer.cancel();

		dynamicUpnpTimer = new Timer("NetworkMonitor", true);
		dynamicUpnpTask = new TimerTask(){
			public void run() {
//				JFritz.getFritzBox().getInternetStats(mPanel);
				getStaticUPnPInfos();
			}

		};
		dynamicUpnpTimer.schedule(dynamicUpnpTask, 1000, 975);
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}

	public void setBytesRate(String sent, String received) {
		if (sent.equals("") || received.equals(""))
		{
			return;
		}
		updateInternetUsage(sent, received);

		if (sent.equals("-")) //$NON-NLS-1$
		{
			sendRateLabel.setText(sent);
		}
		else
		{
			int max = Integer.parseInt(sent);
			float kMax =  max / 1000;
			float mMax = kMax / 1024;
			String kMaxStr = NumberFormat.getInstance().format(kMax);
			String mMaxStr = String.format("%.1f", mMax); //$NON-NLS-1$
			sendRateLabel.setText(kMaxStr + " kB/s (" + mMaxStr + " MB/s)"); //$NON-NLS-1$, //$NON-NLS-2$
		}

		if (received.equals("-"))
		{
			receivedRateLabel.setText(received);
		}
		else
		{
			int max = Integer.parseInt(received);
			float kMax = max / 1000;
			float mMax = kMax / 1024;
			String kMaxStr = NumberFormat.getInstance().format(kMax);
			String mMaxStr = String.format("%.1f", mMax); //$NON-NLS-1$
			receivedRateLabel.setText(kMaxStr + " kB/s (" + mMaxStr + " MB/s)"); //$NON-NLS-1$, //$NON-NLS-2$
		}
	}

	public void setDNSInfo(String dns1, String dns2) {
		dns1Label.setText(dns1);
		dns2Label.setText(dns2);
	}

	public void setTotalBytesInfo(String sent, String received) {
		if (sent.equals("") || received.equals(""))
		{
			return;
		}
		DecimalFormat df = new DecimalFormat("#");
		if (!sent.equals("-")) //$NON-NLS-1$
		{
			long bSent = Long.parseLong(sent);
			double kSent = bSent / 1000;
			double mSent = kSent / 1024;
			double gSent = mSent / 1024;

			if (gSent < 1.0)
			{
				totalSentLabel.setText(df.format(kSent) + " kB (" + df.format(mSent) + " MB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
			else
			{
				totalSentLabel.setText(df.format(mSent) + " MB (" + df.format(gSent) + " GB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
		}
		else
		{
			totalSentLabel.setText(sent);
		}

		if (!received.equals("-")) //$NON-NLS-1$
		{
			long bReceived = Long.parseLong(received);
			double kReceived = bReceived / 1000;
			double mReceived = kReceived / 1024;
			double gReceived = mReceived / 1024;

			if (gReceived < 1.0)
			{
				totalReceivedLabel.setText(df.format(kReceived) + " kB (" + df.format(mReceived) + " MB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
			else
			{
				totalReceivedLabel.setText(df.format(mReceived) + " MB (" + df.format(gReceived) + " GB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
		}
		else
		{
			totalReceivedLabel.setText(sent);
		}
	}

	public void setExternalIp(String externalIp) {
		externalIPLabel.setText(externalIp);
	}

	public void setDownstreamMaxBitRate(String maxDown) {
		if (maxDown.equals(""))
		{
			return;
		}
		if (!maxDown.equals("-")) //$NON-NLS-1$
		{
			int max = Integer.parseInt(maxDown);
			float kMax = max;
			float mMax = kMax / 1000;

			DecimalFormat df = new DecimalFormat("#");
			syncDownLabel.setText(df.format(kMax) + " kbit/s (" + df.format(mMax) + " Mbit/s)"); //$NON-NLS-1$, //$NON-NLS-2$
		}
		else
		{
			syncDownLabel.setText(maxDown);
		}
	}

	public void setUpstreamMaxBitRate(String maxUp) {
		if (maxUp.equals(""))
		{
			return;
		}
		if (!maxUp.equals("-")) //$NON-NLS-1$
		{
			int max = Integer.parseInt(maxUp);
			float kMax = max;
			float mMax = kMax / 1024;

			DecimalFormat df = new DecimalFormat("#");
			syncUpLabel.setText(df.format(kMax) + " kbit/s (" + df.format(mMax) + " Mbit/s)"); //$NON-NLS-1$, //$NON-NLS-2$
		}
		else
		{
			syncUpLabel.setText(maxUp);
		}
	}

	public void setUptime(String uptime) {
		if (uptime.equals(""))
		{
			return;
		}
		if (uptime.equals("-")) //$NON-NLS-1$
		{
			uptimeLabel.setText(uptime);
		}
		else
		{
		      int sek1 = 1;
		      int min = (60*sek1);
		      int std = (60*min);
		      int tag = (24*std);
		      int jah = (365*tag);

		      int sek = Integer.parseInt(uptime);

		      int tagerg = (sek%jah)/(tag);
		      int stderg = (sek%tag)/(std);
		      int minerg = (sek%std)/(min);
		      int sekerg = (sek%min*sek1);

		      String uptimeNew = ""; //$NON-NLS-1$
		      if (tagerg != 0)
		      {
		    	  uptimeNew = uptimeNew + tagerg + " " + messages.getMessage("day_days") + " "; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		      }
		      if ((tagerg == 0 && stderg != 0) || tagerg != 0)
		      {
		    	  uptimeNew = uptimeNew + stderg + " " + messages.getMessage("hour_hours") + " "; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		      }
		      if ((tagerg == 0 && stderg == 0 && minerg != 0) || (tagerg != 0) || (stderg != 0))
		      {
		    	  uptimeNew = uptimeNew + minerg + " " + messages.getMessage("minute_minutes") + " "; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		      }
		      if ((tagerg == 0 && stderg == 0 && minerg == 0 && sekerg != 0) || (tagerg != 0) || (stderg != 0) || (minerg != 0))
		      {
		    	  uptimeNew = uptimeNew + sekerg + " " + messages.getMessage("second_seconds"); //$NON-NLS-1$, //$NON-NLS-2$
		      }

			  uptimeLabel.setText(uptimeNew);
		}
	}

	public void prepareShutdown()
	{
		if (dynamicUpnpTimer != null)
		{
			dynamicUpnpTimer.cancel();
		}
	}
}
