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

import Sirius.server.middleware.types.MetaClass;

import com.vividsolutions.jts.geom.Geometry;

import de.cismet.cids.dynamics.CidsBean;

/**
 * DOCUMENT ME!
 *
 * @author   therter
 * @version  $Revision$, $Date$
 */
public interface LinearReferencingHelper extends LinearReferencingSingletonInstances {

    //~ Methods ----------------------------------------------------------------

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean createLineBeanFromRouteBean(final CidsBean routeBean);

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean createStationBeanFromRouteBean(final CidsBean routeBean);

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean  DOCUMENT ME!
     * @param   value      DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean createStationBeanFromRouteBean(final CidsBean routeBean, final double value);

    /**
     * DOCUMENT ME!
     *
     * @param   cidsBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double distanceOfStationGeomToRouteGeomFromStationBean(final CidsBean cidsBean);

    /**
     * DOCUMENT ME!
     *
     * @param   lineBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean getGeomBeanFromLineBean(final CidsBean lineBean);

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    double getLinearValueFromStationBean(final CidsBean stationBean);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getNewLineId();

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    int getNewStationId();

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Geometry getPointGeometryFromStationBean(final CidsBean stationBean);

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean getRouteBeanFromStationBean(final CidsBean stationBean);

    /**
     * DOCUMENT ME!
     *
     * @param   routeBean    DOCUMENT ME!
     * @param   stationBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void setRouteBeanToStationBean(final CidsBean routeBean, final CidsBean stationBean) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Geometry getRouteGeometryFromStationBean(final CidsBean stationBean);

    /**
     * DOCUMENT ME!
     *
     * @param   stationBean  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getRouteNameFromStationBean(final CidsBean stationBean);

    /**
     * DOCUMENT ME!
     *
     * @param   lineBean  DOCUMENT ME!
     * @param   isFrom    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean getStationBeanFromLineBean(final CidsBean lineBean, final boolean isFrom);

    /**
     * DOCUMENT ME!
     *
     * @param   line      DOCUMENT ME!
     * @param   lineBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void setGeometryToLineBean(final Geometry line, final CidsBean lineBean) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   value        DOCUMENT ME!
     * @param   stationBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void setLinearValueToStationBean(final Double value, final CidsBean stationBean) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   point        DOCUMENT ME!
     * @param   stationBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void setPointGeometryToStationBean(final Geometry point, final CidsBean stationBean) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   geometry     DOCUMENT ME!
     * @param   stationBean  DOCUMENT ME!
     *
     * @throws  Exception  DOCUMENT ME!
     */
    void setRouteGeometryToStationBean(final Geometry geometry, final CidsBean stationBean) throws Exception;

    /**
     * DOCUMENT ME!
     *
     * @param   station  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getValueProperty(CidsBean station);

    /**
     * DOCUMENT ME!
     *
     * @param   route  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String getRouteNamePropertyFromRouteByClassName(String route);

    /**
     * DOCUMENT ME!
     *
     * @param   fromStation  DOCUMENT ME!
     * @param   toStation    DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    CidsBean createLineBeanFromStationBean(CidsBean fromStation, CidsBean toStation);

    /**
     * DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    String[] getAllUsedDomains();

    /**
     * DOCUMENT ME!
     *
     * @param   route  DOCUMENT ME!
     *
     * @return  DOCUMENT ME!
     */
    Geometry getGeometryFromRoute(CidsBean route);
}
