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

import de.moonflower.jfritz.JFritz;
import de.moonflower.jfritz.struct.CallType;
import de.moonflower.jfritz.struct.Person;

/**
 * @author Robert Palmer
 *
 */
public class PrintCallerList {

    private JFritz jfritz;

    private int[] columnWidth;

    private int[] columnStart;

    private JFreeReport report;

    public PrintCallerList(JFritz jfritz) {
        this.jfritz = jfritz;
    }

    private void createColumnWidths() {
        Debug.msg("Create Columns");
        int columnCount = jfritz.getJframe().getCallerTable().getColumnCount();
        int fixedColumnsWidth = 0; // width of all columns except "number" and
        // "participant"
        TableColumnModel colModel = jfritz.getJframe().getCallerTable()
                .getColumnModel();
        columnWidth = new int[columnCount];
        columnStart = new int[columnCount];

        for (int i = 0; i < columnCount; i++) {
            String columnName = colModel.getColumn(i).getHeaderValue()
                    .toString();
            if (columnName.equals(JFritz.getMessage("type"))) {
                // Icon, same Width
                columnWidth[i] = colModel.getColumn(i).getWidth() - 10;
                // Minimum size of type-column (icon)
                if (columnWidth[i] < 20)
                    columnWidth[i] = 20;
            } else {
                // smaller font size: reduce width
                columnWidth[i] = colModel.getColumn(i).getWidth() - 20;
            }
            columnStart[i] = 0;
        }

        // Rest width for columns "number" and "participant"
        // ensure, that restWidth >= 150
        int restWidth = 0;
        do {
            fixedColumnsWidth = 0;
            int columnWithMaxWidth = 0;
            for (int i = 0; i < columnCount; i++) {
                String columnName = colModel.getColumn(i).getHeaderValue()
                .toString();
                System.err.println(columnName);
                if (!(columnName.equals(JFritz.getMessage("number")))
                        && (!columnName.equals(JFritz.getMessage("participant")))) {
                    if (columnWidth[i] > columnWidth[columnWithMaxWidth]) columnWithMaxWidth = i;
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

        int columnNumberIndex = colModel.getColumnIndex(JFritz
                .getMessage("number"));
        double columnNumberWidth = colModel.getColumn(columnNumberIndex)
                .getWidth();
        int columnParticipantIndex = colModel.getColumnIndex(JFritz
                .getMessage("participant"));
        double columnParticipantWidth = colModel.getColumn(
                columnParticipantIndex).getWidth();

        columnWidth[columnNumberIndex] = (int) ((columnNumberWidth / (columnNumberWidth + columnParticipantWidth)) * restWidth);
        columnWidth[columnParticipantIndex] = (int) ((columnParticipantWidth / (columnNumberWidth + columnParticipantWidth)) * restWidth);

        int startPos = 0;
        for (int i = 0; i < columnCount; i++) {
            columnStart[i] = startPos;
            startPos += columnWidth[i];
        }
    }

    private PageHeader createPageHeader(float pageWidth) {
        PageHeader pageHeader = new PageHeader();
        pageHeader.setName("PageHeader");

        FontDefinition font = new FontDefinition("Arial", 16, true, false,
                false, false);
        TextElement label = LabelElementFactory.createLabelElement("JFritz",
                new Rectangle2D.Float(0, 0, pageWidth, 40), Color.BLACK,
                ElementAlignment.CENTER, ElementAlignment.MIDDLE, font,
                "JFritz - Anrufliste");
        pageHeader.addElement(label);

        font = new FontDefinition("Arial", 8, true, false, false, false);

        for (int i = 0; i < jfritz.getJframe().getCallerTable()
                .getColumnCount(); i++) {
            System.err.println(jfritz.getJframe().getCallerTable()
                    .getTableHeader().getColumnModel().getColumn(i)
                    .getHeaderValue().toString());
            label = LabelElementFactory.createLabelElement(jfritz
                    .getCallerlist().getColumnName(i), new Rectangle2D.Float(
                    columnStart[i], 50, columnWidth[i], 20), Color.BLACK,
                    ElementAlignment.CENTER, ElementAlignment.MIDDLE, font,
                    jfritz.getJframe().getCallerTable().getTableHeader()
                            .getColumnModel().getColumn(i).getHeaderValue()
                            .toString());
            System.err.println(columnStart[i] + " " + columnWidth[i]);
            pageHeader.addElement(label);
            ShapeElement selement = StaticShapeElementFactory
                    .createRectangleShapeElement("back", Color.BLACK,
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
        report.setName("JFritz-Anrufliste");

        SimplePageDefinition pageDefinition = new SimplePageDefinition(
                createDINA4PaperLandscape());
        report.setPageDefinition(pageDefinition);

        createColumnWidths();

        report.setPageHeader(createPageHeader(report.getPageDefinition()
                .getWidth()));

        TextFieldElementFactory factory;

        final Image callInImage = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(
                        "/de/moonflower/jfritz/resources/images/callin.png"));
        final Image callInFailedImage = Toolkit
                .getDefaultToolkit()
                .getImage(
                        getClass()
                                .getResource(
                                        "/de/moonflower/jfritz/resources/images/callinfailed.png"));
        final Image callOutImage = Toolkit.getDefaultToolkit().getImage(
                getClass().getResource(
                        "/de/moonflower/jfritz/resources/images/callout.png"));

        // Set Font size to 8
        Integer fontSize = new Integer(8);

        AbstractExpression exp = new AbstractExpression() {
            public Object getValue() {

                Object ob = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(0));
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
            };
        };
        exp.setName("print_type");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object ob = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(1));
                return ob;
            }
        };
        exp.setName("print_date");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object ob = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(2));
                return ob;
            }
        };
        exp.setName("print_callbycall");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object number = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(3));
                return number.toString();
            };
        };
        exp.setName("print_number");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object person = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(4));
                if (person == null)
                    return null;
                return ((Person) person).getFullname();
            };
        };
        exp.setName("print_personname");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object obj = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(5));
                if (obj == null)
                    return null;
                String port = (String) obj;
                String portStr = "";
                if (port.equals("4"))
                    portStr = "ISDN";
                else if (port.equals("0"))
                    portStr = "FON 1";
                else if (port.equals("1"))
                    portStr = "FON 2";
                else if (port.equals("2"))
                    portStr = "FON 3";
                else if (port.equals(""))
                    portStr = "";
                else
                    portStr = "Port " + port;
                return portStr;
            };
        };
        exp.setName("print_port");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object obj = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(6));
                if (obj == null)
                    return null;
                String route = (String) obj;
                return route;
            };
        };
        exp.setName("print_route");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object obj = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(7));
                if (obj == null)
                    return null;
                int duration = Integer.parseInt(obj.toString());
                return Integer.toString(duration / 60) + " min";
            };
        };
        exp.setName("print_duration");
        report.addExpression(exp);

        exp = new AbstractExpression() {
            public Object getValue() {
                Object obj = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(8));
                if (obj == null)
                    return "";
                return obj.toString();
            };
        };
        exp.setName("print_comment");
        report.addExpression(exp);

        /**
         * // Drucke Kosten exp = new AbstractExpression() { public Object
         * getValue() { Object obj = getDataRow().get(
         * jfritz.getCallerlist().getColumnName(8)); if (obj == null) return
         * null; if (Double.parseDouble(obj.toString()) == -1) { return
         * "Unbekannt"; } else if (Double.parseDouble(obj.toString()) == -2) {
         * return "Freiminuten"; } else return obj.toString() + " ct"; }; };
         * exp.setName("print_cost"); report.addExpression(exp);
         */

        for (int i = 0; i < jfritz.getJframe().getCallerTable()
                .getColumnCount(); i++) {

            String columnName = jfritz.getJframe().getCallerTable()
                    .getColumnModel().getColumn(i).getHeaderValue().toString();

            if (columnName.equals(JFritz.getMessage("type"))) {
                ImageElement imageElement = ImageFieldElementFactory
                        .createImageDataRowElement("Type-Element",
                                new Rectangle2D.Float(columnStart[i] + 2, 2,
                                        14, 14), "print_type", true, false);
                imageElement.setDynamicContent(false);
                report.getItemBand().addElement(imageElement);
            } else if (columnName.equals(JFritz.getMessage("date"))) {
                factory = new DateFieldElementFactory();
                factory.setFontSize(fontSize);
                factory.setName(jfritz.getCallerlist().getColumnName(i));
                factory
                        .setAbsolutePosition(new Point2D.Float(columnStart[i],
                                2));
                factory.setMinimumSize(new Dimension(columnWidth[i], 14));
                factory.setMaximumSize(new Dimension(100, 14));
                factory.setColor(Color.black);
                factory.setHorizontalAlignment(ElementAlignment.CENTER);
                factory.setVerticalAlignment(ElementAlignment.MIDDLE);
                factory.setNullString("-");
                factory.setFieldname("print_date");
                report.getItemBand().addElement(factory.createElement());
            } else {
                factory = new TextFieldElementFactory();
                factory.setFontSize(fontSize);
                factory.setName(jfritz.getCallerlist().getColumnName(i));
                factory
                        .setAbsolutePosition(new Point2D.Float(columnStart[i],
                                2));
                factory.setMinimumSize(new Dimension(columnWidth[i], 14));
                factory.setColor(Color.BLACK);
                factory.setHorizontalAlignment(ElementAlignment.CENTER);
                factory.setVerticalAlignment(ElementAlignment.MIDDLE);
                factory.setNullString(" ");
                if (columnName.equals("Call-By-Call")) {
                    factory.setFieldname("print_callbycall");
                } else if (columnName.equals(JFritz.getMessage("number"))) {
                    factory.setFieldname("print_number");
                } else if (columnName.equals(JFritz.getMessage("participant"))) {
                    factory.setFieldname("print_personname");
                } else if (columnName.equals(JFritz.getMessage("port"))) {
                    factory.setFieldname("print_port");
                } else if (columnName.equals(JFritz.getMessage("route"))) {
                    factory.setFieldname("print_route");
                } else if (columnName.equals(JFritz.getMessage("duration"))) {
                    factory.setFieldname("print_duration");
                } else if (columnName.equals("Kommentar")) {
                    factory.setFieldname("print_comment");
                }
                report.getItemBand().addElement(factory.createElement());
            }

            // Rand zeichnen
            ShapeElement selement = StaticShapeElementFactory
                    .createRectangleShapeElement("back", Color
                            .decode("#000000"), new BasicStroke(0),
                            new Rectangle2D.Float(columnStart[i], 0,
                                    columnWidth[i], 18), true, false);
            report.getItemBand().addElement(selement);
        }

        return report;
    }

    public void print() {
        Debug.msg("Start report creation");
        JFreeReport report = createReportDefinition();
        report.setData(jfritz.getCallerlist());
        try {
            PreviewFrame preview = new PreviewFrame(report);
            preview.pack();
            preview.setVisible(true);
        } catch (ReportProcessingException e) {
            Debug.err("Failed to generate report " + e);
        }
    }
}
