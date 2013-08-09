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

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import de.cismet.cismap.commons.LayerConfig;

/**
 * DOCUMENT ME!
 *
 * @author   mroncoroni
 * @version  $Revision$, $Date$
 */
public class CidsCapabilitesTreeCellRenderer extends DefaultTreeCellRenderer {

    //~ Instance fields --------------------------------------------------------

    private final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(this.getClass());
    private javax.swing.ImageIcon layersIcon;
    private javax.swing.ImageIcon layerIcon;
    private javax.swing.ImageIcon layersInfoIcon;
    private javax.swing.ImageIcon layerInfoIcon;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new CidsCapabilitesTreeCellRenderer object.
     */
    public CidsCapabilitesTreeCellRenderer() {
        layersIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers.png"));   // NOI18N
        layerIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layer.png"));    // NOI18N
        layersInfoIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layers_i.png")); // NOI18N
        layerInfoIcon = new javax.swing.ImageIcon(getClass().getResource(
                    "/de/cismet/cismap/commons/raster/wms/res/layer_i.png"));  // NOI18N
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public Component getTreeCellRendererComponent(final JTree tree,
            final Object value,
            final boolean isSelected,
            final boolean expanded,
            final boolean leaf,
            final int row,
            final boolean hasFocus) {
        super.getTreeCellRendererComponent(
            tree,
            value,
            isSelected,
            expanded,
            leaf,
            row,
            hasFocus);
        // setForeground(Color.black);

        if (value instanceof LayerConfig) {
            final LayerConfig tmpLayer = (LayerConfig)value;

            setText(tmpLayer.getTitle());
            setIcon(layerInfoIcon);
        } else if (value instanceof String) {
            setIcon(layersIcon);
        }

        return this;
    }
}
