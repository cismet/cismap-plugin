/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
package de.cismet.cismap.linearreferencing;

import Sirius.navigator.plugin.PluginRegistry;

import com.vividsolutions.jts.geom.Geometry;

import org.openide.util.Lookup;

import java.awt.Color;
import java.awt.EventQueue;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.CopyOnWriteArrayList;

import de.cismet.cids.dynamics.CidsBean;

import de.cismet.cismap.commons.features.Feature;
import de.cismet.cismap.commons.features.FeatureCollection;
import de.cismet.cismap.commons.features.PureNewFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.DrawSelectionFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedLineFeature;
import de.cismet.cismap.commons.gui.piccolo.eventlistener.LinearReferencedPointFeature;
import de.cismet.cismap.commons.interaction.CismapBroker;

import de.cismet.cismap.navigatorplugin.CismapPlugin;

import de.cismet.tools.CurrentStackTrace;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class FeatureRegistry implements LinearReferencingConstants, LinearReferencingSingletonInstances {

    //~ Static fields/initializers ---------------------------------------------

    private static FeatureRegistry instance = new FeatureRegistry();

    private static final Color[] COLORS = new Color[] {
            new Color(217, 215, 204),
            new Color(242, 187, 19),
            new Color(217, 159, 126),
            new Color(242, 65, 48),
            new Color(121, 132, 39),
            new Color(184, 206, 233),
            new Color(216, 120, 57)
        };
//            new Color[] {
//            new Color(41, 86, 178),
//            new Color(101, 156, 239),
//            new Color(125, 189, 0),
//            new Color(220, 246, 0),
//            new Color(255, 91, 0)
//        };

    //~ Instance fields --------------------------------------------------------

    private HashMap<CidsBean, Feature> featureReg = new HashMap<CidsBean, Feature>();
    private HashMap<Feature, CidsBean> cidsBeanReg = new HashMap<Feature, CidsBean>();
    private HashMap<CidsBean, Integer> counterMap = new HashMap<CidsBean, Integer>();
    private HashMap<CidsBean, Collection<FeatureRegistryListener>> listenerMap =
        new HashMap<CidsBean, Collection<FeatureRegistryListener>>();
    private LinearReferencingHelper linearReferencingSolver = null;

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new FeatureRegistry object.
     */
    private FeatureRegistry() {
        final Collection<? extends LinearReferencingHelper> allLinearReferencingSolver = Lookup.getDefault()
                    .lookupAll(LinearReferencingHelper.class);

        if ((allLinearReferencingSolver != null) && (allLinearReferencingSolver.size() > 0)) {
            linearReferencingSolver = allLinearReferencingSolver.toArray(new LinearReferencingHelper[1])[0];
        }
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static FeatureRegistry getInstance() {
        return instance;
    }

    /**
     * >> LISTENERS.
     *
     * @param   cidsBean  DOCUMENT ME!
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean addListener(final CidsBean cidsBean, final FeatureRegistryListener listener) {
        if (listenerMap.get(cidsBean) == null) {
            listenerMap.put(cidsBean, new CopyOnWriteArrayList<FeatureRegistryListener>());
        }
        return listenerMap.get(cidsBean).add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     * @param   listener  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public boolean removeListener(final CidsBean cidsBean, final FeatureRegistryListener listener) {
        final Collection<FeatureRegistryListener> listeners = listenerMap.get(cidsBean);
        if (listeners != null) {
            return listeners.remove(listener);
        } else {
            return false;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     */
    private void fireFeatureCountChanged(final CidsBean cidsBean) {
        final Collection<FeatureRegistryListener> listeners = listenerMap.get(cidsBean);
        if (listeners != null) {
            for (final FeatureRegistryListener listener : listeners) {
                listener.FeatureCountChanged();
            }
        }
    }

    /**
     * LISTENERS << >> ADD FEATURE.
     *
     * @param   cidsBean  DOCUMENT ME!
     * @param   feature   DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Feature addFeature(final CidsBean cidsBean, final Feature feature) {
        if (!featureReg.containsKey(cidsBean)) {
            featureReg.put(cidsBean, feature);
        }
        if (!cidsBeanReg.containsKey(feature)) {
            cidsBeanReg.put(feature, cidsBean);
        }

        if (getCounter(cidsBean) == 0) {
            addFeatureToMap(feature);
        }

        incrementCounter(cidsBean);
        return featureReg.get(cidsBean);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature addStationFeature(final CidsBean cidsBean) {
        final double value = getLinearReferencingSolver().getLinearValueFromStationBean(cidsBean);

        final Geometry routeGeometry = getLinearReferencingSolver().getRouteGeometryFromStationBean(cidsBean);
        addRouteFeature(getLinearReferencingSolver().getRouteBeanFromStationBean(cidsBean), routeGeometry);

        final LinearReferencedPointFeature linRefPoint = new LinearReferencedPointFeature(value, routeGeometry);

        return (LinearReferencedPointFeature)addFeature(cidsBean, linRefPoint);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     * @param   geometry  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public PureNewFeature addRouteFeature(final CidsBean cidsBean, final Geometry geometry) {
        return (RouteFeature)addFeature(cidsBean, new RouteFeature(geometry));
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     * @param   from      DOCUMENT ME!
     * @param   to        DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedLineFeature addLinearReferencedLineFeature(final CidsBean cidsBean,
            final LinearReferencedPointFeature from,
            final LinearReferencedPointFeature to) {
        final LinearReferencedLineFeature linRefLine = new LinearReferencedLineFeature(from, to);

        return (LinearReferencedLineFeature)addFeature(cidsBean, linRefLine);
    }

    /**
     * ADD FEATURE << >> REMOVE FEATURE.
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Feature removeFeature(final CidsBean cidsBean) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("remove " + cidsBean, new CurrentStackTrace());
        }
        if (decrementCounter(cidsBean) <= 0) {
            if (featureReg.containsKey(cidsBean)) {
                final Feature feature = featureReg.remove(cidsBean);
                if (LOG.isDebugEnabled()) {
                    LOG.debug("remove station from map");
                }
                removeFeatureFromMap(feature);
                return feature;
            }
        }
        return null;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   lineBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static Color getNextColor(final CidsBean lineBean) {
        return COLORS[Math.abs(String.valueOf((lineBean.hashCode())).hashCode() % COLORS.length)];
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedLineFeature removeLinearReferencedLineFeature(final CidsBean cidsBean) {
        return (LinearReferencedLineFeature)removeFeature(cidsBean);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public LinearReferencedPointFeature removeStationFeature(final CidsBean cidsBean) {
        removeRouteFeature(getLinearReferencingSolver().getRouteBeanFromStationBean(cidsBean));
        return (LinearReferencedPointFeature)removeFeature(cidsBean);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  cidsBean  DOCUMENT ME!
     */
    public void removeRouteFeature(final CidsBean cidsBean) {
        removeFeature(cidsBean);
    }

    /**
     * REMOVE FEATURE << >> COUNTER.
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int incrementCounter(final CidsBean cidsBean) {
        final int counter = getCounter(cidsBean) + 1;
        counterMap.put(cidsBean, counter);
        logCounterStatus("after increment " + cidsBean);
        fireFeatureCountChanged(cidsBean);
        return counter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private int decrementCounter(final CidsBean cidsBean) {
        final int counter = getCounter(cidsBean) - 1;
        counterMap.put(cidsBean, counter);
        logCounterStatus("after decrement " + cidsBean);
        fireFeatureCountChanged(cidsBean);
        return counter;
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public int getCounter(final CidsBean cidsBean) {
        if (!counterMap.containsKey(cidsBean)) {
            counterMap.put(cidsBean, new Integer(0));
        }
        return ((Integer)counterMap.get(cidsBean));
    }

    /**
     * COUNTER << >> TO MAP.
     *
     * @param  feature  DOCUMENT ME!
     */
    public static void addFeatureToMap(final Feature feature) {
        final CismapPlugin cismap = (CismapPlugin)PluginRegistry.getRegistry().getPlugin("cismap");
        if (cismap != null) {
            cismap.setFeatureCollectionEventBlocker(true);
        }
        final FeatureCollection featureCollection = CismapBroker.getInstance()
                    .getMappingComponent()
                    .getFeatureCollection();
        featureCollection.addFeature(feature);
        featureCollection.holdFeature(feature);

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (cismap != null) {
                        cismap.setFeatureCollectionEventBlocker(false);
                    }
                }
            });
    }

    /**
     * DOCUMENT ME!
     *
     * @param  feature  DOCUMENT ME!
     */
    public static void removeFeatureFromMap(final Feature feature) {
        final CismapPlugin cismap = (CismapPlugin)PluginRegistry.getRegistry().getPlugin("cismap");
        if (cismap != null) {
            cismap.setFeatureCollectionEventBlocker(true);
        }
        final FeatureCollection featureCollection = CismapBroker.getInstance()
                    .getMappingComponent()
                    .getFeatureCollection();
        // Mit diesem if kommt es zu Problemen, wenn man ein Feature ueber das Objekte-Fenster entfernt
// if (featureCollection.getAllFeatures().contains(feature)) {
        featureCollection.removeFeature(feature);
//        }

        EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    if (cismap != null) {
                        cismap.setFeatureCollectionEventBlocker(false);
                    }
                }
            });
    }

    /**
     * << TO MAP.
     *
     * @param   feature  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public CidsBean getCidsBean(final Feature feature) {
        return cidsBeanReg.get(feature);
    }

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public Feature getFeature(final CidsBean cidsBean) {
        return featureReg.get(cidsBean);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  string  DOCUMENT ME!
     */
    private void logCounterStatus(final String string) {
        final StringBuilder sb = new StringBuilder("counterStatus ").append(string)
                    .append(":<br/>\n=============<br/>\n");
        for (final Entry<CidsBean, Integer> entry : counterMap.entrySet()) {
            sb.append(entry.getKey())
                    .append("-")
                    .append(entry.getKey().getMetaObject().getId())
                    .append(" => ")
                    .append(entry.getValue())
                    .append("<br/>\n");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug(sb.toString());
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @return  the linearReferencingSolver
     */
    public LinearReferencingHelper getLinearReferencingSolver() {
        return linearReferencingSolver;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  linearReferencingSolver  the linearReferencingSolver to set
     */
    public void setLinearReferencingSolver(final LinearReferencingHelper linearReferencingSolver) {
        this.linearReferencingSolver = linearReferencingSolver;
    }

    //~ Inner Classes ----------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @version  $Revision$, $Date$
     */
    public class RouteFeature extends PureNewFeature implements DrawSelectionFeature {

        //~ Constructors -------------------------------------------------------

        /**
         * Creates a new RouteFeature object.
         *
         * @param  geomety  DOCUMENT ME!
         */
        public RouteFeature(final Geometry geomety) {
            super(geomety);
            setEditable(false);
            setCanBeSelected(false);
            setName("Route");
        }

        //~ Methods ------------------------------------------------------------

        @Override
        public String getType() {
            return "Route";
        }

        @Override
        public boolean isDrawingSelection() {
            return false;
        }
    }
}
