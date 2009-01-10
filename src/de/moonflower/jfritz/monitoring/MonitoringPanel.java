package de.moonflower.jfritz.monitoring;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;

import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GradientPaint;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.TableCellRenderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;


import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.cellrenderer.*;
import de.moonflower.jfritz.monitoring.CurrentCallsTable;
import de.moonflower.jfritz.utils.JFritzUtils;
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

	private static int count = 0;

	private CurrentCallsTable currentCallsTable;

	private StatusBarController statusBarController = new StatusBarController();

	private JLabel externalIPLabel, uptimeLabel,
					dns1Label, dns2Label, voipDnsLabel1, voipDnsLabel2,
					autoDisconnectLabel, idleTimeLabel,
					syncDownLabel, syncUpLabel,
					upnpControlLabel, pppoePassThroughLabel,
					sendRateLabel, receivedRateLabel,
					totalSentLabel, totalReceivedLabel;
	/**
	 * Creates the two monitoring sub panels and initializes everything
	 *
	 */
	public MonitoringPanel(){
		setLayout(new BorderLayout());

		add(createInternetPanel(), BorderLayout.NORTH);
		add(createPhonePanel(), BorderLayout.CENTER);

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
		voipDnsLabel1 = new JLabel();
		voipDnsLabel2 = new JLabel();
		autoDisconnectLabel = new JLabel();
		idleTimeLabel = new JLabel();
		syncDownLabel = new JLabel();
		syncUpLabel = new JLabel();
		upnpControlLabel = new JLabel();
		pppoePassThroughLabel = new JLabel();

		GridBagConstraints c = new GridBagConstraints();
		c.insets.top = 5;
		c.insets.bottom = 5;
		c.insets.left = 5;
		c.insets.right = 5;
		c.anchor = GridBagConstraints.WEST;

		c.gridx = 0;
		c.gridy = 0;
		staticInformations.add(new JLabel(Main.getMessage("external_ip")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(externalIPLabel, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		staticInformations.add(new JLabel(Main.getMessage("uptime")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(uptimeLabel, c);

		//TODO: add local ip addresses

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(Main.getMessage("sync_down")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(syncDownLabel, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.WEST;
		staticInformations.add(new JLabel(Main.getMessage("upnp_control")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(upnpControlLabel, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(Main.getMessage("sync_up")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(syncUpLabel, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.WEST;
		staticInformations.add(new JLabel(Main.getMessage("pppoe_passthrough")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(pppoePassThroughLabel, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(Main.getMessage("dns_server_1")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(dns1Label, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		staticInformations.add(new JLabel(Main.getMessage("dns_server_voip_1")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(voipDnsLabel1, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(Main.getMessage("dns_server_2")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.insets.right = PADDING_RIGHT;
		staticInformations.add(dns2Label, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		staticInformations.add(new JLabel(Main.getMessage("dns_server_voip_2")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(voipDnsLabel2, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(Main.getMessage("auto_disconnect_time")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(autoDisconnectLabel, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		staticInformations.add(new JLabel(Main.getMessage("connection_idle_time")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		staticInformations.add(idleTimeLabel, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.gridwidth = 4;
		c.anchor = GridBagConstraints.CENTER;
		c.insets.top = PADDING_TOP;
		actualizeStaticData = new JButton(Main.getMessage("actualize")); //$NON-NLS-1$
		actualizeStaticData.setActionCommand("actualizeStaticUpnp"); //$NON-NLS-1$
		actualizeStaticData.addActionListener(this);
		staticInformations.add(actualizeStaticData, c);
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(
			       lowerEtched, Main.getMessage("dsl_information")); //$NON-NLS-1$
		title.setTitleJustification(TitledBorder.CENTER);
		Border upperPanelBorder = BorderFactory.createCompoundBorder(
				title, new EmptyBorder(15,0,0,0));
		staticInformations.setBorder(upperPanelBorder);



		JPanel dynamicInformations = new JPanel();
		dynamicInformations.setLayout(new BorderLayout());
		//initialize the data series, domain is time
		//range is the kb/s value
		inSeries = new XYSeries(Main.getMessage("upload")); //$NON-NLS-1$
		inSeries.add(System.currentTimeMillis(), 0.0);

		outSeries = new XYSeries(Main.getMessage("download")); //$NON-NLS-1$
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
		ValueAxis rangeAxis = new NumberAxis("KB/s"); //$NON-NLS-1$
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
		dynamicLabels.add(new JLabel(Main.getMessage("send_rate")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		dynamicLabels.add(sendRateLabel, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.WEST;
		dynamicLabels.add(new JLabel(Main.getMessage("totaldatasent")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		dynamicLabels.add(totalSentLabel, c);

		c.gridx = 0;
		c.gridy = c.gridy + 1;
		c.anchor = GridBagConstraints.WEST;
		c.insets.top = 0;
		dynamicLabels.add(new JLabel(Main.getMessage("receive_rate")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		c.insets.right = PADDING_RIGHT;
		dynamicLabels.add(receivedRateLabel, c);
		c.insets.right = 0;
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.WEST;
		dynamicLabels.add(new JLabel(Main.getMessage("totaldatareceived")), c); //$NON-NLS-1$
		c.gridx = c.gridx + 1;
		c.anchor = GridBagConstraints.EAST;
		dynamicLabels.add(totalReceivedLabel, c);

		 //setup the monitoring toggle button
		enableInetMonitoring = new JToggleButton(Main.getMessage("enable_inet_monitoring")); //$NON-NLS-1$
		enableInetMonitoring.setActionCommand("toggleInetMonitoring"); //$NON-NLS-1$
		enableInetMonitoring.addActionListener(this);
//		enableInetMonitoring.setSize(new Dimension(200,30));
		if(Main.getStateProperty("inet.monitoring", "false").equals("true")){ //$NON-NLS-1$, //$NON-NLS-2$
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
			       lowerEtched, Main.getMessage("inet_usgage")); //$NON-NLS-1$
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
			       lowerEtched, Main.getMessage("calls_in_progress")); //$NON-NLS-1$
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
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("toggleInetMonitoring")){ //$NON-NLS-1$
			if(enableInetMonitoring.isSelected()){
				Main.setStateProperty("inet.monitoring", "true"); //$NON-NLS-1$, //$NON-NLS-2$
				setDynamicTimer();
				actualizeStaticData.setEnabled(false);
			}else{
				Main.setStateProperty("inet.monitoring", "false"); //$NON-NLS-1$, //$NON-NLS-2$
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
//		JFritz.getFritzBox().getInternetStats(this);
		JFritz.getFritzBox().getCommonLinkInfo(this);
		JFritz.getFritzBox().getStatusInfo(this);
		JFritz.getFritzBox().getExternalIPAddress(this);
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

		dynamicUpnpTimer = new Timer();
		final MonitoringPanel mPanel = this;
		TimerTask task = new TimerTask(){
			public void run() {
				JFritz.getFritzBox().getInternetStats(mPanel);
				getStaticUPnPInfos();
			}

		};
		dynamicUpnpTimer.schedule(task, 1000, 975);
	}

	public StatusBarController getStatusBarController() {
		return statusBarController;
	}

	public void setStatusBarController(StatusBarController statusBarController) {
		this.statusBarController = statusBarController;
	}

	@Override
	public void setBytesRate(String sent, String received) {
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
			sendRateLabel.setText(kMaxStr + " KB/s (" + mMaxStr + " MB/s)"); //$NON-NLS-1$, //$NON-NLS-2$
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
			receivedRateLabel.setText(kMaxStr + " KB/s (" + mMaxStr + " MB/s)"); //$NON-NLS-1$, //$NON-NLS-2$
		}
	}

	@Override
	public void setDNSInfo(String dns1, String dns2) {
		dns1Label.setText(dns1);
		dns2Label.setText(dns2);
	}

	@Override
	public void setDisconnectInfo(String disconnectTime, String idleTime) {
		if (disconnectTime.equals("0"))
		{
			autoDisconnectLabel.setText(Main.getMessage("disabled"));
		}
		else
		{
			autoDisconnectLabel.setText(disconnectTime + " " + Main.getMessage("second_seconds"));
		}
		idleTimeLabel.setText(idleTime + " " + Main.getMessage("second_seconds"));
	}

	@Override
	public void setOtherInfo(String upnpControl, String routedMode) {
		if (upnpControl.equals("1")) //$NON-NLS-1$
		{
			upnpControlLabel.setText(Main.getMessage("enabled")); //$NON-NLS-1$
		}
		else
		{
			upnpControlLabel.setText(Main.getMessage("disabled")); //$NON-NLS-1$
		}

		if (routedMode.equals("1")) //$NON-NLS-1$
		{
			pppoePassThroughLabel.setText(Main.getMessage("enabled")); //$NON-NLS-1$
		}
		else
		{
			pppoePassThroughLabel.setText(Main.getMessage("disabled")); //$NON-NLS-1$
		}
	}

	@Override
	public void setTotalBytesInfo(String sent, String received) {
		if (!sent.equals("-")) //$NON-NLS-1$
		{
			long bSent = Long.parseLong(sent);
			float kSent = bSent / 1000;
			float mSent = kSent / 1024;
			float gSent = mSent / 1024;
			NumberFormat.getInstance().setMinimumFractionDigits(0);
			NumberFormat.getInstance().setMaximumFractionDigits(1);
			String kSentStr = NumberFormat.getInstance().format(kSent);
			String mSentStr = String.format("%.1f", mSent); //$NON-NLS-1$
			String gSentStr = String.format("%.1f", gSent); //$NON-NLS-1$
			if (gSent < 1.0)
			{
				totalSentLabel.setText(kSentStr + " KB (" + mSentStr + " MB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
			else
			{
				totalSentLabel.setText(mSentStr + " MB (" + gSentStr + " GB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
		}
		else
		{
			totalSentLabel.setText(sent);
		}

		if (!received.equals("-")) //$NON-NLS-1$
		{
			long bReceived = Long.parseLong(received);
			float kReceived = bReceived / 1000;
			float mReceived = kReceived / 1024;
			float gReceived = mReceived / 1024;
			NumberFormat.getInstance().setMinimumFractionDigits(0);
			NumberFormat.getInstance().setMaximumFractionDigits(1);
			String kReceivedStr = NumberFormat.getInstance().format(kReceived);
			String mReceivedStr = String.format("%.1f", mReceived); //$NON-NLS-1$
			String gReceivedStr = String.format("%.1f", gReceived); //$NON-NLS-1$
			if (gReceived < 1.0)
			{
				totalReceivedLabel.setText(kReceivedStr + " KB (" + mReceivedStr + " MB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
			else
			{
				totalReceivedLabel.setText(mReceivedStr + " MB (" + gReceivedStr + " GB)"); //$NON-NLS-1$, //$NON-NLS-2$
			}
		}
		else
		{
			totalReceivedLabel.setText(sent);
		}
	}

	@Override
	public void setVoipDNSInfo(String voipDns1, String voipDns2) {
		voipDnsLabel1.setText(voipDns1);
		voipDnsLabel2.setText(voipDns2);
	}

	@Override
	public void setExternalIp(String externalIp) {
		externalIPLabel.setText(externalIp);
	}

	@Override
	public void setDownstreamMaxBitRate(String maxDown) {
		if (!maxDown.equals("-")) //$NON-NLS-1$
		{
			int max = Integer.parseInt(maxDown);
			float kMax = max / 1000;
			float mMax = kMax / 1024;
			String kMaxStr = NumberFormat.getInstance().format(kMax);
			String mMaxStr = String.format("%.1f", mMax); //$NON-NLS-1$
			syncDownLabel.setText(kMaxStr + " KBit/s (" + mMaxStr + " MBit/s)"); //$NON-NLS-1$, //$NON-NLS-2$
		}
		else
		{
			syncDownLabel.setText(maxDown);
		}
	}

	@Override
	public void setUpstreamMaxBitRate(String maxUp) {
		if (!maxUp.equals("-")) //$NON-NLS-1$
		{
			int max = Integer.parseInt(maxUp);
			float kMax = max / 1000;
			float mMax = kMax / 1024;
			String kMaxStr = NumberFormat.getInstance().format(kMax);
			String mMaxStr = String.format("%.1f", mMax); //$NON-NLS-1$
			syncUpLabel.setText(kMaxStr + " KBit/s (" + mMaxStr + " MBit/s)"); //$NON-NLS-1$, //$NON-NLS-2$
		}
		else
		{
			syncUpLabel.setText(maxUp);
		}
	}

	@Override
	public void setUptime(String uptime) {
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
		    	  uptimeNew = uptimeNew + tagerg + " " + Main.getMessage("day_days") + " "; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		      }
		      if ((tagerg == 0 && stderg != 0) || tagerg != 0)
		      {
		    	  uptimeNew = uptimeNew + stderg + " " + Main.getMessage("hour_hours") + " "; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		      }
		      if ((tagerg == 0 && stderg == 0 && minerg != 0) || (tagerg != 0) || (stderg != 0))
		      {
		    	  uptimeNew = uptimeNew + minerg + " " + Main.getMessage("minute_minutes") + " "; //$NON-NLS-1$, //$NON-NLS-2$, //$NON-NLS-3$
		      }
		      if ((tagerg == 0 && stderg == 0 && minerg == 0 && sekerg != 0) || (tagerg != 0) || (stderg != 0) || (minerg != 0))
		      {
		    	  uptimeNew = uptimeNew + sekerg + " " + Main.getMessage("second_seconds"); //$NON-NLS-1$, //$NON-NLS-2$
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

		if (JFritzUtils.parseBoolean(Main.getStateProperty("inet.monitoring", "false")))
		{
			System.err.println("Enabled");
		}
		else
		{
			System.err.println("Disabled");
		}
	}
}
