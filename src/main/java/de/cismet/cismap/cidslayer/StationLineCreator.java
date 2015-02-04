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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import org.apache.log4j.Logger;

import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.List;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedEvent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedListener;
import de.cismet.cismap.commons.gui.attributetable.creator.AbstractFeatureCreator;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.CreateLinearReferencedLineListener;
import de.cismet.cismap.linearreferencing.CreateStationLineListener;
import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StationLineCreator extends AbstractFeatureCreator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(StationLineCreator.class);

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureCreatedListener> listener = new ArrayList<FeatureCreatedListener>();

    private String property;
    private MetaClass routeClass;
    private LinearReferencingHelper helper;
    private float minDistance = 0;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationLineCreator object.
     *
     * @param  property    mode DOCUMENT ME!
     * @param  routeClass  DOCUMENT ME!
     * @param  helper      DOCUMENT ME!
     */
    public StationLineCreator(final String property, final MetaClass routeClass, final LinearReferencingHelper helper) {
        this(property, routeClass, helper, 0);
    }
    /**
     * Creates a new StationLineCreator object.
     *
     * @param  property     mode DOCUMENT ME!
     * @param  routeClass   DOCUMENT ME!
     * @param  helper       DOCUMENT ME!
     * @param  minDistance  DOCUMENT ME!
     */
    public StationLineCreator(final String property,
            final MetaClass routeClass,
            final LinearReferencingHelper helper,
            final float minDistance) {
        this.property = property;
        this.routeClass = routeClass;
        this.helper = helper;
        this.minDistance = minDistance;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void createFeature(final MappingComponent mc, final FeatureServiceFeature feature) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final String oldInteractionMode = mc.getInteractionMode();

                    final CreateLinearReferencedLineListener listener = new CreateLinearReferencedLineListener(
                            mc,
                            new CreateStationLineListener() {

                                @Override
                                public void lineFinished(final CidsBean route,
                                        Geometry lineGeom,
                                        final Geometry startGeom,
                                        final Geometry endGeom,
                                        final double start,
                                        final double end) {
                                    if (route == null) {
                                        // cancel the creation mode
                                        mc.setInteractionMode(oldInteractionMode);
                                        return;
                                    }
                                    mc.setInteractionMode(oldInteractionMode);
                                    lineGeom = CrsTransformer.transformToDefaultCrs(lineGeom);
                                    lineGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                                    final CidsBean line = helper.createLineBeanFromRouteBean(route);
                                    final CidsBean startStation = helper.createStationBeanFromRouteBean(route, start);
                                    final CidsBean endStation = helper.createStationBeanFromRouteBean(route, end);

                                    try {
                                        // todo: use helper
                                        helper.setGeometryToLineBean(lineGeom, line);
                                        line.setProperty("von", startStation);
                                        line.setProperty("bis", endStation);
                                    } catch (Exception ex) {
                                        LOG.error("Error while creating new line feature", ex);
                                    }
                                    feature.setProperty(property, line);
                                    feature.setGeometry(lineGeom);
                                    if (feature instanceof DefaultFeatureServiceFeature) {
                                        try {
                                            fillFeatureWithDefaultValues((DefaultFeatureServiceFeature)feature);

                                            ((DefaultFeatureServiceFeature)feature).saveChanges();

                                            for (final FeatureCreatedListener featureCreatedListener
                                                        : StationLineCreator.this.listener) {
                                                featureCreatedListener.featureCreated(
                                                    new FeatureCreatedEvent(StationLineCreator.this, feature));
                                            }
                                        } catch (Exception e) {
                                            LOG.error("Cannot save new feature", e);
                                        }
                                    }
                                }
                            },
                            routeClass,
                            minDistance);
                    mc.addInputListener(
                        CreateLinearReferencedLineListener.CREATE_LINEAR_REFERENCED_LINE_MODE,
                        listener);
                    mc.setInteractionMode(CreateLinearReferencedLineListener.CREATE_LINEAR_REFERENCED_LINE_MODE);
                }
            });
    }

    @Override
    public void addFeatureCreatedListener(final FeatureCreatedListener listener) {
        this.listener.add(listener);
    }

    @Override
    public String getTypeName() {
        return "stationierte Linie";
    }
}
