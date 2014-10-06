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

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.CrsTransformer;
import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.featureservice.AbstractFeatureService;
import de.cismet.cismap.commons.featureservice.LayerProperties;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreator;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.linearreferencing.CreateLinearReferencedPointListener;
import de.cismet.cismap.linearreferencing.CreateStationListener;
import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class StationCreator implements FeatureCreator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(StationCreator.class);

    //~ Instance fields --------------------------------------------------------

    private final String property;
    private final MetaClass routeClass;
    private final LinearReferencingHelper helper;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new StationLineCreator object.
     *
     * @param  property    mode DOCUMENT ME!
     * @param  routeClass  DOCUMENT ME!
     * @param  helper      DOCUMENT ME!
     */
    public StationCreator(final String property, final MetaClass routeClass, final LinearReferencingHelper helper) {
        this.property = property;
        this.routeClass = routeClass;
        this.helper = helper;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void createFeature(final MappingComponent mc, final FeatureServiceFeature feature) {
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
                                    mc.setInteractionMode(oldInteractionMode);
                                    pointGeom = CrsTransformer.transformToDefaultCrs(pointGeom);
                                    pointGeom.setSRID(CismapBroker.getInstance().getDefaultCrsAlias());
                                    final CidsBean station = helper.createStationBeanFromRouteBean(route, point);

                                    feature.setProperty(property, station);
                                    feature.setGeometry(pointGeom);
                                    if (feature instanceof DefaultFeatureServiceFeature) {
                                        try {
                                            ((DefaultFeatureServiceFeature)feature).saveChanges();

                                            // reload layer
                                            final LayerProperties props = feature.getLayerProperties();

                                            if (props != null) {
                                                final AbstractFeatureService service = props.getFeatureService();

                                                if (service != null) {
                                                    service.retrieve(true);
                                                }
                                            }
                                        } catch (Exception e) {
                                            LOG.error("Cannot save new feature", e);
                                        }
                                    }
                                }
                            },
                            routeClass);
                    mc.addInputListener(
                        CreateLinearReferencedPointListener.CREATE_LINEAR_REFERENCED_POINT_MODE,
                        listener);
                    mc.setInteractionMode(CreateLinearReferencedPointListener.CREATE_LINEAR_REFERENCED_POINT_MODE);
                }
            });
    }

    @Override
    public String getTypeName() {
        return "Station";
    }
}
