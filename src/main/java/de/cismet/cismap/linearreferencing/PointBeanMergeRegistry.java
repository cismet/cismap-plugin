/***************************************************
*
* cismet GmbH, Saarbruecken, Germany
*
*              ... and it just works.
*
****************************************************/
/*
 *  Copyright (C) 2011 jruiz
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package de.cismet.cismap.linearreferencing;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   jruiz
 * @version  $Revision$, $Date$
 */
public class PointBeanMergeRegistry {

    //~ Static fields/initializers ---------------------------------------------

    private static PointBeanMergeRegistry INSTANCE = new PointBeanMergeRegistry();

    //~ Instance fields --------------------------------------------------------

    private HashMap<CidsBean, Collection<PointBeanMergeListener>> listenersMap =
        new HashMap<CidsBean, Collection<PointBeanMergeListener>>();
    private HashMap<CidsBean, Collection<PointBeanMergeRequestListener>> requestListenersMap =
        new HashMap<CidsBean, Collection<PointBeanMergeRequestListener>>();

    //~ Constructors -----------------------------------------------------------

    /**
     * Creates a new PointBeanMergeRegistry object.
     */
    private PointBeanMergeRegistry() {
    }

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    public static PointBeanMergeRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pointBean  DOCUMENT ME!
     * @param  listener   DOCUMENT ME!
     */
    public void addListener(final CidsBean pointBean, final PointBeanMergeListener listener) {
        Collection<PointBeanMergeListener> listeners = listenersMap.get(pointBean);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<PointBeanMergeListener>();
            listenersMap.put(pointBean, listeners);
        }
        listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pointBean  DOCUMENT ME!
     * @param  listener   DOCUMENT ME!
     */
    public void removeListener(final CidsBean pointBean, final PointBeanMergeListener listener) {
        final Collection<PointBeanMergeListener> listeners = listenersMap.get(pointBean);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                listenersMap.remove(pointBean);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectBean  pointBean DOCUMENT ME!
     * @param  listener    DOCUMENT ME!
     */
    public void addRequestListener(final CidsBean objectBean, final PointBeanMergeRequestListener listener) {
        Collection<PointBeanMergeRequestListener> listeners = requestListenersMap.get(objectBean);
        if (listeners == null) {
            listeners = new CopyOnWriteArrayList<PointBeanMergeRequestListener>();
            requestListenersMap.put(objectBean, listeners);
        }
        listeners.add(listener);
    }

    /**
     * DOCUMENT ME!
     *
     * @param  objectBean  pointBean DOCUMENT ME!
     * @param  listener    DOCUMENT ME!
     */
    public void removeRequestListener(final CidsBean objectBean, final PointBeanMergeRequestListener listener) {
        final Collection<PointBeanMergeRequestListener> listeners = requestListenersMap.get(objectBean);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
                requestListenersMap.remove(objectBean);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   pointBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<PointBeanMergeListener> getListenersOfPointBean(final CidsBean pointBean) {
        final Collection<PointBeanMergeListener> listeners = listenersMap.get(pointBean);
        if (listeners == null) {
            return new CopyOnWriteArrayList<PointBeanMergeListener>();
        } else {
            return listeners;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param   object  pointBean DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    private Collection<PointBeanMergeRequestListener> getListenersOfObjectBean(final CidsBean object) {
        final Collection<PointBeanMergeRequestListener> listeners = requestListenersMap.get(object);
        if (listeners == null) {
            return new CopyOnWriteArrayList<PointBeanMergeRequestListener>();
        } else {
            return listeners;
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  oldBean  DOCUMENT ME!
     * @param  newBean  DOCUMENT ME!
     */
    public void firePointBeanMerged(final CidsBean oldBean, final CidsBean newBean) {
        for (final PointBeanMergeListener listener : getListenersOfPointBean(oldBean)) {
            listener.pointBeanMerged(newBean);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  object     oldBean DOCUMENT ME!
     * @param  fromPoint  DOCUMENT ME!
     * @param  newBean    DOCUMENT ME!
     */
    public void firePointBeanMergeRequest(final CidsBean object, final boolean fromPoint, final CidsBean newBean) {
        for (final PointBeanMergeRequestListener requestListener : getListenersOfObjectBean(object)) {
            requestListener.pointBeanMergeRequest(fromPoint, newBean);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  object     oldBean DOCUMENT ME!
     * @param  fromPoint  newBean DOCUMENT ME!
     */
    public void firePointBeanSplitRequest(final CidsBean object, final boolean fromPoint) {
        for (final PointBeanMergeRequestListener requestListener : getListenersOfObjectBean(object)) {
            requestListener.pointBeanSplitRequest(fromPoint);
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param  pointBean  DOCUMENT ME!
     */
    public void firePointBeanSplitted(final CidsBean pointBean) {
        for (final PointBeanMergeListener listener : getListenersOfPointBean(pointBean)) {
            listener.pointBeanSplitted();
        }
    }
}
