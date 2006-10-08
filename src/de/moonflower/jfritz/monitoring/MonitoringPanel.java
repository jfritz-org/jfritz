package de.moonflower.jfritz.monitoring;

import org.jfree.data.xy.XYSeries;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ChartPanel;
import org.jfree.data.general.SeriesChangeEvent;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.chart.renderer.xy.StandardXYItemRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Timer;


import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.monitoring.UpdateInternetTask;

/**
 * Class for displaying monitoring information like current internet
 * or phone usage
 *
 * Class uses jfreechart to display internet usage as chart
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

	/**
	 * Creates the two monitoring sub panels and initializes everything
	 *
	 */
	public MonitoringPanel(){
		setLayout(new BorderLayout());

		add(createPhonePanel(), BorderLayout.SOUTH);
		add(createInternetPanel(), BorderLayout.NORTH);
		//TODO: Phone usage panel here!

	}
	/**
	 * This creates the internet panel, which is the top half of the monitoring tab
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
		renderer1.setPaint(new GradientPaint(0,0, new Color(0, 175, 30), 0, 275,
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
		inetChart =  new JFreeChart("Internet Usage", JFreeChart.DEFAULT_TITLE_FONT, plot, true);
		inetChart.setAntiAlias(true);
		ChartPanel cp = new ChartPanel(inetChart);
		cp.setPreferredSize(new Dimension(500, 250));
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

		phonePanel.add(new JLabel("This is where the phone monitor should go"), BorderLayout.CENTER);
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
		timer.schedule(task, 1000, 950);
	}

}
