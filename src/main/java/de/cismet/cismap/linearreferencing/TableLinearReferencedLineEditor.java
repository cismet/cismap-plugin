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

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class TableLinearReferencedLineEditor implements DisposableCidsBeanStore, PropertyChangeListener {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(TableLinearReferencedLineEditor.class);
    private static LinearReferencingHelper linearReferencedSolver = FeatureRegistry.getInstance()
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
            fromStation.setCidsBean(linearReferencedSolver.getStationBeanFromLineBean(cidsBean, true));
            toStation = new TableStationEditor(true, cidsBean);
            toStation.setCidsBean(linearReferencedSolver.getStationBeanFromLineBean(cidsBean, false));
            lineFeature = FeatureRegistry.getInstance()
                        .addLinearReferencedLineFeature(
                                cidsBean,
                                fromStation.getStationFeature(),
                                toStation.getStationFeature());

            lineColor = FeatureRegistry.getNextColor(cidsBean);
            lineFeature.setLinePaint(getLineColor());

            fromStation.addPropertyChangeListener(this);
            toStation.addPropertyChangeListener(this);

            valueProperty = linearReferencedSolver.getValueProperty(linearReferencedSolver.getStationBeanFromLineBean(
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
        final double from = fromStation.getValue();
        final double to = toStation.getValue();
        fromStation.removePropertyChangeListener(this);
        toStation.removePropertyChangeListener(this);

        final Geometry lineGeom = LinearReferencedLineFeature.createSubline(
                from,
                to,
                linearReferencedSolver.getRouteGeometryFromStationBean(fromStation.getCidsBean()));
        lineGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

        try {
            linearReferencedSolver.setGeometryToLineBean(lineGeom, cidsBean);
        } catch (Exception e) {
            LOG.error("Cannot create line geometry", e);
        }

        FeatureRegistry.getInstance().removeLinearReferencedLineFeature(cidsBean);
    }

    @Override
    public CidsBean getCidsBean() {
        return cidsBean;
    }

    /**
     * DOCUMENT ME!
     */
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
}
