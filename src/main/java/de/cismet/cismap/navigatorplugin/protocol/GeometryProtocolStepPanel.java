/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.cismet.cismap.navigatorplugin.protocol;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.XBoundingBox;
import de.cismet.cismap.commons.features.SearchFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.layerwidget.ActiveLayerModel;
import de.cismet.cismap.commons.interaction.CismapBroker;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWMS;
import de.cismet.cismap.commons.raster.wms.simple.SimpleWmsGetMapUrl;

import de.cismet.commons.gui.protocol.AbstractProtocolStepPanel;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class GeometryProtocolStepPanel extends AbstractProtocolStepPanel<GeometryProtocolStep> {

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private org.jdesktop.swingx.JXHyperlink jXHyperlink1;
    private de.cismet.cismap.commons.gui.MappingComponent mappingComponent1;
    // End of variables declaration//GEN-END:variables

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new GeomSearchProtocolStepPanel object.
     */
    public GeometryProtocolStepPanel() {
        this(null);
    }

    /**
     * Creates a new GeomSearchProtocolStepPanel object.
     *
     * @param  geomSearchProtocolStep  DOCUMENT ME!
     */
    public GeometryProtocolStepPanel(final GeometryProtocolStep geomSearchProtocolStep) {
        this(geomSearchProtocolStep, true);
    }

    /**
     * Creates new form GeomSearchProtocolStepPanel.
     *
     * @param  geomSearchProtocolStep  DOCUMENT ME!
     * @param  showSearchGeometry      DOCUMENT ME!
     */
    public GeometryProtocolStepPanel(final GeometryProtocolStep geomSearchProtocolStep,
            final boolean showSearchGeometry) {
        super(geomSearchProtocolStep);
        initComponents();

        if ((geomSearchProtocolStep != null) && (geomSearchProtocolStep.getGeometry() != null)) {
            initMap(geomSearchProtocolStep.getGeometry(), mappingComponent1, showSearchGeometry);
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param  geom                DOCUMENT ME!
     * @param  map                 DOCUMENT ME!
     * @param  showSearchGeometry  DOCUMENT ME!
     */
    public final void initMap(final Geometry geom, final MappingComponent map, final boolean showSearchGeometry) {
        final GeometryProtocolStepConfiguration configuration = (GeometryProtocolStepConfiguration)getProtocolStep()
                    .getConfiguration();
        final String srs = configuration.getSrs();
        final String wmsMapUrl = configuration.getWmsMapUrl();
        final double zoomFactor = configuration.getZoomFactor();

        final Geometry transformedGeom = CrsTransformer.transformToGivenCrs(geom, srs);

        final XBoundingBox bbox = new XBoundingBox(transformedGeom.getEnvelope(), srs, true);
        if (showSearchGeometry) {
            bbox.setX1(bbox.getX1() - (zoomFactor * bbox.getWidth()));
            bbox.setX2(bbox.getX2() + (zoomFactor * bbox.getWidth()));
            bbox.setY1(bbox.getY1() - (zoomFactor * bbox.getHeight()));
            bbox.setY2(bbox.getY2() + (zoomFactor * bbox.getHeight()));
        }
        final ActiveLayerModel mappingModel = new ActiveLayerModel();
        mappingModel.setSrs(srs);
        mappingModel.addHome(bbox);

        final SimpleWMS swms = new SimpleWMS(new SimpleWmsGetMapUrl(wmsMapUrl));

        mappingModel.addLayer(swms);

        map.setMappingModel(mappingModel);
        map.setInteracting(false);
        map.setInteractionMode(MappingComponent.OVERVIEW);

        if (showSearchGeometry) {
            final SearchFeature searchFeature = new SearchFeature(transformedGeom, null);
            map.getFeatureCollection().addFeature(searchFeature);
        }

        map.unlock();
        map.gotoInitialBoundingBox();
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The
     * content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mappingComponent1 = new de.cismet.cismap.commons.gui.MappingComponent();
        jXHyperlink1 = new org.jdesktop.swingx.JXHyperlink();

        setMinimumSize(new java.awt.Dimension(29, 150));
        setPreferredSize(new java.awt.Dimension(300, 200));
        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(mappingComponent1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(
            jXHyperlink1,
            org.openide.util.NbBundle.getMessage(
                GeometryProtocolStepPanel.class,
                "GeometryProtocolStepPanel.jXHyperlink1.text")); // NOI18N
        jXHyperlink1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jXHyperlink1.addActionListener(new java.awt.event.ActionListener() {

                @Override
                public void actionPerformed(final java.awt.event.ActionEvent evt) {
                    jXHyperlink1ActionPerformed(evt);
                }
            });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.weightx = 1.0;
        add(jXHyperlink1, gridBagConstraints);
    } // </editor-fold>//GEN-END:initComponents

    /**
     * DOCUMENT ME!
     *
     * @param  evt  DOCUMENT ME!
     */
    private void jXHyperlink1ActionPerformed(final java.awt.event.ActionEvent evt) { //GEN-FIRST:event_jXHyperlink1ActionPerformed
        CismapBroker.getInstance()
                .getMappingComponent()
                .gotoBoundingBoxWithHistory(new XBoundingBox(getProtocolStep().getGeometry()));
    }                                                                                //GEN-LAST:event_jXHyperlink1ActionPerformed
}
