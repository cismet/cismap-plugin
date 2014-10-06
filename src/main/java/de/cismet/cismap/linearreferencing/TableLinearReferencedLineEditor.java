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

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.awt.Color;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cids.dynamics.CidsBean;
import de.cismet.cids.dynamics.DisposableCidsBeanStore;

import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedLineFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.tools.StationEditorInterface;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TableLinearReferencedLineEditor implements DisposableCidsBeanStore,
    PropertyChangeListener,
    StationEditorInterface {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(TableLinearReferencedLineEditor.class);
    private static LinearReferencingHelper linearReferencedHelper = FeatureRegistry.getInstance()
                .getLinearReferencingSolver();

    //~ Instance fields --------------------------------------------------------

    private TableStationEditor fromStation;
    private TableStationEditor toStation;
    private CidsBean cidsBean;
    private LinearReferencedLineFeature lineFeature;
    private Color lineColor;
    private List<PropertyChangeListener> propListener = new ArrayList<PropertyChangeListener>();
    private String valueProperty;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TableLinearReferencedLineEditor object.
     */
    public TableLinearReferencedLineEditor() {
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setCidsBean(final CidsBean cidsBean) {
        this.cidsBean = cidsBean;

        if (cidsBean != null) {
            fromStation = new TableStationEditor(true, cidsBean);
            fromStation.setCidsBean(linearReferencedHelper.getStationBeanFromLineBean(cidsBean, true));
            toStation = new TableStationEditor(true, cidsBean);
            toStation.setCidsBean(linearReferencedHelper.getStationBeanFromLineBean(cidsBean, false));
            lineFeature = FeatureRegistry.getInstance()
                        .addLinearReferencedLineFeature(
                                cidsBean,
                                fromStation.getStationFeature(),
                                toStation.getStationFeature());

            lineColor = FeatureRegistry.getNextColor(cidsBean);
            lineFeature.setLinePaint(getLineColor());

            fromStation.addPropertyChangeListener(this);
            toStation.addPropertyChangeListener(this);

            valueProperty = linearReferencedHelper.getValueProperty(linearReferencedHelper.getStationBeanFromLineBean(
                        cidsBean,
                        false));
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the von
     */
    public TableStationEditor getFromStation() {
        return fromStation;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the bis
     */
    public TableStationEditor getToStation() {
        return toStation;
    }

    @Override
    public void dispose() {
        fromStation.removePropertyChangeListener(this);
        toStation.removePropertyChangeListener(this);

        recreateGeometry();

        FeatureRegistry.getInstance().removeLinearReferencedLineFeature(cidsBean);
    }

    /**
     * DOCUMENT ME!
     */
    private void recreateGeometry() {
        final double from = (Double)fromStation.getValue();
        final double to = (Double)toStation.getValue();

        final Geometry lineGeom = LinearReferencedLineFeature.createSubline(
                from,
                to,
                linearReferencedHelper.getRouteGeometryFromStationBean(fromStation.getCidsBean()));
        lineGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

        try {
            linearReferencedHelper.setGeometryToLineBean(lineGeom, cidsBean);
        } catch (Exception e) {
            LOG.error("Cannot create line geometry", e);
        }
    }

    @Override
    public CidsBean getCidsBean() {
        return cidsBean;
    }

    /**
     * DOCUMENT ME!
     */
    @Override
    public void undoChanges() {
        fromStation.undoChanges();
        toStation.undoChanges();
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the lineColor
     */
    public Color getLineColor() {
        return lineColor;
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        // Only fire a property change event, if the value property changed.
        // If the change event will be fire for all property change events on the station bean,
        // an infinite loop will be begin
        if (evt.getPropertyName().equalsIgnoreCase(valueProperty)) {
            recreateGeometry();
            firePropertyChange(evt);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void addPropertyChangeListener(final PropertyChangeListener l) {
        propListener.add(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  l  DOCUMENT ME!
     */
    public void removePropertyChangeListener(final PropertyChangeListener l) {
        propListener.remove(l);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  e  DOCUMENT ME!
     */
    protected void firePropertyChange(final PropertyChangeEvent e) {
        for (final PropertyChangeListener l : propListener) {
            l.propertyChange(e);
        }
    }

    @Override
    public Object getValue() {
        final CidsBean geomBean = linearReferencedHelper.getGeomBeanFromLineBean(cidsBean);

        return geomBean.getProperty("geo_field");
    }
}
