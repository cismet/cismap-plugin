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
package de.cismet.cismap.cidslayer;

import java.awt.Component;
import java.awt.Container;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import de.cismet.cids.editors.DefaultBindableReferenceCombo;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanel;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerReferencedComboEditor extends AbstractCellEditor implements TableCellEditor {

    //~ Instance fields --------------------------------------------------------

    private DefaultBindableReferenceCombo combo = new DefaultBindableReferenceCombo();
    private String colName;
    private CidsBeanFilter filter;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerReferencedComboEditor object.
     *
     * @param  colName  DOCUMENT ME!
     */
    public CidsLayerReferencedComboEditor(final String colName) {
        this(colName, null);
    }

    /**
     * Creates a new CidsLayerReferencedComboEditor object.
     *
     * @param  colName  DOCUMENT ME!
     * @param  filter   DOCUMENT ME!
     */
    public CidsLayerReferencedComboEditor(final String colName, final CidsBeanFilter filter) {
        this.colName = colName;
        this.filter = filter;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getCellEditorValue() {
        final Object value = combo.getSelectedItem();

        if (value == null) {
            return "";
        } else {
            return String.valueOf(combo.getSelectedItem());
        }
    }

    @Override
    public Component getTableCellEditorComponent(final JTable table,
            final Object value,
            final boolean isSelected,
            final int row,
            final int column) {
        Container o = table;

        while ((o.getParent() != null)
                    && (!(o.getParent() instanceof AttributeTable) && !(o.getParent() instanceof FeatureInfoPanel))) {
            o = o.getParent();
        }

        if (o.getParent() instanceof AttributeTable) {
            final AttributeTable tab = (AttributeTable)o.getParent();
            final FeatureServiceFeature feature = tab.getFeatureByRow(row);

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                combo = cidsFeature.getCatalogueCombo(colName);
            }
        } else if (o.getParent() instanceof FeatureInfoPanel) {
            final FeatureInfoPanel infoPanel = (FeatureInfoPanel)o.getParent();
            final FeatureServiceFeature feature = infoPanel.getSelectedFeature();

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                combo = cidsFeature.getCatalogueCombo(colName);
            }
        }

//        for (int i = 0; i < combo.getItemCount(); ++i) {
//            Object item = combo.getItemAt(i);
//            if ((item == null && value == null) || (item != null && item.toString().equals(value))) {
//                combo.setSelectedIndex(i);
//                break;
//            }
//        }

        return combo;
    }
}
