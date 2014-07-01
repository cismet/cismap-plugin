/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.linearreferencing;

import java.awt.Component;
import java.awt.Container;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StationTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    //~ Instance fields --------------------------------------------------------

    private TableStationEditor stat = new TableStationEditor();
    private String columnName;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationTableCellEditor object.
     *
     * @param  columnName  DOCUMENT ME!
     */
    public StationTableCellEditor(final String columnName) {
        this.columnName = columnName;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTableCellEditorComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column) {
//        AttributeTable a = (AttributeTable)table;
//        table.getModel();
        Container o = table;

        while ((o.getParent() != null) && !(o.getParent() instanceof AttributeTable)) {
            o = o.getParent();
        }

        if (o.getParent() instanceof AttributeTable) {
            final AttributeTable tab = (AttributeTable)o.getParent();
            final FeatureServiceFeature feature = tab.getFeatureByRow(row);

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                return cidsFeature.getStationEditor(columnName);
            } else {
                return stat;
            }
        } else {
            return stat;
        }
    }

    @Override
    public Object getCellEditorValue() {
        return stat.getValue();
    }
}
