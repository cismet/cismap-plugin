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
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedEvent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedListener;
import de.cismet.cismap.commons.gui.attributetable.creator.AbstractFeatureCreator;
import de.cismet.cismap.commons.gui.piccolo.PFeature;
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
    private float minDistance = 0.004f;
    private float maxDistance = Float.MAX_VALUE;
    private String routeName;
    private StationCreationCheck check;
    private AbstractFeatureService service = null;
    private CreateLinearReferencedLineListener lastListener = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationLineCreator object.
     *
     * @param  property    mode DOCUMENT ME!
     * @param  routeClass  DOCUMENT ME!
     * @param  routeName   DOCUMENT ME!
     * @param  helper      DOCUMENT ME!
     */
    public StationLineCreator(final String property,
            final MetaClass routeClass,
            final String routeName,
            final LinearReferencingHelper helper) {
        this(property, routeClass, routeName, helper, 0);
    }

    /**
     * Creates a new StationLineCreator object.
     *
     * @param  property     mode DOCUMENT ME!
     * @param  routeClass   DOCUMENT ME!
     * @param  routeName    DOCUMENT ME!
     * @param  helper       DOCUMENT ME!
     * @param  minDistance  DOCUMENT ME!
     */
    public StationLineCreator(final String property,
            final MetaClass routeClass,
            final String routeName,
            final LinearReferencingHelper helper,
            final float minDistance) {
        this(property, routeClass, routeName, helper, minDistance, Float.MAX_VALUE);
    }
    /**
     * Creates a new StationLineCreator object.
     *
     * @param  property     mode DOCUMENT ME!
     * @param  routeClass   DOCUMENT ME!
     * @param  routeName    DOCUMENT ME!
     * @param  helper       DOCUMENT ME!
     * @param  minDistance  DOCUMENT ME!
     * @param  maxDistance  DOCUMENT ME!
     */
    public StationLineCreator(final String property,
            final MetaClass routeClass,
            final String routeName,
            final LinearReferencingHelper helper,
            final float minDistance,
            final float maxDistance) {
        this.property = property;
        this.routeClass = routeClass;
        this.helper = helper;
        this.minDistance = minDistance;
        this.maxDistance = maxDistance;
        this.routeName = routeName;
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  the check
     */
    public StationCreationCheck getCheck() {
        return check;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  check  the check to set
     */
    public void setCheck(final StationCreationCheck check) {
        this.check = check;
    }

    @Override
    public void createFeature(final MappingComponent mc, final FeatureServiceFeature feature) {
        if ((feature != null) && (feature.getLayerProperties() != null)) {
            service = feature.getLayerProperties().getFeatureService();
        }
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
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
// mc.setInteractionMode(oldInteractionMode);
                                        return;
                                    }
//                                    mc.setInteractionMode(oldInteractionMode);
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
                                            fillFeatureWithDefaultValues(
                                                (DefaultFeatureServiceFeature)feature,
                                                properties);

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
                            routeName,
                            minDistance,
                            maxDistance,
                            check);
                    listener.setIdenticalPositionDelta(minDistance);
                    mc.addInputListener(
                        CreateLinearReferencedLineListener.CREATE_LINEAR_REFERENCED_LINE_MODE,
                        listener);
                    mc.setInteractionMode(CreateLinearReferencedLineListener.CREATE_LINEAR_REFERENCED_LINE_MODE);
                    lastListener = listener;
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

    @Override
    public void cancel() {
    }

    @Override
    public void resume() {
        CismapBroker.getInstance()
                .getMappingComponent()
                .setInteractionMode(CreateLinearReferencedLineListener.CREATE_LINEAR_REFERENCED_LINE_MODE);

        if (lastListener != null) {
            lastListener.resumed();
        }
    }

    @Override
    public AbstractFeatureService getService() {
        return service;
    }

    @Override
    public boolean isCreationAllowed(final MappingComponent mc) {
        final PFeature line = CreateLinearReferencedLineListener.getSelectedLinePFeature(mc, routeClass);

        return (line == null) || ((check != null) && check.isRouteValid(line));
    }
}
