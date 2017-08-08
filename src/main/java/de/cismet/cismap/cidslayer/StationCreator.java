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
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.CreateLinearReferencedLineListener;
import de.cismet.cismap.linearreferencing.CreateLinearReferencedPointListener;
import de.cismet.cismap.linearreferencing.CreateStationListener;
import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StationCreator extends AbstractFeatureCreator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(StationCreator.class);

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureCreatedListener> listener = new ArrayList<FeatureCreatedListener>();

    private final String property;
    private final MetaClass routeClass;
    private final LinearReferencingHelper helper;
    private final String routeName;
    private StationCreationCheck check;
    private AbstractFeatureService service = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationLineCreator object.
     *
     * @param  property    mode DOCUMENT ME!
     * @param  routeClass  DOCUMENT ME!
     * @param  routeName   DOCUMENT ME!
     * @param  helper      DOCUMENT ME!
     */
    public StationCreator(final String property,
            final MetaClass routeClass,
            final String routeName,
            final LinearReferencingHelper helper) {
        this.property = property;
        this.routeClass = routeClass;
        this.helper = helper;
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
                    final String oldInteractionMode = mc.getInteractionMode();

                    final CreateLinearReferencedPointListener listener = new CreateLinearReferencedPointListener(
                            mc,
                            new CreateStationListener() {

                                @Override
                                public void pointFinished(final CidsBean route,
                                        Geometry pointGeom,
                                        final double point) {
                                    if (route == null) {
                                        // the creation was canceled
                                        return;
                                    }
//                                    mc.setInteractionMode(oldInteractionMode);
                                    pointGeom = CrsTransformer.transformToDefaultCrs(pointGeom);
                                    pointGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                                    final CidsBean station = helper.createStationBeanFromRouteBean(route, point);

                                    feature.setProperty(property, station);
                                    feature.setGeometry(pointGeom);
                                    if (feature instanceof DefaultFeatureServiceFeature) {
                                        try {
                                            fillFeatureWithDefaultValues(
                                                (DefaultFeatureServiceFeature)feature,
                                                properties);
                                            ((DefaultFeatureServiceFeature)feature).saveChanges();

                                            for (final FeatureCreatedListener featureCreatedListener
                                                        : StationCreator.this.listener) {
                                                featureCreatedListener.featureCreated(
                                                    new FeatureCreatedEvent(StationCreator.this, feature));
                                            }
                                        } catch (Exception e) {
                                            LOG.error("Cannot save new feature", e);
                                        }
                                    }
                                }
                            },
                            routeClass,
                            routeName,
                            check);
                    mc.addInputListener(
                        CreateLinearReferencedPointListener.CREATE_LINEAR_REFERENCED_POINT_MODE,
                        listener);
                    mc.setInteractionMode(CreateLinearReferencedPointListener.CREATE_LINEAR_REFERENCED_POINT_MODE);
                }
            });
    }

    @Override
    public void addFeatureCreatedListener(final FeatureCreatedListener listener) {
        this.listener.add(listener);
    }

    @Override
    public String getTypeName() {
        return "Station";
    }

    @Override
    public void cancel() {
    }

    @Override
    public void resume() {
        CismapBroker.getInstance()
                .getMappingComponent()
                .setInteractionMode(CreateLinearReferencedPointListener.CREATE_LINEAR_REFERENCED_POINT_MODE);
    }

    @Override
    public AbstractFeatureService getService() {
        return service;
    }
}
