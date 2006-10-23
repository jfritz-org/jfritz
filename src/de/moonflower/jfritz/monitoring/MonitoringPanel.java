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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.GradientPaint;

import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToggleButton;
import javax.swing.table.TableCellRenderer;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;


import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.cellrenderer.*;
import de.moonflower.jfritz.monitoring.CurrentCallsTable;
import de.moonflower.jfritz.monitoring.UpdateInternetTask;

/**
 * Class for displaying monitoring information like current internet
 * or phone usage
 *
 * Class uses jfreechart to display internet usage as filled line chart
 *
 * @author brian jensen
 *
 */
public class MonitoringPanel extends JPanel implements ActionListener {

	public static final long serialVersionUID = 1;

	private XYSeries inSeries, outSeries;

	private XYSeriesCollection collectionIn, collectionOut;

	private JFreeChart inetChart;

	private JToggleButton enableInetMonitoring;

	private Timer timer;

	private static int count = 0;

	private CurrentCallsTable currentCallsTable;

	/**
	 * Creates the two monitoring sub panels and initializes everything
	 *
	 */
	public MonitoringPanel(){
		setLayout(new BorderLayout());

		add(createPhonePanel(), BorderLayout.SOUTH);
		add(createInternetPanel(), BorderLayout.NORTH);

	}

	/**
	 * This creates the internet panel, which is the top half of the monitoring panel
	 *
	 * @return the internet panel
	 */
	public JPanel createInternetPanel(){
		JPanel inetPanel = new JPanel();
		inetPanel.setLayout(new BorderLayout());

		//initialize the data series, domain is time
		//range is the kb/s value
		inSeries = new XYSeries("In");
		inSeries.add(System.currentTimeMillis(), 0.0);

		outSeries = new XYSeries("Out");
		outSeries.add(System.currentTimeMillis(), 0.0);

		collectionIn = new XYSeriesCollection();
		collectionIn.addSeries(inSeries);

		collectionOut = new XYSeriesCollection();
		collectionOut.addSeries(outSeries);


		//create the filled chart plot
		XYDataset data1 = collectionIn;
		XYItemRenderer renderer1 = new XYAreaRenderer(XYAreaRenderer.AREA);
		renderer1.setToolTipGenerator(
				new StandardXYToolTipGenerator(
		                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
		                new SimpleDateFormat("yy"), new DecimalFormat("0.0")
		        )
		);

		//setup the other chart options including gradient fill color
		DateAxis domainAxis = new DateAxis();
		domainAxis.setTickLabelsVisible(false);
		domainAxis.setTickMarksVisible(false);
		ValueAxis rangeAxis = new NumberAxis("KB\\s");
		renderer1.setPaint(new GradientPaint(0,0, new Color(0, 175, 30), 0, 215,
				new Color(220, 250, 220), false));
		XYPlot plot = new XYPlot(data1, domainAxis, rangeAxis, renderer1);

		//create the line chart plot
		XYDataset data2 = collectionOut;
		XYItemRenderer renderer2 = new StandardXYItemRenderer();
		renderer2.setToolTipGenerator(
	            new StandardXYToolTipGenerator(
	                StandardXYToolTipGenerator.DEFAULT_TOOL_TIP_FORMAT,
	                new SimpleDateFormat("yy"), new DecimalFormat("0.0")
	            )
	    );

		//Add the new data set to the first plot
		plot.setDataset(1, data2);
		plot.setRenderer(1, renderer2);
		plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

		//finally create the chart object and post it to the panel
		inetChart =  new JFreeChart("", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		inetChart.setAntiAlias(true);
		ChartPanel cp = new ChartPanel(inetChart);
		cp.setPreferredSize(new Dimension(500, 175));
		inetPanel.add(cp, BorderLayout.CENTER);

		 //setup the monitoring toggle button
		enableInetMonitoring = new JToggleButton(Main.getMessage("enable_inet_monitoring"));
		enableInetMonitoring.setActionCommand("toggleInetMonitoring");
		enableInetMonitoring.addActionListener(this);
		enableInetMonitoring.setSize(new Dimension(200,30));
		if(Main.getProperty("inet.monitoring", "false").equals("true")){
			enableInetMonitoring.setSelected(true);
			setTimer();
		}else{
			enableInetMonitoring.setSelected(false);
		}
		inetPanel.add(enableInetMonitoring, BorderLayout.SOUTH);

		//create the border for the panel
		Border lowerEtched = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		TitledBorder title = BorderFactory.createTitledBorder(
			       lowerEtched, Main.getMessage("inet_usgage"));
		title.setTitleJustification(TitledBorder.CENTER);
		Border upperPanelBorder = BorderFactory.createCompoundBorder(
				title, new EmptyBorder(15,0,0,0));
		inetPanel.setBorder(upperPanelBorder);

		//we don't want to draw the chart every single update time
		inSeries.setNotify(false);
		outSeries.setNotify(false);

		return inetPanel;
	}

	/**
	 * TODO
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
				if (rowIndex % 2 == 0 && !isCellSelected(rowIndex, vColIndex)) {
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
			       lowerEtched, "Calls in Progress");
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
	 * @param out, the current amout of outgoing bytes
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

	public void setStatus(){
		JFritz.getJframe().setStatus("Change Me!");
	}

	public void actionPerformed(ActionEvent e) {

		if (e.getActionCommand().equals("toggleInetMonitoring")){
			if(enableInetMonitoring.isSelected()){
				Main.setProperty("inet.monitoring", "true");
				setTimer();
			}else{
				Main.setProperty("inet.monitoring", "false");
				timer.cancel();
			}
		}

	}

	/**
	 * This function sets up the timer for automatic updates
	 * of the current internet status
	 *
	 */
	private void setTimer(){
		//Create timer for updating the values
		timer = new Timer();
		UpdateInternetTask task = new UpdateInternetTask(this);
		timer.schedule(task, 1000, 975);
	}

}
