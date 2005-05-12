/*
 * Created on 09.04.2005
 *
 */
package de.moonflower.jfritz;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

/**
 * This class manages tool tips on the columns of the table header.
 * @author Arno Willig
 *
 */
public class ColumnHeaderToolTips extends MouseMotionAdapter {
    // Current column whose tooltip is being displayed.
    // This variable is used to minimize the calls to setToolTipText().
    TableColumn curCol;

    // Maps TableColumn objects to tooltips
    Map tips = new HashMap();

    // If tooltip is null, removes any tooltip text.
    public void setToolTip(TableColumn col, String tooltip) {
        if (tooltip == null) {
            tips.remove(col);
        } else {
            tips.put(col, tooltip);
        }
    }

    public void mouseMoved(MouseEvent evt) {
        TableColumn col = null;
        JTableHeader header = (JTableHeader)evt.getSource();
        JTable table = header.getTable();
        TableColumnModel colModel = table.getColumnModel();
        int vColIndex = colModel.getColumnIndexAtX(evt.getX());

        // Return if not clicked on any column header
        if (vColIndex >= 0) {
            col = colModel.getColumn(vColIndex);
        }

        if (col != curCol) {
            header.setToolTipText((String)tips.get(col));
            curCol = col;
        }
    }
}