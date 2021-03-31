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

import org.apache.log4j.Logger;

import org.jdesktop.swingx.autocomplete.AutoCompleteDecorator;
import org.jdesktop.swingx.autocomplete.ObjectToStringConverter;

import java.awt.Component;
import java.awt.Container;
import java.awt.EventQueue;

import java.util.HashSet;

import javax.swing.AbstractCellEditor;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.table.TableCellEditor;

import de.cismet.cids.editors.DefaultBindableReferenceCombo;

import de.cismet.cids.tools.CidsBeanFilter;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.FeatureServiceAttribute;
import de.cismet.cismap.commons.gui.attributetable.AttributeTable;
import de.cismet.cismap.commons.gui.featureinfopanel.FeatureInfoPanel;
import de.cismet.cismap.commons.tools.FeatureTools;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class CidsLayerReferencedComboEditor extends AbstractCellEditor implements TableCellEditor {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(CidsLayerReferencedComboEditor.class);

    //~ Instance fields --------------------------------------------------------

    private DefaultCidsLayerBindableReferenceCombo combo = new DefaultCidsLayerBindableReferenceCombo();
    private CidsLayerFeatureFilter filter;
    private FeatureServiceAttribute attr;
    private boolean useAutoCompleteDecorator = true;
    private ListCellRenderer listRenderer = null;
    private boolean nullable = false;
    private HashSet<DefaultCidsLayerBindableReferenceCombo> configuredCombos =
        new HashSet<DefaultCidsLayerBindableReferenceCombo>();
    private String sortingColumn = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsLayerReferencedComboEditor object.
     *
     * @param  attr  The attribute, this editor will be used for.
     */
    public CidsLayerReferencedComboEditor(final FeatureServiceAttribute attr) {
        this(attr, null);
    }

    /**
     * Creates a new CidsLayerReferencedComboEditor object.
     *
     * @param  attr                      The attribute, this editor will be used for.
     * @param  useAutoCompleteDecorator  DOCUMENT ME!
     */
    public CidsLayerReferencedComboEditor(final FeatureServiceAttribute attr, final boolean useAutoCompleteDecorator) {
        this(attr, useAutoCompleteDecorator, null);
    }

    /**
     * Creates a new CidsLayerReferencedComboEditor object.
     *
     * @param  attr    The attribute, this editor will be used for.
     * @param  filter  DOCUMENT ME!
     */
    public CidsLayerReferencedComboEditor(final FeatureServiceAttribute attr, final CidsLayerFeatureFilter filter) {
        this(attr, true, filter);
    }

    /**
     * Creates a new CidsLayerReferencedComboEditor object.
     *
     * @param  attr                      The attribute, this editor will be used for.
     * @param  useAutoCompleteDecorator  DOCUMENT ME!
     * @param  filter                    DOCUMENT ME!
     */
    public CidsLayerReferencedComboEditor(final FeatureServiceAttribute attr,
            final boolean useAutoCompleteDecorator,
            final CidsLayerFeatureFilter filter) {
        this.filter = filter;
        this.useAutoCompleteDecorator = useAutoCompleteDecorator;
        this.attr = attr;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Object getCellEditorValue() {
        final Object value = combo.getSelectedItem();

        if (value == null) {
            return "";
        } else {
            try {
                return FeatureTools.convertObjectToClass(combo.getSelectedItem(), FeatureTools.getClass(attr));
            } catch (Exception e) {
                LOG.error("Cannot convert the given object.", e);
                return null;
            }
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
                combo = cidsFeature.getCatalogueCombo(attr.getName());
            }
        } else if (o.getParent() instanceof FeatureInfoPanel) {
            final FeatureInfoPanel infoPanel = (FeatureInfoPanel)o.getParent();
            final FeatureServiceFeature feature = infoPanel.getSelectedFeature();

            if (feature instanceof CidsLayerFeature) {
                final CidsLayerFeature cidsFeature = (CidsLayerFeature)feature;
                combo = cidsFeature.getCatalogueCombo(attr.getName());
            }
        }

        if ((combo != null) && !configuredCombos.contains(combo)) {
            combo.setNullable(nullable);
            combo.setBeanFilter(filter);
            if (sortingColumn != null) {
                combo.setSortingColumn(sortingColumn);
            }

            if (useAutoCompleteDecorator) {
                AutoCompleteDecorator.decorate(combo, new ObjectToStringConverter() {

                        @Override
                        public String getPreferredStringForItem(final Object o) {
                            if (o == null) {
                                return "";
                            } else {
                                return o.toString();
                            }
                        }
                    });
            }

            if (getListRenderer() != null) {
                combo.setRenderer(getListRenderer());
            }

            configuredCombos.add(combo);
        }

        EventQueue.invokeLater(new Thread("RequestFocus") {

                @Override
                public void run() {
                    // When the tab key was used to enter a cell editor, the focus must be requested to allow the user
                    // to instantly edit
                    combo.requestFocusInWindow();
                }
            });

        return combo;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the listRenderer
     */
    public ListCellRenderer getListRenderer() {
        return listRenderer;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  listRenderer  the listRenderer to set
     */
    public void setListRenderer(final ListCellRenderer listRenderer) {
        this.listRenderer = listRenderer;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the nullable
     */
    public boolean isNullable() {
        return nullable;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  nullable  the nullable to set
     */
    public void setNullable(final boolean nullable) {
        this.nullable = nullable;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the sortingColumn
     */
    public String getSortingColumn() {
        return sortingColumn;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  sortingColumn  the sortingColumn to set
     */
    public void setSortingColumn(final String sortingColumn) {
        this.sortingColumn = sortingColumn;
    }
}
