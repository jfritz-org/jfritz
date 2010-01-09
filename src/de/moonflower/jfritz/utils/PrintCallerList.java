/*
 * Created on 25.10.2005
 *
 */
package de.moonflower.jfritz.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.print.PageFormat;
import java.awt.print.Paper;

import javax.swing.ImageIcon;
import javax.swing.table.TableColumnModel;

import org.jfree.report.ElementAlignment;
import org.jfree.report.ImageElement;
import org.jfree.report.JFreeReport;
import org.jfree.report.PageHeader;
import org.jfree.report.ReportProcessingException;
import org.jfree.report.ShapeElement;
import org.jfree.report.SimplePageDefinition;
import org.jfree.report.TextElement;
import org.jfree.report.elementfactory.DateFieldElementFactory;
import org.jfree.report.elementfactory.ImageFieldElementFactory;
import org.jfree.report.elementfactory.LabelElementFactory;
import org.jfree.report.elementfactory.StaticShapeElementFactory;
import org.jfree.report.elementfactory.TextFieldElementFactory;
import org.jfree.report.function.AbstractExpression;
import org.jfree.report.modules.gui.base.PreviewFrame;
import org.jfree.report.style.FontDefinition;

import sun.awt.image.ToolkitImage;

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.Main;
import de.moonflower.jfritz.ProgramConstants;
import de.moonflower.jfritz.callerlist.CallerTable;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.Person;
import de.moonflower.jfritz.struct.Port;

/**
 * @author Robert Palmer
 *
 */
public class PrintCallerList extends Thread {

    private int[] columnWidth;

    private int[] columnStart;

    private JFreeReport report;

    private void createColumnWidths() {
        Debug.debug("Create Columns"); //$NON-NLS-1$
        int columnCount = JFritz.getJframe().getCallerTable().getColumnCount();
        int fixedColumnsWidth = 0; // width of all columns except "number" and
        // "participant"
        TableColumnModel colModel = JFritz.getJframe().getCallerTable()
                .getColumnModel();
        columnWidth = new int[columnCount];
        columnStart = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            String columnName = colModel.getColumn(i).getHeaderValue()
                    .toString();
            if (columnName.equals(Main.getMessage("type"))) { //$NON-NLS-1$
                // Icon, same Width
                columnWidth[i] = colModel.getColumn(i).getWidth() - 10;
                // Minimum size of type-column (icon)
                if (columnWidth[i] < 20)
                    columnWidth[i] = 20;
            } else if (columnName.equals(Main.getMessage("picture")))
            {
            	// pictures have always the same width
            	columnWidth[i] = colModel.getColumn(i).getWidth();
            } else {
                // smaller font size: reduce width
                columnWidth[i] = colModel.getColumn(i).getWidth() - 20;
            }
            columnStart[i] = 0;
            Debug.always("Column "+i+ " [" + columnName + "] width: " + columnWidth[i]);
        }

        // Rest width for columns "number" and "participant"
        // ensure, that restWidth >= 150
        int restWidth = 0;
        do {
            fixedColumnsWidth = 0;
            int columnWithMaxWidth = 0;
            Debug.debug(columnCount + " Columns"); //$NON-NLS-1$
            for (int i = 0; i < columnCount; i++) {
                String columnName = colModel.getColumn(i).getHeaderValue()
                .toString();
                if (!(columnName.equals(Main.getMessage("number"))) //$NON-NLS-1$
                        && (!columnName.equals(Main.getMessage("participant")))) //$NON-NLS-1$
                {
                    if (columnWidth[i] > columnWidth[columnWithMaxWidth])
                	{
                    	columnWithMaxWidth = i;
                	}
                    fixedColumnsWidth += columnWidth[i];
                }
            }

            restWidth = (int) report.getPageDefinition().getWidth()
                    - fixedColumnsWidth;

            // shrink biggest column by 5 pixel
            if (restWidth < 150) {
                columnWidth[columnWithMaxWidth] -= 5;
            }

        } while (restWidth < 150);

        double columnParticipantWidth = 0.0;
        double columnNumberWidth = 0.0;

        int columnNumberIndex = JFritz.getJframe().getCallerTable().getVisibleColumnIndex(CallerTable.COLUMN_NUMBER); //$NON-NLS-1$
        if (columnNumberIndex != -1)
        {
        	columnNumberWidth = colModel.getColumn(columnNumberIndex)
                .getWidth();
        }

        int columnParticipantIndex = JFritz.getJframe().getCallerTable().getVisibleColumnIndex(CallerTable.COLUMN_PARTICIPANT); //$NON-NLS-1$
        if (columnParticipantIndex != -1)
        {
        	columnParticipantWidth = colModel.getColumn(
                columnParticipantIndex).getWidth();
        }

        if (columnNumberIndex != -1)
        {
        	columnWidth[columnNumberIndex] = (int) ((columnNumberWidth / (columnNumberWidth + columnParticipantWidth)) * restWidth);
        }
        if (columnParticipantIndex != -1)
        {
        	columnWidth[columnParticipantIndex] = (int) ((columnParticipantWidth / (columnNumberWidth + columnParticipantWidth)) * restWidth);
        }

        int startPos = 0;
        for (int i = 0; i < columnCount; i++) {
            columnStart[i] = startPos;
            startPos += columnWidth[i];
        }
    }

    private PageHeader createPageHeader(float pageWidth) {
        PageHeader pageHeader = new PageHeader();
        pageHeader.setName("PageHeader"); //$NON-NLS-1$

        FontDefinition font = new FontDefinition("Arial", 16, true, false, //$NON-NLS-1$
                false, false);
        TextElement label = LabelElementFactory.createLabelElement("JFritz", //$NON-NLS-1$
                new Rectangle2D.Float(0, 0, pageWidth, 40), Color.BLACK,
                ElementAlignment.CENTER, ElementAlignment.MIDDLE, font,
                ProgramConstants.PROGRAM_NAME + " - " + Main.getMessage("callerlist")); //$NON-NLS-1$,  //$NON-NLS-2$
        pageHeader.addElement(label);

        font = new FontDefinition("Arial", 8, true, false, false, false); //$NON-NLS-1$

        String columnName = ""; //$NON-NLS-1$
        for (int i = 0; i < JFritz.getJframe().getCallerTable()
                .getColumnCount(); i++) {
            columnName  =JFritz.getJframe().getCallerTable()
            .getTableHeader().getColumnModel().getColumn(i)
            .getHeaderValue().toString();
            label = LabelElementFactory.createLabelElement(JFritz
                    .getCallerList().getColumnName(i), new Rectangle2D.Float(
                    columnStart[i], 50, columnWidth[i], 20), Color.BLACK,
                    ElementAlignment.CENTER, ElementAlignment.MIDDLE, font,
                    columnName);
            Debug.debug("Column: " + columnName +  //$NON-NLS-1$
            		" Start: "+columnStart[i] + //$NON-NLS-1$
            		" Width: " + columnWidth[i]); //$NON-NLS-1$
            pageHeader.addElement(label);
            ShapeElement selement = StaticShapeElementFactory
                    .createRectangleShapeElement("back", Color.BLACK, //$NON-NLS-1$
                            new BasicStroke(0), new Rectangle2D.Float(
                                    columnStart[i], 50, columnWidth[i], 20),
                            true, false);
            pageHeader.addElement(selement);

        }

        pageHeader.setVisible(true);
        return pageHeader;
    }

    private PageFormat createDINA4PaperLandscape() {
        Paper a4Paper = new Paper();
        PageFormat pageFormat = new PageFormat();
        pageFormat.setOrientation(PageFormat.LANDSCAPE);

        /*
         * set size of paper sheet DIN A4 should be 8.26x11.69 inches
         */
        double paperWidth = 8.26;
        double paperHeight = 11.69;
        a4Paper.setSize(paperWidth * 72.0, paperHeight * 72.0);

        /*
         * set the margins respectively the imageable area
         */
        double leftMargin = 0.78; /* should be about 2cm */
        double rightMargin = 0.78;
        double topMargin = 0.78;
        double bottomMargin = 0.78;

        a4Paper.setImageableArea(leftMargin * 72.0, topMargin * 72.0,
                (paperWidth - leftMargin - rightMargin) * 72.0, (paperHeight
                        - topMargin - bottomMargin) * 72.0);
        pageFormat.setPaper(a4Paper);
        return pageFormat;
    }

    public JFreeReport createReportDefinition() {
        report = new JFreeReport();
        report.setName(ProgramConstants.PROGRAM_NAME + "-" + Main.getMessage("callerlist")); //$NON-NLS-1$,  //$NON-NLS-2$

        SimplePageDefinition pageDefinition = new SimplePageDefinition(
                createDINA4PaperLandscape());
        report.setPageDefinition(pageDefinition);

        Debug.debug("Creating column widths ..."); //$NON-NLS-1$
        createColumnWidths();

        report.setPageHeader(createPageHeader(report.getPageDefinition()
                .getWidth()));

        TextFieldElementFactory factory;

        final Image callInImage = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(
                        "/de/moonflower/jfritz/resources/images/callin.png")); //$NON-NLS-1$
        final Image callInFailedImage = Toolkit
                .getDefaultToolkit()
                .getImage(
                        getClass()
                                .getResource(
                                        "/de/moonflower/jfritz/resources/images/callinfailed.png")); //$NON-NLS-1$
        final Image callOutImage = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(
                        "/de/moonflower/jfritz/resources/images/callout.png")); //$NON-NLS-1$

        final Image emptyImage = Toolkit.getDefaultToolkit().getImage(
        		getClass().getResource(
        				"/de/moonflower/jfritz/resources/images/empty.png")); //$NON-NLS-1$

        // Set Font size to 8
        Integer fontSize = Integer.valueOf(8);

        Debug.debug("Adding fields ..."); //$NON-NLS-1$
        AbstractExpression exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {

                Object ob = getDataRow().get(
                        JFritz.getCallerList().getColumnName(0));
                if (ob == null)
                    return null;
                CallType callType = (CallType) ob;
                switch (callType.calltype) {
	                case CallType.CALLIN:
	                    return callInImage;
	                case CallType.CALLIN_FAILED:
	                    return callInFailedImage;
	                case CallType.CALLOUT:
	                    return callOutImage;
	                default:
	                    return null;
                }
            }
        };
        Debug.debug("Adding print field ..."); //$NON-NLS-1$
        exp.setName("print_type"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object ob = getDataRow().get(
                        JFritz.getCallerList().getColumnName(1));
                if (ob == null) {
                    return ""; //$NON-NLS-1$
                }
                return ob;
            }
        };
        Debug.debug("Adding date field ..."); //$NON-NLS-1$
        exp.setName("print_date"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object ob = getDataRow().get(
                        JFritz.getCallerList().getColumnName(2));
                if (ob == null) {
                    return ""; //$NON-NLS-1$
                }
                return ob;
            }
        };
        Debug.debug("Adding callbycall field ..."); //$NON-NLS-1$
        exp.setName("print_callbycall"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object number = getDataRow().get(
                        JFritz.getCallerList().getColumnName(3));
                if (number == null) {
                    return ""; //$NON-NLS-1$
                }
                return number.toString();
            }
        };
        Debug.debug("Adding number field ..."); //$NON-NLS-1$
        exp.setName("print_number"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object person = getDataRow().get(
                        JFritz.getCallerList().getColumnName(4));
                if (person == null)
                    return ""; //$NON-NLS-1$
                return ((Person) person).getFullname();
            }
        };
        Debug.debug("Adding name field ..."); //$NON-NLS-1$
        exp.setName("print_personname"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object obj = getDataRow().get(
                        JFritz.getCallerList().getColumnName(5));
                if (obj == null)
                    return ""; //$NON-NLS-1$
                Port port = (Port) obj;
                return port.getName();
            }
        };
        Debug.debug("Adding port field ..."); //$NON-NLS-1$
        exp.setName("print_port"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object obj = getDataRow().get(
                        JFritz.getCallerList().getColumnName(6));
                if (obj == null)
                    return ""; //$NON-NLS-1$
                String route = (String) obj;
                return route;
            }
        };
        Debug.debug("Adding route field ..."); //$NON-NLS-1$
        exp.setName("print_route"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object obj = getDataRow().get(
                        JFritz.getCallerList().getColumnName(7));
                if (obj == null)
                    return ""; //$NON-NLS-1$
                int duration = Integer.parseInt(obj.toString());
                return Integer.toString(duration / 60) + " min"; //$NON-NLS-1$
            }
        };
        Debug.debug("Adding duration field ..."); //$NON-NLS-1$
        exp.setName("print_duration"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object obj = getDataRow().get(
                        JFritz.getCallerList().getColumnName(8));
                if (obj == null)
                    return ""; //$NON-NLS-1$
                return obj.toString();
            }
        };
        Debug.debug("Adding comment field ..."); //$NON-NLS-1$
        exp.setName("print_comment"); //$NON-NLS-1$
        report.addExpression(exp);

        exp = new AbstractExpression() {
            /**
			 *
			 */
			private static final long serialVersionUID = 1L;

			public Object getValue() {
                Object obj = getDataRow().get(
                        JFritz.getCallerList().getColumnName(9));
                if (obj == null)
                    return emptyImage; //$NON-NLS-1$
                else
                {
	                if (ToolkitImage.class.isInstance(obj))
	                {
		                ToolkitImage image = (ToolkitImage)(obj);
		                if (image.getHeight() == -1 || image.getWidth() == -1)
		                {
		                	return emptyImage;
		                }
		                else
		                {
		                	return image;
		                }
	                }
	                else if (ImageIcon.class.isInstance(obj))
	                {
	                	ImageIcon imageIcon = (ImageIcon)(obj);
	                	if (imageIcon.getIconHeight() == -1 || imageIcon.getIconWidth() == -1)
	                	{
	                		return emptyImage;
	                	}
	                	else
	                	{
	                		return imageIcon.getImage();
	                	}
	                }
	                else
	                {
	                	return emptyImage;
	                }
                }
            }
        };
        Debug.debug("Adding picture field ..."); //$NON-NLS-1$
        exp.setName("print_picture"); //$NON-NLS-1$
        report.addExpression(exp);

        Debug.debug("Creating renderer ..."); //$NON-NLS-1$
        for (int i = 0; i < JFritz.getJframe().getCallerTable()
                .getColumnCount(); i++) {

            String columnName = JFritz.getJframe().getCallerTable()
                    .getColumnModel().getColumn(i).getIdentifier().toString();

            if (columnName.equals(CallerTable.COLUMN_TYPE)) { //$NON-NLS-1$
                ImageElement imageElement = ImageFieldElementFactory
                        .createImageDataRowElement("Type-Element", //$NON-NLS-1$
                                new Rectangle2D.Float(columnStart[i] + 2, 2,
                                        14, 14), "print_type", true, false); //$NON-NLS-1$
                imageElement.setDynamicContent(false);
                report.getItemBand().addElement(imageElement);
            } else if (columnName.equals(CallerTable.COLUMN_DATE)) { //$NON-NLS-1$
                factory = new DateFieldElementFactory();
                factory.setFontSize(fontSize);
                factory.setName(JFritz.getCallerList().getColumnName(i));
                factory
                        .setAbsolutePosition(new Point2D.Float(columnStart[i],
                                2));
                factory.setMinimumSize(new Dimension(columnWidth[i], 14));
                factory.setMaximumSize(new Dimension(100, 14));
                factory.setColor(Color.black);
                factory.setHorizontalAlignment(ElementAlignment.CENTER);
                factory.setVerticalAlignment(ElementAlignment.MIDDLE);
                factory.setNullString("-"); //$NON-NLS-1$
                factory.setFieldname("print_date"); //$NON-NLS-1$
                report.getItemBand().addElement(factory.createElement());
            } else if (columnName.equals(CallerTable.COLUMN_PICTURE)) {
            	ImageElement imageElement = ImageFieldElementFactory.createImageDataRowElement("Picture-Element",
            			new Rectangle2D.Float(columnStart[i] + 2, 2, 45, 50), "print_picture", true, false);
            	imageElement.setDynamicContent(false);
            	report.getItemBand().addElement(imageElement);
            } else {
                factory = new TextFieldElementFactory();
                factory.setFontSize(fontSize);
                factory.setName(JFritz.getCallerList().getColumnName(i));
                factory
                        .setAbsolutePosition(new Point2D.Float(columnStart[i],
                                2));
                factory.setMinimumSize(new Dimension(columnWidth[i], 14));
                factory.setColor(Color.BLACK);
                factory.setHorizontalAlignment(ElementAlignment.CENTER);
                factory.setVerticalAlignment(ElementAlignment.MIDDLE);
                factory.setNullString(" "); //$NON-NLS-1$
                if (columnName.equals(CallerTable.COLUMN_CALL_BY_CALL)) { //$NON-NLS-1$
                    factory.setFieldname("print_callbycall"); //$NON-NLS-1$
                } else if (columnName.equals(CallerTable.COLUMN_NUMBER)) { //$NON-NLS-1$
                    factory.setFieldname("print_number"); //$NON-NLS-1$
                } else if (columnName.equals(CallerTable.COLUMN_PARTICIPANT)) { //$NON-NLS-1$
                    factory.setFieldname("print_personname"); //$NON-NLS-1$
                } else if (columnName.equals(CallerTable.COLUMN_PORT)) { //$NON-NLS-1$
                    factory.setFieldname("print_port"); //$NON-NLS-1$
                } else if (columnName.equals(CallerTable.COLUMN_ROUTE)) { //$NON-NLS-1$
                    factory.setFieldname("print_route"); //$NON-NLS-1$
                } else if (columnName.equals(CallerTable.COLUMN_DURATION)) { //$NON-NLS-1$
                    factory.setFieldname("print_duration"); //$NON-NLS-1$
                } else if (columnName.equals(CallerTable.COLUMN_COMMENT)) { //$NON-NLS-1$
                    factory.setFieldname("print_comment"); //$NON-NLS-1$
                }
                report.getItemBand().addElement(factory.createElement());
            }

            // Rand zeichnen
            Debug.debug("Print border ..."); //$NON-NLS-1$
            Rectangle2D.Float border;
            if (JFritzUtils.parseBoolean(Main.getProperty("option.showCallerListColumn."+CallerTable.COLUMN_PICTURE)))
            {
            	border = new Rectangle2D.Float(columnStart[i], 0,
                        columnWidth[i], 55);
            }
            else
            {
            	border = new Rectangle2D.Float(columnStart[i], 0,
                        columnWidth[i], 18);
            }
            ShapeElement selement = StaticShapeElementFactory
                    .createRectangleShapeElement("back", Color //$NON-NLS-1$
                            .decode("#000000"), new BasicStroke(0), //$NON-NLS-1$
                            border , true, false);
            report.getItemBand().addElement(selement);
        }

        return report;
    }

    public void run() {
    	print();
    	JFritz.getJframe().setStatus("");
    }

    private void print() {
        Debug.info("Start print report creation"); //$NON-NLS-1$
        JFreeReport report = createReportDefinition();
        report.setData(JFritz.getCallerList());
        try {
            PreviewFrame preview = new PreviewFrame(report);
            preview.pack();
            preview.setVisible(true);
        } catch (ReportProcessingException e) {
            Debug.error("Failed to generate report " + e); //$NON-NLS-1$
        }
    }
}
