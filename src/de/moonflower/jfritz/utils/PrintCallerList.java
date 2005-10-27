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
        int columnCount = jfritz.getCallerlist().getColumnCount();
        columnWidth = new int[columnCount];
        columnStart = new int[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnWidth[i] = 0;
            columnStart[i] = 0;
        }

        int startPos = 0;
        int endPos = (int) report.getPageDefinition().getWidth();
        int columnOffset = 0;

        // Die ersten Spaltenbreiten bestimmen (für Typ, Datum, CallByCall)
        for (int i = 0; i < 2; i++) {
            columnWidth[i] = jfritz.getJframe().getCallerTable()
                    .getColumnModel().getColumn(i).getWidth();
            columnStart[i] = startPos;
            startPos += columnWidth[i];
        }
        if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showCallByCall", "false"))) {
            columnWidth[2] = jfritz.getJframe().getCallerTable()
                    .getColumnModel().getColumn(2).getWidth() - 20;
            columnStart[2] = startPos;
            startPos += columnWidth[2];
        } else {
            columnOffset = 1;
        }

        // Die letzen Spaltenbreiten bestimmen (für Kosten, Dauer, Route, Port)
        for (int i = columnCount - 1; i > 4; i--) {
            columnWidth[i] = jfritz.getJframe().getCallerTable()
                    .getColumnModel().getColumn(i - columnOffset).getWidth() - 20;
            columnStart[i] = endPos - columnWidth[i];
            endPos -= columnWidth[i];
        }

        // Spaltenbreiten von Rufnummer und Kontakt dynamisch gestalten
        int restWidth = endPos - startPos;
        // Spaltenbreite Rufnummer in Anrufliste
        int columnWidthNumber = jfritz.getJframe().getCallerTable()
                .getColumnModel().getColumn(3 - columnOffset).getWidth();
        // Spaltenbreite Name in Anrufliste
        int columnWidthName = jfritz.getJframe().getCallerTable()
                .getColumnModel().getColumn(4 - columnOffset).getWidth();

        // Spaltenbreite Rufnummer Drucken
        columnWidth[3] = (int) (restWidth * ((float) columnWidthNumber / (float) (columnWidthNumber + columnWidthName)));
        columnStart[3] = startPos;

        // Spaltenbreite Rufnummer Name
        columnStart[4] = startPos + columnWidth[3];
        columnWidth[4] = columnStart[5] - columnStart[4];
        System.err.println("RestWidth: " + restWidth);
        for (int i = 0; i < columnCount; i++) {
            System.err.println("Spalte " + i + ": " + columnStart[i]
                    + " Breite " + columnWidth[i]);
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
                "JFritz! - Anrufliste");
        pageHeader.addElement(label);

        font = new FontDefinition("Arial", 8, true, false, false, false);

        for (int i = 0; i < jfritz.getJframe().getCallerTable().getTableHeader().getColumnModel().getColumnCount(); i++) {
            System.err.println(jfritz.getJframe().getCallerTable().getTableHeader().getColumnModel().getColumn(i).getHeaderValue().toString());
            int columnOffset = 0;
            if ((!JFritzUtils.parseBoolean(JFritz.getProperty(
                    "option.showCallByCall", "false"))) && (i > 1)) {
                columnOffset = 1;
            }
            label = LabelElementFactory.createLabelElement(jfritz
                    .getCallerlist().getColumnName(i), new Rectangle2D.Float(
                    columnStart[i+columnOffset], 50, columnWidth[i+columnOffset], 20), Color.BLACK,
                    ElementAlignment.CENTER, ElementAlignment.MIDDLE, font,
                    jfritz.getJframe().getCallerTable().getTableHeader().getColumnModel().getColumn(i).getHeaderValue().toString());
            System.err.println(columnStart[i+columnOffset] + " " + columnWidth[i+columnOffset]);
            pageHeader.addElement(label);
            ShapeElement selement = StaticShapeElementFactory
                    .createRectangleShapeElement("back", Color.BLACK,
                            new BasicStroke(0), new Rectangle2D.Float(
                                    columnStart[i+columnOffset], 50, columnWidth[i+columnOffset], 20),
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
        report.setName("JFritz!  -  FRITZ!Box Anrufliste");

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

        // Set Font size to 8
        Integer fontSize = new Integer(8);
        ImageElement imageElement = ImageFieldElementFactory
                .createImageDataRowElement("Type-Element",
                        new Rectangle2D.Float(columnStart[0] + 2, 2, 14, 14),
                        "print_type", true, false);
        imageElement.setDynamicContent(false);
        report.getItemBand().addElement(imageElement);
        ShapeElement selement = StaticShapeElementFactory
                .createRectangleShapeElement("back", Color.decode("#000000"),
                        new BasicStroke(0), new Rectangle2D.Float(
                                columnStart[0], 0, columnWidth[0], 18), true,
                        false);
        report.getItemBand().addElement(selement);

        // Drucke Datum
        factory = new DateFieldElementFactory();
        factory.setFontSize(fontSize);
        factory.setName(jfritz.getCallerlist().getColumnName(1));
        factory.setAbsolutePosition(new Point2D.Float(columnStart[1], 2));
        factory.setMinimumSize(new Dimension(columnWidth[1], 14));
        factory.setMaximumSize(new Dimension(100, 14));
        factory.setColor(Color.black);
        factory.setHorizontalAlignment(ElementAlignment.CENTER);
        factory.setVerticalAlignment(ElementAlignment.MIDDLE);
        factory.setNullString("-");
        factory.setFieldname(jfritz.getCallerlist().getColumnName(1));
        report.getItemBand().addElement(factory.createElement());
        selement = StaticShapeElementFactory.createRectangleShapeElement(
                "back", Color.BLACK, new BasicStroke(0), new Rectangle2D.Float(
                        columnStart[1], 0, columnWidth[1], 18), true, false);
        report.getItemBand().addElement(selement);

        if (JFritzUtils.parseBoolean(JFritz.getProperty(
                "option.showCallByCall", "false"))) {
            // Drucke CallByCall
            factory = new TextFieldElementFactory();
            factory.setFontSize(fontSize);
            factory.setName(jfritz.getCallerlist().getColumnName(2));
            factory.setAbsolutePosition(new Point2D.Float(columnStart[2], 2));
            factory.setMinimumSize(new Dimension(columnWidth[2], 14));
            factory.setColor(Color.BLACK);
            factory.setHorizontalAlignment(ElementAlignment.CENTER);
            factory.setVerticalAlignment(ElementAlignment.MIDDLE);
            factory.setNullString(" ");
            factory.setFieldname(jfritz.getCallerlist().getColumnName(2));
            report.getItemBand().addElement(factory.createElement());
            selement = StaticShapeElementFactory
                    .createRectangleShapeElement("back", Color.BLACK,
                            new BasicStroke(0), new Rectangle2D.Float(
                                    columnStart[2], 0, columnWidth[2], 18),
                            true, false);
            report.getItemBand().addElement(selement);
        }

        // Drucke Rufnummer
        exp = new AbstractExpression() {
            public Object getValue() {
                Object number = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(3));
                return number.toString();
            };
        };
        exp.setName("print_number");
        report.addExpression(exp);

        // Drucke Person (Fullname)
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

        // Drucke Port
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

        // Drucke Route
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

        // Drucke Dauer
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

        // Drucke Kosten
        exp = new AbstractExpression() {
            public Object getValue() {
                Object obj = getDataRow().get(
                        jfritz.getCallerlist().getColumnName(8));
                if (obj == null)
                    return null;
                if (Double.parseDouble(obj.toString()) == -1) {
                    return "Unbekannt";
                } else if (Double.parseDouble(obj.toString()) == -2) {
                    return "Freiminuten";
                } else
                    return obj.toString() + " ct";
            };
        };
        exp.setName("print_cost");
        report.addExpression(exp);

        for (int i = 3; i < jfritz.getCallerlist().getColumnCount(); i++) {
            factory = new TextFieldElementFactory();
            factory.setFontSize(fontSize);
            factory.setName(jfritz.getCallerlist().getColumnName(i));
            factory.setAbsolutePosition(new Point2D.Float(columnStart[i], 2));
            factory.setMinimumSize(new Dimension(columnWidth[i], 14));
            factory.setColor(Color.black);
            factory.setHorizontalAlignment(ElementAlignment.CENTER);
            factory.setVerticalAlignment(ElementAlignment.MIDDLE);
            factory.setNullString(" ");
            switch (i) {
            case 3:
                factory.setFieldname("print_number");
                break;
            case 4:
                factory.setFieldname("print_personname");
                break;
            case 5:
                factory.setFieldname("print_port");
                break;
            case 6:
                factory.setFieldname("print_route");
                break;
            case 7:
                factory.setFieldname("print_duration");
                break;
            case 8:
                factory.setFieldname("print_cost");
                break;
            default: {
            }
            }
            report.getItemBand().addElement(factory.createElement());
            selement = StaticShapeElementFactory
                    .createRectangleShapeElement("back", Color
                            .decode("#000000"), new BasicStroke(0),
                            new Rectangle2D.Float(columnStart[i], 0,
                                    columnWidth[i], 18), true, false);
            report.getItemBand().addElement(selement);
        }

        return report;
    }

    public void print() {
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
