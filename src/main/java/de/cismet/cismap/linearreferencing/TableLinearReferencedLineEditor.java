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

import de.cismet.cismap.cidslayer.CidsLayerFeature;

import de.cismet.cismap.commons.features.FeatureServiceFeature;
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
    private String routeName;
    private String otherLinesFrom;
    private String otherLinesQuery;
    private FeatureServiceFeature parentFeature;
    private String stationProperty;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new TableLinearReferencedLineEditor object.
     *
     * @param  routeName        DOCUMENT ME!
     * @param  parentFeature    DOCUMENT ME!
     * @param  stationProperty  DOCUMENT ME!
     */
    public TableLinearReferencedLineEditor(final String routeName,
            final FeatureServiceFeature parentFeature,
            final String stationProperty) {
        this.routeName = routeName;
        this.parentFeature = parentFeature;
        this.stationProperty = stationProperty;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void setCidsBean(final CidsBean cidsBean) {
        this.cidsBean = cidsBean;

//        if (cidsBean != null) {
        fromStation = new TableStationEditor(true, cidsBean, routeName, parentFeature, stationProperty);
        toStation = new TableStationEditor(true, cidsBean, routeName, parentFeature, stationProperty);
        if ((otherLinesFrom != null) && (otherLinesQuery != null)) {
            fromStation.setOtherLinesFrom(otherLinesFrom);
            fromStation.setOtherLinesQuery(otherLinesQuery);
            toStation.setOtherLinesFrom(otherLinesFrom);
            toStation.setOtherLinesQuery(otherLinesQuery);
        }
        fromStation.setCidsBean(linearReferencedHelper.getStationBeanFromLineBean(cidsBean, true));
        toStation.setCidsBean(linearReferencedHelper.getStationBeanFromLineBean(cidsBean, false));

        if (fromStation.getStationFeature() != null) {
            lineFeature = FeatureRegistry.getInstance()
                        .addLinearReferencedLineFeature(
                                cidsBean,
                                fromStation.getStationFeature(),
                                toStation.getStationFeature());

            lineColor = FeatureRegistry.getNextColor(cidsBean);
            lineFeature.setLinePaint(getLineColor());
        }
        fromStation.addPropertyChangeListener(this);
        toStation.addPropertyChangeListener(this);

        valueProperty = linearReferencedHelper.getValueProperty(linearReferencedHelper.getStationBeanFromLineBean(
                    cidsBean,
                    false));
//        }
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
        if (fromStation != null) {
            fromStation.removePropertyChangeListener(this);
        }

        if (toStation != null) {
            toStation.removePropertyChangeListener(this);
        }

        try {
            recreateGeometry();
        } catch (Exception e) {
            LOG.warn("Cannot recreate geometry.", e);
        }
        if (cidsBean != null) {
            FeatureRegistry.getInstance().removeLinearReferencedLineFeature(cidsBean);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Geometry recreateGeometry() {
        if ((fromStation != null) && (toStation != null)) {
            final double from = (Double)fromStation.getValue();
            final double to = (Double)toStation.getValue();

            final Geometry lineGeom = LinearReferencedLineFeature.createSubline(
                    from,
                    to,
                    linearReferencedHelper.getRouteGeometryFromStationBean(fromStation.getCidsBean()));
            lineGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());

            try {
                linearReferencedHelper.setGeometryToLineBean(lineGeom, cidsBean);
                if (Math.abs(to - toStation.getFeature().getCurrentPosition()) > 0.1) {
                    linearReferencedHelper.setLinearValueToStationBean(toStation.getFeature().getCurrentPosition(),
                        toStation.getCidsBean());
                }
                if (Math.abs(from - fromStation.getFeature().getCurrentPosition()) > 0.1) {
                    linearReferencedHelper.setLinearValueToStationBean(fromStation.getFeature().getCurrentPosition(),
                        fromStation.getCidsBean());
                }
            } catch (Exception e) {
                LOG.error("Cannot create line geometry", e);
            }

            return lineGeom;
        }

        return null;
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
        if (fromStation != null) {
            fromStation.undoChanges();
        }
        if (fromStation != null) {
            toStation.undoChanges();
        }
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

    /**
     * DOCUMENT ME!
     *
     * @return  the otherLinesFrom
     */
    public String getOtherLinesFrom() {
        return otherLinesFrom;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  otherLinesFrom  the otherLinesFrom to set
     */
    public void setOtherLinesFrom(final String otherLinesFrom) {
        this.otherLinesFrom = otherLinesFrom;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the otherLinesQuery
     */
    public String getOtherLinesQuery() {
        return otherLinesQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  otherLinesQuery  the otherLinesQuery to set
     */
    public void setOtherLinesQuery(final String otherLinesQuery) {
        this.otherLinesQuery = otherLinesQuery;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the parentFeature
     */
    public FeatureServiceFeature getParentFeature() {
        return parentFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  parentFeature  the parentFeature to set
     */
    public void setParentFeature(final FeatureServiceFeature parentFeature) {
        this.parentFeature = parentFeature;
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the stationProperty
     */
    public String getStationProperty() {
        return stationProperty;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  stationProperty  the stationProperty to set
     */
    public void setStationProperty(final String stationProperty) {
        this.stationProperty = stationProperty;
    }

    @Override
    public Geometry getGeometry() {
        try {
            if (cidsBean != null) {
                return (Geometry)linearReferencedHelper.getGeomBeanFromLineBean(cidsBean).getProperty("geo_field");
            }
        } catch (Exception e) {
            LOG.error("Cannot create line geometry", e);
        }

        return null;
    }
}
