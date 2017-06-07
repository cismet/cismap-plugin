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

import Sirius.navigator.connection.SessionManager;

import Sirius.server.middleware.types.MetaClass;
import Sirius.server.middleware.types.MetaObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import org.apache.log4j.Logger;

import org.openide.util.NbBundle;

import java.awt.Cursor;
import java.awt.EventQueue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.features.DefaultFeatureServiceFeature;
import de.cismet.cismap.commons.features.FeatureServiceFeature;
import de.cismet.cismap.commons.gui.MappingComponent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedEvent;
import de.cismet.cismap.commons.gui.attributetable.FeatureCreatedListener;
import de.cismet.cismap.commons.gui.attributetable.creator.AbstractFeatureCreator;
import de.cismet.cismap.commons.gui.attributetable.creator.CreaterGeometryListener;
import de.cismet.cismap.commons.gui.attributetable.creator.GeometryFinishedListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateGeometryListenerInterface;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.CreateNewGeometryListener;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;

import de.cismet.cismap.linearreferencing.LinearReferencingHelper;

import de.cismet.tools.gui.WaitingDialogThread;

import static de.cismet.cismap.commons.gui.attributetable.FeatureCreator.SIMPLE_GEOMETRY_LISTENER_KEY;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public class LineAndStationCreator extends AbstractFeatureCreator {

    //~ Static fields/initializers ---------------------------------------------

    private static final Logger LOG = Logger.getLogger(LineAndStationCreator.class);
    private static final String SUITABLE_ROUTE_QUERY = "select %1$s, %3$s.%2$s from %3$s "
                + "join geom on (geom = geom.id) order by st_distance(geo_field, '%4$s') asc limit 1;";

    //~ Instance fields --------------------------------------------------------

    protected List<FeatureCreatedListener> listener = new ArrayList<FeatureCreatedListener>();

    private final String mode = CreateGeometryListenerInterface.LINESTRING;
    private final String stationProperty;
    private final MetaClass routeClass;
    private final LinearReferencingHelper helper;
    private float minDistance = 0;
    private float maxDistance = Float.MAX_VALUE;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PrimitiveGeometryCreator object.
     *
     * @param  stationProperty  DOCUMENT ME!
     * @param  properties       DOCUMENT ME!
     * @param  routeClass       DOCUMENT ME!
     * @param  helper           DOCUMENT ME!
     */
    public LineAndStationCreator(final String stationProperty,
            final Map<String, Object> properties,
            final MetaClass routeClass,
            final LinearReferencingHelper helper) {
        this.properties = properties;
        this.routeClass = routeClass;
        this.helper = helper;
        this.stationProperty = stationProperty;
    }

    //~ Methods ----------------------------------------------------------------

    @Override
    public void createFeature(final MappingComponent mc, final FeatureServiceFeature feature) {
        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    final CreateNewGeometryListener listener = new CreaterGeometryListener(
                            mc,
                            new GeometryFinishedListener() {

                                @Override
                                public void geometryFinished(final Geometry g) {
                                    feature.setGeometry(g);
//                                    mc.setInteractionMode(oldInteractionMode);

                                    if (feature instanceof DefaultFeatureServiceFeature) {
                                        try {
                                            fillFeatureWithDefaultValues(
                                                (DefaultFeatureServiceFeature)feature,
                                                properties);
                                            final WaitingDialogThread dia = new WaitingDialogThread<Void>(
                                                    null,
                                                    true,
                                                    NbBundle.getMessage(
                                                        LineAndStationCreator.class,
                                                        "LineAndStationCreator.createFeature()"),
                                                    null,
                                                    100) {

                                                    @Override
                                                    protected Void doInBackground() throws Exception {
                                                        final Geometry firstPoint = createPointFromCoords(
                                                                g.getCoordinates()[0],
                                                                g.getFactory());
                                                        final Geometry lastPoint = createPointFromCoords(
                                                                g.getCoordinates()[g.getNumPoints() - 1],
                                                                g.getFactory());
                                                        ;
                                                        final String query = String.format(
                                                                SUITABLE_ROUTE_QUERY,
                                                                routeClass.getID(),
                                                                routeClass.getPrimaryKey(),
                                                                routeClass.getTableName(),
                                                                firstPoint.toText());
                                                        final MetaObject[] mo = SessionManager.getProxy()
                                                                        .getMetaObjectByQuery(
                                                                            SessionManager.getSession().getUser(),
                                                                            query);

                                                        if ((mo != null) && (mo.length == 1)) {
                                                            final CidsBean routeBean = mo[0].getBean();

                                                            final CidsBean firstStation = createStationFromRoute(
                                                                    routeBean,
                                                                    firstPoint);
                                                            final CidsBean lastStation = createStationFromRoute(
                                                                    routeBean,
                                                                    lastPoint);
                                                            final CidsBean lineBean =
                                                                helper.createLineBeanFromRouteBean(routeBean);
                                                            final Double firstVal = (Double)firstStation.getProperty(
                                                                    helper.getValueProperty(firstStation));
                                                            final Double lastVal = (Double)lastStation.getProperty(
                                                                    helper.getValueProperty(lastStation));

                                                            helper.setLinearValueToStationBean(
                                                                firstVal,
                                                                helper.getStationBeanFromLineBean(lineBean, true));
                                                            helper.setLinearValueToStationBean(
                                                                lastVal,
                                                                helper.getStationBeanFromLineBean(lineBean, false));
                                                            feature.setProperty(stationProperty, lineBean);
                                                        }

                                                        ((DefaultFeatureServiceFeature)feature).saveChanges();

                                                        return null;
                                                    }

                                                    @Override
                                                    protected void done() {
                                                        try {
                                                            get();

                                                            for (final FeatureCreatedListener featureCreatedListener
                                                                        : LineAndStationCreator.this.listener) {
                                                                featureCreatedListener.featureCreated(
                                                                    new FeatureCreatedEvent(
                                                                        LineAndStationCreator.this,
                                                                        feature));
                                                            }
                                                        } catch (Exception e) {
                                                            LOG.error("Cannot save new feature", e);
                                                        }
                                                    }
                                                };

                                            dia.start();
                                        } catch (Exception e) {
                                            LOG.error("Cannot save new feature", e);
                                        }
                                    }
                                }

                                private CidsBean createStationFromRoute(final CidsBean routeBean,
                                        final Geometry point) {
                                    final Coordinate[] firstCoords = DistanceOp.nearestPoints(
                                            helper.getGeometryFromRoute(routeBean),
                                            point);
                                    final double firstPosition = LinearReferencedPointFeature.getPositionOnLine(
                                            firstCoords[0],
                                            helper.getGeometryFromRoute(routeBean));

                                    final CidsBean station = helper.createStationBeanFromRouteBean(
                                            routeBean,
                                            firstPosition);

                                    return station;
                                }

                                private Geometry createPointFromCoords(final Coordinate coord,
                                        final GeometryFactory factory) {
                                    return factory.createPoint(coord);
                                }
                            });
                    mc.addInputListener(SIMPLE_GEOMETRY_LISTENER_KEY, listener);
                    mc.putCursor(SIMPLE_GEOMETRY_LISTENER_KEY, new Cursor(Cursor.CROSSHAIR_CURSOR));
                    listener.setMode(mode);
                    mc.setInteractionMode(SIMPLE_GEOMETRY_LISTENER_KEY);
                }
            });
    }

    @Override
    public void addFeatureCreatedListener(final FeatureCreatedListener listener) {
        this.listener.add(listener);
    }

    @Override
    public String getTypeName() {
        return NbBundle.getMessage(LineAndStationCreator.class, "PointAndStationCreator.getTypeName()");
    }
}
